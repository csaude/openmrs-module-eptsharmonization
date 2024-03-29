package org.openmrs.module.eptsharmonization.web.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsharmonization.api.HarmonizationPatientIdentifierTypeService;
import org.openmrs.module.eptsharmonization.api.exception.UUIDDuplicationException;
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

  public static final String ADD_PATIENT_IDENTIFIER_TYPE_FROM_MDS_MAPPING =
      HarmonizePatientIdentifierTypesController.CONTROLLER_PATH
          + "/addPatientIdentifierTypeFromMDSMapping";

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

    Builder logBuilder =
        new PatientIdentifierTypesHarmonizationCSVLog.Builder(this.getDefaultLocation());

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
    Builder logBuilder =
        new PatientIdentifierTypesHarmonizationCSVLog.Builder(this.getDefaultLocation());
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
    Builder logBuilder =
        new PatientIdentifierTypesHarmonizationCSVLog.Builder(this.getDefaultLocation());
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
  public ModelAndView processHarmonizationStep4(
      HttpSession session,
      HttpServletRequest request,
      @ModelAttribute("swappablePatientIdentifierTypes")
          List<PatientIdentifierType> swappablePatientIdentifierTypes,
      @ModelAttribute("mdsPatientIdentifierTypeNotHarmonizedYet")
          List<PatientIdentifierType> mdsPatientIdentifierTypeNotHarmonizedYet)
      throws Exception {

    Map<PatientIdentifierType, PatientIdentifierType> manualHarmonizePatientIdentifierTypes =
        (Map<PatientIdentifierType, PatientIdentifierType>)
            session.getAttribute("manualHarmonizePatientIdentifierTypes");

    ModelAndView modelAndView = getRedirectModelAndView();
    if (manualHarmonizePatientIdentifierTypes != null
        && !manualHarmonizePatientIdentifierTypes.isEmpty()) {

      try {

        Builder logBuilder =
            new PatientIdentifierTypesHarmonizationCSVLog.Builder(this.getDefaultLocation());

        delegate.processManualMapping(manualHarmonizePatientIdentifierTypes, logBuilder);
        HarmonizePatientIdentifierTypesDelegate.SUMMARY_EXECUTED_SCENARIOS.add(
            "eptsharmonization.encounterType.newDefinedMapping");

      } catch (UUIDDuplicationException e) {

        for (Entry<PatientIdentifierType, PatientIdentifierType> entry :
            manualHarmonizePatientIdentifierTypes.entrySet()) {
          if (!swappablePatientIdentifierTypes.contains(entry.getKey())) {
            swappablePatientIdentifierTypes.add(entry.getKey());
          }
          if (!mdsPatientIdentifierTypeNotHarmonizedYet.contains(entry.getKey())) {
            mdsPatientIdentifierTypeNotHarmonizedYet.add(entry.getValue());
          }
        }

        modelAndView.addObject("errorProcessingManualMapping", e.getMessage());
        return modelAndView;
      } catch (SQLException e) {
        e.printStackTrace();
        throw new Exception(e);
      }
    }

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

    if (harmonizationItem.getValue() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getValue()))) {
      modelAndView.addObject(
          "errorRequiredMdsValue", "eptsharmonization.error.encounterForMapping.required");
      return modelAndView;
    }

    if (harmonizationItem.getKey() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getKey()))) {
      modelAndView.addObject(
          "errorRequiredPDSValue", "eptsharmonization.error.encounterForMapping.required");
      return modelAndView;
    }

    PatientIdentifierType pdsPatientIdentifierType =
        this.harmonizationPatientIdentifierTypeService.findPDSPatientIdentifierTypeByUuid(
            (String) harmonizationItem.getKey());
    PatientIdentifierType mdsPatientIdentifierType =
        this.harmonizationPatientIdentifierTypeService.findPDSPatientIdentifierTypeByUuid(
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

  @SuppressWarnings("unchecked")
  @RequestMapping(value = ADD_PATIENT_IDENTIFIER_TYPE_FROM_MDS_MAPPING, method = RequestMethod.POST)
  public ModelAndView addPatientIdentifierTypeFromMDSMapping(
      HttpSession session,
      @ModelAttribute("swappablePatientIdentifierTypes")
          List<PatientIdentifierType> swappablePatientIdentifierTypes,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem,
      @ModelAttribute("mdsPatientIdentifierTypeNotHarmonizedYet")
          List<PatientIdentifierType> mdsPatientIdentifierTypeNotHarmonizedYet) {

    ModelAndView modelAndView = this.getRedirectModelAndView();

    if (harmonizationItem.getValue() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getValue()))) {
      modelAndView.addObject(
          "errorRequiredMdsValueFromMDS", "eptsharmonization.error.encounterForMapping.required");
      return modelAndView;
    }

    if (harmonizationItem.getKey() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getKey()))) {
      modelAndView.addObject(
          "errorRequiredPDSValueFromMDS", "eptsharmonization.error.encounterForMapping.required");
      return modelAndView;
    }

    PatientIdentifierType pdsPatientIdentifierType =
        this.harmonizationPatientIdentifierTypeService.findPDSPatientIdentifierTypeByUuid(
            (String) harmonizationItem.getKey());

    PatientIdentifierType mdsPatientIdentifierType =
        this.harmonizationPatientIdentifierTypeService.findMDSPatientIdentifierTypeByUuid(
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

    if (mdsPatientIdentifierTypeNotHarmonizedYet != null) {
      boolean mdsIsPresent = false;
      for (PatientIdentifierType patientIdentifierType : mdsPatientIdentifierTypeNotHarmonizedYet) {
        if (patientIdentifierType.getUuid().contentEquals(mdsPatientIdentifierType.getUuid())
            && patientIdentifierType.getName().equals(mdsPatientIdentifierType.getName())
            && patientIdentifierType.getId().equals(mdsPatientIdentifierType.getId())) {
          mdsIsPresent = true;
          break;
        }
      }
      if (mdsIsPresent) {
        mdsPatientIdentifierTypeNotHarmonizedYet.remove(mdsPatientIdentifierType);
      }
    }
    return modelAndView;
  }

  @RequestMapping(value = REMOVE_PATIENT_IDENTIFIER_TYPE_MAPPING, method = RequestMethod.POST)
  public ModelAndView removePatientIdentifierTypeMapping(
      HttpSession session,
      @ModelAttribute("swappablePatientIdentifierTypes")
          List<PatientIdentifierType> swappablePatientIdentifierTypes,
      @ModelAttribute("notSwappablePatientIdentifierTypes")
          List<PatientIdentifierType> notSwappablePatientIdentifierTypes,
      @ModelAttribute("mdsPatientIdentifierTypeNotHarmonizedYet")
          List<PatientIdentifierType> mdsPatientIdentifierTypeNotHarmonizedYet,
      HttpServletRequest request) {

    PatientIdentifierType productionPatientIdentifierType =
        this.harmonizationPatientIdentifierTypeService.findPDSPatientIdentifierTypeByUuid(
            request.getParameter("productionServerPatientIdentifierTypeUuID"));

    @SuppressWarnings("unchecked")
    Map<PatientIdentifierType, PatientIdentifierType> manualHarmonizePatientIdentifierTypes =
        (Map<PatientIdentifierType, PatientIdentifierType>)
            session.getAttribute("manualHarmonizePatientIdentifierTypes");

    PatientIdentifierType mdsPatientIdentifierType =
        manualHarmonizePatientIdentifierTypes.get(productionPatientIdentifierType);
    manualHarmonizePatientIdentifierTypes.remove(productionPatientIdentifierType);
    swappablePatientIdentifierTypes.add(productionPatientIdentifierType);

    if (notSwappablePatientIdentifierTypes != null) {

      boolean mdsIsPresent = false;
      for (PatientIdentifierType patientIdentifierType : notSwappablePatientIdentifierTypes) {
        if (patientIdentifierType.getUuid().contentEquals(mdsPatientIdentifierType.getUuid())
            && patientIdentifierType.getName().equals(mdsPatientIdentifierType.getName())
            && patientIdentifierType.getId().equals(mdsPatientIdentifierType.getId())) {
          mdsIsPresent = true;
          break;
        }
      }
      if (!mdsIsPresent
          && mdsPatientIdentifierTypeNotHarmonizedYet != null
          && !mdsPatientIdentifierTypeNotHarmonizedYet.contains(mdsPatientIdentifierType)) {
        mdsPatientIdentifierTypeNotHarmonizedYet.add(mdsPatientIdentifierType);
      }
    }

    if (mdsPatientIdentifierTypeNotHarmonizedYet != null) {
      this.sortByName(mdsPatientIdentifierTypeNotHarmonizedYet);
    }
    if (swappablePatientIdentifierTypes != null) {
      this.sortByName(swappablePatientIdentifierTypes);
    }

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
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName="
            + this.getFormattedLocationName(this.getDefaultLocation())
            + "-patient_identifier_types_harmonization-log.csv");
    response.setContentLength(outputStream.size());
    return outputStream.toByteArray();
  }

  @RequestMapping(value = EXPORT_PATIENT_IDENTIFIER_TYPES, method = RequestMethod.POST)
  public @ResponseBody byte[] exportPatientIdentifierTypes(
      HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model)
      throws FileNotFoundException {

    HarmonizationData productionItemsToExport =
        (HarmonizationData) session.getAttribute("productionItemsToExport");
    String defaultLocationName = this.getDefaultLocation();

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
        "attachment; fileName="
            + this.getFormattedLocationName(defaultLocationName)
            + "-patient_identifier_types_harmonization-export-log.csv");
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
    return sortByName(this.harmonizationPatientIdentifierTypeService.findAllSwappable());
  }

  @ModelAttribute("notSwappablePatientIdentifierTypes")
  public List<PatientIdentifierType> getNotSwappablePatientIdentifierTypes() {
    return sortByName(this.harmonizationPatientIdentifierTypeService.findAllNotSwappable());
  }

  private ModelAndView getRedirectModelAndView() {
    return new ModelAndView("redirect:" + PATIENT_IDENTIFIER_TYPES_LIST + ".form");
  }

  @ModelAttribute("mdsPatientIdentifierTypeNotHarmonizedYet")
  public List<PatientIdentifierType> getMDSPatientIdentifierTypeNotHarmonizedYet() {
    return this.sortByName(this.delegate.getMDSNotHarmonizedYet());
  }

  @SuppressWarnings("unchecked")
  private List<PatientIdentifierType> sortByName(List<PatientIdentifierType> list) {
    BeanComparator comparator = new BeanComparator("name");
    Collections.sort(list, comparator);
    return list;
  }

  private String getDefaultLocation() {
    return Context.getAdministrationService().getGlobalProperty("default_location");
  }

  private String getFormattedLocationName(String defaultLocationName) {
    if (defaultLocationName == null) {
      defaultLocationName = StringUtils.EMPTY;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(defaultLocationName.replaceAll(" ", "_"));
    return sb.toString();
  }
}
