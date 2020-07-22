package org.openmrs.module.eptsharmonization.web.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationProgramWorkflowStateService;
import org.openmrs.module.eptsharmonization.api.exception.UUIDDuplicationException;
import org.openmrs.module.eptsharmonization.api.model.ProgramWorkflowStateDTO;
import org.openmrs.module.eptsharmonization.web.EptsHarmonizationConstants;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationData;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationItem;
import org.openmrs.module.eptsharmonization.web.bean.ProgramWorkflowStatesHarmonizationCSVLog;
import org.openmrs.module.eptsharmonization.web.bean.ProgramWorkflowStatesHarmonizationCSVLog.Builder;
import org.openmrs.module.eptsharmonization.web.delegate.HarmonizeProgramWorkflowStatesDelegate;
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

@Controller("eptsharmonization.harmonizeProgramWorkflowStateController")
@SessionAttributes({
  "differentIDsAndEqualUUID",
  "differentProgramWorkflowsOrConceptsAndSameUUIDAndID"
})
public class HarmonizeProgramWorkflowStatesController {

  public static final String CONTROLLER_PATH =
      EptsHarmonizationConstants.MODULE_PATH + "/programWorkflowStates";

  public static final String PROGRAM_WORKFLOWS_LIST =
      HarmonizeProgramWorkflowStatesController.CONTROLLER_PATH
          + "/harmonizeProgramWorkflowStatesList";

  public static final String ADD_PROGRAM_WORKFLOW_STATE_MAPPING =
      HarmonizeProgramWorkflowStatesController.CONTROLLER_PATH + "/addProgramWorkflowStateMapping";

  public static final String ADD_PROGRAM_WORKFLOW_STATE_FROM_MDS_MAPPING =
      HarmonizeProgramWorkflowStatesController.CONTROLLER_PATH
          + "/addProgramWorkflowStateFromMDSMapping";

  public static final String REMOVE_PROGRAM_WORKFLOW_STATE_MAPPING =
      HarmonizeProgramWorkflowStatesController.CONTROLLER_PATH
          + "/removeProgramWorkflowStateMapping";

  public static final String PROCESS_HARMONIZATION_STEP1 =
      HarmonizeProgramWorkflowStatesController.CONTROLLER_PATH + "/processHarmonizationStep1";

  public static final String PROCESS_HARMONIZATION_STEP2 =
      HarmonizeProgramWorkflowStatesController.CONTROLLER_PATH + "/processHarmonizationStep2";

  public static final String PROCESS_HARMONIZATION_STEP3 =
      HarmonizeProgramWorkflowStatesController.CONTROLLER_PATH + "/processHarmonizationStep3";

  public static final String PROCESS_HARMONIZATION_STEP4 =
      HarmonizeProgramWorkflowStatesController.CONTROLLER_PATH + "/processHarmonizationStep4";

  public static final String EXPORT_PROGRAMWORKFLOWS =
      HarmonizeProgramWorkflowStatesController.CONTROLLER_PATH
          + "/harmonizeExportProgramWorkflowStates";

  public static final String EXPORT_LOG =
      HarmonizeProgramWorkflowStatesController.CONTROLLER_PATH
          + "/harmonizeProgramWorkflowStatesListExportLog";

  public static boolean HAS_ATLEAST_ONE_ROW_HARMONIZED = false;

  public static boolean IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = false;
  public static boolean IS_NAMES_DIFFERENCES_HARMONIZED = false;

  private HarmonizationProgramWorkflowStateService harmonizationProgramWorkflowStateService;

  private HarmonizeProgramWorkflowStatesDelegate delegate;

  @Autowired
  public void setHarmonizationProgramWorkflowStateService(
      HarmonizationProgramWorkflowStateService harmonizationProgramWorkflowStateService) {
    this.harmonizationProgramWorkflowStateService = harmonizationProgramWorkflowStateService;
  }

  @Autowired
  public void setDelegate(HarmonizeProgramWorkflowStatesDelegate delegate) {
    this.delegate = delegate;
  }

