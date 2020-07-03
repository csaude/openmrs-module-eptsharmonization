package org.openmrs.module.eptsharmonization.web.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationProgramWorkflowService;
import org.openmrs.module.eptsharmonization.api.model.ProgramWorkflowDTO;
import org.openmrs.module.eptsharmonization.web.EptsHarmonizationConstants;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationData;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationItem;
import org.openmrs.module.eptsharmonization.web.bean.ProgramWorkflowsHarmonizationCSVLog;
import org.openmrs.module.eptsharmonization.web.bean.ProgramWorkflowsHarmonizationCSVLog.Builder;
import org.openmrs.module.eptsharmonization.web.delegate.HarmonizeProgramWorkflowsDelegate;
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

@Controller("eptsharmonization.harmonizeProgramWorkflowController")
@SessionAttributes({"differentIDsAndEqualUUID", "differentProgramsOrConceptsAndSameUUIDAndID"})
public class HarmonizeProgramWorkflowsController {

  public static final String CONTROLLER_PATH =
      EptsHarmonizationConstants.MODULE_PATH + "/programWorkflows";

  public static final String PROGRAM_WORKFLOWS_LIST =
      HarmonizeProgramWorkflowsController.CONTROLLER_PATH + "/harmonizeProgramWorkflowsList";

  public static final String ADD_PROGRAM_MAPPING =
      HarmonizeProgramWorkflowsController.CONTROLLER_PATH + "/addProgramWorkflowMapping";

  public static final String REMOVE_PROGRAM_MAPPING =
      HarmonizeProgramWorkflowsController.CONTROLLER_PATH + "/removeProgramWorkflowMapping";

  public static final String PROCESS_HARMONIZATION_STEP1 =
      HarmonizeProgramWorkflowsController.CONTROLLER_PATH + "/processHarmonizationStep1";

  public static final String PROCESS_HARMONIZATION_STEP2 =
      HarmonizeProgramWorkflowsController.CONTROLLER_PATH + "/processHarmonizationStep2";

  public static final String PROCESS_HARMONIZATION_STEP3 =
      HarmonizeProgramWorkflowsController.CONTROLLER_PATH + "/processHarmonizationStep3";

  public static final String PROCESS_HARMONIZATION_STEP4 =
      HarmonizeProgramWorkflowsController.CONTROLLER_PATH + "/processHarmonizationStep4";

  public static final String EXPORT_PROGRAMWORKFLOWS =
      HarmonizeProgramWorkflowsController.CONTROLLER_PATH + "/harmonizeExportProgramWorkflows";

  public static final String EXPORT_LOG =
      HarmonizeProgramWorkflowsController.CONTROLLER_PATH
          + "/harmonizeProgramWorkflowsListExportLog";

  public static boolean HAS_ATLEAST_ONE_ROW_HARMONIZED = false;

  public static boolean IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = false;
  public static boolean IS_NAMES_DIFFERENCES_HARMONIZED = false;

  private HarmonizationProgramWorkflowService harmonizationProgramWorkflowService;

  private HarmonizeProgramWorkflowsDelegate delegate;

  @Autowired
  public void setHarmonizationProgramWorkflowService(
      HarmonizationProgramWorkflowService harmonizationProgramWorkflowService) {
    this.harmonizationProgramWorkflowService = harmonizationProgramWorkflowService;
  }

  @Autowired
  public void setDelegate(HarmonizeProgramWorkflowsDelegate delegate) {
    this.delegate = delegate;
  }

