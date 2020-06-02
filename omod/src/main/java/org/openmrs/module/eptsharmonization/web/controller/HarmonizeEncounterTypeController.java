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
import org.openmrs.module.eptsharmonization.api.HarmonizationEncounterTypeService;
import org.openmrs.module.eptsharmonization.api.model.EncounterTypeDTO;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationCSVLog;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationCSVLog.Builder;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationData;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationItem;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HarmonizeEncounterTypeController {

  public static List<String> HARMONIZED_CACHED_SUMMARY = new ArrayList<>();

  @RequestMapping(
      value = {"/module/eptsharmonization/harmonizeEncounterTypeList"},
      method = {org.springframework.web.bind.annotation.RequestMethod.GET})
  public ModelAndView getHarmonizationDataToDashboard(
      HttpSession session,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem,
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg,
      @RequestParam(required = false, value = "errorRequiredMdsValue") String errorRequiredMdsValue,
      @RequestParam(required = false, value = "errorRequiredPDSValue")
          String errorRequiredPDSValue) {
    ModelAndView modelAndView = new ModelAndView();

    HarmonizationEncounterTypeService encounterTypeService =
        HarmonizationUtils.getHarmonizationEncounterTypeService();

    List<EncounterTypeDTO> onlyMetadataEncounterTypes =
        encounterTypeService.findAllMetadataEncounterNotContainedInProductionServer();

    List<EncounterTypeDTO> onlyProductionEncounterTypes =
        encounterTypeService.findAllProductionEncountersNotContainedInMetadataServer();

    Map<String, List<EncounterTypeDTO>> encounterTypesWithDifferentNames =
        encounterTypeService.findAllEncounterTypesWithDifferentNameAndSameUUIDAndID();

    Map<String, List<EncounterTypeDTO>> encounterTypesWithDifferentIDsSameUUIDs =
        encounterTypeService.findAllEncounterTypesWithDifferentIDAndSameUUID();

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

    List<EncounterType> swappableEncounterTypes =
        encounterTypeService.findAllSwappableEncounterTypes();
    List<EncounterType> notSwappableEncounterTypes =
        encounterTypeService.findAllNotSwappableEncounterTypes();

    session.setAttribute("onlyMetadataEncounterTypes", getData(onlyMetadataEncounterTypes));
    session.setAttribute("productionItemsToDelete", productionItemsToDelete);
    session.setAttribute("productionItemsToExport", getData(productionItemsToExport));
    session.setAttribute("encounterTypesWithDifferentNames", encounterTypesWithDifferentNames);
    session.setAttribute(
        "encounterTypesPartialEqual", getData(encounterTypesWithDifferentIDsSameUUIDs));
    session.setAttribute(
        "swappableEncounterTypesClone", new ArrayList<EncounterType>(swappableEncounterTypes));
    session.setAttribute(
        "notSwappableEncounterTypesClone",
        new ArrayList<EncounterType>(notSwappableEncounterTypes));

    this.removeAllChoosenToManualHarmonize(
        session, notSwappableEncounterTypes, swappableEncounterTypes);
    session.setAttribute("swappableEncounterTypes", swappableEncounterTypes);
    session.setAttribute("notSwappableEncounterTypes", notSwappableEncounterTypes);
    this.setHarmonizationStatus(session);
    session.setAttribute("harmonizedETSummary", HARMONIZED_CACHED_SUMMARY);
    session.setAttribute("openmrs_msg", openmrs_msg);
    session.setAttribute("errorRequiredMdsValue", errorRequiredMdsValue);
    session.setAttribute("errorRequiredPDSValue", errorRequiredPDSValue);

    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(
      value = {"/module/eptsharmonization/harmonizeEncounterTypeList"},
      method = {org.springframework.web.bind.annotation.RequestMethod.POST})
  public ModelAndView processFirstStepHarmonization(
      HttpServletRequest request,
      HttpServletResponse response,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem,
      HttpSession session,
      ModelMap model) {

    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
    Builder logBuilder = new HarmonizationCSVLog.Builder(defaultLocationName);

    this.processAddNewFromMetadataServer(
        (HarmonizationData) session.getAttribute("onlyMetadataEncounterTypes"), logBuilder);

    this.processDeleteFromProductionServer(
        (List<EncounterTypeDTO>) session.getAttribute("productionItemsToDelete"), logBuilder);

    this.processUpdateEncounterNames(
        (Map<String, List<EncounterTypeDTO>>)
            session.getAttribute("encounterTypesWithDifferentNames"),
        logBuilder);

    this.processEncounterTypesWithDiferrentIdsAndEqualUUID(
        (HarmonizationData) session.getAttribute("encounterTypesPartialEqual"), logBuilder);

    logBuilder.build();

    ModelAndView modelAndView =
        new ModelAndView("redirect:/module/eptsharmonization/harmonizeEncounterTypeList.form");
    modelAndView.addObject("openmrs_msg", "eptsharmonization.encountertype.harmonized");

    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(
      value = {"/module/eptsharmonization/addEncounterTypeMapping"},
      method = {org.springframework.web.bind.annotation.RequestMethod.POST})
  public ModelAndView addEncounterTypeMapping(
      HttpSession session,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem,
      BindingResult result,
      SessionStatus status,
      HttpServletRequest request) {

    ModelAndView modelAndView =
        new ModelAndView("redirect:/module/eptsharmonization/harmonizeEncounterTypeList.form");

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
    List<EncounterType> swappableEncounterTypes =
        (List<EncounterType>) session.getAttribute("swappableEncounterTypes");

    List<EncounterType> notSwappableEncounterTypes =
        (List<EncounterType>) session.getAttribute("notSwappableEncounterTypes");
    notSwappableEncounterTypes.remove(mdsEncounterType);
    swappableEncounterTypes.remove(pdsEncounterType);
    manualHarmonizeEtypes.put(mdsEncounterType, pdsEncounterType);
    session.setAttribute("manualHarmonizeEtypes", manualHarmonizeEtypes);

    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(
      value = {"/module/eptsharmonization/removeEncounterTypeMapping"},
      method = {org.springframework.web.bind.annotation.RequestMethod.POST})
  public ModelAndView removeEncounterTypeMapping(HttpSession session, HttpServletRequest request) {

    EncounterType mdsEncounterType =
        Context.getEncounterService().getEncounterTypeByUuid(request.getParameter("mdsID"));

    Map<EncounterType, EncounterType> manualHarmonizeEtypes =
        (Map<EncounterType, EncounterType>) session.getAttribute("manualHarmonizeEtypes");

    EncounterType pdsEncounterType = manualHarmonizeEtypes.get(mdsEncounterType);
    List<EncounterType> swappableEncounterTypes =
        (List<EncounterType>) session.getAttribute("swappableEncounterTypes");
    List<EncounterType> notSwappableEncounterTypes =
        (List<EncounterType>) session.getAttribute("notSwappableEncounterTypes");

    swappableEncounterTypes.add(pdsEncounterType);
    notSwappableEncounterTypes.add(mdsEncounterType);
    manualHarmonizeEtypes.remove(mdsEncounterType);

    if (manualHarmonizeEtypes.isEmpty()) {
      session.removeAttribute("manualHarmonizeEtypes");
    }
    return new ModelAndView("redirect:/module/eptsharmonization/harmonizeEncounterTypeList.form");
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(
      value = {"/module/eptsharmonization/processHarmonizeManualMappingEncounterType"},
      method = {org.springframework.web.bind.annotation.RequestMethod.POST})
  public ModelAndView harmonizeManualMappedEncounterTypes(
      HttpSession session, HttpServletRequest request) {

    Map<EncounterType, EncounterType> manualHarmonizeEtypes =
        (Map<EncounterType, EncounterType>) session.getAttribute("manualHarmonizeEtypes");

    if (manualHarmonizeEtypes != null && !manualHarmonizeEtypes.isEmpty()) {

      HarmonizationUtils.getHarmonizationEncounterTypeService()
          .saveEncounterTypesWithDifferentIDAndUUID(manualHarmonizeEtypes);
      HarmonizationCSVLog.appendNewMappedEncounterTypes(manualHarmonizeEtypes);
      HarmonizeEncounterTypeController.HARMONIZED_CACHED_SUMMARY.add(
          "eptsharmonization.encounterType.newDefinedMapping");
    }
    ModelAndView modelAndView =
        new ModelAndView("redirect:/module/eptsharmonization/harmonizeEncounterTypeList.form");
    modelAndView.addObject("openmrs_msg", "eptsharmonization.encountertype.harmonized");
    session.removeAttribute("manualHarmonizeEtypes");

    return modelAndView;
  }

  @RequestMapping(
      value = {"/module/eptsharmonization/harmonizeEncounterTypeListExportLog"},
      method = {org.springframework.web.bind.annotation.RequestMethod.POST})
  public @ResponseBody byte[] exportLog(
      HttpServletRequest request,
      HttpServletResponse response,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem)
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
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition", "attachment; fileName=encounter_type_harmonization-log.csv");
    response.setContentLength(outputStream.size());
    return outputStream.toByteArray();
  }

  @ModelAttribute("harmonizationItem")
  HarmonizationItem formBackingObject() {
    return new HarmonizationItem();
  }

  @SuppressWarnings("unchecked")
  private void setHarmonizationStatus(HttpSession session) {

    HarmonizationData onlyMetadataEncounterTypes =
        (HarmonizationData) session.getAttribute("onlyMetadataEncounterTypes");
    List<EncounterTypeDTO> productionItemsToDelete =
        (List<EncounterTypeDTO>) session.getAttribute("productionItemsToDelete");
    HarmonizationData productionItemsToExport =
        (HarmonizationData) session.getAttribute("productionItemsToExport");
    Map<String, List<EncounterTypeDTO>> encounterTypesWithDifferentNames =
        (Map<String, List<EncounterTypeDTO>>)
            session.getAttribute("encounterTypesWithDifferentNames");
    HarmonizationData encounterTypesPartialEqual =
        (HarmonizationData) session.getAttribute("encounterTypesPartialEqual");

    boolean isFirstStepHarmonizationCompleted =
        onlyMetadataEncounterTypes.getItems().isEmpty()
            && productionItemsToDelete.isEmpty()
            && encounterTypesWithDifferentNames.isEmpty()
            && encounterTypesPartialEqual.getItems().isEmpty();
    boolean hasSecondStepHarmonization =
        isFirstStepHarmonizationCompleted && !productionItemsToExport.getItems().isEmpty();

    session.setAttribute("isFirstStepHarmonizationCompleted", isFirstStepHarmonizationCompleted);
    session.setAttribute("hasSecondStepHarmonization", hasSecondStepHarmonization);
  }

  @SuppressWarnings("unchecked")
  private void removeAllChoosenToManualHarmonize(
      HttpSession session,
      List<EncounterType> notSwappableEncounterTypes,
      List<EncounterType> swappableEncounterTypes) {
    Map<EncounterType, EncounterType> manualHarmonizeEtypes =
        (Map<EncounterType, EncounterType>) session.getAttribute("manualHarmonizeEtypes");

    if (manualHarmonizeEtypes != null) {
      for (Entry<EncounterType, EncounterType> entry : manualHarmonizeEtypes.entrySet()) {
        notSwappableEncounterTypes.remove(entry.getKey());
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
      HarmonizeEncounterTypeController.HARMONIZED_CACHED_SUMMARY.add(
          "eptsharmonization.summary.encountertype.harmonize.onlyOnMDServer");
      logBuilder.appendLogForNewHarmonizedFromMDSEncounterTypes(list);
    }
  }

  private void processDeleteFromProductionServer(List<EncounterTypeDTO> list, Builder logBuilder) {
    if (!list.isEmpty()) {
      HarmonizationUtils.getHarmonizationEncounterTypeService()
          .deleteNewEncounterTypesFromPDS(list);
      HarmonizeEncounterTypeController.HARMONIZED_CACHED_SUMMARY.add(
          "eptsharmonization.summary.encountertype.harmonize.onlyOnPServer.unused");
      logBuilder.appendLogForDeleteFromProductionServer(list);
    }
  }

  private void processUpdateEncounterNames(
      Map<String, List<EncounterTypeDTO>> data, Builder logBuilder) {
    if (!data.isEmpty()) {
      HarmonizationUtils.getHarmonizationEncounterTypeService()
          .saveEncounterTypesWithDifferentNames(data);
      HarmonizeEncounterTypeController.HARMONIZED_CACHED_SUMMARY.add(
          "eptsharmonization.summary.encountertype.harmonize.differentNamesAndSameUUIDAndID");
      logBuilder.appendLogForUpdatedEncounterNames(data);
    }
  }

  @SuppressWarnings("unchecked")
  private void processEncounterTypesWithDiferrentIdsAndEqualUUID(
      HarmonizationData data, Builder logBuilder) {
    Map<String, List<EncounterTypeDTO>> list = new HashMap<>();
    for (HarmonizationItem item : data.getItems()) {
      list.put((String) item.getKey(), (List<EncounterTypeDTO>) item.getValue());
    }
    if (!list.isEmpty()) {
      HarmonizationUtils.getHarmonizationEncounterTypeService()
          .saveEncounterTypesWithDifferentIDAndEqualUUID(list);
      HarmonizeEncounterTypeController.HARMONIZED_CACHED_SUMMARY.add(
          "eptsharmonization.summary.encountertype.harmonize.differentID.andEqualUUID");
      logBuilder.appendLogForEncounterTypesWithDiferrentIdsAndEqualUUID(list);
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
      HarmonizationItem item = new HarmonizationItem(key, eTypes);
      item.setEncountersCount(service.getNumberOfAffectedEncounters(eTypes.get(1)));
      item.setFormsCount(service.getNumberOfAffectedForms(eTypes.get(1)));
      items.add(item);
    }
    return new HarmonizationData(items);
  }
}
