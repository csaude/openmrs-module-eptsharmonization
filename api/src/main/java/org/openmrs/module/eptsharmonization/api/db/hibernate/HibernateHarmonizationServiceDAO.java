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
import java.sql.Statement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.api.context.Context;
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
  public void setEnableCheckConstraints() throws Exception {
    Connection connection = sessionFactory.getCurrentSession().connection();
    Statement stmt = connection.createStatement();
    stmt.addBatch("SET FOREIGN_KEY_CHECKS=1");
    stmt.executeBatch();
    Context.flushSession();
  }

  @Override
  public void setDisabledCheckConstraints() throws Exception {
    Connection connection = sessionFactory.getCurrentSession().connection();
    Statement stmt = connection.createStatement();
    stmt.addBatch("SET FOREIGN_KEY_CHECKS=0");
    stmt.executeBatch();
  }
}
