package dev.the_fireplace.annotateddi.test.unit.datastructure;

import com.google.common.collect.Sets;
import dev.the_fireplace.annotateddi.impl.datastructure.LargePowerSet;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public final class LargePowerSetTest
{
    @Test
    public void test_next_emptyInputElements_returnsEmptyEntry() {
        // Arrange
        LargePowerSet<Integer> largePowerSet = new LargePowerSet<>(new HashSet<>(), 0, 0);
        Iterator<Set<Integer>> powerSetIterator = largePowerSet.iterator();

        // Act
        Set<Integer> actual = powerSetIterator.next();

        // Assert
        assertTrue(actual.isEmpty());
        assertFalse(powerSetIterator.hasNext());
    }

    @Test
    public void test_hasNext_withEmptySetNext_returnTrue() {
        // Arrange
        LargePowerSet<Integer> largePowerSet = new LargePowerSet<>(new HashSet<>(), 0, 0);
        Iterator<Set<Integer>> powerSetIterator = largePowerSet.iterator();

        // Act/Assert
        assertTrue(powerSetIterator.hasNext());
    }

    @Test
    public void test_next_multiplePowerSetEntries_returnsEntry_hasNext() {
        // Arrange
        LargePowerSet<String> largePowerSet = new LargePowerSet<>(Sets.newHashSet("1"), 0, 1);
        Iterator<Set<String>> powerSetIterator = largePowerSet.iterator();

        // Act
        powerSetIterator.next();

        // Assert
        assertTrue(powerSetIterator.hasNext());
    }

    @Test
    public void test_hasNext_emptyInputElements_minimumSubsetSizeOne_returnFalse() {
        // Arrange
        LargePowerSet<Integer> largePowerSet = new LargePowerSet<>(new HashSet<>(), 1, 1);
        Iterator<Set<Integer>> powerSetIterator = largePowerSet.iterator();

        // Act/Assert
        assertFalse(powerSetIterator.hasNext());
    }

    @Test
    public void test_hasNext_fiveEntries_minimumSubsetSizeSix_returnFalse() {
        // Arrange
        LargePowerSet<String> largePowerSet = new LargePowerSet<>(Sets.newHashSet("1", "2", "3", "4", "5"), 6, 6);
        Iterator<Set<String>> powerSetIterator = largePowerSet.iterator();

        // Act/Assert
        assertFalse(powerSetIterator.hasNext());
    }

    @Test
    public void test_next_doesNotHaveNext_throwsException() {
        // Arrange
        LargePowerSet<String> largePowerSet = new LargePowerSet<>(Sets.newHashSet("1", "2", "3", "4", "5"), 6, 6);
        Iterator<Set<String>> powerSetIterator = largePowerSet.iterator();

        // Act/Assert
        assertThrows(NoSuchElementException.class, powerSetIterator::next);
    }

    @Test
    public void test_iterator_sixInputs_returns64Subsets() {
        // Arrange
        LargePowerSet<String> largePowerSet = new LargePowerSet<>(Sets.newHashSet("1", "2", "3", "4", "5", "6"), 0, 6);
        Iterator<Set<String>> powerSetIterator = largePowerSet.iterator();

        // Act
        int entryCount = 0;
        while (powerSetIterator.hasNext()) {
            powerSetIterator.next();
            entryCount++;
        }

        // Assert
        assertEquals((int) Math.pow(2, 6), entryCount);
    }

    @Test
    public void test_iterator_sixInputs_minSubsetSize0_maxSubsetSize1_returns7Subsets() {
        // Arrange
        LargePowerSet<String> largePowerSet = new LargePowerSet<>(Sets.newHashSet("1", "2", "3", "4", "5", "6"), 0, 1);
        Iterator<Set<String>> powerSetIterator = largePowerSet.iterator();

        // Act
        int entryCount = 0;
        while (powerSetIterator.hasNext()) {
            powerSetIterator.next();
            entryCount++;
        }

        // Assert
        assertEquals(7, entryCount);
    }

    @Test
    public void test_iterator_sixInputs_minSubsetSize6_maxSubsetSize6_returns1Subset() {
        // Arrange
        LargePowerSet<String> largePowerSet = new LargePowerSet<>(Sets.newHashSet("1", "2", "3", "4", "5", "6"), 6, 6);
        Iterator<Set<String>> powerSetIterator = largePowerSet.iterator();

        // Act
        int entryCount = 0;
        while (powerSetIterator.hasNext()) {
            powerSetIterator.next();
            entryCount++;
        }

        // Assert
        assertEquals(1, entryCount);
    }


    @Test
    public void test_iterator_fourInputs_minSubsetSize2_maxSubsetSize3_returns10Subsets() {
        // Arrange
        LargePowerSet<String> largePowerSet = new LargePowerSet<>(Sets.newHashSet("1", "2", "3", "4"), 2, 3);
        Iterator<Set<String>> powerSetIterator = largePowerSet.iterator();

        // Act
        int entryCount = 0;
        while (powerSetIterator.hasNext()) {
            powerSetIterator.next();
            entryCount++;
        }

        // Assert
        assertEquals(10, entryCount);
    }

    @Test
    public void test_shouldBeRebuiltForPerformance_notYetIterated_smallerReplacement_returnTrue() {
        // Arrange
        LargePowerSet<String> largePowerSet = new LargePowerSet<>(Sets.newHashSet("1", "2", "3", "4", "5", "6"), 3, 3);
        LargePowerSet.LargeIterator<String> powerSetIterator = largePowerSet.iterator();

        // Act
        boolean actual = powerSetIterator.shouldBeRebuiltForPerformance(5);

        // Assert
        assertTrue(actual);
    }

    @Test
    public void test_shouldBeRebuiltForPerformance_notYetIterated_sameSizeReplacement_returnFalse() {
        // Arrange
        LargePowerSet<String> largePowerSet = new LargePowerSet<>(Sets.newHashSet("1", "2", "3", "4", "5", "6"), 3, 3);
        LargePowerSet.LargeIterator<String> powerSetIterator = largePowerSet.iterator();

        // Act
        boolean actual = powerSetIterator.shouldBeRebuiltForPerformance(6);

        // Assert
        assertFalse(actual);
    }

    @Test
    public void test_shouldBeRebuiltForPerformance_largerReplacement_returnFalse() {
        // Arrange
        LargePowerSet<String> largePowerSet = new LargePowerSet<>(Sets.newHashSet("1", "2", "3", "4", "5", "6"), 3, 3);
        LargePowerSet.LargeIterator<String> powerSetIterator = largePowerSet.iterator();

        // Act
        boolean actual = powerSetIterator.shouldBeRebuiltForPerformance(10);

        // Assert
        assertFalse(actual);
    }

    @Test
    public void test_shouldBeRebuiltForPerformance_smallerReplacement_remainingIterationsMoreThanRebuiltIterations_returnTrue() {
        // Arrange
        LargePowerSet<String> largePowerSet = new LargePowerSet<>(Sets.newHashSet("1", "2", "3", "4", "5", "6"), 0, 6);
        LargePowerSet.LargeIterator<String> powerSetIterator = largePowerSet.iterator();
        for (int i = 0; i < (Math.pow(2, 6) - Math.pow(2, 5) - 1); i++) {
            powerSetIterator.next();
        }

        // Act
        boolean actual = powerSetIterator.shouldBeRebuiltForPerformance(5);

        // Assert
        assertTrue(actual);
    }

    @Test
    public void test_shouldBeRebuiltForPerformance_smallerReplacement_remainingIterationsEqualToRebuiltIterations_returnFalse() {
        // Arrange
        LargePowerSet<String> largePowerSet = new LargePowerSet<>(Sets.newHashSet("1", "2", "3", "4", "5", "6"), 0, 6);
        LargePowerSet.LargeIterator<String> powerSetIterator = largePowerSet.iterator();
        for (int i = 0; i < (Math.pow(2, 6) - Math.pow(2, 5)); i++) {
            powerSetIterator.next();
        }

        // Act
        boolean actual = powerSetIterator.shouldBeRebuiltForPerformance(5);

        // Assert
        assertFalse(actual);
    }

    @Test
    public void test_shouldBeRebuiltForPerformance_smallerReplacement_remainingIterationsLessThanRebuiltIterations_returnFalse() {
        // Arrange
        LargePowerSet<String> largePowerSet = new LargePowerSet<>(Sets.newHashSet("1", "2", "3", "4", "5", "6"), 0, 6);
        LargePowerSet.LargeIterator<String> powerSetIterator = largePowerSet.iterator();
        for (int i = 0; i < (Math.pow(2, 6) - Math.pow(2, 5) + 1); i++) {
            powerSetIterator.next();
        }

        // Act
        boolean actual = powerSetIterator.shouldBeRebuiltForPerformance(5);

        // Assert
        assertFalse(actual);
    }
}
