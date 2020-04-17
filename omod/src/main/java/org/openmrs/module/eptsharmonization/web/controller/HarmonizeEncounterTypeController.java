package org.openmrs.module.eptsharmonization.web.controller;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.eptsharmonization.HarmonizationUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationService;
import org.openmrs.module.eptsharmonization.api.model.EncounterTypeDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HarmonizeEncounterTypeController {

  protected final Log log = LogFactory.getLog(getClass());

  @RequestMapping(
      value = {"/module/eptsharmonization/harmonizeEncounterTypeList"},
      method = {org.springframework.web.bind.annotation.RequestMethod.GET})
  public ModelAndView getAffinityTypeList(
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg) {
    ModelAndView modelAndView = new ModelAndView();

    HarmonizationService harmonizationService = HarmonizationUtils.getService();

    List<EncounterTypeDTO> onlyMetadataEncounterTypes =
        harmonizationService.findAllMetadataEncounterNotContainedInProductionServer();
    List<EncounterTypeDTO> OnlyProductionEncounterTypes =
        harmonizationService.findAllProductionEncountersNotContainedInMetadataServer();
    List<EncounterTypeDTO> mdsEncountersPartialEqual =
        harmonizationService.findAllMetadataEncounterPartialEqualsToProductionServer();
    List<EncounterTypeDTO> pdsEncountersPartialEqual =
        harmonizationService.findAllProductionEncountersPartialEqualsToMetadataServer();

    modelAndView.addObject("onlyMetadataEncounterTypes", onlyMetadataEncounterTypes);
    modelAndView.addObject("OnlyProductionEncounterTypes", OnlyProductionEncounterTypes);
    modelAndView.addObject("mdsEncountersPartialEqual", mdsEncountersPartialEqual);
    modelAndView.addObject("pdsEncountersPartialEqual", pdsEncountersPartialEqual);

    modelAndView.addObject("openmrs_msg", openmrs_msg);

    return modelAndView;
  }
}
