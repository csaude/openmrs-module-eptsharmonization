package org.openmrs.module.eptsharmonization.web.delegate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.http.HttpSession;
import org.openmrs.Form;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationFormService;
import org.openmrs.module.eptsharmonization.api.model.FormDTO;
import org.openmrs.module.eptsharmonization.web.bean.FormHarmonizationCSVLog.Builder;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationData;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationItem;
import org.openmrs.module.eptsharmonization.web.controller.HarmonizeFormController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HarmonizeFormDelegate {

  private HarmonizationFormService harmonizationFormService;

  @Autowired
  public HarmonizeFormDelegate(HarmonizationFormService harmonizationFormService) {
    this.harmonizationFormService = harmonizationFormService;
  }

  public static List<String> SUMMARY_EXECUTED_SCENARIOS = new ArrayList<>();
  public static List<Form> EXECUTED_FORMS_MANUALLY_CACHE = new ArrayList<>();
  private static List<Form> FORMS_NOT_PROCESSED = new ArrayList<>();

  public HarmonizationData getConvertedData(List<FormDTO> forms) {

    Set<HarmonizationItem> items = new TreeSet<>();
    for (FormDTO formDTO : forms) {
      HarmonizationItem item = new HarmonizationItem(formDTO.getForm().getUuid(), formDTO);
      item.setEncountersCount(
          this.harmonizationFormService.getNumberOfAffectedEncounters(formDTO.getForm()));
      item.setFormFieldsCount(
          this.harmonizationFormService.getNumberOfAffectedFormFields(formDTO.getForm()));
      item.setFormResourceCount(
          this.harmonizationFormService.getNumberOfAffectedFormResourses(formDTO.getForm()));
      items.add(item);
    }
    return new HarmonizationData(items);
  }

  public HarmonizationData getConvertedData(Map<String, List<FormDTO>> mapForms) {
    Set<HarmonizationItem> items = new TreeSet<>();
    for (String key : mapForms.keySet()) {
      List<FormDTO> forms = mapForms.get(key);
      if (forms != null) {
        HarmonizationItem item = new HarmonizationItem(key, forms);
        item.setEncountersCount(
            this.harmonizationFormService.getNumberOfAffectedEncounters(forms.get(1).getForm()));
        item.setFormFieldsCount(
            this.harmonizationFormService.getNumberOfAffectedFormFields(forms.get(1).getForm()));
        item.setFormResourceCount(
            this.harmonizationFormService.getNumberOfAffectedFormResourses(forms.get(1).getForm()));
        items.add(item);
      }
    }
    return new HarmonizationData(items);
  }

  public void setHarmonizationStage(
      HttpSession session,
      HarmonizationData newMDSForms,
      List<FormDTO> productionItemsToDelete,
      HarmonizationData productionItemsToExportForm,
      HarmonizationData differentIDsAndEqualUUID,
      HarmonizationData differentNameAndSameUUIDAndID,
      List<Form> notSwappableForms,
      List<Form> swappableForms) {

    FORMS_NOT_PROCESSED.removeAll(EXECUTED_FORMS_MANUALLY_CACHE);

    this.updateProductionToExportList(productionItemsToExportForm);
    this.setSwappableDataClones(session, notSwappableForms, swappableForms);
    this.removeAllChoosenToManualHarmonize(session, swappableForms);

    boolean isFirstStepFormHarmonizationCompleted =
        newMDSForms.getItems().isEmpty() && productionItemsToDelete.isEmpty();

    this.setIDsAndUUIDsFormHarmonizationStatus(
        productionItemsToExportForm,
        differentIDsAndEqualUUID,
        isFirstStepFormHarmonizationCompleted);
    this.setNAMEsDifferencesFormHarmonizationStatus(
        productionItemsToExportForm,
        differentNameAndSameUUIDAndID,
        isFirstStepFormHarmonizationCompleted);

    boolean hasSecondStepFormHarmonization =
        isFirstStepFormHarmonizationCompleted
            && HarmonizeFormController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED
            && HarmonizeFormController.IS_NAMES_DIFFERENCES_HARMONIZED
            && !productionItemsToExportForm.getItems().isEmpty();

    session.setAttribute(
        "isFirstStepFormHarmonizationCompleted", isFirstStepFormHarmonizationCompleted);
    session.setAttribute(
        "isUUIDsAndIDsFormHarmonized",
        HarmonizeFormController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED);
    session.setAttribute(
        "isNamesFormHarmonized", HarmonizeFormController.IS_NAMES_DIFFERENCES_HARMONIZED);
    session.setAttribute("hasSecondStepFormHarmonization", hasSecondStepFormHarmonization);
  }

  private void setIDsAndUUIDsFormHarmonizationStatus(
      HarmonizationData productionItemsToExport,
      HarmonizationData differentIDsAndEqualUUID,
      boolean isFirstStepFormHarmonizationCompleted) {

    HarmonizeFormController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED =
        HarmonizeFormController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED
            || differentIDsAndEqualUUID.getItems().isEmpty();

    if (!isFirstStepFormHarmonizationCompleted) {
      HarmonizeFormController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = false;
    }

    if (HarmonizeFormController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED
        && productionItemsToExport.getItems().isEmpty()
        && !differentIDsAndEqualUUID.getItems().isEmpty()) {
      HarmonizeFormController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = false;
    }
  }

  private void setNAMEsDifferencesFormHarmonizationStatus(
      HarmonizationData productionItemsToExportForm,
      HarmonizationData differentNameAndSameUUIDAndIDForm,
      boolean isFirstStepFormHarmonizationCompleted) {

    HarmonizeFormController.IS_NAMES_DIFFERENCES_HARMONIZED =
        HarmonizeFormController.IS_NAMES_DIFFERENCES_HARMONIZED
            || differentNameAndSameUUIDAndIDForm.getItems().isEmpty();

    if (!isFirstStepFormHarmonizationCompleted) {
      HarmonizeFormController.IS_NAMES_DIFFERENCES_HARMONIZED = false;
    }

    if (HarmonizeFormController.IS_NAMES_DIFFERENCES_HARMONIZED
        && productionItemsToExportForm.getItems().isEmpty()
        && !differentNameAndSameUUIDAndIDForm.getItems().isEmpty()) {
      HarmonizeFormController.IS_NAMES_DIFFERENCES_HARMONIZED = false;
    }
  }

  private void updateProductionToExportList(HarmonizationData productionItemsToExport) {

    HarmonizationData newItemsToExport = getConvertedData(DTOUtils.fromForms(FORMS_NOT_PROCESSED));
    productionItemsToExport.getItems().addAll(newItemsToExport.getItems());

    Set<HarmonizationItem> itemsToRemove =
        getConvertedData(DTOUtils.fromForms(EXECUTED_FORMS_MANUALLY_CACHE)).getItems();
    productionItemsToExport.getItems().removeAll(itemsToRemove);
  }

  private void setSwappableDataClones(
      HttpSession session, List<Form> notSwappableForms, List<Form> swappableForms) {

    swappableForms.removeAll(EXECUTED_FORMS_MANUALLY_CACHE);
    for (Form form : FORMS_NOT_PROCESSED) {
      if (!swappableForms.contains(form)) {
        swappableForms.add(form);
      }
    }
    session.setAttribute("swappableFormsClone", new ArrayList<Form>(swappableForms));
    session.setAttribute("notSwappableFormsClone", new ArrayList<Form>(notSwappableForms));
  }

  @SuppressWarnings("unchecked")
  public void removeAllChoosenToManualHarmonize(HttpSession session, List<Form> swappableForms) {
    Map<Form, Form> manualHarmonizeForms =
        (Map<Form, Form>) session.getAttribute("manualHarmonizeForms");
    if (manualHarmonizeForms != null) {
      for (Entry<Form, Form> entry : manualHarmonizeForms.entrySet()) {
        swappableForms.remove(entry.getKey());
      }
    }
  }

  public void processAddNewFromMetadataServer(
      HarmonizationData onlyMetadataForms, Builder logBuilder) {
    List<FormDTO> list = new ArrayList<>();
    for (HarmonizationItem item : onlyMetadataForms.getItems()) {
      list.add((FormDTO) item.getValue());
    }
    if (!list.isEmpty()) {
      this.harmonizationFormService.saveNewFormsFromMDS(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.encountertype.harmonize.onlyOnMDServer");
      // logBuilder.appendLogForNewHarmonizedFromMDSEncounterTypes(list);
    }
  }

  public void processDeleteFromProductionServer(List<FormDTO> list, Builder logBuilder) {
    if (!list.isEmpty()) {
      this.harmonizationFormService.deleteNewFormsFromPDS(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.encountertype.harmonize.onlyOnPServer.unused");
      // logBuilder.appendLogForDeleteFromProductionServer(list);
    }
  }

  @SuppressWarnings("unchecked")
  public void processUpdateFormNames(HarmonizationData data, Builder logBuilder) {
    Map<String, List<FormDTO>> list = new HashMap<>();
    for (HarmonizationItem item : data.getItems()) {
      List<FormDTO> value = (List<FormDTO>) item.getValue();
      if (item.isSelected()) {
        list.put((String) item.getKey(), value);
      } else {
        FORMS_NOT_PROCESSED.add(value.get(1).getForm());
      }
    }
    if (!list.isEmpty()) {
      this.harmonizationFormService.saveFormsWithDifferentNames(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.encountertype.harmonize.differentNamesAndSameUUIDAndID");
      // logBuilder.appendLogForUpdatedEncounterNames(list);
      HarmonizeFormController.HAS_ATLEAST_ONE_ROW_HARMONIZED = true;
    }
  }

  @SuppressWarnings("unchecked")
  public void processFormsWithDiferrentIdsAndEqualUUID(HarmonizationData data, Builder logBuilder) {
    Map<String, List<FormDTO>> list = new HashMap<>();

    for (HarmonizationItem item : data.getItems()) {
      List<FormDTO> value = (List<FormDTO>) item.getValue();
      if (item.isSelected()) {
        list.put((String) item.getKey(), value);
      } else {
        FORMS_NOT_PROCESSED.add(value.get(1).getForm());
      }
    }
    if (!list.isEmpty()) {
      this.harmonizationFormService.saveFormsWithDifferentIDAndEqualUUID(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.encountertype.harmonize.differentID.andEqualUUID");
      // logBuilder.appendLogForEncounterTypesWithDiferrentIdsAndEqualUUID(list);
      HarmonizeFormController.HAS_ATLEAST_ONE_ROW_HARMONIZED = true;
    }
  }
}
