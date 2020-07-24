package org.openmrs.module.eptsharmonization.web.delegate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpSession;
import org.apache.commons.beanutils.BeanComparator;
import org.openmrs.module.eptsharmonization.api.HarmonizationProgramWorkflowStateService;
import org.openmrs.module.eptsharmonization.api.model.ProgramWorkflowStateDTO;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationData;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationItem;
import org.openmrs.module.eptsharmonization.web.bean.ProgramWorkflowStatesHarmonizationCSVLog.Builder;
import org.openmrs.module.eptsharmonization.web.controller.HarmonizeProgramWorkflowStatesController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HarmonizeProgramWorkflowStatesDelegate {

  private HarmonizationProgramWorkflowStateService harmonizationProgramWorkflowStateService;

  @Autowired
  public void setHarmonizationProgramWorkflowStateService(
      HarmonizationProgramWorkflowStateService harmonizationProgramWorkflowStateService) {
    this.harmonizationProgramWorkflowStateService = harmonizationProgramWorkflowStateService;
  }

  public static List<String> SUMMARY_EXECUTED_SCENARIOS = new ArrayList<>();
  public static List<ProgramWorkflowStateDTO> EXECUTED_PROGRAM_WORKFLOW_STATES_MANUALLY_CACHE =
      new ArrayList<>();
  public static List<ProgramWorkflowStateDTO> PROGRAM_WORKFLOW_STATES_NOT_PROCESSED =
      new ArrayList<>();
  public static List<ProgramWorkflowStateDTO> MDS_PROGRAM_WORKFLOW_STATES_NOT_PROCESSED =
      new ArrayList<>();

  public HarmonizationData getConvertedData(List<ProgramWorkflowStateDTO> programWorkflowStates) {

    List<HarmonizationItem> items = new ArrayList<>();
    for (ProgramWorkflowStateDTO programWorkflowStateDTO : programWorkflowStates) {
      HarmonizationItem item =
          new HarmonizationItem(
              programWorkflowStateDTO.getProgramWorkflowState().getUuid(), programWorkflowStateDTO);
      item.setEncountersCount(
          this.harmonizationProgramWorkflowStateService.getNumberOfAffectedConceptStateConversions(
              programWorkflowStateDTO));
      item.setFormsCount(
          this.harmonizationProgramWorkflowStateService.getNumberOfAffectedPatientStates(
              programWorkflowStateDTO));
      items.add(item);
    }
    return new HarmonizationData(items);
  }

  public HarmonizationData getConvertedData(
      Map<String, List<ProgramWorkflowStateDTO>> mapProgramWorkflowStates) {
    List<HarmonizationItem> items = new ArrayList<>();
    for (String key : mapProgramWorkflowStates.keySet()) {
      List<ProgramWorkflowStateDTO> programWorkflowStates = mapProgramWorkflowStates.get(key);
      if (programWorkflowStates != null) {
        HarmonizationItem item = new HarmonizationItem(key, programWorkflowStates);
        item.setEncountersCount(
            this.harmonizationProgramWorkflowStateService
                .getNumberOfAffectedConceptStateConversions(programWorkflowStates.get(1)));
        item.setFormsCount(
            this.harmonizationProgramWorkflowStateService.getNumberOfAffectedPatientStates(
                programWorkflowStates.get(1)));
        items.add(item);
      }
    }
    return new HarmonizationData(items);
  }

  public void setHarmonizationStage(
      HttpSession session,
      HarmonizationData newMDSProgramWorkflowStates,
      List<ProgramWorkflowStateDTO> productionItemsToDelete,
      HarmonizationData productionItemsToExport,
      HarmonizationData differentIDsAndEqualUUID,
      HarmonizationData differentNameAndSameUUIDAndID,
      List<ProgramWorkflowStateDTO> notSwappableProgramWorkflowStates,
      List<ProgramWorkflowStateDTO> swappableProgramWorkflowStates) {

    PROGRAM_WORKFLOW_STATES_NOT_PROCESSED.removeAll(
        EXECUTED_PROGRAM_WORKFLOW_STATES_MANUALLY_CACHE);

    this.updateProductionToExportList(productionItemsToExport);
    this.setSwappableDataClones(
        session, notSwappableProgramWorkflowStates, swappableProgramWorkflowStates);
    this.removeAllChoosenToManualHarmonize(session, swappableProgramWorkflowStates);

    boolean isFirstStepHarmonizationCompleted =
        newMDSProgramWorkflowStates.getItems().isEmpty() && productionItemsToDelete.isEmpty();

    this.setIDsAndUUIDsHarmonizationStatus(
        productionItemsToExport, differentIDsAndEqualUUID, isFirstStepHarmonizationCompleted);
    this.setNAMEsDifferencesHarmonizationStatus(
        productionItemsToExport, differentNameAndSameUUIDAndID, isFirstStepHarmonizationCompleted);

    boolean hasSecondStepHarmonization =
        isFirstStepHarmonizationCompleted
            && HarmonizeProgramWorkflowStatesController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED
            && HarmonizeProgramWorkflowStatesController.IS_NAMES_DIFFERENCES_HARMONIZED
            && !productionItemsToExport.getItems().isEmpty();

    session.setAttribute("isFirstStepHarmonizationCompleted", isFirstStepHarmonizationCompleted);
    session.setAttribute(
        "isUUIDsAndIDsHarmonized",
        HarmonizeProgramWorkflowStatesController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED);
    session.setAttribute(
        "isNamesHarmonized",
        HarmonizeProgramWorkflowStatesController.IS_NAMES_DIFFERENCES_HARMONIZED);
    session.setAttribute("hasSecondStepHarmonization", hasSecondStepHarmonization);
  }

  private void setIDsAndUUIDsHarmonizationStatus(
      HarmonizationData productionItemsToExport,
      HarmonizationData differentIDsAndEqualUUID,
      boolean isFirstStepHarmonizationCompleted) {

    HarmonizeProgramWorkflowStatesController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED =
        HarmonizeProgramWorkflowStatesController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED
            || differentIDsAndEqualUUID.getItems().isEmpty();

    if (!isFirstStepHarmonizationCompleted) {
      HarmonizeProgramWorkflowStatesController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = false;
    }

    if (HarmonizeProgramWorkflowStatesController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED
        && productionItemsToExport.getItems().isEmpty()
        && !differentIDsAndEqualUUID.getItems().isEmpty()) {
      HarmonizeProgramWorkflowStatesController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = false;
    }
  }

  private void setNAMEsDifferencesHarmonizationStatus(
      HarmonizationData productionItemsToExport,
      HarmonizationData differentNameAndSameUUIDAndID,
      boolean isFirstStepHarmonizationCompleted) {

    HarmonizeProgramWorkflowStatesController.IS_NAMES_DIFFERENCES_HARMONIZED =
        HarmonizeProgramWorkflowStatesController.IS_NAMES_DIFFERENCES_HARMONIZED
            || differentNameAndSameUUIDAndID.getItems().isEmpty();

    if (!isFirstStepHarmonizationCompleted) {
      HarmonizeProgramWorkflowStatesController.IS_NAMES_DIFFERENCES_HARMONIZED = false;
    }

    if (HarmonizeProgramWorkflowStatesController.IS_NAMES_DIFFERENCES_HARMONIZED
        && productionItemsToExport.getItems().isEmpty()
        && !differentNameAndSameUUIDAndID.getItems().isEmpty()) {
      HarmonizeProgramWorkflowStatesController.IS_NAMES_DIFFERENCES_HARMONIZED = false;
    }
  }

  @SuppressWarnings("unchecked")
  private void updateProductionToExportList(HarmonizationData productionItemsToExport) {

    HarmonizationData newItemsToExport = getConvertedData(PROGRAM_WORKFLOW_STATES_NOT_PROCESSED);
    productionItemsToExport.getItems().addAll(newItemsToExport.getItems());

    List<HarmonizationItem> itemsToRemove =
        getConvertedData(EXECUTED_PROGRAM_WORKFLOW_STATES_MANUALLY_CACHE).getItems();

    List<HarmonizationItem> items = productionItemsToExport.getItems();

    BeanComparator comparator = new BeanComparator("value.programWorkflowState.id");
    Collections.sort(items, comparator);
    productionItemsToExport.getItems().removeAll(itemsToRemove);
  }

  private void setSwappableDataClones(
      HttpSession session,
      List<ProgramWorkflowStateDTO> notSwappableProgramWorkflowStates,
      List<ProgramWorkflowStateDTO> swappableProgramWorkflowStates) {

    swappableProgramWorkflowStates.removeAll(EXECUTED_PROGRAM_WORKFLOW_STATES_MANUALLY_CACHE);

    for (ProgramWorkflowStateDTO programWorkflowState : PROGRAM_WORKFLOW_STATES_NOT_PROCESSED) {
      if (!swappableProgramWorkflowStates.contains(programWorkflowState)) {
        swappableProgramWorkflowStates.add(programWorkflowState);
      }
    }
    session.setAttribute("swappableProgramWorkflowStatesClone", swappableProgramWorkflowStates);
    session.setAttribute(
        "notSwappableProgramWorkflowStatesClone", notSwappableProgramWorkflowStates);
  }

  @SuppressWarnings("unchecked")
  public void removeAllChoosenToManualHarmonize(
      HttpSession session, List<ProgramWorkflowStateDTO> swappableProgramWorkflowStates) {
    Map<ProgramWorkflowStateDTO, ProgramWorkflowStateDTO> manualHarmonizeProgramWorkflowStates =
        (Map<ProgramWorkflowStateDTO, ProgramWorkflowStateDTO>)
            session.getAttribute("manualHarmonizeProgramWorkflowStates");
    if (manualHarmonizeProgramWorkflowStates != null) {
      for (Entry<ProgramWorkflowStateDTO, ProgramWorkflowStateDTO> entry :
          manualHarmonizeProgramWorkflowStates.entrySet()) {
        swappableProgramWorkflowStates.remove(entry.getKey());
      }
    }
  }

  public void processAddNewFromMetadataServer(
      HarmonizationData onlyMetadataProgramWorkflowStates, Builder logBuilder) {
    List<ProgramWorkflowStateDTO> list = new ArrayList<>();
    for (HarmonizationItem item : onlyMetadataProgramWorkflowStates.getItems()) {
      list.add((ProgramWorkflowStateDTO) item.getValue());
    }
    if (!list.isEmpty()) {
      this.harmonizationProgramWorkflowStateService.saveNewProgramWorkflowStatesFromMDS(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.programworkflowstate.harmonize.onlyOnMDServer");
      logBuilder.appendLogForNewHarmonizedFromMDSProgramWorkflowStates(list);
    }
  }

  public void processDeleteFromProductionServer(
      List<ProgramWorkflowStateDTO> list, Builder logBuilder) {
    if (!list.isEmpty()) {
      this.harmonizationProgramWorkflowStateService.deleteNewProgramWorkflowStatesFromPDS(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.programworkflowstate.harmonize.onlyOnPServer.unused");
      logBuilder.appendLogForDeleteFromProductionServer(list);
    }
  }

  @SuppressWarnings("unchecked")
  public void processUpdateProgramWorkflowsAndConcepts(HarmonizationData data, Builder logBuilder) {
    Map<String, List<ProgramWorkflowStateDTO>> list = new HashMap<>();
    for (HarmonizationItem item : data.getItems()) {
      List<ProgramWorkflowStateDTO> value = (List<ProgramWorkflowStateDTO>) item.getValue();
      if (item.isSelected()) {
        list.put((String) item.getKey(), value);
      } else {
        MDS_PROGRAM_WORKFLOW_STATES_NOT_PROCESSED.add(value.get(0));
        PROGRAM_WORKFLOW_STATES_NOT_PROCESSED.add(value.get(1));
      }
    }
    if (!list.isEmpty()) {
      this.harmonizationProgramWorkflowStateService.updateStatesWithDifferentWorkflowsOrConcept(
          list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.programworkflowstate.harmonize.differentProgramWorkflowsOrConceptsAndSameUUIDAndID");
      logBuilder.appendLogForUpdatedProgramsAndConcepts(list);
      HarmonizeProgramWorkflowStatesController.HAS_ATLEAST_ONE_ROW_HARMONIZED = true;
    }
  }

  @SuppressWarnings("unchecked")
  public void processProgramWorkflowStatesWithDiferrentIdsAndEqualUUID(
      HarmonizationData data, Builder logBuilder) {
    Map<String, List<ProgramWorkflowStateDTO>> list = new HashMap<>();

    for (HarmonizationItem item : data.getItems()) {
      List<ProgramWorkflowStateDTO> value = (List<ProgramWorkflowStateDTO>) item.getValue();
      if (item.isSelected()) {
        list.put((String) item.getKey(), value);
      } else {
        MDS_PROGRAM_WORKFLOW_STATES_NOT_PROCESSED.add(value.get(0));
        PROGRAM_WORKFLOW_STATES_NOT_PROCESSED.add(value.get(1));
      }
    }
    if (!list.isEmpty()) {
      this.harmonizationProgramWorkflowStateService.saveStatesWithDifferentIDAndEqualUUID(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.programworkflowstate.harmonize.differentID.andEqualUUID");
      logBuilder.appendLogForProgramWorkflowStatesWithDiferrentIdsAndEqualUUID(list);
      HarmonizeProgramWorkflowStatesController.HAS_ATLEAST_ONE_ROW_HARMONIZED = true;
    }
  }
}
