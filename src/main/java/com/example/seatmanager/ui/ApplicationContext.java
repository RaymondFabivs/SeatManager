package com.example.seatmanager.ui;

import com.example.seatmanager.entity.Student;

/**
 * ApplicationContext：保存全局共享状态，例如当前登录学生、选定的自习室日期与房间 ID 等
 */
public class ApplicationContext {
    // 单例实例
    private static final ApplicationContext instance = new ApplicationContext();

    // 当前已登录的学生信息
    private Student currentStudent;

    // 在“手动预约 → 查看示意图”流程中，需要记录用户选定的日期和自习室 ID
    private String selectedDate;
    private int selectedRoomId;

    // 私有构造，防止外部 new
    private ApplicationContext() { }

    public static ApplicationContext getInstance() {
        return instance;
    }

    public Student getCurrentStudent() {
        return currentStudent;
    }
    public void setCurrentStudent(Student currentStudent) {
        this.currentStudent = currentStudent;
    }

    public String getSelectedDate() {
        return selectedDate;
    }
    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }

    public int getSelectedRoomId() {
        return selectedRoomId;
    }
    public void setSelectedRoomId(int selectedRoomId) {
        this.selectedRoomId = selectedRoomId;
    }
}
