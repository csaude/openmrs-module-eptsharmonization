package org.openmrs.module.eptsharmonization.api.impl;

import java.util.List;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.eptsharmonization.api.HarmonizationConceptService;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationConceptDao;
import org.openmrs.module.eptsharmonization.api.model.ConceptDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** @uthor Willa Mhawila<a.mhawila@gmail.com> on 7/1/20. */
@Transactional
@Service(HarmonizationConceptServiceImpl.BEAN_NAME)
public class HarmonizationConceptServiceImpl extends BaseOpenmrsService
    implements HarmonizationConceptService {
  public static final String BEAN_NAME = "eptsharmonization.harmonizationConceptService";
  private HarmonizationConceptDao harmonizationConceptDao;

  @Autowired
  public void setHarmonizationConceptDao(HarmonizationConceptDao harmonizationConceptDao) {
    this.harmonizationConceptDao = harmonizationConceptDao;
  }

  /**
   * Compares concept_id, uuid, concept class and datatype to determine whether a particular concept
   * is in MDS That for a concept from PDS to be considered that it already exists in MDS then there
   * should exist a corresponding concept in MDS whose concept_id, uuid, concept class and datatype
   * are exactly the same as the one from PDS.
   *
   * @return
   * @throws APIException
   */
  @Override
  public List<ConceptDTO> findPDSConceptsNotInMDS() throws APIException {
    return harmonizationConceptDao.findAllPDSConceptsNotInMDS();
  }

  @Override
  public List<ConceptDTO> findMDSConceptsNotInPDS() throws APIException {
    return harmonizationConceptDao.findAllMDSConceptsNotInPDS();
  }
}
