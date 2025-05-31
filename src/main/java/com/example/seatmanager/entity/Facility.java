package com.example.seatmanager.entity;

import java.util.Objects;

/**
 * Facility.java
 * 对应数据库表：facility
 *
 * CREATE TABLE IF NOT EXISTS facility (
 *   facility_id     INT            AUTO_INCREMENT PRIMARY KEY,     -- 设施编号
 *   room_id         INT            NOT NULL,                       -- 所属自习室
 *   type            ENUM('DOOR','WINDOW','SOCKET') NOT NULL,      -- 类型：门/窗/插座
 *   x_coord         INT            NOT NULL,                       -- 设施坐标 X（1–100）
 *   y_coord         INT            NOT NULL,                       -- 设施坐标 Y（1–100）
 *   FOREIGN KEY (room_id) REFERENCES study_room(room_id)
 *     ON DELETE CASCADE ON UPDATE CASCADE
 * );
 * :contentReference[oaicite:11]{index=11}
 */
public class Facility {
    public enum FacilityType {
        DOOR, WINDOW, SOCKET
    }

    private int facilityId;
    private int roomId;
    private FacilityType type;
    private int xCoord;
    private int yCoord;

    public Facility() { }

    public Facility(int facilityId, int roomId,
                    FacilityType type, int xCoord, int yCoord) {
        this.facilityId = facilityId;
        this.roomId = roomId;
        this.type = type;
        this.xCoord = xCoord;
        this.yCoord = yCoord;
    }

    // Getters and setters
    public int getFacilityId() {
        return facilityId;
    }
    public void setFacilityId(int facilityId) {
        this.facilityId = facilityId;
    }
    public int getRoomId() {
        return roomId;
    }
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
    public FacilityType getType() {
        return type;
    }
    public void setType(FacilityType type) {
        this.type = type;
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
        return "Facility{" +
                "facilityId=" + facilityId +
                ", roomId=" + roomId +
                ", type=" + type +
                ", xCoord=" + xCoord +
                ", yCoord=" + yCoord +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Facility facility = (Facility) o;
        return facilityId == facility.facilityId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(facilityId);
    }
}
