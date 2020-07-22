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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.PersonService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationPersonAttributeTypeService;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationPersonAttributeTypeServiceDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.openmrs.module.eptsharmonization.api.exception.UUIDDuplicationException;
import org.openmrs.module.eptsharmonization.api.model.PersonAttributeTypeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** It is a default implementation of {@link HarmonizationPersonAttributeTypeService}. */
@Transactional
@Service("eptsharmonization.harmonizatPersonAttributeTypeService")
public class HarmonizationPersonAttrbuteTypeServiceImpl extends BaseOpenmrsService
    implements HarmonizationPersonAttributeTypeService {

  private HarmonizationServiceDAO harmonizationDAO;
  private PersonService personService;
  private HarmonizationPersonAttributeTypeServiceDAO harmonizationPersonAttributeTypeServiceDAO;

  @Autowired
  public void setHarmonizationDAO(HarmonizationServiceDAO harmonizationDAO) {
    this.harmonizationDAO = harmonizationDAO;
  }

  @Autowired
  public void setHarmonizationPersonAttributeTypeServiceDAO(
      HarmonizationPersonAttributeTypeServiceDAO harmonizationPersonAttributeTypeServiceDAO) {
    this.harmonizationPersonAttributeTypeServiceDAO = harmonizationPersonAttributeTypeServiceDAO;
  }

  @Autowired
  public void setPersonService(PersonService personService) {
    this.personService = personService;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Person Attribute Types"})
  public List<PersonAttributeTypeDTO>
      findAllMetadataPersonAttributeTypesNotContainedInProductionServer() throws APIException {
    this.harmonizationDAO.evictCache();
    List<PersonAttributeType> mdsPersonAttributeTypes =
        harmonizationPersonAttributeTypeServiceDAO.findAllMetadataServerPersonAttributeTypes();
    List<PersonAttributeType> pdsPersonAttributeTypes =
        harmonizationPersonAttributeTypeServiceDAO.findAllProductionServerPersonAttributeTypes();
    mdsPersonAttributeTypes.removeAll(pdsPersonAttributeTypes);
    return DTOUtils.fromPersonAttributeTypes(mdsPersonAttributeTypes);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Person Attribute Types"})
  public List<PersonAttributeTypeDTO>
      findAllProductionPersonAttributeTypesNotContainedInMetadataServer() throws APIException {
    this.harmonizationDAO.evictCache();
    List<PersonAttributeType> pdsPersonAttributeTypes =
        harmonizationPersonAttributeTypeServiceDAO.findAllProductionServerPersonAttributeTypes();
    List<PersonAttributeType> mdsPersonAttributeTypes =
        harmonizationPersonAttributeTypeServiceDAO.findAllMetadataServerPersonAttributeTypes();
    pdsPersonAttributeTypes.removeAll(mdsPersonAttributeTypes);
    return DTOUtils.fromPersonAttributeTypes(pdsPersonAttributeTypes);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Person Attribute Types"})
  public List<PersonAttributeTypeDTO>
      findAllMetadataPersonAttributeTypesPartialEqualsToProductionServer() throws APIException {
    this.harmonizationDAO.evictCache();
    List<PersonAttributeType> allMDS =
        harmonizationPersonAttributeTypeServiceDAO.findAllMetadataServerPersonAttributeTypes();
    List<PersonAttributeType> allPDS =
        harmonizationPersonAttributeTypeServiceDAO.findAllProductionServerPersonAttributeTypes();
    List<PersonAttributeType> mdsPersonAttributeTypes =
        this.removeElementsWithDifferentIDsAndUUIDs(allMDS, allPDS);
    allMDS.removeAll(allPDS);
    mdsPersonAttributeTypes.removeAll(allMDS);
    return DTOUtils.fromPersonAttributeTypes(mdsPersonAttributeTypes);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Person Attribute Types"})
  public List<PersonAttributeTypeDTO>
      findAllProductionPersonAttributeTypesPartialEqualsToMetadataServer() throws APIException {
    this.harmonizationDAO.evictCache();
    List<PersonAttributeType> allPDS =
        harmonizationPersonAttributeTypeServiceDAO.findAllProductionServerPersonAttributeTypes();
    List<PersonAttributeType> allMDS =
        harmonizationPersonAttributeTypeServiceDAO.findAllMetadataServerPersonAttributeTypes();
    List<PersonAttributeType> pdsPersonAttributeTypes =
        this.removeElementsWithDifferentIDsAndUUIDs(allPDS, allMDS);
    allPDS.removeAll(allMDS);
    pdsPersonAttributeTypes.removeAll(allPDS);
    return DTOUtils.fromPersonAttributeTypes(pdsPersonAttributeTypes);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Person Attribute Types"})
  public Map<String, List<PersonAttributeTypeDTO>>
      findAllPersonAttributeTypesWithDifferentNameAndSameUUIDAndID() throws APIException {
    this.harmonizationDAO.evictCache();
    Map<String, List<PersonAttributeTypeDTO>> result = new HashMap<>();
    Map<String, List<PersonAttributeType>> map = findByWithDifferentNameAndSameUUIDAndID();
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromPersonAttributeTypes(map.get(key)));
    }
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Person Attribute Types"})
  public Map<String, List<PersonAttributeTypeDTO>>
      findAllPersonAttributeTypesWithDifferentIDAndSameUUID() throws APIException {
    this.harmonizationDAO.evictCache();
    Map<String, List<PersonAttributeTypeDTO>> result = new HashMap<>();
    Map<String, List<PersonAttributeType>> map = findByWithDifferentIDAndSameUUID();
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromPersonAttributeTypes(map.get(key)));
    }
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Person Attribute Types"})
  public int getNumberOfAffectedPersonAttributes(PersonAttributeTypeDTO personAttributeTypeDTO) {
    this.harmonizationDAO.evictCache();
    return harmonizationPersonAttributeTypeServiceDAO
        .findPersonAttributesByPersonAttributeTypeId(
            personAttributeTypeDTO.getPersonAttributeType().getPersonAttributeTypeId())
        .size();
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Person Attribute Types"})
  public List<PersonAttributeType> findPDSPersonAttributeTypesNotExistsInMDServer()
      throws APIException {
    this.harmonizationDAO.evictCache();
    List<PersonAttributeType> result =
        harmonizationPersonAttributeTypeServiceDAO.findPDSPersonAttributeTypesNotExistsInMDServer();
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Person Attribute Types"})
  public List<PersonAttributeType> findAllNotSwappablePersonAttributeTypes() throws APIException {
    this.harmonizationDAO.evictCache();
    List<PersonAttributeType> result =
        this.harmonizationPersonAttributeTypeServiceDAO.findAllNotSwappable();
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Person Attribute Types"})
  public List<PersonAttributeType> findAllMetadataPersonAttributeTypes() throws APIException {
    this.harmonizationDAO.evictCache();
    List<PersonAttributeType> result =
        this.harmonizationPersonAttributeTypeServiceDAO.findAllMetadataServerPersonAttributeTypes();
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Person Attribute Types"})
  public List<PersonAttributeType> findAllSwappablePersonAttributeTypes() throws APIException {
    this.harmonizationDAO.evictCache();
    List<PersonAttributeType> findAllSwappable =
        this.harmonizationPersonAttributeTypeServiceDAO.findAllSwappable();
    return findAllSwappable;
  }

  @Override
  @Authorized({"Manage Person Attribute Types"})
  public void savePersonAttributeTypesWithDifferentNames(
      Map<String, List<PersonAttributeTypeDTO>> personAttributeTypes) throws APIException {
    this.harmonizationDAO.evictCache();
    for (String key : personAttributeTypes.keySet()) {
      List<PersonAttributeTypeDTO> list = personAttributeTypes.get(key);
      PersonAttributeTypeDTO mdsPersonAttributeType = list.get(0);
      PersonAttributeTypeDTO pdsPersonAttributeType = list.get(1);
      PersonAttributeType personAttributeType =
          this.personService.getPersonAttributeType(
              pdsPersonAttributeType.getPersonAttributeType().getId());
      personAttributeType.setName(mdsPersonAttributeType.getPersonAttributeType().getName());
      personAttributeType.setDescription(
          mdsPersonAttributeType.getPersonAttributeType().getDescription());
      this.personService.savePersonAttributeType(personAttributeType);
    }
  }

  @Override
  @Authorized({"Manage Person Attribute Types"})
  public void saveNewPersonAttributeTypesFromMDS(List<PersonAttributeTypeDTO> personAttributeTypes)
      throws APIException {
    this.harmonizationDAO.evictCache();
    try {
      this.harmonizationDAO.setDisabledCheckConstraints();
      for (PersonAttributeType personAttributeType :
          DTOUtils.fromPersonAttributeTypesDTOs(personAttributeTypes)) {

        PersonAttributeType found =
            this.harmonizationPersonAttributeTypeServiceDAO.getPersonAttributeTypeById(
                personAttributeType.getId());

        if (found != null) {

          if (!this.harmonizationPersonAttributeTypeServiceDAO.isSwappable(found)) {

            throw new APIException(
                String.format(
                    "Cannot Insert Person Attribute Type with ID %s, UUID %s and NAME %s. This ID is being in use by another Person Attribute Type from Metatada server with UUID %s and name %s ",
                    personAttributeType.getId(),
                    personAttributeType.getUuid(),
                    personAttributeType.getName(),
                    found.getUuid(),
                    found.getName()));
          }
          List<PersonAttribute> relatedPersonAttributes =
              this.harmonizationPersonAttributeTypeServiceDAO
                  .findPersonAttributesByPersonAttributeTypeId(found.getId());
          this.updateToNextAvailableID(found, relatedPersonAttributes);
        }
        this.harmonizationPersonAttributeTypeServiceDAO.saveNotSwappablePersonAttributeType(
            personAttributeType);
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
  @Authorized({"Manage Person Attribute Types"})
  public void savePersonAttributeTypesWithDifferentIDAndEqualUUID(
      Map<String, List<PersonAttributeTypeDTO>> mapPersonAttributeTypes) throws APIException {
    this.harmonizationDAO.evictCache();
    try {

      this.harmonizationDAO.setDisabledCheckConstraints();
      for (String uuid : mapPersonAttributeTypes.keySet()) {

        List<PersonAttributeTypeDTO> list = mapPersonAttributeTypes.get(uuid);
        PersonAttributeType mdsPersonAttributeType = list.get(0).getPersonAttributeType();
        PersonAttributeType pdSPersonAttributeType = list.get(1).getPersonAttributeType();
        Integer mdServerPersonAttributeTypeId = mdsPersonAttributeType.getPersonAttributeTypeId();

        PersonAttributeType foundMDS =
            this.harmonizationPersonAttributeTypeServiceDAO.getPersonAttributeTypeById(
                mdsPersonAttributeType.getId());

        if (foundMDS != null) {
          if (!this.harmonizationPersonAttributeTypeServiceDAO.isSwappable(foundMDS)) {
            throw new APIException(
                String.format(
                    "Cannot update the Production Server Person Attribute Type [ID = {%s}, UUID = {%s}, NAME = {%s}] with the ID {%s} this new ID is already referencing an Existing Person Attribute Type In Metadata Server",
                    pdSPersonAttributeType.getId(),
                    pdSPersonAttributeType.getUuid(),
                    pdSPersonAttributeType.getName(),
                    mdServerPersonAttributeTypeId));
          }
          List<PersonAttribute> relatedPersonAttributes =
              this.harmonizationPersonAttributeTypeServiceDAO
                  .findPersonAttributesByPersonAttributeTypeId(foundMDS.getId());
          this.updateToNextAvailableID(foundMDS, relatedPersonAttributes);
        }

        List<PersonAttribute> relatedPersonAttributes =
            this.harmonizationPersonAttributeTypeServiceDAO
                .findPersonAttributesByPersonAttributeTypeId(pdSPersonAttributeType.getId());
        this.updateToGivenId(
            pdSPersonAttributeType, mdServerPersonAttributeTypeId, false, relatedPersonAttributes);
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
  @Authorized({"Manage Person Attribute Types"})
  public void deleteNewPersonAttributeTypesFromPDS(
      List<PersonAttributeTypeDTO> personAttributeTypes) throws APIException {
    this.harmonizationDAO.evictCache();
    for (PersonAttributeType personAttributeType :
        DTOUtils.fromPersonAttributeTypesDTOs(personAttributeTypes)) {
      this.harmonizationPersonAttributeTypeServiceDAO.deletePersonAttributeType(
          personAttributeType);
    }
  }

  @Override
  @Authorized({"Manage Person Attribute Types"})
  public void saveManualMapping(
      Map<PersonAttributeType, PersonAttributeType> mapPersonAttributeTypes)
      throws UUIDDuplicationException, SQLException {
    this.harmonizationDAO.evictCache();
    try {

      this.harmonizationDAO.setDisabledCheckConstraints();
      for (Entry<PersonAttributeType, PersonAttributeType> entry :
          mapPersonAttributeTypes.entrySet()) {

        PersonAttributeType pdSPersonAttributeType = entry.getKey();
        PersonAttributeType mdsPersonAttributeType = entry.getValue();

        PersonAttributeType foundMDSPersonAttributeTypeByUuid =
            this.harmonizationPersonAttributeTypeServiceDAO.getPersonAttributeTypeByUuid(
                mdsPersonAttributeType.getUuid());

        if ((foundMDSPersonAttributeTypeByUuid != null
                && !foundMDSPersonAttributeTypeByUuid
                    .getId()
                    .equals(mdsPersonAttributeType.getId()))
            && (!foundMDSPersonAttributeTypeByUuid.getId().equals(pdSPersonAttributeType.getId())
                && !foundMDSPersonAttributeTypeByUuid
                    .getUuid()
                    .equals(pdSPersonAttributeType.getUuid()))) {

          throw new UUIDDuplicationException(
              String.format(
                  " Cannot Update the Person Attribute Type '%s' to '%s'. There is one entry with NAME='%s', ID='%s' an UUID='%s' ",
                  pdSPersonAttributeType.getName(),
                  mdsPersonAttributeType.getName(),
                  foundMDSPersonAttributeTypeByUuid.getName(),
                  foundMDSPersonAttributeTypeByUuid.getId(),
                  foundMDSPersonAttributeTypeByUuid.getUuid()));
        }

        PersonAttributeType foundPDS =
            this.harmonizationPersonAttributeTypeServiceDAO.getPersonAttributeTypeById(
                pdSPersonAttributeType.getId());

        if (mdsPersonAttributeType.getUuid().equals(pdSPersonAttributeType.getUuid())
            && mdsPersonAttributeType.getId().equals(pdSPersonAttributeType.getId())) {
          if (mdsPersonAttributeType.getId().equals(pdSPersonAttributeType.getId())
              && mdsPersonAttributeType.getName().equals(pdSPersonAttributeType.getName())) {
            return;
          }
        } else {
          List<PersonAttribute> relatedPersonAttributes =
              this.harmonizationPersonAttributeTypeServiceDAO
                  .findPersonAttributesByPersonAttributeTypeId(foundPDS.getId());

          for (PersonAttribute personAttributes : relatedPersonAttributes) {
            this.harmonizationPersonAttributeTypeServiceDAO.updatePersonAttribute(
                personAttributes, mdsPersonAttributeType.getPersonAttributeTypeId());
          }
          this.harmonizationPersonAttributeTypeServiceDAO.deletePersonAttributeType(foundPDS);

          PersonAttributeType foundMDSPersonAttributeTypeByID =
              this.harmonizationPersonAttributeTypeServiceDAO.getPersonAttributeTypeById(
                  mdsPersonAttributeType.getId());
          if (foundMDSPersonAttributeTypeByID == null) {
            this.harmonizationPersonAttributeTypeServiceDAO.saveNotSwappablePersonAttributeType(
                mdsPersonAttributeType);
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

  private List<PersonAttributeType> removeElementsWithDifferentIDsAndUUIDs(
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
        harmonizationPersonAttributeTypeServiceDAO.findAllMetadataServerPersonAttributeTypes();
    List<PersonAttributeType> allPDS =
        harmonizationPersonAttributeTypeServiceDAO.findAllProductionServerPersonAttributeTypes();

    Map<String, List<PersonAttributeType>> map = new TreeMap<>();
    for (PersonAttributeType mdsItem : allMDS) {
      for (PersonAttributeType pdsItem : allPDS) {
        if (mdsItem.getUuid().equals(pdsItem.getUuid())
            && mdsItem.getId() == pdsItem.getId()
            && !mdsItem.getName().trim().equalsIgnoreCase(pdsItem.getName().trim())) {
          map.put(mdsItem.getUuid(), Arrays.asList(mdsItem, pdsItem));
        }
      }
    }
    return map;
  }

  private Map<String, List<PersonAttributeType>> findByWithDifferentIDAndSameUUID() {
    List<PersonAttributeType> allPDS =
        harmonizationPersonAttributeTypeServiceDAO.findAllProductionServerPersonAttributeTypes();
    List<PersonAttributeType> allMDS =
        harmonizationPersonAttributeTypeServiceDAO.findAllMetadataServerPersonAttributeTypes();
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

  private void updateToGivenId(
      PersonAttributeType personAttributeType,
      Integer personAttributeTypeId,
      boolean swappable,
      List<PersonAttribute> relatedPersonAttributes) {
    this.harmonizationPersonAttributeTypeServiceDAO.updatePersonAttributeType(
        personAttributeTypeId, personAttributeType, swappable);

    for (PersonAttribute personAttributeTypes : relatedPersonAttributes) {
      this.harmonizationPersonAttributeTypeServiceDAO.updatePersonAttribute(
          personAttributeTypes, personAttributeTypeId);
    }
  }

  private void updateToNextAvailableID(
      PersonAttributeType personAttributeType, List<PersonAttribute> relatedPersonAttributes) {
    PersonAttributeType updated =
        this.harmonizationPersonAttributeTypeServiceDAO.updateToNextAvailableId(
            personAttributeType);
    for (PersonAttribute encounter : relatedPersonAttributes) {
      this.harmonizationPersonAttributeTypeServiceDAO.updatePersonAttribute(
          encounter, updated.getPersonAttributeTypeId());
    }
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Person Attribute Types"})
  public PersonAttributeType findProductionPersonAttributeTypeByUuid(String uuid)
      throws APIException {
    this.harmonizationDAO.evictCache();
    return personService.getPersonAttributeTypeByUuid(uuid);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Person Attribute Types"})
  public PersonAttributeType findMetadataPersonAttributeTypeByUuid(String uuid)
      throws APIException {
    this.harmonizationDAO.evictCache();
    PersonAttributeType result =
        this.harmonizationPersonAttributeTypeServiceDAO.findMDSPersonAttributeTypeByUuid(uuid);
    return result;
  }
}
