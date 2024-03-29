package org.openmrs.module.eptsharmonization.api.model;

import java.util.Objects;
import org.openmrs.LocationTag;

public class LocationTagDTO extends BaseDTO {
  /** */
  private static final long serialVersionUID = -5666871175077269215L;

  private LocationTag locationTag;

  public LocationTagDTO(LocationTag locationTag) {
    super(locationTag.getId(), locationTag.getUuid());
    this.setLocationTag(locationTag);
  }

  public LocationTag getLocationTag() {
    return locationTag;
  }

  public void setLocationTag(LocationTag locationTag) {
    this.locationTag = locationTag;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getLocationTag());
  }
}
