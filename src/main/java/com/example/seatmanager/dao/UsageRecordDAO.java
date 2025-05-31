package com.example.seatmanager.dao;

import com.example.seatmanager.entity.UsageRecord;
import com.example.seatmanager.util.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * UsageRecordDAO：对 usage_record 表的增删改查操作
 * 表结构（简略）：
 * CREATE TABLE usage_record (
 *   record_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
 *   student_id  VARCHAR(20) NOT NULL,
 *   seat_id     BIGINT NOT NULL,
 *   record_date DATE NOT NULL,
 *   signed      BOOLEAN NOT NULL DEFAULT FALSE,
 *   time_bitmap BINARY(24) NOT NULL,
 *   FOREIGN KEY (student_id) REFERENCES student(student_id),
 *   FOREIGN KEY (seat_id) REFERENCES seat(seat_id)
 * );
 * :contentReference[oaicite:7]{index=7}
 */
public class UsageRecordDAO {

    /**
     * 根据 record_id 查询单条使用记录
     */
    public UsageRecord findById(long recordId) throws SQLException {
        String sql = "SELECT record_id, student_id, seat_id, record_date, signed, time_bitmap " +
                "FROM usage_record WHERE record_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, recordId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUsageRecord(rs);
                }
            }
        }
        return null;
    }

    /**
     * 查询某学生所有预约记录，按日期倒序
     */
    public List<UsageRecord> findByStudent(String studentId) throws SQLException {
        List<UsageRecord> list = new ArrayList<>();
        String sql = "SELECT record_id, student_id, seat_id, record_date, signed, time_bitmap " +
                "FROM usage_record WHERE student_id = ? ORDER BY record_date DESC, record_id DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToUsageRecord(rs));
                }
            }
        }
        return list;
    }

    /**
     * 查询某座位在某日期的预约情况（按 record_id 升序）
     */
    public List<UsageRecord> findBySeatAndDate(long seatId, LocalDate date) throws SQLException {
        List<UsageRecord> list = new ArrayList<>();
        String sql = "SELECT record_id, student_id, seat_id, record_date, signed, time_bitmap " +
                "FROM usage_record WHERE seat_id = ? AND record_date = ? ORDER BY record_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, seatId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToUsageRecord(rs));
                }
            }
        }
        return list;
    }

    /**
     * 插入新预约记录
     * @return 插入成功返回 true，否则 false
     */
    public boolean insert(UsageRecord record) throws SQLException {
        String sql = "INSERT INTO usage_record(student_id, seat_id, record_date, signed, time_bitmap) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, record.getStudentId());
            ps.setLong(2, record.getSeatId());
            ps.setDate(3, Date.valueOf(record.getRecordDate()));
            ps.setBoolean(4, record.isSigned());
            ps.setBytes(5, record.getTimeBitmap());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        record.setRecordId(keys.getLong(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 更新签到状态
     * @return 更新成功返回 true，否则 false
     */
    public boolean updateSigned(long recordId, boolean signed) throws SQLException {
        String sql = "UPDATE usage_record SET signed = ? WHERE record_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, signed);
            ps.setLong(2, recordId);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    /**
     * 删除预约记录（取消预约）
     * @return 删除成功返回 true，否则 false
     */
    public boolean deleteById(long recordId) throws SQLException {
        String sql = "DELETE FROM usage_record WHERE record_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, recordId);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    /**
     * 将 ResultSet 当前行映射为 UsageRecord 对象
     */
    private UsageRecord mapRowToUsageRecord(ResultSet rs) throws SQLException {
        UsageRecord r = new UsageRecord();
        r.setRecordId(rs.getLong("record_id"));
        r.setStudentId(rs.getString("student_id"));
        r.setSeatId(rs.getLong("seat_id"));
        r.setRecordDate(rs.getDate("record_date").toLocalDate());
        r.setSigned(rs.getBoolean("signed"));
        r.setTimeBitmap(rs.getBytes("time_bitmap"));
        return r;
    }
}
