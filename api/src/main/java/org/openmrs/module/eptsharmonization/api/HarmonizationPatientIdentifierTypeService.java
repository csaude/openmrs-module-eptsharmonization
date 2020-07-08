package org.openmrs.module.eptsharmonization.api;

import java.util.List;
import java.util.Map;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.APIException;
import org.openmrs.module.eptsharmonization.api.model.PatientIdentifierTypeDTO;

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
public interface HarmonizationPatientIdentifierTypeService {

  public List<PatientIdentifierTypeDTO> findAllFromMDSNotContainedInPDS() throws APIException;

  public List<PatientIdentifierTypeDTO> findAllFromPDSNotContainedInMDS() throws APIException;

  public Map<String, List<PatientIdentifierTypeDTO>> findAllWithDifferentNameAndSameUUIDAndID()
      throws APIException;

  public Map<String, List<PatientIdentifierTypeDTO>> findAllWithDifferentIDAndSameUUID()
      throws APIException;

  public List<PatientIdentifierType> findAllNotSwappable() throws APIException;

  public List<PatientIdentifierType> findAllSwappable() throws APIException;

  public List<PatientIdentifierType> findAllFromMDS() throws APIException;

  public int getNumberOfAffectedPatientIdentifiers(
      PatientIdentifierTypeDTO patientIdentifierTypeDTO);

  public void saveWithDifferentNames(
      Map<String, List<PatientIdentifierTypeDTO>> patientIdentifierTypes) throws APIException;

  public void saveNewFromMDS(List<PatientIdentifierTypeDTO> patientIdentifierTypes)
      throws APIException;

  public void saveWithDifferentIDAndEqualUUID(
      Map<String, List<PatientIdentifierTypeDTO>> patientIdentifierTypes) throws APIException;

  public void saveManualMapping(
      Map<PatientIdentifierType, PatientIdentifierType> patientIdentifierTypes) throws APIException;

  public void deleteNewFromPDS(List<PatientIdentifierTypeDTO> patientIdentifierTypes)
      throws APIException;

  public PatientIdentifierType findFromPDSByUuid(String uuid) throws APIException;

  public PatientIdentifierType findFromMDSByUuid(String uuid) throws APIException;
}
