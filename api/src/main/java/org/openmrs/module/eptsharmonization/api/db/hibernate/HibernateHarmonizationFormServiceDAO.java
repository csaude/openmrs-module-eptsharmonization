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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.FormResource;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.EncounterDAO;
import org.openmrs.api.db.FormDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationFormServiceDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.openmrs.module.eptsharmonization.api.model.FormFilter;
import org.openmrs.module.eptsharmonization.api.model.HtmlForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/** It is a default implementation of {@link HarmonizationFormServiceDAO}. */
@Repository("eptsharmonization.hibernateHarmonizationFormServiceDAO")
public class HibernateHarmonizationFormServiceDAO implements HarmonizationFormServiceDAO {

  private SessionFactory sessionFactory;
  private FormDAO formDAO;
  private HarmonizationServiceDAO harmonizationServiceDAO;
  private EncounterDAO encounterDAO;

  @Autowired
  public HibernateHarmonizationFormServiceDAO(
      SessionFactory sessionFactory,
      FormDAO formDAO,
      HarmonizationServiceDAO harmonizationServiceDAO,
      EncounterDAO encounterDAO) {
    this.sessionFactory = sessionFactory;
    this.formDAO = formDAO;
    this.harmonizationServiceDAO = harmonizationServiceDAO;
    this.encounterDAO = encounterDAO;
  }

