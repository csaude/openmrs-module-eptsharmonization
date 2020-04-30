package org.openmrs.module.eptsharmonization.web.controller;

import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.eptsharmonization.HarmonizationUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationPersonAttributeTypeService;
import org.openmrs.module.eptsharmonization.api.model.PersonAttributeTypeDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HarmonizePersonAttributeTypesController {

  protected final Log log = LogFactory.getLog(getClass());

  @RequestMapping(
      value = {"/module/eptsharmonization/harmonizePersonAttributeTypesList"},
      method = {org.springframework.web.bind.annotation.RequestMethod.GET})
  public ModelAndView getAffinityTypeList(
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg) {
    ModelAndView modelAndView = new ModelAndView();

    HarmonizationPersonAttributeTypeService personAttributeTypeService =
        HarmonizationUtils.getHarmonizationPersonAttributeTypeService();

    List<PersonAttributeTypeDTO> onlyMetadataPersonAttributeTypes =
        personAttributeTypeService.findAllMetadataPersonAttributeTypesNotInProductionServer();
    List<PersonAttributeTypeDTO> OnlyProductionPersonAttributeTypes =
        personAttributeTypeService.findAllProductionPersonAttibuteTypesNotInMetadataServer();
    List<PersonAttributeTypeDTO> mdsPersonAttributeTypesPartialEqual =
        personAttributeTypeService
            .findAllMetadataPersonAttributeTypesPartialEqualsToProductionServer();
    List<PersonAttributeTypeDTO> pdsPersonAttributeTypesPartialEqual =
        personAttributeTypeService
            .findAllProductionPersonAttributeTypesPartialEqualsToMetadataServer();

    Collections.sort(mdsPersonAttributeTypesPartialEqual);
    Collections.sort(pdsPersonAttributeTypesPartialEqual);

    modelAndView.addObject("onlyMetadataPersonAttributeTypes", onlyMetadataPersonAttributeTypes);
    modelAndView.addObject(
        "OnlyProductionPersonAttributeTypes", OnlyProductionPersonAttributeTypes);
    modelAndView.addObject(
        "mdsPersonAttributeTypesPartialEqual", mdsPersonAttributeTypesPartialEqual);
    modelAndView.addObject(
        "pdsPersonAttributeTypesPartialEqual", pdsPersonAttributeTypesPartialEqual);

    modelAndView.addObject("openmrs_msg", openmrs_msg);

    return modelAndView;
  }

  @RequestMapping(
      value = {"/module/eptsharmonization/harmonizePersonAttributeTypesList"},
      method = {org.springframework.web.bind.annotation.RequestMethod.POST})
  public ModelAndView processForm(
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg) {
    ModelAndView modelAndView = new ModelAndView();

    HarmonizationPersonAttributeTypeService personAttributeTypeService =
        HarmonizationUtils.getHarmonizationPersonAttributeTypeService();

    List<PersonAttributeTypeDTO> onlyMetadataPersonAttributeTypes =
        personAttributeTypeService.findAllMetadataPersonAttributeTypesNotInProductionServer();
    List<PersonAttributeTypeDTO> OnlyProductionPersonAttributeTypes =
        personAttributeTypeService.findAllProductionPersonAttibuteTypesNotInMetadataServer();
    List<PersonAttributeTypeDTO> mdsPersonAttributeTypesPartialEqual =
        personAttributeTypeService
            .findAllMetadataPersonAttributeTypesPartialEqualsToProductionServer();
    List<PersonAttributeTypeDTO> pdsPersonAttributeTypesPartialEqual =
        personAttributeTypeService
            .findAllProductionPersonAttributeTypesPartialEqualsToMetadataServer();

    Collections.sort(mdsPersonAttributeTypesPartialEqual);
    Collections.sort(pdsPersonAttributeTypesPartialEqual);

    modelAndView.addObject("onlyMetadataPersonAttributeTypes", onlyMetadataPersonAttributeTypes);
    modelAndView.addObject(
        "OnlyProductionPersonAttributeTypes", OnlyProductionPersonAttributeTypes);
    modelAndView.addObject(
        "mdsPersonAttributeTypesPartialEqual", mdsPersonAttributeTypesPartialEqual);
    modelAndView.addObject(
        "pdsPersonAttributeTypesPartialEqual", pdsPersonAttributeTypesPartialEqual);

    modelAndView.addObject("openmrs_msg", openmrs_msg);

    return modelAndView;
  }
}
