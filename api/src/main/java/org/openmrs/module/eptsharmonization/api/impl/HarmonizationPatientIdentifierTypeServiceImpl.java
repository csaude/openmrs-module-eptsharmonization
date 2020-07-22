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
import org.apache.commons.lang3.StringUtils;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationPatientIdentifierTypeService;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationPatientIdentifierTypeServiceDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.openmrs.module.eptsharmonization.api.exception.UUIDDuplicationException;
import org.openmrs.module.eptsharmonization.api.model.PatientIdentifierTypeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** It is a default implementation of {@link HarmonizationPatientIdentifierTypeService}. */
@Transactional
@Service("eptsharmonization.harmonizatPatientIdentifierTypeService")
public class HarmonizationPatientIdentifierTypeServiceImpl extends BaseOpenmrsService
    implements HarmonizationPatientIdentifierTypeService {

  private HarmonizationServiceDAO harmonizationDAO;
  private PatientService patientService;
  private HarmonizationPatientIdentifierTypeServiceDAO harmonizationPatientIdentifierTypeServiceDAO;

  @Autowired
  public void setHarmonizationDAO(HarmonizationServiceDAO harmonizationDAO) {
    this.harmonizationDAO = harmonizationDAO;
  }

  @Autowired
  public void setHarmonizationPatientIdentifierTypeServiceDAO(
      HarmonizationPatientIdentifierTypeServiceDAO harmonizationPatientIdentifierTypeServiceDAO) {
    this.harmonizationPatientIdentifierTypeServiceDAO =
        harmonizationPatientIdentifierTypeServiceDAO;
  }

  @Autowired
  public void setPatientService(PatientService patientService) {
    this.patientService = patientService;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Patient Identifier Types"})
  public List<PatientIdentifierTypeDTO> findAllFromMDSNotContainedInPDS() throws APIException {
    this.harmonizationDAO.evictCache();
    List<PatientIdentifierType> mdsPatientIdentifierTypes = findAllFromMDS();
    List<PatientIdentifierType> pdsPatientIdentifierTypes =
        harmonizationPatientIdentifierTypeServiceDAO.findAllPDSServerPatientIdentifierTypes();
    mdsPatientIdentifierTypes.removeAll(pdsPatientIdentifierTypes);
    return DTOUtils.fromPatientIdentifierTypes(mdsPatientIdentifierTypes);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Patient Identifier Types"})
  public List<PatientIdentifierTypeDTO> findAllFromPDSNotContainedInMDS() throws APIException {
    this.harmonizationDAO.evictCache();
    List<PatientIdentifierType> pdsPatientIdentifierTypes =
        harmonizationPatientIdentifierTypeServiceDAO.findAllPDSServerPatientIdentifierTypes();
    List<PatientIdentifierType> mdsPatientIdentifierTypes = findAllFromMDS();
    pdsPatientIdentifierTypes.removeAll(mdsPatientIdentifierTypes);
    return DTOUtils.fromPatientIdentifierTypes(pdsPatientIdentifierTypes);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Patient Identifier Types"})
  public Map<String, List<PatientIdentifierTypeDTO>> findAllWithDifferentNameAndSameUUIDAndID()
      throws APIException {
    this.harmonizationDAO.evictCache();
    Map<String, List<PatientIdentifierTypeDTO>> result = new HashMap<>();
    Map<String, List<PatientIdentifierType>> map = findByWithDifferentNameAndSameUUIDAndID();
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromPatientIdentifierTypes(map.get(key)));
    }
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Patient Identifier Types"})
  public Map<String, List<PatientIdentifierTypeDTO>>
      findAllWithDifferentDetailsAndSameNameUUIDAndID() throws APIException {
    this.harmonizationDAO.evictCache();
    Map<String, List<PatientIdentifierTypeDTO>> result = new HashMap<>();
    Map<String, List<PatientIdentifierType>> map = findByWithDifferentDetailsAndSameNameUUIDAndID();
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromPatientIdentifierTypes(map.get(key)));
    }
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Patient Identifier Types"})
  public Map<String, List<PatientIdentifierTypeDTO>> findAllWithDifferentIDAndSameUUID()
      throws APIException {
    this.harmonizationDAO.evictCache();
    Map<String, List<PatientIdentifierTypeDTO>> result = new HashMap<>();
    Map<String, List<PatientIdentifierType>> map = findByWithDifferentIDAndSameUUID();
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromPatientIdentifierTypes(map.get(key)));
    }
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Patient Identifier Types"})
  public int getNumberOfAffectedPatientIdentifiers(
      PatientIdentifierTypeDTO patientIdentifierTypeDTO) {
    this.harmonizationDAO.evictCache();
    return harmonizationPatientIdentifierTypeServiceDAO
        .findPatientIdentifiersByPatientIdentifierTypeId(
            patientIdentifierTypeDTO.getPatientIdentifierType().getPatientIdentifierTypeId())
        .size();
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Patient Identifier Types"})
  public List<PatientIdentifierType> findAllNotSwappable() throws APIException {
    this.harmonizationDAO.evictCache();
    List<PatientIdentifierType> result =
        this.harmonizationPatientIdentifierTypeServiceDAO.findAllNotSwappable();
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Patient Identifier Types"})
  public List<PatientIdentifierType> findAllFromMDS() throws APIException {
    this.harmonizationDAO.evictCache();
    List<PatientIdentifierType> result =
        this.harmonizationPatientIdentifierTypeServiceDAO.findAllMDSServerPatientIdentifierTypes();
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Patient Identifier Types"})
  public List<PatientIdentifierType> findAllSwappable() throws APIException {
    this.harmonizationDAO.evictCache();
    List<PatientIdentifierType> findAllSwappable =
        this.harmonizationPatientIdentifierTypeServiceDAO.findAllSwappable();
    return findAllSwappable;
  }

  @Override
  public PatientIdentifierType findMDSPatientIdentifierTypeByUuid(String uuid) throws APIException {
    return this.harmonizationPatientIdentifierTypeServiceDAO.findMDSPatientIdentifierTypeByUuid(
        uuid);
  }

  @Override
  public PatientIdentifierType findPDSPatientIdentifierTypeByUuid(String uuid) throws APIException {
    return this.harmonizationPatientIdentifierTypeServiceDAO.findPDSPatientIdentifierTypeByUuid(
        uuid);
  }

  @Override
  @Authorized({"Manage Patient Identifier Types"})
  public void saveWithDifferentNames(
      Map<String, List<PatientIdentifierTypeDTO>> patientIdentifierTypes) throws APIException {
    this.harmonizationDAO.evictCache();
    for (String key : patientIdentifierTypes.keySet()) {
      List<PatientIdentifierTypeDTO> list = patientIdentifierTypes.get(key);
      PatientIdentifierType mdsPatientIdentifierType = list.get(0).getPatientIdentifierType();
      PatientIdentifierType pdsPatientIdentifierType = list.get(1).getPatientIdentifierType();
      PatientIdentifierType patientIdentifierType =
          this.patientService.getPatientIdentifierType(pdsPatientIdentifierType.getId());
      patientIdentifierType.setName(mdsPatientIdentifierType.getName());
      patientIdentifierType.setDescription(mdsPatientIdentifierType.getDescription());
      this.patientService.savePatientIdentifierType(patientIdentifierType);
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  @Authorized({"Manage Patient Identifier Types"})
  public void saveWithDifferentDetails(
      Map<String, List<PatientIdentifierTypeDTO>> patientIdentifierTypes) throws APIException {
    this.harmonizationDAO.evictCache();
    for (String key : patientIdentifierTypes.keySet()) {
      List<PatientIdentifierTypeDTO> list = patientIdentifierTypes.get(key);
      PatientIdentifierType mdsPatientIdentifierType = list.get(0).getPatientIdentifierType();
      PatientIdentifierType pdsPatientIdentifierType = list.get(1).getPatientIdentifierType();
      PatientIdentifierType patientIdentifierType =
          this.patientService.getPatientIdentifierType(pdsPatientIdentifierType.getId());
      patientIdentifierType.setFormat(mdsPatientIdentifierType.getFormat());
      patientIdentifierType.setCheckDigit(mdsPatientIdentifierType.getCheckDigit());
      patientIdentifierType.setRequired(mdsPatientIdentifierType.getRequired());
      this.patientService.savePatientIdentifierType(patientIdentifierType);
    }
  }

  @Override
  @Authorized({"Manage Patient Identifier Types"})
  public void saveNewFromMDS(List<PatientIdentifierTypeDTO> patientIdentifierTypes)
      throws APIException {
    this.harmonizationDAO.evictCache();
    try {
      this.harmonizationDAO.setDisabledCheckConstraints();
      for (PatientIdentifierType patientIdentifierType :
          DTOUtils.fromPatientIdentifierTypesDTOs(patientIdentifierTypes)) {

        PatientIdentifierType found =
            this.harmonizationPatientIdentifierTypeServiceDAO.getPatientIdentifierTypeById(
                patientIdentifierType.getId());

        if (found != null) {

          if (!this.harmonizationPatientIdentifierTypeServiceDAO.isSwappable(found)) {

            throw new APIException(
                String.format(
                    "Cannot Insert Patient Identifier Type with ID %s, UUID %s and NAME %s. This ID is being in use by another Patient Identifier Type from Metatada server with UUID %s and name %s ",
                    patientIdentifierType.getId(),
                    patientIdentifierType.getUuid(),
                    patientIdentifierType.getName(),
                    found.getUuid(),
                    found.getName()));
          }
          List<PatientIdentifier> relatedPatientIdentifiers =
              this.harmonizationPatientIdentifierTypeServiceDAO
                  .findPatientIdentifiersByPatientIdentifierTypeId(found.getId());
          this.updateToNextAvailableID(found, relatedPatientIdentifiers);
        }
        this.harmonizationPatientIdentifierTypeServiceDAO.saveNotSwappablePatientIdentifierType(
            patientIdentifierType);
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
  @Authorized({"Manage Patient Identifier Types"})
  public void saveWithDifferentIDAndEqualUUID(
      Map<String, List<PatientIdentifierTypeDTO>> mapPatientIdentifierTypes) throws APIException {
    this.harmonizationDAO.evictCache();
    try {

      this.harmonizationDAO.setDisabledCheckConstraints();
      for (String uuid : mapPatientIdentifierTypes.keySet()) {

        List<PatientIdentifierTypeDTO> list = mapPatientIdentifierTypes.get(uuid);
        PatientIdentifierType mdsPatientIdentifierType = list.get(0).getPatientIdentifierType();
        PatientIdentifierType pdSPatientIdentifierType = list.get(1).getPatientIdentifierType();
        Integer mdServerPatientIdentifierTypeId =
            mdsPatientIdentifierType.getPatientIdentifierTypeId();

        PatientIdentifierType foundMDS =
            this.harmonizationPatientIdentifierTypeServiceDAO.getPatientIdentifierTypeById(
                mdsPatientIdentifierType.getId());

        if (foundMDS != null) {
          if (!this.harmonizationPatientIdentifierTypeServiceDAO.isSwappable(foundMDS)) {
            throw new APIException(
                String.format(
                    "Cannot update the Production Server Patient Identifier Type [ID = {%s}, UUID = {%s}, NAME = {%s}] with the ID {%s} this new ID is already referencing an Existing Patient Identifier Type In Metadata Server",
                    pdSPatientIdentifierType.getId(),
                    pdSPatientIdentifierType.getUuid(),
                    pdSPatientIdentifierType.getName(),
                    mdServerPatientIdentifierTypeId));
          }
          List<PatientIdentifier> relatedPatientIdentifiers =
              this.harmonizationPatientIdentifierTypeServiceDAO
                  .findPatientIdentifiersByPatientIdentifierTypeId(foundMDS.getId());
          this.updateToNextAvailableID(foundMDS, relatedPatientIdentifiers);
        }

        PatientIdentifierType foundPDS =
            this.harmonizationPatientIdentifierTypeServiceDAO.getPatientIdentifierTypeById(
                pdSPatientIdentifierType.getId());
        if (!this.harmonizationPatientIdentifierTypeServiceDAO.isSwappable(foundPDS)) {
          throw new APIException(
              String.format(
                  "Cannot update the Production Server Patient Identifier Type with ID {%s}, UUID {%s} and NAME {%s}. This Patient Identifier Type is a Reference from an Patient Identifier Type of Metadata Server",
                  foundPDS.getId(), foundPDS.getUuid(), foundPDS.getName()));
        }
        List<PatientIdentifier> relatedPatientIdentifiers =
            this.harmonizationPatientIdentifierTypeServiceDAO
                .findPatientIdentifiersByPatientIdentifierTypeId(foundPDS.getId());
        this.updateToGivenId(
            foundPDS, mdServerPatientIdentifierTypeId, false, relatedPatientIdentifiers);
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
  @Authorized({"Manage Patient Identifier Types"})
  public void deleteNewFromPDS(List<PatientIdentifierTypeDTO> patientIdentifierTypes)
      throws APIException {
    this.harmonizationDAO.evictCache();
    for (PatientIdentifierType patientIdentifierType :
        DTOUtils.fromPatientIdentifierTypesDTOs(patientIdentifierTypes)) {
      this.harmonizationPatientIdentifierTypeServiceDAO.deletePatientIdentifierType(
          patientIdentifierType);
    }
  }

  @Override
  @Authorized({"Manage Patient Identifier Types"})
  public void saveManualMapping(
      Map<PatientIdentifierType, PatientIdentifierType> mapPatientIdentifierTypes)
      throws UUIDDuplicationException, SQLException {
    this.harmonizationDAO.evictCache();
    try {

      this.harmonizationDAO.setDisabledCheckConstraints();
      for (Entry<PatientIdentifierType, PatientIdentifierType> entry :
          mapPatientIdentifierTypes.entrySet()) {

        PatientIdentifierType pdSPatientIdentifierType = entry.getKey();
        PatientIdentifierType mdsPatientIdentifierType = entry.getValue();

        PatientIdentifierType foundMDSPatientIdentifierTypeByUuid =
            this.harmonizationPatientIdentifierTypeServiceDAO.getPatientIdentifierTypeByUuid(
                mdsPatientIdentifierType.getUuid());

        if ((foundMDSPatientIdentifierTypeByUuid != null
                && !foundMDSPatientIdentifierTypeByUuid
                    .getId()
                    .equals(mdsPatientIdentifierType.getId()))
            && (!foundMDSPatientIdentifierTypeByUuid
                    .getId()
                    .equals(pdSPatientIdentifierType.getId())
                && !foundMDSPatientIdentifierTypeByUuid
                    .getUuid()
                    .equals(pdSPatientIdentifierType.getUuid()))) {

          throw new UUIDDuplicationException(
              String.format(
                  " Cannot Update the PatientIdentifierType '%s' to '%s'. There is one entry with NAME='%s', ID='%s' an UUID='%s' ",
                  pdSPatientIdentifierType.getName(),
                  mdsPatientIdentifierType.getName(),
                  foundMDSPatientIdentifierTypeByUuid.getName(),
                  foundMDSPatientIdentifierTypeByUuid.getId(),
                  foundMDSPatientIdentifierTypeByUuid.getUuid()));
        }

        PatientIdentifierType foundPDS =
            this.harmonizationPatientIdentifierTypeServiceDAO.getPatientIdentifierTypeById(
                pdSPatientIdentifierType.getId());

        if (mdsPatientIdentifierType.getUuid().equals(pdSPatientIdentifierType.getUuid())
            && mdsPatientIdentifierType.getId().equals(pdSPatientIdentifierType.getId())) {

          if (mdsPatientIdentifierType
              .getName()
              .equalsIgnoreCase(pdSPatientIdentifierType.getName())) {
            return;
          }
          foundPDS.setName(mdsPatientIdentifierType.getName());
          foundPDS.setDescription(mdsPatientIdentifierType.getDescription());
          this.patientService.savePatientIdentifierType(foundPDS);

        } else {
          List<PatientIdentifier> relatedPatientIdentifiers =
              this.harmonizationPatientIdentifierTypeServiceDAO
                  .findPatientIdentifiersByPatientIdentifierTypeId(foundPDS.getId());

          for (PatientIdentifier patientIdentifiers : relatedPatientIdentifiers) {
            this.harmonizationPatientIdentifierTypeServiceDAO.updatePatientIdentifier(
                patientIdentifiers, mdsPatientIdentifierType.getPatientIdentifierTypeId());
          }
          this.harmonizationPatientIdentifierTypeServiceDAO.deletePatientIdentifierType(foundPDS);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      try {
        this.harmonizationDAO.setEnableCheckConstraints();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private Map<String, List<PatientIdentifierType>> findByWithDifferentNameAndSameUUIDAndID() {
    List<PatientIdentifierType> allMDS =
        harmonizationPatientIdentifierTypeServiceDAO.findAllMDSServerPatientIdentifierTypes();
    List<PatientIdentifierType> allPDS =
        harmonizationPatientIdentifierTypeServiceDAO.findAllPDSServerPatientIdentifierTypes();

    Map<String, List<PatientIdentifierType>> map = new TreeMap<>();
    for (PatientIdentifierType mdsItem : allMDS) {
      for (PatientIdentifierType pdsItem : allPDS) {
        if (mdsItem.getUuid().equals(pdsItem.getUuid())
            && mdsItem.getId() == pdsItem.getId()
            && !mdsItem.getName().equalsIgnoreCase(pdsItem.getName())) {
          map.put(mdsItem.getUuid(), Arrays.asList(mdsItem, pdsItem));
        }
      }
    }
    return map;
  }

  @SuppressWarnings("deprecation")
  private Map<String, List<PatientIdentifierType>>
      findByWithDifferentDetailsAndSameNameUUIDAndID() {
    List<PatientIdentifierType> allMDS =
        harmonizationPatientIdentifierTypeServiceDAO.findAllMDSServerPatientIdentifierTypes();
    List<PatientIdentifierType> allPDS =
        harmonizationPatientIdentifierTypeServiceDAO.findAllPDSServerPatientIdentifierTypes();

    Map<String, List<PatientIdentifierType>> map = new TreeMap<>();
    for (PatientIdentifierType mdsItem : allMDS) {
      for (PatientIdentifierType pdsItem : allPDS) {
        if (mdsItem.getUuid().equals(pdsItem.getUuid())
            && mdsItem.getId() == pdsItem.getId()
            && mdsItem.getName().equalsIgnoreCase(pdsItem.getName())
            && !(StringUtils.defaultString(mdsItem.getFormat())
                    .equalsIgnoreCase(StringUtils.defaultString(pdsItem.getFormat()))
                && mdsItem.getCheckDigit().equals(pdsItem.getCheckDigit())
                && mdsItem.getRequired().equals(pdsItem.getRequired()))) {
          map.put(mdsItem.getUuid(), Arrays.asList(mdsItem, pdsItem));
        }
      }
    }
    return map;
  }

  private Map<String, List<PatientIdentifierType>> findByWithDifferentIDAndSameUUID() {
    List<PatientIdentifierType> allPDS =
        harmonizationPatientIdentifierTypeServiceDAO.findAllPDSServerPatientIdentifierTypes();
    List<PatientIdentifierType> allMDS =
        harmonizationPatientIdentifierTypeServiceDAO.findAllMDSServerPatientIdentifierTypes();
    Map<String, List<PatientIdentifierType>> map = new TreeMap<>();
    for (PatientIdentifierType mdsItem : allMDS) {
      for (PatientIdentifierType pdsItem : allPDS) {
        if (mdsItem.getUuid().equals(pdsItem.getUuid()) && mdsItem.getId() != pdsItem.getId()) {
          map.put(mdsItem.getUuid(), Arrays.asList(mdsItem, pdsItem));
        }
      }
    }
    return map;
  }

  private void updateToGivenId(
      PatientIdentifierType patientIdentifierType,
      Integer patientIdentifierTypeId,
      boolean swappable,
      List<PatientIdentifier> relatedPatientIdentifiers) {
    this.harmonizationPatientIdentifierTypeServiceDAO.updatePatientIdentifierType(
        patientIdentifierTypeId, patientIdentifierType, swappable);

    for (PatientIdentifier patientIdentifierTypes : relatedPatientIdentifiers) {
      this.harmonizationPatientIdentifierTypeServiceDAO.updatePatientIdentifier(
          patientIdentifierTypes, patientIdentifierTypeId);
    }
  }

  private void updateToNextAvailableID(
      PatientIdentifierType patientIdentifierType,
      List<PatientIdentifier> relatedPatientIdentifiers) {
    PatientIdentifierType updated =
        this.harmonizationPatientIdentifierTypeServiceDAO.updateToNextAvailableId(
            patientIdentifierType);
    for (PatientIdentifier encounter : relatedPatientIdentifiers) {
      this.harmonizationPatientIdentifierTypeServiceDAO.updatePatientIdentifier(
          encounter, updated.getPatientIdentifierTypeId());
    }
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Patient Identifier Types"})
  public PatientIdentifierType findFromPDSByUuid(String uuid) throws APIException {
    this.harmonizationDAO.evictCache();
    return patientService.getPatientIdentifierTypeByUuid(uuid);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Patient Identifier Types"})
  public PatientIdentifierType findFromMDSByUuid(String uuid) throws APIException {
    this.harmonizationDAO.evictCache();
    PatientIdentifierType result =
        this.harmonizationPatientIdentifierTypeServiceDAO.findMDSPatientIdentifierTypeByUuid(uuid);
    return result;
  }
}
