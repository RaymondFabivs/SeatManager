package com.example.seatmanager.ui;

import com.example.seatmanager.controller.RecordController;
import com.example.seatmanager.entity.UsageRecord;
import com.example.seatmanager.entity.Student;
import com.example.seatmanager.service.DataAccessException;
import com.example.seatmanager.util.BitmapUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * RecordPanel：展示当前登录学生的所有预约记录，并提供“取消”与“签到”操作
 */
public class RecordPanel extends JPanel {
    private final MainFrame mainFrame;
    private final JTable tblRecords;
    private final DefaultTableModel tblModel;
    private final JButton btnCancel;
    private final JButton btnSignIn;
    private final JButton btnBack;

    private final RecordController recordController = new RecordController();

    public RecordPanel(MainFrame frame) {
        this.mainFrame = frame;
        setLayout(new BorderLayout(10, 10));

        // 顶部按钮区域
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnBack = new JButton("返回");
        btnCancel = new JButton("取消预约");
        btnSignIn = new JButton("签到");
        topPanel.add(btnBack);
        topPanel.add(btnCancel);
        topPanel.add(btnSignIn);
        add(topPanel, BorderLayout.NORTH);

        // 表格：展示预约记录
        String[] columns = {"记录ID", "座位ID", "日期", "已签到", "示例时段(第一个可用)"};
        tblModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblRecords = new JTable(tblModel);
        tblRecords.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(tblRecords);
        add(scrollPane, BorderLayout.CENTER);

        // 事件监听
        btnBack.addActionListener(e -> mainFrame.showPanel(MainFrame.PANEL_MAIN));
        btnCancel.addActionListener(e -> cancelSelectedRecord());
        btnSignIn.addActionListener(e -> signInSelectedRecord());
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            loadRecords();
        }
    }

    /** 加载当前登录学生的预约记录到表格 */
    private void loadRecords() {
        tblModel.setRowCount(0);
        Student current = ApplicationContext.getInstance().getCurrentStudent();
        if (current == null) return;
        try {
            List<UsageRecord> list = recordController.handleGetRecordsByStudent(current.getStudentId());
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (UsageRecord r : list) {
                // 只拿出 timeBitmap 中的第一个可用/占用时段示例
                boolean[] avail = BitmapUtil.binaryToAvailability(r.getTimeBitmap());
                String timeSlot = BitmapUtil.availabilityToTimeSlots(avail).stream().findFirst().orElse("—");
                Object[] row = {
                        r.getRecordId(),
                        r.getSeatId(),
                        r.getRecordDate().format(df),
                        r.isSigned() ? "是" : "否",
                        timeSlot
                };
                tblModel.addRow(row);
            }
        } catch (DataAccessException dae) {
            JOptionPane.showMessageDialog(this, "加载预约记录失败，请稍后再试", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** 取消在表格中选中的预约记录 */
    private void cancelSelectedRecord() {
        int selectedRow = tblRecords.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要取消的记录", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        long recordId = (Long) tblModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "确定要取消此预约吗？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean success = recordController.handleCancelRecord(recordId);
            if (success) {
                JOptionPane.showMessageDialog(this, "已成功取消预约");
                loadRecords();
            } else {
                JOptionPane.showMessageDialog(this, "取消失败，请稍后重试", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (DataAccessException dae) {
            JOptionPane.showMessageDialog(this, "系统繁忙，请稍后再试", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** 对表格中选中的预约记录进行“签到” */
    private void signInSelectedRecord() {
        int selectedRow = tblRecords.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要签到的记录", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Long recordId = (Long) tblModel.getValueAt(selectedRow, 0);
        String signedStr = (String) tblModel.getValueAt(selectedRow, 3);
        if ("是".equals(signedStr)) {
            JOptionPane.showMessageDialog(this, "该记录已签到，无需重复操作", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            boolean success = recordController.handleSignInRecord(recordId);
            if (success) {
                JOptionPane.showMessageDialog(this, "签到成功");
                loadRecords();
            } else {
                JOptionPane.showMessageDialog(this, "签到失败，请稍后重试", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (DataAccessException dae) {
            JOptionPane.showMessageDialog(this, "系统繁忙，请稍后再试", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
