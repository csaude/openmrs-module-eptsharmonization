package org.openmrs.module.eptsharmonization;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsharmonization.EptsHarmonizationFormLoader.FormData.FormDataTypes;
import org.openmrs.util.OpenmrsClassLoader;

public class EptsHarmonizationFormLoader {

  public static void loadForms() throws IOException {

    List<List<FormData>> entries = EptsHarmonizationFormLoader.loadFormEntries();

    for (List<FormData> formItems : entries) {

      String insertColumns = "";
      String insertValues = "";

      Iterator<FormData> iteratorItems = formItems.iterator();

      FormData formItem0 = iteratorItems.next();

      insertColumns += formItem0.getColumnName();
      insertValues += formItem0.getValue();

      while (iteratorItems.hasNext()) {
        FormData item = iteratorItems.next();
        insertColumns += ", " + item.getColumnName();
        insertValues += ", " + item.getConvertedValue();
      }
      StringBuilder sb = new StringBuilder();
      sb.append("insert into _form (" + insertColumns + ") values(" + insertValues + ")");
      Context.getAdministrationService().executeSQL(sb.toString(), false);
    }
  }

  public static void loadHtmlForms() throws IOException {

    List<List<FormData>> entries = EptsHarmonizationFormLoader.loadHtmlFormEntries();

    for (List<FormData> formItems : entries) {

      String insertColumns = "";
      String insertValues = "";

      Iterator<FormData> iteratorItems = formItems.iterator();

      FormData formItem0 = iteratorItems.next();

      insertColumns += formItem0.getColumnName();
      insertValues += formItem0.getValue();

      while (iteratorItems.hasNext()) {
        FormData item = iteratorItems.next();
        insertColumns += ", " + item.getColumnName();
        insertValues += ", " + item.getConvertedValue();
      }
      StringBuilder sb = new StringBuilder();
      sb.append(
          "insert into _htmlformentry_html_form ("
              + insertColumns
              + ") values("
              + insertValues
              + ")");
      Context.getAdministrationService().executeSQL(sb.toString(), false);
    }
  }

  private static List<List<FormData>> loadFormEntries() throws IOException {
    List<List<FormData>> formData = new ArrayList<>();
    Sheet sheet = getFormResource();
    Iterator<Row> rows = sheet.rowIterator();

    if (rows.hasNext()) {
      rows.next();
    }
    while (rows.hasNext()) {
      Row row = (Row) rows.next();

      List<FormData> rowList = new ArrayList<>();

      rowList.add(
          new FormData("form_id", getRequiredNumericValue(row.getCell(0)), FormDataTypes.NUMBER));
      rowList.add(
          new FormData("name", getRequiredStringValue(row.getCell(1)), FormDataTypes.STRING));
      rowList.add(
          new FormData(
              "version", getRequiredNumericValueAsString(row.getCell(2)), FormDataTypes.STRING));
      rowList.add(
          new FormData("build", getOptionalNumericValue(row.getCell(3)), FormDataTypes.NUMBER));
      rowList.add(
          new FormData(
              "published", getRequiredBooleanValue(row.getCell(4)), FormDataTypes.BOOLEAN));
      rowList.add(
          new FormData(
              "encounter_type", getOptionalNumericValue(row.getCell(6)), FormDataTypes.NUMBER));
      rowList.add(
          new FormData("creator", getRequiredNumericValue(row.getCell(9)), FormDataTypes.NUMBER));
      rowList.add(
          new FormData("date_created", getRequiredDateValue(row.getCell(10)), FormDataTypes.DATE));
      rowList.add(
          new FormData(
              "changed_by", getOptionalNumericValue(row.getCell(11)), FormDataTypes.NUMBER));
      rowList.add(
          new FormData("date_changed", getOptionalDateValue(row.getCell(12)), FormDataTypes.DATE));
      rowList.add(
          new FormData("retired", getRequiredBooleanValue(row.getCell(13)), FormDataTypes.BOOLEAN));
      rowList.add(
          new FormData(
              "retired_by", getOptionalNumericValue(row.getCell(14)), FormDataTypes.NUMBER));
      rowList.add(
          new FormData("date_retired", getOptionalDateValue(row.getCell(15)), FormDataTypes.DATE));
      rowList.add(
          new FormData(
              "retired_reason", getOptionalStringValue(row.getCell(16)), FormDataTypes.STRING));
      rowList.add(
          new FormData("uuid", getRequiredStringValue(row.getCell(17)), FormDataTypes.STRING));

      formData.add(rowList);
    }
    return filterNonNullValues(formData);
  }

