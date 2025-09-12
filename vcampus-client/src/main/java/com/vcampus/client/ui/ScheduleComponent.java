package com.vcampus.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单的时间轴预约组件
 * 只替换中间内容区域，不影响整体布局
 */
public class ScheduleComponent {

    private String spaceName;
    private LocalDate currentDate;
    private Runnable onBackCallback;

    // 预约状态
    public enum TimeSlotStatus {
        AVAILABLE("可预约", "#ffffff", "#000000"),
        TEACHING("教学占用", "#ffe6e6", "#c0392b"),
        STUDENT_RESERVED("学生借用", "#e6f7ff", "#138496"),
        MAINTENANCE("暂不开放", "#f0f0f0", "#6c757d");

        private final String displayName;
        private final String bgColor;
        private final String textColor;

        TimeSlotStatus(String displayName, String bgColor, String textColor) {
            this.displayName = displayName;
            this.bgColor = bgColor;
            this.textColor = textColor;
        }

        public String getDisplayName() { return displayName; }
        public String getBgColor() { return bgColor; }
        public String getTextColor() { return textColor; }
    }

    // 模拟时间段数据
    private Map<String, TimeSlotInfo> timeSlotData;

    public ScheduleComponent(String spaceName, Runnable onBack) {
        this.spaceName = spaceName;
        this.currentDate = LocalDate.now();
        this.onBackCallback = onBack;
        initializeMockData();
    }

    /**
     * 时间段信息类
     */
    private static class TimeSlotInfo {
        TimeSlotStatus status;
        String description;

        TimeSlotInfo(TimeSlotStatus status, String description) {
            this.status = status;
            this.description = description;
        }
    }

    /**
     * 初始化模拟数据
     */
    private void initializeMockData() {
        timeSlotData = new HashMap<>();

        // 模拟一天的时间段数据
        timeSlotData.put("06:00", new TimeSlotInfo(TimeSlotStatus.AVAILABLE, ""));
        timeSlotData.put("07:00", new TimeSlotInfo(TimeSlotStatus.AVAILABLE, ""));
        timeSlotData.put("08:00", new TimeSlotInfo(TimeSlotStatus.TEACHING, "高等数学课程"));
        timeSlotData.put("09:00", new TimeSlotInfo(TimeSlotStatus.TEACHING, "线性代数课程"));
        timeSlotData.put("10:00", new TimeSlotInfo(TimeSlotStatus.STUDENT_RESERVED, "预约人：王同学(小组讨论)"));
        timeSlotData.put("11:00", new TimeSlotInfo(TimeSlotStatus.STUDENT_RESERVED, "预约人：李同学(项目汇报)"));
        timeSlotData.put("12:00", new TimeSlotInfo(TimeSlotStatus.AVAILABLE, ""));
        timeSlotData.put("13:00", new TimeSlotInfo(TimeSlotStatus.AVAILABLE, ""));
        timeSlotData.put("14:00", new TimeSlotInfo(TimeSlotStatus.TEACHING, "计算机基础"));
        timeSlotData.put("15:00", new TimeSlotInfo(TimeSlotStatus.AVAILABLE, ""));
        timeSlotData.put("16:00", new TimeSlotInfo(TimeSlotStatus.AVAILABLE, ""));
        timeSlotData.put("17:00", new TimeSlotInfo(TimeSlotStatus.AVAILABLE, ""));
        timeSlotData.put("18:00", new TimeSlotInfo(TimeSlotStatus.MAINTENANCE, "设备维护中"));
        timeSlotData.put("19:00", new TimeSlotInfo(TimeSlotStatus.MAINTENANCE, "清洁时间"));
        timeSlotData.put("20:00", new TimeSlotInfo(TimeSlotStatus.AVAILABLE, ""));
        timeSlotData.put("21:00", new TimeSlotInfo(TimeSlotStatus.AVAILABLE, ""));
        timeSlotData.put("22:00", new TimeSlotInfo(TimeSlotStatus.MAINTENANCE, "闭馆时间"));
    }

    /**
     * 创建时间轴界面
     */
    public HBox createScheduleInterface() {
        HBox container = new HBox();
        container.setSpacing(0);
        container.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9);");


        // 右侧时间轴
        VBox rightPanel = createTimelinePanel();

        container.getChildren().addAll(rightPanel);

