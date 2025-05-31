package com.example.seatmanager.entity;

/**
 * Building.java
 * 对应数据库表：building
 *
 * CREATE TABLE IF NOT EXISTS building (
 *   building_id  INT            PRIMARY KEY,  -- 教学楼编号（1–6）
 *   x_coord      INT            NOT NULL,     -- 建筑坐标 X（1–100）
 *   y_coord      INT            NOT NULL      -- 建筑坐标 Y（1–100）
 * );
 * :contentReference[oaicite:8]{index=8}
 */
public class Building {
    private int buildingId;
    private int xCoord;
    private int yCoord;

    public Building() { }

    public Building(int buildingId, int xCoord, int yCoord) {
        this.buildingId = buildingId;
        this.xCoord = xCoord;
        this.yCoord = yCoord;
    }

    // Getters and setters
    public int getBuildingId() {
        return buildingId;
    }
    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }
    public int getXCoord() {
        return xCoord;
    }
    public void setXCoord(int xCoord) {
        this.xCoord = xCoord;
    }
    public int getYCoord() {
        return yCoord;
    }
    public void setYCoord(int yCoord) {
        this.yCoord = yCoord;
    }

    @Override
    public String toString() {
        return "Building{" +
                "buildingId=" + buildingId +
                ", xCoord=" + xCoord +
                ", yCoord=" + yCoord +
                '}';
    }
}
