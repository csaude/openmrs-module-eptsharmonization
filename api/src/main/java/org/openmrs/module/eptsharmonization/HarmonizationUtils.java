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

    sb = new StringBuilder();
    sb.append(
        "update person_attribute_type set swappable = true                      "
            + "  where person_attribute_type_id in(  select a.* from(                  "
            + "   select person_attribute_type.person_attribute_type_id from person_attribute_type  "
            + "   where NOT EXISTS (select * from _person_attribute_type "
            + "  where _person_attribute_type.person_attribute_type_id = person_attribute_type.person_attribute_type_id    "
            + "  and _person_attribute_type.uuid = person_attribute_type.uuid)) as a)                            ");
    Context.getAdministrationService().executeSQL(sb.toString(), false);

    sb = new StringBuilder();
    sb.append(
        "update program set swappable = true                      "
            + "  where program_id in(  select a.* from(                  "
            + "   select program.program_id from program  "
            + "   where NOT EXISTS (select * from _program "
            + "  where _program.program_id = program.program_id    "
            + "  and _program.uuid = program.uuid)) as a)                            ");
    Context.getAdministrationService().executeSQL(sb.toString(), false);
  }
}
