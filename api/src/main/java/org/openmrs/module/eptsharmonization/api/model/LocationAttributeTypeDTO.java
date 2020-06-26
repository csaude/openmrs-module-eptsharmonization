package org.openmrs.module.eptsharmonization.api.model;

import java.util.Objects;
import org.openmrs.LocationAttributeType;

public class LocationAttributeTypeDTO extends BaseDTO {
  /** */
  private static final long serialVersionUID = 3711971175077269003L;

  private LocationAttributeType locationAttributeType;

  public LocationAttributeTypeDTO(LocationAttributeType locationAttributeType) {
    super(locationAttributeType.getId(), locationAttributeType.getUuid());
    this.setLocationAttributeType(locationAttributeType);
  }

  public LocationAttributeType getLocationAttributeType() {
    return locationAttributeType;
  }

  public void setLocationAttributeType(LocationAttributeType locationAttributeType) {
    this.locationAttributeType = locationAttributeType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LocationAttributeTypeDTO)) return false;
    if (!super.equals(o)) return false;
    LocationAttributeTypeDTO that = (LocationAttributeTypeDTO) o;
    return Objects.equals(getLocationAttributeType(), that.getLocationAttributeType());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getLocationAttributeType());
  }
}
