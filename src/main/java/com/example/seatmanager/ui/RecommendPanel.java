package com.example.seatmanager.ui;

import com.example.seatmanager.controller.RecommendController;
import com.example.seatmanager.entity.Seat;
import com.example.seatmanager.service.DataAccessException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * RecommendPanel：智能推荐预约界面
 */
public class RecommendPanel extends JPanel {
    private final MainFrame mainFrame;

    private final JTextField tfDate;
    private final JComboBox<Integer> cbBuilding;
    private final JComboBox<Integer> cbRoom;
    private final JCheckBox chkWindow;
    private final JCheckBox chkDoor;
    private final JCheckBox chkSocket;
    private final JComboBox<Integer> cbTopN;
    private final JButton btnGetRecommendations;
    private final JButton btnBack;

    private final JTable tblResults;
    private final DefaultTableModel tblModel;

    private final RecommendController recommendController = new RecommendController();

    public RecommendPanel(MainFrame frame) {
        this.mainFrame = frame;
        setLayout(new BorderLayout(10, 10));

        // 顶部输入区
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        // 预约日期
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(new JLabel("日期(yyyy-MM-dd)："), gbc);
        tfDate = new JTextField(LocalDate.now().toString(), 10);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(tfDate, gbc);

        // 教学楼
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(new JLabel("教学楼(1-6)："), gbc);
        cbBuilding = new JComboBox<>();
        for (int i = 1; i <= 6; i++) cbBuilding.addItem(i);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(cbBuilding, gbc);

        // 自习室
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(new JLabel("自习室(ID)："), gbc);
        cbRoom = new JComboBox<>();
        // 默认值，可在用户选择教学楼后动态加载
        updateRooms();
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(cbRoom, gbc);

        // 靠窗/靠门/靠插座
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(new JLabel("附加要求："), gbc);
        JPanel chkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        chkWindow = new JCheckBox("靠窗");
        chkDoor   = new JCheckBox("靠门");
        chkSocket = new JCheckBox("靠插座");
        chkPanel.add(chkWindow);
        chkPanel.add(chkDoor);
        chkPanel.add(chkSocket);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(chkPanel, gbc);

        // Top N
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(new JLabel("最多返回："), gbc);
        cbTopN = new JComboBox<>();
        for (int i = 1; i <= 10; i++) cbTopN.addItem(i);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(cbTopN, gbc);

        // 获取推荐按钮
        btnGetRecommendations = new JButton("获取推荐");
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(btnGetRecommendations, gbc);

        // 返回按钮
        btnBack = new JButton("返回");
        gbc.gridy = 6;
        inputPanel.add(btnBack, gbc);

        add(inputPanel, BorderLayout.NORTH);

        // 结果表格
        String[] columns = {"座位ID", "靠窗", "靠门", "靠插座", "可用时段数", "综合权重"};
        tblModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblResults = new JTable(tblModel);
        JScrollPane scrollPane = new JScrollPane(tblResults);
        add(scrollPane, BorderLayout.CENTER);

        // 事件监听
        cbBuilding.addActionListener(e -> updateRooms());
        btnGetRecommendations.addActionListener(e -> fetchRecommendations());
        btnBack.addActionListener(e -> mainFrame.showPanel(MainFrame.PANEL_MAIN));
    }

    /** 根据选定的教学楼，动态加载该楼对应的自习室 ID（示例仅简单填 1-10） */
    private void updateRooms() {
        int buildingId = (Integer) cbBuilding.getSelectedItem();
        cbRoom.removeAllItems();
        // TODO: 实际业务中应调用 StudyRoomDAO 根据 buildingId 查询自习室列表
        // 此处简单示例：假设每栋楼有 10 个自习室，ID = buildingId * 10 + i
        for (int i = 1; i <= 10; i++) {
            cbRoom.addItem(buildingId * 10 + i);
        }
    }

    private void fetchRecommendations() {
        String dateStr = tfDate.getText().trim();
        int roomId = (Integer) cbRoom.getSelectedItem();
        boolean needWindow = chkWindow.isSelected();
        boolean needDoor   = chkDoor.isSelected();
        boolean needSocket = chkSocket.isSelected();
        int topN = (Integer) cbTopN.getSelectedItem();

        // 清空表格
        tblModel.setRowCount(0);

        try {
            // 调用控制器获取推荐列表
            java.util.List<Seat> results = recommendController.handleGetRecommendations(
                    dateStr, roomId, needWindow, needDoor, needSocket, topN);

            // 遍历结果并加入表格。此处无法直接获取“可用时段数”、“综合权重”，需要在 RecommendService 返回额外信息。
            // 为简化展示，只显示座位基本属性，示例将“可用时段数”和“综合权重”省略。
            for (Seat s : results) {
                Object[] row = {
                        s.getSeatId(),
                        s.isNearWindow(),
                        s.isNearDoor(),
                        s.isNearSocket(),
                        "—", // 占位：可用时段数
                        "—"  // 占位：综合权重
                };
                tblModel.addRow(row);
            }
            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "无满足条件的推荐座位", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (DataAccessException dae) {
            JOptionPane.showMessageDialog(this, "系统繁忙，请稍后再试", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
