package org.openmrs.module.eptsharmonization;

import java.io.File;
import org.openmrs.util.OpenmrsUtil;

/** @uthor Willa Mhawila<a.mhawila@gmail.com> on 6/12/20. */
public class EptsHarmonizationConstants {
  public static final String MODULE_DATA_DIRECTORY =
      OpenmrsUtil.getApplicationDataDirectory().concat("/eptsharmonization").concat(File.separator);

  public static final String VISITS_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAPPING_GP_VALUE =
      "12:1, 26:2, 6:3, 9:3, 10:4, 13:7, 18:8, 23:9, 11:9, 19:10, 24:10, 29:10, 8:11, 5:12, 7:12, 1:12, 3:12, 27:13, 32:14, 33:14,35:10,47:10,21:15,28:3, 52:17, 53:17, 49:15, 50:16, 51:7, 48:15, 34:10, 31:10, 30:15, 25:10, 22:7, 20:13, 17:3, 14:10, 2:3";
}
