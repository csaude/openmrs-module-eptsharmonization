package org.openmrs.module.eptsharmonization.web.bean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVStrategy;
import org.openmrs.api.APIException;
import org.openmrs.module.eptsharmonization.api.model.ProgramWorkflowStateDTO;

public class ProgramWorkflowStatesHarmonizationCSVLog {

  CSVPrinter printer;

  public static class Builder {

    ByteArrayOutputStream outputStream = getByteArrayOutputStream();
    private CSVPrinter printer;

    public Builder(String defaultLocationName) {

      try {
        printer =
            new CSVPrinter(
                new OutputStreamWriter(outputStream, StandardCharsets.ISO_8859_1),
                new CSVStrategy(
                    '\t',
                    ' ',
                    CSVStrategy.COMMENTS_DISABLED,
                    CSVStrategy.ESCAPE_DISABLED,
                    false,
                    false,
                    false,
                    true));

        printer.print("Location: " + defaultLocationName);
        printer.println();
        printer.print("Execution Date: " + Calendar.getInstance().getTime());
        printer.println();
        printer.println();

        printer.flush();
      } catch (Exception e) {
        e.printStackTrace();
        throw new APIException("Unable to build OutputStream for CSV: " + e.getMessage());
      }
    }

    public Builder appendLogForNewHarmonizedFromMDSProgramWorkflowStates(
        List<ProgramWorkflowStateDTO> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Added New ProgramWorkflowStates from Metadata Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (ProgramWorkflowStateDTO item : data) {
          try {
            printer.print(
                String.format(
                    "ProgramWorkflowState ID:{%s}, UUID:%s, STATE:%s, WORKFLOW:'%s', PROGRAM:'%s'",
                    item.getProgramWorkflowState().getId(),
                    item.getProgramWorkflowState().getUuid(),
                    item.getConcept(),
                    item.getFlowConcept(),
                    item.getFlowProgram()));
            printer.println();
          } catch (Exception e) {
            e.printStackTrace();
            throw new APIException("Unable to write record to CSV: " + e.getMessage());
          }
        }
        printer.println();

        printer.flush();
      } catch (Exception e) {
        e.printStackTrace();
        throw new APIException("Unable to build OutputStream for CSV: " + e.getMessage());
      }
      return this;
    }

