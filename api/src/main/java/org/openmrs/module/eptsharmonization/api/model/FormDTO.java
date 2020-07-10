package org.openmrs.module.eptsharmonization.api.model;

import org.openmrs.Form;

public class FormDTO extends BaseDTO {
  /** */
  private static final long serialVersionUID = -5555871173077269215L;

  private Form form;

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
}
