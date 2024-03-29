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
package org.openmrs.module.eptsharmonization.api.db;

import java.sql.SQLException;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.eptsharmonization.api.HarmonizationEncounterTypeService;

/** Database methods for {@link HarmonizationEncounterTypeService}. */
public interface HarmonizationServiceDAO {

  public void setEnableCheckConstraints() throws DAOException, SQLException;

  public void setDisabledCheckConstraints() throws DAOException, SQLException;

  public void evictCache();
}
