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
import org.openmrs.VisitType;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.eptsharmonization.api.model.VisitTypeDTO;

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
public interface HarmonizationVisitTypeService extends OpenmrsService {
  List<VisitTypeDTO> findAllMetadataVisitTypesNotSharingUuidWithAnyFromProduction()
      throws APIException;

  List<VisitTypeDTO> findAllMetadataVisitTypesNotInHarmonyWithProduction() throws APIException;

  List<VisitTypeDTO> findAllProductionVisitTypesNotSharingUuidWithAnyFromMetadata()
      throws APIException;

  List<VisitTypeDTO> findAllUselessProductionVisitTypesNotSharingUuidWithAnyFromMetadata()
      throws APIException;

  Map<VisitTypeDTO, Integer> findAllUsedProductionVisitTypesNotSharingUuidWithAnyFromMetadata()
      throws APIException;

  Map<String, List<VisitTypeDTO>> findAllVisitTypesWithDifferentNameAndSameUUIDAndID()
      throws APIException;

  VisitType findMDSVisitTypeByUuid(String uuid) throws APIException;

  VisitType findPDSVisitTypeByUuid(String uuid) throws APIException;

  Map<String, List<VisitTypeDTO>> findAllVisitTypesWithDifferentIDAndSameUUID() throws APIException;

  int getNumberOfAffectedVisits(VisitTypeDTO visitTypeDTO);

  List<VisitTypeDTO> findAllProductionVisitTypesNotInHarmonyWithMetadata() throws APIException;

  void saveVisitTypesWithDifferentNames(Map<String, List<VisitTypeDTO>> visitTypes)
      throws APIException;

  void saveNewVisitTypeFromMetadata(VisitTypeDTO visitTypes) throws APIException;

  void saveNewVisitTypesFromMetadata(List<VisitTypeDTO> visitTypeDTO) throws APIException;

  void updateVisitTypesFromProductionWithSameUuidWithInformationFromMetadata(
      Map<String, List<VisitTypeDTO>> visitTypes) throws APIException;

  void replacePDSVisitTypesWithSameUuidWithThoseFromMDS(
      Map<String, List<VisitTypeDTO>> visitTypesDTO) throws APIException;

  void deleteVisitTypesFromProduction(List<VisitTypeDTO> visitTypeDTOS) throws APIException;

  void saveManualVisitTypeMappings(Map<VisitType, VisitType> manualVisitTypeMappings)
      throws APIException;
}
