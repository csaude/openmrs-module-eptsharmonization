package org.openmrs.module.eptsharmonization.api.db.hibernate;

import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationVisitTypeDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/** @uthor Willa Mhawila<a.mhawila@gmail.com> on 5/26/20. */
@Repository(HibernateHarmonizationVisitTypeDAO.BEAN_NAME)
public class HibernateHarmonizationVisitTypeDAO implements HarmonizationVisitTypeDAO {
  public static final String BEAN_NAME = "eptsharmonization.hibernateHarmonizationVisitTypeDAO";
  private SessionFactory sessionFactory;

  /** @param sessionFactory the sessionFactory to set */
  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public List<VisitType> findAllMDSVisitTypes() throws DAOException {
    clearAndFlushSession();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select v.* from _visit_type v")
            .addEntity(VisitType.class);
    List<VisitType> visitTypeList = query.list();
    clearAndFlushSession();
    return visitTypeList;
  }

  @Override
  public List<Visit> findVisitsByVisitType(final VisitType visitType) throws DAOException {
    this.clearAndFlushSession();
    return visitsByVisitTypeCriteria(visitType).list();
  }

  @Override
  public Integer getCountOfVisitsByVisitType(final VisitType visitType) throws DAOException {
    final Criteria criteria = visitsByVisitTypeCriteria(visitType);
    criteria.setProjection(Projections.rowCount());
    return ((Number) criteria.uniqueResult()).intValue();
  }

  @Override
  public VisitType findMDSVisitTypeByUuid(String uuid) throws DAOException {
    this.clearAndFlushSession();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select v.* from _visit_type v where v.uuid = :uuid ")
            .addEntity(VisitType.class);
    query.setString("uuid", uuid);
    return (VisitType) query.uniqueResult();
  }

  @Override
  public VisitType findPDSVisitTypeByUuid(String uuid) throws DAOException {
    this.clearAndFlushSession();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select v.* from visit_type v where v.uuid = :uuid ")
            .addEntity(VisitType.class);
    query.setString("uuid", uuid);
    return (VisitType) query.uniqueResult();
  }

