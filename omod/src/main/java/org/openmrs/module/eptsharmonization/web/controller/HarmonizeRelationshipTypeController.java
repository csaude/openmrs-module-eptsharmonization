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
import org.openmrs.RelationshipType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PersonService;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationRelationshipTypeService;
import org.openmrs.module.eptsharmonization.api.model.RelationshipTypeDTO;
import org.openmrs.module.eptsharmonization.web.bean.RelationshipTypeBean;
import org.openmrs.module.eptsharmonization.web.bean.RelationshipTypeHarmonizationCSVLog;
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
@Controller(HarmonizeRelationshipTypeController.CONTROLLER_NAME)
@SessionAttributes({"removedMappableRelationshipTypes"})
public class HarmonizeRelationshipTypeController {
  public static final String CONTROLLER_NAME =
      "eptsharmonization.harmonizeRelationshipTypeController";
  public static final String ANALYSIS_STEP = "/module/eptsharmonization/harmonizeRelationshipType";
  public static final String MANDATORY_STEP =
      "/module/eptsharmonization/mandatoryRelationshipTypeHarmonization";
  public static final String MANUAL_MAPPING_STEP =
      "/module/eptsharmonization/manualMappingRelationshipTypeHarmonization";
  public static final String ADD_VISIT_TYPE_MAPPING =
      "/module/eptsharmonization/addRelationshipTypeMapping";
  private static final String REMOVE_VISIT_TYPE_MAPPING =
      "/module/eptsharmonization/removeRelationshipTypeMapping";
  private static final String EXPORT_VISIT_TYPES_LOG =
      "/module/eptsharmonization/exportRelationshipTypesHarmonizationLog";
  private static final String EXPORT_VISIT_TYPES_FOR_CRB =
      "/module/eptsharmonization/exportRelationshipTypes";

  public static List<String> HARMONIZED_CACHED_SUMMARY = new ArrayList<>();
  private static final Logger LOGGER =
      LoggerFactory.getLogger(HarmonizeRelationshipTypeController.class);
  private static String defaultLocation;

  private HarmonizationRelationshipTypeService harmonizationRelationshipTypeService;
  private MessageSourceService messageSourceService;
  private PersonService personService;
  private AdministrationService adminService;
  private static RelationshipTypeHarmonizationCSVLog.Builder logBuilder;

  @Autowired
  public void setHarmonizationRelationshipTypeService(
      HarmonizationRelationshipTypeService harmonizationRelationshipTypeService) {
    this.harmonizationRelationshipTypeService = harmonizationRelationshipTypeService;
  }

  @Autowired
  public void setMessageSourceService(MessageSourceService messageSourceService) {
    this.messageSourceService = messageSourceService;
  }

