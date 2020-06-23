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
package org.openmrs.module.eptsharmonization.api.db;

import java.util.List;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.eptsharmonization.api.HarmonizationProgramService;

/** Database methods for {@link HarmonizationProgramService}. */
public interface HarmonizationProgramServiceDAO {

  public List<Program> findAllMetadataServerPrograms() throws DAOException;

  public List<Program> findAllProductionServerPrograms() throws DAOException;

  public List<PatientProgram> findPatientProgramsByProgramId(Integer programId) throws DAOException;

  public List<ProgramWorkflow> findProgramWorkflowsByProgramId(Integer programId)
      throws DAOException;

  public List<Program> findPDSProgramsNotExistsInMDServer() throws DAOException;

  public Program getProgramById(Integer programId) throws DAOException;

  public boolean isSwappable(Program program) throws DAOException;

  public List<Program> findAllSwappable() throws DAOException;

  public List<Program> findAllNotSwappable() throws DAOException;

  public Program updateProgram(Integer nextId, Program program, boolean swappable)
      throws DAOException;

  public void updatePatientProgram(PatientProgram patientProgram, Integer programId)
      throws DAOException;

  public void updateProgramWorkflow(ProgramWorkflow programWorkflow, Integer programId)
      throws DAOException;

  public void saveNotSwappableProgram(Program program) throws DAOException;

  public Program updateToNextAvailableId(Program program) throws DAOException;

  public void deleteProgram(Program program) throws DAOException;

  public Program findMDSPProgramByUuid(String uuid) throws DAOException;

  public Program findPDSPProgramByUuid(String uuid) throws DAOException;
}
