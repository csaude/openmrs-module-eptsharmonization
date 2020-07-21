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
import java.util.Set;
import java.util.UUID;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.LocationService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.eptsharmonization.HarmonizationUtils;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationLocationAttributeTypeService;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationLocationAttributeTypeDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.openmrs.module.eptsharmonization.api.model.LocationAttributeTypeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** It is a default implementation of {@link HarmonizationLocationAttributeTypeService}. */
@Transactional
@Service(HarmonizationLocationAttributeTypeServiceImpl.BEAN_NAME)
public class HarmonizationLocationAttributeTypeServiceImpl extends BaseOpenmrsService
    implements HarmonizationLocationAttributeTypeService {
  public static final String BEAN_NAME =
      "eptsharmonization.harmonizationLocationAttributeTypeService";

  private HarmonizationServiceDAO dao;

  private HarmonizationLocationAttributeTypeDAO harmonizationLocationAttributeTypeDao;

  private LocationService locationService;

  @Autowired
  public void setLocationService(LocationService locationService) {
    this.locationService = locationService;
  }

  @Autowired
  public void setDao(HarmonizationServiceDAO dao) {
    this.dao = dao;
  }

  @Autowired
  public void setHarmonizationLocationAttributeTypeDao(
      HarmonizationLocationAttributeTypeDAO harmonizationLocationAttributeTypeDao) {
    this.harmonizationLocationAttributeTypeDao = harmonizationLocationAttributeTypeDao;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View LocationAttribute Types"})
  public List<LocationAttributeTypeDTO>
      findAllMetadataLocationAttributeTypesNotSharingUuidWithAnyFromProduction()
          throws APIException {
    List<LocationAttributeType> mdsLocationAttributeTypes =
        harmonizationLocationAttributeTypeDao.findAllMDSLocationAttributeTypes();
    List<LocationAttributeType> pdsLocationAttributeTypes =
        locationService.getAllLocationAttributeTypes();
    mdsLocationAttributeTypes.removeAll(pdsLocationAttributeTypes);
    return DTOUtils.fromLocationAttributeTypes(mdsLocationAttributeTypes);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View LocationAttribute Types"})
  public List<LocationAttributeTypeDTO>
      findAllProductionLocationAttributeTypesNotSharingUuidWithAnyFromMetadata()
          throws APIException {
    List<LocationAttributeType> pdsLocationAttributeTypes =
        locationService.getAllLocationAttributeTypes();
    List<LocationAttributeType> mdsLocationAttributeTypes =
        harmonizationLocationAttributeTypeDao.findAllMDSLocationAttributeTypes();
    pdsLocationAttributeTypes.removeAll(mdsLocationAttributeTypes);
    return DTOUtils.fromLocationAttributeTypes(pdsLocationAttributeTypes);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View LocationAttribute Types"})
  public List<LocationAttributeTypeDTO>
      findAllUselessProductionLocationAttributeTypesNotSharingUuidWithAnyFromMetadata()
          throws APIException {
    List<LocationAttributeTypeDTO> allInPDSNotSharingUuidWithMDS =
        findAllProductionLocationAttributeTypesNotSharingUuidWithAnyFromMetadata();
    List<LocationAttributeTypeDTO> uselessOnes = new ArrayList<>();
    for (LocationAttributeTypeDTO locationAttributeTypeDTO : allInPDSNotSharingUuidWithMDS) {
      int count = getNumberOfAffectedLocationAttributes(locationAttributeTypeDTO);
      if (count == 0) {
        uselessOnes.add(locationAttributeTypeDTO);
      }
    }
    return uselessOnes;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View LocationAttribute Types"})
  public Map<LocationAttributeTypeDTO, Integer>
      findAllUsedProductionLocationAttributeTypesNotSharingUuidWithAnyFromMetadata()
          throws APIException {
    List<LocationAttributeTypeDTO> allInPDSNotSharingUuidWithMDS =
        findAllProductionLocationAttributeTypesNotSharingUuidWithAnyFromMetadata();
    Map<LocationAttributeTypeDTO, Integer> usedOnes = new HashMap<>();
    for (LocationAttributeTypeDTO locationAttributeTypeDTO : allInPDSNotSharingUuidWithMDS) {
      int count = getNumberOfAffectedLocationAttributes(locationAttributeTypeDTO);
      if (count > 0) {
        usedOnes.put(locationAttributeTypeDTO, count);
      }
    }
    return usedOnes;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View LocationAttribute Types"})
  public List<LocationAttributeTypeDTO>
      findAllMetadataLocationAttributeTypesNotInHarmonyWithProduction() throws APIException {
    List<LocationAttributeType> mdsLocationAttributeTypes =
        harmonizationLocationAttributeTypeDao.findAllMDSLocationAttributeTypes();
    List<LocationAttributeType> pdsLocationAttributeTypes =
        locationService.getAllLocationAttributeTypes();
    HarmonizationUtils.removeAllHarmonizedAttributes(
        mdsLocationAttributeTypes, pdsLocationAttributeTypes);
    return DTOUtils.fromLocationAttributeTypes(mdsLocationAttributeTypes);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View LocationAttribute Types"})
  public List<LocationAttributeTypeDTO>
      findAllProductionLocationAttributeTypesNotInHarmonyWithMetadata() throws APIException {
    List<LocationAttributeType> notInHarmonyWithMetadata =
        harmonizationLocationAttributeTypeDao.findPDSLocationAttributeTypesNotExistsInMDServer();
    return DTOUtils.fromLocationAttributeTypes(notInHarmonyWithMetadata);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View LocationAttribute Types"})
  public Map<String, List<LocationAttributeTypeDTO>>
      findAllLocationAttributeTypesWithDifferentNameAndSameUUIDAndID() throws APIException {
    List<LocationAttributeType> allPDS = locationService.getAllLocationAttributeTypes();
    List<LocationAttributeType> allMDS =
        harmonizationLocationAttributeTypeDao.findAllMDSLocationAttributeTypes();
    Map<String, List<LocationAttributeTypeDTO>> result = new HashMap<>();
    Map<String, List<LocationAttributeType>> map =
        HarmonizationUtils.findAttributeTypesWithSameUuidsAndIdsDifferentConfiguration(
            allPDS, allMDS);
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromLocationAttributeTypes(map.get(key)));
    }
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View LocationAttribute Types"})
  public Map<String, List<LocationAttributeTypeDTO>>
      findAllLocationAttributeTypesWithDifferentIDAndSameUUID() throws APIException {
    List<LocationAttributeType> allPDS = locationService.getAllLocationAttributeTypes();
    List<LocationAttributeType> allMDS =
        harmonizationLocationAttributeTypeDao.findAllMDSLocationAttributeTypes();
    Map<String, List<LocationAttributeTypeDTO>> result = new HashMap<>();
    Map<String, List<LocationAttributeType>> map =
        HarmonizationUtils.findElementsWithDifferentIdsSameUuids(allPDS, allMDS);
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromLocationAttributeTypes(map.get(key)));
    }
    return result;
  }

  @Override
  public LocationAttributeType findMDSLocationAttributeTypeByUuid(String uuid) throws APIException {
    return this.harmonizationLocationAttributeTypeDao.findMDSLocationAttributeTypeByUuid(uuid);
  }

  @Override
  public LocationAttributeType findPDSLocationAttributeTypeByUuid(String uuid) throws APIException {
    return this.harmonizationLocationAttributeTypeDao.findPDSLocationAttributeTypeByUuid(uuid);
  }

  @Override
  public int getNumberOfAffectedLocationAttributes(
      LocationAttributeTypeDTO locationAttributeTypeDTO) {
    return harmonizationLocationAttributeTypeDao
        .getCountOfLocationAttributesByLocationAttributeType(
            locationAttributeTypeDTO.getLocationAttributeType());
  }

  @Override
  @Authorized({"Manage LocationAttribute Types"})
  public void saveLocationAttributeTypesWithDifferentDetails(
      Map<String, List<LocationAttributeTypeDTO>> locationAttributeTypes) throws APIException {
    for (String key : locationAttributeTypes.keySet()) {
      List<LocationAttributeTypeDTO> list = locationAttributeTypes.get(key);
      LocationAttributeTypeDTO pdsLocationAttribute = list.get(0);
      LocationAttributeTypeDTO mdsLocationAttribute = list.get(1);
      LocationAttributeType locationAttributeType =
          locationService.getLocationAttributeType(
              pdsLocationAttribute.getLocationAttributeType().getId());
      locationAttributeType.setName(mdsLocationAttribute.getLocationAttributeType().getName());
      locationAttributeType.setDescription(
          mdsLocationAttribute.getLocationAttributeType().getDescription());
      locationAttributeType.setDatatypeClassname(
          mdsLocationAttribute.getLocationAttributeType().getDatatypeClassname());
      locationAttributeType.setDatatypeConfig(
          mdsLocationAttribute.getLocationAttributeType().getDatatypeConfig());
      locationAttributeType.setHandlerConfig(
          mdsLocationAttribute.getLocationAttributeType().getHandlerConfig());
      locationAttributeType.setPreferredHandlerClassname(
          mdsLocationAttribute.getLocationAttributeType().getPreferredHandlerClassname());
      locationAttributeType.setMinOccurs(
          mdsLocationAttribute.getLocationAttributeType().getMinOccurs());
      locationAttributeType.setMaxOccurs(
          mdsLocationAttribute.getLocationAttributeType().getMaxOccurs());
      locationService.saveLocationAttributeType(locationAttributeType);
    }
  }

  @Override
  @Authorized({"Manage LocationAttribute Types"})
  @Transactional
  public void saveNewLocationAttributeTypeFromMetadata(
      LocationAttributeTypeDTO locationAttributeTypeDTO) throws APIException {
    try {
      dao.setDisabledCheckConstraints();
      saveNewLocationAttributeTypeFromDTO(locationAttributeTypeDTO);
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
  @Authorized({"Manage LocationAttribute Types"})
  @Transactional(propagation = Propagation.REQUIRED)
  public void saveNewLocationAttributeTypesFromMetadata(
      List<LocationAttributeTypeDTO> locationAttributeTypeDTOList) throws APIException {
    try {
      dao.setDisabledCheckConstraints();
      for (LocationAttributeTypeDTO locationAttributeTypeDTO : locationAttributeTypeDTOList) {
        saveNewLocationAttributeTypeFromDTO(locationAttributeTypeDTO);
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
  public void updateLocationAttributeTypesFromProductionWithSameUuidWithInformationFromMetadata(
      Map<String, List<LocationAttributeTypeDTO>> mapLocationAttributeTypes) throws APIException {
    try {
      this.dao.setDisabledCheckConstraints();
      for (String uuid : mapLocationAttributeTypes.keySet()) {
        List<LocationAttributeTypeDTO> list = mapLocationAttributeTypes.get(uuid);
        LocationAttributeType pdsLocationAttributeType = list.get(0).getLocationAttributeType();
        LocationAttributeType mdsLocationAttributeType = list.get(1).getLocationAttributeType();
        Integer mdsLocationAttributeTypeId = pdsLocationAttributeType.getLocationAttributeTypeId();

        LocationAttributeType foundPDS =
            locationService.getLocationAttributeType(mdsLocationAttributeType.getId());
        if (foundPDS != null && !foundPDS.getUuid().equals(mdsLocationAttributeType.getUuid())) {
          moveUnrelatedProductionLocationAttributeType(foundPDS);
        }

        if (!this.harmonizationLocationAttributeTypeDao.isSwappable(pdsLocationAttributeType)) {
          throw new APIException(
              String.format(
                  "Cannot update the Production Server LocationAttribute type [ID = {%s}, UUID = {%s}, NAME = {%s}] with the ID {%s} "
                      + "this new ID is already referencing an Existing LocationAttribute Type In Metadata Server",
                  mdsLocationAttributeType.getId(),
                  mdsLocationAttributeType.getUuid(),
                  mdsLocationAttributeType.getName(),
                  mdsLocationAttributeTypeId));
        }
        List<LocationAttribute> relatedLocationAttributes =
            harmonizationLocationAttributeTypeDao.findLocationAttributesByLocationAttributeType(
                pdsLocationAttributeType);
        // Get a fresh copy from the database.
        pdsLocationAttributeType =
            locationService.getLocationAttributeType(
                pdsLocationAttributeType.getLocationAttributeTypeId());
        overwriteLocationAttributeType(
            pdsLocationAttributeType, mdsLocationAttributeType, relatedLocationAttributes);
      }

    } catch (Exception e) {
      if (e instanceof APIException) throw (APIException) e;
      throw new APIException(e.getMessage(), e);
    } finally {
      try {
        this.dao.setEnableCheckConstraints();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  @Transactional
  public void replacePDSLocationAttributeTypesWithSameUuidWithThoseFromMDS(
      Map<String, List<LocationAttributeTypeDTO>> locationAttributeTypesDTO) throws APIException {
    try {
      dao.setDisabledCheckConstraints();
      for (String uuid : locationAttributeTypesDTO.keySet()) {
        List<LocationAttributeTypeDTO> list = locationAttributeTypesDTO.get(uuid);
        LocationAttributeType pdsLocationAttributeType = list.get(0).getLocationAttributeType();
        LocationAttributeType mdsLocationAttributeType = list.get(1).getLocationAttributeType();

        LocationAttributeType foundPDS =
            locationService.getLocationAttributeType(mdsLocationAttributeType.getId());
        if (foundPDS != null && !foundPDS.getUuid().equals(mdsLocationAttributeType.getUuid())) {
          moveUnrelatedProductionLocationAttributeType(foundPDS);
        }

        if (pdsLocationAttributeType
            .getLocationAttributeTypeId()
            .equals(mdsLocationAttributeType.getLocationAttributeTypeId())) {
          List<LocationAttribute> relatedLocationAttributes =
              harmonizationLocationAttributeTypeDao.findLocationAttributesByLocationAttributeType(
                  pdsLocationAttributeType);
          Integer nextId = harmonizationLocationAttributeTypeDao.getNextLocationAttributeTypeId();
          harmonizationLocationAttributeTypeDao.updateLocationAttributeType(
              pdsLocationAttributeType, nextId, UUID.randomUUID().toString());
          for (LocationAttribute locationAttribute : relatedLocationAttributes) {
            this.harmonizationLocationAttributeTypeDao.updateLocationAttribute(
                locationAttribute, nextId);
          }
        } else {
          // Simply assign new uuid
          harmonizationLocationAttributeTypeDao.updateLocationAttributeType(
              pdsLocationAttributeType, UUID.randomUUID().toString());
        }

        // Save the one from metadata server
        harmonizationLocationAttributeTypeDao.insertLocationAttributeType(mdsLocationAttributeType);
      }
    } catch (Exception e) {
      e.printStackTrace();
      if (e instanceof APIException) throw (APIException) e;
      throw new APIException(e.getMessage(), e);
    } finally {
      try {
        dao.setEnableCheckConstraints();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  @Authorized({"Manage LocationAttribute Types"})
  public void deleteLocationAttributeTypesFromProduction(
      List<LocationAttributeTypeDTO> locationAttributeTypes) throws APIException {
    for (LocationAttributeType locationAttributeType :
        DTOUtils.fromLocationAttributeTypeDTOs(locationAttributeTypes)) {
      locationService.purgeLocationAttributeType(locationAttributeType);
    }
  }

  @Override
  public void saveManualLocationAttributeTypeMappings(
      Map<LocationAttributeType, LocationAttributeType> manualLocationAttributeTypeMappings)
      throws APIException {
    // Get LocationAttributes related to mapped one.
    for (Map.Entry<LocationAttributeType, LocationAttributeType> locationAttributeTypeMapping :
        manualLocationAttributeTypeMappings.entrySet()) {
      LocationAttributeType pdsLocationAttributeType = locationAttributeTypeMapping.getKey();
      LocationAttributeType mdsLocationAttributeType = locationAttributeTypeMapping.getValue();
      // Get related locationAttributes
      List<LocationAttribute> locationAttributes =
          getLocationAttributesAssociatedWithAttribute(pdsLocationAttributeType);
      this.overwriteLocationAttributeType(
          pdsLocationAttributeType, mdsLocationAttributeType, locationAttributes);

      //			for (LocationAttribute locationAttribute : locationAttributes) {
      //				harmonizationLocationAttributeTypeDao.updateLocationAttribute(locationAttribute,
      //						mdsLocationAttributeType.getLocationAttributeTypeId());
      //				locationService.purgeLocationAttributeType(pdsLocationAttributeType);
      //			}
    }
  }

  private void updateToGivenId(
      LocationAttributeType locationAttributeType,
      Integer locationAttributeTypeId,
      List<LocationAttribute> relatedLocationAttributes) {
    this.harmonizationLocationAttributeTypeDao.updateLocationAttributeType(
        locationAttributeType, locationAttributeTypeId);
    for (LocationAttribute locationAttribute : relatedLocationAttributes) {
      this.harmonizationLocationAttributeTypeDao.updateLocationAttribute(
          locationAttribute, locationAttributeTypeId);
    }
  }

  private void overwriteLocationAttributeType(
      LocationAttributeType pdsToOverwrite,
      LocationAttributeType fromMds,
      List<LocationAttribute> relatedPdsLocationAttributes) {
    harmonizationLocationAttributeTypeDao.overwriteLocationAttributeTypeDetails(
        pdsToOverwrite, fromMds);
    for (LocationAttribute locationAttribute : relatedPdsLocationAttributes) {
      harmonizationLocationAttributeTypeDao.updateLocationAttribute(
          locationAttribute, pdsToOverwrite.getLocationAttributeTypeId());
    }
  }

  private void moveUnrelatedProductionLocationAttributeType(LocationAttributeType toBeMoved)
      throws APIException {
    if (!this.harmonizationLocationAttributeTypeDao.isSwappable(toBeMoved)) {
      throw new APIException(
          String.format(
              "Cannot update the Production Server LocationAttribute type with ID {%s}, UUID {%s} and NAME {%s}. This LocationAttribute Type is a "
                  + "reference from an LocationAttribute Type of Metadata Server",
              toBeMoved.getId(), toBeMoved.getUuid(), toBeMoved.getName()));
    }
    List<LocationAttribute> relatedLocationAttributes =
        harmonizationLocationAttributeTypeDao.findLocationAttributesByLocationAttributeType(
            toBeMoved);
    Integer nextId = harmonizationLocationAttributeTypeDao.getNextLocationAttributeTypeId();
    updateToGivenId(toBeMoved, nextId, relatedLocationAttributes);
  }

  private void saveNewLocationAttributeTypeFromDTO(
      LocationAttributeTypeDTO locationAttributeTypeDTO) throws APIException {
    LocationAttributeType locationAttributeTypeFromDTO =
        locationAttributeTypeDTO.getLocationAttributeType();
    LocationAttributeType found =
        locationService.getLocationAttributeType(
            locationAttributeTypeFromDTO.getLocationAttributeTypeId());
    if (found != null) {

      if (!this.harmonizationLocationAttributeTypeDao.isSwappable(found)) {
        throw new APIException(
            String.format(
                "Cannot Insert LocationAttribute type with ID %s, UUID %s and NAME %s. This ID is being in use by another LocationAttribute type from Metadata server with UUID %s and name %s ",
                locationAttributeTypeFromDTO.getId(),
                locationAttributeTypeFromDTO.getUuid(),
                locationAttributeTypeFromDTO.getName(),
                found.getUuid(),
                found.getName()));
      }
      List<LocationAttribute> relatedLocationAttributes =
          this.harmonizationLocationAttributeTypeDao.findLocationAttributesByLocationAttributeType(
              locationAttributeTypeFromDTO);
      Integer nextId = this.harmonizationLocationAttributeTypeDao.getNextLocationAttributeTypeId();
      this.updateToGivenId(found, nextId, relatedLocationAttributes);
    }
    this.harmonizationLocationAttributeTypeDao.insertLocationAttributeType(
        locationAttributeTypeFromDTO);
  }

  private List<LocationAttribute> getLocationAttributesAssociatedWithAttribute(
      LocationAttributeType locationAttributeType) {
    List<Location> locs = locationService.getAllLocations(true);
    List<LocationAttribute> attributes = new ArrayList<>();
    for (Location l : locs) {
      Set<LocationAttribute> las = l.getAttributes();
      for (LocationAttribute la : las) {
        if (la.getAttributeType().equals(locationAttributeType)) {
          attributes.add(la);
        }
      }
    }

    return attributes;
  }
}