  @Autowired
  public void setPersonService(PersonService personService) {
    this.personService = personService;
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
  public ModelAndView relationshipTypesHarmonyAnalysis(
      HttpSession session,
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg) {
    ModelAndView modelAndView = new ModelAndView();

    // Get missing relationship types.
    List<RelationshipTypeDTO> missingInPDS =
        harmonizationRelationshipTypeService
            .findAllMetadataRelationshipTypesNotSharingUuidWithAnyFromProduction();
    LOGGER.debug(
        "Number of relationship types missing in production found is %s", missingInPDS.size());

    List<RelationshipTypeDTO> notInMDSNotInUse =
        harmonizationRelationshipTypeService
            .findAllUselessProductionRelationshipTypesNotSharingUuidWithAnyFromMetadata();
    LOGGER.debug(
        "Number of useless relationship types in production and not in metadata server is %s",
        notInMDSNotInUse.size());

    Map<String, List<RelationshipTypeDTO>> sameIdAndUuidDifferentNames =
        harmonizationRelationshipTypeService
            .findAllRelationshipTypeWithDifferentTypesAndSameUUIDAndID();
    LOGGER.debug(
        "Number of relationship types with same id and uuid but different names is %s",
        sameIdAndUuidDifferentNames.size());

    Map<List<RelationshipTypeDTO>, Integer> sameIdAndUuidDifferentNamesWithCountAffected =
        new HashMap<>();
    // First entry is from Production server
    for (List<RelationshipTypeDTO> matchList : sameIdAndUuidDifferentNames.values()) {
      Integer affectedRows =
          harmonizationRelationshipTypeService.getNumberOfAffectedRelationships(matchList.get(0));
      sameIdAndUuidDifferentNamesWithCountAffected.put(matchList, affectedRows);
    }

    Map<String, List<RelationshipTypeDTO>> sameUuidDifferentIds =
        harmonizationRelationshipTypeService.findAllRelationshipTypesWithDifferentIDAndSameUUID();
    LOGGER.debug(
        "Number of relationship types with same uuid but different ids and/or names is %s",
        sameUuidDifferentIds.size());
    Map<List<RelationshipTypeDTO>, Integer> sameUuidDifferentIdsWithAffectedRows = new HashMap<>();
    // First entry is from Production server
    for (List<RelationshipTypeDTO> matchList : sameUuidDifferentIds.values()) {
      Integer affectedRows =
          harmonizationRelationshipTypeService.getNumberOfAffectedRelationships(matchList.get(0));
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
    modelAndView.addObject("sameUuidDifferentIds", sameUuidDifferentIdsWithAffectedRows);
    return modelAndView;
  }

  @RequestMapping(
      value = MANDATORY_STEP,
      method = {RequestMethod.POST, RequestMethod.GET})
  public ModelAndView mandatoryStep(HttpSession session, HttpServletRequest request)
      throws IOException {
    ModelAndView modelAndView = new ModelAndView();

    List<RelationshipTypeDTO> missingInPDS =
        harmonizationRelationshipTypeService
            .findAllMetadataRelationshipTypesNotSharingUuidWithAnyFromProduction();

    if (missingInPDS.size() > 0) {
      LOGGER.debug("Adding new %s relationship types from metadata server", missingInPDS.size());

      harmonizationRelationshipTypeService.saveNewRelationshipTypesFromMetadata(missingInPDS);
      String message =
          messageSourceService.getMessage(
              "eptsharmonization.relationshiptype.harmonize.newRelationshipTypeAdded");
      HARMONIZED_CACHED_SUMMARY.add(message + " " + missingInPDS.size());
    }

    RelationshipTypeHarmonizationCSVLog.Builder logBuilder = getLogBuilder();
    logBuilder.appendLogForNewHarmonizedFromMDSRelationshipTypes(missingInPDS);

    List<RelationshipTypeDTO> notInMDSNotInUse =
        harmonizationRelationshipTypeService
            .findAllUselessProductionRelationshipTypesNotSharingUuidWithAnyFromMetadata();
    if (notInMDSNotInUse.size() > 0) {
      LOGGER.debug(
          "Deleting %s useless relationship types from production", notInMDSNotInUse.size());
      harmonizationRelationshipTypeService.deleteRelationshipTypesFromProduction(notInMDSNotInUse);
      String message =
          messageSourceService.getMessage(
              "eptsharmonization.relationshiptype.harmonize.relationshipTypesDeleted");
      HARMONIZED_CACHED_SUMMARY.add(message + " " + notInMDSNotInUse.size());
      logBuilder.appendLogForDeleteFromProductionServer(notInMDSNotInUse);
    }

    Map<String, List<RelationshipTypeDTO>> sameIdAndUuidDifferentNames =
        harmonizationRelationshipTypeService
            .findAllRelationshipTypeWithDifferentTypesAndSameUUIDAndID();
    if (sameIdAndUuidDifferentNames.size() > 0) {
      LOGGER.debug(
          "Number of relationship types with same id and uuid but different names is %s",
          sameIdAndUuidDifferentNames.size());
      Map<String, List<RelationshipTypeDTO>> toOverwrite =
          filterConfirmedOverwrites(sameIdAndUuidDifferentNames, request);
      if (toOverwrite.size() > 0) {
        LOGGER.debug(
            "Relationship types with same IDs and UUIDs to be overwritten are %s",
            toOverwrite.size());
        harmonizationRelationshipTypeService.saveRelationshipTypesWithDifferentNames(toOverwrite);

        // Remove all overwritten ones.
        for (String uuid : toOverwrite.keySet()) {
          sameIdAndUuidDifferentNames.remove(uuid);
        }

        String message =
            messageSourceService.getMessage(
                "eptsharmonization.relationshiptype.harmonize.relationshipTypesUpdated");
        HARMONIZED_CACHED_SUMMARY.add(message + " " + toOverwrite.size());
        logBuilder.appendLogForUpdatedRelationshipNames(toOverwrite);
      }
    }

    Map<String, List<RelationshipTypeDTO>> sameUuidDifferentIds =
        harmonizationRelationshipTypeService.findAllRelationshipTypesWithDifferentIDAndSameUUID();
    if (sameUuidDifferentIds.size() > 0) {
      Map<String, List<RelationshipTypeDTO>> toOverwrite =
          filterConfirmedOverwrites(sameUuidDifferentIds, request);
      if (toOverwrite.size() > 0) {
        LOGGER.debug(
            "Relationship types with same uuid but different ids and/or names being overwritten with metadata %s",
            toOverwrite.size());
        harmonizationRelationshipTypeService
            .updateRelationshipTypesFromProductionWithSameUuidWithInformationFromMetadata(
                toOverwrite);
        String message =
            messageSourceService.getMessage(
                "eptsharmonization.relationshiptype.harmonize.relationshipTypeSameUuidUpdated");
        HARMONIZED_CACHED_SUMMARY.add(message + " " + toOverwrite.size());
        logBuilder.appendLogForRelationshipTypesWithDiferrentIdsAndEqualUUID(toOverwrite);
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
    harmonizationRelationshipTypeService.replacePDSRelationshipTypesWithSameUuidWithThoseFromMDS(
        sameIdAndUuidDifferentNames);
    harmonizationRelationshipTypeService.replacePDSRelationshipTypesWithSameUuidWithThoseFromMDS(
        sameUuidDifferentIds);

    Map<RelationshipTypeDTO, Integer> mappableRelationshipTypes =
        getMappableRelationshipTypes(session);

    if (mappableRelationshipTypes != null && mappableRelationshipTypes.size() > 0) {
      List<RelationshipType> allRelationshipTypes = personService.getAllRelationshipTypes(true);
      List<RelationshipTypeDTO> toRemoveFromAll =
          new ArrayList<>(mappableRelationshipTypes.keySet());
      allRelationshipTypes.removeAll(DTOUtils.fromRelationshipTypeDTOs(toRemoveFromAll));
      modelAndView.addObject(
          "availableMappingTypes", DTOUtils.fromRelationshipTypes(allRelationshipTypes));
      session.setAttribute("mappableRelationshipTypes", mappableRelationshipTypes);

      // Copy mappables under a different name.
      session.setAttribute("productionRelationshipTypesToExport", mappableRelationshipTypes);
      modelAndView.addObject(
          "mappableRelationshipTypesList", new ArrayList<>(mappableRelationshipTypes.keySet()));
    } else {
      session.removeAttribute("mappableRelationshipTypes");
      session.removeAttribute("productionRelationshipTypesToExport");
    }

    modelAndView.addObject("harmonizedVTSummary", HARMONIZED_CACHED_SUMMARY);
    // Write log to file.
    logBuilder.build();

    if (mappableRelationshipTypes.size() == 0) {
      modelAndView.addObject("harmonizationCompleted", true);
    }

    return modelAndView;
  }

  @RequestMapping(value = ADD_VISIT_TYPE_MAPPING, method = RequestMethod.POST)
  public ModelAndView addRelationshipTypeMapping(
      HttpSession session,
      @ModelAttribute("relationshipTypeBean") RelationshipTypeBean relationshipTypeBean) {

    Map<RelationshipTypeDTO, Integer> mappableRelationshipTypes =
        getMappableRelationshipTypes(session);
    ModelAndView modelAndView = getRedirectToMandatoryStep();

    if (relationshipTypeBean.getKey() == null
        || StringUtils.isEmpty(((String) relationshipTypeBean.getKey()))) {
      modelAndView.addObject(
          "errorRequiredPDSValue", "eptsharmonization.error.relationshipTypeForMapping.required");
      return modelAndView;
    }
    if (relationshipTypeBean.getValue() == null
        || StringUtils.isEmpty(((String) relationshipTypeBean.getValue()))) {
      modelAndView.addObject(
          "errorRequiredMdsValue", "eptsharmonization.error.relationshipTypeForMapping.required");
      return modelAndView;
    }

    RelationshipType pdsRelationshipType =
        personService.getRelationshipTypeByUuid((String) relationshipTypeBean.getKey());
    RelationshipType mdsRelationshipType =
        personService.getRelationshipTypeByUuid((String) relationshipTypeBean.getValue());

    Map<RelationshipType, RelationshipType> manualRelationshipTypeMappings =
        (Map<RelationshipType, RelationshipType>)
            session.getAttribute("manualRelationshipTypeMappings");

    if (manualRelationshipTypeMappings == null) {
      manualRelationshipTypeMappings = new HashMap<>();
    }

    Map<RelationshipTypeDTO, Integer> removedMappableRelationshipTypes =
        (Map) session.getAttribute("removedMappableRelationshipTypes");
    if (removedMappableRelationshipTypes == null) {
      removedMappableRelationshipTypes = new HashMap<>();
    }

    RelationshipTypeDTO associatedRelationshipTypeDTO =
        new RelationshipTypeDTO(pdsRelationshipType);
    removedMappableRelationshipTypes.put(
        associatedRelationshipTypeDTO,
        mappableRelationshipTypes.remove(associatedRelationshipTypeDTO));
    manualRelationshipTypeMappings.put(pdsRelationshipType, mdsRelationshipType);

    session.setAttribute("manualRelationshipTypeMappings", manualRelationshipTypeMappings);
    session.setAttribute("removedMappableRelationshipTypes", removedMappableRelationshipTypes);

    return modelAndView;
  }

  @RequestMapping(value = REMOVE_VISIT_TYPE_MAPPING, method = RequestMethod.POST)
  public ModelAndView removeRelationshipTypeMapping(
      HttpSession session,
      @RequestParam("productionServerRelationshipTypeUuID") String pdsRelationshipTypeUuid) {
    Map<RelationshipTypeDTO, Integer> mappableRelationshipTypes =
        getMappableRelationshipTypes(session);
    Map<RelationshipType, RelationshipType> manualRelationshipTypeMappings =
        (Map<RelationshipType, RelationshipType>)
            session.getAttribute("manualRelationshipTypeMappings");

    Map<RelationshipTypeDTO, Integer> removedMappableRelationshipTypes =
        (Map) session.getAttribute("removedMappableRelationshipTypes");

    RelationshipTypeDTO relationshipTypeDTOFromRemoved = null;
    for (RelationshipTypeDTO relationshipTypeDTO : removedMappableRelationshipTypes.keySet()) {
      if (relationshipTypeDTO.getUuid().equals(pdsRelationshipTypeUuid)) {
        relationshipTypeDTOFromRemoved = relationshipTypeDTO;
        break;
      }
    }

    manualRelationshipTypeMappings.remove(relationshipTypeDTOFromRemoved.getRelationshipType());
    mappableRelationshipTypes.put(
        relationshipTypeDTOFromRemoved,
        removedMappableRelationshipTypes.get(relationshipTypeDTOFromRemoved));

    if (manualRelationshipTypeMappings.isEmpty()) {
      session.removeAttribute("manualRelationshipTypeMappings");
    }

    if (removedMappableRelationshipTypes.isEmpty()) {
      session.removeAttribute("removedMappableRelationshipTypes");
    }

    return getRedirectToMandatoryStep();
  }

  @RequestMapping(value = MANUAL_MAPPING_STEP, method = RequestMethod.POST)
  public ModelAndView processManualMapping(HttpSession session, HttpServletRequest request) {
    Map<RelationshipTypeDTO, Integer> mappableRelationshipTypes =
        getMappableRelationshipTypes(session);
    RelationshipTypeHarmonizationCSVLog.Builder logBuilder = getLogBuilder();
    Map<RelationshipType, RelationshipType> manualRelationshipTypeMappings =
        (Map<RelationshipType, RelationshipType>)
            session.getAttribute("manualRelationshipTypeMappings");

    if (manualRelationshipTypeMappings != null && !manualRelationshipTypeMappings.isEmpty()) {
      harmonizationRelationshipTypeService.saveManualRelationshipTypeMappings(
          manualRelationshipTypeMappings);
      String message =
          messageSourceService.getMessage(
              "eptsharmonization.relationshiptype.harmonize.manualMappingDone");
      HARMONIZED_CACHED_SUMMARY.add(message + " " + manualRelationshipTypeMappings.size());

      // Remove from mappable
      for (RelationshipType relationshipType : manualRelationshipTypeMappings.keySet()) {
        mappableRelationshipTypes.remove(new RelationshipTypeDTO(relationshipType));
      }

      logBuilder.appendNewMappedRelationshipTypes(manualRelationshipTypeMappings);
    }
    session.removeAttribute("manualRelationshipTypeMappings");
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

    File file = new File("harmonizationRelationshipTypeLog");
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
        "attachment; fileName=relationship_type_harmonization_" + defaultLocation + ".log.csv");
    response.setHeader("Cache-Control", "no-cache");
    response.getWriter().write(outputStream.toString(StandardCharsets.ISO_8859_1.name()));
  }

  @RequestMapping(value = EXPORT_VISIT_TYPES_FOR_CRB, method = RequestMethod.POST)
  public void exportRelationshipTypes(
      HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model)
      throws IOException {

    Map<RelationshipTypeDTO, Integer> productionItemsToExport =
        (Map) session.getAttribute("mappableRelationshipTypes");

    ByteArrayOutputStream outputStream =
        RelationshipTypeHarmonizationCSVLog.exportRelationshipTypeLogs(
            defaultLocation, new ArrayList<>(productionItemsToExport.keySet()));
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName=relationship_type_crb_" + defaultLocation + "_export.csv");
    response.setHeader("Cache-Control", "no-cache");
    response.getWriter().write(outputStream.toString(StandardCharsets.ISO_8859_1.name()));
  }

  @ModelAttribute("relationshipTypeBean")
  public RelationshipTypeBean formBackingObject() {
    return new RelationshipTypeBean();
  }

  private Map<String, List<RelationshipTypeDTO>> filterConfirmedOverwrites(
      Map<String, List<RelationshipTypeDTO>> bucket, HttpServletRequest request) {
    Map<String, List<RelationshipTypeDTO>> filtered = new HashMap<>();
    for (String uuid : bucket.keySet()) {
      if (Boolean.parseBoolean(request.getParameter(uuid))) {
        filtered.put(uuid, bucket.get(uuid));
      }
    }
    return filtered;
  }

  private Map<RelationshipTypeDTO, Integer> getMappableRelationshipTypes(HttpSession session) {
    Map<RelationshipTypeDTO, Integer> mappableRelationshipTypes = null;
    if (session != null) {
      mappableRelationshipTypes = (Map) session.getAttribute("mappableRelationshipTypes");
    }

    if (mappableRelationshipTypes != null) {
      return mappableRelationshipTypes;
    }

    return harmonizationRelationshipTypeService
        .findAllUsedProductionRelationshipTypesNotSharingUuidWithAnyFromMetadata();
  }

  private ModelAndView getRedirectToMandatoryStep() {
    return new ModelAndView("redirect:" + MANDATORY_STEP + ".form");
  }

  private RelationshipTypeHarmonizationCSVLog.Builder getLogBuilder() {
    if (logBuilder == null) {
      logBuilder = new RelationshipTypeHarmonizationCSVLog.Builder(defaultLocation);
    }
    return logBuilder;
  }
}
