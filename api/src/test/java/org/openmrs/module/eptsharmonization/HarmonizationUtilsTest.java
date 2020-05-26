package org.openmrs.module.eptsharmonization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import org.openmrs.VisitType;
import org.springframework.util.StringUtils;

/** @uthor Willa Mhawila<a.mhawila@gmail.com> on 5/27/20. */
public class HarmonizationUtilsTest {
  private static class PredefinedValues {
    Integer id;
    String name;
    UUID uuid;

    public PredefinedValues(UUID uuid) {
      this.uuid = uuid;
    }

    public PredefinedValues(Integer id, UUID uuid) {
      this.id = id;
      this.uuid = uuid;
    }

    public PredefinedValues(Integer id, String name, UUID uuid) {
      this.id = id;
      this.name = name;
      this.uuid = uuid;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;

      if (o == null || getClass() != o.getClass()) return false;

      PredefinedValues that = (PredefinedValues) o;

      return new EqualsBuilder()
          .append(id, that.id)
          .append(name, that.name)
          .append(uuid, that.uuid)
          .isEquals();
    }

    @Override
    public int hashCode() {
      return new HashCodeBuilder(17, 37).append(id).append(name).append(uuid).toHashCode();
    }
  }

  private static Random RANDOM = new Random(123476l);

  @Test
  public void
      findElementsInList1WithSameUuidsButDifferentIdsFromList2_shouldReturnCorrectElements() {
    final UUID UUID1 = UUID.randomUUID();
    final UUID UUID2 = UUID.randomUUID();

    Set<PredefinedValues> predefinedValuesSet =
        Sets.newSet(new PredefinedValues(UUID1), new PredefinedValues(UUID2));

    List<VisitType> list1 = new ArrayList<>(createVisitTypeSet(10, predefinedValuesSet));
    List<VisitType> list2 = new ArrayList<>(createVisitTypeSet(20, predefinedValuesSet));

    List<VisitType> sameUuidsDifferentIds =
        HarmonizationUtils.findElementsInList1WithSameUuidsButDifferentIdsFromList2(list1, list2);

    assertEquals(2, sameUuidsDifferentIds.size());
    Set<String> foundUuids =
        Sets.newSet(sameUuidsDifferentIds.get(0).getUuid(), sameUuidsDifferentIds.get(1).getUuid());
    Set<String> expectedUuids = Sets.newSet(UUID1.toString(), UUID2.toString());

    assertEquals(expectedUuids, foundUuids);
  }

  @Test
  public void
      findElementsInList1WithSameUuidsButDifferentIdsFromList2_shouldReturnEmptyListIfNonMatches() {
    List<VisitType> list1 = new ArrayList<>(createVisitTypeSet(10, null));
    List<VisitType> list2 = new ArrayList<>(createVisitTypeSet(20, null));
    List<VisitType> sameUuidsDifferentIds =
        HarmonizationUtils.findElementsInList1WithSameUuidsButDifferentIdsFromList2(list1, list2);
    assertTrue(sameUuidsDifferentIds.isEmpty());
  }

  @Test
  public void findElementsWithDifferentNamesSameUuidsAndIds_shouldReturnCorrectElements() {
    Set<PredefinedValues> sameUuidVisitTypes =
        Sets.newSet(
            new PredefinedValues(1, UUID.randomUUID()), new PredefinedValues(5, UUID.randomUUID()));

    Set<VisitType> set1 = createVisitTypeSet(4, sameUuidVisitTypes);
    Set<VisitType> set2 = createVisitTypeSet(7, sameUuidVisitTypes);

    assertEquals(4, set1.size());
    assertEquals(7, set2.size());

    Map<String, List<VisitType>> foundSameUuidsAndIds =
        HarmonizationUtils.findElementsWithDifferentNamesSameUuidsAndIds(
            new ArrayList<>(set1), new ArrayList<>(set2));

    assertEquals(2, foundSameUuidsAndIds.size());

    Iterator<PredefinedValues> iterator = sameUuidVisitTypes.iterator();
    List<String> expectedUuids =
        Arrays.asList(iterator.next().uuid.toString(), iterator.next().uuid.toString());
    assertTrue(foundSameUuidsAndIds.keySet().containsAll(expectedUuids));

    for (List<VisitType> foundVisitTypes : foundSameUuidsAndIds.values()) {
      for (VisitType visitType : foundVisitTypes) {
        assertTrue(visitType.getId().equals(1) || visitType.getId().equals(5));
      }
    }
  }

