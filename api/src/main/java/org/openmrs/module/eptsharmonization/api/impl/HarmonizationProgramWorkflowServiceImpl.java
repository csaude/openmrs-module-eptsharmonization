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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.openmrs.ConceptStateConversion;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationProgramWorkflowService;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationProgramWorkflowServiceDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.openmrs.module.eptsharmonization.api.model.ProgramWorkflowDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** It is a default implementation of {@link HarmonizationProgramWorkflowService}. */
@Transactional
@Service("eptsharmonization.harmonizationProgramWorkflowService")
public class HarmonizationProgramWorkflowServiceImpl extends BaseOpenmrsService
    implements HarmonizationProgramWorkflowService {

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
    return DTOUtils.fromProgramWorkflows(mdsProgramWorkflows);
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
    return DTOUtils.fromProgramWorkflows(pdsProgramWorkflows);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View ProgramWorkflow"})
  public List<ProgramWorkflowDTO> findAllMetadataProgramWorkflowsPartialEqualsToProductionServer()
      throws APIException {
    this.harmonizationDAO.evictCache();
    List<ProgramWorkflow> allMDS =
        harmonizationProgramWorkflowServiceDAO.findAllMetadataServerProgramWorkflows();
    List<ProgramWorkflow> allPDS =
        harmonizationProgramWorkflowServiceDAO.findAllProductionServerProgramWorkflows();
    List<ProgramWorkflow> mdsProgramWorkflows =
        this.removeElementsWithDifferentIDsAndSameUUIDs(allMDS, allPDS);
    allMDS.removeAll(allPDS);
    mdsProgramWorkflows.removeAll(allMDS);
    return DTOUtils.fromProgramWorkflows(mdsProgramWorkflows);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View ProgramWorkflow"})
  public List<ProgramWorkflowDTO> findAllProductionProgramWorkflowsPartialEqualsToMetadataServer()
      throws APIException {
    this.harmonizationDAO.evictCache();
    List<ProgramWorkflow> allPDS =
        harmonizationProgramWorkflowServiceDAO.findAllProductionServerProgramWorkflows();
    List<ProgramWorkflow> allMDS =
        harmonizationProgramWorkflowServiceDAO.findAllMetadataServerProgramWorkflows();
    List<ProgramWorkflow> pdsProgramWorkflows =
        this.removeElementsWithDifferentIDsAndSameUUIDs(allPDS, allMDS);
    allPDS.removeAll(allMDS);
    pdsProgramWorkflows.removeAll(allPDS);
    return DTOUtils.fromProgramWorkflows(pdsProgramWorkflows);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View ProgramWorkflow"})
  public Map<String, List<ProgramWorkflowDTO>>
      findAllProgramWorkflowsWithDifferentNameAndSameUUIDAndID() throws APIException {
    this.harmonizationDAO.evictCache();
    Map<String, List<ProgramWorkflowDTO>> result = new HashMap<>();
    Map<String, List<ProgramWorkflow>> map = findByWithDifferentNameAndSameUUIDAndID();
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromProgramWorkflows(map.get(key)));
    }
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View ProgramWorkflow"})
  public Map<String, List<ProgramWorkflowDTO>> findAllProgramWorkflowsWithDifferentIDAndSameUUID()
      throws APIException {
    this.harmonizationDAO.evictCache();
    Map<String, List<ProgramWorkflowDTO>> result = new HashMap<>();
    Map<String, List<ProgramWorkflow>> map = findByWithDifferentIDAndSameUUID();
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromProgramWorkflows(map.get(key)));
    }
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Program"})
  public int getNumberOfAffectedConceptStateConversions(ProgramWorkflowDTO programWorkflowDTO) {
    this.harmonizationDAO.evictCache();
    return harmonizationProgramWorkflowServiceDAO
        .findConceptStateConversionsByProgramWorkflowId(
            programWorkflowDTO.getProgramWorkflow().getProgramWorkflowId())
        .size();
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View ProgramWorkflow"})
  public int getNumberOfAffectedProgramWorkflowStates(ProgramWorkflowDTO programWorkflowDTO) {
    this.harmonizationDAO.evictCache();
    return harmonizationProgramWorkflowServiceDAO
        .findProgramWorkflowStatesByProgramWorkflowId(
            programWorkflowDTO.getProgramWorkflow().getId())
        .size();
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View ProgramWorkflow"})
  public List<ProgramWorkflow> findPDSProgramWorkflowsNotExistsInMDServer() throws APIException {
    this.harmonizationDAO.evictCache();
    List<ProgramWorkflow> result =
        harmonizationProgramWorkflowServiceDAO.findPDSProgramWorkflowsNotExistsInMDServer();
    return result;
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
  public void UpdateProgramWorkflowsWithDifferentProgramsOrConcept(
      Map<String, List<ProgramWorkflowDTO>> programWorkflows) throws APIException {
    this.harmonizationDAO.evictCache();
    for (String key : programWorkflows.keySet()) {
      List<ProgramWorkflowDTO> list = programWorkflows.get(key);
      ProgramWorkflowDTO mdsProgramWorkflow = list.get(0);
      ProgramWorkflowDTO pdsProgramWorkflow = list.get(1);
      ProgramWorkflow programWorkflow =
          this.programWorkflowService.getWorkflow(pdsProgramWorkflow.getProgramWorkflow().getId());
      programWorkflow.setProgram(mdsProgramWorkflow.getProgramWorkflow().getProgram());
      programWorkflow.setConcept(mdsProgramWorkflow.getProgramWorkflow().getConcept());
      this.programWorkflowService.updateWorkflow(programWorkflow);
    }
  }

  @Override
  @Authorized({"Manage ProgramWorkflow"})
  public void saveNewProgramWorkflowsFromMDS(List<ProgramWorkflowDTO> programs)
      throws APIException {
    this.harmonizationDAO.evictCache();
    try {
      this.harmonizationDAO.setDisabledCheckConstraints();
      for (ProgramWorkflow program : DTOUtils.fromProgramWorkflowDTOs(programs)) {

        ProgramWorkflow found =
            this.harmonizationProgramWorkflowServiceDAO.getProgramWorkflowById(program.getId());

        if (found != null) {

          if (!this.harmonizationProgramWorkflowServiceDAO.isSwappable(found)) {

            throw new APIException(
                String.format(
                    "Cannot Insert Program Workflow with ID %s, UUID %s and NAME %s. This ID is being in use by another Program Workflow from Metatada server with UUID %s and name %s ",
                    program.getId(),
                    program.getUuid(),
                    program.getName(),
                    found.getUuid(),
                    found.getName()));
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
        this.harmonizationProgramWorkflowServiceDAO.saveNotSwappableProgramWorkflow(program);
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
                    "Cannot update the Production Server Program Workflow [ID = {%s}, UUID = {%s}, NAME = {%s}] with the ID {%s} this new ID is already referencing an Existing Program Workflow In Metadata Server",
                    pdSProgramWorkflow.getId(),
                    pdSProgramWorkflow.getUuid(),
                    pdSProgramWorkflow.getName(),
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

        ProgramWorkflow foundPDS =
            this.harmonizationProgramWorkflowServiceDAO.getProgramWorkflowById(
                pdSProgramWorkflow.getId());
        if (!this.harmonizationProgramWorkflowServiceDAO.isSwappable(foundPDS)) {
          throw new APIException(
              String.format(
                  "Cannot update the Production Server ProgramWorkflow with ID {%s}, UUID {%s} and NAME {%s}. This ProgramWorkflow is a Reference from an ProgramWorkflow of Metadata Server",
                  foundPDS.getId(), foundPDS.getUuid(), foundPDS.getName()));
        }
        List<ConceptStateConversion> relatedConceptStateConversions =
            this.harmonizationProgramWorkflowServiceDAO
                .findConceptStateConversionsByProgramWorkflowId(foundPDS.getId());
        List<ProgramWorkflowState> relatedProgramWorkflowStates =
            this.harmonizationProgramWorkflowServiceDAO
                .findProgramWorkflowStatesByProgramWorkflowId(foundPDS.getId());
        this.updateToGivenId(
            foundPDS,
            mdServerProgramWorkflowId,
            false,
            relatedConceptStateConversions,
            relatedProgramWorkflowStates);
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
  @Authorized({"Manage ProgramWorkflow"})
  public void deleteNewProgramWorkflowsFromPDS(List<ProgramWorkflowDTO> programs)
      throws APIException {
    this.harmonizationDAO.evictCache();
    for (ProgramWorkflow program : DTOUtils.fromProgramWorkflowDTOs(programs)) {
      this.harmonizationProgramWorkflowServiceDAO.deleteProgramWorkflow(program);
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  @Authorized({"Manage ProgramWorkflow"})
  public void saveManualMapping(Map<ProgramWorkflow, ProgramWorkflow> mapProgramWorkflows)
      throws APIException {
    this.harmonizationDAO.evictCache();
    try {

      this.harmonizationDAO.setDisabledCheckConstraints();
      for (Entry<ProgramWorkflow, ProgramWorkflow> entry : mapProgramWorkflows.entrySet()) {

        ProgramWorkflow pdSProgramWorkflow = entry.getKey();
        ProgramWorkflow mdsProgramWorkflow = entry.getValue();

        ProgramWorkflow foundPDS =
            this.harmonizationProgramWorkflowServiceDAO.getProgramWorkflowById(
                pdSProgramWorkflow.getId());

        if (mdsProgramWorkflow.getUuid().equals(pdSProgramWorkflow.getUuid())
            && mdsProgramWorkflow.getId().equals(pdSProgramWorkflow.getId())) {
          if (mdsProgramWorkflow.getId().equals(pdSProgramWorkflow.getId())
              && mdsProgramWorkflow.getName().equals(pdSProgramWorkflow.getName())) {
            return;
          }
          foundPDS.setName(mdsProgramWorkflow.getName());
          foundPDS.setDescription(mdsProgramWorkflow.getDescription());
          this.programWorkflowService.updateWorkflow(foundPDS);

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
        }
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

  private List<ProgramWorkflow> removeElementsWithDifferentIDsAndSameUUIDs(
      List<ProgramWorkflow> mdsProgramWorkflows, List<ProgramWorkflow> pdsProgramWorkflows) {
    List<ProgramWorkflow> auxMDS = new ArrayList<>();
    for (ProgramWorkflow mdsProgramWorkflow : mdsProgramWorkflows) {
      for (ProgramWorkflow pdsProgramWorkflow : pdsProgramWorkflows) {
        if (mdsProgramWorkflow.getId().compareTo(pdsProgramWorkflow.getId()) != 0
            && mdsProgramWorkflow.getUuid().contentEquals(pdsProgramWorkflow.getUuid())) {
          auxMDS.add(mdsProgramWorkflow);
        }
      }
    }
    return auxMDS;
  }

  private Map<String, List<ProgramWorkflow>> findByWithDifferentNameAndSameUUIDAndID() {
    List<ProgramWorkflow> allMDS =
        harmonizationProgramWorkflowServiceDAO.findAllMetadataServerProgramWorkflows();
    List<ProgramWorkflow> allPDS =
        harmonizationProgramWorkflowServiceDAO.findAllProductionServerProgramWorkflows();

    Map<String, List<ProgramWorkflow>> map = new TreeMap<>();
    for (ProgramWorkflow mdsItem : allMDS) {
      for (ProgramWorkflow pdsItem : allPDS) {
        if (mdsItem.getUuid().equals(pdsItem.getUuid())
            && mdsItem.getId().equals(pdsItem.getId())
            && !mdsItem.getName().equalsIgnoreCase(pdsItem.getName())) {
          map.put(mdsItem.getUuid(), Arrays.asList(mdsItem, pdsItem));
        }
      }
    }
    return map;
  }

  private Map<String, List<ProgramWorkflow>> findByWithDifferentIDAndSameUUID() {
    List<ProgramWorkflow> allPDS =
        harmonizationProgramWorkflowServiceDAO.findAllProductionServerProgramWorkflows();
    List<ProgramWorkflow> allMDS =
        harmonizationProgramWorkflowServiceDAO.findAllMetadataServerProgramWorkflows();
    Map<String, List<ProgramWorkflow>> map = new TreeMap<>();
    for (ProgramWorkflow mdsItem : allMDS) {
      for (ProgramWorkflow pdsItem : allPDS) {
        if (mdsItem.getUuid().equals(pdsItem.getUuid())
            && !mdsItem.getId().equals(pdsItem.getId())) {
          map.put(mdsItem.getUuid(), Arrays.asList(mdsItem, pdsItem));
        }
      }
    }
    return map;
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
  public void setProgramAndConceptNames(List<ProgramWorkflowDTO> programWorkflowDTOs)
      throws APIException {
    for (ProgramWorkflowDTO programWorkflowDTO : programWorkflowDTOs) {
      programWorkflowDTO.setProgram(
          harmonizationProgramWorkflowServiceDAO.getProgramWorkflowProgramName(
              programWorkflowDTO.getProgramWorkflow()));
      programWorkflowDTO.setConcept(
          harmonizationProgramWorkflowServiceDAO.getProgramWorkflowConceptName(
              programWorkflowDTO.getProgramWorkflow()));
    }
  }
}
