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

import java.util.List;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.db.DAOException;

/** Database methods for {@link HarmonizationPersonAttributeTypeServiceDAO}. */
public interface HarmonizationPersonAttributeTypeServiceDAO {

  public List<PersonAttributeType> findAllMetadataServerPersonAttributeTypes() throws DAOException;

  public List<PersonAttributeType> findAllProductionServerPersonAttributeTypes()
      throws DAOException;

  public PersonAttributeType getPersonAttributeTypeById(Integer personAttributeType)
      throws DAOException;

  public boolean isSwappable(PersonAttributeType personAttributeType) throws DAOException;

  public Integer getNextPersonAttriTypeId() throws DAOException;

  public PersonAttributeType updatePersonAttributeType(
      Integer nextId, PersonAttributeType personAttributeType, boolean swappable)
      throws DAOException;

  public void updatePersonAttribute(PersonAttribute personAttribute, Integer personAttTypeId)
      throws DAOException;

  public void saveNotSwappablePersonAttributeType(PersonAttributeType personAttributeType)
      throws DAOException;

  public List<PersonAttribute> findPersonAttributeByPersonAttributeTypeId(
      Integer personAttributeTypeId);

  public List<PersonAttributeType> findPDSPersonAttributeTypesNotExistsInMDServer()
      throws DAOException;
}
