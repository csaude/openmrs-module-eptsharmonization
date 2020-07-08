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
package org.openmrs.module.eptsharmonization.api.db;

import java.util.List;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.eptsharmonization.api.HarmonizationPatientIdentifierTypeService;

/** Database methods for {@link HarmonizationPatientIdentifierTypeService}. */
public interface HarmonizationPatientIdentifierTypeServiceDAO {

  public List<PatientIdentifierType> findAllMDSServerPatientIdentifierTypes() throws DAOException;

  public List<PatientIdentifierType> findAllPDSServerPatientIdentifierTypes() throws DAOException;

  public List<PatientIdentifier> findPatientIdentifiersByPatientIdentifierTypeId(
      Integer patientIdentifierTypeId) throws DAOException;

  public PatientIdentifierType getPatientIdentifierTypeById(Integer patientIdentifierTypeId)
      throws DAOException;

  public boolean isSwappable(PatientIdentifierType patientIdentifierType) throws DAOException;

  public List<PatientIdentifierType> findAllSwappable() throws DAOException;

  public List<PatientIdentifierType> findAllNotSwappable() throws DAOException;

  public PatientIdentifierType updatePatientIdentifierType(
      Integer nextId, PatientIdentifierType patientIdentifierType, boolean swappable)
      throws DAOException;

  public void updatePatientIdentifier(
      PatientIdentifier patientIdentifier, Integer patientIdentifierTypeId) throws DAOException;

  public void saveNotSwappablePatientIdentifierType(PatientIdentifierType patientIdentifierType)
      throws DAOException;

  public PatientIdentifierType updateToNextAvailableId(PatientIdentifierType patientIdentifierType)
      throws DAOException;

  public void deletePatientIdentifierType(PatientIdentifierType patientIdentifierType)
      throws DAOException;

  public PatientIdentifierType findMDSPatientIdentifierTypeByUuid(String uuid) throws DAOException;
}
