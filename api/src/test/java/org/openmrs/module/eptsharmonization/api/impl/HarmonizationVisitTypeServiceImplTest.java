package org.openmrs.module.eptsharmonization.api.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.VisitService;
import org.openmrs.module.eptsharmonization.BaseHarmonizationContextSensitiveTest;
import org.openmrs.module.eptsharmonization.api.HarmonizationVisitTypeService;
import org.openmrs.module.eptsharmonization.api.model.VisitTypeDTO;
import org.openmrs.util.DatabaseUpdater;
import org.springframework.beans.factory.annotation.Autowired;

/** @uthor Willa Mhawila<a.mhawila@gmail.com> on 5/27/20. */
public class HarmonizationVisitTypeServiceImplTest extends BaseHarmonizationContextSensitiveTest {
  private static final String VISIT_TYPE_TEST_DATA_PATH = "visit-type-test-data.xml";

  private HarmonizationVisitTypeService harmonizationVisitTypeService;
  private VisitService visitService;

  public HarmonizationVisitTypeServiceImplTest() throws Exception {}

  @Autowired
  public void setHarmonizationVisitTypeService(
      HarmonizationVisitTypeService harmonizationVisitTypeService) {
    this.harmonizationVisitTypeService = harmonizationVisitTypeService;
  }

  @Autowired
  public void setVisitService(VisitService visitService) {
    this.visitService = visitService;
  }

  @Before
  public void setup() throws Exception {
    // Purging visits and visit types to ensure each test starts with a clean slate,
    // otherwise the
    // save tests wreck havoc.
    DatabaseUpdater.getConnection().prepareStatement("DELETE from visit").execute();
    DatabaseUpdater.getConnection().prepareStatement("DELETE from visit_type").execute();
    executeDataSet(VISIT_TYPE_TEST_DATA_PATH);
  }

  @Test
  public void
      findAllMetadataVisitTypesNotInHarmonyWithProduction_shouldFindAllVisitTypesFromMDSNotInHarmonyWithPDS() {
    List<VisitTypeDTO> notInHarmonyWithProduction =
        harmonizationVisitTypeService.findAllMetadataVisitTypesNotInHarmonyWithProduction();
    assertEquals(13, notInHarmonyWithProduction.size());
  }

  @Test
  public void
      findAllProductionVisitTypesNotInHarmonyWithMetadata_shouldFindAllVisitTypesFromPDSNotInHarmonyWithMDS() {
    List<VisitTypeDTO> notInHarmonyWithMetadata =
        harmonizationVisitTypeService.findAllProductionVisitTypesNotInHarmonyWithMetadata();
    assertEquals(8, notInHarmonyWithMetadata.size());
  }

  @Test
  public void
      findAllProductionVisitTypesNotSharingUuidWithAnyFromMetadata_shouldFindTheCorrectRecords() {
    List<VisitTypeDTO> notInMDS =
        harmonizationVisitTypeService
            .findAllProductionVisitTypesNotSharingUuidWithAnyFromMetadata();
    assertEquals(2, notInMDS.size());
  }

  @Test
  public void
      findAllUselessProductionVisitTypesNotSharingUuidWithAnyFromMetadata_shouldFindTheCorrectRecords() {
    List<VisitTypeDTO> uselessOnes =
        harmonizationVisitTypeService
            .findAllUselessProductionVisitTypesNotSharingUuidWithAnyFromMetadata();
    assertEquals(1, uselessOnes.size());
    final Integer EXPECTED_ID = 22;
    assertEquals(EXPECTED_ID, uselessOnes.get(0).getId());
  }

  @Test
  public void findAllVisitTypesWithDifferentNameAndSameUUIDAndID_shouldFindTheCorrectRecords() {
    Map<String, List<VisitTypeDTO>> sameIdUUidDifferentNames =
        harmonizationVisitTypeService.findAllVisitTypesWithDifferentNameAndSameUUIDAndID();

    List<String> expectedUuids =
        Arrays.asList(
            "64a510a1-fbf4-465f-acd2-cd37bc321cee", "744ebe99-c086-4b2c-bfbb-a68ed4741e49");
    assertEquals(expectedUuids.size(), sameIdUUidDifferentNames.size());
    assertTrue(sameIdUUidDifferentNames.keySet().containsAll(expectedUuids));
  }

  @Test
  public void
      findAllMetadataVisitTypesNotSharingUuidWithAnyFromProduction_shouldFindTheCorrectRecords() {
    List<VisitTypeDTO> completelyMissingInPD =
        harmonizationVisitTypeService
            .findAllMetadataVisitTypesNotSharingUuidWithAnyFromProduction();
    List<String> expectedUuids =
        Arrays.asList(
            "16e8e70e-71a9-4ba7-9138-dfaae88a6969",
            "7fefe3bb-de47-41e1-8993-c9bfcdaf3f37",
            "85b891bf-4ef9-4c08-9c1e-b92c5bd441b0",
            "1cfb7589-c648-45d3-b524-3e8bff50a3c1",
            "d9911494-b231-4b3c-9246-1fe5f269476c",
            "ceae3031-b8a7-47dc-9d4a-700d73dda335",
            "cbcb03a4-b90a-4351-93ee-4b118ed4f6c3");

    assertEquals(expectedUuids.size(), completelyMissingInPD.size());

    List<String> uuids = new ArrayList<>();
    for (VisitTypeDTO dto : completelyMissingInPD) {
      uuids.add(dto.getUuid());
    }

    assertTrue(uuids.containsAll(expectedUuids));
  }

