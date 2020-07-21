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
import org.openmrs.LocationAttributeType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.eptsharmonization.api.DTOUtils;
import org.openmrs.module.eptsharmonization.api.HarmonizationLocationAttributeTypeService;
import org.openmrs.module.eptsharmonization.api.model.LocationAttributeTypeDTO;
import org.openmrs.module.eptsharmonization.web.bean.LocationAttributeTypeBean;
import org.openmrs.module.eptsharmonization.web.bean.LocationAttributeTypeHarmonizationCSVLog;
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
@Controller(HarmonizeLocationAttributeTypeController.CONTROLLER_NAME)
@SessionAttributes({"removedMappableLocationAttributeTypes"})
public class HarmonizeLocationAttributeTypeController {
  public static final String CONTROLLER_NAME =
      "eptsharmonization.harmonizeLocationAttributeTypeController";
  public static final String ANALYSIS_STEP =
      "/module/eptsharmonization/harmonizeLocationAttributeType";
  public static final String MANDATORY_STEP =
      "/module/eptsharmonization/mandatoryLocationAttributeTypeHarmonization";
  public static final String MANUAL_MAPPING_STEP =
      "/module/eptsharmonization/manualMappingLocationAttributeTypeHarmonization";
  public static final String ADD_LOCATION_ATTRIBUTE_TYPE_MAPPING =
      "/module/eptsharmonization/addLocationAttributeTypeMapping";
  public static final String ADD_LOCATION_ATTRIBUTE_TYPE_FROM_MDS_MAPPING =
      "/module/eptsharmonization/addLocationAttributeTypeFromMDSMapping";
  private static final String REMOVE_LOCATION_ATTRIBUTE_TYPE_MAPPING =
      "/module/eptsharmonization/removeLocationAttributeTypeMapping";
  private static final String EXPORT_LOCATION_ATTRIBUTE_TYPES_LOG =
      "/module/eptsharmonization/exportLocationAttributeTypesHarmonizationLog";
  private static final String EXPORT_LOCATION_ATTRIBUTE_TYPES_FOR_CRB =
      "/module/eptsharmonization/exportLocationAttributeTypes";

  public static List<String> HARMONIZED_CACHED_SUMMARY = new ArrayList<>();
  private static final Logger LOGGER =
      LoggerFactory.getLogger(HarmonizeLocationAttributeTypeController.class);
  private static String defaultLocation;

  private HarmonizationLocationAttributeTypeService harmonizationLocationAttributeTypeService;
  private MessageSourceService messageSourceService;
  private LocationService locationService;
  private AdministrationService adminService;
  private static LocationAttributeTypeHarmonizationCSVLog.Builder logBuilder;

