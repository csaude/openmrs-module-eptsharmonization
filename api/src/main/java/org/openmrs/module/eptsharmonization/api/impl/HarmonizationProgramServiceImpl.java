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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationProgramService;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationProgramServiceDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.openmrs.module.eptsharmonization.api.exception.UUIDDuplicationException;
import org.openmrs.module.eptsharmonization.api.model.ProgramDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** It is a default implementation of {@link HarmonizationProgramService}. */
@Transactional
@Service("eptsharmonization.harmonizationProgramService")
public class HarmonizationProgramServiceImpl extends BaseOpenmrsService
    implements HarmonizationProgramService {

  private HarmonizationServiceDAO harmonizationDAO;
  private ProgramWorkflowService programWorkflowService;
  private HarmonizationProgramServiceDAO harmonizationProgramServiceDAO;

  @Autowired
  public void setHarmonizationDAO(HarmonizationServiceDAO harmonizationDAO) {
    this.harmonizationDAO = harmonizationDAO;
  }

  @Autowired
  public void setHarmonizationProgramServiceDAO(
      HarmonizationProgramServiceDAO harmonizationProgramServiceDAO) {
    this.harmonizationProgramServiceDAO = harmonizationProgramServiceDAO;
  }

  @Autowired
  public void setProgramWorkflowService(ProgramWorkflowService programWorkflowService) {
    this.programWorkflowService = programWorkflowService;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Program"})
  public List<ProgramDTO> findAllMetadataProgramsNotContainedInProductionServer()
      throws APIException {
    this.harmonizationDAO.evictCache();
    List<Program> mdsPrograms = harmonizationProgramServiceDAO.findAllMetadataServerPrograms();
    List<Program> pdsPrograms = harmonizationProgramServiceDAO.findAllProductionServerPrograms();
    mdsPrograms.removeAll(pdsPrograms);
    return DTOUtils.fromPrograms(mdsPrograms);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Program"})
  public List<ProgramDTO> findAllProductionProgramsNotContainedInMetadataServer()
      throws APIException {
    this.harmonizationDAO.evictCache();
    List<Program> pdsPrograms = harmonizationProgramServiceDAO.findAllProductionServerPrograms();
    List<Program> mdsPrograms = harmonizationProgramServiceDAO.findAllMetadataServerPrograms();
    pdsPrograms.removeAll(mdsPrograms);
    return DTOUtils.fromPrograms(pdsPrograms);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Program"})
  public List<ProgramDTO> findAllMetadataProgramsPartialEqualsToProductionServer()
      throws APIException {
    this.harmonizationDAO.evictCache();
    List<Program> allMDS = harmonizationProgramServiceDAO.findAllMetadataServerPrograms();
    List<Program> allPDS = harmonizationProgramServiceDAO.findAllProductionServerPrograms();
    List<Program> mdsPrograms = this.removeElementsWithDifferentIDsAndSameUUIDs(allMDS, allPDS);
    allMDS.removeAll(allPDS);
    mdsPrograms.removeAll(allMDS);
    return DTOUtils.fromPrograms(mdsPrograms);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Program"})
  public List<ProgramDTO> findAllProductionProgramsPartialEqualsToMetadataServer()
      throws APIException {
    this.harmonizationDAO.evictCache();
    List<Program> allPDS = harmonizationProgramServiceDAO.findAllProductionServerPrograms();
    List<Program> allMDS = harmonizationProgramServiceDAO.findAllMetadataServerPrograms();
    List<Program> pdsPrograms = this.removeElementsWithDifferentIDsAndSameUUIDs(allPDS, allMDS);
    allPDS.removeAll(allMDS);
    pdsPrograms.removeAll(allPDS);
    return DTOUtils.fromPrograms(pdsPrograms);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Program"})
  public Map<String, List<ProgramDTO>> findAllProgramsWithDifferentNameAndSameUUIDAndID()
      throws APIException {
    this.harmonizationDAO.evictCache();
    Map<String, List<ProgramDTO>> result = new HashMap<>();
    Map<String, List<Program>> map = findByWithDifferentNameAndSameUUIDAndID();
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromPrograms(map.get(key)));
    }
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Program"})
  public Map<String, List<ProgramDTO>> findAllProgramsWithDifferentIDAndSameUUID()
      throws APIException {
    this.harmonizationDAO.evictCache();
    Map<String, List<ProgramDTO>> result = new HashMap<>();
    Map<String, List<Program>> map = findByWithDifferentIDAndSameUUID();
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromPrograms(map.get(key)));
    }
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Program"})
  public int getNumberOfAffectedPatientPrograms(ProgramDTO programDTO) {
    this.harmonizationDAO.evictCache();
    return harmonizationProgramServiceDAO
        .findPatientProgramsByProgramId(programDTO.getProgram().getProgramId())
        .size();
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Program"})
  public int getNumberOfAffectedProgramWorkflow(ProgramDTO programDTO) {
    this.harmonizationDAO.evictCache();
    return harmonizationProgramServiceDAO
        .findProgramWorkflowsByProgramId(programDTO.getProgram().getProgramId())
        .size();
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Program"})
  public List<Program> findPDSProgramsNotExistsInMDServer() throws APIException {
    this.harmonizationDAO.evictCache();
    List<Program> result = harmonizationProgramServiceDAO.findPDSProgramsNotExistsInMDServer();
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Program"})
  public List<Program> findAllNotSwappablePrograms() throws APIException {
    this.harmonizationDAO.evictCache();
    List<Program> result = this.harmonizationProgramServiceDAO.findAllNotSwappable();
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Program"})
  public List<Program> findAllSwappablePrograms() throws APIException {
    this.harmonizationDAO.evictCache();
    List<Program> findAllSwappable = this.harmonizationProgramServiceDAO.findAllSwappable();
    return findAllSwappable;
  }

  @Override
  @Authorized({"Manage Program"})
  public void saveProgramsWithDifferentNames(Map<String, List<ProgramDTO>> programs)
      throws APIException {
    this.harmonizationDAO.evictCache();
    for (String key : programs.keySet()) {
      List<ProgramDTO> list = programs.get(key);
      ProgramDTO mdsProgram = list.get(0);
      ProgramDTO pdsProgram = list.get(1);
      Program program = this.programWorkflowService.getProgram(pdsProgram.getProgram().getId());
      program.setName(mdsProgram.getProgram().getName());
      program.setDescription(mdsProgram.getProgram().getDescription());
      this.programWorkflowService.saveProgram(program);
    }
  }

  @Override
  @Authorized({"Manage Program"})
  public void saveNewProgramsFromMDS(List<ProgramDTO> programs) throws APIException {
    this.harmonizationDAO.evictCache();
    try {
      this.harmonizationDAO.setDisabledCheckConstraints();
      for (Program program : DTOUtils.fromProgramDTOs(programs)) {

        Program found = this.harmonizationProgramServiceDAO.getProgramById(program.getId());

        if (found != null) {

          if (!this.harmonizationProgramServiceDAO.isSwappable(found)) {

            throw new APIException(
                String.format(
                    "Cannot Insert Program with ID %s, UUID %s and NAME %s. This ID is being in use by another Program from Metatada server with UUID %s and name %s ",
                    program.getId(),
                    program.getUuid(),
                    program.getName(),
                    found.getUuid(),
                    found.getName()));
          }
          List<PatientProgram> relatedPatientPrograms =
              this.harmonizationProgramServiceDAO.findPatientProgramsByProgramId(found.getId());
          List<ProgramWorkflow> relatedProgramWorkflows =
              this.harmonizationProgramServiceDAO.findProgramWorkflowsByProgramId(found.getId());
          this.updateToNextAvailableID(found, relatedPatientPrograms, relatedProgramWorkflows);
        }
        this.harmonizationProgramServiceDAO.saveNotSwappableProgram(program);
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
  @Authorized({"Manage Program"})
  public void saveProgramsWithDifferentIDAndEqualUUID(Map<String, List<ProgramDTO>> mapPrograms)
      throws APIException {
    this.harmonizationDAO.evictCache();
    try {

      this.harmonizationDAO.setDisabledCheckConstraints();
      for (String uuid : mapPrograms.keySet()) {

        List<ProgramDTO> list = mapPrograms.get(uuid);
        Program mdsProgram = list.get(0).getProgram();
        Program pdSProgram = list.get(1).getProgram();
        Integer mdServerProgramId = mdsProgram.getProgramId();

        Program foundMDS = this.harmonizationProgramServiceDAO.getProgramById(mdsProgram.getId());

        if (foundMDS != null) {
          if (!this.harmonizationProgramServiceDAO.isSwappable(foundMDS)) {
            throw new APIException(
                String.format(
                    "Cannot update the Production Server Program [ID = {%s}, UUID = {%s}, NAME = {%s}] with the ID {%s} this new ID is already referencing an Existing Program In Metadata Server",
                    pdSProgram.getId(),
                    pdSProgram.getUuid(),
                    pdSProgram.getName(),
                    mdServerProgramId));
          }
          List<PatientProgram> relatedPatientPrograms =
              this.harmonizationProgramServiceDAO.findPatientProgramsByProgramId(foundMDS.getId());
          List<ProgramWorkflow> relatedProgramWorkflows =
              this.harmonizationProgramServiceDAO.findProgramWorkflowsByProgramId(foundMDS.getId());
          this.updateToNextAvailableID(foundMDS, relatedPatientPrograms, relatedProgramWorkflows);
        }

        Program foundPDS = this.harmonizationProgramServiceDAO.getProgramById(pdSProgram.getId());
        if (!this.harmonizationProgramServiceDAO.isSwappable(foundPDS)) {
          throw new APIException(
              String.format(
                  "Cannot update the Production Server Program with ID {%s}, UUID {%s} and NAME {%s}. This Program is a Reference from an Program of Metadata Server",
                  foundPDS.getId(), foundPDS.getUuid(), foundPDS.getName()));
        }
        List<PatientProgram> relatedPatientPrograms =
            this.harmonizationProgramServiceDAO.findPatientProgramsByProgramId(foundPDS.getId());
        List<ProgramWorkflow> relatedProgramWorkflows =
            this.harmonizationProgramServiceDAO.findProgramWorkflowsByProgramId(foundPDS.getId());
        this.updateToGivenId(
            foundPDS, mdServerProgramId, false, relatedPatientPrograms, relatedProgramWorkflows);
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
  @Authorized({"Manage Program"})
  public void deleteNewProgramsFromPDS(List<ProgramDTO> programs) throws APIException {
    this.harmonizationDAO.evictCache();
    for (Program program : DTOUtils.fromProgramDTOs(programs)) {
      this.harmonizationProgramServiceDAO.deleteProgram(program);
    }
  }

  @Override
  @Authorized({"Manage Program"})
  public void saveManualMapping(Map<Program, Program> mapPrograms)
      throws UUIDDuplicationException, SQLException {
    this.harmonizationDAO.evictCache();
    try {

      this.harmonizationDAO.setDisabledCheckConstraints();
      for (Entry<Program, Program> entry : mapPrograms.entrySet()) {

        Program pdSProgram = entry.getKey();
        Program mdsProgram = entry.getValue();

        Program foundMDSProgramByUuid =
            this.harmonizationProgramServiceDAO.getProgramByUuid(mdsProgram.getUuid());

        if ((foundMDSProgramByUuid != null
                && !foundMDSProgramByUuid.getId().equals(mdsProgram.getId()))
            && (!foundMDSProgramByUuid.getId().equals(pdSProgram.getId())
                && !foundMDSProgramByUuid.getUuid().equals(pdSProgram.getUuid()))) {

          throw new UUIDDuplicationException(
              String.format(
                  " Cannot Update the Program '%s' to '%s'. There is one entry with NAME='%s', ID='%s' an UUID='%s' ",
                  pdSProgram.getName(),
                  mdsProgram.getName(),
                  foundMDSProgramByUuid.getName(),
                  foundMDSProgramByUuid.getId(),
                  foundMDSProgramByUuid.getUuid()));
        }

        Program foundPDS = this.harmonizationProgramServiceDAO.getProgramById(pdSProgram.getId());

        if (mdsProgram.getUuid().equals(pdSProgram.getUuid())
            && mdsProgram.getId().equals(pdSProgram.getId())) {
          if (mdsProgram.getId().equals(pdSProgram.getId())
              && mdsProgram.getName().equals(pdSProgram.getName())) {
            return;
          }
        } else {
          List<PatientProgram> relatedPatientPrograms =
              this.harmonizationProgramServiceDAO.findPatientProgramsByProgramId(foundPDS.getId());

          List<ProgramWorkflow> relatedProgramWorkflows =
              this.harmonizationProgramServiceDAO.findProgramWorkflowsByProgramId(foundPDS.getId());

          for (ProgramWorkflow programWorkflow : relatedProgramWorkflows) {
            this.harmonizationProgramServiceDAO.updateProgramWorkflow(
                programWorkflow, mdsProgram.getProgramId());
          }
          for (PatientProgram patientProgram : relatedPatientPrograms) {
            this.harmonizationProgramServiceDAO.updatePatientProgram(
                patientProgram, mdsProgram.getProgramId());
          }
          this.harmonizationProgramServiceDAO.deleteProgram(foundPDS);

          Program foundMDSProgramByID =
              this.harmonizationProgramServiceDAO.getProgramById(mdsProgram.getId());
          if (foundMDSProgramByID == null) {
            this.harmonizationProgramServiceDAO.saveNotSwappableProgram(mdsProgram);
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

  private List<Program> removeElementsWithDifferentIDsAndSameUUIDs(
      List<Program> mdsPrograms, List<Program> pdsPrograms) {
    List<Program> auxMDS = new ArrayList<>();
    for (Program mdsProgram : mdsPrograms) {
      for (Program pdsProgram : pdsPrograms) {
        if (mdsProgram.getId().compareTo(pdsProgram.getId()) != 0
            && mdsProgram.getUuid().contentEquals(pdsProgram.getUuid())) {
          auxMDS.add(mdsProgram);
        }
      }
    }
    return auxMDS;
  }

  private Map<String, List<Program>> findByWithDifferentNameAndSameUUIDAndID() {
    List<Program> allMDS = harmonizationProgramServiceDAO.findAllMetadataServerPrograms();
    List<Program> allPDS = harmonizationProgramServiceDAO.findAllProductionServerPrograms();

    Map<String, List<Program>> map = new TreeMap<>();
    for (Program mdsItem : allMDS) {
      for (Program pdsItem : allPDS) {
        if (mdsItem.getUuid().equals(pdsItem.getUuid())
            && mdsItem.getId().equals(pdsItem.getId())
            && !mdsItem.getName().trim().equalsIgnoreCase(pdsItem.getName().trim())) {
          map.put(mdsItem.getUuid(), Arrays.asList(mdsItem, pdsItem));
        }
      }
    }
    return map;
  }

  private Map<String, List<Program>> findByWithDifferentIDAndSameUUID() {
    List<Program> allPDS = harmonizationProgramServiceDAO.findAllProductionServerPrograms();
    List<Program> allMDS = harmonizationProgramServiceDAO.findAllMetadataServerPrograms();
    Map<String, List<Program>> map = new TreeMap<>();
    for (Program mdsItem : allMDS) {
      for (Program pdsItem : allPDS) {
        if (mdsItem.getUuid().equals(pdsItem.getUuid())
            && !mdsItem.getId().equals(pdsItem.getId())) {
          map.put(mdsItem.getUuid(), Arrays.asList(mdsItem, pdsItem));
        }
      }
    }
    return map;
  }

  private void updateToGivenId(
      Program program,
      Integer programId,
      boolean swappable,
      List<PatientProgram> relatedPatientPrograms,
      List<ProgramWorkflow> relatedProgramWorkflows) {
    this.harmonizationProgramServiceDAO.updateProgram(programId, program, swappable);

    for (ProgramWorkflow programWorkflow : relatedProgramWorkflows) {
      this.harmonizationProgramServiceDAO.updateProgramWorkflow(programWorkflow, programId);
    }
    for (PatientProgram patientProgram : relatedPatientPrograms) {
      this.harmonizationProgramServiceDAO.updatePatientProgram(patientProgram, programId);
    }
  }

  private void updateToNextAvailableID(
      Program program,
      List<PatientProgram> relatedPatientPrograms,
      List<ProgramWorkflow> relatedProgramWorkflow) {
    Program updated = this.harmonizationProgramServiceDAO.updateToNextAvailableId(program);
    for (ProgramWorkflow programWorkflow : relatedProgramWorkflow) {
      this.harmonizationProgramServiceDAO.updateProgramWorkflow(
          programWorkflow, updated.getProgramId());
    }
    for (PatientProgram patientProgram : relatedPatientPrograms) {
      this.harmonizationProgramServiceDAO.updatePatientProgram(
          patientProgram, updated.getProgramId());
    }
  }

  @Override
  public Program findMetadataProgramByUuid(String uuid) throws APIException {
    return harmonizationProgramServiceDAO.findMDSPProgramByUuid(uuid);
  }

  @Override
  public Program findProductionProgramByUuid(String uuid) throws APIException {
    this.harmonizationDAO.evictCache();
    return programWorkflowService.getProgramByUuid(uuid);
  }

  @Override
  public List<Program> findAllMetadataPrograms() throws APIException {
    this.harmonizationDAO.evictCache();
    return this.harmonizationProgramServiceDAO.findAllMetadataServerPrograms();
  }
}
