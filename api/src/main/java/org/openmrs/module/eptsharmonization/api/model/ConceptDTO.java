package org.openmrs.module.eptsharmonization.api.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;

/** @uthor Willa Mhawila<a.mhawila@gmail.com> on 6/30/20. */
public class ConceptDTO {
  private Integer conceptId;
  private Integer datatypeId;
  private Integer classId;
  private String version;
  private Boolean isSet;
  private Date dateCreated;
  private Boolean retired;
  private Date dateRetired;
  private String retireReason;
  private String uuid;

  private List<Name> names = new ArrayList<>();
  private List<Description> descriptions = new ArrayList<>();

  public ConceptDTO() {}

  public ConceptDTO(Integer conceptId, String uuid) {
    this.conceptId = conceptId;
    this.uuid = uuid;
  }

  public ConceptDTO(Concept concept) {
    this.conceptId = concept.getConceptId();
    this.uuid = concept.getUuid();
    ConceptDatatype datatype = concept.getDatatype();
    this.datatypeId = datatype != null ? datatype.getConceptDatatypeId() : null;

    ConceptClass conceptClass = concept.getConceptClass();
    this.classId = conceptClass != null ? conceptClass.getConceptClassId() : null;

    this.isSet = concept.getSet();
    this.version = concept.getVersion();

    this.dateCreated = concept.getDateCreated();
    this.retired = concept.isRetired();
    this.retireReason = concept.getRetireReason();
  }

  public Integer getConceptId() {
    return conceptId;
  }

  public void setConceptId(Integer conceptId) {
    this.conceptId = conceptId;
  }

  public Integer getDatatypeId() {
    return datatypeId;
  }

  public void setDatatypeId(Integer datatypeId) {
    this.datatypeId = datatypeId;
  }

  public Integer getClassId() {
    return classId;
  }

  public void setClassId(Integer classId) {
    this.classId = classId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Boolean getSet() {
    return isSet;
  }

  public void setSet(Boolean isSet) {
    this.isSet = isSet;
  }

  public Date getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(Date dateCreated) {
    this.dateCreated = dateCreated;
  }

  public Boolean getRetired() {
    return retired;
  }

  public void setRetired(Boolean retired) {
    this.retired = retired;
  }

  public Date getDateRetired() {
    return dateRetired;
  }

  public void setDateRetired(Date dateRetired) {
    this.dateRetired = dateRetired;
  }

  public String getRetireReason() {
    return retireReason;
  }

  public void setRetireReason(String retireReason) {
    this.retireReason = retireReason;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public List<Name> getNames() {
    return names;
  }

  public void setNames(List<Name> names) {
    this.names = names;
  }

  public List<Description> getDescriptions() {
    return descriptions;
  }

  public void setDescriptions(List<Description> descriptions) {
    this.descriptions = descriptions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ConceptDTO)) return false;
    ConceptDTO that = (ConceptDTO) o;
    return Objects.equals(getConceptId(), that.getConceptId())
        && Objects.equals(getDatatypeId(), that.getDatatypeId())
        && Objects.equals(getClassId(), that.getClassId())
        && Objects.equals(getSet(), that.getSet())
        && Objects.equals(getUuid(), that.getUuid());
  }

  public void addName(Name name) {
    getNames().add(name);
  }

  public void addDescription(Description description) {
    getDescriptions().add(description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getConceptId(), getDatatypeId(), getClassId(), getUuid());
  }

  public class Name {
    private Integer nameId;
    private Locale locale;
    private String name;
    private String uuid;
    private Boolean full;

    public Name() {}

    public Name(Integer nameId, Locale locale, String name, String uuid, Boolean full) {
      this.nameId = nameId;
      this.locale = locale;
      this.name = name;
      this.uuid = uuid;
      this.full = full;
    }

    public Name(ConceptName conceptName) {
      this.nameId = conceptName.getConceptNameId();
      this.locale = conceptName.getLocale();
      this.name = conceptName.getName();
      this.uuid = conceptName.getUuid();
      this.full = conceptName.isFullySpecifiedName();
    }

    public Integer getNameId() {
      return nameId;
    }

    public void setNameId(Integer nameId) {
      this.nameId = nameId;
    }

    public Locale getLocale() {
      return locale;
    }

    public void setLocale(Locale locale) {
      this.locale = locale;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getUuid() {
      return uuid;
    }

    public void setUuid(String uuid) {
      this.uuid = uuid;
    }

    public Boolean getFull() {
      return full;
    }

    public void setFull(Boolean full) {
      this.full = full;
    }
  }

  public class Description {
    private Integer descriptionId;
    private Locale locale;
    private String description;
    private String uuid;

    public Description() {}

    public Description(Integer descriptionId, Locale locale, String name, String uuid) {
      this.descriptionId = descriptionId;
      this.locale = locale;
      this.description = name;
      this.uuid = uuid;
    }

    public Description(ConceptDescription conceptDescription) {
      this.descriptionId = conceptDescription.getConceptDescriptionId();
      this.locale = conceptDescription.getLocale();
      this.description = conceptDescription.getDescription();
      this.uuid = conceptDescription.getUuid();
    }

    public Integer getDescriptionId() {
      return descriptionId;
    }

    public void setDescriptionId(Integer descriptionId) {
      this.descriptionId = descriptionId;
    }

    public Locale getLocale() {
      return locale;
    }

    public void setLocale(Locale locale) {
      this.locale = locale;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public String getUuid() {
      return uuid;
    }

    public void setUuid(String uuid) {
      this.uuid = uuid;
    }
  }
}
