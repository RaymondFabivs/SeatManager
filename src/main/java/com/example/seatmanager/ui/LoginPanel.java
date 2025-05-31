package com.example.seatmanager.ui;

import com.example.seatmanager.controller.AuthController;
import com.example.seatmanager.entity.Student;
import com.example.seatmanager.service.DataAccessException;

import javax.swing.*;
import java.awt.*;

/**
 * LoginPanel：用户登录界面
 */
public class LoginPanel extends JPanel {
    private final MainFrame mainFrame;
    private final JTextField tfStudentId;
    private final JTextField tfName;
    private final JButton btnLogin;
    private final JButton btnGotoRegister;

    private final AuthController authController = new AuthController();

    public LoginPanel(MainFrame frame) {
        this.mainFrame = frame;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel lblTitle = new JLabel("登录");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(lblTitle, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;

        // 学号
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("学号："), gbc);
        tfStudentId = new JTextField(20);
        gbc.gridx = 1;
        add(tfStudentId, gbc);

        // 姓名
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("姓名："), gbc);
        tfName = new JTextField(20);
        gbc.gridx = 1;
        add(tfName, gbc);

        // 登录按钮
        btnLogin = new JButton("登录");
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(btnLogin, gbc);

        // 跳转到注册
        btnGotoRegister = new JButton("没有账号？注册");
        gbc.gridx = 0; gbc.gridy = 4;
        add(btnGotoRegister, gbc);

        // 事件监听
        btnLogin.addActionListener(e -> attemptLogin());
        btnGotoRegister.addActionListener(e -> mainFrame.showPanel(MainFrame.PANEL_REGISTER));
    }

    private void attemptLogin() {
        String studentId = tfStudentId.getText().trim();
        String name = tfName.getText().trim();
        if (studentId.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "学号与姓名不能为空", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Student s = authController.handleLogin(studentId, name);
            if (s == null) {
                JOptionPane.showMessageDialog(this, "登录失败：请检查学号/姓名，或您已超出违规次数", "登录失败", JOptionPane.ERROR_MESSAGE);
            } else {
                // 登录成功，保存登录学生信息到全局状态（可用单例或静态变量，此处简单存放在 MainFrame）
                ApplicationContext.getInstance().setCurrentStudent(s);
                JOptionPane.showMessageDialog(this, "登录成功，欢迎 " + s.getName());
                mainFrame.showPanel(MainFrame.PANEL_MAIN);
            }
        } catch (DataAccessException dae) {
            JOptionPane.showMessageDialog(this, "系统繁忙，请稍后再试", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
