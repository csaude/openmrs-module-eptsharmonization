package org.openmrs.module.eptsharmonization.web.bean;

import java.io.Serializable;
import org.openmrs.module.eptsharmonization.api.model.VisitTypeDTO;

/** @uthor Willa Mhawila<a.mhawila@gmail.com> on 6/17/20. */
public class VisitTypeBean implements Serializable {
  private static final long serialVersionUID = 7519508679151116347L;

  private Object key;
  private Object value;

  private Boolean selected;
  private Integer visitsCount;

  public VisitTypeBean() {}

  public VisitTypeBean(VisitTypeDTO key, VisitTypeDTO value) {
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

  public Boolean getSelected() {
    return selected;
  }

  public void setSelected(Boolean selected) {
    this.selected = selected;
  }

  public Integer getVisitsCount() {
    return visitsCount;
  }

  public void setVisitsCount(Integer visitsCount) {
    this.visitsCount = visitsCount;
  }
}
