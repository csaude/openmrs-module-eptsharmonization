package org.openmrs.module.eptsharmonization.web.delegate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpSession;
import org.openmrs.PersonAttributeType;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationPersonAttributeTypeService;
import org.openmrs.module.eptsharmonization.api.model.PersonAttributeTypeDTO;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationData;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationItem;
import org.openmrs.module.eptsharmonization.web.bean.PersonAttributeTypesHarmonizationCSVLog.Builder;
import org.openmrs.module.eptsharmonization.web.controller.HarmonizePersonAttributeTypesController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HarmonizePersonAttributeTypeDelegate {

  private HarmonizationPersonAttributeTypeService harmonizationPersonAttributeTypeService;

  @Autowired
  public void setHarmonizationEncounterTypeService(
      HarmonizationPersonAttributeTypeService harmonizationEncounterTypeService) {
    this.harmonizationPersonAttributeTypeService = harmonizationEncounterTypeService;
  }

  public static List<String> SUMMARY_EXECUTED_SCENARIOS = new ArrayList<>();
  public static List<PersonAttributeType> EXECUTED_PERSONATTRIBUTETYPES_MANUALLY_CACHE =
      new ArrayList<>();
  public static List<PersonAttributeType> PERSON_ATTRIBUTE_TYPES_NOT_PROCESSED = new ArrayList<>();

  public HarmonizationData getConvertedData(List<PersonAttributeTypeDTO> personAttributeTypes) {

    List<HarmonizationItem> items = new ArrayList<>();
    for (PersonAttributeTypeDTO personAttributeTypeDTO : personAttributeTypes) {
      HarmonizationItem item =
          new HarmonizationItem(
              personAttributeTypeDTO.getPersonAttributeType().getUuid(), personAttributeTypeDTO);
      item.setEncountersCount(
          this.harmonizationPersonAttributeTypeService.getNumberOfAffectedPersonAttributes(
              personAttributeTypeDTO));
      if (!items.contains(item)) {
        items.add(item);
      }
    }
    return new HarmonizationData(items);
  }

  public HarmonizationData getConvertedData(
      Map<String, List<PersonAttributeTypeDTO>> mapPersonAttributeTypes) {
    List<HarmonizationItem> items = new ArrayList<>();
    for (String key : mapPersonAttributeTypes.keySet()) {
      List<PersonAttributeTypeDTO> personAttributeTypes = mapPersonAttributeTypes.get(key);
      if (personAttributeTypes != null) {
        HarmonizationItem item = new HarmonizationItem(key, personAttributeTypes);
        item.setEncountersCount(
            this.harmonizationPersonAttributeTypeService.getNumberOfAffectedPersonAttributes(
                personAttributeTypes.get(1)));
        if (!items.contains(item)) {
          items.add(item);
        }
      }
    }
    return new HarmonizationData(items);
  }

  public void setHarmonizationStage(
      HttpSession session,
      HarmonizationData newMDSPersonAttributeTypes,
      List<PersonAttributeTypeDTO> productionItemsToDelete,
      HarmonizationData productionItemsToExport,
      HarmonizationData differentIDsAndEqualUUID,
      HarmonizationData differentNameAndSameUUIDAndID,
      List<PersonAttributeType> notSwappablePersonAttributeTypes,
      List<PersonAttributeType> swappablePersonAttributeTypes) {

    PERSON_ATTRIBUTE_TYPES_NOT_PROCESSED.removeAll(EXECUTED_PERSONATTRIBUTETYPES_MANUALLY_CACHE);

    this.updateProductionToExportList(productionItemsToExport);
    this.setSwappableDataClones(
        session, notSwappablePersonAttributeTypes, swappablePersonAttributeTypes);
    this.removeAllChoosenToManualHarmonize(session, swappablePersonAttributeTypes);

    boolean isFirstStepHarmonizationCompleted =
        newMDSPersonAttributeTypes.getItems().isEmpty() && productionItemsToDelete.isEmpty();

    this.setIDsAndUUIDsHarmonizationStatus(
        productionItemsToExport, differentIDsAndEqualUUID, isFirstStepHarmonizationCompleted);
    this.setNAMEsDifferencesHarmonizationStatus(
        productionItemsToExport, differentNameAndSameUUIDAndID, isFirstStepHarmonizationCompleted);

    boolean hasSecondStepHarmonization =
        isFirstStepHarmonizationCompleted
            && HarmonizePersonAttributeTypesController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED
            && HarmonizePersonAttributeTypesController.IS_NAMES_DIFFERENCES_HARMONIZED
            && !productionItemsToExport.getItems().isEmpty();

    session.setAttribute("isFirstStepHarmonizationCompleted", isFirstStepHarmonizationCompleted);
    session.setAttribute(
        "isUUIDsAndIDsHarmonized",
        HarmonizePersonAttributeTypesController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED);
    session.setAttribute(
        "isNamesHarmonized",
        HarmonizePersonAttributeTypesController.IS_NAMES_DIFFERENCES_HARMONIZED);
    session.setAttribute("hasSecondStepHarmonization", hasSecondStepHarmonization);
  }

  private void setIDsAndUUIDsHarmonizationStatus(
      HarmonizationData productionItemsToExport,
      HarmonizationData differentIDsAndEqualUUID,
      boolean isFirstStepHarmonizationCompleted) {

    HarmonizePersonAttributeTypesController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED =
        HarmonizePersonAttributeTypesController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED
            || differentIDsAndEqualUUID.getItems().isEmpty();

    if (!isFirstStepHarmonizationCompleted) {
      HarmonizePersonAttributeTypesController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = false;
    }

    if (HarmonizePersonAttributeTypesController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED
        && productionItemsToExport.getItems().isEmpty()
        && !differentIDsAndEqualUUID.getItems().isEmpty()) {
      HarmonizePersonAttributeTypesController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = false;
    }
  }

  private void setNAMEsDifferencesHarmonizationStatus(
      HarmonizationData productionItemsToExport,
      HarmonizationData differentNameAndSameUUIDAndID,
      boolean isFirstStepHarmonizationCompleted) {

    HarmonizePersonAttributeTypesController.IS_NAMES_DIFFERENCES_HARMONIZED =
        HarmonizePersonAttributeTypesController.IS_NAMES_DIFFERENCES_HARMONIZED
            || differentNameAndSameUUIDAndID.getItems().isEmpty();

    if (!isFirstStepHarmonizationCompleted) {
      HarmonizePersonAttributeTypesController.IS_NAMES_DIFFERENCES_HARMONIZED = false;
    }

    if (HarmonizePersonAttributeTypesController.IS_NAMES_DIFFERENCES_HARMONIZED
        && productionItemsToExport.getItems().isEmpty()
        && !differentNameAndSameUUIDAndID.getItems().isEmpty()) {
      HarmonizePersonAttributeTypesController.IS_NAMES_DIFFERENCES_HARMONIZED = false;
    }
  }

  private void updateProductionToExportList(HarmonizationData productionItemsToExport) {

    HarmonizationData newItemsToExport =
        getConvertedData(DTOUtils.fromPersonAttributeTypes(PERSON_ATTRIBUTE_TYPES_NOT_PROCESSED));
    productionItemsToExport.getItems().addAll(newItemsToExport.getItems());

    List<HarmonizationItem> itemsToRemove =
        getConvertedData(
                DTOUtils.fromPersonAttributeTypes(EXECUTED_PERSONATTRIBUTETYPES_MANUALLY_CACHE))
            .getItems();
    productionItemsToExport.getItems().removeAll(itemsToRemove);
  }

  private void setSwappableDataClones(
      HttpSession session,
      List<PersonAttributeType> notSwappablePersonAttributeTypes,
      List<PersonAttributeType> swappablePersonAttributeTypes) {

    swappablePersonAttributeTypes.removeAll(EXECUTED_PERSONATTRIBUTETYPES_MANUALLY_CACHE);

    for (PersonAttributeType PersonAttributeType : PERSON_ATTRIBUTE_TYPES_NOT_PROCESSED) {
      if (!swappablePersonAttributeTypes.contains(PersonAttributeType)) {
        swappablePersonAttributeTypes.add(PersonAttributeType);
      }
    }
    session.setAttribute(
        "swappablePersonAttributeTypesClone",
        new ArrayList<PersonAttributeType>(swappablePersonAttributeTypes));
    session.setAttribute(
        "notSwappablePersonAttributeTypesClone",
        new ArrayList<PersonAttributeType>(notSwappablePersonAttributeTypes));
  }

  @SuppressWarnings("unchecked")
  public void removeAllChoosenToManualHarmonize(
      HttpSession session, List<PersonAttributeType> swappablePersonAttributeTypes) {
    Map<PersonAttributeType, PersonAttributeType> manualHarmonizePersonAttributeTypes =
        (Map<PersonAttributeType, PersonAttributeType>)
            session.getAttribute("manualHarmonizePersonAttributeTypes");
    if (manualHarmonizePersonAttributeTypes != null) {
      for (Entry<PersonAttributeType, PersonAttributeType> entry :
          manualHarmonizePersonAttributeTypes.entrySet()) {
        swappablePersonAttributeTypes.remove(entry.getKey());
      }
    }
  }

  public void processAddNewFromMetadataServer(
      HarmonizationData onlyMetadataPersonAttributeTypes, Builder logBuilder) {
    List<PersonAttributeTypeDTO> list = new ArrayList<>();
    for (HarmonizationItem item : onlyMetadataPersonAttributeTypes.getItems()) {
      list.add((PersonAttributeTypeDTO) item.getValue());
    }
    if (!list.isEmpty()) {
      this.harmonizationPersonAttributeTypeService.saveNewPersonAttributeTypesFromMDS(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.personattributetype.harmonize.onlyOnMDServer");
      logBuilder.appendLogForNewHarmonizedFromMDSPersonAttributeTypes(list);
    }
  }

  public void processDeleteFromProductionServer(
      List<PersonAttributeTypeDTO> list, Builder logBuilder) {
    if (!list.isEmpty()) {
      this.harmonizationPersonAttributeTypeService.deleteNewPersonAttributeTypesFromPDS(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.personattributetype.harmonize.onlyOnPServer.unused");
      logBuilder.appendLogForDeleteFromProductionServer(list);
    }
  }

  @SuppressWarnings("unchecked")
  public void processUpdatePersonAttributeTypesNames(HarmonizationData data, Builder logBuilder) {
    Map<String, List<PersonAttributeTypeDTO>> list = new HashMap<>();
    for (HarmonizationItem item : data.getItems()) {
      List<PersonAttributeTypeDTO> value = (List<PersonAttributeTypeDTO>) item.getValue();
      if (item.isSelected()) {
        list.put((String) item.getKey(), value);
      } else {
        PERSON_ATTRIBUTE_TYPES_NOT_PROCESSED.add(value.get(1).getPersonAttributeType());
      }
    }
    if (!list.isEmpty()) {
      this.harmonizationPersonAttributeTypeService.savePersonAttributeTypesWithDifferentNames(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.personattributetype.harmonize.differentNamesAndSameUUIDAndID");
      logBuilder.appendLogForUpdatedPersonAttributeTypesNames(list);
      HarmonizePersonAttributeTypesController.HAS_ATLEAST_ONE_ROW_HARMONIZED = true;
    }
  }

  @SuppressWarnings("unchecked")
  public void processPersonAttributeTypesWithDiferrentIdsAndEqualUUID(
      HarmonizationData data, Builder logBuilder) {
    Map<String, List<PersonAttributeTypeDTO>> list = new HashMap<>();

    for (HarmonizationItem item : data.getItems()) {
      List<PersonAttributeTypeDTO> value = (List<PersonAttributeTypeDTO>) item.getValue();
      if (item.isSelected()) {
        list.put((String) item.getKey(), value);
      } else {
        PERSON_ATTRIBUTE_TYPES_NOT_PROCESSED.add(value.get(1).getPersonAttributeType());
      }
    }
    if (!list.isEmpty()) {
      this.harmonizationPersonAttributeTypeService
          .savePersonAttributeTypesWithDifferentIDAndEqualUUID(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.personattributetype.harmonize.differentID.andEqualUUID");
      logBuilder.appendLogForPersonAttributeTypesWithDiferrentIdsAndEqualUUID(list);
      HarmonizePersonAttributeTypesController.HAS_ATLEAST_ONE_ROW_HARMONIZED = true;
    }
  }

  public void processManualMapping(
      Map<PersonAttributeType, PersonAttributeType> manualHarmonizePersonAttributeTypes,
      Builder logBuilder) {

    List<HarmonizationItem> differntNamesItems = new ArrayList<>();
    List<HarmonizationItem> differntIDsItems = new ArrayList<>();

    Map<PersonAttributeType, PersonAttributeType> manualHarmonizeItens = new HashMap<>();

    for (Entry<PersonAttributeType, PersonAttributeType> entry :
        manualHarmonizePersonAttributeTypes.entrySet()) {
      PersonAttributeType pdsPersonAttributeType = entry.getKey();
      PersonAttributeType mdsPersonAttributeType = entry.getValue();

      if (mdsPersonAttributeType.getUuid().equals(pdsPersonAttributeType.getUuid())) {

        if (!mdsPersonAttributeType.getId().equals(pdsPersonAttributeType.getId())) {
          HarmonizationItem item =
              new HarmonizationItem(
                  mdsPersonAttributeType.getUuid(),
                  DTOUtils.fromPersonAttributeTypes(
                      Arrays.asList(mdsPersonAttributeType, pdsPersonAttributeType)));
          item.setEncountersCount(
              this.harmonizationPersonAttributeTypeService.getNumberOfAffectedPersonAttributes(
                  DTOUtils.fromPersonAttributeType(pdsPersonAttributeType)));
          item.setSelected(Boolean.TRUE);
          if (!differntIDsItems.contains(item)) {
            differntIDsItems.add(item);
          }
        } else {
          if (!mdsPersonAttributeType.getName().equals(pdsPersonAttributeType.getName())) {
            HarmonizationItem item =
                new HarmonizationItem(
                    mdsPersonAttributeType.getUuid(),
                    DTOUtils.fromPersonAttributeTypes(
                        Arrays.asList(mdsPersonAttributeType, pdsPersonAttributeType)));
            item.setEncountersCount(
                this.harmonizationPersonAttributeTypeService.getNumberOfAffectedPersonAttributes(
                    DTOUtils.fromPersonAttributeType(pdsPersonAttributeType)));
            item.setSelected(Boolean.TRUE);

            if (!differntNamesItems.contains(item)) {
              differntNamesItems.add(item);
            }
          }
        }

      } else {
        manualHarmonizeItens.put(pdsPersonAttributeType, mdsPersonAttributeType);
      }
    }

    if (!manualHarmonizeItens.isEmpty()) {
      this.harmonizationPersonAttributeTypeService.saveManualMapping(
          manualHarmonizePersonAttributeTypes);
      logBuilder.appendNewMappedPersonAttributeTypes(manualHarmonizePersonAttributeTypes);
      logBuilder.build();
    }
    if (!differntIDsItems.isEmpty()) {
      HarmonizationData harmonizationData = new HarmonizationData(differntIDsItems);
      processPersonAttributeTypesWithDiferrentIdsAndEqualUUID(harmonizationData, logBuilder);
      logBuilder.build();
      HarmonizePersonAttributeTypesController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = true;
    }
    if (!differntNamesItems.isEmpty()) {
      HarmonizationData harmonizationData = new HarmonizationData(differntNamesItems);
      processUpdatePersonAttributeTypesNames(harmonizationData, logBuilder);
      logBuilder.build();
      HarmonizePersonAttributeTypesController.IS_NAMES_DIFFERENCES_HARMONIZED = true;
    }
  }
}
