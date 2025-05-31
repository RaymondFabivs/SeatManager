package com.example.seatmanager.util;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * BitmapUtil：将数据库中 BINARY(24) ↔ boolean[192]（或 BitSet）相互转换，
 * 并支持按 5 分钟一格切分成具体时段列表的辅助方法。
 */
public class BitmapUtil {

    /**
     * 将长度为 24 的 byte 数组（二进制总共 192 位）转换为 boolean[192] 数组。
     * 其中，第 0 位表示 0:00-0:05，第 1 位表示 0:05-0:10 …… 第 191 位表示 15:55-16:00，依此类推。
     *
     * @param binaryData 数据库中 BINARY(24) 字段对应的 byte[24]
     * @return boolean[192]：true 表示该 5 分钟时段“可用”，false 表示该时段“已占用”
     */
    public static boolean[] binaryToAvailability(byte[] binaryData) {
        if (binaryData == null || binaryData.length != 24) {
            throw new IllegalArgumentException("binaryData must be exactly 24 bytes (192 bits).");
        }
        boolean[] availability = new boolean[192];
        // 遍历每个字节，从高位到低位依次取 bit
        for (int byteIndex = 0; byteIndex < 24; byteIndex++) {
            byte b = binaryData[byteIndex];
            for (int bitIndex = 0; bitIndex < 8; bitIndex++) {
                // (b >> (7 - bitIndex)) & 0x01 得到当前位的值；1 表示可用
                boolean bit = ((b >> (7 - bitIndex)) & 0x01) == 1;
                availability[byteIndex * 8 + bitIndex] = bit;
            }
        }
        return availability;
    }

    /**
     * 将 boolean[192]（可用/占用）转换回长度为 24 的 byte 数组。
     * 配合 binaryToAvailability() 使用，便于写回数据库。
     *
     * @param availability boolean[192]：true 表示该 5 分钟时段“可用”，false 表示“已占用”
     * @return byte[24]，符合 BINARY(24) 存储格式
     */
    public static byte[] availabilityToBinary(boolean[] availability) {
        if (availability == null || availability.length != 192) {
            throw new IllegalArgumentException("availability must be exactly 192 elements.");
        }
        byte[] binaryData = new byte[24];
        for (int i = 0; i < 24; i++) {
            byte b = 0;
            for (int j = 0; j < 8; j++) {
                int idx = i * 8 + j;
                if (availability[idx]) {
                    // 从高位 (7) 往低位 (0) 赋值
                    b |= (1 << (7 - j));
                }
            }
            binaryData[i] = b;
        }
        return binaryData;
    }

    /**
     * 将 boolean[192] 转换为 List&lt;Integer&gt;，每个 Integer 表示对应可用时段的索引（0 - 191）。
     * 例如，返回列表 [0,1,2, ...] 表示这些时段可用。
     *
     * @param availability boolean[192]
     * @return List<Integer>：所有为 true 的索引集合
     */
    public static List<Integer> getAvailableIndices(boolean[] availability) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < availability.length; i++) {
            if (availability[i]) {
                indices.add(i);
            }
        }
        return indices;
    }

    /**
     * 将 boolean[192] 转换为按“小时:分钟”格式的时段字符串列表，例如“07:00-07:05”、“07:05-07:10”……，
     * 仅包含 availability 为 true 的时段。适合用来在 UI 下拉框里展示可选时间段。
     *
     * @param availability boolean[192]
     * @return List<String>，每个 String 形如 “HH:mm-HH:mm”
     */
    public static List<String> availabilityToTimeSlots(boolean[] availability) {
        List<String> timeSlots = new ArrayList<>();
        for (int index = 0; index < availability.length; index++) {
            if (availability[index]) {
                // 每个 index 对应 5 分钟一个时段
                int totalMinutes = index * 5;               // 例如 index=0 → 0 分钟；index=1 → 5 分钟
                int hour = totalMinutes / 60;               // 小时
                int minute = totalMinutes % 60;             // 分钟
                String start = String.format("%02d:%02d", hour, minute);
                // 结束时间再加 5 分钟
                int endTotalMinutes = totalMinutes + 5;
                int endHour = endTotalMinutes / 60;
                int endMinute = endTotalMinutes % 60;
                String end = String.format("%02d:%02d", endHour, endMinute);
                timeSlots.add(start + "-" + end);
            }
        }
        return timeSlots;
    }

    /**
     * 将 List&lt;Integer&gt;（可用时段索引）转换为按连续区间合并的 String 列表，
     * 例如： [0,1,2,5,6] → ["00:00-00:15", "00:25-00:35"]
     * 用于展示一整块连续可用区间，而不是所有单一的 5 分钟粒度。
     *
     * @param availableIndices List&lt;Integer&gt;，必须是升序且不重复
     * @return List<String>，每个 String 形如 “HH:mm-HH:mm”
     */
    public static List<String> mergeIntoContinuousSlots(List<Integer> availableIndices) {
        List<String> merged = new ArrayList<>();
        if (availableIndices == null || availableIndices.isEmpty()) {
            return merged;
        }
        // 先保证传入集合是升序
        int startIdx = availableIndices.get(0);
        int prevIdx = startIdx;
        for (int i = 1; i < availableIndices.size(); i++) {
            int curr = availableIndices.get(i);
            if (curr == prevIdx + 1) {
                // 继续在同一连续区间
                prevIdx = curr;
            } else {
                // 当前连续区间结束，生成时间区间字符串
                merged.add(buildTimeRangeString(startIdx, prevIdx + 1));
                // 重新开新的连续区间
                startIdx = curr;
                prevIdx = curr;
            }
        }
        // 最后一个区间
        merged.add(buildTimeRangeString(startIdx, prevIdx + 1));
        return merged;
    }

    /**
     * 辅助：将时段索引 rangeStart（包含）到 rangeEnd（不包含）合并，生成“HH:mm-HH:mm”。
     *
     * @param rangeStart 起始索引
     * @param rangeEnd   结束索引（exclusive），表示最后一个时段索引 + 1
     * @return 格式化后的时间区间字符串
     */
    private static String buildTimeRangeString(int rangeStart, int rangeEnd) {
        // rangeStart → 开始时间；rangeEnd → 结束时间
        int startTotal = rangeStart * 5;
        int endTotal = rangeEnd * 5;
        int startHour = startTotal / 60;
        int startMinute = startTotal % 60;
        int endHour = endTotal / 60;
        int endMinute = endTotal % 60;
        String start = String.format("%02d:%02d", startHour, startMinute);
        String end = String.format("%02d:%02d", endHour, endMinute);
        return start + "-" + end;
    }
}