  @RequestMapping(value = PROGRAM_WORKFLOWS_LIST, method = RequestMethod.GET)
  public ModelAndView getHarmonizationScenariousData(
      HttpSession session,
      Model model,
      @ModelAttribute("newMDSProgramWorkflows") HarmonizationData newMDSProgramWorkflows,
      @ModelAttribute("productionItemsToDelete") List<ProgramWorkflowDTO> productionItemsToDelete,
      @ModelAttribute("differentIDsAndEqualUUID") HarmonizationData differentIDsAndEqualUUID,
      @ModelAttribute("differentProgramsOrConceptsAndSameUUIDAndID")
          HarmonizationData differentProgramsOrConceptsAndSameUUIDAndID,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem,
      @ModelAttribute("notSwappableProgramWorkflows")
          List<ProgramWorkflowDTO> notSwappableProgramWorkflows,
      @ModelAttribute("swappableProgramWorkflows")
          List<ProgramWorkflowDTO> swappableProgramWorkflows,
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg,
      @RequestParam(required = false, value = "errorRequiredMdsValue") String errorRequiredMdsValue,
      @RequestParam(required = false, value = "errorRequiredPDSValue")
          String errorRequiredPDSValue) {

    newMDSProgramWorkflows = getNewMDSProgramWorkflows();
    differentIDsAndEqualUUID = this.getDifferentIDsAndEqualUUID();
    differentProgramsOrConceptsAndSameUUIDAndID =
        this.getDifferentProgramsOrConceptsAndSameUUIDAndID();
    HarmonizationData productionItemsToExport =
        delegate.getConvertedData(getProductionItemsToExport());

    session.setAttribute(
        "harmonizedProgramWorkflowsSummary",
        HarmonizeProgramWorkflowsDelegate.SUMMARY_EXECUTED_SCENARIOS);
    session.setAttribute("openmrs_msg", openmrs_msg);
    session.setAttribute("errorRequiredMdsValue", errorRequiredMdsValue);
    session.setAttribute("errorRequiredPDSValue", errorRequiredPDSValue);

    delegate.setHarmonizationStage(
        session,
        newMDSProgramWorkflows,
        productionItemsToDelete,
        productionItemsToExport,
        differentIDsAndEqualUUID,
        differentProgramsOrConceptsAndSameUUIDAndID,
        notSwappableProgramWorkflows,
        swappableProgramWorkflows);

    session.removeAttribute("productionItemsToExport");
    session.setAttribute("productionItemsToExport", productionItemsToExport);

    ModelAndView modelAndView = new ModelAndView();
    model.addAttribute("newMDSProgramWorkflows", newMDSProgramWorkflows);
    model.addAttribute("productionItemsToExport", productionItemsToExport);
    model.addAttribute("differentIDsAndEqualUUID", differentIDsAndEqualUUID);
    model.addAttribute(
        "differentProgramsOrConceptsAndSameUUIDAndID", differentProgramsOrConceptsAndSameUUIDAndID);

    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP1, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep1(
      HttpSession session,
      @ModelAttribute("newMDSProgramWorkflows") HarmonizationData newMDSProgramWorkflows,
      @ModelAttribute("productionItemsToDelete") List<ProgramWorkflowDTO> productionItemsToDelete) {

    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
    Builder logBuilder = new ProgramWorkflowsHarmonizationCSVLog.Builder(defaultLocationName);

    delegate.processAddNewFromMetadataServer(newMDSProgramWorkflows, logBuilder);
    delegate.processDeleteFromProductionServer(productionItemsToDelete, logBuilder);

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
    Builder logBuilder = new ProgramWorkflowsHarmonizationCSVLog.Builder(defaultLocationName);
    delegate.processProgramWorkflowsWithDiferrentIdsAndEqualUUID(
        differentIDsAndEqualUUID, logBuilder);
    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    if (HAS_ATLEAST_ONE_ROW_HARMONIZED) {
      modelAndView.addObject("openmrs_msg", "eptsharmonization.programWorkflow.harmonized");
    }
    IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = true;
    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP3, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep3(
      @ModelAttribute("differentProgramsOrConceptsAndSameUUIDAndID")
          HarmonizationData differentProgramsOrConceptsAndSameUUIDAndID) {

    HAS_ATLEAST_ONE_ROW_HARMONIZED = false;
    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
    Builder logBuilder = new ProgramWorkflowsHarmonizationCSVLog.Builder(defaultLocationName);
    delegate.processUpdateProgramWorkflowsProgramsAndConcepts(
        differentProgramsOrConceptsAndSameUUIDAndID, logBuilder);
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
  public ModelAndView processHarmonizationStep4(HttpSession session, HttpServletRequest request) {

    Map<ProgramWorkflowDTO, ProgramWorkflowDTO> manualHarmonizeProgramWorkflows =
        (Map<ProgramWorkflowDTO, ProgramWorkflowDTO>)
            session.getAttribute("manualHarmonizeProgramWorkflows");

    if (manualHarmonizeProgramWorkflows != null && !manualHarmonizeProgramWorkflows.isEmpty()) {
      String defaultLocationName =
          Context.getAdministrationService().getGlobalProperty("default_location");
      Builder logBuilder = new ProgramWorkflowsHarmonizationCSVLog.Builder(defaultLocationName);
      delegate.processManualMapping(manualHarmonizeProgramWorkflows, logBuilder);
      HarmonizeProgramWorkflowsDelegate.SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.encounterType.newDefinedMapping");
    }
    ModelAndView modelAndView = getRedirectModelAndView();
    modelAndView.addObject("openmrs_msg", "eptsharmonization.programworkflow.harmonized");

    HarmonizeProgramWorkflowsDelegate.EXECUTED_PROGRAM_WORKFLOWS_MANUALLY_CACHE =
        new ArrayList<>(manualHarmonizeProgramWorkflows.keySet());
    session.removeAttribute("manualHarmonizeProgramWorkflows");

    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = ADD_PROGRAM_MAPPING, method = RequestMethod.POST)
  public ModelAndView addProgramWorkflowMapping(
      HttpSession session,
      @ModelAttribute("swappableProgramWorkflows")
          List<ProgramWorkflowDTO> swappableProgramWorkflows,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem) {

    ModelAndView modelAndView = this.getRedirectModelAndView();

    if (harmonizationItem.getKey() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getKey()))) {
      modelAndView.addObject(
          "errorRequiredPDSValue", "eptsharmonization.error.programWorkflowForMapping.required");
      return modelAndView;
    }
    if (harmonizationItem.getValue() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getValue()))) {
      modelAndView.addObject(
          "errorRequiredMdsValue", "eptsharmonization.error.programWorkflowForMapping.required");
      return modelAndView;
    }

