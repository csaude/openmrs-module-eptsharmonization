package org.openmrs.module.eptsharmonization.api;

import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;

public interface HarmonizationService extends OpenmrsService {
  public boolean isAllMetadataHarmonized() throws APIException;
}
