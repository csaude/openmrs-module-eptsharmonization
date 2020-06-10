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
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsharmonization.HarmonizationUtils;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationEncounterTypeService;
import org.openmrs.module.eptsharmonization.api.model.EncounterTypeDTO;
import org.openmrs.module.eptsharmonization.web.EptsHarmonizationConstants;
import org.openmrs.module.eptsharmonization.web.bean.EncounterTypeHarmonizationCSVLog;
import org.openmrs.module.eptsharmonization.web.bean.EncounterTypeHarmonizationCSVLog.Builder;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationData;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationItem;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

@Controller // (EptsHarmonizationConstants.MODULE_ID + ".HarmonizeEncounterTypeController")
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

  private static List<String> executedScenariosSummary = new ArrayList<>();
  private static List<EncounterType> executedEncounterTypesManualMappingCache = new ArrayList<>();

  private static boolean isUUIDsAndIDsHarmonized = false;
  private static boolean isNamesHarmonized = false;
  private static boolean hasAtLeastOneRowHarmonized = false;

  private static List<EncounterType> encounterTypesNotProcessed = new ArrayList<>();

  @RequestMapping(value = ENCOUNTER_TYPES_LIST, method = RequestMethod.GET)
  public void getHarmonizationDataToDashboard(
      HttpSession session,
      Model model,
      @ModelAttribute("newMDSEncounterTypes") HarmonizationData newMDSEncounterTypes,
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

    HarmonizationEncounterTypeService encounterTypeService =
        HarmonizationUtils.getHarmonizationEncounterTypeService();

    List<EncounterTypeDTO> onlyProductionEncounterTypes =
        encounterTypeService.findAllProductionEncountersNotContainedInMetadataServer();

    List<EncounterTypeDTO> productionItemsToDelete = new ArrayList<>();
    List<EncounterTypeDTO> productionItemsToExport = new ArrayList<>();

    for (EncounterTypeDTO encounterTypeDTO : onlyProductionEncounterTypes) {
      final int numberOfAffectedEncounters =
          HarmonizationUtils.getHarmonizationEncounterTypeService()
              .getNumberOfAffectedEncounters(encounterTypeDTO);
      final int numberOfAffectedForms =
          HarmonizationUtils.getHarmonizationEncounterTypeService()
              .getNumberOfAffectedForms(encounterTypeDTO);
      if (numberOfAffectedEncounters == 0 && numberOfAffectedForms == 0) {
        productionItemsToDelete.add(encounterTypeDTO);
      } else {
        productionItemsToExport.add(encounterTypeDTO);
      }
    }

    session.setAttribute("productionItemsToDelete", productionItemsToDelete);
    session.setAttribute("productionItemsToExport", getData(productionItemsToExport));

    session.setAttribute("harmonizedETSummary", executedScenariosSummary);
    session.setAttribute("openmrs_msg", openmrs_msg);
    session.setAttribute("errorRequiredMdsValue", errorRequiredMdsValue);
    session.setAttribute("errorRequiredPDSValue", errorRequiredPDSValue);

    this.setHarmonizationStatus(
        session,
        newMDSEncounterTypes,
        differentIDsAndEqualUUID,
        differentNameAndSameUUIDAndID,
        notSwappableEncounterTypes,
        swappableEncounterTypes);
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = PROCESS_HARMONIZATION_STEP1, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep1(
      @ModelAttribute("newMDSEncounterTypes") HarmonizationData newMDSEncounterTypes,
      HttpSession session) {

    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
    Builder logBuilder = new EncounterTypeHarmonizationCSVLog.Builder(defaultLocationName);

    this.processAddNewFromMetadataServer(newMDSEncounterTypes, logBuilder);
    this.processDeleteFromProductionServer(
        (List<EncounterTypeDTO>) session.getAttribute("productionItemsToDelete"), logBuilder);

    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    modelAndView.addObject("openmrs_msg", "eptsharmonization.encountertype.harmonized");

    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP2, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep2(
      @ModelAttribute("differentIDsAndEqualUUID") HarmonizationData differentIDsAndEqualUUID) {

    hasAtLeastOneRowHarmonized = false;
    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
    Builder logBuilder = new EncounterTypeHarmonizationCSVLog.Builder(defaultLocationName);
    this.processEncounterTypesWithDiferrentIdsAndEqualUUID(differentIDsAndEqualUUID, logBuilder);
    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    if (hasAtLeastOneRowHarmonized) {
      modelAndView.addObject("openmrs_msg", "eptsharmonization.encountertype.harmonized");
    }
    isUUIDsAndIDsHarmonized = true;
    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP3, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep3(
      @ModelAttribute("differentNameAndSameUUIDAndID")
          HarmonizationData differentNameAndSameUUIDAndID) {

    hasAtLeastOneRowHarmonized = false;
    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
    Builder logBuilder = new EncounterTypeHarmonizationCSVLog.Builder(defaultLocationName);
    this.processUpdateEncounterNames(differentNameAndSameUUIDAndID, logBuilder);
    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    if (hasAtLeastOneRowHarmonized) {
      modelAndView.addObject("openmrs_msg", "eptsharmonization.encountertype.harmonized");
    }
    isNamesHarmonized = true;
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

      executedScenariosSummary.add("eptsharmonization.encounterType.newDefinedMapping");
    }
    ModelAndView modelAndView = getRedirectModelAndView();
    modelAndView.addObject("openmrs_msg", "eptsharmonization.encountertype.harmonized");

    executedEncounterTypesManualMappingCache = new ArrayList<>(manualHarmonizeEtypes.values());
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
          "errorRequiredMdsValue", "eptsharmonization.error.encounterForMapping.required");
      return modelAndView;
    }
    if (harmonizationItem.getValue() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getValue()))) {
      modelAndView.addObject(
          "errorRequiredPDSValue", "eptsharmonization.error.encounterForMapping.required");
      return modelAndView;
    }

    EncounterType mdsEncounterType =
        Context.getEncounterService().getEncounterTypeByUuid((String) harmonizationItem.getKey());
    EncounterType pdsEncounterType =
        Context.getEncounterService().getEncounterTypeByUuid((String) harmonizationItem.getValue());

    Map<EncounterType, EncounterType> manualHarmonizeEtypes =
        (Map<EncounterType, EncounterType>) session.getAttribute("manualHarmonizeEtypes");

    if (manualHarmonizeEtypes == null) {
      manualHarmonizeEtypes = new HashMap<>();
    }

    swappableEncounterTypes.remove(pdsEncounterType);
    manualHarmonizeEtypes.put(mdsEncounterType, pdsEncounterType);
    session.setAttribute("manualHarmonizeEtypes", manualHarmonizeEtypes);

    return modelAndView;
  }

  @RequestMapping(value = REMOVE_ENCOUNTER_TYPE_MAPPING, method = RequestMethod.POST)
  public ModelAndView removeEncounterTypeMapping(
      HttpSession session,
      @ModelAttribute("swappableEncounterTypes") List<EncounterType> swappableEncounterTypes,
      HttpServletRequest request) {

    EncounterType mdsEncounterType =
        Context.getEncounterService()
            .getEncounterTypeByUuid(request.getParameter("metadataServerEncounterTypeUuID"));

    @SuppressWarnings("unchecked")
    Map<EncounterType, EncounterType> manualHarmonizeEtypes =
        (Map<EncounterType, EncounterType>) session.getAttribute("manualHarmonizeEtypes");

    EncounterType pdsEncounterType = manualHarmonizeEtypes.get(mdsEncounterType);

    manualHarmonizeEtypes.remove(mdsEncounterType);
    swappableEncounterTypes.add(pdsEncounterType);

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
      HttpSession session, HttpServletRequest request, HttpServletResponse response)
      throws FileNotFoundException {

    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
    HarmonizationData productionItemsToExport =
        (HarmonizationData) session.getAttribute("productionItemsToExport");

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

  @ModelAttribute("harmonizationItem")
  HarmonizationItem formBackingObject() {
    return new HarmonizationItem();
  }

  @ModelAttribute("newMDSEncounterTypes")
  public HarmonizationData getNewMDSEncounterTypes() {
    List<EncounterTypeDTO> data =
        HarmonizationUtils.getHarmonizationEncounterTypeService()
            .findAllMetadataEncounterNotContainedInProductionServer();
    return getData(data);
  }

  @ModelAttribute("differentIDsAndEqualUUID")
  public HarmonizationData getDifferentIDsAndEqualUUID() {
    Map<String, List<EncounterTypeDTO>> encounterTypesWithDifferentIDsSameUUIDs =
        HarmonizationUtils.getHarmonizationEncounterTypeService()
            .findAllEncounterTypesWithDifferentIDAndSameUUID();
    return getData(encounterTypesWithDifferentIDsSameUUIDs);
  }

  @ModelAttribute("differentNameAndSameUUIDAndID")
  public HarmonizationData getDifferentNameAndSameUUIDAndID() {
    Map<String, List<EncounterTypeDTO>> encounterTypesWithDifferentNames =
        HarmonizationUtils.getHarmonizationEncounterTypeService()
            .findAllEncounterTypesWithDifferentNameAndSameUUIDAndID();
    return getData(encounterTypesWithDifferentNames);
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

  @SuppressWarnings("unchecked")
  private void setHarmonizationStatus(
      HttpSession session,
      HarmonizationData newMDSEncounterTypes,
      HarmonizationData differentIDsAndEqualUUID,
      HarmonizationData differentNameAndSameUUIDAndID,
      List<EncounterType> notSwappableEncounterTypes,
      List<EncounterType> swappableEncounterTypes) {

    List<EncounterTypeDTO> productionItemsToDelete =
        (List<EncounterTypeDTO>) session.getAttribute("productionItemsToDelete");
    HarmonizationData productionItemsToExport =
        (HarmonizationData) session.getAttribute("productionItemsToExport");

    boolean isFirstStepHarmonizationCompleted =
        newMDSEncounterTypes.getItems().isEmpty() && productionItemsToDelete.isEmpty();

    isUUIDsAndIDsHarmonized =
        isUUIDsAndIDsHarmonized || differentIDsAndEqualUUID.getItems().isEmpty();
    isNamesHarmonized = isNamesHarmonized || differentNameAndSameUUIDAndID.getItems().isEmpty();

    if (!isFirstStepHarmonizationCompleted) {
      isUUIDsAndIDsHarmonized = false;
      isNamesHarmonized = false;
    }

    boolean hasSecondStepHarmonization =
        isFirstStepHarmonizationCompleted
            && isUUIDsAndIDsHarmonized
            && isNamesHarmonized
            && !productionItemsToExport.getItems().isEmpty();

    session.setAttribute("isFirstStepHarmonizationCompleted", isFirstStepHarmonizationCompleted);
    session.setAttribute("isUUIDsAndIDsHarmonized", isUUIDsAndIDsHarmonized);
    session.setAttribute("isNamesHarmonized", isNamesHarmonized);
    session.setAttribute("hasSecondStepHarmonization", hasSecondStepHarmonization);

    this.addEncounterTypesToManualMappings(
        session, notSwappableEncounterTypes, swappableEncounterTypes);
  }

  private void addEncounterTypesToManualMappings(
      HttpSession session,
      List<EncounterType> notSwappableEncounterTypes,
      List<EncounterType> swappableEncounterTypes) {

    HarmonizationData productionItemsToExport =
        (HarmonizationData) session.getAttribute("productionItemsToExport");
    List<HarmonizationItem> items = productionItemsToExport.getItems();
    List<HarmonizationItem> itemsToRemove =
        getData(DTOUtils.fromEncounterTypes(executedEncounterTypesManualMappingCache)).getItems();
    items.removeAll(itemsToRemove);
    productionItemsToExport.setItems(items);
    swappableEncounterTypes.removeAll(executedEncounterTypesManualMappingCache);

    encounterTypesNotProcessed.removeAll(executedEncounterTypesManualMappingCache);
    for (EncounterType encounterType : encounterTypesNotProcessed) {
      if (!swappableEncounterTypes.contains(encounterType)) {
        swappableEncounterTypes.add(encounterType);
      }
    }

    HarmonizationData newItemsToExport =
        getData(DTOUtils.fromEncounterTypes(encounterTypesNotProcessed));
    productionItemsToExport.getItems().addAll(newItemsToExport.getItems());
    session.setAttribute("productionItemsToExport", productionItemsToExport);
    session.setAttribute(
        "swappableEncounterTypesClone", new ArrayList<EncounterType>(swappableEncounterTypes));
    session.setAttribute(
        "notSwappableEncounterTypesClone",
        new ArrayList<EncounterType>(notSwappableEncounterTypes));

    this.removeAllChoosenToManualHarmonize(session, swappableEncounterTypes);
  }

  private ModelAndView getRedirectModelAndView() {
    return new ModelAndView("redirect:" + ENCOUNTER_TYPES_LIST + ".form");
  }

  @SuppressWarnings("unchecked")
  private void removeAllChoosenToManualHarmonize(
      HttpSession session, List<EncounterType> swappableEncounterTypes) {
    Map<EncounterType, EncounterType> manualHarmonizeEtypes =
        (Map<EncounterType, EncounterType>) session.getAttribute("manualHarmonizeEtypes");
    if (manualHarmonizeEtypes != null) {
      for (Entry<EncounterType, EncounterType> entry : manualHarmonizeEtypes.entrySet()) {
        swappableEncounterTypes.remove(entry.getValue());
      }
    }
  }

  private void processAddNewFromMetadataServer(
      HarmonizationData onlyMetadataEncounterTypes, Builder logBuilder) {
    List<EncounterTypeDTO> list = new ArrayList<>();
    for (HarmonizationItem item : onlyMetadataEncounterTypes.getItems()) {
      list.add((EncounterTypeDTO) item.getValue());
    }
    if (!list.isEmpty()) {
      HarmonizationUtils.getHarmonizationEncounterTypeService().saveNewEncounterTypesFromMDS(list);
      executedScenariosSummary.add(
          "eptsharmonization.summary.encountertype.harmonize.onlyOnMDServer");
      logBuilder.appendLogForNewHarmonizedFromMDSEncounterTypes(list);
    }
  }

  private void processDeleteFromProductionServer(List<EncounterTypeDTO> list, Builder logBuilder) {
    if (!list.isEmpty()) {
      HarmonizationUtils.getHarmonizationEncounterTypeService()
          .deleteNewEncounterTypesFromPDS(list);
      executedScenariosSummary.add(
          "eptsharmonization.summary.encountertype.harmonize.onlyOnPServer.unused");
      logBuilder.appendLogForDeleteFromProductionServer(list);
    }
  }

  @SuppressWarnings("unchecked")
  private void processUpdateEncounterNames(HarmonizationData data, Builder logBuilder) {
    Map<String, List<EncounterTypeDTO>> list = new HashMap<>();
    for (HarmonizationItem item : data.getItems()) {
      List<EncounterTypeDTO> value = (List<EncounterTypeDTO>) item.getValue();
      if (item.isSelected()) {
        list.put((String) item.getKey(), value);
      } else {
        encounterTypesNotProcessed.add(value.get(1).getEncounterType());
      }
    }
    if (!list.isEmpty()) {
      HarmonizationUtils.getHarmonizationEncounterTypeService()
          .saveEncounterTypesWithDifferentNames(list);
      executedScenariosSummary.add(
          "eptsharmonization.summary.encountertype.harmonize.differentNamesAndSameUUIDAndID");
      logBuilder.appendLogForUpdatedEncounterNames(list);
      hasAtLeastOneRowHarmonized = true;
    }
  }

  @SuppressWarnings("unchecked")
  private void processEncounterTypesWithDiferrentIdsAndEqualUUID(
      HarmonizationData data, Builder logBuilder) {
    Map<String, List<EncounterTypeDTO>> list = new HashMap<>();

    for (HarmonizationItem item : data.getItems()) {
      List<EncounterTypeDTO> value = (List<EncounterTypeDTO>) item.getValue();
      if (item.isSelected()) {
        list.put((String) item.getKey(), value);
      } else {
        encounterTypesNotProcessed.add(value.get(1).getEncounterType());
      }
    }
    if (!list.isEmpty()) {
      HarmonizationUtils.getHarmonizationEncounterTypeService()
          .saveEncounterTypesWithDifferentIDAndEqualUUID(list);
      executedScenariosSummary.add(
          "eptsharmonization.summary.encountertype.harmonize.differentID.andEqualUUID");
      logBuilder.appendLogForEncounterTypesWithDiferrentIdsAndEqualUUID(list);
      hasAtLeastOneRowHarmonized = true;
    }
  }

  private HarmonizationData getData(List<EncounterTypeDTO> encounterTypes) {
    HarmonizationEncounterTypeService service =
        HarmonizationUtils.getHarmonizationEncounterTypeService();
    List<HarmonizationItem> items = new ArrayList<>();
    for (EncounterTypeDTO encounterTypeDTO : encounterTypes) {
      HarmonizationItem item =
          new HarmonizationItem(encounterTypeDTO.getEncounterType().getUuid(), encounterTypeDTO);
      item.setEncountersCount(service.getNumberOfAffectedEncounters(encounterTypeDTO));
      item.setFormsCount(service.getNumberOfAffectedForms(encounterTypeDTO));
      items.add(item);
    }
    return new HarmonizationData(items);
  }

  private HarmonizationData getData(Map<String, List<EncounterTypeDTO>> mapEncounterTypes) {
    HarmonizationEncounterTypeService service =
        HarmonizationUtils.getHarmonizationEncounterTypeService();
    List<HarmonizationItem> items = new ArrayList<>();
    for (String key : mapEncounterTypes.keySet()) {
      List<EncounterTypeDTO> eTypes = mapEncounterTypes.get(key);
      if (eTypes != null) {
        HarmonizationItem item = new HarmonizationItem(key, eTypes);
        item.setEncountersCount(service.getNumberOfAffectedEncounters(eTypes.get(1)));
        item.setFormsCount(service.getNumberOfAffectedForms(eTypes.get(1)));
        items.add(item);
      }
    }
    return new HarmonizationData(items);
  }
}
