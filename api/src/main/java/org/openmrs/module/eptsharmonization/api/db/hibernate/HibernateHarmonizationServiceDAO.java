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
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.EncounterType;
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
    Context.clearSession();
    Context.flushSession();
    return this.findMDSEncounterTypes();
  }

  @Override
  public List<EncounterType> findAllProductionServerEncounterTypes() throws DAOException {
    // TODO: I had to do this to prevent cached data
    Context.clearSession();
    Context.flushSession();
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
}
