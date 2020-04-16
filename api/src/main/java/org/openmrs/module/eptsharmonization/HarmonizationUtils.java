package org.openmrs.module.eptsharmonization;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsharmonization.api.HarmonizationService;

public class HarmonizationUtils {

  protected final Log log = LogFactory.getLog(getClass());

  public static HarmonizationService getService() {
    return (HarmonizationService) Context.getService(HarmonizationService.class);
  }
}
