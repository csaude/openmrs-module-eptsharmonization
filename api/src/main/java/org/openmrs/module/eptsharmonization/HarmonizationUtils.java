package org.openmrs.module.eptsharmonization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.RelationshipType;
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

    sb = new StringBuilder();
    sb.append(
        "update program_workflow set swappable = true                      "
            + "  where program_workflow_id in(  select a.* from(                  "
            + "   select program_workflow.program_workflow_id from program_workflow  "
            + "   where NOT EXISTS (select * from _program_workflow "
            + "  where _program_workflow.program_workflow_id = program_workflow.program_workflow_id    "
            + "  and _program_workflow.uuid = program_workflow.uuid)) as a)           ");
    Context.getAdministrationService().executeSQL(sb.toString(), false);
  }

  public static <T extends OpenmrsMetadata>
      List<T> findElementsInList1WithSameUuidsButDifferentIdsFromList2(
          final List<T> list1, final List<T> list2) {
    List<T> results = new ArrayList<>();
    for (T ele1 : list1) {
      for (T ele2 : list2) {
        if (ele1.getId().compareTo(ele2.getId()) != 0
            && ele1.getUuid().contentEquals(ele2.getUuid())) {
          results.add(ele1);
        }
      }
    }
    return results;
  }

  public static <T extends OpenmrsMetadata>
      Map<String, List<T>> findElementsWithDifferentNamesSameUuidsAndIds(
          final List<T> list1, final List<T> list2) {
    final Map<String, List<T>> results = new HashMap<>();
    for (T ele1 : list1) {
      for (T ele2 : list2) {
        if (ele1.getId().equals(ele2.getId())
            && ele1.getUuid().contentEquals(ele2.getUuid())
            && !ele1.getName().equalsIgnoreCase(ele2.getName())) {
          results.put(ele1.getUuid(), Arrays.asList(ele1, ele2));
        }
      }
    }
    return results;
  }

  public static Map<String, List<RelationshipType>>
      findRelationshipTypeWithDifferentTypesAndSameUUIDAndID(
          final List<RelationshipType> list1, final List<RelationshipType> list2) {
    final Map<String, List<RelationshipType>> results = new HashMap<>();
    for (RelationshipType type1 : list1) {
      for (RelationshipType type2 : list2) {
        if (type1.getRelationshipTypeId().equals(type2.getRelationshipTypeId())
            && type1.getUuid().contentEquals(type2.getUuid())
            && (!type1.getaIsToB().equalsIgnoreCase(type2.getaIsToB())
                || !type1.getbIsToA().equalsIgnoreCase(type2.getbIsToA()))) {
          results.put(type1.getUuid(), Arrays.asList(type1, type2));
        }
      }
    }
    return results;
  }

  public static <T extends OpenmrsMetadata>
      Map<String, List<T>> findElementsWithDifferentIdsSameUuids(
          final List<T> list1, final List<T> list2) {
    final Map<String, List<T>> results = new HashMap<>();
    for (T ele1 : list1) {
      for (T ele2 : list2) {
        if (!ele1.getId().equals(ele2.getId()) && ele1.getUuid().contentEquals(ele2.getUuid())) {
          results.put(ele1.getUuid(), Arrays.asList(ele1, ele2));
        }
      }
    }
    return results;
  }

  public static <T extends OpenmrsMetadata> boolean isThePairHarmonized(T metadata1, T metadata2) {
    if (metadata1 == null || metadata2 == null) return false;
    if (metadata1.getId() == null
        || metadata2.getId() == null
        || metadata1.getName() == null
        || metadata2.getName() == null
        || metadata1.getUuid() == null
        || metadata2.getUuid() == null) {
      return false;
    }

    return (metadata1.getId().equals(metadata2.getId())
        && metadata1.getName().equalsIgnoreCase(metadata2.getName())
        && metadata1.getUuid().equals(metadata2.getUuid()));
  }

  public static <T extends OpenmrsMetadata> void removeAllHarmonizedElements(
      Collection<T> collection1, Collection<T> collection2) {
    Iterator<T> iterator1 = collection1.iterator();
    while (iterator1.hasNext()) {
      T element1 = iterator1.next();
      Iterator<T> iterator2 = collection2.iterator();
      while (iterator2.hasNext()) {
        T element2 = iterator2.next();
        if (isThePairHarmonized(element1, element2)) {
          iterator1.remove();
          break;
        }
      }
    }
  }
}
