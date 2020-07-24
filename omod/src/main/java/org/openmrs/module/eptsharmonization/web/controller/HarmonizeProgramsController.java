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
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsharmonization.api.HarmonizationProgramService;
import org.openmrs.module.eptsharmonization.api.exception.UUIDDuplicationException;
import org.openmrs.module.eptsharmonization.api.model.ProgramDTO;
import org.openmrs.module.eptsharmonization.web.EptsHarmonizationConstants;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationData;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationItem;
import org.openmrs.module.eptsharmonization.web.bean.ProgramsHarmonizationCSVLog;
import org.openmrs.module.eptsharmonization.web.bean.ProgramsHarmonizationCSVLog.Builder;
import org.openmrs.module.eptsharmonization.web.delegate.HarmonizeProgramsDelegate;
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

@Controller("eptsharmonization.harmonizeProgramController")
@SessionAttributes({"differentIDsAndEqualUUID", "differentNameAndSameUUIDAndID"})
public class HarmonizeProgramsController {

  public static final String CONTROLLER_PATH = EptsHarmonizationConstants.MODULE_PATH + "/programs";

  public static final String PROGRAMS_LIST =
      HarmonizeProgramsController.CONTROLLER_PATH + "/harmonizeProgramsList";

  public static final String ADD_PROGRAM_MAPPING =
      HarmonizeProgramsController.CONTROLLER_PATH + "/addProgramMapping";

  public static final String ADD_PROGRAM_FROM_MDS_MAPPING =
      HarmonizeProgramsController.CONTROLLER_PATH + "/addProgramFromMDSMapping";

  public static final String REMOVE_PROGRAM_MAPPING =
      HarmonizeProgramsController.CONTROLLER_PATH + "/removeProgramMapping";

  public static final String PROCESS_HARMONIZATION_STEP1 =
      HarmonizeProgramsController.CONTROLLER_PATH + "/processHarmonizationStep1";

  public static final String PROCESS_HARMONIZATION_STEP2 =
      HarmonizeProgramsController.CONTROLLER_PATH + "/processHarmonizationStep2";

  public static final String PROCESS_HARMONIZATION_STEP3 =
      HarmonizeProgramsController.CONTROLLER_PATH + "/processHarmonizationStep3";

  public static final String PROCESS_HARMONIZATION_STEP4 =
      HarmonizeProgramsController.CONTROLLER_PATH + "/processHarmonizationStep4";

  public static final String EXPORT_PROGRAMS =
      HarmonizeProgramsController.CONTROLLER_PATH + "/harmonizeExportPrograms";

  public static final String EXPORT_LOG =
      HarmonizeProgramsController.CONTROLLER_PATH + "/harmonizeProgramsListExportLog";

  public static boolean HAS_ATLEAST_ONE_ROW_HARMONIZED = false;

  public static boolean IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = false;
  public static boolean IS_NAMES_DIFFERENCES_HARMONIZED = false;

  private HarmonizationProgramService harmonizationProgramService;

  private HarmonizeProgramsDelegate delegate;

  @Autowired
  public void setHarmonizationProgramService(
      HarmonizationProgramService harmonizationProgramService) {
    this.harmonizationProgramService = harmonizationProgramService;
  }

  @Autowired
  public void setDelegate(HarmonizeProgramsDelegate delegate) {
    this.delegate = delegate;
  }

