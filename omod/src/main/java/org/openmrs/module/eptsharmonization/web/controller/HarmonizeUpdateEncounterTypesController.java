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
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationCSVLog;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationData;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationItem;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

@Controller
@SessionAttributes({"harmonizationModel"})
public class HarmonizeUpdateEncounterTypesController {

  private HarmonizationData harmonizationModel = null;

  @RequestMapping(
      value = "/module/eptsharmonization/harmonizeUpdateEncounterTypes",
      method = RequestMethod.GET)
  public ModelAndView initForm(
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel) {

    ModelAndView modelAndView = new ModelAndView();
    return modelAndView;
  }

  @RequestMapping(
      value = "/module/eptsharmonization/harmonizeUpdateEncounterTypes",
      method = RequestMethod.POST)
  public ModelAndView confirmHarmonizationData(
      HttpServletRequest request,
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel) {

    ModelAndView modelAndView =
        new ModelAndView("redirect:/module/eptsharmonization/harmonizeUpdateEncounterTypes2.form");
    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(
      value = {"/module/eptsharmonization/harmonizeUpdateEncounterTypes2"},
      method = {org.springframework.web.bind.annotation.RequestMethod.GET})
  public ModelAndView initFormProcessHarmonization(
      HttpServletRequest request,
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel) {

    this.harmonizationModel = harmonizationModel;

    for (HarmonizationItem item : this.harmonizationModel.getItems()) {
      if (item.isSelected()) {
        List<EncounterTypeDTO> encounterTypes = (List<EncounterTypeDTO>) item.getValue();
        EncounterTypeDTO encouterType = encounterTypes.get(1);
        item.setEncountersCount(
            HarmonizationUtils.getHarmonizationEncounterTypeService()
                .getNumberOfAffectedEncounters(encouterType));
        item.setFormsCount(
            HarmonizationUtils.getHarmonizationEncounterTypeService()
                .getNumberOfAffectedForms(encouterType));
      }
    }
    ModelAndView modelAndView = new ModelAndView();
    modelAndView.addObject("harmonizationModel", harmonizationModel);
    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(
      value = {"/module/eptsharmonization/harmonizeUpdateEncounterTypes2"},
      method = {org.springframework.web.bind.annotation.RequestMethod.POST})
  public ModelAndView processHarmonization(
      HttpServletRequest request,
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel,
      ModelMap model) {

    Map<String, List<EncounterTypeDTO>> selectedRows = new HashMap<>();
    for (HarmonizationItem item : this.harmonizationModel.getItems()) {

      if (item.isSelected()) {
        selectedRows.put((String) item.getKey(), (List<EncounterTypeDTO>) item.getValue());
      }
    }
    HarmonizationUtils.getHarmonizationEncounterTypeService()
        .saveEncounterTypesWithDifferentIDAndEqualUUID(selectedRows);

    model.addAttribute("openmrs_msg", "eptsharmonization.encountertype.harmonized");
    model.addAttribute("harmonizationModel", this.harmonizationModel);
    ModelAndView modelAndView =
        new ModelAndView(
            "redirect:/module/eptsharmonization/harmonizeUpdateEncounterTypes3.form", model);

    if (getHarmonizationModel().getItems().isEmpty()) {
      HarmonizeEncounterTypeController.HARMONIZED_CACHED_SUMMARY.add(
          "eptsharmonization.encountertype.harmonize.differentID.andEqualUUID");
    }
    return modelAndView;
  }

  @RequestMapping(
      value = {"/module/eptsharmonization/harmonizeUpdateEncounterTypes3"},
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
      value = {"/module/eptsharmonization/harmonizeUpdateEncounterTypes3"},
      method = {org.springframework.web.bind.annotation.RequestMethod.POST})
  public @ResponseBody byte[] exportLog(HttpServletRequest request, HttpServletResponse response) {

    Map<String, List<EncounterTypeDTO>> selectedRows = new HashMap<>();
    for (HarmonizationItem item : this.harmonizationModel.getItems()) {

      if (item.isSelected()) {
        selectedRows.put((String) item.getKey(), (List<EncounterTypeDTO>) item.getValue());
      }
    }

    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
    ByteArrayOutputStream outputStream =
        HarmonizationCSVLog.generateLogForHarmonizationMapOfEncounterTypes(
            defaultLocationName,
            selectedRows,
            "Harmonized Encounter Types With different ID and equal UUID");

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
            .findAllEncounterTypesWithDifferentIDAndSameUUID();
    for (String key : data.keySet()) {
      items.add(new HarmonizationItem(key, data.get(key)));
    }
    return new HarmonizationData(items);
  }
}
