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

import java.util.List;
import java.util.Map;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.eptsharmonization.api.model.ProgramWorkflowStateDTO;

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
public interface HarmonizationProgramWorkflowStateService extends OpenmrsService {

  public List<ProgramWorkflowStateDTO> findAllMDSStatesNotContainedInPDS() throws APIException;

  public List<ProgramWorkflowStateDTO> findAllPDSStatesNotContainedInMDS() throws APIException;

  public Map<String, List<ProgramWorkflowStateDTO>>
      findAllStatesWithDifferentWorkflowOrConceptAndSameUUIDAndID() throws APIException;

  public Map<String, List<ProgramWorkflowStateDTO>> findAllStatesWithDifferentIDAndSameUUID()
      throws APIException;

  public List<ProgramWorkflowState> findAllNotSwappableProgramWorkflowStates() throws APIException;

  public List<ProgramWorkflowState> findAllSwappableProgramWorkflowStates() throws APIException;

  public int getNumberOfAffectedConceptStateConversions(
      ProgramWorkflowStateDTO programWorkflowStateDTO);

  public int getNumberOfAffectedPatientStates(ProgramWorkflowStateDTO programWorkflowStateDTO);

  public void updateStatesWithDifferentWorkflowsOrConcept(
      Map<String, List<ProgramWorkflowStateDTO>> programWorkflowStates) throws APIException;

  public void saveNewProgramWorkflowStatesFromMDS(
      List<ProgramWorkflowStateDTO> programWorkflowStates) throws APIException;

  public void saveStatesWithDifferentIDAndEqualUUID(
      Map<String, List<ProgramWorkflowStateDTO>> programWorkflowStates) throws APIException;

  public void saveManualMapping(
      Map<ProgramWorkflowStateDTO, ProgramWorkflowStateDTO> manualHarmonizeProgramWorkflowStates)
      throws APIException;

  public void deleteNewProgramWorkflowStatesFromPDS(
      List<ProgramWorkflowStateDTO> programWorkflowStates) throws APIException;

  public ProgramWorkflowState findMDSProgramWorkflowStateByUuid(String uuid) throws APIException;

  public ProgramWorkflowState findPDSProgramWorkflowStateByUuid(String uuid) throws APIException;

  public List<ProgramWorkflowState> findAllMDSProgramWorkflowStates() throws APIException;

  public void setProgramWorkflowAndConcept(
      List<ProgramWorkflowStateDTO> programWorkflowStateDTO, boolean isFromMetadata)
      throws APIException;

  public ProgramWorkflow getProgramWorkflow(
      ProgramWorkflowState programWorkflowState, boolean isFromMetadata) throws APIException;

  public Integer getProgramWorkflowStateConceptId(
      ProgramWorkflowState programWorkflowState, boolean isFromMetadata) throws APIException;
}