  @Override
  public List<Form> findAllMetadataServerForms() throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    return this.findMDSForms();
  }

  @Override
  public List<Form> findAllProductionServerForms() throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    return this.setRelatedMetadataFromTableForm(this.formDAO.getAllForms(true));
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Form> findPDSFormsNotExistsInMDServer() throws DAOException {
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select form.* from form where NOT EXISTS (select * from _form "
                    + "        where _form.form_id = form.form_id and _form.uuid = form.uuid)")
            .addEntity(Form.class);
    return this.setRelatedMetadataFromTableForm(query.list());
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

  public Form findFormById(Integer formId) {
    this.harmonizationServiceDAO.evictCache();
    return this.formDAO.getForm(formId);
  }

  public Form findFormByUuid(String uuid) throws DAOException {
    return this.formDAO.getFormByUuid(uuid);
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
  public List<FormFilter> findFormFilterByForm(Form form) throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select f.form_filter_id, f.form_id from formfilter_form_filter f where f.form_id="
                    + form.getFormId());
    List<FormFilter> result = new ArrayList<>();
    List<Object[]> list = query.list();
    for (Object[] object : list) {
      result.add(new FormFilter((Integer) object[0], (Integer) object[1]));
    }
    return result;
  }

  @Override
  public HtmlForm findPDSHtmlFormByForm(Form form) throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    Connection connection = sessionFactory.getCurrentSession().connection();

    String sql =
        String.format(
            "select f.id, f.form_id, f.xml_data, f.uuid, f.retired, f.creator, f.date_created from htmlformentry_html_form f where f.form_id = %s",
            form.getId());
    HtmlForm htmlForm = null;
    try {
      PreparedStatement prepareStatement = null;
      ResultSet rs = null;

      prepareStatement = connection.prepareStatement(sql);
      rs = prepareStatement.executeQuery();
      while (rs.next()) {
        htmlForm =
            new HtmlForm(
                rs.getInt("id"),
                rs.getInt("form_id"),
                rs.getString("xml_data"),
                rs.getString("uuid"),
                rs.getBoolean("retired"),
                rs.getInt("creator"),
                new Date(rs.getDate("date_created").getTime()));
        htmlForm.setForm(form);
      }
      rs.close();
      prepareStatement.close();
      connection.close();
    } catch (SQLException e) {
      throw new RuntimeException(e.getCause());
    }
    return htmlForm;
  }

  private HtmlForm findPDSHtmlFormByUuid(String uuid) throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    Connection connection = sessionFactory.getCurrentSession().connection();

    String sql =
        String.format(
            "select f.id, f.form_id, f.xml_data, f.uuid, f.retired, f.creator, f.date_created from htmlformentry_html_form f where f.uuid = '%s' ",
            uuid);
    HtmlForm htmlForm = null;
    try {
      PreparedStatement prepareStatement = null;
      ResultSet rs = null;

      prepareStatement = connection.prepareStatement(sql);
      rs = prepareStatement.executeQuery();
      while (rs.next()) {
        htmlForm =
            new HtmlForm(
                rs.getInt("id"),
                rs.getInt("form_id"),
                rs.getString("xml_data"),
                rs.getString("uuid"),
                rs.getBoolean("retired"),
                rs.getInt("creator"),
                new Date(rs.getDate("date_created").getTime()));
      }
      rs.close();
      prepareStatement.close();
      connection.close();
    } catch (SQLException e) {
      throw new RuntimeException(e.getCause());
    }
    return htmlForm;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<FormResource> findFormResourcesByForm(Form form) throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select fr.* from form_resource fr where fr.form_id = " + form.getFormId())
            .addEntity(FormResource.class);
    return query.list();
  }

  @SuppressWarnings("unchecked")
  public List<Form> findFormsByEncounterTypeId(Integer encounterTypeId) {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select f.* from form f where f.encounter_type=" + encounterTypeId)
            .addEntity(Form.class);

    return this.setRelatedMetadataFromTableForm(query.list());
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
    return setRelatedMetadataFromTableMDSForm(query.list());
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Form> findMDSFormsWithoutEncountersReferencesInPDServer() {
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "SELECT * FROM _form WHERE NOT EXISTS (SELECT * FROM encounter_type WHERE encounter_type.encounter_type_id = _form.encounter_type )")
            .addEntity(Form.class);
    return setRelatedMetadataFromTableMDSForm(query.list());
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Form> findAllSwappable() throws DAOException {
    return this.setRelatedMetadataFromTableForm(
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(String.format("select form.* from form where swappable = true "))
            .addEntity(Form.class)
            .list());
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Form> findAllNotSwappable() throws DAOException {
    return this.setRelatedMetadataFromTableForm(
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(String.format("select form.* from form where swappable = false "))
            .addEntity(Form.class)
            .list());
  }

  @Override
  public List<Form> findDiferrencesByIDsHavingSameUuidMDS() throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select _form.* from _form where EXISTS (select * from form where _form.form_id <> form.form_id and _form.uuid = form.uuid) ")
            .addEntity(Form.class);
    return setRelatedMetadataFromTableMDSForm(query.list());
  }

  @Override
  public List<Form> findDiferrencesByNameHavingSameIdAndUuidMDS() throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select _form.* from _form where EXISTS (select * from form where _form.form_id = form.form_id and _form.uuid = form.uuid and _form.name <> form.name)")
            .addEntity(Form.class);
    return setRelatedMetadataFromTableMDSForm(query.list());
  }

  @Override
  public List<Form> findDiferrencesByIDsHavingSameUuidPDS() throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select form.* from form where EXISTS (select * from _form where _form.form_id <> form.form_id and _form.uuid = form.uuid)")
            .addEntity(Form.class);
    return setRelatedMetadataFromTableForm(query.list());
  }

  @Override
  public List<Form> findDiferrencesByNameHavingSameIdAndUuidPDS() throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select form.* from form where EXISTS (select * from _form where _form.form_id = form.form_id and _form.uuid = form.uuid and _form.name <> form.name)")
            .addEntity(Form.class);
    return setRelatedMetadataFromTableForm(query.list());
  }

  @Override
  public List<Form> findNotUsedPDSForms() {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "SELECT * FROM form WHERE NOT EXISTS (SELECT * FROM encounter WHERE encounter.form_id = form.form_id ) and swappable is true")
            .addEntity(Form.class);
    return setRelatedMetadataFromTableForm(query.list());
  }

  @Override
  public List<Form> findUsedPDSForms() {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "SELECT * FROM form WHERE EXISTS (SELECT * FROM encounter WHERE encounter.form_id = form.form_id )")
            .addEntity(Form.class);
    return setRelatedMetadataFromTableForm(query.list());
  }

  @Override
  public List<HtmlForm> findHtmlFormMDSWithDifferentFormAndEqualUuidFromPDS() {

    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "SELECT id, form_id, xml_data, uuid, retired, creator, date_created FROM _htmlformentry_html_form where EXISTS (select * from htmlformentry_html_form  "
                    + " where htmlformentry_html_form.form_id <> _htmlformentry_html_form.form_id and    "
                    + " htmlformentry_html_form.uuid = _htmlformentry_html_form.uuid) and _htmlformentry_html_form.form_id "
                    + " in (select htmlformentry_html_form.form_id from htmlformentry_html_form where htmlformentry_html_form.form_id = _htmlformentry_html_form.form_id )                   ");

    return this.convertToHtmlFormObjects(query.list());
  }

  @Override
  public List<HtmlForm> findHtmlFormPDSWithDifferentFormAndEqualUuidFromMDS() {
    this.harmonizationServiceDAO.evictCache();

    String sql =
        "SELECT id, form_id, xml_data, uuid, retired, creator, date_created FROM htmlformentry_html_form where EXISTS (select * from _htmlformentry_html_form          "
            + " where _htmlformentry_html_form.form_id <> htmlformentry_html_form.form_id and            "
            + " _htmlformentry_html_form.uuid = htmlformentry_html_form.uuid)                            ";
    Connection connection = sessionFactory.getCurrentSession().connection();
    List<HtmlForm> result = new ArrayList<>();
    try {
      PreparedStatement prepareStatement = null;
      ResultSet rs = null;

      prepareStatement = connection.prepareStatement(sql);
      rs = prepareStatement.executeQuery();
      while (rs.next()) {
        HtmlForm htmlForm =
            new HtmlForm(
                rs.getInt("id"),
                rs.getInt("form_id"),
                rs.getString("xml_data"),
                rs.getString("uuid"),
                rs.getBoolean("retired"),
                rs.getInt("creator"),
                new Date(rs.getDate("date_created").getTime()));
        Form form = this.formDAO.getForm(rs.getInt("form_id"));
        htmlForm.setForm(form);
        result.add(htmlForm);
      }
      rs.close();
      prepareStatement.close();
      connection.close();
    } catch (SQLException e) {
      throw new RuntimeException(e.getCause());
    }
    return result;
  }

  @Override
  public List<HtmlForm> findHtmlMDSNotPresentInPDS() {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "SELECT id, form_id, xml_data, uuid, retired, creator, date_created FROM _htmlformentry_html_form     "
                    + " where  not EXISTS (select * from htmlformentry_html_form                      "
                    + " where htmlformentry_html_form.form_id = _htmlformentry_html_form.form_id and htmlformentry_html_form.uuid = _htmlformentry_html_form.uuid) and "
                    + "EXISTS ( select * FROM form where form.form_id = _htmlformentry_html_form.form_id )  ");

    return this.convertToHtmlFormObjects(query.list());
  }

  @Override
  public Form updateForm(Form pdsForm, Form mdsForm, boolean swappable) throws DAOException {
    String updateSql =
        "update form set form_id = :formId, name = :name, encounter_type = :encounterType, swappable = :swappable, uuid = :uuid where form_id = :baseFormId";
    Query query = sessionFactory.getCurrentSession().createSQLQuery(updateSql);

    query.setInteger("formId", mdsForm.getFormId());
    query.setString("name", mdsForm.getName());
    query.setInteger("encounterType", mdsForm.getEncounterType().getId());
    query.setBoolean("swappable", swappable);
    query.setInteger("baseFormId", pdsForm.getId());
    query.setString("uuid", mdsForm.getUuid());
    query.executeUpdate();

    final Criteria searchCriteria =
        this.sessionFactory.getCurrentSession().createCriteria(Form.class, "form");
    searchCriteria.add(Restrictions.eq("formId", mdsForm.getFormId()));
    this.sessionFactory.getCurrentSession().flush();

    return (Form) searchCriteria.uniqueResult();
  }

  @Override
  public void saveNotSwappableForm(Form form) throws DAOException {
    this.harmonizationServiceDAO.evictCache();

    String insert =
        "insert into form (form_id, name, version, build, published, description, encounter_type, creator, date_created, retired, uuid, swappable) "
            + " values (:formId, :name, :version, :build, :published, :description, :encounterType, :creator, :dateCreated, :retired, :uuid,  :swappable)";
    if (form.getBuild() == null) {
      insert = insert.replaceAll(":build,", "");
      insert = insert.replaceAll("build,", "");
    }

    Query query = sessionFactory.getCurrentSession().createSQLQuery(insert);
    query.setInteger("formId", form.getId());
    query.setString("name", form.getName());
    query.setString("version", form.getVersion());
    query.setBoolean("published", form.getPublished());
    query.setString("description", form.getDescription());
    query.setInteger("encounterType", form.getEncounterType().getId());
    query.setInteger("creator", Context.getAuthenticatedUser().getId());
    query.setDate("dateCreated", Calendar.getInstance().getTime());
    query.setBoolean("retired", form.isRetired());
    query.setString("uuid", form.getUuid());
    query.setBoolean("swappable", false);
    if (form.getBuild() != null) {
      query.setInteger("build", form.getBuild());
    }
    query.executeUpdate();
    this.sessionFactory.getCurrentSession().flush();

    HtmlForm mdsHtmlForm = this.getMDSHtmlForm(form);
    if (mdsHtmlForm != null) {
      HtmlForm pdsHtmlForm = this.findPDSHtmlFormByForm(form);

      if (pdsHtmlForm == null) {
        this.createHtmlFormPDS(mdsHtmlForm);
      } else {
        this.updatePDSHtmlForm(pdsHtmlForm, form);
      }
    }
  }

  public void createHtmlFormPDS(HtmlForm htmlForm) throws DAOException {
    this.harmonizationServiceDAO.evictCache();

    String insert =
        "insert into htmlformentry_html_form (form_id, xml_data, creator, date_created, retired, uuid) "
            + " values (?, ?, ?, ?, ?, ?)";

    Connection connection = sessionFactory.getCurrentSession().connection();
    try {
      PreparedStatement prepareStatement = null;
      prepareStatement = connection.prepareStatement(insert);
      prepareStatement.setInt(1, htmlForm.getFormId());
      prepareStatement.setString(2, htmlForm.getXmlData());
      prepareStatement.setInt(3, htmlForm.getCreator());
      prepareStatement.setDate(4, new java.sql.Date(htmlForm.getCreationDate().getTime()));
      prepareStatement.setBoolean(5, htmlForm.getRetired());
      prepareStatement.setString(6, htmlForm.getUuid());
      prepareStatement.executeUpdate();
      prepareStatement.close();
      connection.close();
    } catch (SQLException e) {
      throw new RuntimeException(e.getCause());
    }
  }

  public void updateHtmlForm(HtmlForm pdsHtmlForm, HtmlForm mdsHtmlForm) throws DAOException {
    this.harmonizationServiceDAO.evictCache();

    String update =
        "update htmlformentry_html_form set form_id =:newFormId where form_id = :formId";

    Query query = sessionFactory.getCurrentSession().createSQLQuery(update);
    query.setInteger("formId", pdsHtmlForm.getFormId());
    query.setInteger("newFormId", mdsHtmlForm.getFormId());

    query.executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @Override
  public Form updateToNextAvailableId(Form form) throws DAOException {
    this.harmonizationServiceDAO.evictCache();
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

  @Override
  public void deleteRelatedPDSHtmlForm(Form form) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "delete from htmlformentry_html_form where form_id =%s ", form.getFormId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @Override
  public void deleteRelatedEncounter(Form form) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(String.format("delete from encounter where form_id =%s ", form.getFormId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @Override
  public void deleteRelatedFormFilter(Form form) throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    this.deleteRelatedFormFilterPropery(form);
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "delete from formfilter_form_filter where form_id =%s ", form.getFormId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @Override
  public void deleteRelatedFormResource(Form form) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format("delete from form_resource where form_id =%s ", form.getFormId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @Override
  public void deleteRelatedFormField(Form form) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format("delete from form_field where form_id =%s ", form.getFormId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @SuppressWarnings("unchecked")
  public List<Encounter> findEncontersByEncounterTypeId(Integer encounterTypeId) {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select e.* from encounter e where e.encounter_type=" + encounterTypeId)
            .addEntity(Encounter.class);

    return query.list();
  }

  public void updateEncounter(Encounter encounter, Form form) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "update encounter set form_id =%s where encounter_id =%s ",
                form.getFormId(), encounter.getId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  public void updateFormField(FormField formField, Form form) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "update form_field set form_id =%s where form_field_id =%s ",
                form.getFormId(), formField.getFormFieldId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  public void updateFormResource(FormResource formResource, Form form) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "update form_resource set form_id =%s where form_resource_id =%s ",
                form.getFormId(), formResource.getFormResourceId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  public void updateFormFilter(FormFilter formFilter, Form form) throws DAOException {

    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "update formfilter_form_filter set form_id =%s where form_filter_id =%s ",
                form.getFormId(), formFilter.getFormFilterId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  public void updatePDSHtmlForm(HtmlForm htmlForm, Form form) throws DAOException {

    HtmlForm pdsHtmlFormByUuid = this.findPDSHtmlFormByUuid(htmlForm.getUuid());

    boolean updateUUid = false;

    if ((pdsHtmlFormByUuid == null)
        || (pdsHtmlFormByUuid != null
            && (!htmlForm.getUuid().equals(pdsHtmlFormByUuid.getUuid()))
            && htmlForm.getFormId().equals(pdsHtmlFormByUuid.getFormId()))) {
      updateUUid = true;
    }

    if (updateUUid) {
      this.sessionFactory
          .getCurrentSession()
          .createSQLQuery(
              String.format(
                  "update htmlformentry_html_form set form_id =%s, uuid =%s where id =%s ",
                  form.getFormId(), htmlForm.getUuid(), htmlForm.getId()))
          .executeUpdate();

    } else {
      this.sessionFactory
          .getCurrentSession()
          .createSQLQuery(
              String.format(
                  "update htmlformentry_html_form set form_id =%s where id =%s ",
                  form.getFormId(), htmlForm.getId()))
          .executeUpdate();
    }

    this.sessionFactory.getCurrentSession().flush();
  }

  public Form setRelatedFormMetadataFromTablMDSForm(Form form) {
    return this.setRelatedMetadataFromTableMDSForm(Arrays.asList(form)).get(0);
  }

  public Form setRelatedFormMetadataFromTableForm(Form form) {
    return this.setRelatedMetadataFromTableForm(Arrays.asList(form)).get(0);
  }

  private HtmlForm getMDSHtmlForm(Form form) {
    this.harmonizationServiceDAO.evictCache();

    String sql =
        "select id, form_id, xml_data, uuid, retired, creator, date_created FROM _htmlformentry_html_form where form_id = :formId";
    Query query = sessionFactory.getCurrentSession().createSQLQuery(sql);
    query.setInteger("formId", form.getId());

    Object[] uniqueResult = (Object[]) query.uniqueResult();

    if (uniqueResult != null) {
      List<Object[]> lst = new ArrayList<>();
      lst.add(uniqueResult);
      return convertToHtmlFormObjects(lst).get(0);
    }
    return null;
  }

  private void deleteRelatedFormFilterPropery(Form form) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                " delete from formfilter_filter_property where form_filter_id in(      "
                    + "     select * from ( select fp.filter_property_id from formfilter_filter_property fp      "
                    + "     join formfilter_form_filter f on (f.form_filter_id = fp.filter_property_id) where f.form_id = %s) a) ",
                form.getFormId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  private Integer findRelatedEncounterTypeFromTableMDSForm(Form form) throws DAOException {
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                String.format(
                    "select _form.encounter_type from _form where _form.form_id = %s",
                    form.getFormId()));
    return (Integer) query.uniqueResult();
  }

  private Integer findRelatedEncounterTypeFromTableForm(Form form) throws DAOException {
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                String.format(
                    "select form.encounter_type from form where form.form_id = %s",
                    form.getFormId()));
    return (Integer) query.uniqueResult();
  }

  private List<Form> setRelatedMetadataFromTableMDSForm(List<Form> forms) {
    this.harmonizationServiceDAO.evictCache();
    List<Form> result = new ArrayList<>();
    for (Form form : forms) {
      Integer encounterTypeId = this.findRelatedEncounterTypeFromTableMDSForm(form);

      EncounterType relatedEncounterType = null;
      try {
        relatedEncounterType = this.encounterDAO.getEncounterType(encounterTypeId);
        if (relatedEncounterType == null) {
          relatedEncounterType = new EncounterType(encounterTypeId);
        }

      } catch (ObjectNotFoundException e) {

      }
      result.add(clone(form, relatedEncounterType));
    }
    return result;
  }

  private List<Form> setRelatedMetadataFromTableForm(List<Form> forms) {
    this.harmonizationServiceDAO.evictCache();
    List<Form> result = new ArrayList<>();

    for (Form form : forms) {
      Integer encounterTypeId = this.findRelatedEncounterTypeFromTableForm(form);

      EncounterType relatedEncounterType = null;
      try {
        relatedEncounterType = this.encounterDAO.getEncounterType(encounterTypeId);
        if (relatedEncounterType == null) {
          relatedEncounterType = new EncounterType(encounterTypeId);
        }

      } catch (ObjectNotFoundException e) {

      }
      result.add(clone(form, relatedEncounterType));
    }
    return result;
  }

  private Form clone(Form form, EncounterType encounterType) {
    Form f = new Form(form.getId());
    f.setName(form.getName());
    f.setDescription(form.getDescription());
    f.setUuid(form.getUuid());
    f.setVersion(form.getVersion());
    f.setPublished(form.getPublished());
    f.setBuild(form.getBuild());
    f.setCreator(form.getCreator());
    f.setDateCreated(form.getDateCreated());
    f.setChangedBy(form.getChangedBy());
    f.setDateChanged(form.getDateChanged());
    f.setRetired(form.isRetired());
    f.setDateRetired(form.getDateRetired());
    f.setRetiredReason(form.getRetiredReason());
    f.setEncounterType(encounterType);
    return f;
  }

  private List<HtmlForm> convertToHtmlFormObjects(List<Object[]> objects) {

    List<HtmlForm> result = new ArrayList<>();
    for (Object[] object : objects) {

      HtmlForm htmlForm =
          new HtmlForm(
              (Integer) object[0],
              (Integer) object[1],
              (String) object[2],
              (String) object[3],
              (Boolean) object[4],
              (Integer) object[5],
              (Date) object[6]);
      Form form = this.formDAO.getForm(htmlForm.getFormId());
      htmlForm.setForm(form);
      result.add(htmlForm);
    }
    return result;
  }
}
