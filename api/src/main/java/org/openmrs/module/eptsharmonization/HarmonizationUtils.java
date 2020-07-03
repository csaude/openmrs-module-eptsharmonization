package org.openmrs.module.eptsharmonization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.RelationshipType;
import org.openmrs.api.context.Context;
import org.openmrs.attribute.BaseAttributeType;

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
        "update form set swappable = true                          "
            + "  where form_id in(  select a.* from(                  "
            + "   select form.form_id from form                     "
            + "   where NOT EXISTS (select * from _form             "
            + "  where _form.form_id = form.form_id                "
            + "  and _form.uuid = form.uuid)) as a)                            ");
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

  public static <AttributeType extends BaseAttributeType<?>>
      Map<String, List<AttributeType>> findAttributeTypesWithSameUuidsAndIdsDifferentConfiguration(
          final List<AttributeType> list1, final List<AttributeType> list2) {
    final Map<String, List<AttributeType>> results = new HashMap<>();
    for (AttributeType ele1 : list1) {
      for (AttributeType ele2 : list2) {
        String ele1DatatypeClassname =
            org.springframework.util.StringUtils.trimWhitespace(ele1.getDatatypeClassname());
        String ele2DatatypeClassname =
            org.springframework.util.StringUtils.trimWhitespace(ele2.getDatatypeClassname());
        boolean differentDatatypeClassname =
            ((org.springframework.util.StringUtils.isEmpty(ele1DatatypeClassname)
                    && !org.springframework.util.StringUtils.isEmpty(ele2DatatypeClassname))
                || (!org.springframework.util.StringUtils.isEmpty(ele1DatatypeClassname)
                    && org.springframework.util.StringUtils.isEmpty(ele2DatatypeClassname))
                || !StringUtils.equals(ele1DatatypeClassname, ele2DatatypeClassname));

        String ele1DatatypeConfig =
            org.springframework.util.StringUtils.trimWhitespace(ele1.getDatatypeConfig());
        String ele2DatatypeConfig =
            org.springframework.util.StringUtils.trimWhitespace(ele2.getDatatypeConfig());
        boolean differentDatatypeConfig =
            ((org.springframework.util.StringUtils.isEmpty(ele1DatatypeConfig)
                    && !org.springframework.util.StringUtils.isEmpty(ele2DatatypeConfig))
                || (!org.springframework.util.StringUtils.isEmpty(ele1DatatypeConfig)
                    && org.springframework.util.StringUtils.isEmpty(ele2DatatypeConfig))
                || !StringUtils.equals(ele1DatatypeConfig, ele2DatatypeConfig));

        String ele1HandlerConfig =
            org.springframework.util.StringUtils.trimWhitespace(ele1.getHandlerConfig());
        String ele2HandlerConfig =
            org.springframework.util.StringUtils.trimWhitespace(ele2.getHandlerConfig());
        boolean differentHandlerConfig =
            ((org.springframework.util.StringUtils.isEmpty(ele1HandlerConfig)
                    && !org.springframework.util.StringUtils.isEmpty(ele2HandlerConfig))
                || (!org.springframework.util.StringUtils.isEmpty(ele1HandlerConfig)
                    && org.springframework.util.StringUtils.isEmpty(ele2HandlerConfig))
                || !StringUtils.equals(ele1HandlerConfig, ele2HandlerConfig));

        String ele1PreferredHandlerClassname =
            org.springframework.util.StringUtils.trimWhitespace(
                ele1.getPreferredHandlerClassname());
        String ele2PreferredHandlerClassname =
            org.springframework.util.StringUtils.trimWhitespace(
                ele2.getPreferredHandlerClassname());
        boolean differentPreferredHandlerClassname =
            ((org.springframework.util.StringUtils.isEmpty(ele1PreferredHandlerClassname)
                    && !org.springframework.util.StringUtils.isEmpty(ele2PreferredHandlerClassname))
                || (!org.springframework.util.StringUtils.isEmpty(ele1PreferredHandlerClassname)
                    && org.springframework.util.StringUtils.isEmpty(ele2PreferredHandlerClassname))
                || !StringUtils.equals(
                    ele1PreferredHandlerClassname, ele2PreferredHandlerClassname));

        boolean differentConfigs =
            (!StringUtils.equalsIgnoreCase(ele1.getName(), ele2.getName())
                || differentDatatypeClassname
                || differentDatatypeConfig
                || differentHandlerConfig
                || differentPreferredHandlerClassname
                || Integer.compare(ele1.getMinOccurs(), ele2.getMinOccurs()) != 0
                || Integer.compare(ele1.getMaxOccurs(), ele2.getMaxOccurs()) != 0);
        if (ele1.getId().equals(ele2.getId())
            && ele1.getUuid().contentEquals(ele2.getUuid())
            && differentConfigs) {
          results.put(ele1.getUuid(), Arrays.asList(ele1, ele2));
        }
      }
    }
    return results;
  }

  public static <AttributeType extends BaseAttributeType<?>> boolean isTheAttributePairHarmonized(
      AttributeType type1, AttributeType type2) {
    if (type1 == null || type2 == null) return false;
    if (type1.getId() == null
        || type2.getId() == null
        || type1.getName() == null
        || type2.getName() == null
        || type1.getUuid() == null
        || type2.getUuid() == null) {
      return false;
    }

    return (type1.getId().equals(type2.getId())
        && type1.getName().equalsIgnoreCase(type2.getName())
        && type1.getUuid().equals(type2.getUuid())
        && StringUtils.equals(type1.getDatatypeClassname(), type2.getDatatypeClassname())
        && StringUtils.equalsIgnoreCase(type1.getDatatypeConfig(), type2.getDatatypeConfig())
        && StringUtils.equalsIgnoreCase(type1.getHandlerConfig(), type2.getHandlerConfig())
        && StringUtils.equals(
            type1.getPreferredHandlerClassname(), type2.getPreferredHandlerClassname())
        && Integer.compare(type1.getMinOccurs(), type2.getMinOccurs()) == 0
        && Integer.compare(type1.getMaxOccurs(), type2.getMaxOccurs()) == 0);
  }

  public static <AttributeType extends BaseAttributeType<?>> void removeAllHarmonizedAttributes(
      Collection<AttributeType> collection1, Collection<AttributeType> collection2) {
    Iterator<AttributeType> iterator1 = collection1.iterator();
    while (iterator1.hasNext()) {
      AttributeType element1 = iterator1.next();
      Iterator<AttributeType> iterator2 = collection2.iterator();
      while (iterator2.hasNext()) {
        AttributeType element2 = iterator2.next();
        if (isTheAttributePairHarmonized(element1, element2)) {
          iterator1.remove();
          break;
        }
      }
    }
  }
}