    ProgramWorkflow pdsProgramWorkflow =
        this.harmonizationProgramWorkflowService.findProductionProgramWorkflowByUuid(
            (String) harmonizationItem.getKey());
    ProgramWorkflow mdsProgramWorkflow =
        this.harmonizationProgramWorkflowService.findMetadataProgramWorkflowByUuid(
            (String) harmonizationItem.getValue());

    Map<ProgramWorkflowDTO, ProgramWorkflowDTO> manualHarmonizeProgramWorkflows =
        (Map<ProgramWorkflowDTO, ProgramWorkflowDTO>)
            session.getAttribute("manualHarmonizeProgramWorkflows");

    if (manualHarmonizeProgramWorkflows == null) {
      manualHarmonizeProgramWorkflows = new HashMap<>();
    }

    final ProgramWorkflowDTO pdsProgramWorkflowDTO =
        DTOUtils.fromProgramWorkflow(pdsProgramWorkflow);
    final ProgramWorkflowDTO mdsProgramWorkflowDTO =
        DTOUtils.fromProgramWorkflow(mdsProgramWorkflow);
    // TODO: This is not removing the items from list: may be related to cache
    swappableProgramWorkflows.remove(pdsProgramWorkflowDTO);
    harmonizationProgramWorkflowService.setProgramAndConceptNames(
        Arrays.asList(pdsProgramWorkflowDTO), false);
    harmonizationProgramWorkflowService.setProgramAndConceptNames(
        Arrays.asList(mdsProgramWorkflowDTO), true);
    manualHarmonizeProgramWorkflows.put(pdsProgramWorkflowDTO, mdsProgramWorkflowDTO);
    session.setAttribute("manualHarmonizeProgramWorkflows", manualHarmonizeProgramWorkflows);

