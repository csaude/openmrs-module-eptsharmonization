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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;

/** It is a default implementation of {@link HarmonizationServiceDAO}. */
public class HibernateHarmonizationServiceDAO implements HarmonizationServiceDAO {
  protected final Log log = LogFactory.getLog(this.getClass());

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
  public List<EncounterType> findAllMetadataServerEncounterTypes() throws DAOException {
    // TODO: I had to do this to prevent cached data
    this.evictCache();
    return this.findMDSEncounterTypes();
  }

  @Override
  public List<EncounterType> findAllProductionServerEncounterTypes() throws DAOException {
    // TODO: I had to do this to prevent cached data
    this.evictCache();
    return Context.getEncounterService().getAllEncounterTypes();
  }

  @SuppressWarnings("unchecked")
  private List<EncounterType> findMDSEncounterTypes() {
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select e.* from _encounter_type e")
            .addEntity(EncounterType.class);
    return query.list();
  }

  @Override
  public List<PersonAttributeType> findAllMetadataServerPersonAttributeTypes() throws DAOException {
    return this.findMDSPersonAttributeType();
  }

  @Override
  public List<PersonAttributeType> findAllProductionServerPersonAttributeTypes()
      throws DAOException {
    return Context.getPersonService().getAllPersonAttributeTypes();
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

  @SuppressWarnings("unchecked")
  public List<Encounter> findEncontersByEncounterTypeId(Integer encounterTypeId) {
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select e.* from encounter e where e.encounter_type=" + encounterTypeId)
            .addEntity(Encounter.class);

    return query.list();
  }

  @SuppressWarnings("unchecked")
  public List<Form> findFormsByEncounterTypeId(Integer encounterTypeId) {
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select f.* from form f where f.encounter_type=" + encounterTypeId)
            .addEntity(Form.class);

    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<EncounterType> findPDSEncounterTypesNotExistsInMDServer() throws DAOException {
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select encounter_type.* from encounter_type "
                    + "   where NOT EXISTS (select * from _encounter_type "
                    + "        where _encounter_type.encounter_type_id = encounter_type.encounter_type_id "
                    + "         and _encounter_type.uuid = encounter_type.uuid)")
            .addEntity(EncounterType.class);
    return query.list();
  }

  public EncounterType getEncounterTypeById(Integer encounterTypeId) {
    this.evictCache();
    return Context.getEncounterService().getEncounterType(encounterTypeId);
  }

  public boolean isSwappable(EncounterType encounterType) {
    return (boolean)
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                String.format(
                    "select swappable from encounter_type where encounter_type_id = %s ",
                    encounterType.getId()))
            .uniqueResult();
  }

  public Integer getSwapId(EncounterType encounterType) {
    return (Integer)
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                String.format(
                    "select swap_id from encounter_type where encounter_type_id = %s ",
                    encounterType.getId()))
            .uniqueResult();
  }

  public Integer getNextEncounterTypeId() {
    Integer maxId =
        (Integer)
            this.sessionFactory
                .getCurrentSession()
                .createSQLQuery("select max(encounter_type_id) from encounter_type ")
                .uniqueResult();
    return ++maxId;
  }

  // private Map<Integer, EncounterType> toMapEncounterTypes(List<EncounterType>
  // encounterTypes) {
  // Map<Integer, EncounterType> map = new HashMap<>();
  // for (EncounterType encounterType : encounterTypes) {
  // map.put(encounterType.getId(), encounterType);
  // }
  // return map;
  // }

  private void evictCache() {
    Context.clearSession();
    Context.flushSession();
  }

  @Override
  public EncounterType updateEncounterType(Integer nextId, EncounterType encounterType) {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "update encounter_type set encounter_type_id =%s where encounter_type_id =%s ",
                nextId, encounterType.getId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();

    final Criteria searchCriteria =
        this.sessionFactory
            .getCurrentSession()
            .createCriteria(EncounterType.class, "encounterType");
    searchCriteria.add(Restrictions.eq("encounterTypeId", nextId));
    return (EncounterType) searchCriteria.uniqueResult();
  }

  @Override
  public void saveNotSwappableEncounterType(EncounterType encounterType) throws DAOException {

    sessionFactory.getCurrentSession().clear();
    String insert =
        String.format(
            "insert into encounter_type (encounter_type_id, name, description, creator, date_created, retired, uuid, swappable) "
                + " values (%s, '%s', '%s', %s, '%s', %s, '%s', %s)",
            encounterType.getId(),
            encounterType.getName(),
            encounterType.getDescription(),
            Context.getAuthenticatedUser().getId(),
            encounterType.getDateCreated(),
            encounterType.getRetired(),
            encounterType.getUuid(),
            false);

    Query query = sessionFactory.getCurrentSession().createSQLQuery(insert);
    query.executeUpdate();
  }
}
