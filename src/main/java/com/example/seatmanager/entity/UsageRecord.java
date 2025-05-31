package com.example.seatmanager.entity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;

/**
 * UsageRecord.java
 * 对应数据库表：usage_record
 *
 * CREATE TABLE IF NOT EXISTS usage_record (
 *   record_id       BIGINT         AUTO_INCREMENT PRIMARY KEY,        -- 记录编号
 *   student_id      VARCHAR(20)    NOT NULL,                            -- 预约学生
 *   seat_id         BIGINT         NOT NULL,                            -- 预约座位
 *   record_date     DATE           NOT NULL,                            -- 预约日期
 *   signed          BOOLEAN        NOT NULL DEFAULT FALSE,              -- 是否签到
 *   time_bitmap     BINARY(24)     NOT NULL,                            -- 192 段空闲/占用位图
 *   FOREIGN KEY (student_id) REFERENCES student(student_id)
 *     ON DELETE CASCADE ON UPDATE CASCADE,
 *   FOREIGN KEY (seat_id) REFERENCES seat(seat_id)
 *     ON DELETE CASCADE ON UPDATE CASCADE,
 *   INDEX idx_usage_date_seat (record_date, seat_id)
 * );
 * :contentReference[oaicite:13]{index=13}
 */
public class UsageRecord {
    private long recordId;
    private String studentId;
    private long seatId;
    private LocalDate recordDate;
    private boolean signed;
    private byte[] timeBitmap;  // 长度固定 24 字节

    public UsageRecord() { }

    public UsageRecord(long recordId, String studentId, long seatId,
                       LocalDate recordDate, boolean signed, byte[] timeBitmap) {
        this.recordId = recordId;
        this.studentId = studentId;
        this.seatId = seatId;
        this.recordDate = recordDate;
        this.signed = signed;
        this.timeBitmap = timeBitmap != null ? Arrays.copyOf(timeBitmap, 24) : null;
    }

    // Getters and setters
    public long getRecordId() {
        return recordId;
    }
    public void setRecordId(long recordId) {
        this.recordId = recordId;
    }
    public String getStudentId() {
        return studentId;
    }
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    public long getSeatId() {
        return seatId;
    }
    public void setSeatId(long seatId) {
        this.seatId = seatId;
    }
    public LocalDate getRecordDate() {
        return recordDate;
    }
    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }
    public boolean isSigned() {
        return signed;
    }
    public void setSigned(boolean signed) {
        this.signed = signed;
    }
    public byte[] getTimeBitmap() {
        return timeBitmap != null ? Arrays.copyOf(timeBitmap, 24) : null;
    }
    public void setTimeBitmap(byte[] timeBitmap) {
        this.timeBitmap = timeBitmap != null ? Arrays.copyOf(timeBitmap, 24) : null;
    }

    @Override
    public String toString() {
        return "UsageRecord{" +
                "recordId=" + recordId +
                ", studentId='" + studentId + '\'' +
                ", seatId=" + seatId +
                ", recordDate=" + recordDate +
                ", signed=" + signed +
                ", timeBitmap=" + Arrays.toString(timeBitmap) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UsageRecord that = (UsageRecord) o;
        return recordId == that.recordId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordId);
    }
}
