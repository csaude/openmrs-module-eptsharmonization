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
import java.util.UUID;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.LocationService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.eptsharmonization.HarmonizationUtils;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationLocationTagService;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationLocationTagDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.openmrs.module.eptsharmonization.api.model.LocationTagDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** It is a default implementation of {@link HarmonizationLocationTagService}. */
@Transactional
@Service(HarmonizationLocationTagServiceImpl.BEAN_NAME)
public class HarmonizationLocationTagServiceImpl extends BaseOpenmrsService
    implements HarmonizationLocationTagService {
  public static final String BEAN_NAME = "eptsharmonization.harmonizationLocationTagService";

  private HarmonizationServiceDAO dao;

  private HarmonizationLocationTagDAO harmonizationLocationTagDao;

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
  public void setHarmonizationLocationTagDao(
      HarmonizationLocationTagDAO harmonizationLocationTagDao) {
    this.harmonizationLocationTagDao = harmonizationLocationTagDao;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Location Types"})
  public List<LocationTagDTO> findAllMetadataLocationTagsNotSharingUuidWithAnyFromProduction()
      throws APIException {
    List<LocationTag> mdsLocationTags = harmonizationLocationTagDao.findAllMDSLocationTags();
    List<LocationTag> pdsLocationTags = locationService.getAllLocationTags(true);
    mdsLocationTags.removeAll(pdsLocationTags);
    return DTOUtils.fromLocationTags(mdsLocationTags);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Location Types"})
  public List<LocationTagDTO> findAllProductionLocationTagsNotSharingUuidWithAnyFromMetadata()
      throws APIException {
    List<LocationTag> pdsLocationTags = locationService.getAllLocationTags();
    List<LocationTag> mdsLocationTags = harmonizationLocationTagDao.findAllMDSLocationTags();
    pdsLocationTags.removeAll(mdsLocationTags);
    return DTOUtils.fromLocationTags(pdsLocationTags);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Location Types"})
  public List<LocationTagDTO>
      findAllUselessProductionLocationTagsNotSharingUuidWithAnyFromMetadata() throws APIException {
    List<LocationTagDTO> allInPDSNotSharingUuidWithMDS =
        findAllProductionLocationTagsNotSharingUuidWithAnyFromMetadata();
    List<LocationTagDTO> uselessOnes = new ArrayList<>();
    for (LocationTagDTO locationTagDTO : allInPDSNotSharingUuidWithMDS) {
      int count = getNumberOfAffectedLocations(locationTagDTO);
      if (count == 0) {
        uselessOnes.add(locationTagDTO);
      }
    }
    return uselessOnes;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Location Types"})
  public Map<LocationTagDTO, Integer>
      findAllUsedProductionLocationTagsNotSharingUuidWithAnyFromMetadata() throws APIException {
    List<LocationTagDTO> allInPDSNotSharingUuidWithMDS =
        findAllProductionLocationTagsNotSharingUuidWithAnyFromMetadata();
    Map<LocationTagDTO, Integer> usedOnes = new HashMap<>();
    for (LocationTagDTO locationTagDTO : allInPDSNotSharingUuidWithMDS) {
      int count = getNumberOfAffectedLocations(locationTagDTO);
      if (count > 0) {
        usedOnes.put(locationTagDTO, count);
      }
    }
    return usedOnes;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Location Types"})
  public List<LocationTagDTO> findAllMetadataLocationTagsNotInHarmonyWithProduction()
      throws APIException {
    List<LocationTag> mdsLocationTags = harmonizationLocationTagDao.findAllMDSLocationTags();
    List<LocationTag> pdsLocationTags = locationService.getAllLocationTags(true);
    HarmonizationUtils.removeAllHarmonizedElements(mdsLocationTags, pdsLocationTags);
    return DTOUtils.fromLocationTags(mdsLocationTags);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Location Types"})
  public List<LocationTagDTO> findAllProductionLocationTagsNotInHarmonyWithMetadata()
      throws APIException {
    List<LocationTag> notInHarmonyWithMetadata =
        harmonizationLocationTagDao.findPDSLocationTagsNotExistsInMDServer();
    return DTOUtils.fromLocationTags(notInHarmonyWithMetadata);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Location Types"})
  public Map<String, List<LocationTagDTO>> findAllLocationTagsWithDifferentNameAndSameUUIDAndID()
      throws APIException {
    List<LocationTag> allPDS = locationService.getAllLocationTags();
    List<LocationTag> allMDS = harmonizationLocationTagDao.findAllMDSLocationTags();
    Map<String, List<LocationTagDTO>> result = new HashMap<>();
    Map<String, List<LocationTag>> map =
        HarmonizationUtils.findElementsWithDifferentNamesSameUuidsAndIds(allPDS, allMDS);
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromLocationTags(map.get(key)));
    }
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Location Types"})
  public Map<String, List<LocationTagDTO>> findAllLocationTagsWithDifferentIDAndSameUUID()
      throws APIException {
    List<LocationTag> allPDS = locationService.getAllLocationTags();
    List<LocationTag> allMDS = harmonizationLocationTagDao.findAllMDSLocationTags();
    Map<String, List<LocationTagDTO>> result = new HashMap<>();
    Map<String, List<LocationTag>> map =
        HarmonizationUtils.findElementsWithDifferentIdsSameUuids(allPDS, allMDS);
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromLocationTags(map.get(key)));
    }
    return result;
  }

  @Override
  public int getNumberOfAffectedLocations(LocationTagDTO locationTagDTO) {
    return harmonizationLocationTagDao.getCountOfLocationsByLocationTag(
        locationTagDTO.getLocationTag());
  }

  @Override
  @Authorized({"Manage Location Types"})
  public void saveLocationTagsWithDifferentNames(Map<String, List<LocationTagDTO>> locationTags)
      throws APIException {
    for (String key : locationTags.keySet()) {
      List<LocationTagDTO> list = locationTags.get(key);
      LocationTagDTO pdsLocation = list.get(0);
      LocationTagDTO mdsLocation = list.get(1);
      LocationTag locationTag =
          locationService.getLocationTag(pdsLocation.getLocationTag().getId());
      locationTag.setName(mdsLocation.getLocationTag().getName());
      locationTag.setDescription(mdsLocation.getLocationTag().getDescription());
      locationService.saveLocationTag(locationTag);
    }
  }

  @Override
  public LocationTag findMDSLocationTagByUuid(String uuid) throws APIException {
    return this.harmonizationLocationTagDao.findMDSLocationTagByUuid(uuid);
  }

  @Override
  public LocationTag findPDSLocationTagByUuid(String uuid) throws APIException {
    return this.harmonizationLocationTagDao.findPDSLocationTagByUuid(uuid);
  }

  @Override
  @Authorized({"Manage Location Types"})
  @Transactional
  public void saveNewLocationTagFromMetadata(LocationTagDTO locationTagDTO) throws APIException {
    try {
      dao.setDisabledCheckConstraints();
      saveNewLocationTagFromDTO(locationTagDTO);
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
  @Authorized({"Manage Location Types"})
  @Transactional(propagation = Propagation.REQUIRED)
  public void saveNewLocationTagsFromMetadata(List<LocationTagDTO> locationTagDTOList)
      throws APIException {
    try {
      dao.setDisabledCheckConstraints();
      for (LocationTagDTO locationTagDTO : locationTagDTOList) {
        saveNewLocationTagFromDTO(locationTagDTO);
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
  public void updateLocationTagsFromProductionWithSameUuidWithInformationFromMetadata(
      Map<String, List<LocationTagDTO>> mapLocationTags) throws APIException {
    try {
      this.dao.setDisabledCheckConstraints();
      for (String uuid : mapLocationTags.keySet()) {
        List<LocationTagDTO> list = mapLocationTags.get(uuid);
        LocationTag pdsLocationTag = list.get(0).getLocationTag();
        LocationTag mdsLocationTag = list.get(1).getLocationTag();
        Integer mdsLocationTagId = pdsLocationTag.getLocationTagId();

        LocationTag foundPDS = locationService.getLocationTag(mdsLocationTag.getId());
        if (foundPDS != null && !foundPDS.getUuid().equals(mdsLocationTag.getUuid())) {
          moveUnrelatedProductionLocationTag(foundPDS);
        }

        if (!this.harmonizationLocationTagDao.isSwappable(pdsLocationTag)) {
          throw new APIException(
              String.format(
                  "Cannot update the Production Server Location tag [ID = {%s}, UUID = {%s}, NAME = {%s}] with the ID {%s} "
                      + "this new ID is already referencing an Existing Location Type In Metadata Server",
                  mdsLocationTag.getId(),
                  mdsLocationTag.getUuid(),
                  mdsLocationTag.getName(),
                  mdsLocationTagId));
        }
        List<Location> relatedLocations =
            harmonizationLocationTagDao.findLocationsByLocationTag(pdsLocationTag);
        // Get a fresh copy from the database.
        pdsLocationTag = locationService.getLocationTag(pdsLocationTag.getLocationTagId());
        overwriteLocationTag(pdsLocationTag, mdsLocationTag, relatedLocations);
      }

    } catch (Exception e) {
      if (e instanceof APIException) throw (APIException) e;
      throw new APIException(e.getMessage(), e);
    } finally {
      try {
        this.dao.setEnableCheckConstraints();
      } catch (Exception e) {
        throw new APIException(e);
      }
    }
  }

  @Override
  @Transactional
  public void replacePDSLocationTagsWithSameUuidWithThoseFromMDS(
      Map<String, List<LocationTagDTO>> locationTagsDTO) throws APIException {
    try {
      dao.setDisabledCheckConstraints();
      for (String uuid : locationTagsDTO.keySet()) {
        List<LocationTagDTO> list = locationTagsDTO.get(uuid);
        LocationTag pdsLocationTag = list.get(0).getLocationTag();
        LocationTag mdsLocationTag = list.get(1).getLocationTag();

        LocationTag foundPDS = locationService.getLocationTag(mdsLocationTag.getId());
        if (foundPDS != null && !foundPDS.getUuid().equals(mdsLocationTag.getUuid())) {
          moveUnrelatedProductionLocationTag(foundPDS);
        }

        if (pdsLocationTag.getLocationTagId().equals(mdsLocationTag.getLocationTagId())) {
          List<Location> relatedLocations =
              harmonizationLocationTagDao.findLocationsByLocationTag(pdsLocationTag);
          Integer nextId = harmonizationLocationTagDao.getNextLocationTagId();
          Integer currentLocationTagId = pdsLocationTag.getLocationTagId();
          harmonizationLocationTagDao.updateLocationTag(
              pdsLocationTag, nextId, UUID.randomUUID().toString());
          for (Location location : relatedLocations) {
            this.harmonizationLocationTagDao.updateLocation(location, currentLocationTagId, nextId);
          }
        } else {
          // Simply assign new uuid
          harmonizationLocationTagDao.updateLocationTag(
              pdsLocationTag, UUID.randomUUID().toString());
        }

        // Save the one from metadata server
        harmonizationLocationTagDao.insertLocationTag(mdsLocationTag);
      }
    } catch (Exception e) {
      e.printStackTrace();
      if (e instanceof APIException) throw (APIException) e;
      throw new APIException(e.getMessage(), e);
    } finally {
      try {
        dao.setEnableCheckConstraints();
      } catch (Exception e) {
        throw new APIException(e);
      }
    }
  }

  @Override
  @Authorized({"Manage Location Types"})
  public void deleteLocationTagsFromProduction(List<LocationTagDTO> locationTags)
      throws APIException {
    for (LocationTag locationTag : DTOUtils.fromLocationTagDTOs(locationTags)) {
      locationService.purgeLocationTag(locationTag);
    }
  }

  @Override
  public void saveManualLocationTagMappings(Map<LocationTag, LocationTag> manualLocationTagMappings)
      throws APIException {
    // Get Locations related to mapped one.
    for (Map.Entry<LocationTag, LocationTag> locationTagMapping :
        manualLocationTagMappings.entrySet()) {
      LocationTag pdsLocationTag = locationTagMapping.getKey();
      LocationTag mdsLocationTag = locationTagMapping.getValue();
      // Get related locations
      List<Location> locations = locationService.getLocationsByTag(pdsLocationTag);

      this.overwriteLocationTag(pdsLocationTag, mdsLocationTag, locations);
      // for (Location location : locations) {
      // harmonizationLocationTagDao.updateLocation(location,
      // pdsLocationTag.getLocationTagId(),
      // mdsLocationTag.getLocationTagId());
      // locationService.purgeLocationTag(pdsLocationTag);
      // }
    }
  }

  private void updateToGivenId(
      LocationTag locationTag, Integer locationTagId, List<Location> relatedLocations) {
    Integer currentLocationTagId = locationTag.getLocationTagId();
    this.harmonizationLocationTagDao.updateLocationTag(locationTag, locationTagId);
    for (Location location : relatedLocations) {
      this.harmonizationLocationTagDao.updateLocation(
          location, currentLocationTagId, locationTagId);
    }
  }

  private void overwriteLocationTag(
      LocationTag pdsToOverwrite, LocationTag fromMds, List<Location> relatedPdsLocations) {
    Integer currentLocationTagId = pdsToOverwrite.getLocationTagId();
    harmonizationLocationTagDao.overwriteLocationTagDetails(pdsToOverwrite, fromMds);
    for (Location location : relatedPdsLocations) {
      harmonizationLocationTagDao.updateLocation(
          location, currentLocationTagId, pdsToOverwrite.getLocationTagId());
    }
  }

  private void moveUnrelatedProductionLocationTag(LocationTag toBeMoved) throws APIException {
    if (!this.harmonizationLocationTagDao.isSwappable(toBeMoved)) {
      throw new APIException(
          String.format(
              "Cannot update the Production Server Location tag with ID {%s}, UUID {%s} and NAME {%s}. This Location Type is a "
                  + "reference from an Location Type of Metadata Server",
              toBeMoved.getId(), toBeMoved.getUuid(), toBeMoved.getName()));
    }
    List<Location> relatedLocations =
        harmonizationLocationTagDao.findLocationsByLocationTag(toBeMoved);
    Integer nextId = harmonizationLocationTagDao.getNextLocationTagId();
    updateToGivenId(toBeMoved, nextId, relatedLocations);
  }

  private void saveNewLocationTagFromDTO(LocationTagDTO locationTagDTO) throws APIException {
    LocationTag locationTagFromDTO = locationTagDTO.getLocationTag();
    LocationTag found = locationService.getLocationTag(locationTagFromDTO.getLocationTagId());
    if (found != null) {

      if (!this.harmonizationLocationTagDao.isSwappable(found)) {
        throw new APIException(
            String.format(
                "Cannot Insert Location tag with ID %s, UUID %s and NAME %s. This ID is being in use by another Location tag from Metadata server with UUID %s and name %s ",
                locationTagFromDTO.getId(),
                locationTagFromDTO.getUuid(),
                locationTagFromDTO.getName(),
                found.getUuid(),
                found.getName()));
      }
      List<Location> relatedLocations =
          this.harmonizationLocationTagDao.findLocationsByLocationTag(locationTagFromDTO);
      Integer nextId = this.harmonizationLocationTagDao.getNextLocationTagId();
      this.updateToGivenId(found, nextId, relatedLocations);
    }
    this.harmonizationLocationTagDao.insertLocationTag(locationTagFromDTO);
  }
}
