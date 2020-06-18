package org.openmrs.module.eptsharmonization.api.model;

import org.openmrs.Program;

public class ProgramDTO extends BaseDTO {

  /** */
  private static final long serialVersionUID = 4989290144656577428L;

  private Program program;

  public ProgramDTO(Program program) {
    super(program.getId(), program.getUuid());
    setProgram(program);
  }

  public Program getProgram() {
    return program;
  }

  public void setProgram(Program program) {
    this.program = program;
  }
}
