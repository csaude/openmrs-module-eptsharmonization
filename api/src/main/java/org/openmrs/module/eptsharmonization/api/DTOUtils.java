package org.openmrs.module.eptsharmonization.api;

import java.util.ArrayList;
import java.util.List;
import org.openmrs.EncounterType;
import org.openmrs.PersonAttributeType;
import org.openmrs.module.eptsharmonization.api.model.EncounterTypeDTO;
import org.openmrs.module.eptsharmonization.api.model.PersonAttributeTypeDTO;

public class DTOUtils {

  public static List<EncounterTypeDTO> fromEncounterTypes(List<EncounterType> encounterTypes) {
    List<EncounterTypeDTO> result = new ArrayList<>();

    for (EncounterType encounterType : encounterTypes) {
      result.add(new EncounterTypeDTO(encounterType));
    }
    return result;
  }

  public static List<EncounterType> fromEncounterTypeDTOs(
      List<EncounterTypeDTO> encounterTypeDTOs) {
    List<EncounterType> result = new ArrayList<>();
    for (EncounterTypeDTO encounterTypeDTO : encounterTypeDTOs) {
      result.add(encounterTypeDTO.getEncounterType());
    }
    return result;
  }

  public static EncounterTypeDTO fromEncounterType(EncounterType encounterType) {
    return new EncounterTypeDTO(encounterType);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static List<PersonAttributeTypeDTO> fromPersonAttributeTypes(
      List<PersonAttributeType> personAttributeTypes) {
    List<PersonAttributeTypeDTO> result = new ArrayList();

    for (PersonAttributeType personAttributeType : personAttributeTypes) {
      result.add(new PersonAttributeTypeDTO(personAttributeType));
    }

    return result;
  }

  public static List<PersonAttributeType> fromPersonAttributeTypesDTOs(
      List<PersonAttributeTypeDTO> personAttributeDTOs) {
    List<PersonAttributeType> result = new ArrayList<>();
    for (PersonAttributeTypeDTO item : personAttributeDTOs) {
      result.add(item.getPersonAttributeType());
    }
    return result;
  }

  public static PersonAttributeTypeDTO fromPersonAttributeType(
      PersonAttributeType personAttributeType) {
    return new PersonAttributeTypeDTO(personAttributeType);
  }
}
