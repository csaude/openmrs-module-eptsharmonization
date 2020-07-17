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
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsharmonization.api.HarmonizationPersonAttributeTypeService;
import org.openmrs.module.eptsharmonization.api.exception.UUIDDuplicationException;
import org.openmrs.module.eptsharmonization.api.model.PersonAttributeTypeDTO;
import org.openmrs.module.eptsharmonization.web.EptsHarmonizationConstants;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationData;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationItem;
import org.openmrs.module.eptsharmonization.web.bean.PersonAttributeTypesHarmonizationCSVLog;
import org.openmrs.module.eptsharmonization.web.bean.PersonAttributeTypesHarmonizationCSVLog.Builder;
import org.openmrs.module.eptsharmonization.web.delegate.HarmonizePersonAttributeTypeDelegate;
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

@Controller("eptsharmonization.harmonizePersonAttributeTypesController")
@SessionAttributes({"differentIDsAndEqualUUID", "differentNameAndSameUUIDAndID"})
public class HarmonizePersonAttributeTypesController {

  public static final String CONTROLLER_PATH =
      EptsHarmonizationConstants.MODULE_PATH + "/personAttributeTypes";

  public static final String PERSON_ATTRIBUTE_TYPES_LIST =
      HarmonizePersonAttributeTypesController.CONTROLLER_PATH
          + "/harmonizePersonAttributeTypesList";

  public static final String ADD_PERSON_ATTRIBUTE_TYPE_MAPPING =
      HarmonizePersonAttributeTypesController.CONTROLLER_PATH + "/addPersonAttributeTypeMapping";

  public static final String ADD_PERSON_ATTRIBUTE_TYPE_FROM_MDS_MAPPING =
      HarmonizePersonAttributeTypesController.CONTROLLER_PATH
          + "/addPersonAttributeTypeFromMDSMapping";

  public static final String REMOVE_PERSON_ATTRIBUTE_TYPE_MAPPING =
      HarmonizePersonAttributeTypesController.CONTROLLER_PATH + "/removePersonAttributeTypeMapping";

  public static final String PROCESS_HARMONIZATION_STEP1 =
      HarmonizePersonAttributeTypesController.CONTROLLER_PATH + "/processHarmonizationStep1";

  public static final String PROCESS_HARMONIZATION_STEP2 =
      HarmonizePersonAttributeTypesController.CONTROLLER_PATH + "/processHarmonizationStep2";

  public static final String PROCESS_HARMONIZATION_STEP3 =
      HarmonizePersonAttributeTypesController.CONTROLLER_PATH + "/processHarmonizationStep3";

  public static final String PROCESS_HARMONIZATION_STEP4 =
      HarmonizePersonAttributeTypesController.CONTROLLER_PATH + "/processHarmonizationStep4";

  public static final String EXPORT_PERSON_ATTRIBUTE_TYPES =
      HarmonizePersonAttributeTypesController.CONTROLLER_PATH
          + "/harmonizeExportPersonAttributeTypes";

  public static final String EXPORT_LOG =
      HarmonizePersonAttributeTypesController.CONTROLLER_PATH
          + "/harmonizePersonAttributeTypesListExportLog";

  public static boolean HAS_ATLEAST_ONE_ROW_HARMONIZED = false;

  public static boolean IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = false;

  public static boolean IS_NAMES_DIFFERENCES_HARMONIZED = false;

  private HarmonizationPersonAttributeTypeService harmonizationPersonAttributeTypeService;

  private HarmonizePersonAttributeTypeDelegate delegate;

  @Autowired
  public void setHarmonizationPersonAttributeTypeService(
      HarmonizationPersonAttributeTypeService harmonizationPersonAttributeTypeService) {
    this.harmonizationPersonAttributeTypeService = harmonizationPersonAttributeTypeService;
  }

  @Autowired
  public void setDelegate(HarmonizePersonAttributeTypeDelegate delegate) {
    this.delegate = delegate;
  }

