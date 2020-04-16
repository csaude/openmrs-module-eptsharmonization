package org.openmrs.module.eptsharmonization.api;

import java.util.ArrayList;
import java.util.List;
import org.openmrs.EncounterType;
import org.openmrs.module.eptsharmonization.api.model.EncounterTypeDTO;

public class DTOUtils {

  public static List<EncounterTypeDTO> fromEncounterTypes(List<EncounterType> encounterTypes) {
    List<EncounterTypeDTO> result = new ArrayList<>();

    for (EncounterType encounterType : encounterTypes) {
      result.add(new EncounterTypeDTO(encounterType));
    }
    return result;
  }

  public static EncounterTypeDTO fromEncounterType(EncounterType encounterType) {
    return new EncounterTypeDTO(encounterType);
  }
}
