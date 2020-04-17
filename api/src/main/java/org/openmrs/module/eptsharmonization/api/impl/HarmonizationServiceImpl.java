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
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.EncounterType;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationService;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.openmrs.module.eptsharmonization.api.model.EncounterTypeDTO;

/** It is a default implementation of {@link HarmonizationService}. */
public class HarmonizationServiceImpl extends BaseOpenmrsService implements HarmonizationService {

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
  public List<EncounterTypeDTO> findAllMetadataEncounterNotContainedInProductionServer()
      throws APIException {
    List<EncounterType> mdsEncounterTypes = dao.findAllMetadataServerEncounterTypes();
    List<EncounterType> pdsEncounterTypes = dao.findAllProductionServerEncounterTypes();
    mdsEncounterTypes.removeAll(pdsEncounterTypes);
    return DTOUtils.fromEncounterTypes(mdsEncounterTypes);
  }

  @Override
  public List<EncounterTypeDTO> findAllProductionEncountersNotContainedInMetadataServer()
      throws APIException {
    List<EncounterType> pdsEncounterTypes = dao.findAllProductionServerEncounterTypes();
    List<EncounterType> mdsEncounterTypes = dao.findAllMetadataServerEncounterTypes();
    pdsEncounterTypes.removeAll(mdsEncounterTypes);
    return DTOUtils.fromEncounterTypes(pdsEncounterTypes);
  }

  @Override
  public List<EncounterTypeDTO> findAllMetadataEncounterPartialEqualsToProductionServer()
      throws APIException {
    List<EncounterType> mdsEncounterTypes =
        this.removeElementsWithDifferentIDsAndUUIDs(
            dao.findAllMetadataServerEncounterTypes(), dao.findAllProductionServerEncounterTypes());
    List<EncounterType> allMDS = dao.findAllMetadataServerEncounterTypes();
    List<EncounterType> allPDS = dao.findAllProductionServerEncounterTypes();
    allMDS.removeAll(allPDS);
    mdsEncounterTypes.removeAll(allMDS);
    return DTOUtils.fromEncounterTypes(mdsEncounterTypes);
  }

  @Override
  public List<EncounterTypeDTO> findAllProductionEncountersPartialEqualsToMetadataServer()
      throws APIException {
    List<EncounterType> pdsEncounterTypes =
        this.removeElementsWithDifferentIDsAndUUIDs(
            dao.findAllProductionServerEncounterTypes(), dao.findAllMetadataServerEncounterTypes());
    List<EncounterType> allPDS = dao.findAllProductionServerEncounterTypes();
    List<EncounterType> allMDS = dao.findAllMetadataServerEncounterTypes();
    allPDS.removeAll(allMDS);
    pdsEncounterTypes.removeAll(allPDS);
    return DTOUtils.fromEncounterTypes(pdsEncounterTypes);
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
}
