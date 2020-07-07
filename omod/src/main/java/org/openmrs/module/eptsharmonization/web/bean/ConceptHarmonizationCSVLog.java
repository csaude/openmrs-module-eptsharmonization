package org.openmrs.module.eptsharmonization.web.bean;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.List;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVStrategy;
import org.openmrs.api.APIException;
import org.openmrs.module.eptsharmonization.api.model.ConceptDTO;

public class ConceptHarmonizationCSVLog {
  public static ByteArrayOutputStream exportConceptLogs(
      String defaultLocationName, List<ConceptDTO> data) {

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
      printer.print("Site Concepts Not Already in Metadata package for Review ");
      printer.println();
      printer.print(
          "===============================================================================================================================");
      printer.println();
      printer.println();

      printer.print("Concept ID, Name, Description, UUID");
      printer.println();
      for (ConceptDTO conceptDTO : data) {

        try {
          printer.print(
              String.format(
                  "%s, %s, %s, %s",
                  conceptDTO.getConceptId(),
                  conceptDTO.getNames().get(0).getName(),
                  conceptDTO.getDescriptions().get(0).getDescription(),
                  conceptDTO.getUuid()));
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
