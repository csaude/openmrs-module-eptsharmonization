package org.openmrs.module.eptsharmonization;

import java.io.File;
import org.openmrs.util.OpenmrsUtil;

/** @uthor Willa Mhawila<a.mhawila@gmail.com> on 6/12/20. */
public class EptsHarmonizationConstants {
  public static final String MODULE_DATA_DIRECTORY =
      OpenmrsUtil.getApplicationDataDirectory().concat("/eptsharmonization").concat(File.separator);
}
