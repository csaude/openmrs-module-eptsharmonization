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
import org.openmrs.EncounterType;
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
  public int countEncounterRows(Integer encounterTypeId) {
    return dao.findEncontersByEncounterTypeId(encounterTypeId).size();
  }

  @Override
  public int countFormRows(Integer encounterTypeId) {
    return dao.findFormsByEncounterTypeId(encounterTypeId).size();
  }
}
