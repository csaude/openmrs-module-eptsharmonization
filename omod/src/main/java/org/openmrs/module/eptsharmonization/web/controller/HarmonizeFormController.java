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
import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsharmonization.api.HarmonizationFormService;
import org.openmrs.module.eptsharmonization.api.model.FormDTO;
import org.openmrs.module.eptsharmonization.web.EptsHarmonizationConstants;
import org.openmrs.module.eptsharmonization.web.bean.FormHarmonizationCSVLog;
import org.openmrs.module.eptsharmonization.web.bean.FormHarmonizationCSVLog.Builder;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationData;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationItem;
import org.openmrs.module.eptsharmonization.web.delegate.HarmonizeFormDelegate;
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

@Controller("eptsharmonization.harmonizeFormController")
@SessionAttributes({"differentIDsAndEqualUUIDForm", "differentNameAndSameUUIDAndIDForm"})
public class HarmonizeFormController {

  public static final String CONTROLLER_PATH = EptsHarmonizationConstants.MODULE_PATH + "/form";

  public static final String FORMS_LIST =
      HarmonizeFormController.CONTROLLER_PATH + "/harmonizeFormList";

  public static final String ADD_FORM_MAPPING =
      HarmonizeFormController.CONTROLLER_PATH + "/addFormMapping";

  public static final String REMOVE_FORM_MAPPING =
      HarmonizeFormController.CONTROLLER_PATH + "/removeFormMapping";

  public static final String PROCESS_HARMONIZATION_STEP1 =
      HarmonizeFormController.CONTROLLER_PATH + "/processHarmonizationStep1";

  public static final String PROCESS_HARMONIZATION_STEP2 =
      HarmonizeFormController.CONTROLLER_PATH + "/processHarmonizationStep2";

  public static final String PROCESS_HARMONIZATION_STEP3 =
      HarmonizeFormController.CONTROLLER_PATH + "/processHarmonizationStep3";

  public static final String PROCESS_HARMONIZATION_STEP4 =
      HarmonizeFormController.CONTROLLER_PATH + "/processHarmonizationStep4";

  public static final String EXPORT_FORMS =
      HarmonizeFormController.CONTROLLER_PATH + "/harmonizeExportForms";

  public static final String EXPORT_LOG =
      HarmonizeFormController.CONTROLLER_PATH + "/harmonizeFormListExportLog";

  public static boolean HAS_ATLEAST_ONE_ROW_HARMONIZED = false;

  public static boolean IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = false;
  public static boolean IS_NAMES_DIFFERENCES_HARMONIZED = false;

  private HarmonizationFormService harmonizationFormService;

  private HarmonizeFormDelegate delegate;

  @Autowired
  public HarmonizeFormController(
      HarmonizationFormService harmonizationFormService, HarmonizeFormDelegate delegate) {
    this.harmonizationFormService = harmonizationFormService;
    this.delegate = delegate;
  }

