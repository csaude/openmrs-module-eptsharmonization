package org.openmrs.module.eptsharmonization;

import java.io.File;
import org.openmrs.util.OpenmrsUtil;

/** @uthor Willa Mhawila<a.mhawila@gmail.com> on 6/12/20. */
public class EptsHarmonizationConstants {
  public static final String MODULE_DATA_DIRECTORY =
      OpenmrsUtil.getApplicationDataDirectory().concat("/eptsharmonization").concat(File.separator);

  public static final String VISITS_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAPPING_GP_PROPERTY =
      "visits.encounterTypeToVisitTypeMapping";

  public static final String VISITS_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAPPING_GP_VALUE =
      "12:1, 26:2, 6:3, 9:3, 10:4, 6:5, 9:5, 13:7, 18:8, 23:9, 11:9, 19:10, 24:10, 29:10, 8:11, 5:12, 7:12, 1:12, 3:12, 27:13, 32:14, 33:14, 49:15, 50:15, 51:7, 52:8,53:16";
}
