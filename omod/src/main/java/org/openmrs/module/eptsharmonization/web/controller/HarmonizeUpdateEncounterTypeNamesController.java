package org.openmrs.module.eptsharmonization.web.controller;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsharmonization.HarmonizationUtils;
import org.openmrs.module.eptsharmonization.api.model.EncounterTypeDTO;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationCSVLogUtils;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationData;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationItem;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
@SessionAttributes({"harmonizationModel"})
public class HarmonizeUpdateEncounterTypeNamesController {

  private HarmonizationData harmonizationModel = null;

  @RequestMapping(
      value = "/module/eptsharmonization/harmonizeUpdateEncounterTypeNames",
      method = RequestMethod.GET)
  public ModelAndView initForm(
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel) {

    ModelAndView modelAndView = new ModelAndView();
    return modelAndView;
  }

  @RequestMapping(
      value = "/module/eptsharmonization/harmonizeUpdateEncounterTypeNames",
      method = RequestMethod.POST)
  public ModelAndView confirmHarmonizationData(
      HttpServletRequest request,
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel,
      BindingResult result) {

    ModelAndView modelAndView =
        new ModelAndView(
            "redirect:/module/eptsharmonization/harmonizeUpdateEncounterTypeNames2.form");
    return modelAndView;
  }

  @RequestMapping(
      value = {"/module/eptsharmonization/harmonizeUpdateEncounterTypeNames2"},
      method = {org.springframework.web.bind.annotation.RequestMethod.GET})
  public ModelAndView initFormProcessHarmonization(
      HttpServletRequest request,
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel) {

    ModelAndView modelAndView = new ModelAndView();
    modelAndView.addObject("harmonizationModel", harmonizationModel);
    this.harmonizationModel = harmonizationModel;
    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(
      value = {"/module/eptsharmonization/harmonizeUpdateEncounterTypeNames2"},
      method = {org.springframework.web.bind.annotation.RequestMethod.POST})
  public ModelAndView processHarmonization(
      HttpServletRequest request,
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel,
      ModelMap model,
      SessionStatus status) {

    Map<String, List<EncounterTypeDTO>> resultMap = new HashMap<String, List<EncounterTypeDTO>>();
    for (HarmonizationItem item : this.harmonizationModel.getItems()) {
      if (item.isSelected()) {
        resultMap.put((String) item.getKey(), (List<EncounterTypeDTO>) item.getValue());
      }
    }
    HarmonizationUtils.getHarmonizationEncounterTypeService()
        .saveEncounterTypesWithDifferentNames(resultMap);

    model.addAttribute("openmrs_msg", "eptsharmonization.encountertype.harmonized");
    model.addAttribute("harmonizationModel", this.harmonizationModel);
    ModelAndView modelAndView =
        new ModelAndView(
            "redirect:/module/eptsharmonization/harmonizeUpdateEncounterTypeNames3.form", model);
    status.setComplete();

    if (getHarmonizationModel().getItems().isEmpty()) {
      HarmonizeEncounterTypeController.HARMONIZED_CACHED_SUMMARY.add(
          "eptsharmonization.encountertype.harmonize.differentNamesAndSameUUIDAndID");
    }
    return modelAndView;
  }

  @RequestMapping(
      value = {"/module/eptsharmonization/harmonizeUpdateEncounterTypeNames3"},
      method = {org.springframework.web.bind.annotation.RequestMethod.GET})
  public ModelAndView initExportLog(
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel,
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg,
      ModelMap model) {

    ModelAndView modelAndView = new ModelAndView();
    modelAndView.addObject("openmrs_msg", openmrs_msg);
    model.addAttribute("harmonizationModel", this.harmonizationModel);

    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(
      value = "/module/eptsharmonization/harmonizeUpdateEncounterTypeNames3",
      method = RequestMethod.POST)
  public @ResponseBody byte[] exportLog(HttpServletRequest request, HttpServletResponse response) {

    Map<String, List<EncounterTypeDTO>> resultMap = new HashMap<String, List<EncounterTypeDTO>>();
    for (HarmonizationItem item : this.harmonizationModel.getItems()) {
      if (item.isSelected()) {
        resultMap.put((String) item.getKey(), (List<EncounterTypeDTO>) item.getValue());
      }
    }
    String LocationName = Context.getAdministrationService().getGlobalProperty("default_location");
    ByteArrayOutputStream outputStream =
        HarmonizationCSVLogUtils.generateLogForHarmonizationMapOfEncounterTypes(
            LocationName,
            resultMap,
            "Harmonized Encounter Types With different Name and Equal ID and UUID");

    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName=harmonized-encounter-types-different-names.csv");
    response.setContentLength(outputStream.size());
    return outputStream.toByteArray();
  }

  @ModelAttribute("harmonizationModel")
  public HarmonizationData getHarmonizationModel() {
    List<HarmonizationItem> items = new ArrayList<>();
    Map<String, List<EncounterTypeDTO>> data =
        HarmonizationUtils.getHarmonizationEncounterTypeService()
            .findAllEncounterTypesWithDifferentNameAndSameUUIDAndID();
    for (String key : data.keySet()) {
      items.add(new HarmonizationItem(key, data.get(key)));
    }
    return new HarmonizationData(items);
  }
}