    public Builder appendLogForDeleteFromProductionServer(List<ProgramWorkflowStateDTO> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Deleted ProgramWorkflowStates in Production Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (ProgramWorkflowStateDTO item : data) {
          try {
            printer.print(
                String.format(
                    "ProgramWorkflowState ID:{%s}, UUID:%s, STATE:%s, FLOW:'%s', PROGRAM:'%s'",
                    item.getProgramWorkflowState().getId(),
                    item.getProgramWorkflowState().getUuid(),
                    item.getConcept(),
                    item.getFlowConcept(),
                    item.getFlowProgram()));
            printer.println();
          } catch (Exception e) {
            e.printStackTrace();
            throw new APIException("Unable to write record to CSV: " + e.getMessage());
          }
        }
        printer.println();

        printer.flush();
      } catch (Exception e) {
        e.printStackTrace();
        throw new APIException("Unable to build OutputStream for CSV: " + e.getMessage());
      }
      return this;
    }

    public Builder appendLogForUpdatedProgramsAndConcepts(
        Map<String, List<ProgramWorkflowStateDTO>> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Updated ProgramWorkflowState Program Workflows and Concepts");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (String key : data.keySet()) {
          List<ProgramWorkflowStateDTO> dtos = data.get(key);
          ProgramWorkflowStateDTO mdServerProgramWorkflowState = dtos.get(0);
          ProgramWorkflowStateDTO pdServerPeogram = dtos.get(1);
          try {
            printer.print(
                String.format(
                    "ID:{%s}, UUID:%s, updated STATE from '%s' to '%s', WORKFLOW from '%s' to '%s', and PROGRAM from '%s' to '%s'",
                    mdServerProgramWorkflowState.getId(),
                    mdServerProgramWorkflowState.getUuid(),
                    pdServerPeogram.getConcept(),
                    mdServerProgramWorkflowState.getConcept(),
                    pdServerPeogram.getFlowConcept(),
                    mdServerProgramWorkflowState.getFlowConcept(),
                    pdServerPeogram.getFlowProgram(),
                    mdServerProgramWorkflowState.getFlowProgram()));
            printer.println();
          } catch (Exception e) {
            e.printStackTrace();
            throw new APIException("Unable to write record to CSV: " + e.getMessage());
          }
        }
        printer.println();

        printer.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return this;
    }

    public Builder appendLogForProgramWorkflowStatesWithDiferrentIdsAndEqualUUID(
        Map<String, List<ProgramWorkflowStateDTO>> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Updated ProgramWorkflowStates With different ID and equal UUID");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (String key : data.keySet()) {
          List<ProgramWorkflowStateDTO> dtos = data.get(key);
          ProgramWorkflowStateDTO mdServerProgramWorkflowState = dtos.get(0);
          ProgramWorkflowStateDTO pdServerProgramWorkflowState = dtos.get(1);
          try {
            printer.print(
                String.format(
                    "ProductionServer with ProgramWorkflowState STATE:'%s', WORKFLOW:'%s', PROGRAM:'%s' and UUID:%s updated ID from {%s} to {%s}",
                    pdServerProgramWorkflowState.getConcept(),
                    pdServerProgramWorkflowState.getFlowConcept(),
                    pdServerProgramWorkflowState.getFlowProgram(),
                    pdServerProgramWorkflowState.getUuid(),
                    pdServerProgramWorkflowState.getId(),
                    mdServerProgramWorkflowState.getId()));
            printer.println();
          } catch (Exception e) {
            e.printStackTrace();
            throw new APIException("Unable to write record to CSV: " + e.getMessage());
          }
        }
        printer.println();

        printer.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return this;
    }

    public Builder appendNewMappedProgramWorkflowStates(
        Map<ProgramWorkflowStateDTO, ProgramWorkflowStateDTO>
            manualHarmonizeProgramWorkflowStates) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Added new ProgramWorkflowState Mappings ");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (Entry<ProgramWorkflowStateDTO, ProgramWorkflowStateDTO> entry :
            manualHarmonizeProgramWorkflowStates.entrySet()) {

          ProgramWorkflowStateDTO pdsServer = entry.getKey();
          ProgramWorkflowStateDTO mdsServer = entry.getValue();
          try {
            printer.print(
                String.format(
                    "ProgramWorkflowState ID:{%s}, STATE:'%s', FLOW:'%s', PROGRAM:'%s', UUID:%s  Updated to ProgramWorkflowState ID:{%s}, STATE:'%s', FLOW:'%s', PROGRAM:'%s', UUID:%s ",
                    pdsServer.getId(),
                    pdsServer.getConcept(),
                    pdsServer.getFlowConcept(),
                    pdsServer.getFlowProgram(),
                    pdsServer.getUuid(),
                    mdsServer.getId(),
                    mdsServer.getConcept(),
                    mdsServer.getFlowConcept(),
                    mdsServer.getFlowProgram(),
                    mdsServer.getUuid()));
            printer.println();
          } catch (Exception e) {
            e.printStackTrace();
            throw new APIException("Unable to write record to CSV: " + e.getMessage());
          }
        }
        printer.println();

        printer.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return this;
    }

    public ProgramWorkflowStatesHarmonizationCSVLog build() {
      try {
        FileOutputStream fos =
            new FileOutputStream(new File("harmonizationProgramWorkflowStatesLog"));
        outputStream.writeTo(fos);
        outputStream.close();
        fos.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        throw new APIException(
            "Unable To Append the log file'harmonizationProgramWorkflowStatesLog'");
      } catch (IOException e) {
        e.printStackTrace();
        throw new APIException(
            "Unable To Append the log file'harmonizationProgramWorkflowStatesLog'");
      }
      return new ProgramWorkflowStatesHarmonizationCSVLog();
    }
  }

  private ProgramWorkflowStatesHarmonizationCSVLog() {}

  private static ByteArrayOutputStream getByteArrayOutputStream() {

    ByteArrayOutputStream outputStream = null;
    try {
      File file = new File("harmonizationProgramWorkflowStatesLog");
      file.createNewFile();
      FileInputStream fis = new FileInputStream(file);
      outputStream = new ByteArrayOutputStream();

      byte[] buf = new byte[1024];
      try {
        for (int readNum; (readNum = fis.read(buf)) != -1; ) {
          outputStream.write(buf, 0, readNum);
        }
      } catch (IOException ex) {
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return outputStream;
  }

  public static ByteArrayOutputStream exportProgramWorkflowStatesLogs(
      String defaultLocationName, List<ProgramWorkflowStateDTO> data) {

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    try {
      CSVPrinter printer =
          new CSVPrinter(
              new OutputStreamWriter(outputStream, StandardCharsets.ISO_8859_1),
              new CSVStrategy(
                  '\t',
                  ' ',
                  CSVStrategy.COMMENTS_DISABLED,
                  CSVStrategy.ESCAPE_DISABLED,
                  false,
                  false,
                  false,
                  true));

      printer.print("Location: " + defaultLocationName);
      printer.println();
      printer.print("Execution Date: " + Calendar.getInstance().getTime());
      printer.println();
      printer.print(
          "Metadata Harmonization Process Flow: Export ProgramWorkflowStates for Analysis ");
      printer.println();
      printer.print(
          "===============================================================================================================================");
      printer.println();
      printer.println();

      for (ProgramWorkflowStateDTO programWorkflow : data) {

        try {
          printer.print(
              String.format(
                  "ID:%s, PROGRAM:'%s', CONCEPT:'%s', UUID:%s",
                  programWorkflow.getId(),
                  programWorkflow.getFlowProgram(),
                  programWorkflow.getConcept(),
                  programWorkflow.getUuid()));
          printer.println();
        } catch (Exception e) {
          e.printStackTrace();
          throw new APIException("Unable to write record to CSV: " + e.getMessage());
        }
      }
      printer.flush();
    } catch (Exception e) {
      e.printStackTrace();
      throw new APIException("Unable to build OutputStream for CSV: " + e.getMessage());
    }
    return outputStream;
  }
}
