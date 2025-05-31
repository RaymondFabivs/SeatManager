package com.example.seatmanager.service;

import com.example.seatmanager.dao.UsageRecordDAO;
import com.example.seatmanager.entity.UsageRecord;

import java.sql.SQLException;
import java.util.List;

/**
 * RecordService：有关预约记录的业务逻辑层
 */
public class RecordService {

    private final UsageRecordDAO usageRecordDAO = new UsageRecordDAO();

    /**
     * 获取某学生所有预约记录，按 record_date DESC、record_id DESC 排序
     *
     * @param studentId 学号
     * @return 该学生的预约记录列表，空列表表示无记录
     * @throws DataAccessException 如果底层数据库操作发生错误
     */
    public List<UsageRecord> getRecordsByStudent(String studentId) {
        try {
            return usageRecordDAO.findByStudent(studentId);
        } catch (SQLException e) {
            throw new DataAccessException("查询学生预约记录失败", e);
        }
    }

    /**
     * 取消一条预约记录
     *
     * @param recordId 记录 ID
     * @return 如果删除成功返回 true，否则 false
     * @throws DataAccessException 如果底层数据库操作发生错误
     */
    public boolean cancelRecord(long recordId) {
        try {
            return usageRecordDAO.deleteById(recordId);
        } catch (SQLException e) {
            throw new DataAccessException("取消预约记录失败", e);
        }
    }

    /**
     * 签到：将使用记录的 signed 字段更新为 true
     *
     * @param recordId 记录 ID
     * @return 如果更新成功返回 true，否则 false
     * @throws DataAccessException 如果底层数据库操作发生错误
     */
    public boolean signInRecord(long recordId) {
        try {
            return usageRecordDAO.updateSigned(recordId, true);
        } catch (SQLException e) {
            throw new DataAccessException("签到操作失败", e);
        }
    }
}
