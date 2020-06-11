package org.openmrs.module.eptsharmonization.web.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsharmonization.HarmonizationUtils;
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

@Controller
@SessionAttributes({"differentIDsAndEqualUUID", "differentNameAndSameUUIDAndID"})
public class HarmonizeEncounterTypeController {

  public static final String CONTROLLER_PATH =
      EptsHarmonizationConstants.MODULE_PATH + "/encounterType";

  public static final String ENCOUNTER_TYPES_LIST =
      HarmonizeEncounterTypeController.CONTROLLER_PATH + "/harmonizeEncounterTypeList";

  public static final String ADD_ENCOUNTER_TYPE_MAPPING =
      HarmonizeEncounterTypeController.CONTROLLER_PATH + "/addEncounterTypeMapping";

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

  @Autowired private HarmonizeEncounterTypeDelegate delegate;

  @RequestMapping(value = ENCOUNTER_TYPES_LIST, method = RequestMethod.GET)
  public ModelAndView getHarmonizationScenariousData(
      HttpSession session,
      Model model,
      @ModelAttribute("newMDSEncounterTypes") HarmonizationData newMDSEncounterTypes,
      @ModelAttribute("productionItemsToDelete") List<EncounterTypeDTO> productionItemsToDelete,
      @ModelAttribute("differentIDsAndEqualUUID") HarmonizationData differentIDsAndEqualUUID,
      @ModelAttribute("differentNameAndSameUUIDAndID")
          HarmonizationData differentNameAndSameUUIDAndID,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem,
      @ModelAttribute("notSwappableEncounterTypes") List<EncounterType> notSwappableEncounterTypes,
      @ModelAttribute("swappableEncounterTypes") List<EncounterType> swappableEncounterTypes,
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg,
      @RequestParam(required = false, value = "errorRequiredMdsValue") String errorRequiredMdsValue,
      @RequestParam(required = false, value = "errorRequiredPDSValue")
          String errorRequiredPDSValue) {

    // TODO: I did this fetch as a workaround to prevent having cached data
    newMDSEncounterTypes = getNewMDSEncounterTypes();
    differentIDsAndEqualUUID = this.getDifferentIDsAndEqualUUID();
    differentNameAndSameUUIDAndID = this.getDifferentNameAndSameUUIDAndID();
    HarmonizationData productionItemsToExport = getProductionItemsToExport();

    session.setAttribute(
        "harmonizedETSummary", HarmonizeEncounterTypeDelegate.SUMMARY_EXECUTED_SCENARIOS);
    session.setAttribute("openmrs_msg", openmrs_msg);
    session.setAttribute("errorRequiredMdsValue", errorRequiredMdsValue);
    session.setAttribute("errorRequiredPDSValue", errorRequiredPDSValue);

    delegate.setHarmonizationStatus(
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

    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP1, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep1(
      HttpSession session,
      @ModelAttribute("newMDSEncounterTypes") HarmonizationData newMDSEncounterTypes,
      @ModelAttribute("productionItemsToDelete") List<EncounterTypeDTO> productionItemsToDelete) {

    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
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
    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
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
    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
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
  public ModelAndView processHarmonizationStep4(HttpSession session, HttpServletRequest request) {

    Map<EncounterType, EncounterType> manualHarmonizeEtypes =
        (Map<EncounterType, EncounterType>) session.getAttribute("manualHarmonizeEtypes");

    if (manualHarmonizeEtypes != null && !manualHarmonizeEtypes.isEmpty()) {
      HarmonizationUtils.getHarmonizationEncounterTypeService()
          .saveManualMapping(manualHarmonizeEtypes);

      String defaultLocationName =
          Context.getAdministrationService().getGlobalProperty("default_location");
      Builder logBuilder = new EncounterTypeHarmonizationCSVLog.Builder(defaultLocationName);
      logBuilder.appendNewMappedEncounterTypes(manualHarmonizeEtypes);
      logBuilder.build();

      HarmonizeEncounterTypeDelegate.SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.encounterType.newDefinedMapping");
    }
    ModelAndView modelAndView = getRedirectModelAndView();
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

    if (harmonizationItem.getKey() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getKey()))) {
      modelAndView.addObject(
          "errorRequiredPDSValue", "eptsharmonization.error.encounterForMapping.required");
      return modelAndView;
    }
    if (harmonizationItem.getValue() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getValue()))) {
      modelAndView.addObject(
          "errorRequiredMdsValue", "eptsharmonization.error.encounterForMapping.required");
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

  @RequestMapping(value = REMOVE_ENCOUNTER_TYPE_MAPPING, method = RequestMethod.POST)
  public ModelAndView removeEncounterTypeMapping(
      HttpSession session,
      @ModelAttribute("swappableEncounterTypes") List<EncounterType> swappableEncounterTypes,
      HttpServletRequest request) {

    EncounterType productionEncounterType =
        Context.getEncounterService()
            .getEncounterTypeByUuid(request.getParameter("productionServerEncounterTypeUuID"));

    @SuppressWarnings("unchecked")
    Map<EncounterType, EncounterType> manualHarmonizeEtypes =
        (Map<EncounterType, EncounterType>) session.getAttribute("manualHarmonizeEtypes");

    manualHarmonizeEtypes.remove(productionEncounterType);
    swappableEncounterTypes.add(productionEncounterType);

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
    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName=encounter_type_harmonization_" + defaultLocationName + "-log.csv");
    response.setContentLength(outputStream.size());
    return outputStream.toByteArray();
  }

  @RequestMapping(value = EXPORT_ENCOUNTER_TYPES, method = RequestMethod.POST)
  public @ResponseBody byte[] exportEncounterTypes(
      HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model)
      throws FileNotFoundException {

    HarmonizationData productionItemsToExport =
        (HarmonizationData) session.getAttribute("productionItemsToExport");
    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");

    List<EncounterTypeDTO> list = new ArrayList<>();
    for (HarmonizationItem item : productionItemsToExport.getItems()) {
      list.add((EncounterTypeDTO) item.getValue());
    }

    ByteArrayOutputStream outputStream =
        EncounterTypeHarmonizationCSVLog.exportEncounterTypeLogs(defaultLocationName, list);
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName=encounter_type_harmonization_"
            + defaultLocationName
            + "-export-log.csv");
    response.setContentLength(outputStream.size());
    return outputStream.toByteArray();
  }

