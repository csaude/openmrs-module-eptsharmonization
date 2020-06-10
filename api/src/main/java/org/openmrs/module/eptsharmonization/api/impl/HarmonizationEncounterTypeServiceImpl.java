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
import java.util.Map.Entry;
import java.util.TreeMap;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationEncounterTypeService;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationEncounterTypeServiceDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.openmrs.module.eptsharmonization.api.model.EncounterTypeDTO;

/** It is a default implementation of {@link HarmonizationEncounterTypeService}. */
public class HarmonizationEncounterTypeServiceImpl extends BaseOpenmrsService
    implements HarmonizationEncounterTypeService {

  private HarmonizationServiceDAO dao;

  private HarmonizationEncounterTypeServiceDAO encounterTypeServiceDAO;

  /** @param dao the dao to set */
  public void setDao(HarmonizationServiceDAO dao) {
    this.dao = dao;
  }

  /** @return the dao */
  public HarmonizationServiceDAO getDao() {
    return dao;
  }

  public HarmonizationEncounterTypeServiceDAO getEncounterTypeServiceDAO() {
    return encounterTypeServiceDAO;
  }

  public void setEncounterTypeServiceDAO(
      HarmonizationEncounterTypeServiceDAO encounterTypeServiceDAO) {
    this.encounterTypeServiceDAO = encounterTypeServiceDAO;
  }

  @Override
  public List<EncounterTypeDTO> findAllMetadataEncounterNotContainedInProductionServer()
      throws APIException {
    List<EncounterType> mdsEncounterTypes =
        encounterTypeServiceDAO.findAllMetadataServerEncounterTypes();
    List<EncounterType> pdsEncounterTypes =
        encounterTypeServiceDAO.findAllProductionServerEncounterTypes();
    mdsEncounterTypes.removeAll(pdsEncounterTypes);
    return DTOUtils.fromEncounterTypes(mdsEncounterTypes);
  }

  @Override
  public List<EncounterTypeDTO> findAllProductionEncountersNotContainedInMetadataServer()
      throws APIException {
    List<EncounterType> pdsEncounterTypes =
        encounterTypeServiceDAO.findAllProductionServerEncounterTypes();
    List<EncounterType> mdsEncounterTypes =
        encounterTypeServiceDAO.findAllMetadataServerEncounterTypes();
    pdsEncounterTypes.removeAll(mdsEncounterTypes);
    return DTOUtils.fromEncounterTypes(pdsEncounterTypes);
  }

  @Override
  public List<EncounterTypeDTO> findAllMetadataEncounterPartialEqualsToProductionServer()
      throws APIException {
    List<EncounterType> allMDS = encounterTypeServiceDAO.findAllMetadataServerEncounterTypes();
    List<EncounterType> allPDS = encounterTypeServiceDAO.findAllProductionServerEncounterTypes();
    List<EncounterType> mdsEncounterTypes =
        this.removeElementsWithDifferentIDsAndUUIDs(allMDS, allPDS);
    allMDS.removeAll(allPDS);
    mdsEncounterTypes.removeAll(allMDS);
    return DTOUtils.fromEncounterTypes(mdsEncounterTypes);
  }

  @Override
  public List<EncounterTypeDTO> findAllProductionEncountersPartialEqualsToMetadataServer()
      throws APIException {
    List<EncounterType> allPDS = encounterTypeServiceDAO.findAllProductionServerEncounterTypes();
    List<EncounterType> allMDS = encounterTypeServiceDAO.findAllMetadataServerEncounterTypes();
    List<EncounterType> pdsEncounterTypes =
        this.removeElementsWithDifferentIDsAndUUIDs(allPDS, allMDS);
    allPDS.removeAll(allMDS);
    pdsEncounterTypes.removeAll(allPDS);
    return DTOUtils.fromEncounterTypes(pdsEncounterTypes);
  }

  @Override
  public Map<String, List<EncounterTypeDTO>>
      findAllEncounterTypesWithDifferentNameAndSameUUIDAndID() throws APIException {
    Map<String, List<EncounterTypeDTO>> result = new HashMap<>();
    Map<String, List<EncounterType>> map = findByWithDifferentNameAndSameUUIDAndID();
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromEncounterTypes(map.get(key)));
    }
    return result;
  }

  @Override
  public Map<String, List<EncounterTypeDTO>> findAllEncounterTypesWithDifferentIDAndSameUUID()
      throws APIException {
    Map<String, List<EncounterTypeDTO>> result = new HashMap<>();
    Map<String, List<EncounterType>> map = findByWithDifferentIDAndSameUUID();
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromEncounterTypes(map.get(key)));
    }
    return result;
  }

  public Map<String, List<EncounterType>> findByWithDifferentIDAndSameUUID() {
    List<EncounterType> allPDS = encounterTypeServiceDAO.findAllProductionServerEncounterTypes();
    List<EncounterType> allMDS = encounterTypeServiceDAO.findAllMetadataServerEncounterTypes();
    Map<String, List<EncounterType>> map = new TreeMap<>();
    for (EncounterType mdsItem : allMDS) {
      for (EncounterType pdsItem : allPDS) {
        if (mdsItem.getUuid().equals(pdsItem.getUuid()) && mdsItem.getId() != pdsItem.getId()) {
          map.put(mdsItem.getUuid(), Arrays.asList(mdsItem, pdsItem));
        }
      }
    }
    return map;
  }

  @Override
  public int getNumberOfAffectedEncounters(EncounterTypeDTO encounterTypeDTO) {
    return encounterTypeServiceDAO
        .findEncontersByEncounterTypeId(encounterTypeDTO.getEncounterType().getEncounterTypeId())
        .size();
  }

  @Override
  public int getNumberOfAffectedForms(EncounterTypeDTO encounterTypeDTO) {
    return encounterTypeServiceDAO
        .findFormsByEncounterTypeId(encounterTypeDTO.getEncounterType().getEncounterTypeId())
        .size();
  }

  @Override
  public List<EncounterType> findPDSEncounterTypesNotExistsInMDServer() throws APIException {
    List<EncounterType> result = encounterTypeServiceDAO.findPDSEncounterTypesNotExistsInMDServer();
    return result;
  }

  @Override
  public List<EncounterType> findAllNotSwappableEncounterTypes() throws APIException {
    List<EncounterType> result = this.encounterTypeServiceDAO.findAllNotSwappable();
    return result;
  }

  @Override
  public List<EncounterType> findAllSwappableEncounterTypes() throws APIException {
    List<EncounterType> findAllSwappable = this.encounterTypeServiceDAO.findAllSwappable();
    return findAllSwappable;
  }

  @Override
  public void saveEncounterTypesWithDifferentNames(
      Map<String, List<EncounterTypeDTO>> encounterTypes) throws APIException {
    for (String key : encounterTypes.keySet()) {
      List<EncounterTypeDTO> list = encounterTypes.get(key);
      EncounterTypeDTO mdsEncounter = list.get(0);
      EncounterTypeDTO pdsEncounter = list.get(1);
      EncounterType encounterType =
          Context.getEncounterService().getEncounterType(pdsEncounter.getEncounterType().getId());
      encounterType.setName(mdsEncounter.getEncounterType().getName());
      encounterType.setDescription(mdsEncounter.getEncounterType().getDescription());
      Context.getEncounterService().saveEncounterType(encounterType);
    }
  }

  @Override
  public void saveNewEncounterTypesFromMDS(List<EncounterTypeDTO> encounterTypes)
      throws APIException {
    try {
      this.dao.setDisabledCheckConstraints();
      for (EncounterType encounterType : DTOUtils.fromEncounterTypeDTOs(encounterTypes)) {

        EncounterType found =
            this.encounterTypeServiceDAO.getEncounterTypeById(encounterType.getId());

        if (found != null) {

          if (!this.encounterTypeServiceDAO.isSwappable(found)) {

            throw new APIException(
                String.format(
                    "Cannot Insert Encounter type with ID %s, UUID %s and NAME %s. This ID is being in use by another Enconter type from Metatada server with UUID %s and name %s ",
                    encounterType.getId(),
                    encounterType.getUuid(),
                    encounterType.getName(),
                    found.getUuid(),
                    found.getName()));
          }
          List<Encounter> relatedEncounters =
              this.encounterTypeServiceDAO.findEncontersByEncounterTypeId(found.getId());
          List<Form> relatedForms =
              this.encounterTypeServiceDAO.findFormsByEncounterTypeId(found.getId());
          this.updateToNextAvailableID(found, relatedEncounters, relatedForms);
        }
        this.encounterTypeServiceDAO.saveNotSwappableEncounterType(encounterType);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        this.dao.setEnableCheckConstraints();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void saveEncounterTypesWithDifferentIDAndEqualUUID(
      Map<String, List<EncounterTypeDTO>> mapEncounterTypes) throws APIException {
    try {

      this.dao.setDisabledCheckConstraints();
      for (String uuid : mapEncounterTypes.keySet()) {

        List<EncounterTypeDTO> list = mapEncounterTypes.get(uuid);
        EncounterType mdsEncounterType = list.get(0).getEncounterType();
        EncounterType pdSEncounterType = list.get(1).getEncounterType();
        Integer mdServerEncounterId = mdsEncounterType.getEncounterTypeId();

        EncounterType foundMDS =
            this.encounterTypeServiceDAO.getEncounterTypeById(mdsEncounterType.getId());

        if (foundMDS != null) {
          if (!this.encounterTypeServiceDAO.isSwappable(foundMDS)) {
            throw new APIException(
                String.format(
                    "Cannot update the Production Server Encounter type [ID = {%s}, UUID = {%s}, NAME = {%s}] with the ID {%s} this new ID is already referencing an Existing Encounter Type In Metadata Server",
                    pdSEncounterType.getId(),
                    pdSEncounterType.getUuid(),
                    pdSEncounterType.getName(),
                    mdServerEncounterId));
          }
          List<Encounter> relatedEncounters =
              this.encounterTypeServiceDAO.findEncontersByEncounterTypeId(foundMDS.getId());
          List<Form> relatedForms =
              this.encounterTypeServiceDAO.findFormsByEncounterTypeId(foundMDS.getId());
          this.updateToNextAvailableID(foundMDS, relatedEncounters, relatedForms);
        }

        EncounterType foundPDS =
            this.encounterTypeServiceDAO.getEncounterTypeById(pdSEncounterType.getId());
        if (!this.encounterTypeServiceDAO.isSwappable(foundPDS)) {
          throw new APIException(
              String.format(
                  "Cannot update the Production Server Encounter type with ID {%s}, UUID {%s} and NAME {%s}. This Encounter Type is a Reference from an Encounter Type of Metadata Server",
                  foundPDS.getId(), foundPDS.getUuid(), foundPDS.getName()));
        }
        List<Encounter> relatedEncounters =
            this.encounterTypeServiceDAO.findEncontersByEncounterTypeId(foundPDS.getId());
        List<Form> relatedForms =
            this.encounterTypeServiceDAO.findFormsByEncounterTypeId(foundPDS.getId());
        this.updateToGivenId(foundPDS, mdServerEncounterId, false, relatedEncounters, relatedForms);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        this.dao.setEnableCheckConstraints();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void deleteNewEncounterTypesFromPDS(List<EncounterTypeDTO> encounterTypes)
      throws APIException {
    for (EncounterType encounterType : DTOUtils.fromEncounterTypeDTOs(encounterTypes)) {
      Context.getEncounterService().purgeEncounterType(encounterType);
    }
  }

  @Override
  public void saveManualMapping(Map<EncounterType, EncounterType> mapEncounterTypes)
      throws APIException {
    try {

      this.dao.setDisabledCheckConstraints();
      for (Entry<EncounterType, EncounterType> entry : mapEncounterTypes.entrySet()) {

        EncounterType mdsEncounterType = entry.getKey();
        EncounterType pdSEncounterType = entry.getValue();

        EncounterType foundPDS =
            this.encounterTypeServiceDAO.getEncounterTypeById(pdSEncounterType.getId());

        if (mdsEncounterType.getUuid().equals(pdSEncounterType.getUuid())
            && mdsEncounterType.getId().equals(pdSEncounterType.getId())) {

          foundPDS.setName(mdsEncounterType.getName());
          foundPDS.setDescription(mdsEncounterType.getDescription());
          Context.getEncounterService().saveEncounterType(foundPDS);

        } else {
          List<Encounter> relatedEncounters =
              this.encounterTypeServiceDAO.findEncontersByEncounterTypeId(foundPDS.getId());

          List<Form> relatedForms =
              this.encounterTypeServiceDAO.findFormsByEncounterTypeId(foundPDS.getId());

          for (Form form : relatedForms) {
            this.encounterTypeServiceDAO.updateForm(form, mdsEncounterType.getEncounterTypeId());
          }
          for (Encounter encounter : relatedEncounters) {
            this.encounterTypeServiceDAO.updateEncounter(
                encounter, mdsEncounterType.getEncounterTypeId());
          }
          Context.getEncounterService().purgeEncounterType(foundPDS);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        this.dao.setEnableCheckConstraints();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private List<EncounterType> removeElementsWithDifferentIDsAndUUIDs(
      List<EncounterType> mdsEncounterTypes, List<EncounterType> pdsEncounterTypes) {
    List<EncounterType> auxMDS = new ArrayList<>();
    for (EncounterType mdsEncounterType : mdsEncounterTypes) {
      for (EncounterType pdsEncounterType : pdsEncounterTypes) {
        if (mdsEncounterType.getId().compareTo(pdsEncounterType.getId()) != 0
            && mdsEncounterType.getUuid().contentEquals(pdsEncounterType.getUuid())) {
          auxMDS.add(mdsEncounterType);
        }
      }
    }
    return auxMDS;
  }

  private Map<String, List<EncounterType>> findByWithDifferentNameAndSameUUIDAndID() {

    List<EncounterType> allMDS = encounterTypeServiceDAO.findAllMetadataServerEncounterTypes();
    List<EncounterType> allPDS = encounterTypeServiceDAO.findAllProductionServerEncounterTypes();

    Map<String, List<EncounterType>> map = new TreeMap<>();
    for (EncounterType mdsItem : allMDS) {
      for (EncounterType pdsItem : allPDS) {
        if (mdsItem.getUuid().equals(pdsItem.getUuid())
            && mdsItem.getId() == pdsItem.getId()
            && !mdsItem.getName().equalsIgnoreCase(pdsItem.getName())) {
          map.put(mdsItem.getUuid(), Arrays.asList(mdsItem, pdsItem));
        }
      }
    }
    return map;
  }

  private void updateToGivenId(
      EncounterType encounterType,
      Integer encounterTypeId,
      boolean swappable,
      List<Encounter> relatedEncounters,
      List<Form> relatedForms) {
    this.encounterTypeServiceDAO.updateEncounterType(encounterTypeId, encounterType, swappable);

    for (Form form : relatedForms) {
      this.encounterTypeServiceDAO.updateForm(form, encounterTypeId);
    }
    for (Encounter encounter : relatedEncounters) {
      this.encounterTypeServiceDAO.updateEncounter(encounter, encounterTypeId);
    }
  }

  private void updateToNextAvailableID(
      EncounterType encounterType, List<Encounter> relatedEncounters, List<Form> relatedForms) {
    EncounterType updated = this.encounterTypeServiceDAO.updateToNextAvailableId(encounterType);
    for (Form form : relatedForms) {
      this.encounterTypeServiceDAO.updateForm(form, updated.getEncounterTypeId());
    }
    for (Encounter encounter : relatedEncounters) {
      this.encounterTypeServiceDAO.updateEncounter(encounter, updated.getEncounterTypeId());
    }
  }
}