  @RequestMapping(value = PROGRAMS_LIST, method = RequestMethod.GET)
  public ModelAndView getHarmonizationScenariousData(
      HttpSession session,
      Model model,
      @ModelAttribute("newMDSPrograms") HarmonizationData newMDSPrograms,
      @ModelAttribute("productionItemsToDelete") List<ProgramDTO> productionItemsToDelete,
      @ModelAttribute("differentIDsAndEqualUUID") HarmonizationData differentIDsAndEqualUUID,
      @ModelAttribute("differentNameAndSameUUIDAndID")
          HarmonizationData differentNameAndSameUUIDAndID,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem,
      @ModelAttribute("notSwappablePrograms") List<Program> notSwappablePrograms,
      @ModelAttribute("swappablePrograms") List<Program> swappablePrograms,
      @ModelAttribute("mdsProgramNotHarmonizedYet") List<Program> mdsProgramNotHarmonizedYet,
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg,
      @RequestParam(required = false, value = "errorRequiredMdsValue") String errorRequiredMdsValue,
      @RequestParam(required = false, value = "errorRequiredPDSValue") String errorRequiredPDSValue,
      @RequestParam(required = false, value = "errorRequiredMdsValueFromMDS")
          String errorRequiredMdsValueFromMDS,
      @RequestParam(required = false, value = "errorRequiredPDSValueFromMDS")
          String errorRequiredPDSValueFromMDS,
      @RequestParam(required = false, value = "errorProcessingManualMapping")
          String errorProcessingManualMapping) {

    // TODO: I did this fetch as a workaround to prevent having cached data
    newMDSPrograms = getNewMDSPrograms();
    differentIDsAndEqualUUID = this.getDifferentIDsAndEqualUUID();
    differentNameAndSameUUIDAndID = this.getDifferentNameAndSameUUIDAndID();
    HarmonizationData productionItemsToExport =
        delegate.getConvertedData(getProductionItemsToExport());

    session.setAttribute(
        "harmonizedProgramsSummary", HarmonizeProgramsDelegate.SUMMARY_EXECUTED_SCENARIOS);
    session.setAttribute("openmrs_msg", openmrs_msg);
    session.setAttribute("errorRequiredMdsValue", errorRequiredMdsValue);
    session.setAttribute("errorRequiredPDSValue", errorRequiredPDSValue);
    session.setAttribute("errorRequiredMdsValueFromMDS", errorRequiredMdsValueFromMDS);
    session.setAttribute("errorRequiredPDSValueFromMDS", errorRequiredPDSValueFromMDS);
    session.setAttribute("errorProcessingManualMapping", errorProcessingManualMapping);

    delegate.setHarmonizationStage(
        session,
        newMDSPrograms,
        productionItemsToDelete,
        productionItemsToExport,
        differentIDsAndEqualUUID,
        differentNameAndSameUUIDAndID,
        notSwappablePrograms,
        swappablePrograms);

    session.removeAttribute("productionItemsToExport");
    session.setAttribute("productionItemsToExport", productionItemsToExport);

    ModelAndView modelAndView = new ModelAndView();
    model.addAttribute("newMDSPrograms", newMDSPrograms);
    model.addAttribute("productionItemsToExport", productionItemsToExport);
    model.addAttribute("differentIDsAndEqualUUID", differentIDsAndEqualUUID);
    model.addAttribute("differentNameAndSameUUIDAndID", differentNameAndSameUUIDAndID);

    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP1, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep1(
      HttpSession session,
      @ModelAttribute("newMDSPrograms") HarmonizationData newMDSPrograms,
      @ModelAttribute("productionItemsToDelete") List<ProgramDTO> productionItemsToDelete) {

    Builder logBuilder = new ProgramsHarmonizationCSVLog.Builder(this.getDefaultLocation());

    delegate.processAddNewFromMetadataServer(newMDSPrograms, logBuilder);
    delegate.processDeleteFromProductionServer(productionItemsToDelete, logBuilder);

    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    modelAndView.addObject("openmrs_msg", "eptsharmonization.program.harmonized");

    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP2, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep2(
      @ModelAttribute("differentIDsAndEqualUUID") HarmonizationData differentIDsAndEqualUUID) {

    HAS_ATLEAST_ONE_ROW_HARMONIZED = false;
    Builder logBuilder = new ProgramsHarmonizationCSVLog.Builder(this.getDefaultLocation());
    delegate.processProgramsWithDiferrentIdsAndEqualUUID(differentIDsAndEqualUUID, logBuilder);
    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    if (HAS_ATLEAST_ONE_ROW_HARMONIZED) {
      modelAndView.addObject("openmrs_msg", "eptsharmonization.program.harmonized");
    }
    IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = true;
    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP3, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep3(
      @ModelAttribute("differentNameAndSameUUIDAndID")
          HarmonizationData differentNameAndSameUUIDAndID) {

    HAS_ATLEAST_ONE_ROW_HARMONIZED = false;
    Builder logBuilder = new ProgramsHarmonizationCSVLog.Builder(this.getDefaultLocation());
    delegate.processUpdateProgramsNames(differentNameAndSameUUIDAndID, logBuilder);
    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    if (HAS_ATLEAST_ONE_ROW_HARMONIZED) {
      modelAndView.addObject("openmrs_msg", "eptsharmonization.program.harmonized");
    }
    IS_NAMES_DIFFERENCES_HARMONIZED = true;
    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = PROCESS_HARMONIZATION_STEP4, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep4(
      HttpSession session,
      HttpServletRequest request,
      @ModelAttribute("swappablePrograms") List<Program> swappablePrograms,
      @ModelAttribute("mdsProgramNotHarmonizedYet") List<Program> mdsProgramNotHarmonizedYet)
      throws Exception {

    Map<Program, Program> manualHarmonizePrograms =
        (Map<Program, Program>) session.getAttribute("manualHarmonizePrograms");

    ModelAndView modelAndView = getRedirectModelAndView();

    if (manualHarmonizePrograms != null && !manualHarmonizePrograms.isEmpty()) {

      try {
        this.harmonizationProgramService.saveManualMapping(manualHarmonizePrograms);
      } catch (UUIDDuplicationException e) {

        for (Entry<Program, Program> entry : manualHarmonizePrograms.entrySet()) {
          if (!swappablePrograms.contains(entry.getKey())) {
            swappablePrograms.add(entry.getKey());
          }
          if (!mdsProgramNotHarmonizedYet.contains(entry.getKey())) {
            mdsProgramNotHarmonizedYet.add(entry.getValue());
          }
        }

        modelAndView.addObject("errorProcessingManualMapping", e.getMessage());
        return modelAndView;
      } catch (SQLException e) {
        e.printStackTrace();
        throw new Exception(e);
      }
      Builder logBuilder = new ProgramsHarmonizationCSVLog.Builder(this.getDefaultLocation());
      logBuilder.appendNewMappedPrograms(manualHarmonizePrograms);
      HarmonizeProgramsDelegate.SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.encounterType.newDefinedMapping");
    }

    modelAndView.addObject("openmrs_msg", "eptsharmonization.program.harmonized");

    HarmonizeProgramsDelegate.EXECUTED_PROGRAMS_MANUALLY_CACHE =
        new ArrayList<>(manualHarmonizePrograms.keySet());
    session.removeAttribute("manualHarmonizePrograms");

    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = ADD_PROGRAM_MAPPING, method = RequestMethod.POST)
  public ModelAndView addProgramMapping(
      HttpSession session,
      @ModelAttribute("swappablePrograms") List<Program> swappablePrograms,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem) {

    ModelAndView modelAndView = this.getRedirectModelAndView();

    if (harmonizationItem.getKey() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getKey()))) {
      modelAndView.addObject(
          "errorRequiredPDSValue", "eptsharmonization.error.programForMapping.required");
      return modelAndView;
    }
    if (harmonizationItem.getValue() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getValue()))) {
      modelAndView.addObject(
          "errorRequiredMdsValue", "eptsharmonization.error.programForMapping.required");
      return modelAndView;
    }

    Program pdsProgram =
        this.harmonizationProgramService.findProductionProgramByUuid(
            (String) harmonizationItem.getKey());
    Program mdsProgram =
        this.harmonizationProgramService.findMetadataProgramByUuid(
            (String) harmonizationItem.getValue());

    Map<Program, Program> manualHarmonizePrograms =
        (Map<Program, Program>) session.getAttribute("manualHarmonizePrograms");

    if (manualHarmonizePrograms == null) {
      manualHarmonizePrograms = new HashMap<>();
    }
    swappablePrograms.remove(pdsProgram);
    manualHarmonizePrograms.put(pdsProgram, mdsProgram);
    session.setAttribute("manualHarmonizePrograms", manualHarmonizePrograms);

    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = ADD_PROGRAM_FROM_MDS_MAPPING, method = RequestMethod.POST)
  public ModelAndView addProgramFromMDSMapping(
      HttpSession session,
      @ModelAttribute("swappablePrograms") List<Program> swappablePrograms,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem,
      @ModelAttribute("mdsProgramNotHarmonizedYet") List<Program> mdsProgramNotHarmonizedYet) {

    ModelAndView modelAndView = this.getRedirectModelAndView();

    if (harmonizationItem.getValue() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getValue()))) {
      modelAndView.addObject(
          "errorRequiredMdsValueFromMDS", "eptsharmonization.error.programForMapping.required");
      return modelAndView;
    }

    if (harmonizationItem.getKey() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getKey()))) {
      modelAndView.addObject(
          "errorRequiredPDSValueFromMDS", "eptsharmonization.error.programForMapping.required");
      return modelAndView;
    }

    Program pdsProgram =
        Context.getProgramWorkflowService().getProgramByUuid((String) harmonizationItem.getKey());

    String mdsPATUuid = (String) harmonizationItem.getValue();
    Program mdsProgram = null;
    for (Program personAttributeType : mdsProgramNotHarmonizedYet) {
      if (mdsPATUuid.equals(personAttributeType.getUuid())) {
        mdsProgram = personAttributeType;
        break;
      }
    }

    Map<Program, Program> manualHarmonizePrograms =
        (Map<Program, Program>) session.getAttribute("manualHarmonizePrograms");

    if (manualHarmonizePrograms == null) {
      manualHarmonizePrograms = new HashMap<>();
    }
    swappablePrograms.remove(pdsProgram);
    manualHarmonizePrograms.put(pdsProgram, mdsProgram);
    session.setAttribute("manualHarmonizePrograms", manualHarmonizePrograms);

    if (mdsProgramNotHarmonizedYet != null && mdsProgramNotHarmonizedYet.contains(mdsProgram)) {
      mdsProgramNotHarmonizedYet.remove(mdsProgram);
    }

    return modelAndView;
  }

  @RequestMapping(value = REMOVE_PROGRAM_MAPPING, method = RequestMethod.POST)
  public ModelAndView removeProgramMapping(
      HttpSession session,
      @ModelAttribute("swappablePrograms") List<Program> swappablePrograms,
      @ModelAttribute("notSwappablePrograms") List<Program> notSwappablePrograms,
      @ModelAttribute("mdsProgramNotHarmonizedYet") List<Program> mdsProgramNotHarmonizedYet,
      HttpServletRequest request) {

    Program productionProgram =
        this.harmonizationProgramService.findProductionProgramByUuid(
            request.getParameter("productionServerProgramUuID"));

    @SuppressWarnings("unchecked")
    Map<Program, Program> manualHarmonizePrograms =
        (Map<Program, Program>) session.getAttribute("manualHarmonizePrograms");

    Program mdsProgram = manualHarmonizePrograms.get(productionProgram);

    manualHarmonizePrograms.remove(productionProgram);
    swappablePrograms.add(productionProgram);

    if (notSwappablePrograms != null && !notSwappablePrograms.contains(mdsProgram)) {
      if (mdsProgramNotHarmonizedYet != null && !mdsProgramNotHarmonizedYet.contains(mdsProgram)) {
        mdsProgramNotHarmonizedYet.add(mdsProgram);
      }
    }

    if (mdsProgramNotHarmonizedYet != null) {
      this.sortByName(mdsProgramNotHarmonizedYet);
    }
    if (swappablePrograms != null) {
      this.sortByName(swappablePrograms);
    }

    if (manualHarmonizePrograms.isEmpty()) {
      session.removeAttribute("manualHarmonizePrograms");
    }
    return this.getRedirectModelAndView();
  }

  @RequestMapping(value = EXPORT_LOG, method = RequestMethod.POST)
  public @ResponseBody byte[] exportLog(HttpServletRequest request, HttpServletResponse response)
      throws FileNotFoundException {

    File file = new File("harmonizationProgramsLog");
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
            + "-programs_harmonization-log.csv");
    response.setContentLength(outputStream.size());
    return outputStream.toByteArray();
  }

  @RequestMapping(value = EXPORT_PROGRAMS, method = RequestMethod.POST)
  public @ResponseBody byte[] exportPrograms(
      HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model)
      throws FileNotFoundException {

    HarmonizationData productionItemsToExport =
        (HarmonizationData) session.getAttribute("productionItemsToExport");
    String defaultLocationName = this.getDefaultLocation();

    List<ProgramDTO> list = new ArrayList<>();
    for (HarmonizationItem item : productionItemsToExport.getItems()) {
      list.add((ProgramDTO) item.getValue());
    }

    ByteArrayOutputStream outputStream =
        ProgramsHarmonizationCSVLog.exportProgramLogs(
            defaultLocationName, list, getNotSwappablePrograms());
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName="
            + this.getFormattedLocationName(defaultLocationName)
            + "-programs_harmonization-export-log.csv");
    response.setContentLength(outputStream.size());
    return outputStream.toByteArray();
  }

  @ModelAttribute("productionItemsToDelete")
  public List<ProgramDTO> getProductionItemsToDelete() {
    List<ProgramDTO> productionItemsToDelete = new ArrayList<>();
    List<ProgramDTO> onlyProductionPrograms =
        this.harmonizationProgramService.findAllProductionProgramsNotContainedInMetadataServer();
    for (ProgramDTO programDTO : onlyProductionPrograms) {
      final int numberOfAffectedPatientPrograms =
          this.harmonizationProgramService.getNumberOfAffectedPatientPrograms(programDTO);
      final int numberOfAffectedProgramWorkflows =
          this.harmonizationProgramService.getNumberOfAffectedProgramWorkflow(programDTO);
      if (numberOfAffectedPatientPrograms == 0 && numberOfAffectedProgramWorkflows == 0) {
        productionItemsToDelete.add(programDTO);
      }
    }
    return productionItemsToDelete;
  }

  public List<ProgramDTO> getProductionItemsToExport() {
    List<ProgramDTO> onlyProductionPrograms =
        this.harmonizationProgramService.findAllProductionProgramsNotContainedInMetadataServer();
    List<ProgramDTO> productionItemsToExport = new ArrayList<>();
    for (ProgramDTO programDTO : onlyProductionPrograms) {
      final int numberOfAffectedPatientPrograms =
          this.harmonizationProgramService.getNumberOfAffectedPatientPrograms(programDTO);
      final int numberOfAffectedProgramWorkflows =
          this.harmonizationProgramService.getNumberOfAffectedProgramWorkflow(programDTO);
      if (numberOfAffectedPatientPrograms > 0 || numberOfAffectedProgramWorkflows > 0) {
        productionItemsToExport.add(programDTO);
      }
    }
    return productionItemsToExport;
  }

  @ModelAttribute("harmonizationItem")
  HarmonizationItem formHarmonizationItem() {
    return new HarmonizationItem();
  }

  @ModelAttribute("newMDSPrograms")
  public HarmonizationData getNewMDSPrograms() {
    List<ProgramDTO> data =
        this.harmonizationProgramService.findAllMetadataProgramsNotContainedInProductionServer();
    return delegate.getConvertedData(data);
  }

  @ModelAttribute("differentIDsAndEqualUUID")
  public HarmonizationData getDifferentIDsAndEqualUUID() {
    Map<String, List<ProgramDTO>> programsWithDifferentIDsSameUUIDs =
        this.harmonizationProgramService.findAllProgramsWithDifferentIDAndSameUUID();
    return delegate.getConvertedData(programsWithDifferentIDsSameUUIDs);
  }

  @ModelAttribute("differentNameAndSameUUIDAndID")
  public HarmonizationData getDifferentNameAndSameUUIDAndID() {
    Map<String, List<ProgramDTO>> programsWithDifferentNames =
        this.harmonizationProgramService.findAllProgramsWithDifferentNameAndSameUUIDAndID();
    return delegate.getConvertedData(programsWithDifferentNames);
  }

  @ModelAttribute("swappablePrograms")
  public List<Program> getSwappablePrograms() {
    return sortByName(harmonizationProgramService.findAllSwappablePrograms());
  }

  @ModelAttribute("notSwappablePrograms")
  public List<Program> getNotSwappablePrograms() {
    return sortByName(this.harmonizationProgramService.findAllNotSwappablePrograms());
  }

  @ModelAttribute("mdsProgramNotHarmonizedYet")
  public List<Program> getMDSProgramNotHarmonizedYet() {
    return this.sortByName(HarmonizeProgramsDelegate.MDS_PROGRAMS_NOT_PROCESSED);
  }

  @SuppressWarnings("unchecked")
  private List<Program> sortByName(List<Program> list) {
    BeanComparator comparator = new BeanComparator("name");
    Collections.sort(list, comparator);
    return list;
  }

  private ModelAndView getRedirectModelAndView() {
    return new ModelAndView("redirect:" + PROGRAMS_LIST + ".form");
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
