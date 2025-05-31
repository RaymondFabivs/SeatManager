package com.example.seatmanager.controller;

import com.example.seatmanager.entity.Student;
import com.example.seatmanager.service.AuthService;
import com.example.seatmanager.service.DataAccessException;

/**
 * AuthController：处理与用户认证相关的请求，供 UI 层调用
 */
public class AuthController {

    private final AuthService authService = new AuthService();

    /**
     * 尝试注册新用户
     *
     * @param studentId  学号
     * @param name       姓名
     * @param photoPath  照片相对路径
     * @param email      邮箱地址
     * @return 注册成功返回 true；如果学号已存在或发生其他可预见的失败返回 false
     * @throws RuntimeException 如果底层出现数据访问异常
     */
    public boolean handleRegister(String studentId, String name, String photoPath, String email) {
        try {
            return authService.register(studentId, name, photoPath, email);
        } catch (DataAccessException dae) {
            // 将 DataAccessException 直接抛给 UI 层，由 UI 层统一展示“系统繁忙”等提示
            throw dae;
        }
    }

    /**
     * 尝试登录
     *
     * @param studentId  学号
     * @param name       姓名
     * @return 登录成功返回对应的 Student 对象；登录失败（学号/姓名不匹配或违规次数超限）返回 null
     * @throws RuntimeException 如果底层出现数据访问异常
     */
    public Student handleLogin(String studentId, String name) {
        try {
            return authService.login(studentId, name);
        } catch (DataAccessException dae) {
            throw dae;
        }
    }

    /**
     * 更新用户违规次数（可由其他模块调用，如“签到超时”场景下）
     *
     * @param studentId  学号
     * @param newCount   新的违规次数
     * @return 更新成功返回 true，否则 false
     * @throws RuntimeException 如果底层出现数据访问异常
     */
    public boolean handleUpdateViolation(String studentId, int newCount) {
        try {
            return authService.updateViolationCount(studentId, newCount);
        } catch (DataAccessException dae) {
            throw dae;
        }
    }
}
