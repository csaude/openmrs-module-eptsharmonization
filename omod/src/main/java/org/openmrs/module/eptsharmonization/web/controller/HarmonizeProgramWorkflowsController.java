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
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationProgramWorkflowService;
import org.openmrs.module.eptsharmonization.api.exception.UUIDDuplicationException;
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

  public static final String ADD_PROGRAM_WORKFLOW_MAPPING =
      HarmonizeProgramWorkflowsController.CONTROLLER_PATH + "/addProgramWorkflowMapping";

  public static final String ADD_PROGRAM_WORKFLOW_FROM_MDS_MAPPING =
      HarmonizeProgramWorkflowsController.CONTROLLER_PATH + "/addProgramWorkflowFromMDSMapping";

  public static final String REMOVE_PROGRAM_WORKFLOW_MAPPING =
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
      @ModelAttribute("mdsProgramWorkflowNotHarmonizedYet")
          List<ProgramWorkflowDTO> mdsProgramWorkflowNotHarmonizedYet,
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg,
      @RequestParam(required = false, value = "errorRequiredMdsValue") String errorRequiredMdsValue,
      @RequestParam(required = false, value = "errorRequiredPDSValue") String errorRequiredPDSValue,
      @RequestParam(required = false, value = "errorRequiredMdsValueFromMDS")
          String errorRequiredMdsValueFromMDS,
      @RequestParam(required = false, value = "errorRequiredPDSValueFromMDS")
          String errorRequiredPDSValueFromMDS,
      @RequestParam(required = false, value = "errorProcessingManualMapping")
          String errorProcessingManualMapping) {

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
    session.setAttribute("errorRequiredMdsValueFromMDS", errorRequiredMdsValueFromMDS);
    session.setAttribute("errorRequiredPDSValueFromMDS", errorRequiredPDSValueFromMDS);
    session.setAttribute("errorProcessingManualMapping", errorProcessingManualMapping);

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

    Builder logBuilder = new ProgramWorkflowsHarmonizationCSVLog.Builder(this.getDefaultLocation());

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
    Builder logBuilder = new ProgramWorkflowsHarmonizationCSVLog.Builder(this.getDefaultLocation());
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
    Builder logBuilder = new ProgramWorkflowsHarmonizationCSVLog.Builder(this.getDefaultLocation());
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
  public ModelAndView processHarmonizationStep4(
      HttpSession session,
      HttpServletRequest request,
      @ModelAttribute("swappableProgramWorkflows")
          List<ProgramWorkflowDTO> swappableProgramWorkflows,
      @ModelAttribute("mdsProgramWorkflowNotHarmonizedYet")
          List<ProgramWorkflowDTO> mdsProgramWorkflowNotHarmonizedYet)
      throws Exception {

    Map<ProgramWorkflowDTO, ProgramWorkflowDTO> manualHarmonizeProgramWorkflows =
        (Map<ProgramWorkflowDTO, ProgramWorkflowDTO>)
            session.getAttribute("manualHarmonizeProgramWorkflows");

    ModelAndView modelAndView = getRedirectModelAndView();

    if (manualHarmonizeProgramWorkflows != null && !manualHarmonizeProgramWorkflows.isEmpty()) {
      try {
        this.harmonizationProgramWorkflowService.saveManualMapping(manualHarmonizeProgramWorkflows);
      } catch (UUIDDuplicationException e) {

        for (Entry<ProgramWorkflowDTO, ProgramWorkflowDTO> entry :
            manualHarmonizeProgramWorkflows.entrySet()) {
          if (!swappableProgramWorkflows.contains(entry.getKey())) {
            swappableProgramWorkflows.add(entry.getKey());
          }
          if (!mdsProgramWorkflowNotHarmonizedYet.contains(entry.getKey())) {
            mdsProgramWorkflowNotHarmonizedYet.add(entry.getValue());
          }
        }

        modelAndView.addObject("errorProcessingManualMapping", e.getMessage());
        return modelAndView;
      } catch (SQLException e) {
        e.printStackTrace();
        throw new Exception(e);
      }

      Builder logBuilder =
          new ProgramWorkflowsHarmonizationCSVLog.Builder(this.getDefaultLocation());
      logBuilder.appendNewMappedProgramWorkflows(manualHarmonizeProgramWorkflows);
      HarmonizeProgramWorkflowsDelegate.SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.encounterType.newDefinedMapping");
    }
    modelAndView.addObject("openmrs_msg", "eptsharmonization.programworkflow.harmonized");

    HarmonizeProgramWorkflowsDelegate.EXECUTED_PROGRAM_WORKFLOWS_MANUALLY_CACHE =
        new ArrayList<>(manualHarmonizeProgramWorkflows.keySet());
    session.removeAttribute("manualHarmonizeProgramWorkflows");

    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = ADD_PROGRAM_WORKFLOW_MAPPING, method = RequestMethod.POST)
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

    ProgramWorkflowDTO pdsProgramWorkflowDTO = DTOUtils.fromProgramWorkflow(pdsProgramWorkflow);
    ProgramWorkflowDTO mdsProgramWorkflowDTO = DTOUtils.fromProgramWorkflow(mdsProgramWorkflow);
    harmonizationProgramWorkflowService.setProgramAndConceptNames(
        Arrays.asList(pdsProgramWorkflowDTO), false);
    harmonizationProgramWorkflowService.setProgramAndConceptNames(
        Arrays.asList(mdsProgramWorkflowDTO), true);
    // TODO: This is not removing the items from list: may be related to cache?
    swappableProgramWorkflows.remove(pdsProgramWorkflowDTO);
    manualHarmonizeProgramWorkflows.put(pdsProgramWorkflowDTO, mdsProgramWorkflowDTO);
    session.setAttribute("manualHarmonizeProgramWorkflows", manualHarmonizeProgramWorkflows);

    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = ADD_PROGRAM_WORKFLOW_FROM_MDS_MAPPING, method = RequestMethod.POST)
  public ModelAndView addProgramWorkflowFromMDSMapping(
      HttpSession session,
      @ModelAttribute("swappableProgramWorkflows")
          List<ProgramWorkflowDTO> swappableProgramWorkflows,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem,
      @ModelAttribute("mdsProgramWorkflowNotHarmonizedYet")
          List<ProgramWorkflowDTO> mdsProgramWorkflowNotHarmonizedYet) {

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

    ProgramWorkflowDTO pdsProgramWorkflow =
        DTOUtils.fromProgramWorkflow(
            harmonizationProgramWorkflowService.findProductionProgramWorkflowByUuid(
                (String) harmonizationItem.getKey()));
    harmonizationProgramWorkflowService.setProgramAndConceptNames(
        Arrays.asList(pdsProgramWorkflow), false);

    String mdsPATUuid = (String) harmonizationItem.getValue();
    ProgramWorkflowDTO mdsProgramWorkflow = null;
    for (ProgramWorkflowDTO programWorkflow : mdsProgramWorkflowNotHarmonizedYet) {
      if (mdsPATUuid.equals(programWorkflow.getUuid())) {
        mdsProgramWorkflow = programWorkflow;
        break;
      }
    }

    Map<ProgramWorkflowDTO, ProgramWorkflowDTO> manualHarmonizeProgramWorkflows =
        (Map<ProgramWorkflowDTO, ProgramWorkflowDTO>)
            session.getAttribute("manualHarmonizeProgramWorkflows");

    if (manualHarmonizeProgramWorkflows == null) {
      manualHarmonizeProgramWorkflows = new HashMap<>();
    }
    // TODO: This is not removing the items from list: may be related to cache?
    swappableProgramWorkflows.remove(pdsProgramWorkflow);
    manualHarmonizeProgramWorkflows.put(pdsProgramWorkflow, mdsProgramWorkflow);
    session.setAttribute("manualHarmonizeProgramWorkflows", manualHarmonizeProgramWorkflows);

    if (mdsProgramWorkflowNotHarmonizedYet != null
        && mdsProgramWorkflowNotHarmonizedYet.contains(mdsProgramWorkflow)) {
      mdsProgramWorkflowNotHarmonizedYet.remove(mdsProgramWorkflow);
    }

    return modelAndView;
  }

  @RequestMapping(value = REMOVE_PROGRAM_WORKFLOW_MAPPING, method = RequestMethod.POST)
  public ModelAndView removeProgramWorkflowMapping(
      HttpSession session,
      @ModelAttribute("swappableProgramWorkflows")
          List<ProgramWorkflowDTO> swappableProgramWorkflows,
      @ModelAttribute("notSwappableProgramWorkflows")
          List<ProgramWorkflowDTO> notSwappableProgramWorkflows,
      @ModelAttribute("mdsProgramWorkflowNotHarmonizedYet")
          List<ProgramWorkflowDTO> mdsProgramWorkflowNotHarmonizedYet,
      HttpServletRequest request) {

    ProgramWorkflow productionProgramWorkflow =
        this.harmonizationProgramWorkflowService.findProductionProgramWorkflowByUuid(
            request.getParameter("productionServerProgramWorkflowUuID"));
    final ProgramWorkflowDTO pdsProgramWorkflowDTO =
        DTOUtils.fromProgramWorkflow(productionProgramWorkflow);

    @SuppressWarnings("unchecked")
    Map<ProgramWorkflowDTO, ProgramWorkflowDTO> manualHarmonizeProgramWorkflows =
        (Map<ProgramWorkflowDTO, ProgramWorkflowDTO>)
            session.getAttribute("manualHarmonizeProgramWorkflows");

    ProgramWorkflowDTO mdsProgramWorkflow =
        manualHarmonizeProgramWorkflows.get(pdsProgramWorkflowDTO);

    manualHarmonizeProgramWorkflows.remove(pdsProgramWorkflowDTO);
    harmonizationProgramWorkflowService.setProgramAndConceptNames(
        Arrays.asList(pdsProgramWorkflowDTO), false);
    swappableProgramWorkflows.add(pdsProgramWorkflowDTO);

    if (notSwappableProgramWorkflows != null
        && !notSwappableProgramWorkflows.contains(mdsProgramWorkflow)) {
      if (mdsProgramWorkflowNotHarmonizedYet != null
          && !mdsProgramWorkflowNotHarmonizedYet.contains(mdsProgramWorkflow)) {
        mdsProgramWorkflowNotHarmonizedYet.add(mdsProgramWorkflow);
      }
    }

    if (mdsProgramWorkflowNotHarmonizedYet != null) {
      this.sortByName(mdsProgramWorkflowNotHarmonizedYet);
    }
    if (swappableProgramWorkflows != null) {
      this.sortByName(swappableProgramWorkflows);
    }

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
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName="
            + this.getFormattedLocationName(this.getDefaultLocation())
            + "-programworkflows_harmonization-log.csv");
    response.setContentLength(outputStream.size());
    return outputStream.toByteArray();
  }

  @RequestMapping(value = EXPORT_PROGRAMWORKFLOWS, method = RequestMethod.POST)
  public @ResponseBody byte[] exportProgramWorkflows(
      HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model)
      throws FileNotFoundException {

    HarmonizationData productionItemsToExport =
        (HarmonizationData) session.getAttribute("productionItemsToExport");
    String defaultLocationName = this.getDefaultLocation();

    List<ProgramWorkflowDTO> list = new ArrayList<>();
    for (HarmonizationItem item : productionItemsToExport.getItems()) {
      list.add((ProgramWorkflowDTO) item.getValue());
    }

    ByteArrayOutputStream outputStream =
        ProgramWorkflowsHarmonizationCSVLog.exportProgramWorkflowsLogs(
            defaultLocationName, list, getNotSwappableProgramWorkflows());
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName="
            + this.getFormattedLocationName(defaultLocationName)
            + "-programworkflows_harmonization-export-log.csv");
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
  HarmonizationItem formHarmonizationItem() {
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
    List<ProgramWorkflowDTO> swappableProgramWorkflows =
        DTOUtils.fromProgramWorkflows(
            harmonizationProgramWorkflowService.findAllSwappableProgramWorkflows());
    harmonizationProgramWorkflowService.setProgramAndConceptNames(swappableProgramWorkflows, false);
    return sortByName(swappableProgramWorkflows);
  }

  @ModelAttribute("notSwappableProgramWorkflows")
  public List<ProgramWorkflowDTO> getNotSwappableProgramWorkflows() {
    final List<ProgramWorkflowDTO> programWorkflows =
        DTOUtils.fromProgramWorkflows(
            this.harmonizationProgramWorkflowService.findAllNotSwappableProgramWorkflows());
    harmonizationProgramWorkflowService.setProgramAndConceptNames(programWorkflows, false);
    return sortByName(programWorkflows);
  }

  @ModelAttribute("mdsProgramWorkflowNotHarmonizedYet")
  public List<ProgramWorkflowDTO> getMDSProgramWorkflowNotHarmonizedYet() {
    return this.sortByName(HarmonizeProgramWorkflowsDelegate.MDS_PROGRAM_WORKFLOWS_NOT_PROCESSED);
  }

  @SuppressWarnings("unchecked")
  private List<ProgramWorkflowDTO> sortByName(List<ProgramWorkflowDTO> list) {
    BeanComparator comparator = new BeanComparator("concept");
    Collections.sort(list, comparator);
    return list;
  }

  private ModelAndView getRedirectModelAndView() {
    return new ModelAndView("redirect:" + PROGRAM_WORKFLOWS_LIST + ".form");
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