  @RequestMapping(value = FORMS_LIST, method = RequestMethod.GET)
  public ModelAndView getHarmonizationScenariousData(
      HttpSession session,
      Model model,
      @ModelAttribute("newMDSForms") HarmonizationData newMDSForms,
      @ModelAttribute("productionItemsToDeleteForm") List<FormDTO> productionItemsToDeleteForm,
      @ModelAttribute("differentIDsAndEqualUUIDForm")
          HarmonizationData differentIDsAndEqualUUIDForm,
      @ModelAttribute("differentNameAndSameUUIDAndIDForm")
          HarmonizationData differentNameAndSameUUIDAndIDForm,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem,
      @ModelAttribute("notSwappableForms") List<Form> notSwappableForms,
      @ModelAttribute("swappableForms") List<Form> swappableForms,
      @RequestParam(required = false, value = "openmrs_msgForm") String openmrs_msgForm,
      @RequestParam(required = false, value = "errorRequiredMdsValueForm")
          String errorRequiredMdsValueForm,
      @RequestParam(required = false, value = "errorRequiredPDSValueForm")
          String errorRequiredPDSValueForm) {

    newMDSForms = getNewMDSForms();
    differentIDsAndEqualUUIDForm = this.getDifferentIDsAndEqualUUIDForm();
    differentNameAndSameUUIDAndIDForm = this.getDifferentNameAndSameUUIDAndIDForm();
    HarmonizationData productionItemsToExportForm = getProductionItemsToExportForm();

    session.setAttribute("harmonizedFormSummary", HarmonizeFormDelegate.SUMMARY_EXECUTED_SCENARIOS);
    session.setAttribute("openmrs_msgForm", openmrs_msgForm);
    session.setAttribute("errorRequiredMdsValueForm", errorRequiredMdsValueForm);
    session.setAttribute("errorRequiredPDSValueForm", errorRequiredPDSValueForm);

    delegate.setHarmonizationStage(
        session,
        newMDSForms,
        productionItemsToDeleteForm,
        productionItemsToExportForm,
        differentIDsAndEqualUUIDForm,
        differentNameAndSameUUIDAndIDForm,
        notSwappableForms,
        swappableForms);

    session.removeAttribute("productionItemsToExportForm");
    session.setAttribute("productionItemsToExportForm", productionItemsToExportForm);

    ModelAndView modelAndView = new ModelAndView();
    model.addAttribute("newMDSForms", newMDSForms);
    model.addAttribute("productionItemsToExportForm", productionItemsToExportForm);
    model.addAttribute("differentIDsAndEqualUUIDForm", differentIDsAndEqualUUIDForm);
    model.addAttribute("differentNameAndSameUUIDAndIDForm", differentNameAndSameUUIDAndIDForm);

    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP1, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep1(
      HttpSession session,
      @ModelAttribute("newMDSForms") HarmonizationData newMDSForms,
      @ModelAttribute("productionItemsToDeleteForm") List<FormDTO> productionItemsToDeleteForm) {

    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
    Builder logBuilder = new FormHarmonizationCSVLog.Builder(defaultLocationName);

    delegate.processAddNewFromMetadataServer(newMDSForms, logBuilder);
    delegate.processDeleteFromProductionServer(productionItemsToDeleteForm, logBuilder);

    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    modelAndView.addObject("openmrs_msgForm", "eptsharmonization.encountertype.harmonized");

    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP2, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep2(
      @ModelAttribute("differentIDsAndEqualUUIDForm")
          HarmonizationData differentIDsAndEqualUUIDForm) {

    HAS_ATLEAST_ONE_ROW_HARMONIZED = false;
    Builder logBuilder = new FormHarmonizationCSVLog.Builder(this.getDefaultLocation());
    delegate.processFormsWithDiferrentIdsAndEqualUUID(differentIDsAndEqualUUIDForm, logBuilder);
    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    if (HAS_ATLEAST_ONE_ROW_HARMONIZED) {
      modelAndView.addObject("openmrs_msgForm", "eptsharmonization.encountertype.harmonized");
    }
    IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = true;
    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP3, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep3(
      @ModelAttribute("differentNameAndSameUUIDAndIDForm")
          HarmonizationData differentNameAndSameUUIDAndIDForm) {

    HAS_ATLEAST_ONE_ROW_HARMONIZED = false;
    Builder logBuilder = new FormHarmonizationCSVLog.Builder(this.getDefaultLocation());
    delegate.processUpdateFormNames(differentNameAndSameUUIDAndIDForm, logBuilder);
    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    if (HAS_ATLEAST_ONE_ROW_HARMONIZED) {
      modelAndView.addObject("openmrs_msgForm", "eptsharmonization.encountertype.harmonized");
    }
    IS_NAMES_DIFFERENCES_HARMONIZED = true;
    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = PROCESS_HARMONIZATION_STEP4, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep4(HttpSession session, HttpServletRequest request) {

    Map<Form, Form> manualHarmonizeForms =
        (Map<Form, Form>) session.getAttribute("manualHarmonizeForms");

    if (manualHarmonizeForms != null && !manualHarmonizeForms.isEmpty()) {
      this.harmonizationFormService.saveManualMapping(manualHarmonizeForms);

      Builder logBuilder = new FormHarmonizationCSVLog.Builder(this.getDefaultLocation());
      // logBuilder.appendNewMappedEncounterTypes(manualHarmonizeForms);
      logBuilder.build();

      HarmonizeFormDelegate.SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.encounterType.newDefinedMapping");
    }
    ModelAndView modelAndView = getRedirectModelAndView();
    modelAndView.addObject("openmrs_msgForm", "eptsharmonization.encountertype.harmonized");

    HarmonizeFormDelegate.EXECUTED_FORMS_MANUALLY_CACHE =
        new ArrayList<>(manualHarmonizeForms.keySet());
    session.removeAttribute("manualHarmonizeForms");

    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = ADD_FORM_MAPPING, method = RequestMethod.POST)
  public ModelAndView addEncounterTypeMapping(
      HttpSession session,
      @ModelAttribute("notSwappableForms") List<Form> notSwappableForms,
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

    Form pdsForm = Context.getFormService().getFormByUuid((String) harmonizationItem.getKey());
    Form mdsForm = Context.getFormService().getFormByUuid((String) harmonizationItem.getValue());

    Map<Form, Form> manualHarmonizeForms =
        (Map<Form, Form>) session.getAttribute("manualHarmonizeForms");

    if (manualHarmonizeForms == null) {
      manualHarmonizeForms = new HashMap<>();
    }
    notSwappableForms.remove(pdsForm);
    manualHarmonizeForms.put(pdsForm, mdsForm);
    session.setAttribute("manualHarmonizeForms", manualHarmonizeForms);

    return modelAndView;
  }

  @RequestMapping(value = REMOVE_FORM_MAPPING, method = RequestMethod.POST)
  public ModelAndView removeEncounterTypeMapping(
      HttpSession session,
      @ModelAttribute("swappableForms") List<Form> swappableForms,
      HttpServletRequest request) {

    Form productionForm =
        Context.getFormService().getFormByUuid(request.getParameter("productionServerFormUuID"));

    @SuppressWarnings("unchecked")
    Map<Form, Form> manualHarmonizeForms =
        (Map<Form, Form>) session.getAttribute("manualHarmonizeForms");

    manualHarmonizeForms.remove(productionForm);
    swappableForms.add(productionForm);

    if (manualHarmonizeForms.isEmpty()) {
      session.removeAttribute("manualHarmonizeForms");
    }
    return this.getRedirectModelAndView();
  }

  @RequestMapping(value = EXPORT_LOG, method = RequestMethod.POST)
  public @ResponseBody byte[] exportLog(HttpServletRequest request, HttpServletResponse response)
      throws FileNotFoundException {

    File file = new File("harmonizationFormLog");
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
        "attachment; fileName=form_harmonization_" + this.getDefaultLocation() + "-log.csv");
    response.setContentLength(outputStream.size());
    return outputStream.toByteArray();
  }

  @RequestMapping(value = EXPORT_FORMS, method = RequestMethod.POST)
  public @ResponseBody byte[] exportForms(
      HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model)
      throws FileNotFoundException {

    HarmonizationData productionItemsToExport =
        (HarmonizationData) session.getAttribute("productionItemsToExportForm");
    String defaultLocationName = this.getDefaultLocation();

    List<FormDTO> list = new ArrayList<>();
    for (HarmonizationItem item : productionItemsToExport.getItems()) {
      list.add((FormDTO) item.getValue());
    }

    ByteArrayOutputStream outputStream = null;
    // EncounterTypeHarmonizationCSVLog.exportEncounterTypeLogs(defaultLocationName,
    // list);
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName=encounter_type_harmonization_"
            + defaultLocationName
            + "-export-log.csv");
    response.setContentLength(outputStream.size());
    return outputStream.toByteArray();
  }

  @ModelAttribute("productionItemsToDeleteForm")
  public List<FormDTO> getProductionItemsToDelete() {
    List<FormDTO> productionItemsToDelete = new ArrayList<>();
    List<FormDTO> onlyProductionForms =
        this.harmonizationFormService.findAllProductionFormsNotContainedInMetadataServer();
    for (FormDTO formDTO : onlyProductionForms) {
      final int numberOfAffectedEncounters =
          this.harmonizationFormService.getNumberOfAffectedEncounters(formDTO.getForm());
      final int numberOfAffectedFormFields =
          this.harmonizationFormService.getNumberOfAffectedFormFields(formDTO.getForm());
      if (numberOfAffectedEncounters == 0 && numberOfAffectedFormFields == 0) {
        productionItemsToDelete.add(formDTO);
      }
    }
    return productionItemsToDelete;
  }

  public HarmonizationData getProductionItemsToExportForm() {
    List<FormDTO> onlyProductionForms =
        this.harmonizationFormService.findAllProductionFormsNotContainedInMetadataServer();
    List<FormDTO> productionItemsToExportForm = new ArrayList<>();
    for (FormDTO form : onlyProductionForms) {
      final int numberOfAffectedEncounters =
          this.harmonizationFormService.getNumberOfAffectedEncounters(form.getForm());
      final int numberOfAffectedFormFields =
          this.harmonizationFormService.getNumberOfAffectedFormFields(form.getForm());
      if (numberOfAffectedEncounters > 0 || numberOfAffectedFormFields > 0) {
        productionItemsToExportForm.add(form);
      }
    }
    return delegate.getConvertedData(productionItemsToExportForm);
  }

  @ModelAttribute("harmonizationItem")
  HarmonizationItem formBackingObject() {
    return new HarmonizationItem();
  }

  @ModelAttribute("newMDSForms")
  public HarmonizationData getNewMDSForms() {
    List<FormDTO> data =
        this.harmonizationFormService.findAllMetadataFormsNotContainedInProductionServer();
    return delegate.getConvertedData(data);
  }

  @ModelAttribute("differentIDsAndEqualUUIDForm")
  public HarmonizationData getDifferentIDsAndEqualUUIDForm() {
    Map<String, List<FormDTO>> formsWithDifferentIDsSameUUIDs =
        this.harmonizationFormService.findAllFormsWithDifferentIDAndSameUUID();
    return delegate.getConvertedData(formsWithDifferentIDsSameUUIDs);
  }

  @ModelAttribute("differentNameAndSameUUIDAndIDForm")
  public HarmonizationData getDifferentNameAndSameUUIDAndIDForm() {
    Map<String, List<FormDTO>> formsWithDifferentNames =
        this.harmonizationFormService.findAllFormsWithDifferentNameAndSameUUIDAndID();
    return delegate.getConvertedData(formsWithDifferentNames);
  }

  @ModelAttribute("swappableForms")
  public List<Form> getSwappableForms() {
    return this.harmonizationFormService.findAllSwappableForms();
  }

  @ModelAttribute("notSwappableForms")
  public List<Form> getNotSwappableForms() {
    return this.harmonizationFormService.findAllNotSwappableForms();
  }

  private ModelAndView getRedirectModelAndView() {
    return new ModelAndView("redirect:" + FORMS_LIST + ".form");
  }

  private String getDefaultLocation() {
    return Context.getAdministrationService().getGlobalProperty("default_location");
  }
}
