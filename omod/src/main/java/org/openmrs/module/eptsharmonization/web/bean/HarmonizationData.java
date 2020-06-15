package org.openmrs.module.eptsharmonization.web.bean;

import java.io.Serializable;
import java.util.Set;

public class HarmonizationData implements Serializable {

  private static final long serialVersionUID = 1178645170662134449L;

  private Set<HarmonizationItem> items;

  public HarmonizationData(Set<HarmonizationItem> items) {
    this.items = items;
  }

  public Set<HarmonizationItem> getItems() {
    return items;
  }

  public void setItems(Set<HarmonizationItem> items) {
    this.items = items;
  }
}
