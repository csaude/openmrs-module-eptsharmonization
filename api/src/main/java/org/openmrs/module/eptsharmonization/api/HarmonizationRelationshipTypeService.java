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
import org.openmrs.RelationshipType;
import org.openmrs.api.APIException;
import org.openmrs.module.eptsharmonization.api.model.RelationshipTypeDTO;

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
public interface HarmonizationRelationshipTypeService extends HarmonizationService {
  List<RelationshipTypeDTO> findAllMetadataRelationshipTypesNotSharingUuidWithAnyFromProduction()
      throws APIException;

  List<RelationshipTypeDTO> findAllMetadataRelationshipTypesNotInHarmonyWithProduction()
      throws APIException;

  List<RelationshipTypeDTO> findAllProductionRelationshipTypesNotSharingUuidWithAnyFromMetadata()
      throws APIException;

  List<RelationshipTypeDTO>
      findAllUselessProductionRelationshipTypesNotSharingUuidWithAnyFromMetadata()
          throws APIException;

  Map<RelationshipTypeDTO, Integer>
      findAllUsedProductionRelationshipTypesNotSharingUuidWithAnyFromMetadata() throws APIException;

  Map<String, List<RelationshipTypeDTO>> findAllRelationshipTypeWithDifferentTypesAndSameUUIDAndID()
      throws APIException;

  Map<String, List<RelationshipTypeDTO>> findAllRelationshipTypesWithDifferentIDAndSameUUID()
      throws APIException;

  RelationshipType findMDSRelationshipTypeByUuid(String uuid) throws APIException;

  RelationshipType findPDSRelationshipTypeByUuid(String uuid) throws APIException;

  int getNumberOfAffectedRelationships(RelationshipTypeDTO relationshipTypeDTO);

  void saveRelationshipTypesWithDifferentNames(
      Map<String, List<RelationshipTypeDTO>> relationshipTypes) throws APIException;

  void saveNewRelationshipTypeFromMetadata(RelationshipTypeDTO relationshipTypes)
      throws APIException;

  void saveNewRelationshipTypesFromMetadata(List<RelationshipTypeDTO> relationshipTypeDTO)
      throws APIException;

  void updateRelationshipTypesFromProductionWithSameUuidWithInformationFromMetadata(
      Map<String, List<RelationshipTypeDTO>> relationshipTypes) throws APIException;

  void replacePDSRelationshipTypesWithSameUuidWithThoseFromMDS(
      Map<String, List<RelationshipTypeDTO>> relationshipTypesDTO) throws APIException;

  void deleteRelationshipTypesFromProduction(List<RelationshipTypeDTO> relationshipTypeDTOS)
      throws APIException;

  void saveManualRelationshipTypeMappings(
      Map<RelationshipType, RelationshipType> manualRelationshipTypeMappings) throws APIException;
}
