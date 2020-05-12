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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.dataexchange.DataImporter;

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

    StringBuilder sb = new StringBuilder();
    sb.append("ALTER TABLE `encounter_type` ADD COLUMN `swappable` boolean default false");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder();
    sb.append("ALTER TABLE `encounter_type` ADD COLUMN `swap_id` int");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder();
    sb.append("ALTER TABLE `encounter` ADD COLUMN `swap_id` int");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder();
    sb.append("ALTER TABLE `form` ADD COLUMN `swap_id` int");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    HarmonizationUtils.onActivator();
  }

  @Override
  public void stopped() {
    log.info("Shutting down Epts Harmonization Module");
    this.deletePreviousHarmonizationLoadedDDLAndLiquibase();
  }

  private void deletePreviousHarmonizationLoadedDDLAndLiquibase() {
    StringBuilder sb = new StringBuilder();
    sb.append("DROP TABLE IF EXISTS `_encounter_type`");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder();
    sb.append("DROP TABLE IF EXISTS `_person_attribute_type`");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder();
    sb.append("ALTER TABLE `encounter_type` DROP `swappable`");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder();
    sb.append("ALTER TABLE `encounter_type` DROP `swap_id`");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder();
    sb.append("ALTER TABLE `encounter` DROP `swap_id`");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder();
    sb.append("ALTER TABLE `form` DROP `swap_id`");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder();
    sb.append("delete from liquibasechangelog where ID ='20200402-1806';");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder();
    sb.append("delete from liquibasechangelog where ID ='20200423-0840';");
    Context.getAdministrationService().executeSQL(sb.toString(), false);
  }
}