  @ModelAttribute("productionItemsToDelete")
  public List<EncounterTypeDTO> getProductionItemsToDelete() {
    List<EncounterTypeDTO> productionItemsToDelete = new ArrayList<>();
    List<EncounterTypeDTO> onlyProductionEncounterTypes =
        HarmonizationUtils.getHarmonizationEncounterTypeService()
            .findAllProductionEncountersNotContainedInMetadataServer();
    for (EncounterTypeDTO encounterTypeDTO : onlyProductionEncounterTypes) {
      final int numberOfAffectedEncounters =
          HarmonizationUtils.getHarmonizationEncounterTypeService()
              .getNumberOfAffectedEncounters(encounterTypeDTO);
      final int numberOfAffectedForms =
          HarmonizationUtils.getHarmonizationEncounterTypeService()
              .getNumberOfAffectedForms(encounterTypeDTO);
      if (numberOfAffectedEncounters == 0 && numberOfAffectedForms == 0) {
        productionItemsToDelete.add(encounterTypeDTO);
      }
    }
    return productionItemsToDelete;
  }

  public HarmonizationData getProductionItemsToExport() {
    List<EncounterTypeDTO> onlyProductionEncounterTypes =
        HarmonizationUtils.getHarmonizationEncounterTypeService()
            .findAllProductionEncountersNotContainedInMetadataServer();
    List<EncounterTypeDTO> productionItemsToExport = new ArrayList<>();
    for (EncounterTypeDTO encounterTypeDTO : onlyProductionEncounterTypes) {
      final int numberOfAffectedEncounters =
          HarmonizationUtils.getHarmonizationEncounterTypeService()
              .getNumberOfAffectedEncounters(encounterTypeDTO);
      final int numberOfAffectedForms =
          HarmonizationUtils.getHarmonizationEncounterTypeService()
              .getNumberOfAffectedForms(encounterTypeDTO);
      if (numberOfAffectedEncounters > 0 || numberOfAffectedForms > 0) {
        productionItemsToExport.add(encounterTypeDTO);
      }
    }
    return delegate.getConvertedData(productionItemsToExport);
  }

  @ModelAttribute("harmonizationItem")
  HarmonizationItem formBackingObject() {
    return new HarmonizationItem();
  }

  @ModelAttribute("newMDSEncounterTypes")
  public HarmonizationData getNewMDSEncounterTypes() {
    List<EncounterTypeDTO> data =
        HarmonizationUtils.getHarmonizationEncounterTypeService()
            .findAllMetadataEncounterNotContainedInProductionServer();
    return delegate.getConvertedData(data);
  }

  @ModelAttribute("differentIDsAndEqualUUID")
  public HarmonizationData getDifferentIDsAndEqualUUID() {
    Map<String, List<EncounterTypeDTO>> encounterTypesWithDifferentIDsSameUUIDs =
        HarmonizationUtils.getHarmonizationEncounterTypeService()
            .findAllEncounterTypesWithDifferentIDAndSameUUID();
    return delegate.getConvertedData(encounterTypesWithDifferentIDsSameUUIDs);
  }

  @ModelAttribute("differentNameAndSameUUIDAndID")
  public HarmonizationData getDifferentNameAndSameUUIDAndID() {
    Map<String, List<EncounterTypeDTO>> encounterTypesWithDifferentNames =
        HarmonizationUtils.getHarmonizationEncounterTypeService()
            .findAllEncounterTypesWithDifferentNameAndSameUUIDAndID();
    return delegate.getConvertedData(encounterTypesWithDifferentNames);
  }

  @ModelAttribute("swappableEncounterTypes")
  public List<EncounterType> getSwappableEncounterTypes() {
    return HarmonizationUtils.getHarmonizationEncounterTypeService()
        .findAllSwappableEncounterTypes();
  }

  @ModelAttribute("notSwappableEncounterTypes")
  public List<EncounterType> getNotSwappableEncounterTypes() {
    return HarmonizationUtils.getHarmonizationEncounterTypeService()
        .findAllNotSwappableEncounterTypes();
  }

  private ModelAndView getRedirectModelAndView() {
    return new ModelAndView("redirect:" + ENCOUNTER_TYPES_LIST + ".form");
  }
}
