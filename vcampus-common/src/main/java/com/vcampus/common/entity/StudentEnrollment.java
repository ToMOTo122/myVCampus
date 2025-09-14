package com.vcampus.common.entity;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

/** 学籍信息（与 tbl_student_info 对应） */
public class StudentEnrollment implements Serializable {
    private String studentId;
    private String studentNo;
    private Integer admissionYear;
    private Integer graduationYear;
    private Double gpa;            // 只读
    private Integer totalCredits;  // 只读
    private String status;         // 在读/休学/退学/毕业
    private String advisorId;

    // --- getters/setters ---
    public String getStudentId() { return studentId; }
    public void setStudentId(String v) { this.studentId = v; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String v) { this.studentNo = v; }
    public Integer getAdmissionYear() { return admissionYear; }
    public void setAdmissionYear(Integer v) { this.admissionYear = v; }
    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer v) { this.graduationYear = v; }
    public Double getGpa() { return gpa; }
    public void setGpa(Double v) { this.gpa = v; }
    public Integer getTotalCredits() { return totalCredits; }
    public void setTotalCredits(Integer v) { this.totalCredits = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getAdvisorId() { return advisorId; }
    public void setAdvisorId(String v) { this.advisorId = v; }

    /** 便捷：从 ResultSet 构造 */
    public static StudentEnrollment fromRS(ResultSet rs) throws SQLException {
        StudentEnrollment s = new StudentEnrollment();
        s.setStudentId(rs.getString("student_id"));
        s.setStudentNo(rs.getString("student_no"));
        s.setAdmissionYear((Integer) rs.getObject("admission_year"));
        s.setGraduationYear((Integer) rs.getObject("graduation_year"));
        s.setGpa((Double) rs.getObject("gpa"));
        s.setTotalCredits((Integer) rs.getObject("total_credits"));
        s.setStatus(rs.getString("status"));
        s.setAdvisorId(rs.getString("advisor_id"));
        return s;
    }
}