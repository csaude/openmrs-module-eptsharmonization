package org.openmrs.module.eptsharmonization.web.controller;

import java.util.Collections;
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
public class HarmonizeUpdateEncounterTypesController {

  protected final Log log = LogFactory.getLog(getClass());

  @RequestMapping(
      value = {"/module/eptsharmonization/harmonizeUpdateEncounterTypes"},
      method = {org.springframework.web.bind.annotation.RequestMethod.GET})
  public ModelAndView getAffinityTypeList(
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg) {
    ModelAndView modelAndView = new ModelAndView();

    HarmonizationService harmonizationService = HarmonizationUtils.getService();

    List<EncounterTypeDTO> mdsEncountersPartialEqual =
        harmonizationService.findAllMetadataEncounterPartialEqualsToProductionServer();
    List<EncounterTypeDTO> pdsEncountersPartialEqual =
        harmonizationService.findAllProductionEncountersPartialEqualsToMetadataServer();

    Collections.sort(mdsEncountersPartialEqual);
    Collections.sort(pdsEncountersPartialEqual);

    modelAndView.addObject("mdsEncountersPartialEqual", mdsEncountersPartialEqual);
    modelAndView.addObject("pdsEncountersPartialEqual", pdsEncountersPartialEqual);

    modelAndView.addObject("openmrs_msg", openmrs_msg);

    return modelAndView;
  }

  @RequestMapping(
      value = {"/module/eptsharmonization/harmonizeUpdateEncounterTypes"},
      method = {org.springframework.web.bind.annotation.RequestMethod.POST})
  public ModelAndView processHarmonization(
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg) {
    ModelAndView modelAndView = new ModelAndView();

    HarmonizationService harmonizationService = HarmonizationUtils.getService();

    List<EncounterTypeDTO> mdsEncountersPartialEqual =
        harmonizationService.findAllMetadataEncounterPartialEqualsToProductionServer();
    List<EncounterTypeDTO> pdsEncountersPartialEqual =
        harmonizationService.findAllProductionEncountersPartialEqualsToMetadataServer();

    Collections.sort(mdsEncountersPartialEqual);
    Collections.sort(pdsEncountersPartialEqual);

    modelAndView.addObject("mdsEncountersPartialEqual", mdsEncountersPartialEqual);
    modelAndView.addObject("pdsEncountersPartialEqual", pdsEncountersPartialEqual);

    modelAndView.addObject("openmrs_msg", openmrs_msg);

    return modelAndView;
  }
}
