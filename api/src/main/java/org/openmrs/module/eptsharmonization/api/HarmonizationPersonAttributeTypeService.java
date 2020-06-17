package org.openmrs.module.eptsharmonization.api;

import java.util.List;
import java.util.Map;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.APIException;
import org.openmrs.module.eptsharmonization.api.model.PersonAttributeTypeDTO;

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
public interface HarmonizationPersonAttributeTypeService {

  public List<PersonAttributeTypeDTO>
      findAllMetadataPersonAttributeTypesNotContainedInProductionServer() throws APIException;

  public List<PersonAttributeTypeDTO>
      findAllProductionPersonAttributeTypesNotContainedInMetadataServer() throws APIException;

  public List<PersonAttributeTypeDTO>
      findAllMetadataPersonAttributeTypesPartialEqualsToProductionServer() throws APIException;

  public List<PersonAttributeTypeDTO>
      findAllProductionPersonAttributeTypesPartialEqualsToMetadataServer() throws APIException;

  public Map<String, List<PersonAttributeTypeDTO>>
      findAllPersonAttributeTypesWithDifferentNameAndSameUUIDAndID() throws APIException;

  public Map<String, List<PersonAttributeTypeDTO>>
      findAllPersonAttributeTypesWithDifferentIDAndSameUUID() throws APIException;

  public List<PersonAttributeType> findAllNotSwappablePersonAttributeTypes() throws APIException;

  public List<PersonAttributeType> findAllSwappablePersonAttributeTypes() throws APIException;

  public int getNumberOfAffectedPersonAttributes(PersonAttributeTypeDTO personAttributeTypeDTO);

  public List<PersonAttributeType> findPDSPersonAttributeTypesNotExistsInMDServer()
      throws APIException;

  public void savePersonAttributeTypesWithDifferentNames(
      Map<String, List<PersonAttributeTypeDTO>> personAttributeTypes) throws APIException;

  public void saveNewPersonAttributeTypesFromMDS(List<PersonAttributeTypeDTO> personAttributeTypes)
      throws APIException;

  public void savePersonAttributeTypesWithDifferentIDAndEqualUUID(
      Map<String, List<PersonAttributeTypeDTO>> personAttributeTypes) throws APIException;

  public void saveManualMapping(Map<PersonAttributeType, PersonAttributeType> personAttributeTypes)
      throws APIException;

  public void deleteNewPersonAttributeTypesFromPDS(
      List<PersonAttributeTypeDTO> personAttributeTypes) throws APIException;
}
