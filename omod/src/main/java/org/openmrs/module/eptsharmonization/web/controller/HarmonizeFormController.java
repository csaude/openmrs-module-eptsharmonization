package org.openmrs.module.eptsharmonization.web.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsharmonization.api.HarmonizationFormService;
import org.openmrs.module.eptsharmonization.api.exception.UUIDDuplicationException;
import org.openmrs.module.eptsharmonization.api.model.FormDTO;
import org.openmrs.module.eptsharmonization.api.model.HtmlForm;
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

  public static final String ADD_FORMS_FROM_MDS_MAPPING =
      HarmonizeFormController.CONTROLLER_PATH + "/addFormFromMDSMapping";

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

  public static final String PROCESS_HARMONIZATION_STEP5 =
      HarmonizeFormController.CONTROLLER_PATH + "/processHarmonizationStep5";

  public static final String PROCESS_HARMONIZATION_STEP6 =
      HarmonizeFormController.CONTROLLER_PATH + "/processHarmonizationStep6";

  public static final String EXPORT_FORMS =
      HarmonizeFormController.CONTROLLER_PATH + "/harmonizeFormExportForms";

  public static final String EXPORT_LOG =
      HarmonizeFormController.CONTROLLER_PATH + "/harmonizeFormExportLog";

  public static boolean HAS_ATLEAST_ONE_ROW_HARMONIZED = false;

  public static boolean IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = false;
  public static boolean IS_NAMES_DIFFERENCES_HARMONIZED = false;

  private HarmonizationFormService harmonizationFormService;

  private HarmonizeFormDelegate delegate;

  @Autowired
  public void setHarmonizationFormService(HarmonizationFormService harmonizationFormService) {
    this.harmonizationFormService = harmonizationFormService;
  }

  @Autowired
  public void setDelegate(HarmonizeFormDelegate delegate) {
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
      @ModelAttribute("htmlFormsWithDifferentFormAndEqualUuid")
          HarmonizationData htmlFormsWithDifferentFormAndEqualUuid,
      @ModelAttribute("newHtmlFormFromMDS") List<HtmlForm> newHtmlFormFromMDS,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem,
      @ModelAttribute("notSwappableForms") List<Form> notSwappableForms,
      @ModelAttribute("swappableForms") List<Form> swappableForms,
      @ModelAttribute("mdsFormsWithoutEncounterReferences")
          List<Form> mdsFormsWithoutEncounterReferences,
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg,
      @RequestParam(required = false, value = "errorRequiredMdsValueForm")
          String errorRequiredMdsValueForm,
      @RequestParam(required = false, value = "errorRequiredPDSValueForm")
          String errorRequiredPDSValueForm,
      @RequestParam(required = false, value = "errorRequiredMdsValueFromMDS")
          String errorRequiredMdsValueFromMDS,
      @RequestParam(required = false, value = "errorRequiredPDSValueFromMDS")
          String errorRequiredPDSValueFromMDS,
      @RequestParam(required = false, value = "errorProcessingManualMapping")
          String errorProcessingManualMapping) {

    newMDSForms = getNewMDSForms();
    differentIDsAndEqualUUIDForm = this.getDifferentIDsAndEqualUUIDForm();
    differentNameAndSameUUIDAndIDForm = this.getDifferentNameAndSameUUIDAndIDForm();
    HarmonizationData productionItemsToExportForm = getProductionItemsToExportForm();

    session.setAttribute("harmonizedFormSummary", HarmonizeFormDelegate.SUMMARY_EXECUTED_SCENARIOS);
    session.setAttribute("openmrs_msg", openmrs_msg);
    session.setAttribute("errorRequiredMdsValueForm", errorRequiredMdsValueForm);
    session.setAttribute("errorRequiredPDSValueForm", errorRequiredPDSValueForm);
    session.setAttribute("errorRequiredMdsValueFromMDS", errorRequiredMdsValueFromMDS);
    session.setAttribute("errorRequiredPDSValueFromMDS", errorRequiredPDSValueFromMDS);
    session.setAttribute("errorProcessingManualMapping", errorProcessingManualMapping);

    delegate.setHarmonizationStage(
        session,
        mdsFormsWithoutEncounterReferences,
        newMDSForms,
        productionItemsToDeleteForm,
        productionItemsToExportForm,
        differentIDsAndEqualUUIDForm,
        differentNameAndSameUUIDAndIDForm,
        notSwappableForms,
        swappableForms,
        htmlFormsWithDifferentFormAndEqualUuid,
        newHtmlFormFromMDS);

    session.removeAttribute("productionItemsToExportForm");
    session.setAttribute("productionItemsToExportForm", productionItemsToExportForm);

    ModelAndView modelAndView = new ModelAndView();
    model.addAttribute("newMDSForms", newMDSForms);
    model.addAttribute("productionItemsToExportForm", productionItemsToExportForm);
    model.addAttribute("differentIDsAndEqualUUIDForm", differentIDsAndEqualUUIDForm);
    model.addAttribute("differentNameAndSameUUIDAndIDForm", differentNameAndSameUUIDAndIDForm);
    model.addAttribute("mdsFormsWithoutEncounterReferences", mdsFormsWithoutEncounterReferences);

    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP1, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep1(
      HttpSession session,
      @ModelAttribute("newMDSForms") HarmonizationData newMDSForms,
      @ModelAttribute("productionItemsToDeleteForm") List<FormDTO> productionItemsToDeleteForm) {

    Builder logBuilder = new FormHarmonizationCSVLog.Builder(getDefaultLocation());

    delegate.processDeleteFromProductionServer(productionItemsToDeleteForm, logBuilder);
    delegate.processAddNewFromMetadataServer(newMDSForms, logBuilder);

    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    modelAndView.addObject("openmrs_msg", "eptsharmonization.form.harmonized");

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
      modelAndView.addObject("openmrs_msg", "eptsharmonization.form.harmonized");
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
      modelAndView.addObject("openmrs_msg", "eptsharmonization.form.harmonized");
    }
    IS_NAMES_DIFFERENCES_HARMONIZED = true;
    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = PROCESS_HARMONIZATION_STEP4, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep4(
      HttpSession session,
      HttpServletRequest request,
      @ModelAttribute("swappableForms") List<Form> swappableForms,
      @ModelAttribute("mdsFormNotHarmonizedYet") List<Form> mdsFormNotHarmonizedYet)
      throws Exception {

    Map<Form, Form> manualHarmonizeForms =
        (Map<Form, Form>) session.getAttribute("manualHarmonizeForms");
    ModelAndView modelAndView = getRedirectModelAndView();
    if (manualHarmonizeForms != null && !manualHarmonizeForms.isEmpty()) {

      try {
        this.harmonizationFormService.saveManualMapping(manualHarmonizeForms);
      } catch (UUIDDuplicationException e) {

        for (Entry<Form, Form> entry : manualHarmonizeForms.entrySet()) {
          if (!swappableForms.contains(entry.getKey())) {
            swappableForms.add(entry.getKey());
          }
          if (!mdsFormNotHarmonizedYet.contains(entry.getKey())) {
            mdsFormNotHarmonizedYet.add(entry.getValue());
          }
        }

        modelAndView.addObject("errorProcessingManualMapping", e.getMessage());
        return modelAndView;
      } catch (SQLException e) {
        e.printStackTrace();
        throw new Exception(e);
      }

      Builder logBuilder = new FormHarmonizationCSVLog.Builder(this.getDefaultLocation());
      logBuilder.appendNewMappedForms(manualHarmonizeForms);
      logBuilder.build();

      HarmonizeFormDelegate.SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.form.newDefinedMapping");
    }
    modelAndView.addObject("openmrs_msg", "eptsharmonization.form.harmonized");

    HarmonizeFormDelegate.EXECUTED_FORMS_MANUALLY_CACHE =
        new ArrayList<>(manualHarmonizeForms.keySet());
    session.removeAttribute("manualHarmonizeForms");
    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = PROCESS_HARMONIZATION_STEP5, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep5(
      @ModelAttribute("htmlFormsWithDifferentFormAndEqualUuid") HarmonizationData data) {

    Map<String, List<HtmlForm>> map = new HashMap<>();
    for (HarmonizationItem item : data.getItems()) {
      List<HtmlForm> value = (List<HtmlForm>) item.getValue();
      map.put((String) item.getKey(), value);
    }
    this.harmonizationFormService.saveHtmlFormsWithDifferentFormNamesAndEqualHtmlFormUuid(map);
    Builder logBuilder = new FormHarmonizationCSVLog.Builder(this.getDefaultLocation());
    logBuilder.appendLogForHtmlFormStep1(map);
    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    modelAndView.addObject("openmrs_msg", "eptsharmonization.form.harmonized");
    HarmonizeFormDelegate.SUMMARY_EXECUTED_SCENARIOS.add(
        "eptsharmonization.summary.form.harmonize.htmlformStep1");

    return modelAndView;
  }

  @RequestMapping(value = PROCESS_HARMONIZATION_STEP6, method = RequestMethod.POST)
  public ModelAndView processHarmonizationStep6(
      @ModelAttribute("newHtmlFormFromMDS") List<HtmlForm> newHtmlFormFromMDS) {

    this.harmonizationFormService.saveNewHtmlFormsFromMetadataServer(newHtmlFormFromMDS);
    Builder logBuilder = new FormHarmonizationCSVLog.Builder(this.getDefaultLocation());
    logBuilder.appendLogForHtmlFormStep2(newHtmlFormFromMDS);
    logBuilder.build();

    ModelAndView modelAndView = this.getRedirectModelAndView();
    modelAndView.addObject("openmrs_msg", "eptsharmonization.form.harmonized");

    HarmonizeFormDelegate.SUMMARY_EXECUTED_SCENARIOS.add(
        "eptsharmonization.summary.form.harmonize.htmlformStep2");
    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = ADD_FORM_MAPPING, method = RequestMethod.POST)
  public ModelAndView addFormToManualMapping(
      HttpSession session,
      @ModelAttribute("notSwappableForms") List<Form> notSwappableForms,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem) {

    ModelAndView modelAndView = this.getRedirectModelAndView();

    if (harmonizationItem.getKey() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getKey()))) {
      modelAndView.addObject(
          "errorRequiredPDSValue", "eptsharmonization.form.error.formForMapping.required");
      return modelAndView;
    }
    if (harmonizationItem.getValue() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getValue()))) {
      modelAndView.addObject(
          "errorRequiredMdsValue", "eptsharmonization.form.error.formForMapping.required");
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
    manualHarmonizeForms.put(
        this.harmonizationFormService.findRelatedFormMetadataFromTableForm(pdsForm),
        this.harmonizationFormService.findRelatedFormMetadataFromTablMDSForm(mdsForm));
    session.setAttribute("manualHarmonizeForms", manualHarmonizeForms);

    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = ADD_FORMS_FROM_MDS_MAPPING, method = RequestMethod.POST)
  public ModelAndView addFormFromMDSToManualMapping(
      HttpSession session,
      @ModelAttribute("swappableForms") List<Form> swappableForms,
      @ModelAttribute("harmonizationItem") HarmonizationItem harmonizationItem,
      @ModelAttribute("mdsFormNotHarmonizedYet") List<Form> mdsFormNotHarmonizedYet) {

    ModelAndView modelAndView = this.getRedirectModelAndView();

    if (harmonizationItem.getValue() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getValue()))) {
      modelAndView.addObject(
          "errorRequiredMdsValueFromMDS", "eptsharmonization.form.error.formForMapping.required");
      return modelAndView;
    }

    if (harmonizationItem.getKey() == null
        || StringUtils.isEmpty(((String) harmonizationItem.getKey()))) {
      modelAndView.addObject(
          "errorRequiredPDSValueFromMDS", "eptsharmonization.form.error.formForMapping.required");
      return modelAndView;
    }

    Form pdsForm = Context.getFormService().getFormByUuid((String) harmonizationItem.getKey());

    String mdsFormUuid = (String) harmonizationItem.getValue();
    Form mdsForm = null;
    for (Form form : mdsFormNotHarmonizedYet) {
      if (mdsFormUuid.equals(form.getUuid())) {
        mdsForm = form;
        break;
      }
    }

    Map<Form, Form> manualHarmonizeForms =
        (Map<Form, Form>) session.getAttribute("manualHarmonizeForms");

    if (manualHarmonizeForms == null) {
      manualHarmonizeForms = new HashMap<>();
    }
    swappableForms.remove(pdsForm);
    manualHarmonizeForms.put(
        this.harmonizationFormService.findRelatedFormMetadataFromTableForm(pdsForm),
        this.harmonizationFormService.findRelatedFormMetadataFromTablMDSForm(mdsForm));
    session.setAttribute("manualHarmonizeForms", manualHarmonizeForms);

    if (mdsFormNotHarmonizedYet != null && mdsFormNotHarmonizedYet.contains(mdsForm)) {
      mdsFormNotHarmonizedYet.remove(mdsForm);
    }

    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = REMOVE_FORM_MAPPING, method = RequestMethod.POST)
  public ModelAndView removeFormFromManualMapping(
      HttpSession session,
      @ModelAttribute("swappableForms") List<Form> swappableForms,
      @ModelAttribute("notSwappableForms") List<Form> notSwappableForms,
      @ModelAttribute("mdsFormNotHarmonizedYet") List<Form> mdsFormNotHarmonizedYet,
      HttpServletRequest request) {

    Form productionForm =
        Context.getFormService().getFormByUuid(request.getParameter("productionServerFormUuID"));

    Map<Form, Form> manualHarmonizeForms =
        (Map<Form, Form>) session.getAttribute("manualHarmonizeForms");

    Form mdsForm = manualHarmonizeForms.get(productionForm);
    manualHarmonizeForms.remove(productionForm);
    swappableForms.add(productionForm);

    if (notSwappableForms != null && !notSwappableForms.contains(mdsForm)) {
      if (mdsFormNotHarmonizedYet != null && !mdsFormNotHarmonizedYet.contains(mdsForm)) {
        mdsFormNotHarmonizedYet.add(mdsForm);
      }
    }

    if (mdsFormNotHarmonizedYet != null) {
      this.sortByName(mdsFormNotHarmonizedYet);
    }
    if (swappableForms != null) {
      this.sortByName(swappableForms);
    }

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
    List<Form> formMetadataServer = this.harmonizationFormService.findAllFormsFromMetadataServer();

    List<FormDTO> list = new ArrayList<>();
    for (HarmonizationItem item : productionItemsToExport.getItems()) {
      list.add((FormDTO) item.getValue());
    }

    ByteArrayOutputStream outputStream =
        FormHarmonizationCSVLog.exportFormLogs(defaultLocationName, list, formMetadataServer);
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName=forms_harmonization_" + defaultLocationName + "-export-log.csv");
    response.setContentLength(outputStream.size());
    return outputStream.toByteArray();
  }
  //
  // @ModelAttribute("productionItemsToDeleteForm")
  // public List<FormDTO> getProductionItemsToDelete() {
  // return this.harmonizationFormService.findUnusedProductionServerForms();
  // }

  @ModelAttribute("productionItemsToDeleteForm")
  public List<FormDTO> getProductionItemsToDelete() {
    List<FormDTO> productionItemsToDelete = new ArrayList<>();
    List<FormDTO> onlyProductionForms =
        this.harmonizationFormService.findAllProductionFormsNotContainedInMetadataServer();

    for (FormDTO form : onlyProductionForms) {
      final int numberOfAffectedEncounters =
          this.harmonizationFormService.getNumberOfAffectedEncounters(form.getForm());

      if (numberOfAffectedEncounters == 0) {
        productionItemsToDelete.add(form);
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
      if (numberOfAffectedEncounters > 0) {
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
    return this.sortByName(this.harmonizationFormService.findAllSwappableForms());
  }

  @ModelAttribute("notSwappableForms")
  public List<Form> getNotSwappableForms() {
    return this.sortByName(this.harmonizationFormService.findAllNotSwappableForms());
  }

  @ModelAttribute("mdsFormNotHarmonizedYet")
  public List<Form> getMDSFormNotHarmonizedYet() {
    return this.sortByName(this.sortByName(this.delegate.getMDSNotHarmonizedYet()));
  }

  @SuppressWarnings("unchecked")
  @ModelAttribute("mdsFormsWithoutEncounterReferences")
  public List<Form> getFormsWithoutEncountersReferences() {
    List<Form> forms =
        this.harmonizationFormService.findMDSFormsWithoutEncountersReferencesInPDServer();
    Comparator<Form> comp = new BeanComparator("formId");
    Collections.sort(forms, comp);
    return forms;
  }

  @ModelAttribute("htmlFormsWithDifferentFormAndEqualUuid")
  public HarmonizationData getMdsHtmlFormWithDifferentFormAndEqualUuid() {

    Map<String, List<HtmlForm>> result =
        this.harmonizationFormService.findHtmlFormWithDifferentFormAndEqualUuid();

    List<HarmonizationItem> items = new ArrayList<>();
    for (String key : result.keySet()) {
      List<HtmlForm> htmlForms = result.get(key);
      if (htmlForms != null) {
        HarmonizationItem item = new HarmonizationItem(key, htmlForms);
        if (!items.contains(item)) {
          items.add(item);
        }
      }
    }
    return new HarmonizationData(items);
  }

  @ModelAttribute("newHtmlFormFromMDS")
  public List<HtmlForm> getNewHtmlFormFromMds() {
    return this.harmonizationFormService.findHtmlFormMetadataServerNotPresentInProductionServer();
  }

  @SuppressWarnings("unchecked")
  private List<Form> sortByName(List<Form> list) {
    BeanComparator comparator = new BeanComparator("name");
    Collections.sort(list, comparator);
    return list;
  }

  private ModelAndView getRedirectModelAndView() {
    return new ModelAndView("redirect:" + FORMS_LIST + ".form");
  }

  private String getDefaultLocation() {
    return Context.getAdministrationService().getGlobalProperty("default_location");
  }
}
