package org.openmrs.module.eptsharmonization.api.model;

import org.openmrs.EncounterType;

public class EncounterTypeDTO extends BaseDTO {
  /** */
  private static final long serialVersionUID = -5555871173077269215L;

  private EncounterType encounterType;

  public EncounterTypeDTO(EncounterType encounterType) {
    super(encounterType.getId(), encounterType.getUuid());
    this.setEncounterType(encounterType);
  }

  public EncounterType getEncounterType() {
    return encounterType;
  }

  public void setEncounterType(EncounterType encounterType) {
    this.encounterType = encounterType;
  }
}
