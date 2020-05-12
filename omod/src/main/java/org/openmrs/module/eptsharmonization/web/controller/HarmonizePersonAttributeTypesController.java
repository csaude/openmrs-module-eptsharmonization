package org.openmrs.module.eptsharmonization.web.controller;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
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
  public ModelAndView getPersonAttributeTypesList(
      HttpSession session,
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg) {

    session.removeAttribute("harmonizationModel");

    ModelAndView modelAndView = new ModelAndView();

    HarmonizationPersonAttributeTypeService personAttributeTypeService =
        HarmonizationUtils.getHarmonizationPersonAttributeTypeService();

    List<PersonAttributeTypeDTO> onlyMetadataPersonAttributeTypes =
        personAttributeTypeService.findAllMetadataPersonAttributeTypesNotInProductionServer();
    List<PersonAttributeTypeDTO> OnlyProductionPersonAttributeTypes =
        personAttributeTypeService.findAllProductionPersonAttibuteTypesNotInMetadataServer();
    Map<String, List<PersonAttributeTypeDTO>> personAttributeTypesWithDifferentIDsSameUUIDs =
        personAttributeTypeService.findAllPersonAttributeTypesWithDifferentIDAndSameUUID();
    final Map<String, List<PersonAttributeTypeDTO>> personAttributeTypesWithDifferentNames =
        personAttributeTypeService.findAllPersonAttributeTypesWithDifferentNameAndSameUUIDAndID();

    modelAndView.addObject("onlyMetadataPersonAttributeTypes", onlyMetadataPersonAttributeTypes);
    modelAndView.addObject(
        "OnlyProductionPersonAttributeTypes", OnlyProductionPersonAttributeTypes);
    modelAndView.addObject(
        "personAttributeTypesPartialEqual", personAttributeTypesWithDifferentIDsSameUUIDs);
    modelAndView.addObject(
        "personAttributeTypesWithDifferentNames", personAttributeTypesWithDifferentNames);

    modelAndView.addObject("openmrs_msg", openmrs_msg);

    return modelAndView;
  }
}
