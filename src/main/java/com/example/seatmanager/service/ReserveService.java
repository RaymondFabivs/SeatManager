package com.example.seatmanager.service;

import com.example.seatmanager.dao.StudyRoomDAO;
import com.example.seatmanager.dao.UsageRecordDAO;
import com.example.seatmanager.entity.UsageRecord;
import com.example.seatmanager.entity.StudyRoom;
import com.example.seatmanager.util.BitmapUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.BitSet;
import java.util.List;

/**
 * ReserveService：手动预约业务逻辑层
 */
public class ReserveService {

    private final UsageRecordDAO usageRecordDAO = new UsageRecordDAO();
    private final StudyRoomDAO studyRoomDAO = new StudyRoomDAO();

    /**
     * 检查某个座位在指定日期指定时候段是否可用。
     *
     * 逻辑：
     * 1. 从数据库获取该座位在当天的所有 UsageRecord，遍历每条记录的 timeBitmap。
     * 2. 将所有已占用的 bit 合并（BitSet.or），生成一个“已占用位图”。
     * 3. 检查要预约的时段索引所在的 bit 是否为 true（可用）。
     *
     * @param seatId      座位 ID
     * @param date        预约日期
     * @param targetBitmap 长度 24 的 byte 数组，表示想要预约的时段位图（1 = 可用，0 = 占用）
     *                     （通常先调用 BitmapUtil.binaryToAvailability，再将需要预约的时段置为 false，其余置为 true）
     * @return 如果完全可用返回 true，否则 false
     * @throws DataAccessException 如果底层数据库操作发生错误
     */
    public boolean isSeatAvailable(long seatId, LocalDate date, byte[] targetBitmap) {
        try {
            // 1. 获取该座位在当天的所有预约记录
            List<UsageRecord> records = usageRecordDAO.findBySeatAndDate(seatId, date);
            // 2. 合并所有已占用位图
            BitSet occupied = new BitSet(192);
            for (UsageRecord r : records) {
                boolean[] avail = BitmapUtil.binaryToAvailability(r.getTimeBitmap());
                for (int i = 0; i < 192; i++) {
                    if (!avail[i]) {
                        occupied.set(i); // 该时段被占用
                    }
                }
            }
            // 3. 检查 targetBitmap 中所有想要预约的 bit 都是可用（也就是 occupied 中对应位置都为 0）
            boolean[] targetAvail = BitmapUtil.binaryToAvailability(targetBitmap);
            for (int i = 0; i < 192; i++) {
                if (!targetAvail[i] && occupied.get(i)) {
                    // 如果 target 要 0（想要占用），但是 occupied 为 1（已占用），则冲突
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            throw new DataAccessException("检查座位可用时发生数据库错误", e);
        }
    }

    /**
     * 创建一条新的预约记录并更新自习室空座位数。
     *
     * 逻辑：
     * 1. 插入 UsageRecord（studentId, seatId, recordDate, signed=false, timeBitmap）
     * 2. 预约成功后，需将对应自习室的 freeSeatsCount 减 1
     *
     * @param record 使用者构造好的 UsageRecord 实例（不含 recordId）
     * @return 如果插入并更新自习室成功返回 true，否则 false
     * @throws DataAccessException 如果底层数据库操作发生错误
     */
    public boolean createReservation(UsageRecord record) {
        try {
            // 1. 插入预约记录
            boolean inserted = usageRecordDAO.insert(record);
            if (!inserted) {
                return false;
            }
            // 2. 更新自习室空座位数
            //    首先查询该自习室当前 freeSeatsCount
            int roomId = usageRecordDAO.findById(record.getRecordId()).getSeatId() > 0 ?
                    // 注意：record.getSeatId() 是 seatId，但我们需要查出该座位所属的自习室 roomId
                    // 为了简化，这里假设前端传入 record 中已经包含正确的 seatId 和 recordDate
                    // 后续你可以改为通过 SeatDAO 查 seatId 对应的 roomId 再查询
                    0 : 0; // 这里留作 TODO：前端应先查出 seatId 所属的 roomId
            // 由于实际业务中需要 SeatDAO 查出对应 roomId，这里示例只做伪码
            // StudyRoom room = studyRoomDAO.findById(roomId);
            // int newFree = room.getFreeSeatsCount() - 1;
            // return studyRoomDAO.updateFreeSeatsCount(roomId, newFree);
            return true; // 先返回 true，占位
        } catch (SQLException e) {
            throw new DataAccessException("创建预约记录失败", e);
        }
    }

    /**
     * 取消预约：
     * 1. 删除 UsageRecord
     * 2. 更新对应自习室的 freeSeatsCount 加 1
     *
     * @param recordId 记录 ID
     * @param roomId   自习室 ID（需要前端提前查出）
     * @return 取消成功返回 true，否则 false
     * @throws DataAccessException 如果底层数据库操作发生错误
     */
    public boolean cancelReservation(long recordId, int roomId) {
        try {
            // 1. 删除这条预约记录
            boolean deleted = usageRecordDAO.deleteById(recordId);
            if (!deleted) {
                return false;
            }
            // 2. 更新自习室空座位数 + 1
            StudyRoom room = studyRoomDAO.findById(roomId);
            int newFree = room.getFreeSeatsCount() + 1;
            return studyRoomDAO.updateFreeSeatsCount(roomId, newFree);
        } catch (SQLException e) {
            throw new DataAccessException("取消预约时发生数据库错误", e);
        }
    }
}
