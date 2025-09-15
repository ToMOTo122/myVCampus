package com.vcampus.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class StudentAssignment implements Serializable {
    private static final long serialVersionUID = 1L;

    private String studentId;
    private String studentName;
    private int assignmentId;
    private String status;
    private int score;
    private String feedback;
    private String filePath;
    private Timestamp submitTime;
    private String className;

    public StudentAssignment( String studentId, String studentName, int assignmentId, String status, int score, String feedback, String filePath, Timestamp submitTime) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.assignmentId = assignmentId;
        this.status = status;
        this.score = score;
        this.feedback = feedback;
        this.filePath = filePath;
        this.submitTime = submitTime;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public int getAssignmentId() { return assignmentId; }
    public void setAssignmentId(int assignmentId) { this.assignmentId = assignmentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public Timestamp getSubmitTime() { return submitTime; }
    public void setSubmitTime(Timestamp submitTime) { this.submitTime = submitTime; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
}