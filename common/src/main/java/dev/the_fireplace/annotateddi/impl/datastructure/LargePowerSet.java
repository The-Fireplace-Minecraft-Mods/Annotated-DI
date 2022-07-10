package dev.the_fireplace.annotateddi.impl.datastructure;

import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.util.*;

public class LargePowerSet<T> implements Iterable<Set<T>>
{
    private final Collection<T> inputEntries;
    private final int minimumSubsetSize;
    private final int maximumSubsetSize;

    /**
     * Keep the number of entries to a minimum, power sets get large and slow very quickly.
     * If you're able to come up with entries as you process which will result in skipping any remaining subsets containing them,
     * it's worth checking {@link LargeIterator#shouldBeRebuiltForPerformance} and creating a new LargePowerSet to iterate through if true.
     */
    public LargePowerSet(Collection<T> inputEntries, int minimumSubsetSize, int maximumSubsetSize) {
        this.inputEntries = inputEntries;
        this.minimumSubsetSize = minimumSubsetSize;
        this.maximumSubsetSize = maximumSubsetSize;
    }

    @Override
    public LargeIterator<T> iterator() {
        return new LargeIterator<>(inputEntries, minimumSubsetSize, maximumSubsetSize);
    }

    public static class LargeIterator<T> implements Iterator<Set<T>>
    {
        //TODO very slow, perhaps make a Spliterator as well to take advantage of multithreading?
        private final List<T> entries;
        private final BigInteger largestCombination;
        private final int minimumSubsetSize;
        private final int maximumSubsetSize;
        private BigInteger currentCombination;
        private Collection<Integer> indicesWithEnabledBit;

        private LargeIterator(Collection<T> inputEntries, int minimumSubsetSize, int maximumSubsetSize) {
            this.entries = ImmutableList.copyOf(inputEntries);
            this.minimumSubsetSize = Math.max(minimumSubsetSize, 0);
            this.maximumSubsetSize = Math.min(maximumSubsetSize, entries.size());
            this.currentCombination = BigInteger.ZERO.subtract(BigInteger.ONE);
            this.largestCombination = getLargestPossibleCombination(entries.size());
        }

        private BigInteger getLargestPossibleCombination(int inputEntryCount) {
            BigInteger largestCombination = BigInteger.valueOf(2).pow(inputEntryCount).subtract(BigInteger.ONE);
            int rightBitsToZeroOut = largestCombination.bitLength() - Math.min(this.maximumSubsetSize, inputEntryCount);
            largestCombination = largestCombination.shiftRight(rightBitsToZeroOut);
            largestCombination = largestCombination.shiftLeft(rightBitsToZeroOut);
            return largestCombination;
        }

        public boolean shouldBeRebuiltForPerformance(int newInputEntryCount) {
            BigInteger rebuiltIterationCount = getLargestPossibleCombination(newInputEntryCount);
            return getRemainingIterations().compareTo(rebuiltIterationCount) > 0;
        }

        private BigInteger getRemainingIterations() {
            return largestCombination.subtract(currentCombination).subtract(BigInteger.ONE);
        }

        @Override
        public boolean hasNext() {
            if (currentCombination.equals(BigInteger.valueOf(-1))) {
                return entries.size() >= minimumSubsetSize;
            }

            return largestCombination.compareTo(currentCombination) > 0;
        }

        @Override
        public Set<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            findNextIndices();
            Set<T> currentSet = new HashSet<>();
            for (Integer index : indicesWithEnabledBit) {
                currentSet.add(entries.get(index));
            }

            return currentSet;
        }

        private void findNextIndices() {
            currentCombination = currentCombination.add(BigInteger.ONE);
            String combinationBinaryValue = currentCombination.toString(2);
            indicesWithEnabledBit = new HashSet<>();
            int index = combinationBinaryValue.indexOf('1');
            while (index != -1) {
                indicesWithEnabledBit.add(index);
                index = combinationBinaryValue.indexOf('1', index + 1);
            }
            if (indicesWithEnabledBit.size() < minimumSubsetSize || indicesWithEnabledBit.size() > maximumSubsetSize) {
                findNextIndices();
            }
        }
    }
}
