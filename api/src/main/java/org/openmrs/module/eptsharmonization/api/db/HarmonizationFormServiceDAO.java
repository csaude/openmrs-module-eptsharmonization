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
import org.openmrs.module.eptsharmonization.api.model.FormFilter;
import org.openmrs.module.eptsharmonization.api.model.FormentryXsn;
import org.openmrs.module.eptsharmonization.api.model.HtmlForm;

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

  public List<FormFilter> findFormFilterByForm(Form form) throws DAOException;

  public List<FormentryXsn> findFormentryXsnByForm(Form form) throws DAOException;

  public HtmlForm findPDSHtmlFormByForm(Form form) throws DAOException;

  public Form updateForm(Form pdsForm, Form mdsForm, boolean swappable) throws DAOException;

  public List<Form> findMDSFormsWithoutEncountersReferencesInPDServer();

  public List<HtmlForm> findHtmlFormMDSWithDifferentFormAndEqualUuidFromPDS();

  public List<HtmlForm> findHtmlFormPDSWithDifferentFormAndEqualUuidFromMDS();

  public List<HtmlForm> findHtmlMDSNotPresentInPDS();

  public List<Form> findUsedPDSForms();

  public List<Form> findNotUsedPDSForms();

  public void saveNotSwappableForm(Form form) throws DAOException;

  public Form findFormById(Integer formId);

  public Form findFormByUuid(String uuid);

  public Form updateToNextAvailableId(Form form) throws DAOException;

  public void deleteForm(Form form) throws DAOException;

  public void deleteRelatedPDSHtmlForm(Form form) throws DAOException;

  public void deleteRelatedEncounter(Form form) throws DAOException;

  public void deleteRelatedFormFilter(Form form) throws DAOException;

  public void deleteRelatedFormResource(Form form) throws DAOException;

  public void deleteRelatedFormentryXsn(Form form) throws DAOException;

  public void deleteRelatedFormField(Form form) throws DAOException;

  public void updateEncounter(Encounter encounter, Form form) throws DAOException;

  public void updateFormField(FormField formField, Form form) throws DAOException;

  public void updateFormResource(FormResource formResource, Form form) throws DAOException;

  public void updateFormentryxsn(FormentryXsn formentryXsn, Form form) throws DAOException;

  public void updateFormFilter(FormFilter formFilter, Form form) throws DAOException;

  public void updatePDSHtmlForm(HtmlForm htmlForm, Form form) throws DAOException;

  public List<Form> findDiferrencesByIDsHavingSameUuidMDS() throws DAOException;

  public List<Form> findDiferrencesByIDsHavingSameUuidPDS() throws DAOException;

  public List<Form> findDiferrencesByNameHavingSameIdAndUuidMDS() throws DAOException;

  public List<Form> findDiferrencesByNameHavingSameIdAndUuidPDS() throws DAOException;

  public Form setRelatedFormMetadataFromTablMDSForm(Form form);

  public Form setRelatedFormMetadataFromTableForm(Form form);

  public void createHtmlFormPDS(HtmlForm htmlForm) throws DAOException;

  public void updateHtmlForm(HtmlForm pdsHtmlForm, HtmlForm mdsHtmlForm) throws DAOException;
}
