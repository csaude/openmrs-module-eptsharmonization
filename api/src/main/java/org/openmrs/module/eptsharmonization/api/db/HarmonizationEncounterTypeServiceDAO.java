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
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.eptsharmonization.api.HarmonizationEncounterTypeService;

/** Database methods for {@link HarmonizationEncounterTypeService}. */
public interface HarmonizationEncounterTypeServiceDAO {

  public List<EncounterType> findAllMetadataServerEncounterTypes() throws DAOException;

  public List<EncounterType> findAllProductionServerEncounterTypes() throws DAOException;

  public List<Encounter> findEncontersByEncounterTypeId(Integer encounterTypeId)
      throws DAOException;

  public List<Form> findFormsByEncounterTypeId(Integer encounterTypeId) throws DAOException;

  public List<EncounterType> findPDSEncounterTypesNotExistsInMDServer() throws DAOException;

  public EncounterType getEncounterTypeById(Integer encounterTypeId) throws DAOException;

  public boolean isSwappable(EncounterType encounterType) throws DAOException;

  public List<EncounterType> findAllSwappable() throws DAOException;

  public List<EncounterType> findAllNotSwappable() throws DAOException;

  public EncounterType updateEncounterType(
      Integer nextId, EncounterType encounterType, boolean swappable) throws DAOException;

  public void updateEncounter(Encounter encounter, Integer encounterTypeId) throws DAOException;

  public void updateForm(Form form, Integer encounterTypeId) throws DAOException;

  public void saveNotSwappableEncounterType(EncounterType encounterType) throws DAOException;

  public EncounterType updateToNextAvailableId(EncounterType encounterType) throws DAOException;

  public void deleteEncounterType(EncounterType encounterType) throws DAOException;
}
