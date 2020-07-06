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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.hibernate.ObjectNotFoundException;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.FormResource;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationFormService;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationFormServiceDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.openmrs.module.eptsharmonization.api.model.FormDTO;
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
  private EncounterService encounterService;

  // @Autowired
  // public void setHarmonizationDAO(HarmonizationServiceDAO harmonizationDAO) {
  // this.harmonizationDAO = harmonizationDAO;
  // }
  //
  // @Autowired
  // public void setHarmonizationFormServiceDAO(
  // HarmonizationEncounterTypeServiceDAO harmonizationEncounterTypeServiceDAO) {
  // this.harmonizationFormServiceDAO = harmonizationEncounterTypeServiceDAO;
  // }
  //
  // @Autowired
  // public void setEncounterService(EncounterService encounterService) {
  // this.formService = encounterService;
  // }

  @Autowired
  public HarmonizationFormServiceImpl(
      FormService formService,
      HarmonizationServiceDAO harmonizationDAO,
      HarmonizationFormServiceDAO harmonizationFormServiceDAO,
      EncounterService encounterService) {
    this.formService = formService;
    this.harmonizationDAO = harmonizationDAO;
    this.harmonizationFormServiceDAO = harmonizationFormServiceDAO;
    this.encounterService = encounterService;
  }

  @Override
  @Transactional(readOnly = true)
  public List<FormDTO> findAllMetadataFormsNotContainedInProductionServer() throws APIException {
    this.harmonizationDAO.evictCache();
    List<Form> mdsForms = harmonizationFormServiceDAO.findAllMetadataServerForms();
    List<Form> pdsForms = harmonizationFormServiceDAO.findAllProductionServerForms();
    mdsForms.removeAll(pdsForms);

    List<FormDTO> dtos = DTOUtils.fromForms(mdsForms);
    seXDTORelatedData(dtos);
    return dtos;
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
  @Transactional(readOnly = true)
  public List<FormDTO> findAllMetadataServerFormsPartialEqualsToProductionServer()
      throws APIException {
    this.harmonizationDAO.evictCache();
    List<Form> allMDS = harmonizationFormServiceDAO.findAllMetadataServerForms();
    List<Form> allPDS = harmonizationFormServiceDAO.findAllProductionServerForms();
    List<Form> mdsForms = this.removeElementsWithDifferentIDsAndUUIDs(allMDS, allPDS);
    allMDS.removeAll(allPDS);
    mdsForms.removeAll(allMDS);
    return DTOUtils.fromForms(mdsForms);
  }

  @Override
  @Transactional(readOnly = true)
  public List<FormDTO> findAllProductionServerFormsPartialEqualsToMetadataServer()
      throws APIException {
    this.harmonizationDAO.evictCache();
    List<Form> allPDS = harmonizationFormServiceDAO.findAllProductionServerForms();
    List<Form> allMDS = harmonizationFormServiceDAO.findAllMetadataServerForms();
    List<Form> pdsForms = this.removeElementsWithDifferentIDsAndUUIDs(allPDS, allMDS);
    allPDS.removeAll(allMDS);
    pdsForms.removeAll(allPDS);
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
  @Transactional(readOnly = true)
  public int getNumberOfAffectedEncounters(Form form) {
    this.harmonizationDAO.evictCache();
    return this.harmonizationFormServiceDAO.findEncountersByForm(form).size();
  }

  @Override
  public int getNumberOfAffectedFormFields(Form form) {
    this.harmonizationDAO.evictCache();
    return this.harmonizationFormServiceDAO.findFormFieldsByForm(form).size();
  }

  @Override
  @Transactional(readOnly = true)
  public int getNumberOfAffectedFormResourses(Form form) {
    this.harmonizationDAO.evictCache();
    return this.harmonizationFormServiceDAO.findFormResourcesByForm(form).size();
  }

  @Override
  public List<Form> findPDSFormsNotExistsInMDServer() throws APIException {
    this.harmonizationDAO.evictCache();
    return harmonizationFormServiceDAO.findPDSFormsNotExistsInMDServer();
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
          List<Encounter> relatedEncounters =
              this.harmonizationFormServiceDAO.findEncountersByForm(found);
          List<FormField> relatedFormFields =
              this.harmonizationFormServiceDAO.findFormFieldsByForm(found);
          List<FormResource> relatedFormResources =
              this.harmonizationFormServiceDAO.findFormResourcesByForm(found);

          this.updateToNextAvailableID(
              form, relatedEncounters, relatedFormFields, relatedFormResources);
        }
        this.harmonizationFormServiceDAO.saveNotSwappableForm(form);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        this.harmonizationDAO.setEnableCheckConstraints();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void saveFormsWithDifferentIDAndEqualUUID(Map<String, List<FormDTO>> forms)
      throws APIException {

    // this.harmonizationDAO.evictCache();
    // try {
    //
    // this.harmonizationDAO.setDisabledCheckConstraints();
    // for (String uuid : mapEncounterTypes.keySet()) {
    //
    // List<EncounterTypeDTO> list = mapEncounterTypes.get(uuid);
    // EncounterType mdsEncounterType = list.get(0).getEncounterType();
    // EncounterType pdSEncounterType = list.get(1).getEncounterType();
    // Integer mdServerEncounterId = mdsEncounterType.getEncounterTypeId();
    //
    // EncounterType foundMDS = this.harmonizationFormServiceDAO
    // .getEncounterTypeById(mdsEncounterType.getId());
    //
    // if (foundMDS != null) {
    // if (!this.harmonizationFormServiceDAO.isSwappable(foundMDS)) {
    // throw new APIException(String.format(
    // "Cannot update the Production Server Encounter type [ID = {%s}, UUID = {%s},
    // NAME =
    // {%s}] with the ID {%s} this new ID is already referencing an Existing
    // Encounter Type In
    // Metadata Server",
    // pdSEncounterType.getId(), pdSEncounterType.getUuid(),
    // pdSEncounterType.getName(),
    // mdServerEncounterId));
    // }
    // List<Encounter> relatedEncounters = this.harmonizationFormServiceDAO
    // .findEncontersByEncounterTypeId(foundMDS.getId());
    // List<Form> relatedForms = this.harmonizationFormServiceDAO
    // .findFormsByEncounterTypeId(foundMDS.getId());
    // this.updateToNextAvailableID(foundMDS, relatedEncounters, relatedForms);
    // }
    //
    // EncounterType foundPDS = this.harmonizationFormServiceDAO
    // .getEncounterTypeById(pdSEncounterType.getId());
    // if (!this.harmonizationFormServiceDAO.isSwappable(foundPDS)) {
    // throw new APIException(String.format(
    // "Cannot update the Production Server Encounter type with ID {%s}, UUID {%s}
    // and NAME
    // {%s}. This Encounter Type is a Reference from an Encounter Type of Metadata
    // Server",
    // foundPDS.getId(), foundPDS.getUuid(), foundPDS.getName()));
    // }
    // List<Encounter> relatedEncounters = this.harmonizationFormServiceDAO
    // .findEncontersByEncounterTypeId(foundPDS.getId());
    // List<Form> relatedForms =
    // this.harmonizationFormServiceDAO.findFormsByEncounterTypeId(foundPDS.getId());
    // this.updateToGivenId(foundPDS, mdServerEncounterId, false, relatedEncounters,
    // relatedForms);
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // } finally {
    // try {
    // this.harmonizationDAO.setEnableCheckConstraints();
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }

  }

  @Override
  public void saveManualMapping(Map<Form, Form> forms) throws APIException {

    //
    // this.harmonizationDAO.evictCache();
    // try {
    //
    // this.harmonizationDAO.setDisabledCheckConstraints();
    // for (Entry<EncounterType, EncounterType> entry :
    // mapEncounterTypes.entrySet()) {
    //
    // EncounterType pdSEncounterType = entry.getKey();
    // EncounterType mdsEncounterType = entry.getValue();
    //
    // EncounterType foundPDS = this.harmonizationFormServiceDAO
    // .getEncounterTypeById(pdSEncounterType.getId());
    //
    // if (mdsEncounterType.getUuid().equals(pdSEncounterType.getUuid())
    // && mdsEncounterType.getId().equals(pdSEncounterType.getId())) {
    // if (mdsEncounterType.getId().equals(pdSEncounterType.getId())
    // && mdsEncounterType.getName().equals(pdSEncounterType.getName())) {
    // return;
    // }
    // foundPDS.setName(mdsEncounterType.getName());
    // foundPDS.setDescription(mdsEncounterType.getDescription());
    // this.formService.saveEncounterType(foundPDS);
    //
    // } else {
    // List<Encounter> relatedEncounters = this.harmonizationFormServiceDAO
    // .findEncontersByEncounterTypeId(foundPDS.getId());
    //
    // List<Form> relatedForms = this.harmonizationFormServiceDAO
    // .findFormsByEncounterTypeId(foundPDS.getId());
    //
    // for (Form form : relatedForms) {
    // this.harmonizationFormServiceDAO.updateForm(form,
    // mdsEncounterType.getEncounterTypeId());
    // }
    // for (Encounter encounter : relatedEncounters) {
    // this.harmonizationFormServiceDAO.updateEncounter(encounter,
    // mdsEncounterType.getEncounterTypeId());
    // }
    // this.harmonizationFormServiceDAO.deleteEncounterType(foundPDS);
    // }
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // } finally {
    // try {
    // this.harmonizationDAO.setEnableCheckConstraints();
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }

  }

  @Override
  public void deleteNewFormsFromPDS(List<FormDTO> forms) throws APIException {

    this.harmonizationDAO.evictCache();
    for (Form form : DTOUtils.fromFormDTOs(forms)) {
      this.harmonizationFormServiceDAO.deleteForm(form);
    }
  }

  private List<Form> removeElementsWithDifferentIDsAndUUIDs(
      List<Form> mdsForms, List<Form> pdsForms) {
    List<Form> auxMDS = new ArrayList<>();
    for (Form mdsForm : mdsForms) {
      for (Form pdsForm : pdsForms) {
        if (mdsForm.getId().compareTo(pdsForm.getId()) != 0
            && mdsForm.getUuid().contentEquals(pdsForm.getUuid())) {
          auxMDS.add(mdsForm);
        }
      }
    }
    return auxMDS;
  }

  private Map<String, List<Form>> findByWithDifferentNameAndSameUUIDAndID() {
    List<Form> allMDS = harmonizationFormServiceDAO.findAllMetadataServerForms();
    List<Form> allPDS = harmonizationFormServiceDAO.findAllProductionServerForms();

    Map<String, List<Form>> map = new TreeMap<>();
    for (Form mdsItem : allMDS) {
      for (Form pdsItem : allPDS) {
        if (mdsItem.getUuid().equals(pdsItem.getUuid())
            && mdsItem.getId() == pdsItem.getId()
            && !mdsItem.getName().equalsIgnoreCase(pdsItem.getName())) {
          map.put(mdsItem.getUuid(), Arrays.asList(mdsItem, pdsItem));
        }
      }
    }
    return map;
  }

  private Map<String, List<Form>> findByWithDifferentIDAndSameUUID() {
    List<Form> allPDS = harmonizationFormServiceDAO.findAllProductionServerForms();
    List<Form> allMDS = harmonizationFormServiceDAO.findAllMetadataServerForms();
    Map<String, List<Form>> map = new TreeMap<>();
    for (Form mdsItem : allMDS) {
      for (Form pdsItem : allPDS) {
        if (mdsItem.getUuid().equals(pdsItem.getUuid()) && mdsItem.getId() != pdsItem.getId()) {
          map.put(mdsItem.getUuid(), Arrays.asList(mdsItem, pdsItem));
        }
      }
    }
    return map;
  }

  // private void updateToGivenId(EncounterType encounterType, Integer
  // encounterTypeId, boolean
  // swappable,
  // List<Encounter> relatedEncounters, List<Form> relatedForms) {
  // this.harmonizationFormServiceDAO.updateEncounterType(encounterTypeId,
  // encounterType,
  // swappable);
  //
  // for (Form form : relatedForms) {
  // this.harmonizationFormServiceDAO.updateForm(form, encounterTypeId);
  // }
  // for (Encounter encounter : relatedEncounters) {
  // this.harmonizationFormServiceDAO.updateEncounter(encounter, encounterTypeId);
  // }
  // }

  private void updateToNextAvailableID(
      Form form,
      List<Encounter> relatedEncounters,
      List<FormField> relatedFormFields,
      List<FormResource> relatedFormResources) {
    Form updated = this.harmonizationFormServiceDAO.updateToNextAvailableId(form);

    // for (Encounter encounter : relatedEncounters) {
    // this.harmonizationFormServiceDAO.updateEncounter(encounter,
    // updated.getEncounterTypeId());
    // }
  }

  private void seXDTORelatedData(List<FormDTO> dtos) {

    for (FormDTO formDTO : dtos) {
      if (formDTO.getForm().getEncounterType() != null) {
        try {
          formDTO.setEncounterType(
              this.encounterService.getEncounterType(formDTO.getForm().getEncounterType().getId()));
        } catch (ObjectNotFoundException e) {
        }
      }
    }
  }
}
