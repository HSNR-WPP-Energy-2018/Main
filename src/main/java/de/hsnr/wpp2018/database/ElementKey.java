package de.hsnr.wpp2018.database;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Objects;

/**
 * Element key data model
 */
public class ElementKey {

    /**
     * Key types enum
     */
    public enum Type {
        QUARTER,
        MONTH,
        WEEK_OF_YEAR
    }

    /**
     * Determine interval number for given time
     *
     * @param keyType key type enum
     * @param time    key time
     * @return interval number
     */
    public static int getKeyInterval(Type keyType, LocalDate time) {
        switch (keyType) {
            case QUARTER:
                return time.getMonthValue() % 3;
            case MONTH:
                return time.getMonthValue();
            case WEEK_OF_YEAR:
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                return time.get(weekFields.weekOfWeekBasedYear());
            default:
                return 0;
        }
    }

    private Type keyType;
    private int interval;
    private DayOfWeek dayOfWeek;

    public ElementKey(Type keyType, int interval, DayOfWeek dayOfWeek) {
        this.keyType = keyType;
        this.interval = interval;
        this.dayOfWeek = dayOfWeek;
    }

    public Type getKeyType() {
        return keyType;
    }

    public int getInterval() {
        return interval;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    /**
     * Determine if othe time matches this keys time
     *
     * @param date key date
     * @return boolean result
     */
    public boolean matches(LocalDate date) {
        if (getKeyInterval(keyType, date) != this.interval) {
            return false;
        }
        // TODO: set public holidays as sunday
        return date.getDayOfWeek() == dayOfWeek;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementKey that = (ElementKey) o;
        return getInterval() == that.getInterval() &&
                getKeyType() == that.getKeyType() &&
                getDayOfWeek() == that.getDayOfWeek();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKeyType(), getInterval(), getDayOfWeek());
    }

    @Override
    public String toString() {
        return getKeyType() + ":" + getInterval() + ":" + getDayOfWeek();
    }
}
