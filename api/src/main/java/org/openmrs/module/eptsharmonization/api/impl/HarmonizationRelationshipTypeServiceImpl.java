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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.PersonService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.eptsharmonization.HarmonizationUtils;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationRelationshipTypeService;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationRelationshipTypeDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.openmrs.module.eptsharmonization.api.exception.UUIDDuplicationException;
import org.openmrs.module.eptsharmonization.api.model.RelationshipTypeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** It is a default implementation of {@link HarmonizationRelationshipTypeService}. */
@Transactional
@Service(HarmonizationRelationshipTypeServiceImpl.BEAN_NAME)
public class HarmonizationRelationshipTypeServiceImpl extends BaseOpenmrsService
    implements HarmonizationRelationshipTypeService {
  public static final String BEAN_NAME = "eptsharmonization.harmonizationRelationshipTypeService";

  private HarmonizationServiceDAO dao;

  private HarmonizationRelationshipTypeDAO harmonizationRelationshipTypeDao;

  private PersonService personService;

  @Autowired
  public void setPersonService(PersonService personService) {
    this.personService = personService;
  }

  @Autowired
  public void setDao(HarmonizationServiceDAO dao) {
    this.dao = dao;
  }

  @Autowired
  public void setHarmonizationRelationshipTypeDao(
      HarmonizationRelationshipTypeDAO harmonizationRelationshipTypeDao) {
    this.harmonizationRelationshipTypeDao = harmonizationRelationshipTypeDao;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Relationship Types"})
  public List<RelationshipTypeDTO>
      findAllMetadataRelationshipTypesNotSharingUuidWithAnyFromProduction() throws APIException {
    List<RelationshipType> mdsRelationshipTypes =
        harmonizationRelationshipTypeDao.findAllMDSRelationshipTypes();
    List<RelationshipType> pdsRelationshipTypes = personService.getAllRelationshipTypes(true);
    mdsRelationshipTypes.removeAll(pdsRelationshipTypes);
    return DTOUtils.fromRelationshipTypes(mdsRelationshipTypes);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Relationship Types"})
  public List<RelationshipTypeDTO>
      findAllProductionRelationshipTypesNotSharingUuidWithAnyFromMetadata() throws APIException {
    List<RelationshipType> pdsRelationshipTypes = personService.getAllRelationshipTypes();
    List<RelationshipType> mdsRelationshipTypes =
        harmonizationRelationshipTypeDao.findAllMDSRelationshipTypes();
    pdsRelationshipTypes.removeAll(mdsRelationshipTypes);
    return DTOUtils.fromRelationshipTypes(pdsRelationshipTypes);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Relationship Types"})
  public List<RelationshipTypeDTO>
      findAllUselessProductionRelationshipTypesNotSharingUuidWithAnyFromMetadata()
          throws APIException {
    List<RelationshipTypeDTO> allInPDSNotSharingUuidWithMDS =
        findAllProductionRelationshipTypesNotSharingUuidWithAnyFromMetadata();
    List<RelationshipTypeDTO> uselessOnes = new ArrayList<>();
    for (RelationshipTypeDTO relationshipTypeDTO : allInPDSNotSharingUuidWithMDS) {
      int count = getNumberOfAffectedRelationships(relationshipTypeDTO);
      if (count == 0) {
        uselessOnes.add(relationshipTypeDTO);
      }
    }
    return uselessOnes;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Relationship Types"})
  public Map<RelationshipTypeDTO, Integer>
      findAllUsedProductionRelationshipTypesNotSharingUuidWithAnyFromMetadata()
          throws APIException {
    List<RelationshipTypeDTO> allInPDSNotSharingUuidWithMDS =
        findAllProductionRelationshipTypesNotSharingUuidWithAnyFromMetadata();
    Map<RelationshipTypeDTO, Integer> usedOnes = new HashMap<>();
    for (RelationshipTypeDTO relationshipTypeDTO : allInPDSNotSharingUuidWithMDS) {
      int count = getNumberOfAffectedRelationships(relationshipTypeDTO);
      if (count > 0) {
        usedOnes.put(relationshipTypeDTO, count);
      }
    }
    return usedOnes;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Relationship Types"})
  public List<RelationshipTypeDTO> findAllMetadataRelationshipTypesNotInHarmonyWithProduction()
      throws APIException {
    List<RelationshipType> mdsRelationshipTypes =
        harmonizationRelationshipTypeDao.findAllMDSRelationshipTypes();
    List<RelationshipType> pdsRelationshipTypes = personService.getAllRelationshipTypes(true);
    HarmonizationUtils.removeAllRelationshipTypeHarmonizedElements(
        mdsRelationshipTypes, pdsRelationshipTypes);
    return DTOUtils.fromRelationshipTypes(mdsRelationshipTypes);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Relationship Types"})
  public List<RelationshipTypeDTO> findAllProductionRelationshipTypesNotInHarmonyWithMetadata()
      throws APIException {
    List<RelationshipType> notInHarmonyWithMetadata =
        harmonizationRelationshipTypeDao.findPDSRelationshipTypesNotExistsInMDServer();
    return DTOUtils.fromRelationshipTypes(notInHarmonyWithMetadata);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Relationship Types"})
  public Map<String, List<RelationshipTypeDTO>>
      findAllRelationshipTypeWithDifferentTypesAndSameUUIDAndID() throws APIException {
    List<RelationshipType> allPDS = personService.getAllRelationshipTypes();
    List<RelationshipType> allMDS = harmonizationRelationshipTypeDao.findAllMDSRelationshipTypes();
    Map<String, List<RelationshipTypeDTO>> result = new HashMap<>();
    Map<String, List<RelationshipType>> map =
        HarmonizationUtils.findRelationshipTypeWithDifferentTypesAndSameUUIDAndID(allPDS, allMDS);
    for (Map.Entry<String, List<RelationshipType>> entry : map.entrySet()) {
      result.put(entry.getKey(), DTOUtils.fromRelationshipTypes(entry.getValue()));
    }
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Relationship Types"})
  public Map<String, List<RelationshipTypeDTO>> findAllRelationshipTypesWithDifferentIDAndSameUUID()
      throws APIException {
    List<RelationshipType> allPDS = personService.getAllRelationshipTypes();
    List<RelationshipType> allMDS = harmonizationRelationshipTypeDao.findAllMDSRelationshipTypes();
    Map<String, List<RelationshipTypeDTO>> result = new HashMap<>();
    Map<String, List<RelationshipType>> map =
        HarmonizationUtils.findElementsWithDifferentIdsSameUuids(allPDS, allMDS);
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromRelationshipTypes(map.get(key)));
    }
    return result;
  }

  @Override
  public int getNumberOfAffectedRelationships(RelationshipTypeDTO relationshipTypeDTO) {
    return harmonizationRelationshipTypeDao.getCountOfRelationshipsByRelationshipType(
        relationshipTypeDTO.getRelationshipType());
  }

  @Override
  public RelationshipType findMDSRelationshipTypeByUuid(String uuid) throws APIException {
    return this.harmonizationRelationshipTypeDao.findMDSRelationshipTypeByUuid(uuid);
  }

  @Override
  public RelationshipType findPDSRelationshipTypeByUuid(String uuid) throws APIException {
    return this.harmonizationRelationshipTypeDao.findPDSRelationshipTypeByUuid(uuid);
  }

  @Override
  @Authorized({"Manage Relationship Types"})
  public void saveRelationshipTypesWithDifferentNames(
      Map<String, List<RelationshipTypeDTO>> relationshipTypes) throws APIException {
    for (String key : relationshipTypes.keySet()) {
      List<RelationshipTypeDTO> list = relationshipTypes.get(key);
      RelationshipTypeDTO pdsRelationship = list.get(0);
      RelationshipTypeDTO mdsRelationship = list.get(1);
      RelationshipType relationshipType =
          personService.getRelationshipType(pdsRelationship.getRelationshipType().getId());
      relationshipType.setName(mdsRelationship.getRelationshipType().getName());
      relationshipType.setDescription(mdsRelationship.getRelationshipType().getDescription());
      personService.saveRelationshipType(relationshipType);
    }
  }

  @Override
  @Authorized({"Manage Relationship Types"})
  @Transactional
  public void saveNewRelationshipTypeFromMetadata(RelationshipTypeDTO relationshipTypeDTO)
      throws APIException {
    try {
      dao.setDisabledCheckConstraints();
      saveNewRelationshipTypeFromDTO(relationshipTypeDTO);
    } catch (Exception e) {
      throw new APIException(e);
    } finally {
      try {
        dao.setEnableCheckConstraints();
      } catch (Exception e) {
        throw new APIException(e);
      }
    }
  }

  @Override
  @Authorized({"Manage Relationship Types"})
  @Transactional(propagation = Propagation.REQUIRED)
  public void saveNewRelationshipTypesFromMetadata(
      List<RelationshipTypeDTO> relationshipTypeDTOList) throws APIException {
    try {
      dao.setDisabledCheckConstraints();
      for (RelationshipTypeDTO relationshipTypeDTO : relationshipTypeDTOList) {
        saveNewRelationshipTypeFromDTO(relationshipTypeDTO);
      }
    } catch (Exception e) {
      throw new APIException(e);
    } finally {
      try {
        dao.setEnableCheckConstraints();
      } catch (Exception e) {
        throw new APIException(e);
      }
    }
  }

  @Override
  public void updateRelationshipTypesFromProductionWithSameUuidWithInformationFromMetadata(
      Map<String, List<RelationshipTypeDTO>> mapRelationshipTypes) throws APIException {
    try {
      this.dao.setDisabledCheckConstraints();
      for (String uuid : mapRelationshipTypes.keySet()) {
        List<RelationshipTypeDTO> list = mapRelationshipTypes.get(uuid);
        RelationshipType pdsRelationshipType = list.get(0).getRelationshipType();
        RelationshipType mdsRelationshipType = list.get(1).getRelationshipType();
        Integer mdsRelationshipTypeId = pdsRelationshipType.getRelationshipTypeId();

        RelationshipType foundPDS = personService.getRelationshipType(mdsRelationshipType.getId());
        if (foundPDS != null && !foundPDS.getUuid().equals(mdsRelationshipType.getUuid())) {
          moveUnrelatedProductionRelationshipType(foundPDS);
        }

        if (!this.harmonizationRelationshipTypeDao.isSwappable(pdsRelationshipType)) {
          throw new APIException(
              String.format(
                  "Cannot update the Production Server Relationship type [ID = {%s}, UUID = {%s}, NAME = {%s}] with the ID {%s} "
                      + "this new ID is already referencing an Existing Relationship Type In Metadata Server",
                  mdsRelationshipType.getId(),
                  mdsRelationshipType.getUuid(),
                  mdsRelationshipType.getName(),
                  mdsRelationshipTypeId));
        }
        List<Relationship> relatedRelationships =
            harmonizationRelationshipTypeDao.findRelationshipsByRelationshipType(
                pdsRelationshipType);
        // Get a fresh copy from the database.
        pdsRelationshipType =
            personService.getRelationshipType(pdsRelationshipType.getRelationshipTypeId());
        overwriteRelationshipType(pdsRelationshipType, mdsRelationshipType, relatedRelationships);
      }

    } catch (Exception e) {
      if (e instanceof APIException) throw (APIException) e;
      throw new APIException(e.getMessage(), e);
    } finally {
      try {
        this.dao.setEnableCheckConstraints();
      } catch (Exception e) {
        throw new APIException(e.getMessage(), e);
      }
    }
  }

  @Override
  @Transactional
  public void replacePDSRelationshipTypesWithSameUuidWithThoseFromMDS(
      Map<String, List<RelationshipTypeDTO>> relationshipTypesDTO) throws APIException {
    try {
      dao.setDisabledCheckConstraints();
      for (String uuid : relationshipTypesDTO.keySet()) {
        List<RelationshipTypeDTO> list = relationshipTypesDTO.get(uuid);
        RelationshipType pdsRelationshipType = list.get(0).getRelationshipType();
        RelationshipType mdsRelationshipType = list.get(1).getRelationshipType();

        RelationshipType foundPDS = personService.getRelationshipType(mdsRelationshipType.getId());
        if (foundPDS != null && !foundPDS.getUuid().equals(mdsRelationshipType.getUuid())) {
          moveUnrelatedProductionRelationshipType(foundPDS);
        }

        if (pdsRelationshipType
            .getRelationshipTypeId()
            .equals(mdsRelationshipType.getRelationshipTypeId())) {
          List<Relationship> relatedRelationships =
              harmonizationRelationshipTypeDao.findRelationshipsByRelationshipType(
                  pdsRelationshipType);
          Integer nextId = harmonizationRelationshipTypeDao.getNextRelationshipTypeId();
          harmonizationRelationshipTypeDao.updateRelationshipType(
              pdsRelationshipType, nextId, UUID.randomUUID().toString());
          for (Relationship relationship : relatedRelationships) {
            this.harmonizationRelationshipTypeDao.updateRelationship(relationship, nextId);
          }
        } else {
          // Simply assign new uuid
          harmonizationRelationshipTypeDao.updateRelationshipType(
              pdsRelationshipType, UUID.randomUUID().toString());
        }

        // Save the one from metadata server
        harmonizationRelationshipTypeDao.insertRelationshipType(mdsRelationshipType);
      }
    } catch (Exception e) {
      e.printStackTrace();
      if (e instanceof APIException) throw (APIException) e;
      throw new APIException(e.getMessage(), e);
    } finally {
      try {
        dao.setEnableCheckConstraints();
      } catch (Exception e) {
        throw new APIException(e.getMessage(), e);
      }
    }
  }

  @Override
  @Authorized({"Manage Relationship Types"})
  public void deleteRelationshipTypesFromProduction(List<RelationshipTypeDTO> relationshipTypes)
      throws APIException {
    for (RelationshipType relationshipType : DTOUtils.fromRelationshipTypeDTOs(relationshipTypes)) {
      personService.purgeRelationshipType(relationshipType);
    }
  }

  @Override
  public void saveManualRelationshipTypeMappings(
      Map<RelationshipType, RelationshipType> manualRelationshipTypeMappings) throws APIException {
    this.dao.evictCache();

    try {
      this.dao.setDisabledCheckConstraints();

      for (Entry<RelationshipType, RelationshipType> entry :
          manualRelationshipTypeMappings.entrySet()) {

        RelationshipType pdsRelationshipType = entry.getKey();
        RelationshipType mdsRelationshipType = entry.getValue();

        RelationshipType foundMDSRelationshipTypeByUuid =
            this.harmonizationRelationshipTypeDao.findPDSRelationshipTypeByUuid(
                mdsRelationshipType.getUuid());

        if ((foundMDSRelationshipTypeByUuid != null
                && !foundMDSRelationshipTypeByUuid.getId().equals(mdsRelationshipType.getId()))
            && (!foundMDSRelationshipTypeByUuid.getId().equals(pdsRelationshipType.getId())
                && !foundMDSRelationshipTypeByUuid
                    .getUuid()
                    .equals(pdsRelationshipType.getUuid()))) {

          throw new UUIDDuplicationException(
              String.format(
                  " Cannot Update the RelationshipType '%s' to '%s'. There is one entry with NAME='%s', ID='%s' an UUID='%s' ",
                  pdsRelationshipType.getaIsToB(),
                  mdsRelationshipType.getaIsToB(),
                  foundMDSRelationshipTypeByUuid.getaIsToB(),
                  foundMDSRelationshipTypeByUuid.getId(),
                  foundMDSRelationshipTypeByUuid.getUuid()));
        }

        if (mdsRelationshipType.getUuid().equals(pdsRelationshipType.getUuid())
            && mdsRelationshipType.getId().equals(pdsRelationshipType.getId())
            && mdsRelationshipType.getaIsToB().equalsIgnoreCase(pdsRelationshipType.getaIsToB())
            && mdsRelationshipType.getbIsToA().equalsIgnoreCase(pdsRelationshipType.getbIsToA())) {
          return;
        } else {
          this.dao.evictCache();
          RelationshipType foundPDS =
              this.personService.getRelationshipType(pdsRelationshipType.getId());

          List<Relationship> relationships =
              harmonizationRelationshipTypeDao.getRelashionshipsByType(foundPDS);

          for (Relationship relationship : relationships) {
            harmonizationRelationshipTypeDao.updateRelationship(
                relationship, mdsRelationshipType.getRelationshipTypeId());
          }
          personService.purgeRelationshipType(foundPDS);

          RelationshipType foundMDSRelationshipTypeID =
              this.personService.getRelationshipType(mdsRelationshipType.getId());

          if (foundMDSRelationshipTypeID == null) {
            this.harmonizationRelationshipTypeDao.insertRelationshipType(mdsRelationshipType);
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
      if (e instanceof APIException) throw (APIException) e;
      throw new APIException(e.getMessage(), e);
    } finally {
      try {
        dao.setEnableCheckConstraints();
      } catch (Exception e) {
        throw new APIException(e.getMessage(), e);
      }
    }
  }

  private void updateToGivenId(
      RelationshipType relationshipType,
      Integer relationshipTypeId,
      List<Relationship> relatedRelationships) {
    this.harmonizationRelationshipTypeDao.updateRelationshipType(
        relationshipType, relationshipTypeId);
    for (Relationship relationship : relatedRelationships) {
      this.harmonizationRelationshipTypeDao.updateRelationship(relationship, relationshipTypeId);
    }
  }

  private void overwriteRelationshipType(
      RelationshipType pdsToOverwrite,
      RelationshipType fromMds,
      List<Relationship> relatedPdsRelationships) {
    harmonizationRelationshipTypeDao.overwriteRelationshipTypeDetails(pdsToOverwrite, fromMds);
    for (Relationship relationship : relatedPdsRelationships) {
      harmonizationRelationshipTypeDao.updateRelationship(
          relationship, pdsToOverwrite.getRelationshipTypeId());
    }
  }

  private void moveUnrelatedProductionRelationshipType(RelationshipType toBeMoved)
      throws APIException {
    if (!this.harmonizationRelationshipTypeDao.isSwappable(toBeMoved)) {
      throw new APIException(
          String.format(
              "Cannot update the Production Server Relationship type with ID {%s}, UUID {%s} and NAME {%s}. This Relationship Type is a "
                  + "reference from an Relationship Type of Metadata Server",
              toBeMoved.getId(), toBeMoved.getUuid(), toBeMoved.getName()));
    }
    List<Relationship> relatedRelationships =
        harmonizationRelationshipTypeDao.findRelationshipsByRelationshipType(toBeMoved);
    Integer nextId = harmonizationRelationshipTypeDao.getNextRelationshipTypeId();
    updateToGivenId(toBeMoved, nextId, relatedRelationships);
  }

  private void saveNewRelationshipTypeFromDTO(RelationshipTypeDTO relationshipTypeDTO)
      throws APIException {
    RelationshipType relationshipTypeFromDTO = relationshipTypeDTO.getRelationshipType();
    RelationshipType found =
        personService.getRelationshipType(relationshipTypeFromDTO.getRelationshipTypeId());
    if (found != null) {

      if (!this.harmonizationRelationshipTypeDao.isSwappable(found)) {
        throw new APIException(
            String.format(
                "Cannot Insert Relationship type with ID %s, UUID %s and NAME %s. This ID is being in use by another Relationship type from Metadata server with UUID %s and name %s ",
                relationshipTypeFromDTO.getId(),
                relationshipTypeFromDTO.getUuid(),
                relationshipTypeFromDTO.getName(),
                found.getUuid(),
                found.getName()));
      }
      List<Relationship> relatedRelationships =
          this.harmonizationRelationshipTypeDao.findRelationshipsByRelationshipType(
              relationshipTypeFromDTO);
      Integer nextId = this.harmonizationRelationshipTypeDao.getNextRelationshipTypeId();
      this.updateToGivenId(found, nextId, relatedRelationships);
    }
    this.harmonizationRelationshipTypeDao.insertRelationshipType(relationshipTypeFromDTO);
  }
}
