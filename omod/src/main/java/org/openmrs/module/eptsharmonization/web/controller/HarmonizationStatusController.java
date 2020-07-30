package org.openmrs.module.eptsharmonization.web.controller;

import org.openmrs.module.eptsharmonization.api.HarmonizationEncounterTypeService;
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

  @Autowired
  public void setHarmonizationEncounterTypeService(
      HarmonizationEncounterTypeService harmonizationEncounterTypeService) {
    this.harmonizationEncounterTypeService = harmonizationEncounterTypeService;
  }

  @RequestMapping(value = HARMONIZATION_STATUS_LIST, method = RequestMethod.GET)
  public ModelAndView getHarmonizationStatusList() {

    ModelAndView modelAndView = new ModelAndView();
    modelAndView.addObject(
        "encounterTypeStatus",
        this.harmonizationEncounterTypeService.isAllEncounterTypeMedatadaHarmonized());

    return modelAndView;
  }
}
