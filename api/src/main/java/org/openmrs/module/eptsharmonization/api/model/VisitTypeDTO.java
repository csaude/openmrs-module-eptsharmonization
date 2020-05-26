package org.openmrs.module.eptsharmonization.api.model;

import java.util.Objects;
import org.openmrs.VisitType;

public class VisitTypeDTO extends BaseDTO {
  /** */
  private static final long serialVersionUID = -5666871175077269215L;

  private VisitType visitType;

  public VisitTypeDTO(VisitType visitType) {
    super(visitType.getId(), visitType.getUuid());
    this.setVisitType(visitType);
  }

  public VisitType getVisitType() {
    return visitType;
  }

  public void setVisitType(VisitType visitType) {
    this.visitType = visitType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof VisitTypeDTO)) return false;
    if (!super.equals(o)) return false;
    VisitTypeDTO that = (VisitTypeDTO) o;
    return Objects.equals(getVisitType(), that.getVisitType());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getVisitType());
  }
}
