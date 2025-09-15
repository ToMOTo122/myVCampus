// ============= 4. TimeUtils工具类 =============
// src/main/java/com/vcampus/client/utils/TimeUtils.java
package com.vcampus.client.utils;

import com.vcampus.client.entity.TimeSlot;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {

    private static final String[] WEEKDAY_NAMES = {
            "周一", "周二", "周三", "周四", "周五", "周六", "周日"
    };

    public static List<TimeSlot> parseScheduleString(String schedule) {
        List<TimeSlot> timeSlots = new ArrayList<>();

        if (schedule == null || schedule.trim().isEmpty()) {
            return timeSlots;
        }

        // 正则表达式匹配 "周X节次-节次节" 格式
        Pattern pattern = Pattern.compile("周([一二三四五六日])(\\d+)-(\\d+)节");
        Matcher matcher = pattern.matcher(schedule);

        while (matcher.find()) {
            String dayStr = matcher.group(1);
            int startPeriod = Integer.parseInt(matcher.group(2));
            int endPeriod = Integer.parseInt(matcher.group(3));

            DayOfWeek dayOfWeek = parseDayOfWeek(dayStr);
            if (dayOfWeek != null) {
                TimeSlot timeSlot = new TimeSlot(dayOfWeek, startPeriod, endPeriod);
                timeSlots.add(timeSlot);
            }
        }

        return timeSlots;
    }

    public static String formatScheduleString(List<TimeSlot> timeSlots) {
        if (timeSlots == null || timeSlots.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < timeSlots.size(); i++) {
            TimeSlot timeSlot = timeSlots.get(i);
            if (i > 0) {
                sb.append(",");
            }

            String dayName = WEEKDAY_NAMES[timeSlot.getDayOfWeek().getValue() - 1];
            sb.append(dayName)
                    .append(timeSlot.getStartPeriod())
                    .append("-")
                    .append(timeSlot.getEndPeriod())
                    .append("节");
        }

        return sb.toString();
    }

    private static DayOfWeek parseDayOfWeek(String dayStr) {
        switch (dayStr) {
            case "一": return DayOfWeek.MONDAY;
            case "二": return DayOfWeek.TUESDAY;
            case "三": return DayOfWeek.WEDNESDAY;
            case "四": return DayOfWeek.THURSDAY;
            case "五": return DayOfWeek.FRIDAY;
            case "六": return DayOfWeek.SATURDAY;
            case "日": return DayOfWeek.SUNDAY;
            default: return null;
        }
    }

    public static boolean hasTimeConflict(List<TimeSlot> timeSlots1, List<TimeSlot> timeSlots2) {
        for (TimeSlot slot1 : timeSlots1) {
            for (TimeSlot slot2 : timeSlots2) {
                if (slot1.conflictsWith(slot2)) {
                    return true;
                }
            }
        }
        return false;
    }
}