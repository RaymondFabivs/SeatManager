package com.example.seatmanager.entity;

import java.util.Arrays;
import java.util.Objects;

/**
 * Seat.java
 * 对应数据库表：seat
 *
 * CREATE TABLE IF NOT EXISTS seat (
 *   seat_id         BIGINT         AUTO_INCREMENT PRIMARY KEY,  -- 座位编号
 *   room_id         INT            NOT NULL,                    -- 所属自习室
 *   x_coord         INT            NOT NULL CHECK (x_coord BETWEEN 1 AND 100), -- 座位 X 坐标
 *   y_coord         INT            NOT NULL CHECK (y_coord BETWEEN 1 AND 100), -- 座位 Y 坐标
 *   near_window     BOOLEAN        NOT NULL DEFAULT FALSE,      -- 是否靠窗
 *   near_door       BOOLEAN        NOT NULL DEFAULT FALSE,      -- 是否靠门
 *   near_socket     BOOLEAN        NOT NULL DEFAULT FALSE,      -- 是否靠近插座
 *   FOREIGN KEY (room_id) REFERENCES study_room(room_id)
 *     ON DELETE CASCADE ON UPDATE CASCADE,
 *   UNIQUE KEY idx_seat_coord (room_id, x_coord, y_coord)
 * );
 * :contentReference[oaicite:12]{index=12}
 */
public class Seat {
    private long seatId;
    private int roomId;
    private int xCoord;
    private int yCoord;
    private boolean nearWindow;
    private boolean nearDoor;
    private boolean nearSocket;

    public Seat() { }

    public Seat(long seatId, int roomId, int xCoord, int yCoord,
                boolean nearWindow, boolean nearDoor, boolean nearSocket) {
        this.seatId = seatId;
        this.roomId = roomId;
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.nearWindow = nearWindow;
        this.nearDoor = nearDoor;
        this.nearSocket = nearSocket;
    }

    // Getters and setters
    public long getSeatId() {
        return seatId;
    }
    public void setSeatId(long seatId) {
        this.seatId = seatId;
    }
    public int getRoomId() {
        return roomId;
    }
    public void setRoomId(int roomId) {
        this.roomId = roomId;
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
    public boolean isNearWindow() {
        return nearWindow;
    }
    public void setNearWindow(boolean nearWindow) {
        this.nearWindow = nearWindow;
    }
    public boolean isNearDoor() {
        return nearDoor;
    }
    public void setNearDoor(boolean nearDoor) {
        this.nearDoor = nearDoor;
    }
    public boolean isNearSocket() {
        return nearSocket;
    }
    public void setNearSocket(boolean nearSocket) {
        this.nearSocket = nearSocket;
    }

    @Override
    public String toString() {
        return "Seat{" +
                "seatId=" + seatId +
                ", roomId=" + roomId +
                ", xCoord=" + xCoord +
                ", yCoord=" + yCoord +
                ", nearWindow=" + nearWindow +
                ", nearDoor=" + nearDoor +
                ", nearSocket=" + nearSocket +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Seat seat = (Seat) o;
        return seatId == seat.seatId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(seatId);
    }
}
