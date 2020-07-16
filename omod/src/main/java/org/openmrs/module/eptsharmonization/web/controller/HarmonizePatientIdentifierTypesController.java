package org.openmrs.module.eptsharmonization.web.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationPatientIdentifierTypeService;
import org.openmrs.module.eptsharmonization.api.model.PatientIdentifierTypeDTO;
import org.openmrs.module.eptsharmonization.web.EptsHarmonizationConstants;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationData;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationItem;
import org.openmrs.module.eptsharmonization.web.bean.PatientIdentifierTypesHarmonizationCSVLog;
import org.openmrs.module.eptsharmonization.web.bean.PatientIdentifierTypesHarmonizationCSVLog.Builder;
import org.openmrs.module.eptsharmonization.web.delegate.HarmonizePatientIdentifierTypesDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

@Controller("eptsharmonization.harmonizePatientIdentifierTypesController")
@SessionAttributes({"differentIDsAndEqualUUID", "differentNameAndSameUUIDAndID"})
public class HarmonizePatientIdentifierTypesController {

  public static final String CONTROLLER_PATH =
      EptsHarmonizationConstants.MODULE_PATH + "/patientIdentifierTypes";

  public static final String PATIENT_IDENTIFIER_TYPES_LIST =
      HarmonizePatientIdentifierTypesController.CONTROLLER_PATH
          + "/harmonizePatientIdentifierTypesList";

  public static final String ADD_PATIENT_IDENTIFIER_TYPE_MAPPING =
      HarmonizePatientIdentifierTypesController.CONTROLLER_PATH
          + "/addPatientIdentifierTypeMapping";

  public static final String REMOVE_PATIENT_IDENTIFIER_TYPE_MAPPING =
      HarmonizePatientIdentifierTypesController.CONTROLLER_PATH
          + "/removePatientIdentifierTypeMapping";

  public static final String PROCESS_HARMONIZATION_STEP1 =
      HarmonizePatientIdentifierTypesController.CONTROLLER_PATH + "/processHarmonizationStep1";

  public static final String PROCESS_HARMONIZATION_STEP2 =
      HarmonizePatientIdentifierTypesController.CONTROLLER_PATH + "/processHarmonizationStep2";

  public static final String PROCESS_HARMONIZATION_STEP3 =
      HarmonizePatientIdentifierTypesController.CONTROLLER_PATH + "/processHarmonizationStep3";

  public static final String PROCESS_HARMONIZATION_STEP4 =
      HarmonizePatientIdentifierTypesController.CONTROLLER_PATH + "/processHarmonizationStep4";

  public static final String EXPORT_PATIENT_IDENTIFIER_TYPES =
      HarmonizePatientIdentifierTypesController.CONTROLLER_PATH
          + "/harmonizeExportPatientIdentifierTypes";

  public static final String EXPORT_LOG =
      HarmonizePatientIdentifierTypesController.CONTROLLER_PATH
          + "/harmonizePatientIdentifierTypesListExportLog";

  public static boolean HAS_ATLEAST_ONE_ROW_HARMONIZED = false;

  public static boolean IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = false;

  public static boolean IS_NAMES_DIFFERENCES_HARMONIZED = false;

  private HarmonizationPatientIdentifierTypeService harmonizationPatientIdentifierTypeService;

  private HarmonizePatientIdentifierTypesDelegate delegate;

  @Autowired
  public void setHarmonizationPatientIdentifierTypeService(
      HarmonizationPatientIdentifierTypeService harmonizationPatientIdentifierTypeService) {
    this.harmonizationPatientIdentifierTypeService = harmonizationPatientIdentifierTypeService;
  }

  @Autowired
  public void setDelegate(HarmonizePatientIdentifierTypesDelegate delegate) {
    this.delegate = delegate;
  }

