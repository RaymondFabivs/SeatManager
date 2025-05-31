package com.example.seatmanager.dao;

import com.example.seatmanager.entity.Seat;
import com.example.seatmanager.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SeatDAO：对 seat 表的增删改查操作
 * 表结构（简略）：
 * CREATE TABLE seat (
 *   seat_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
 *   room_id       INT NOT NULL,
 *   x_coord       INT NOT NULL,
 *   y_coord       INT NOT NULL,
 *   near_window   BOOLEAN NOT NULL DEFAULT FALSE,
 *   near_door     BOOLEAN NOT NULL DEFAULT FALSE,
 *   near_socket   BOOLEAN NOT NULL DEFAULT FALSE,
 *   FOREIGN KEY (room_id) REFERENCES study_room(room_id),
 *   UNIQUE KEY idx_seat_coord (room_id, x_coord, y_coord)
 * );
 * :contentReference[oaicite:5]{index=5}
 */
public class SeatDAO {

    /**
     * 根据 seat_id 查询单个座位
     */
    public Seat findById(long seatId) throws SQLException {
        String sql = "SELECT seat_id, room_id, x_coord, y_coord, near_window, near_door, near_socket " +
                "FROM seat WHERE seat_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, seatId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToSeat(rs);
                }
            }
        }
        return null;
    }

    /**
     * 查询某自习室下的所有座位
     */
    public List<Seat> findByRoomId(int roomId) throws SQLException {
        List<Seat> list = new ArrayList<>();
        String sql = "SELECT seat_id, room_id, x_coord, y_coord, near_window, near_door, near_socket " +
                "FROM seat WHERE room_id = ? ORDER BY seat_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToSeat(rs));
                }
            }
        }
        return list;
    }

    /**
     * 查询符合条件的座位（如靠窗、靠插座等）
     * @param roomId 自习室 ID
     * @param needWindow true 需靠窗，否则忽略
     * @param needDoor   true 需靠门，否则忽略
     * @param needSocket true 需靠插座，否则忽略
     */
    public List<Seat> findByAttributes(int roomId, boolean needWindow, boolean needDoor, boolean needSocket) throws SQLException {
        StringBuilder sb = new StringBuilder(
                "SELECT seat_id, room_id, x_coord, y_coord, near_window, near_door, near_socket " +
                        "FROM seat WHERE room_id = ?");
        if (needWindow) {
            sb.append(" AND near_window = TRUE");
        }
        if (needDoor) {
            sb.append(" AND near_door = TRUE");
        }
        if (needSocket) {
            sb.append(" AND near_socket = TRUE");
        }
        sb.append(" ORDER BY seat_id");

        List<Seat> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToSeat(rs));
                }
            }
        }
        return list;
    }

    /**
     * 插入新的座位（一般由初始化脚本完成，此处可选）
     */
    public boolean insert(Seat seat) throws SQLException {
        String sql = "INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, seat.getRoomId());
            ps.setInt(2, seat.getXCoord());
            ps.setInt(3, seat.getYCoord());
            ps.setBoolean(4, seat.isNearWindow());
            ps.setBoolean(5, seat.isNearDoor());
            ps.setBoolean(6, seat.isNearSocket());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        seat.setSeatId(keys.getLong(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 删除座位（慎用）
     */
    public boolean deleteById(long seatId) throws SQLException {
        String sql = "DELETE FROM seat WHERE seat_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, seatId);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    /**
     * 将 ResultSet 当前行映射为 Seat 对象
     */
    private Seat mapRowToSeat(ResultSet rs) throws SQLException {
        Seat s = new Seat();
        s.setSeatId(rs.getLong("seat_id"));
        s.setRoomId(rs.getInt("room_id"));
        s.setXCoord(rs.getInt("x_coord"));
        s.setYCoord(rs.getInt("y_coord"));
        s.setNearWindow(rs.getBoolean("near_window"));
        s.setNearDoor(rs.getBoolean("near_door"));
        s.setNearSocket(rs.getBoolean("near_socket"));
        return s;
    }
}
