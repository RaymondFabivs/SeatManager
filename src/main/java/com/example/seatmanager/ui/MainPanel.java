package com.example.seatmanager.ui;

import com.example.seatmanager.entity.Student;

import javax.swing.*;
import java.awt.*;

/**
 * MainPanel：登录成功后的主界面，包含导航按钮
 */
public class MainPanel extends JPanel {
    private final MainFrame mainFrame;
    private final JLabel lblWelcome;
    private final JButton btnRecommend;
    private final JButton btnReserve;
    private final JButton btnRecord;
    private final JButton btnLogout;

    public MainPanel(MainFrame frame) {
        this.mainFrame = frame;
        setLayout(new BorderLayout());

        // 顶部欢迎文字
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblWelcome = new JLabel("");
        lblWelcome.setFont(new Font("SansSerif", Font.BOLD, 18));
        topPanel.add(lblWelcome);
        add(topPanel, BorderLayout.NORTH);

        // 中央按钮区
        JPanel center = new JPanel(new GridLayout(2, 2, 20, 20));
        btnRecommend = new JButton("智能推荐预约");
        btnReserve   = new JButton("手动预约");
        btnRecord    = new JButton("查看预约记录");
        btnLogout    = new JButton("退出登录");

        center.add(btnRecommend);
        center.add(btnReserve);
        center.add(btnRecord);
        center.add(btnLogout);

        add(center, BorderLayout.CENTER);

        // 事件监听
        btnRecommend.addActionListener(e -> mainFrame.showPanel(MainFrame.PANEL_RECOMMEND));
        btnReserve.addActionListener(e -> mainFrame.showPanel(MainFrame.PANEL_RESERVE));
        btnRecord.addActionListener(e -> mainFrame.showPanel(MainFrame.PANEL_RECORD));
        btnLogout.addActionListener(e -> {
            ApplicationContext.getInstance().setCurrentStudent(null);
            mainFrame.showPanel(MainFrame.PANEL_LOGIN);
        });
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            // 每次显示时更新欢迎文字
            Student s = ApplicationContext.getInstance().getCurrentStudent();
            if (s != null) {
                lblWelcome.setText("欢迎, " + s.getName() + " (学号: " + s.getStudentId() + ")");
            }
        }
    }
}
