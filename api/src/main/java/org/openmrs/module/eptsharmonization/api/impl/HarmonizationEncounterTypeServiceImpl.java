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
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.eptsharmonization.EptsHarmonizationConstants;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationEncounterTypeService;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationEncounterTypeServiceDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.openmrs.module.eptsharmonization.api.exception.UUIDDuplicationException;
import org.openmrs.module.eptsharmonization.api.model.EncounterTypeDTO;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** It is a default implementation of {@link HarmonizationEncounterTypeService}. */
@Transactional
@Service("eptsharmonization.harmonizationEncounterTypeService")
public class HarmonizationEncounterTypeServiceImpl extends BaseOpenmrsService
    implements HarmonizationEncounterTypeService {

  private HarmonizationServiceDAO harmonizationDAO;
  private EncounterService encounterService;
  private HarmonizationEncounterTypeServiceDAO harmonizationEncounterTypeServiceDAO;

  @Autowired
  public void setHarmonizationDAO(HarmonizationServiceDAO harmonizationDAO) {
    this.harmonizationDAO = harmonizationDAO;
  }

  @Autowired
  public void setEncounterService(EncounterService encounterService) {
    this.encounterService = encounterService;
  }

  @Autowired
  public void setHarmonizationEncounterTypeServiceDAO(
      HarmonizationEncounterTypeServiceDAO harmonizationEncounterTypeServiceDAO) {
    this.harmonizationEncounterTypeServiceDAO = harmonizationEncounterTypeServiceDAO;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Encountery Types"})
  public List<EncounterTypeDTO> findAllMetadataEncounterNotContainedInProductionServer()
      throws APIException {
    this.harmonizationDAO.evictCache();
    List<EncounterType> mdsEncounterTypes =
        harmonizationEncounterTypeServiceDAO.findAllMetadataServerEncounterTypes();
    List<EncounterType> pdsEncounterTypes =
        harmonizationEncounterTypeServiceDAO.findAllProductionServerEncounterTypes();
    mdsEncounterTypes.removeAll(pdsEncounterTypes);
    return DTOUtils.fromEncounterTypes(mdsEncounterTypes);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Encountery Types"})
  public List<EncounterTypeDTO> findAllProductionEncountersNotContainedInMetadataServer()
      throws APIException {
    this.harmonizationDAO.evictCache();
    List<EncounterType> pdsEncounterTypes =
        harmonizationEncounterTypeServiceDAO.findAllProductionServerEncounterTypes();
    List<EncounterType> mdsEncounterTypes =
        harmonizationEncounterTypeServiceDAO.findAllMetadataServerEncounterTypes();
    pdsEncounterTypes.removeAll(mdsEncounterTypes);
    return DTOUtils.fromEncounterTypes(pdsEncounterTypes);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Encountery Types"})
  public List<EncounterTypeDTO> findAllMetadataEncounterPartialEqualsToProductionServer()
      throws APIException {
    this.harmonizationDAO.evictCache();
    List<EncounterType> allMDS =
        harmonizationEncounterTypeServiceDAO.findAllMetadataServerEncounterTypes();
    List<EncounterType> allPDS =
        harmonizationEncounterTypeServiceDAO.findAllProductionServerEncounterTypes();
    List<EncounterType> mdsEncounterTypes =
        this.removeElementsWithDifferentIDsAndUUIDs(allMDS, allPDS);
    allMDS.removeAll(allPDS);
    mdsEncounterTypes.removeAll(allMDS);
    return DTOUtils.fromEncounterTypes(mdsEncounterTypes);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Encountery Types"})
  public List<EncounterTypeDTO> findAllProductionEncountersPartialEqualsToMetadataServer()
      throws APIException {
    this.harmonizationDAO.evictCache();
    List<EncounterType> allPDS =
        harmonizationEncounterTypeServiceDAO.findAllProductionServerEncounterTypes();
    List<EncounterType> allMDS =
        harmonizationEncounterTypeServiceDAO.findAllMetadataServerEncounterTypes();
    List<EncounterType> pdsEncounterTypes =
        this.removeElementsWithDifferentIDsAndUUIDs(allPDS, allMDS);
    allPDS.removeAll(allMDS);
    pdsEncounterTypes.removeAll(allPDS);
    return DTOUtils.fromEncounterTypes(pdsEncounterTypes);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Encountery Types"})
  public Map<String, List<EncounterTypeDTO>>
      findAllEncounterTypesWithDifferentNameAndSameUUIDAndID() throws APIException {
    this.harmonizationDAO.evictCache();
    Map<String, List<EncounterTypeDTO>> result = new HashMap<>();
    Map<String, List<EncounterType>> map = findByWithDifferentNameAndSameUUIDAndID();
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromEncounterTypes(map.get(key)));
    }
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Encountery Types"})
  public Map<String, List<EncounterTypeDTO>> findAllEncounterTypesWithDifferentIDAndSameUUID()
      throws APIException {
    this.harmonizationDAO.evictCache();
    Map<String, List<EncounterTypeDTO>> result = new HashMap<>();
    Map<String, List<EncounterType>> map = findByWithDifferentIDAndSameUUID();
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromEncounterTypes(map.get(key)));
    }
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Encountery Types"})
  public int getNumberOfAffectedEncounters(EncounterTypeDTO encounterTypeDTO) {
    this.harmonizationDAO.evictCache();
    return harmonizationEncounterTypeServiceDAO
        .findEncontersByEncounterTypeId(encounterTypeDTO.getEncounterType().getEncounterTypeId())
        .size();
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Encountery Types"})
  public int getNumberOfAffectedForms(EncounterTypeDTO encounterTypeDTO) {
    this.harmonizationDAO.evictCache();
    return harmonizationEncounterTypeServiceDAO
        .findFormsByEncounterTypeId(encounterTypeDTO.getEncounterType().getEncounterTypeId())
        .size();
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Encountery Types"})
  public List<EncounterType> findPDSEncounterTypesNotExistsInMDServer() throws APIException {
    this.harmonizationDAO.evictCache();
    List<EncounterType> result =
        harmonizationEncounterTypeServiceDAO.findPDSEncounterTypesNotExistsInMDServer();
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Encountery Types"})
  public List<EncounterType> findAllNotSwappableEncounterTypes() throws APIException {
    this.harmonizationDAO.evictCache();
    List<EncounterType> result = this.harmonizationEncounterTypeServiceDAO.findAllNotSwappable();
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Encountery Types"})
  public List<EncounterType> findAllSwappableEncounterTypes() throws APIException {
    this.harmonizationDAO.evictCache();
    List<EncounterType> findAllSwappable =
        this.harmonizationEncounterTypeServiceDAO.findAllSwappable();
    return findAllSwappable;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Encountery Types"})
  public List<EncounterType> findAllMetadataServerEncounterTypes() throws APIException {

    return this.harmonizationEncounterTypeServiceDAO.findAllMetadataServerEncounterTypes();
  }

  @Override
  @Authorized({"Manage Encountery Types"})
  public void saveEncounterTypesWithDifferentNames(
      Map<String, List<EncounterTypeDTO>> encounterTypes) throws APIException {
    this.harmonizationDAO.evictCache();
    for (String key : encounterTypes.keySet()) {
      List<EncounterTypeDTO> list = encounterTypes.get(key);
      EncounterTypeDTO mdsEncounter = list.get(0);
      EncounterTypeDTO pdsEncounter = list.get(1);
      EncounterType encounterType =
          this.encounterService.getEncounterType(pdsEncounter.getEncounterType().getId());
      encounterType.setName(mdsEncounter.getEncounterType().getName());
      encounterType.setDescription(mdsEncounter.getEncounterType().getDescription());
      this.encounterService.saveEncounterType(encounterType);
    }
  }

  @Override
  @Authorized({"Manage Encountery Types"})
  public void saveNewEncounterTypesFromMDS(List<EncounterTypeDTO> encounterTypes)
      throws APIException {
    this.harmonizationDAO.evictCache();
    try {
      this.harmonizationDAO.setDisabledCheckConstraints();
      for (EncounterType encounterType : DTOUtils.fromEncounterTypeDTOs(encounterTypes)) {

        EncounterType found =
            this.harmonizationEncounterTypeServiceDAO.getEncounterTypeById(encounterType.getId());

        if (found != null) {

          if (!this.harmonizationEncounterTypeServiceDAO.isSwappable(found)) {

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
              this.harmonizationEncounterTypeServiceDAO.findEncontersByEncounterTypeId(
                  found.getId());
          List<Form> relatedForms =
              this.harmonizationEncounterTypeServiceDAO.findFormsByEncounterTypeId(found.getId());
          this.updateToNextAvailableID(found, relatedEncounters, relatedForms);
        }
        this.harmonizationEncounterTypeServiceDAO.saveNotSwappableEncounterType(encounterType);
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
  @Authorized({"Manage Encountery Types"})
  public void saveEncounterTypesWithDifferentIDAndEqualUUID(
      Map<String, List<EncounterTypeDTO>> mapEncounterTypes) throws APIException {
    this.harmonizationDAO.evictCache();
    try {

      this.harmonizationDAO.setDisabledCheckConstraints();
      for (String uuid : mapEncounterTypes.keySet()) {

        List<EncounterTypeDTO> list = mapEncounterTypes.get(uuid);
        EncounterType mdsEncounterType = list.get(0).getEncounterType();
        EncounterType pdSEncounterType = list.get(1).getEncounterType();
        Integer mdServerEncounterId = mdsEncounterType.getEncounterTypeId();

        EncounterType foundMDS =
            this.harmonizationEncounterTypeServiceDAO.getEncounterTypeById(
                mdsEncounterType.getId());

        if (foundMDS != null) {
          if (!this.harmonizationEncounterTypeServiceDAO.isSwappable(foundMDS)) {
            throw new APIException(
                String.format(
                    "Cannot update the Production Server Encounter type [ID = {%s}, UUID = {%s}, NAME = {%s}] with the ID {%s} this new ID is already referencing an Existing Encounter Type In Metadata Server",
                    pdSEncounterType.getId(),
                    pdSEncounterType.getUuid(),
                    pdSEncounterType.getName(),
                    mdServerEncounterId));
          }
          List<Encounter> relatedEncounters =
              this.harmonizationEncounterTypeServiceDAO.findEncontersByEncounterTypeId(
                  foundMDS.getId());
          List<Form> relatedForms =
              this.harmonizationEncounterTypeServiceDAO.findFormsByEncounterTypeId(
                  foundMDS.getId());
          this.updateToNextAvailableID(foundMDS, relatedEncounters, relatedForms);
        }

        EncounterType foundPDS =
            this.harmonizationEncounterTypeServiceDAO.getEncounterTypeById(
                pdSEncounterType.getId());
        if (!this.harmonizationEncounterTypeServiceDAO.isSwappable(foundPDS)) {
          throw new APIException(
              String.format(
                  "Cannot update the Production Server Encounter type with ID {%s}, UUID {%s} and NAME {%s}. This Encounter Type is a Reference from an Encounter Type of Metadata Server",
                  foundPDS.getId(), foundPDS.getUuid(), foundPDS.getName()));
        }
        List<Encounter> relatedEncounters =
            this.harmonizationEncounterTypeServiceDAO.findEncontersByEncounterTypeId(
                foundPDS.getId());
        List<Form> relatedForms =
            this.harmonizationEncounterTypeServiceDAO.findFormsByEncounterTypeId(foundPDS.getId());
        this.updateToGivenId(foundPDS, mdServerEncounterId, false, relatedEncounters, relatedForms);
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
  @Authorized({"Manage Encountery Types"})
  public void deleteNewEncounterTypesFromPDS(List<EncounterTypeDTO> encounterTypes)
      throws APIException {
    this.harmonizationDAO.evictCache();
    for (EncounterType encounterType : DTOUtils.fromEncounterTypeDTOs(encounterTypes)) {
      this.harmonizationEncounterTypeServiceDAO.deleteEncounterType(encounterType);
    }
  }

  @Override
  @Authorized({"Manage Encountery Types"})
  public void saveManualMapping(Map<EncounterType, EncounterType> mapEncounterTypes)
      throws UUIDDuplicationException, SQLException {
    this.harmonizationDAO.evictCache();
    try {

      this.harmonizationDAO.setDisabledCheckConstraints();
      for (Entry<EncounterType, EncounterType> entry : mapEncounterTypes.entrySet()) {

        EncounterType pdSEncounterType = entry.getKey();
        EncounterType mdsEncounterType = entry.getValue();

        EncounterType foundMDSEncounterTypeByUuid =
            this.harmonizationEncounterTypeServiceDAO.getEncounterTypeByUuid(
                mdsEncounterType.getUuid());

        if ((foundMDSEncounterTypeByUuid != null
                && !foundMDSEncounterTypeByUuid.getId().equals(mdsEncounterType.getId()))
            && (!foundMDSEncounterTypeByUuid.getId().equals(pdSEncounterType.getId())
                && !foundMDSEncounterTypeByUuid.getUuid().equals(pdSEncounterType.getUuid()))) {

          throw new UUIDDuplicationException(
              String.format(
                  " Cannot Update the Encounter Type '%s' to '%s'. There is one entry with NAME='%s', ID='%s' an UUID='%s' ",
                  pdSEncounterType.getName(),
                  mdsEncounterType.getName(),
                  foundMDSEncounterTypeByUuid.getName(),
                  foundMDSEncounterTypeByUuid.getId(),
                  foundMDSEncounterTypeByUuid.getUuid()));
        }

        EncounterType foundPDS =
            this.harmonizationEncounterTypeServiceDAO.getEncounterTypeById(
                pdSEncounterType.getId());

        if (mdsEncounterType.getUuid().equals(pdSEncounterType.getUuid())
            && mdsEncounterType.getId().equals(pdSEncounterType.getId())
            && mdsEncounterType.getName().equalsIgnoreCase(pdSEncounterType.getName())) {
          return;
        } else {
          List<Encounter> relatedEncounters =
              this.harmonizationEncounterTypeServiceDAO.findEncontersByEncounterTypeId(
                  foundPDS.getId());

          List<Form> relatedForms =
              this.harmonizationEncounterTypeServiceDAO.findFormsByEncounterTypeId(
                  foundPDS.getId());

          for (Form form : relatedForms) {
            this.harmonizationEncounterTypeServiceDAO.updateForm(
                form, mdsEncounterType.getEncounterTypeId());
          }
          for (Encounter encounter : relatedEncounters) {
            this.harmonizationEncounterTypeServiceDAO.updateEncounter(
                encounter, mdsEncounterType.getEncounterTypeId());
          }
          this.harmonizationEncounterTypeServiceDAO.deleteEncounterType(foundPDS);

          EncounterType foundMDSEncounterTypeByID =
              this.harmonizationEncounterTypeServiceDAO.getEncounterTypeById(
                  mdsEncounterType.getId());
          if (foundMDSEncounterTypeByID == null) {
            this.harmonizationEncounterTypeServiceDAO.saveNotSwappableEncounterType(
                mdsEncounterType);
          }
          // this.updateEncounterToVisitMappingGlobalPropertiy(foundPDS,
          // mdsEncounterType);
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
    List<EncounterType> allMDS =
        harmonizationEncounterTypeServiceDAO.findAllMetadataServerEncounterTypes();
    List<EncounterType> allPDS =
        harmonizationEncounterTypeServiceDAO.findAllProductionServerEncounterTypes();

    Map<String, List<EncounterType>> map = new TreeMap<>();
    for (EncounterType mdsItem : allMDS) {
      for (EncounterType pdsItem : allPDS) {
        if (mdsItem.getUuid().equals(pdsItem.getUuid())
            && mdsItem.getId() == pdsItem.getId()
            && !mdsItem.getName().trim().equalsIgnoreCase(pdsItem.getName().trim())) {
          map.put(mdsItem.getUuid(), Arrays.asList(mdsItem, pdsItem));
        }
      }
    }
    return map;
  }

  private Map<String, List<EncounterType>> findByWithDifferentIDAndSameUUID() {
    List<EncounterType> allPDS =
        harmonizationEncounterTypeServiceDAO.findAllProductionServerEncounterTypes();
    List<EncounterType> allMDS =
        harmonizationEncounterTypeServiceDAO.findAllMetadataServerEncounterTypes();
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
  public boolean isAllEncounterTypeMedatadaHarmonized() {
    return this.harmonizationEncounterTypeServiceDAO.isAllMedatadaHarmonized();
  }

  private void updateToGivenId(
      EncounterType encounterType,
      Integer encounterTypeId,
      boolean swappable,
      List<Encounter> relatedEncounters,
      List<Form> relatedForms) {
    EncounterType updated =
        this.harmonizationEncounterTypeServiceDAO.updateEncounterType(
            encounterTypeId, encounterType, swappable);

    for (Form form : relatedForms) {
      this.harmonizationEncounterTypeServiceDAO.updateForm(form, encounterTypeId);
    }
    for (Encounter encounter : relatedEncounters) {
      this.harmonizationEncounterTypeServiceDAO.updateEncounter(encounter, encounterTypeId);
    }
    // this.updateEncounterToVisitMappingGlobalPropertiy(encounterType, updated);
  }

  private void updateToNextAvailableID(
      EncounterType encounterType, List<Encounter> relatedEncounters, List<Form> relatedForms) {
    EncounterType updated =
        this.harmonizationEncounterTypeServiceDAO.updateToNextAvailableId(encounterType);
    for (Form form : relatedForms) {
      this.harmonizationEncounterTypeServiceDAO.updateForm(form, updated.getEncounterTypeId());
    }
    for (Encounter encounter : relatedEncounters) {
      this.harmonizationEncounterTypeServiceDAO.updateEncounter(
          encounter, updated.getEncounterTypeId());
    }
    // this.updateEncounterToVisitMappingGlobalPropertiy(encounterType, updated);
  }

  @Override
  public void updateGPEncounterTypeToVisitTypeMapping() {

    String pdsValue =
        Context.getAdministrationService()
            .getGlobalPropertyValue(
                OpenmrsConstants.GP_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAPPING, StringUtils.EMPTY);

    if (!EptsHarmonizationConstants.VISITS_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAPPING_GP_VALUE.equals(
        pdsValue)) {
      Context.getAdministrationService()
          .setGlobalProperty(
              OpenmrsConstants.GP_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAPPING,
              EptsHarmonizationConstants.VISITS_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAPPING_GP_VALUE);
    }

    // TODO: esta logica reserva-se para para outros Parceiros

    //		String[] mdsMappings =
    // EptsHarmonizationConstants.VISITS_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAPPING_GP_VALUE
    //		.split(",");
    //		String[] pdsMappings = pdsValue.split(",");
    //
    //		String toAdd = StringUtils.EMPTY;
    //		for (String mdsPair : mdsMappings) {
    //
    //			boolean foundPair = false;
    //			for (String pdsPair : pdsMappings) {
    //				if (mdsPair.trim().equals(pdsPair.trim())) {
    //					foundPair = true;
    //					break;
    //				}
    //			}
    //			if (!foundPair) {
    //				toAdd += mdsPair + ", ";
    //			}
    //		}
    //
    //		if (!toAdd.isEmpty()) {
    //			toAdd = toAdd.substring(0, toAdd.length() - 2);
    //			pdsValue += ", " + toAdd;
    //			Context.getAdministrationService()
    //					.setGlobalProperty(OpenmrsConstants.GP_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAPPING, pdsValue);
    //		}
  }

  // TODO: Este logica de codigo vamos usar para os outros parceiro
  // private void updateEncounterToVisitMappingGlobalPropertiy(
  // EncounterType sourceEType, EncounterType targetEType) {
  // this.harmonizationDAO.evictCache();
  // String globalProperty =
  // Context.getAdministrationService()
  // .getGlobalProperty(OpenmrsConstants.GP_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAPPING);
  //
  // StringTokenizer stringTokenizer = new StringTokenizer(globalProperty, ",");
  // boolean isTokenFound = false;
  // while (stringTokenizer.hasMoreElements()) {
  // String token = (String) stringTokenizer.nextElement();
  // String[] split = token.split(":");
  //
  // if (sourceEType.getId().equals(Integer.valueOf(split[0].trim()))) {
  // isTokenFound = true;
  // break;
  // }
  // }
  // if (isTokenFound) {
  // String replacement = StringUtils.EMPTY;
  // stringTokenizer = new StringTokenizer(globalProperty, ",");
  //
  // while (stringTokenizer.hasMoreElements()) {
  // String token = (String) stringTokenizer.nextElement();
  // String[] split = token.split(":");
  //
  // if (sourceEType.getId().equals(Integer.valueOf(split[0].trim()))) {
  // split[0] = String.valueOf(targetEType.getId());
  // }
  // replacement +=
  // new StringBuilder()
  // .append(split[0].trim())
  // .append(":")
  // .append(split[1].trim())
  // .append(", ")
  // .toString();
  // }
  // if (!replacement.isEmpty()) {
  // replacement = replacement.substring(0, replacement.length() - 2);
  // }
  // Context.getAdministrationService()
  // .setGlobalProperty(OpenmrsConstants.GP_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAPPING,
  // replacement);
  // }
  // }
}
