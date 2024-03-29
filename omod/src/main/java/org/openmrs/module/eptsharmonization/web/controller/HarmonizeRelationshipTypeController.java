package org.openmrs.module.eptsharmonization.web.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparatorChain;
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
import org.springframework.web.servlet.ModelAndView;

/** @uthor Willa Mhawila<a.mhawila@gmail.com> on 6/2/20. */
@Controller(HarmonizeRelationshipTypeController.CONTROLLER_NAME)
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
  public static final String ADD_RELATION_TYPE_FROM_MDS_MAPPING =
      "/module/eptsharmonization/addRelationshipTypeFromMDSMapping";
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

  private static List<RelationshipTypeDTO> RELATIONSHIPTYPE_MDS_MAPPING_CACHE_CONTROL =
      new ArrayList<>();

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
          "Number of relationship types with same id and uuid but different names or status is %s",
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

    Map<RelationshipTypeDTO, Integer> mappableRelationshipTypes =
        getMappableRelationshipTypes(session);

    if (RELATIONSHIPTYPE_MDS_MAPPING_CACHE_CONTROL.isEmpty()) {

      for (Entry<String, List<RelationshipTypeDTO>> entry :
          sameIdAndUuidDifferentNames.entrySet()) {
        RELATIONSHIPTYPE_MDS_MAPPING_CACHE_CONTROL.add(entry.getValue().get(0));
      }
      for (Entry<String, List<RelationshipTypeDTO>> entry : sameUuidDifferentIds.entrySet()) {
        RELATIONSHIPTYPE_MDS_MAPPING_CACHE_CONTROL.add(entry.getValue().get(0));
      }

      if (!RELATIONSHIPTYPE_MDS_MAPPING_CACHE_CONTROL.isEmpty()) {
        for (RelationshipTypeDTO dto : RELATIONSHIPTYPE_MDS_MAPPING_CACHE_CONTROL) {
          mappableRelationshipTypes.put(
              dto, this.harmonizationRelationshipTypeService.getNumberOfAffectedRelationships(dto));
        }
      }
    }

    if (!RELATIONSHIPTYPE_MDS_MAPPING_CACHE_CONTROL.isEmpty()) {
      List<RelationshipTypeDTO> availableMDSMappingRelationshipTypes =
          (List<RelationshipTypeDTO>) session.getAttribute("availableMDSMappingRelationshipTypes");
      if (availableMDSMappingRelationshipTypes == null) {
        availableMDSMappingRelationshipTypes =
            this.harmonizationRelationshipTypeService
                .findAllMetadataRelationshipTypesNotInHarmonyWithProduction();
        this.sortByDTOName(availableMDSMappingRelationshipTypes);
        session.setAttribute(
            "availableMDSMappingRelationshipTypes", availableMDSMappingRelationshipTypes);
      }
    }

    if (mappableRelationshipTypes != null && mappableRelationshipTypes.size() > 0) {
      List<RelationshipType> allRelationshipTypes = personService.getAllRelationshipTypes(true);
      List<RelationshipTypeDTO> toRemoveFromAll =
          new ArrayList<>(mappableRelationshipTypes.keySet());
      allRelationshipTypes.removeAll(DTOUtils.fromRelationshipTypeDTOs(toRemoveFromAll));
      this.sortByName(allRelationshipTypes);
      session.setAttribute(
          "availableMappingTypes", DTOUtils.fromRelationshipTypes(allRelationshipTypes));
      session.setAttribute("mappableRelationshipTypes", mappableRelationshipTypes);
      session.setAttribute("productionRelationshipTypesToExport", mappableRelationshipTypes);

      List<RelationshipTypeDTO> keySet = new ArrayList<>(mappableRelationshipTypes.keySet());
      this.sortByDTOName(keySet);
      session.setAttribute("mappablePDSRelationshipTypes", keySet);

    } else {
      session.removeAttribute("mappableRelationshipTypes");
      session.removeAttribute("productionRelationshipTypesToExport");
      session.removeAttribute("mappablePDSRelationshipTypes");
    }

    modelAndView.addObject("harmonizedVTSummary", HARMONIZED_CACHED_SUMMARY);
    // Write log to file.
    logBuilder.build();

    Map<RelationshipType, RelationshipType> manualRelationshipTypeMappings =
        (Map<RelationshipType, RelationshipType>)
            session.getAttribute("manualRelationshipTypeMappings");

    if (mappableRelationshipTypes.size() == 0
        && sameUuidDifferentIds.isEmpty()
        && sameIdAndUuidDifferentNames.isEmpty()
        && (manualRelationshipTypeMappings == null || manualRelationshipTypeMappings.isEmpty())) {
      modelAndView.addObject("harmonizationCompleted", true);
    }

    return modelAndView;
  }

  @SuppressWarnings("unchecked")
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
        this.harmonizationRelationshipTypeService.findPDSRelationshipTypeByUuid(
            (String) relationshipTypeBean.getKey());
    RelationshipType mdsRelationshipType =
        this.harmonizationRelationshipTypeService.findPDSRelationshipTypeByUuid(
            (String) relationshipTypeBean.getValue());

    Map<RelationshipType, RelationshipType> manualRelationshipTypeMappings =
        (Map<RelationshipType, RelationshipType>)
            session.getAttribute("manualRelationshipTypeMappings");

    if (manualRelationshipTypeMappings == null) {
      manualRelationshipTypeMappings = new HashMap<>();
    }

    mappableRelationshipTypes.remove(new RelationshipTypeDTO(pdsRelationshipType));
    manualRelationshipTypeMappings.put(pdsRelationshipType, mdsRelationshipType);
    session.setAttribute("manualRelationshipTypeMappings", manualRelationshipTypeMappings);

    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = ADD_RELATION_TYPE_FROM_MDS_MAPPING, method = RequestMethod.POST)
  public ModelAndView addRelationTypeFromMDSMapping(
      HttpSession session,
      @ModelAttribute("relationshipTypeBean") RelationshipTypeBean relationShipTypeBean) {

    Map<RelationshipTypeDTO, Integer> mappableRelationshipTypes =
        getMappableRelationshipTypes(session);
    ModelAndView modelAndView = getRedirectToMandatoryStep();

    if (relationShipTypeBean.getKey() == null
        || StringUtils.isEmpty(((String) relationShipTypeBean.getKey()))) {
      modelAndView.addObject(
          "errorRequiredPDSValue", "eptsharmonization.error.relationshipTypeForMapping.required");
      return modelAndView;
    }
    if (relationShipTypeBean.getValue() == null
        || StringUtils.isEmpty(((String) relationShipTypeBean.getValue()))) {
      modelAndView.addObject(
          "errorRequiredMdsValue", "eptsharmonization.error.relationshipTypeForMapping.required");
      return modelAndView;
    }

    RelationshipType pdsRelationType =
        this.harmonizationRelationshipTypeService.findPDSRelationshipTypeByUuid(
            (String) relationShipTypeBean.getKey());
    RelationshipType mdsRelationType =
        this.harmonizationRelationshipTypeService.findMDSRelationshipTypeByUuid(
            (String) relationShipTypeBean.getValue());

    Map<RelationshipType, RelationshipType> manualRelationshipTypeMappings =
        (Map<RelationshipType, RelationshipType>)
            session.getAttribute("manualRelationshipTypeMappings");

    if (manualRelationshipTypeMappings == null) {
      manualRelationshipTypeMappings = new HashMap<>();
    }

    List<RelationshipTypeDTO> availableMDSMappingRelationshipTypes =
        (List<RelationshipTypeDTO>) session.getAttribute("availableMDSMappingRelationshipTypes");
    availableMDSMappingRelationshipTypes.remove(new RelationshipTypeDTO(mdsRelationType));

    mappableRelationshipTypes.remove(new RelationshipTypeDTO(pdsRelationType));
    manualRelationshipTypeMappings.put(pdsRelationType, mdsRelationType);
    session.setAttribute("manualRelationshipTypeMappings", manualRelationshipTypeMappings);

    return modelAndView;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = REMOVE_VISIT_TYPE_MAPPING, method = RequestMethod.POST)
  public ModelAndView removeRelationshipTypeMapping(
      HttpSession session,
      @RequestParam("productionServerRelationshipTypeUuID") String pdsRelationshipTypeUuid) {

    List<RelationshipTypeDTO> availableMappingTypes =
        (List<RelationshipTypeDTO>) session.getAttribute("availableMappingTypes");

    Map<RelationshipTypeDTO, Integer> mappableRelationshipTypes =
        getMappableRelationshipTypes(session);

    RelationshipType pdsRelationShipType =
        this.harmonizationRelationshipTypeService.findPDSRelationshipTypeByUuid(
            pdsRelationshipTypeUuid);

    Map<RelationshipType, RelationshipType> manualRelationshipTypeMappings =
        (Map<RelationshipType, RelationshipType>)
            session.getAttribute("manualRelationshipTypeMappings");

    RelationshipType mdsRelationshipType =
        manualRelationshipTypeMappings.remove(pdsRelationShipType);

    boolean isMDSAlreadyInPDS = false;
    for (RelationshipTypeDTO dto : availableMappingTypes) {
      RelationshipType type = dto.getRelationshipType();
      if (mdsRelationshipType.getUuid().equals(type.getUuid())
          && mdsRelationshipType.getId().equals(type.getId())
          && mdsRelationshipType.getaIsToB().contentEquals(type.getaIsToB())
          && mdsRelationshipType.getbIsToA().contentEquals(type.getbIsToA())) {
        isMDSAlreadyInPDS = true;
        break;
      }
    }

    if (!isMDSAlreadyInPDS) {
      List<RelationshipTypeDTO> availableMDSMappingRelationshipTypes =
          (List<RelationshipTypeDTO>) session.getAttribute("availableMDSMappingRelationshipTypes");
      if (availableMDSMappingRelationshipTypes != null) {
        RelationshipTypeDTO dto = new RelationshipTypeDTO(mdsRelationshipType);
        if (!availableMDSMappingRelationshipTypes.contains(dto)) {
          availableMDSMappingRelationshipTypes.add(dto);
          this.sortByDTOName(availableMDSMappingRelationshipTypes);
        }
      }
    }
    RelationshipTypeDTO pdsDTO = new RelationshipTypeDTO(pdsRelationShipType);
    mappableRelationshipTypes.put(
        pdsDTO, this.harmonizationRelationshipTypeService.getNumberOfAffectedRelationships(pdsDTO));

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

    RELATIONSHIPTYPE_MDS_MAPPING_CACHE_CONTROL = new ArrayList<>();

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
        "attachment; fileName="
            + this.getFormattedLocationName(defaultLocation)
            + "-relationship_type_harmonization-log.csv");
    response.setHeader("Cache-Control", "no-cache");
    response.getWriter().write(outputStream.toString(StandardCharsets.ISO_8859_1.name()));
  }

  @RequestMapping(value = EXPORT_VISIT_TYPES_FOR_CRB, method = RequestMethod.POST)
  public void exportRelationshipTypes(
      HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model)
      throws IOException {

    Map<RelationshipTypeDTO, Integer> productionItemsToExport =
        (Map) session.getAttribute("productionRelationshipTypesToExport");

    ByteArrayOutputStream outputStream =
        RelationshipTypeHarmonizationCSVLog.exportRelationshipTypeLogs(
            defaultLocation, new ArrayList<>(productionItemsToExport.keySet()));
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName="
            + this.getFormattedLocationName(defaultLocation)
            + "-relationship_type_harmonization-export.csv");
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

    Map<RelationshipTypeDTO, Integer> mappableRelationshipTypes =
        (Map) session.getAttribute("mappableRelationshipTypes");
    if (mappableRelationshipTypes != null) {
      return mappableRelationshipTypes;
    }
    mappableRelationshipTypes =
        harmonizationRelationshipTypeService
            .findAllUsedProductionRelationshipTypesNotSharingUuidWithAnyFromMetadata();
    session.setAttribute("mappableRelationshipTypes", mappableRelationshipTypes);

    return mappableRelationshipTypes;
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

  @SuppressWarnings("unchecked")
  private List<RelationshipType> sortByName(List<RelationshipType> list) {
    ComparatorChain chain =
        new ComparatorChain(
            Arrays.asList(new BeanComparator("aIsToB"), new BeanComparator("bIsToA")));
    Collections.sort(list, chain);
    return list;
  }

  @SuppressWarnings("unchecked")
  private List<RelationshipTypeDTO> sortByDTOName(List<RelationshipTypeDTO> list) {
    ComparatorChain chain =
        new ComparatorChain(
            Arrays.asList(
                new BeanComparator("relationshipType.aIsToB"),
                new BeanComparator("relationshipType.bIsToA")));
    Collections.sort(list, chain);
    return list;
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
