package org.openmrs.module.eptsharmonization.web.controller;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.openmrs.module.eptsharmonization.HarmonizationUtils;
import org.openmrs.module.eptsharmonization.api.model.EncounterTypeDTO;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationData;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationItem;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

@Controller
@SessionAttributes({"harmonizationModel"})
public class HarmonizeAddNewEncounterTypesController {

  private HarmonizationData harmonizationModel = null;

  @RequestMapping(
      value = "/module/eptsharmonization/harmonizeAddNewEncounterTypes",
      method = RequestMethod.GET)
  public ModelAndView initForm(
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel) {

    ModelAndView modelAndView = new ModelAndView();
    return modelAndView;
  }

  @RequestMapping(
      value = "/module/eptsharmonization/harmonizeAddNewEncounterTypes",
      method = RequestMethod.POST)
  public ModelAndView confirmHarmonizationData(
      HttpServletRequest request,
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel) {

    ModelAndView modelAndView =
        new ModelAndView("redirect:/module/eptsharmonization/harmonizeAddNewEncounterTypes2.form");
    return modelAndView;
  }

  @RequestMapping(
      value = "/module/eptsharmonization/harmonizeAddNewEncounterTypes2",
      method = org.springframework.web.bind.annotation.RequestMethod.GET)
  public ModelAndView initFormProcessHarmonization(
      HttpServletRequest request,
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel) {

    ModelAndView modelAndView = new ModelAndView();
    modelAndView.addObject("harmonizationModel", harmonizationModel);
    this.harmonizationModel = harmonizationModel;

    for (HarmonizationItem item : harmonizationModel.getItems()) {
      EncounterTypeDTO encouterType = (EncounterTypeDTO) item.getValue();
      item.setEncountersCount(
          HarmonizationUtils.getHarmonizationEncounterTypeService()
              .countEncounterRows(encouterType.getEncounterType().getId()));
      item.setFormsCount(
          HarmonizationUtils.getHarmonizationEncounterTypeService()
              .countFormRows(encouterType.getEncounterType().getId()));
    }

    return modelAndView;
  }

  @RequestMapping(
      value = "/module/eptsharmonization/harmonizeAddNewEncounterTypes2",
      method = org.springframework.web.bind.annotation.RequestMethod.POST)
  public ModelAndView processHarmonization(
      HttpServletRequest request,
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel,
      ModelMap model) {

    model.addAttribute("openmrs_msg", "eptsharmonization.encountertype.harmonized");
    model.addAttribute("harmonizationModel", this.harmonizationModel);
    ModelAndView modelAndView =
        new ModelAndView(
            "redirect:/module/eptsharmonization/harmonizeAddNewEncounterTypes3.form", model);
    return modelAndView;
  }

  @RequestMapping(
      value = "/module/eptsharmonization/harmonizeAddNewEncounterTypes3",
      method = org.springframework.web.bind.annotation.RequestMethod.GET)
  public ModelAndView initExportLog(
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel,
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg,
      ModelMap model) {

    ModelAndView modelAndView = new ModelAndView();
    modelAndView.addObject("openmrs_msg", openmrs_msg);
    model.addAttribute("harmonizationModel", this.harmonizationModel);

    return modelAndView;
  }

  @RequestMapping(
      value = "/module/eptsharmonization/harmonizeAddNewEncounterTypes3",
      method = org.springframework.web.bind.annotation.RequestMethod.POST)
  public ModelAndView exportLog(
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel,
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg,
      ModelMap model) {

    ModelAndView modelAndView = new ModelAndView();
    modelAndView.addObject("openmrs_msg", openmrs_msg);
    model.addAttribute("harmonizationModel", this.harmonizationModel);
    return modelAndView;
  }

  @ModelAttribute("harmonizationModel")
  public HarmonizationData getHarmonizationModel() {
    List<HarmonizationItem> items = new ArrayList<>();
    List<EncounterTypeDTO> data =
        HarmonizationUtils.getHarmonizationEncounterTypeService()
            .findAllMetadataEncounterNotContainedInProductionServer();

    for (EncounterTypeDTO encounterTypeDTO : data) {
      items.add(new HarmonizationItem(encounterTypeDTO.getUuid(), encounterTypeDTO));
    }
    return new HarmonizationData(items);
  }
}
