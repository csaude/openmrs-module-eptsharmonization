package org.openmrs.module.eptsharmonization;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.EncounterType;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsharmonization.api.HarmonizationEncounterTypeService;
import org.openmrs.module.eptsharmonization.api.HarmonizationPersonAttributeTypeService;

public class HarmonizationUtils {

  protected final Log log = LogFactory.getLog(getClass());

  public static HarmonizationEncounterTypeService getHarmonizationEncounterTypeService() {
    return (HarmonizationEncounterTypeService)
        Context.getService(HarmonizationEncounterTypeService.class);
  }

  public static HarmonizationPersonAttributeTypeService
      getHarmonizationPersonAttributeTypeService() {
    return (HarmonizationPersonAttributeTypeService)
        Context.getService(HarmonizationPersonAttributeTypeService.class);
  }

  public static void onActivator() {

    List<EncounterType> swappableEncounterTypes =
        Context.getService(HarmonizationEncounterTypeService.class)
            .findPDSEncounterTypesNotExistsInMDServer();
    for (EncounterType item : swappableEncounterTypes) {
      StringBuilder sb = new StringBuilder();
      sb.append(
          String.format(
              "update encounter_type set swappable = true where encounter_type_id = %s",
              item.getEncounterTypeId()));
      Context.getAdministrationService().executeSQL(sb.toString(), false);
    }

    List<PersonAttributeType> swappablePersonAttTypes =
        Context.getService(HarmonizationPersonAttributeTypeService.class)
            .findPDSPersonAttributeTypesNotExistsInMDServer();
    for (PersonAttributeType item : swappablePersonAttTypes) {

      StringBuilder sb = new StringBuilder();
      sb.append(
          String.format(
              "update person_attribute_type set swappable = true where person_attribute_type_id = %s",
              item.getPersonAttributeTypeId()));
      Context.getAdministrationService().executeSQL(sb.toString(), false);
    }
  }
}
