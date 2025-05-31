package com.example.seatmanager.dao;

import com.example.seatmanager.entity.Building;
import com.example.seatmanager.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * BuildingDAO：对 building 表的增删改查操作
 * 表结构（简略）：
 * CREATE TABLE building (
 *   building_id  INT PRIMARY KEY,
 *   x_coord      INT NOT NULL,
 *   y_coord      INT NOT NULL
 * );
 * :contentReference[oaicite:2]{index=2}
 */
public class BuildingDAO {

    /**
     * 根据 building_id 查询单个教学楼信息
     */
    public Building findById(int buildingId) throws SQLException {
        String sql = "SELECT building_id, x_coord, y_coord FROM building WHERE building_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, buildingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Building(
                            rs.getInt("building_id"),
                            rs.getInt("x_coord"),
                            rs.getInt("y_coord")
                    );
                }
            }
        }
        return null;
    }

    /**
     * 查询所有教学楼信息
     */
    public List<Building> findAll() throws SQLException {
        List<Building> list = new ArrayList<>();
        String sql = "SELECT building_id, x_coord, y_coord FROM building ORDER BY building_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Building(
                        rs.getInt("building_id"),
                        rs.getInt("x_coord"),
                        rs.getInt("y_coord")
                ));
            }
        }
        return list;
    }

    /**
     * 插入新的教学楼（一般为初始化脚本完成，此处可选）
     */
    public boolean insert(Building building) throws SQLException {
        String sql = "INSERT INTO building(building_id, x_coord, y_coord) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, building.getBuildingId());
            ps.setInt(2, building.getXCoord());
            ps.setInt(3, building.getYCoord());
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    /**
     * 更新教学楼坐标（通常不需要）
     */
    public boolean update(Building building) throws SQLException {
        String sql = "UPDATE building SET x_coord = ?, y_coord = ? WHERE building_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, building.getXCoord());
            ps.setInt(2, building.getYCoord());
            ps.setInt(3, building.getBuildingId());
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    /**
     * 删除教学楼（慎用：会级联删除其下属自习室、座位、设施等）
     */
    public boolean deleteById(int buildingId) throws SQLException {
        String sql = "DELETE FROM building WHERE building_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, buildingId);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }
}
