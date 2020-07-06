package org.openmrs.module.eptsharmonization.api.model;

import org.openmrs.ProgramWorkflow;

public class ProgramWorkflowDTO extends BaseDTO {

  /** */
  private static final long serialVersionUID = -8236463885764192877L;

  private ProgramWorkflow programWorkflow;

  private String program;

  private String concept;

  public ProgramWorkflowDTO(ProgramWorkflow programWorkflow) {
    super(programWorkflow.getId(), programWorkflow.getUuid());
    setProgramWorkflow(programWorkflow);
  }

  public ProgramWorkflow getProgramWorkflow() {
    return programWorkflow;
  }

  public void setProgramWorkflow(ProgramWorkflow programWorkflow) {
    this.programWorkflow = programWorkflow;
  }

  public String getProgram() {
    return program;
  }

  public void setProgram(String program) {
    this.program = program;
  }

  public String getConcept() {
    return concept;
  }

  public void setConcept(String concept) {
    this.concept = concept;
  }
}
