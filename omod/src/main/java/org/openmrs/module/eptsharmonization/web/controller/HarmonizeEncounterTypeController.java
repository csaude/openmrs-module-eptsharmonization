package org.openmrs.module.eptsharmonization.web.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.eptsharmonization.HarmonizationUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationEncounterTypeService;
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

    HarmonizationEncounterTypeService encounterTypeService =
        HarmonizationUtils.getHarmonizationEncounterTypeService();

    List<EncounterTypeDTO> onlyMetadataEncounterTypes =
        encounterTypeService.findAllMetadataEncounterNotContainedInProductionServer();
    List<EncounterTypeDTO> OnlyProductionEncounterTypes =
        encounterTypeService.findAllProductionEncountersNotContainedInMetadataServer();
    List<EncounterTypeDTO> mdsEncountersPartialEqual =
        encounterTypeService.findAllMetadataEncounterPartialEqualsToProductionServer();
    List<EncounterTypeDTO> pdsEncountersPartialEqual =
        encounterTypeService.findAllProductionEncountersPartialEqualsToMetadataServer();
    Map<String, List<EncounterTypeDTO>> encounterTypesWithDifferentNames =
        encounterTypeService.findAllEncounterTypesWithDifferentNameAndSameUUIDAndID();

    Collections.sort(mdsEncountersPartialEqual);
    Collections.sort(pdsEncountersPartialEqual);

    modelAndView.addObject("onlyMetadataEncounterTypes", onlyMetadataEncounterTypes);
    modelAndView.addObject("OnlyProductionEncounterTypes", OnlyProductionEncounterTypes);
    modelAndView.addObject("mdsEncountersPartialEqual", mdsEncountersPartialEqual);
    modelAndView.addObject("pdsEncountersPartialEqual", pdsEncountersPartialEqual);
    modelAndView.addObject("encounterTypesWithDifferentNames", encounterTypesWithDifferentNames);

    modelAndView.addObject("openmrs_msg", openmrs_msg);

    return modelAndView;
  }

  @RequestMapping(
      value = {"/module/eptsharmonization/harmonizeEncounterTypeList"},
      method = {org.springframework.web.bind.annotation.RequestMethod.POST})
  public ModelAndView processForm(
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg) {
    ModelAndView modelAndView = new ModelAndView();

    HarmonizationEncounterTypeService encounterTypeService =
        HarmonizationUtils.getHarmonizationEncounterTypeService();

    List<EncounterTypeDTO> onlyMetadataEncounterTypes =
        encounterTypeService.findAllMetadataEncounterNotContainedInProductionServer();
    List<EncounterTypeDTO> OnlyProductionEncounterTypes =
        encounterTypeService.findAllProductionEncountersNotContainedInMetadataServer();
    List<EncounterTypeDTO> mdsEncountersPartialEqual =
        encounterTypeService.findAllMetadataEncounterPartialEqualsToProductionServer();
    List<EncounterTypeDTO> pdsEncountersPartialEqual =
        encounterTypeService.findAllProductionEncountersPartialEqualsToMetadataServer();
    Map<String, List<EncounterTypeDTO>> encounterTypesWithDifferentNames =
        encounterTypeService.findAllEncounterTypesWithDifferentNameAndSameUUIDAndID();

    Collections.sort(mdsEncountersPartialEqual);
    Collections.sort(pdsEncountersPartialEqual);

    modelAndView.addObject("onlyMetadataEncounterTypes", onlyMetadataEncounterTypes);
    modelAndView.addObject("OnlyProductionEncounterTypes", OnlyProductionEncounterTypes);
    modelAndView.addObject("mdsEncountersPartialEqual", mdsEncountersPartialEqual);
    modelAndView.addObject("pdsEncountersPartialEqual", pdsEncountersPartialEqual);
    modelAndView.addObject("encounterTypesWithDifferentNames", encounterTypesWithDifferentNames);

    modelAndView.addObject("openmrs_msg", openmrs_msg);

    return modelAndView;
  }
}
