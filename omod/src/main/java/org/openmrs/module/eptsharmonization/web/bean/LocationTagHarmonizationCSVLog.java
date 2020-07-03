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
import org.openmrs.LocationTag;
import org.openmrs.api.APIException;
import org.openmrs.module.eptsharmonization.api.model.LocationTagDTO;

public class LocationTagHarmonizationCSVLog {

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

    public Builder appendLogForNewHarmonizedFromMDSLocationTags(List<LocationTagDTO> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Added New Location Tags from Metadata Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (LocationTagDTO item : data) {
          try {
            printer.print(
                String.format(
                    "LocationTag ID:{%s}, UUID:%s, NAME:'%s', DESCRIPTION:'%s'",
                    item.getLocationTag().getId(),
                    item.getLocationTag().getUuid(),
                    item.getLocationTag().getName(),
                    item.getLocationTag().getDescription()));
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

    public Builder appendLogForDeleteFromProductionServer(List<LocationTagDTO> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Deleted Location Tags in Production Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (LocationTagDTO item : data) {
          try {
            printer.print(
                String.format(
                    "LocationTag ID:{%s}, UUID:%s, NAME:'%s', DESCRIPTION:'%s'",
                    item.getLocationTag().getId(),
                    item.getLocationTag().getUuid(),
                    item.getLocationTag().getName(),
                    item.getLocationTag().getDescription()));
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

    public Builder appendLogForUpdatedLocationTagNames(Map<String, List<LocationTagDTO>> data) {

      try {
        printer.print("Metadata Harmonization Process Flow: Updated Location Tag Names");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (String key : data.keySet()) {
          List<LocationTagDTO> dtos = data.get(key);
          LocationTag mdServerET = dtos.get(0).getLocationTag();
          LocationTag pdServerET = dtos.get(1).getLocationTag();
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

    public Builder appendLogForLocationTagsWithDiferrentIdsAndEqualUUID(
        Map<String, List<LocationTagDTO>> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Updated Location Tags With different ID and equal UUID");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (String key : data.keySet()) {
          List<LocationTagDTO> dtos = data.get(key);
          LocationTag mdServerLT = dtos.get(0).getLocationTag();
          LocationTag pdServerLT = dtos.get(1).getLocationTag();
          try {
            printer.print(
                String.format(
                    "ProductionServer with LocationTag NAME:'%s' and UUID:%s updated ID from {%s} to {%s}",
                    pdServerLT.getName(),
                    pdServerLT.getUuid(),
                    pdServerLT.getId(),
                    mdServerLT.getId()));
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

    public Builder appendNewMappedLocationTags(Map<LocationTag, LocationTag> mapLocationTags) {

      try {
        printer.print("Metadata Harmonization Process Flow: Added new LocationTag Mappings ");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (Entry<LocationTag, LocationTag> entry : mapLocationTags.entrySet()) {

          LocationTag pdsServer = entry.getKey();
          LocationTag mdsServer = entry.getValue();
          try {
            printer.print(
                String.format(
                    "LocationTag ID:{%s},NAME:'%s',DESCRIPTION:'%s',UUID:%s  Updated to LocationTag ID:{%s},NAME:'%s',DESCRIPTION:'%s',UUID:%s ",
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

    public LocationTagHarmonizationCSVLog build() {
      try {
        FileOutputStream fos = new FileOutputStream(new File("harmonizationLocationTagLog"));
        outputStream.writeTo(fos);
        outputStream.close();
        fos.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        throw new APIException("Unable To Append the log file'harmonizationLocationTagLog'");
      } catch (IOException e) {
        e.printStackTrace();
        throw new APIException("Unable To Append the log file'harmonizationLocationTagLog'");
      }
      return new LocationTagHarmonizationCSVLog();
    }
  }

  private LocationTagHarmonizationCSVLog() {}

  private static ByteArrayOutputStream getByteArrayOutputStream() {

    ByteArrayOutputStream outputStream = null;
    try {
      File file = new File("harmonizationLocationTagLog");
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

  public static ByteArrayOutputStream exportLocationTagLogs(
      String defaultLocationName, List<LocationTagDTO> data) {

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
      printer.print("Metadata Harmonization Process Flow: Export Location Tags for Analysis ");
      printer.println();
      printer.print(
          "===============================================================================================================================");
      printer.println();
      printer.println();

      for (LocationTagDTO dto : data) {

        LocationTag locationTag = dto.getLocationTag();
        try {
          printer.print(
              String.format(
                  "ID:%s, NAME:'%s', DESCRIPTION:'%s', UUID:%s",
                  locationTag.getId(),
                  locationTag.getName(),
                  locationTag.getDescription(),
                  locationTag.getUuid()));
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
