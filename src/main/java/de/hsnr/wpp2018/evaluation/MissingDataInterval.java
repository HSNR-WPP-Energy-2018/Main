package de.hsnr.wpp2018.evaluation;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * data model for managing missing data intervals
 */
public class MissingDataInterval {
    private LocalDateTime start;
    private LocalDateTime end;

    /**
     * Constructor
     *
     * @param start interval start time
     * @param end   interval end time
     */
    public MissingDataInterval(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Getter for start time
     *
     * @return start time
     */
    public LocalDateTime getStart() {
        return start;
    }

    /**
     * Getter for end time
     *
     * @return end time
     */
    public LocalDateTime getEnd() {
        return end;
    }

    /**
     * Get the number of entries that would be contained in this range
     *
     * @param interval data interval in seconds
     * @return number of values
     */
    public int getEntryCount(int interval) {
        int diff = Math.toIntExact(getStart().until(getEnd(), ChronoUnit.SECONDS));
        return (diff / interval) + 1;
    }
}