  private static List<List<FormData>> loadHtmlFormEntries() throws IOException {
    List<List<FormData>> formData = new ArrayList<>();
    Sheet sheet = getHtmlFormResource();
    Iterator<Row> rows = sheet.rowIterator();

    if (rows.hasNext()) {
      rows.next();
    }
    while (rows.hasNext()) {
      Row row = (Row) rows.next();

      List<FormData> rowList = new ArrayList<>();
      rowList.add(
          new FormData("id", getRequiredNumericValue(row.getCell(0)), FormDataTypes.NUMBER));
      rowList.add(
          new FormData("form_id", getOptionalNumericValue(row.getCell(1)), FormDataTypes.NUMBER));
      rowList.add(
          new FormData("name", getOptionalStringValue(row.getCell(2)), FormDataTypes.STRING));
      rowList.add(
          new FormData("xml_data", getRequiredStringValue(row.getCell(3)), FormDataTypes.XML));
      rowList.add(
          new FormData("creator", getRequiredNumericValue(row.getCell(4)), FormDataTypes.NUMBER));
      rowList.add(
          new FormData("date_created", getRequiredDateValue(row.getCell(5)), FormDataTypes.DATE));
      rowList.add(
          new FormData(
              "changed_by", getOptionalNumericValue(row.getCell(6)), FormDataTypes.NUMBER));
      rowList.add(
          new FormData("date_changed", getOptionalDateValue(row.getCell(7)), FormDataTypes.DATE));
      rowList.add(
          new FormData("retired", getRequiredBooleanValue(row.getCell(8)), FormDataTypes.BOOLEAN));
      rowList.add(
          new FormData("uuid", getRequiredStringValue(row.getCell(9)), FormDataTypes.STRING));
      rowList.add(
          new FormData(
              "description", getOptionalStringValue(row.getCell(10)), FormDataTypes.STRING));
      rowList.add(
          new FormData(
              "retired_by", getOptionalNumericValue(row.getCell(11)), FormDataTypes.NUMBER));
      rowList.add(
          new FormData("date_retired", getOptionalDateValue(row.getCell(12)), FormDataTypes.DATE));
      rowList.add(
          new FormData(
              "retired_reason", getOptionalStringValue(row.getCell(13)), FormDataTypes.STRING));
      formData.add(rowList);
    }
    return filterNonNullValues(formData);
  }

  @SuppressWarnings("resource")
  private static Sheet getFormResource() throws IOException {

    InputStream excelFileToRead =
        OpenmrsClassLoader.getInstance().getResourceAsStream("all-forms.xls");
    XSSFWorkbook xssfWBook = new XSSFWorkbook(excelFileToRead);

    return xssfWBook.getSheetAt(0);
  }

  @SuppressWarnings("resource")
  private static Sheet getHtmlFormResource() throws IOException {

    InputStream excelFileToRead =
        OpenmrsClassLoader.getInstance()
            .getResourceAsStream("all-htmlformentry_html_form_13072020.xls");
    XSSFWorkbook xssfWBook = new XSSFWorkbook(excelFileToRead);

    return xssfWBook.getSheetAt(0);
  }

  private static List<List<FormData>> filterNonNullValues(List<List<FormData>> formData) {
    for (List<FormData> list : formData) {
      Iterator<FormData> iterator = list.iterator();
      while (iterator.hasNext()) {
        EptsHarmonizationFormLoader.FormData formDataItem =
            (EptsHarmonizationFormLoader.FormData) iterator.next();
        if (formDataItem.isNullValue()) {
          iterator.remove();
        }
      }
    }
    return formData;
  }

  private static int getRequiredNumericValue(Cell cell) {
    return Double.valueOf(cell.getNumericCellValue()).intValue();
  }

  private static String getRequiredNumericValueAsString(Cell cell) {
    return String.valueOf(cell.getNumericCellValue());
  }

  private static String getRequiredStringValue(Cell cell) {
    return cell.getStringCellValue().trim();
  }

  @SuppressWarnings("deprecation")
  private static Date getRequiredDateValue(Cell cell) {
    return new Date(cell.toString());
  }

  @SuppressWarnings("deprecation")
  private static Date getOptionalDateValue(Cell cell) {
    if (cell == null) {
      return null;
    }
    if (cell.toString().equals("\\N") || cell.toString().isEmpty()) {
      return null;
    }
    return new Date(cell.toString());
  }

  private static String getOptionalNumericValue(Cell cell) {
    if (cell == null) {
      return null;
    }
    if (cell.toString().equals("\\N")) {
      return null;
    }
    return String.valueOf(Double.valueOf(cell.getNumericCellValue()).intValue());
  }

  private static String getOptionalStringValue(Cell cell) {
    if (cell == null) {
      return null;
    }
    if (cell.toString().equals("\\N")) {
      return null;
    }
    return cell.getStringCellValue();
  }

  private static boolean getRequiredBooleanValue(Cell cell) {
    int value = Double.valueOf(cell.getNumericCellValue()).intValue();
    return value == 1 ? true : false;
  }

  public static class FormData {

    private String columnName;
    private Object value;
    private FormDataTypes dataType;

    public FormData(String columnName, Object value, FormDataTypes dataType) {
      this.columnName = columnName;
      this.value = value;
      this.dataType = dataType;
    }

    public String getColumnName() {
      return columnName;
    }

    public Object getValue() {
      return value;
    }

    public FormDataTypes getDataType() {
      return dataType;
    }

    public boolean isNullValue() {
      if (this.value == null) {
        return true;
      }
      if (this.value instanceof String) {
        String valueAsString = (String) this.value;

        if (StringUtils.isEmpty(valueAsString) || valueAsString.equals("null")) {
          return true;
        }
      }
      return false;
    }

    public Object getConvertedValue() {

      if (FormDataTypes.STRING.equals(this.getDataType())) {
        return "'" + this.value + "'";
      }

      if (FormDataTypes.NUMBER.equals(this.getDataType())) {
        return Integer.valueOf(this.value.toString());
      }

      if (FormDataTypes.BOOLEAN.equals(this.getDataType())) {
        return (Boolean) this.value;
      }
      if (FormDataTypes.XML.equals(this.getDataType())) {

        return "N'" + this.value + "'";
      }

      if (FormDataTypes.DATE.equals(this.getDataType())) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = dateFormat.format((Date) this.value);
        return "'" + strDate + "'";
      }
      return null;
    }

    public enum FormDataTypes {
      STRING,
      NUMBER,
      DATE,
      BOOLEAN,
      XML;
    }
  }
}
