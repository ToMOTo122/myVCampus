// ============= 3. TimeSlot实体类 =============
// src/main/java/com/vcampus/client/entity/TimeSlot.java
package com.vcampus.client.entity;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class TimeSlot implements Serializable {
    private static final long serialVersionUID = 1L;

    private DayOfWeek dayOfWeek;
    private int startPeriod;
    private int endPeriod;
    private LocalTime startTime;
    private LocalTime endTime;

    public static final LocalTime[] PERIOD_START_TIMES = {
            LocalTime.of(8, 0),   // 第1节
            LocalTime.of(8, 50),  // 第2节
            LocalTime.of(10, 0),  // 第3节
            LocalTime.of(10, 50), // 第4节
            LocalTime.of(14, 0),  // 第5节
            LocalTime.of(14, 50), // 第6节
            LocalTime.of(16, 0),  // 第7节
            LocalTime.of(16, 50), // 第8节
            LocalTime.of(19, 0),  // 第9节
            LocalTime.of(19, 50)  // 第10节
    };

    public TimeSlot(DayOfWeek dayOfWeek, int startPeriod, int endPeriod) {
        this.dayOfWeek = dayOfWeek;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
        if (startPeriod >= 1 && startPeriod <= PERIOD_START_TIMES.length) {
            this.startTime = PERIOD_START_TIMES[startPeriod - 1];
            this.endTime = PERIOD_START_TIMES[endPeriod - 1].plusMinutes(45);
        }
    }

    public boolean conflictsWith(TimeSlot other) {
        if (this.dayOfWeek != other.dayOfWeek) return false;
        return !(this.endPeriod < other.startPeriod || this.startPeriod > other.endPeriod);
    }

    // Getter方法
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public int getStartPeriod() { return startPeriod; }
    public int getEndPeriod() { return endPeriod; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
}