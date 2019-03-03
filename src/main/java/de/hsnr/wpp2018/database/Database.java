package de.hsnr.wpp2018.database;

import de.hsnr.wpp2018.base.Consumption;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * The actual Database containing a list of elements
 */
public class Database {

    private List<Element> elements;

    public Database() {
        this.elements = new ArrayList<>();
    }

    /**
     * Add additional element to the list
     *
     * @param element element
     */
    public void addElement(Element element) {
        this.elements.add(element);
    }

    public List<Element> getElements() {
        return elements;
    }

    /**
     * Get the elements from the list that match either at least one or all descriptors provided
     *
     * @param descriptors required descriptors
     * @param matchAll    match all or at least one descriptor from the provided list
     * @return subset of elements matching the search criteria
     */
    public List<Element> getElements(List<Descriptor> descriptors, boolean matchAll) {
        List<Element> res = new ArrayList<>();
        for (Element element : elements) {
            if (element.matchesDescriptors(descriptors, matchAll)) {
                res.add(element);
            }
        }
        return res;
    }

    /**
     * Get the elements from the list that match at least one descriptors provided
     *
     * @param descriptors required descriptors
     * @return subset of elements matching the search criteria
     */
    public List<Element> getElements(List<Descriptor> descriptors) {
        return getElements(descriptors, false);
    }

    /**
     * Value interpolation for the defined time using the elements matching the descriptors
     *
     * @param elements descriptors to be matched
     * @param time     time to be interpolated
     * @return interpolated value
     */
    private Consumption getValue(List<Element> elements, LocalDateTime time) {
        double value = 0;
        for (Element element : elements) {
            value += element.getValue(time);
        }
        return new Consumption(value / elements.size(), true); //TODO: support other form of weighting?
    }

    /**
     * Interpolation for the given time range with the provided interval without using already recorded data
     *
     * @param descriptors descriptor to be matched
     * @param start       start time
     * @param end         end time
     * @param interval    interval in seconds
     * @return interpolated dataset
     */
    public TreeMap<LocalDateTime, Consumption> interpolate(List<Descriptor> descriptors, LocalDateTime start, LocalDateTime end, int interval) {
        return interpolate(descriptors, start, end, interval, new TreeMap<>());
    }

    /**
     * Interpolation for the given time range with the provided interval using recorded data whenever available
     *
     * @param descriptors descriptor to be matched
     * @param start       start time
     * @param end         end time
     * @param interval    interval in seconds
     * @param data        recorded data with missing elements
     * @return interpolated dataset
     */
    public TreeMap<LocalDateTime, Consumption> interpolate(List<Descriptor> descriptors, LocalDateTime start, LocalDateTime end, int interval, TreeMap<LocalDateTime, Consumption> data) {
        List<Element> elements = getElements(descriptors);
        TreeMap<LocalDateTime, Consumption> res = new TreeMap<>();
        LocalDateTime time = start;
        while (!time.isAfter(end)) {
            //TODO: adjust value according to provided data by defining heuristic
            res.put(time, data.containsKey(time) ? data.get(time) : getValue(elements, time));
            // System.out.println(time + " - " + res.get(time).getValue() + " : " + res.get(time).getValue() + " | " + Math.abs(res.get(time).getValue() - res.get(time).getValue()));
            time = time.plusSeconds(interval);
        }
        return res;
    }
}