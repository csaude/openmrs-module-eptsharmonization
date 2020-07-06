package org.openmrs.module.eptsharmonization.api.model;

import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.Form;

public class HtmlFormDTO extends BaseOpenmrsMetadata {

  private Integer id;

  private Form form;

  private String xmlData;

  public HtmlFormDTO() {}

  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  public Form getForm() {
    return form;
  }

  public void setForm(Form form) {
    this.form = form;
  }

  @Override
  public String getName() {
    return form != null ? form.getName() : null;
  }

  @Override
  public void setName(String name) {
    // throw new UnsupportedOperationException("Not supported. Set the name on form
    // instead");
  }

  @Override
  public String getDescription() {
    return form != null ? form.getDescription() : null;
  }

  @Override
  public void setDescription(String description) {}

  public String getXmlData() {
    return xmlData;
  }

  public void setXmlData(String xmlData) {
    this.xmlData = xmlData;
  }
}
