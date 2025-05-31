package com.example.seatmanager.ui;

import com.example.seatmanager.util.DBUtil;

import javax.swing.*;
import java.awt.*;

/**
 * MainFrame：程序入口，使用 CardLayout 管理各子面板
 */
public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel cards;

    // 子面板的名称常量
    public static final String PANEL_LOGIN    = "login";
    public static final String PANEL_REGISTER = "register";
    public static final String PANEL_MAIN     = "main";
    public static final String PANEL_RECOMMEND= "recommend";
    public static final String PANEL_RESERVE  = "reserve";
    public static final String PANEL_MAP      = "map";
    public static final String PANEL_RECORD   = "record";

    public MainFrame() {
        setTitle("智能自习室座位推荐系统");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 初始化嵌入式数据库
        DBUtil.initDatabase();

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        // 创建子面板实例
        LoginPanel loginPanel = new LoginPanel(this);
        RegisterPanel registerPanel = new RegisterPanel(this);
        MainPanel mainPanel = new MainPanel(this);
        RecommendPanel recommendPanel = new RecommendPanel(this);
        ManualReservePanel reservePanel = new ManualReservePanel(this);
        RoomMapPanel mapPanel = new RoomMapPanel(this);
        RecordPanel recordPanel = new RecordPanel(this);

        // 将面板加入 cards
        cards.add(loginPanel, PANEL_LOGIN);
        cards.add(registerPanel, PANEL_REGISTER);
        cards.add(mainPanel, PANEL_MAIN);
        cards.add(recommendPanel, PANEL_RECOMMEND);
        cards.add(reservePanel, PANEL_RESERVE);
        cards.add(mapPanel, PANEL_MAP);
        cards.add(recordPanel, PANEL_RECORD);

        add(cards);
        // 默认显示登录面板
        showPanel(PANEL_LOGIN);
    }

    /**
     * 切换到指定名称的面板
     */
    public void showPanel(String panelName) {
        cardLayout.show(cards, panelName);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}
