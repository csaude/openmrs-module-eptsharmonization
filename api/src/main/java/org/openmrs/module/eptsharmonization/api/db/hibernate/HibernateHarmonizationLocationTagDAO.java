package org.openmrs.module.eptsharmonization.api.db.hibernate;

import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.hibernate.type.BooleanType;
import org.hibernate.type.DateType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationLocationTagDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/** @uthor Willa Mhawila<a.mhawila@gmail.com> on 5/26/20. */
@Repository(HibernateHarmonizationLocationTagDAO.BEAN_NAME)
public class HibernateHarmonizationLocationTagDAO implements HarmonizationLocationTagDAO {
  public static final String BEAN_NAME = "eptsharmonization.hibernateHarmonizationLocationTagDAO";
  private SessionFactory sessionFactory;

  /** @param sessionFactory the sessionFactory to set */
  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public List<LocationTag> findAllMDSLocationTags() throws DAOException {
    clearAndFlushSession();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select location_tag_id as id, name, description, date_created as dateCreated, retired, date_retired as dateRetired, "
                    + "retire_reason as retireReason, uuid from _location_tag")
            .addScalar("id", IntegerType.INSTANCE)
            .addScalar("name", StringType.INSTANCE)
            .addScalar("description", StringType.INSTANCE)
            .addScalar("dateCreated", DateType.INSTANCE)
            .addScalar("dateRetired", DateType.INSTANCE)
            .addScalar("retired", BooleanType.INSTANCE)
            .addScalar("retireReason", StringType.INSTANCE)
            .addScalar("uuid", StringType.INSTANCE)
            .setResultTransformer(Transformers.aliasToBean(LocationTag.class));
    List<LocationTag> locationTagList = query.list();
    clearAndFlushSession();
    return locationTagList;
  }

  @Override
  public List<Location> findLocationsByLocationTag(final LocationTag locationTag)
      throws DAOException {
    this.clearAndFlushSession();
    return locationsByLocationTagCriteria(locationTag).list();
  }

  @Override
  public Integer getCountOfLocationsByLocationTag(final LocationTag locationTag)
      throws DAOException {
    final Criteria criteria = locationsByLocationTagCriteria(locationTag);
    criteria.setProjection(Projections.rowCount());
    return ((Number) criteria.uniqueResult()).intValue();
  }

  @Override
  public List<LocationTag> findPDSLocationTagsNotExistsInMDServer() throws DAOException {
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select l.location_tag_id as id, l.name as name, l.description as description, l.date_created as dateCreated, "
                    + "l.retired as retired, l.date_retired as dateRetired, l.retire_reason as retireReason, l.uuid as uuid from location_tag l"
                    + "where NOT EXISTS (select * from _location_tag "
                    + "where _location_tag.location_tag_id = l.location_tag_id "
                    + "and _location_tag.uuid = l.uuid "
                    + "and _location_tag.name = l.name)")
            .addScalar("id", IntegerType.INSTANCE)
            .addScalar("name", StringType.INSTANCE)
            .addScalar("description", StringType.INSTANCE)
            .addScalar("dateCreated", DateType.INSTANCE)
            .addScalar("dateRetired", DateType.INSTANCE)
            .addScalar("retired", BooleanType.INSTANCE)
            .addScalar("retireReason", StringType.INSTANCE)
            .addScalar("uuid", StringType.INSTANCE)
            .setResultTransformer(Transformers.aliasToBean(LocationTag.class));
    return query.list();
  }

  @Override
  public boolean isSwappable(LocationTag locationTag) throws DAOException {
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select count(*) from _location_tag v where v.location_tag_id = :id and v.uuid = :uuid and v.name = :name")
            .setInteger("id", locationTag.getId())
            .setString("uuid", locationTag.getUuid())
            .setString("name", locationTag.getName());
    int matchingMDSRecords = ((Number) query.uniqueResult()).intValue();
    return matchingMDSRecords == 0;
  }

  @Override
  public LocationTag findMDSLocationTagByUuid(String uuid) throws DAOException {
    this.clearAndFlushSession();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select v.* from _location_tag v where v.uuid = :uuid ")
            .addEntity(LocationTag.class);
    query.setString("uuid", uuid);
    return (LocationTag) query.uniqueResult();
  }

  @Override
  public LocationTag findPDSLocationTagByUuid(String uuid) throws DAOException {
    this.clearAndFlushSession();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select v.* from location_tag v where v.uuid = :uuid ")
            .addEntity(LocationTag.class);
    query.setString("uuid", uuid);
    return (LocationTag) query.uniqueResult();
  }

  @Override
  public Integer getNextLocationTagId() throws DAOException {
    Integer maxId =
        (Integer)
            this.sessionFactory
                .getCurrentSession()
                .createSQLQuery("select max(location_tag_id) from location_tag ")
                .uniqueResult();
    return ++maxId;
  }

  @Override
  public void updateLocationTag(LocationTag locationTag, Integer nextId) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update location_tag set location_tag_id =:nextId where location_tag_id = :currentId")
        .setInteger("nextId", nextId)
        .setInteger("currentId", locationTag.getLocationTagId())
        .executeUpdate();
    clearAndFlushSession();
  }

  @Override
  public void updateLocationTag(LocationTag locationTag, String newUuid) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery("update location_tag set uuid =:uuid where location_tag_id = :currentId")
        .setString("uuid", newUuid)
        .setInteger("currentId", locationTag.getLocationTagId())
        .executeUpdate();
    clearAndFlushSession();
  }

  @Override
  public void updateLocationTag(LocationTag locationTag, Integer newId, String newUuid)
      throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update location_tag set location_tag_id = :id, uuid=:uuid where uuid = :currentUuid")
        .setInteger("id", newId)
        .setString("uuid", newUuid)
        .setString("currentUuid", locationTag.getUuid())
        .executeUpdate();
    clearAndFlushSession();
  }

  @Override
  public void overwriteLocationTagDetails(LocationTag toOverwrite, LocationTag toOverwriteWith)
      throws DAOException {
    sessionFactory.getCurrentSession().evict(toOverwrite);
    sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update location_tag set location_tag_id=:id, name=:name, description=:description,"
                + "date_created=:dateCreated, retired=:retired, retire_reason=:retireReason, uuid =:uuid where location_tag_id=:currentId")
        .setInteger("id", toOverwriteWith.getLocationTagId())
        .setString("name", toOverwriteWith.getName())
        .setString("description", toOverwriteWith.getDescription())
        .setInteger("currentId", toOverwrite.getLocationTagId())
        .setDate("dateCreated", toOverwriteWith.getDateCreated())
        .setBoolean("retired", toOverwriteWith.isRetired())
        .setString("retireReason", toOverwriteWith.getRetireReason())
        .setString("uuid", toOverwriteWith.getUuid())
        .executeUpdate();
    clearAndFlushSession();
  }

  @Override
  public void updateLocation(Location location, Integer oldLocationTagId, Integer newLocationTagId)
      throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update location_tag_map set location_tag_id = :newTagId where location_tag_id = :oldTagId and location_id=:locationId")
        .setInteger("newTagId", newLocationTagId)
        .setInteger("oldTagId", oldLocationTagId)
        .setInteger("locationId", location.getLocationId())
        .executeUpdate();
    clearAndFlushSession();
  }

  @Override
  public void insertLocationTag(LocationTag locationTag) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "insert into location_tag(location_tag_id, name, description, creator, date_created, retired, retire_reason, uuid) "
                + "VALUES(:locationTagId, :name, :description, :creator, :dateCreated, :retired, :retireReason, :uuid)")
        .setInteger("locationTagId", locationTag.getLocationTagId())
        .setString("name", locationTag.getName())
        .setString("description", locationTag.getDescription())
        .setInteger("creator", Context.getAuthenticatedUser().getUserId())
        .setDate("dateCreated", locationTag.getDateCreated())
        .setBoolean("retired", locationTag.isRetired())
        .setString("retireReason", locationTag.getRetireReason())
        .setString("uuid", locationTag.getUuid())
        .executeUpdate();
    clearAndFlushSession();
  }

  private void clearAndFlushSession() {
    Context.clearSession();
    Context.flushSession();
  }

  private Criteria locationsByLocationTagCriteria(LocationTag locationTag) {
    return sessionFactory
        .getCurrentSession()
        .createCriteria(Location.class, "location")
        .createAlias("tags", "t")
        .add(Restrictions.eq("t.uuid", locationTag.getUuid()))
        .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
  }
}
