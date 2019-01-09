package de.hsnr.wpp2018.database;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DayEntry {
    private HashMap<Key, Double> values;

    public DayEntry() {
        this.values = new HashMap<>();
    }

    public DayEntry(HashMap<Key, Double> values) {
        this.values = values;
    }

    public HashMap<Key, Double> getValues() {
        return values;
    }

    public double getValue(Key key) {
        return values.getOrDefault(key, 0d);
    }

    public double getValue(LocalTime time) {
        return getValue(new Key(time.getHour(), time.getMinute(), time.getSecond()));
    }

    public void addValue(Key key, double value) {
        values.put(key, value);
    }

    public void addValue(LocalTime time, double value) {
        values.put(new Key(time.getHour(), time.getMinute(), time.getSecond()), value);
    }

    public void addValues(Map<Key, Double> values) {
        this.values.putAll(values);
    }

    public void addDateValues(Map<LocalTime, Double> values) {
        values.forEach(this::addValue);
    }

    public static class Key {
        private int hour;
        private int minute;
        private int second;

        public Key(int hour, int minute, int second) {
            this.hour = hour;
            this.minute = minute;
            this.second = second;
        }

        public Key(LocalDateTime dateTime) {
            this.hour = dateTime.getHour();
            this.minute = dateTime.getMinute();
            this.second = dateTime.getSecond();
        }

        public int getHour() {
            return hour;
        }

        public int getMinute() {
            return minute;
        }

        public int getSecond() {
            return second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return getHour() == key.getHour() &&
                    getMinute() == key.getMinute() &&
                    getSecond() == key.getSecond();
        }

        @Override
        public int hashCode() {
            return Objects.hash(getHour(), getMinute(), getSecond());
        }
    }
}