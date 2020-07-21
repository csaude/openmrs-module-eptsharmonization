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
import org.openmrs.LocationTag;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.eptsharmonization.api.model.LocationTagDTO;

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
public interface HarmonizationLocationTagService extends OpenmrsService {
  List<LocationTagDTO> findAllMetadataLocationTagsNotSharingUuidWithAnyFromProduction()
      throws APIException;

  List<LocationTagDTO> findAllMetadataLocationTagsNotInHarmonyWithProduction() throws APIException;

  List<LocationTagDTO> findAllProductionLocationTagsNotSharingUuidWithAnyFromMetadata()
      throws APIException;

  List<LocationTagDTO> findAllUselessProductionLocationTagsNotSharingUuidWithAnyFromMetadata()
      throws APIException;

  Map<LocationTagDTO, Integer> findAllUsedProductionLocationTagsNotSharingUuidWithAnyFromMetadata()
      throws APIException;

  Map<String, List<LocationTagDTO>> findAllLocationTagsWithDifferentNameAndSameUUIDAndID()
      throws APIException;

  Map<String, List<LocationTagDTO>> findAllLocationTagsWithDifferentIDAndSameUUID()
      throws APIException;

  int getNumberOfAffectedLocations(LocationTagDTO locationTagDTO);

  List<LocationTagDTO> findAllProductionLocationTagsNotInHarmonyWithMetadata() throws APIException;

  LocationTag findMDSLocationTagByUuid(String uuid) throws APIException;

  LocationTag findPDSLocationTagByUuid(String uuid) throws APIException;

  void saveLocationTagsWithDifferentNames(Map<String, List<LocationTagDTO>> locationTags)
      throws APIException;

  void saveNewLocationTagFromMetadata(LocationTagDTO locationTags) throws APIException;

  void saveNewLocationTagsFromMetadata(List<LocationTagDTO> locationTagDTO) throws APIException;

  void updateLocationTagsFromProductionWithSameUuidWithInformationFromMetadata(
      Map<String, List<LocationTagDTO>> locationTags) throws APIException;

  void replacePDSLocationTagsWithSameUuidWithThoseFromMDS(
      Map<String, List<LocationTagDTO>> locationTagsDTO) throws APIException;

  void deleteLocationTagsFromProduction(List<LocationTagDTO> locationTagDTOS) throws APIException;

  void saveManualLocationTagMappings(Map<LocationTag, LocationTag> manualLocationTagMappings)
      throws APIException;
}
