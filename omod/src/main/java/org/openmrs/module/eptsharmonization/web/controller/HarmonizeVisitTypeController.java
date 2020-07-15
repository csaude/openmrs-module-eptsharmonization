package org.openmrs.module.eptsharmonization.web.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.VisitType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.VisitService;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationVisitTypeService;
import org.openmrs.module.eptsharmonization.api.model.VisitTypeDTO;
import org.openmrs.module.eptsharmonization.web.bean.VisitTypeBean;
import org.openmrs.module.eptsharmonization.web.bean.VisitTypeHarmonizationCSVLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

/** @uthor Willa Mhawila<a.mhawila@gmail.com> on 6/2/20. */
@Controller(HarmonizeVisitTypeController.CONTROLLER_NAME)
@SessionAttributes({"removedMappableVisitTypes"})
public class HarmonizeVisitTypeController {
  public static final String CONTROLLER_NAME = "eptsharmonization.harmonizeVisitTypeController";
  public static final String ANALYSIS_STEP = "/module/eptsharmonization/harmonizeVisitType";
  public static final String MANDATORY_STEP =
      "/module/eptsharmonization/mandatoryVisitTypeHarmonization";
  public static final String MANUAL_MAPPING_STEP =
      "/module/eptsharmonization/manualMappingVisitTypeHarmonization";
  public static final String ADD_VISIT_TYPE_MAPPING =
      "/module/eptsharmonization/addVisitTypeMapping";
  private static final String REMOVE_VISIT_TYPE_MAPPING =
      "/module/eptsharmonization/removeVisitTypeMapping";
  private static final String EXPORT_VISIT_TYPES_LOG =
      "/module/eptsharmonization/exportVisitTypesHarmonizationLog";
  private static final String EXPORT_VISIT_TYPES_FOR_CRB =
      "/module/eptsharmonization/exportVisitTypes";

  public static List<String> HARMONIZED_CACHED_SUMMARY = new ArrayList<>();
  private static final Logger LOGGER = LoggerFactory.getLogger(HarmonizeVisitTypeController.class);
  private static String defaultLocation;

  private HarmonizationVisitTypeService harmonizationVisitTypeService;
  private MessageSourceService messageSourceService;
  private VisitService visitService;
  private AdministrationService adminService;
  private static VisitTypeHarmonizationCSVLog.Builder logBuilder;

  @Autowired
  public void setHarmonizationVisitTypeService(
      HarmonizationVisitTypeService harmonizationVisitTypeService) {
    this.harmonizationVisitTypeService = harmonizationVisitTypeService;
  }

  @Autowired
  public void setMessageSourceService(MessageSourceService messageSourceService) {
    this.messageSourceService = messageSourceService;
  }

