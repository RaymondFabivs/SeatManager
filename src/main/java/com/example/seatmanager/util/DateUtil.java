package com.example.seatmanager.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * DateUtil：提供常用的日期、时间下拉框选项生成及格式化方法
 */
public class DateUtil {

    /** 时间格式化器：yyyy-MM-dd */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /** 时间格式化器：HH:mm */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 获取从当天开始向后 n 天的日期列表（包含当天）。
     * 下拉框可直接使用此列表展示“2025-06-01”、“2025-06-02”……等格式。
     *
     * @param daysAhead 包含今天，一共往后推算多少天（例如传 7，则返回 7 条数据：今天至今天 + 6）
     * @return List<String>，每个元素格式为 "yyyy-MM-dd"
     */
    public static List<String> getNextNDates(int daysAhead) {
        List<String> dates = new ArrayList<>();
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        for (int i = 0; i < daysAhead; i++) {
            LocalDate d = today.plusDays(i);
            dates.add(d.format(DATE_FORMATTER));
        }
        return dates;
    }

    /**
     * 生成一日内所有 5 分钟粒度的时段列表：从 07:00-07:05、07:05-07:10 …… 到 23:00。
     * 可在“手动预约”界面用于展示全部可选时段（后续再根据当天当前时间及冲突情况过滤）。
     *
     * @return List<String>，每个元素形如 "HH:mm-HH:mm"
     */
    public static List<String> generateAllDailyTimeSlots() {
        List<String> slots = new ArrayList<>();
        // 定义预约起始时间为 07:00，结束时间为 23:00（不包含 23:00-23:05 这样的时段）
        LocalTime start = LocalTime.of(7, 0);
        LocalTime end   = LocalTime.of(23, 0);

        LocalTime cursor = start;
        while (cursor.isBefore(end)) {
            LocalTime next = cursor.plusMinutes(5);
            String slot = cursor.format(TIME_FORMATTER) + "-" + next.format(TIME_FORMATTER);
            slots.add(slot);
            cursor = next;
        }
        return slots;
    }

    /**
     * 根据当前本地时间，判断传入的 "HH:mm-HH:mm" 时段是否已过期（结束时间 ≤ 现在）。
     *
     * @param timeSlot 格式 “HH:mm-HH:mm”
     * @return 如果时段结束时间 ≤ 当前时间返回 true，否则 false
     */
    public static boolean isTimeSlotExpired(String timeSlot) {
        String[] arr = timeSlot.split("-");
        if (arr.length != 2) {
            throw new IllegalArgumentException("Invalid timeSlot format: " + timeSlot);
        }
        LocalTime end = LocalTime.parse(arr[1], TIME_FORMATTER);
        LocalTime now = LocalTime.now(ZoneId.systemDefault());
        return !now.isBefore(end); // 结束时间 ≤ 当前时间则表示已过期
    }

    /**
     * 将 LocalDateTime 转换为字符串 “yyyy-MM-dd HH:mm:ss”，用于日志或数据库操作。
     *
     * @param dt LocalDateTime 对象
     * @return 格式化字符串
     */
    public static String formatDateTime(LocalDateTime dt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dt.format(formatter);
    }

    /**
     * 将日期字符串（yyyy-MM-dd）转换为 LocalDate 对象。
     *
     * @param dateStr "yyyy-MM-dd"
     * @return LocalDate
     */
    public static LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }
}
