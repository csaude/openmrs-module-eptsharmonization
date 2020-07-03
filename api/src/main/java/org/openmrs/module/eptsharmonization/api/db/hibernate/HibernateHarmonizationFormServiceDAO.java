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
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.FormResource;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.FormDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationFormServiceDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/** It is a default implementation of {@link HarmonizationFormServiceDAO}. */
@Repository("eptsharmonization.hibernateHarmonizationFormServiceDAO")
public class HibernateHarmonizationFormServiceDAO implements HarmonizationFormServiceDAO {

  private SessionFactory sessionFactory;
  private FormDAO formDAO;
  private HarmonizationServiceDAO harmonizationServiceDAO;

  //	@Autowired
  //	public void setSessionFactory(SessionFactory sessionFactory) {
  //		this.sessionFactory = sessionFactory;
  //	}
  //
  //	@Autowired
  //	public void setFformDAO(FormDAO formDAO) {
  //		this.formDAO = formDAO;
  //	}
  //
  //	@Autowired
  //	public void setHarmonizationServiceDAO(HarmonizationServiceDAO harmonizationServiceDAO) {
  //		this.harmonizationServiceDAO = harmonizationServiceDAO;
  //	}

  @Autowired
  public HibernateHarmonizationFormServiceDAO(
      SessionFactory sessionFactory,
      FormDAO formDAO,
      HarmonizationServiceDAO harmonizationServiceDAO) {
    this.sessionFactory = sessionFactory;
    this.formDAO = formDAO;
    this.harmonizationServiceDAO = harmonizationServiceDAO;
  }

  @Override
  public List<Form> findAllMetadataServerForms() throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    return this.findMDSForms();
  }

  @Override
  public List<Form> findAllProductionServerForms() throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    return this.formDAO.getAllForms(true);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Form> findPDSFormsNotExistsInMDServer() throws DAOException {
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select form.* from form "
                    + "   where NOT EXISTS (select * from _form "
                    + "        where _form.form_id = form.form_id "
                    + "         and _form.uuid = form.uuid)")
            .addEntity(Form.class);
    return query.list();
  }

  @Override
  public boolean isSwappable(Form form) throws DAOException {
    return (boolean)
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                String.format("select swappable from form where form_id = %s ", form.getId()))
            .uniqueResult();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Encounter> findEncountersByForm(Form form) throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select e.* from encounter e where e.form_id=" + form.getFormId())
            .addEntity(Encounter.class);

    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<FormField> findFormFieldsByForm(Form form) throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select ff.* from form_field ff where ff.form_id=" + form.getFormId())
            .addEntity(FormField.class);

    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<FormResource> findFormResourcesByForm(Form form) throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select fr.* from form_resource fr where fr.form_id=" + form.getFormId())
            .addEntity(FormResource.class);
    return query.list();
  }

  @Override
  public Form updateForm(Integer nextId, Form form, boolean swappable) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "update form set form_id =%s, swappable =%s where form_id =%s ",
                nextId, swappable, form.getId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();

    final Criteria searchCriteria =
        this.sessionFactory.getCurrentSession().createCriteria(Form.class, "form");
    searchCriteria.add(Restrictions.eq("formId", nextId));
    return (Form) searchCriteria.uniqueResult();
  }

  @Override
  public void saveNotSwappableForm(Form form) throws DAOException {
    String insert =
        String.format(
            "insert into form (form_id, name, version, build, published, description, encounter_type, template, xslt, creator, date_created, retired, uuid, swappable) "
                + " values (%s, '%s', '%s', '%s',  %s, %s, '%s', %s, '%s', %s)",
            form.getId(),
            form.getName(),
            form.getVersion(),
            form.getBuild(),
            form.getPublished(),
            form.getDescription(),
            form.getEncounterType().getId(),
            form.getTemplate(),
            form.getXslt(),
            Context.getAuthenticatedUser().getId(),
            form.getDateCreated(),
            form.getRetired(),
            form.getUuid(),
            false);

    Query query = sessionFactory.getCurrentSession().createSQLQuery(insert);
    query.executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @Override
  public Form updateToNextAvailableId(Form form) throws DAOException {
    Integer nextAvailableFormId = this.getNextFormId();
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "update form set form_id =%s, swappable = true where form_id =%s ",
                nextAvailableFormId, form.getId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();

    final Criteria searchCriteria =
        this.sessionFactory.getCurrentSession().createCriteria(Form.class, "form");
    searchCriteria.add(Restrictions.eq("formId", nextAvailableFormId));
    return (Form) searchCriteria.uniqueResult();
  }

  @Override
  public void deleteForm(Form form) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(String.format("delete from form where form_id =%s ", form.getFormId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
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

  private Integer getNextFormId() {
    Integer maxId =
        (Integer)
            this.sessionFactory
                .getCurrentSession()
                .createSQLQuery("select max(form_id) from form ")
                .uniqueResult();
    return ++maxId;
  }

  @SuppressWarnings("unchecked")
  private List<Form> findMDSForms() {
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select f.* from _form f")
            .addEntity(Form.class);
    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Form> findAllSwappable() throws DAOException {
    return this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(String.format("select form.* from form where swappable = true "))
        .addEntity(Form.class)
        .list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Form> findAllNotSwappable() throws DAOException {
    return this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(String.format("select form.* from form where swappable = false "))
        .addEntity(Form.class)
        .list();
  }
}
