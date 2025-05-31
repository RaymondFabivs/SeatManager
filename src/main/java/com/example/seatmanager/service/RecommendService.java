package com.example.seatmanager.service;

import com.example.seatmanager.dao.SeatDAO;
import com.example.seatmanager.dao.UsageRecordDAO;
import com.example.seatmanager.entity.Seat;
import com.example.seatmanager.entity.UsageRecord;
import com.example.seatmanager.util.BitmapUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RecommendService：智能推荐座位业务逻辑层
 */
public class RecommendService {

    private final SeatDAO seatDAO = new SeatDAO();
    private final UsageRecordDAO usageRecordDAO = new UsageRecordDAO();

    /**
     * 获取推荐座位列表（按综合权重排序后返回前 topN）
     *
     * @param date         预约日期
     * @param roomId       自习室 ID
     * @param needWindow   是否要求靠窗
     * @param needDoor     是否要求靠门
     * @param needSocket   是否要求靠插座
     * @param topN         最多返回的座位数量
     * @return 按综合权重排序的座位列表（最多 topN 条）
     * @throws DataAccessException 如果底层数据库操作发生错误
     */
    public List<Seat> getRecommendedSeats(LocalDate date,
                                          int roomId,
                                          boolean needWindow,
                                          boolean needDoor,
                                          boolean needSocket,
                                          int topN) {
        try {
            // 1. 根据属性筛选候选座位
            List<Seat> candidates = seatDAO.findByAttributes(roomId, needWindow, needDoor, needSocket);

            // 2. 计算每个座位的“可用时段数量”与“附加分”
            Map<Seat, Integer> weightMap = new HashMap<>();
            for (Seat seat : candidates) {
                // 2.1 查询该座位当天所有预约记录
                List<UsageRecord> records = usageRecordDAO.findBySeatAndDate(seat.getSeatId(), date);

                // 2.2 合并位图，计算可用时段数量
                boolean[] allAvail = new boolean[192];
                Arrays.fill(allAvail, true); // 初始假设全天可用
                for (UsageRecord r : records) {
                    boolean[] avail = BitmapUtil.binaryToAvailability(r.getTimeBitmap());
                    for (int i = 0; i < 192; i++) {
                        if (!avail[i]) {
                            allAvail[i] = false; // 已被占用
                        }
                    }
                }
                int availableCount = 0;
                for (boolean b : allAvail) {
                    if (b) availableCount++;
                }

                // 2.3 计算附加分：靠窗 +2，靠门 +1，靠插座 +3（示例分值，可根据实际调整）
                int bonus = 0;
                if (seat.isNearWindow()) bonus += 2;
                if (seat.isNearDoor())   bonus += 1;
                if (seat.isNearSocket()) bonus += 3;

                int weight = availableCount + bonus;
                weightMap.put(seat, weight);
            }

            // 3. 按权重降序排序，取前 topN
            return weightMap.entrySet().stream()
                    .sorted(Map.Entry.<Seat, Integer>comparingByValue(Comparator.reverseOrder()))
                    .limit(topN)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

        } catch (SQLException e) {
            throw new DataAccessException("推荐座位时发生数据库错误", e);
        }
    }
}