  @Test
  public void findElementsWithDifferentNamesSameUuidsAndIds_shouldReturnEmptyMapWhenNoMatchFound() {
    List<VisitType> list1 = new ArrayList<>(createVisitTypeSet(3, null));
    List<VisitType> list2 = new ArrayList<>(createVisitTypeSet(10, null));

    assertEquals(3, list1.size());
    assertEquals(10, list2.size());

    Map<String, List<VisitType>> foundSameUuidsAndIds =
        HarmonizationUtils.findElementsWithDifferentNamesSameUuidsAndIds(list1, list2);

    assertTrue(foundSameUuidsAndIds.isEmpty());
  }

  @Test
  public void findElementsWithDifferentIdsSameUuids_shouldFindCorrectElements() {
    final UUID EXPECTED_IN_FOUND = UUID.randomUUID();
    Set<PredefinedValues> predefinedValuesSet =
        Sets.newSet(new PredefinedValues(1, UUID.randomUUID())); // Should not be found.
    predefinedValuesSet.add(new PredefinedValues(EXPECTED_IN_FOUND));

    Set<VisitType> set1 = createVisitTypeSet(3, predefinedValuesSet);
    Set<VisitType> set2 = createVisitTypeSet(5, predefinedValuesSet);

    Map<String, List<VisitType>> foundSameUuids =
        HarmonizationUtils.findElementsWithDifferentIdsSameUuids(
            new ArrayList<>(set1), new ArrayList<>(set2));

    assertEquals(1, foundSameUuids.size());
    assertEquals(EXPECTED_IN_FOUND.toString(), foundSameUuids.keySet().iterator().next());
  }

  @Test
  public void
      findElementsWithDifferentIdsSameUuids_shouldFindNotFindAnythingIfConditionsAreNotSatisfied() {
    Set<VisitType> set1 = createVisitTypeSet(3, null);
    Set<VisitType> set2 = createVisitTypeSet(5, null);

    Map<String, List<VisitType>> foundSameUuids =
        HarmonizationUtils.findElementsWithDifferentIdsSameUuids(
            new ArrayList<>(set1), new ArrayList<>(set2));

    assertTrue(foundSameUuids.isEmpty());
  }

  @Test
  public void isThePairHarmonized_shouldReturnTrueForSameIdNameAndUuid() {
    final Integer ID = 200;
    final String TYPE_NAME = "Visit Type 1";
    final String TYPE_UUID = UUID.randomUUID().toString();
    VisitType pdsVisitType = createVisitType(ID, TYPE_NAME, TYPE_UUID);
    VisitType mdsVisitType = createVisitType(ID, TYPE_NAME, TYPE_UUID);

    pdsVisitType.setDescription("This can be different according to the spec");
    mdsVisitType.setDescription("This is inddeed different");

    assertTrue(HarmonizationUtils.isThePairHarmonized(pdsVisitType, mdsVisitType));
  }

  @Test
  public void isThePairHarmonized_shouldReturnFalseWhenIdIsNotTheSame() {
    final Integer ID = 200;
    final String TYPE_NAME = "Visit Type 1";
    final String TYPE_UUID = UUID.randomUUID().toString();
    VisitType pdsVisitType = createVisitType(ID, TYPE_NAME, TYPE_UUID);
    VisitType mdsVisitType = createVisitType(201, TYPE_NAME, TYPE_UUID);

    assertFalse(HarmonizationUtils.isThePairHarmonized(pdsVisitType, mdsVisitType));
  }

  @Test
  public void isThePairHarmonized_shouldReturnFalseWhenIdNameNotTheSame() {
    final Integer ID = 200;
    final String TYPE_NAME = "Visit Type 1";
    final String TYPE_UUID = UUID.randomUUID().toString();
    VisitType pdsVisitType = createVisitType(ID, TYPE_NAME, TYPE_UUID);
    VisitType mdsVisitType = createVisitType(ID, "Visit Type 2", TYPE_UUID);

    assertFalse(HarmonizationUtils.isThePairHarmonized(pdsVisitType, mdsVisitType));
  }

  @Test
  public void isThePairHarmonized_shouldReturnFalseWhenIdUuidNotTheSame() {
    final Integer ID = 2500;
    final String TYPE_NAME = "Visit Type 4";
    final String TYPE_UUID = UUID.randomUUID().toString();
    VisitType pdsVisitType = createVisitType(ID, TYPE_NAME, TYPE_UUID);
    VisitType mdsVisitType = createVisitType(ID, TYPE_NAME, UUID.randomUUID().toString());

    assertFalse(HarmonizationUtils.isThePairHarmonized(pdsVisitType, mdsVisitType));
  }

