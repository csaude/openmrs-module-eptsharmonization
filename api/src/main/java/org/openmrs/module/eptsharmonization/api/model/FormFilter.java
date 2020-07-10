package org.openmrs.module.eptsharmonization.api.model;

import java.io.Serializable;

public class FormFilter implements Serializable {

  /** */
  private static final long serialVersionUID = 6850889360926654886L;

  private Integer formFilterId;

  private Integer formId;

  public FormFilter(Integer formFilterId, Integer formId) {
    this.formFilterId = formFilterId;
    this.formId = formId;
  }

  public Integer getFormFilterId() {
    return formFilterId;
  }

  public void setFormFilterId(Integer formFilterId) {
    this.formFilterId = formFilterId;
  }

  public Integer getFormId() {
    return formId;
  }

  public void setFormId(Integer formId) {
    this.formId = formId;
  }
}
