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
package org.openmrs.module.eptsharmonization.extension.html;

import java.util.HashMap;
import java.util.Map;
import org.openmrs.module.Extension;
import org.openmrs.module.web.extension.AdministrationSectionExt;

/**
 * This class defines the links that will appear on the administration page under the
 * "basicmodule.title" heading. This extension is enabled by defining (uncommenting) it in the
 * /metadata/config.xml file.
 */
public class AdminList extends AdministrationSectionExt {

  /** @see org.openmrs.module.web.extension.AdministrationSectionExt#getMediaType() */
  public Extension.MEDIA_TYPE getMediaType() {
    return Extension.MEDIA_TYPE.html;
  }

  /** @see org.openmrs.module.web.extension.AdministrationSectionExt#getTitle() */
  public String getTitle() {
    return "eptsharmonization.title";
  }

  /** @see org.openmrs.module.web.extension.AdministrationSectionExt#getLinks() */
  public Map<String, String> getLinks() {
    Map<String, String> map = new HashMap<String, String>();
    map.put(
        "module/eptsharmonization/encounterType/harmonizeEncounterTypeList.form",
        "eptsharmonization.harmonize.encountertype");
    map.put(
        "module/eptsharmonization/personAttributeTypes/harmonizePersonAttributeTypesList.form",
        "eptsharmonization.harmonize.personattributetypes");
    map.put(
        "module/eptsharmonization/programs/harmonizeProgramsList.form",
        "eptsharmonization.harmonize.programs");
    map.put(
        "module/eptsharmonization/programWorkflows/harmonizeProgramWorkflowsList.form",
        "eptsharmonization.harmonize.programworkflows");
    map.put(
        "module/eptsharmonization/programWorkflowStates/harmonizeProgramWorkflowStatesList.form",
        "eptsharmonization.harmonize.programworkflowstates");
    map.put(
        "module/eptsharmonization/harmonizeVisitType.form",
        "eptsharmonization.visittype.harmonize");
    map.put(
        "module/eptsharmonization/harmonizeRelationshipType.form",
        "eptsharmonization.relationshiptype.harmonize");
    map.put(
        "module/eptsharmonization/harmonizeLocationAttributeType.form",
        "eptsharmonization.locationattributetype.harmonize");
    map.put(
        "module/eptsharmonization/harmonizeLocationTag.form",
        "eptsharmonization.locationtag.harmonize");
    map.put(
        "module/eptsharmonization/harmonizeConcept.form",
        "eptsharmonization.concept.harmonize.status");
    map.put(
        "module/eptsharmonization/patientIdentifierTypes/harmonizePatientIdentifierTypesList.form",
        "eptsharmonization.harmonize.patientidentifiertypes");
    map.put(
        "module/eptsharmonization/form/harmonizeFormList.form", "eptsharmonization.harmonize.form");
    return map;
  }
}
