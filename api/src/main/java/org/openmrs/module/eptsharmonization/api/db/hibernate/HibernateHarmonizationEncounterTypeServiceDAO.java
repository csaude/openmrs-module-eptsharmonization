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

import java.util.Calendar;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.BooleanType;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.EncounterDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationEncounterTypeServiceDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/** It is a default implementation of {@link HarmonizationEncounterTypeServiceDAO}. */
@Repository("eptsharmonization.hibernateHarmonizationEncounterTypeServiceDAO")
public class HibernateHarmonizationEncounterTypeServiceDAO
    implements HarmonizationEncounterTypeServiceDAO {

  private SessionFactory sessionFactory;
  private EncounterDAO encounterDAO;
  private HarmonizationServiceDAO harmonizationServiceDAO;

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Autowired
  public void setEncounterDAO(EncounterDAO encounterDAO) {
    this.encounterDAO = encounterDAO;
  }

  @Autowired
  public void setHarmonizationServiceDAO(HarmonizationServiceDAO harmonizationServiceDAO) {
    this.harmonizationServiceDAO = harmonizationServiceDAO;
  }

  @Override
  public List<EncounterType> findAllMetadataServerEncounterTypes() throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    return this.findMDSEncounterTypes();
  }

  @Override
  public List<EncounterType> findAllProductionServerEncounterTypes() throws DAOException {
    // TODO: I had to do this to prevent cached data
    this.harmonizationServiceDAO.evictCache();
    return this.encounterDAO.getAllEncounterTypes(true);
  }

  @SuppressWarnings("unchecked")
  public List<Encounter> findEncontersByEncounterTypeId(Integer encounterTypeId) {
    // TODO: I had to do this to prevent cached data
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select e.* from encounter e where e.encounter_type=" + encounterTypeId)
            .addEntity(Encounter.class);

    return query.list();
  }

  @SuppressWarnings("unchecked")
  public List<Form> findFormsByEncounterTypeId(Integer encounterTypeId) {
    // TODO: I had to do this to prevent cached data
    this.harmonizationServiceDAO.evictCache();
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
    this.harmonizationServiceDAO.evictCache();
    return this.encounterDAO.getEncounterType(encounterTypeId);
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

  @Override
  public boolean isAllMedatadaHarmonized() {
    return (boolean)
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "  select if(count(*)> 0, false, true) as res from (  "
                    + "select _encounter_type.encounter_type_id from _encounter_type where not exists (  "
                    + "	select * from encounter_type where                                            "
                    + "	 encounter_type.encounter_type_id = _encounter_type.encounter_type_id         "
                    + "	 and encounter_type.uuid = _encounter_type.uuid                               "
                    + "	 and lower(trim(encounter_type.name)) = lower(trim(_encounter_type.name)))    "
                    + " union                                                                            "
                    + " select encounter_type.encounter_type_id from encounter_type where not exists (   "
                    + "	select * from _encounter_type where                                            "
                    + "	 _encounter_type.encounter_type_id = encounter_type.encounter_type_id          "
                    + "	 and _encounter_type.uuid = encounter_type.uuid                                "
                    + "	 and lower(trim( _encounter_type.name)) = lower(trim(encounter_type.name)))) a  ")
            .addScalar("res", new BooleanType())
            .uniqueResult();
  }

  private Integer getNextEncounterTypeId() {
    Integer maxId =
        (Integer)
            this.sessionFactory
                .getCurrentSession()
                .createSQLQuery("select max(encounter_type_id) from encounter_type ")
                .uniqueResult();
    return ++maxId;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<EncounterType> findAllSwappable() throws DAOException {
    return this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format("select  encounter_type.* from encounter_type where swappable = true "))
        .addEntity(EncounterType.class)
        .list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<EncounterType> findAllNotSwappable() throws DAOException {
    return this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format("select  encounter_type.* from encounter_type where swappable = false "))
        .addEntity(EncounterType.class)
        .list();
  }

  @Override
  public EncounterType updateEncounterType(
      Integer nextId, EncounterType encounterType, boolean swappable) {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "update encounter_type set encounter_type_id =%s, swappable =%s where encounter_type_id =%s ",
                nextId, swappable, encounterType.getId()))
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
  public EncounterType updateToNextAvailableId(EncounterType encounterType) throws DAOException {

    Integer nextAvailableEncounterTypeId = this.getNextEncounterTypeId();
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "update encounter_type set encounter_type_id =%s, swappable = true where encounter_type_id =%s ",
                nextAvailableEncounterTypeId, encounterType.getId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();

    final Criteria searchCriteria =
        this.sessionFactory
            .getCurrentSession()
            .createCriteria(EncounterType.class, "encounterType");
    searchCriteria.add(Restrictions.eq("encounterTypeId", nextAvailableEncounterTypeId));
    return (EncounterType) searchCriteria.uniqueResult();
  }

  @Override
  public void saveNotSwappableEncounterType(EncounterType encounterType) throws DAOException {
    String insert =
        "insert into encounter_type (encounter_type_id, name, description, creator, date_created, retired, uuid, swappable) "
            + " values (:encounterTypeId, :name, :description, :creator, :dateCreated, :retired, :uuid, :swappable)";

    Query query = sessionFactory.getCurrentSession().createSQLQuery(insert);

    query.setInteger("encounterTypeId", encounterType.getId());
    query.setString("name", encounterType.getName());
    query.setString("description", encounterType.getDescription());
    query.setInteger("creator", Context.getAuthenticatedUser().getId());
    query.setDate("dateCreated", Calendar.getInstance().getTime());
    query.setBoolean("retired", encounterType.isRetired());
    query.setString("uuid", encounterType.getUuid());
    query.setBoolean("swappable", false);
    query.executeUpdate();

    this.sessionFactory.getCurrentSession().flush();
  }

  @Override
  public void updateEncounter(Encounter encounter, Integer encounterTypeId) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "update encounter set encounter_type =%s where encounter_id =%s ",
                encounterTypeId, encounter.getId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @Override
  public void updateForm(Form form, Integer encounterTypeId) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "update form set encounter_type =%s where form_id =%s ",
                encounterTypeId, form.getId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
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
  public void deleteEncounterType(EncounterType encounterType) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "delete from encounter_type where encounter_type_id =%s ",
                encounterType.getEncounterTypeId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @Override
  public EncounterType getEncounterTypeByUuid(String uuid) throws DAOException {
    return this.encounterDAO.getEncounterTypeByUuid(uuid);
  }
}
