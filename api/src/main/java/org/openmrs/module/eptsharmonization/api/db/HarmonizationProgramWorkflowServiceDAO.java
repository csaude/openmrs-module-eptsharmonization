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
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.eptsharmonization.api.HarmonizationProgramWorkflowService;

/** Database methods for {@link HarmonizationProgramWorkflowService}. */
public interface HarmonizationProgramWorkflowServiceDAO {

  public List<ProgramWorkflow> findAllMetadataServerProgramWorkflows() throws DAOException;

  public List<ProgramWorkflow> findAllProductionServerProgramWorkflows() throws DAOException;

  public List<ProgramWorkflowState> findProgramWorkflowStatesByProgramWorkflowId(
      Integer programWorkflowId) throws DAOException;

  public List<ConceptStateConversion> findConceptStateConversionsByProgramWorkflowId(
      Integer programWorkflowId) throws DAOException;

  public List<ProgramWorkflow> findPDSProgramWorkflowsNotExistsInMDServer() throws DAOException;

  public ProgramWorkflow getProgramWorkflowById(Integer programWorkflowId) throws DAOException;

  public boolean isSwappable(ProgramWorkflow programWorkflow) throws DAOException;

  public List<ProgramWorkflow> findAllSwappable() throws DAOException;

  public List<ProgramWorkflow> findAllNotSwappable() throws DAOException;

  public ProgramWorkflow updateProgramWorkflow(
      Integer nextId, ProgramWorkflow programWorkflow, boolean swappable) throws DAOException;

  public void updateConceptStateConversion(
      ConceptStateConversion coceptStateConversion, Integer programWorkflowId) throws DAOException;

  public void updateProgramWorkflowState(
      ProgramWorkflowState programWorkflowState, Integer programWorkflowId) throws DAOException;

  public void saveNotSwappableProgramWorkflow(ProgramWorkflow programWorkflow) throws DAOException;

  public ProgramWorkflow updateToNextAvailableId(ProgramWorkflow programWorkflow)
      throws DAOException;

  public void deleteProgramWorkflow(ProgramWorkflow programWorkflow) throws DAOException;

  public ProgramWorkflow findMDSPProgramWorkflowByUuid(String uuid) throws DAOException;

  public String getProgramWorkflowProgramName(ProgramWorkflow programWorkflow) throws DAOException;

  public String getProgramWorkflowConceptName(ProgramWorkflow programWorkflow) throws DAOException;
}
