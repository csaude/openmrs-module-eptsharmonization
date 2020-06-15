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
import org.openmrs.EncounterType;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.eptsharmonization.api.model.EncounterTypeDTO;

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
public interface HarmonizationEncounterTypeService extends OpenmrsService {

  public List<EncounterTypeDTO> findAllMetadataEncounterNotContainedInProductionServer()
      throws APIException;

  public List<EncounterTypeDTO> findAllProductionEncountersNotContainedInMetadataServer()
      throws APIException;

  public List<EncounterTypeDTO> findAllMetadataEncounterPartialEqualsToProductionServer()
      throws APIException;

  public List<EncounterTypeDTO> findAllProductionEncountersPartialEqualsToMetadataServer()
      throws APIException;

  public Map<String, List<EncounterTypeDTO>>
      findAllEncounterTypesWithDifferentNameAndSameUUIDAndID() throws APIException;

  public Map<String, List<EncounterTypeDTO>> findAllEncounterTypesWithDifferentIDAndSameUUID()
      throws APIException;

  public List<EncounterType> findAllNotSwappableEncounterTypes() throws APIException;

  public List<EncounterType> findAllSwappableEncounterTypes() throws APIException;

  public int getNumberOfAffectedEncounters(EncounterTypeDTO encounterTypeDTO);

  public int getNumberOfAffectedForms(EncounterTypeDTO encounterTypeDTO);

  public List<EncounterType> findPDSEncounterTypesNotExistsInMDServer() throws APIException;

  public void saveEncounterTypesWithDifferentNames(
      Map<String, List<EncounterTypeDTO>> encounterTypes) throws APIException;

  public void saveNewEncounterTypesFromMDS(List<EncounterTypeDTO> encounterTypes)
      throws APIException;

  public void saveEncounterTypesWithDifferentIDAndEqualUUID(
      Map<String, List<EncounterTypeDTO>> encounterTypes) throws APIException;

  public void saveManualMapping(Map<EncounterType, EncounterType> encounterTypes)
      throws APIException;

  public void deleteNewEncounterTypesFromPDS(List<EncounterTypeDTO> encounterTypes)
      throws APIException;
}
