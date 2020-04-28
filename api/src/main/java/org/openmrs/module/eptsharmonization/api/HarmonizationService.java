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
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.eptsharmonization.api.model.EncounterTypeDTO;
import org.openmrs.module.eptsharmonization.api.model.PersonAttributeTypeDTO;
import org.springframework.transaction.annotation.Transactional;

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
@Transactional
public interface HarmonizationService extends OpenmrsService {

  // @Authorized({ "Manage Encountery Types" })
  @Transactional(readOnly = true)
  @Authorized({"View Encountery Types"})
  public List<EncounterTypeDTO> findAllMetadataEncounterNotContainedInProductionServer()
      throws APIException;

  @Transactional(readOnly = true)
  @Authorized({"View Encountery Types"})
  public List<EncounterTypeDTO> findAllProductionEncountersNotContainedInMetadataServer()
      throws APIException;

  // @Authorized({ "Manage Encountery Types" })
  @Transactional(readOnly = true)
  @Authorized({"View Encountery Types"})
  public List<EncounterTypeDTO> findAllMetadataEncounterPartialEqualsToProductionServer()
      throws APIException;

  @Transactional(readOnly = true)
  @Authorized({"View Encountery Types"})
  public List<EncounterTypeDTO> findAllProductionEncountersPartialEqualsToMetadataServer()
      throws APIException;

  @Transactional(readOnly = true)
  @Authorized({"View Person Attribute Types"})
  public List<PersonAttributeTypeDTO> findAllMetadataPersonAttributeTypesNotInProductionServer()
      throws APIException;

  @Transactional(readOnly = true)
  @Authorized({"View Person Attribute Types"})
  public List<PersonAttributeTypeDTO> findAllProductionPersonAttibuteTypesNotInMetadataServer()
      throws APIException;

  @Transactional(readOnly = true)
  @Authorized({"View Person Attribute Types"})
  public List<PersonAttributeTypeDTO>
      findAllMetadataPersonAttributeTypesPartialEqualsToProductionServer() throws APIException;

  @Transactional(readOnly = true)
  @Authorized({"View Person Attribute Types"})
  public List<PersonAttributeTypeDTO>
      findAllProductionPersonAttributeTypesPartialEqualsToMetadataServer() throws APIException;
}
