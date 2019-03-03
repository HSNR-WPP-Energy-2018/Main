package de.hsnr.wpp2018.evaluation;

import java.util.TreeMap;

/**
 * data model for missing data
 */
public class MissingRangesStatistics {
    private TreeMap<Integer, Integer> data;

    /**
     * Constructor
     *
     * @param data already calculated information about missing data
     */
    public MissingRangesStatistics(TreeMap<Integer, Integer> data) {
        this.data = data;
    }

    /**
     * Getter for missing data information
     *
     * @return missing data information
     */
    public TreeMap<Integer, Integer> getData() {
        return data;
    }

    /**
     * Check if there are missing range with the provided count of elements
     *
     * @param elementCount number of elements contained by the missing range
     * @return boolean result
     */
    public boolean containsElementCount(int elementCount) {
        return data.containsKey(elementCount);
    }

    /**
     * Get the number of missing ranges with the provided length
     *
     * @param elementCount mising range number of elements
     * @return number of missing ranges with this length
     */
    public int getCount(int elementCount) {
        return data.getOrDefault(elementCount, 0);
    }

    /**
     * Get lowest number of range elements for a missing range
     *
     * @return lowest key
     */
    public int lowestElementCount() {
        return data.firstKey();
    }

    /**
     * Get highest number of range elements for a missing range
     *
     * @return highest key
     */
    public int highestElementCount() {
        return data.lastKey();
    }

    /**
     * Check whether a missing range exists with fewer elements
     *
     * @param elementCount number of elements
     * @param inclusive    include the provided elementCount
     * @return boolean result
     */
    public boolean hasLowerRange(int elementCount, boolean inclusive) {
        return inclusive ? lowestElementCount() <= elementCount : lowestElementCount() < elementCount;
    }

    /**
     * Check whether a missing range exists with fewer elements
     *
     * @param elementCount number of elements
     * @return boolean result
     */
    public boolean hasLowerRange(int elementCount) {
        return hasLowerRange(elementCount, true);
    }

    /**
     * Check whether a missing range exists with more elements
     *
     * @param elementCount number of elements
     * @param inclusive    include the provided elementCount
     * @return boolean result
     */
    public boolean hasHigherRange(int elementCount, boolean inclusive) {
        return inclusive ? highestElementCount() >= elementCount : highestElementCount() > elementCount;
    }

    /**
     * Check whether a missing range exists with more elements
     *
     * @param elementCount number of elements
     * @return boolean result
     */
    public boolean hasHigherRange(int elementCount) {
        return hasHigherRange(elementCount, true);
    }
}
