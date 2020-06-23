package org.openmrs.module.eptsharmonization.api.model;

import java.util.Objects;
import org.openmrs.RelationshipType;

public class RelationshipTypeDTO extends BaseDTO {
  /** */
  private static final long serialVersionUID = -3783871175077269243L;

  private RelationshipType relationshipType;

  public RelationshipTypeDTO(RelationshipType relationshipType) {
    super(relationshipType.getId(), relationshipType.getUuid());
    this.setRelationshipType(relationshipType);
  }

  public RelationshipType getRelationshipType() {
    return relationshipType;
  }

  public void setRelationshipType(RelationshipType relationshipType) {
    this.relationshipType = relationshipType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RelationshipTypeDTO)) return false;
    if (!super.equals(o)) return false;
    RelationshipTypeDTO that = (RelationshipTypeDTO) o;
    return Objects.equals(getRelationshipType(), that.getRelationshipType());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getRelationshipType());
  }
}
