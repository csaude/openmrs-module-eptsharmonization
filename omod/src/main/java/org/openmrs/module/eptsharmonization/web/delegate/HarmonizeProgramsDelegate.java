package org.openmrs.module.eptsharmonization.web.delegate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpSession;
import org.apache.commons.beanutils.BeanComparator;
import org.openmrs.Program;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationProgramService;
import org.openmrs.module.eptsharmonization.api.model.ProgramDTO;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationData;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationItem;
import org.openmrs.module.eptsharmonization.web.bean.ProgramsHarmonizationCSVLog.Builder;
import org.openmrs.module.eptsharmonization.web.controller.HarmonizeProgramsController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HarmonizeProgramsDelegate {

  private HarmonizationProgramService harmonizationProgramService;

  @Autowired
  public void setHarmonizationProgramService(
      HarmonizationProgramService harmonizationProgramService) {
    this.harmonizationProgramService = harmonizationProgramService;
  }

  public static List<String> SUMMARY_EXECUTED_SCENARIOS = new ArrayList<>();
  public static List<Program> EXECUTED_PROGRAMS_MANUALLY_CACHE = new ArrayList<>();
  public static List<Program> PROGRAMS_NOT_PROCESSED = new ArrayList<>();
  public static List<Program> MDS_PROGRAMS_NOT_PROCESSED = new ArrayList<>();

  public HarmonizationData getConvertedData(List<ProgramDTO> programs) {

    List<HarmonizationItem> items = new ArrayList<>();
    for (ProgramDTO programDTO : programs) {
      HarmonizationItem item = new HarmonizationItem(programDTO.getProgram().getUuid(), programDTO);
      item.setEncountersCount(
          this.harmonizationProgramService.getNumberOfAffectedPatientPrograms(programDTO));
      item.setFormsCount(
          this.harmonizationProgramService.getNumberOfAffectedProgramWorkflow(programDTO));
      items.add(item);
    }
    return new HarmonizationData(items);
  }

  public HarmonizationData getConvertedData(Map<String, List<ProgramDTO>> mapPrograms) {
    List<HarmonizationItem> items = new ArrayList<>();
    for (String key : mapPrograms.keySet()) {
      List<ProgramDTO> programs = mapPrograms.get(key);
      if (programs != null) {
        HarmonizationItem item = new HarmonizationItem(key, programs);
        item.setEncountersCount(
            this.harmonizationProgramService.getNumberOfAffectedPatientPrograms(programs.get(1)));
        item.setFormsCount(
            this.harmonizationProgramService.getNumberOfAffectedProgramWorkflow(programs.get(1)));
        items.add(item);
      }
    }
    return new HarmonizationData(items);
  }

  public void setHarmonizationStage(
      HttpSession session,
      HarmonizationData newMDSPrograms,
      List<ProgramDTO> productionItemsToDelete,
      HarmonizationData productionItemsToExport,
      HarmonizationData differentIDsAndEqualUUID,
      HarmonizationData differentNameAndSameUUIDAndID,
      List<Program> notSwappablePrograms,
      List<Program> swappablePrograms) {

    PROGRAMS_NOT_PROCESSED.removeAll(EXECUTED_PROGRAMS_MANUALLY_CACHE);

    this.updateProductionToExportList(productionItemsToExport);
    this.setSwappableDataClones(session, notSwappablePrograms, swappablePrograms);
    this.removeAllChoosenToManualHarmonize(session, swappablePrograms);

    boolean isFirstStepHarmonizationCompleted =
        newMDSPrograms.getItems().isEmpty() && productionItemsToDelete.isEmpty();

    this.setIDsAndUUIDsHarmonizationStatus(
        productionItemsToExport, differentIDsAndEqualUUID, isFirstStepHarmonizationCompleted);
    this.setNAMEsDifferencesHarmonizationStatus(
        productionItemsToExport, differentNameAndSameUUIDAndID, isFirstStepHarmonizationCompleted);

    boolean hasSecondStepHarmonization =
        isFirstStepHarmonizationCompleted
            && HarmonizeProgramsController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED
            && HarmonizeProgramsController.IS_NAMES_DIFFERENCES_HARMONIZED
            && !productionItemsToExport.getItems().isEmpty();

    session.setAttribute("isFirstStepHarmonizationCompleted", isFirstStepHarmonizationCompleted);
    session.setAttribute(
        "isUUIDsAndIDsHarmonized",
        HarmonizeProgramsController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED);
    session.setAttribute(
        "isNamesHarmonized", HarmonizeProgramsController.IS_NAMES_DIFFERENCES_HARMONIZED);
    session.setAttribute("hasSecondStepHarmonization", hasSecondStepHarmonization);
  }

  private void setIDsAndUUIDsHarmonizationStatus(
      HarmonizationData productionItemsToExport,
      HarmonizationData differentIDsAndEqualUUID,
      boolean isFirstStepHarmonizationCompleted) {

    HarmonizeProgramsController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED =
        HarmonizeProgramsController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED
            || differentIDsAndEqualUUID.getItems().isEmpty();

    if (!isFirstStepHarmonizationCompleted) {
      HarmonizeProgramsController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = false;
    }

    if (HarmonizeProgramsController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED
        && productionItemsToExport.getItems().isEmpty()
        && !differentIDsAndEqualUUID.getItems().isEmpty()) {
      HarmonizeProgramsController.IS_IDS_AND_UUID_DIFFERENCES_HARMONIZED = false;
    }
  }

  private void setNAMEsDifferencesHarmonizationStatus(
      HarmonizationData productionItemsToExport,
      HarmonizationData differentNameAndSameUUIDAndID,
      boolean isFirstStepHarmonizationCompleted) {

    HarmonizeProgramsController.IS_NAMES_DIFFERENCES_HARMONIZED =
        HarmonizeProgramsController.IS_NAMES_DIFFERENCES_HARMONIZED
            || differentNameAndSameUUIDAndID.getItems().isEmpty();

    if (!isFirstStepHarmonizationCompleted) {
      HarmonizeProgramsController.IS_NAMES_DIFFERENCES_HARMONIZED = false;
    }

    if (HarmonizeProgramsController.IS_NAMES_DIFFERENCES_HARMONIZED
        && productionItemsToExport.getItems().isEmpty()
        && !differentNameAndSameUUIDAndID.getItems().isEmpty()) {
      HarmonizeProgramsController.IS_NAMES_DIFFERENCES_HARMONIZED = false;
    }
  }

  @SuppressWarnings("unchecked")
  private void updateProductionToExportList(HarmonizationData productionItemsToExport) {

    HarmonizationData newItemsToExport =
        getConvertedData(DTOUtils.fromPrograms(PROGRAMS_NOT_PROCESSED));
    productionItemsToExport.getItems().addAll(newItemsToExport.getItems());

    List<HarmonizationItem> itemsToRemove =
        getConvertedData(DTOUtils.fromPrograms(EXECUTED_PROGRAMS_MANUALLY_CACHE)).getItems();

    List<HarmonizationItem> items = productionItemsToExport.getItems();

    BeanComparator comparator = new BeanComparator("value.program.id");
    Collections.sort(items, comparator);
    productionItemsToExport.getItems().removeAll(itemsToRemove);
  }

  private void setSwappableDataClones(
      HttpSession session, List<Program> notSwappablePrograms, List<Program> swappablePrograms) {

    swappablePrograms.removeAll(EXECUTED_PROGRAMS_MANUALLY_CACHE);

    for (Program program : PROGRAMS_NOT_PROCESSED) {
      if (!swappablePrograms.contains(program)) {
        swappablePrograms.add(program);
      }
    }
    session.setAttribute("swappableProgramsClone", new ArrayList<Program>(swappablePrograms));
    session.setAttribute("notSwappableProgramsClone", new ArrayList<Program>(notSwappablePrograms));
  }

  @SuppressWarnings("unchecked")
  public void removeAllChoosenToManualHarmonize(
      HttpSession session, List<Program> swappablePrograms) {
    Map<Program, Program> manualHarmonizePrograms =
        (Map<Program, Program>) session.getAttribute("manualHarmonizePrograms");
    if (manualHarmonizePrograms != null) {
      for (Entry<Program, Program> entry : manualHarmonizePrograms.entrySet()) {
        swappablePrograms.remove(entry.getKey());
      }
    }
  }

  public void processAddNewFromMetadataServer(
      HarmonizationData onlyMetadataPrograms, Builder logBuilder) {
    List<ProgramDTO> list = new ArrayList<>();
    for (HarmonizationItem item : onlyMetadataPrograms.getItems()) {
      list.add((ProgramDTO) item.getValue());
    }
    if (!list.isEmpty()) {
      this.harmonizationProgramService.saveNewProgramsFromMDS(list);
      SUMMARY_EXECUTED_SCENARIOS.add("eptsharmonization.summary.program.harmonize.onlyOnMDServer");
      logBuilder.appendLogForNewHarmonizedFromMDSPrograms(list);
    }
  }

  public void processDeleteFromProductionServer(List<ProgramDTO> list, Builder logBuilder) {
    if (!list.isEmpty()) {
      this.harmonizationProgramService.deleteNewProgramsFromPDS(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.program.harmonize.onlyOnPServer.unused");
      logBuilder.appendLogForDeleteFromProductionServer(list);
    }
  }

  @SuppressWarnings("unchecked")
  public void processUpdateProgramsNames(HarmonizationData data, Builder logBuilder) {
    Map<String, List<ProgramDTO>> list = new HashMap<>();
    for (HarmonizationItem item : data.getItems()) {
      List<ProgramDTO> value = (List<ProgramDTO>) item.getValue();
      if (item.isSelected()) {
        list.put((String) item.getKey(), value);
      } else {
        MDS_PROGRAMS_NOT_PROCESSED.add(value.get(0).getProgram());
        PROGRAMS_NOT_PROCESSED.add(value.get(1).getProgram());
      }
    }
    if (!list.isEmpty()) {
      this.harmonizationProgramService.saveProgramsWithDifferentNames(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.program.harmonize.differentNamesAndSameUUIDAndID");
      logBuilder.appendLogForUpdatedEncounterNames(list);
      HarmonizeProgramsController.HAS_ATLEAST_ONE_ROW_HARMONIZED = true;
    }
  }

  @SuppressWarnings("unchecked")
  public void processProgramsWithDiferrentIdsAndEqualUUID(
      HarmonizationData data, Builder logBuilder) {
    Map<String, List<ProgramDTO>> list = new HashMap<>();

    for (HarmonizationItem item : data.getItems()) {
      List<ProgramDTO> value = (List<ProgramDTO>) item.getValue();
      if (item.isSelected()) {
        list.put((String) item.getKey(), value);
      } else {
        MDS_PROGRAMS_NOT_PROCESSED.add(value.get(0).getProgram());
        PROGRAMS_NOT_PROCESSED.add(value.get(1).getProgram());
      }
    }
    if (!list.isEmpty()) {
      this.harmonizationProgramService.saveProgramsWithDifferentIDAndEqualUUID(list);
      SUMMARY_EXECUTED_SCENARIOS.add(
          "eptsharmonization.summary.program.harmonize.differentID.andEqualUUID");
      logBuilder.appendLogForProgramsWithDiferrentIdsAndEqualUUID(list);
      HarmonizeProgramsController.HAS_ATLEAST_ONE_ROW_HARMONIZED = true;
    }
  }
}
