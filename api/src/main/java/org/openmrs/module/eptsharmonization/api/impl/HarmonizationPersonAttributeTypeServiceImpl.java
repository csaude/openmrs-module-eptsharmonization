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
import org.openmrs.PersonAttribute;
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
  public int getNumberOfAffectedPersonAttributes(PersonAttributeTypeDTO personAttributeTypeDTO) {
    return dao.findPersonAttributeByPersonAttributeTypeId(
            personAttributeTypeDTO.getPersonAttributeType().getId())
        .size();
  }

  @Override
  public List<PersonAttributeType> findPDSPersonAttributeTypesNotExistsInMDServer()
      throws APIException {
    return this.dao.findPDSPersonAttributeTypesNotExistsInMDServer();
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
      personAttributeType.setDescription(mdsEncounter.getPersonAttributeType().getDescription());
      Context.getPersonService().savePersonAttributeType(personAttributeType);
    }
  }

  @Override
  public void saveNewPersonAttributeTypesFromMDS(List<PersonAttributeTypeDTO> personAttributeTypes)
      throws APIException {

    try {
      this.dao.setDisabledCheckConstraints();
      for (PersonAttributeType personAttributeType :
          DTOUtils.fromPersonAttributeDTOs(personAttributeTypes)) {

        PersonAttributeType found =
            this.dao.getPersonAttributeTypeById(personAttributeType.getId());

        if (found != null) {

          if (!this.dao.isSwappable(found)) {

            throw new APIException(
                String.format(
                    "Cannot Insert PersonAttributeType with ID %s, UUID %s and NAME %s. This ID is being in use by another PersonAttributeType from Metatada server with UUID %s and name %s ",
                    personAttributeType.getId(),
                    personAttributeType.getUuid(),
                    personAttributeType.getName(),
                    found.getUuid(),
                    found.getName()));
          }
          List<PersonAttribute> relatedPersonAttributes =
              this.dao.findPersonAttributeByPersonAttributeTypeId(found.getId());
          Integer nextId = this.dao.getNextPersonAttriTypeId();

          this.updateToGivenId(found, nextId, true, relatedPersonAttributes);
        }
        this.dao.saveNotSwappablePersonAttributeType(personAttributeType);
        Context.flushSession();
      }
      this.dao.setEnableCheckConstraints();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void savePersonAttributeTypesWithDifferentIDAndEqualUUID(
      Map<String, List<PersonAttributeTypeDTO>> personAttributeTypes) throws APIException {

    try {

      this.dao.setDisabledCheckConstraints();
      for (String uuid : personAttributeTypes.keySet()) {

        List<PersonAttributeTypeDTO> list = personAttributeTypes.get(uuid);
        PersonAttributeType mdsPersonAttributeType = list.get(0).getPersonAttributeType();
        PersonAttributeType pdSPersonAttributeType = list.get(1).getPersonAttributeType();
        Integer mdServerEncounterId = mdsPersonAttributeType.getPersonAttributeTypeId();

        PersonAttributeType foundMDS =
            this.dao.getPersonAttributeTypeById(mdsPersonAttributeType.getId());

        if (foundMDS != null) {
          if (!this.dao.isSwappable(foundMDS)) {
            throw new APIException(
                String.format(
                    "Cannot update the Production Server PersonAttributeType [ID = {%s}, UUID = {%s}, NAME = {%s}] with the ID {%s} this new ID is already referencing an Existing PersonAttributeType In Metadata Server",
                    pdSPersonAttributeType.getId(),
                    pdSPersonAttributeType.getUuid(),
                    pdSPersonAttributeType.getName(),
                    mdServerEncounterId));
          }
          List<PersonAttribute> personAttributes =
              this.dao.findPersonAttributeByPersonAttributeTypeId(foundMDS.getId());
          Integer nextId = this.dao.getNextPersonAttriTypeId();
          this.updateToGivenId(foundMDS, nextId, true, personAttributes);
        }

        PersonAttributeType foundPDS =
            this.dao.getPersonAttributeTypeById(pdSPersonAttributeType.getId());
        if (!this.dao.isSwappable(foundPDS)) {
          throw new APIException(
              String.format(
                  "Cannot update the Production Server PersonAttributeType with ID {%s}, UUID {%s} and NAME {%s}. This PersonAttributeType is a Reference from an PersonAttributeType of Metadata Server",
                  foundPDS.getId(), foundPDS.getUuid(), foundPDS.getName()));
        }
        List<PersonAttribute> personAttributes =
            this.dao.findPersonAttributeByPersonAttributeTypeId(foundPDS.getId());
        this.updateToGivenId(foundPDS, mdServerEncounterId, true, personAttributes);
      }
      this.dao.setEnableCheckConstraints();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void updateToGivenId(
      PersonAttributeType personAttributeType,
      Integer personAttributeTypeId,
      boolean swappable,
      List<PersonAttribute> relatedPersonAttributes) {
    this.dao.updatePersonAttributeType(personAttributeTypeId, personAttributeType, swappable);
    for (PersonAttribute personAttribute : relatedPersonAttributes) {
      this.dao.updatePersonAttribute(personAttribute, personAttributeTypeId);
    }
  }
}
