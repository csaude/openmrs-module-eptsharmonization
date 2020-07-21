package org.openmrs.module.eptsharmonization.web.delegate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpSession;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationPatientIdentifierTypeService;
import org.openmrs.module.eptsharmonization.api.exception.UUIDDuplicationException;
import org.openmrs.module.eptsharmonization.api.model.PatientIdentifierTypeDTO;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationData;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationItem;
import org.openmrs.module.eptsharmonization.web.bean.PatientIdentifierTypesHarmonizationCSVLog.Builder;
import org.openmrs.module.eptsharmonization.web.controller.HarmonizePatientIdentifierTypesController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HarmonizePatientIdentifierTypesDelegate {

  private HarmonizationPatientIdentifierTypeService harmonizationPatientIdentifierTypeService;

  @Autowired
  public void setHarmonizationPatientIdentifierTypeService(
      HarmonizationPatientIdentifierTypeService harmonizationPatientIdentifierTypeService) {
    this.harmonizationPatientIdentifierTypeService = harmonizationPatientIdentifierTypeService;
  }

  public static List<String> SUMMARY_EXECUTED_SCENARIOS = new ArrayList<>();
  public static List<PatientIdentifierType> EXECUTED_PATIENT_IDENTIFIER_TYPES_MANUALLY_CACHE =
      new ArrayList<>();
  public static List<PatientIdentifierType> PATIENT_IDENTIFIER_TYPES_NOT_PROCESSED =
      new ArrayList<>();
  private static List<PatientIdentifierType> MDS_PATIENT_IDENTIFIER_TYPES_NOT_PROCESSED =
      new ArrayList<>();

  public HarmonizationData getConvertedData(List<PatientIdentifierTypeDTO> patientIdentifierTypes) {

    List<HarmonizationItem> items = new ArrayList<>();
    for (PatientIdentifierTypeDTO patientIdentifierTypeDTO : patientIdentifierTypes) {
      HarmonizationItem item =
          new HarmonizationItem(
              patientIdentifierTypeDTO.getPatientIdentifierType().getUuid(),
              patientIdentifierTypeDTO);
      item.setEncountersCount(
          this.harmonizationPatientIdentifierTypeService.getNumberOfAffectedPatientIdentifiers(
              patientIdentifierTypeDTO));
      if (!items.contains(item)) {
        items.add(item);
      }
    }
    return new HarmonizationData(items);
  }

  public HarmonizationData getConvertedData(
      Map<String, List<PatientIdentifierTypeDTO>> mapPatientIdentifierTypes) {
    List<HarmonizationItem> items = new ArrayList<>();
    for (String key : mapPatientIdentifierTypes.keySet()) {
      List<PatientIdentifierTypeDTO> patientIdentifierTypes = mapPatientIdentifierTypes.get(key);
      if (patientIdentifierTypes != null) {
        HarmonizationItem item = new HarmonizationItem(key, patientIdentifierTypes);
        item.setEncountersCount(
            this.harmonizationPatientIdentifierTypeService.getNumberOfAffectedPatientIdentifiers(
                patientIdentifierTypes.get(1)));
        if (!items.contains(item)) {
          items.add(item);
        }
      }
    }
    return new HarmonizationData(items);
  }

  public void setHarmonizationStage(
      HttpSession session,
      HarmonizationData newMDSPatientIdentifierTypes,
      List<PatientIdentifierTypeDTO> productionItemsToDelete,
      HarmonizationData productionItemsToExport,
      HarmonizationData differentIDsAndEqualUUID,
      HarmonizationData differentNameAndSameUUIDAndID,
      HarmonizationData differentDetailsAndSameNameUUIDAndID,
      List<PatientIdentifierType> notSwappablePatientIdentifierTypes,
      List<PatientIdentifierType> swappablePatientIdentifierTypes) {

    PATIENT_IDENTIFIER_TYPES_NOT_PROCESSED.removeAll(
        EXECUTED_PATIENT_IDENTIFIER_TYPES_MANUALLY_CACHE);

    this.updateProductionToExportList(productionItemsToExport);
    this.setSwappableDataClones(
        session, notSwappablePatientIdentifierTypes, swappablePatientIdentifierTypes);
    this.removeAllChoosenToManualHarmonize(session, swappablePatientIdentifierTypes);

    boolean isFirstStepHarmonizationCompleted =
        newMDSPatientIdentifierTypes.getItems().isEmpty()
            && productionItemsToDelete.isEmpty()
            && differentDetailsAndSameNameUUIDAndID.getItems().isEmpty();

    this.setIDsAndUUIDsHarmonizationStatus(
        productionItemsToExport, differentIDsAndEqualUUID, isFirstStepHarmonizationCompleted);
    this.setNAMEsDifferencesHarmonizationStatus(
        productionItemsToExport, differentNameAndSameUUIDAndID, isFirstStepHarmonizationCompleted);

    boolean hasSecondStepHarmonization =
        isFirstStepHarmonizationCompleted
            && HarmonizePatientIdentifierTypesController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED
            && HarmonizePatientIdentifierTypesController.IS_NAMES_DIFFERENCES_HARMONIZED
            && !productionItemsToExport.getItems().isEmpty();

    session.setAttribute("isFirstStepHarmonizationCompleted", isFirstStepHarmonizationCompleted);
    session.setAttribute(
        "isUUIDsAndIDsHarmonized",
        HarmonizePatientIdentifierTypesController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED);
    session.setAttribute(
        "isNamesHarmonized",
        HarmonizePatientIdentifierTypesController.IS_NAMES_DIFFERENCES_HARMONIZED);
    session.setAttribute("hasSecondStepHarmonization", hasSecondStepHarmonization);
  }

  private void setIDsAndUUIDsHarmonizationStatus(
      HarmonizationData productionItemsToExport,
      HarmonizationData differentIDsAndEqualUUID,
      boolean isFirstStepHarmonizationCompleted) {

    HarmonizePatientIdentifierTypesController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED =
        HarmonizePatientIdentifierTypesController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED
            || differentIDsAndEqualUUID.getItems().isEmpty();

    if (!isFirstStepHarmonizationCompleted) {
      HarmonizePatientIdentifierTypesController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = false;
    }

    if (HarmonizePatientIdentifierTypesController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED
        && productionItemsToExport.getItems().isEmpty()
        && !differentIDsAndEqualUUID.getItems().isEmpty()) {
      HarmonizePatientIdentifierTypesController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = false;
    }
  }

  private void setNAMEsDifferencesHarmonizationStatus(
      HarmonizationData productionItemsToExport,
      HarmonizationData differentNameAndSameUUIDAndID,
      boolean isFirstStepHarmonizationCompleted) {

    HarmonizePatientIdentifierTypesController.IS_NAMES_DIFFERENCES_HARMONIZED =
        HarmonizePatientIdentifierTypesController.IS_NAMES_DIFFERENCES_HARMONIZED
            || differentNameAndSameUUIDAndID.getItems().isEmpty();

    if (!isFirstStepHarmonizationCompleted) {
      HarmonizePatientIdentifierTypesController.IS_NAMES_DIFFERENCES_HARMONIZED = false;
    }

    if (HarmonizePatientIdentifierTypesController.IS_NAMES_DIFFERENCES_HARMONIZED
        && productionItemsToExport.getItems().isEmpty()
        && !differentNameAndSameUUIDAndID.getItems().isEmpty()) {
      HarmonizePatientIdentifierTypesController.IS_NAMES_DIFFERENCES_HARMONIZED = false;
    }
  }

  @SuppressWarnings("unchecked")
  private void updateProductionToExportList(HarmonizationData productionItemsToExport) {

    HarmonizationData newItemsToExport =
        getConvertedData(
            DTOUtils.fromPatientIdentifierTypes(PATIENT_IDENTIFIER_TYPES_NOT_PROCESSED));
    productionItemsToExport.getItems().addAll(newItemsToExport.getItems());

    List<HarmonizationItem> itemsToRemove =
        getConvertedData(
                DTOUtils.fromPatientIdentifierTypes(
                    EXECUTED_PATIENT_IDENTIFIER_TYPES_MANUALLY_CACHE))
            .getItems();

    List<HarmonizationItem> items = productionItemsToExport.getItems();

    BeanComparator comparator = new BeanComparator("value.patientIdentifierType.id");
    Collections.sort(items, comparator);
    productionItemsToExport.getItems().removeAll(itemsToRemove);
  }

  private void setSwappableDataClones(
      HttpSession session,
      List<PatientIdentifierType> notSwappablePatientIdentifierTypes,
      List<PatientIdentifierType> swappablePatientIdentifierTypes) {

    swappablePatientIdentifierTypes.removeAll(EXECUTED_PATIENT_IDENTIFIER_TYPES_MANUALLY_CACHE);

    for (PatientIdentifierType PatientIdentifierType : PATIENT_IDENTIFIER_TYPES_NOT_PROCESSED) {
      if (!swappablePatientIdentifierTypes.contains(PatientIdentifierType)) {
        swappablePatientIdentifierTypes.add(PatientIdentifierType);
      }
    }
    session.setAttribute(
        "swappablePatientIdentifierTypesClone",
        new ArrayList<PatientIdentifierType>(swappablePatientIdentifierTypes));
    session.setAttribute(
        "notSwappablePatientIdentifierTypesClone",
        new ArrayList<PatientIdentifierType>(notSwappablePatientIdentifierTypes));
  }

  @SuppressWarnings("unchecked")
  public void removeAllChoosenToManualHarmonize(
      HttpSession session, List<PatientIdentifierType> swappablePatientIdentifierTypes) {
    Map<PatientIdentifierType, PatientIdentifierType> manualHarmonizePatientIdentifierTypes =
        (Map<PatientIdentifierType, PatientIdentifierType>)
            session.getAttribute("manualHarmonizePatientIdentifierTypes");
    if (manualHarmonizePatientIdentifierTypes != null) {
      for (Entry<PatientIdentifierType, PatientIdentifierType> entry :
          manualHarmonizePatientIdentifierTypes.entrySet()) {
        swappablePatientIdentifierTypes.remove(entry.getKey());
      }
    }
  }

  public void processAddNewFromMetadataServer(
      HarmonizationData onlyMetadataPatientIdentifierTypes, Builder logBuilder) {
    List<PatientIdentifierTypeDTO> list = new ArrayList<>();
    for (HarmonizationItem item : onlyMetadataPatientIdentifierTypes.getItems()) {
      list.add((PatientIdentifierTypeDTO) item.getValue());
    }
    if (!list.isEmpty()) {
      this.harmonizationPatientIdentifierTypeService.saveNewFromMDS(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.patientidentifiertype.harmonize.onlyOnMDServer");
      logBuilder.appendLogForNewHarmonizedFromMDSPatientIdentifierTypes(list);
    }
  }

  public void processDeleteFromProductionServer(
      List<PatientIdentifierTypeDTO> list, Builder logBuilder) {
    if (!list.isEmpty()) {
      this.harmonizationPatientIdentifierTypeService.deleteNewFromPDS(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.patientidentifiertype.harmonize.onlyOnPServer.unused");
      logBuilder.appendLogForDeleteFromProductionServer(list);
    }
  }

  @SuppressWarnings("unchecked")
  public void processUpdatePatientIdentifierTypesNames(HarmonizationData data, Builder logBuilder) {
    Map<String, List<PatientIdentifierTypeDTO>> list = new HashMap<>();
    for (HarmonizationItem item : data.getItems()) {
      List<PatientIdentifierTypeDTO> value = (List<PatientIdentifierTypeDTO>) item.getValue();
      if (item.isSelected()) {
        list.put((String) item.getKey(), value);
      } else {
        PATIENT_IDENTIFIER_TYPES_NOT_PROCESSED.add(value.get(1).getPatientIdentifierType());
        MDS_PATIENT_IDENTIFIER_TYPES_NOT_PROCESSED.add(value.get(0).getPatientIdentifierType());
      }
    }
    if (!list.isEmpty()) {
      this.harmonizationPatientIdentifierTypeService.saveWithDifferentNames(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.patientidentifiertype.harmonize.differentNamesAndSameUUIDAndID");
      logBuilder.appendLogForUpdatedPatientIdentifierTypesNames(list);
      HarmonizePatientIdentifierTypesController.HAS_ATLEAST_ONE_ROW_HARMONIZED = true;
    }
  }

  @SuppressWarnings("unchecked")
  public void processUpdatePatientIdentifierTypesDetails(
      HarmonizationData data, Builder logBuilder) {
    Map<String, List<PatientIdentifierTypeDTO>> list = new HashMap<>();
    for (HarmonizationItem item : data.getItems()) {
      List<PatientIdentifierTypeDTO> value = (List<PatientIdentifierTypeDTO>) item.getValue();
      list.put((String) item.getKey(), value);
    }
    if (!list.isEmpty()) {
      this.harmonizationPatientIdentifierTypeService.saveWithDifferentDetails(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.patientidentifiertype.harmonize.differentDetailsAndSameNamesUUIDAndID");
      logBuilder.appendLogForUpdatedPatientIdentifierTypesDetails(list);
      HarmonizePatientIdentifierTypesController.HAS_ATLEAST_ONE_ROW_HARMONIZED = true;
    }
  }

  @SuppressWarnings("unchecked")
  public void processPatientIdentifierTypesWithDiferrentIdsAndEqualUUID(
      HarmonizationData data, Builder logBuilder) {
    Map<String, List<PatientIdentifierTypeDTO>> list = new HashMap<>();

    for (HarmonizationItem item : data.getItems()) {
      List<PatientIdentifierTypeDTO> value = (List<PatientIdentifierTypeDTO>) item.getValue();
      if (item.isSelected()) {
        list.put((String) item.getKey(), value);
      } else {
        PATIENT_IDENTIFIER_TYPES_NOT_PROCESSED.add(value.get(1).getPatientIdentifierType());
        MDS_PATIENT_IDENTIFIER_TYPES_NOT_PROCESSED.add(value.get(0).getPatientIdentifierType());
      }
    }
    if (!list.isEmpty()) {
      this.harmonizationPatientIdentifierTypeService.saveWithDifferentIDAndEqualUUID(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.patientidentifiertype.harmonize.differentID.andEqualUUID");
      logBuilder.appendLogForPatientIdentifierTypesWithDiferrentIdsAndEqualUUID(list);
      HarmonizePatientIdentifierTypesController.HAS_ATLEAST_ONE_ROW_HARMONIZED = true;
    }
  }

  @SuppressWarnings("deprecation")
  public void processManualMapping(
      Map<PatientIdentifierType, PatientIdentifierType> manualHarmonizePatientIdentifierTypes,
      Builder logBuilder)
      throws UUIDDuplicationException, SQLException {

    List<HarmonizationItem> differentDetailsItems = new ArrayList<>();
    List<HarmonizationItem> differentIDsItems = new ArrayList<>();

    Map<PatientIdentifierType, PatientIdentifierType> manualHarmonizeItens = new HashMap<>();

    for (Entry<PatientIdentifierType, PatientIdentifierType> entry :
        manualHarmonizePatientIdentifierTypes.entrySet()) {
      PatientIdentifierType pdsPatientIdentifierType = entry.getKey();
      PatientIdentifierType mdsPatientIdentifierType = entry.getValue();

      if (mdsPatientIdentifierType.getUuid().equals(pdsPatientIdentifierType.getUuid())) {

        if (!mdsPatientIdentifierType.getId().equals(pdsPatientIdentifierType.getId())) {
          HarmonizationItem item =
              new HarmonizationItem(
                  mdsPatientIdentifierType.getUuid(),
                  DTOUtils.fromPatientIdentifierTypes(
                      Arrays.asList(mdsPatientIdentifierType, pdsPatientIdentifierType)));
          item.setEncountersCount(
              this.harmonizationPatientIdentifierTypeService.getNumberOfAffectedPatientIdentifiers(
                  DTOUtils.fromPatientIdentifierType(pdsPatientIdentifierType)));
          item.setSelected(Boolean.TRUE);
          if (!differentIDsItems.contains(item)) {
            differentIDsItems.add(item);
          }
        } else {
          if (!(mdsPatientIdentifierType.getName().equals(pdsPatientIdentifierType.getName())
              && StringUtils.defaultString(mdsPatientIdentifierType.getFormat())
                  .equalsIgnoreCase(StringUtils.defaultString(pdsPatientIdentifierType.getFormat()))
              && mdsPatientIdentifierType
                  .getCheckDigit()
                  .equals(pdsPatientIdentifierType.getCheckDigit())
              && mdsPatientIdentifierType
                  .getRequired()
                  .equals(pdsPatientIdentifierType.getRequired()))) {
            HarmonizationItem item =
                new HarmonizationItem(
                    mdsPatientIdentifierType.getUuid(),
                    DTOUtils.fromPatientIdentifierTypes(
                        Arrays.asList(mdsPatientIdentifierType, pdsPatientIdentifierType)));
            item.setEncountersCount(
                this.harmonizationPatientIdentifierTypeService
                    .getNumberOfAffectedPatientIdentifiers(
                        DTOUtils.fromPatientIdentifierType(pdsPatientIdentifierType)));
            item.setSelected(Boolean.TRUE);

            if (!differentDetailsItems.contains(item)) {
              differentDetailsItems.add(item);
            }
          }
        }

      } else {
        manualHarmonizeItens.put(pdsPatientIdentifierType, mdsPatientIdentifierType);
      }
    }

    if (!manualHarmonizeItens.isEmpty()) {
      this.harmonizationPatientIdentifierTypeService.saveManualMapping(
          manualHarmonizePatientIdentifierTypes);
      logBuilder.appendNewMappedPatientIdentifierTypes(manualHarmonizePatientIdentifierTypes);
      logBuilder.build();
    }
    if (!differentIDsItems.isEmpty()) {
      HarmonizationData harmonizationData = new HarmonizationData(differentIDsItems);
      processPatientIdentifierTypesWithDiferrentIdsAndEqualUUID(harmonizationData, logBuilder);
      logBuilder.build();
      HarmonizePatientIdentifierTypesController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = true;
    }
    if (!differentDetailsItems.isEmpty()) {
      HarmonizationData harmonizationData = new HarmonizationData(differentDetailsItems);
      processUpdatePatientIdentifierTypesNames(harmonizationData, logBuilder);
      logBuilder.build();
      HarmonizePatientIdentifierTypesController.IS_NAMES_DIFFERENCES_HARMONIZED = true;
    }
  }

  @SuppressWarnings("unchecked")
  public List<PatientIdentifierType> getMDSNotHarmonizedYet() {
    Comparator<PatientIdentifierType> comp = new BeanComparator("patientIdentifierTypeId");
    Collections.sort(MDS_PATIENT_IDENTIFIER_TYPES_NOT_PROCESSED, comp);
    return MDS_PATIENT_IDENTIFIER_TYPES_NOT_PROCESSED;
  }
}
