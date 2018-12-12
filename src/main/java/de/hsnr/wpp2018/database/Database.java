package de.hsnr.wpp2018.database;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    private double getValue(List<Element> elements, LocalDateTime time) {
        double value = 0;
        for (Element element : elements) {
            value += element.getValue(time);
        }
        return value / elements.size(); //TODO: support other form of weighting?
    }

    public HashMap<LocalDateTime, Double> interpolate(List<Descriptor> descriptors, LocalDateTime start, LocalDateTime end, int interval) {
        List<Element> elements = getElements(descriptors);
        HashMap<LocalDateTime, Double> res = new HashMap<>();
        LocalDateTime time = start;
        while (!time.isAfter(end)) {
            res.put(time, getValue(elements, time));
            time = time.plusSeconds(interval);
        }
        return res;
    }

    public HashMap<LocalDateTime, Double> interpolate(List<Descriptor> descriptors, HashMap<LocalDateTime, Double> data) {
        List<Element> elements = getElements(descriptors);
        HashMap<LocalDateTime, Double> res = new HashMap<>();
        for (LocalDateTime time : data.keySet()) {
            //TODO: adjust value according to provided data by defining heuristic
            res.put(time, getValue(elements, time));
        }
        return res;
    }
}