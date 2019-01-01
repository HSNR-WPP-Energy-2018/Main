package de.hsnr.wpp2018.database;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Element {
    // just keep a list of values and the interval (minutes) to be independent of actual times
    private int interval;
    private List<Double> values;

    private List<Descriptor> descriptors;

    public Element(int interval, List<Double> values, List<Descriptor> descriptors) {
        this.interval = interval;
        this.values = values;
        this.descriptors = descriptors;
    }

    public int getInterval() {
        return interval;
    }

    public List<Double> getValues() {
        return values;
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

    public double getValue(Month month, int day, int hour, int minute, int second) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tempStart = LocalDateTime.of(now.getYear(), Month.JANUARY, 1, 0, 0, 0);
        LocalDateTime tempKey = LocalDateTime.of(now.getYear(), month, day, hour, minute, second);
        //TODO: how to handle requests for dates in between two keys?
        int key = (int) Math.floor((tempStart.until(tempKey, ChronoUnit.MINUTES) / (double) this.interval) % this.values.size());
        return values.get(key);
    }

    public double getValue(LocalDateTime time) {
        return getValue(time.getMonth(), time.getDayOfMonth(), time.getHour(), time.getMinute(), time.getSecond());
    }

    @Override
    public String toString() {
        return "interval=" + getInterval() +
                " - descriptors=" + getDescriptors().stream().map(Object::toString).collect(Collectors.toList()) +
                " - values=" + getValues().stream().map(Objects::toString).collect(Collectors.toList());
    }
}