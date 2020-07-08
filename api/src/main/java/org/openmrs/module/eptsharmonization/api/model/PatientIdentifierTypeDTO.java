package org.openmrs.module.eptsharmonization.api.model;

import org.openmrs.PatientIdentifierType;

public class PatientIdentifierTypeDTO extends BaseDTO {

  /** */
  private static final long serialVersionUID = -3337426487953264776L;

  private PatientIdentifierType patientIdentifierType;

  public PatientIdentifierTypeDTO(PatientIdentifierType patientIdentifierType) {
    super(patientIdentifierType.getId(), patientIdentifierType.getUuid());
    setPatientIdentifierType(patientIdentifierType);
  }

  public PatientIdentifierType getPatientIdentifierType() {
    return patientIdentifierType;
  }

  public void setPatientIdentifierType(PatientIdentifierType patientIdentifierType) {
    this.patientIdentifierType = patientIdentifierType;
  }
}
