package org.openmrs.module.eptsharmonization;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
}
