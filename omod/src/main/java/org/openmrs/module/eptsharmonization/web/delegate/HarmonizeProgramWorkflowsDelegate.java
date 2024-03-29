package org.openmrs.module.eptsharmonization.web.delegate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpSession;
import org.apache.commons.beanutils.BeanComparator;
import org.openmrs.module.eptsharmonization.api.HarmonizationProgramWorkflowService;
import org.openmrs.module.eptsharmonization.api.model.ProgramWorkflowDTO;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationData;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationItem;
import org.openmrs.module.eptsharmonization.web.bean.ProgramWorkflowsHarmonizationCSVLog.Builder;
import org.openmrs.module.eptsharmonization.web.controller.HarmonizeProgramWorkflowsController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HarmonizeProgramWorkflowsDelegate {

  private HarmonizationProgramWorkflowService harmonizationProgramWorkflowService;

  @Autowired
  public void setHarmonizationProgramWorkflowService(
      HarmonizationProgramWorkflowService harmonizationProgramWorkflowService) {
    this.harmonizationProgramWorkflowService = harmonizationProgramWorkflowService;
  }

  public static List<String> SUMMARY_EXECUTED_SCENARIOS = new ArrayList<>();
  public static List<ProgramWorkflowDTO> EXECUTED_PROGRAM_WORKFLOWS_MANUALLY_CACHE =
      new ArrayList<>();
  public static List<ProgramWorkflowDTO> PROGRAM_WORKFLOWS_NOT_PROCESSED = new ArrayList<>();
  public static List<ProgramWorkflowDTO> MDS_PROGRAM_WORKFLOWS_NOT_PROCESSED = new ArrayList<>();

  public HarmonizationData getConvertedData(List<ProgramWorkflowDTO> programWorkflows) {

    List<HarmonizationItem> items = new ArrayList<>();
    for (ProgramWorkflowDTO programWorkflowDTO : programWorkflows) {
      HarmonizationItem item =
          new HarmonizationItem(
              programWorkflowDTO.getProgramWorkflow().getUuid(), programWorkflowDTO);
      item.setEncountersCount(
          this.harmonizationProgramWorkflowService.getNumberOfAffectedConceptStateConversions(
              programWorkflowDTO));
      item.setFormsCount(
          this.harmonizationProgramWorkflowService.getNumberOfAffectedProgramWorkflowStates(
              programWorkflowDTO));
      items.add(item);
    }
    return new HarmonizationData(items);
  }

  public HarmonizationData getConvertedData(
      Map<String, List<ProgramWorkflowDTO>> mapProgramWorkflows) {
    List<HarmonizationItem> items = new ArrayList<>();
    for (String key : mapProgramWorkflows.keySet()) {
      List<ProgramWorkflowDTO> programWorkflows = mapProgramWorkflows.get(key);
      if (programWorkflows != null) {
        HarmonizationItem item = new HarmonizationItem(key, programWorkflows);
        item.setEncountersCount(
            this.harmonizationProgramWorkflowService.getNumberOfAffectedConceptStateConversions(
                programWorkflows.get(1)));
        item.setFormsCount(
            this.harmonizationProgramWorkflowService.getNumberOfAffectedProgramWorkflowStates(
                programWorkflows.get(1)));
        items.add(item);
      }
    }
    return new HarmonizationData(items);
  }

  public void setHarmonizationStage(
      HttpSession session,
      HarmonizationData newMDSProgramWorkflows,
      List<ProgramWorkflowDTO> productionItemsToDelete,
      HarmonizationData productionItemsToExport,
      HarmonizationData differentIDsAndEqualUUID,
      HarmonizationData differentNameAndSameUUIDAndID,
      List<ProgramWorkflowDTO> notSwappableProgramWorkflows,
      List<ProgramWorkflowDTO> swappableProgramWorkflows) {

    PROGRAM_WORKFLOWS_NOT_PROCESSED.removeAll(EXECUTED_PROGRAM_WORKFLOWS_MANUALLY_CACHE);

    this.updateProductionToExportList(productionItemsToExport);
    this.setSwappableDataClones(session, notSwappableProgramWorkflows, swappableProgramWorkflows);
    this.removeAllChoosenToManualHarmonize(session, swappableProgramWorkflows);

    boolean isFirstStepHarmonizationCompleted =
        newMDSProgramWorkflows.getItems().isEmpty() && productionItemsToDelete.isEmpty();

    this.setIDsAndUUIDsHarmonizationStatus(
        productionItemsToExport, differentIDsAndEqualUUID, isFirstStepHarmonizationCompleted);
    this.setNAMEsDifferencesHarmonizationStatus(
        productionItemsToExport, differentNameAndSameUUIDAndID, isFirstStepHarmonizationCompleted);

    boolean hasSecondStepHarmonization =
        isFirstStepHarmonizationCompleted
            && HarmonizeProgramWorkflowsController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED
            && HarmonizeProgramWorkflowsController.IS_NAMES_DIFFERENCES_HARMONIZED
            && !productionItemsToExport.getItems().isEmpty();

    session.setAttribute("isFirstStepHarmonizationCompleted", isFirstStepHarmonizationCompleted);
    session.setAttribute(
        "isUUIDsAndIDsHarmonized",
        HarmonizeProgramWorkflowsController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED);
    session.setAttribute(
        "isNamesHarmonized", HarmonizeProgramWorkflowsController.IS_NAMES_DIFFERENCES_HARMONIZED);
    session.setAttribute("hasSecondStepHarmonization", hasSecondStepHarmonization);
  }

  private void setIDsAndUUIDsHarmonizationStatus(
      HarmonizationData productionItemsToExport,
      HarmonizationData differentIDsAndEqualUUID,
      boolean isFirstStepHarmonizationCompleted) {

    HarmonizeProgramWorkflowsController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED =
        HarmonizeProgramWorkflowsController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED
            || differentIDsAndEqualUUID.getItems().isEmpty();

    if (!isFirstStepHarmonizationCompleted) {
      HarmonizeProgramWorkflowsController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = false;
    }

    if (HarmonizeProgramWorkflowsController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED
        && productionItemsToExport.getItems().isEmpty()
        && !differentIDsAndEqualUUID.getItems().isEmpty()) {
      HarmonizeProgramWorkflowsController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = false;
    }
  }

  private void setNAMEsDifferencesHarmonizationStatus(
      HarmonizationData productionItemsToExport,
      HarmonizationData differentNameAndSameUUIDAndID,
      boolean isFirstStepHarmonizationCompleted) {

    HarmonizeProgramWorkflowsController.IS_NAMES_DIFFERENCES_HARMONIZED =
        HarmonizeProgramWorkflowsController.IS_NAMES_DIFFERENCES_HARMONIZED
            || differentNameAndSameUUIDAndID.getItems().isEmpty();

    if (!isFirstStepHarmonizationCompleted) {
      HarmonizeProgramWorkflowsController.IS_NAMES_DIFFERENCES_HARMONIZED = false;
    }

    if (HarmonizeProgramWorkflowsController.IS_NAMES_DIFFERENCES_HARMONIZED
        && productionItemsToExport.getItems().isEmpty()
        && !differentNameAndSameUUIDAndID.getItems().isEmpty()) {
      HarmonizeProgramWorkflowsController.IS_NAMES_DIFFERENCES_HARMONIZED = false;
    }
  }

  @SuppressWarnings("unchecked")
  private void updateProductionToExportList(HarmonizationData productionItemsToExport) {

    HarmonizationData newItemsToExport = getConvertedData(PROGRAM_WORKFLOWS_NOT_PROCESSED);
    productionItemsToExport.getItems().addAll(newItemsToExport.getItems());

    List<HarmonizationItem> itemsToRemove =
        getConvertedData(EXECUTED_PROGRAM_WORKFLOWS_MANUALLY_CACHE).getItems();

    List<HarmonizationItem> items = productionItemsToExport.getItems();

    BeanComparator comparator = new BeanComparator("value.programWorkflow.id");
    Collections.sort(items, comparator);
    productionItemsToExport.getItems().removeAll(itemsToRemove);
  }

  private void setSwappableDataClones(
      HttpSession session,
      List<ProgramWorkflowDTO> notSwappableProgramWorkflows,
      List<ProgramWorkflowDTO> swappableProgramWorkflows) {

    swappableProgramWorkflows.removeAll(EXECUTED_PROGRAM_WORKFLOWS_MANUALLY_CACHE);

    for (ProgramWorkflowDTO programWorkflow : PROGRAM_WORKFLOWS_NOT_PROCESSED) {
      if (!swappableProgramWorkflows.contains(programWorkflow)) {
        swappableProgramWorkflows.add(programWorkflow);
      }
    }
    session.setAttribute("swappableProgramWorkflowsClone", swappableProgramWorkflows);
    session.setAttribute("notSwappableProgramWorkflowsClone", notSwappableProgramWorkflows);
  }

  @SuppressWarnings("unchecked")
  public void removeAllChoosenToManualHarmonize(
      HttpSession session, List<ProgramWorkflowDTO> swappableProgramWorkflows) {
    Map<ProgramWorkflowDTO, ProgramWorkflowDTO> manualHarmonizeProgramWorkflows =
        (Map<ProgramWorkflowDTO, ProgramWorkflowDTO>)
            session.getAttribute("manualHarmonizeProgramWorkflows");
    if (manualHarmonizeProgramWorkflows != null) {
      for (Entry<ProgramWorkflowDTO, ProgramWorkflowDTO> entry :
          manualHarmonizeProgramWorkflows.entrySet()) {
        swappableProgramWorkflows.remove(entry.getKey());
      }
    }
  }

  public void processAddNewFromMetadataServer(
      HarmonizationData onlyMetadataProgramWorkflows, Builder logBuilder) {
    List<ProgramWorkflowDTO> list = new ArrayList<>();
    for (HarmonizationItem item : onlyMetadataProgramWorkflows.getItems()) {
      list.add((ProgramWorkflowDTO) item.getValue());
    }
    if (!list.isEmpty()) {
      this.harmonizationProgramWorkflowService.saveNewProgramWorkflowsFromMDS(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.programworkflow.harmonize.onlyOnMDServer");
      logBuilder.appendLogForNewHarmonizedFromMDSProgramWorkflows(list);
    }
  }

  public void processDeleteFromProductionServer(List<ProgramWorkflowDTO> list, Builder logBuilder) {
    if (!list.isEmpty()) {
      this.harmonizationProgramWorkflowService.deleteNewProgramWorkflowsFromPDS(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.programworkflow.harmonize.onlyOnPServer.unused");
      logBuilder.appendLogForDeleteFromProductionServer(list);
    }
  }

  @SuppressWarnings("unchecked")
  public void processUpdateProgramWorkflowsProgramsAndConcepts(
      HarmonizationData data, Builder logBuilder) {
    Map<String, List<ProgramWorkflowDTO>> list = new HashMap<>();
    for (HarmonizationItem item : data.getItems()) {
      List<ProgramWorkflowDTO> value = (List<ProgramWorkflowDTO>) item.getValue();
      if (item.isSelected()) {
        list.put((String) item.getKey(), value);
      } else {
        PROGRAM_WORKFLOWS_NOT_PROCESSED.add(value.get(1));
        MDS_PROGRAM_WORKFLOWS_NOT_PROCESSED.add(value.get(0));
      }
    }
    if (!list.isEmpty()) {
      this.harmonizationProgramWorkflowService.updateProgramWorkflowsWithDifferentProgramsOrConcept(
          list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.programworkflow.harmonize.differentProgramsOrConceptsAndSameUUIDAndID");
      logBuilder.appendLogForUpdatedProgramsAndConcepts(list);
      HarmonizeProgramWorkflowsController.HAS_ATLEAST_ONE_ROW_HARMONIZED = true;
    }
  }

  @SuppressWarnings("unchecked")
  public void processProgramWorkflowsWithDiferrentIdsAndEqualUUID(
      HarmonizationData data, Builder logBuilder) {
    Map<String, List<ProgramWorkflowDTO>> list = new HashMap<>();

    for (HarmonizationItem item : data.getItems()) {
      List<ProgramWorkflowDTO> value = (List<ProgramWorkflowDTO>) item.getValue();
      if (item.isSelected()) {
        list.put((String) item.getKey(), value);
      } else {
        PROGRAM_WORKFLOWS_NOT_PROCESSED.add(value.get(1));
        MDS_PROGRAM_WORKFLOWS_NOT_PROCESSED.add(value.get(0));
      }
    }
    if (!list.isEmpty()) {
      this.harmonizationProgramWorkflowService.saveProgramWorkflowsWithDifferentIDAndEqualUUID(
          list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.programworkflow.harmonize.differentID.andEqualUUID");
      logBuilder.appendLogForProgramWorkflowsWithDiferrentIdsAndEqualUUID(list);
      HarmonizeProgramWorkflowsController.HAS_ATLEAST_ONE_ROW_HARMONIZED = true;
    }
  }
}
