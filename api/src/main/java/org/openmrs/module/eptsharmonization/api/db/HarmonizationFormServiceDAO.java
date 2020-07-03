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
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.FormResource;
import org.openmrs.api.db.DAOException;

public interface HarmonizationFormServiceDAO {

  public List<Form> findAllMetadataServerForms() throws DAOException;

  public List<Form> findAllProductionServerForms() throws DAOException;

  public List<Form> findPDSFormsNotExistsInMDServer() throws DAOException;

  public boolean isSwappable(Form form) throws DAOException;

  public List<Form> findAllSwappable() throws DAOException;

  public List<Form> findAllNotSwappable() throws DAOException;

  public List<Encounter> findEncountersByForm(Form form) throws DAOException;

  public List<FormField> findFormFieldsByForm(Form form) throws DAOException;

  public List<FormResource> findFormResourcesByForm(Form form) throws DAOException;

  public Form updateForm(Integer nextId, Form form, boolean swappable) throws DAOException;

  public void saveNotSwappableForm(Form form) throws DAOException;

  public Form updateToNextAvailableId(Form form) throws DAOException;

  public void deleteForm(Form form) throws DAOException;
}
