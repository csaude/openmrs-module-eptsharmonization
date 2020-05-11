package org.openmrs.module.eptsharmonization.web.bean;

import java.io.Serializable;

public class HarmonizationItem implements Serializable {

  private static final long serialVersionUID = 3319508983151116282L;
  private Object key;
  private Object value;

  private boolean selected;
  private int encountersCount;
  private int formsCount;

  public HarmonizationItem(Object key, Object value) {
    this.key = key;
    this.value = value;
  }

  public Object getKey() {
    return key;
  }

  public void setKey(Object key) {
    this.key = key;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public int getEncountersCount() {
    return encountersCount;
  }

  public void setEncountersCount(int encountersCount) {
    this.encountersCount = encountersCount;
  }

  public int getFormsCount() {
    return formsCount;
  }

  public void setFormsCount(int formsCount) {
    this.formsCount = formsCount;
  }
}
