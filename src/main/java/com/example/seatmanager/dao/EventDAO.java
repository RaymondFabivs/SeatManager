package com.example.seatmanager.dao;

import com.example.seatmanager.entity.Event;
import com.example.seatmanager.util.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * EventDAO：对 event 表的增删改查操作
 * 表结构（简略）：
 * CREATE TABLE event (
 *   event_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
 *   room_id    INT NOT NULL,
 *   event_date DATE NOT NULL,
 *   time_bitmap BINARY(24) NOT NULL,
 *   reason     TEXT NOT NULL,
 *   FOREIGN KEY (room_id) REFERENCES study_room(room_id)
 * );
 * :contentReference[oaicite:6]{index=6}
 */
public class EventDAO {

    /**
     * 根据 event_id 查询单个事件
     */
    public Event findById(long eventId) throws SQLException {
        String sql = "SELECT event_id, room_id, event_date, time_bitmap, reason " +
                "FROM event WHERE event_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToEvent(rs);
                }
            }
        }
        return null;
    }

    /**
     * 查询某自习室在特定日期下的所有事件（通常只有一条或零条）
     */
    public List<Event> findByRoomAndDate(int roomId, LocalDate date) throws SQLException {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT event_id, room_id, event_date, time_bitmap, reason " +
                "FROM event WHERE room_id = ? AND event_date = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToEvent(rs));
                }
            }
        }
        return list;
    }

    /**
     * 插入新事件（如自习室整日被占用的情况）
     */
    public boolean insert(Event event) throws SQLException {
        String sql = "INSERT INTO event(room_id, event_date, time_bitmap, reason) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, event.getRoomId());
            ps.setDate(2, Date.valueOf(event.getEventDate()));
            ps.setBytes(3, event.getTimeBitmap());
            ps.setString(4, event.getReason());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        event.setEventId(keys.getLong(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 删除事件（慎用）
     */
    public boolean deleteById(long eventId) throws SQLException {
        String sql = "DELETE FROM event WHERE event_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    /**
     * 将 ResultSet 当前行映射为 Event 对象
     */
    private Event mapRowToEvent(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setEventId(rs.getLong("event_id"));
        e.setRoomId(rs.getInt("room_id"));
        e.setEventDate(rs.getDate("event_date").toLocalDate());
        e.setTimeBitmap(rs.getBytes("time_bitmap"));
        e.setReason(rs.getString("reason"));
        return e;
    }
}
