package com.example.seatmanager.controller;

import com.example.seatmanager.entity.Seat;
import com.example.seatmanager.service.DataAccessException;
import com.example.seatmanager.service.RecommendService;

import java.time.LocalDate;
import java.util.List;

/**
 * RecommendController：处理智能推荐座位的请求，供 UI 层调用
 */
public class RecommendController {

    private final RecommendService recommendService = new RecommendService();

    /**
     * 获取推荐座位列表
     *
     * @param dateStr     预约日期字符串，格式为 "yyyy-MM-dd"
     * @param roomId      自习室 ID
     * @param needWindow  是否要求靠窗
     * @param needDoor    是否要求靠门
     * @param needSocket  是否要求靠插座
     * @param topN        最多返回的座位数量
     * @return 按综合权重排序后的推荐座位列表（最多 topN 条）
     * @throws RuntimeException 如果底层出现数据访问异常
     */
    public List<Seat> handleGetRecommendations(String dateStr,
                                               int roomId,
                                               boolean needWindow,
                                               boolean needDoor,
                                               boolean needSocket,
                                               int topN) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return recommendService.getRecommendedSeats(date, roomId, needWindow, needDoor, needSocket, topN);
        } catch (DataAccessException dae) {
            throw dae;
        }
    }
}
