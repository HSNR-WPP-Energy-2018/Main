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

    public List<Element> getElements() {
        return elements;
    }

    public List<Element> getElements(List<Descriptor> descriptors, boolean matchAll) {
        //TODO implement
        return new ArrayList<>();
    }

    public List<Element> getElements(List<Descriptor> descriptors) {
        return getElements(descriptors, false);
    }

    public HashMap<LocalDateTime, Double> interpolate(List<Descriptor> descriptors, LocalDateTime start, LocalDateTime end, int interval) {
        //TODO: implement
        return new HashMap<>();
    }

    public HashMap<LocalDateTime, Double> interpolate(List<Descriptor> descriptors, HashMap<LocalDateTime, Double> data) {
        //TODO: implement
        return new HashMap<>();
    }
}