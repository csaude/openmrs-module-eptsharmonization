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
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsharmonization.api.HarmonizationEncounterTypeService;
import org.openmrs.module.eptsharmonization.api.HarmonizationVisitTypeService;
import org.openmrs.module.eptsharmonization.api.exception.UUIDDuplicationException;
import org.openmrs.module.eptsharmonization.api.model.EncounterTypeDTO;
import org.openmrs.module.eptsharmonization.web.EptsHarmonizationConstants;
import org.openmrs.module.eptsharmonization.web.bean.EncounterTypeHarmonizationCSVLog;
import org.openmrs.module.eptsharmonization.web.bean.EncounterTypeHarmonizationCSVLog.Builder;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationData;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationItem;
import org.openmrs.module.eptsharmonization.web.delegate.HarmonizeEncounterTypeDelegate;
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

@Controller("eptsharmonization.harmonizeEncounterTypeController")
@SessionAttributes({"differentIDsAndEqualUUID", "differentNameAndSameUUIDAndID"})
public class HarmonizeEncounterTypeController {

  public static final String CONTROLLER_PATH =
      EptsHarmonizationConstants.MODULE_PATH + "/encounterType";

  public static final String ENCOUNTER_TYPES_LIST =
      HarmonizeEncounterTypeController.CONTROLLER_PATH + "/harmonizeEncounterTypeList";

  public static final String ADD_ENCOUNTER_TYPE_MAPPING =
      HarmonizeEncounterTypeController.CONTROLLER_PATH + "/addEncounterTypeMapping";

  public static final String ADD_ENCOUNTER_TYPE_FROM_MDS_MAPPING =
      HarmonizeEncounterTypeController.CONTROLLER_PATH + "/addEncounterTypeFromMDSMapping";

  public static final String REMOVE_ENCOUNTER_TYPE_MAPPING =
      HarmonizeEncounterTypeController.CONTROLLER_PATH + "/removeEncounterTypeMapping";

  public static final String PROCESS_HARMONIZATION_STEP1 =
      HarmonizeEncounterTypeController.CONTROLLER_PATH + "/processHarmonizationStep1";

  public static final String PROCESS_HARMONIZATION_STEP2 =
      HarmonizeEncounterTypeController.CONTROLLER_PATH + "/processHarmonizationStep2";

  public static final String PROCESS_HARMONIZATION_STEP3 =
      HarmonizeEncounterTypeController.CONTROLLER_PATH + "/processHarmonizationStep3";

  public static final String PROCESS_HARMONIZATION_STEP4 =
      HarmonizeEncounterTypeController.CONTROLLER_PATH + "/processHarmonizationStep4";

  public static final String EXPORT_ENCOUNTER_TYPES =
      HarmonizeEncounterTypeController.CONTROLLER_PATH + "/harmonizeExportEncounterTypes";

  public static final String EXPORT_LOG =
      HarmonizeEncounterTypeController.CONTROLLER_PATH + "/harmonizeEncounterTypeListExportLog";

  public static boolean HAS_ATLEAST_ONE_ROW_HARMONIZED = false;

  public static boolean IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = false;
  public static boolean IS_NAMES_DIFFERENCES_HARMONIZED = false;

  private HarmonizationEncounterTypeService harmonizationEncounterTypeService;

  private HarmonizationVisitTypeService harmonizationVisitTypeService;

  private HarmonizeEncounterTypeDelegate delegate;

  @Autowired
  public void setHarmonizationEncounterTypeService(
      HarmonizationEncounterTypeService harmonizationEncounterTypeService) {
    this.harmonizationEncounterTypeService = harmonizationEncounterTypeService;
  }

  @Autowired
  public void setHarmonizationVisitTypeService(
      HarmonizationVisitTypeService harmonizationVisitTypeService) {
    this.harmonizationVisitTypeService = harmonizationVisitTypeService;
  }

  @Autowired
  public void setDelegate(HarmonizeEncounterTypeDelegate delegate) {
    this.delegate = delegate;
  }