  @RequestMapping(value = PROGRAM_WORKFLOWS_LIST, method = RequestMethod.GET)
  public ModelAndView getHarmonizationScenariousData(
      HttpSession session,
      Model model,
      @ModelAttribute("newMDSProgramWorkflowStates") HarmonizationData newMDSProgramWorkflowStates,
      @ModelAttribute("productionItemsToDelete")
          List<ProgramWorkflowStateDTO> productionItemsToDelete,
      @ModelAttribute("differentIDsAndEqualUUID") HarmonizationData differentIDsAndEqualUUID,
      @ModelAttribute("differentProgramWorkflowsOrConceptsAndSameUUIDAndID")
          HarmonizationData differentProgramWorkflowsOrConceptsAndSameUUIDAndID,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem,
      @ModelAttribute("notSwappableProgramWorkflowStates")
          List<ProgramWorkflowStateDTO> notSwappableProgramWorkflowStates,
      @ModelAttribute("swappableProgramWorkflowStates")
          List<ProgramWorkflowStateDTO> swappableProgramWorkflowStates,
      @ModelAttribute("mdsProgramWorkflowStateNotHarmonizedYet")
          List<ProgramWorkflowStateDTO> mdsProgramWorkflowStateNotHarmonizedYet,
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg,
      @RequestParam(required = false, value = "errorRequiredMdsValue") String errorRequiredMdsValue,
      @RequestParam(required = false, value = "errorRequiredPDSValue") String errorRequiredPDSValue,
      @RequestParam(required = false, value = "errorRequiredMdsValueFromMDS")
          String errorRequiredMdsValueFromMDS,
      @RequestParam(required = false, value = "errorRequiredPDSValueFromMDS")
          String errorRequiredPDSValueFromMDS,
      @RequestParam(required = false, value = "errorProcessingManualMapping")
          String errorProcessingManualMapping) {

    newMDSProgramWorkflowStates = getNewMDSProgramWorkflowStates();
    differentIDsAndEqualUUID = this.getDifferentIDsAndEqualUUID();
    differentProgramWorkflowsOrConceptsAndSameUUIDAndID =
        this.getDifferentProgramWorkflowsOrConceptsAndSameUUIDAndID();
    HarmonizationData productionItemsToExport =
        delegate.getConvertedData(getProductionItemsToExport());

    session.setAttribute(
        "harmonizedProgramWorkflowStatesSummary",
        HarmonizeProgramWorkflowStatesDelegate.SUMMARY_EXECUTED_SCENARIOS);
    session.setAttribute("openmrs_msg", openmrs_msg);
    session.setAttribute("errorRequiredMdsValue", errorRequiredMdsValue);
    session.setAttribute("errorRequiredPDSValue", errorRequiredPDSValue);
    session.setAttribute("errorRequiredMdsValueFromMDS", errorRequiredMdsValueFromMDS);
    session.setAttribute("errorRequiredPDSValueFromMDS", errorRequiredPDSValueFromMDS);
    session.setAttribute("errorProcessingManualMapping", errorProcessingManualMapping);

    delegate.setHarmonizationStage(
        session,
        newMDSProgramWorkflowStates,
        productionItemsToDelete,
        productionItemsToExport,
        differentIDsAndEqualUUID,
        differentProgramWorkflowsOrConceptsAndSameUUIDAndID,
        notSwappableProgramWorkflowStates,
        swappableProgramWorkflowStates);

    session.removeAttribute("productionItemsToExport");
    session.setAttribute("productionItemsToExport", productionItemsToExport);

    ModelAndView modelAndView = new ModelAndView();
    model.addAttribute("newMDSProgramWorkflowStates", newMDSProgramWorkflowStates);
    model.addAttribute("productionItemsToExport", productionItemsToExport);
    model.addAttribute("differentIDsAndEqualUUID", differentIDsAndEqualUUID);
    model.addAttribute(
        "differentProgramWorkflowsOrConceptsAndSameUUIDAndID",
        differentProgramWorkflowsOrConceptsAndSameUUIDAndID);

    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP1, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep1(
      HttpSession session,
      @ModelAttribute("newMDSProgramWorkflowStates")
          HarmonizationData newMDSProgramWorkflowStates) {

    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
    Builder logBuilder = new ProgramWorkflowStatesHarmonizationCSVLog.Builder(defaultLocationName);

    delegate.processAddNewFromMetadataServer(newMDSProgramWorkflowStates, logBuilder);
    delegate.processDeleteFromProductionServer(getProductionItemsToDelete(), logBuilder);

    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    modelAndView.addObject("openmrs_msg", "eptsharmonization.programworkflow.harmonized");

    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP2, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep2(
      @ModelAttribute("differentIDsAndEqualUUID") HarmonizationData differentIDsAndEqualUUID) {

    HAS_ATLEAST_ONE_ROW_HARMONIZED = false;
    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
    Builder logBuilder = new ProgramWorkflowStatesHarmonizationCSVLog.Builder(defaultLocationName);
    delegate.processProgramWorkflowStatesWithDiferrentIdsAndEqualUUID(
        differentIDsAndEqualUUID, logBuilder);
    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    if (HAS_ATLEAST_ONE_ROW_HARMONIZED) {
      modelAndView.addObject("openmrs_msg", "eptsharmonization.programWorkflowState.harmonized");
    }
    IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = true;
    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP3, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep3(
      @ModelAttribute("differentProgramWorkflowsOrConceptsAndSameUUIDAndID")
          HarmonizationData differentProgramWorkflowsOrConceptsAndSameUUIDAndID) {

    HAS_ATLEAST_ONE_ROW_HARMONIZED = false;
    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
    Builder logBuilder = new ProgramWorkflowStatesHarmonizationCSVLog.Builder(defaultLocationName);
    delegate.processUpdateProgramWorkflowsAndConcepts(
        differentProgramWorkflowsOrConceptsAndSameUUIDAndID, logBuilder);
    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    if (HAS_ATLEAST_ONE_ROW_HARMONIZED) {
      modelAndView.addObject("openmrs_msg", "eptsharmonization.programworkflow.harmonized");
    }
    IS_NAMES_DIFFERENCES_HARMONIZED = true;
    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = PROCESS_HARMONIZATION_STEP4, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep4(
      HttpSession session,
      HttpServletRequest request,
      @ModelAttribute("swappableProgramWorkflowStates")
          List<ProgramWorkflowStateDTO> swappableProgramWorkflowStates,
      @ModelAttribute("mdsProgramWorkflowStateNotHarmonizedYet")
          List<ProgramWorkflowStateDTO> mdsProgramWorkflowStateNotHarmonizedYet)
      throws Exception {

    Map<ProgramWorkflowStateDTO, ProgramWorkflowStateDTO> manualHarmonizeProgramWorkflowStates =
        (Map<ProgramWorkflowStateDTO, ProgramWorkflowStateDTO>)
            session.getAttribute("manualHarmonizeProgramWorkflowStates");

    ModelAndView modelAndView = getRedirectModelAndView();

    if (manualHarmonizeProgramWorkflowStates != null
        && !manualHarmonizeProgramWorkflowStates.isEmpty()) {
      try {
        this.harmonizationProgramWorkflowStateService.saveManualMapping(
            manualHarmonizeProgramWorkflowStates);
      } catch (UUIDDuplicationException e) {

        for (Entry<ProgramWorkflowStateDTO, ProgramWorkflowStateDTO> entry :
            manualHarmonizeProgramWorkflowStates.entrySet()) {
          if (!swappableProgramWorkflowStates.contains(entry.getKey())) {
            swappableProgramWorkflowStates.add(entry.getKey());
          }
          if (!mdsProgramWorkflowStateNotHarmonizedYet.contains(entry.getKey())) {
            mdsProgramWorkflowStateNotHarmonizedYet.add(entry.getValue());
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
      Builder logBuilder =
          new ProgramWorkflowStatesHarmonizationCSVLog.Builder(defaultLocationName);
      logBuilder.appendNewMappedProgramWorkflowStates(manualHarmonizeProgramWorkflowStates);
      HarmonizeProgramWorkflowStatesDelegate.SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.encounterType.newDefinedMapping");
    }

    modelAndView.addObject("openmrs_msg", "eptsharmonization.programworkflow.harmonized");

    HarmonizeProgramWorkflowStatesDelegate.EXECUTED_PROGRAM_WORKFLOW_STATES_MANUALLY_CACHE =
        new ArrayList<>(manualHarmonizeProgramWorkflowStates.keySet());
    session.removeAttribute("manualHarmonizeProgramWorkflowStates");

    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = ADD_PROGRAM_WORKFLOW_STATE_MAPPING, method = RequestMethod.POST)
  public ModelAndView addProgramWorkflowStateMapping(
      HttpSession session,
      @ModelAttribute("swappableProgramWorkflowStates")
          List<ProgramWorkflowStateDTO> swappableProgramWorkflowStates,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem) {

    ModelAndView modelAndView = this.getRedirectModelAndView();

    if (harmonizationItem.getKey() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getKey()))) {
      modelAndView.addObject(
          "errorRequiredPDSValue",
          "eptsharmonization.error.programWorkflowStateForMapping.required");
      return modelAndView;
    }
    if (harmonizationItem.getValue() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getValue()))) {
      modelAndView.addObject(
          "errorRequiredMdsValue",
          "eptsharmonization.error.programWorkflowStateForMapping.required");
      return modelAndView;
    }

    ProgramWorkflowState pdsProgramWorkflowState =
        this.harmonizationProgramWorkflowStateService.findPDSProgramWorkflowStateByUuid(
            (String) harmonizationItem.getKey());
    ProgramWorkflowState mdsProgramWorkflowState =
        this.harmonizationProgramWorkflowStateService.findMDSProgramWorkflowStateByUuid(
            (String) harmonizationItem.getValue());

    Map<ProgramWorkflowStateDTO, ProgramWorkflowStateDTO> manualHarmonizeProgramWorkflowStates =
        (Map<ProgramWorkflowStateDTO, ProgramWorkflowStateDTO>)
            session.getAttribute("manualHarmonizeProgramWorkflowStates");

    if (manualHarmonizeProgramWorkflowStates == null) {
      manualHarmonizeProgramWorkflowStates = new HashMap<>();
    }

    final ProgramWorkflowStateDTO pdsProgramWorkflowStateDTO =
        DTOUtils.fromProgramWorkflowState(pdsProgramWorkflowState);
    final ProgramWorkflowStateDTO mdsProgramWorkflowStateDTO =
        DTOUtils.fromProgramWorkflowState(mdsProgramWorkflowState);
    // TODO: This is not removing the items from list: may be related to cache
    swappableProgramWorkflowStates.remove(pdsProgramWorkflowStateDTO);
    harmonizationProgramWorkflowStateService.setProgramWorkflowAndConcept(
        Arrays.asList(pdsProgramWorkflowStateDTO), false);
    harmonizationProgramWorkflowStateService.setProgramWorkflowAndConcept(
        Arrays.asList(mdsProgramWorkflowStateDTO), true);
    manualHarmonizeProgramWorkflowStates.put(
        pdsProgramWorkflowStateDTO, mdsProgramWorkflowStateDTO);
    session.setAttribute(
        "manualHarmonizeProgramWorkflowStates", manualHarmonizeProgramWorkflowStates);

    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = ADD_PROGRAM_WORKFLOW_STATE_FROM_MDS_MAPPING, method = RequestMethod.POST)
  public ModelAndView addProgramWorkflowStateFromMDSMapping(
      HttpSession session,
      @ModelAttribute("swappableProgramWorkflowStates")
          List<ProgramWorkflowStateDTO> swappableProgramWorkflowStates,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem,
      @ModelAttribute("mdsProgramWorkflowStateNotHarmonizedYet")
          List<ProgramWorkflowStateDTO> mdsProgramWorkflowStateNotHarmonizedYet) {

    ModelAndView modelAndView = this.getRedirectModelAndView();

    if (harmonizationItem.getValue() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getValue()))) {
      modelAndView.addObject(
          "errorRequiredMdsValueFromMDS",
          "eptsharmonization.error.programWorkflowForMapping.required");
      return modelAndView;
    }

    if (harmonizationItem.getKey() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getKey()))) {
      modelAndView.addObject(
          "errorRequiredPDSValueFromMDS",
          "eptsharmonization.error.programWorkflowForMapping.required");
      return modelAndView;
    }

