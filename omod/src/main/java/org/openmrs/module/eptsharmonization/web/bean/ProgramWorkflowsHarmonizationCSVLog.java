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
import org.openmrs.module.eptsharmonization.api.model.ProgramWorkflowDTO;

public class ProgramWorkflowsHarmonizationCSVLog {

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

    public Builder appendLogForNewHarmonizedFromMDSProgramWorkflows(List<ProgramWorkflowDTO> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Added New ProgramWorkflows from Metadata Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (ProgramWorkflowDTO item : data) {
          try {
            printer.print(
                String.format(
                    "ProgramWorkflow ID:{%s}, UUID:%s, PROGRAM:'%s', CONCEPT:'%s'",
                    item.getProgramWorkflow().getId(),
                    item.getProgramWorkflow().getUuid(),
                    item.getProgram(),
                    item.getConcept()));
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

    public Builder appendLogForDeleteFromProductionServer(List<ProgramWorkflowDTO> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Deleted ProgramWorkflows in Production Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (ProgramWorkflowDTO item : data) {
          try {
            printer.print(
                String.format(
                    "ProgramWorkflow ID:{%s}, UUID:%s, PROGRAMA:'%s', CONCEITO:'%s'",
                    item.getProgramWorkflow().getId(),
                    item.getProgramWorkflow().getUuid(),
                    item.getProgram(),
                    item.getConcept()));
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
        Map<String, List<ProgramWorkflowDTO>> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Updated ProgramWorkflow Programs and Concepts");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (String key : data.keySet()) {
          List<ProgramWorkflowDTO> dtos = data.get(key);
          ProgramWorkflowDTO mdServerProgramWorkflow = dtos.get(0);
          ProgramWorkflowDTO pdServerPeogram = dtos.get(1);
          try {
            printer.print(
                String.format(
                    "ID:{%s}, UUID:%s, updated PROGRAM from '%s' to '%s', and CONCEPT from '%s' to '%s'",
                    mdServerProgramWorkflow.getId(),
                    mdServerProgramWorkflow.getUuid(),
                    pdServerPeogram.getProgram(),
                    mdServerProgramWorkflow.getProgram(),
                    pdServerPeogram.getConcept(),
                    mdServerProgramWorkflow.getConcept()));
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

    public Builder appendLogForProgramWorkflowsWithDiferrentIdsAndEqualUUID(
        Map<String, List<ProgramWorkflowDTO>> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Updated ProgramWorkflows With different ID and equal UUID");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (String key : data.keySet()) {
          List<ProgramWorkflowDTO> dtos = data.get(key);
          ProgramWorkflowDTO mdServerProgramWorkflow = dtos.get(0);
          ProgramWorkflowDTO pdServerProgramWorkflow = dtos.get(1);
          try {
            printer.print(
                String.format(
                    "ProductionServer with ProgramWorkflow PROGRAM:'%s', CONCEPT:'%s' and UUID:%s updated ID from {%s} to {%s}",
                    pdServerProgramWorkflow.getProgram(),
                    pdServerProgramWorkflow.getConcept(),
                    pdServerProgramWorkflow.getUuid(),
                    pdServerProgramWorkflow.getId(),
                    mdServerProgramWorkflow.getId()));
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

    public Builder appendNewMappedProgramWorkflows(
        Map<ProgramWorkflowDTO, ProgramWorkflowDTO> manualHarmonizeProgramWorkflows) {

      try {
        printer.print("Metadata Harmonization Process Flow: Added new ProgramWorkflow Mappings ");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (Entry<ProgramWorkflowDTO, ProgramWorkflowDTO> entry :
            manualHarmonizeProgramWorkflows.entrySet()) {

          ProgramWorkflowDTO pdsServer = entry.getKey();
          ProgramWorkflowDTO mdsServer = entry.getValue();
          try {
            printer.print(
                String.format(
                    "ProgramWorkflow ID:{%s}, PROGRAM:'%s', CONCEPT:'%s', UUID:%s  Updated to ProgramWorkflow ID:{%s}, PROGRAM:'%s', CONCEPT:'%s', UUID:%s ",
                    pdsServer.getId(),
                    pdsServer.getProgram(),
                    pdsServer.getConcept(),
                    pdsServer.getUuid(),
                    mdsServer.getId(),
                    mdsServer.getProgram(),
                    mdsServer.getConcept(),
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

    public ProgramWorkflowsHarmonizationCSVLog build() {
      try {
        FileOutputStream fos = new FileOutputStream(new File("harmonizationProgramWorkflowsLog"));
        outputStream.writeTo(fos);
        outputStream.close();
        fos.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        throw new APIException("Unable To Append the log file'harmonizationProgramWorkflowsLog'");
      } catch (IOException e) {
        e.printStackTrace();
        throw new APIException("Unable To Append the log file'harmonizationProgramWorkflowsLog'");
      }
      return new ProgramWorkflowsHarmonizationCSVLog();
    }
  }

  private ProgramWorkflowsHarmonizationCSVLog() {}

  private static ByteArrayOutputStream getByteArrayOutputStream() {

    ByteArrayOutputStream outputStream = null;
    try {
      File file = new File("harmonizationProgramWorkflowsLog");
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

  public static ByteArrayOutputStream exportProgramWorkflowsLogs(
      String defaultLocationName, List<ProgramWorkflowDTO> data) {

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
      printer.print("Metadata Harmonization Process Flow: Export ProgramWorkflows for Analysis ");
      printer.println();
      printer.print(
          "===============================================================================================================================");
      printer.println();
      printer.println();

      for (ProgramWorkflowDTO programWorkflow : data) {

        try {
          printer.print(
              String.format(
                  "ID:%s, PROGRAM:'%s', CONCEPT:'%s', UUID:%s",
                  programWorkflow.getId(),
                  programWorkflow.getProgram(),
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
