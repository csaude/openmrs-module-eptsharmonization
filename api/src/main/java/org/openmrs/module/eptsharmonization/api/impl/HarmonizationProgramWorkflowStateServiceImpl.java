/**
 * The contents of this file are subject to the OpenMRS Public License Version 1.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://license.openmrs.org
 *
 * <p>Software distributed under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
 * ANY KIND, either express or implied. See the License for the specific language governing rights
 * and limitations under the License.
 *
 * <p>Copyright (C) OpenMRS, LLC. All Rights Reserved.
 */
package org.openmrs.module.eptsharmonization.api.impl;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.openmrs.ConceptStateConversion;
import org.openmrs.PatientState;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationProgramWorkflowStateService;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationProgramWorkflowStateServiceDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.openmrs.module.eptsharmonization.api.exception.UUIDDuplicationException;
import org.openmrs.module.eptsharmonization.api.model.ProgramWorkflowStateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** It is a default implementation of {@link HarmonizationProgramWorkflowStateService}. */
@Transactional
@Service("eptsharmonization.harmonizationProgramWorkflowStateService")
public class HarmonizationProgramWorkflowStateServiceImpl extends BaseOpenmrsService
    implements HarmonizationProgramWorkflowStateService {

  private HarmonizationServiceDAO harmonizationDAO;
  private ProgramWorkflowService programWorkflowService;
  private HarmonizationProgramWorkflowStateServiceDAO harmonizationProgramWorkflowStateServiceDAO;

  @Autowired
  public void setHarmonizationDAO(HarmonizationServiceDAO harmonizationDAO) {
    this.harmonizationDAO = harmonizationDAO;
  }

  @Autowired
  public void setHarmonizationProgramWorkflowStateServiceDAO(
      HarmonizationProgramWorkflowStateServiceDAO harmonizationProgramWorkflowStateServiceDAO) {
    this.harmonizationProgramWorkflowStateServiceDAO = harmonizationProgramWorkflowStateServiceDAO;
  }

  @Autowired
  public void setProgramWorkflowService(ProgramWorkflowService programWorkflowService) {
    this.programWorkflowService = programWorkflowService;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View ProgramWorkflowState"})
  public List<ProgramWorkflowStateDTO> findAllMDSStatesNotContainedInPDS() throws APIException {
    this.harmonizationDAO.evictCache();
    List<ProgramWorkflowState> mdsProgramWorkflowStates =
        harmonizationProgramWorkflowStateServiceDAO.findAllMDSProgramWorkflowStates();
    List<ProgramWorkflowState> pdsProgramWorkflowStates =
        harmonizationProgramWorkflowStateServiceDAO.findAllPDSProgramWorkflowStates();
    mdsProgramWorkflowStates.removeAll(pdsProgramWorkflowStates);
    final List<ProgramWorkflowStateDTO> programWorkflowStates =
        DTOUtils.fromProgramWorkflowStates(mdsProgramWorkflowStates);
    setProgramWorkflowAndConcept(programWorkflowStates, true);
    return programWorkflowStates;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View ProgramWorkflowState"})
  public List<ProgramWorkflowStateDTO> findAllPDSStatesNotContainedInMDS() throws APIException {
    this.harmonizationDAO.evictCache();
    List<ProgramWorkflowState> pdsProgramWorkflowStates =
        harmonizationProgramWorkflowStateServiceDAO.findAllPDSProgramWorkflowStates();
    List<ProgramWorkflowState> mdsProgramWorkflowStates =
        harmonizationProgramWorkflowStateServiceDAO.findAllMDSProgramWorkflowStates();
    pdsProgramWorkflowStates.removeAll(mdsProgramWorkflowStates);
    final List<ProgramWorkflowStateDTO> programWorkflowStates =
        DTOUtils.fromProgramWorkflowStates(pdsProgramWorkflowStates);
    setProgramWorkflowAndConcept(programWorkflowStates, false);
    return programWorkflowStates;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View ProgramWorkflowState"})
  public Map<String, List<ProgramWorkflowStateDTO>>
      findAllStatesWithDifferentWorkflowOrConceptAndSameUUIDAndID() throws APIException {
    this.harmonizationDAO.evictCache();

    List<ProgramWorkflowStateDTO> allMDS =
        DTOUtils.fromProgramWorkflowStates(
            harmonizationProgramWorkflowStateServiceDAO.findAllMDSProgramWorkflowStates());
    List<ProgramWorkflowStateDTO> allPDS =
        DTOUtils.fromProgramWorkflowStates(
            harmonizationProgramWorkflowStateServiceDAO.findAllPDSProgramWorkflowStates());

    Map<String, List<ProgramWorkflowStateDTO>> result = new TreeMap<>();
    for (ProgramWorkflowStateDTO mdsItem : allMDS) {
      final ProgramWorkflow mdsProgramWorkflow =
          harmonizationProgramWorkflowStateServiceDAO.getProgramWorkflow(
              mdsItem.getProgramWorkflowState(), true);
      final Integer mdsConceptId =
          harmonizationProgramWorkflowStateServiceDAO.getConceptId(
              mdsItem.getProgramWorkflowState(), true);
      for (ProgramWorkflowStateDTO pdsItem : allPDS) {
        final ProgramWorkflow pdsProgramWorkflow =
            harmonizationProgramWorkflowStateServiceDAO.getProgramWorkflow(
                pdsItem.getProgramWorkflowState(), false);
        final Integer pdsConceptId =
            harmonizationProgramWorkflowStateServiceDAO.getConceptId(
                pdsItem.getProgramWorkflowState(), false);
        if (mdsItem.getUuid().equals(pdsItem.getUuid())
            && mdsItem.getId().equals(pdsItem.getId())
            && !(mdsProgramWorkflow.getId().equals(pdsProgramWorkflow.getId())
                && mdsConceptId.equals(pdsConceptId))) {
          setProgramWorkflowAndConcept(Arrays.asList(mdsItem), true);
          setProgramWorkflowAndConcept(Arrays.asList(pdsItem), false);
          result.put(mdsItem.getUuid(), Arrays.asList(mdsItem, pdsItem));
        }
      }
    }
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View ProgramWorkflowState"})
  public Map<String, List<ProgramWorkflowStateDTO>> findAllStatesWithDifferentIDAndSameUUID()
      throws APIException {
    this.harmonizationDAO.evictCache();
    List<ProgramWorkflowStateDTO> allPDS =
        DTOUtils.fromProgramWorkflowStates(
            harmonizationProgramWorkflowStateServiceDAO.findAllPDSProgramWorkflowStates());
    List<ProgramWorkflowStateDTO> allMDS =
        DTOUtils.fromProgramWorkflowStates(
            harmonizationProgramWorkflowStateServiceDAO.findAllMDSProgramWorkflowStates());
    setProgramWorkflowAndConcept(allPDS, false);
    setProgramWorkflowAndConcept(allMDS, true);
    Map<String, List<ProgramWorkflowStateDTO>> result = new TreeMap<>();
    for (ProgramWorkflowStateDTO mdsItem : allMDS) {
      for (ProgramWorkflowStateDTO pdsItem : allPDS) {
        if (mdsItem.getUuid().equals(pdsItem.getUuid())
            && !mdsItem.getId().equals(pdsItem.getId())) {
          result.put(mdsItem.getUuid(), Arrays.asList(mdsItem, pdsItem));
        }
      }
    }
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Program"})
  public int getNumberOfAffectedConceptStateConversions(
      ProgramWorkflowStateDTO programWorkflowStateDTO) {
    this.harmonizationDAO.evictCache();
    return harmonizationProgramWorkflowStateServiceDAO
        .findConceptStateConversionsByProgramWorkflowStateId(programWorkflowStateDTO.getId())
        .size();
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View ProgramWorkflowState"})
  public int getNumberOfAffectedPatientStates(ProgramWorkflowStateDTO programWorkflowStateDTO) {
    this.harmonizationDAO.evictCache();
    return harmonizationProgramWorkflowStateServiceDAO
        .findPatientStatesByProgramWorkflowStateId(programWorkflowStateDTO.getId())
        .size();
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View ProgramWorkflowState"})
  public List<ProgramWorkflowState> findAllNotSwappableProgramWorkflowStates() throws APIException {
    this.harmonizationDAO.evictCache();
    List<ProgramWorkflowState> result =
        this.harmonizationProgramWorkflowStateServiceDAO.findAllNotSwappable();
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View ProgramWorkflowState"})
  public List<ProgramWorkflowState> findAllSwappableProgramWorkflowStates() throws APIException {
    this.harmonizationDAO.evictCache();
    List<ProgramWorkflowState> findAllSwappable =
        this.harmonizationProgramWorkflowStateServiceDAO.findAllSwappable();
    return findAllSwappable;
  }

  @SuppressWarnings("deprecation")
  @Override
  @Authorized({"Manage ProgramWorkflowState"})
  public void updateStatesWithDifferentWorkflowsOrConcept(
      Map<String, List<ProgramWorkflowStateDTO>> programWorkflowStates) throws APIException {
    this.harmonizationDAO.evictCache();
    for (String key : programWorkflowStates.keySet()) {
      List<ProgramWorkflowStateDTO> list = programWorkflowStates.get(key);
      ProgramWorkflowState mdsProgramWorkflowState = list.get(0).getProgramWorkflowState();
      ProgramWorkflowState pdsProgramWorkflowState = list.get(1).getProgramWorkflowState();

      final ProgramWorkflow mdsProgramWorkflow =
          harmonizationProgramWorkflowStateServiceDAO.getProgramWorkflow(
              mdsProgramWorkflowState, true);
      final Integer mdsConceptId =
          harmonizationProgramWorkflowStateServiceDAO.getConceptId(mdsProgramWorkflowState, true);

      ProgramWorkflowState programWorkflowState =
          this.programWorkflowService.getState(pdsProgramWorkflowState.getId());
      programWorkflowState.setProgramWorkflow(mdsProgramWorkflow);
      programWorkflowState.setConcept(Context.getConceptService().getConcept(mdsConceptId));
      this.harmonizationProgramWorkflowStateServiceDAO.updateProgramWorkflowState(
          programWorkflowState);
    }
  }

  @Override
  @Authorized({"Manage ProgramWorkflowState"})
  public void saveNewProgramWorkflowStatesFromMDS(
      List<ProgramWorkflowStateDTO> programWorkflowStates) throws APIException {
    this.harmonizationDAO.evictCache();
    try {
      this.harmonizationDAO.setDisabledCheckConstraints();
      for (ProgramWorkflowStateDTO programWorkflowStateDTO : programWorkflowStates) {

        ProgramWorkflowState programWorkflowState =
            programWorkflowStateDTO.getProgramWorkflowState();
        ProgramWorkflowState found =
            this.harmonizationProgramWorkflowStateServiceDAO.getProgramWorkflowStateById(
                programWorkflowState.getId());

        if (found != null) {

          if (!this.harmonizationProgramWorkflowStateServiceDAO.isSwappable(found)) {

            throw new APIException(
                String.format(
                    "Cannot Insert Program Workflow with ID %s, UUID %s, PROGRAM %s and CONCEPT %s. This ID is being in use by another Program Workflow from Metatada server with UUID %s, PROGRAM %s and CONCEPT %s ",
                    programWorkflowState.getId(),
                    programWorkflowState.getUuid(),
                    programWorkflowState.getProgramWorkflow(),
                    programWorkflowState.getConcept(),
                    found.getUuid(),
                    found.getProgramWorkflow(),
                    found.getConcept()));
          }
          List<ConceptStateConversion> relatedConceptStateConversions =
              this.harmonizationProgramWorkflowStateServiceDAO
                  .findConceptStateConversionsByProgramWorkflowStateId(found.getId());
          List<PatientState> relatedPatientStates =
              this.harmonizationProgramWorkflowStateServiceDAO
                  .findPatientStatesByProgramWorkflowStateId(found.getId());
          this.updateToNextAvailableID(found, relatedConceptStateConversions, relatedPatientStates);
        }
        this.harmonizationProgramWorkflowStateServiceDAO.saveNotSwappableProgramWorkflowState(
            programWorkflowState);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        this.harmonizationDAO.setEnableCheckConstraints();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  @Authorized({"Manage ProgramWorkflowState"})
  public void saveStatesWithDifferentIDAndEqualUUID(
      Map<String, List<ProgramWorkflowStateDTO>> mapProgramWorkflowStates) throws APIException {
    this.harmonizationDAO.evictCache();
    try {

      this.harmonizationDAO.setDisabledCheckConstraints();
      for (String uuid : mapProgramWorkflowStates.keySet()) {

        List<ProgramWorkflowStateDTO> list = mapProgramWorkflowStates.get(uuid);
        ProgramWorkflowState mdsProgramWorkflowState = list.get(0).getProgramWorkflowState();
        ProgramWorkflowState pdSProgramWorkflowState = list.get(1).getProgramWorkflowState();
        Integer mdServerProgramWorkflowStateId =
            mdsProgramWorkflowState.getProgramWorkflowStateId();

        ProgramWorkflowState foundMDS =
            this.harmonizationProgramWorkflowStateServiceDAO.getProgramWorkflowStateById(
                mdsProgramWorkflowState.getId());

        if (foundMDS != null) {
          if (!this.harmonizationProgramWorkflowStateServiceDAO.isSwappable(foundMDS)) {
            throw new APIException(
                String.format(
                    "Cannot update the Production Server Program Workflow [ID = {%s}, UUID = {%s}, PROGRAM = {%s}, CONCEPT = {%s}] with the ID {%s} this new ID is already referencing an Existing Program Workflow In Metadata Server",
                    pdSProgramWorkflowState.getId(),
                    pdSProgramWorkflowState.getUuid(),
                    list.get(1).getProgramWorkflowState(),
                    list.get(1).getProgramWorkflowState().getConcept(),
                    mdServerProgramWorkflowStateId));
          }
          List<ConceptStateConversion> relatedConceptStateConversions =
              this.harmonizationProgramWorkflowStateServiceDAO
                  .findConceptStateConversionsByProgramWorkflowStateId(foundMDS.getId());
          List<PatientState> relatedPatientStates =
              this.harmonizationProgramWorkflowStateServiceDAO
                  .findPatientStatesByProgramWorkflowStateId(foundMDS.getId());
          this.updateToNextAvailableID(
              foundMDS, relatedConceptStateConversions, relatedPatientStates);
        }

        ProgramWorkflowState foundPDS =
            this.harmonizationProgramWorkflowStateServiceDAO.getProgramWorkflowStateById(
                pdSProgramWorkflowState.getId());
        if (!this.harmonizationProgramWorkflowStateServiceDAO.isSwappable(foundPDS)) {
          // TODO Review this
          throw new APIException(
              String.format(
                  "Cannot update the Production Server ProgramWorkflowState with ID {%s}, UUID {%s}, PROGRAM {%s} and CONCEPT {%s}. This ProgramWorkflowState is a Reference from an ProgramWorkflowState of Metadata Server",
                  foundPDS.getId(),
                  foundPDS.getUuid(),
                  list.get(1).getProgramWorkflowState(),
                  list.get(1).getProgramWorkflowState().getConcept()));
        }
        List<ConceptStateConversion> relatedConceptStateConversions =
            this.harmonizationProgramWorkflowStateServiceDAO
                .findConceptStateConversionsByProgramWorkflowStateId(foundPDS.getId());
        List<PatientState> relatedPatientStates =
            this.harmonizationProgramWorkflowStateServiceDAO
                .findPatientStatesByProgramWorkflowStateId(foundPDS.getId());
        this.updateToGivenId(
            foundPDS,
            mdServerProgramWorkflowStateId,
            false,
            relatedConceptStateConversions,
            relatedPatientStates);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        this.harmonizationDAO.setEnableCheckConstraints();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  @Authorized({"Manage ProgramWorkflowState"})
  public void deleteNewProgramWorkflowStatesFromPDS(
      List<ProgramWorkflowStateDTO> programWorkflowStates) throws APIException {
    this.harmonizationDAO.evictCache();
    for (ProgramWorkflowState program :
        DTOUtils.fromProgramWorkflowStateDTOs(programWorkflowStates)) {
      this.harmonizationProgramWorkflowStateServiceDAO.deleteProgramWorkflowState(program);
    }
  }

  @Override
  @Authorized({"Manage ProgramWorkflowState"})
  public void saveManualMapping(
      Map<ProgramWorkflowStateDTO, ProgramWorkflowStateDTO> mapProgramWorkflowStates)
      throws UUIDDuplicationException, SQLException {
    this.harmonizationDAO.evictCache();
    try {

      this.harmonizationDAO.setDisabledCheckConstraints();
      for (Entry<ProgramWorkflowStateDTO, ProgramWorkflowStateDTO> entry :
          mapProgramWorkflowStates.entrySet()) {

        ProgramWorkflowStateDTO pdsProgramWorkflowStateDTO = entry.getKey();
        ProgramWorkflowStateDTO mdsProgramWorkflowStateDTO = entry.getValue();
        ProgramWorkflowState pdsProgramWorkflowState =
            pdsProgramWorkflowStateDTO.getProgramWorkflowState();
        ProgramWorkflowState mdsProgramWorkflowState =
            mdsProgramWorkflowStateDTO.getProgramWorkflowState();

        ProgramWorkflowState foundMDSProgramWorkflowStateByUuid =
            this.harmonizationProgramWorkflowStateServiceDAO.getProgramWorkflowStateByUuid(
                mdsProgramWorkflowState.getUuid());

        if ((foundMDSProgramWorkflowStateByUuid != null
                && !foundMDSProgramWorkflowStateByUuid
                    .getId()
                    .equals(mdsProgramWorkflowState.getId()))
            && (!foundMDSProgramWorkflowStateByUuid.getId().equals(pdsProgramWorkflowState.getId())
                && !foundMDSProgramWorkflowStateByUuid
                    .getUuid()
                    .equals(pdsProgramWorkflowState.getUuid()))) {

          throw new UUIDDuplicationException(
              String.format(
                  " Cannot Update the Program Workflow State '%s' to '%s' and '%s' to '%s'. There is one entry with PROGRAM='%s', CONCEPT='%s', ID='%s' an UUID='%s' ",
                  pdsProgramWorkflowStateDTO.getFlowProgram(),
                  mdsProgramWorkflowStateDTO.getFlowProgram(),
                  pdsProgramWorkflowStateDTO.getConcept(),
                  mdsProgramWorkflowStateDTO.getConcept(),
                  harmonizationProgramWorkflowStateServiceDAO
                      .getProgramWorkflow(foundMDSProgramWorkflowStateByUuid, false)
                      .getProgram()
                      .getName(),
                  harmonizationProgramWorkflowStateServiceDAO.getConceptName(
                      foundMDSProgramWorkflowStateByUuid, false),
                  foundMDSProgramWorkflowStateByUuid.getId(),
                  foundMDSProgramWorkflowStateByUuid.getUuid()));
        }

        ProgramWorkflowState foundPDS =
            this.harmonizationProgramWorkflowStateServiceDAO.getProgramWorkflowStateById(
                pdsProgramWorkflowState.getId());

        final ProgramWorkflow mdsProgramWorkflow =
            harmonizationProgramWorkflowStateServiceDAO.getProgramWorkflow(
                mdsProgramWorkflowState, true);
        final Integer mdsConceptId =
            harmonizationProgramWorkflowStateServiceDAO.getConceptId(mdsProgramWorkflowState, true);
        final ProgramWorkflow pdsProgram =
            harmonizationProgramWorkflowStateServiceDAO.getProgramWorkflow(
                pdsProgramWorkflowState, false);
        final Integer pdsConceptId =
            harmonizationProgramWorkflowStateServiceDAO.getConceptId(
                pdsProgramWorkflowState, false);

        if (mdsProgramWorkflowState.getUuid().equals(pdsProgramWorkflowState.getUuid())
            && mdsProgramWorkflowState.getId().equals(pdsProgramWorkflowState.getId())) {
          if (mdsProgramWorkflowState.getId().equals(pdsProgramWorkflowState.getId())
              && mdsProgramWorkflow.getId().equals(pdsProgram.getId())
              && mdsConceptId.equals(pdsConceptId)) {
            return;
          }
        } else {
          List<ConceptStateConversion> relatedConceptStateConversions =
              this.harmonizationProgramWorkflowStateServiceDAO
                  .findConceptStateConversionsByProgramWorkflowStateId(foundPDS.getId());
          List<PatientState> relatedPatientStates =
              this.harmonizationProgramWorkflowStateServiceDAO
                  .findPatientStatesByProgramWorkflowStateId(foundPDS.getId());

          for (ConceptStateConversion conceptStateConversion : relatedConceptStateConversions) {
            this.harmonizationProgramWorkflowStateServiceDAO.updateConceptStateConversion(
                conceptStateConversion, mdsProgramWorkflowState.getProgramWorkflowStateId());
          }
          for (PatientState programWorkflowState : relatedPatientStates) {
            this.harmonizationProgramWorkflowStateServiceDAO.updatePatientState(
                programWorkflowState, mdsProgramWorkflowState.getProgramWorkflowStateId());
          }
          this.harmonizationProgramWorkflowStateServiceDAO.deleteProgramWorkflowState(foundPDS);

          ProgramWorkflowState foundMDSProgramWorkflowStateByID =
              this.harmonizationProgramWorkflowStateServiceDAO.getProgramWorkflowStateById(
                  mdsProgramWorkflow.getId());
          if (foundMDSProgramWorkflowStateByID == null) {
            this.harmonizationProgramWorkflowStateServiceDAO.saveNotSwappableProgramWorkflowState(
                mdsProgramWorkflowState);
          }
        }
      }
    } finally {
      try {
        this.harmonizationDAO.setEnableCheckConstraints();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  private void updateToGivenId(
      ProgramWorkflowState programWorkflowState,
      Integer givenId,
      boolean swappable,
      List<ConceptStateConversion> relatedConceptStateConversions,
      List<PatientState> relatedPatientStates) {
    this.harmonizationProgramWorkflowStateServiceDAO.updateProgramWorkflowState(
        givenId, programWorkflowState, swappable);

    for (ConceptStateConversion conceptStateConversion : relatedConceptStateConversions) {
      this.harmonizationProgramWorkflowStateServiceDAO.updateConceptStateConversion(
          conceptStateConversion, givenId);
    }
    for (PatientState patientState : relatedPatientStates) {
      this.harmonizationProgramWorkflowStateServiceDAO.updatePatientState(patientState, givenId);
    }
  }

  private void updateToNextAvailableID(
      ProgramWorkflowState programWorkflowState,
      List<ConceptStateConversion> relatedConceptStateConversions,
      List<PatientState> relatedPatientStates) {
    ProgramWorkflowState updated =
        this.harmonizationProgramWorkflowStateServiceDAO.updateToNextAvailableId(
            programWorkflowState);

    for (ConceptStateConversion conceptStateConversion : relatedConceptStateConversions) {
      this.harmonizationProgramWorkflowStateServiceDAO.updateConceptStateConversion(
          conceptStateConversion, updated.getProgramWorkflowStateId());
    }
    for (PatientState patientState : relatedPatientStates) {
      this.harmonizationProgramWorkflowStateServiceDAO.updatePatientState(
          patientState, updated.getProgramWorkflowStateId());
    }
  }

  @Override
  public ProgramWorkflowState findMDSProgramWorkflowStateByUuid(String uuid) throws APIException {
    return harmonizationProgramWorkflowStateServiceDAO.findMDSPProgramWorkflowStateByUuid(uuid);
  }

  @Override
  public ProgramWorkflowState findPDSProgramWorkflowStateByUuid(String uuid) throws APIException {
    this.harmonizationDAO.evictCache();
    return programWorkflowService.getStateByUuid(uuid);
  }

  @Override
  public List<ProgramWorkflowState> findAllMDSProgramWorkflowStates() throws APIException {
    this.harmonizationDAO.evictCache();
    return this.harmonizationProgramWorkflowStateServiceDAO.findAllMDSProgramWorkflowStates();
  }

  @Override
  public void setProgramWorkflowAndConcept(
      List<ProgramWorkflowStateDTO> programWorkflowStateDTOs, boolean isFromMetadata)
      throws APIException {
    for (ProgramWorkflowStateDTO programWorkflowStateDTO : programWorkflowStateDTOs) {
      ProgramWorkflow programWorkflow =
          harmonizationProgramWorkflowStateServiceDAO.getProgramWorkflow(
              programWorkflowStateDTO.getProgramWorkflowState(), isFromMetadata);
      programWorkflowStateDTO.setFlowProgram(
          programWorkflowService.getProgram(programWorkflow.getProgram().getId()).getName());
      programWorkflowStateDTO.setFlowConcept(
          harmonizationProgramWorkflowStateServiceDAO.getConceptName(
              programWorkflow.getConcept().getId()));
      programWorkflowStateDTO.setConcept(
          harmonizationProgramWorkflowStateServiceDAO.getConceptName(
              programWorkflowStateDTO.getProgramWorkflowState(), isFromMetadata));
    }
  }

  @Override
  public ProgramWorkflow getProgramWorkflow(
      ProgramWorkflowState programWorkflowState, boolean isFromMetadata) throws APIException {
    this.harmonizationDAO.evictCache();
    return this.harmonizationProgramWorkflowStateServiceDAO.getProgramWorkflow(
        programWorkflowState, isFromMetadata);
  }

  @Override
  public Integer getProgramWorkflowStateConceptId(
      ProgramWorkflowState programWorkflow, boolean isFromMetadata) throws APIException {
    this.harmonizationDAO.evictCache();
    return this.harmonizationProgramWorkflowStateServiceDAO.getConceptId(
        programWorkflow, isFromMetadata);
  }
}
