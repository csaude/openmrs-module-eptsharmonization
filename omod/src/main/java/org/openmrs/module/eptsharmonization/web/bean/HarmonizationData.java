package org.openmrs.module.eptsharmonization.web.bean;

import java.io.Serializable;
import java.util.List;

public class HarmonizationData implements Serializable {

  private static final long serialVersionUID = 1178645170662134449L;

  private List<HarmonizationItem> items;

  public HarmonizationData(List<HarmonizationItem> items) {
    this.items = items;
  }

  public List<HarmonizationItem> getItems() {
    return items;
  }

  public void setItems(List<HarmonizationItem> items) {
    this.items = items;
  }
}
