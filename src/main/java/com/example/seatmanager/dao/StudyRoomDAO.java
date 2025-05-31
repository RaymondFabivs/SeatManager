package com.example.seatmanager.dao;

import com.example.seatmanager.entity.StudyRoom;
import com.example.seatmanager.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * StudyRoomDAO：对 study_room 表的增删改查操作
 * 表结构（简略）：
 * CREATE TABLE study_room (
 *   room_id            INT PRIMARY KEY,
 *   floor              TINYINT NOT NULL,
 *   building_id        INT NOT NULL,
 *   free_seats_count   INT NOT NULL,
 *   total_seats_count  INT NOT NULL,
 *   x_coord            INT NOT NULL,
 *   y_coord            INT NOT NULL,
 *   FOREIGN KEY (building_id) REFERENCES building(building_id)
 * );
 * :contentReference[oaicite:3]{index=3}
 */
public class StudyRoomDAO {

    /**
     * 根据 room_id 查询单个自习室信息
     */
    public StudyRoom findById(int roomId) throws SQLException {
        String sql = "SELECT room_id, floor, building_id, free_seats_count, total_seats_count, x_coord, y_coord " +
                "FROM study_room WHERE room_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new StudyRoom(
                            rs.getInt("room_id"),
                            rs.getInt("floor"),
                            rs.getInt("building_id"),
                            rs.getInt("free_seats_count"),
                            rs.getInt("total_seats_count"),
                            rs.getInt("x_coord"),
                            rs.getInt("y_coord")
                    );
                }
            }
        }
        return null;
    }

    /**
     * 查询某栋楼、某楼层下的所有自习室
     */
    public List<StudyRoom> findByBuildingAndFloor(int buildingId, int floor) throws SQLException {
        List<StudyRoom> list = new ArrayList<>();
        String sql = "SELECT room_id, floor, building_id, free_seats_count, total_seats_count, x_coord, y_coord " +
                "FROM study_room WHERE building_id = ? AND floor = ? ORDER BY room_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, buildingId);
            ps.setInt(2, floor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new StudyRoom(
                            rs.getInt("room_id"),
                            rs.getInt("floor"),
                            rs.getInt("building_id"),
                            rs.getInt("free_seats_count"),
                            rs.getInt("total_seats_count"),
                            rs.getInt("x_coord"),
                            rs.getInt("y_coord")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * 更新自习室的空座位数（预约或取消后需调用）
     */
    public boolean updateFreeSeatsCount(int roomId, int newFreeCount) throws SQLException {
        String sql = "UPDATE study_room SET free_seats_count = ? WHERE room_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newFreeCount);
            ps.setInt(2, roomId);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    /**
     * 查询某栋楼所有自习室（不分楼层）
     */
    public List<StudyRoom> findByBuilding(int buildingId) throws SQLException {
        List<StudyRoom> list = new ArrayList<>();
        String sql = "SELECT room_id, floor, building_id, free_seats_count, total_seats_count, x_coord, y_coord " +
                "FROM study_room WHERE building_id = ? ORDER BY room_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, buildingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new StudyRoom(
                            rs.getInt("room_id"),
                            rs.getInt("floor"),
                            rs.getInt("building_id"),
                            rs.getInt("free_seats_count"),
                            rs.getInt("total_seats_count"),
                            rs.getInt("x_coord"),
                            rs.getInt("y_coord")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * 插入新自习室（一般由初始化脚本完成，此处可选）
     */
    public boolean insert(StudyRoom studyRoom) throws SQLException {
        String sql = "INSERT INTO study_room(room_id, floor, building_id, free_seats_count, total_seats_count, x_coord, y_coord) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studyRoom.getRoomId());
            ps.setInt(2, studyRoom.getFloor());
            ps.setInt(3, studyRoom.getBuildingId());
            ps.setInt(4, studyRoom.getFreeSeatsCount());
            ps.setInt(5, studyRoom.getTotalSeatsCount());
            ps.setInt(6, studyRoom.getXCoord());
            ps.setInt(7, studyRoom.getYCoord());
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    /**
     * 删除自习室（慎用）
     */
    public boolean deleteById(int roomId) throws SQLException {
        String sql = "DELETE FROM study_room WHERE room_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }
}
