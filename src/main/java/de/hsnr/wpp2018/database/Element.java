package de.hsnr.wpp2018.database;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

public class Element {
    private Month startMonth = Month.JANUARY;
    private int startDay = 0;
    private int startHour = 0;
    private int startMinute = 0;
    private int startSecond = 0;

    // just keep a list of values and the interval (seconds) to be independent of actual times
    private int interval;
    private List<Double> values;

    private List<Descriptor> descriptors;

    public Element(Month startMonth, int startDay, int startHour, int startMinute, int startSecond, int interval, List<Double> values, List<Descriptor> descriptors) {
        this.startMonth = startMonth;
        this.startDay = startDay;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.startSecond = startSecond;
        this.interval = interval;
        this.values = values;
        this.descriptors = descriptors;
    }

    public Element(int interval, List<Double> values, List<Descriptor> descriptors) {
        this.interval = interval;
        this.values = values;
        this.descriptors = descriptors;
    }

    public Month getStartMonth() {
        return startMonth;
    }

    public int getStartDay() {
        return startDay;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public int getStartSecond() {
        return startSecond;
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

    public double getValue(Month month, int day, int hour, int minute, int second) {
        // TODO: implement
        return -1;
    }

    public double getValue(LocalDateTime date) {
        return getValue(date.getMonth(), date.getDayOfMonth(), date.getHour(), date.getMinute(), date.getSecond());
    }
}