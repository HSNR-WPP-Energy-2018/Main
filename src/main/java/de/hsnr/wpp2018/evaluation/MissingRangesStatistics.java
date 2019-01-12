package de.hsnr.wpp2018.evaluation;

import java.util.TreeMap;

public class MissingRangesStatistics {
    private TreeMap<Integer, Integer> data;

    public MissingRangesStatistics(TreeMap<Integer, Integer> data) {
        this.data = data;
    }

    public TreeMap<Integer, Integer> getData() {
        return data;
    }

    public boolean containsElementCount(int elementCount) {
        return data.containsKey(elementCount);
    }

    public int getCount(int elementCount) {
        return data.getOrDefault(elementCount, 0);
    }

    public int lowestElementCount() {
        return data.firstKey();
    }

    public int highestElementCount() {
        return data.lastKey();
    }

    public boolean hasLowerRange(int elementCount, boolean inclusive) {
        return inclusive ? lowestElementCount() <= elementCount : lowestElementCount() < elementCount;
    }

    public boolean hasLowerRange(int elementCount) {
        return hasLowerRange(elementCount, true);
    }

    public boolean hasHigherRange(int elementCount, boolean inclusive) {
        return inclusive ? highestElementCount() >= elementCount : highestElementCount() > elementCount;
    }

    public boolean hasHigherRange(int elementCount) {
        return hasHigherRange(elementCount, true);
    }
}
