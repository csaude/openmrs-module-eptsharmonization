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
import org.openmrs.RelationshipType;
import org.openmrs.api.APIException;
import org.openmrs.module.eptsharmonization.api.model.RelationshipTypeDTO;

public class RelationshipTypeHarmonizationCSVLog {

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

    public Builder appendLogForNewHarmonizedFromMDSRelationshipTypes(
        List<RelationshipTypeDTO> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Added New Relationship Types from Metadata Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (RelationshipTypeDTO item : data) {
          try {
            printer.print(
                String.format(
                    "RelationshipType ID:{%s}, UUID:%s, A_IS_TO_B:'%s', B_IS_TO_A: '%s', DESCRIPTION:'%s'",
                    item.getRelationshipType().getId(),
                    item.getRelationshipType().getUuid(),
                    item.getRelationshipType().getaIsToB(),
                    item.getRelationshipType().getbIsToA(),
                    item.getRelationshipType().getDescription()));
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

    public Builder appendLogForDeleteFromProductionServer(List<RelationshipTypeDTO> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Deleted Relationship Types in Production Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (RelationshipTypeDTO item : data) {
          try {
            printer.print(
                String.format(
                    "RelationshipType ID:{%s}, UUID:%s, A_IS_TO_B:'%s', B_IS_TO_A: '%s', DESCRIPTION:'%s'",
                    item.getRelationshipType().getId(),
                    item.getRelationshipType().getUuid(),
                    item.getRelationshipType().getaIsToB(),
                    item.getRelationshipType().getbIsToA(),
                    item.getRelationshipType().getDescription()));
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

    public Builder appendLogForUpdatedRelationshipNames(
        Map<String, List<RelationshipTypeDTO>> data) {

      try {
        printer.print("Metadata Harmonization Process Flow: Updated Relationship Type Names");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (String key : data.keySet()) {
          List<RelationshipTypeDTO> dtos = data.get(key);
          RelationshipType mdServerET = dtos.get(0).getRelationshipType();
          RelationshipType pdServerET = dtos.get(1).getRelationshipType();
          try {
            printer.print(
                String.format(
                    "ID:{%s}, UUID:%s, updated A_IS_TO_B from '%s' to '%s', B_IS_TO_A from '%s' to '%s', DESCRIPTION from '%s' to '%s', and RETIRED from '%s' to '%s'",
                    mdServerET.getId(),
                    mdServerET.getUuid(),
                    pdServerET.getaIsToB(),
                    mdServerET.getaIsToB(),
                    pdServerET.getbIsToA(),
                    mdServerET.getbIsToA(),
                    pdServerET.getDescription(),
                    mdServerET.getDescription(),
                    pdServerET.isRetired(),
                    mdServerET.isRetired()));
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

    public Builder appendLogForRelationshipTypesWithDiferrentIdsAndEqualUUID(
        Map<String, List<RelationshipTypeDTO>> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Updated Relationship Types With different ID and equal UUID");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (String key : data.keySet()) {
          List<RelationshipTypeDTO> dtos = data.get(key);
          RelationshipType mdServerET = dtos.get(0).getRelationshipType();
          RelationshipType pdServerET = dtos.get(1).getRelationshipType();
          try {
            printer.print(
                String.format(
                    "ProductionServer with RelationshipType NAME:'%s' and UUID:%s updated ID from {%s} to {%s}",
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

    public Builder appendNewMappedRelationshipTypes(
        Map<RelationshipType, RelationshipType> mapRelationshipTypes) {

      try {
        printer.print("Metadata Harmonization Process Flow: Added new RelationshipType Mappings ");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (Entry<RelationshipType, RelationshipType> entry : mapRelationshipTypes.entrySet()) {

          RelationshipType pdsServer = entry.getKey();
          RelationshipType mdsServer = entry.getValue();
          try {
            printer.print(
                String.format(
                    "RelationshipType ID:{%s}, A_IS_TO_B:'%s', B_IS_TO_A:'%s', DESCRIPTION:'%s',UUID:%s  Updated to RelationshipType ID:{%s}, A_IS_TO_B:'%s', B_IS_TO_A:'%s', DESCRIPTION:'%s',UUID:%s ",
                    pdsServer.getId(),
                    pdsServer.getaIsToB(),
                    pdsServer.getbIsToA(),
                    pdsServer.getDescription(),
                    pdsServer.getUuid(),
                    mdsServer.getId(),
                    mdsServer.getaIsToB(),
                    mdsServer.getbIsToA(),
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

    public RelationshipTypeHarmonizationCSVLog build() {
      try {
        FileOutputStream fos = new FileOutputStream(new File("harmonizationRelationshipTypeLog"));
        outputStream.writeTo(fos);
        outputStream.close();
        fos.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        throw new APIException("Unable To Append the log file'harmonizationRelationshipTypeLog'");
      } catch (IOException e) {
        e.printStackTrace();
        throw new APIException("Unable To Append the log file'harmonizationRelationshipTypeLog'");
      }
      return new RelationshipTypeHarmonizationCSVLog();
    }
  }

  private RelationshipTypeHarmonizationCSVLog() {}

  private static ByteArrayOutputStream getByteArrayOutputStream() {

    ByteArrayOutputStream outputStream = null;
    try {
      File file = new File("harmonizationRelationshipTypeLog");
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

  public static ByteArrayOutputStream exportRelationshipTypeLogs(
      String defaultLocationName, List<RelationshipTypeDTO> data) {

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
      printer.print("Metadata Harmonization Process Flow: Export Relationship Types for Analysis ");
      printer.println();
      printer.print(
          "===============================================================================================================================");
      printer.println();
      printer.println();

      for (RelationshipTypeDTO dto : data) {

        RelationshipType relationshipType = dto.getRelationshipType();
        try {
          printer.print(
              String.format(
                  "ID:%s, A_IS_TO_B:'%s', B_IS_TO_A:'%s', DESCRIPTION:'%s', UUID:%s",
                  relationshipType.getId(),
                  relationshipType.getaIsToB(),
                  relationshipType.getbIsToA(),
                  relationshipType.getDescription(),
                  relationshipType.getUuid()));
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