  @RequestMapping(value = PERSON_ATTRIBUTE_TYPES_LIST, method = RequestMethod.GET)
  public ModelAndView getHarmonizationScenariousData(
      HttpSession session,
      Model model,
      @ModelAttribute("newMDSPersonAttributeTypes") HarmonizationData newMDSPersonAttributeTypes,
      @ModelAttribute("productionItemsToDelete")
          List<PersonAttributeTypeDTO> productionItemsToDelete,
      @ModelAttribute("differentIDsAndEqualUUID") HarmonizationData differentIDsAndEqualUUID,
      @ModelAttribute("differentNameAndSameUUIDAndID")
          HarmonizationData differentNameAndSameUUIDAndID,
      @ModelAttribute("notSwappablePersonAttributeTypes")
          List<PersonAttributeType> notSwappablePersonAttributeTypes,
      @ModelAttribute("swappablePersonAttributeTypes")
          List<PersonAttributeType> swappablePersonAttributeTypes,
      @ModelAttribute("mdsPersonAttributeTypeNotHarmonizedYet")
          List<PersonAttributeType> mdsPersonAttributeTypeNotHarmonizedYet,
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg,
      @RequestParam(required = false, value = "errorRequiredMdsValue") String errorRequiredMdsValue,
      @RequestParam(required = false, value = "errorRequiredPDSValue") String errorRequiredPDSValue,
      @RequestParam(required = false, value = "errorRequiredMdsValueFromMDS")
          String errorRequiredMdsValueFromMDS,
      @RequestParam(required = false, value = "errorRequiredPDSValueFromMDS")
          String errorRequiredPDSValueFromMDS,
      @RequestParam(required = false, value = "errorProcessingManualMapping")
          String errorProcessingManualMapping) {

    // I did this fetch as a workaround to prevent having cached data
    newMDSPersonAttributeTypes = getNewMDSPersonAttributeTypes();
    differentIDsAndEqualUUID = this.getDifferentIDsAndEqualUUID();
    differentNameAndSameUUIDAndID = this.getDifferentNameAndSameUUIDAndID();
    HarmonizationData productionItemsToExport =
        delegate.getConvertedData(getProductionItemToExport());

    session.setAttribute(
        "harmonizedPersonAttributeTypesSummary",
        HarmonizePersonAttributeTypeDelegate.SUMMARY_EXECUTED_SCENARIOS);
    session.setAttribute("openmrs_msg", openmrs_msg);
    session.setAttribute("errorRequiredMdsValue", errorRequiredMdsValue);
    session.setAttribute("errorRequiredPDSValue", errorRequiredPDSValue);
    session.setAttribute("errorRequiredMdsValueFromMDS", errorRequiredMdsValueFromMDS);
    session.setAttribute("errorRequiredPDSValueFromMDS", errorRequiredPDSValueFromMDS);
    session.setAttribute("errorProcessingManualMapping", errorProcessingManualMapping);

    delegate.setHarmonizationStage(
        session,
        newMDSPersonAttributeTypes,
        productionItemsToDelete,
        productionItemsToExport,
        differentIDsAndEqualUUID,
        differentNameAndSameUUIDAndID,
        notSwappablePersonAttributeTypes,
        swappablePersonAttributeTypes);

    session.removeAttribute("productionItemsToExport");
    session.setAttribute("productionItemsToExport", productionItemsToExport);

    ModelAndView modelAndView = new ModelAndView();
    model.addAttribute("newMDSPersonAttributeTypes", newMDSPersonAttributeTypes);
    model.addAttribute("productionItemsToExport", productionItemsToExport);
    model.addAttribute("differentIDsAndEqualUUID", differentIDsAndEqualUUID);
    model.addAttribute("differentNameAndSameUUIDAndID", differentNameAndSameUUIDAndID);

    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP1, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep1(
      HttpSession session,
      @ModelAttribute("newMDSPersonAttributeTypes") HarmonizationData newMDSPersonAttributeTypes,
      @ModelAttribute("productionItemsToDelete")
          List<PersonAttributeTypeDTO> productionItemsToDelete) {

    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
    Builder logBuilder = new PersonAttributeTypesHarmonizationCSVLog.Builder(defaultLocationName);

    delegate.processAddNewFromMetadataServer(newMDSPersonAttributeTypes, logBuilder);
    delegate.processDeleteFromProductionServer(productionItemsToDelete, logBuilder);

    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    modelAndView.addObject("openmrs_msg", "eptsharmonization.personattributetype.harmonized");

    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP2, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep2(
      @ModelAttribute("differentIDsAndEqualUUID") HarmonizationData differentIDsAndEqualUUID) {

    HAS_ATLEAST_ONE_ROW_HARMONIZED = false;
    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
    Builder logBuilder = new PersonAttributeTypesHarmonizationCSVLog.Builder(defaultLocationName);
    delegate.processPersonAttributeTypesWithDiferrentIdsAndEqualUUID(
        differentIDsAndEqualUUID, logBuilder);
    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    if (HAS_ATLEAST_ONE_ROW_HARMONIZED) {
      modelAndView.addObject("openmrs_msg", "eptsharmonization.personattributetype.harmonized");
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
    Builder logBuilder = new PersonAttributeTypesHarmonizationCSVLog.Builder(defaultLocationName);
    delegate.processUpdatePersonAttributeTypesNames(differentNameAndSameUUIDAndID, logBuilder);
    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    if (HAS_ATLEAST_ONE_ROW_HARMONIZED) {
      modelAndView.addObject("openmrs_msg", "eptsharmonization.personattributetype.harmonized");
    }
    IS_NAMES_DIFFERENCES_HARMONIZED = true;
    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = PROCESS_HARMONIZATION_STEP4, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep4(
      HttpSession session,
      HttpServletRequest request,
      @ModelAttribute("swappablePersonAttributeTypes")
          List<PersonAttributeType> swappablePersonAttributeTypes,
      @ModelAttribute("mdsPersonAttributeTypeNotHarmonizedYet")
          List<PersonAttributeType> mdsPersonAttributeTypeNotHarmonizedYet)
      throws Exception {

    Map<PersonAttributeType, PersonAttributeType> manualHarmonizePersonAttributeTypes =
        (Map<PersonAttributeType, PersonAttributeType>)
            session.getAttribute("manualHarmonizePersonAttributeTypes");

    ModelAndView modelAndView = getRedirectModelAndView();

    if (manualHarmonizePersonAttributeTypes != null
        && !manualHarmonizePersonAttributeTypes.isEmpty()) {

      try {
        this.harmonizationPersonAttributeTypeService.saveManualMapping(
            manualHarmonizePersonAttributeTypes);
      } catch (UUIDDuplicationException e) {

        for (Entry<PersonAttributeType, PersonAttributeType> entry :
            manualHarmonizePersonAttributeTypes.entrySet()) {
          if (!swappablePersonAttributeTypes.contains(entry.getKey())) {
            swappablePersonAttributeTypes.add(entry.getKey());
          }
          if (!mdsPersonAttributeTypeNotHarmonizedYet.contains(entry.getKey())) {
            mdsPersonAttributeTypeNotHarmonizedYet.add(entry.getValue());
          }
        }

        modelAndView.addObject("errorProcessingManualMapping", e.getMessage());
        return modelAndView;
      } catch (SQLException e) {
        e.printStackTrace();
        throw new Exception(e);
      }

      String defaultLocationName =
          Context.getAdministrationService().getGlobalProperty("default_location");
      Builder logBuilder = new PersonAttributeTypesHarmonizationCSVLog.Builder(defaultLocationName);
      logBuilder.appendNewMappedPersonAttributeTypes(manualHarmonizePersonAttributeTypes);
      HarmonizePersonAttributeTypeDelegate.SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.encounterType.newDefinedMapping");
    }
    modelAndView.addObject("openmrs_msg", "eptsharmonization.personattributetype.harmonized");

    HarmonizePersonAttributeTypeDelegate.EXECUTED_PERSONATTRIBUTETYPES_MANUALLY_CACHE =
        new ArrayList<>(manualHarmonizePersonAttributeTypes.keySet());
    session.removeAttribute("manualHarmonizePersonAttributeTypes");

    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = ADD_PERSON_ATTRIBUTE_TYPE_MAPPING, method = RequestMethod.POST)
  public ModelAndView addPersonAttributeTypeMapping(
      HttpSession session,
      @ModelAttribute("swappablePersonAttributeTypes")
          List<PersonAttributeType> swappablePersonAttributeTypes,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem) {

    ModelAndView modelAndView = this.getRedirectModelAndView();

    if (harmonizationItem.getKey() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getKey()))) {
      modelAndView.addObject(
          "errorRequiredPDSValue",
          "eptsharmonization.error.personAttributeTypeForMapping.required");
      return modelAndView;
    }
    if (harmonizationItem.getValue() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getValue()))) {
      modelAndView.addObject(
          "errorRequiredMdsValue",
          "eptsharmonization.error.personAttributeTypeForMapping.required");
      return modelAndView;
    }

    PersonAttributeType pdsPersonAttributeType =
        this.harmonizationPersonAttributeTypeService.findProductionPersonAttributeTypeByUuid(
            (String) harmonizationItem.getKey());
    PersonAttributeType mdsPersonAttributeType =
        this.harmonizationPersonAttributeTypeService.findMetadataPersonAttributeTypeByUuid(
            (String) harmonizationItem.getValue());

    Map<PersonAttributeType, PersonAttributeType> manualHarmonizePersonAttributeTypes =
        (Map<PersonAttributeType, PersonAttributeType>)
            session.getAttribute("manualHarmonizePersonAttributeTypes");

    if (manualHarmonizePersonAttributeTypes == null) {
      manualHarmonizePersonAttributeTypes = new HashMap<>();
    }
    swappablePersonAttributeTypes.remove(pdsPersonAttributeType);
    manualHarmonizePersonAttributeTypes.put(pdsPersonAttributeType, mdsPersonAttributeType);
    session.setAttribute(
        "manualHarmonizePersonAttributeTypes", manualHarmonizePersonAttributeTypes);

    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = ADD_PERSON_ATTRIBUTE_TYPE_FROM_MDS_MAPPING, method = RequestMethod.POST)
  public ModelAndView addPersonAttributeTypeFromMDSMapping(
      HttpSession session,
      @ModelAttribute("swappablePersonAttributeTypes")
          List<PersonAttributeType> swappablePersonAttributeTypes,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem,
      @ModelAttribute("mdsPersonAttributeTypeNotHarmonizedYet")
          List<PersonAttributeType> mdsPersonAttributeTypeNotHarmonizedYet) {

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

    PersonAttributeType pdsPersonAttributeType =
        Context.getPersonService()
            .getPersonAttributeTypeByUuid((String) harmonizationItem.getKey());

    String mdsPATUuid = (String) harmonizationItem.getValue();
    PersonAttributeType mdsPersonAttributeType = null;
    for (PersonAttributeType personAttributeType : mdsPersonAttributeTypeNotHarmonizedYet) {
      if (mdsPATUuid.equals(personAttributeType.getUuid())) {
        mdsPersonAttributeType = personAttributeType;
        break;
      }
    }

    Map<PersonAttributeType, PersonAttributeType> manualHarmonizePersonAttributeTypes =
        (Map<PersonAttributeType, PersonAttributeType>)
            session.getAttribute("manualHarmonizePersonAttributeTypes");

    if (manualHarmonizePersonAttributeTypes == null) {
      manualHarmonizePersonAttributeTypes = new HashMap<>();
    }
    swappablePersonAttributeTypes.remove(pdsPersonAttributeType);
    manualHarmonizePersonAttributeTypes.put(pdsPersonAttributeType, mdsPersonAttributeType);
    session.setAttribute(
        "manualHarmonizePersonAttributeTypes", manualHarmonizePersonAttributeTypes);

    if (mdsPersonAttributeTypeNotHarmonizedYet != null
        && mdsPersonAttributeTypeNotHarmonizedYet.contains(mdsPersonAttributeType)) {
      mdsPersonAttributeTypeNotHarmonizedYet.remove(mdsPersonAttributeType);
    }

    return modelAndView;
  }

