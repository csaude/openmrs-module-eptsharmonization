package org.openmrs.module.eptsharmonization.api.model;

import java.io.Serializable;
import org.apache.commons.lang3.builder.CompareToBuilder;

@SuppressWarnings("serial")
public abstract class BaseDTO implements Serializable, Comparable<BaseDTO> {

  private Integer id;
  private String uuid;

  public BaseDTO(Integer id, String uuid) {
    this.id = id;
    this.uuid = uuid;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    BaseDTO other = (BaseDTO) obj;
    if (id == null) {
      if (other.id != null) return false;
    } else if (!id.equals(other.id)) return false;
    if (uuid == null) {
      if (other.uuid != null) return false;
    } else if (!uuid.equals(other.uuid)) return false;
    return true;
  }

  @Override
  public int compareTo(BaseDTO obj) {
    return new CompareToBuilder()
        .append(this.uuid, obj.uuid)
        .append(this.id, obj.id)
        .toComparison();
  }
}
