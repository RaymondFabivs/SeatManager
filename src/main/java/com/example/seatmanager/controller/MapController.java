package com.example.seatmanager.controller;

import com.example.seatmanager.entity.Seat;
import com.example.seatmanager.service.DataAccessException;
import com.example.seatmanager.service.MapService;

import java.time.LocalDate;
import java.util.Map;

/**
 * MapController：处理自习室示意图相关的请求，供 UI 层调用
 */
public class MapController {

    private final MapService mapService = new MapService();

    /**
     * 获取某个自习室在指定日期下，每个座位的可用位图（boolean[192]）
     *
     * @param roomId  自习室 ID
     * @param dateStr 预约日期字符串，格式 "yyyy-MM-dd"
     * @return Map：key = Seat，value = boolean[192]（true = 可用，false = 不可用）
     * @throws RuntimeException 如果底层出现数据访问异常
     */
    public Map<Seat, boolean[]> handleGetSeatAvailability(String roomId, String dateStr) {
        try {
            int rid = Integer.parseInt(roomId);
            LocalDate date = LocalDate.parse(dateStr);
            return mapService.getSeatAvailabilityMap(rid, date);
        } catch (DataAccessException dae) {
            throw dae;
        }
    }
}
