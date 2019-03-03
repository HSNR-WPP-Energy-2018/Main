package de.hsnr.wpp2018.database;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Data model for the entry of one day.
 * The values are identified by their hour, minute and second of the day.
 */
public class DayEntry {
    private HashMap<Key, Double> values;

    /**
     * Empty constructor
     */
    public DayEntry() {
        this.values = new HashMap<>();
    }

    /**
     * Constructor with data
     *
     * @param values data to be represented by this day
     */
    public DayEntry(HashMap<Key, Double> values) {
        this.values = values;
    }

    /**
     * Getter for managed values
     *
     * @return values
     */
    public HashMap<Key, Double> getValues() {
        return values;
    }

    /**
     * Get value for defined key
     *
     * @param key time of day key
     * @return value represented by the key
     */
    public double getValue(Key key) {
        return values.getOrDefault(key, 0d);
    }

    /**
     * Get value for a date-time.
     *
     * @param time time key
     * @return value represented by the key
     */
    public double getValue(LocalTime time) {
        return getValue(new Key(time.getHour(), time.getMinute(), time.getSecond()));
    }

    /**
     * Add additionally value to the values identified by the key
     *
     * @param key   time of day key
     * @param value value
     */
    public void addValue(Key key, double value) {
        values.put(key, value);
    }

    /**
     * Add additionally value to the values identified by a date-time
     *
     * @param time  time key
     * @param value value
     */
    public void addValue(LocalTime time, double value) {
        values.put(new Key(time.getHour(), time.getMinute(), time.getSecond()), value);
    }

    /**
     * Add multiple values
     *
     * @param values values to be added
     */
    public void addValues(Map<Key, Double> values) {
        this.values.putAll(values);
    }

    /**
     * Add multiple values
     *
     * @param values values to be added
     */
    public void addDateValues(Map<LocalTime, Double> values) {
        values.forEach(this::addValue);
    }

    /**
     * value key composited from hour, minute and second of the day
     */
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