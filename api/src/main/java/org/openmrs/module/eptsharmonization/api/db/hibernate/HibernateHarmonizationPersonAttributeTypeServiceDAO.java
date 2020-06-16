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
import org.openmrs.api.db.PersonDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationPersonAttributeTypeServiceDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/** It is a default implementation of {@link HarmonizationPersonAttributeTypeServiceDAO}. */
@Repository("eptsharmonization.hibernateHarmonizationPersonAttributeTypeServiceDAO")
public class HibernateHarmonizationPersonAttributeTypeServiceDAO
    implements HarmonizationPersonAttributeTypeServiceDAO {

  private SessionFactory sessionFactory;
  private PersonDAO personDAO;
  private HarmonizationServiceDAO harmonizationServiceDAO;

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Autowired
  public void setPersonDAO(PersonDAO personDAO) {
    this.personDAO = personDAO;
  }

  @Autowired
  public void setHarmonizationServiceDAO(HarmonizationServiceDAO harmonizationServiceDAO) {
    this.harmonizationServiceDAO = harmonizationServiceDAO;
  }

  @Override
  public List<PersonAttributeType> findAllMetadataServerPersonAttributeTypes() throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    return this.findMDSPersonAttributeTypes();
  }

  @Override
  public List<PersonAttributeType> findAllProductionServerPersonAttributeTypes()
      throws DAOException {
    // TODO: I had to do this to prevent cached data
    this.harmonizationServiceDAO.evictCache();
    return this.personDAO.getAllPersonAttributeTypes(true);
  }

  @SuppressWarnings("unchecked")
  public List<PersonAttribute> findPersonAttributesByPersonAttributeTypeId(
      Integer personAttributeTypeId) {
    // TODO: I had to do this to prevent cached data
    this.harmonizationServiceDAO.evictCache();
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
                "select p.* from person_attribute_type p "
                    + "   where NOT EXISTS (select * from _person_attribute_type p1 "
                    + "        where p1.person_attribute_type_id = p.person_attribute_type_id "
                    + "         and p1.uuid = p.uuid)")
            .addEntity(PersonAttributeType.class);
    return query.list();
  }

  public PersonAttributeType getPersonAttributeTypeById(Integer personAttributeTypeId) {
    this.harmonizationServiceDAO.evictCache();
    return this.personDAO.getPersonAttributeType(personAttributeTypeId);
  }

  public boolean isSwappable(PersonAttributeType personAttributeType) {
    return (boolean)
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                String.format(
                    "select swappable from person_attribute_type where person_attribute_type_id = %s ",
                    personAttributeType.getId()))
            .uniqueResult();
  }

  private Integer getNextPersonAttributeTypeId() {
    Integer maxId =
        (Integer)
            this.sessionFactory
                .getCurrentSession()
                .createSQLQuery("select max(person_attribute_type_id) from person_attribute_type ")
                .uniqueResult();
    return ++maxId;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<PersonAttributeType> findAllSwappable() throws DAOException {
    return this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format("select p.* from person_attribute_type p where swappable = true "))
        .addEntity(PersonAttributeType.class)
        .list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<PersonAttributeType> findAllNotSwappable() throws DAOException {
    return this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format("select p.* from person_attribute_type p where swappable = false "))
        .addEntity(PersonAttributeType.class)
        .list();
  }

  @Override
  public PersonAttributeType updatePersonAttributeType(
      Integer nextId, PersonAttributeType personAttributeType, boolean swappable) {
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
  public PersonAttributeType updateToNextAvailableId(PersonAttributeType personAttributeType)
      throws DAOException {

    Integer nextAvailablePersonAttributeTypeId = this.getNextPersonAttributeTypeId();
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "update person_attribute_type set person_attribute_type_id =%s, swappable = true where person_attribute_type_id =%s ",
                nextAvailablePersonAttributeTypeId, personAttributeType.getId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();

    final Criteria searchCriteria =
        this.sessionFactory
            .getCurrentSession()
            .createCriteria(PersonAttributeType.class, "personAttributeType");
    searchCriteria.add(
        Restrictions.eq("personAttributeTypeId", nextAvailablePersonAttributeTypeId));
    return (PersonAttributeType) searchCriteria.uniqueResult();
  }

  @Override
  public void saveNotSwappablePersonAttributeType(PersonAttributeType personAttributeType)
      throws DAOException {
    String insert =
        String.format(
            "insert into person_attribute_type (person_attribute_type_id, name, description, format, searchable, creator, date_created, retired, sort_weight, uuid, swappable) "
                + " values (%s, '%s', '%s', '%s', %s, %s, '%s', %s, %s, '%s', %s)",
            personAttributeType.getId(),
            personAttributeType.getName(),
            personAttributeType.getDescription(),
            personAttributeType.getFormat(),
            personAttributeType.getSearchable(),
            Context.getAuthenticatedUser().getId(),
            personAttributeType.getDateCreated(),
            personAttributeType.getRetired(),
            personAttributeType.getSortWeight(),
            personAttributeType.getUuid(),
            false);

    Query query = sessionFactory.getCurrentSession().createSQLQuery(insert);
    query.executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @Override
  public void updatePersonAttribute(PersonAttribute personAttribute, Integer personAttributeTypeId)
      throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "update person_attribute set person_attribute_type_id =%s where person_attribute_id =%s ",
                personAttributeTypeId, personAttribute.getId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @SuppressWarnings("unchecked")
  private List<PersonAttributeType> findMDSPersonAttributeTypes() {
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select p.* from _person_attribute_type p")
            .addEntity(PersonAttributeType.class);
    return query.list();
  }

  @Override
  public void deletePersonAttributeType(PersonAttributeType personAttributeType)
      throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "delete from person_attribute_type where person_attribute_type_id =%s ",
                personAttributeType.getPersonAttributeTypeId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }
}
