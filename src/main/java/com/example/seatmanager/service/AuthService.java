package com.example.seatmanager.service;

import com.example.seatmanager.dao.StudentDAO;
import com.example.seatmanager.entity.Student;

import java.sql.SQLException;

/**
 * AuthService：用户登录、注册的业务逻辑层
 */
public class AuthService {

    private final StudentDAO studentDAO = new StudentDAO();

    /**
     * 注册新学生：
     * 1. 检查 studentId 是否已存在
     * 2. 如果不存在，则插入一条新纪录（默认 violationCount = 0）
     *
     * @param studentId  学号
     * @param name       姓名
     * @param photoPath  照片相对路径（resources/photos 下的文件名）
     * @param email      邮箱地址
     * @return 注册成功返回 true；如果学号已存在或插入失败返回 false
     * @throws DataAccessException 如果底层数据库操作发生错误
     */
    public boolean register(String studentId, String name, String photoPath, String email) {
        try {
            // 1. 学号已存在则返回 false
            if (studentDAO.findById(studentId) != null) {
                return false;
            }
            // 2. 插入新学生
            Student s = new Student();
            s.setStudentId(studentId);
            s.setName(name);
            s.setPhotoPath(photoPath);
            s.setViolationCount(0);  // 默认违规次数为 0
            s.setEmail(email);
            // createdAt 在数据库中由 DEFAULT CURRENT_TIMESTAMP 自动填充
            return studentDAO.insert(s);
        } catch (SQLException e) {
            throw new DataAccessException("注册时插入学生信息失败", e);
        }
    }

    /**
     * 用户登录：
     * 1. 按 studentId 查询 Student
     * 2. 校验姓名是否匹配
     * 3. 检查 violationCount 是否小于 2
     *
     * @param studentId  学号
     * @param name       姓名
     * @return 如果登录成功，返回对应的 Student 实例；否则返回 null
     * @throws DataAccessException 如果底层数据库操作发生错误
     */
    public Student login(String studentId, String name) {
        try {
            Student s = studentDAO.findById(studentId);
            if (s == null) {
                return null; // 学生不存在
            }
            if (!s.getName().equals(name)) {
                return null; // 姓名不匹配
            }
            if (s.getViolationCount() >= 2) {
                return null; // 违规次数超限
            }
            return s;
        } catch (SQLException e) {
            throw new DataAccessException("登录时查询学生信息失败", e);
        }
    }

    /**
     * 更新学生的违规次数（由其它业务逻辑决定何时调用，比如签到失败或超时未签到时）
     *
     * @param studentId    学号
     * @param newCount     新的违规次数
     * @return 更新成功返回 true，否则 false
     * @throws DataAccessException 如果底层数据库操作发生错误
     */
    public boolean updateViolationCount(String studentId, int newCount) {
        try {
            return studentDAO.updateViolationCount(studentId, newCount);
        } catch (SQLException e) {
            throw new DataAccessException("更新学生违规次数失败", e);
        }
    }
}
