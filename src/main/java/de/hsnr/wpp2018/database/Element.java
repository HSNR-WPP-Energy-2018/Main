package de.hsnr.wpp2018.database;

import de.hsnr.wpp2018.base.Consumption;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Element {
    public static final String EXTENSION = "dbe";

    private int interval;
    private List<Descriptor> descriptors;
    private Map<ElementKey, DayEntry> values;

    public Element(int interval, List<Descriptor> descriptors, Map<ElementKey, DayEntry> entries) {
        this.interval = interval;
        this.descriptors = descriptors;
        this.values = entries;
    }

    public Element(int interval, List<Descriptor> descriptors, ElementKey.Type type, TreeMap<LocalDateTime, Consumption> data) {
        this.interval = interval;
        this.descriptors = descriptors;
        this.values = new HashMap<>();
        List<LocalDateTime> borders = getBorders(type, data.firstKey(), data.lastKey());
        for (int i = 1; i < borders.size(); i++) {
            LocalDateTime start = borders.get(i - 1);
            LocalDateTime end = borders.get(i);
            int keyInterval = ElementKey.getKeyInterval(type, start.toLocalDate());
            SortedMap<LocalDateTime, Consumption> range = data.subMap(start, end);
            for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                HashMap<DayEntry.Key, List<Double>> filtered = new HashMap<>();
                for (LocalDateTime time : range.keySet()) {
                    DayEntry.Key key = new DayEntry.Key(time);
                    if (time.getDayOfWeek() == dayOfWeek) {
                        if (!filtered.containsKey(key)) {
                            filtered.put(key, new ArrayList<>());
                        }
                        filtered.get(key).add(range.get(time).getValue());
                    }
                }
                HashMap<DayEntry.Key, Double> values = new HashMap<>();
                for (DayEntry.Key key : filtered.keySet()) {
                    double average = filtered.get(key).stream().mapToDouble(x -> x).average().orElse(0);
                    values.put(key, average);
                }
                this.values.put(new ElementKey(type, keyInterval, dayOfWeek), new DayEntry(values));
            }
        }
    }

    private List<LocalDateTime> getBorders(ElementKey.Type type, LocalDateTime start, LocalDateTime end) {
        List<LocalDateTime> result = new ArrayList<>();
        result.add(start);
        LocalDateTime current = LocalDateTime.of(start.toLocalDate(), start.toLocalTime());
        while (current.isBefore(end)) {
            switch (type) {
                case QUARTER:
                    current = current.plusMonths(3);
                    break;
                case MONTH:
                    current = current.plusMonths(1);
                    break;
                case WEEK_OF_YEAR:
                    current = current.plusWeeks(1);
                    break;
                default: // fallback - should never happen
                    result.add(end);
                    return result;
            }
            if (current.isBefore(end)) {
                result.add(current);
            }
        }
        result.add(end);
        return result;
    }

    public int getInterval() {
        return interval;
    }

    public Map<ElementKey, DayEntry> getValues() {
        return values;
    }

    public double getValue(LocalDateTime time) {
        for (ElementKey key : values.keySet()) {
            if (key.matches(time.toLocalDate())) {
                return values.get(key).getValue(time.toLocalTime());
            }
        }
        return 0;
    }

    public List<Descriptor> getDescriptors() {
        return descriptors;
    }

    public boolean matchesDescriptors(List<Descriptor> descriptors, boolean matchAll) {
        for (Descriptor queryDescriptor : descriptors) {
            boolean matched = false;
            for (Descriptor elementDescriptor : this.descriptors) {
                matched = matched || queryDescriptor.matches(elementDescriptor);
                if (matched && !matchAll) {
                    return true;
                }
            }
            if (!matched && matchAll) {
                return false;
            }
        }
        return matchAll;
    }

    @Override
    public String toString() {
        return "interval=" + getInterval() + " seconds" +
                " - descriptors=" + getDescriptors().stream().map(Object::toString).collect(Collectors.toList()) +
                " - entries=" + getValues().entrySet().stream().map(entry -> entry.getKey().toString()).collect(Collectors.toList());
    }
}