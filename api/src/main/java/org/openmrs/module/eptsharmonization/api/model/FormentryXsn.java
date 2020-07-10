package org.openmrs.module.eptsharmonization.api.model;

import java.io.Serializable;

public class FormentryXsn implements Serializable {

  /** */
  private static final long serialVersionUID = 5744650143785598955L;

  private Integer formentryXsnId;

  private Integer formId;

  public FormentryXsn(Integer formentryXsnId, Integer formId) {
    this.formentryXsnId = formentryXsnId;
    this.formId = formId;
  }

  public Integer getFormentryXsnId() {
    return formentryXsnId;
  }

  public void setFormentryXsnId(Integer formentryXsnId) {
    this.formentryXsnId = formentryXsnId;
  }

  public Integer getFormId() {
    return formId;
  }

  public void setFormId(Integer formId) {
    this.formId = formId;
  }
}
