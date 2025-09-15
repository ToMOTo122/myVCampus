package com.vcampus.common.entity;

import java.io.Serializable;
import java.util.Map;

public class AssignmentStats implements Serializable {
    private static final long serialVersionUID = 1L;

    public int total;
    public int submitted;
    public int notSubmitted;
    public int urgent;

    public AssignmentStats() {}

    public AssignmentStats(Map<String, Object> map) {
        this.total = ((Number) map.getOrDefault("total", 0)).intValue();
        this.submitted = ((Number) map.getOrDefault("submitted", 0)).intValue();
        this.notSubmitted = ((Number) map.getOrDefault("notSubmitted", 0)).intValue();
        this.urgent = ((Number) map.getOrDefault("urgent", 0)).intValue();
    }
}