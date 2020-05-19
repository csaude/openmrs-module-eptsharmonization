/**
 * The contents of this file are subject to the OpenMRS Public License Version 1.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://license.openmrs.org
 *
 * <p>Software distributed under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
 * ANY KIND, either express or implied. See the License for the specific language governing rights
 * and limitations under the License.
 *
 * <p>Copyright (C) OpenMRS, LLC. All Rights Reserved.
 */
package org.openmrs.module.eptsharmonization.api.db.hibernate;

import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationPersonAttributeTypeServiceDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;

/** It is a default implementation of {@link HarmonizationServiceDAO}. */
public class HibernateHarmonizationPersonAttributeTypeServiceDAO
    implements HarmonizationPersonAttributeTypeServiceDAO {

  private SessionFactory sessionFactory;

  /** @param sessionFactory the sessionFactory to set */
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  /** @return the sessionFactory */
  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  @Override
  public List<PersonAttributeType> findAllMetadataServerPersonAttributeTypes() throws DAOException {
    // TODO: I had to do this to prevent cached data
    this.evictCache();
    return this.findMDSPersonAttributeType();
  }

  @Override
  public List<PersonAttributeType> findAllProductionServerPersonAttributeTypes()
      throws DAOException {
    // TODO: I had to do this to prevent cached data
    this.evictCache();
    return Context.getPersonService().getAllPersonAttributeTypes();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<PersonAttribute> findPersonAttributeByPersonAttributeTypeId(
      Integer personAttributeTypeId) {
    // TODO: I had to do this to prevent cached data
    this.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select p.* from person_attribute p where p.person_attribute_type_id="
                    + personAttributeTypeId)
            .addEntity(PersonAttribute.class);

    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<PersonAttributeType> findPDSPersonAttributeTypesNotExistsInMDServer()
      throws DAOException {
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select person_attribute_type.* from person_attribute_type "
                    + "  where NOT EXISTS (select * from _person_attribute_type "
                    + "     where _person_attribute_type.person_attribute_type_id = person_attribute_type.person_attribute_type_id "
                    + "     and _person_attribute_type.uuid = person_attribute_type.uuid) ")
            .addEntity(PersonAttributeType.class);
    return query.list();
  }

  @Override
  public boolean isSwappable(PersonAttributeType personAttributeType) throws DAOException {
    return (boolean)
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                String.format(
                    "select swappable from person_attribute_type where person_attribute_type_id = %s ",
                    personAttributeType.getId()))
            .uniqueResult();
  }

  @Override
  public Integer getNextPersonAttriTypeId() throws DAOException {
    Integer maxId =
        (Integer)
            this.sessionFactory
                .getCurrentSession()
                .createSQLQuery("select max(person_attribute_type_id) from person_attribute_type ")
                .uniqueResult();
    return ++maxId;
  }

  @Override
  public PersonAttributeType getPersonAttributeTypeById(Integer personAttributeTypeId)
      throws DAOException {
    this.evictCache();
    return Context.getPersonService().getPersonAttributeType(personAttributeTypeId);
  }

  @Override
  public PersonAttributeType updatePersonAttributeType(
      Integer nextId, PersonAttributeType personAttributeType, boolean swappable)
      throws DAOException {

    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "update person_attribute_type set person_attribute_type_id =%s, swappable =%s where person_attribute_type_id =%s ",
                nextId, swappable, personAttributeType.getId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();

    final Criteria searchCriteria =
        this.sessionFactory
            .getCurrentSession()
            .createCriteria(PersonAttributeType.class, "personAttributeType");
    searchCriteria.add(Restrictions.eq("personAttributeTypeId", nextId));
    return (PersonAttributeType) searchCriteria.uniqueResult();
  }

  @Override
  public void updatePersonAttribute(PersonAttribute personAttribute, Integer personAttTypeId)
      throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "update person_attribute set person_attribute_type_id =%s where person_attribute_id =%s ",
                personAttTypeId, personAttribute.getId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @Override
  public void saveNotSwappablePersonAttributeType(PersonAttributeType personAttributeType)
      throws DAOException {

    String editPrivileges =
        personAttributeType.getEditPrivilege() != null
            ? personAttributeType.getEditPrivilege().getName()
            : null;
    String insert =
        String.format(
            "insert into person_attribute_type (person_attribute_type_id, name, description, format,     "
                + " foreign_key, searchable, creator, date_created,retired,     "
                + "   edit_privilege, uuid, sort_weight, swappable)                    "
                + " values (%s, '%s', '%s', '%s', %s, %s, %s, '%s', %s,'%s', '%s', %s, %s)",
            personAttributeType.getId(),
            personAttributeType.getName(),
            personAttributeType.getDescription(),
            personAttributeType.getFormat(),
            personAttributeType.getForeignKey(),
            personAttributeType.getSearchable(),
            Context.getAuthenticatedUser().getId(),
            personAttributeType.getDateCreated(),
            personAttributeType.getRetired(),
            editPrivileges,
            personAttributeType.getUuid(),
            personAttributeType.getSortWeight(),
            false);
    Query query = sessionFactory.getCurrentSession().createSQLQuery(insert);
    query.executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @SuppressWarnings("unchecked")
  private List<PersonAttributeType> findMDSPersonAttributeType() {
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select p.* from _person_attribute_type p")
            .addEntity(PersonAttributeType.class);
    return query.list();
  }

  private void evictCache() {
    Context.clearSession();
    Context.flushSession();
  }
}
