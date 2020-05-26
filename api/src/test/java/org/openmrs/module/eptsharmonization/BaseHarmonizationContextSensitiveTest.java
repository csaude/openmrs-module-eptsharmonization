package org.openmrs.module.eptsharmonization;

import java.sql.Connection;
import java.sql.SQLException;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import org.junit.Before;
import org.junit.Ignore;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;

/** @uthor Willa Mhawila<a.mhawila@gmail.com> on 5/28/20. */
@Ignore
public class BaseHarmonizationContextSensitiveTest extends BaseModuleContextSensitiveTest {
  private static final String LIQUIBASE_FILE = "src/main/resources/liquibase.xml";

  public BaseHarmonizationContextSensitiveTest() throws Exception {
    skipBaseSetup();
  }

  @Before
  public void initializeInMemoryDatabase() throws Exception {
    System.out.println("InitializeInMemoryDatabase");
    if (!Context.isSessionOpen()) {
      Context.openSession();
    }
    Connection connection = getConnection();
    super.initializeInMemoryDatabase();
    connection.commit();
    authenticate();

    Liquibase liquibase = null;
    try {
      Database database =
          DatabaseFactory.getInstance()
              .findCorrectDatabaseImplementation(new JdbcConnection(connection));
      liquibase = new Liquibase(LIQUIBASE_FILE, new FileSystemResourceAccessor(), database);
      liquibase.update(null);
      connection.commit();
    } catch (LiquibaseException e) {
      e.printStackTrace();
    } finally {
      if (connection != null) {
        try {
          connection.rollback();
          connection.close();
        } catch (SQLException e) {
          // nothing to do
        }
      }
    }

    Context.clearSession();
  }
}
