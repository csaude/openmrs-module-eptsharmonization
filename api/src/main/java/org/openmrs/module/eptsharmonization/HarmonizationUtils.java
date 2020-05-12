package org.openmrs.module.eptsharmonization;

import java.util.Collection;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.TransformerUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.EncounterType;
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

  @SuppressWarnings("unchecked")
  public static void onActivator() {
    HarmonizationEncounterTypeService encounterTypeService =
        Context.getService(HarmonizationEncounterTypeService.class);
    List<EncounterType> swappableEncounterTypes =
        encounterTypeService.findPDSEncounterTypesNotExistsInMDServer();
    Collection<Integer> ids =
        CollectionUtils.collect(
            swappableEncounterTypes, TransformerUtils.invokerTransformer("getId"));
    int countSwapp = 1000;
    for (Integer id : ids) {
      StringBuilder sb = new StringBuilder();
      sb.append(
          String.format(
              "update encounter_type set swappable = true,  swap_id = %s where encounter_type_id = %s",
              countSwapp++, id));
      Context.getAdministrationService().executeSQL(sb.toString(), false);
    }
  }
}
