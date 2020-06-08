package org.openmrs.module.eptsharmonization.web.bean;

import java.io.Serializable;

public class ProcessHarmonization implements Serializable {

  private static final long serialVersionUID = 8965957574495728847L;

  private String action;

  public ProcessHarmonization(String action) {
    this.action = action;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }
}
