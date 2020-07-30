package org.openmrs.module.eptsharmonization.api;

import org.openmrs.api.APIException;

public interface HarmonizationService {
  public boolean isHarmonized() throws APIException;
}
