package org.openmrs.module.eptsharmonization.api.db;

import java.util.List;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.eptsharmonization.api.model.ConceptDTO;

/** @uthor Willa Mhawila<a.mhawila@gmail.com> on 5/26/20. */
public interface HarmonizationConceptDao {
  List<ConceptDTO> findAllPDSConceptsNotInMDS() throws DAOException;

  List<ConceptDTO> findAllMDSConceptsNotInPDS() throws DAOException;
}