  @RequestMapping(value = REMOVE_PERSON_ATTRIBUTE_TYPE_MAPPING, method = RequestMethod.POST)
  public ModelAndView removePersonAttributeTypeMapping(
      HttpSession session,
      @ModelAttribute("swappablePersonAttributeTypes")
          List<PersonAttributeType> swappablePersonAttributeTypes,
      @ModelAttribute("notSwappablePersonAttributeTypes")
          List<PersonAttributeType> notSwappablePersonAttributeTypes,
      @ModelAttribute("mdsPersonAttributeTypeNotHarmonizedYet")
          List<PersonAttributeType> mdsPersonAttributeTypeNotHarmonizedYet,
      HttpServletRequest request) {

    PersonAttributeType productionPersonAttributeType =
        this.harmonizationPersonAttributeTypeService.findMetadataPersonAttributeTypeByUuid(
            request.getParameter("productionServerPersonAttributeTypeUuID"));

    @SuppressWarnings("unchecked")
    Map<PersonAttributeType, PersonAttributeType> manualHarmonizePersonAttributeTypes =
        (Map<PersonAttributeType, PersonAttributeType>)
            session.getAttribute("manualHarmonizePersonAttributeTypes");

    PersonAttributeType mdsPersonAttributeType =
        manualHarmonizePersonAttributeTypes.get(productionPersonAttributeType);
    manualHarmonizePersonAttributeTypes.remove(productionPersonAttributeType);
    swappablePersonAttributeTypes.add(productionPersonAttributeType);

    if (notSwappablePersonAttributeTypes != null
        && !notSwappablePersonAttributeTypes.contains(mdsPersonAttributeType)) {
      if (mdsPersonAttributeTypeNotHarmonizedYet != null
          && !mdsPersonAttributeTypeNotHarmonizedYet.contains(mdsPersonAttributeType)) {
        mdsPersonAttributeTypeNotHarmonizedYet.add(mdsPersonAttributeType);
      }
    }

    if (mdsPersonAttributeTypeNotHarmonizedYet != null) {
      this.sortByName(mdsPersonAttributeTypeNotHarmonizedYet);
    }
    if (swappablePersonAttributeTypes != null) {
      this.sortByName(swappablePersonAttributeTypes);
    }

    if (manualHarmonizePersonAttributeTypes.isEmpty()) {
      session.removeAttribute("manualHarmonizePersonAttributeTypes");
    }
    return this.getRedirectModelAndView();
  }

