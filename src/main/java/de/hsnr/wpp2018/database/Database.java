package de.hsnr.wpp2018.database;

import de.hsnr.wpp2018.base.Consumption;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class Database {

    private List<Element> elements;

    public Database() {
        this.elements = new ArrayList<>();
    }

    public void addElement(Element element) {
        this.elements.add(element);
    }

    public List<Element> getElements() {
        return elements;
    }

    public List<Element> getElements(List<Descriptor> descriptors, boolean matchAll) {
        List<Element> res = new ArrayList<>();
        for (Element element : elements) {
            if (element.matchesDescriptors(descriptors, matchAll)) {
                res.add(element);
            }
        }
        return res;
    }

    public List<Element> getElements(List<Descriptor> descriptors) {
        return getElements(descriptors, false);
    }

    private Consumption getValue(List<Element> elements, LocalDateTime time) {
        double value = 0;
        for (Element element : elements) {
            value += element.getValue(time);
        }
        return new Consumption(value / elements.size(), true); //TODO: support other form of weighting?
    }

    public TreeMap<LocalDateTime, Consumption> interpolate(List<Descriptor> descriptors, LocalDateTime start, LocalDateTime end, int interval) {
        return interpolate(descriptors, start, end, interval, new TreeMap<>());
    }

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