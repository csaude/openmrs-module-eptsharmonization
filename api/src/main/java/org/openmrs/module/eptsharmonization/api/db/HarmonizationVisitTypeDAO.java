package org.openmrs.module.eptsharmonization.api.db;

import java.util.List;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.db.DAOException;

/** @uthor Willa Mhawila<a.mhawila@gmail.com> on 5/26/20. */
public interface HarmonizationVisitTypeDAO {
  List<VisitType> findAllMDSVisitTypes() throws DAOException;

  List<Visit> findVisitsByVisitType(VisitType visitType) throws DAOException;

  Integer getCountOfVisitsByVisitType(VisitType visitType) throws DAOException;

  List<VisitType> findPDSVisitTypesNotExistsInMDServer() throws DAOException;

  VisitType findMDSVisitTypeByUuid(String uuid) throws DAOException;

  VisitType findPDSVisitTypeByUuid(String uuid) throws DAOException;

  boolean isSwappable(VisitType visitType) throws DAOException;

  Integer getNextVisitTypeId() throws DAOException;

  VisitType updateVisitType(VisitType visitType, Integer nextId) throws DAOException;

  VisitType updateVisitType(VisitType visitType, String newUuid) throws DAOException;

  VisitType updateVisitType(VisitType visitType, Integer newId, String newUuid) throws DAOException;

  VisitType overwriteVisitTypeDetails(VisitType toOverwrite, VisitType toOverwriteWith)
      throws DAOException;

  void updateVisit(Visit visit, Integer visitTypeId) throws DAOException;

  void insertVisitType(VisitType visitType) throws DAOException;
}