  @RequestMapping(value = EXPORT_LOG, method = RequestMethod.POST)
  public @ResponseBody byte[] exportLog(HttpServletRequest request, HttpServletResponse response)
      throws FileNotFoundException {

    File file = new File("harmonizationPersonAttributeTypesLog");
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
        "attachment; fileName=person_attribute_types_harmonization_"
            + defaultLocationName
            + "-log.csv");
    response.setContentLength(outputStream.size());
    return outputStream.toByteArray();
  }

  @RequestMapping(value = EXPORT_PERSON_ATTRIBUTE_TYPES, method = RequestMethod.POST)
  public @ResponseBody byte[] exportPersonAttributeTypes(
      HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model)
      throws FileNotFoundException {

    HarmonizationData productionItemsToExport =
        (HarmonizationData) session.getAttribute("productionItemsToExport");
    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");

    List<PersonAttributeTypeDTO> list = new ArrayList<>();
    for (HarmonizationItem item : productionItemsToExport.getItems()) {
      list.add((PersonAttributeTypeDTO) item.getValue());
    }

    ByteArrayOutputStream outputStream =
        PersonAttributeTypesHarmonizationCSVLog.exportPersonAttributeTypeLogs(
            defaultLocationName, list, getNotSwappablePersonAttributeTypes());
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName=person_Attribute_types_harmonization_"
            + defaultLocationName
            + "-export-log.csv");
    response.setContentLength(outputStream.size());
    return outputStream.toByteArray();
  }

  @ModelAttribute("productionItemsToDelete")
  public List<PersonAttributeTypeDTO> getProductionItemsToDelete() {
    List<PersonAttributeTypeDTO> productionItemsToDelete = new ArrayList<>();
    List<PersonAttributeTypeDTO> onlyProductionPersonAttributeTypes =
        this.harmonizationPersonAttributeTypeService
            .findAllProductionPersonAttributeTypesNotContainedInMetadataServer();
    for (PersonAttributeTypeDTO personAttributeTypeDTO : onlyProductionPersonAttributeTypes) {
      final int numberOfAffectedPersonAttributes =
          this.harmonizationPersonAttributeTypeService.getNumberOfAffectedPersonAttributes(
              personAttributeTypeDTO);
      if (numberOfAffectedPersonAttributes == 0) {
        productionItemsToDelete.add(personAttributeTypeDTO);
      }
    }
    return productionItemsToDelete;
  }

  private List<PersonAttributeTypeDTO> getProductionItemToExport() {
    List<PersonAttributeTypeDTO> onlyProductionPersonAttributeTypes =
        this.harmonizationPersonAttributeTypeService
            .findAllProductionPersonAttributeTypesNotContainedInMetadataServer();
    List<PersonAttributeTypeDTO> productionItemsToExport = new ArrayList<>();
    for (PersonAttributeTypeDTO personAttributeTypeDTO : onlyProductionPersonAttributeTypes) {
      final int numberOfAffectedPersonAttributes =
          this.harmonizationPersonAttributeTypeService.getNumberOfAffectedPersonAttributes(
              personAttributeTypeDTO);
      if (numberOfAffectedPersonAttributes > 0) {
        productionItemsToExport.add(personAttributeTypeDTO);
      }
    }
    return productionItemsToExport;
  }

  @ModelAttribute("harmonizationItem")
  HarmonizationItem formHarmonizationItem() {
    return new HarmonizationItem();
  }

  @ModelAttribute("newMDSPersonAttributeTypes")
  public HarmonizationData getNewMDSPersonAttributeTypes() {
    List<PersonAttributeTypeDTO> data =
        this.harmonizationPersonAttributeTypeService
            .findAllMetadataPersonAttributeTypesNotContainedInProductionServer();
    return delegate.getConvertedData(data);
  }

  @ModelAttribute("differentIDsAndEqualUUID")
  public HarmonizationData getDifferentIDsAndEqualUUID() {
    Map<String, List<PersonAttributeTypeDTO>> personAttributeTypesWithDifferentIDsSameUUIDs =
        this.harmonizationPersonAttributeTypeService
            .findAllPersonAttributeTypesWithDifferentIDAndSameUUID();
    return delegate.getConvertedData(personAttributeTypesWithDifferentIDsSameUUIDs);
  }

  @ModelAttribute("differentNameAndSameUUIDAndID")
  public HarmonizationData getDifferentNameAndSameUUIDAndID() {
    Map<String, List<PersonAttributeTypeDTO>> personAttributeTypesWithDifferentNames =
        this.harmonizationPersonAttributeTypeService
            .findAllPersonAttributeTypesWithDifferentNameAndSameUUIDAndID();
    return delegate.getConvertedData(personAttributeTypesWithDifferentNames);
  }

  @ModelAttribute("swappablePersonAttributeTypes")
  public List<PersonAttributeType> getSwappablePersonAttributeTypes() {
    return sortByName(
        harmonizationPersonAttributeTypeService.findAllSwappablePersonAttributeTypes());
  }

  @ModelAttribute("notSwappablePersonAttributeTypes")
  public List<PersonAttributeType> getNotSwappablePersonAttributeTypes() {
    return sortByName(
        this.harmonizationPersonAttributeTypeService.findAllNotSwappablePersonAttributeTypes());
  }

  @ModelAttribute("mdsPersonAttributeTypeNotHarmonizedYet")
  public List<PersonAttributeType> getMDSPersonAttributeTypeNotHarmonizedYet() {
    return this.sortByName(this.delegate.getMDSNotHarmonizedYet());
  }

  @SuppressWarnings("unchecked")
  private List<PersonAttributeType> sortByName(List<PersonAttributeType> list) {
    BeanComparator comparator = new BeanComparator("name");
    Collections.sort(list, comparator);
    return list;
  }

  private ModelAndView getRedirectModelAndView() {
    return new ModelAndView("redirect:" + PERSON_ATTRIBUTE_TYPES_LIST + ".form");
  }
}
