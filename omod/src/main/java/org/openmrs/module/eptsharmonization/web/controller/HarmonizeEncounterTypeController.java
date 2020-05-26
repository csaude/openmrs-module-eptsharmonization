package org.openmrs.module.eptsharmonization.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.openmrs.module.eptsharmonization.HarmonizationUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationEncounterTypeService;
import org.openmrs.module.eptsharmonization.api.model.EncounterTypeDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HarmonizeEncounterTypeController {

  public static List<String> HARMONIZED_CACHED_SUMMARY = new ArrayList<>();

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

    List<EncounterTypeDTO> productionItemsToDelete = new ArrayList<>();
    List<EncounterTypeDTO> productionItemsToExport = new ArrayList<>();

    for (EncounterTypeDTO encounterTypeDTO : onlyProductionEncounterTypes) {
      final int numberOfAffectedEncounters =
          HarmonizationUtils.getHarmonizationEncounterTypeService()
              .getNumberOfAffectedEncounters(encounterTypeDTO);
      final int numberOfAffectedForms =
          HarmonizationUtils.getHarmonizationEncounterTypeService()
              .getNumberOfAffectedForms(encounterTypeDTO);
      if (numberOfAffectedEncounters == 0 && numberOfAffectedForms == 0) {
        productionItemsToDelete.add(encounterTypeDTO);
      } else {
        productionItemsToExport.add(encounterTypeDTO);
      }
    }

    modelAndView.addObject("onlyMetadataEncounterTypes", onlyMetadataEncounterTypes);
    modelAndView.addObject("onlyProductionEncounterTypes", onlyProductionEncounterTypes);
    modelAndView.addObject("productionItemsToDelete", productionItemsToDelete);
    modelAndView.addObject("productionItemsToExport", productionItemsToExport);
    modelAndView.addObject("encounterTypesWithDifferentNames", encounterTypesWithDifferentNames);
    modelAndView.addObject("encounterTypesPartialEqual", encounterTypesWithDifferentIDsSameUUIDs);
    modelAndView.addObject("harmonizedETSummary", HARMONIZED_CACHED_SUMMARY);
    modelAndView.addObject("openmrs_msg", openmrs_msg);

    return modelAndView;
  }
}
