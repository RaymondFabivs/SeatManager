package com.example.seatmanager.dao;

import com.example.seatmanager.entity.Student;
import com.example.seatmanager.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * StudentDAO：对 student 表的增删改查操作
 * 表结构（简略）：
 * CREATE TABLE student (
 *   student_id      VARCHAR(20) PRIMARY KEY,
 *   name            VARCHAR(100) NOT NULL,
 *   photo_path      VARCHAR(255) NOT NULL,
 *   violation_count INT NOT NULL DEFAULT 0,
 *   email           VARCHAR(100) NOT NULL,
 *   created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
 * );
 * :contentReference[oaicite:1]{index=1}
 */
public class StudentDAO {

    /**
     * 根据 student_id 查询学生信息
     */
    public Student findById(String studentId) throws SQLException {
        String sql = "SELECT student_id, name, photo_path, violation_count, email, created_at " +
                "FROM student WHERE student_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student s = new Student();
                    s.setStudentId(rs.getString("student_id"));
                    s.setName(rs.getString("name"));
                    s.setPhotoPath(rs.getString("photo_path"));
                    s.setViolationCount(rs.getInt("violation_count"));
                    s.setEmail(rs.getString("email"));
                    s.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return s;
                }
            }
        }
        return null;
    }

    /**
     * 插入新学生（注册）
     * @return 插入成功返回 true，否则 false
     */
    public boolean insert(Student student) throws SQLException {
        String sql = "INSERT INTO student(student_id, name, photo_path, violation_count, email) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getStudentId());
            ps.setString(2, student.getName());
            ps.setString(3, student.getPhotoPath());
            ps.setInt(4, student.getViolationCount());
            ps.setString(5, student.getEmail());
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    /**
     * 更新学生当月违规次数
     * @return 更新成功返回 true，否则 false
     */
    public boolean updateViolationCount(String studentId, int newCount) throws SQLException {
        String sql = "UPDATE student SET violation_count = ? WHERE student_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newCount);
            ps.setString(2, studentId);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    /**
     * 删除学生（慎用：会级联删除该学生的使用记录）
     * @return 删除成功返回 true，否则 false
     */
    public boolean deleteById(String studentId) throws SQLException {
        String sql = "DELETE FROM student WHERE student_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }
}
