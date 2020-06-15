package org.openmrs.module.eptsharmonization;

import org.openmrs.api.context.Context;

public class HarmonizationUtils {

  public static void onModuleActivator() {

    StringBuilder sb = new StringBuilder();
    sb.append(
        "update encounter_type set swappable = true                      "
            + "  where encounter_type_id in(  select a.* from(                  "
            + "   select encounter_type.encounter_type_id from encounter_type  "
            + "   where NOT EXISTS (select * from _encounter_type "
            + "  where _encounter_type.encounter_type_id = encounter_type.encounter_type_id    "
            + "  and _encounter_type.uuid = encounter_type.uuid)) as a)                            ");
    Context.getAdministrationService().executeSQL(sb.toString(), false);
  }
}
