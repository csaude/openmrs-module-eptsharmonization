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
import org.openmrs.LocationAttributeType;
import org.openmrs.api.APIException;
import org.openmrs.module.eptsharmonization.api.model.LocationAttributeTypeDTO;

public class LocationAttributeTypeHarmonizationCSVLog {

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

    public Builder appendLogForNewHarmonizedFromMDSLocationAttributeTypes(
        List<LocationAttributeTypeDTO> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Added New Location Attribute Types from Metadata Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (LocationAttributeTypeDTO item : data) {
          try {
            printer.print(
                String.format(
                    "LocationAttributeType ID:{%s}, UUID:%s, NAME:'%s', DESCRIPTION:'%s'",
                    item.getLocationAttributeType().getId(),
                    item.getLocationAttributeType().getUuid(),
                    item.getLocationAttributeType().getName(),
                    item.getLocationAttributeType().getDescription()));
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

    public Builder appendLogForDeleteFromProductionServer(List<LocationAttributeTypeDTO> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Deleted Location Attribute Types in Production Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (LocationAttributeTypeDTO item : data) {
          try {
            printer.print(
                String.format(
                    "LocationAttributeType ID:{%s}, UUID:%s, NAME:'%s', DESCRIPTION:'%s'",
                    item.getLocationAttributeType().getId(),
                    item.getLocationAttributeType().getUuid(),
                    item.getLocationAttributeType().getName(),
                    item.getLocationAttributeType().getDescription()));
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

    public Builder appendLogForUpdatedLocationAttributeDetails(
        Map<String, List<LocationAttributeTypeDTO>> data) {

      try {
        printer.print("Metadata Harmonization Process Flow: Updated Location Attribute Type Names");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (String key : data.keySet()) {
          List<LocationAttributeTypeDTO> dtos = data.get(key);
          LocationAttributeType mdServerET = dtos.get(0).getLocationAttributeType();
          LocationAttributeType pdServerET = dtos.get(1).getLocationAttributeType();
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

    public Builder appendLogForLocationAttributeTypesWithDiferrentIdsAndEqualUUID(
        Map<String, List<LocationAttributeTypeDTO>> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Updated Location Attribute Types With different ID and equal UUID");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (String key : data.keySet()) {
          List<LocationAttributeTypeDTO> dtos = data.get(key);
          LocationAttributeType mdServerET = dtos.get(0).getLocationAttributeType();
          LocationAttributeType pdServerET = dtos.get(1).getLocationAttributeType();
          try {
            printer.print(
                String.format(
                    "ProductionServer with LocationAttributeType NAME:'%s' and UUID:%s updated ID from {%s} to {%s}",
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

    public Builder appendNewMappedLocationAttributeTypes(
        Map<LocationAttributeType, LocationAttributeType> mapLocationAttributeTypes) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Added new LocationAttributeType Mappings ");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (Entry<LocationAttributeType, LocationAttributeType> entry :
            mapLocationAttributeTypes.entrySet()) {

          LocationAttributeType pdsServer = entry.getKey();
          LocationAttributeType mdsServer = entry.getValue();
          try {
            printer.print(
                String.format(
                    "LocationAttributeType ID:{%s},NAME:'%s',DESCRIPTION:'%s',UUID:%s  Updated to LocationAttributeType ID:{%s},NAME:'%s',DESCRIPTION:'%s',UUID:%s ",
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

    public LocationAttributeTypeHarmonizationCSVLog build() {
      try {
        FileOutputStream fos =
            new FileOutputStream(new File("harmonizationLocationAttributeTypeLog"));
        outputStream.writeTo(fos);
        outputStream.close();
        fos.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        throw new APIException(
            "Unable To Append the log file'harmonizationLocationAttributeTypeLog'");
      } catch (IOException e) {
        e.printStackTrace();
        throw new APIException(
            "Unable To Append the log file'harmonizationLocationAttributeTypeLog'");
      }
      return new LocationAttributeTypeHarmonizationCSVLog();
    }
  }

  private LocationAttributeTypeHarmonizationCSVLog() {}

  private static ByteArrayOutputStream getByteArrayOutputStream() {

    ByteArrayOutputStream outputStream = null;
    try {
      File file = new File("harmonizationLocationAttributeTypeLog");
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

  public static ByteArrayOutputStream exportLocationAttributeTypeLogs(
      String defaultLocationName, List<LocationAttributeTypeDTO> data) {

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
          "Metadata Harmonization Process Flow: Export Location Attribute Types for Analysis ");
      printer.println();
      printer.print(
          "===============================================================================================================================");
      printer.println();
      printer.println();

      for (LocationAttributeTypeDTO dto : data) {

        LocationAttributeType locationAttributeType = dto.getLocationAttributeType();
        try {
          printer.print(
              String.format(
                  "ID:%s, NAME:'%s', DESCRIPTION:'%s', UUID:%s",
                  locationAttributeType.getId(),
                  locationAttributeType.getName(),
                  locationAttributeType.getDescription(),
                  locationAttributeType.getUuid()));
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