  @RequestMapping(value = ENCOUNTER_TYPES_LIST, method = RequestMethod.GET)
  public ModelAndView getHarmonizationScenariousData(
      HttpSession session,
      Model model,
      @ModelAttribute("newMDSEncounterTypes") HarmonizationData newMDSEncounterTypes,
      @ModelAttribute("productionItemsToDelete") List<EncounterTypeDTO> productionItemsToDelete,
      @ModelAttribute("differentIDsAndEqualUUID") HarmonizationData differentIDsAndEqualUUID,
      @ModelAttribute("differentNameAndSameUUIDAndID")
          HarmonizationData differentNameAndSameUUIDAndID,
      @ModelAttribute("notSwappableEncounterTypes") List<EncounterType> notSwappableEncounterTypes,
      @ModelAttribute("swappableEncounterTypes") List<EncounterType> swappableEncounterTypes,
      @ModelAttribute("mdsEncounterTypeNotHarmonizedYet")
          List<EncounterType> mdsEncounterTypeNotHarmonizedYet,
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
    newMDSEncounterTypes = getNewMDSEncounterTypes();
    differentIDsAndEqualUUID = this.getDifferentIDsAndEqualUUID();
    differentNameAndSameUUIDAndID = this.getDifferentNameAndSameUUIDAndID();
    HarmonizationData productionItemsToExport = getProductionItemsToExport();

    session.setAttribute(
        "harmonizedETSummary", HarmonizeEncounterTypeDelegate.SUMMARY_EXECUTED_SCENARIOS);
    session.setAttribute("openmrs_msg", openmrs_msg);
    session.setAttribute("errorRequiredMdsValue", errorRequiredMdsValue);
    session.setAttribute("errorRequiredPDSValue", errorRequiredPDSValue);
    session.setAttribute("errorRequiredMdsValueFromMDS", errorRequiredMdsValueFromMDS);
    session.setAttribute("errorRequiredPDSValueFromMDS", errorRequiredPDSValueFromMDS);
    session.setAttribute("errorProcessingManualMapping", errorProcessingManualMapping);

    delegate.setHarmonizationStage(
        session,
        newMDSEncounterTypes,
        productionItemsToDelete,
        productionItemsToExport,
        differentIDsAndEqualUUID,
        differentNameAndSameUUIDAndID,
        notSwappableEncounterTypes,
        swappableEncounterTypes);

    session.removeAttribute("productionItemsToExport");
    session.setAttribute("productionItemsToExport", productionItemsToExport);

    ModelAndView modelAndView = new ModelAndView();
    model.addAttribute("newMDSEncounterTypes", newMDSEncounterTypes);
    model.addAttribute("productionItemsToExport", productionItemsToExport);
    model.addAttribute("differentIDsAndEqualUUID", differentIDsAndEqualUUID);
    model.addAttribute("differentNameAndSameUUIDAndID", differentNameAndSameUUIDAndID);

    if (this.harmonizationEncounterTypeService.isAllEncounterTypeMedatadaHarmonized()
        || this.harmonizationVisitTypeService.isAllMetadataHarmonized()) {
      this.harmonizationEncounterTypeService.updateGPEncounterTypeToVisitTypeMapping();
    }

    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP1, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep1(
      HttpSession session,
      @ModelAttribute("newMDSEncounterTypes") HarmonizationData newMDSEncounterTypes,
      @ModelAttribute("productionItemsToDelete") List<EncounterTypeDTO> productionItemsToDelete) {

    String defaultLocationName = this.getLocationName();
    Builder logBuilder = new EncounterTypeHarmonizationCSVLog.Builder(defaultLocationName);

    delegate.processAddNewFromMetadataServer(newMDSEncounterTypes, logBuilder);
    delegate.processDeleteFromProductionServer(productionItemsToDelete, logBuilder);

    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    modelAndView.addObject("openmrs_msg", "eptsharmonization.encountertype.harmonized");

    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP2, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep2(
      @ModelAttribute("differentIDsAndEqualUUID") HarmonizationData differentIDsAndEqualUUID) {

    HAS_ATLEAST_ONE_ROW_HARMONIZED = false;
    String defaultLocationName = this.getLocationName();
    Builder logBuilder = new EncounterTypeHarmonizationCSVLog.Builder(defaultLocationName);
    delegate.processEncounterTypesWithDiferrentIdsAndEqualUUID(
        differentIDsAndEqualUUID, logBuilder);
    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    if (HAS_ATLEAST_ONE_ROW_HARMONIZED) {
      modelAndView.addObject("openmrs_msg", "eptsharmonization.encountertype.harmonized");
    }
    IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = true;
    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP3, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep3(
      @ModelAttribute("differentNameAndSameUUIDAndID")
          HarmonizationData differentNameAndSameUUIDAndID) {

    HAS_ATLEAST_ONE_ROW_HARMONIZED = false;
    String defaultLocationName = this.getLocationName();
    Builder logBuilder = new EncounterTypeHarmonizationCSVLog.Builder(defaultLocationName);
    delegate.processUpdateEncounterNames(differentNameAndSameUUIDAndID, logBuilder);
    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    if (HAS_ATLEAST_ONE_ROW_HARMONIZED) {
      modelAndView.addObject("openmrs_msg", "eptsharmonization.encountertype.harmonized");
    }
    IS_NAMES_DIFFERENCES_HARMONIZED = true;
    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = PROCESS_HARMONIZATION_STEP4, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep4(
      HttpSession session,
      HttpServletRequest request,
      @ModelAttribute("swappableEncounterTypes") List<EncounterType> swappableEncounterTypes,
      @ModelAttribute("mdsEncounterTypeNotHarmonizedYet")
          List<EncounterType> mdsEncounterTypeNotHarmonizedYet)
      throws Exception {

    Map<EncounterType, EncounterType> manualHarmonizeEtypes =
        (Map<EncounterType, EncounterType>) session.getAttribute("manualHarmonizeEtypes");

    ModelAndView modelAndView = getRedirectModelAndView();
    if (manualHarmonizeEtypes != null && !manualHarmonizeEtypes.isEmpty()) {

      try {
        this.harmonizationEncounterTypeService.saveManualMapping(manualHarmonizeEtypes);
      } catch (UUIDDuplicationException e) {

        for (Entry<EncounterType, EncounterType> entry : manualHarmonizeEtypes.entrySet()) {
          if (!swappableEncounterTypes.contains(entry.getKey())) {
            swappableEncounterTypes.add(entry.getKey());
          }
          if (!mdsEncounterTypeNotHarmonizedYet.contains(entry.getKey())) {
            mdsEncounterTypeNotHarmonizedYet.add(entry.getValue());
          }
        }

        modelAndView.addObject("errorProcessingManualMapping", e.getMessage());
        return modelAndView;
      } catch (SQLException e) {
        e.printStackTrace();
        throw new Exception(e);
      }

      String defaultLocationName = this.getLocationName();
      Builder logBuilder = new EncounterTypeHarmonizationCSVLog.Builder(defaultLocationName);
      logBuilder.appendNewMappedEncounterTypes(manualHarmonizeEtypes);
      logBuilder.build();

      HarmonizeEncounterTypeDelegate.SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.encounterType.newDefinedMapping");
    }

    modelAndView.addObject("openmrs_msg", "eptsharmonization.encountertype.harmonized");

    HarmonizeEncounterTypeDelegate.EXECUTED_ENCOUNTERTYPES_MANUALLY_CACHE =
        new ArrayList<>(manualHarmonizeEtypes.keySet());
    session.removeAttribute("manualHarmonizeEtypes");

    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = ADD_ENCOUNTER_TYPE_MAPPING, method = RequestMethod.POST)
  public ModelAndView addEncounterTypeMapping(
      HttpSession session,
      @ModelAttribute("swappableEncounterTypes") List<EncounterType> swappableEncounterTypes,
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

    EncounterType pdsEncounterType =
        Context.getEncounterService().getEncounterTypeByUuid((String) harmonizationItem.getKey());
    EncounterType mdsEncounterType =
        Context.getEncounterService().getEncounterTypeByUuid((String) harmonizationItem.getValue());

    Map<EncounterType, EncounterType> manualHarmonizeEtypes =
        (Map<EncounterType, EncounterType>) session.getAttribute("manualHarmonizeEtypes");

    if (manualHarmonizeEtypes == null) {
      manualHarmonizeEtypes = new HashMap<>();
    }
    swappableEncounterTypes.remove(pdsEncounterType);
    manualHarmonizeEtypes.put(pdsEncounterType, mdsEncounterType);
    session.setAttribute("manualHarmonizeEtypes", manualHarmonizeEtypes);

    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = ADD_ENCOUNTER_TYPE_FROM_MDS_MAPPING, method = RequestMethod.POST)
  public ModelAndView addEncounterTypeFromMDSMapping(
      HttpSession session,
      @ModelAttribute("swappableEncounterTypes") List<EncounterType> swappableEncounterTypes,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem,
      @ModelAttribute("mdsEncounterTypeNotHarmonizedYet")
          List<EncounterType> mdsEncounterTypeNotHarmonizedYet) {

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

    EncounterType pdsEncounterType =
        Context.getEncounterService().getEncounterTypeByUuid((String) harmonizationItem.getKey());

    String mdsETUuid = (String) harmonizationItem.getValue();
    EncounterType mdsEncounterType = null;
    for (EncounterType encounterType : mdsEncounterTypeNotHarmonizedYet) {
      if (mdsETUuid.equals(encounterType.getUuid())) {
        mdsEncounterType = encounterType;
        break;
      }
    }

    Map<EncounterType, EncounterType> manualHarmonizeEtypes =
        (Map<EncounterType, EncounterType>) session.getAttribute("manualHarmonizeEtypes");

    if (manualHarmonizeEtypes == null) {
      manualHarmonizeEtypes = new HashMap<>();
    }
    swappableEncounterTypes.remove(pdsEncounterType);
    manualHarmonizeEtypes.put(pdsEncounterType, mdsEncounterType);
    session.setAttribute("manualHarmonizeEtypes", manualHarmonizeEtypes);

    if (mdsEncounterTypeNotHarmonizedYet != null
        && mdsEncounterTypeNotHarmonizedYet.contains(mdsEncounterType)) {
      mdsEncounterTypeNotHarmonizedYet.remove(mdsEncounterType);
    }

    return modelAndView;
  }

  @RequestMapping(value = REMOVE_ENCOUNTER_TYPE_MAPPING, method = RequestMethod.POST)
  public ModelAndView removeEncounterTypeMapping(
      HttpSession session,
      @ModelAttribute("swappableEncounterTypes") List<EncounterType> swappableEncounterTypes,
      @ModelAttribute("notSwappableEncounterTypes") List<EncounterType> notSwappableEncounterTypes,
      @ModelAttribute("mdsEncounterTypeNotHarmonizedYet")
          List<EncounterType> mdsEncounterTypeNotHarmonizedYet,
      HttpServletRequest request) {

    EncounterType productionEncounterType =
        Context.getEncounterService()
            .getEncounterTypeByUuid(request.getParameter("productionServerEncounterTypeUuID"));

    @SuppressWarnings("unchecked")
    Map<EncounterType, EncounterType> manualHarmonizeEtypes =
        (Map<EncounterType, EncounterType>) session.getAttribute("manualHarmonizeEtypes");

    EncounterType mdsEncounterType = manualHarmonizeEtypes.get(productionEncounterType);
    manualHarmonizeEtypes.remove(productionEncounterType);
    swappableEncounterTypes.add(productionEncounterType);

    if (notSwappableEncounterTypes != null
        && !notSwappableEncounterTypes.contains(mdsEncounterType)) {
      if (mdsEncounterTypeNotHarmonizedYet != null
          && !mdsEncounterTypeNotHarmonizedYet.contains(mdsEncounterType)) {
        mdsEncounterTypeNotHarmonizedYet.add(mdsEncounterType);
      }
    }

    if (mdsEncounterTypeNotHarmonizedYet != null) {
      this.sortByName(mdsEncounterTypeNotHarmonizedYet);
    }
    if (swappableEncounterTypes != null) {
      this.sortByName(swappableEncounterTypes);
    }

    if (manualHarmonizeEtypes.isEmpty()) {
      session.removeAttribute("manualHarmonizeEtypes");
    }
    return this.getRedirectModelAndView();
  }

  @RequestMapping(value = EXPORT_LOG, method = RequestMethod.POST)
  public @ResponseBody byte[] exportLog(HttpServletRequest request, HttpServletResponse response)
      throws FileNotFoundException {

    File file = new File("harmonizationEncounterTypeLog");
    FileInputStream fis = new FileInputStream(file);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    byte[] buf = new byte[1024];
    try {
      for (int readNum; (readNum = fis.read(buf)) != -1; ) {
        outputStream.write(buf, 0, readNum);
      }
    } catch (IOException ex) {

    }
    String defaultLocationName = this.getFormattedLocationName(this.getLocationName());
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName=" + defaultLocationName + "-encounter_type_harmonization-log.csv");
    response.setContentLength(outputStream.size());
    return outputStream.toByteArray();
  }

  @RequestMapping(value = EXPORT_ENCOUNTER_TYPES, method = RequestMethod.POST)
  public @ResponseBody byte[] exportEncounterTypes(
      HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model)
      throws FileNotFoundException {

    HarmonizationData productionItemsToExport =
        (HarmonizationData) session.getAttribute("productionItemsToExport");
    String defaultLocationName = this.getLocationName();

    List<EncounterType> metaadataServerEncounterTypes =
        this.harmonizationEncounterTypeService.findAllMetadataServerEncounterTypes();

    List<EncounterTypeDTO> list = new ArrayList<>();
    for (HarmonizationItem item : productionItemsToExport.getItems()) {
      list.add((EncounterTypeDTO) item.getValue());
    }

    ByteArrayOutputStream outputStream =
        EncounterTypeHarmonizationCSVLog.exportEncounterTypeLogs(
            defaultLocationName, list, metaadataServerEncounterTypes);
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName="
            + getFormattedLocationName(defaultLocationName)
            + "-encounter_type_harmonization-export-log.csv");
    response.setContentLength(outputStream.size());
    return outputStream.toByteArray();
  }

  @ModelAttribute("productionItemsToDelete")
  public List<EncounterTypeDTO> getProductionItemsToDelete() {
    List<EncounterTypeDTO> productionItemsToDelete = new ArrayList<>();
    List<EncounterTypeDTO> onlyProductionEncounterTypes =
        this.harmonizationEncounterTypeService
            .findAllProductionEncountersNotContainedInMetadataServer();
    for (EncounterTypeDTO encounterTypeDTO : onlyProductionEncounterTypes) {
      final int numberOfAffectedEncounters =
          this.harmonizationEncounterTypeService.getNumberOfAffectedEncounters(encounterTypeDTO);
      final int numberOfAffectedForms =
          this.harmonizationEncounterTypeService.getNumberOfAffectedForms(encounterTypeDTO);
      if (numberOfAffectedEncounters == 0 && numberOfAffectedForms == 0) {
        productionItemsToDelete.add(encounterTypeDTO);
      }
    }
    return productionItemsToDelete;
  }

  public HarmonizationData getProductionItemsToExport() {
    List<EncounterTypeDTO> onlyProductionEncounterTypes =
        this.harmonizationEncounterTypeService
            .findAllProductionEncountersNotContainedInMetadataServer();
    List<EncounterTypeDTO> productionItemsToExport = new ArrayList<>();
    for (EncounterTypeDTO encounterTypeDTO : onlyProductionEncounterTypes) {
      final int numberOfAffectedEncounters =
          this.harmonizationEncounterTypeService.getNumberOfAffectedEncounters(encounterTypeDTO);
      final int numberOfAffectedForms =
          this.harmonizationEncounterTypeService.getNumberOfAffectedForms(encounterTypeDTO);
      if (numberOfAffectedEncounters > 0 || numberOfAffectedForms > 0) {
        productionItemsToExport.add(encounterTypeDTO);
      }
    }
    return delegate.getConvertedData(productionItemsToExport);
  }

  @ModelAttribute("harmonizationItem")
  HarmonizationItem formHarmonizationItem() {
    return new HarmonizationItem();
  }

  @ModelAttribute("newMDSEncounterTypes")
  public HarmonizationData getNewMDSEncounterTypes() {
    List<EncounterTypeDTO> data =
        this.harmonizationEncounterTypeService
            .findAllMetadataEncounterNotContainedInProductionServer();
    return delegate.getConvertedData(data);
  }

  @ModelAttribute("differentIDsAndEqualUUID")
  public HarmonizationData getDifferentIDsAndEqualUUID() {
    Map<String, List<EncounterTypeDTO>> encounterTypesWithDifferentIDsSameUUIDs =
        this.harmonizationEncounterTypeService.findAllEncounterTypesWithDifferentIDAndSameUUID();
    return delegate.getConvertedData(encounterTypesWithDifferentIDsSameUUIDs);
  }

  @ModelAttribute("differentNameAndSameUUIDAndID")
  public HarmonizationData getDifferentNameAndSameUUIDAndID() {
    Map<String, List<EncounterTypeDTO>> encounterTypesWithDifferentNames =
        this.harmonizationEncounterTypeService
            .findAllEncounterTypesWithDifferentNameAndSameUUIDAndID();
    return delegate.getConvertedData(encounterTypesWithDifferentNames);
  }

  @ModelAttribute("swappableEncounterTypes")
  public List<EncounterType> getSwappableEncounterTypes() {
    return this.sortByName(this.harmonizationEncounterTypeService.findAllSwappableEncounterTypes());
  }

  @ModelAttribute("notSwappableEncounterTypes")
  public List<EncounterType> getNotSwappableEncounterTypes() {
    return this.sortByName(
        this.harmonizationEncounterTypeService.findAllNotSwappableEncounterTypes());
  }

  @ModelAttribute("mdsEncounterTypeNotHarmonizedYet")
  public List<EncounterType> getMDSEncounterTypeNotHarmonizedYet() {
    return this.sortByName(this.delegate.getMDSNotHarmonizedYet());
  }

  @SuppressWarnings("unchecked")
  private List<EncounterType> sortByName(List<EncounterType> list) {
    BeanComparator comparator = new BeanComparator("name");
    Collections.sort(list, comparator);
    return list;
  }

  private ModelAndView getRedirectModelAndView() {
    return new ModelAndView("redirect:" + ENCOUNTER_TYPES_LIST + ".form");
  }

  private String getLocationName() {
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