        return container;
    }


    /**
     * 创建右侧时间轴面板
     */
    private VBox createTimelinePanel() {
        VBox panel = new VBox();
        panel.setSpacing(0);

        // 顶部工具栏
        HBox toolbar = new HBox();
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(15, 20, 15, 20));
        toolbar.setSpacing(15);
        toolbar.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");

        Button backButton = new Button("← 返回空间预约主页");
        backButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 16; -fx-font-weight: bold;");
        backButton.setOnAction(e -> {
            if (onBackCallback != null) {
                onBackCallback.run();
            }
        });

        Label spaceLabel = new Label("当前场地：" + spaceName);
        spaceLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        spaceLabel.setTextFill(Color.valueOf("#2c3e50"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button quickBookBtn = new Button("立即预约");
        quickBookBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 16; -fx-font-weight: bold;");
        quickBookBtn.setOnAction(e -> showQuickBookDialog());

        toolbar.getChildren().addAll(backButton, spaceLabel, spacer, quickBookBtn);

        // 时间轴滚动区域
        ScrollPane timelineScroll = new ScrollPane();
        timelineScroll.setFitToWidth(true);
        timelineScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        timelineScroll.setPrefHeight(500);
        timelineScroll.setStyle("-fx-background-color: white;");

        VBox timeline = new VBox();
        timeline.setSpacing(0);

        // 生成时间段
        for (int hour = 6; hour <= 22; hour++) {
            String timeKey = String.format("%02d:00", hour);
            TimeSlotInfo slotInfo = timeSlotData.get(timeKey);

            HBox timeSlot = createTimeSlot(timeKey, slotInfo);
            timeline.getChildren().add(timeSlot);
        }

        timelineScroll.setContent(timeline);

        VBox.setVgrow(timelineScroll, Priority.ALWAYS);
        panel.getChildren().addAll(toolbar, timelineScroll);

        return panel;
    }

    /**
     * 创建单个时间段
     */
    private HBox createTimeSlot(String time, TimeSlotInfo slotInfo) {
        HBox slot = new HBox();
        slot.setAlignment(Pos.CENTER_LEFT);
        slot.setPrefHeight(60);
        slot.setPadding(new Insets(10, 20, 10, 20));
        slot.setSpacing(15);

        // 根据状态设置背景色
        String bgColor = slotInfo.status.getBgColor();
        String textColor = slotInfo.status.getTextColor();
        String borderStyle = slotInfo.status == TimeSlotStatus.AVAILABLE ? "" :
                "-fx-border-color: " + textColor + "; -fx-border-width: 0 0 0 4;";

        slot.setStyle("-fx-background-color: " + bgColor + "; " + borderStyle +
                " -fx-border-color: #f1f3f4; -fx-border-width: 0 0 1 0;");

        // 时间标签
        Label timeLabel = new Label(time);
        timeLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        timeLabel.setTextFill(Color.web(textColor));
        timeLabel.setPrefWidth(60);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 状态信息
        if (!slotInfo.description.isEmpty()) {
            Label infoLabel = new Label(slotInfo.description);
            infoLabel.setFont(Font.font("Microsoft YaHei", 13));
            infoLabel.setTextFill(Color.web(textColor));
            slot.getChildren().addAll(timeLabel, spacer, infoLabel);
        } else {
            slot.getChildren().addAll(timeLabel, spacer);
        }

        // 可预约的时段添加点击事件和悬停效果
        if (slotInfo.status == TimeSlotStatus.AVAILABLE) {
            slot.setOnMouseClicked(e -> showBookingDialog(time));
            slot.setOnMouseEntered(e -> slot.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #f1f3f4; -fx-border-width: 0 0 1 0; -fx-cursor: hand;"));
            slot.setOnMouseExited(e -> slot.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: #f1f3f4; -fx-border-width: 0 0 1 0;"));
        }

        return slot;
    }

    /**
     * 改变日期
     */
    private void changeDate(int days) {
        currentDate = currentDate.plusDays(days);
        System.out.println("日期变更为: " + currentDate);
        // TODO: 刷新界面
    }

    /**
     * 选择空间
     */
    private void selectSpace(String newSpaceName) {
        this.spaceName = newSpaceName;
        System.out.println("选择空间: " + newSpaceName);
        // TODO: 刷新时间表数据
    }

    /**
     * 显示预约对话框
     */
    private void showBookingDialog(String time) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("预约确认");
        dialog.setHeaderText("预约 " + spaceName + " - " + time);
        dialog.setContentText("确定要预约这个时间段吗？\n\n" +
                "空间：" + spaceName + "\n" +
                "日期：" + currentDate + "\n" +
                "时间：" + time + ":00 - " + (Integer.parseInt(time.split(":")[0]) + 1) + ":00");

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("确认预约: " + spaceName + " " + currentDate + " " + time);
                // TODO: 调用后端API创建预约
                showBookingSuccess(time);
            }
        });
    }

    /**
     * 显示快速预约对话框
     */
    private void showQuickBookDialog() {
        System.out.println("显示快速预约对话框");
        // TODO: 实现快速预约功能
    }

    /**
     * 显示预约成功提示
     */
    private void showBookingSuccess(String time) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("预约成功");
        alert.setHeaderText("预约申请已提交");
        alert.setContentText("您已成功预约 " + spaceName + " 在 " + currentDate + " " + time + " 的使用权限。\n\n请等待管理员审核。");
        alert.showAndWait();
    }
}