package org.openmrs.module.eptsharmonization.web.bean;

import java.io.Serializable;

public class HarmonizationItem implements Serializable {

  private static final long serialVersionUID = 3319508983151116282L;
  private Object key;
  private Object value;

  private boolean selected;
  private int encountersCount;
  private int formsCount;
  private int formFieldsCount;
  private int formResourceCount;

  public HarmonizationItem(Object key, Object value) {
    this.key = key;
    this.value = value;
  }

  public HarmonizationItem() {}

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

  public int getFormFieldsCount() {
    return formFieldsCount;
  }

  public void setFormFieldsCount(int formFieldsCount) {
    this.formFieldsCount = formFieldsCount;
  }

  public int getFormResourceCount() {
    return formResourceCount;
  }

  public void setFormResourceCount(int formResourceCount) {
    this.formResourceCount = formResourceCount;
  }

  //  @Override
  //  public int compareTo(HarmonizationItem other) {
  //
  //    if ((this.key instanceof String) && other.getKey() instanceof String) {
  //      return ((String) this.key).compareTo(((String) other.getKey()));
  //    }
  //
  //    if ((this.key instanceof EncounterTypeDTO) && other.getKey() instanceof EncounterTypeDTO) {
  //      return ((EncounterTypeDTO) this.key).compareTo(((EncounterTypeDTO) other.getKey()));
  //    }
  //
  //    if ((this.key instanceof EncounterType) && other.getKey() instanceof EncounterType) {
  //      return ((EncounterType) this.key)
  //          .getUuid()
  //          .compareTo(((EncounterType) other.getKey()).getUuid());
  //    }
  //
  //    if ((this.key instanceof Form) && other.getKey() instanceof Form) {
  //      return ((Form) this.key).getUuid().compareTo(((Form) other.getKey()).getUuid());
  //    }
  //
  //    if ((this.key instanceof FormDTO) && other.getKey() instanceof FormDTO) {
  //      return ((FormDTO) this.key)
  //          .getForm()
  //          .getUuid()
  //          .compareTo(((FormDTO) other.getKey()).getForm().getUuid());
  //    }
  //
  //    return -1;
  //  }
}
