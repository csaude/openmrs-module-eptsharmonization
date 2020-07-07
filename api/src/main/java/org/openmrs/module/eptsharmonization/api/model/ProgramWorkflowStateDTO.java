package org.openmrs.module.eptsharmonization.api.model;

import org.openmrs.ProgramWorkflowState;

public class ProgramWorkflowStateDTO extends BaseDTO {

  /** */
  private static final long serialVersionUID = 1128003657023695534L;

  private ProgramWorkflowState programWorkflowState;

  private String flowProgram;

  private String flowConcept;

  private String concept;

  public ProgramWorkflowStateDTO(ProgramWorkflowState programWorkflowState) {
    super(programWorkflowState.getId(), programWorkflowState.getUuid());
    this.programWorkflowState = programWorkflowState;
  }

  public ProgramWorkflowState getProgramWorkflowState() {
    return programWorkflowState;
  }

  public void setProgramWorkflowState(ProgramWorkflowState programWorkflowSate) {
    this.programWorkflowState = programWorkflowSate;
  }

  public String getFlowProgram() {
    return flowProgram;
  }

  public void setFlowProgram(String flowProgram) {
    this.flowProgram = flowProgram;
  }

  public String getFlowConcept() {
    return flowConcept;
  }

  public void setFlowConcept(String flowConcept) {
    this.flowConcept = flowConcept;
  }

  public String getConcept() {
    return concept;
  }

  public void setConcept(String concept) {
    this.concept = concept;
  }
}
