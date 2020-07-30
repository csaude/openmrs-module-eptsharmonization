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
package org.openmrs.module.eptsharmonization.api;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.APIException;
import org.openmrs.module.eptsharmonization.api.exception.UUIDDuplicationException;
import org.openmrs.module.eptsharmonization.api.model.ProgramWorkflowDTO;

/**
 * This service exposes module's core functionality. It is a Spring managed bean which is configured
 * in moduleApplicationContext.xml.
 *
 * <p>It can be accessed only via Context:<br>
 * <code>
 * Context.getService(HarmonizationService.class).someMethod();
 * </code>
 *
 * @see org.openmrs.api.context.Context
 */
public interface HarmonizationProgramWorkflowService extends HarmonizationService {

  public List<ProgramWorkflowDTO> findAllMetadataProgramWorkflowsNotContainedInProductionServer()
      throws APIException;

  public List<ProgramWorkflowDTO> findAllProductionProgramWorkflowsNotContainedInMetadataServer()
      throws APIException;

  public Map<String, List<ProgramWorkflowDTO>>
      findAllProgramWorkflowsWithDifferentProgramOrConceptAndSameUUIDAndID() throws APIException;

  public Map<String, List<ProgramWorkflowDTO>> findAllProgramWorkflowsWithDifferentIDAndSameUUID()
      throws APIException;

  public List<ProgramWorkflow> findAllNotSwappableProgramWorkflows() throws APIException;

  public List<ProgramWorkflow> findAllSwappableProgramWorkflows() throws APIException;

  public int getNumberOfAffectedConceptStateConversions(ProgramWorkflowDTO programWorkflowDTO);

  public int getNumberOfAffectedProgramWorkflowStates(ProgramWorkflowDTO programWorkflowDTO);

  public void updateProgramWorkflowsWithDifferentProgramsOrConcept(
      Map<String, List<ProgramWorkflowDTO>> programWorkflows) throws APIException;

  public void saveNewProgramWorkflowsFromMDS(List<ProgramWorkflowDTO> programWorkflows)
      throws APIException;

  public void saveProgramWorkflowsWithDifferentIDAndEqualUUID(
      Map<String, List<ProgramWorkflowDTO>> programWorkflows) throws APIException;

  public void saveManualMapping(
      Map<ProgramWorkflowDTO, ProgramWorkflowDTO> manualHarmonizeProgramWorkflows)
      throws UUIDDuplicationException, SQLException;

  public void deleteNewProgramWorkflowsFromPDS(List<ProgramWorkflowDTO> programWorkflows)
      throws APIException;

  public ProgramWorkflow findMetadataProgramWorkflowByUuid(String uuid) throws APIException;

  public ProgramWorkflow findProductionProgramWorkflowByUuid(String uuid) throws APIException;

  public List<ProgramWorkflow> findAllMetadataProgramWorkflows() throws APIException;

  public void setProgramAndConceptNames(
      List<ProgramWorkflowDTO> programWorkflowDTO, boolean isFromMetadata) throws APIException;

  public Program getProgramWorkflowProgram(ProgramWorkflow programWorkflow, boolean isFromMetadata)
      throws APIException;

  public Integer getProgramWorkflowConceptId(
      ProgramWorkflow programWorkflow, boolean isFromMetadata) throws APIException;
}
