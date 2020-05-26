package org.openmrs.module.eptsharmonization.web.controller;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.openmrs.module.eptsharmonization.HarmonizationUtils;
import org.openmrs.module.eptsharmonization.api.model.EncounterTypeDTO;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationData;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationItem;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

@Controller
@SessionAttributes({"harmonizationModel"})
public class HarmonizeExportEncounterTypesController {

  @RequestMapping(
      value = "/module/eptsharmonization/harmonizeExportEncounterTypes",
      method = RequestMethod.GET)
  public ModelAndView initForm(
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel) {

    ModelAndView modelAndView = new ModelAndView();
    return modelAndView;
  }

  @RequestMapping(
      value = "/module/eptsharmonization/harmonizeExportEncounterTypes",
      method = RequestMethod.POST)
  public ModelAndView confirmHarmonizationData(
      HttpServletRequest request,
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel) {

    ModelAndView modelAndView = new ModelAndView();
    return modelAndView;
  }

  @ModelAttribute("harmonizationModel")
  public HarmonizationData getHarmonizationModel() {
    List<HarmonizationItem> items = new ArrayList<>();
    List<EncounterTypeDTO> data =
        HarmonizationUtils.getHarmonizationEncounterTypeService()
            .findAllProductionEncountersNotContainedInMetadataServer();

    for (EncounterTypeDTO encounterTypeDTO : data) {
      final HarmonizationItem harmonizationItem =
          new HarmonizationItem(encounterTypeDTO.getUuid(), encounterTypeDTO);
      harmonizationItem.setEncountersCount(
          HarmonizationUtils.getHarmonizationEncounterTypeService()
              .getNumberOfAffectedEncounters(encounterTypeDTO));
      harmonizationItem.setFormsCount(
          HarmonizationUtils.getHarmonizationEncounterTypeService()
              .getNumberOfAffectedForms(encounterTypeDTO));
      if (harmonizationItem.getEncountersCount() != 0 || harmonizationItem.getFormsCount() != 0) {
        items.add(harmonizationItem);
      }
    }
    return new HarmonizationData(items);
  }
}
