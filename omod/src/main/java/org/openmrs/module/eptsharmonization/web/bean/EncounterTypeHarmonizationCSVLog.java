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
import org.openmrs.EncounterType;
import org.openmrs.api.APIException;
import org.openmrs.module.eptsharmonization.api.model.EncounterTypeDTO;

public class EncounterTypeHarmonizationCSVLog {

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

    public Builder appendLogForNewHarmonizedFromMDSEncounterTypes(List<EncounterTypeDTO> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Added New Encounter Types from Metadata Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (EncounterTypeDTO item : data) {
          try {
            printer.print(
                String.format(
                    "EncounterType ID:{%s}, UUID:%s, NAME:'%s', DESCRIPTION:'%s'",
                    item.getEncounterType().getId(),
                    item.getEncounterType().getUuid(),
                    item.getEncounterType().getName(),
                    item.getEncounterType().getDescription()));
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

    public Builder appendLogForDeleteFromProductionServer(List<EncounterTypeDTO> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Encounter Types deleted in Production Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (EncounterTypeDTO item : data) {
          try {
            printer.print(
                String.format(
                    "EncounterType ID:{%s}, UUID:%s, NAME:'%s', DESCRIPTION:'%s'",
                    item.getEncounterType().getId(),
                    item.getEncounterType().getUuid(),
                    item.getEncounterType().getName(),
                    item.getEncounterType().getDescription()));
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

    public Builder appendLogForUpdatedEncounterNames(Map<String, List<EncounterTypeDTO>> data) {

      try {
        printer.print("Metadata Harmonization Process Flow: Updated Encounter Type Names");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (String key : data.keySet()) {
          List<EncounterTypeDTO> dtos = data.get(key);
          EncounterType mdServerET = dtos.get(0).getEncounterType();
          EncounterType pdServerET = dtos.get(1).getEncounterType();
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

    public Builder appendLogForEncounterTypesWithDiferrentIdsAndEqualUUID(
        Map<String, List<EncounterTypeDTO>> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Encounter Types With different ID and equal UUID");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (String key : data.keySet()) {
          List<EncounterTypeDTO> dtos = data.get(key);
          EncounterType mdServerET = dtos.get(0).getEncounterType();
          EncounterType pdServerET = dtos.get(1).getEncounterType();
          try {
            printer.print(
                String.format(
                    "ProductionServer with EncounterType NAME:'%s' and UUID:%s updated ID from {%s} to {%s}",
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

    public Builder appendNewMappedEncounterTypes(
        Map<EncounterType, EncounterType> mapEncounterTypes) {

      try {
        printer.print("Metadata Harmonization Process Flow: Added new EncounterType Mappings ");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (Entry<EncounterType, EncounterType> entry : mapEncounterTypes.entrySet()) {

          EncounterType mdsServer = entry.getKey();
          EncounterType pdsServer = entry.getValue();
          try {
            printer.print(
                String.format(
                    "EncounterType ID:{%s},NAME:'%s',DESCRIPTION:'%s',UUID:%s  Updated to EncounterType ID:{%s},NAME:'%s',DESCRIPTION:'%s',UUID:%s ",
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

    public EncounterTypeHarmonizationCSVLog build() {
      try {
        FileOutputStream fos = new FileOutputStream(new File("harmonizationEncounterTypeLog"));
        outputStream.writeTo(fos);
        outputStream.close();
        fos.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        throw new APIException("Unable To Append the log file'harmonizationEncounterTypeLog'");
      } catch (IOException e) {
        e.printStackTrace();
        throw new APIException("Unable To Append the log file'harmonizationEncounterTypeLog'");
      }
      return new EncounterTypeHarmonizationCSVLog();
    }
  }

  private EncounterTypeHarmonizationCSVLog() {}

  private static ByteArrayOutputStream getByteArrayOutputStream() {

    ByteArrayOutputStream outputStream = null;
    try {
      File file = new File("harmonizationEncounterTypeLog");
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

  public static ByteArrayOutputStream exportEncounterTypeLogs(
      String defaultLocationName, List<EncounterTypeDTO> data) {

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
      printer.print("Metadata Harmonization Process Flow: Export Encounter Types for Analysis ");
      printer.println();
      printer.print(
          "===============================================================================================================================");
      printer.println();
      printer.println();

      for (EncounterTypeDTO dto : data) {

        EncounterType encounterType = dto.getEncounterType();
        try {
          printer.print(
              String.format(
                  "ID:%s, NAME:'%s', DESCRIPTION:'%s', UUID:%s",
                  encounterType.getId(),
                  encounterType.getName(),
                  encounterType.getDescription(),
                  encounterType.getUuid()));
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
