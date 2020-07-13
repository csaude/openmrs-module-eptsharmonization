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
package org.openmrs.module.eptsharmonization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.dataexchange.DataImporter;
import org.openmrs.util.DatabaseUpdater;

/**
 * This class contains the logic that is run every time this module is either started or shutdown
 */
public class EptsHarmonizationActivator extends BaseModuleActivator {

  private Log log = LogFactory.getLog(this.getClass());

  @Override
  public void contextRefreshed() {
    log.debug("EPTS  Harmonization Module refreshed");
  }

  @Override
  public void willRefreshContext() {
    log.debug("Refreshing Epts Harmonization Module");
  }

  @Override
  public void willStart() {
    log.debug("Creating Epts Harmnonization data directory");
    try {
      createLogFilesDirectory();
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
    log.debug("Starting Epts Harmonization Module");
  }

  @Override
  public void willStop() {
    log.debug("Stopping Epts Harmonization Module");
  }

  public void started() {
    log.info("Starting Epts Harmonization Module");
    this.installMetaData();
  }

  private void installMetaData() {
    DataImporter dataImporter = Context.getRegisteredComponents(DataImporter.class).get(0);

    log.info("Importing _encounter_type Metadata");
    dataImporter.importData("encounter-types.xml");
    log.info(" _encounter_type Metadata imported");

    log.info("Importing _person_attribute_type Metadata");
    dataImporter.importData("person-attribute-types.xml");
    log.info(" _person_attribute_type Metadata imported");

    log.info("Importing _program Metadata");
    dataImporter.importData("program.xml");
    log.info(" _program Metadata imported");

    log.info("Importing _visit_type metadata");
    dataImporter.importData("visit-types.xml");
    log.info(" _visit_type metadata imported");

    log.info("Importing _relationship_type metadata");
    dataImporter.importData("relationship-types.xml");
    log.info(" _relationship_type metadata imported");

    log.info("Importing _program_workflow Metadata");
    dataImporter.importData("program_workflow.xml");
    log.info(" _program_workflow Metadata imported");

    log.info("Importing _location_attribute_type metadata");
    dataImporter.importData("location-attribute-types.xml");
    log.info(" _location_attribute_type metadata imported");

    log.info("Importing _location_tag metadata");
    dataImporter.importData("location-tags.xml");
    log.info(" _location_tag metadata imported");

    log.info("Importing _program_workflow_state Metadata");
    dataImporter.importData("program-workflow-state.xml");
    log.info("_program_workflow_state Metadata imported");

    log.info("Importing _concept metadata");
    dataImporter.importData("concepts.xml");
    log.info("_concept metadata imported");

    log.info("Importing _patient_identifier_type Metadata");
    dataImporter.importData("patient-identifier-types.xml");
    log.info(" _patient_identifier_type Metadata imported");

    StringBuilder sb = new StringBuilder();
    sb.append("ALTER TABLE `encounter_type` ADD COLUMN `swappable` boolean default false");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb =
        new StringBuilder(
            "ALTER TABLE `person_attribute_type` ADD COLUMN `swappable` boolean default false");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder("ALTER TABLE `program` ADD COLUMN `swappable` boolean default false");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder("ALTER TABLE `form` ADD COLUMN `swappable` boolean default false");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb =
        new StringBuilder(
            "ALTER TABLE `htmlformentry_html_form` ADD COLUMN `swappable` boolean default false");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb =
        new StringBuilder(
            "ALTER TABLE `program_workflow` ADD COLUMN `swappable` boolean default false");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    try {
      log.info("Importing _form Metadata Server ");
      EptsHarmonizationFormLoader.loadForms();
    } catch (Exception e) {
      log.error("Loading _forms entries", e);
      throw new RuntimeException(e);
    }

    try {
      log.info("Importing _htmlformentry_html_form Metadata Server ");
      EptsHarmonizationFormLoader.loadHtmlForms();
    } catch (Exception e) {
      log.error("Loading _htmlformentry_html_form entries", e);
      throw new RuntimeException(e);
    }
    sb =
        new StringBuilder(
            "ALTER TABLE `program_workflow_state` ADD COLUMN `swappable` boolean default false");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb =
        new StringBuilder(
            "ALTER TABLE `patient_identifier_type` ADD COLUMN `swappable` boolean default false");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    HarmonizationUtils.onModuleActivator();
  }

  @Override
  public void stopped() {
    log.info("Shutting down Epts Harmonization Module");
    this.deletePreviousHarmonizationLoadedDDLAndLiquibase();
  }

  private void deletePreviousHarmonizationLoadedDDLAndLiquibase() {
    StringBuilder sb = new StringBuilder("DROP TABLE IF EXISTS `_encounter_type`");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder("DROP TABLE IF EXISTS `_person_attribute_type`");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder("DROP TABLE IF EXISTS `_program`");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder("DROP TABLE IF EXISTS `_program_workflow`");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder("DROP TABLE IF EXISTS `_visit_type`");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder("DROP TABLE IF EXISTS `_relationship_type`");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder("DROP TABLE IF EXISTS `_location_tag`");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder("DROP TABLE IF EXISTS `_concept`");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder("DROP TABLE IF EXISTS `_program`");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder("DROP TABLE IF EXISTS `_location_attribute_type`");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder("DROP TABLE IF EXISTS `_form`");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder("DROP TABLE IF EXISTS `_htmlformentry_html_form`");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder("DROP TABLE IF EXISTS `_program_workflow_state`");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder("DROP TABLE IF EXISTS `_patient_identifier_type`");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    if (columnExists("encounter_type", "swappable")) {
      sb = new StringBuilder("ALTER TABLE `encounter_type` DROP `swappable`");
      Context.getAdministrationService().executeSQL(sb.toString(), false);
    }

    if (columnExists("person_attribute_type", "swappable")) {
      sb = new StringBuilder("ALTER TABLE `person_attribute_type` DROP `swappable`");
      Context.getAdministrationService().executeSQL(sb.toString(), false);
    }

    if (columnExists("program", "swappable")) {
      sb = new StringBuilder("ALTER TABLE `program` DROP `swappable`");
      Context.getAdministrationService().executeSQL(sb.toString(), false);
    }

    if (columnExists("program_workflow", "swappable")) {
      sb = new StringBuilder("ALTER TABLE `program_workflow` DROP `swappable`");
      Context.getAdministrationService().executeSQL(sb.toString(), false);
    }

    if (columnExists("form", "swappable")) {
      sb = new StringBuilder("ALTER TABLE `form` DROP `swappable`");
      Context.getAdministrationService().executeSQL(sb.toString(), false);
    }

    if (columnExists("htmlformentry_html_form", "swappable")) {
      sb = new StringBuilder("ALTER TABLE `htmlformentry_html_form` DROP `swappable`");
      Context.getAdministrationService().executeSQL(sb.toString(), false);
    }

    sb = new StringBuilder("delete from liquibasechangelog where ID ='20200402-1806';");
    if (columnExists("program_workflow_state", "swappable")) {
      sb = new StringBuilder();
      sb.append("ALTER TABLE `program_workflow_state` DROP `swappable`");
      Context.getAdministrationService().executeSQL(sb.toString(), false);
    }

    if (columnExists("patient_identifier_type", "swappable")) {
      sb = new StringBuilder();
      sb.append("ALTER TABLE `patient_identifier_type` DROP `swappable`");
      Context.getAdministrationService().executeSQL(sb.toString(), false);
    }

    sb = new StringBuilder();
    sb.append("delete from liquibasechangelog where ID ='20200402-1806';");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder("delete from liquibasechangelog where ID ='20200423-0840';");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder("delete from liquibasechangelog where ID ='20200616-1620';");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb =
        new StringBuilder(
            "delete from liquibasechangelog where ID ='eptsharmonization_20200526-1507';");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb =
        new StringBuilder(
            "delete from liquibasechangelog where ID ='eptsharmonization_20200622-1547';");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder("delete from liquibasechangelog where ID ='20200624-1130';");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb =
        new StringBuilder(
            "delete from liquibasechangelog where ID ='eptsharmonization_20200624-1242';");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder("delete from liquibasechangelog where ID ='20200601-1000';");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder("delete from liquibasechangelog where ID ='20200601-1010';");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb =
        new StringBuilder(
            "delete from liquibasechangelog where ID ='eptsharmonization_20200626-1520';");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder();
    sb.append("delete from liquibasechangelog where ID ='20200706-0945';");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb =
        new StringBuilder(
            "delete from liquibasechangelog where ID ='eptsharmonization_20200701-1657';");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder();
    sb.append("delete from liquibasechangelog where ID ='20200708-0930';");
    Context.getAdministrationService().executeSQL(sb.toString(), false);
  }

  private void createLogFilesDirectory() throws IOException {
    Path dataDirectoryPath = Paths.get(EptsHarmonizationConstants.MODULE_DATA_DIRECTORY);

    if (!Files.exists(dataDirectoryPath)) {
      Files.createDirectories(dataDirectoryPath);
    }
  }

  private boolean columnExists(String table, String column) {
    try {
      return DatabaseUpdater.getConnection()
          .getMetaData()
          .getColumns(null, null, table, column)
          .next();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
