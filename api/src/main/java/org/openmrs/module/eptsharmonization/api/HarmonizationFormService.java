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

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.openmrs.Form;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.eptsharmonization.api.exception.UUIDDuplicationException;
import org.openmrs.module.eptsharmonization.api.model.FormDTO;
import org.openmrs.module.eptsharmonization.api.model.HtmlForm;

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
public interface HarmonizationFormService extends OpenmrsService {

  public List<FormDTO> findAllMetadataFormsNotContainedInProductionServer() throws APIException;

  public List<FormDTO> findAllProductionFormsNotContainedInMetadataServer() throws APIException;

  public List<Form> findAllFormsFromMetadataServer() throws APIException;

  public Map<String, List<FormDTO>> findAllFormsWithDifferentNameAndSameUUIDAndID()
      throws APIException;

  public Map<String, List<FormDTO>> findAllFormsWithDifferentIDAndSameUUID() throws APIException;

  public List<Form> findAllNotSwappableForms() throws APIException;

  public List<Form> findAllSwappableForms() throws APIException;

  public int getNumberOfAffectedEncounters(Form form);

  public int getNumberOfAffectedFormFields(Form form);

  public int getNumberOfAffectedFormResourses(Form form);

  public int getNumberOfAffectedFormFilters(Form form);

  public int getNumberOfAffectedFormEntryXsn(Form form);

  public Form findRelatedFormMetadataFromTablMDSForm(Form form);

  public Form findRelatedFormMetadataFromTableForm(Form form);

  public List<Form> findMDSFormsWithoutEncountersReferencesInPDServer();

  public List<FormDTO> findUnusedProductionServerForms();

  public void saveFormsWithDifferentNames(Map<String, List<FormDTO>> forms) throws APIException;

  public void saveNewFormsFromMDS(List<FormDTO> forms) throws APIException;

  public void saveFormsWithDifferentIDAndEqualUUID(Map<String, List<FormDTO>> forms)
      throws APIException;

  public void saveManualMapping(Map<Form, Form> forms)
      throws UUIDDuplicationException, SQLException;

  public void deleteNewFormsFromPDS(List<FormDTO> forms) throws APIException;

  public Map<String, List<HtmlForm>> findHtmlFormWithDifferentFormAndEqualUuid();

  public List<HtmlForm> findHtmlFormMetadataServerNotPresentInProductionServer();

  public void saveHtmlFormsWithDifferentFormNamesAndEqualHtmlFormUuid(
      Map<String, List<HtmlForm>> data);

  public void saveNewHtmlFormsFromMetadataServer(List<HtmlForm> htmlForms);
}
