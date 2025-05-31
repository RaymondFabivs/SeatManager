package com.example.seatmanager.ui;

import com.example.seatmanager.controller.ReserveController;
import com.example.seatmanager.entity.Student;
import com.example.seatmanager.service.DataAccessException;
import com.example.seatmanager.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * ManualReservePanel：手动预约界面，包括级联下拉框：日期 → 教学楼 → 楼层 → 自习室
 */
public class ManualReservePanel extends JPanel {
    private final MainFrame mainFrame;

    private final JComboBox<String> cbDate;
    private final JComboBox<Integer> cbBuilding;
    private final JComboBox<Integer> cbFloor;
    private final JComboBox<Integer> cbRoom;
    private final JButton btnViewMap;
    private final JButton btnBack;

    private final ReserveController reserveController = new ReserveController();

    public ManualReservePanel(MainFrame frame) {
        this.mainFrame = frame;
        setLayout(new BorderLayout(10, 10));

        // 上方级联筛选区
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        // 日期
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        topPanel.add(new JLabel("日期："), gbc);
        cbDate = new JComboBox<>();
        List<String> dates = DateUtil.getNextNDates(7);
        for (String d : dates) cbDate.addItem(d);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(cbDate, gbc);

        // 教学楼
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        topPanel.add(new JLabel("教学楼："), gbc);
        cbBuilding = new JComboBox<>();
        for (int i = 1; i <= 6; i++) cbBuilding.addItem(i);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(cbBuilding, gbc);

        // 楼层 (1-6)
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        topPanel.add(new JLabel("楼层："), gbc);
        cbFloor = new JComboBox<>();
        for (int i = 1; i <= 6; i++) cbFloor.addItem(i);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(cbFloor, gbc);

        // 自习室
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        topPanel.add(new JLabel("自习室："), gbc);
        cbRoom = new JComboBox<>();
        updateRooms();
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(cbRoom, gbc);

        // 查看示意图按钮
        btnViewMap = new JButton("查看示意图");
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        topPanel.add(btnViewMap, gbc);

        // 返回按钮
        btnBack = new JButton("返回");
        gbc.gridy = 5;
        topPanel.add(btnBack, gbc);

        add(topPanel, BorderLayout.NORTH);

        // 事件监听
        cbBuilding.addActionListener(e -> updateRooms());
        cbFloor.addActionListener(e -> updateRooms());
        btnViewMap.addActionListener(e -> viewMap());
        btnBack.addActionListener(e -> mainFrame.showPanel(MainFrame.PANEL_MAIN));
    }

    /** 根据所选教学楼与楼层，动态加载自习室 ID（示例仅简单填充） */
    private void updateRooms() {
        int buildingId = (Integer) cbBuilding.getSelectedItem();
        int floor = (Integer) cbFloor.getSelectedItem();
        cbRoom.removeAllItems();
        // TODO: 实际业务应调用 StudyRoomDAO，根据 buildingId 和 floor 查询自习室 ID
        // 示例：每个楼层 10 个自习室，ID = buildingId*100 + floor*10 + i
        for (int i = 1; i <= 10; i++) {
            cbRoom.addItem(buildingId * 100 + floor * 10 + i);
        }
    }

    /** 点击“查看示意图”后，切换到 RoomMapPanel 并传递所选参数 */
    private void viewMap() {
        String dateStr = (String) cbDate.getSelectedItem();
        int roomId = (Integer) cbRoom.getSelectedItem();
        // 将参数保存在 ApplicationContext 以便 RoomMapPanel 获取
        ApplicationContext.getInstance().setSelectedDate(dateStr);
        ApplicationContext.getInstance().setSelectedRoomId(roomId);
        mainFrame.showPanel(MainFrame.PANEL_MAP);
    }
}