  @Override
  public List<VisitType> findPDSVisitTypesNotExistsInMDServer() throws DAOException {
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select visit_type.* from visit_type "
                    + "where NOT EXISTS (select * from _visit_type "
                    + "where _visit_type.visit_type_id = visit_type.visit_type_id "
                    + "and _visit_type.uuid = visit_type.uuid "
                    + "and _visit_type.name = visit_type.name)")
            .addEntity(VisitType.class);
    return query.list();
  }

  @Override
  public boolean isSwappable(VisitType visitType) throws DAOException {
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select count(*) from _visit_type v where v.visit_type_id = :id and v.uuid = :uuid and v.name = :name")
            .setInteger("id", visitType.getId())
            .setString("uuid", visitType.getUuid())
            .setString("name", visitType.getName());
    int matchingMDSRecords = ((Number) query.uniqueResult()).intValue();
    return matchingMDSRecords == 0;
  }

  @Override
  public Integer getNextVisitTypeId() throws DAOException {
    Integer maxId =
        (Integer)
            this.sessionFactory
                .getCurrentSession()
                .createSQLQuery("select max(visit_type_id) from visit_type ")
                .uniqueResult();
    return ++maxId;
  }

  @Override
  public VisitType updateVisitType(VisitType visitType, Integer nextId) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update visit_type set visit_type_id =:nextId where visit_type_id = :currentId")
        .setInteger("nextId", nextId)
        .setInteger("currentId", visitType.getVisitTypeId())
        .executeUpdate();
    clearAndFlushSession();

    final Criteria searchCriteria =
        this.sessionFactory.getCurrentSession().createCriteria(VisitType.class, "visitType");
    searchCriteria.add(Restrictions.eq("visitTypeId", nextId));
    return (VisitType) searchCriteria.uniqueResult();
  }

  @Override
  public VisitType updateVisitType(VisitType visitType, String newUuid) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery("update visit_type set uuid =:uuid where visit_type_id = :currentId")
        .setString("uuid", newUuid)
        .setInteger("currentId", visitType.getVisitTypeId())
        .executeUpdate();
    clearAndFlushSession();

    final Criteria searchCriteria =
        this.sessionFactory.getCurrentSession().createCriteria(VisitType.class, "visitType");
    searchCriteria.add(Restrictions.eq("visitTypeId", visitType.getVisitTypeId()));
    return (VisitType) searchCriteria.uniqueResult();
  }

  @Override
  public VisitType updateVisitType(VisitType visitType, Integer newId, String newUuid)
      throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update visit_type set visit_type_id = :id, uuid=:uuid where uuid = :currentUuid")
        .setInteger("id", newId)
        .setString("uuid", newUuid)
        .setString("currentUuid", visitType.getUuid())
        .executeUpdate();
    clearAndFlushSession();
    final Criteria searchCriteria =
        this.sessionFactory.getCurrentSession().createCriteria(VisitType.class, "visitType");
    searchCriteria.add(Restrictions.eq("visitTypeId", newId));
    return (VisitType) searchCriteria.uniqueResult();
  }

  @Override
  public VisitType overwriteVisitTypeDetails(VisitType toOverwrite, VisitType toOverwriteWith)
      throws DAOException {
    sessionFactory.getCurrentSession().evict(toOverwrite);
    sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update visit_type set visit_type_id=:id, name=:name, description=:description,"
                + "date_created=:dateCreated, retired=:retired, retire_reason=:retireReason, uuid=:uuid where visit_type_id=:currentId")
        .setInteger("id", toOverwriteWith.getVisitTypeId())
        .setString("name", toOverwriteWith.getName())
        .setString("description", toOverwriteWith.getDescription())
        .setInteger("currentId", toOverwrite.getVisitTypeId())
        .setDate("dateCreated", toOverwriteWith.getDateCreated())
        .setBoolean("retired", toOverwriteWith.isRetired())
        .setString("retireReason", toOverwriteWith.getRetireReason())
        .setString("uuid", toOverwriteWith.getUuid())
        .executeUpdate();
    clearAndFlushSession();

    final Criteria searchCriteria =
        this.sessionFactory.getCurrentSession().createCriteria(VisitType.class, "visitType");
    searchCriteria.add(Restrictions.eq("visitTypeId", toOverwriteWith.getVisitTypeId()));
    return (VisitType) searchCriteria.uniqueResult();
  }

  @Override
  public void updateVisit(Visit visit, Integer visitTypeId) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery("update visit set visit_type_id = :id where uuid = :uuid")
        .setInteger("id", visitTypeId)
        .setString("uuid", visit.getUuid())
        .executeUpdate();
    clearAndFlushSession();
  }

  @Override
  public void insertVisitType(VisitType visitType) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "insert into visit_type(visit_type_id, name, description, creator, date_created, retired, retire_reason, uuid) "
                + "VALUES(:visitTypeId, :name, :description, :creator, :dateCreated, :retired, :retireReason, :uuid)")
        .setInteger("visitTypeId", visitType.getVisitTypeId())
        .setString("name", visitType.getName())
        .setString("description", visitType.getDescription())
        .setInteger("creator", Context.getAuthenticatedUser().getUserId())
        .setDate("dateCreated", visitType.getDateCreated())
        .setBoolean("retired", visitType.isRetired())
        .setString("retireReason", visitType.getRetireReason())
        .setString("uuid", visitType.getUuid())
        .executeUpdate();
    clearAndFlushSession();
  }

  private void clearAndFlushSession() {
    Context.clearSession();
    Context.flushSession();
  }

  private Criteria visitsByVisitTypeCriteria(VisitType visitType) {
    Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Visit.class);
    criteria.add(Restrictions.eq("visitType", visitType));
    return criteria;
  }
}
