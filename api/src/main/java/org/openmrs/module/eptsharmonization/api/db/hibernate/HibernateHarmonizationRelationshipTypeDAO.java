package org.openmrs.module.eptsharmonization.api.db.hibernate;

import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationRelationshipTypeDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/** @uthor Willa Mhawila<a.mhawila@gmail.com> on 5/26/20. */
@Repository(HibernateHarmonizationRelationshipTypeDAO.BEAN_NAME)
public class HibernateHarmonizationRelationshipTypeDAO implements HarmonizationRelationshipTypeDAO {
  public static final String BEAN_NAME =
      "eptsharmonization.hibernateHarmonizationRelationshipTypeDao";
  private SessionFactory sessionFactory;

  /** @param sessionFactory the sessionFactory to set */
  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public List<RelationshipType> findAllMDSRelationshipTypes() throws DAOException {
    clearAndFlushSession();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select v.* from _relationship_type v")
            .addEntity(RelationshipType.class);
    List<RelationshipType> relationshipTypeList = query.list();
    clearAndFlushSession();
    return relationshipTypeList;
  }

  @Override
  public List<Relationship> findRelationshipsByRelationshipType(
      final RelationshipType relationshipType) throws DAOException {
    this.clearAndFlushSession();
    return relationshipsByRelationshipTypeCriteria(relationshipType).list();
  }

  @Override
  public Integer getCountOfRelationshipsByRelationshipType(final RelationshipType relationshipType)
      throws DAOException {
    final Criteria criteria = relationshipsByRelationshipTypeCriteria(relationshipType);
    criteria.setProjection(Projections.rowCount());
    return ((Number) criteria.uniqueResult()).intValue();
  }