    ProgramWorkflowStateDTO pdsProgramWorkflowState =
        DTOUtils.fromProgramWorkflowState(
            harmonizationProgramWorkflowStateService.findPDSProgramWorkflowStateByUuid(
                (String) harmonizationItem.getKey()));
    harmonizationProgramWorkflowStateService.setProgramWorkflowAndConcept(
        Arrays.asList(pdsProgramWorkflowState), false);

    String mdsPATUuid = (String) harmonizationItem.getValue();
    ProgramWorkflowStateDTO mdsProgramWorkflowState = null;
    for (ProgramWorkflowStateDTO programWorkflowState : mdsProgramWorkflowStateNotHarmonizedYet) {
      if (mdsPATUuid.equals(programWorkflowState.getUuid())) {
        mdsProgramWorkflowState = programWorkflowState;
        break;
      }
    }

    Map<ProgramWorkflowStateDTO, ProgramWorkflowStateDTO> manualHarmonizeProgramWorkflowStates =
        (Map<ProgramWorkflowStateDTO, ProgramWorkflowStateDTO>)
            session.getAttribute("manualHarmonizeProgramWorkflowStates");

    if (manualHarmonizeProgramWorkflowStates == null) {
      manualHarmonizeProgramWorkflowStates = new HashMap<>();
    }
    // TODO: This is not removing the items from list: may be related to cache?
    swappableProgramWorkflowStates.remove(pdsProgramWorkflowState);
    manualHarmonizeProgramWorkflowStates.put(pdsProgramWorkflowState, mdsProgramWorkflowState);
    session.setAttribute(
        "manualHarmonizeProgramWorkflowStates", manualHarmonizeProgramWorkflowStates);

