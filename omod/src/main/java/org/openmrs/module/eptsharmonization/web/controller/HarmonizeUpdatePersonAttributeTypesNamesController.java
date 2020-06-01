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
import org.openmrs.module.eptsharmonization.api.model.PersonAttributeTypeDTO;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationCSVLog;
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
public class HarmonizeUpdatePersonAttributeTypesNamesController {

  private HarmonizationData harmonizationModel = null;

  @RequestMapping(
      value = "/module/eptsharmonization/harmonizeUpdatePersonAttributeTypesNames",
      method = RequestMethod.GET)
  public ModelAndView initForm(
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel) {

    ModelAndView modelAndView = new ModelAndView();
    return modelAndView;
  }

  @RequestMapping(
      value = "/module/eptsharmonization/harmonizeUpdatePersonAttributeTypesNames",
      method = RequestMethod.POST)
  public ModelAndView confirmHarmonizationData(
      HttpServletRequest request,
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel,
      BindingResult result) {

    ModelAndView modelAndView =
        new ModelAndView(
            "redirect:/module/eptsharmonization/harmonizeUpdatePersonAttributeTypesNames2.form");
    return modelAndView;
  }

  @RequestMapping(
      value = {"/module/eptsharmonization/harmonizeUpdatePersonAttributeTypesNames2"},
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
      value = {"/module/eptsharmonization/harmonizeUpdatePersonAttributeTypesNames2"},
      method = {org.springframework.web.bind.annotation.RequestMethod.POST})
  public ModelAndView processHarmonization(
      HttpServletRequest request,
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel,
      ModelMap model,
      SessionStatus status) {

    Map<String, List<PersonAttributeTypeDTO>> resultMap =
        new HashMap<String, List<PersonAttributeTypeDTO>>();
    for (HarmonizationItem item : this.harmonizationModel.getItems()) {
      if (item.isSelected()) {
        resultMap.put((String) item.getKey(), (List<PersonAttributeTypeDTO>) item.getValue());
      }
    }
    HarmonizationUtils.getHarmonizationPersonAttributeTypeService()
        .savePersonAttributeTypesWithDifferentNames(resultMap);

    model.addAttribute("openmrs_msg", "eptsharmonization.personattributetype.harmonized");
    model.addAttribute("harmonizationModel", this.harmonizationModel);
    ModelAndView modelAndView =
        new ModelAndView(
            "redirect:/module/eptsharmonization/harmonizeUpdatePersonAttributeTypesNames3.form",
            model);
    status.setComplete();
    return modelAndView;
  }

  @RequestMapping(
      value = {"/module/eptsharmonization/harmonizeUpdatePersonAttributeTypesNames3"},
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
      value = "/module/eptsharmonization/harmonizeUpdatePersonAttributeTypesNames3",
      method = RequestMethod.POST)
  public @ResponseBody byte[] exportLog(HttpServletRequest request, HttpServletResponse response) {

    Map<String, List<PersonAttributeTypeDTO>> resultMap =
        new HashMap<String, List<PersonAttributeTypeDTO>>();
    for (HarmonizationItem item : this.harmonizationModel.getItems()) {
      if (item.isSelected()) {
        resultMap.put((String) item.getKey(), (List<PersonAttributeTypeDTO>) item.getValue());
      }
    }
    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
    ByteArrayOutputStream outputStream =
        HarmonizationCSVLog.generateLogForHarmonizationMapOfPersonAttributeTypes(
            defaultLocationName,
            resultMap,
            "Harmonized PersonAttributeTypes With different Name and equal ID and UUID");

    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName=harmonized-person-attribute-types-different-names-and-equal-id-and-uuid.csv");
    response.setContentLength(outputStream.size());
    return outputStream.toByteArray();
  }

  @ModelAttribute("harmonizationModel")
  public HarmonizationData getHarmonizationModel() {
    List<HarmonizationItem> items = new ArrayList<>();
    Map<String, List<PersonAttributeTypeDTO>> data =
        HarmonizationUtils.getHarmonizationPersonAttributeTypeService()
            .findAllPersonAttributeTypesWithDifferentNameAndSameUUIDAndID();
    for (String key : data.keySet()) {
      items.add(new HarmonizationItem(key, data.get(key)));
    }
    return new HarmonizationData(items);
  }
}
