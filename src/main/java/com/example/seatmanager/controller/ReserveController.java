package com.example.seatmanager.controller;

import com.example.seatmanager.entity.UsageRecord;
import com.example.seatmanager.service.DataAccessException;
import com.example.seatmanager.service.ReserveService;

import java.time.LocalDate;

/**
 * ReserveController：处理手动预约相关的请求，供 UI 层调用
 */
public class ReserveController {

    private final ReserveService reserveService = new ReserveService();

    /**
     * 检查某个座位在指定日期是否可用（传入用于预约的时间位图 binaryData）
     *
     * @param seatId       座位 ID
     * @param dateStr      预约日期字符串，格式 "yyyy-MM-dd"
     * @param targetBitmap 长度 24 的 byte 数组，表示想要预约的时段位图（1 = 可用，0 = 占用）
     * @return 可用返回 true，否则 false
     * @throws RuntimeException 如果底层出现数据访问异常
     */
    public boolean handleCheckAvailability(long seatId, String dateStr, byte[] targetBitmap) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return reserveService.isSeatAvailable(seatId, date, targetBitmap);
        } catch (DataAccessException dae) {
            throw dae;
        }
    }

    /**
     * 创建预约：插入一条新的 UsageRecord，并更新自习室空座位数（前端需先设置好 UsageRecord 中的 seatId、studentId、recordDate、timeBitmap）
     *
     * @param record       UsageRecord 对象（不含 recordId，含 seatId、studentId、recordDate、timeBitmap）
     * @param roomId       自习室 ID（用于更新空座位数时调用）
     * @return 创建成功返回 true，否则 false
     * @throws RuntimeException 如果底层出现数据访问异常
     */
    public boolean handleCreateReservation(UsageRecord record, int roomId) {
        try {
            return reserveService.createReservation(record);
        } catch (DataAccessException dae) {
            throw dae;
        }
    }

    /**
     * 取消预约：删除一条 UsageRecord，并更新自习室空座位数
     *
     * @param recordId 记录 ID
     * @param roomId   自习室 ID
     * @return 取消成功返回 true，否则 false
     * @throws RuntimeException 如果底层出现数据访问异常
     */
    public boolean handleCancelReservation(long recordId, int roomId) {
        try {
            return reserveService.cancelReservation(recordId, roomId);
        } catch (DataAccessException dae) {
            throw dae;
        }
    }
}
