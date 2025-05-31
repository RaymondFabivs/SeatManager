package com.example.seatmanager.dao;

import com.example.seatmanager.entity.Facility;
import com.example.seatmanager.entity.Facility.FacilityType;
import com.example.seatmanager.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FacilityDAO：对 facility 表的增删改查操作
 * 表结构（简略）：
 * CREATE TABLE facility (
 *   facility_id   INT AUTO_INCREMENT PRIMARY KEY,
 *   room_id       INT NOT NULL,
 *   type          ENUM('DOOR','WINDOW','SOCKET') NOT NULL,
 *   x_coord       INT NOT NULL,
 *   y_coord       INT NOT NULL,
 *   FOREIGN KEY (room_id) REFERENCES study_room(room_id)
 * );
 * :contentReference[oaicite:4]{index=4}
 */
public class FacilityDAO {

    /**
     * 根据 facility_id 查询单个设施信息
     */
    public Facility findById(int facilityId) throws SQLException {
        String sql = "SELECT facility_id, room_id, type, x_coord, y_coord FROM facility WHERE facility_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToFacility(rs);
                }
            }
        }
        return null;
    }

    /**
     * 查询某个自习室下的所有设施（门、窗、插座）
     */
    public List<Facility> findByRoom(int roomId) throws SQLException {
        List<Facility> list = new ArrayList<>();
        String sql = "SELECT facility_id, room_id, type, x_coord, y_coord FROM facility WHERE room_id = ? ORDER BY facility_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToFacility(rs));
                }
            }
        }
        return list;
    }

    /**
     * 插入新设施（一般由初始化脚本完成，此处可选）
     */
    public boolean insert(Facility facility) throws SQLException {
        String sql = "INSERT INTO facility(room_id, type, x_coord, y_coord) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, facility.getRoomId());
            ps.setString(2, facility.getType().name());
            ps.setInt(3, facility.getXCoord());
            ps.setInt(4, facility.getYCoord());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        facility.setFacilityId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 删除设施（慎用）
     */
    public boolean deleteById(int facilityId) throws SQLException {
        String sql = "DELETE FROM facility WHERE facility_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    /**
     * 将 ResultSet 当前行映射为 Facility 对象
     */
    private Facility mapRowToFacility(ResultSet rs) throws SQLException {
        Facility f = new Facility();
        f.setFacilityId(rs.getInt("facility_id"));
        f.setRoomId(rs.getInt("room_id"));
        f.setType(FacilityType.valueOf(rs.getString("type")));
        f.setXCoord(rs.getInt("x_coord"));
        f.setYCoord(rs.getInt("y_coord"));
        return f;
    }
}