    if (mdsProgramWorkflowStateNotHarmonizedYet != null
        && mdsProgramWorkflowStateNotHarmonizedYet.contains(mdsProgramWorkflowState)) {
      mdsProgramWorkflowStateNotHarmonizedYet.remove(mdsProgramWorkflowState);
    }

    return modelAndView;
  }

  @RequestMapping(value = REMOVE_PROGRAM_WORKFLOW_STATE_MAPPING, method = RequestMethod.POST)
  public ModelAndView removeProgramWorkflowStateMapping(
      HttpSession session,
      @ModelAttribute("swappableProgramWorkflowStates")
          List<ProgramWorkflowStateDTO> swappableProgramWorkflowStates,
      @ModelAttribute("notSwappableProgramWorkflowStates")
          List<ProgramWorkflowStateDTO> notSwappableProgramWorkflowStates,
      @ModelAttribute("mdsProgramWorkflowStateNotHarmonizedYet")
          List<ProgramWorkflowStateDTO> mdsProgramWorkflowStateNotHarmonizedYet,
      HttpServletRequest request) {

    ProgramWorkflowState productionProgramWorkflowState =
        this.harmonizationProgramWorkflowStateService.findPDSProgramWorkflowStateByUuid(
            request.getParameter("productionServerProgramWorkflowStateUuID"));
    final ProgramWorkflowStateDTO pdsProgramWorkflowStateDTO =
        DTOUtils.fromProgramWorkflowState(productionProgramWorkflowState);

    @SuppressWarnings("unchecked")
    Map<ProgramWorkflowStateDTO, ProgramWorkflowStateDTO> manualHarmonizeProgramWorkflowStates =
        (Map<ProgramWorkflowStateDTO, ProgramWorkflowStateDTO>)
            session.getAttribute("manualHarmonizeProgramWorkflowStates");

    ProgramWorkflowStateDTO mdsProgramWorkflowState =
        manualHarmonizeProgramWorkflowStates.get(pdsProgramWorkflowStateDTO);

    manualHarmonizeProgramWorkflowStates.remove(pdsProgramWorkflowStateDTO);
    harmonizationProgramWorkflowStateService.setProgramWorkflowAndConcept(
        Arrays.asList(pdsProgramWorkflowStateDTO), false);
    swappableProgramWorkflowStates.add(pdsProgramWorkflowStateDTO);

    if (notSwappableProgramWorkflowStates != null
        && !notSwappableProgramWorkflowStates.contains(mdsProgramWorkflowState)) {
      if (mdsProgramWorkflowStateNotHarmonizedYet != null
          && !mdsProgramWorkflowStateNotHarmonizedYet.contains(mdsProgramWorkflowState)) {
        mdsProgramWorkflowStateNotHarmonizedYet.add(mdsProgramWorkflowState);
      }
    }

    if (mdsProgramWorkflowStateNotHarmonizedYet != null) {
      this.sortByName(mdsProgramWorkflowStateNotHarmonizedYet);
    }
    if (swappableProgramWorkflowStates != null) {
      this.sortByName(swappableProgramWorkflowStates);
    }

    if (manualHarmonizeProgramWorkflowStates.isEmpty()) {
      session.removeAttribute("manualHarmonizeProgramWorkflowStates");
    }
    return this.getRedirectModelAndView();
  }

  @RequestMapping(value = EXPORT_LOG, method = RequestMethod.POST)
  public @ResponseBody byte[] exportLog(HttpServletRequest request, HttpServletResponse response)
      throws FileNotFoundException {

    File file = new File("harmonizationProgramWorkflowStatesLog");
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
        "attachment; fileName=programworkflowstates_harmonization_"
            + defaultLocationName
            + "-log.csv");
    response.setContentLength(outputStream.size());
    return outputStream.toByteArray();
  }

  @RequestMapping(value = EXPORT_PROGRAMWORKFLOWS, method = RequestMethod.POST)
  public @ResponseBody byte[] exportProgramWorkflowStates(
      HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model)
      throws FileNotFoundException {

    HarmonizationData productionItemsToExport =
        (HarmonizationData) session.getAttribute("productionItemsToExport");
    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");

    List<ProgramWorkflowStateDTO> list = new ArrayList<>();
    for (HarmonizationItem item : productionItemsToExport.getItems()) {
      list.add((ProgramWorkflowStateDTO) item.getValue());
    }

    ByteArrayOutputStream outputStream =
        ProgramWorkflowStatesHarmonizationCSVLog.exportProgramWorkflowStatesLogs(
            defaultLocationName, list, getNotSwappableProgramWorkflowStates());
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName=programworkflowstates_harmonization_"
            + defaultLocationName
            + "-export-log.csv");
    response.setContentLength(outputStream.size());
    return outputStream.toByteArray();
  }

  @ModelAttribute("productionItemsToDelete")
  public List<ProgramWorkflowStateDTO> getProductionItemsToDelete() {
    List<ProgramWorkflowStateDTO> productionItemsToDelete = new ArrayList<>();
    List<ProgramWorkflowStateDTO> onlyProductionProgramWorkflowStates =
        this.harmonizationProgramWorkflowStateService.findAllPDSStatesNotContainedInMDS();
    for (ProgramWorkflowStateDTO programWorkflowStateDTO : onlyProductionProgramWorkflowStates) {
      final int numberOfAffectedConceptStateConversions =
          this.harmonizationProgramWorkflowStateService.getNumberOfAffectedConceptStateConversions(
              programWorkflowStateDTO);
      final int numberOfAffectedPatientStates =
          this.harmonizationProgramWorkflowStateService.getNumberOfAffectedPatientStates(
              programWorkflowStateDTO);
      if (numberOfAffectedConceptStateConversions == 0 && numberOfAffectedPatientStates == 0) {
        productionItemsToDelete.add(programWorkflowStateDTO);
      }
    }
    return productionItemsToDelete;
  }

  public List<ProgramWorkflowStateDTO> getProductionItemsToExport() {
    List<ProgramWorkflowStateDTO> onlyProductionProgramWorkflowStates =
        this.harmonizationProgramWorkflowStateService.findAllPDSStatesNotContainedInMDS();

    List<ProgramWorkflowStateDTO> productionItemsToExport = new ArrayList<>();
    for (ProgramWorkflowStateDTO programWorkflowStateDTO : onlyProductionProgramWorkflowStates) {
      final int numberOfAffectedConceptStateConversions =
          this.harmonizationProgramWorkflowStateService.getNumberOfAffectedConceptStateConversions(
              programWorkflowStateDTO);
      final int numberOfAffectedProgramWorkflowStateStates =
          this.harmonizationProgramWorkflowStateService.getNumberOfAffectedPatientStates(
              programWorkflowStateDTO);
      if (numberOfAffectedConceptStateConversions > 0
          || numberOfAffectedProgramWorkflowStateStates > 0) {
        productionItemsToExport.add(programWorkflowStateDTO);
      }
    }
    return productionItemsToExport;
  }

  @ModelAttribute("harmonizationItem")
  HarmonizationItem formHarmonizationItem() {
    return new HarmonizationItem();
  }

  @ModelAttribute("newMDSProgramWorkflowStates")
  public HarmonizationData getNewMDSProgramWorkflowStates() {
    List<ProgramWorkflowStateDTO> data =
        this.harmonizationProgramWorkflowStateService.findAllMDSStatesNotContainedInPDS();
    return delegate.getConvertedData(data);
  }

  @ModelAttribute("differentIDsAndEqualUUID")
  public HarmonizationData getDifferentIDsAndEqualUUID() {
    Map<String, List<ProgramWorkflowStateDTO>> programWorkflowStatesWithDifferentIDsSameUUIDs =
        this.harmonizationProgramWorkflowStateService.findAllStatesWithDifferentIDAndSameUUID();
    return delegate.getConvertedData(programWorkflowStatesWithDifferentIDsSameUUIDs);
  }

  @ModelAttribute("differentProgramWorkflowsOrConceptsAndSameUUIDAndID")
  public HarmonizationData getDifferentProgramWorkflowsOrConceptsAndSameUUIDAndID() {
    Map<String, List<ProgramWorkflowStateDTO>> programWorkflowStatesWithDifferentNames =
        this.harmonizationProgramWorkflowStateService
            .findAllStatesWithDifferentWorkflowOrConceptAndSameUUIDAndID();
    return delegate.getConvertedData(programWorkflowStatesWithDifferentNames);
  }

  @ModelAttribute("swappableProgramWorkflowStates")
  public List<ProgramWorkflowStateDTO> getSwappableProgramWorkflowStates() {
    List<ProgramWorkflowStateDTO> swappableProgramWorkflowStates =
        DTOUtils.fromProgramWorkflowStates(
            harmonizationProgramWorkflowStateService.findAllSwappableProgramWorkflowStates());
    harmonizationProgramWorkflowStateService.setProgramWorkflowAndConcept(
        swappableProgramWorkflowStates, false);
    return sortByName(swappableProgramWorkflowStates);
  }

  @ModelAttribute("notSwappableProgramWorkflowStates")
  public List<ProgramWorkflowStateDTO> getNotSwappableProgramWorkflowStates() {
    final List<ProgramWorkflowStateDTO> programWorkflowStates =
        DTOUtils.fromProgramWorkflowStates(
            this.harmonizationProgramWorkflowStateService
                .findAllNotSwappableProgramWorkflowStates());
    harmonizationProgramWorkflowStateService.setProgramWorkflowAndConcept(
        programWorkflowStates, false);
    return sortByName(programWorkflowStates);
  }

  @ModelAttribute("mdsProgramWorkflowStateNotHarmonizedYet")
  public List<ProgramWorkflowStateDTO> getMDSProgramWorkflowStateNotHarmonizedYet() {
    return this.sortByName(
        HarmonizeProgramWorkflowStatesDelegate.MDS_PROGRAM_WORKFLOW_STATES_NOT_PROCESSED);
  }

  @SuppressWarnings("unchecked")
  private List<ProgramWorkflowStateDTO> sortByName(List<ProgramWorkflowStateDTO> list) {
    BeanComparator comparator = new BeanComparator("concept");
    Collections.sort(list, comparator);
    return list;
  }

  private ModelAndView getRedirectModelAndView() {
    return new ModelAndView("redirect:" + PROGRAM_WORKFLOWS_LIST + ".form");
  }
}
