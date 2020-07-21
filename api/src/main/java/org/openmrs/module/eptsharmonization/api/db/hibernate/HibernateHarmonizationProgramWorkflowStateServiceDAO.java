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
import org.openmrs.ConceptStateConversion;
import org.openmrs.PatientState;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.ProgramWorkflowDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationProgramWorkflowStateServiceDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/** It is a default implementation of {@link HarmonizationProgramWorkflowStateServiceDAO}. */
@Repository("eptsharmonization.hibernateHarmonizationProgramWorkflowStateServiceDAO")
public class HibernateHarmonizationProgramWorkflowStateServiceDAO
    implements HarmonizationProgramWorkflowStateServiceDAO {

  private SessionFactory sessionFactory;
  private ProgramWorkflowDAO programWorkflowDAO;
  private HarmonizationServiceDAO harmonizationServiceDAO;

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Autowired
  public void setProgramWorkflowDAO(ProgramWorkflowDAO programWorkflowDAO) {
    this.programWorkflowDAO = programWorkflowDAO;
  }

  @Autowired
  public void setHarmonizationServiceDAO(HarmonizationServiceDAO harmonizationServiceDAO) {
    this.harmonizationServiceDAO = harmonizationServiceDAO;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<ProgramWorkflowState> findAllMDSProgramWorkflowStates() throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select p.* from _program_workflow_state p")
            .addEntity(ProgramWorkflowState.class);
    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<ProgramWorkflowState> findAllPDSProgramWorkflowStates() throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select * from program_workflow_state")
            .addEntity(ProgramWorkflowState.class);
    return query.list();
  }

  @SuppressWarnings("unchecked")
  public List<ConceptStateConversion> findConceptStateConversionsByProgramWorkflowStateId(
      Integer programWorkflowStateId) {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select c.* from concept_state_conversion c where c.program_workflow_state_id=:programWorkflowStateId")
            .addEntity(ConceptStateConversion.class)
            .setInteger("programWorkflowStateId", programWorkflowStateId);

    return query.list();
  }

  @SuppressWarnings("unchecked")
  public List<PatientState> findPatientStatesByProgramWorkflowStateId(
      Integer programWorkflowStateId) {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select p.* from patient_state p where p.state=:programWorkflowStateId")
            .addEntity(PatientState.class)
            .setInteger("programWorkflowStateId", programWorkflowStateId);

    return query.list();
  }

  public ProgramWorkflowState getProgramWorkflowStateById(Integer programWorkflowStateId) {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select p.* from program_workflow_state p where p.program_workflow_state_id=:programWorkflowStateId")
            .addEntity(ProgramWorkflowState.class)
            .setInteger("programWorkflowStateId", programWorkflowStateId);
    return (ProgramWorkflowState) query.uniqueResult();
  }

  public boolean isSwappable(ProgramWorkflowState programWorkflowState) {
    return (boolean)
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select swappable from program_workflow_state where program_workflow_state_id = :programWorkflowStateId ")
            .setInteger("programWorkflowStateId", programWorkflowState.getId())
            .uniqueResult();
  }

  private Integer getNextProgramWorkflowStateId() {
    Integer maxId =
        (Integer)
            this.sessionFactory
                .getCurrentSession()
                .createSQLQuery(
                    "select max(program_workflow_state_id) from program_workflow_state ")
                .uniqueResult();
    return ++maxId;
  }

  private Integer getProgramWorkflowId(ProgramWorkflowState programWorkflowState)
      throws DAOException {
    return (Integer)
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select program_workflow_id from _program_workflow_state where program_workflow_state_id = :programWorkflowStateId ")
            .setInteger("programWorkflowStateId", programWorkflowState.getId())
            .uniqueResult();
  }

  public Integer getConceptId(ProgramWorkflowState programWorkflowState, boolean isFromMetadata)
      throws DAOException {
    return isFromMetadata
        ? (Integer)
            this.sessionFactory
                .getCurrentSession()
                .createSQLQuery(
                    "select concept_id from _program_workflow_state where program_workflow_state_id = :programWorkflowStateId ")
                .setInteger("programWorkflowStateId", programWorkflowState.getId())
                .uniqueResult()
        : getProgramWorkflowStateByUuid(programWorkflowState.getUuid()).getConcept().getId();
  }

  @SuppressWarnings("deprecation")
  @Override
  public ProgramWorkflow getProgramWorkflow(
      ProgramWorkflowState programWorkflowState, boolean isFromMetadata) throws DAOException {
    final Integer programWorkflowId =
        isFromMetadata
            ? getProgramWorkflowId(programWorkflowState)
            : getProgramWorkflowStateByUuid(programWorkflowState.getUuid())
                .getProgramWorkflow()
                .getId();
    return Context.getProgramWorkflowService().getWorkflow(programWorkflowId);
  }

  @Override
  public String getConceptName(ProgramWorkflowState programWorkflowState, boolean isFromMetadata)
      throws DAOException {
    final Integer conceptId = getConceptId(programWorkflowState, isFromMetadata);

    return getConceptName(conceptId);
  }

  public String getConceptName(Integer conceptId) throws DAOException {
    String result =
        (String)
            this.sessionFactory
                .getCurrentSession()
                .createSQLQuery(
                    "select c.name from concept_name c where c.concept_id=:conceptId and c.locale='pt' and c.locale_preferred=1 ")
                .setInteger("conceptId", conceptId)
                .uniqueResult();
    if (result == null) {
      result =
          (String)
              this.sessionFactory
                  .getCurrentSession()
                  .createSQLQuery(
                      "select c.name from concept_name c where c.concept_id=:conceptId and c.locale='pt' ")
                  .setInteger("conceptId", conceptId)
                  .list()
                  .get(0);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<ProgramWorkflowState> findAllSwappable() throws DAOException {
    return this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "select program_workflow_state.* from program_workflow_state where swappable = true ")
        .addEntity(ProgramWorkflowState.class)
        .list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<ProgramWorkflowState> findAllNotSwappable() throws DAOException {
    return this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "select program_workflow_state.* from program_workflow_state where swappable = false "))
        .addEntity(ProgramWorkflowState.class)
        .list();
  }

  @Override
  public ProgramWorkflowState updateProgramWorkflowState(
      Integer nextId, ProgramWorkflowState programWorkflowState, boolean swappable) {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update program_workflow_state set program_workflow_state_id =:newProgramWorkflowStateId, swappable =:swappable where program_workflow_state_id =:programWorkflowStateId ")
        .setInteger("newProgramWorkflowStateId", nextId)
        .setBoolean("swappable", swappable)
        .setInteger("programWorkflowStateId", programWorkflowState.getId())
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();

    final Criteria searchCriteria =
        this.sessionFactory
            .getCurrentSession()
            .createCriteria(ProgramWorkflowState.class, "program");
    searchCriteria.add(Restrictions.eq("programWorkflowStateId", nextId));
    return (ProgramWorkflowState) searchCriteria.uniqueResult();
  }

  @Override
  public ProgramWorkflowState updateProgramWorkflowState(ProgramWorkflowState programWorkflowState)
      throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update program_workflow_state set program_workflow_id =:programWorkflowId, concept_id =:conceptId where program_workflow_state_id =:programWorkflowStateId ")
        .setInteger("programWorkflowId", programWorkflowState.getProgramWorkflow().getId())
        .setInteger("conceptId", programWorkflowState.getConcept().getId())
        .setInteger("programWorkflowStateId", programWorkflowState.getId())
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();

    final Criteria searchCriteria =
        this.sessionFactory
            .getCurrentSession()
            .createCriteria(ProgramWorkflowState.class, "programWorkflowState");
    searchCriteria.add(Restrictions.eq("programWorkflowStateId", programWorkflowState.getId()));
    return (ProgramWorkflowState) searchCriteria.uniqueResult();
  }

  @Override
  public ProgramWorkflowState updateToNextAvailableId(ProgramWorkflowState programWorkflowState)
      throws DAOException {
    Integer nextAvailableProgramWorkflowStateId = this.getNextProgramWorkflowStateId();
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update program_workflow_state set program_workflow_state_id =:newProgramWorkflowStateId, swappable = true where program_workflow_state_id =:programWorkflowStateId ")
        .setInteger("programWorkflowStateId", programWorkflowState.getId())
        .setInteger("newProgramWorkflowStateId", nextAvailableProgramWorkflowStateId)
        .executeUpdate();

    final Criteria searchCriteria =
        this.sessionFactory
            .getCurrentSession()
            .createCriteria(ProgramWorkflowState.class, "programWorkflowState");
    searchCriteria.add(
        Restrictions.eq("programWorkflowStateId", nextAvailableProgramWorkflowStateId));
    return (ProgramWorkflowState) searchCriteria.uniqueResult();
  }

  @Override
  public void saveNotSwappableProgramWorkflowState(ProgramWorkflowState programWorkflowState)
      throws DAOException {
    String insert =
        "insert into program_workflow_state (program_workflow_state_id, program_workflow_id, concept_id, initial, terminal, creator, date_created, retired, uuid, swappable) "
            + " values (:programWorkflowStateId, :programWorkflowId, :conceptId, :initial, :terminal, :creator, :dateCreated, :retired, :uuid, :swappable)";
    Query query = sessionFactory.getCurrentSession().createSQLQuery(insert);
    query
        .setInteger("programWorkflowStateId", programWorkflowState.getId())
        .setInteger("programWorkflowId", getProgramWorkflowId(programWorkflowState))
        .setInteger("conceptId", getConceptId(programWorkflowState, true))
        .setBoolean("initial", programWorkflowState.getInitial())
        .setBoolean("terminal", programWorkflowState.getTerminal())
        .setInteger("creator", Context.getAuthenticatedUser().getId())
        .setDate("dateCreated", programWorkflowState.getDateCreated())
        .setBoolean("retired", programWorkflowState.getRetired())
        .setString("uuid", programWorkflowState.getUuid())
        .setBoolean("swappable", false);
    query.executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @Override
  public void updatePatientState(PatientState patientState, Integer programWorkflowStateId)
      throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update patient_state set state =:programWorkflowStateId where state =:patientStateId ")
        .setInteger("programWorkflowStateId", programWorkflowStateId)
        .setInteger("patientStateId", patientState.getId())
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @Override
  public void updateConceptStateConversion(
      ConceptStateConversion conceptStateConversion, Integer programWorkflowStateId)
      throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update concept_state_conversion set program_workflow_state_id =:programWorkflowStateId where concept_state_conversion_id =:conceptStateConversionId ")
        .setInteger("programWorkflowStateId", programWorkflowStateId)
        .setInteger("conceptStateConversionId", conceptStateConversion.getId())
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @Override
  public void deleteProgramWorkflowState(ProgramWorkflowState programWorkflowState)
      throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "delete from program_workflow_state where program_workflow_state_id =:programWorkflowStateId ")
        .setInteger("programWorkflowStateId", programWorkflowState.getProgramWorkflowStateId())
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @Override
  public ProgramWorkflowState findMDSPProgramWorkflowStateByUuid(String uuid) throws DAOException {
    return (ProgramWorkflowState)
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select * from _program_workflow_state where uuid=:uuidValue ")
            .addEntity(ProgramWorkflowState.class)
            .setString("uuidValue", uuid)
            .uniqueResult();
  }

  @Override
  public ProgramWorkflowState getProgramWorkflowStateByUuid(String uuid) throws DAOException {
    return this.programWorkflowDAO.getStateByUuid(uuid);
  }
}
