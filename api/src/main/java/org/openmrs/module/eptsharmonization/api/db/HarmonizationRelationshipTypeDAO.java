package org.openmrs.module.eptsharmonization.api.db;

import java.util.List;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.APIException;
import org.openmrs.api.db.DAOException;

/** @uthor Willa Mhawila<a.mhawila@gmail.com> on 5/26/20. */
public interface HarmonizationRelationshipTypeDAO {
  List<RelationshipType> findAllMDSRelationshipTypes() throws DAOException;

  List<Relationship> findRelationshipsByRelationshipType(RelationshipType relationshipType)
      throws DAOException;

  Integer getCountOfRelationshipsByRelationshipType(RelationshipType relationshipType)
      throws DAOException;

  List<RelationshipType> findPDSRelationshipTypesNotExistsInMDServer() throws DAOException;

  boolean isSwappable(RelationshipType relationshipType) throws DAOException;

  Integer getNextRelationshipTypeId() throws DAOException;

  RelationshipType findMDSRelationshipTypeByUuid(String uuid) throws APIException;

  RelationshipType findPDSRelationshipTypeByUuid(String uuid) throws APIException;

  RelationshipType updateRelationshipType(RelationshipType relationshipType, Integer nextId)
      throws DAOException;

  void deleteRelationshipType(RelationshipType relationshipType) throws DAOException;

  RelationshipType updateRelationshipType(RelationshipType relationshipType, String newUuid)
      throws DAOException;

  RelationshipType updateRelationshipType(
      RelationshipType relationshipType, Integer newId, String newUuid) throws DAOException;

  RelationshipType overwriteRelationshipTypeDetails(
      RelationshipType toOverwrite, RelationshipType toOverwriteWith) throws DAOException;

  void updateRelationship(Relationship relationship, Integer relationshipTypeId)
      throws DAOException;

  void insertRelationshipType(RelationshipType relationshipType) throws DAOException;

  List<Relationship> getRelashionshipsByType(RelationshipType relationshipType) throws DAOException;
}
