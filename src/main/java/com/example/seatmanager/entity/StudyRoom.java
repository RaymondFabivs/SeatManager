package com.example.seatmanager.entity;

/**
 * StudyRoom.java
 * 对应数据库表：study_room
 *
 * CREATE TABLE IF NOT EXISTS study_room (
 *   room_id           INT            PRIMARY KEY,     -- 自习室 ID
 *   floor             TINYINT        NOT NULL CHECK (floor BETWEEN 1 AND 6),
 *   building_id       INT            NOT NULL,         -- 所属教学楼
 *   free_seats_count  INT            NOT NULL,         -- 当前空座位数
 *   total_seats_count INT            NOT NULL,         -- 总座位数
 *   x_coord           INT            NOT NULL,         -- 自习室坐标 X（1–100）
 *   y_coord           INT            NOT NULL,         -- 自习室坐标 Y（1–100）
 *   FOREIGN KEY (building_id) REFERENCES building(building_id)
 *     ON DELETE RESTRICT ON UPDATE CASCADE
 * );
 * :contentReference[oaicite:9]{index=9}
 */
public class StudyRoom {
    private int roomId;
    private int floor;
    private int buildingId;
    private int freeSeatsCount;
    private int totalSeatsCount;
    private int xCoord;
    private int yCoord;

    public StudyRoom() { }

    public StudyRoom(int roomId, int floor, int buildingId,
                     int freeSeatsCount, int totalSeatsCount,
                     int xCoord, int yCoord) {
        this.roomId = roomId;
        this.floor = floor;
        this.buildingId = buildingId;
        this.freeSeatsCount = freeSeatsCount;
        this.totalSeatsCount = totalSeatsCount;
        this.xCoord = xCoord;
        this.yCoord = yCoord;
    }

    // Getters and setters
    public int getRoomId() {
        return roomId;
    }
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
    public int getFloor() {
        return floor;
    }
    public void setFloor(int floor) {
        this.floor = floor;
    }
    public int getBuildingId() {
        return buildingId;
    }
    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }
    public int getFreeSeatsCount() {
        return freeSeatsCount;
    }
    public void setFreeSeatsCount(int freeSeatsCount) {
        this.freeSeatsCount = freeSeatsCount;
    }
    public int getTotalSeatsCount() {
        return totalSeatsCount;
    }
    public void setTotalSeatsCount(int totalSeatsCount) {
        this.totalSeatsCount = totalSeatsCount;
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
        return "StudyRoom{" +
                "roomId=" + roomId +
                ", floor=" + floor +
                ", buildingId=" + buildingId +
                ", freeSeatsCount=" + freeSeatsCount +
                ", totalSeatsCount=" + totalSeatsCount +
                ", xCoord=" + xCoord +
                ", yCoord=" + yCoord +
                '}';
    }
}
