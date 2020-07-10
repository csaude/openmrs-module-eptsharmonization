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
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.PatientDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationPatientIdentifierTypeServiceDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/** It is a default implementation of {@link HarmonizationPatientIdentifierTypeServiceDAO}. */
@Repository("eptsharmonization.hibernateHarmonizationPatientIdentifierTypeServiceDAO")
public class HibernateHarmonizationPatientIdentifierTypeServiceDAO
    implements HarmonizationPatientIdentifierTypeServiceDAO {

  private SessionFactory sessionFactory;
  private PatientDAO patientDAO;
  private HarmonizationServiceDAO harmonizationServiceDAO;

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Autowired
  public void setPatientDAO(PatientDAO patientDAO) {
    this.patientDAO = patientDAO;
  }

  @Autowired
  public void setHarmonizationServiceDAO(HarmonizationServiceDAO harmonizationServiceDAO) {
    this.harmonizationServiceDAO = harmonizationServiceDAO;
  }

  @Override
  public List<PatientIdentifierType> findAllMDSServerPatientIdentifierTypes() throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    return this.findMDSPatientIdentifierTypes();
  }

  @Override
  public List<PatientIdentifierType> findAllPDSServerPatientIdentifierTypes() throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    return this.patientDAO.getAllPatientIdentifierTypes(true);
  }

  @SuppressWarnings("unchecked")
  public List<PatientIdentifier> findPatientIdentifiersByPatientIdentifierTypeId(
      Integer patientIdentifierTypeId) {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select p.* from patient_identifier p where p.identifier_type="
                    + patientIdentifierTypeId)
            .addEntity(PatientIdentifier.class);

    return query.list();
  }

  public PatientIdentifierType getPatientIdentifierTypeById(Integer patientIdentifierTypeId) {
    this.harmonizationServiceDAO.evictCache();
    return this.patientDAO.getPatientIdentifierType(patientIdentifierTypeId);
  }

  public boolean isSwappable(PatientIdentifierType patientIdentifierType) {
    return (boolean)
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select swappable from patient_identifier_type where patient_identifier_type_id = :patientIdentifierTypeId ")
            .setInteger("patientIdentifierTypeId", patientIdentifierType.getId())
            .uniqueResult();
  }

  private Integer getNextPatientIdentifierTypeId() {
    Integer maxId =
        (Integer)
            this.sessionFactory
                .getCurrentSession()
                .createSQLQuery(
                    "select max(patient_identifier_type_id) from patient_identifier_type ")
                .uniqueResult();
    return ++maxId;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<PatientIdentifierType> findAllSwappable() throws DAOException {
    return this.sessionFactory
        .getCurrentSession()
        .createSQLQuery("select p.* from patient_identifier_type p where swappable = true ")
        .addEntity(PatientIdentifierType.class)
        .list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<PatientIdentifierType> findAllNotSwappable() throws DAOException {
    return this.sessionFactory
        .getCurrentSession()
        .createSQLQuery("select p.* from patient_identifier_type p where swappable = false ")
        .addEntity(PatientIdentifierType.class)
        .list();
  }

  @Override
  public PatientIdentifierType updatePatientIdentifierType(
      Integer nextId, PatientIdentifierType patientIdentifierType, boolean swappable) {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update patient_identifier_type set patient_identifier_type_id =:nextId, swappable =:swappable where patient_identifier_type_id =:patientIdentifierTypeId ")
        .setInteger("nextId", nextId)
        .setBoolean("swappable", swappable)
        .setInteger("patientIdentifierTypeId", patientIdentifierType.getId())
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();

    final Criteria searchCriteria =
        this.sessionFactory
            .getCurrentSession()
            .createCriteria(PatientIdentifierType.class, "patientIdentifierType");
    searchCriteria.add(Restrictions.eq("patientIdentifierTypeId", nextId));
    return (PatientIdentifierType) searchCriteria.uniqueResult();
  }

  @Override
  public PatientIdentifierType updateToNextAvailableId(PatientIdentifierType patientIdentifierType)
      throws DAOException {

    Integer nextAvailablePatientIdentifierTypeId = this.getNextPatientIdentifierTypeId();
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update patient_identifier_type set patient_identifier_type_id =:nextAvailablePatientIdentifierTypeId, swappable = true where patient_identifier_type_id =:patientIdentifierTypeId ")
        .setInteger("nextAvailablePatientIdentifierTypeId", nextAvailablePatientIdentifierTypeId)
        .setInteger("patientIdentifierTypeId", patientIdentifierType.getId())
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();

    final Criteria searchCriteria =
        this.sessionFactory
            .getCurrentSession()
            .createCriteria(PatientIdentifierType.class, "patientIdentifierType");
    searchCriteria.add(
        Restrictions.eq("patientIdentifierTypeId", nextAvailablePatientIdentifierTypeId));
    return (PatientIdentifierType) searchCriteria.uniqueResult();
  }

  @SuppressWarnings("deprecation")
  @Override
  public void saveNotSwappablePatientIdentifierType(PatientIdentifierType patientIdentifierType)
      throws DAOException {
    Query query =
        sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "insert into patient_identifier_type (patient_identifier_type_id, name, description, format, check_digit, creator, date_created, required, format_description, validator, retired, uuid, swappable) "
                    + " values (:patientIdentifierTypeId, :name, :description, :format, :checkDigit, :creator, :dateCreated, :required, :formatDescription, :validator, :retired, :uuid, :swappable)")
            .setInteger("patientIdentifierTypeId", patientIdentifierType.getId())
            .setString("name", patientIdentifierType.getName())
            .setString("description", patientIdentifierType.getDescription())
            .setString("format", patientIdentifierType.getFormat())
            .setBoolean("checkDigit", patientIdentifierType.getCheckDigit())
            .setInteger("creator", Context.getAuthenticatedUser().getId())
            .setDate("dateCreated", patientIdentifierType.getDateCreated())
            .setBoolean("required", patientIdentifierType.getRequired())
            .setString("formatDescription", patientIdentifierType.getFormatDescription())
            .setString("validator", patientIdentifierType.getValidator())
            .setBoolean("retired", patientIdentifierType.getRetired())
            .setString("uuid", patientIdentifierType.getUuid())
            .setBoolean("swappable", false);

    query.executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @Override
  public void updatePatientIdentifier(
      PatientIdentifier patientIdentifier, Integer patientIdentifierTypeId) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update patient_identifier set identifier_type =:patientIdentifierTypeId where patient_identifier_id =:patientIdentifierId")
        .setInteger("patientIdentifierTypeId", patientIdentifierTypeId)
        .setInteger("patientIdentifierId", patientIdentifier.getId())
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @SuppressWarnings("unchecked")
  private List<PatientIdentifierType> findMDSPatientIdentifierTypes() throws DAOException {
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select p.* from _patient_identifier_type p")
            .addEntity(PatientIdentifierType.class);
    return query.list();
  }

  public PatientIdentifierType findMDSPatientIdentifierTypeByUuid(String uuid) throws DAOException {
    return (PatientIdentifierType)
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select * from _patient_identifier_type where uuid=:uuidValue")
            .addEntity(PatientIdentifierType.class)
            .setString("uuidValue", uuid)
            .uniqueResult();
  }

  @Override
  public void deletePatientIdentifierType(PatientIdentifierType patientIdentifierType)
      throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "delete from patient_identifier_type where patient_identifier_type_id =:patientIdentifierTypeId ")
        .setInteger("patientIdentifierTypeId", patientIdentifierType.getPatientIdentifierTypeId())
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }
}
