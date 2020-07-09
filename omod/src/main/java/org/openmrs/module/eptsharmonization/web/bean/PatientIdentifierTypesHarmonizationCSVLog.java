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
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.APIException;
import org.openmrs.module.eptsharmonization.api.model.PatientIdentifierTypeDTO;

public class PatientIdentifierTypesHarmonizationCSVLog {

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

    public Builder appendLogForNewHarmonizedFromMDSPatientIdentifierTypes(
        List<PatientIdentifierTypeDTO> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Added New Patient Identifier Types from Metadata Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (PatientIdentifierTypeDTO item : data) {
          try {
            printer.print(
                String.format(
                    "PatientIdentifierType ID:{%s}, UUID:%s, NAME:'%s', DESCRIPTION:'%s'",
                    item.getPatientIdentifierType().getId(),
                    item.getPatientIdentifierType().getUuid(),
                    item.getPatientIdentifierType().getName(),
                    item.getPatientIdentifierType().getDescription()));
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

    public Builder appendLogForDeleteFromProductionServer(List<PatientIdentifierTypeDTO> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Deleted Patient Identifier Types in Production Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (PatientIdentifierTypeDTO item : data) {
          try {
            printer.print(
                String.format(
                    "PatientIdentifierType ID:{%s}, UUID:%s, NAME:'%s', DESCRIPTION:'%s'",
                    item.getPatientIdentifierType().getId(),
                    item.getPatientIdentifierType().getUuid(),
                    item.getPatientIdentifierType().getName(),
                    item.getPatientIdentifierType().getDescription()));
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

    public Builder appendLogForUpdatedPatientIdentifierTypesNames(
        Map<String, List<PatientIdentifierTypeDTO>> data) {

      try {
        printer.print("Metadata Harmonization Process Flow: Updated Patient Identifier Type Names");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (String key : data.keySet()) {
          List<PatientIdentifierTypeDTO> dtos = data.get(key);
          PatientIdentifierType mdServerET = dtos.get(0).getPatientIdentifierType();
          PatientIdentifierType pdServerET = dtos.get(1).getPatientIdentifierType();
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

    public Builder appendLogForPatientIdentifierTypesWithDiferrentIdsAndEqualUUID(
        Map<String, List<PatientIdentifierTypeDTO>> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Updated Patient Identifier Types With different ID and equal UUID");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (String key : data.keySet()) {
          List<PatientIdentifierTypeDTO> dtos = data.get(key);
          PatientIdentifierType mdServerET = dtos.get(0).getPatientIdentifierType();
          PatientIdentifierType pdServerET = dtos.get(1).getPatientIdentifierType();
          try {
            printer.print(
                String.format(
                    "ProductionServer with PatientIdentifierType NAME:'%s' and UUID:%s updated ID from {%s} to {%s}",
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

    public Builder appendNewMappedPatientIdentifierTypes(
        Map<PatientIdentifierType, PatientIdentifierType> mapPatientIdentifierTypes) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Added new PatientIdentifierType Mappings ");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (Entry<PatientIdentifierType, PatientIdentifierType> entry :
            mapPatientIdentifierTypes.entrySet()) {

          PatientIdentifierType pdsServer = entry.getKey();
          PatientIdentifierType mdsServer = entry.getValue();
          try {
            printer.print(
                String.format(
                    "PatientIdentifierType ID:{%s},NAME:'%s',DESCRIPTION:'%s',UUID:%s  Updated to PatientIdentifierType ID:{%s},NAME:'%s',DESCRIPTION:'%s',UUID:%s ",
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

    public PatientIdentifierTypesHarmonizationCSVLog build() {
      try {
        FileOutputStream fos =
            new FileOutputStream(new File("harmonizationPatientIdentifierTypesLog"));
        outputStream.writeTo(fos);
        outputStream.close();
        fos.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        throw new APIException(
            "Unable To Append the log file'harmonizationPatientIdentifierTypesLog'");
      } catch (IOException e) {
        e.printStackTrace();
        throw new APIException(
            "Unable To Append the log file'harmonizationPatientIdentifierTypesLog'");
      }
      return new PatientIdentifierTypesHarmonizationCSVLog();
    }
  }

  private PatientIdentifierTypesHarmonizationCSVLog() {}

  private static ByteArrayOutputStream getByteArrayOutputStream() {

    ByteArrayOutputStream outputStream = null;
    try {
      File file = new File("harmonizationPatientIdentifierTypesLog");
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

  public static ByteArrayOutputStream exportPatientIdentifierTypeLogs(
      String defaultLocationName, List<PatientIdentifierTypeDTO> data) {

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
          "Metadata Harmonization Process Flow: Export Patient Identifier Types for Analysis ");
      printer.println();
      printer.print(
          "===============================================================================================================================");
      printer.println();
      printer.println();

      for (PatientIdentifierTypeDTO dto : data) {

        PatientIdentifierType patientIdentifierType = dto.getPatientIdentifierType();
        try {
          printer.print(
              String.format(
                  "ID:%s, NAME:'%s', DESCRIPTION:'%s', UUID:%s",
                  patientIdentifierType.getId(),
                  patientIdentifierType.getName(),
                  patientIdentifierType.getDescription(),
                  patientIdentifierType.getUuid()));
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
