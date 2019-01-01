package de.hsnr.wpp2018.base;

import java.time.LocalTime;

public class TimeInterval {

    public static TimeInterval createRange(LocalTime starttime, LocalTime endtime) {
        return new TimeInterval(starttime, endtime);
    }

    private LocalTime startTime;
    private LocalTime endTime;

    private TimeInterval(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public boolean inRange(LocalTime time) {
        return !time.isBefore(startTime) && time.isBefore(endTime);
    }
}