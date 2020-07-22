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
import org.openmrs.ConceptStateConversion;
import org.openmrs.PatientState;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.eptsharmonization.api.HarmonizationProgramWorkflowStateService;

/** Database methods for {@link HarmonizationProgramWorkflowStateService}. */
public interface HarmonizationProgramWorkflowStateServiceDAO {

  public List<ProgramWorkflowState> findAllMDSProgramWorkflowStates() throws DAOException;

  public List<ProgramWorkflowState> findAllPDSProgramWorkflowStates() throws DAOException;

  public List<PatientState> findPatientStatesByProgramWorkflowStateId(
      Integer programWorkflowStateId) throws DAOException;

  public List<ConceptStateConversion> findConceptStateConversionsByProgramWorkflowStateId(
      Integer programWorkflowStateId) throws DAOException;

  public ProgramWorkflowState getProgramWorkflowStateById(Integer programWorkflowStateId)
      throws DAOException;

  public ProgramWorkflowState getProgramWorkflowStateByUuid(String uuid) throws DAOException;

  public boolean isSwappable(ProgramWorkflowState programWorkflowState) throws DAOException;

  public List<ProgramWorkflowState> findAllSwappable() throws DAOException;

  public List<ProgramWorkflowState> findAllNotSwappable() throws DAOException;

  public ProgramWorkflowState updateProgramWorkflowState(ProgramWorkflowState programWorkflowState)
      throws DAOException;

  public ProgramWorkflowState updateProgramWorkflowState(
      Integer nextId, ProgramWorkflowState programWorkflowState, boolean swappable)
      throws DAOException;

  public void updateConceptStateConversion(
      ConceptStateConversion coceptStateConversion, Integer programWorkflowStateId)
      throws DAOException;

  public void updatePatientState(PatientState patientState, Integer programWorkflowStateId)
      throws DAOException;

  public void saveNotSwappableProgramWorkflowState(ProgramWorkflowState programWorkflowState)
      throws DAOException;

  public ProgramWorkflowState updateToNextAvailableId(ProgramWorkflowState programWorkflowState)
      throws DAOException;

  public void deleteProgramWorkflowState(ProgramWorkflowState programWorkflowState)
      throws DAOException;

  public ProgramWorkflowState findMDSPProgramWorkflowStateByUuid(String uuid) throws DAOException;

  public ProgramWorkflow getProgramWorkflow(
      ProgramWorkflowState programWorkflowState, boolean isFromMetadata) throws DAOException;

  public Integer getConceptId(ProgramWorkflowState programWorkflowState, boolean isFromMetadata)
      throws DAOException;

  public String getConceptName(ProgramWorkflowState programWorkflowState, boolean isFromMetadata)
      throws DAOException;

  public String getConceptName(Integer conceptId) throws DAOException;
}
