package org.openmrs.module.eptsharmonization.web.bean;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVStrategy;
import org.openmrs.EncounterType;
import org.openmrs.api.APIException;
import org.openmrs.module.eptsharmonization.api.model.EncounterTypeDTO;

public class HarmonizationCSVLogUtils {

	public static ByteArrayOutputStream generateLogForHarmonizationEncounterTypesWithDifferentNames(
			String defaultLocationName, Map<String, List<EncounterTypeDTO>> data) {

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try {
			CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(outputStream, StandardCharsets.ISO_8859_1),
					new CSVStrategy('\t', ' ', CSVStrategy.COMMENTS_DISABLED, CSVStrategy.ESCAPE_DISABLED, false, false,
							false, true));

			printer.print("Location: " + defaultLocationName);
			printer.println();
			printer.print("Execution Date: " + Calendar.getInstance().getTime());
			printer.println();
			printer.print(
					"Metadata Harmonization Process Flow: Harmonized Encounter Types With different Name and Equal ID and UUID");
			printer.println();
			printer.print(
					"===============================================================================================================================");
			printer.println();
			printer.println();

			for (String key : data.keySet()) {
				List<EncounterTypeDTO> dtos = data.get(key);
				EncounterType mdServerET = dtos.get(0).getEncounterType();
				EncounterType pdServerET = dtos.get(1).getEncounterType();
				try {
					printer.print(String.format(
							"EncounterType with ID = {%s} and UUID = {%s}, updated name from  {%s} TO ->>  {%s} ",
							mdServerET.getId(), mdServerET.getUuid(), pdServerET.getName(), mdServerET.getName()));
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

	public static ByteArrayOutputStream generateLogForNewHarmonizedFromMDS(String defaultLocationName,
			List<EncounterTypeDTO> data) {

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try {
			CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(outputStream, StandardCharsets.ISO_8859_1),
					new CSVStrategy('\t', ' ', CSVStrategy.COMMENTS_DISABLED, CSVStrategy.ESCAPE_DISABLED, false, false,
							false, true));

			printer.print("Location: " + defaultLocationName);
			printer.println();
			printer.print("Execution Date: " + Calendar.getInstance().getTime());
			printer.println();
			printer.print(
					"Metadata Harmonization Process Flow: Created New Entries from Metadata Server to Production Server");
			printer.println();
			printer.print(
					"===============================================================================================================================");
			printer.println();
			printer.println();

			for (EncounterTypeDTO item : data) {
				try {
					printer.print(String.format("EncounterType[ID = {%s}, UUID = {%s}, NAME = {%s}]",
							item.getEncounterType().getId(), item.getEncounterType().getUuid(),
							item.getEncounterType().getName()));
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
