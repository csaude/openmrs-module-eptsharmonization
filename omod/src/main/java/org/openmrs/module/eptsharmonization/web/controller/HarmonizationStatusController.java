package org.openmrs.module.eptsharmonization.web.controller;

import org.openmrs.module.eptsharmonization.api.HarmonizationConceptService;
import org.openmrs.module.eptsharmonization.api.HarmonizationEncounterTypeService;
import org.openmrs.module.eptsharmonization.api.HarmonizationFormService;
import org.openmrs.module.eptsharmonization.api.HarmonizationLocationAttributeTypeService;
import org.openmrs.module.eptsharmonization.api.HarmonizationLocationTagService;
import org.openmrs.module.eptsharmonization.api.HarmonizationPatientIdentifierTypeService;
import org.openmrs.module.eptsharmonization.api.HarmonizationPersonAttributeTypeService;
import org.openmrs.module.eptsharmonization.api.HarmonizationProgramService;
import org.openmrs.module.eptsharmonization.api.HarmonizationProgramWorkflowService;
import org.openmrs.module.eptsharmonization.api.HarmonizationProgramWorkflowStateService;
import org.openmrs.module.eptsharmonization.api.HarmonizationRelationshipTypeService;
import org.openmrs.module.eptsharmonization.api.HarmonizationVisitTypeService;
import org.openmrs.module.eptsharmonization.web.EptsHarmonizationConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller("eptsharmonization.harmonizationhStatusController")
public class HarmonizationStatusController {

  public static final String CONTROLLER_PATH =
      EptsHarmonizationConstants.MODULE_PATH + "/harmonizationstatus";

  public static final String HARMONIZATION_STATUS_LIST =
      HarmonizationStatusController.CONTROLLER_PATH + "/harmonizationStatusList";

  private HarmonizationEncounterTypeService harmonizationEncounterTypeService;
  private HarmonizationPersonAttributeTypeService harmonizationPersonAttributeTypeService;
  private HarmonizationProgramService harmonizationProgramService;
  private HarmonizationProgramWorkflowService harmonizationProgramWorkflowService;
  private HarmonizationProgramWorkflowStateService harmonizationProgramWorkflowStateService;
  private HarmonizationPatientIdentifierTypeService harmonizationPatientIdentifierTypeService;
  private HarmonizationVisitTypeService harmonizationVisitTypeService;
  private HarmonizationLocationTagService harmonizationLocationTagService;
  private HarmonizationLocationAttributeTypeService harmonizationLocationAttributeTypeService;
  private HarmonizationFormService harmonizationFormService;
  private HarmonizationRelationshipTypeService harmonizationRelationshipTypeService;
  private HarmonizationConceptService harmonizationConceptService;

  @Autowired
  public void setHarmonizationEncounterTypeService(
      HarmonizationEncounterTypeService harmonizationEncounterTypeService) {
    this.harmonizationEncounterTypeService = harmonizationEncounterTypeService;
  }

  @Autowired
  public void setHarmonizationPersonAttributeTypeService(
      HarmonizationPersonAttributeTypeService harmonizationPersonAttributeTypeService) {
    this.harmonizationPersonAttributeTypeService = harmonizationPersonAttributeTypeService;
  }

  @Autowired
  public void setHarmonizationProgramService(
      HarmonizationProgramService harmonizationProgramService) {
    this.harmonizationProgramService = harmonizationProgramService;
  }

  @Autowired
  public void setHarmonizationProgramWorkflowService(
      HarmonizationProgramWorkflowService harmonizationProgramWorkflowService) {
    this.harmonizationProgramWorkflowService = harmonizationProgramWorkflowService;
  }

  @Autowired
  public void setHarmonizationProgramWorkflowStateService(
      HarmonizationProgramWorkflowStateService harmonizationProgramWorkflowStateService) {
    this.harmonizationProgramWorkflowStateService = harmonizationProgramWorkflowStateService;
  }

  @Autowired
  public void setHarmonizationPatientIdentifierTypeService(
      HarmonizationPatientIdentifierTypeService harmonizationPatientIdentifierTypeService) {
    this.harmonizationPatientIdentifierTypeService = harmonizationPatientIdentifierTypeService;
  }

  @Autowired
  public void setHarmonizeVisitTypeController(
      HarmonizationVisitTypeService harmonizationVisitTypeService) {
    this.harmonizationVisitTypeService = harmonizationVisitTypeService;
  }

  @Autowired
  public void setHarmonizationLocationTagService(
      HarmonizationLocationTagService harmonizationLocationTagService) {
    this.harmonizationLocationTagService = harmonizationLocationTagService;
  }

  @Autowired
  public void setHarmonizationLocationAttributeTypeService(
      HarmonizationLocationAttributeTypeService harmonizationLocationAttributeTypeService) {
    this.harmonizationLocationAttributeTypeService = harmonizationLocationAttributeTypeService;
  }

  @Autowired
  public void setHarmonizationFormService(HarmonizationFormService harmonizationFormService) {
    this.harmonizationFormService = harmonizationFormService;
  }

  @Autowired
  public void setHarmonizationRelationshipTypeService(
      HarmonizationRelationshipTypeService harmonizationRelationshipTypeService) {
    this.harmonizationRelationshipTypeService = harmonizationRelationshipTypeService;
  }

  @Autowired
  public void setHarmonizationConceptService(
      HarmonizationConceptService harmonizationConceptService) {
    this.harmonizationConceptService = harmonizationConceptService;
  }

  @RequestMapping(value = HARMONIZATION_STATUS_LIST, method = RequestMethod.GET)
  public ModelAndView getHarmonizationStatusList() {

    ModelAndView modelAndView = new ModelAndView();
    modelAndView.addObject(
        "visitTypeStatus", this.harmonizationVisitTypeService.isAllMetadataHarmonized());
    modelAndView.addObject(
        "locationTagStatus", this.harmonizationLocationTagService.isAllMetadataHarmonized());
    modelAndView.addObject(
        "locationAttributeTypeStatus",
        this.harmonizationLocationAttributeTypeService.isAllMetadataHarmonized());
    modelAndView.addObject(
        "patientIdentifierTypeStatus",
        this.harmonizationPatientIdentifierTypeService.isAllMetadataHarmonized());
    modelAndView.addObject(
        "programStatus", this.harmonizationProgramService.isAllMetadataHarmonized());
    modelAndView.addObject(
        "programWorkflowStatus",
        this.harmonizationProgramWorkflowService.isAllMetadataHarmonized());
    modelAndView.addObject(
        "programWorkflowStateStatus",
        this.harmonizationProgramWorkflowStateService.isAllMetadataHarmonized());
    modelAndView.addObject(
        "encounterTypeStatus",
        this.harmonizationEncounterTypeService.isAllEncounterTypeMedatadaHarmonized());
    modelAndView.addObject("formStatus", this.harmonizationFormService.isAllMetadataHarmonized());
    modelAndView.addObject(
        "personAttributeTypeStatus",
        this.harmonizationPersonAttributeTypeService.isAllMetadataHarmonized());
    modelAndView.addObject(
        "relationshipTypeStatus",
        this.harmonizationRelationshipTypeService.isAllMetadataHarmonized());
    modelAndView.addObject(
        "conceptStatus", this.harmonizationConceptService.isAllMetadataHarmonized());

    return modelAndView;
  }
}
