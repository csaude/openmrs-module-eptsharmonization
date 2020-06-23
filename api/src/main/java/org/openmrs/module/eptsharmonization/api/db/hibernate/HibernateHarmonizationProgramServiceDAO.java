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
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.ProgramWorkflowDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationProgramServiceDAO;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/** It is a default implementation of {@link HarmonizationProgramServiceDAO}. */
@Repository("eptsharmonization.hibernateHarmonizationProgramServiceDAO")
public class HibernateHarmonizationProgramServiceDAO implements HarmonizationProgramServiceDAO {

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

  @Override
  public List<Program> findAllMetadataServerPrograms() throws DAOException {
    this.harmonizationServiceDAO.evictCache();
    return this.findMDSPrograms();
  }

  @Override
  public List<Program> findAllProductionServerPrograms() throws DAOException {
    // TODO: I had to do this to prevent cached data
    this.harmonizationServiceDAO.evictCache();
    return this.programWorkflowDAO.getAllPrograms(true);
  }

  @SuppressWarnings("unchecked")
  public List<PatientProgram> findPatientProgramsByProgramId(Integer programId) {
    // TODO: I had to do this to prevent cached data
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select p.* from patient_program p where p.program_id=" + programId)
            .addEntity(PatientProgram.class);

    return query.list();
  }

  @SuppressWarnings("unchecked")
  public List<ProgramWorkflow> findProgramWorkflowsByProgramId(Integer programId) {
    // TODO: I had to do this to prevent cached data
    this.harmonizationServiceDAO.evictCache();
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select p.* from program_workflow p where p.program_id=" + programId)
            .addEntity(ProgramWorkflow.class);

    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Program> findPDSProgramsNotExistsInMDServer() throws DAOException {
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                "select program.* from program "
                    + "   where NOT EXISTS (select * from _program "
                    + "        where _program.program_id = program.program_id "
                    + "         and _program.uuid = program.uuid)")
            .addEntity(Program.class);
    return query.list();
  }

  public Program getProgramById(Integer programId) {
    this.harmonizationServiceDAO.evictCache();
    return this.programWorkflowDAO.getProgram(programId);
  }

  public boolean isSwappable(Program program) {
    return (boolean)
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                String.format(
                    "select swappable from program where program_id = %s ", program.getId()))
            .uniqueResult();
  }

  private Integer getNextProgramId() {
    Integer maxId =
        (Integer)
            this.sessionFactory
                .getCurrentSession()
                .createSQLQuery("select max(program_id) from program ")
                .uniqueResult();
    return ++maxId;
  }

  private Integer getProgramConceptId(Program program) {
    return (Integer)
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(
                String.format(
                    "select concept_id from _program where program_id = %s ", program.getId()))
            .uniqueResult();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Program> findAllSwappable() throws DAOException {
    return this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(String.format("select program.* from program where swappable = true "))
        .addEntity(Program.class)
        .list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Program> findAllNotSwappable() throws DAOException {
    return this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(String.format("select program.* from program where swappable = false "))
        .addEntity(Program.class)
        .list();
  }

  @Override
  public Program updateProgram(Integer nextId, Program program, boolean swappable) {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "update program set program_id =%s, swappable =%s where program_id =%s ",
                nextId, swappable, program.getId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();

    final Criteria searchCriteria =
        this.sessionFactory.getCurrentSession().createCriteria(Program.class, "program");
    searchCriteria.add(Restrictions.eq("programId", nextId));
    return (Program) searchCriteria.uniqueResult();
  }

  @Override
  public Program updateToNextAvailableId(Program program) throws DAOException {

    Integer nextAvailableProgramId = this.getNextProgramId();
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "update program set program_id =%s, swappable = true where program_id =%s ",
                nextAvailableProgramId, program.getId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();

    final Criteria searchCriteria =
        this.sessionFactory.getCurrentSession().createCriteria(Program.class, "program");
    searchCriteria.add(Restrictions.eq("programId", nextAvailableProgramId));
    return (Program) searchCriteria.uniqueResult();
  }

  @Override
  public void saveNotSwappableProgram(Program program) throws DAOException {
    String insert =
        String.format(
            "insert into program (program_id, concept_id, creator, date_created, retired, name, description, uuid, outcomes_concept_id, swappable) "
                + " values (%s, %s, %s, '%s', %s, '%s', '%s', '%s', %s, %s)",
            program.getId(),
            getProgramConceptId(program),
            Context.getAuthenticatedUser().getId(),
            program.getDateCreated(),
            program.getRetired(),
            program.getName(),
            program.getDescription(),
            program.getUuid(),
            program.getOutcomesConcept() == null
                ? program.getOutcomesConcept()
                : program.getOutcomesConcept().getId(),
            false);

    Query query = sessionFactory.getCurrentSession().createSQLQuery(insert);
    query.executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @Override
  public void updatePatientProgram(PatientProgram patientProgram, Integer programId)
      throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "update patient_program set program_id =%s where patient_program_id =%s ",
                programId, patientProgram.getId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @Override
  public void updateProgramWorkflow(ProgramWorkflow programWorkflow, Integer programId)
      throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format(
                "update program_workflow set program_id =%s where program_workflow_id =%s ",
                programId, programWorkflow.getId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @SuppressWarnings("unchecked")
  private List<Program> findMDSPrograms() {
    final Query query =
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery("select p.* from _program p")
            .addEntity(Program.class);
    return query.list();
  }

  @Override
  public void deleteProgram(Program program) throws DAOException {
    this.sessionFactory
        .getCurrentSession()
        .createSQLQuery(
            String.format("delete from program where program_id =%s ", program.getProgramId()))
        .executeUpdate();
    this.sessionFactory.getCurrentSession().flush();
  }

  @Override
  public Program findMDSPProgramByUuid(String uuid) throws DAOException {
    return (Program)
        this.sessionFactory
            .getCurrentSession()
            .createSQLQuery(String.format("select * from _program where uuid=:uuidValue "))
            .addEntity(Program.class)
            .setString("uuidValue", uuid)
            .uniqueResult();
  }
}
