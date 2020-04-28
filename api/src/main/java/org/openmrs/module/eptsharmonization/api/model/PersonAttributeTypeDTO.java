package org.openmrs.module.eptsharmonization.api.model;

import org.openmrs.PersonAttributeType;

public class PersonAttributeTypeDTO extends BaseDTO {

  /** */
  private static final long serialVersionUID = 92788743898637999L;

  private PersonAttributeType personAttributeType;

  public PersonAttributeTypeDTO(PersonAttributeType personAttributeType) {
    super(personAttributeType.getId(), personAttributeType.getUuid());
    setPersonAttributeType(personAttributeType);
  }

  public PersonAttributeType getPersonAttributeType() {
    return personAttributeType;
  }

  public void setPersonAttributeType(PersonAttributeType personAttributeType) {
    this.personAttributeType = personAttributeType;
  }
}
