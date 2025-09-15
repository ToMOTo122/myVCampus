// ============= 5. ScheduleConflictChecker工具类 =============
// src/main/java/com/vcampus/client/utils/ScheduleConflictChecker.java
package com.vcampus.client.utils;

import com.vcampus.client.entity.TimeSlot;
import com.vcampus.common.entity.Course;
import javafx.collections.ObservableList;
import java.util.List;

public class ScheduleConflictChecker {

    public boolean checkConflicts(List<TimeSlot> newTimeSlots, String teacherId,
                                  String classroom, ObservableList<Course> existingCourses) {

        for (Course course : existingCourses) {
            // 检查教师冲突
            if (teacherId != null && teacherId.equals(course.getTeacherId())) {
                List<TimeSlot> courseTimeSlots = TimeUtils.parseScheduleString(course.getSchedule());
                if (TimeUtils.hasTimeConflict(newTimeSlots, courseTimeSlots)) {
                    return true;
                }
            }

            // 检查教室冲突
            if (classroom != null && classroom.equals(course.getClassroom())) {
                List<TimeSlot> courseTimeSlots = TimeUtils.parseScheduleString(course.getSchedule());
                if (TimeUtils.hasTimeConflict(newTimeSlots, courseTimeSlots)) {
                    return true;
                }
            }
        }

        return false;
    }

    public String getConflictDetails(List<TimeSlot> newTimeSlots, String teacherId,
                                     String classroom, ObservableList<Course> existingCourses) {

        StringBuilder conflicts = new StringBuilder();

        for (Course course : existingCourses) {
            List<TimeSlot> courseTimeSlots = TimeUtils.parseScheduleString(course.getSchedule());

            if (teacherId != null && teacherId.equals(course.getTeacherId()) &&
                    TimeUtils.hasTimeConflict(newTimeSlots, courseTimeSlots)) {
                conflicts.append("教师冲突：").append(course.getCourseName()).append("\n");
            }

            if (classroom != null && classroom.equals(course.getClassroom()) &&
                    TimeUtils.hasTimeConflict(newTimeSlots, courseTimeSlots)) {
                conflicts.append("教室冲突：").append(course.getCourseName()).append("\n");
            }
        }

        return conflicts.toString();
    }
}