  @RequestMapping(value = PATIENT_IDENTIFIER_TYPES_LIST, method = RequestMethod.GET)
  public ModelAndView getHarmonizationScenariousData(
      HttpSession session,
      Model model,
      @ModelAttribute("newMDSPatientIdentifierTypes")
          HarmonizationData newMDSPatientIdentifierTypes,
      @ModelAttribute("productionItemsToDelete")
          List<PatientIdentifierTypeDTO> productionItemsToDelete,
      @ModelAttribute("differentIDsAndEqualUUID") HarmonizationData differentIDsAndEqualUUID,
      @ModelAttribute("differentNameAndSameUUIDAndID")
          HarmonizationData differentNameAndSameUUIDAndID,
      @ModelAttribute("differentDetailsAndSameNameUUIDAndID")
          HarmonizationData differentDetailsAndSameNameUUIDAndID,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem,
      @ModelAttribute("notSwappablePatientIdentifierTypes")
          List<PatientIdentifierType> notSwappablePatientIdentifierTypes,
      @ModelAttribute("swappablePatientIdentifierTypes")
          List<PatientIdentifierType> swappablePatientIdentifierTypes,
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg,
      @RequestParam(required = false, value = "errorRequiredMdsValue") String errorRequiredMdsValue,
      @RequestParam(required = false, value = "errorRequiredPDSValue")
          String errorRequiredPDSValue) {

    // TODO: I did this fetch as a workaround to prevent having cached data
    newMDSPatientIdentifierTypes = getNewMDSPatientIdentifierTypes();
    differentIDsAndEqualUUID = this.getDifferentIDsAndEqualUUID();
    differentNameAndSameUUIDAndID = this.getDifferentNameAndSameUUIDAndID();
    differentDetailsAndSameNameUUIDAndID = this.getDifferentDetailsAndSameNameUUIDAndID();
    HarmonizationData productionItemsToExport =
        delegate.getConvertedData(getProductionItemToExport());

    session.setAttribute(
        "harmonizedPatientIdentifierTypesSummary",
        HarmonizePatientIdentifierTypesDelegate.SUMMARY_EXECUTED_SCENARIOS);
    session.setAttribute("openmrs_msg", openmrs_msg);
    session.setAttribute("errorRequiredMdsValue", errorRequiredMdsValue);
    session.setAttribute("errorRequiredPDSValue", errorRequiredPDSValue);

    delegate.setHarmonizationStage(
        session,
        newMDSPatientIdentifierTypes,
        productionItemsToDelete,
        productionItemsToExport,
        differentIDsAndEqualUUID,
        differentNameAndSameUUIDAndID,
        differentDetailsAndSameNameUUIDAndID,
        notSwappablePatientIdentifierTypes,
        swappablePatientIdentifierTypes);

    session.removeAttribute("productionItemsToExport");
    session.setAttribute("productionItemsToExport", productionItemsToExport);

    ModelAndView modelAndView = new ModelAndView();
    model.addAttribute("newMDSPatientIdentifierTypes", newMDSPatientIdentifierTypes);
    model.addAttribute("productionItemsToExport", productionItemsToExport);
    model.addAttribute("differentIDsAndEqualUUID", differentIDsAndEqualUUID);
    model.addAttribute("differentNameAndSameUUIDAndID", differentNameAndSameUUIDAndID);
    model.addAttribute(
        "differentDetailsAndSameNameUUIDAndID", differentDetailsAndSameNameUUIDAndID);

    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP1, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep1(
      HttpSession session,
      @ModelAttribute("newMDSPatientIdentifierTypes")
          HarmonizationData newMDSPatientIdentifierTypes,
      @ModelAttribute("productionItemsToDelete")
          List<PatientIdentifierTypeDTO> productionItemsToDelete,
      @ModelAttribute("differentDetailsAndSameNameUUIDAndID")
          HarmonizationData differentDetailsAndSameNameUUIDAndID) {

    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
    Builder logBuilder = new PatientIdentifierTypesHarmonizationCSVLog.Builder(defaultLocationName);

    delegate.processAddNewFromMetadataServer(newMDSPatientIdentifierTypes, logBuilder);
    delegate.processDeleteFromProductionServer(productionItemsToDelete, logBuilder);
    delegate.processUpdatePatientIdentifierTypesDetails(
        differentDetailsAndSameNameUUIDAndID, logBuilder);

    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    modelAndView.addObject("openmrs_msg", "eptsharmonization.patientidentifiertype.harmonized");

    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP2, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep2(
      @ModelAttribute("differentIDsAndEqualUUID") HarmonizationData differentIDsAndEqualUUID) {

    HAS_ATLEAST_ONE_ROW_HARMONIZED = false;
    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
    Builder logBuilder = new PatientIdentifierTypesHarmonizationCSVLog.Builder(defaultLocationName);
    delegate.processPatientIdentifierTypesWithDiferrentIdsAndEqualUUID(
        differentIDsAndEqualUUID, logBuilder);
    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    if (HAS_ATLEAST_ONE_ROW_HARMONIZED) {
      modelAndView.addObject("openmrs_msg", "eptsharmonization.patientidentifiertype.harmonized");
    }
    IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = true;
    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP3, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep3(
      @ModelAttribute("differentNameAndSameUUIDAndID")
          HarmonizationData differentNameAndSameUUIDAndID) {

    HAS_ATLEAST_ONE_ROW_HARMONIZED = false;
    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
    Builder logBuilder = new PatientIdentifierTypesHarmonizationCSVLog.Builder(defaultLocationName);
    delegate.processUpdatePatientIdentifierTypesNames(differentNameAndSameUUIDAndID, logBuilder);
    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    if (HAS_ATLEAST_ONE_ROW_HARMONIZED) {
      modelAndView.addObject("openmrs_msg", "eptsharmonization.patientidentifiertype.harmonized");
    }
    IS_NAMES_DIFFERENCES_HARMONIZED = true;
    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = PROCESS_HARMONIZATION_STEP4, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep4(HttpSession session, HttpServletRequest request) {

    Map<PatientIdentifierType, PatientIdentifierType> manualHarmonizePatientIdentifierTypes =
        (Map<PatientIdentifierType, PatientIdentifierType>)
            session.getAttribute("manualHarmonizePatientIdentifierTypes");

    if (manualHarmonizePatientIdentifierTypes != null
        && !manualHarmonizePatientIdentifierTypes.isEmpty()) {
      String defaultLocationName =
          Context.getAdministrationService().getGlobalProperty("default_location");
      Builder logBuilder =
          new PatientIdentifierTypesHarmonizationCSVLog.Builder(defaultLocationName);
      delegate.processManualMapping(manualHarmonizePatientIdentifierTypes, logBuilder);
      HarmonizePatientIdentifierTypesDelegate.SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.encounterType.newDefinedMapping");
    }
    ModelAndView modelAndView = getRedirectModelAndView();
    modelAndView.addObject("openmrs_msg", "eptsharmonization.patientidentifiertype.harmonized");

    HarmonizePatientIdentifierTypesDelegate.EXECUTED_PATIENT_IDENTIFIER_TYPES_MANUALLY_CACHE =
        new ArrayList<>(manualHarmonizePatientIdentifierTypes.keySet());
    session.removeAttribute("manualHarmonizePatientIdentifierTypes");

    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = ADD_PATIENT_IDENTIFIER_TYPE_MAPPING, method = RequestMethod.POST)
  public ModelAndView addPatientIdentifierTypeMapping(
      HttpSession session,
      @ModelAttribute("swappablePatientIdentifierTypes")
          List<PatientIdentifierType> swappablePatientIdentifierTypes,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem) {

    ModelAndView modelAndView = this.getRedirectModelAndView();

    if (harmonizationItem.getKey() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getKey()))) {
      modelAndView.addObject(
          "errorRequiredPDSValue",
          "eptsharmonization.error.patientIdentifierTypeForMapping.required");
      return modelAndView;
    }
    if (harmonizationItem.getValue() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getValue()))) {
      modelAndView.addObject(
          "errorRequiredMdsValue",
          "eptsharmonization.error.patientIdentifierTypeForMapping.required");
      return modelAndView;
    }

    PatientIdentifierType pdsPatientIdentifierType =
        this.harmonizationPatientIdentifierTypeService.findFromPDSByUuid(
            (String) harmonizationItem.getKey());
    PatientIdentifierType mdsPatientIdentifierType =
        this.harmonizationPatientIdentifierTypeService.findFromMDSByUuid(
            (String) harmonizationItem.getValue());

    Map<PatientIdentifierType, PatientIdentifierType> manualHarmonizePatientIdentifierTypes =
        (Map<PatientIdentifierType, PatientIdentifierType>)
            session.getAttribute("manualHarmonizePatientIdentifierTypes");

    if (manualHarmonizePatientIdentifierTypes == null) {
      manualHarmonizePatientIdentifierTypes = new HashMap<>();
    }
    swappablePatientIdentifierTypes.remove(pdsPatientIdentifierType);
    manualHarmonizePatientIdentifierTypes.put(pdsPatientIdentifierType, mdsPatientIdentifierType);
    session.setAttribute(
        "manualHarmonizePatientIdentifierTypes", manualHarmonizePatientIdentifierTypes);

    return modelAndView;
  }

