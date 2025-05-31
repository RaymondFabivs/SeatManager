package com.example.seatmanager.ui;

import com.example.seatmanager.controller.MapController;
import com.example.seatmanager.controller.ReserveController;
import com.example.seatmanager.entity.Seat;
import com.example.seatmanager.entity.UsageRecord;
import com.example.seatmanager.entity.Student;
import com.example.seatmanager.service.DataAccessException;
import com.example.seatmanager.util.BitmapUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * RoomMapPanel：自习室示意图面板，根据 MapController 返回的可用位图绘制每个座位状态
 */
public class RoomMapPanel extends JPanel {
    private final MainFrame mainFrame;
    private final JButton btnBack;
    private final JButton btnRefreshMap;

    private final MapController mapController = new MapController();
    private final ReserveController reserveController = new ReserveController();

    // 存储当前自习室可用位图：key = Seat，value = boolean[192]
    private Map<Seat, boolean[]> seatAvailabilityMap;

    public RoomMapPanel(MainFrame frame) {
        this.mainFrame = frame;
        setLayout(new BorderLayout(10, 10));

        // 顶部按钮区：返回 + 刷新示意图
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnBack = new JButton("返回");
        btnRefreshMap = new JButton("刷新示意图");
        topPanel.add(btnBack);
        topPanel.add(btnRefreshMap);
        add(topPanel, BorderLayout.NORTH);

        // 中央画布区
        DrawingCanvas canvas = new DrawingCanvas();
        JScrollPane scrollPane = new JScrollPane(canvas);
        add(scrollPane, BorderLayout.CENTER);

        // 事件监听
        btnBack.addActionListener(e -> mainFrame.showPanel(MainFrame.PANEL_RESERVE));
        btnRefreshMap.addActionListener(e -> refreshMap());
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            refreshMap();
        }
    }

    /** 从 MapController 获取最新可用位图并重绘 */
    private void refreshMap() {
        try {
            String dateStr = ApplicationContext.getInstance().getSelectedDate();
            int roomId = ApplicationContext.getInstance().getSelectedRoomId();
            seatAvailabilityMap = mapController.handleGetSeatAvailability(String.valueOf(roomId), dateStr);
            repaint();
        } catch (DataAccessException dae) {
            JOptionPane.showMessageDialog(this, "获取示意图失败，请稍后再试", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * DrawingCanvas：实际绘制座位的 JPanel
     */
    private class DrawingCanvas extends JPanel {
        private final int SEAT_SIZE = 40; // 每个座位方块大小（像素）
        private final int GAP       = 10; // 座位之间间隔

        // 用于点击检测：Rectangle  → Seat 对象
        private final java.util.Map<Rectangle, Seat> seatRectMap = new java.util.HashMap<>();

        public DrawingCanvas() {
            // 根据实际自习室大小调整此值
            setPreferredSize(new Dimension(1200, 800));
            setBackground(Color.WHITE);

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    handleClick(e.getX(), e.getY());
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (seatAvailabilityMap == null) return;

            seatRectMap.clear(); // 清除上次绘制时存的矩形映射

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x = GAP;
            int y = GAP;
            for (Seat seat : seatAvailabilityMap.keySet()) {
                boolean[] avail = seatAvailabilityMap.get(seat);
                // 判断整个位图：全 true = 全可用；全 false = 全占用；否则部分占用
                boolean allTrue = true, allFalse = true;
                for (boolean b : avail) {
                    if (b) {
                        allFalse = false;
                    } else {
                        allTrue = false;
                    }
                }
                if (allTrue) {
                    g2d.setColor(Color.GREEN);
                } else if (allFalse) {
                    g2d.setColor(Color.RED);
                } else {
                    g2d.setColor(Color.ORANGE);
                }
                g2d.fillRect(x, y, SEAT_SIZE, SEAT_SIZE);

                // 绘制座位 ID
                g2d.setColor(Color.BLACK);
                String text = String.valueOf(seat.getSeatId());
                FontMetrics fm = g2d.getFontMetrics();
                int tx = x + (SEAT_SIZE - fm.stringWidth(text)) / 2;
                int ty = y + ((SEAT_SIZE - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(text, tx, ty);

                // 存储当前座位对应的矩形区域
                Rectangle rect = new Rectangle(x, y, SEAT_SIZE, SEAT_SIZE);
                seatRectMap.put(rect, seat);

                // 准备绘制下一个座位：横向排列，若超出画布宽度则换行
                x += SEAT_SIZE + GAP;
                if (x + SEAT_SIZE + GAP > getWidth()) {
                    x = GAP;
                    y += SEAT_SIZE + GAP;
                }
            }
        }

        /**
         * 处理用户点击某个座位方块：弹出时段选择对话框，并尝试预约
         */
        private void handleClick(int mouseX, int mouseY) {
            for (Rectangle rect : seatRectMap.keySet()) {
                if (rect.contains(mouseX, mouseY)) {
                    Seat clickedSeat = seatRectMap.get(rect);
                    boolean[] avail = seatAvailabilityMap.get(clickedSeat);

                    // 从 avail（boolean[192]）直接生成可用时段列表
                    java.util.List<String> timeSlots = BitmapUtil.availabilityToTimeSlots(avail);
                    if (timeSlots.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "该座位已无可用时段", "提示", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    // 弹出对话框供用户选择一个时段
                    String selectedSlot = (String) JOptionPane.showInputDialog(
                            this,
                            "选择预约时段：",
                            "预约 座位 " + clickedSeat.getSeatId(),
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            timeSlots.toArray(),
                            timeSlots.get(0)
                    );
                    if (selectedSlot != null) {
                        // 找到 selectedSlot 对应的索引 idx
                        int targetIdx = -1;
                        for (int i = 0; i < 192; i++) {
                            int totalMin = i * 5;
                            int h = totalMin / 60;
                            int m = totalMin % 60;
                            String start = String.format("%02d:%02d", h, m);
                            int endTotal = totalMin + 5;
                            int eh = endTotal / 60;
                            int em = endTotal % 60;
                            String end = String.format("%02d:%02d", eh, em);
                            if ((start + "-" + end).equals(selectedSlot)) {
                                targetIdx = i;
                                break;
                            }
                        }
                        if (targetIdx < 0) return;

                        // 构建新的目标可用位图：复制原始 avail，再把 targetIdx 置为 false（表示要占用这段）
                        boolean[] targetAvail = avail.clone();
                        targetAvail[targetIdx] = false;

                        // 将 boolean[192] 转换为 byte[24]
                        byte[] targetBitmap = BitmapUtil.availabilityToBinary(targetAvail);

                        // 构造 UsageRecord 对象，并调用 ReserveController 创建预约
                        UsageRecord record = new UsageRecord();
                        Student current = ApplicationContext.getInstance().getCurrentStudent();
                        record.setStudentId(current.getStudentId());
                        record.setSeatId(clickedSeat.getSeatId());
                        record.setRecordDate(LocalDate.parse(ApplicationContext.getInstance().getSelectedDate()));
                        record.setSigned(false);
                        record.setTimeBitmap(targetBitmap);

                        try {
                            boolean success = reserveController.handleCreateReservation(record, clickedSeat.getRoomId());
                            if (success) {
                                JOptionPane.showMessageDialog(this, "预约成功");
                                refreshMap();
                            } else {
                                JOptionPane.showMessageDialog(this, "预约失败，存在时间冲突或数据库错误", "错误", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (DataAccessException dae) {
                            JOptionPane.showMessageDialog(this, "系统繁忙，请稍后再试", "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    break;
                }
            }
        }
    }
}
