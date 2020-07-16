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
import org.openmrs.Program;
import org.openmrs.api.APIException;
import org.openmrs.module.eptsharmonization.api.model.ProgramDTO;

public class ProgramsHarmonizationCSVLog {

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

    public Builder appendLogForNewHarmonizedFromMDSPrograms(List<ProgramDTO> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Added New Programs from Metadata Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (ProgramDTO item : data) {
          try {
            printer.print(
                String.format(
                    "Program ID:{%s}, UUID:%s, NAME:'%s', DESCRIPTION:'%s'",
                    item.getProgram().getId(),
                    item.getProgram().getUuid(),
                    item.getProgram().getName(),
                    item.getProgram().getDescription()));
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

    public Builder appendLogForDeleteFromProductionServer(List<ProgramDTO> data) {

      try {
        printer.print("Metadata Harmonization Process Flow: Deleted Programs in Production Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (ProgramDTO item : data) {
          try {
            printer.print(
                String.format(
                    "Program ID:{%s}, UUID:%s, NAME:'%s', DESCRIPTION:'%s'",
                    item.getProgram().getId(),
                    item.getProgram().getUuid(),
                    item.getProgram().getName(),
                    item.getProgram().getDescription()));
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

    public Builder appendLogForUpdatedEncounterNames(Map<String, List<ProgramDTO>> data) {

      try {
        printer.print("Metadata Harmonization Process Flow: Updated Program Names");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (String key : data.keySet()) {
          List<ProgramDTO> dtos = data.get(key);
          Program mdServerProgram = dtos.get(0).getProgram();
          Program pdServerPeogram = dtos.get(1).getProgram();
          try {
            printer.print(
                String.format(
                    "ID:{%s}, UUID:%s, updated NAME from '%s' to '%s', and DESCRIPTION from '%s' to '%s'",
                    mdServerProgram.getId(),
                    mdServerProgram.getUuid(),
                    pdServerPeogram.getName(),
                    mdServerProgram.getName(),
                    pdServerPeogram.getDescription(),
                    mdServerProgram.getDescription()));
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

    public Builder appendLogForProgramsWithDiferrentIdsAndEqualUUID(
        Map<String, List<ProgramDTO>> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Updated Programs With different ID and equal UUID");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (String key : data.keySet()) {
          List<ProgramDTO> dtos = data.get(key);
          Program mdServerProgram = dtos.get(0).getProgram();
          Program pdServerProgram = dtos.get(1).getProgram();
          try {
            printer.print(
                String.format(
                    "ProductionServer with Program NAME:'%s' and UUID:%s updated ID from {%s} to {%s}",
                    pdServerProgram.getName(),
                    pdServerProgram.getUuid(),
                    pdServerProgram.getId(),
                    mdServerProgram.getId()));
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

    public Builder appendNewMappedPrograms(Map<Program, Program> mapPrograms) {

      try {
        printer.print("Metadata Harmonization Process Flow: Added new Program Mappings ");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (Entry<Program, Program> entry : mapPrograms.entrySet()) {

          Program pdsServer = entry.getKey();
          Program mdsServer = entry.getValue();
          try {
            printer.print(
                String.format(
                    "Program ID:{%s},NAME:'%s',DESCRIPTION:'%s',UUID:%s  Updated to Program ID:{%s},NAME:'%s',DESCRIPTION:'%s',UUID:%s ",
                    pdsServer.getId(),
                    pdsServer.getName(),
                    pdsServer.getDescription(),
                    pdsServer.getUuid(),
                    mdsServer.getId(),
                    mdsServer.getName(),
                    mdsServer.getDescription(),
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

    public ProgramsHarmonizationCSVLog build() {
      try {
        FileOutputStream fos = new FileOutputStream(new File("harmonizationProgramsLog"));
        outputStream.writeTo(fos);
        outputStream.close();
        fos.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        throw new APIException("Unable To Append the log file'harmonizationProgramsLog'");
      } catch (IOException e) {
        e.printStackTrace();
        throw new APIException("Unable To Append the log file'harmonizationProgramsLog'");
      }
      return new ProgramsHarmonizationCSVLog();
    }
  }

  private ProgramsHarmonizationCSVLog() {}

  private static ByteArrayOutputStream getByteArrayOutputStream() {

    ByteArrayOutputStream outputStream = null;
    try {
      File file = new File("harmonizationProgramsLog");
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

  public static ByteArrayOutputStream exportProgramLogs(
      String defaultLocationName, List<ProgramDTO> data, List<Program> mdsPrograms) {

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
      printer.print("Metadata Harmonization Process Flow: Export Programs for Analysis ");
      printer.println();
      printer.print(
          "===============================================================================================================================");
      printer.println();
      printer.println();

      for (ProgramDTO dto : data) {

        Program program = dto.getProgram();
        try {
          printer.print(
              String.format(
                  "ID:%s, NAME:'%s', DESCRIPTION:'%s', UUID:%s",
                  program.getId(), program.getName(), program.getDescription(), program.getUuid()));
          printer.println();
        } catch (Exception e) {
          e.printStackTrace();
          throw new APIException("Unable to write record to CSV: " + e.getMessage());
        }
      }

      printer.println();
      printer.println();
      printer.print("Programs From Metadata Server ");
      printer.println();
      printer.print(
          "===============================================================================================================================");
      printer.println();

      for (Program programs : mdsPrograms) {
        try {
          printer.print(
              String.format(
                  "ID:%s, NAME:'%s', DESCRIPTION:'%s', UUID:%s",
                  programs.getId(),
                  programs.getName(),
                  programs.getDescription(),
                  programs.getUuid()));
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
