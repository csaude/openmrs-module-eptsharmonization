package org.openmrs.module.eptsharmonization.web.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
import org.apache.commons.lang3.StringUtils;
import org.openmrs.LocationTag;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationLocationTagService;
import org.openmrs.module.eptsharmonization.api.model.LocationTagDTO;
import org.openmrs.module.eptsharmonization.web.bean.LocationTagBean;
import org.openmrs.module.eptsharmonization.web.bean.LocationTagHarmonizationCSVLog;
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
@Controller(HarmonizeLocationTagController.CONTROLLER_NAME)
public class HarmonizeLocationTagController {
  public static final String CONTROLLER_NAME = "eptsharmonization.harmonizeLocationTagController";
  public static final String ANALYSIS_STEP = "/module/eptsharmonization/harmonizeLocationTag";
  public static final String MANDATORY_STEP =
      "/module/eptsharmonization/mandatoryLocationTagHarmonization";
  public static final String MANUAL_MAPPING_STEP =
      "/module/eptsharmonization/manualMappingLocationTagHarmonization";
  public static final String ADD_VISIT_TYPE_MAPPING =
      "/module/eptsharmonization/addLocationTagMapping";
  public static final String ADD_LOCATION_TAG_FROM_MDS_MAPPING =
      "/module/eptsharmonization/addLocationTagFromMDSMapping";
  private static final String REMOVE_VISIT_TYPE_MAPPING =
      "/module/eptsharmonization/removeLocationTagMapping";
  private static final String EXPORT_VISIT_TYPES_LOG =
      "/module/eptsharmonization/exportLocationTagsHarmonizationLog";
  private static final String EXPORT_VISIT_TYPES_FOR_CRB =
      "/module/eptsharmonization/exportLocationTags";

  public static List<String> HARMONIZED_CACHED_SUMMARY = new ArrayList<>();
  private static final Logger LOGGER =
      LoggerFactory.getLogger(HarmonizeLocationTagController.class);
  private static String defaultLocation;

  private HarmonizationLocationTagService harmonizationLocationTagService;
  private MessageSourceService messageSourceService;
  private LocationService locationService;
  private AdministrationService adminService;
  private static LocationTagHarmonizationCSVLog.Builder logBuilder;

  private static List<LocationTagDTO> LOCATION_TAG_MDS_MAPPING_CACHE_CONTROL = new ArrayList<>();

  @Autowired
  public void setHarmonizationLocationTagService(
      HarmonizationLocationTagService harmonizationLocationTagService) {
    this.harmonizationLocationTagService = harmonizationLocationTagService;
  }

  @Autowired
  public void setMessageSourceService(MessageSourceService messageSourceService) {
    this.messageSourceService = messageSourceService;
  }

