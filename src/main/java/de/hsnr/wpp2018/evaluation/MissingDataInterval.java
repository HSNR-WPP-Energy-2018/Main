package de.hsnr.wpp2018.evaluation;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class MissingDataInterval {
    private LocalDateTime start;
    private LocalDateTime end;

    public MissingDataInterval(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public int getEntryCount(int interval) {
        int diff = Math.toIntExact(getStart().until(getEnd(), ChronoUnit.SECONDS));
        return (diff / interval) + 1;
    }
}