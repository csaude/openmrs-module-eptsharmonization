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
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationPersonAttributeTypeService;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationPersonAttributeTypeServiceDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.openmrs.module.eptsharmonization.api.model.PersonAttributeTypeDTO;

/** It is a default implementation of {@link HarmonizationPersonAttributeTypeService}. */
public class HarmonizationPersonAttributeTypeServiceImpl extends BaseOpenmrsService
    implements HarmonizationPersonAttributeTypeService {

  private HarmonizationServiceDAO dao;

  private HarmonizationPersonAttributeTypeServiceDAO personAttributeServiceDAO;

  /** @param dao the dao to set */
  public void setDao(HarmonizationServiceDAO dao) {
    this.dao = dao;
  }

  /** @return the dao */
  public HarmonizationServiceDAO getDAO() {
    return dao;
  }

  public HarmonizationPersonAttributeTypeServiceDAO getPersonAttributeServiceDAO() {
    return personAttributeServiceDAO;
  }

  public void setPersonAttributeServiceDAO(
      HarmonizationPersonAttributeTypeServiceDAO personAttributeServiceDAO) {
    this.personAttributeServiceDAO = personAttributeServiceDAO;
  }

  @Override
  public List<PersonAttributeTypeDTO> findAllMetadataPersonAttributeTypesNotInProductionServer()
      throws APIException {
    List<PersonAttributeType> mdsPersonAttributeTypes =
        personAttributeServiceDAO.findAllMetadataServerPersonAttributeTypes();
    List<PersonAttributeType> pdsPersonAttributeTypes =
        personAttributeServiceDAO.findAllProductionServerPersonAttributeTypes();
    mdsPersonAttributeTypes.removeAll(pdsPersonAttributeTypes);
    return DTOUtils.fromPersonAttributeTypes(mdsPersonAttributeTypes);
  }

  @Override
  public List<PersonAttributeTypeDTO> findAllProductionPersonAttibuteTypesNotInMetadataServer()
      throws APIException {
    List<PersonAttributeType> mdsPersonAttributeTypes =
        personAttributeServiceDAO.findAllMetadataServerPersonAttributeTypes();
    List<PersonAttributeType> pdsPersonAttributeTypes =
        personAttributeServiceDAO.findAllProductionServerPersonAttributeTypes();
    pdsPersonAttributeTypes.removeAll(mdsPersonAttributeTypes);
    return DTOUtils.fromPersonAttributeTypes(pdsPersonAttributeTypes);
  }

  @Override
  public List<PersonAttributeTypeDTO>
      findAllMetadataPersonAttributeTypesPartialEqualsToProductionServer() throws APIException {
    List<PersonAttributeType> allMDS =
        personAttributeServiceDAO.findAllMetadataServerPersonAttributeTypes();
    List<PersonAttributeType> allPDS =
        personAttributeServiceDAO.findAllProductionServerPersonAttributeTypes();
    List<PersonAttributeType> mdsPersonAttributeTypes =
        this.removePATWithDifferentIDsAndUUIDs(allMDS, allPDS);
    allMDS.removeAll(allPDS);
    mdsPersonAttributeTypes.removeAll(allMDS);
    return DTOUtils.fromPersonAttributeTypes(mdsPersonAttributeTypes);
  }

  @Override
  public List<PersonAttributeTypeDTO>
      findAllProductionPersonAttributeTypesPartialEqualsToMetadataServer() throws APIException {
    List<PersonAttributeType> allMDS =
        personAttributeServiceDAO.findAllMetadataServerPersonAttributeTypes();
    List<PersonAttributeType> allPDS =
        personAttributeServiceDAO.findAllProductionServerPersonAttributeTypes();
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
    List<PersonAttributeType> allMDS =
        personAttributeServiceDAO.findAllMetadataServerPersonAttributeTypes();
    List<PersonAttributeType> allPDS =
        personAttributeServiceDAO.findAllProductionServerPersonAttributeTypes();

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
    List<PersonAttributeType> allPDS =
        personAttributeServiceDAO.findAllProductionServerPersonAttributeTypes();
    List<PersonAttributeType> allMDS =
        personAttributeServiceDAO.findAllMetadataServerPersonAttributeTypes();

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
    return personAttributeServiceDAO
        .findPersonAttributeByPersonAttributeTypeId(
            personAttributeTypeDTO.getPersonAttributeType().getId())
        .size();
  }

  @Override
  public List<PersonAttributeType> findPDSPersonAttributeTypesNotExistsInMDServer()
      throws APIException {
    return this.personAttributeServiceDAO.findPDSPersonAttributeTypesNotExistsInMDServer();
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
            this.personAttributeServiceDAO.getPersonAttributeTypeById(personAttributeType.getId());

        if (found != null) {

          if (!this.personAttributeServiceDAO.isSwappable(found)) {

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
              this.personAttributeServiceDAO.findPersonAttributeByPersonAttributeTypeId(
                  found.getId());
          Integer nextId = this.personAttributeServiceDAO.getNextPersonAttriTypeId();

          this.updateToGivenId(found, nextId, true, relatedPersonAttributes);
        }
        this.personAttributeServiceDAO.saveNotSwappablePersonAttributeType(personAttributeType);
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
            this.personAttributeServiceDAO.getPersonAttributeTypeById(
                mdsPersonAttributeType.getId());

        if (foundMDS != null) {
          if (!this.personAttributeServiceDAO.isSwappable(foundMDS)) {
            throw new APIException(
                String.format(
                    "Cannot update the Production Server PersonAttributeType [ID = {%s}, UUID = {%s}, NAME = {%s}] with the ID {%s} this new ID is already referencing an Existing PersonAttributeType In Metadata Server",
                    pdSPersonAttributeType.getId(),
                    pdSPersonAttributeType.getUuid(),
                    pdSPersonAttributeType.getName(),
                    mdServerEncounterId));
          }
          List<PersonAttribute> personAttributes =
              this.personAttributeServiceDAO.findPersonAttributeByPersonAttributeTypeId(
                  foundMDS.getId());
          Integer nextId = this.personAttributeServiceDAO.getNextPersonAttriTypeId();
          this.updateToGivenId(foundMDS, nextId, true, personAttributes);
        }

        PersonAttributeType foundPDS =
            this.personAttributeServiceDAO.getPersonAttributeTypeById(
                pdSPersonAttributeType.getId());
        if (!this.personAttributeServiceDAO.isSwappable(foundPDS)) {
          throw new APIException(
              String.format(
                  "Cannot update the Production Server PersonAttributeType with ID {%s}, UUID {%s} and NAME {%s}. This PersonAttributeType is a Reference from an PersonAttributeType of Metadata Server",
                  foundPDS.getId(), foundPDS.getUuid(), foundPDS.getName()));
        }
        List<PersonAttribute> personAttributes =
            this.personAttributeServiceDAO.findPersonAttributeByPersonAttributeTypeId(
                foundPDS.getId());
        this.updateToGivenId(foundPDS, mdServerEncounterId, true, personAttributes);
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

  private void updateToGivenId(
      PersonAttributeType personAttributeType,
      Integer personAttributeTypeId,
      boolean swappable,
      List<PersonAttribute> relatedPersonAttributes) {
    this.personAttributeServiceDAO.updatePersonAttributeType(
        personAttributeTypeId, personAttributeType, swappable);
    for (PersonAttribute personAttribute : relatedPersonAttributes) {
      this.personAttributeServiceDAO.updatePersonAttribute(personAttribute, personAttributeTypeId);
    }
  }
}
