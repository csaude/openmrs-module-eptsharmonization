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
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationProgramWorkflowServiceDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/** It is a default implementation of {@link HarmonizationProgramWorkflowServiceDAO}. */
@Repository("eptsharmonization.hibernateHarmonizationProgramWorkflowServiceDAO")
public class HibernateHarmonizationProgramWorkflowServiceDAO
    implements HarmonizationProgramWorkflowServiceDAO {

  private SessionFactory sessionFactory;
  private HarmonizationServiceDAO harmonizationServiceDAO;

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Autowired
  public void setHarmonizationServiceDAO(HarmonizationServiceDAO harmonizationServiceDAO) {
    this.harmonizationServiceDAO = harmonizationServiceDAO;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<ProgramWorkflow> findAllMetadataServerProgramWorkflows() throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select p.* from _program_workflow p")
            .addEntity(ProgramWorkflow.class);
    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<ProgramWorkflow> findAllProductionServerProgramWorkflows() throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select * from program_workflow")
            .addEntity(ProgramWorkflow.class);
    return query.list();
  }

  @SuppressWarnings("unchecked")
  public List<ConceptStateConversion> findConceptStateConversionsByProgramWorkflowId(
      Integer programWorkflowId) {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select c.* from concept_state_conversion c where c.program_workflow_id=:programWorkflowId")
            .addEntity(ConceptStateConversion.class)
            .setInteger("programWorkflowId", programWorkflowId);

    return query.list();
  }

  @SuppressWarnings("unchecked")
  public List<ProgramWorkflowState> findProgramWorkflowStatesByProgramWorkflowId(
      Integer programWorkflowId) {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select p.* from program_workflow_state p where p.program_workflow_id=:programWorkflowId")
            .addEntity(ProgramWorkflowState.class)
            .setInteger("programWorkflowId", programWorkflowId);

    return query.list();
  }

  public ProgramWorkflow getProgramWorkflowById(Integer programWorkflowId) {
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select p.* from program_workflow p where p.program_workflow_id=:programWorkflowId")
            .addEntity(ProgramWorkflow.class)
            .setInteger("programWorkflowId", programWorkflowId);
    return (ProgramWorkflow) query.uniqueResult();
  }

  public boolean isSwappable(ProgramWorkflow programWorkflow) {
    return (boolean)
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select swappable from program_workflow where program_workflow_id = :programWorkflowId ")
            .setInteger("programWorkflowId", programWorkflow.getId())
            .uniqueResult();
  }

  private Integer getNextProgramWorkflowId() {
    Integer maxId =
        (Integer)
            this.sessionFactory
                .getCurrentSession()
                .createSQLQuery("select max(program_workflow_id) from program_workflow ")
                .uniqueResult();
    return ++maxId;
  }

  private Integer getProgramWorkflowProgramId(ProgramWorkflow programWorkflow) throws DAOException {
    return (Integer)
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select program_id from _program_workflow where program_workflow_id = :programWorkflowId ")
            .setInteger("programWorkflowId", programWorkflow.getId())
            .uniqueResult();
  }

  public Integer getProgramWorkflowConceptId(
      ProgramWorkflow programWorkflow, boolean isFromMetadata) throws DAOException {
    return isFromMetadata
        ? (Integer)
            this.sessionFactory
                .getCurrentSession()
                .createSQLQuery(
                    "select concept_id from _program_workflow where program_workflow_id = :programWorkflowId ")
                .setInteger("programWorkflowId", programWorkflow.getId())
                .uniqueResult()
        : Context.getProgramWorkflowService()
            .getWorkflowByUuid(programWorkflow.getUuid())
            .getConcept()
            .getId();
  }

  @Override
  public Program getProgramWorkflowProgram(ProgramWorkflow programWorkflow, boolean isFromMetadata)
      throws DAOException {
    final Integer programId =
        isFromMetadata
            ? getProgramWorkflowProgramId(programWorkflow)
            : Context.getProgramWorkflowService()
                .getWorkflowByUuid(programWorkflow.getUuid())
                .getProgram()
                .getId();
    return Context.getProgramWorkflowService().getProgram(programId);
  }

  @Override
  public String getProgramWorkflowConceptName(
      ProgramWorkflow programWorkflow, boolean isFromMetadata) throws DAOException {
    return (String)
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select c.name from concept_name c where c.concept_id=:conceptId and c.locale='pt' ")
            .setInteger("conceptId", getProgramWorkflowConceptId(programWorkflow, isFromMetadata))
            .list()
            .get(0);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<ProgramWorkflow> findAllSwappable() throws DAOException {
    return this.sessionFactory
        .getCurrentSession()
        .createSQLQuery("select program_workflow.* from program_workflow where swappable = true ")
        .addEntity(ProgramWorkflow.class)
        .list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<ProgramWorkflow> findAllNotSwappable() throws DAOException {
    return this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "select program_workflow.* from program_workflow where swappable = false "))
        .addEntity(ProgramWorkflow.class)
        .list();
  }

  @Override
  public ProgramWorkflow updateProgramWorkflow(
      Integer nextId, ProgramWorkflow programWorkflow, boolean swappable) {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update program_workflow set program_workflow_id =:newProgramWorkflowId, swappable =:swappable where program_workflow_id =:programWorkflowId ")
        .setInteger("newProgramWorkflowId", nextId)
        .setBoolean("swappable", swappable)
        .setInteger("programWorkflowId", programWorkflow.getId())
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();

    final Criteria searchCriteria =
        this.sessionFactory.getCurrentSession().createCriteria(ProgramWorkflow.class, "program");
    searchCriteria.add(Restrictions.eq("programWorkflowId", nextId));
    return (ProgramWorkflow) searchCriteria.uniqueResult();
  }

  @Override
  public ProgramWorkflow updateProgramWorkflow(ProgramWorkflow programWorkflow)
      throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update program_workflow set program_id =:programId, concept_id =:conceptId where program_workflow_id =:programWorkflowId ")
        .setInteger("programId", programWorkflow.getProgram().getId())
        .setInteger("conceptId", programWorkflow.getConcept().getId())
        .setInteger("programWorkflowId", programWorkflow.getId())
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();

    final Criteria searchCriteria =
        this.sessionFactory.getCurrentSession().createCriteria(ProgramWorkflow.class, "program");
    searchCriteria.add(Restrictions.eq("programWorkflowId", programWorkflow.getId()));
    return (ProgramWorkflow) searchCriteria.uniqueResult();
  }

  @Override
  public ProgramWorkflow updateToNextAvailableId(ProgramWorkflow programWorkflow)
      throws DAOException {
    Integer nextAvailableProgramWorkflowId = this.getNextProgramWorkflowId();
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update program_workflow set program_workflow_id =:newProgramWorkflowId, swappable = true where program_workflow_id =:programWorkflowId ")
        .setInteger("programWorkflowId", programWorkflow.getId())
        .setInteger("newProgramWorkflowId", nextAvailableProgramWorkflowId)
        .executeUpdate();

    final Criteria searchCriteria =
        this.sessionFactory
            .getCurrentSession()
            .createCriteria(ProgramWorkflow.class, "programWorkflow");
    searchCriteria.add(Restrictions.eq("programWorkflowId", nextAvailableProgramWorkflowId));
    return (ProgramWorkflow) searchCriteria.uniqueResult();
  }

  @Override
  public void saveNotSwappableProgramWorkflow(ProgramWorkflow programWorkflow) throws DAOException {
    String insert =
        "insert into program_workflow (program_workflow_id, program_id, concept_id, creator, date_created, retired, uuid, swappable) "
            + " values (:programWorkflowId, :programId, :conceptId, :creator, :dateCreated, :retired, :uuid, :swappable)";

    Query query = sessionFactory.getCurrentSession().createSQLQuery(insert);
    query
        .setInteger("programWorkflowId", programWorkflow.getId())
        .setInteger("programId", getProgramWorkflowProgramId(programWorkflow))
        .setInteger("conceptId", getProgramWorkflowConceptId(programWorkflow, true))
        .setInteger("creator", Context.getAuthenticatedUser().getId())
        .setDate("dateCreated", programWorkflow.getDateCreated())
        .setBoolean("retired", programWorkflow.getRetired())
        .setString("uuid", programWorkflow.getUuid())
        .setBoolean("swappable", false);
    query.executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @Override
  public void updateProgramWorkflowState(
      ProgramWorkflowState programWorkflowState, Integer programWorkflowId) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update program_workflow_state set program_workflow_id =:programWorkflowId where program_workflow_state_id =:programWorkflowStateId ")
        .setInteger("programWorkflowId", programWorkflowId)
        .setInteger("programWorkflowStateId", programWorkflowState.getId())
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @Override
  public void updateConceptStateConversion(
      ConceptStateConversion conceptStateConversion, Integer programWorkflowId)
      throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "update concept_state_conversion set program_workflow_id =:programWorkflowId where concept_state_conversion_id =:conceptStateConversionId ")
        .setInteger("programWorkflowId", programWorkflowId)
        .setInteger("conceptStateConversionId", conceptStateConversion.getId())
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @Override
  public void deleteProgramWorkflow(ProgramWorkflow programWorkflow) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            "delete from program_workflow where program_workflow_id =:programWorkflowId ")
        .setInteger("programWorkflowId", programWorkflow.getProgramWorkflowId())
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @Override
  public ProgramWorkflow findMDSPProgramWorkflowByUuid(String uuid) throws DAOException {
    return (ProgramWorkflow)
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select * from _program_workflow where uuid=:uuidValue ")
            .addEntity(ProgramWorkflow.class)
            .setString("uuidValue", uuid)
            .uniqueResult();
  }
}
