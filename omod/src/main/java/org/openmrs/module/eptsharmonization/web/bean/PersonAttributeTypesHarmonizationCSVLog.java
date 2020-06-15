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
import org.openmrs.PersonAttributeType;
import org.openmrs.api.APIException;
import org.openmrs.module.eptsharmonization.api.model.PersonAttributeTypeDTO;

public class PersonAttributeTypesHarmonizationCSVLog {

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

    public Builder appendLogForNewHarmonizedFromMDSPersonAttributeTypes(
        List<PersonAttributeTypeDTO> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Added New Person Attribute Types from Metadata Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (PersonAttributeTypeDTO item : data) {
          try {
            printer.print(
                String.format(
                    "PersonAttributeType ID:{%s}, UUID:%s, NAME:'%s', DESCRIPTION:'%s'",
                    item.getPersonAttributeType().getId(),
                    item.getPersonAttributeType().getUuid(),
                    item.getPersonAttributeType().getName(),
                    item.getPersonAttributeType().getDescription()));
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

    public Builder appendLogForDeleteFromProductionServer(List<PersonAttributeTypeDTO> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Deleted Person Attribute Types in Production Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (PersonAttributeTypeDTO item : data) {
          try {
            printer.print(
                String.format(
                    "PersonAttributeType ID:{%s}, UUID:%s, NAME:'%s', DESCRIPTION:'%s'",
                    item.getPersonAttributeType().getId(),
                    item.getPersonAttributeType().getUuid(),
                    item.getPersonAttributeType().getName(),
                    item.getPersonAttributeType().getDescription()));
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

    public Builder appendLogForUpdatedPersonAttributeTypesNames(
        Map<String, List<PersonAttributeTypeDTO>> data) {

      try {
        printer.print("Metadata Harmonization Process Flow: Updated Person Attribute Type Names");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (String key : data.keySet()) {
          List<PersonAttributeTypeDTO> dtos = data.get(key);
          PersonAttributeType mdServerET = dtos.get(0).getPersonAttributeType();
          PersonAttributeType pdServerET = dtos.get(1).getPersonAttributeType();
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

    public Builder appendLogForPersonAttributeTypesWithDiferrentIdsAndEqualUUID(
        Map<String, List<PersonAttributeTypeDTO>> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Updated Person Attribute Types With different ID and equal UUID");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (String key : data.keySet()) {
          List<PersonAttributeTypeDTO> dtos = data.get(key);
          PersonAttributeType mdServerET = dtos.get(0).getPersonAttributeType();
          PersonAttributeType pdServerET = dtos.get(1).getPersonAttributeType();
          try {
            printer.print(
                String.format(
                    "ProductionServer with PersonAttributeType NAME:'%s' and UUID:%s updated ID from {%s} to {%s}",
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

    public Builder appendNewMappedPersonAttributeTypes(
        Map<PersonAttributeType, PersonAttributeType> mapPersonAttributeTypes) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Added new PersonAttributeType Mappings ");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (Entry<PersonAttributeType, PersonAttributeType> entry :
            mapPersonAttributeTypes.entrySet()) {

          PersonAttributeType pdsServer = entry.getKey();
          PersonAttributeType mdsServer = entry.getValue();
          try {
            printer.print(
                String.format(
                    "PersonAttributeType ID:{%s},NAME:'%s',DESCRIPTION:'%s',UUID:%s  Updated to PersonAttributeType ID:{%s},NAME:'%s',DESCRIPTION:'%s',UUID:%s ",
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

    public PersonAttributeTypesHarmonizationCSVLog build() {
      try {
        FileOutputStream fos =
            new FileOutputStream(new File("harmonizationPersonAttributeTypesLog"));
        outputStream.writeTo(fos);
        outputStream.close();
        fos.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        throw new APIException(
            "Unable To Append the log file'harmonizationPersonAttributeTypesLog'");
      } catch (IOException e) {
        e.printStackTrace();
        throw new APIException(
            "Unable To Append the log file'harmonizationPersonAttributeTypesLog'");
      }
      return new PersonAttributeTypesHarmonizationCSVLog();
    }
  }

  private PersonAttributeTypesHarmonizationCSVLog() {}

  private static ByteArrayOutputStream getByteArrayOutputStream() {

    ByteArrayOutputStream outputStream = null;
    try {
      File file = new File("harmonizationPersonAttributeTypesLog");
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

  public static ByteArrayOutputStream exportPersonAttributeTypeLogs(
      String defaultLocationName, List<PersonAttributeTypeDTO> data) {

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
          "Metadata Harmonization Process Flow: Export Person Attribute Types for Analysis ");
      printer.println();
      printer.print(
          "===============================================================================================================================");
      printer.println();
      printer.println();

      for (PersonAttributeTypeDTO dto : data) {

        PersonAttributeType encounterType = dto.getPersonAttributeType();
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
