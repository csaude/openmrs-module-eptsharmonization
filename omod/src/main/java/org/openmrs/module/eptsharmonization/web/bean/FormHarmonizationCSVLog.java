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
import org.openmrs.Form;
import org.openmrs.api.APIException;
import org.openmrs.module.eptsharmonization.api.model.FormDTO;
import org.openmrs.module.eptsharmonization.api.model.HtmlForm;

public class FormHarmonizationCSVLog {

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

    public Builder appendLogForNewHarmonizedFromMDSForms(List<FormDTO> data) {

      try {
        printer.print("Metadata Harmonization Process Flow: Added New Form from Metadata Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (FormDTO item : data) {
          try {
            printer.print(
                String.format(
                    "Form ID:{%s}, NAME:'%s', UUID:%s, DESCRIPTION:'%s', EncounterType ID:{%s}, EncounterType Name: '%s'",
                    item.getForm().getId(),
                    item.getForm().getName(),
                    item.getForm().getUuid(),
                    item.getForm().getDescription(),
                    item.getForm().getEncounterType().getName(),
                    item.getForm().getEncounterType().getDescription()));
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

    public Builder appendLogForDeleteFormFromProductionServer(List<FormDTO> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Deleted unused Forms in Production Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (FormDTO item : data) {
          try {
            printer.print(
                String.format(
                    "Form ID:{%s}, UUID:%s, NAME:'%s', DESCRIPTION:'%s'",
                    item.getForm().getId(),
                    item.getForm().getUuid(),
                    item.getForm().getName(),
                    item.getForm().getDescription()));
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

    public Builder appendLogForUpdatedFormNames(Map<String, List<FormDTO>> data) {

      try {
        printer.print("Metadata Harmonization Process Flow: Updated Form Names");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (String key : data.keySet()) {
          List<FormDTO> dtos = data.get(key);
          Form mdServer = dtos.get(0).getForm();
          Form pdServer = dtos.get(1).getForm();
          try {
            printer.print(
                String.format(
                    "ID:{%s}, UUID:%s, updated NAME from '%s' to '%s', and encounter type from '%s' to '%s'",
                    pdServer.getId(),
                    pdServer.getUuid(),
                    pdServer.getName(),
                    mdServer.getName(),
                    pdServer.getEncounterType().getName(),
                    mdServer.getEncounterType().getName()));
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

    public Builder appendLogForFormWithDiferrentIdsAndEqualUUID(Map<String, List<FormDTO>> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Updated Forms With different ID and equal UUID");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (String key : data.keySet()) {
          List<FormDTO> dtos = data.get(key);
          Form mdServer = dtos.get(0).getForm();
          Form pdServer = dtos.get(1).getForm();
          try {
            printer.print(
                String.format(
                    "ProductionServer with Form NAME:'%s' and UUID:%s updated ID from {%s} to {%s} and Encounter Type from '%s' to '%s' ",
                    pdServer.getName(),
                    pdServer.getUuid(),
                    pdServer.getId(),
                    mdServer.getId(),
                    pdServer.getEncounterType().getName(),
                    mdServer.getEncounterType().getName()));
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

    public Builder appendLogForHtmlFormStep1(Map<String, List<HtmlForm>> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Updated Html Forms from Metadata Server with Different Form Name and equal UUID To Production Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (String key : data.keySet()) {
          List<HtmlForm> dtos = data.get(key);
          HtmlForm mdServer = dtos.get(0);
          HtmlForm pdServer = dtos.get(1);
          try {
            printer.print(
                String.format(
                    "Html Form with ID:{%s} and UUID:%s updated Form ID from {%s} to {%s} ",
                    pdServer.getId(),
                    pdServer.getUuid(),
                    pdServer.getForm().getId(),
                    mdServer.getForm().getId()));
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

    public Builder appendLogForHtmlFormStep2(List<HtmlForm> data) {

      try {
        printer.print(
            "Metadata Harmonization Process Flow: Added New Html Forms from Metadata Server");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (HtmlForm item : data) {

          try {
            printer.print(
                String.format(
                    "Html Form ID: {%s}, Form_ID: {%s}, FORM_Name: '%s, UUID:%s ",
                    item.getId(),
                    item.getForm().getId(),
                    item.getForm().getName(),
                    item.getUuid()));
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

    public Builder appendNewMappedForms(Map<Form, Form> mapForms) {

      try {
        printer.print("Metadata Harmonization Process Flow: Added new Forms Mappings ");
        printer.println();
        printer.print(
            "===============================================================================================================================");
        printer.println();

        for (Entry<Form, Form> entry : mapForms.entrySet()) {

          Form pdsServer = entry.getKey();
          Form mdsServer = entry.getValue();
          try {
            printer.print(
                String.format(
                    "Form ID:{%s},NAME:'%s',DESCRIPTION:'%s',UUID:%s  Updated to Form ID:{%s},NAME:'%s',DESCRIPTION:'%s',UUID:%s ",
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

    public FormHarmonizationCSVLog build() {
      try {
        FileOutputStream fos = new FileOutputStream(new File("harmonizationFormLog"));
        outputStream.writeTo(fos);
        outputStream.close();
        fos.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        throw new APIException("Unable To Append the log harmonizationFormLog'");
      } catch (IOException e) {
        e.printStackTrace();
        throw new APIException("Unable To Append the log harmonizationFormLog'");
      }
      return new FormHarmonizationCSVLog();
    }
  }

  private FormHarmonizationCSVLog() {}

  private static ByteArrayOutputStream getByteArrayOutputStream() {

    ByteArrayOutputStream outputStream = null;
    try {
      File file = new File("harmonizationFormLog");
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

  public static ByteArrayOutputStream exportFormLogs(
      String defaultLocationName, List<FormDTO> data, List<Form> metadataServerForms) {

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
      printer.print("Metadata Harmonization Process Flow: Export Forms for Analysis ");
      printer.println();
      printer.print(
          "===============================================================================================================================");
      printer.println();
      printer.println();

      for (FormDTO dto : data) {

        Form form = dto.getForm();
        try {
          printer.print(
              String.format(
                  "ID:%s, NAME:'%s', DESCRIPTION:'%s', UUID:%s, Encounter Type ID:%s, Encounter Type NAME:'%s'",
                  form.getId(),
                  form.getName(),
                  form.getDescription(),
                  form.getUuid(),
                  form.getEncounterType().getId(),
                  form.getEncounterType().getName()));
          printer.println();
        } catch (Exception e) {
          e.printStackTrace();
          throw new APIException("Unable to write record to CSV: " + e.getMessage());
        }
      }

      printer.println();
      printer.println();
      printer.print("Forms From Metadata Server ");
      printer.println();
      printer.print(
          "===============================================================================================================================");
      printer.println();

      for (Form form : metadataServerForms) {

        try {
          printer.print(
              String.format(
                  "ID:%s, NAME:'%s', DESCRIPTION:'%s', UUID:%s, Encounter Type ID:%s, Encounter Type NAME:'%s'",
                  form.getId(),
                  form.getName(),
                  form.getDescription(),
                  form.getUuid(),
                  form.getEncounterType().getId(),
                  form.getEncounterType().getName()));
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
