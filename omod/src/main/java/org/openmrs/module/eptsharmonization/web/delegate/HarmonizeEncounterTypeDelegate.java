package org.openmrs.module.eptsharmonization.web.delegate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpSession;
import org.apache.commons.beanutils.BeanComparator;
import org.openmrs.EncounterType;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationEncounterTypeService;
import org.openmrs.module.eptsharmonization.api.model.EncounterTypeDTO;
import org.openmrs.module.eptsharmonization.web.bean.EncounterTypeHarmonizationCSVLog.Builder;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationData;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationItem;
import org.openmrs.module.eptsharmonization.web.controller.HarmonizeEncounterTypeController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HarmonizeEncounterTypeDelegate {

  private HarmonizationEncounterTypeService harmonizationEncounterTypeService;

  @Autowired
  public void setHarmonizationEncounterTypeService(
      HarmonizationEncounterTypeService harmonizationEncounterTypeService) {
    this.harmonizationEncounterTypeService = harmonizationEncounterTypeService;
  }

  public static List<String> SUMMARY_EXECUTED_SCENARIOS = new ArrayList<>();
  public static List<EncounterType> EXECUTED_ENCOUNTERTYPES_MANUALLY_CACHE = new ArrayList<>();
  private static List<EncounterType> ENCOUNTERTYPES_NOT_PROCESSED = new ArrayList<>();
  private static List<EncounterType> MDS_ENCOUNTERTYPES_NOT_PROCESSED = new ArrayList<>();

  public HarmonizationData getConvertedData(List<EncounterTypeDTO> encounterTypes) {

    List<HarmonizationItem> items = new ArrayList<>();

    for (EncounterTypeDTO encounterTypeDTO : encounterTypes) {
      HarmonizationItem item =
          new HarmonizationItem(encounterTypeDTO.getEncounterType().getUuid(), encounterTypeDTO);
      item.setEncountersCount(
          this.harmonizationEncounterTypeService.getNumberOfAffectedEncounters(encounterTypeDTO));
      item.setFormsCount(
          this.harmonizationEncounterTypeService.getNumberOfAffectedForms(encounterTypeDTO));
      if (!items.contains(item)) {
        items.add(item);
      }
    }
    return new HarmonizationData(items);
  }

  public HarmonizationData getConvertedData(Map<String, List<EncounterTypeDTO>> mapEncounterTypes) {
    List<HarmonizationItem> items = new ArrayList<>();

    for (String key : mapEncounterTypes.keySet()) {
      List<EncounterTypeDTO> eTypes = mapEncounterTypes.get(key);
      if (eTypes != null) {
        HarmonizationItem item = new HarmonizationItem(key, eTypes);
        item.setEncountersCount(
            this.harmonizationEncounterTypeService.getNumberOfAffectedEncounters(eTypes.get(1)));
        item.setFormsCount(
            this.harmonizationEncounterTypeService.getNumberOfAffectedForms(eTypes.get(1)));
        if (!items.contains(item)) {
          items.add(item);
        }
      }
    }
    return new HarmonizationData(items);
  }

  public void setHarmonizationStage(
      HttpSession session,
      HarmonizationData newMDSEncounterTypes,
      List<EncounterTypeDTO> productionItemsToDelete,
      HarmonizationData productionItemsToExport,
      HarmonizationData differentIDsAndEqualUUID,
      HarmonizationData differentNameAndSameUUIDAndID,
      List<EncounterType> notSwappableEncounterTypes,
      List<EncounterType> swappableEncounterTypes) {

    ENCOUNTERTYPES_NOT_PROCESSED.removeAll(EXECUTED_ENCOUNTERTYPES_MANUALLY_CACHE);

    this.updateProductionToExportList(productionItemsToExport);
    this.setSwappableDataClones(session, notSwappableEncounterTypes, swappableEncounterTypes);
    this.removeAllChoosenToManualHarmonize(session, swappableEncounterTypes);

    boolean isFirstStepHarmonizationCompleted =
        newMDSEncounterTypes.getItems().isEmpty() && productionItemsToDelete.isEmpty();

    this.setIDsAndUUIDsHarmonizationStatus(
        productionItemsToExport, differentIDsAndEqualUUID, isFirstStepHarmonizationCompleted);
    this.setNAMEsDifferencesHarmonizationStatus(
        productionItemsToExport, differentNameAndSameUUIDAndID, isFirstStepHarmonizationCompleted);

    boolean hasSecondStepHarmonization =
        isFirstStepHarmonizationCompleted
            && HarmonizeEncounterTypeController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED
            && HarmonizeEncounterTypeController.IS_NAMES_DIFFERENCES_HARMONIZED
            && !productionItemsToExport.getItems().isEmpty();

    session.setAttribute("isFirstStepHarmonizationCompleted", isFirstStepHarmonizationCompleted);
    session.setAttribute(
        "isUUIDsAndIDsHarmonized",
        HarmonizeEncounterTypeController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED);
    session.setAttribute(
        "isNamesHarmonized", HarmonizeEncounterTypeController.IS_NAMES_DIFFERENCES_HARMONIZED);
    session.setAttribute("hasSecondStepHarmonization", hasSecondStepHarmonization);
  }

  private void setIDsAndUUIDsHarmonizationStatus(
      HarmonizationData productionItemsToExport,
      HarmonizationData differentIDsAndEqualUUID,
      boolean isFirstStepHarmonizationCompleted) {

    HarmonizeEncounterTypeController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED =
        HarmonizeEncounterTypeController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED
            || differentIDsAndEqualUUID.getItems().isEmpty();

    if (!isFirstStepHarmonizationCompleted) {
      HarmonizeEncounterTypeController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = false;
    }

    if (HarmonizeEncounterTypeController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED
        && productionItemsToExport.getItems().isEmpty()
        && !differentIDsAndEqualUUID.getItems().isEmpty()) {
      HarmonizeEncounterTypeController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = false;
    }
  }

  private void setNAMEsDifferencesHarmonizationStatus(
      HarmonizationData productionItemsToExport,
      HarmonizationData differentNameAndSameUUIDAndID,
      boolean isFirstStepHarmonizationCompleted) {

    HarmonizeEncounterTypeController.IS_NAMES_DIFFERENCES_HARMONIZED =
        HarmonizeEncounterTypeController.IS_NAMES_DIFFERENCES_HARMONIZED
            || differentNameAndSameUUIDAndID.getItems().isEmpty();

    if (!isFirstStepHarmonizationCompleted) {
      HarmonizeEncounterTypeController.IS_NAMES_DIFFERENCES_HARMONIZED = false;
    }

    if (HarmonizeEncounterTypeController.IS_NAMES_DIFFERENCES_HARMONIZED
        && productionItemsToExport.getItems().isEmpty()
        && !differentNameAndSameUUIDAndID.getItems().isEmpty()) {
      HarmonizeEncounterTypeController.IS_NAMES_DIFFERENCES_HARMONIZED = false;
    }
  }

  @SuppressWarnings("unchecked")
  private void updateProductionToExportList(HarmonizationData productionItemsToExport) {

    HarmonizationData newItemsToExport =
        getConvertedData(DTOUtils.fromEncounterTypes(ENCOUNTERTYPES_NOT_PROCESSED));
    productionItemsToExport.getItems().addAll(newItemsToExport.getItems());

    List<HarmonizationItem> itemsToRemove =
        getConvertedData(DTOUtils.fromEncounterTypes(EXECUTED_ENCOUNTERTYPES_MANUALLY_CACHE))
            .getItems();
    productionItemsToExport.getItems().removeAll(itemsToRemove);

    List<HarmonizationItem> items = productionItemsToExport.getItems();

    BeanComparator comparator = new BeanComparator("value.encounterType.name");
    Collections.sort(items, comparator);
    productionItemsToExport.setItems(items);
  }

  private void setSwappableDataClones(
      HttpSession session,
      List<EncounterType> notSwappableEncounterTypes,
      List<EncounterType> swappableEncounterTypes) {

    swappableEncounterTypes.removeAll(EXECUTED_ENCOUNTERTYPES_MANUALLY_CACHE);

    for (EncounterType encounterType : ENCOUNTERTYPES_NOT_PROCESSED) {
      if (!swappableEncounterTypes.contains(encounterType)) {
        swappableEncounterTypes.add(encounterType);
      }
    }
    session.setAttribute(
        "swappableEncounterTypesClone", new ArrayList<EncounterType>(swappableEncounterTypes));
    session.setAttribute(
        "notSwappableEncounterTypesClone",
        new ArrayList<EncounterType>(notSwappableEncounterTypes));
  }

  @SuppressWarnings("unchecked")
  public void removeAllChoosenToManualHarmonize(
      HttpSession session, List<EncounterType> swappableEncounterTypes) {
    Map<EncounterType, EncounterType> manualHarmonizeEtypes =
        (Map<EncounterType, EncounterType>) session.getAttribute("manualHarmonizeEtypes");
    if (manualHarmonizeEtypes != null) {
      for (Entry<EncounterType, EncounterType> entry : manualHarmonizeEtypes.entrySet()) {
        swappableEncounterTypes.remove(entry.getKey());
      }
    }
  }

  public void processAddNewFromMetadataServer(
      HarmonizationData onlyMetadataEncounterTypes, Builder logBuilder) {
    List<EncounterTypeDTO> list = new ArrayList<>();
    for (HarmonizationItem item : onlyMetadataEncounterTypes.getItems()) {
      list.add((EncounterTypeDTO) item.getValue());
    }
    if (!list.isEmpty()) {
      this.harmonizationEncounterTypeService.saveNewEncounterTypesFromMDS(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.encountertype.harmonize.onlyOnMDServer");
      logBuilder.appendLogForNewHarmonizedFromMDSEncounterTypes(list);
    }
  }

  public void processDeleteFromProductionServer(List<EncounterTypeDTO> list, Builder logBuilder) {
    if (!list.isEmpty()) {
      this.harmonizationEncounterTypeService.deleteNewEncounterTypesFromPDS(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.encountertype.harmonize.onlyOnPServer.unused");
      logBuilder.appendLogForDeleteFromProductionServer(list);
    }
  }

  @SuppressWarnings("unchecked")
  public void processUpdateEncounterNames(HarmonizationData data, Builder logBuilder) {
    Map<String, List<EncounterTypeDTO>> list = new HashMap<>();
    for (HarmonizationItem item : data.getItems()) {
      List<EncounterTypeDTO> value = (List<EncounterTypeDTO>) item.getValue();
      if (item.isSelected()) {
        list.put((String) item.getKey(), value);
      } else {
        MDS_ENCOUNTERTYPES_NOT_PROCESSED.add(value.get(0).getEncounterType());
        ENCOUNTERTYPES_NOT_PROCESSED.add(value.get(1).getEncounterType());
      }
    }
    if (!list.isEmpty()) {
      this.harmonizationEncounterTypeService.saveEncounterTypesWithDifferentNames(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.encountertype.harmonize.differentNamesAndSameUUIDAndID");
      logBuilder.appendLogForUpdatedEncounterNames(list);
      HarmonizeEncounterTypeController.HAS_ATLEAST_ONE_ROW_HARMONIZED = true;
    }
  }

  @SuppressWarnings("unchecked")
  public void processEncounterTypesWithDiferrentIdsAndEqualUUID(
      HarmonizationData data, Builder logBuilder) {
    Map<String, List<EncounterTypeDTO>> list = new HashMap<>();

    for (HarmonizationItem item : data.getItems()) {
      List<EncounterTypeDTO> value = (List<EncounterTypeDTO>) item.getValue();
      if (item.isSelected()) {
        list.put((String) item.getKey(), value);
      } else {
        MDS_ENCOUNTERTYPES_NOT_PROCESSED.add(value.get(0).getEncounterType());
        ENCOUNTERTYPES_NOT_PROCESSED.add(value.get(1).getEncounterType());
      }
    }
    if (!list.isEmpty()) {
      this.harmonizationEncounterTypeService.saveEncounterTypesWithDifferentIDAndEqualUUID(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.encountertype.harmonize.differentID.andEqualUUID");
      logBuilder.appendLogForEncounterTypesWithDiferrentIdsAndEqualUUID(list);
      HarmonizeEncounterTypeController.HAS_ATLEAST_ONE_ROW_HARMONIZED = true;
    }
  }

  @SuppressWarnings("unchecked")
  public List<EncounterType> getMDSNotHarmonizedYet() {
    Comparator<EncounterType> comp = new BeanComparator("encounterTypeId");
    Collections.sort(MDS_ENCOUNTERTYPES_NOT_PROCESSED, comp);
    return MDS_ENCOUNTERTYPES_NOT_PROCESSED;
  }
}