  @Test
  public void saveNewVisitTypeFromMetadata_shouldSaveANewTypeHavingNoConflict() {
    List<VisitTypeDTO> completelyMissingInPD =
        harmonizationVisitTypeService
            .findAllMetadataVisitTypesNotSharingUuidWithAnyFromProduction();
    assertEquals(7, completelyMissingInPD.size());

    // 13, 14, 15,16 and 17 are all not in PDS
    Integer visitTypeIdNotInPDS = 13;
    assertNull(visitService.getVisitType(visitTypeIdNotInPDS));
    // save the one with visit_type_id = 22
    for (VisitTypeDTO visitTypeDTO : completelyMissingInPD) {
      if (visitTypeDTO.getId().equals(visitTypeIdNotInPDS)) {
        harmonizationVisitTypeService.saveNewVisitTypeFromMetadata(visitTypeDTO);
        break;
      }
    }

    VisitType newlySavedVisitType = visitService.getVisitType(visitTypeIdNotInPDS);

    assertEquals(visitTypeIdNotInPDS, newlySavedVisitType.getVisitTypeId());
  }

  @Test
  public void saveNewVisitTypeFromMetadata_shouldSaveANewTypeWHavingIdConflictWithExisting() {
    List<VisitTypeDTO> completelyMissingInPD =
        harmonizationVisitTypeService
            .findAllMetadataVisitTypesNotSharingUuidWithAnyFromProduction();
    assertEquals(7, completelyMissingInPD.size());

    // 11 & 12 have ID conflict
    Integer visitTypeIdOccupiedAlreadyInPDS = 11;

    // Get the conflicted visit type in PDS.
    VisitType conflictedInPDS = visitService.getVisitType(visitTypeIdOccupiedAlreadyInPDS);

    assertNotNull(conflictedInPDS);

    // Get associated existing visits.
    List<Visit> visitsAssociatedWithConflict =
        visitService.getVisits(
            Arrays.asList(conflictedInPDS),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            true,
            true);
    assertEquals(1, visitsAssociatedWithConflict.size());

    // Find and Save the new one with visit type id 11
    VisitTypeDTO theOneToBeSaved = null;
    for (VisitTypeDTO visitTypeDTO : completelyMissingInPD) {
      if (visitTypeDTO.getId().equals(visitTypeIdOccupiedAlreadyInPDS)) {
        theOneToBeSaved = visitTypeDTO;
        break;
      }
    }

    VisitType theNewFromMDSWithConflictedId = theOneToBeSaved.getVisitType();
    harmonizationVisitTypeService.saveNewVisitTypeFromMetadata(theOneToBeSaved);

    // Checks
    VisitType theSavedOne = visitService.getVisitType(visitTypeIdOccupiedAlreadyInPDS);
    assertEquals(theNewFromMDSWithConflictedId.getUuid(), theSavedOne.getUuid());

    // Get the shifted visit type
    VisitType shiftedOne = visitService.getVisitTypeByUuid(conflictedInPDS.getUuid());
    assertFalse(shiftedOne.getVisitTypeId().equals(visitTypeIdOccupiedAlreadyInPDS));

    List<Visit> visitAssociatedWithConflictedAfterTheShift =
        visitService.getVisits(
            Arrays.asList(shiftedOne), null, null, null, null, null, null, null, null, true, true);
    assertEquals(1, visitAssociatedWithConflictedAfterTheShift.size());

    assertEquals(shiftedOne, visitAssociatedWithConflictedAfterTheShift.get(0).getVisitType());
  }

  @Test
  public void saveNewVisitTypesFromMetadata_shouldSaveMissingVisitTypesInPDS() {
    List<VisitTypeDTO> completelyMissingInPD =
        harmonizationVisitTypeService
            .findAllMetadataVisitTypesNotSharingUuidWithAnyFromProduction();
    assertEquals(7, completelyMissingInPD.size());

    for (VisitTypeDTO visitTypeDTO : completelyMissingInPD) {
      assertNull(visitService.getVisitTypeByUuid(visitTypeDTO.getUuid()));
    }

    // Save them.
    harmonizationVisitTypeService.saveNewVisitTypesFromMetadata(completelyMissingInPD);
    for (VisitTypeDTO visitTypeDTO : completelyMissingInPD) {
      assertNotNull(visitService.getVisitTypeByUuid(visitTypeDTO.getUuid()));
    }
  }

  @Test
  public void saveVisitTypesWithDifferentIDAndEqualUUID_shouldSaveVisitTypes() {
    Map<String, List<VisitTypeDTO>> sameUuidDifferentIds =
        harmonizationVisitTypeService.findAllVisitTypesWithDifferentIDAndSameUUID();

    // Expect no exception here.
    harmonizationVisitTypeService
        .updateVisitTypesFromProductionWithSameUuidWithInformationFromMetadata(
            sameUuidDifferentIds);
  }
}
