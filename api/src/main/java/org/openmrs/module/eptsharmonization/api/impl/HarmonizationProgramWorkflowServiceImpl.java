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
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationProgramWorkflowService;
import org.openmrs.module.eptsharmonization.api.HarmonizationService;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationProgramWorkflowServiceDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.openmrs.module.eptsharmonization.api.exception.UUIDDuplicationException;
import org.openmrs.module.eptsharmonization.api.model.ProgramWorkflowDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** It is a default implementation of {@link HarmonizationProgramWorkflowService}. */
@Transactional
@Service("eptsharmonization.harmonizationProgramWorkflowService")
public class HarmonizationProgramWorkflowServiceImpl extends BaseOpenmrsService
    implements HarmonizationProgramWorkflowService, HarmonizationService {

  private HarmonizationServiceDAO harmonizationDAO;
  private ProgramWorkflowService programWorkflowService;
  private HarmonizationProgramWorkflowServiceDAO harmonizationProgramWorkflowServiceDAO;

  @Autowired
  public void setHarmonizationDAO(HarmonizationServiceDAO harmonizationDAO) {
    this.harmonizationDAO = harmonizationDAO;
  }

  @Autowired
  public void setHarmonizationProgramWorkflowServiceDAO(
      HarmonizationProgramWorkflowServiceDAO harmonizationProgramWorkflowServiceDAO) {
    this.harmonizationProgramWorkflowServiceDAO = harmonizationProgramWorkflowServiceDAO;
  }

  @Autowired
  public void setProgramWorkflowService(ProgramWorkflowService programWorkflowService) {
    this.programWorkflowService = programWorkflowService;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View ProgramWorkflow"})
  public List<ProgramWorkflowDTO> findAllMetadataProgramWorkflowsNotContainedInProductionServer()
      throws APIException {
    this.harmonizationDAO.evictCache();
    List<ProgramWorkflow> mdsProgramWorkflows =
        harmonizationProgramWorkflowServiceDAO.findAllMetadataServerProgramWorkflows();
    List<ProgramWorkflow> pdsProgramWorkflows =
        harmonizationProgramWorkflowServiceDAO.findAllProductionServerProgramWorkflows();
    mdsProgramWorkflows.removeAll(pdsProgramWorkflows);
    final List<ProgramWorkflowDTO> programWorkflows =
        DTOUtils.fromProgramWorkflows(mdsProgramWorkflows);
    setProgramAndConceptNames(programWorkflows, true);
    return programWorkflows;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View ProgramWorkflow"})
  public List<ProgramWorkflowDTO> findAllProductionProgramWorkflowsNotContainedInMetadataServer()
      throws APIException {
    this.harmonizationDAO.evictCache();
    List<ProgramWorkflow> pdsProgramWorkflows =
        harmonizationProgramWorkflowServiceDAO.findAllProductionServerProgramWorkflows();
    List<ProgramWorkflow> mdsProgramWorkflows =
        harmonizationProgramWorkflowServiceDAO.findAllMetadataServerProgramWorkflows();
    pdsProgramWorkflows.removeAll(mdsProgramWorkflows);
    final List<ProgramWorkflowDTO> programWorkflows =
        DTOUtils.fromProgramWorkflows(pdsProgramWorkflows);
    setProgramAndConceptNames(programWorkflows, false);
    return programWorkflows;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View ProgramWorkflow"})
  public Map<String, List<ProgramWorkflowDTO>>
      findAllProgramWorkflowsWithDifferentProgramOrConceptAndSameUUIDAndID() throws APIException {
    this.harmonizationDAO.evictCache();

    List<ProgramWorkflowDTO> allMDS =
        DTOUtils.fromProgramWorkflows(
            harmonizationProgramWorkflowServiceDAO.findAllMetadataServerProgramWorkflows());
    List<ProgramWorkflowDTO> allPDS =
        DTOUtils.fromProgramWorkflows(
            harmonizationProgramWorkflowServiceDAO.findAllProductionServerProgramWorkflows());

    Map<String, List<ProgramWorkflowDTO>> result = new TreeMap<>();
    for (ProgramWorkflowDTO mdsItem : allMDS) {
      final Program mdsProgram =
          harmonizationProgramWorkflowServiceDAO.getProgramWorkflowProgram(
              mdsItem.getProgramWorkflow(), true);
      final Integer mdsConceptId =
          harmonizationProgramWorkflowServiceDAO.getProgramWorkflowConceptId(
              mdsItem.getProgramWorkflow(), true);
      for (ProgramWorkflowDTO pdsItem : allPDS) {
        final Program pdsProgram =
            harmonizationProgramWorkflowServiceDAO.getProgramWorkflowProgram(
                pdsItem.getProgramWorkflow(), false);
        final Integer pdsConceptId =
            harmonizationProgramWorkflowServiceDAO.getProgramWorkflowConceptId(
                pdsItem.getProgramWorkflow(), false);
        if (mdsItem.getUuid().equals(pdsItem.getUuid())
            && mdsItem.getId().equals(pdsItem.getId())
            && !(mdsProgram.getId().equals(pdsProgram.getId())
                && mdsConceptId.equals(pdsConceptId))) {
          setProgramAndConceptNames(Arrays.asList(mdsItem), true);
          setProgramAndConceptNames(Arrays.asList(pdsItem), false);
          result.put(mdsItem.getUuid(), Arrays.asList(mdsItem, pdsItem));
        }
      }
    }
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View ProgramWorkflow"})
  public Map<String, List<ProgramWorkflowDTO>> findAllProgramWorkflowsWithDifferentIDAndSameUUID()
      throws APIException {
    this.harmonizationDAO.evictCache();
    List<ProgramWorkflowDTO> allPDS =
        DTOUtils.fromProgramWorkflows(
            harmonizationProgramWorkflowServiceDAO.findAllProductionServerProgramWorkflows());
    List<ProgramWorkflowDTO> allMDS =
        DTOUtils.fromProgramWorkflows(
            harmonizationProgramWorkflowServiceDAO.findAllMetadataServerProgramWorkflows());
    setProgramAndConceptNames(allPDS, false);
    setProgramAndConceptNames(allMDS, true);
    Map<String, List<ProgramWorkflowDTO>> result = new TreeMap<>();
    for (ProgramWorkflowDTO mdsItem : allMDS) {
      for (ProgramWorkflowDTO pdsItem : allPDS) {
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
  public int getNumberOfAffectedConceptStateConversions(ProgramWorkflowDTO programWorkflowDTO) {
    this.harmonizationDAO.evictCache();
    return harmonizationProgramWorkflowServiceDAO
        .findConceptStateConversionsByProgramWorkflowId(programWorkflowDTO.getId())
        .size();
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View ProgramWorkflow"})
  public int getNumberOfAffectedProgramWorkflowStates(ProgramWorkflowDTO programWorkflowDTO) {
    this.harmonizationDAO.evictCache();
    return harmonizationProgramWorkflowServiceDAO
        .findProgramWorkflowStatesByProgramWorkflowId(programWorkflowDTO.getId())
        .size();
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View ProgramWorkflow"})
  public List<ProgramWorkflow> findAllNotSwappableProgramWorkflows() throws APIException {
    this.harmonizationDAO.evictCache();
    List<ProgramWorkflow> result =
        this.harmonizationProgramWorkflowServiceDAO.findAllNotSwappable();
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View ProgramWorkflow"})
  public List<ProgramWorkflow> findAllSwappableProgramWorkflows() throws APIException {
    this.harmonizationDAO.evictCache();
    List<ProgramWorkflow> findAllSwappable =
        this.harmonizationProgramWorkflowServiceDAO.findAllSwappable();
    return findAllSwappable;
  }

  @SuppressWarnings("deprecation")
  @Override
  @Authorized({"Manage ProgramWorkflow"})
  public void updateProgramWorkflowsWithDifferentProgramsOrConcept(
      Map<String, List<ProgramWorkflowDTO>> programWorkflows) throws APIException {
    this.harmonizationDAO.evictCache();
    for (String key : programWorkflows.keySet()) {
      List<ProgramWorkflowDTO> list = programWorkflows.get(key);
      ProgramWorkflow mdsProgramWorkflow = list.get(0).getProgramWorkflow();
      ProgramWorkflow pdsProgramWorkflow = list.get(1).getProgramWorkflow();

      final Program mdsProgram =
          harmonizationProgramWorkflowServiceDAO.getProgramWorkflowProgram(
              mdsProgramWorkflow, true);
      final Integer mdsConceptId =
          harmonizationProgramWorkflowServiceDAO.getProgramWorkflowConceptId(
              mdsProgramWorkflow, true);

      ProgramWorkflow programWorkflow =
          this.programWorkflowService.getWorkflow(pdsProgramWorkflow.getId());
      programWorkflow.setProgram(mdsProgram);
      programWorkflow.setConcept(Context.getConceptService().getConcept(mdsConceptId));
      this.harmonizationProgramWorkflowServiceDAO.updateProgramWorkflow(programWorkflow);
    }
  }

  @Override
  @Authorized({"Manage ProgramWorkflow"})
  public void saveNewProgramWorkflowsFromMDS(List<ProgramWorkflowDTO> programWorkflows)
      throws APIException {
    this.harmonizationDAO.evictCache();
    try {
      this.harmonizationDAO.setDisabledCheckConstraints();
      for (ProgramWorkflowDTO programWorkflowDTO : programWorkflows) {

        ProgramWorkflow programWorkflow = programWorkflowDTO.getProgramWorkflow();
        ProgramWorkflow found =
            this.harmonizationProgramWorkflowServiceDAO.getProgramWorkflowById(
                programWorkflow.getId());

        if (found != null) {

          if (!this.harmonizationProgramWorkflowServiceDAO.isSwappable(found)) {

            throw new APIException(
                String.format(
                    "Cannot Insert Program Workflow with ID %s, UUID %s, PROGRAM %s and CONCEPT %s. This ID is being in use by another Program Workflow from Metatada server with UUID %s, PROGRAM %s and CONCEPT %s ",
                    programWorkflow.getId(),
                    programWorkflow.getUuid(),
                    programWorkflow.getProgram(),
                    programWorkflow.getConcept(),
                    found.getUuid(),
                    found.getProgram(),
                    found.getConcept()));
          }
          List<ConceptStateConversion> relatedConceptStateConversions =
              this.harmonizationProgramWorkflowServiceDAO
                  .findConceptStateConversionsByProgramWorkflowId(found.getId());
          List<ProgramWorkflowState> relatedProgramWorkflowStates =
              this.harmonizationProgramWorkflowServiceDAO
                  .findProgramWorkflowStatesByProgramWorkflowId(found.getId());
          this.updateToNextAvailableID(
              found, relatedConceptStateConversions, relatedProgramWorkflowStates);
        }
        this.harmonizationProgramWorkflowServiceDAO.saveNotSwappableProgramWorkflow(
            programWorkflow);
      }
    } catch (Exception e) {
      throw new APIException(e.getMessage(), e);
    } finally {
      try {
        this.harmonizationDAO.setEnableCheckConstraints();
      } catch (Exception e) {
        throw new APIException(e.getMessage(), e);
      }
    }
  }

  @Override
  @Authorized({"Manage ProgramWorkflow"})
  public void saveProgramWorkflowsWithDifferentIDAndEqualUUID(
      Map<String, List<ProgramWorkflowDTO>> mapProgramWorkflows) throws APIException {
    this.harmonizationDAO.evictCache();
    try {

      this.harmonizationDAO.setDisabledCheckConstraints();
      for (String uuid : mapProgramWorkflows.keySet()) {

        List<ProgramWorkflowDTO> list = mapProgramWorkflows.get(uuid);
        ProgramWorkflow mdsProgramWorkflow = list.get(0).getProgramWorkflow();
        ProgramWorkflow pdSProgramWorkflow = list.get(1).getProgramWorkflow();
        Integer mdServerProgramWorkflowId = mdsProgramWorkflow.getProgramWorkflowId();

        ProgramWorkflow foundMDS =
            this.harmonizationProgramWorkflowServiceDAO.getProgramWorkflowById(
                mdsProgramWorkflow.getId());

        if (foundMDS != null) {
          if (!this.harmonizationProgramWorkflowServiceDAO.isSwappable(foundMDS)) {
            throw new APIException(
                String.format(
                    "Cannot update the Production Server Program Workflow [ID = {%s}, UUID = {%s}, PROGRAM = {%s}, CONCEPT = {%s}] with the ID {%s} this new ID is already referencing an Existing Program Workflow In Metadata Server",
                    pdSProgramWorkflow.getId(),
                    pdSProgramWorkflow.getUuid(),
                    list.get(1).getProgram(),
                    list.get(1).getConcept(),
                    mdServerProgramWorkflowId));
          }
          List<ConceptStateConversion> relatedConceptStateConversions =
              this.harmonizationProgramWorkflowServiceDAO
                  .findConceptStateConversionsByProgramWorkflowId(foundMDS.getId());
          List<ProgramWorkflowState> relatedProgramWorkflowStates =
              this.harmonizationProgramWorkflowServiceDAO
                  .findProgramWorkflowStatesByProgramWorkflowId(foundMDS.getId());
          this.updateToNextAvailableID(
              foundMDS, relatedConceptStateConversions, relatedProgramWorkflowStates);
        }
        List<ConceptStateConversion> relatedConceptStateConversions =
            this.harmonizationProgramWorkflowServiceDAO
                .findConceptStateConversionsByProgramWorkflowId(pdSProgramWorkflow.getId());
        List<ProgramWorkflowState> relatedProgramWorkflowStates =
            this.harmonizationProgramWorkflowServiceDAO
                .findProgramWorkflowStatesByProgramWorkflowId(pdSProgramWorkflow.getId());
        this.updateToGivenId(
            pdSProgramWorkflow,
            mdServerProgramWorkflowId,
            false,
            relatedConceptStateConversions,
            relatedProgramWorkflowStates);
      }
    } catch (Exception e) {
      throw new APIException(e.getMessage(), e);
    } finally {
      try {
        this.harmonizationDAO.setEnableCheckConstraints();
      } catch (Exception e) {
        throw new APIException(e.getMessage(), e);
      }
    }
  }

  @Override
  @Authorized({"Manage ProgramWorkflow"})
  public void deleteNewProgramWorkflowsFromPDS(List<ProgramWorkflowDTO> programs)
      throws APIException {
    this.harmonizationDAO.evictCache();
    for (ProgramWorkflow program : DTOUtils.fromProgramWorkflowDTOs(programs)) {
      this.harmonizationProgramWorkflowServiceDAO.deleteProgramWorkflow(program);
    }
  }

  @Override
  @Authorized({"Manage ProgramWorkflow"})
  public void saveManualMapping(Map<ProgramWorkflowDTO, ProgramWorkflowDTO> mapProgramWorkflows)
      throws UUIDDuplicationException, SQLException {
    this.harmonizationDAO.evictCache();
    try {

      this.harmonizationDAO.setDisabledCheckConstraints();
      for (Entry<ProgramWorkflowDTO, ProgramWorkflowDTO> entry : mapProgramWorkflows.entrySet()) {

        ProgramWorkflowDTO pdsProgramWorkflowDTO = entry.getKey();
        ProgramWorkflowDTO mdsProgramWorkflowDTO = entry.getValue();
        ProgramWorkflow pdsProgramWorkflow = pdsProgramWorkflowDTO.getProgramWorkflow();
        ProgramWorkflow mdsProgramWorkflow = mdsProgramWorkflowDTO.getProgramWorkflow();

        ProgramWorkflow foundMDSProgramWorkflowByUuid =
            this.harmonizationProgramWorkflowServiceDAO.getProgramWorkflowByUuid(
                mdsProgramWorkflow.getUuid());

        if ((foundMDSProgramWorkflowByUuid != null
                && !foundMDSProgramWorkflowByUuid.getId().equals(mdsProgramWorkflow.getId()))
            && (!foundMDSProgramWorkflowByUuid.getId().equals(pdsProgramWorkflow.getId())
                && !foundMDSProgramWorkflowByUuid.getUuid().equals(pdsProgramWorkflow.getUuid()))) {

          throw new UUIDDuplicationException(
              String.format(
                  " Cannot Update the Program Workflow '%s' to '%s' and '%s' to '%s'. There is one entry with PROGRAM='%s', CONCEPT='%s', ID='%s' an UUID='%s' ",
                  pdsProgramWorkflowDTO.getProgram(),
                  mdsProgramWorkflowDTO.getProgram(),
                  pdsProgramWorkflowDTO.getConcept(),
                  mdsProgramWorkflowDTO.getConcept(),
                  harmonizationProgramWorkflowServiceDAO
                      .getProgramWorkflowProgram(foundMDSProgramWorkflowByUuid, false)
                      .getName(),
                  harmonizationProgramWorkflowServiceDAO.getProgramWorkflowConceptName(
                      foundMDSProgramWorkflowByUuid, false),
                  foundMDSProgramWorkflowByUuid.getId(),
                  foundMDSProgramWorkflowByUuid.getUuid()));
        }

        ProgramWorkflow foundPDS =
            this.harmonizationProgramWorkflowServiceDAO.getProgramWorkflowById(
                pdsProgramWorkflow.getId());

        final Program mdsProgram =
            harmonizationProgramWorkflowServiceDAO.getProgramWorkflowProgram(
                mdsProgramWorkflow, true);
        final Integer mdsConceptId =
            harmonizationProgramWorkflowServiceDAO.getProgramWorkflowConceptId(
                mdsProgramWorkflow, true);
        final Program pdsProgram =
            harmonizationProgramWorkflowServiceDAO.getProgramWorkflowProgram(
                pdsProgramWorkflow, false);
        final Integer pdsConceptId =
            harmonizationProgramWorkflowServiceDAO.getProgramWorkflowConceptId(
                pdsProgramWorkflow, false);

        if (mdsProgramWorkflow.getUuid().equals(pdsProgramWorkflow.getUuid())
            && mdsProgramWorkflow.getId().equals(pdsProgramWorkflow.getId())) {
          if (mdsProgramWorkflow.getId().equals(pdsProgramWorkflow.getId())
              && mdsProgram.getId().equals(pdsProgram.getId())
              && mdsConceptId.equals(pdsConceptId)) {
            return;
          }
        } else {
          List<ConceptStateConversion> relatedConceptStateConversions =
              this.harmonizationProgramWorkflowServiceDAO
                  .findConceptStateConversionsByProgramWorkflowId(foundPDS.getId());
          List<ProgramWorkflowState> relatedProgramWorkflowStates =
              this.harmonizationProgramWorkflowServiceDAO
                  .findProgramWorkflowStatesByProgramWorkflowId(foundPDS.getId());

          for (ConceptStateConversion conceptStateConversion : relatedConceptStateConversions) {
            this.harmonizationProgramWorkflowServiceDAO.updateConceptStateConversion(
                conceptStateConversion, mdsProgramWorkflow.getProgramWorkflowId());
          }
          for (ProgramWorkflowState programWorkflowState : relatedProgramWorkflowStates) {
            this.harmonizationProgramWorkflowServiceDAO.updateProgramWorkflowState(
                programWorkflowState, mdsProgramWorkflow.getProgramWorkflowId());
          }
          this.harmonizationProgramWorkflowServiceDAO.deleteProgramWorkflow(foundPDS);

          ProgramWorkflow foundMDSProgramWorkflowByID =
              this.harmonizationProgramWorkflowServiceDAO.getProgramWorkflowById(
                  mdsProgramWorkflow.getId());
          if (foundMDSProgramWorkflowByID == null) {
            this.harmonizationProgramWorkflowServiceDAO.saveNotSwappableProgramWorkflow(
                mdsProgramWorkflow);
          }
        }
      }
    } finally {
      try {
        this.harmonizationDAO.setEnableCheckConstraints();
      } catch (SQLException e) {
        throw new APIException(e.getMessage(), e);
      }
    }
  }

  private void updateToGivenId(
      ProgramWorkflow programWorkflow,
      Integer givenId,
      boolean swappable,
      List<ConceptStateConversion> relatedConceptStateConversions,
      List<ProgramWorkflowState> relatedProgramWorkflowStates) {
    this.harmonizationProgramWorkflowServiceDAO.updateProgramWorkflow(
        givenId, programWorkflow, swappable);

    for (ConceptStateConversion conceptStateConversion : relatedConceptStateConversions) {
      this.harmonizationProgramWorkflowServiceDAO.updateConceptStateConversion(
          conceptStateConversion, givenId);
    }
    for (ProgramWorkflowState programWorkflowState : relatedProgramWorkflowStates) {
      this.harmonizationProgramWorkflowServiceDAO.updateProgramWorkflowState(
          programWorkflowState, givenId);
    }
  }

  private void updateToNextAvailableID(
      ProgramWorkflow program,
      List<ConceptStateConversion> relatedConceptStateConversions,
      List<ProgramWorkflowState> relatedProgramWorkflowStates) {
    ProgramWorkflow updated =
        this.harmonizationProgramWorkflowServiceDAO.updateToNextAvailableId(program);

    for (ConceptStateConversion conceptStateConversion : relatedConceptStateConversions) {
      this.harmonizationProgramWorkflowServiceDAO.updateConceptStateConversion(
          conceptStateConversion, updated.getProgramWorkflowId());
    }
    for (ProgramWorkflowState programWorkflowState : relatedProgramWorkflowStates) {
      this.harmonizationProgramWorkflowServiceDAO.updateProgramWorkflowState(
          programWorkflowState, updated.getProgramWorkflowId());
    }
  }

  @Override
  public ProgramWorkflow findMetadataProgramWorkflowByUuid(String uuid) throws APIException {
    return harmonizationProgramWorkflowServiceDAO.findMDSPProgramWorkflowByUuid(uuid);
  }

  @Override
  public ProgramWorkflow findProductionProgramWorkflowByUuid(String uuid) throws APIException {
    this.harmonizationDAO.evictCache();
    return programWorkflowService.getWorkflowByUuid(uuid);
  }

  @Override
  public List<ProgramWorkflow> findAllMetadataProgramWorkflows() throws APIException {
    this.harmonizationDAO.evictCache();
    return this.harmonizationProgramWorkflowServiceDAO.findAllMetadataServerProgramWorkflows();
  }

  @Override
  public void setProgramAndConceptNames(
      List<ProgramWorkflowDTO> programWorkflowDTOs, boolean isFromMetadata) throws APIException {
    for (ProgramWorkflowDTO programWorkflowDTO : programWorkflowDTOs) {
      programWorkflowDTO.setProgram(
          harmonizationProgramWorkflowServiceDAO
              .getProgramWorkflowProgram(programWorkflowDTO.getProgramWorkflow(), isFromMetadata)
              .getName());
      programWorkflowDTO.setConcept(
          harmonizationProgramWorkflowServiceDAO.getProgramWorkflowConceptName(
              programWorkflowDTO.getProgramWorkflow(), isFromMetadata));
    }
  }

  @Override
  public Program getProgramWorkflowProgram(ProgramWorkflow programWorkflow, boolean isFromMetadata)
      throws APIException {
    this.harmonizationDAO.evictCache();
    return this.harmonizationProgramWorkflowServiceDAO.getProgramWorkflowProgram(
        programWorkflow, isFromMetadata);
  }

  @Override
  public Integer getProgramWorkflowConceptId(
      ProgramWorkflow programWorkflow, boolean isFromMetadata) throws APIException {
    this.harmonizationDAO.evictCache();
    return this.harmonizationProgramWorkflowServiceDAO.getProgramWorkflowConceptId(
        programWorkflow, isFromMetadata);
  }

  @Override
  public boolean isHarmonized() throws APIException {
    return findAllMetadataProgramWorkflowsNotContainedInProductionServer().isEmpty()
        && findAllProductionProgramWorkflowsNotContainedInMetadataServer().isEmpty()
        && findAllProgramWorkflowsWithDifferentIDAndSameUUID().isEmpty()
        && findAllProgramWorkflowsWithDifferentProgramOrConceptAndSameUUIDAndID().isEmpty();
  }
}
