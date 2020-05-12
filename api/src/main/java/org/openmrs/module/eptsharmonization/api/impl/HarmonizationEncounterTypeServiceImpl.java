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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationEncounterTypeService;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.openmrs.module.eptsharmonization.api.model.EncounterTypeDTO;

/** It is a default implementation of {@link HarmonizationEncounterTypeService}. */
public class HarmonizationEncounterTypeServiceImpl extends BaseOpenmrsService
    implements HarmonizationEncounterTypeService {

  protected final Log log = LogFactory.getLog(this.getClass());

  private HarmonizationServiceDAO dao;

  /** @param dao the dao to set */
  public void setDao(HarmonizationServiceDAO dao) {
    this.dao = dao;
  }

  /** @return the dao */
  public HarmonizationServiceDAO getDao() {
    return dao;
  }

  @Override
  public List<EncounterTypeDTO> findAllMetadataEncounterNotContainedInProductionServer()
      throws APIException {
    List<EncounterType> mdsEncounterTypes = dao.findAllMetadataServerEncounterTypes();
    List<EncounterType> pdsEncounterTypes = dao.findAllProductionServerEncounterTypes();
    mdsEncounterTypes.removeAll(pdsEncounterTypes);
    return DTOUtils.fromEncounterTypes(mdsEncounterTypes);
  }

  @Override
  public List<EncounterTypeDTO> findAllProductionEncountersNotContainedInMetadataServer()
      throws APIException {
    List<EncounterType> pdsEncounterTypes = dao.findAllProductionServerEncounterTypes();
    List<EncounterType> mdsEncounterTypes = dao.findAllMetadataServerEncounterTypes();
    pdsEncounterTypes.removeAll(mdsEncounterTypes);
    return DTOUtils.fromEncounterTypes(pdsEncounterTypes);
  }

  @Override
  public List<EncounterTypeDTO> findAllMetadataEncounterPartialEqualsToProductionServer()
      throws APIException {
    List<EncounterType> allMDS = dao.findAllMetadataServerEncounterTypes();
    List<EncounterType> allPDS = dao.findAllProductionServerEncounterTypes();
    List<EncounterType> mdsEncounterTypes =
        this.removeElementsWithDifferentIDsAndUUIDs(allMDS, allPDS);
    allMDS.removeAll(allPDS);
    mdsEncounterTypes.removeAll(allMDS);
    return DTOUtils.fromEncounterTypes(mdsEncounterTypes);
  }

  @Override
  public List<EncounterTypeDTO> findAllProductionEncountersPartialEqualsToMetadataServer()
      throws APIException {
    List<EncounterType> allPDS = dao.findAllProductionServerEncounterTypes();
    List<EncounterType> allMDS = dao.findAllMetadataServerEncounterTypes();
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
    List<EncounterType> allMDS = dao.findAllMetadataServerEncounterTypes();
    List<EncounterType> allPDS = dao.findAllProductionServerEncounterTypes();

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

  public Map<String, List<EncounterType>> findByWithDifferentIDAndSameUUID() {
    List<EncounterType> allPDS = dao.findAllProductionServerEncounterTypes();
    List<EncounterType> allMDS = dao.findAllMetadataServerEncounterTypes();

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
    return dao.findEncontersByEncounterTypeId(
            encounterTypeDTO.getEncounterType().getEncounterTypeId())
        .size();
  }

  @Override
  public int getNumberOfAffectedForms(EncounterTypeDTO encounterTypeDTO) {
    return dao.findFormsByEncounterTypeId(encounterTypeDTO.getEncounterType().getEncounterTypeId())
        .size();
  }

  @Override
  public List<EncounterType> findPDSEncounterTypesNotExistsInMDServer() throws APIException {
    return dao.findPDSEncounterTypesNotExistsInMDServer();
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
      Context.getEncounterService().saveEncounterType(encounterType);
    }
  }

  @Override
  public void saveNewEncounterTypesFromMDS(List<EncounterTypeDTO> encounterTypes)
      throws APIException {
    try {
      this.dao.setDisabledCheckConstraints();
      for (EncounterType encounterType : DTOUtils.fromEncounterTypeDTOs(encounterTypes)) {

        EncounterType found = this.dao.getEncounterTypeById(encounterType.getId());

        if (found != null) {

          if (!this.dao.isSwappable(found)) {

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
              this.dao.findEncontersByEncounterTypeId(found.getId());
          List<Form> relatedForms = this.dao.findFormsByEncounterTypeId(found.getId());
          Integer nextId = this.dao.getNextEncounterTypeId();
          this.updateToGivenId(found, nextId, true, relatedEncounters, relatedForms);
        }
        this.dao.saveNotSwappableEncounterType(encounterType);
        Context.flushSession();
      }
      this.dao.setEnableCheckConstraints();

    } catch (Exception e) {
      e.printStackTrace();
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

        EncounterType foundMDS = this.dao.getEncounterTypeById(mdsEncounterType.getId());

        if (foundMDS != null) {
          if (!this.dao.isSwappable(foundMDS)) {
            throw new APIException(
                String.format(
                    "Cannot update the Production Server Encounter type [ID = {%s}, UUID = {%s}, NAME = {%s}] with the ID {%s} this new ID is already referencing an Existing Encounter Type In Metadata Server",
                    pdSEncounterType.getId(),
                    pdSEncounterType.getUuid(),
                    pdSEncounterType.getName(),
                    mdServerEncounterId));
          }
          List<Encounter> relatedEncounters =
              this.dao.findEncontersByEncounterTypeId(foundMDS.getId());
          List<Form> relatedForms = this.dao.findFormsByEncounterTypeId(foundMDS.getId());
          Integer nextId = this.dao.getNextEncounterTypeId();
          this.updateToGivenId(foundMDS, nextId, true, relatedEncounters, relatedForms);
        }

        EncounterType foundPDS = this.dao.getEncounterTypeById(pdSEncounterType.getId());
        if (!this.dao.isSwappable(foundPDS)) {
          throw new APIException(
              String.format(
                  "Cannot update the Production Server Encounter type with ID {%s}, UUID {%s} and NAME {%s}. This Encounter Type is a Reference from an Encounter Type of Metadata Server",
                  foundPDS.getId(), foundPDS.getUuid(), foundPDS.getName()));
        }
        List<Encounter> relatedEncounters =
            this.dao.findEncontersByEncounterTypeId(foundPDS.getId());
        List<Form> relatedForms = this.dao.findFormsByEncounterTypeId(foundPDS.getId());
        this.updateToGivenId(foundPDS, mdServerEncounterId, true, relatedEncounters, relatedForms);
      }
      this.dao.setEnableCheckConstraints();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void updateToGivenId(
      EncounterType encounterType,
      Integer encounterTypeId,
      boolean swappable,
      List<Encounter> relatedEncounters,
      List<Form> relatedForms) {
    this.dao.updateEncounterType(encounterTypeId, encounterType, swappable);
    for (Form form : relatedForms) {
      this.dao.updateForm(form, encounterTypeId);
    }
    for (Encounter encounter : relatedEncounters) {
      this.dao.updateEncounter(encounter, encounterTypeId);
    }
  }
}