  @Autowired
  public void setVisitService(VisitService visitService) {
    this.visitService = visitService;
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
  public ModelAndView visitTypesHarmonyAnalysis(
      HttpSession session,
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg) {
    ModelAndView modelAndView = new ModelAndView();

    // Get missing visit types.
    List<VisitTypeDTO> missingInPDS =
        harmonizationVisitTypeService
            .findAllMetadataVisitTypesNotSharingUuidWithAnyFromProduction();
    LOGGER.debug("Number of visit types missing in production found is %s", missingInPDS.size());

    List<VisitTypeDTO> notInMDSNotInUse =
        harmonizationVisitTypeService
            .findAllUselessProductionVisitTypesNotSharingUuidWithAnyFromMetadata();
    LOGGER.debug(
        "Number of useless visit types in production and not in metadata server is %s",
        notInMDSNotInUse.size());

    Map<String, List<VisitTypeDTO>> sameIdAndUuidDifferentNames =
        harmonizationVisitTypeService.findAllVisitTypesWithDifferentNameAndSameUUIDAndID();
    LOGGER.debug(
        "Number of visit types with same id and uuid but different names is %s",
        sameIdAndUuidDifferentNames.size());

    Map<List<VisitTypeDTO>, Integer> sameIdAndUuidDifferentNamesWithCountAffected = new HashMap<>();
    // First entry is from Production server
    for (List<VisitTypeDTO> matchList : sameIdAndUuidDifferentNames.values()) {
      Integer affectedRows =
          harmonizationVisitTypeService.getNumberOfAffectedVisits(matchList.get(0));
      sameIdAndUuidDifferentNamesWithCountAffected.put(matchList, affectedRows);
    }

    Map<String, List<VisitTypeDTO>> sameUuidDifferentIds =
        harmonizationVisitTypeService.findAllVisitTypesWithDifferentIDAndSameUUID();
    LOGGER.debug(
        "Number of visit types with same uuid but different ids and/or names is %s",
        sameUuidDifferentIds.size());
    Map<List<VisitTypeDTO>, Integer> sameUuidDifferentIdsWithAffectedRows = new HashMap<>();
    // First entry is from Production server
    for (List<VisitTypeDTO> matchList : sameUuidDifferentIds.values()) {
      Integer affectedRows =
          harmonizationVisitTypeService.getNumberOfAffectedVisits(matchList.get(0));
      sameUuidDifferentIdsWithAffectedRows.put(matchList, affectedRows);
    }

    if (missingInPDS.isEmpty()
        && notInMDSNotInUse.isEmpty()
        && sameIdAndUuidDifferentNamesWithCountAffected.isEmpty()
        && sameUuidDifferentIdsWithAffectedRows.isEmpty()) {
      return getRedirectToMandatoryStep();
    }

    modelAndView.addObject("missingInPDS", missingInPDS);
    modelAndView.addObject("notInMDSNotUsed", notInMDSNotInUse);
    modelAndView.addObject(
        "sameIdAndUuidDifferentNames", sameIdAndUuidDifferentNamesWithCountAffected);
    modelAndView.addObject("sameUuidDifferentIdsAndNames", sameUuidDifferentIdsWithAffectedRows);
    return modelAndView;
  }

  @RequestMapping(
      value = MANDATORY_STEP,
      method = {RequestMethod.POST, RequestMethod.GET})
  public ModelAndView mandatoryStep(HttpSession session, HttpServletRequest request)
      throws IOException {
    ModelAndView modelAndView = new ModelAndView();

    List<VisitTypeDTO> missingInPDS =
        harmonizationVisitTypeService
            .findAllMetadataVisitTypesNotSharingUuidWithAnyFromProduction();

    if (missingInPDS.size() > 0) {
      LOGGER.debug("Adding new %s visit types from metadata server", missingInPDS.size());

      harmonizationVisitTypeService.saveNewVisitTypesFromMetadata(missingInPDS);
      String message =
          messageSourceService.getMessage(
              "eptsharmonization.visittype.harmonize.newVisitTypeAdded");
      HARMONIZED_CACHED_SUMMARY.add(message + " " + missingInPDS.size());
    }

    VisitTypeHarmonizationCSVLog.Builder logBuilder = getLogBuilder();
    logBuilder.appendLogForNewHarmonizedFromMDSVisitTypes(missingInPDS);

    List<VisitTypeDTO> notInMDSNotInUse =
        harmonizationVisitTypeService
            .findAllUselessProductionVisitTypesNotSharingUuidWithAnyFromMetadata();
    if (notInMDSNotInUse.size() > 0) {
      LOGGER.debug("Deleting %s useless visit types from production", notInMDSNotInUse.size());
      harmonizationVisitTypeService.deleteVisitTypesFromProduction(notInMDSNotInUse);
      String message =
          messageSourceService.getMessage(
              "eptsharmonization.visittype.harmonize.visitTypesDeleted");
      HARMONIZED_CACHED_SUMMARY.add(message + " " + notInMDSNotInUse.size());
      logBuilder.appendLogForDeleteFromProductionServer(notInMDSNotInUse);
    }

    Map<String, List<VisitTypeDTO>> sameIdAndUuidDifferentNames =
        harmonizationVisitTypeService.findAllVisitTypesWithDifferentNameAndSameUUIDAndID();
    if (sameIdAndUuidDifferentNames.size() > 0) {
      LOGGER.debug(
          "Number of visit types with same id and uuid but different names is %s",
          sameIdAndUuidDifferentNames.size());
      Map<String, List<VisitTypeDTO>> toOverwrite =
          filterConfirmedOverwrites(sameIdAndUuidDifferentNames, request);
      if (toOverwrite.size() > 0) {
        LOGGER.debug(
            "Visit types with same IDs and UUIDs to be overwritten are %s", toOverwrite.size());
        harmonizationVisitTypeService.saveVisitTypesWithDifferentNames(toOverwrite);

        // Remove all overwritten ones.
        for (String uuid : toOverwrite.keySet()) {
          sameIdAndUuidDifferentNames.remove(uuid);
        }

        String message =
            messageSourceService.getMessage(
                "eptsharmonization.visittype.harmonize.visitTypesUpdated");
        HARMONIZED_CACHED_SUMMARY.add(message + " " + toOverwrite.size());
        logBuilder.appendLogForUpdatedVisitNames(toOverwrite);
      }
    }

    Map<String, List<VisitTypeDTO>> sameUuidDifferentIds =
        harmonizationVisitTypeService.findAllVisitTypesWithDifferentIDAndSameUUID();
    if (sameUuidDifferentIds.size() > 0) {
      Map<String, List<VisitTypeDTO>> toOverwrite =
          filterConfirmedOverwrites(sameUuidDifferentIds, request);
      if (toOverwrite.size() > 0) {
        LOGGER.debug(
            "Visit types with same uuid but different ids and/or names being overwritten with metadata %s",
            toOverwrite.size());
        harmonizationVisitTypeService
            .updateVisitTypesFromProductionWithSameUuidWithInformationFromMetadata(toOverwrite);
        String message =
            messageSourceService.getMessage(
                "eptsharmonization.visittype.harmonize.visitTypeSameUuidUpdated");
        HARMONIZED_CACHED_SUMMARY.add(message + " " + toOverwrite.size());
        logBuilder.appendLogForVisitTypesWithDiferrentIdsAndEqualUUID(toOverwrite);
        // Remove overwritten ones.
        for (String uuid : toOverwrite.keySet()) {
          sameUuidDifferentIds.remove(uuid);
        }
      }
    }

    // Overwrite the remaining same UUID different IDs anyway shifting the already existing ones to
    // new ID and UUID.
    // Because metadata always wins. If one needs to keep them they may be mapped or exported as new
    // suggesting to CRB
    harmonizationVisitTypeService.replacePDSVisitTypesWithSameUuidWithThoseFromMDS(
        sameIdAndUuidDifferentNames);
    harmonizationVisitTypeService.replacePDSVisitTypesWithSameUuidWithThoseFromMDS(
        sameUuidDifferentIds);

    Map<VisitTypeDTO, Integer> mappableVisitTypes = getMappableVisitTypes(session);

    if (mappableVisitTypes != null && mappableVisitTypes.size() > 0) {
      List<VisitType> allVisitTypes = visitService.getAllVisitTypes(true);
      List<VisitTypeDTO> toRemoveFromAll = new ArrayList<>(mappableVisitTypes.keySet());
      allVisitTypes.removeAll(DTOUtils.fromVisitTypeDTOs(toRemoveFromAll));
      modelAndView.addObject("availableMappingTypes", DTOUtils.fromVisitTypes(allVisitTypes));
      session.setAttribute("mappableVisitTypes", mappableVisitTypes);

      // Copy mappables under a different name.
      session.setAttribute("productionVisitTypesToExport", mappableVisitTypes);
      modelAndView.addObject(
          "mappableVisitTypesList", new ArrayList<>(mappableVisitTypes.keySet()));
    } else {
      session.removeAttribute("mappableVisitTypes");
      session.removeAttribute("productionVisitTypesToExport");
    }

    modelAndView.addObject("harmonizedVTSummary", HARMONIZED_CACHED_SUMMARY);
    // Write log to file.
    logBuilder.build();

    if (mappableVisitTypes.size() == 0) {
      modelAndView.addObject("harmonizationCompleted", true);
    }

    return modelAndView;
  }

  @RequestMapping(value = ADD_VISIT_TYPE_MAPPING, method = RequestMethod.POST)
  public ModelAndView addVisitTypeMapping(
      HttpSession session, @ModelAttribute("visitTypeBean") VisitTypeBean visitTypeBean) {

    Map<VisitTypeDTO, Integer> mappableVisitTypes = getMappableVisitTypes(session);
    ModelAndView modelAndView = getRedirectToMandatoryStep();

    if (visitTypeBean.getKey() == null || StringUtils.isEmpty(((String) visitTypeBean.getKey()))) {
      modelAndView.addObject(
          "errorRequiredPDSValue", "eptsharmonization.error.visitTypeForMapping.required");
      return modelAndView;
    }
    if (visitTypeBean.getValue() == null
        || StringUtils.isEmpty(((String) visitTypeBean.getValue()))) {
      modelAndView.addObject(
          "errorRequiredMdsValue", "eptsharmonization.error.visitTypeForMapping.required");
      return modelAndView;
    }

    VisitType pdsVisitType = visitService.getVisitTypeByUuid((String) visitTypeBean.getKey());
    VisitType mdsVisitType = visitService.getVisitTypeByUuid((String) visitTypeBean.getValue());

    Map<VisitType, VisitType> manualVisitTypeMappings =
        (Map<VisitType, VisitType>) session.getAttribute("manualVisitTypeMappings");

    if (manualVisitTypeMappings == null) {
      manualVisitTypeMappings = new HashMap<>();
    }

    Map<VisitTypeDTO, Integer> removedMappableVisitTypes =
        (Map) session.getAttribute("removedMappableVisitTypes");
    if (removedMappableVisitTypes == null) {
      removedMappableVisitTypes = new HashMap<>();
    }

    VisitTypeDTO associatedVisitTypeDTO = new VisitTypeDTO(pdsVisitType);
    removedMappableVisitTypes.put(
        associatedVisitTypeDTO, mappableVisitTypes.remove(associatedVisitTypeDTO));
    manualVisitTypeMappings.put(pdsVisitType, mdsVisitType);

    session.setAttribute("manualVisitTypeMappings", manualVisitTypeMappings);
    session.setAttribute("removedMappableVisitTypes", removedMappableVisitTypes);

    return modelAndView;
  }

  @RequestMapping(value = REMOVE_VISIT_TYPE_MAPPING, method = RequestMethod.POST)
  public ModelAndView removeVisitTypeMapping(
      HttpSession session, @RequestParam("productionServerVisitTypeUuID") String pdsVisitTypeUuid) {
    Map<VisitTypeDTO, Integer> mappableVisitTypes = getMappableVisitTypes(session);
    Map<VisitType, VisitType> manualVisitTypeMappings =
        (Map<VisitType, VisitType>) session.getAttribute("manualVisitTypeMappings");

    Map<VisitTypeDTO, Integer> removedMappableVisitTypes =
        (Map) session.getAttribute("removedMappableVisitTypes");

    VisitTypeDTO visitTypeDTOFromRemoved = null;
    for (VisitTypeDTO visitTypeDTO : removedMappableVisitTypes.keySet()) {
      if (visitTypeDTO.getUuid().equals(pdsVisitTypeUuid)) {
        visitTypeDTOFromRemoved = visitTypeDTO;
        break;
      }
    }

    manualVisitTypeMappings.remove(visitTypeDTOFromRemoved.getVisitType());
    mappableVisitTypes.put(
        visitTypeDTOFromRemoved, removedMappableVisitTypes.get(visitTypeDTOFromRemoved));

    if (manualVisitTypeMappings.isEmpty()) {
      session.removeAttribute("manualVisitTypeMappings");
    }

    if (removedMappableVisitTypes.isEmpty()) {
      session.removeAttribute("removedMappableVisitTypes");
    }

    return getRedirectToMandatoryStep();
  }

  @RequestMapping(value = MANUAL_MAPPING_STEP, method = RequestMethod.POST)
  public ModelAndView processManualMapping(HttpSession session, HttpServletRequest request) {
    Map<VisitTypeDTO, Integer> mappableVisitTypes = getMappableVisitTypes(session);
    VisitTypeHarmonizationCSVLog.Builder logBuilder = getLogBuilder();
    Map<VisitType, VisitType> manualVisitTypeMappings =
        (Map<VisitType, VisitType>) session.getAttribute("manualVisitTypeMappings");

    if (manualVisitTypeMappings != null && !manualVisitTypeMappings.isEmpty()) {
      harmonizationVisitTypeService.saveManualVisitTypeMappings(manualVisitTypeMappings);
      String message =
          messageSourceService.getMessage(
              "eptsharmonization.visittype.harmonize.manualMappingDone");
      HARMONIZED_CACHED_SUMMARY.add(message + " " + manualVisitTypeMappings.size());

      // Remove from mappable
      for (VisitType visitType : manualVisitTypeMappings.keySet()) {
        mappableVisitTypes.remove(new VisitTypeDTO(visitType));
      }

      logBuilder.appendNewMappedVisitTypes(manualVisitTypeMappings);
    }
    session.removeAttribute("manualVisitTypeMappings");
    ModelAndView modelAndView = getRedirectToMandatoryStep();
    modelAndView.addObject("openmrs_msg", "eptsharmonization.encountertype.harmonized");

    // Write to log file.
    logBuilder.build();

    return modelAndView;
  }

  @RequestMapping(
      value = EXPORT_VISIT_TYPES_LOG,
      method = RequestMethod.POST,
      produces = MediaType.TEXT_PLAIN_VALUE)
  public void exportLog(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    File file = new File("harmonizationVisitTypeLog");
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
        "attachment; fileName=visit_type_harmonization_" + defaultLocation + ".log.csv");
    response.setHeader("Cache-Control", "no-cache");
    response.getWriter().write(outputStream.toString(StandardCharsets.ISO_8859_1.name()));
  }

