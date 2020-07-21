package org.openmrs.module.eptsharmonization.api.db;

import java.util.List;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.db.DAOException;

/** @uthor Willa Mhawila<a.mhawila@gmail.com> on 5/26/20. */
public interface HarmonizationLocationTagDAO {
  List<LocationTag> findAllMDSLocationTags() throws DAOException;

  List<Location> findLocationsByLocationTag(LocationTag locationTag) throws DAOException;

  Integer getCountOfLocationsByLocationTag(LocationTag locationTag) throws DAOException;

  List<LocationTag> findPDSLocationTagsNotExistsInMDServer() throws DAOException;

  LocationTag findMDSLocationTagByUuid(String uuid) throws DAOException;

  LocationTag findPDSLocationTagByUuid(String uuid) throws DAOException;

  boolean isSwappable(LocationTag locationTag) throws DAOException;

  Integer getNextLocationTagId() throws DAOException;

  void updateLocationTag(LocationTag locationTag, Integer nextId) throws DAOException;

  void updateLocationTag(LocationTag locationTag, String newUuid) throws DAOException;

  void updateLocationTag(LocationTag locationTag, Integer newId, String newUuid)
      throws DAOException;

  void overwriteLocationTagDetails(LocationTag toOverwrite, LocationTag toOverwriteWith)
      throws DAOException;

  void updateLocation(Location location, Integer oldLocationTagId, Integer newLocationTagId)
      throws DAOException;

  void insertLocationTag(LocationTag locationTag) throws DAOException;
}
