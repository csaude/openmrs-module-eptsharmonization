package org.openmrs.module.eptsharmonization.web.controller;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
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
  public ModelAndView getEncounterTypesList(
      HttpSession session,
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg) {
    ModelAndView modelAndView = new ModelAndView();

    session.removeAttribute("harmonizationModel");

    HarmonizationEncounterTypeService encounterTypeService =
        HarmonizationUtils.getHarmonizationEncounterTypeService();

    List<EncounterTypeDTO> onlyMetadataEncounterTypes =
        encounterTypeService.findAllMetadataEncounterNotContainedInProductionServer();
    List<EncounterTypeDTO> onlyProductionEncounterTypes =
        encounterTypeService.findAllProductionEncountersNotContainedInMetadataServer();
    Map<String, List<EncounterTypeDTO>> encounterTypesWithDifferentNames =
        encounterTypeService.findAllEncounterTypesWithDifferentNameAndSameUUIDAndID();
    Map<String, List<EncounterTypeDTO>> encounterTypesWithDifferentIDsSameUUIDs =
        encounterTypeService.findAllEncounterTypesWithDifferentIDAndSameUUID();

    modelAndView.addObject("onlyMetadataEncounterTypes", onlyMetadataEncounterTypes);
    modelAndView.addObject("onlyProductionEncounterTypes", onlyProductionEncounterTypes);
    modelAndView.addObject("encounterTypesWithDifferentNames", encounterTypesWithDifferentNames);
    modelAndView.addObject("encounterTypesPartialEqual", encounterTypesWithDifferentIDsSameUUIDs);

    modelAndView.addObject("openmrs_msg", openmrs_msg);

    return modelAndView;
  }
}