  @Autowired
  public void setLocationService(LocationService locationService) {
    this.locationService = locationService;
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
  public ModelAndView locationTagsHarmonyAnalysis(
      HttpSession session,
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg) {
    ModelAndView modelAndView = new ModelAndView();

    // Get missing location tags.
    List<LocationTagDTO> missingInPDS =
        harmonizationLocationTagService
            .findAllMetadataLocationTagsNotSharingUuidWithAnyFromProduction();
    LOGGER.debug("Number of location tags missing in production found is %s", missingInPDS.size());

    List<LocationTagDTO> notInMDSNotInUse =
        harmonizationLocationTagService
            .findAllUselessProductionLocationTagsNotSharingUuidWithAnyFromMetadata();
    LOGGER.debug(
        "Number of useless location tags in production and not in metadata server is %s",
        notInMDSNotInUse.size());

    Map<String, List<LocationTagDTO>> sameIdAndUuidDifferentNames =
        harmonizationLocationTagService.findAllLocationTagsWithDifferentNameAndSameUUIDAndID();
    LOGGER.debug(
        "Number of location tags with same id and uuid but different names is %s",
        sameIdAndUuidDifferentNames.size());

    Map<List<LocationTagDTO>, Integer> sameIdAndUuidDifferentNamesWithCountAffected =
        new HashMap<>();
    // First entry is from Production server
    for (List<LocationTagDTO> matchList : sameIdAndUuidDifferentNames.values()) {
      Integer affectedRows =
          harmonizationLocationTagService.getNumberOfAffectedLocations(matchList.get(0));
      sameIdAndUuidDifferentNamesWithCountAffected.put(matchList, affectedRows);
    }

    Map<String, List<LocationTagDTO>> sameUuidDifferentIds =
        harmonizationLocationTagService.findAllLocationTagsWithDifferentIDAndSameUUID();
    LOGGER.debug(
        "Number of location tags with same uuid but different ids and/or names is %s",
        sameUuidDifferentIds.size());
    Map<List<LocationTagDTO>, Integer> sameUuidDifferentIdsWithAffectedRows = new HashMap<>();
    // First entry is from Production server
    for (List<LocationTagDTO> matchList : sameUuidDifferentIds.values()) {
      Integer affectedRows =
          harmonizationLocationTagService.getNumberOfAffectedLocations(matchList.get(0));
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

    List<LocationTagDTO> missingInPDS =
        harmonizationLocationTagService
            .findAllMetadataLocationTagsNotSharingUuidWithAnyFromProduction();

    if (missingInPDS.size() > 0) {
      LOGGER.debug("Adding new %s location tags from metadata server", missingInPDS.size());

      harmonizationLocationTagService.saveNewLocationTagsFromMetadata(missingInPDS);
      String message =
          messageSourceService.getMessage(
              "eptsharmonization.locationtag.harmonize.newLocationTagAdded");
      HARMONIZED_CACHED_SUMMARY.add(message + " " + missingInPDS.size());
    }

    LocationTagHarmonizationCSVLog.Builder logBuilder = getLogBuilder();
    logBuilder.appendLogForNewHarmonizedFromMDSLocationTags(missingInPDS);

    List<LocationTagDTO> notInMDSNotInUse =
        harmonizationLocationTagService
            .findAllUselessProductionLocationTagsNotSharingUuidWithAnyFromMetadata();
    if (notInMDSNotInUse.size() > 0) {
      LOGGER.debug("Deleting %s useless location tags from production", notInMDSNotInUse.size());
      harmonizationLocationTagService.deleteLocationTagsFromProduction(notInMDSNotInUse);
      String message =
          messageSourceService.getMessage(
              "eptsharmonization.locationtag.harmonize.locationTagsDeleted");
      HARMONIZED_CACHED_SUMMARY.add(message + " " + notInMDSNotInUse.size());
      logBuilder.appendLogForDeleteFromProductionServer(notInMDSNotInUse);
    }

    Map<String, List<LocationTagDTO>> sameIdAndUuidDifferentNames =
        harmonizationLocationTagService.findAllLocationTagsWithDifferentNameAndSameUUIDAndID();
    if (sameIdAndUuidDifferentNames.size() > 0) {
      LOGGER.debug(
          "Number of location tags with same id and uuid but different names is %s",
          sameIdAndUuidDifferentNames.size());
      Map<String, List<LocationTagDTO>> toOverwrite =
          filterConfirmedOverwrites(sameIdAndUuidDifferentNames, request);
      if (toOverwrite.size() > 0) {
        LOGGER.debug(
            "Location tags with same IDs and UUIDs to be overwritten are %s", toOverwrite.size());
        harmonizationLocationTagService.saveLocationTagsWithDifferentNames(toOverwrite);

        // Remove all overwritten ones.
        for (String uuid : toOverwrite.keySet()) {
          sameIdAndUuidDifferentNames.remove(uuid);
        }

        String message =
            messageSourceService.getMessage(
                "eptsharmonization.locationtag.harmonize.locationTagsUpdated");
        HARMONIZED_CACHED_SUMMARY.add(message + " " + toOverwrite.size());
        logBuilder.appendLogForUpdatedLocationTagNames(toOverwrite);
      }
    }

    Map<String, List<LocationTagDTO>> sameUuidDifferentIds =
        harmonizationLocationTagService.findAllLocationTagsWithDifferentIDAndSameUUID();
    if (sameUuidDifferentIds.size() > 0) {
      Map<String, List<LocationTagDTO>> toOverwrite =
          filterConfirmedOverwrites(sameUuidDifferentIds, request);
      if (toOverwrite.size() > 0) {
        LOGGER.debug(
            "Location tags with same uuid but different ids and/or names being overwritten with metadata %s",
            toOverwrite.size());
        harmonizationLocationTagService
            .updateLocationTagsFromProductionWithSameUuidWithInformationFromMetadata(toOverwrite);
        String message =
            messageSourceService.getMessage(
                "eptsharmonization.locationtag.harmonize.locationTagSameUuidUpdated");
        HARMONIZED_CACHED_SUMMARY.add(message + " " + toOverwrite.size());
        logBuilder.appendLogForLocationTagsWithDiferrentIdsAndEqualUUID(toOverwrite);
        // Remove overwritten ones.
        for (String uuid : toOverwrite.keySet()) {
          sameUuidDifferentIds.remove(uuid);
        }
      }
    }

    Map<LocationTagDTO, Integer> mappableLocationTags = getMappableLocationTags(session);

    if (LOCATION_TAG_MDS_MAPPING_CACHE_CONTROL.isEmpty()) {

      for (Entry<String, List<LocationTagDTO>> entry : sameIdAndUuidDifferentNames.entrySet()) {
        LOCATION_TAG_MDS_MAPPING_CACHE_CONTROL.add(entry.getValue().get(0));
      }
      for (Entry<String, List<LocationTagDTO>> entry : sameUuidDifferentIds.entrySet()) {
        LOCATION_TAG_MDS_MAPPING_CACHE_CONTROL.add(entry.getValue().get(0));
      }

      if (!LOCATION_TAG_MDS_MAPPING_CACHE_CONTROL.isEmpty()) {
        for (LocationTagDTO dto : LOCATION_TAG_MDS_MAPPING_CACHE_CONTROL) {
          mappableLocationTags.put(
              dto, this.harmonizationLocationTagService.getNumberOfAffectedLocations(dto));
        }
      }
    }
    if (!LOCATION_TAG_MDS_MAPPING_CACHE_CONTROL.isEmpty()) {
      List<LocationTagDTO> availableMDSMappingTypes =
          (List<LocationTagDTO>) session.getAttribute("availableMDSMappingTypes");
      if (availableMDSMappingTypes == null) {
        availableMDSMappingTypes =
            this.harmonizationLocationTagService
                .findAllMetadataLocationTagsNotInHarmonyWithProduction();
        this.sortByDTOName(availableMDSMappingTypes);
        session.setAttribute("availableMDSMappingTypes", availableMDSMappingTypes);
      }
    }

    if (mappableLocationTags != null && mappableLocationTags.size() > 0) {
      List<LocationTag> allLocationTags = locationService.getAllLocationTags(true);
      List<LocationTagDTO> toRemoveFromAll = new ArrayList<>(mappableLocationTags.keySet());
      allLocationTags.removeAll(DTOUtils.fromLocationTagDTOs(toRemoveFromAll));
      this.sortByName(allLocationTags);
      session.setAttribute("availableMappingTypes", DTOUtils.fromLocationTags(allLocationTags));
      session.setAttribute("mappableLocationTags", mappableLocationTags);
      session.setAttribute("productionLocationTagsToExport", mappableLocationTags);

      List<LocationTagDTO> keySet = new ArrayList<>(mappableLocationTags.keySet());
      this.sortByDTOName(keySet);
      session.setAttribute("mappablePDSLocationTags", keySet);
    } else {
      session.removeAttribute("mappableLocationTags");
      session.removeAttribute("productionLocationTagsToExport");
      session.removeAttribute("mappablePDSLocationTags");
    }

    modelAndView.addObject("harmonizedVTSummary", HARMONIZED_CACHED_SUMMARY);
    // Write log to file.
    logBuilder.build();

    Map<LocationTag, LocationTag> manualLocationTagMappings =
        (Map<LocationTag, LocationTag>) session.getAttribute("manualLocationTagMappings");

    if (mappableLocationTags.size() == 0
        && sameUuidDifferentIds.isEmpty()
        && sameIdAndUuidDifferentNames.isEmpty()
        && (manualLocationTagMappings == null || manualLocationTagMappings.isEmpty())) {
      modelAndView.addObject("harmonizationCompleted", true);
    }

    return modelAndView;
  }

  @RequestMapping(value = ADD_VISIT_TYPE_MAPPING, method = RequestMethod.POST)
  public ModelAndView addLocationTagMapping(
      HttpSession session, @ModelAttribute("locationTagBean") LocationTagBean locationTagBean) {

    Map<LocationTagDTO, Integer> mappableLocationTags = getMappableLocationTags(session);
    ModelAndView modelAndView = getRedirectToMandatoryStep();

    if (locationTagBean.getKey() == null
        || StringUtils.isEmpty(((String) locationTagBean.getKey()))) {
      modelAndView.addObject(
          "errorRequiredPDSValue", "eptsharmonization.error.locationTagForMapping.required");
      return modelAndView;
    }
    if (locationTagBean.getValue() == null
        || StringUtils.isEmpty(((String) locationTagBean.getValue()))) {
      modelAndView.addObject(
          "errorRequiredMdsValue", "eptsharmonization.error.locationTagForMapping.required");
      return modelAndView;
    }

    LocationTag pdsLocationTag =
        this.harmonizationLocationTagService.findPDSLocationTagByUuid(
            (String) locationTagBean.getKey());
    LocationTag mdsLocationTag =
        this.harmonizationLocationTagService.findPDSLocationTagByUuid(
            (String) locationTagBean.getValue());

    Map<LocationTag, LocationTag> manualLocationTagMappings =
        (Map<LocationTag, LocationTag>) session.getAttribute("manualLocationTagMappings");

    if (manualLocationTagMappings == null) {
      manualLocationTagMappings = new HashMap<>();
    }

    mappableLocationTags.remove(new LocationTagDTO(pdsLocationTag));
    manualLocationTagMappings.put(pdsLocationTag, mdsLocationTag);
    session.setAttribute("manualLocationTagMappings", manualLocationTagMappings);

    return modelAndView;
  }

  @RequestMapping(value = ADD_LOCATION_TAG_FROM_MDS_MAPPING, method = RequestMethod.POST)
  public ModelAndView addLocationTagFromMDSMapping(
      HttpSession session, @ModelAttribute("locationTagBean") LocationTagBean locationTagBean) {

    Map<LocationTagDTO, Integer> mappableLocationTags = getMappableLocationTags(session);
    ModelAndView modelAndView = getRedirectToMandatoryStep();

    if (locationTagBean.getKey() == null
        || StringUtils.isEmpty(((String) locationTagBean.getKey()))) {
      modelAndView.addObject(
          "errorRequiredPDSValue", "eptsharmonization.error.locationTagForMapping.required");
      return modelAndView;
    }
    if (locationTagBean.getValue() == null
        || StringUtils.isEmpty(((String) locationTagBean.getValue()))) {
      modelAndView.addObject(
          "errorRequiredMdsValue", "eptsharmonization.error.locationTagForMapping.required");
      return modelAndView;
    }

    LocationTag pdsLocationTag =
        this.harmonizationLocationTagService.findPDSLocationTagByUuid(
            (String) locationTagBean.getKey());
    LocationTag mdsLocationTag =
        this.harmonizationLocationTagService.findMDSLocationTagByUuid(
            (String) locationTagBean.getValue());

    Map<LocationTag, LocationTag> manualLocationTagMappings =
        (Map<LocationTag, LocationTag>) session.getAttribute("manualLocationTagMappings");

    if (manualLocationTagMappings == null) {
      manualLocationTagMappings = new HashMap<>();
    }

    List<LocationTagDTO> availableMDSMappingTypes =
        (List<LocationTagDTO>) session.getAttribute("availableMDSMappingTypes");
    availableMDSMappingTypes.remove(new LocationTagDTO(mdsLocationTag));

    mappableLocationTags.remove(new LocationTagDTO(pdsLocationTag));
    manualLocationTagMappings.put(pdsLocationTag, mdsLocationTag);
    session.setAttribute("manualLocationTagMappings", manualLocationTagMappings);

    return modelAndView;
  }

  @RequestMapping(value = REMOVE_VISIT_TYPE_MAPPING, method = RequestMethod.POST)
  public ModelAndView removeLocationTagMapping(
      HttpSession session,
      @RequestParam("productionServerLocationTagUuID") String pdsLocationTagUuid) {

    LocationTag pdsLocationTag =
        this.harmonizationLocationTagService.findPDSLocationTagByUuid(pdsLocationTagUuid);

    List<LocationTagDTO> availableMappingTypes =
        (List<LocationTagDTO>) session.getAttribute("availableMappingTypes");

    Map<LocationTagDTO, Integer> mappableLocationTags = getMappableLocationTags(session);

    Map<LocationTag, LocationTag> manualLocationTagMappings =
        (Map<LocationTag, LocationTag>) session.getAttribute("manualLocationTagMappings");

    LocationTag mdsLocationTag = manualLocationTagMappings.remove(pdsLocationTag);

    if (manualLocationTagMappings.isEmpty()) {
      session.removeAttribute("manualLocationTagMappings");
    }

    boolean isMDSAlreadyInPDS = false;
    for (LocationTagDTO dto : availableMappingTypes) {
      LocationTag locationTag = dto.getLocationTag();
      if (mdsLocationTag.getUuid().equals(locationTag.getUuid())
          && mdsLocationTag.getId().equals(locationTag.getId())
          && mdsLocationTag.getName().contentEquals(locationTag.getName())) {
        isMDSAlreadyInPDS = true;
        break;
      }
    }
    if (!isMDSAlreadyInPDS) {
      List<LocationTagDTO> availableMDSMappingTypes =
          (List<LocationTagDTO>) session.getAttribute("availableMDSMappingTypes");
      if (availableMDSMappingTypes != null) {
        LocationTagDTO dto = new LocationTagDTO(mdsLocationTag);
        if (!availableMDSMappingTypes.contains(dto)) {
          availableMDSMappingTypes.add(dto);
          this.sortByDTOName(availableMDSMappingTypes);
        }
      }
    }
    LocationTagDTO pdsDTO = new LocationTagDTO(pdsLocationTag);
    mappableLocationTags.put(
        pdsDTO, this.harmonizationLocationTagService.getNumberOfAffectedLocations(pdsDTO));

    return getRedirectToMandatoryStep();
  }

  @RequestMapping(value = MANUAL_MAPPING_STEP, method = RequestMethod.POST)
  public ModelAndView processManualMapping(HttpSession session, HttpServletRequest request) {
    Map<LocationTagDTO, Integer> mappableLocationTags = getMappableLocationTags(session);
    LocationTagHarmonizationCSVLog.Builder logBuilder = getLogBuilder();
    Map<LocationTag, LocationTag> manualLocationTagMappings =
        (Map<LocationTag, LocationTag>) session.getAttribute("manualLocationTagMappings");

    if (manualLocationTagMappings != null && !manualLocationTagMappings.isEmpty()) {
      harmonizationLocationTagService.saveManualLocationTagMappings(manualLocationTagMappings);
      String message =
          messageSourceService.getMessage(
              "eptsharmonization.locationtag.harmonize.manualMappingDone");
      HARMONIZED_CACHED_SUMMARY.add(message + " " + manualLocationTagMappings.size());

      // Remove from mappable
      for (LocationTag locationTag : manualLocationTagMappings.keySet()) {
        mappableLocationTags.remove(new LocationTagDTO(locationTag));
      }

      logBuilder.appendNewMappedLocationTags(manualLocationTagMappings);
    }
    session.removeAttribute("manualLocationTagMappings");
    ModelAndView modelAndView = getRedirectToMandatoryStep();
    modelAndView.addObject("openmrs_msg", "eptsharmonization.locationtag.harmonized");

    // Write to log file.
    logBuilder.build();
    LOCATION_TAG_MDS_MAPPING_CACHE_CONTROL = new ArrayList<>();
    return modelAndView;
  }

  @RequestMapping(
      value = EXPORT_VISIT_TYPES_LOG,
      method = RequestMethod.POST,
      produces = MediaType.TEXT_PLAIN_VALUE)
  public void exportLog(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    File file = new File("harmonizationLocationTagLog");
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
        "attachment; fileName=location_tag_harmonization_" + defaultLocation + ".log.csv");
    response.setHeader("Cache-Control", "no-cache");
    response.getWriter().write(outputStream.toString(StandardCharsets.ISO_8859_1.name()));
  }

  @RequestMapping(value = EXPORT_VISIT_TYPES_FOR_CRB, method = RequestMethod.POST)
  public void exportLocationTags(
      HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model)
      throws IOException {

    Map<LocationTagDTO, Integer> productionItemsToExport =
        (Map) session.getAttribute("mappableLocationTags");

    ByteArrayOutputStream outputStream =
        LocationTagHarmonizationCSVLog.exportLocationTagLogs(
            defaultLocation, new ArrayList<>(productionItemsToExport.keySet()));
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName=location_tag_crb_" + defaultLocation + "_export.csv");
    response.setHeader("Cache-Control", "no-cache");
    response.getWriter().write(outputStream.toString(StandardCharsets.ISO_8859_1.name()));
  }

  @ModelAttribute("locationTagBean")
  public LocationTagBean formBackingObject() {
    return new LocationTagBean();
  }

  private Map<String, List<LocationTagDTO>> filterConfirmedOverwrites(
      Map<String, List<LocationTagDTO>> bucket, HttpServletRequest request) {
    Map<String, List<LocationTagDTO>> filtered = new HashMap<>();
    for (String uuid : bucket.keySet()) {
      if (Boolean.parseBoolean(request.getParameter(uuid))) {
        filtered.put(uuid, bucket.get(uuid));
      }
    }
    return filtered;
  }

  private Map<LocationTagDTO, Integer> getMappableLocationTags(HttpSession session) {
    Map<LocationTagDTO, Integer> mappableLocationTags =
        (Map) session.getAttribute("mappableLocationTags");
    if (mappableLocationTags != null) {
      return mappableLocationTags;
    }
    mappableLocationTags =
        harmonizationLocationTagService
            .findAllUsedProductionLocationTagsNotSharingUuidWithAnyFromMetadata();
    session.setAttribute("mappableLocationTags", mappableLocationTags);
    return mappableLocationTags;
  }

  private ModelAndView getRedirectToMandatoryStep() {
    return new ModelAndView("redirect:" + MANDATORY_STEP + ".form");
  }

  private LocationTagHarmonizationCSVLog.Builder getLogBuilder() {
    if (logBuilder == null) {
      logBuilder = new LocationTagHarmonizationCSVLog.Builder(defaultLocation);
    }
    return logBuilder;
  }

  @SuppressWarnings("unchecked")
  private List<LocationTag> sortByName(List<LocationTag> list) {
    BeanComparator comparator = new BeanComparator("name");
    Collections.sort(list, comparator);
    return list;
  }

  @SuppressWarnings("unchecked")
  private List<LocationTagDTO> sortByDTOName(List<LocationTagDTO> list) {
    BeanComparator comparator = new BeanComparator("locationTag.name");
    Collections.sort(list, comparator);
    return list;
  }
}
