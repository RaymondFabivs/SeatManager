package com.example.seatmanager.entity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;

/**
 * Event.java
 * 对应数据库表：event
 *
 * CREATE TABLE IF NOT EXISTS event (
 *   event_id        BIGINT         AUTO_INCREMENT PRIMARY KEY,   -- 事件编号
 *   room_id         INT            NOT NULL,                     -- 发生占用的自习室
 *   event_date      DATE           NOT NULL,                     -- 占用日期
 *   time_bitmap     BINARY(24)     NOT NULL,                     -- 192 段时间的占用位图
 *   reason          TEXT           NOT NULL,                     -- 原因描述
 *   FOREIGN KEY (room_id) REFERENCES study_room(room_id)
 *     ON DELETE CASCADE ON UPDATE CASCADE
 * );
 * :contentReference[oaicite:10]{index=10}
 */
public class Event {
    private long eventId;
    private int roomId;
    private LocalDate eventDate;
    private byte[] timeBitmap;   // 长度固定 24 字节
    private String reason;

    public Event() { }

    public Event(long eventId, int roomId, LocalDate eventDate,
                 byte[] timeBitmap, String reason) {
        this.eventId = eventId;
        this.roomId = roomId;
        this.eventDate = eventDate;
        this.timeBitmap = timeBitmap != null ? Arrays.copyOf(timeBitmap, 24) : null;
        this.reason = reason;
    }

    // Getters and setters
    public long getEventId() {
        return eventId;
    }
    public void setEventId(long eventId) {
        this.eventId = eventId;
    }
    public int getRoomId() {
        return roomId;
    }
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
    public LocalDate getEventDate() {
        return eventDate;
    }
    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }
    public byte[] getTimeBitmap() {
        return timeBitmap != null ? Arrays.copyOf(timeBitmap, 24) : null;
    }
    public void setTimeBitmap(byte[] timeBitmap) {
        this.timeBitmap = timeBitmap != null ? Arrays.copyOf(timeBitmap, 24) : null;
    }
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventId=" + eventId +
                ", roomId=" + roomId +
                ", eventDate=" + eventDate +
                ", timeBitmap=" + Arrays.toString(timeBitmap) +
                ", reason='" + reason + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;
        return eventId == event.eventId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }
}
