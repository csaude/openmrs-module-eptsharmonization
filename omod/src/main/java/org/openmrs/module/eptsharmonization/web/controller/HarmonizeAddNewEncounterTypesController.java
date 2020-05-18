package org.openmrs.module.eptsharmonization.web.controller;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

@Controller
@SessionAttributes({"harmonizationModel"})
public class HarmonizeAddNewEncounterTypesController {

  private HarmonizationData harmonizationModel = null;

  @RequestMapping(
      value = "/module/eptsharmonization/harmonizeAddNewEncounterTypes",
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
              .getNumberOfAffectedEncounters(encouterType));
      item.setFormsCount(
          HarmonizationUtils.getHarmonizationEncounterTypeService()
              .getNumberOfAffectedForms(encouterType));
    }

    return modelAndView;
  }

  @RequestMapping(
      value = "/module/eptsharmonization/harmonizeAddNewEncounterTypes",
      method = org.springframework.web.bind.annotation.RequestMethod.POST)
  public ModelAndView processHarmonization(
      HttpServletRequest request,
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel,
      ModelMap model) {

    List<EncounterTypeDTO> list = new ArrayList<>();
    for (HarmonizationItem item : this.harmonizationModel.getItems()) {
      list.add((EncounterTypeDTO) item.getValue());
    }
    HarmonizationUtils.getHarmonizationEncounterTypeService().saveNewEncounterTypesFromMDS(list);

    model.addAttribute("openmrs_msg", "eptsharmonization.encountertype.harmonized");
    model.addAttribute("harmonizationModel", this.harmonizationModel);
    ModelAndView modelAndView =
        new ModelAndView(
            "redirect:/module/eptsharmonization/harmonizeAddNewEncounterTypes2.form", model);
    return modelAndView;
  }

  @RequestMapping(
      value = "/module/eptsharmonization/harmonizeAddNewEncounterTypes2",
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
      value = "/module/eptsharmonization/harmonizeAddNewEncounterTypes2",
      method = org.springframework.web.bind.annotation.RequestMethod.POST)
  public @ResponseBody byte[] exportLog(HttpServletRequest request, HttpServletResponse response) {

    List<EncounterTypeDTO> list = new ArrayList<>();
    for (HarmonizationItem item : this.harmonizationModel.getItems()) {
      list.add((EncounterTypeDTO) item.getValue());
    }
    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
    ByteArrayOutputStream outputStream =
        HarmonizationCSVLogUtils.generateLogForNewHarmonizedFromMDSEncounterTypes(
            defaultLocationName,
            list,
            "Created New Entries from Metadata Server to Production Server");

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
    List<EncounterTypeDTO> data =
        HarmonizationUtils.getHarmonizationEncounterTypeService()
            .findAllMetadataEncounterNotContainedInProductionServer();

    for (EncounterTypeDTO encounterTypeDTO : data) {
      items.add(new HarmonizationItem(encounterTypeDTO.getUuid(), encounterTypeDTO));
    }
    return new HarmonizationData(items);
  }
}
