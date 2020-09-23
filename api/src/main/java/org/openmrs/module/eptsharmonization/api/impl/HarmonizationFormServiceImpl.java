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
package org.openmrs.module.eptsharmonization.api.impl;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.FormResource;
import org.openmrs.api.APIException;
import org.openmrs.api.FormService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationFormService;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationFormServiceDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.openmrs.module.eptsharmonization.api.exception.UUIDDuplicationException;
import org.openmrs.module.eptsharmonization.api.model.FormDTO;
import org.openmrs.module.eptsharmonization.api.model.FormFilter;
import org.openmrs.module.eptsharmonization.api.model.HtmlForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** It is a default implementation of {@link HarmonizationFormService}. */
@Transactional
@Service("eptsharmonization.harmonizationFormService")
public class HarmonizationFormServiceImpl extends BaseOpenmrsService
    implements HarmonizationFormService {

  private HarmonizationServiceDAO harmonizationDAO;
  private FormService formService;
  private HarmonizationFormServiceDAO harmonizationFormServiceDAO;

  @Autowired
  public HarmonizationFormServiceImpl(
      FormService formService,
      HarmonizationServiceDAO harmonizationDAO,
      HarmonizationFormServiceDAO harmonizationFormServiceDAO) {
    this.formService = formService;
    this.harmonizationDAO = harmonizationDAO;
    this.harmonizationFormServiceDAO = harmonizationFormServiceDAO;
  }

  @Override
  public List<Form> findAllFormsFromMetadataServer() throws APIException {
    return this.harmonizationFormServiceDAO.findAllMetadataServerForms();
  }

  @Override
  @Transactional(readOnly = true)
  public List<FormDTO> findAllMetadataFormsNotContainedInProductionServer() throws APIException {
    this.harmonizationDAO.evictCache();
    List<Form> mdsForms = harmonizationFormServiceDAO.findAllMetadataServerForms();
    List<Form> pdsForms = harmonizationFormServiceDAO.findAllProductionServerForms();
    mdsForms.removeAll(pdsForms);
    return DTOUtils.fromForms(mdsForms);
  }

  @Override
  @Transactional(readOnly = true)
  public List<FormDTO> findAllProductionFormsNotContainedInMetadataServer() throws APIException {
    this.harmonizationDAO.evictCache();
    List<Form> pdsForms = harmonizationFormServiceDAO.findAllProductionServerForms();
    List<Form> mdsForms = harmonizationFormServiceDAO.findAllMetadataServerForms();
    pdsForms.removeAll(mdsForms);
    return DTOUtils.fromForms(pdsForms);
  }

  @Override
  public Map<String, List<FormDTO>> findAllFormsWithDifferentNameAndSameUUIDAndID()
      throws APIException {
    this.harmonizationDAO.evictCache();
    Map<String, List<FormDTO>> result = new HashMap<>();
    Map<String, List<Form>> map = findByWithDifferentNameAndSameUUIDAndID();
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromForms(map.get(key)));
    }
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, List<FormDTO>> findAllFormsWithDifferentIDAndSameUUID() throws APIException {
    this.harmonizationDAO.evictCache();
    Map<String, List<FormDTO>> result = new HashMap<>();
    Map<String, List<Form>> map = findByWithDifferentIDAndSameUUID();
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromForms(map.get(key)));
    }
    return result;
  }

  @Override
  public List<FormDTO> findUnusedProductionServerForms() {
    return DTOUtils.fromForms(this.harmonizationFormServiceDAO.findNotUsedPDSForms());
  }

  @Override
  public Form findRelatedFormMetadataFromTablMDSForm(Form form) {
    return this.harmonizationFormServiceDAO.setRelatedFormMetadataFromTablMDSForm(form);
  }

  @Override
  public Form findRelatedFormMetadataFromTableForm(Form form) {
    return this.harmonizationFormServiceDAO.setRelatedFormMetadataFromTableForm(form);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Form> findAllNotSwappableForms() throws APIException {
    this.harmonizationDAO.evictCache();
    return this.harmonizationFormServiceDAO.findAllNotSwappable();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Form> findAllSwappableForms() throws APIException {
    this.harmonizationDAO.evictCache();
    return this.harmonizationFormServiceDAO.findAllSwappable();
  }

  @Override
  public List<Form> findMDSFormsWithoutEncountersReferencesInPDServer() {
    this.harmonizationDAO.evictCache();
    return this.harmonizationFormServiceDAO.findMDSFormsWithoutEncountersReferencesInPDServer();
  }

  @Override
  @Transactional(readOnly = true)
  public int getNumberOfAffectedEncounters(Form form) {
    this.harmonizationDAO.evictCache();
    return this.harmonizationFormServiceDAO.findEncountersByForm(form).size();
  }

  @Override
  public Map<String, List<HtmlForm>> findHtmlFormWithDifferentFormAndEqualUuid() {
    List<HtmlForm> mdsForm =
        this.harmonizationFormServiceDAO.findHtmlFormMDSWithDifferentFormAndEqualUuidFromPDS();
    List<HtmlForm> pdsForm =
        this.harmonizationFormServiceDAO.findHtmlFormPDSWithDifferentFormAndEqualUuidFromMDS();

    Map<String, List<HtmlForm>> map = new TreeMap<>();
    for (HtmlForm mdsItem : mdsForm) {
      for (HtmlForm pdsItem : pdsForm) {
        if (mdsItem.getUuid().equals(pdsItem.getUuid())) {
          map.put(mdsItem.getUuid(), Arrays.asList(mdsItem, pdsItem));
        }
      }
    }
    return map;
  }

  @Override
  public List<HtmlForm> findHtmlFormMetadataServerNotPresentInProductionServer() {
    return this.harmonizationFormServiceDAO.findHtmlMDSNotPresentInPDS();
  }

  @Override
  public void saveFormsWithDifferentNames(Map<String, List<FormDTO>> forms) throws APIException {
    this.harmonizationDAO.evictCache();
    for (String key : forms.keySet()) {
      List<FormDTO> list = forms.get(key);
      FormDTO mdsForm = list.get(0);
      FormDTO pdsForm = list.get(1);
      Form form = this.formService.getForm(pdsForm.getForm().getId());
      form.setName(mdsForm.getForm().getName());
      form.setDescription(mdsForm.getForm().getDescription());
      form.setEncounterType(mdsForm.getForm().getEncounterType());
      this.formService.saveForm(form);
    }
  }

  @Override
  public void saveNewFormsFromMDS(List<FormDTO> forms) throws APIException {
    this.harmonizationDAO.evictCache();
    try {
      this.harmonizationDAO.setDisabledCheckConstraints();
      for (Form form : DTOUtils.fromFormDTOs(forms)) {
        Form found = this.formService.getForm(form.getId());

        if (found != null) {

          if (!this.harmonizationFormServiceDAO.isSwappable(found)) {
            throw new APIException(
                String.format(
                    "Cannot Insert Form with ID %s, UUID %s and NAME %s. This ID is being in use by another Fomr from Metatada server with UUID %s and name %s ",
                    form.getId(),
                    form.getUuid(),
                    form.getName(),
                    found.getUuid(),
                    found.getName()));
          }
          Form updated = this.harmonizationFormServiceDAO.updateToNextAvailableId(found);
          this.updateRelatedMetadata(found, updated);
        }
        this.harmonizationFormServiceDAO.saveNotSwappableForm(form);
      }
    } catch (Exception e) {
      throw new APIException(e.getMessage(), e);
    } finally {
      try {
        this.harmonizationDAO.setEnableCheckConstraints();
      } catch (Exception e) {
        throw new APIException(e.getMessage(), e);
      }
    }
  }

  @Override
  public void saveFormsWithDifferentIDAndEqualUUID(Map<String, List<FormDTO>> forms)
      throws APIException {

    this.harmonizationDAO.evictCache();
    try {

      this.harmonizationDAO.setDisabledCheckConstraints();
      for (String uuid : forms.keySet()) {

        List<FormDTO> list = forms.get(uuid);
        Form mdsForm = list.get(0).getForm();
        Form pdSForm = list.get(1).getForm();
        Integer mdServerFormId = mdsForm.getFormId();

        Form foundMDS = this.harmonizationFormServiceDAO.findFormById(mdsForm.getId());

        if (foundMDS != null) {
          if (!this.harmonizationFormServiceDAO.isSwappable(foundMDS)) {
            throw new APIException(
                String.format(
                    "Cannot update the Production Server Form [ID = {%s}, UUID = {%s}, NAME = {%s}] with the ID {%s} this new ID is already referencing an Existing Form In Metadata Server",
                    pdSForm.getId(), pdSForm.getUuid(), pdSForm.getName(), mdServerFormId));
          }
          Form updated = this.harmonizationFormServiceDAO.updateToNextAvailableId(foundMDS);
          this.updateRelatedMetadata(foundMDS, updated);
        }

        Form foundPDS = this.harmonizationFormServiceDAO.findFormById(pdSForm.getId());
        if (!this.harmonizationFormServiceDAO.isSwappable(foundPDS)) {
          throw new APIException(
              String.format(
                  "Cannot update the Production Server Form with ID {%s}, UUID {%s} and NAME {%s}. This Encounter Type is a Reference from an Form of Metadata Server",
                  foundPDS.getId(), foundPDS.getUuid(), foundPDS.getName()));
        }
        Form updated = this.harmonizationFormServiceDAO.updateForm(foundPDS, mdsForm, false);
        updateRelatedMetadata(foundPDS, updated);
      }
    } catch (Exception e) {
      throw new APIException(e.getMessage(), e);
    } finally {
      try {
        this.harmonizationDAO.setEnableCheckConstraints();
      } catch (Exception e) {
        throw new APIException(e.getMessage(), e);
      }
    }
  }

  @Override
  public void saveManualMapping(Map<Form, Form> mapForms)
      throws UUIDDuplicationException, SQLException {
    this.harmonizationDAO.evictCache();
    try {

      this.harmonizationDAO.setDisabledCheckConstraints();
      for (Entry<Form, Form> entry : mapForms.entrySet()) {

        Form pdsForm = entry.getKey();
        Form mdsForm = entry.getValue();

        Form foundMDSFormByUuid =
            this.harmonizationFormServiceDAO.findFormByUuid(mdsForm.getUuid());

        if ((foundMDSFormByUuid != null && !foundMDSFormByUuid.getId().equals(mdsForm.getId()))
            && (!foundMDSFormByUuid.getId().equals(pdsForm.getId())
                && !foundMDSFormByUuid.getUuid().equals(pdsForm.getUuid()))) {

          throw new UUIDDuplicationException(
              String.format(
                  " Cannot Update the Form '%s' to '%s'. There is one entry with NAME='%s', ID='%s' an UUID='%s' ",
                  pdsForm.getName(),
                  mdsForm.getName(),
                  foundMDSFormByUuid.getName(),
                  foundMDSFormByUuid.getId(),
                  foundMDSFormByUuid.getUuid()));
        }

        Form foundPDS = this.harmonizationFormServiceDAO.findFormById(pdsForm.getId());

        if (mdsForm.getUuid().equals(pdsForm.getUuid())
            && mdsForm.getId().equals(pdsForm.getId())
            && mdsForm.getName().equalsIgnoreCase(pdsForm.getName())) {
          return;
        } else {

          if (!foundPDS.getId().equals(mdsForm.getId())) {
            updateRelatedMetadata(foundPDS, mdsForm);
          }
          this.harmonizationFormServiceDAO.deleteForm(foundPDS);

          Form foundMDSFormByID = this.harmonizationFormServiceDAO.findFormById(mdsForm.getId());
          if (foundMDSFormByID == null) {
            this.harmonizationFormServiceDAO.saveNotSwappableForm(mdsForm);
          }
        }
      }
    } finally {
      try {
        this.harmonizationDAO.setEnableCheckConstraints();
      } catch (SQLException e) {
        throw new APIException(e.getMessage(), e);
      }
    }
  }

  @Override
  public void deleteNewFormsFromPDS(List<FormDTO> forms) throws APIException {
    this.harmonizationDAO.evictCache();
    try {
      this.harmonizationDAO.setDisabledCheckConstraints();

      for (Form form : DTOUtils.fromFormDTOs(forms)) {
        this.harmonizationFormServiceDAO.deleteRelatedEncounter(form);
        this.harmonizationFormServiceDAO.deleteRelatedFormFilter(form);
        this.harmonizationFormServiceDAO.deleteRelatedFormResource(form);
        this.harmonizationFormServiceDAO.deleteRelatedPDSHtmlForm(form);
        this.harmonizationFormServiceDAO.deleteRelatedFormField(form);
        this.harmonizationFormServiceDAO.deleteForm(form);
      }
    } catch (Exception e) {
      throw new APIException(e.getMessage(), e);
    } finally {
      try {
        this.harmonizationDAO.setEnableCheckConstraints();
      } catch (Exception e) {
        throw new APIException(e.getMessage(), e);
      }
    }
  }

  @Override
  public void saveHtmlFormsWithDifferentFormNamesAndEqualHtmlFormUuid(
      Map<String, List<HtmlForm>> data) {
    this.harmonizationDAO.evictCache();

    try {
      this.harmonizationDAO.setDisabledCheckConstraints();
      for (Entry<String, List<HtmlForm>> entry : data.entrySet()) {

        HtmlForm mdsHtmlForm = entry.getValue().get(0);
        HtmlForm pdsHtmlForm = entry.getValue().get(1);

        this.harmonizationFormServiceDAO.updateHtmlForm(pdsHtmlForm, mdsHtmlForm);
      }
    } catch (Exception e) {
      throw new APIException(e.getMessage(), e);
    } finally {
      try {
        this.harmonizationDAO.setEnableCheckConstraints();
      } catch (Exception e) {
        throw new APIException(e.getMessage(), e);
      }
    }
  }

  @Override
  public void saveNewHtmlFormsFromMetadataServer(List<HtmlForm> htmlForms) {
    this.harmonizationDAO.evictCache();

    try {
      this.harmonizationDAO.setDisabledCheckConstraints();
      for (HtmlForm htmlForm : htmlForms) {
        this.harmonizationFormServiceDAO.createHtmlFormPDS(htmlForm);
      }
    } catch (Exception e) {
      throw new APIException(e.getMessage(), e);
    } finally {
      try {
        this.harmonizationDAO.setEnableCheckConstraints();
      } catch (Exception e) {
        throw new APIException(e.getMessage(), e);
      }
    }
  }

  private Map<String, List<Form>> findByWithDifferentNameAndSameUUIDAndID() {
    List<Form> allMDS = harmonizationFormServiceDAO.findDiferrencesByNameHavingSameIdAndUuidMDS();
    List<Form> allPDS = harmonizationFormServiceDAO.findDiferrencesByNameHavingSameIdAndUuidPDS();

    Map<String, List<Form>> map = new TreeMap<>();
    for (Form mdsItem : allMDS) {
      for (Form pdsItem : allPDS) {
        if (mdsItem.getUuid().equals(pdsItem.getUuid())
            && mdsItem.getId().equals(pdsItem.getId())
            && !mdsItem.getName().equalsIgnoreCase(pdsItem.getName())) {
          map.put(mdsItem.getUuid(), Arrays.asList(mdsItem, pdsItem));
        }
      }
    }
    return map;
  }

  private Map<String, List<Form>> findByWithDifferentIDAndSameUUID() {
    List<Form> allPDS = harmonizationFormServiceDAO.findDiferrencesByIDsHavingSameUuidPDS();
    List<Form> allMDS = harmonizationFormServiceDAO.findDiferrencesByIDsHavingSameUuidMDS();
    Map<String, List<Form>> map = new TreeMap<>();
    for (Form mdsItem : allMDS) {
      for (Form pdsItem : allPDS) {
        if (mdsItem.getUuid().equals(pdsItem.getUuid())
            && !mdsItem.getId().equals(pdsItem.getId())) {
          map.put(mdsItem.getUuid(), Arrays.asList(mdsItem, pdsItem));
        }
      }
    }
    return map;
  }

  private void updateRelatedMetadata(Form pdsForm, Form updated) {
    this.harmonizationDAO.evictCache();
    List<Encounter> relatedEncounters =
        this.harmonizationFormServiceDAO.findEncountersByForm(pdsForm);
    List<FormField> relatedFormFields =
        this.harmonizationFormServiceDAO.findFormFieldsByForm(pdsForm);
    List<FormResource> relatedFormResources =
        this.harmonizationFormServiceDAO.findFormResourcesByForm(pdsForm);
    HtmlForm relatedHtmlForm = this.harmonizationFormServiceDAO.findPDSHtmlFormByForm(pdsForm);
    List<FormFilter> relatedFormFilter =
        this.harmonizationFormServiceDAO.findFormFilterByForm(pdsForm);

    for (Encounter encounter : relatedEncounters) {
      this.harmonizationFormServiceDAO.updateEncounter(encounter, updated);
    }

    for (FormField formField : relatedFormFields) {
      List<FormField> mdsRelatedFormFields =
          this.harmonizationFormServiceDAO.findFormFieldsByForm(updated);
      if (mdsRelatedFormFields.isEmpty()) {
        this.harmonizationFormServiceDAO.updateFormField(formField, updated);
      } else {
        this.harmonizationFormServiceDAO.deleteRelatedFormField(pdsForm);
      }
    }

    for (FormResource formResource : relatedFormResources) {
      this.harmonizationFormServiceDAO.updateFormResource(formResource, updated);
    }

    for (FormFilter formFilter : relatedFormFilter) {
      List<FormFilter> mdsFormFilter =
          this.harmonizationFormServiceDAO.findFormFilterByForm(updated);
      if (mdsFormFilter.isEmpty()) {
        this.harmonizationFormServiceDAO.updateFormFilter(formFilter, updated);
      } else {
        this.harmonizationFormServiceDAO.deleteRelatedFormFilter(pdsForm);
      }
    }

    if (relatedHtmlForm != null) {
      HtmlForm mdsRelatedHtmlForm = this.harmonizationFormServiceDAO.findPDSHtmlFormByForm(updated);
      if (mdsRelatedHtmlForm == null) {
        this.harmonizationFormServiceDAO.updatePDSHtmlForm(relatedHtmlForm, updated);
      } else {
        this.harmonizationFormServiceDAO.deleteRelatedPDSHtmlForm(pdsForm);
      }
    }
  }

  @Override
  public boolean isAllMetadataHarmonized() throws APIException {
    return findAllFormsWithDifferentIDAndSameUUID().isEmpty()
        && findAllFormsWithDifferentNameAndSameUUIDAndID().isEmpty()
        && findAllMetadataFormsNotContainedInProductionServer().isEmpty()
        && findAllProductionFormsNotContainedInMetadataServer().isEmpty()
        && findHtmlFormWithDifferentFormAndEqualUuid().isEmpty()
        && findHtmlFormMetadataServerNotPresentInProductionServer().isEmpty();
  }
}