  @RequestMapping(value = REMOVE_PATIENT_IDENTIFIER_TYPE_MAPPING, method = RequestMethod.POST)
  public ModelAndView removePatientIdentifierTypeMapping(
      HttpSession session,
      @ModelAttribute("swappablePatientIdentifierTypes")
          List<PatientIdentifierType> swappablePatientIdentifierTypes,
      HttpServletRequest request) {

    PatientIdentifierType productionPatientIdentifierType =
        this.harmonizationPatientIdentifierTypeService.findFromMDSByUuid(
            request.getParameter("productionServerPatientIdentifierTypeUuID"));

    @SuppressWarnings("unchecked")
    Map<PatientIdentifierType, PatientIdentifierType> manualHarmonizePatientIdentifierTypes =
        (Map<PatientIdentifierType, PatientIdentifierType>)
            session.getAttribute("manualHarmonizePatientIdentifierTypes");

    manualHarmonizePatientIdentifierTypes.remove(productionPatientIdentifierType);
    swappablePatientIdentifierTypes.add(productionPatientIdentifierType);

    if (manualHarmonizePatientIdentifierTypes.isEmpty()) {
      session.removeAttribute("manualHarmonizePatientIdentifierTypes");
    }
    return this.getRedirectModelAndView();
  }

  @RequestMapping(value = EXPORT_LOG, method = RequestMethod.POST)
  public @ResponseBody byte[] exportLog(HttpServletRequest request, HttpServletResponse response)
      throws FileNotFoundException {

    File file = new File("harmonizationPatientIdentifierTypesLog");
    FileInputStream fis = new FileInputStream(file);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    byte[] buf = new byte[1024];
    try {
      for (int readNum; (readNum = fis.read(buf)) != -1; ) {
        outputStream.write(buf, 0, readNum);
      }
    } catch (IOException ex) {

    }
    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName=patient_identifier_types_harmonization_"
            + defaultLocationName
            + "-log.csv");
    response.setContentLength(outputStream.size());
    return outputStream.toByteArray();
  }

  @RequestMapping(value = EXPORT_PATIENT_IDENTIFIER_TYPES, method = RequestMethod.POST)
  public @ResponseBody byte[] exportPatientIdentifierTypes(
      HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model)
      throws FileNotFoundException {

    HarmonizationData productionItemsToExport =
        (HarmonizationData) session.getAttribute("productionItemsToExport");
    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");

    List<PatientIdentifierTypeDTO> list = new ArrayList<>();
    for (HarmonizationItem item : productionItemsToExport.getItems()) {
      list.add((PatientIdentifierTypeDTO) item.getValue());
    }

    ByteArrayOutputStream outputStream =
        PatientIdentifierTypesHarmonizationCSVLog.exportPatientIdentifierTypeLogs(
            defaultLocationName, list, getNotSwappablePatientIdentifierTypes());
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName=patient_identifier_types_harmonization_"
            + defaultLocationName
            + "-export-log.csv");
    response.setContentLength(outputStream.size());
    return outputStream.toByteArray();
  }

  @ModelAttribute("productionItemsToDelete")
  public List<PatientIdentifierTypeDTO> getProductionItemsToDelete() {
    List<PatientIdentifierTypeDTO> productionItemsToDelete = new ArrayList<>();
    List<PatientIdentifierTypeDTO> onlyProductionPatientIdentifierTypes =
        this.harmonizationPatientIdentifierTypeService.findAllFromPDSNotContainedInMDS();
    for (PatientIdentifierTypeDTO patientIdentifierTypeDTO : onlyProductionPatientIdentifierTypes) {
      final int numberOfAffectedPatientIdentifiers =
          this.harmonizationPatientIdentifierTypeService.getNumberOfAffectedPatientIdentifiers(
              patientIdentifierTypeDTO);
      if (numberOfAffectedPatientIdentifiers == 0) {
        productionItemsToDelete.add(patientIdentifierTypeDTO);
      }
    }
    return productionItemsToDelete;
  }

  private List<PatientIdentifierTypeDTO> getProductionItemToExport() {
    List<PatientIdentifierTypeDTO> onlyProductionPatientIdentifierTypes =
        this.harmonizationPatientIdentifierTypeService.findAllFromPDSNotContainedInMDS();
    List<PatientIdentifierTypeDTO> productionItemsToExport = new ArrayList<>();
    for (PatientIdentifierTypeDTO patientIdentifierTypeDTO : onlyProductionPatientIdentifierTypes) {
      final int numberOfAffectedPatientIdentifiers =
          this.harmonizationPatientIdentifierTypeService.getNumberOfAffectedPatientIdentifiers(
              patientIdentifierTypeDTO);
      if (numberOfAffectedPatientIdentifiers > 0) {
        productionItemsToExport.add(patientIdentifierTypeDTO);
      }
    }
    return productionItemsToExport;
  }

  @ModelAttribute("harmonizationItem")
  HarmonizationItem formBackingObject() {
    return new HarmonizationItem();
  }

  @ModelAttribute("newMDSPatientIdentifierTypes")
  public HarmonizationData getNewMDSPatientIdentifierTypes() {
    List<PatientIdentifierTypeDTO> data =
        this.harmonizationPatientIdentifierTypeService.findAllFromMDSNotContainedInPDS();
    return delegate.getConvertedData(data);
  }

  @ModelAttribute("differentIDsAndEqualUUID")
  public HarmonizationData getDifferentIDsAndEqualUUID() {
    Map<String, List<PatientIdentifierTypeDTO>> patientIdentifierTypesWithDifferentIDsSameUUIDs =
        this.harmonizationPatientIdentifierTypeService.findAllWithDifferentIDAndSameUUID();
    return delegate.getConvertedData(patientIdentifierTypesWithDifferentIDsSameUUIDs);
  }

  @ModelAttribute("differentNameAndSameUUIDAndID")
  public HarmonizationData getDifferentNameAndSameUUIDAndID() {
    Map<String, List<PatientIdentifierTypeDTO>> patientIdentifierTypesWithDifferentNames =
        this.harmonizationPatientIdentifierTypeService.findAllWithDifferentNameAndSameUUIDAndID();
    return delegate.getConvertedData(patientIdentifierTypesWithDifferentNames);
  }

  @ModelAttribute("differentDetailsAndSameNameUUIDAndID")
  public HarmonizationData getDifferentDetailsAndSameNameUUIDAndID() {
    Map<String, List<PatientIdentifierTypeDTO>> patientIdentifierTypesWithDifferentNames =
        this.harmonizationPatientIdentifierTypeService
            .findAllWithDifferentDetailsAndSameNameUUIDAndID();
    return delegate.getConvertedData(patientIdentifierTypesWithDifferentNames);
  }

  @ModelAttribute("swappablePatientIdentifierTypes")
  public List<PatientIdentifierType> getSwappablePatientIdentifierTypes() {
    List<PatientIdentifierType> productionItemsToExport =
        DTOUtils.fromPatientIdentifierTypesDTOs(getProductionItemToExport());
    productionItemsToExport.addAll(
        HarmonizePatientIdentifierTypesDelegate.PATIENT_IDENTIFIER_TYPES_NOT_PROCESSED);
    return sortByName(productionItemsToExport);
  }

  @ModelAttribute("notSwappablePatientIdentifierTypes")
  public List<PatientIdentifierType> getNotSwappablePatientIdentifierTypes() {
    return sortByName(this.harmonizationPatientIdentifierTypeService.findAllFromMDS());
  }

  private ModelAndView getRedirectModelAndView() {
    return new ModelAndView("redirect:" + PATIENT_IDENTIFIER_TYPES_LIST + ".form");
  }

  @SuppressWarnings("unchecked")
  private List<PatientIdentifierType> sortByName(List<PatientIdentifierType> list) {
    BeanComparator comparator = new BeanComparator("name");
    Collections.sort(list, comparator);
    return list;
  }
}
