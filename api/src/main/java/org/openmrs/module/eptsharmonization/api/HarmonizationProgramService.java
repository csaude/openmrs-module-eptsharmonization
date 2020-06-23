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
import org.openmrs.Program;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.eptsharmonization.api.model.ProgramDTO;

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
public interface HarmonizationProgramService extends OpenmrsService {

  public List<ProgramDTO> findAllMetadataProgramsNotContainedInProductionServer()
      throws APIException;

  public List<ProgramDTO> findAllProductionProgramsNotContainedInMetadataServer()
      throws APIException;

  public List<ProgramDTO> findAllMetadataProgramsPartialEqualsToProductionServer()
      throws APIException;

  public List<ProgramDTO> findAllProductionProgramsPartialEqualsToMetadataServer()
      throws APIException;

  public Map<String, List<ProgramDTO>> findAllProgramsWithDifferentNameAndSameUUIDAndID()
      throws APIException;

  public Map<String, List<ProgramDTO>> findAllProgramsWithDifferentIDAndSameUUID()
      throws APIException;

  public List<Program> findAllNotSwappablePrograms() throws APIException;

  public List<Program> findAllSwappablePrograms() throws APIException;

  public int getNumberOfAffectedPatientPrograms(ProgramDTO programDTO);

  public int getNumberOfAffectedProgramWorkflow(ProgramDTO programDTO);

  public List<Program> findPDSProgramsNotExistsInMDServer() throws APIException;

  public void saveProgramsWithDifferentNames(Map<String, List<ProgramDTO>> programs)
      throws APIException;

  public void saveNewProgramsFromMDS(List<ProgramDTO> programs) throws APIException;

  public void saveProgramsWithDifferentIDAndEqualUUID(Map<String, List<ProgramDTO>> programs)
      throws APIException;

  public void saveManualMapping(Map<Program, Program> programs) throws APIException;

  public void deleteNewProgramsFromPDS(List<ProgramDTO> programs) throws APIException;

  public Program findMetadataProgramByUuid(String uuid) throws APIException;

  public Program findProductionProgramByUuid(String uuid) throws APIException;

  public List<Program> findAllMetadataPrograms() throws APIException;
}