  @Test
  public void isThePairHarmonized_shouldReturnFalseWhenIdOrNameOrUuidIsNull() {
    final Integer ID = 203;
    final String TYPE_NAME = "Visit Type";
    final String TYPE_UUID = UUID.randomUUID().toString();
    VisitType pdsVisitType = createVisitType(ID, TYPE_NAME, TYPE_UUID);
    VisitType mdsVisitType = createVisitType(ID, TYPE_NAME, UUID.randomUUID().toString());

    mdsVisitType.setId(null);
    assertFalse(HarmonizationUtils.isThePairHarmonized(pdsVisitType, mdsVisitType));

    mdsVisitType.setId(ID);
    mdsVisitType.setName(null);
    assertFalse(HarmonizationUtils.isThePairHarmonized(pdsVisitType, mdsVisitType));

    mdsVisitType.setName(TYPE_NAME);
    mdsVisitType.setUuid(null);
    assertFalse(HarmonizationUtils.isThePairHarmonized(pdsVisitType, mdsVisitType));
  }

  @Test
  public void removeAllHarmonizedElements_ShouldRemoveAllHarmonizedFromFirstCollection() {
    final Integer[] COMMON_IDS = {200, 300};
    final String[] COMMON_NAMES = {"type 1", "type 2"};
    final UUID[] COMMON_UUIDS = {UUID.randomUUID(), UUID.randomUUID()};
    Set<PredefinedValues> predefinedValuesSet =
        Sets.newSet(
            new PredefinedValues(COMMON_IDS[0], COMMON_NAMES[0], COMMON_UUIDS[0]),
            new PredefinedValues(COMMON_IDS[1], COMMON_NAMES[1], COMMON_UUIDS[1]));

    // Two sets share two harmonized visit types.
    Set<VisitType> set1 = createVisitTypeSet(4, predefinedValuesSet);
    Set<VisitType> set2 = createVisitTypeSet(5, predefinedValuesSet);

    // Copy set1 before ruining it for later use.
    Set<VisitType> copyOfSet1 = new HashSet<>(set1);

    HarmonizationUtils.removeAllHarmonizedElements(set1, set2);
    assertEquals(2, set1.size());

    HarmonizationUtils.removeAllHarmonizedElements(set2, copyOfSet1);
    assertEquals(3, set2.size());
  }

  private static VisitType createVisitType(final Integer id, final String name, final String uuid) {
    final Integer ID = 200;
    final String TYPE_NAME = "Visit Type 1";
    final String TYPE_UUID = UUID.randomUUID().toString();

    VisitType visitType = new VisitType(id);
    visitType.setName(name);
    visitType.setUuid(uuid);

    return visitType;
  }

  private static Set<VisitType> createVisitTypeSet(int size, Set<PredefinedValues> values) {
    Set<VisitType> visitTypeSet = new HashSet<>();
    int randomOnes = size;
    byte[] randomStringBytes = new byte[7];
    Set<Integer> idsToExcludeLater = new HashSet<>();
    if (values != null && !values.isEmpty()) {
      randomOnes = size - values.size();

      for (PredefinedValues v : values) {
        VisitType visitType = new VisitType();
        if (v.id != null) visitType.setId(v.id);
        else visitType.setId(RANDOM.nextInt());

        visitType.setUuid(v.uuid.toString());

        if (StringUtils.hasText(v.name)) visitType.setName(v.name);
        else visitType.setName(randomString());

        idsToExcludeLater.add(visitType.getId());
        visitTypeSet.add(visitType);
      }
    }

    for (int count = 1; count <= randomOnes; count++) {
      int nextId = RANDOM.nextInt();
      while (idsToExcludeLater.contains(nextId)) {
        nextId = RANDOM.nextInt();
      }
      idsToExcludeLater.add(nextId);

      VisitType visitType = new VisitType(nextId);
      visitType.setUuid(UUID.randomUUID().toString());

      visitType.setName(randomString());

      idsToExcludeLater.add(visitType.getId());
      visitTypeSet.add(visitType);
    }

    return visitTypeSet;
  }

  private static String randomString() {
    byte[] randomStringBytes = new byte[8];
    RANDOM.nextBytes(randomStringBytes);
    return new String(randomStringBytes, Charset.forName("UTF-8"));
  }
}
