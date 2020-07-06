package org.openmrs.module.eptsharmonization.api.db.hibernate;

import java.util.List;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.hibernate.type.BooleanType;
import org.hibernate.type.DateType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
import org.openmrs.Concept;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationConceptDao;
import org.openmrs.module.eptsharmonization.api.model.ConceptDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/** @uthor Willa Mhawila<a.mhawila@gmail.com> on 7/1/20. */
@Repository(HibernateHarmonizationConceptDao.BEAN_NAME)
public class HibernateHarmonizationConceptDao implements HarmonizationConceptDao {
  public static final String BEAN_NAME = "eptsharmonization.hibernateHarmonizationConceptDao";
  private SessionFactory sessionFactory;
  private ConceptService conceptService;

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Autowired
  public void setConceptService(ConceptService conceptService) {
    this.conceptService = conceptService;
  }

  @Override
  public List<ConceptDTO> findAllPDSConceptsNotInMDS() throws DAOException {
    clearAndFlushSession();
    final StringBuilder queryBuilder =
        new StringBuilder(
                "SELECT c.concept_id as conceptId, c.datatype_id as datatypeId, c.class_id as classId,")
            .append(
                "c.version as version, c.is_set as isSet, c.date_created as dateCreated, c.retired as retired, c.date_retired as dateRetired, ")
            .append(
                "c.retire_reason as retireReason, c.uuid FROM concept c WHERE NOT EXISTS (SELECT 1 FROM _concept c1 WHERE ")
            .append("c.concept_id = c1.concept_id AND ")
            .append("c.uuid = c1.uuid AND ")
            .append("c.class_id = c1.class_id AND ")
            .append("c.datatype_id = c1.datatype_id)");

    final Query query =
        sessionFactory
            .getCurrentSession()
            .createSQLQuery(queryBuilder.toString())
            .addScalar("conceptId", IntegerType.INSTANCE)
            .addScalar("datatypeId", IntegerType.INSTANCE)
            .addScalar("classId", IntegerType.INSTANCE)
            .addScalar("version", StringType.INSTANCE)
            .addScalar("isSet", BooleanType.INSTANCE)
            .addScalar("dateCreated", DateType.INSTANCE)
            .addScalar("retired", BooleanType.INSTANCE)
            .addScalar("dateRetired", DateType.INSTANCE)
            .addScalar("retireReason", StringType.INSTANCE)
            .addScalar("uuid", StringType.INSTANCE)
            .setResultTransformer(Transformers.aliasToBean(ConceptDTO.class));

    List<ConceptDTO> conceptDTOList = query.list();
    for (ConceptDTO conceptDTO : conceptDTOList) {
      // Get associated names
      final Concept concept = conceptService.getConceptByUuid(conceptDTO.getUuid());
      for (ConceptName name : concept.getNames()) {
        conceptDTO.addName(conceptDTO.new Name(name));
      }

      // Get descriptions;
      for (ConceptDescription description : concept.getDescriptions()) {
        conceptDTO.addDescription(conceptDTO.new Description(description));
      }
    }

    return conceptDTOList;
  }

  @Override
  public List<ConceptDTO> findAllMDSConceptsNotInPDS() throws DAOException {
    clearAndFlushSession();
    final StringBuilder queryBuilder =
        new StringBuilder(
                "SELECT c.concept_id as conceptId, c.datatype_id as datatypeId, c.class_id as classId,")
            .append(
                "c.version as version, c.is_set as isSet, c.date_created as dateCreated, c.retired as retired, c.date_retired as dateRetired, ")
            .append(
                "c.retire_reason as retireReason, c.uuid FROM _concept c WHERE NOT EXISTS (SELECT 1 FROM concept c1 WHERE ")
            .append("c.concept_id = c1.concept_id AND ")
            .append("c.uuid = c1.uuid AND ")
            .append("c.class_id = c1.class_id AND ")
            .append("c.datatype_id = c1.datatype_id)");

    final Query query =
        sessionFactory
            .getCurrentSession()
            .createSQLQuery(queryBuilder.toString())
            .addScalar("conceptId", IntegerType.INSTANCE)
            .addScalar("datatypeId", IntegerType.INSTANCE)
            .addScalar("classId", IntegerType.INSTANCE)
            .addScalar("version", StringType.INSTANCE)
            .addScalar("isSet", BooleanType.INSTANCE)
            .addScalar("dateCreated", DateType.INSTANCE)
            .addScalar("retired", BooleanType.INSTANCE)
            .addScalar("dateRetired", DateType.INSTANCE)
            .addScalar("retireReason", StringType.INSTANCE)
            .addScalar("uuid", StringType.INSTANCE)
            .setResultTransformer(Transformers.aliasToBean(ConceptDTO.class));
    return query.list();
  }

  private void clearAndFlushSession() {
    Context.clearSession();
    Context.flushSession();
  }
}