  @RequestMapping(value = EXPORT_VISIT_TYPES_FOR_CRB, method = RequestMethod.POST)
  public void exportVisitTypes(
      HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model)
      throws IOException {

    Map<VisitTypeDTO, Integer> productionItemsToExport =
        (Map) session.getAttribute("mappableVisitTypes");

    List<VisitType> existingVisitTypes = visitService.getAllVisitTypes();
    ByteArrayOutputStream outputStream =
        VisitTypeHarmonizationCSVLog.exportVisitTypeLogs(
            defaultLocation, new ArrayList<>(productionItemsToExport.keySet()), existingVisitTypes);
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName=visit_type_crb_" + defaultLocation + "_export.csv");
    response.setHeader("Cache-Control", "no-cache");
    response.getWriter().write(outputStream.toString(StandardCharsets.ISO_8859_1.name()));
  }

  @ModelAttribute("visitTypeBean")
  public VisitTypeBean formBackingObject() {
    return new VisitTypeBean();
  }

  private Map<String, List<VisitTypeDTO>> filterConfirmedOverwrites(
      Map<String, List<VisitTypeDTO>> bucket, HttpServletRequest request) {
    Map<String, List<VisitTypeDTO>> filtered = new HashMap<>();
    for (String uuid : bucket.keySet()) {
      if (Boolean.parseBoolean(request.getParameter(uuid))) {
        filtered.put(uuid, bucket.get(uuid));
      }
    }
    return filtered;
  }

  private Map<VisitTypeDTO, Integer> getMappableVisitTypes(HttpSession session) {
    Map<VisitTypeDTO, Integer> mappableVisitTypes = null;
    if (session != null) {
      mappableVisitTypes = (Map) session.getAttribute("mappableVisitTypes");
    }

    if (mappableVisitTypes != null) {
      return mappableVisitTypes;
    }

    return harmonizationVisitTypeService
        .findAllUsedProductionVisitTypesNotSharingUuidWithAnyFromMetadata();
  }

  private ModelAndView getRedirectToMandatoryStep() {
    return new ModelAndView("redirect:" + MANDATORY_STEP + ".form");
  }

  private VisitTypeHarmonizationCSVLog.Builder getLogBuilder() {
    if (logBuilder == null) {
      logBuilder = new VisitTypeHarmonizationCSVLog.Builder(defaultLocation);
    }
    return logBuilder;
  }
}
