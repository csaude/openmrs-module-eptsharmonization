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
import java.util.UUID;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.VisitService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.eptsharmonization.HarmonizationUtils;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationVisitTypeService;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationVisitTypeDAO;
import org.openmrs.module.eptsharmonization.api.model.VisitTypeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** It is a default implementation of {@link HarmonizationVisitTypeService}. */
@Transactional
@Service(HarmonizationVisitTypeServiceImpl.BEAN_NAME)
public class HarmonizationVisitTypeServiceImpl extends BaseOpenmrsService
    implements HarmonizationVisitTypeService {
  public static final String BEAN_NAME = "eptsharmonization.harmonizationVisitTypeService";

  private HarmonizationServiceDAO dao;

  private HarmonizationVisitTypeDAO harmonizationVisitTypeDAO;

  private VisitService visitService;

  private AdministrationService adminService;

  @Autowired
  public void setVisitService(VisitService visitService) {
    this.visitService = visitService;
  }

  @Autowired
  public void setDao(HarmonizationServiceDAO dao) {
    this.dao = dao;
  }

  @Autowired
  public void setHarmonizationVisitTypeDAO(HarmonizationVisitTypeDAO harmonizationVisitTypeDAO) {
    this.harmonizationVisitTypeDAO = harmonizationVisitTypeDAO;
  }

  @Autowired
  public void setAdminService(AdministrationService adminService) {
    this.adminService = adminService;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Visit Types"})
  public List<VisitTypeDTO> findAllMetadataVisitTypesNotSharingUuidWithAnyFromProduction()
      throws APIException {
    List<VisitType> mdsVisitTypes = harmonizationVisitTypeDAO.findAllMDSVisitTypes();
    List<VisitType> pdsVisitTypes = visitService.getAllVisitTypes(true);
    mdsVisitTypes.removeAll(pdsVisitTypes);
    return DTOUtils.fromVisitTypes(mdsVisitTypes);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Visit Types"})
  public List<VisitTypeDTO> findAllProductionVisitTypesNotSharingUuidWithAnyFromMetadata()
      throws APIException {
    List<VisitType> pdsVisitTypes = visitService.getAllVisitTypes();
    List<VisitType> mdsVisitTypes = harmonizationVisitTypeDAO.findAllMDSVisitTypes();
    pdsVisitTypes.removeAll(mdsVisitTypes);
    return DTOUtils.fromVisitTypes(pdsVisitTypes);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Visit Types"})
  public List<VisitTypeDTO> findAllUselessProductionVisitTypesNotSharingUuidWithAnyFromMetadata()
      throws APIException {
    List<VisitTypeDTO> allInPDSNotSharingUuidWithMDS =
        findAllProductionVisitTypesNotSharingUuidWithAnyFromMetadata();
    List<VisitTypeDTO> uselessOnes = new ArrayList<>();
    for (VisitTypeDTO visitTypeDTO : allInPDSNotSharingUuidWithMDS) {
      int count = getNumberOfAffectedVisits(visitTypeDTO);
      if (count == 0) {
        uselessOnes.add(visitTypeDTO);
      }
    }
    return uselessOnes;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Visit Types"})
  public Map<VisitTypeDTO, Integer>
      findAllUsedProductionVisitTypesNotSharingUuidWithAnyFromMetadata() throws APIException {
    List<VisitTypeDTO> allInPDSNotSharingUuidWithMDS =
        findAllProductionVisitTypesNotSharingUuidWithAnyFromMetadata();
    Map<VisitTypeDTO, Integer> usedOnes = new HashMap<>();
    for (VisitTypeDTO visitTypeDTO : allInPDSNotSharingUuidWithMDS) {
      int count = getNumberOfAffectedVisits(visitTypeDTO);
      if (count > 0) {
        usedOnes.put(visitTypeDTO, count);
      }
    }
    return usedOnes;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Visit Types"})
  public List<VisitTypeDTO> findAllMetadataVisitTypesNotInHarmonyWithProduction()
      throws APIException {
    List<VisitType> mdsVisitTypes = harmonizationVisitTypeDAO.findAllMDSVisitTypes();
    List<VisitType> pdsVisitTypes = visitService.getAllVisitTypes(true);
    HarmonizationUtils.removeAllHarmonizedElements(mdsVisitTypes, pdsVisitTypes);
    return DTOUtils.fromVisitTypes(mdsVisitTypes);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Visit Types"})
  public List<VisitTypeDTO> findAllProductionVisitTypesNotInHarmonyWithMetadata()
      throws APIException {
    List<VisitType> notInHarmonyWithMetadata =
        harmonizationVisitTypeDAO.findPDSVisitTypesNotExistsInMDServer();
    return DTOUtils.fromVisitTypes(notInHarmonyWithMetadata);
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Visit Types"})
  public Map<String, List<VisitTypeDTO>> findAllVisitTypesWithDifferentNameAndSameUUIDAndID()
      throws APIException {
    List<VisitType> allPDS = visitService.getAllVisitTypes();
    List<VisitType> allMDS = harmonizationVisitTypeDAO.findAllMDSVisitTypes();
    Map<String, List<VisitTypeDTO>> result = new HashMap<>();
    Map<String, List<VisitType>> map =
        HarmonizationUtils.findElementsWithDifferentNamesSameUuidsAndIds(allPDS, allMDS);
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromVisitTypes(map.get(key)));
    }
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  @Authorized({"View Visit Types"})
  public Map<String, List<VisitTypeDTO>> findAllVisitTypesWithDifferentIDAndSameUUID()
      throws APIException {
    List<VisitType> allPDS = visitService.getAllVisitTypes();
    List<VisitType> allMDS = harmonizationVisitTypeDAO.findAllMDSVisitTypes();
    Map<String, List<VisitTypeDTO>> result = new HashMap<>();
    Map<String, List<VisitType>> map =
        HarmonizationUtils.findElementsWithDifferentIdsSameUuids(allPDS, allMDS);
    for (String key : map.keySet()) {
      result.put(key, DTOUtils.fromVisitTypes(map.get(key)));
    }
    return result;
  }

  @Override
  public int getNumberOfAffectedVisits(VisitTypeDTO visitTypeDTO) {
    return harmonizationVisitTypeDAO.getCountOfVisitsByVisitType(visitTypeDTO.getVisitType());
  }

  @Override
  @Authorized({"Manage Visit Types"})
  public void saveVisitTypesWithDifferentNames(Map<String, List<VisitTypeDTO>> visitTypes)
      throws APIException {
    for (String key : visitTypes.keySet()) {
      List<VisitTypeDTO> list = visitTypes.get(key);
      VisitTypeDTO pdsVisit = list.get(0);
      VisitTypeDTO mdsVisit = list.get(1);
      VisitType visitType = visitService.getVisitType(pdsVisit.getVisitType().getId());
      visitType.setName(mdsVisit.getVisitType().getName());
      visitType.setDescription(mdsVisit.getVisitType().getDescription());
      visitService.saveVisitType(visitType);
    }
  }

  @Override
  public VisitType findMDSVisitTypeByUuid(String uuid) throws APIException {
    return this.harmonizationVisitTypeDAO.findMDSVisitTypeByUuid(uuid);
  }

  @Override
  public VisitType findPDSVisitTypeByUuid(String uuid) throws APIException {
    return this.harmonizationVisitTypeDAO.findPDSVisitTypeByUuid(uuid);
  }

  @Override
  @Authorized({"Manage Visit Types"})
  @Transactional
  public void saveNewVisitTypeFromMetadata(VisitTypeDTO visitTypeDTO) throws APIException {
    try {
      dao.setDisabledCheckConstraints();
      saveNewVisitTypeFromDTO(visitTypeDTO);
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
  @Authorized({"Manage Visit Types"})
  @Transactional(propagation = Propagation.REQUIRED)
  public void saveNewVisitTypesFromMetadata(List<VisitTypeDTO> visitTypeDTOList)
      throws APIException {
    try {
      dao.setDisabledCheckConstraints();
      for (VisitTypeDTO visitTypeDTO : visitTypeDTOList) {
        saveNewVisitTypeFromDTO(visitTypeDTO);
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
  public void updateVisitTypesFromProductionWithSameUuidWithInformationFromMetadata(
      Map<String, List<VisitTypeDTO>> mapVisitTypes) throws APIException {
    try {
      this.dao.setDisabledCheckConstraints();
      for (String uuid : mapVisitTypes.keySet()) {
        List<VisitTypeDTO> list = mapVisitTypes.get(uuid);
        VisitType pdsVisitType = list.get(0).getVisitType();
        VisitType mdsVisitType = list.get(1).getVisitType();

        VisitType foundPDS = visitService.getVisitType(mdsVisitType.getId());
        if (foundPDS != null && !foundPDS.getUuid().equals(mdsVisitType.getUuid())) {
          moveUnrelatedProductionVisitType(foundPDS);
        }

        if (!this.harmonizationVisitTypeDAO.isSwappable(pdsVisitType)) {
          throw new APIException(
              String.format(
                  "Cannot update the Production Server Visit type [ID = {%s}, UUID = {%s}, NAME = {%s}] with the ID {%s} "
                      + "this new ID is already referencing an Existing Visit Type In Metadata Server",
                  mdsVisitType.getId(),
                  mdsVisitType.getUuid(),
                  mdsVisitType.getName(),
                  mdsVisitType.getVisitTypeId()));
        }
        List<Visit> relatedVisits = harmonizationVisitTypeDAO.findVisitsByVisitType(pdsVisitType);
        // Get a fresh copy from the database.
        pdsVisitType = visitService.getVisitType(pdsVisitType.getVisitTypeId());
        overwriteVisitType(pdsVisitType, mdsVisitType, relatedVisits);

        // Update global properties mapping if any.
        updateGPWithNewVisitTypeId(pdsVisitType.getVisitTypeId(), mdsVisitType.getVisitTypeId());
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
  public void replacePDSVisitTypesWithSameUuidWithThoseFromMDS(
      Map<String, List<VisitTypeDTO>> visitTypesDTO) throws APIException {
    try {
      dao.setDisabledCheckConstraints();
      for (String uuid : visitTypesDTO.keySet()) {
        List<VisitTypeDTO> list = visitTypesDTO.get(uuid);
        VisitType pdsVisitType = list.get(0).getVisitType();
        VisitType mdsVisitType = list.get(1).getVisitType();

        VisitType foundPDS = visitService.getVisitType(mdsVisitType.getId());
        if (foundPDS != null && !foundPDS.getUuid().equals(mdsVisitType.getUuid())) {
          moveUnrelatedProductionVisitType(foundPDS);
        }

        if (pdsVisitType.getVisitTypeId().equals(mdsVisitType.getVisitTypeId())) {
          List<Visit> relatedVisits = harmonizationVisitTypeDAO.findVisitsByVisitType(pdsVisitType);
          Integer nextId = harmonizationVisitTypeDAO.getNextVisitTypeId();
          harmonizationVisitTypeDAO.updateVisitType(
              pdsVisitType, nextId, UUID.randomUUID().toString());

          updateGPWithNewVisitTypeId(pdsVisitType.getVisitTypeId(), nextId);
          for (Visit visit : relatedVisits) {
            this.harmonizationVisitTypeDAO.updateVisit(visit, nextId);
          }
        } else {
          // Simply assign new uuid
          harmonizationVisitTypeDAO.updateVisitType(pdsVisitType, UUID.randomUUID().toString());
        }

        // Save the one from metadata server
        harmonizationVisitTypeDAO.insertVisitType(mdsVisitType);
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
  @Authorized({"Manage Visit Types"})
  public void deleteVisitTypesFromProduction(List<VisitTypeDTO> visitTypes) throws APIException {
    for (VisitType visitType : DTOUtils.fromVisitTypeDTOs(visitTypes)) {
      visitService.purgeVisitType(visitType);
    }
  }

  @Override
  public void saveManualVisitTypeMappings(Map<VisitType, VisitType> manualVisitTypeMappings)
      throws APIException {
    // Get Visits related to mapped one.
    for (Map.Entry<VisitType, VisitType> visitTypeMapping : manualVisitTypeMappings.entrySet()) {
      VisitType pdsVisitType = visitTypeMapping.getKey();
      VisitType mdsVisitType = visitTypeMapping.getValue();
      // Get related visits
      List<Visit> visits =
          visitService.getVisits(
              Arrays.asList(pdsVisitType),
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              true,
              true);

      this.overwriteVisitType(pdsVisitType, mdsVisitType, visits);

      //			for (Visit visit : visits) {
      //				harmonizationVisitTypeDAO.updateVisit(visit, mdsVisitType.getVisitTypeId());
      //				visitService.purgeVisitType(pdsVisitType);
      //			}
    }
  }

  private void updateToGivenId(
      VisitType visitType, Integer visitTypeId, List<Visit> relatedVisits) {
    this.harmonizationVisitTypeDAO.updateVisitType(visitType, visitTypeId);
    for (Visit visit : relatedVisits) {
      this.harmonizationVisitTypeDAO.updateVisit(visit, visitTypeId);
    }
  }

  private void overwriteVisitType(
      VisitType pdsToOverwrite, VisitType fromMds, List<Visit> relatedPdsVisits) {
    harmonizationVisitTypeDAO.overwriteVisitTypeDetails(pdsToOverwrite, fromMds);
    for (Visit visit : relatedPdsVisits) {
      harmonizationVisitTypeDAO.updateVisit(visit, pdsToOverwrite.getVisitTypeId());
    }
  }

  private void moveUnrelatedProductionVisitType(VisitType toBeMoved) throws APIException {
    if (!this.harmonizationVisitTypeDAO.isSwappable(toBeMoved)) {
      throw new APIException(
          String.format(
              "Cannot update the Production Server Visit type with ID {%s}, UUID {%s} and NAME {%s}. This Visit Type is a "
                  + "reference from an Visit Type of Metadata Server",
              toBeMoved.getId(), toBeMoved.getUuid(), toBeMoved.getName()));
    }
    List<Visit> relatedVisits = harmonizationVisitTypeDAO.findVisitsByVisitType(toBeMoved);
    Integer nextId = harmonizationVisitTypeDAO.getNextVisitTypeId();
    updateToGivenId(toBeMoved, nextId, relatedVisits);

    // Update the global property mapping if any
    updateGPWithNewVisitTypeId(toBeMoved.getVisitTypeId(), nextId);
  }

  private void saveNewVisitTypeFromDTO(VisitTypeDTO visitTypeDTO) throws APIException {
    VisitType visitTypeFromDTO = visitTypeDTO.getVisitType();
    VisitType found = visitService.getVisitType(visitTypeFromDTO.getVisitTypeId());
    if (found != null) {

      if (!this.harmonizationVisitTypeDAO.isSwappable(found)) {
        throw new APIException(
            String.format(
                "Cannot Insert Visit type with ID %s, UUID %s and NAME %s. This ID is being in use by another Visit type from Metadata server with UUID %s and name %s ",
                visitTypeFromDTO.getId(),
                visitTypeFromDTO.getUuid(),
                visitTypeFromDTO.getName(),
                found.getUuid(),
                found.getName()));
      }
      List<Visit> relatedVisits =
          this.harmonizationVisitTypeDAO.findVisitsByVisitType(visitTypeFromDTO);
      Integer nextId = this.harmonizationVisitTypeDAO.getNextVisitTypeId();
      this.updateToGivenId(found, nextId, relatedVisits);
      updateGPWithNewVisitTypeId(found.getVisitTypeId(), nextId);
    }
    this.harmonizationVisitTypeDAO.insertVisitType(visitTypeFromDTO);
  }

  private void updateGPWithNewVisitTypeId(Integer oldVisitTypeId, Integer newVisitTypeId) {
    final String V_TO_E_GP = "visits.encounterTypeToVisitTypeMapping";
    String gpValue = adminService.getGlobalProperty(V_TO_E_GP);
    if (!StringUtils.isEmpty(gpValue)) {
      String toReplaceMiddle = ":" + oldVisitTypeId + "\\s*,";
      String replacement = ":" + newVisitTypeId + ",";
      gpValue = gpValue.replaceAll(toReplaceMiddle, replacement);

      String toReplaceEnd = ":" + oldVisitTypeId + "\\s*$";
      replacement = ":" + newVisitTypeId;
      gpValue = gpValue.replaceAll(toReplaceEnd, replacement);

      adminService.setGlobalProperty(V_TO_E_GP, gpValue);
    }
  }
}
