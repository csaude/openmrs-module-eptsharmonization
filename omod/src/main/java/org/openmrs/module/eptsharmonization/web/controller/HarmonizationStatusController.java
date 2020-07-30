package org.openmrs.module.eptsharmonization.web.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import org.openmrs.module.eptsharmonization.api.HarmonizationEncounterTypeService;
import org.openmrs.module.eptsharmonization.api.HarmonizationPersonAttributeTypeService;
import org.openmrs.module.eptsharmonization.api.HarmonizationProgramService;
import org.openmrs.module.eptsharmonization.api.HarmonizationProgramWorkflowService;
import org.openmrs.module.eptsharmonization.api.HarmonizationProgramWorkflowStateService;
import org.openmrs.module.eptsharmonization.web.EptsHarmonizationConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller("eptsharmonization.harmonizationhStatusController")
public class HarmonizationStatusController {

	public static final String CONTROLLER_PATH = EptsHarmonizationConstants.MODULE_PATH + "/harmonizationstatus";

	public static final String HARMONIZATION_STATUS_LIST = HarmonizationStatusController.CONTROLLER_PATH
			+ "/harmonizationStatusList";

	private HarmonizationEncounterTypeService harmonizationEncounterTypeService;
	private HarmonizationPersonAttributeTypeService harmonizationPersonAttributeTypeService;
	private HarmonizationProgramService harmonizationProgramService;
	private HarmonizationProgramWorkflowService harmonizationProgramWorkflowService;
	private HarmonizationProgramWorkflowStateService harmonizationProgramWorkflowStateService;

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
	public void setHarmonizationProgramService(HarmonizationProgramService harmonizationProgramService) {
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

	@RequestMapping(value = HARMONIZATION_STATUS_LIST, method = RequestMethod.GET)
	public ModelAndView getHarmonizationStatusList() {

		Map<String, Boolean> metadataTypes = new LinkedHashMap<>();

		metadataTypes.put("eptsharmonization.harmonizationstatus.encounterType",
				this.harmonizationEncounterTypeService.isAllEncounterTypeMedatadaHarmonized());
		metadataTypes.put("eptsharmonization.harmonizationstatus.program",
				this.harmonizationProgramService.isAllMetadataHarmonized());
		metadataTypes.put("eptsharmonization.harmonizationstatus.programWorkflow",
				this.harmonizationProgramWorkflowService.isAllMetadataHarmonized());
		metadataTypes.put("eptsharmonization.harmonizationstatus.programWorkflowState",
				this.harmonizationProgramWorkflowStateService.isAllMetadataHarmonized());
		metadataTypes.put("eptsharmonization.harmonizationstatus.personAttributeType",
				this.harmonizationPersonAttributeTypeService.isAllMetadataHarmonized());

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("metadata", metadataTypes);

		return modelAndView;
	}
}
