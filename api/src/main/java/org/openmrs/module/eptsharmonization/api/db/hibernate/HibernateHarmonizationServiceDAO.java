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
import org.hibernate.SessionFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsharmonization.api.db.HarmonizationServiceDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/** It is a default implementation of {@link HarmonizationServiceDAO}. */
@Repository("eptsharmonization.hibernateHarmonizationServiceDAO")
public class HibernateHarmonizationServiceDAO implements HarmonizationServiceDAO {

  private SessionFactory sessionFactory;

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
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

  @Override
  public void evictCache() {
    Context.clearSession();
    Context.flushSession();
  }
}