  @Autowired
  public void setHarmonizationLocationAttributeTypeService(
      HarmonizationLocationAttributeTypeService harmonizationLocationAttributeTypeService) {
    this.harmonizationLocationAttributeTypeService = harmonizationLocationAttributeTypeService;
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
  public ModelAndView locationAttributeTypesHarmonyAnalysis(
      HttpSession session,
      @RequestParam(required = false, value = "openmrs_msg") String openmrs_msg) {
    ModelAndView modelAndView = new ModelAndView();

    // Get missing locationAttribute types.
    List<LocationAttributeTypeDTO> missingInPDS =
        harmonizationLocationAttributeTypeService
            .findAllMetadataLocationAttributeTypesNotSharingUuidWithAnyFromProduction();
    LOGGER.debug(
        "Number of location attributes missing in production found is %s", missingInPDS.size());

    List<LocationAttributeTypeDTO> notInMDSNotInUse =
        harmonizationLocationAttributeTypeService
            .findAllUselessProductionLocationAttributeTypesNotSharingUuidWithAnyFromMetadata();
    LOGGER.debug(
        "Number of useless location attributes in production and not in metadata server is %s",
        notInMDSNotInUse.size());

    Map<String, List<LocationAttributeTypeDTO>> sameIdAndUuidDifferentNames =
        harmonizationLocationAttributeTypeService
            .findAllLocationAttributeTypesWithDifferentNameAndSameUUIDAndID();
    LOGGER.debug(
        "Number of location attributes with same id and uuid but different names is %s",
        sameIdAndUuidDifferentNames.size());

    Map<List<LocationAttributeTypeDTO>, Integer> sameIdAndUuidDifferentNamesWithCountAffected =
        new HashMap<>();
    // First entry is from Production server
    for (List<LocationAttributeTypeDTO> matchList : sameIdAndUuidDifferentNames.values()) {
      Integer affectedRows =
          harmonizationLocationAttributeTypeService.getNumberOfAffectedLocationAttributes(
              matchList.get(0));
      sameIdAndUuidDifferentNamesWithCountAffected.put(matchList, affectedRows);
    }

    Map<String, List<LocationAttributeTypeDTO>> sameUuidDifferentIds =
        harmonizationLocationAttributeTypeService
            .findAllLocationAttributeTypesWithDifferentIDAndSameUUID();
    LOGGER.debug(
        "Number of location attributes with same uuid but different ids and/or names is %s",
        sameUuidDifferentIds.size());
    Map<List<LocationAttributeTypeDTO>, Integer> sameUuidDifferentIdsWithAffectedRows =
        new HashMap<>();
    // First entry is from Production server
    for (List<LocationAttributeTypeDTO> matchList : sameUuidDifferentIds.values()) {
      Integer affectedRows =
          harmonizationLocationAttributeTypeService.getNumberOfAffectedLocationAttributes(
              matchList.get(0));
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

    List<LocationAttributeTypeDTO> missingInPDS =
        harmonizationLocationAttributeTypeService
            .findAllMetadataLocationAttributeTypesNotSharingUuidWithAnyFromProduction();

    if (missingInPDS.size() > 0) {
      LOGGER.debug("Adding new %s location attributes from metadata server", missingInPDS.size());

      harmonizationLocationAttributeTypeService.saveNewLocationAttributeTypesFromMetadata(
          missingInPDS);
      String message =
          messageSourceService.getMessage(
              "eptsharmonization.locationattributetype.harmonize.newLocationAttributeTypeAdded");
      HARMONIZED_CACHED_SUMMARY.add(message + " " + missingInPDS.size());
    }

    LocationAttributeTypeHarmonizationCSVLog.Builder logBuilder = getLogBuilder();
    logBuilder.appendLogForNewHarmonizedFromMDSLocationAttributeTypes(missingInPDS);

    List<LocationAttributeTypeDTO> notInMDSNotInUse =
        harmonizationLocationAttributeTypeService
            .findAllUselessProductionLocationAttributeTypesNotSharingUuidWithAnyFromMetadata();
    if (notInMDSNotInUse.size() > 0) {
      LOGGER.debug(
          "Deleting %s useless location attributes from production", notInMDSNotInUse.size());
      harmonizationLocationAttributeTypeService.deleteLocationAttributeTypesFromProduction(
          notInMDSNotInUse);
      String message =
          messageSourceService.getMessage(
              "eptsharmonization.locationattributetype.harmonize.locationAttributeTypesDeleted");
      HARMONIZED_CACHED_SUMMARY.add(message + " " + notInMDSNotInUse.size());
      logBuilder.appendLogForDeleteFromProductionServer(notInMDSNotInUse);
    }

    Map<String, List<LocationAttributeTypeDTO>> sameIdAndUuidDifferentNames =
        harmonizationLocationAttributeTypeService
            .findAllLocationAttributeTypesWithDifferentNameAndSameUUIDAndID();
    if (sameIdAndUuidDifferentNames.size() > 0) {
      LOGGER.debug(
          "Number of location attributes with same id and uuid but different names is %s",
          sameIdAndUuidDifferentNames.size());
      Map<String, List<LocationAttributeTypeDTO>> toOverwrite =
          filterConfirmedOverwrites(sameIdAndUuidDifferentNames, request);
      if (toOverwrite.size() > 0) {
        LOGGER.debug(
            "LocationAttribute types with same IDs and UUIDs to be overwritten are %s",
            toOverwrite.size());
        harmonizationLocationAttributeTypeService.saveLocationAttributeTypesWithDifferentDetails(
            toOverwrite);

        // Remove all overwritten ones.
        for (String uuid : toOverwrite.keySet()) {
          sameIdAndUuidDifferentNames.remove(uuid);
        }

        String message =
            messageSourceService.getMessage(
                "eptsharmonization.locationattributetype.harmonize.locationAttributeTypesUpdated");
        HARMONIZED_CACHED_SUMMARY.add(message + " " + toOverwrite.size());
        logBuilder.appendLogForUpdatedLocationAttributeDetails(toOverwrite);
      }
    }

    Map<String, List<LocationAttributeTypeDTO>> sameUuidDifferentIds =
        harmonizationLocationAttributeTypeService
            .findAllLocationAttributeTypesWithDifferentIDAndSameUUID();
    if (sameUuidDifferentIds.size() > 0) {
      Map<String, List<LocationAttributeTypeDTO>> toOverwrite =
          filterConfirmedOverwrites(sameUuidDifferentIds, request);
      if (toOverwrite.size() > 0) {
        LOGGER.debug(
            "LocationAttribute types with same uuid but different ids and/or names being overwritten with metadata %s",
            toOverwrite.size());
        harmonizationLocationAttributeTypeService
            .updateLocationAttributeTypesFromProductionWithSameUuidWithInformationFromMetadata(
                toOverwrite);
        String message =
            messageSourceService.getMessage(
                "eptsharmonization.locationattributetype.harmonize.locationAttributeTypeSameUuidUpdated");
        HARMONIZED_CACHED_SUMMARY.add(message + " " + toOverwrite.size());
        logBuilder.appendLogForLocationAttributeTypesWithDiferrentIdsAndEqualUUID(toOverwrite);
        // Remove overwritten ones.
        for (String uuid : toOverwrite.keySet()) {
          sameUuidDifferentIds.remove(uuid);
        }
      }
    }

    // Overwrite the remaining same UUID different IDs anyway shifting the already
    // existing ones to
    // new ID and UUID.
    // Because metadata always wins. If one needs to keep them they may be mapped or
    // exported as new
    // suggesting to CRB

    //    harmonizationLocationAttributeTypeService
    //
    // .replacePDSLocationAttributeTypesWithSameUuidWithThoseFromMDS(sameIdAndUuidDifferentNames);
    //    harmonizationLocationAttributeTypeService
    //        .replacePDSLocationAttributeTypesWithSameUuidWithThoseFromMDS(sameUuidDifferentIds);

    Map<LocationAttributeTypeDTO, Integer> mappableLocationAttributeTypes =
        getMappableLocationAttributeTypes(session);

    if (!sameUuidDifferentIds.isEmpty() || !sameIdAndUuidDifferentNames.isEmpty()) {

      List<LocationAttributeTypeDTO> availableMDSMappingTypes =
          this.harmonizationLocationAttributeTypeService
              .findAllMetadataLocationAttributeTypesNotInHarmonyWithProduction();

      LocationAttributeTypeDTO addedMDSMappableLocationAttributeType =
          (LocationAttributeTypeDTO) session.getAttribute("addedMDSMappableLocationAttributeType");
      LocationAttributeType removedMDSMappableLocationAttributeType =
          (LocationAttributeType) session.getAttribute("removedMDSMappableLocationAttributeType");

      session.removeAttribute("addedMDSMappableLocationAttributeType");
      session.removeAttribute("removedMDSMappableLocationAttributeType");
      availableMDSMappingTypes.remove(addedMDSMappableLocationAttributeType);
      if (removedMDSMappableLocationAttributeType != null) {
        LocationAttributeTypeDTO dto =
            new LocationAttributeTypeDTO(removedMDSMappableLocationAttributeType);
        if (!availableMDSMappingTypes.contains(dto)) {
          availableMDSMappingTypes.add(dto);
        }
      }
      session.setAttribute("availableMDSMappingTypes", availableMDSMappingTypes);

      for (Entry<String, List<LocationAttributeTypeDTO>> entry : sameUuidDifferentIds.entrySet()) {
        mappableLocationAttributeTypes.put(entry.getValue().get(0), 1);
      }
      for (Entry<String, List<LocationAttributeTypeDTO>> entry :
          sameIdAndUuidDifferentNames.entrySet()) {
        mappableLocationAttributeTypes.put(entry.getValue().get(0), 1);
      }
      LocationAttributeTypeDTO addedMappableLocationAttributeType =
          (LocationAttributeTypeDTO) session.getAttribute("addedMappableLocationAttributeType");
      LocationAttributeType removedMappableLocationAttributeType =
          (LocationAttributeType) session.getAttribute("removedMappableLocationAttributeType");
      session.removeAttribute("addedMappableLocationAttributeType");
      session.removeAttribute("removedMappableLocationAttributeType");

      mappableLocationAttributeTypes.remove(addedMappableLocationAttributeType);
      if (removedMappableLocationAttributeType != null) {
        mappableLocationAttributeTypes.put(
            new LocationAttributeTypeDTO(removedMappableLocationAttributeType), 1);
      }
      if (!mappableLocationAttributeTypes.isEmpty()) {
        session.setAttribute("mappableLocationAttributeTypes", mappableLocationAttributeTypes);
      }
    }

    if (mappableLocationAttributeTypes != null && mappableLocationAttributeTypes.size() > 0) {
      List<LocationAttributeType> allLocationAttributeTypes =
          locationService.getAllLocationAttributeTypes();
      List<LocationAttributeTypeDTO> toRemoveFromAll =
          new ArrayList<>(mappableLocationAttributeTypes.keySet());
      allLocationAttributeTypes.removeAll(DTOUtils.fromLocationAttributeTypeDTOs(toRemoveFromAll));
      this.sortByName(allLocationAttributeTypes);
      session.setAttribute(
          "availableMappingTypes", DTOUtils.fromLocationAttributeTypes(allLocationAttributeTypes));
      session.setAttribute("mappableLocationAttributeTypes", mappableLocationAttributeTypes);

      // Copy mappables under a different name.
      session.setAttribute(
          "productionLocationAttributeTypesToExport", mappableLocationAttributeTypes);
    } else {
      session.removeAttribute("mappableLocationAttributeTypes");
      session.removeAttribute("productionLocationAttributeTypesToExport");
    }

    modelAndView.addObject("harmonizedVTSummary", HARMONIZED_CACHED_SUMMARY);
    // Write log to file.
    logBuilder.build();

    if (mappableLocationAttributeTypes.size() == 0
        && sameUuidDifferentIds.isEmpty()
        && sameIdAndUuidDifferentNames.isEmpty()) {
      modelAndView.addObject("harmonizationCompleted", true);
    }

    return modelAndView;
  }

  @RequestMapping(value = ADD_LOCATION_ATTRIBUTE_TYPE_MAPPING, method = RequestMethod.POST)
  public ModelAndView addLocationAttributeTypeMapping(
      HttpSession session,
      @ModelAttribute("locationAttributeTypeBean")
          LocationAttributeTypeBean locationAttributeTypeBean) {

    Map<LocationAttributeTypeDTO, Integer> mappableLocationAttributeTypes =
        getMappableLocationAttributeTypes(session);
    ModelAndView modelAndView = getRedirectToMandatoryStep();

    if (locationAttributeTypeBean.getKey() == null
        || StringUtils.isEmpty(((String) locationAttributeTypeBean.getKey()))) {
      modelAndView.addObject(
          "errorRequiredPDSValue",
          "eptsharmonization.error.locationAttributeTypeForMapping.required");
      return modelAndView;
    }
    if (locationAttributeTypeBean.getValue() == null
        || StringUtils.isEmpty(((String) locationAttributeTypeBean.getValue()))) {
      modelAndView.addObject(
          "errorRequiredMdsValue",
          "eptsharmonization.error.locationAttributeTypeForMapping.required");
      return modelAndView;
    }

    LocationAttributeType pdsLocationAttributeType =
        this.harmonizationLocationAttributeTypeService.findPDSLocationAttributeTypeByUuid(
            (String) locationAttributeTypeBean.getKey());
    LocationAttributeType mdsLocationAttributeType =
        this.harmonizationLocationAttributeTypeService.findPDSLocationAttributeTypeByUuid(
            (String) locationAttributeTypeBean.getValue());

    Map<LocationAttributeType, LocationAttributeType> manualLocationAttributeTypeMappings =
        (Map<LocationAttributeType, LocationAttributeType>)
            session.getAttribute("manualLocationAttributeTypeMappings");

    if (manualLocationAttributeTypeMappings == null) {
      manualLocationAttributeTypeMappings = new HashMap<>();
    }

    mappableLocationAttributeTypes.remove(new LocationAttributeTypeDTO(pdsLocationAttributeType));
    manualLocationAttributeTypeMappings.put(pdsLocationAttributeType, mdsLocationAttributeType);

    session.setAttribute(
        "manualLocationAttributeTypeMappings", manualLocationAttributeTypeMappings);
    session.setAttribute(
        "addedMappableLocationAttributeType",
        new LocationAttributeTypeDTO(pdsLocationAttributeType));

    return modelAndView;
  }

  @RequestMapping(value = ADD_LOCATION_ATTRIBUTE_TYPE_FROM_MDS_MAPPING, method = RequestMethod.POST)
  public ModelAndView addLocationAttributeTypeFromMDSMapping(
      HttpSession session,
      @ModelAttribute("locationAttributeTypeBean")
          LocationAttributeTypeBean locationAttributeTypeBean) {

    Map<LocationAttributeTypeDTO, Integer> mappableLocationAttributeTypes =
        getMappableLocationAttributeTypes(session);
    ModelAndView modelAndView = getRedirectToMandatoryStep();

    if (locationAttributeTypeBean.getKey() == null
        || StringUtils.isEmpty(((String) locationAttributeTypeBean.getKey()))) {
      modelAndView.addObject(
          "errorRequiredPDSValue",
          "eptsharmonization.error.locationAttributeTypeForMapping.required");
      return modelAndView;
    }
    if (locationAttributeTypeBean.getValue() == null
        || StringUtils.isEmpty(((String) locationAttributeTypeBean.getValue()))) {
      modelAndView.addObject(
          "errorRequiredMdsValue",
          "eptsharmonization.error.locationAttributeTypeForMapping.required");
      return modelAndView;
    }

    LocationAttributeType pdsLocationAttributeType =
        this.harmonizationLocationAttributeTypeService.findPDSLocationAttributeTypeByUuid(
            (String) locationAttributeTypeBean.getKey());
    LocationAttributeType mdsLocationAttributeType =
        this.harmonizationLocationAttributeTypeService.findMDSLocationAttributeTypeByUuid(
            (String) locationAttributeTypeBean.getValue());

    Map<LocationAttributeType, LocationAttributeType> manualLocationAttributeTypeMappings =
        (Map<LocationAttributeType, LocationAttributeType>)
            session.getAttribute("manualLocationAttributeTypeMappings");

    if (manualLocationAttributeTypeMappings == null) {
      manualLocationAttributeTypeMappings = new HashMap<>();
    }

    List<LocationAttributeTypeDTO> availableMDSMappingTypes =
        (List<LocationAttributeTypeDTO>) session.getAttribute("availableMDSMappingTypes");
    availableMDSMappingTypes.remove(new LocationAttributeTypeDTO(mdsLocationAttributeType));

    mappableLocationAttributeTypes.remove(new LocationAttributeTypeDTO(pdsLocationAttributeType));
    manualLocationAttributeTypeMappings.put(pdsLocationAttributeType, mdsLocationAttributeType);
    session.setAttribute(
        "manualLocationAttributeTypeMappings", manualLocationAttributeTypeMappings);
    session.setAttribute(
        "addedMappableLocationAttributeType",
        new LocationAttributeTypeDTO(pdsLocationAttributeType));
    session.setAttribute(
        "addedMDSMappableLocationAttributeType",
        new LocationAttributeTypeDTO(mdsLocationAttributeType));

    return modelAndView;
  }

  @RequestMapping(value = REMOVE_LOCATION_ATTRIBUTE_TYPE_MAPPING, method = RequestMethod.POST)
  public ModelAndView removeLocationAttributeTypeMapping(
      HttpSession session,
      @RequestParam("productionServerLocationAttributeTypeUuID")
          String pdsLocationAttributeTypeUuid) {

    LocationAttributeType pdsLocationAttributeType =
        this.harmonizationLocationAttributeTypeService.findPDSLocationAttributeTypeByUuid(
            pdsLocationAttributeTypeUuid);

    Map<LocationAttributeType, LocationAttributeType> manualLocationAttributeTypeMappings =
        (Map<LocationAttributeType, LocationAttributeType>)
            session.getAttribute("manualLocationAttributeTypeMappings");

    LocationAttributeType mdsLocationAttributeType =
        manualLocationAttributeTypeMappings.remove(pdsLocationAttributeType);

    if (manualLocationAttributeTypeMappings.isEmpty()) {
      session.removeAttribute("manualLocationAttributeTypeMappings");
    }

    List<LocationAttributeTypeDTO> availableMappingTypes =
        (List<LocationAttributeTypeDTO>) session.getAttribute("availableMappingTypes");
    boolean isMDSAlreadyInPDS = false;
    for (LocationAttributeTypeDTO dto : availableMappingTypes) {
      LocationAttributeType type = dto.getLocationAttributeType();
      if (mdsLocationAttributeType.getUuid().equals(type.getUuid())
          && mdsLocationAttributeType.getId().equals(type.getId())
          && mdsLocationAttributeType.getName().contentEquals(type.getName())) {
        isMDSAlreadyInPDS = true;
        break;
      }
    }

    if (!isMDSAlreadyInPDS) {
      session.setAttribute("removedMDSMappableLocationAttributeType", mdsLocationAttributeType);
    }
    session.setAttribute("removedMappableLocationAttributeType", pdsLocationAttributeType);

    return getRedirectToMandatoryStep();
  }

  @RequestMapping(value = MANUAL_MAPPING_STEP, method = RequestMethod.POST)
  public ModelAndView processManualMapping(HttpSession session, HttpServletRequest request) {
    Map<LocationAttributeTypeDTO, Integer> mappableLocationAttributeTypes =
        getMappableLocationAttributeTypes(session);
    LocationAttributeTypeHarmonizationCSVLog.Builder logBuilder = getLogBuilder();
    Map<LocationAttributeType, LocationAttributeType> manualLocationAttributeTypeMappings =
        (Map<LocationAttributeType, LocationAttributeType>)
            session.getAttribute("manualLocationAttributeTypeMappings");

    if (manualLocationAttributeTypeMappings != null
        && !manualLocationAttributeTypeMappings.isEmpty()) {
      harmonizationLocationAttributeTypeService.saveManualLocationAttributeTypeMappings(
          manualLocationAttributeTypeMappings);
      String message =
          messageSourceService.getMessage(
              "eptsharmonization.locationattributetype.harmonize.manualMappingDone");
      HARMONIZED_CACHED_SUMMARY.add(message + " " + manualLocationAttributeTypeMappings.size());

      // Remove from mappable
      for (LocationAttributeType locationAttributeType :
          manualLocationAttributeTypeMappings.keySet()) {
        mappableLocationAttributeTypes.remove(new LocationAttributeTypeDTO(locationAttributeType));
      }

      logBuilder.appendNewMappedLocationAttributeTypes(manualLocationAttributeTypeMappings);
    }
    session.removeAttribute("manualLocationAttributeTypeMappings");
    ModelAndView modelAndView = getRedirectToMandatoryStep();
    modelAndView.addObject("openmrs_msg", "eptsharmonization.encountertype.harmonized");

    // Write to log file.
    logBuilder.build();

    return modelAndView;
  }

  @RequestMapping(
      value = EXPORT_LOCATION_ATTRIBUTE_TYPES_LOG,
      method = RequestMethod.POST,
      produces = MediaType.TEXT_PLAIN_VALUE)
  public void exportLog(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    File file = new File("harmonizationLocationAttributeTypeLog");
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
        "attachment; fileName=location_attribute_type_harmonization_"
            + defaultLocation
            + ".log.csv");
    response.setHeader("Cache-Control", "no-cache");
    response.getWriter().write(outputStream.toString(StandardCharsets.ISO_8859_1.name()));
  }

  @RequestMapping(value = EXPORT_LOCATION_ATTRIBUTE_TYPES_FOR_CRB, method = RequestMethod.POST)
  public void exportLocationAttributeTypes(
      HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model)
      throws IOException {

    Map<LocationAttributeTypeDTO, Integer> productionItemsToExport =
        (Map) session.getAttribute("mappableLocationAttributeTypes");

    ByteArrayOutputStream outputStream =
        LocationAttributeTypeHarmonizationCSVLog.exportLocationAttributeTypeLogs(
            defaultLocation, new ArrayList<>(productionItemsToExport.keySet()));
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition",
        "attachment; fileName=location_attribute_type_crb_" + defaultLocation + "_export.csv");
    response.setHeader("Cache-Control", "no-cache");
    response.getWriter().write(outputStream.toString(StandardCharsets.ISO_8859_1.name()));
  }

