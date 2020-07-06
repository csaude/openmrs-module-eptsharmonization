package org.openmrs.module.eptsharmonization.api.model;

import org.openmrs.EncounterType;
import org.openmrs.Form;

public class FormDTO extends BaseDTO {
  /** */
  private static final long serialVersionUID = -5555871173077269215L;

  private Form form;

  private EncounterType encounterType;

  public FormDTO(Form form) {
    super(form.getId(), form.getUuid());
    this.setForm(form);
  }

  public Form getForm() {
    return form;
  }

  public void setForm(Form form) {
    this.form = form;
  }

  public EncounterType getEncounterType() {
    return encounterType;
  }

  public void setEncounterType(EncounterType encounterType) {
    this.encounterType = encounterType;
  }
}
