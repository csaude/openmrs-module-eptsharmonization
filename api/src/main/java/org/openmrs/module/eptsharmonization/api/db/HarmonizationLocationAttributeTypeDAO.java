package org.openmrs.module.eptsharmonization.api.db;

import java.util.List;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.api.db.DAOException;

/** @uthor Willa Mhawila<a.mhawila@gmail.com> on 5/26/20. */
public interface HarmonizationLocationAttributeTypeDAO {
  List<LocationAttributeType> findAllMDSLocationAttributeTypes() throws DAOException;

  List<LocationAttribute> findLocationAttributesByLocationAttributeType(
      LocationAttributeType locationAttributeType) throws DAOException;

  Integer getCountOfLocationAttributesByLocationAttributeType(
      LocationAttributeType locationAttributeType) throws DAOException;

  boolean isSwappable(LocationAttributeType locationAttributeType) throws DAOException;

  LocationAttributeType findMDSLocationAttributeTypeByUuid(String uuid) throws DAOException;

  LocationAttributeType findPDSLocationAttributeTypeByUuid(String uuid) throws DAOException;

  Integer getNextLocationAttributeTypeId() throws DAOException;

  LocationAttributeType updateLocationAttributeType(
      LocationAttributeType locationAttributeType, Integer nextId) throws DAOException;

  LocationAttributeType updateLocationAttributeType(
      LocationAttributeType locationAttributeType, String newUuid) throws DAOException;

  LocationAttributeType updateLocationAttributeType(
      LocationAttributeType locationAttributeType, Integer newId, String newUuid)
      throws DAOException;

  LocationAttributeType overwriteLocationAttributeTypeDetails(
      LocationAttributeType toOverwrite, LocationAttributeType toOverwriteWith) throws DAOException;

  void updateLocationAttribute(LocationAttribute locationAttribute, Integer locationAttributeTypeId)
      throws DAOException;

  void insertLocationAttributeType(LocationAttributeType locationAttributeType) throws DAOException;

  void deleteLocationAttributeType(LocationAttributeType locationAttributeType) throws DAOException;
}
