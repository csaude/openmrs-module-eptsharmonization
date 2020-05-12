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
import org.openmrs.PersonAttributeType;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationPersonAttributeTypeService;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.openmrs.module.eptsharmonization.api.model.PersonAttributeTypeDTO;

/** It is a default implementation of {@link HarmonizationPersonAttributeTypeService}. */
public class HarmonizationPersonAttributeTypeServiceImpl extends BaseOpenmrsService
    implements HarmonizationPersonAttributeTypeService {

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
  public List<PersonAttributeTypeDTO> findAllMetadataPersonAttributeTypesNotInProductionServer()
      throws APIException {
    List<PersonAttributeType> mdsPersonAttributeTypes =
        dao.findAllMetadataServerPersonAttributeTypes();
    List<PersonAttributeType> pdsPersonAttributeTypes =
        dao.findAllProductionServerPersonAttributeTypes();
    mdsPersonAttributeTypes.removeAll(pdsPersonAttributeTypes);
    return DTOUtils.fromPersonAttributeTypes(mdsPersonAttributeTypes);
  }

  @Override
  public List<PersonAttributeTypeDTO> findAllProductionPersonAttibuteTypesNotInMetadataServer()
      throws APIException {
    List<PersonAttributeType> mdsPersonAttributeTypes =
        dao.findAllMetadataServerPersonAttributeTypes();
    List<PersonAttributeType> pdsPersonAttributeTypes =
        dao.findAllProductionServerPersonAttributeTypes();
    pdsPersonAttributeTypes.removeAll(mdsPersonAttributeTypes);
    return DTOUtils.fromPersonAttributeTypes(pdsPersonAttributeTypes);
  }

  @Override
  public List<PersonAttributeTypeDTO>
      findAllMetadataPersonAttributeTypesPartialEqualsToProductionServer() throws APIException {
    List<PersonAttributeType> allMDS = dao.findAllMetadataServerPersonAttributeTypes();
    List<PersonAttributeType> allPDS = dao.findAllProductionServerPersonAttributeTypes();
    List<PersonAttributeType> mdsPersonAttributeTypes =
        this.removePATWithDifferentIDsAndUUIDs(allMDS, allPDS);
    allMDS.removeAll(allPDS);
    mdsPersonAttributeTypes.removeAll(allMDS);
    return DTOUtils.fromPersonAttributeTypes(mdsPersonAttributeTypes);
  }

  @Override
  public List<PersonAttributeTypeDTO>
      findAllProductionPersonAttributeTypesPartialEqualsToMetadataServer() throws APIException {
    List<PersonAttributeType> allMDS = dao.findAllMetadataServerPersonAttributeTypes();
    List<PersonAttributeType> allPDS = dao.findAllProductionServerPersonAttributeTypes();
    List<PersonAttributeType> pdsPersonAttributeTypes =
        this.removePATWithDifferentIDsAndUUIDs(allPDS, allMDS);
    allPDS.removeAll(allMDS);
    pdsPersonAttributeTypes.removeAll(allPDS);
    return DTOUtils.fromPersonAttributeTypes(pdsPersonAttributeTypes);
  }

  @Override
  public Map<String, List<PersonAttributeTypeDTO>>
      findAllPersonAttributeTypesWithDifferentNameAndSameUUIDAndID() throws APIException {
    Map<String, List<PersonAttributeTypeDTO>> result = new HashMap<>();
    Map<String, List<PersonAttributeType>> map = findByWithDifferentNameAndSameUUIDAndID();
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromPersonAttributeTypes(map.get(key)));
    }
    return result;
  }

  @Override
  public Map<String, List<PersonAttributeTypeDTO>>
      findAllPersonAttributeTypesWithDifferentIDAndSameUUID() throws APIException {
    Map<String, List<PersonAttributeTypeDTO>> result = new HashMap<>();
    Map<String, List<PersonAttributeType>> map = findByWithDifferentIDAndSameUUID();
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromPersonAttributeTypes(map.get(key)));
    }
    return result;
  }

  private List<PersonAttributeType> removePATWithDifferentIDsAndUUIDs(
      List<PersonAttributeType> mdsPersonAttributeTypes,
      List<PersonAttributeType> pdsPersonAttributeTypes) {
    List<PersonAttributeType> auxMDS = new ArrayList<>();
    for (PersonAttributeType mdsPersonAttributeType : mdsPersonAttributeTypes) {
      for (PersonAttributeType pdsPersonAttributeType : pdsPersonAttributeTypes) {
        if (mdsPersonAttributeType.getId().compareTo(pdsPersonAttributeType.getId()) != 0
            && mdsPersonAttributeType.getUuid().contentEquals(pdsPersonAttributeType.getUuid())) {
          auxMDS.add(mdsPersonAttributeType);
        }
      }
    }
    return auxMDS;
  }

  private Map<String, List<PersonAttributeType>> findByWithDifferentNameAndSameUUIDAndID() {
    List<PersonAttributeType> allMDS = dao.findAllMetadataServerPersonAttributeTypes();
    List<PersonAttributeType> allPDS = dao.findAllProductionServerPersonAttributeTypes();

    Map<String, List<PersonAttributeType>> map = new TreeMap<>();
    for (PersonAttributeType mdsItem : allMDS) {
      for (PersonAttributeType pdsItem : allPDS) {
        if (mdsItem.getUuid().equals(pdsItem.getUuid())
            && mdsItem.getId() == pdsItem.getId()
            && !mdsItem.getName().equalsIgnoreCase(pdsItem.getName())) {
          map.put(mdsItem.getUuid(), Arrays.asList(mdsItem, pdsItem));
        }
      }
    }
    return map;
  }

  public Map<String, List<PersonAttributeType>> findByWithDifferentIDAndSameUUID() {
    List<PersonAttributeType> allPDS = dao.findAllProductionServerPersonAttributeTypes();
    List<PersonAttributeType> allMDS = dao.findAllMetadataServerPersonAttributeTypes();

    Map<String, List<PersonAttributeType>> map = new TreeMap<>();
    for (PersonAttributeType mdsItem : allMDS) {
      for (PersonAttributeType pdsItem : allPDS) {
        if (mdsItem.getUuid().equals(pdsItem.getUuid()) && mdsItem.getId() != pdsItem.getId()) {
          map.put(mdsItem.getUuid(), Arrays.asList(mdsItem, pdsItem));
        }
      }
    }
    return map;
  }

  @Override
  public void savePersonAttributeTypesWithDifferentNames(
      Map<String, List<PersonAttributeTypeDTO>> personAttributeTypes) throws APIException {
    for (String key : personAttributeTypes.keySet()) {
      List<PersonAttributeTypeDTO> list = personAttributeTypes.get(key);
      PersonAttributeTypeDTO mdsEncounter = list.get(0);
      PersonAttributeTypeDTO pdsEncounter = list.get(1);
      PersonAttributeType personAttributeType =
          Context.getPersonService()
              .getPersonAttributeType(pdsEncounter.getPersonAttributeType().getId());
      personAttributeType.setName(mdsEncounter.getPersonAttributeType().getName());
      Context.getPersonService().savePersonAttributeType(personAttributeType);
    }
  }

  @Override
  public int countPersonAttributeRows(Integer personAttributeTypeId) {
    return dao.findPersonAttributeByTypeId(personAttributeTypeId).size();
  }
}