  @ModelAttribute("locationAttributeTypeBean")
  public LocationAttributeTypeBean formBackingObject() {
    return new LocationAttributeTypeBean();
  }

  private Map<String, List<LocationAttributeTypeDTO>> filterConfirmedOverwrites(
      Map<String, List<LocationAttributeTypeDTO>> bucket, HttpServletRequest request) {
    Map<String, List<LocationAttributeTypeDTO>> filtered = new HashMap<>();
    for (String uuid : bucket.keySet()) {
      if (Boolean.parseBoolean(request.getParameter(uuid))) {
        filtered.put(uuid, bucket.get(uuid));
      }
    }
    return filtered;
  }

  private Map<LocationAttributeTypeDTO, Integer> getMappableLocationAttributeTypes(
      HttpSession session) {
    Map<LocationAttributeTypeDTO, Integer> mappableLocationAttributeTypes = null;
    if (session != null) {
      mappableLocationAttributeTypes = (Map) session.getAttribute("mappableLocationAttributeTypes");
    }

    if (mappableLocationAttributeTypes != null) {
      return mappableLocationAttributeTypes;
    }

    return harmonizationLocationAttributeTypeService
        .findAllUsedProductionLocationAttributeTypesNotSharingUuidWithAnyFromMetadata();
  }

  private ModelAndView getRedirectToMandatoryStep() {
    return new ModelAndView("redirect:" + MANDATORY_STEP + ".form");
  }

  private LocationAttributeTypeHarmonizationCSVLog.Builder getLogBuilder() {
    if (logBuilder == null) {
      logBuilder = new LocationAttributeTypeHarmonizationCSVLog.Builder(defaultLocation);
    }
    return logBuilder;
  }

  @SuppressWarnings("unchecked")
  private List<LocationAttributeType> sortByName(List<LocationAttributeType> list) {
    BeanComparator comparator = new BeanComparator("name");
    Collections.sort(list, comparator);
    return list;
  }
}
