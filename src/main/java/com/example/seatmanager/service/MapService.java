package com.example.seatmanager.service;

import com.example.seatmanager.dao.EventDAO;
import com.example.seatmanager.dao.UsageRecordDAO;
import com.example.seatmanager.dao.SeatDAO;
import com.example.seatmanager.entity.Event;
import com.example.seatmanager.entity.Seat;
import com.example.seatmanager.entity.UsageRecord;
import com.example.seatmanager.util.BitmapUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MapService：生成自习室示意图数据的业务逻辑层
 */
public class MapService {

    private final EventDAO eventDAO = new EventDAO();
    private final UsageRecordDAO usageRecordDAO = new UsageRecordDAO();
    private final SeatDAO seatDAO = new SeatDAO();

    /**
     * 获取某个自习室在指定日期下，每个座位的可用状态位图（合并事件和预约）
     *
     * @param roomId 自习室 ID
     * @param date   指定日期
     * @return Map：key = Seat 实例，value = boolean[192] 位图（true = 可用，false = 不可用）
     * @throws DataAccessException 如果底层数据库操作出错
     */
    public Map<Seat, boolean[]> getSeatAvailabilityMap(int roomId, LocalDate date) {
        try {
            // 1. 查询该自习室是否有整日事件（即 event.timeBitmap 全 0 或指定时段 0 表示整日不可用）
            List<Event> events = eventDAO.findByRoomAndDate(roomId, date);
            boolean[] roomEventMask = new boolean[192];
            // 默认初始化为全可用
            for (int i = 0; i < 192; i++) {
                roomEventMask[i] = true;
            }
            for (Event e : events) {
                boolean[] eventAvail = BitmapUtil.binaryToAvailability(e.getTimeBitmap());
                for (int i = 0; i < 192; i++) {
                    if (!eventAvail[i]) {
                        roomEventMask[i] = false; // 该时段被事件占用
                    }
                }
            }

            // 2. 获取该自习室下所有座位
            List<Seat> seats = seatDAO.findByRoomId(roomId);

            // 3. 对每个座位：
            //    3.1 初始化一个“可用位图”为全 true
            //    3.2 将 roomEventMask 中的 false 合并进来（事件占用） → 得到 seatMask
            //    3.3 查询该座位当天所有 UsageRecord，将预约占用位段合并（置 false）
            //    3.4 最终得到该座位的完整可用位图
            Map<Seat, boolean[]> resultMap = new HashMap<>();
            for (Seat seat : seats) {
                // 3.1 默认全可用
                boolean[] availability = new boolean[192];
                for (int i = 0; i < 192; i++) {
                    availability[i] = true;
                }
                // 3.2 合并事件
                for (int i = 0; i < 192; i++) {
                    if (!roomEventMask[i]) {
                        availability[i] = false;
                    }
                }
                // 3.3 合并预约占用
                List<UsageRecord> records = usageRecordDAO.findBySeatAndDate(seat.getSeatId(), date);
                for (UsageRecord r : records) {
                    boolean[] recordAvail = BitmapUtil.binaryToAvailability(r.getTimeBitmap());
                    for (int i = 0; i < 192; i++) {
                        if (!recordAvail[i]) {
                            availability[i] = false;
                        }
                    }
                }
                // 3.4 将座位与其可用位图放入结果
                resultMap.put(seat, availability);
            }

            return resultMap;
        } catch (SQLException e) {
            throw new DataAccessException("生成自习室示意图数据时发生数据库错误", e);
        }
    }
}
