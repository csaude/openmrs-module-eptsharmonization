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
import org.openmrs.LocationAttributeType;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.eptsharmonization.api.model.LocationAttributeTypeDTO;

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
public interface HarmonizationLocationAttributeTypeService extends OpenmrsService {
  List<LocationAttributeTypeDTO>
      findAllMetadataLocationAttributeTypesNotSharingUuidWithAnyFromProduction()
          throws APIException;

  List<LocationAttributeTypeDTO> findAllMetadataLocationAttributeTypesNotInHarmonyWithProduction()
      throws APIException;

  List<LocationAttributeTypeDTO>
      findAllProductionLocationAttributeTypesNotSharingUuidWithAnyFromMetadata()
          throws APIException;

  List<LocationAttributeTypeDTO>
      findAllUselessProductionLocationAttributeTypesNotSharingUuidWithAnyFromMetadata()
          throws APIException;

  Map<LocationAttributeTypeDTO, Integer>
      findAllUsedProductionLocationAttributeTypesNotSharingUuidWithAnyFromMetadata()
          throws APIException;

  Map<String, List<LocationAttributeTypeDTO>>
      findAllLocationAttributeTypesWithDifferentNameAndSameUUIDAndID() throws APIException;

  Map<String, List<LocationAttributeTypeDTO>>
      findAllLocationAttributeTypesWithDifferentIDAndSameUUID() throws APIException;

  int getNumberOfAffectedLocationAttributes(LocationAttributeTypeDTO locationAttributeTypeDTO);

  List<LocationAttributeTypeDTO> findAllProductionLocationAttributeTypesNotInHarmonyWithMetadata()
      throws APIException;

  LocationAttributeType findMDSLocationAttributeTypeByUuid(String uuid) throws APIException;

  LocationAttributeType findPDSLocationAttributeTypeByUuid(String uuid) throws APIException;

  void saveLocationAttributeTypesWithDifferentDetails(
      Map<String, List<LocationAttributeTypeDTO>> locationAttributeTypes) throws APIException;

  void saveNewLocationAttributeTypeFromMetadata(LocationAttributeTypeDTO locationAttributeTypes)
      throws APIException;

  void saveNewLocationAttributeTypesFromMetadata(
      List<LocationAttributeTypeDTO> locationAttributeTypeDTO) throws APIException;

  void updateLocationAttributeTypesFromProductionWithSameUuidWithInformationFromMetadata(
      Map<String, List<LocationAttributeTypeDTO>> locationAttributeTypes) throws APIException;

  void replacePDSLocationAttributeTypesWithSameUuidWithThoseFromMDS(
      Map<String, List<LocationAttributeTypeDTO>> locationAttributeTypesDTO) throws APIException;

  void deleteLocationAttributeTypesFromProduction(
      List<LocationAttributeTypeDTO> locationAttributeTypeDTOS) throws APIException;

  void saveManualLocationAttributeTypeMappings(
      Map<LocationAttributeType, LocationAttributeType> manualLocationAttributeTypeMappings)
      throws APIException;
}