  @Override
  public List<RelationshipType> findPDSRelationshipTypesNotExistsInMDServer() throws DAOException {
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select relationship_type.* from relationship_type "
                    + "where NOT EXISTS (select * from _relationship_type "
                    + "where _relationship_type.relationship_type_id = relationship_type.relationship_type_id "
                    + "and _relationship_type.uuid = relationship_type.uuid "
                    + "and _relationship_type.a_is_to_b = relationship_type.a_is_to_b "
                    + "and _relationship_type.b_is_to_a = relationship_type.b_is_to_a)")
            .addEntity(RelationshipType.class);
    return query.list();
  }

  @Override
  public RelationshipType findMDSRelationshipTypeByUuid(String uuid) throws APIException {
    this.clearAndFlushSession();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select r.* from _relationship_type r where r.uuid = :uuid ")
            .addEntity(RelationshipType.class);
    query.setString("uuid", uuid);
    return (RelationshipType) query.uniqueResult();
  }

  @Override
  public RelationshipType findPDSRelationshipTypeByUuid(String uuid) throws APIException {
    this.clearAndFlushSession();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select r.* from relationship_type r where r.uuid = :uuid ")
            .addEntity(RelationshipType.class);
    query.setString("uuid", uuid);
    return (RelationshipType) query.uniqueResult();
  }

  @Override
  public boolean isSwappable(RelationshipType relationshipType) throws DAOException {
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select count(*) from _relationship_type v where v.relationship_type_id = :id and v.uuid = :uuid "
                    + "and v.a_is_to_b = :aIsToB and v.b_is_to_a = :bIsToA")
            .setInteger("id", relationshipType.getId())
            .setString("uuid", relationshipType.getUuid())
            .setString("aIsToB", relationshipType.getaIsToB())
            .setString("bIsToA", relationshipType.getbIsToA());
    int matchingMDSRecords = ((Number) query.uniqueResult()).intValue();
    return matchingMDSRecords == 0;
  }

  @Override
  public Integer getNextRelationshipTypeId() throws DAOException {
    Integer maxId =
        (Integer)
            this.sessionFactory
                .getCurrentSession()
                .createSQLQuery("select max(relationship_type_id) from relationship_type ")
                .uniqueResult();
    return ++maxId;
  }

  @Override
  public RelationshipType updateRelationshipType(RelationshipType relationshipType, Integer nextId)
      throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update relationship_type set relationship_type_id =:nextId where relationship_type_id = :currentId")
        .setInteger("nextId", nextId)
        .setInteger("currentId", relationshipType.getRelationshipTypeId())
        .executeUpdate();
    clearAndFlushSession();

    final Criteria searchCriteria =
        this.sessionFactory
            .getCurrentSession()
            .createCriteria(RelationshipType.class, "relationshipType");
    searchCriteria.add(Restrictions.eq("relationshipTypeId", nextId));
    return (RelationshipType) searchCriteria.uniqueResult();
  }

  @Override
  public RelationshipType updateRelationshipType(RelationshipType relationshipType, String newUuid)
      throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update relationship_type set uuid =:uuid where relationship_type_id = :currentId")
        .setString("uuid", newUuid)
        .setInteger("currentId", relationshipType.getRelationshipTypeId())
        .executeUpdate();
    clearAndFlushSession();

    final Criteria searchCriteria =
        this.sessionFactory
            .getCurrentSession()
            .createCriteria(RelationshipType.class, "relationshipType");
    searchCriteria.add(
        Restrictions.eq("relationshipTypeId", relationshipType.getRelationshipTypeId()));
    return (RelationshipType) searchCriteria.uniqueResult();
  }

  @Override
  public RelationshipType updateRelationshipType(
      RelationshipType relationshipType, Integer newId, String newUuid) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update relationship_type set relationship_type_id = :id, uuid=:uuid where uuid = :currentUuid")
        .setInteger("id", newId)
        .setString("uuid", newUuid)
        .setString("currentUuid", relationshipType.getUuid())
        .executeUpdate();
    clearAndFlushSession();
    final Criteria searchCriteria =
        this.sessionFactory
            .getCurrentSession()
            .createCriteria(RelationshipType.class, "relationshipType");
    searchCriteria.add(Restrictions.eq("relationshipTypeId", newId));
    return (RelationshipType) searchCriteria.uniqueResult();
  }

  @Override
  public RelationshipType overwriteRelationshipTypeDetails(
      RelationshipType toOverwrite, RelationshipType toOverwriteWith) throws DAOException {
    sessionFactory.getCurrentSession().evict(toOverwrite);
    sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update relationship_type set relationship_type_id=:id, a_is_to_b=:aIsToB, b_is_to_a=:bIsToA, weight = :weight, "
                + " preferred=:preferred, description=:description,date_created=:dateCreated, retired=:retired, retire_reason=:retireReason, uuid=:uuid "
                + " where relationship_type_id=:currentId")
        .setInteger("id", toOverwriteWith.getRelationshipTypeId())
        .setString("aIsToB", toOverwriteWith.getaIsToB())
        .setString("bIsToA", toOverwriteWith.getbIsToA())
        .setInteger("weight", toOverwriteWith.getWeight())
        .setBoolean("preferred", toOverwriteWith.isPreferred())
        .setString("description", toOverwriteWith.getDescription())
        .setInteger("currentId", toOverwrite.getRelationshipTypeId())
        .setDate("dateCreated", toOverwriteWith.getDateCreated())
        .setBoolean("retired", toOverwriteWith.isRetired())
        .setString("retireReason", toOverwriteWith.getRetireReason())
        .setString("uuid", toOverwriteWith.getUuid())
        .executeUpdate();
    clearAndFlushSession();

    final Criteria searchCriteria =
        this.sessionFactory
            .getCurrentSession()
            .createCriteria(RelationshipType.class, "relationshipType");
    searchCriteria.add(
        Restrictions.eq("relationshipTypeId", toOverwriteWith.getRelationshipTypeId()));
    return (RelationshipType) searchCriteria.uniqueResult();
  }

  @Override
  public void updateRelationship(Relationship relationship, Integer relationshipTypeId)
      throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery("update relationship set relationship = :id where uuid = :uuid")
        .setInteger("id", relationshipTypeId)
        .setString("uuid", relationship.getUuid())
        .executeUpdate();
    clearAndFlushSession();
  }

  @Override
  public void insertRelationshipType(RelationshipType relationshipType) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "insert into relationship_type(relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, retired, retire_reason, uuid) "
                + "VALUES(:relationshipTypeId, :aIsToB, :bIsToA, :preferred, :weight, :description, :creator, :dateCreated, :retired, :retireReason, :uuid)")
        .setInteger("relationshipTypeId", relationshipType.getRelationshipTypeId())
        .setString("aIsToB", relationshipType.getaIsToB())
        .setString("bIsToA", relationshipType.getbIsToA())
        .setString("description", relationshipType.getDescription())
        .setBoolean("preferred", relationshipType.isPreferred())
        .setInteger("weight", relationshipType.getWeight())
        .setInteger("creator", Context.getAuthenticatedUser().getUserId())
        .setDate("dateCreated", relationshipType.getDateCreated())
        .setBoolean("retired", relationshipType.isRetired())
        .setString("retireReason", relationshipType.getRetireReason())
        .setString("uuid", relationshipType.getUuid())
        .executeUpdate();
    clearAndFlushSession();
  }

  @Override
  public List<Relationship> getRelashionshipsByType(RelationshipType relationshipType)
      throws DAOException {
    Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Relationship.class);
    criteria.add(Restrictions.eq("relationshipType", relationshipType));
    return (List<Relationship>) criteria.list();
  }

  @Override
  public void deleteRelationshipType(RelationshipType relationshipType) throws DAOException {
    clearAndFlushSession();
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "delete from relationship_type where relationship_type_id =%s ",
                relationshipType.getRelationshipTypeId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  private void clearAndFlushSession() {
    Context.clearSession();
    Context.flushSession();
  }

  private Criteria relationshipsByRelationshipTypeCriteria(RelationshipType relationshipType) {
    Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Relationship.class);
    criteria.add(Restrictions.eq("relationshipType", relationshipType));
    return criteria;
  }
}
