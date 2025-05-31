package com.example.seatmanager.controller;

import com.example.seatmanager.entity.UsageRecord;
import com.example.seatmanager.service.DataAccessException;
import com.example.seatmanager.service.RecordService;

import java.util.List;

/**
 * RecordController：处理预约记录相关的请求，供 UI 层调用
 */
public class RecordController {

    private final RecordService recordService = new RecordService();

    /**
     * 获取某学生所有预约记录（按日期倒序）
     *
     * @param studentId 学号
     * @return UsageRecord 列表，空列表表示无记录
     * @throws RuntimeException 如果底层出现数据访问异常
     */
    public List<UsageRecord> handleGetRecordsByStudent(String studentId) {
        try {
            return recordService.getRecordsByStudent(studentId);
        } catch (DataAccessException dae) {
            throw dae;
        }
    }

    /**
     * 取消一条预约记录
     *
     * @param recordId 记录 ID
     * @return 取消成功返回 true，否则 false
     * @throws RuntimeException 如果底层出现数据访问异常
     */
    public boolean handleCancelRecord(long recordId) {
        try {
            return recordService.cancelRecord(recordId);
        } catch (DataAccessException dae) {
            throw dae;
        }
    }

    /**
     * 签到某条预约记录
     *
     * @param recordId 记录 ID
     * @return 签到成功返回 true，否则 false
     * @throws RuntimeException 如果底层出现数据访问异常
     */
    public boolean handleSignInRecord(long recordId) {
        try {
            return recordService.signInRecord(recordId);
        } catch (DataAccessException dae) {
            throw dae;
        }
    }
}
