package com.vcampus.common.entity;

import java.io.Serializable;
import java.util.Map;

public class CourseStats implements Serializable {
    private static final long serialVersionUID = 1L;

    public int total;
    public int notStarted;
    public int inProgress;
    public int completed;

    public CourseStats() {}

    public CourseStats(Map<String, Object> map) {
        this.total = ((Number) map.getOrDefault("total", 0)).intValue();
        this.notStarted = ((Number) map.getOrDefault("notStarted", 0)).intValue();
        this.inProgress = ((Number) map.getOrDefault("inProgress", 0)).intValue();
        this.completed = ((Number) map.getOrDefault("completed", 0)).intValue();
    }
}