    return modelAndView;
  }

  @RequestMapping(value = REMOVE_PROGRAM_MAPPING, method = RequestMethod.POST)
  public ModelAndView removeProgramWorkflowMapping(
      HttpSession session,
      @ModelAttribute("swappableProgramWorkflows")
          List<ProgramWorkflowDTO> swappableProgramWorkflows,
      HttpServletRequest request) {

    ProgramWorkflow productionProgramWorkflow =
        this.harmonizationProgramWorkflowService.findProductionProgramWorkflowByUuid(
            request.getParameter("productionServerProgramWorkflowUuID"));

    @SuppressWarnings("unchecked")
    Map<ProgramWorkflowDTO, ProgramWorkflowDTO> manualHarmonizeProgramWorkflows =
        (Map<ProgramWorkflowDTO, ProgramWorkflowDTO>)
            session.getAttribute("manualHarmonizeProgramWorkflows");

    final ProgramWorkflowDTO pdsProgramWorkflowDTO =
        DTOUtils.fromProgramWorkflow(productionProgramWorkflow);
    manualHarmonizeProgramWorkflows.remove(pdsProgramWorkflowDTO);
    harmonizationProgramWorkflowService.setProgramAndConceptNames(
        Arrays.asList(pdsProgramWorkflowDTO), false);
    swappableProgramWorkflows.add(pdsProgramWorkflowDTO);

    if (manualHarmonizeProgramWorkflows.isEmpty()) {
      session.removeAttribute("manualHarmonizeProgramWorkflows");
    }
    return this.getRedirectModelAndView();
  }

  @RequestMapping(value = EXPORT_LOG, method = RequestMethod.POST)
  public @ResponseBody byte[] exportLog(HttpServletRequest request, HttpServletResponse response)
      throws FileNotFoundException {

    File file = new File("harmonizationProgramWorkflowsLog");
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
        "attachment; fileName=programworkflows_harmonization_" + defaultLocationName + "-log.csv");
    response.setContentLength(outputStream.size());
    return outputStream.toByteArray();
  }

  @RequestMapping(value = EXPORT_PROGRAMWORKFLOWS, method = RequestMethod.POST)
  public @ResponseBody byte[] exportProgramWorkflows(
      HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model)
      throws FileNotFoundException {

    HarmonizationData productionItemsToExport =
        (HarmonizationData) session.getAttribute("productionItemsToExport");
    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");

    List<ProgramWorkflowDTO> list = new ArrayList<>();
    for (HarmonizationItem item : productionItemsToExport.getItems()) {
      list.add((ProgramWorkflowDTO) item.getValue());
    }

    ByteArrayOutputStream outputStream =
        ProgramWorkflowsHarmonizationCSVLog.exportProgramWorkflowsLogs(defaultLocationName, list);
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName=programworkflows_harmonization_"
            + defaultLocationName
            + "-export-log.csv");
    response.setContentLength(outputStream.size());
    return outputStream.toByteArray();
  }

  @ModelAttribute("productionItemsToDelete")
  public List<ProgramWorkflowDTO> getProductionItemsToDelete() {
    List<ProgramWorkflowDTO> productionItemsToDelete = new ArrayList<>();
    List<ProgramWorkflowDTO> onlyProductionProgramWorkflows =
        this.harmonizationProgramWorkflowService
            .findAllProductionProgramWorkflowsNotContainedInMetadataServer();
    for (ProgramWorkflowDTO programWorkflowDTO : onlyProductionProgramWorkflows) {
      final int numberOfAffectedConceptStateConversions =
          this.harmonizationProgramWorkflowService.getNumberOfAffectedConceptStateConversions(
              programWorkflowDTO);
      final int numberOfAffectedProgramWorkflowStates =
          this.harmonizationProgramWorkflowService.getNumberOfAffectedProgramWorkflowStates(
              programWorkflowDTO);
      if (numberOfAffectedConceptStateConversions == 0
          && numberOfAffectedProgramWorkflowStates == 0) {
        productionItemsToDelete.add(programWorkflowDTO);
      }
    }
    return productionItemsToDelete;
  }

  public List<ProgramWorkflowDTO> getProductionItemsToExport() {
    List<ProgramWorkflowDTO> onlyProductionProgramWorkflows =
        this.harmonizationProgramWorkflowService
            .findAllProductionProgramWorkflowsNotContainedInMetadataServer();

    List<ProgramWorkflowDTO> productionItemsToExport = new ArrayList<>();
    for (ProgramWorkflowDTO programWorkflowDTO : onlyProductionProgramWorkflows) {
      final int numberOfAffectedConceptStateConversions =
          this.harmonizationProgramWorkflowService.getNumberOfAffectedConceptStateConversions(
              programWorkflowDTO);
      final int numberOfAffectedProgramWorkflowStates =
          this.harmonizationProgramWorkflowService.getNumberOfAffectedProgramWorkflowStates(
              programWorkflowDTO);
      if (numberOfAffectedConceptStateConversions > 0
          || numberOfAffectedProgramWorkflowStates > 0) {
        productionItemsToExport.add(programWorkflowDTO);
      }
    }
    return productionItemsToExport;
  }

  @ModelAttribute("harmonizationItem")
  HarmonizationItem formBackingObject() {
    return new HarmonizationItem();
  }

  @ModelAttribute("newMDSProgramWorkflows")
  public HarmonizationData getNewMDSProgramWorkflows() {
    List<ProgramWorkflowDTO> data =
        this.harmonizationProgramWorkflowService
            .findAllMetadataProgramWorkflowsNotContainedInProductionServer();
    return delegate.getConvertedData(data);
  }

  @ModelAttribute("differentIDsAndEqualUUID")
  public HarmonizationData getDifferentIDsAndEqualUUID() {
    Map<String, List<ProgramWorkflowDTO>> programWorkflowsWithDifferentIDsSameUUIDs =
        this.harmonizationProgramWorkflowService
            .findAllProgramWorkflowsWithDifferentIDAndSameUUID();
    return delegate.getConvertedData(programWorkflowsWithDifferentIDsSameUUIDs);
  }

  @ModelAttribute("differentProgramsOrConceptsAndSameUUIDAndID")
  public HarmonizationData getDifferentProgramsOrConceptsAndSameUUIDAndID() {
    Map<String, List<ProgramWorkflowDTO>> programWorkflowsWithDifferentNames =
        this.harmonizationProgramWorkflowService
            .findAllProgramWorkflowsWithDifferentProgramOrConceptAndSameUUIDAndID();
    return delegate.getConvertedData(programWorkflowsWithDifferentNames);
  }

  @ModelAttribute("swappableProgramWorkflows")
  public List<ProgramWorkflowDTO> getSwappableProgramWorkflows() {

    List<ProgramWorkflowDTO> swappableProgramWorkflows = new ArrayList<>();
    List<ProgramWorkflowDTO> productionItemsToExport = getProductionItemsToExport();
    productionItemsToExport.addAll(
        HarmonizeProgramWorkflowsDelegate.PROGRAM_WORKFLOWS_NOT_PROCESSED);
    final List<ProgramWorkflowDTO> programWorkflows =
        DTOUtils.fromProgramWorkflows(
            this.harmonizationProgramWorkflowService.findAllSwappableProgramWorkflows());
    for (ProgramWorkflowDTO programWorkflow : programWorkflows) {
      if (productionItemsToExport.contains(programWorkflow)) {
        swappableProgramWorkflows.add(programWorkflow);
      }
    }
    harmonizationProgramWorkflowService.setProgramAndConceptNames(swappableProgramWorkflows, false);
    return swappableProgramWorkflows;
  }

  @ModelAttribute("notSwappableProgramWorkflows")
  public List<ProgramWorkflowDTO> getNotSwappableProgramWorkflows() {
    final List<ProgramWorkflowDTO> programWorkflows =
        DTOUtils.fromProgramWorkflows(
            this.harmonizationProgramWorkflowService.findAllMetadataProgramWorkflows());
    harmonizationProgramWorkflowService.setProgramAndConceptNames(programWorkflows, true);
    return programWorkflows;
  }

  private ModelAndView getRedirectModelAndView() {
    return new ModelAndView("redirect:" + PROGRAM_WORKFLOWS_LIST + ".form");
  }
}
