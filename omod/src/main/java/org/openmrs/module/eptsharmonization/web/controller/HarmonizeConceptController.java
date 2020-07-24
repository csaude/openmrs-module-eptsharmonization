package org.openmrs.module.eptsharmonization.web.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.eptsharmonization.api.HarmonizationConceptService;
import org.openmrs.module.eptsharmonization.api.model.ConceptDTO;
import org.openmrs.module.eptsharmonization.web.bean.ConceptHarmonizationCSVLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/** @uthor Willa Mhawila<a.mhawila@gmail.com> on 6/2/20. */
@Controller(HarmonizeConceptController.CONTROLLER_NAME)
public class HarmonizeConceptController {
  public static final String CONTROLLER_NAME = "eptsharmonization.harmonizeConceptController";
  public static final String ANALYSIS_STEP = "/module/eptsharmonization/harmonizeConcept";
  private static final String EXPORT_CONCEPTS_FOR_CRB = "/module/eptsharmonization/exportConcepts";
  private static final String EXPORT_CONCEPTS_LOG =
      "/module/eptsharmonization/exportConceptsHarmonizationLog";

  private static final Logger LOGGER = LoggerFactory.getLogger(HarmonizeConceptController.class);
  private static String defaultLocation;

  private HarmonizationConceptService harmonizationConceptService;
  private MessageSourceService messageSourceService;
  private AdministrationService adminService;
  // private static ConceptHarmonizationCSVLog.Builder logBuilder;

  @Autowired
  public void setHarmonizationConceptService(
      HarmonizationConceptService harmonizationConceptService) {
    this.harmonizationConceptService = harmonizationConceptService;
  }

  @Autowired
  public void setMessageSourceService(MessageSourceService messageSourceService) {
    this.messageSourceService = messageSourceService;
  }

  @Autowired
  public void setAdminService(AdministrationService adminService) {
    this.adminService = adminService;
  }

  @PostConstruct
  public void prepare() {
    defaultLocation = adminService.getGlobalProperty("default_location");
  }

  @RequestMapping(value = ANALYSIS_STEP, method = RequestMethod.GET)
  public ModelAndView conceptsHarmonyAnalysis(
      HttpSession session,
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg) {
    List<String> summaries = new ArrayList<>();
    ModelAndView modelAndView = new ModelAndView();

    List<ConceptDTO> missingInMDS = harmonizationConceptService.findPDSConceptsNotInMDS();
    LOGGER.debug(
        "Number of concepts in production and not in metadata server is %s", missingInMDS.size());

    List<ConceptDTO> missingInPDS = harmonizationConceptService.findMDSConceptsNotInPDS();
    LOGGER.debug(
        "Number of concepts in metadata server and not in production server is %s",
        missingInPDS.size());

    if (!missingInMDS.isEmpty()) {
      String summary =
          messageSourceService.getMessage("eptsharmonization.concept.harmonize.hasMissingInMDS");
      summaries.add(summary);
    }

    if (!missingInPDS.isEmpty()) {
      String summary =
          messageSourceService.getMessage("eptsharmonization.concept.harmonize.hasMissingInPDS");
      summaries.add(summary);
    }

    if (missingInPDS.isEmpty() && missingInMDS.isEmpty()) {
      String message =
          messageSourceService.getMessage(
              "eptsharmonization.concept.harmonize.noMissingInBothPDSAndMDS");
      summaries.add(message);
    }
    // Retain names and description in current locale
    retainCurrentLocale(missingInMDS);
    modelAndView.addObject("missingInMDS", missingInMDS);
    modelAndView.addObject("summaries", summaries);
    return modelAndView;
  }

  @RequestMapping(
      value = EXPORT_CONCEPTS_LOG,
      method = RequestMethod.POST,
      produces = MediaType.TEXT_PLAIN_VALUE)
  public void exportLog(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    File file = new File("harmonizationConceptLog");
    FileInputStream fis = new FileInputStream(file);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    byte[] buf = new byte[1024];
    try {
      for (int readNum; (readNum = fis.read(buf)) != -1; ) {
        outputStream.write(buf, 0, readNum);
      }
    } catch (IOException ex) {

    }
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName="
            + this.getFormattedLocationName(defaultLocation)
            + "-concept_harmonization-log.csv");
    response.setHeader("Cache-Control", "no-cache");
    response.getWriter().write(outputStream.toString(StandardCharsets.ISO_8859_1.name()));
  }

  @RequestMapping(value = EXPORT_CONCEPTS_FOR_CRB, method = RequestMethod.POST)
  public void exportConcepts(
      HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model)
      throws IOException {

    List<ConceptDTO> productionItemsToExport =
        harmonizationConceptService.findPDSConceptsNotInMDS();

    retainCurrentLocale(productionItemsToExport);

    ByteArrayOutputStream outputStream =
        ConceptHarmonizationCSVLog.exportConceptLogs(defaultLocation, productionItemsToExport);
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName="
            + this.getFormattedLocationName(defaultLocation)
            + "-concepts_harmonization-export.csv");
    response.setHeader("Cache-Control", "no-cache");
    response.getWriter().write(outputStream.toString(StandardCharsets.ISO_8859_1.name()));
  }

  private void retainCurrentLocale(List<ConceptDTO> conceptDTOs) {
    Locale curLocale = Context.getLocale();
    for (ConceptDTO conceptDTO : conceptDTOs) {
      Iterator<ConceptDTO.Name> nameIterator = conceptDTO.getNames().iterator();
      while (nameIterator.hasNext()) {
        ConceptDTO.Name name = nameIterator.next();
        if (!curLocale.equals(name.getLocale())) {
          if (conceptDTO.getNames().size() > 1) {
            nameIterator.remove();
          }
        }
      }

      Iterator<ConceptDTO.Description> descriptionIterator =
          conceptDTO.getDescriptions().iterator();
      while (descriptionIterator.hasNext()) {
        ConceptDTO.Description description = descriptionIterator.next();
        if (!curLocale.equals(description.getLocale())) {
          if (conceptDTO.getDescriptions().size() > 1) {
            descriptionIterator.remove();
          }
        }
      }
    }
  }

  private String getFormattedLocationName(String defaultLocationName) {
    if (defaultLocationName == null) {
      defaultLocationName = StringUtils.EMPTY;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(defaultLocationName.replaceAll(" ", "_"));
    return sb.toString();
  }
}
