package org.openmrs.module.eptsharmonization.api;

import java.util.ArrayList;
import java.util.List;
import org.openmrs.EncounterType;
import org.openmrs.LocationAttributeType;
import org.openmrs.PersonAttributeType;
import org.openmrs.Program;
import org.openmrs.RelationshipType;
import org.openmrs.VisitType;
import org.openmrs.module.eptsharmonization.api.model.EncounterTypeDTO;
import org.openmrs.module.eptsharmonization.api.model.LocationAttributeTypeDTO;
import org.openmrs.module.eptsharmonization.api.model.PersonAttributeTypeDTO;
import org.openmrs.module.eptsharmonization.api.model.ProgramDTO;
import org.openmrs.module.eptsharmonization.api.model.RelationshipTypeDTO;
import org.openmrs.module.eptsharmonization.api.model.VisitTypeDTO;

public class DTOUtils {

  public static List<EncounterTypeDTO> fromEncounterTypes(List<EncounterType> encounterTypes) {
    List<EncounterTypeDTO> result = new ArrayList<>();

    for (EncounterType encounterType : encounterTypes) {
      result.add(new EncounterTypeDTO(encounterType));
    }
    return result;
  }

  public static List<EncounterType> fromEncounterTypeDTOs(
      List<EncounterTypeDTO> encounterTypeDTOs) {
    List<EncounterType> result = new ArrayList<>();
    for (EncounterTypeDTO encounterTypeDTO : encounterTypeDTOs) {
      result.add(encounterTypeDTO.getEncounterType());
    }
    return result;
  }

  public static EncounterTypeDTO fromEncounterType(EncounterType encounterType) {
    return new EncounterTypeDTO(encounterType);
  }

  public static List<PersonAttributeTypeDTO> fromPersonAttributeTypes(
      List<PersonAttributeType> personAttributeTypes) {
    List<PersonAttributeTypeDTO> result = new ArrayList<>();

    for (PersonAttributeType personAttributeType : personAttributeTypes) {
      result.add(new PersonAttributeTypeDTO(personAttributeType));
    }

    return result;
  }

  public static List<PersonAttributeType> fromPersonAttributeTypesDTOs(
      List<PersonAttributeTypeDTO> personAttributeDTOs) {
    List<PersonAttributeType> result = new ArrayList<>();
    for (PersonAttributeTypeDTO item : personAttributeDTOs) {
      result.add(item.getPersonAttributeType());
    }
    return result;
  }

  public static PersonAttributeTypeDTO fromPersonAttributeType(
      PersonAttributeType personAttributeType) {
    return new PersonAttributeTypeDTO(personAttributeType);
  }

  public static List<ProgramDTO> fromPrograms(List<Program> programs) {
    List<ProgramDTO> result = new ArrayList<>();

    for (Program program : programs) {
      result.add(new ProgramDTO(program));
    }
    return result;
  }

  public static List<Program> fromProgramDTOs(List<ProgramDTO> programDTOs) {
    List<Program> result = new ArrayList<>();
    for (ProgramDTO programDTO : programDTOs) {
      result.add(programDTO.getProgram());
    }
    return result;
  }

  public static ProgramDTO fromProgram(Program program) {
    return new ProgramDTO(program);
  }

  public static List<VisitTypeDTO> fromVisitTypes(final List<VisitType> visitTypes) {
    List<VisitTypeDTO> visitTypeDTOList = new ArrayList<>();
    for (VisitType visitType : visitTypes) {
      visitTypeDTOList.add(new VisitTypeDTO(visitType));
    }
    return visitTypeDTOList;
  }

  public static List<VisitType> fromVisitTypeDTOs(List<VisitTypeDTO> visitTypeDTOs) {
    List<VisitType> result = new ArrayList<>();
    for (VisitTypeDTO visitTypeDTO : visitTypeDTOs) {
      result.add(visitTypeDTO.getVisitType());
    }
    return result;
  }

  public static List<RelationshipTypeDTO> fromRelationshipTypes(
      final List<RelationshipType> relationshipTypes) {
    List<RelationshipTypeDTO> relationshipTypeDTOList = new ArrayList<>();
    for (RelationshipType relationshipType : relationshipTypes) {
      relationshipTypeDTOList.add(new RelationshipTypeDTO(relationshipType));
    }
    return relationshipTypeDTOList;
  }

  public static List<RelationshipType> fromRelationshipTypeDTOs(
      List<RelationshipTypeDTO> relationshipTypeDTOs) {
    List<RelationshipType> result = new ArrayList<>();
    for (RelationshipTypeDTO relationshipTypeDTO : relationshipTypeDTOs) {
      result.add(relationshipTypeDTO.getRelationshipType());
    }
    return result;
  }

  public static List<LocationAttributeTypeDTO> fromLocationAttributeTypes(
      final List<LocationAttributeType> locationAttributeTypes) {
    List<LocationAttributeTypeDTO> locationAttributeTypeDTOList = new ArrayList<>();
    for (LocationAttributeType locationAttributeType : locationAttributeTypes) {
      locationAttributeTypeDTOList.add(new LocationAttributeTypeDTO(locationAttributeType));
    }
    return locationAttributeTypeDTOList;
  }

  public static List<LocationAttributeType> fromLocationAttributeTypeDTOs(
      List<LocationAttributeTypeDTO> locationAttributeTypeDTOs) {
    List<LocationAttributeType> result = new ArrayList<>();
    for (LocationAttributeTypeDTO locationAttributeTypeDTO : locationAttributeTypeDTOs) {
      result.add(locationAttributeTypeDTO.getLocationAttributeType());
    }
    return result;
  }
}
