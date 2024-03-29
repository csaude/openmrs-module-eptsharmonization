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
import org.openmrs.VisitType;
import org.openmrs.api.APIException;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.model.VisitTypeDTO;

public class VisitTypeHarmonizationCSVLog {

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

    public Builder appendLogForNewHarmonizedFromMDSVisitTypes(List<VisitTypeDTO> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Added New Visit Types from Metadata Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (VisitTypeDTO item : data) {
          try {
            printer.print(
                String.format(
                    "VisitType ID:{%s}, UUID:%s, NAME:'%s', DESCRIPTION:'%s'",
                    item.getVisitType().getId(),
                    item.getVisitType().getUuid(),
                    item.getVisitType().getName(),
                    item.getVisitType().getDescription()));
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

    public Builder appendLogForDeleteFromProductionServer(List<VisitTypeDTO> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Deleted Visit Types in Production Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (VisitTypeDTO item : data) {
          try {
            printer.print(
                String.format(
                    "VisitType ID:{%s}, UUID:%s, NAME:'%s', DESCRIPTION:'%s'",
                    item.getVisitType().getId(),
                    item.getVisitType().getUuid(),
                    item.getVisitType().getName(),
                    item.getVisitType().getDescription()));
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

    public Builder appendLogForUpdatedVisitNames(Map<String, List<VisitTypeDTO>> data) {

      try {
        printer.print("Metadata Harmonization Process Flow: Updated Visit Type Names");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (String key : data.keySet()) {
          List<VisitTypeDTO> dtos = data.get(key);
          VisitType mdServerET = dtos.get(0).getVisitType();
          VisitType pdServerET = dtos.get(1).getVisitType();
          try {
            printer.print(
                String.format(
                    "ID:{%s}, UUID:%s, updated NAME from '%s' to '%s', and DESCRIPTION from '%s' to '%s'",
                    mdServerET.getId(),
                    mdServerET.getUuid(),
                    pdServerET.getName(),
                    mdServerET.getName(),
                    pdServerET.getDescription(),
                    mdServerET.getDescription()));
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

    public Builder appendLogForVisitTypesWithDiferrentIdsAndEqualUUID(
        Map<String, List<VisitTypeDTO>> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Updated Visit Types With different ID and equal UUID");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (String key : data.keySet()) {
          List<VisitTypeDTO> dtos = data.get(key);
          VisitType mdServerET = dtos.get(0).getVisitType();
          VisitType pdServerET = dtos.get(1).getVisitType();
          try {
            printer.print(
                String.format(
                    "ProductionServer with VisitType NAME:'%s' and UUID:%s updated ID from {%s} to {%s}",
                    pdServerET.getName(),
                    pdServerET.getUuid(),
                    pdServerET.getId(),
                    mdServerET.getId()));
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

    public Builder appendNewMappedVisitTypes(Map<VisitType, VisitType> mapVisitTypes) {

      try {
        printer.print("Metadata Harmonization Process Flow: Added new VisitType Mappings ");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (Entry<VisitType, VisitType> entry : mapVisitTypes.entrySet()) {

          VisitType pdsServer = entry.getKey();
          VisitType mdsServer = entry.getValue();
          try {
            printer.print(
                String.format(
                    "VisitType ID:{%s},NAME:'%s',DESCRIPTION:'%s',UUID:%s  Updated to VisitType ID:{%s},NAME:'%s',DESCRIPTION:'%s',UUID:%s ",
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

    public VisitTypeHarmonizationCSVLog build() {
      try {
        FileOutputStream fos = new FileOutputStream(new File("harmonizationVisitTypeLog"));
        outputStream.writeTo(fos);
        outputStream.close();
        fos.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        throw new APIException("Unable To Append the log file'harmonizationVisitTypeLog'");
      } catch (IOException e) {
        e.printStackTrace();
        throw new APIException("Unable To Append the log file'harmonizationVisitTypeLog'");
      }
      return new VisitTypeHarmonizationCSVLog();
    }
  }

  private VisitTypeHarmonizationCSVLog() {}

  private static ByteArrayOutputStream getByteArrayOutputStream() {

    ByteArrayOutputStream outputStream = null;
    try {
      File file = new File("harmonizationVisitTypeLog");
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

  public static ByteArrayOutputStream exportVisitTypeLogs(
      String defaultLocationName,
      List<VisitTypeDTO> visitTypesForReview,
      List<VisitType> existingVisitTypes) {

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
      printer.print("Metadata Harmonization Process Flow: Export Visit Types for Analysis ");
      printer.println();
      printer.print(
          "===============================================================================================================================");
      printer.println();
      printer.println();
      List<VisitType> visitTypes = DTOUtils.fromVisitTypeDTOs(visitTypesForReview);
      printVisitTypes(printer, visitTypes);
      printer.println();
      printer.println();

      printer.print("Existing Visit Types ");
      printer.println();
      printer.print(
          "===============================================================================================================================");
      printer.println();
      printer.println();
      printVisitTypes(printer, existingVisitTypes);
      printer.flush();
    } catch (Exception e) {
      e.printStackTrace();
      throw new APIException("Unable to build OutputStream for CSV: " + e.getMessage());
    }
    return outputStream;
  }

  private static CSVPrinter printVisitTypes(CSVPrinter printer, List<VisitType> visitTypes) {
    try {
      printer.print("ID, NAME, DESCRIPTION, UUID");
      printer.println();
      for (VisitType visitType : visitTypes) {
        printer.print(
            String.format(
                "%s, %s, %s, %s",
                visitType.getId(),
                visitType.getName(),
                visitType.getDescription(),
                visitType.getUuid()));
        printer.println();
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new APIException("Unable to write record to CSV: " + e.getMessage());
    }
    return printer;
  }
}
