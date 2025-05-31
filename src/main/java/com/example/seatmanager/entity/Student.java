package com.example.seatmanager.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

/**
 * Student.java
 * 对应数据库表：student
 *
 * CREATE TABLE IF NOT EXISTS student (
 *   student_id        VARCHAR(20)    PRIMARY KEY,     -- 学号
 *   name              VARCHAR(100)   NOT NULL,         -- 姓名
 *   photo_path        VARCHAR(255)   NOT NULL,         -- 照片相对路径
 *   violation_count   INT            NOT NULL DEFAULT 0,-- 当月违规次数
 *   email             VARCHAR(100)   NOT NULL,         -- 邮箱地址
 *   created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
 * );
 * :contentReference[oaicite:7]{index=7}
 */
public class Student {
    private String studentId;
    private String name;
    private String photoPath;
    private int violationCount;
    private String email;
    private LocalDateTime createdAt;

    public Student() { }

    public Student(String studentId, String name, String photoPath,
                   int violationCount, String email, LocalDateTime createdAt) {
        this.studentId = studentId;
        this.name = name;
        this.photoPath = photoPath;
        this.violationCount = violationCount;
        this.email = email;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public String getStudentId() {
        return studentId;
    }
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPhotoPath() {
        return photoPath;
    }
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
    public int getViolationCount() {
        return violationCount;
    }
    public void setViolationCount(int violationCount) {
        this.violationCount = violationCount;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", name='" + name + '\'' +
                ", photoPath='" + photoPath + '\'' +
                ", violationCount=" + violationCount +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Student student = (Student) o;
        return Objects.equals(studentId, student.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }
}
