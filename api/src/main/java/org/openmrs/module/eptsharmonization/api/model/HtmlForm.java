package org.openmrs.module.eptsharmonization.api.model;

import java.io.Serializable;
import java.util.Date;
import org.openmrs.Form;

public class HtmlForm implements Serializable {

  private static final long serialVersionUID = -2311446968235761357L;

  private Integer id;

  private Integer formId;

  private Form form;

  private String xmlData;

  private String uuid;

  private Boolean retired;

  private Integer creator;

  private Date creationDate;

  public HtmlForm(
      Integer id,
      Integer formId,
      String xmlData,
      String uuid,
      Boolean retired,
      Integer creator,
      Date creationDate) {
    this.id = id;
    this.formId = formId;
    this.xmlData = xmlData;
    this.uuid = uuid;
    this.retired = retired;
    this.creator = creator;
    this.creationDate = creationDate;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  public Integer getId() {
    return id;
  }

  public Integer getFormId() {
    return formId;
  }

  public String getXmlData() {
    return xmlData;
  }

  public String getUuid() {
    return uuid;
  }

  public Boolean getRetired() {
    return retired;
  }

  public Integer getCreator() {
    return creator;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public Form getForm() {
    return form;
  }

  public void setForm(Form form) {
    this.form = form;
  }
}
