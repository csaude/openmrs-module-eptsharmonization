package org.openmrs.module.eptsharmonization.api.db.hibernate;

import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationLocationAttributeTypeDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/** @uthor Willa Mhawila<a.mhawila@gmail.com> on 5/26/20. */
@Repository(HibernateHarmonizationLocationAttributeTypeDAO.BEAN_NAME)
public class HibernateHarmonizationLocationAttributeTypeDAO
    implements HarmonizationLocationAttributeTypeDAO {
  public static final String BEAN_NAME =
      "eptsharmonization.hibernateHarmonizationLocationAttributeTypeDao";
  private SessionFactory sessionFactory;

  /** @param sessionFactory the sessionFactory to set */
  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public List<LocationAttributeType> findAllMDSLocationAttributeTypes() throws DAOException {
    clearAndFlushSession();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select * from _location_attribute_type")
            .addEntity(LocationAttributeType.class);
    List<LocationAttributeType> locationAttributeTypeList = query.list();
    clearAndFlushSession();
    return locationAttributeTypeList;
  }

  @Override
  public List<LocationAttribute> findLocationAttributesByLocationAttributeType(
      final LocationAttributeType locationAttributeType) throws DAOException {
    this.clearAndFlushSession();
    return locationAttributesByLocationAttributeTypeCriteria(locationAttributeType).list();
  }

  @Override
  public Integer getCountOfLocationAttributesByLocationAttributeType(
      final LocationAttributeType locationAttributeType) throws DAOException {
    final Criteria criteria =
        locationAttributesByLocationAttributeTypeCriteria(locationAttributeType);
    criteria.setProjection(Projections.rowCount());
    return ((Number) criteria.uniqueResult()).intValue();
  }

  @Override
  public List<LocationAttributeType> findPDSLocationAttributeTypesNotExistsInMDServer()
      throws DAOException {
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select * from location_attribute_type "
                    + "where NOT EXISTS (select * from _location_attribute_type lat"
                    + "where _location_attribute_type.location_attribute_type_id = location_attribute_type.location_attribute_type_id "
                    + "and lat.uuid = location_attribute_type.uuid "
                    + "and lat.name = location_attribute_type.name "
                    + "and lat.datatype = location_attribute_type.datatype "
                    + "and lat.datatype_config = location_attribute_type.datatype_config "
                    + "and lat.preferred_handler = location_attribute_type.preferred_handler "
                    + "and lat.handler_config = location_attribute_type.handler_config "
                    + "and lat.min_occurs = location_attribute_type.min_occurs "
                    + "and lat.max_occurs = location_attribute_type.max_occurs)")
            .addEntity(LocationAttributeType.class);
    return query.list();
  }

  @Override
  public boolean isSwappable(LocationAttributeType locationAttributeType) throws DAOException {
    StringBuilder queryBulder =
        new StringBuilder("select count(*) from _location_attribute_type v ")
            .append("where v.location_attribute_type_id = :id and ")
            .append("v.uuid = :uuid and v.name = :name and ")
            .append("v.datatype = :dataType and ")
            .append("v.datatype_config = :dataTypeConfig and ")
            .append("v.preferred_handler = :preferredHandler and ")
            .append("v.handler_config = :handlerConfig and ")
            .append("v.min_occurs = :minOccurs and ")
            .append("v.max_occurs = :maxOccurs");

    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(queryBulder.toString())
            .setInteger("id", locationAttributeType.getId())
            .setString("uuid", locationAttributeType.getUuid())
            .setString("name", locationAttributeType.getName())
            .setString("dataType", locationAttributeType.getDatatypeClassname())
            .setString("dataTypeConfig", locationAttributeType.getDatatypeConfig())
            .setString("preferredHandler", locationAttributeType.getPreferredHandlerClassname())
            .setString("handlerConfig", locationAttributeType.getHandlerConfig())
            .setString("handlerConfig", locationAttributeType.getHandlerConfig())
            .setInteger("minOccurs", locationAttributeType.getMinOccurs())
            .setInteger("maxOccurs", locationAttributeType.getMaxOccurs());
    int matchingMDSRecords = ((Number) query.uniqueResult()).intValue();
    return matchingMDSRecords == 0;
  }

  @Override
  public Integer getNextLocationAttributeTypeId() throws DAOException {
    Integer maxId =
        (Integer)
            this.sessionFactory
                .getCurrentSession()
                .createSQLQuery(
                    "select max(location_attribute_type_id) from location_attribute_type ")
                .uniqueResult();
    return ++maxId;
  }

  @Override
  public LocationAttributeType findMDSLocationAttributeTypeByUuid(String uuid) throws DAOException {
    clearAndFlushSession();
    this.clearAndFlushSession();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select v.* from _location_attribute_type v where v.uuid = :uuid ")
            .addEntity(LocationAttributeType.class);
    query.setString("uuid", uuid);
    return (LocationAttributeType) query.uniqueResult();
  }

  @Override
  public LocationAttributeType findPDSLocationAttributeTypeByUuid(String uuid) throws DAOException {
    clearAndFlushSession();
    this.clearAndFlushSession();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select v.* from location_attribute_type v where v.uuid = :uuid ")
            .addEntity(LocationAttributeType.class);
    query.setString("uuid", uuid);
    return (LocationAttributeType) query.uniqueResult();
  }

  @Override
  public LocationAttributeType updateLocationAttributeType(
      LocationAttributeType locationAttributeType, Integer nextId) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update location_attribute_type set location_attribute_type_id =:nextId where location_attribute_type_id = :currentId")
        .setInteger("nextId", nextId)
        .setInteger("currentId", locationAttributeType.getLocationAttributeTypeId())
        .executeUpdate();
    clearAndFlushSession();

    final Criteria searchCriteria =
        this.sessionFactory
            .getCurrentSession()
            .createCriteria(LocationAttributeType.class, "locationAttributeType");
    searchCriteria.add(Restrictions.eq("locationAttributeTypeId", nextId));
    return (LocationAttributeType) searchCriteria.uniqueResult();
  }

  @Override
  public LocationAttributeType updateLocationAttributeType(
      LocationAttributeType locationAttributeType, String newUuid) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update location_attribute_type set uuid =:uuid where location_attribute_type_id = :currentId")
        .setString("uuid", newUuid)
        .setInteger("currentId", locationAttributeType.getLocationAttributeTypeId())
        .executeUpdate();
    clearAndFlushSession();

    final Criteria searchCriteria =
        this.sessionFactory
            .getCurrentSession()
            .createCriteria(LocationAttributeType.class, "locationAttributeType");
    searchCriteria.add(
        Restrictions.eq(
            "locationAttributeTypeId", locationAttributeType.getLocationAttributeTypeId()));
    return (LocationAttributeType) searchCriteria.uniqueResult();
  }

  @Override
  public LocationAttributeType updateLocationAttributeType(
      LocationAttributeType locationAttributeType, Integer newId, String newUuid)
      throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update location_attribute_type set location_attribute_type_id = :id, uuid=:uuid where uuid = :currentUuid")
        .setInteger("id", newId)
        .setString("uuid", newUuid)
        .setString("currentUuid", locationAttributeType.getUuid())
        .executeUpdate();
    clearAndFlushSession();
    final Criteria searchCriteria =
        this.sessionFactory
            .getCurrentSession()
            .createCriteria(LocationAttributeType.class, "locationAttributeType");
    searchCriteria.add(Restrictions.eq("locationAttributeTypeId", newId));
    return (LocationAttributeType) searchCriteria.uniqueResult();
  }

  @Override
  public LocationAttributeType overwriteLocationAttributeTypeDetails(
      LocationAttributeType toOverwrite, LocationAttributeType toOverwriteWith)
      throws DAOException {
    sessionFactory.getCurrentSession().evict(toOverwrite);

    String update =
        " update location_attribute_type set location_attribute_type_id=:id, name=:name, description=:description, datatype=:dataType, "
            + "  datatype_config=:dataTypeConfig, preferred_handler=:preferredHandler, handler_config=:handlerConfig, "
            + "  min_occurs=:minOccurs, max_occurs=:maxOccurs, date_created=:dateCreated, retired=:retired, retire_reason=:retireReason, "
            + "  uuid =:uuid where location_attribute_type_id=:currentId";

    sessionFactory
        .getCurrentSession()
        .createSQLQuery(update)
        .setInteger("id", toOverwriteWith.getLocationAttributeTypeId())
        .setString("name", toOverwriteWith.getName())
        .setString("description", toOverwriteWith.getDescription())
        .setString("dataType", toOverwriteWith.getDatatypeClassname())
        .setString("dataTypeConfig", toOverwriteWith.getDatatypeConfig())
        .setString("preferredHandler", toOverwriteWith.getPreferredHandlerClassname())
        .setString("handlerConfig", toOverwriteWith.getHandlerConfig())
        .setInteger("minOccurs", toOverwriteWith.getMinOccurs())
        .setInteger("maxOccurs", toOverwriteWith.getMaxOccurs())
        .setInteger("currentId", toOverwrite.getLocationAttributeTypeId())
        .setDate("dateCreated", toOverwriteWith.getDateCreated())
        .setBoolean("retired", toOverwriteWith.isRetired())
        .setString("retireReason", toOverwriteWith.getRetireReason())
        .setString("uuid", toOverwriteWith.getUuid())
        .executeUpdate();
    clearAndFlushSession();

    final Criteria searchCriteria =
        this.sessionFactory
            .getCurrentSession()
            .createCriteria(LocationAttributeType.class, "locationAttributeType");
    searchCriteria.add(
        Restrictions.eq("locationAttributeTypeId", toOverwriteWith.getLocationAttributeTypeId()));
    return (LocationAttributeType) searchCriteria.uniqueResult();
  }

  @Override
  public void updateLocationAttribute(
      LocationAttribute locationAttribute, Integer locationAttributeTypeId) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery("update locationAttribute set attribute_type_id = :id where uuid = :uuid")
        .setInteger("id", locationAttributeTypeId)
        .setString("uuid", locationAttribute.getUuid())
        .executeUpdate();
    clearAndFlushSession();
  }

  @Override
  public void insertLocationAttributeType(LocationAttributeType locationAttributeType)
      throws DAOException {
    StringBuilder queryBuilder =
        new StringBuilder("INSERT INTO location_attribute_type(")
            .append(
                "location_attribute_type_id, name, description, datatype, datatype_config, preferred_handler, ")
            .append(
                "handler_config, min_occurs, max_occurs, creator, date_created, retired, retire_reason, uuid) ")
            .append(
                "VALUES(:locationAttributeTypeId, :name, :description, :dataType, :dataTypeConfig, :preferredHandler, ")
            .append(
                ":handlerConfig, :minOccurs, :maxOccurs, :creator, :dateCreated, :retired, :retireReason, :uuid)");

    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(queryBuilder.toString())
        .setInteger("locationAttributeTypeId", locationAttributeType.getLocationAttributeTypeId())
        .setString("name", locationAttributeType.getName())
        .setString("description", locationAttributeType.getDescription())
        .setString("dataType", locationAttributeType.getDatatypeClassname())
        .setString("dataTypeConfig", locationAttributeType.getDatatypeConfig())
        .setString("preferredHandler", locationAttributeType.getPreferredHandlerClassname())
        .setString("handlerConfig", locationAttributeType.getHandlerConfig())
        .setInteger("minOccurs", locationAttributeType.getMinOccurs())
        .setInteger("maxOccurs", locationAttributeType.getMaxOccurs())
        .setInteger("creator", Context.getAuthenticatedUser().getUserId())
        .setDate("dateCreated", locationAttributeType.getDateCreated())
        .setBoolean("retired", locationAttributeType.isRetired())
        .setString("retireReason", locationAttributeType.getRetireReason())
        .setString("uuid", locationAttributeType.getUuid())
        .executeUpdate();
    clearAndFlushSession();
  }

  @Override
  public void deleteLocationAttributeType(LocationAttributeType locationAttributeType)
      throws DAOException {
    clearAndFlushSession();
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "delete from location_attribute_type where location_attribute_type_id =%s ",
                locationAttributeType.getId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  private void clearAndFlushSession() {
    Context.clearSession();
    Context.flushSession();
  }

  private Criteria locationAttributesByLocationAttributeTypeCriteria(
      LocationAttributeType locationAttributeType) {
    Criteria criteria = sessionFactory.getCurrentSession().createCriteria(LocationAttribute.class);
    criteria.add(Restrictions.eq("attributeType", locationAttributeType));
    return criteria;
  }
}
