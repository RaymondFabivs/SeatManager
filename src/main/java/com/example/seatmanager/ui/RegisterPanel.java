package com.example.seatmanager.ui;

import com.example.seatmanager.controller.AuthController;
import com.example.seatmanager.service.DataAccessException;

import javax.swing.*;
import java.awt.*;

/**
 * RegisterPanel：用户注册界面
 */
public class RegisterPanel extends JPanel {
    private final MainFrame mainFrame;
    private final JTextField tfStudentId;
    private final JTextField tfName;
    private final JTextField tfPhotoPath;
    private final JTextField tfEmail;
    private final JButton btnRegister;
    private final JButton btnGotoLogin;

    private final AuthController authController = new AuthController();

    public RegisterPanel(MainFrame frame) {
        this.mainFrame = frame;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        JLabel lblTitle = new JLabel("注册");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblTitle, gbc);

        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;

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

        // 照片路径
        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("照片文件名："), gbc);
        tfPhotoPath = new JTextField(20);
        tfPhotoPath.setToolTipText("示例：photos/张三.jpg");
        gbc.gridx = 1;
        add(tfPhotoPath, gbc);

        // 邮箱
        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("邮箱："), gbc);
        tfEmail = new JTextField(20);
        gbc.gridx = 1;
        add(tfEmail, gbc);

        // 注册按钮
        btnRegister = new JButton("注册");
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        add(btnRegister, gbc);

        // 返回登录
        btnGotoLogin = new JButton("已有账号？登录");
        gbc.gridy = 6;
        add(btnGotoLogin, gbc);

        // 事件监听
        btnRegister.addActionListener(e -> attemptRegister());
        btnGotoLogin.addActionListener(e -> mainFrame.showPanel(MainFrame.PANEL_LOGIN));
    }

    private void attemptRegister() {
        String studentId = tfStudentId.getText().trim();
        String name = tfName.getText().trim();
        String photoPath = tfPhotoPath.getText().trim();
        String email = tfEmail.getText().trim();

        if (studentId.isEmpty() || name.isEmpty() || photoPath.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "所有字段均不能为空", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            boolean success = authController.handleRegister(studentId, name, photoPath, email);
            if (success) {
                JOptionPane.showMessageDialog(this, "注册成功，请登录");
                mainFrame.showPanel(MainFrame.PANEL_LOGIN);
            } else {
                JOptionPane.showMessageDialog(this, "学号已存在，请更换", "注册失败", JOptionPane.ERROR_MESSAGE);
            }
        } catch (DataAccessException dae) {
            JOptionPane.showMessageDialog(this, "系统繁忙，请稍后再试", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
