package org.openmrs.module.eptsharmonization.web.controller;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsharmonization.HarmonizationUtils;
import org.openmrs.module.eptsharmonization.api.model.PersonAttributeTypeDTO;
import org.openmrs.module.eptsharmonization.web.bean.HarmonizationCSVLogUtils;
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

@Controller("eptsharmonization.harmonizeAddNewPersonAttributeTypes")
@SessionAttributes({"harmonizationModel"})
public class HarmonizeAddNewPersonAttributeTypesController {

  private HarmonizationData harmonizationModel = null;

  @RequestMapping(
      value = "/module/eptsharmonization/harmonizeAddNewPersonAttributeTypes",
      method = RequestMethod.GET)
  public ModelAndView initForm(
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel) {

    ModelAndView modelAndView = new ModelAndView();
    return modelAndView;
  }

  @RequestMapping(
      value = "/module/eptsharmonization/harmonizeAddNewPersonAttributeTypes",
      method = RequestMethod.POST)
  public ModelAndView confirmHarmonizationData(
      HttpServletRequest request,
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel) {

    ModelAndView modelAndView =
        new ModelAndView(
            "redirect:/module/eptsharmonization/harmonizeAddNewPersonAttributeTypes2.form");
    return modelAndView;
  }

  @RequestMapping(
      value = "/module/eptsharmonization/harmonizeAddNewPersonAttributeTypes2",
      method = org.springframework.web.bind.annotation.RequestMethod.GET)
  public ModelAndView initFormProcessHarmonization(
      HttpServletRequest request,
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel) {

    ModelAndView modelAndView = new ModelAndView();
    modelAndView.addObject("harmonizationModel", harmonizationModel);
    this.harmonizationModel = harmonizationModel;

    for (HarmonizationItem item : harmonizationModel.getItems()) {
      PersonAttributeTypeDTO personAttributeType = (PersonAttributeTypeDTO) item.getValue();
      item.setEncountersCount(
          HarmonizationUtils.getHarmonizationPersonAttributeTypeService()
              .getNumberOfAffectedPersonAttributes(personAttributeType));
    }
    return modelAndView;
  }

  @RequestMapping(
      value = "/module/eptsharmonization/harmonizeAddNewPersonAttributeTypes2",
      method = org.springframework.web.bind.annotation.RequestMethod.POST)
  public ModelAndView processHarmonization(
      HttpServletRequest request,
      @ModelAttribute("harmonizationModel") HarmonizationData harmonizationModel,
      ModelMap model) {

    List<PersonAttributeTypeDTO> list = new ArrayList<>();
    for (HarmonizationItem item : this.harmonizationModel.getItems()) {
      if (item.isSelected()) {
        list.add((PersonAttributeTypeDTO) item.getValue());
      }
    }
    HarmonizationUtils.getHarmonizationPersonAttributeTypeService()
        .saveNewPersonAttributeTypesFromMDS(list);

    model.addAttribute("openmrs_msg", "eptsharmonization.encountertype.harmonized");
    model.addAttribute("harmonizationModel", this.harmonizationModel);

    model.addAttribute("openmrs_msg", "eptsharmonization.PersonAttributeType.harmonized");
    model.addAttribute("harmonizationModel", this.harmonizationModel);
    ModelAndView modelAndView =
        new ModelAndView(
            "redirect:/module/eptsharmonization/harmonizeAddNewPersonAttributeTypes3.form", model);
    return modelAndView;
  }

  @RequestMapping(
      value = "/module/eptsharmonization/harmonizeAddNewPersonAttributeTypes3",
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
      value = "/module/eptsharmonization/harmonizeAddNewPersonAttributeTypes3",
      method = org.springframework.web.bind.annotation.RequestMethod.POST)
  public @ResponseBody byte[] exportLog(HttpServletRequest request, HttpServletResponse response) {

    List<PersonAttributeTypeDTO> list = new ArrayList<>();
    for (HarmonizationItem item : this.harmonizationModel.getItems()) {
      if (item.isSelected()) {
        list.add((PersonAttributeTypeDTO) item.getValue());
      }
    }
    String defaultLocationName =
        Context.getAdministrationService().getGlobalProperty("default_location");
    ByteArrayOutputStream outputStream =
        HarmonizationCSVLogUtils.generateLogForNewHarmonizedFromMDSPersonAttributeTypes(
            defaultLocationName,
            list,
            "Created New Person Attribute Types from Metadata Server to Production Server");

    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName=harmonized-person-attribute-types-new-from-mds.csv");
    response.setContentLength(outputStream.size());
    return outputStream.toByteArray();
  }

  @ModelAttribute("harmonizationModel")
  public HarmonizationData getHarmonizationModel() {
    List<HarmonizationItem> items = new ArrayList<>();
    List<PersonAttributeTypeDTO> data =
        HarmonizationUtils.getHarmonizationPersonAttributeTypeService()
            .findAllMetadataPersonAttributeTypesNotInProductionServer();

    for (PersonAttributeTypeDTO PersonAttributeTypeDTO : data) {
      items.add(new HarmonizationItem(PersonAttributeTypeDTO.getUuid(), PersonAttributeTypeDTO));
    }
    return new HarmonizationData(items);
  }
}
