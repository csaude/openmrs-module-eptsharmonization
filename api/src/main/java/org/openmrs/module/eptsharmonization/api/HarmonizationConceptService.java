package org.openmrs.module.eptsharmonization.api;

import java.util.List;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.eptsharmonization.api.model.ConceptDTO;

/** @uthor Willa Mhawila<a.mhawila@gmail.com> on 7/1/20. */
public interface HarmonizationConceptService extends OpenmrsService {
  @Authorized({"View Concepts"})
  List<ConceptDTO> findPDSConceptsNotInMDS() throws APIException;

  List<ConceptDTO> findMDSConceptsNotInPDS() throws APIException;
}
