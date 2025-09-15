package com.vcampus.client.LifeService;

import com.vcampus.client.service.ClientService;
import com.vcampus.common.entity.*;
import com.vcampus.common.util.CommonUtils;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class LifeService extends BorderPane {
    private VBox cardServiceContainer; // 一卡通服务容器
    private VBox repairServiceContainer; // 后勤报修服务容器
    private VBox paymentServiceContainer; // 生活缴费服务容器
    private HBox topBar; // 顶部导航栏
    private Button homeButton; // 返回首页按钮
    private ClientService clientService;
    private User currentUser;
    private Label balanceValue;
    private Label statusLabel;
    private List<RepairRecord> repairRecords = new ArrayList<>();
    private List<LifePaymentBill> pendingBills = new ArrayList<>();
    private List<LifePaymentRecord> paymentRecords = new ArrayList<>();


    // 添加成员变量保存原始内容
    private HBox buttonContainer;
    private VBox activityContainer;

    public LifeService(ClientService clientService, User currentUser) {
        this.clientService = clientService;
        this.currentUser = currentUser;
        initialize();
        loadRepairData();
    }

    private void initialize() {
        // 设置整体样式
        this.setStyle("-fx-background-color: #f5f7fa;");

        // 创建顶部标题和返回按钮
        topBar = createTopBar();
        this.setTop(topBar);

        // 创建功能按钮区域并保存引用
        buttonContainer = createButtonContainer();
        this.setCenter(buttonContainer);

        // 创建底部活动记录区域并保存引用
        activityContainer = createActivityContainer();
        this.setBottom(activityContainer);

        loadRepairData();
        loadPaymentData();

        // 初始化一卡通服务容器（初始时隐藏）
        cardServiceContainer = createCardServiceContainer();
        cardServiceContainer.setVisible(false);

        // 初始化后勤报修服务容器（初始时隐藏）
        repairServiceContainer = createRepairServiceContainer();
        repairServiceContainer.setVisible(false);

        // 初始化生活缴费服务容器（初始时隐藏）
        paymentServiceContainer = createPaymentServiceContainer();
        paymentServiceContainer.setVisible(false);
    }

    private void refreshRepairUI() {
        // 重新加载数据
        loadRepairData();

        // 更新统计栏
        HBox newStatsContainer = createRepairStats();

        // 获取ScrollPane及其内容
        ScrollPane scrollPane = (ScrollPane) repairServiceContainer.getChildren().get(1);
        VBox mainContainer = (VBox) scrollPane.getContent();

        // 找到并替换现有的统计栏
        BorderPane topContainer = (BorderPane) mainContainer.getChildren().get(0);  // 改为BorderPane
        HBox oldStatsContainer = (HBox) topContainer.getCenter();  // 从Center获取统计栏

        topContainer.setCenter(newStatsContainer); // 将新的统计栏设置到Center位置

        // 刷新报修列表
        TabPane tabPane = (TabPane) mainContainer.getChildren().get(3);
        Tab pendingTab = tabPane.getTabs().get(0);
        Tab completedTab = tabPane.getTabs().get(1);

        pendingTab.setContent(createRepairList(true));
        completedTab.setContent(createRepairList(false));
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // 标题
        Label titleLabel = new Label("欢迎使用生活服务功能");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        // 使用HBox来布局，让标题居中
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // 返回首页按钮（初始时隐藏）
        homeButton = new Button("返回首页");
        homeButton.setStyle("-fx-background-color: #3498db; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-font-family: 'Microsoft YaHei'; " +
                "-fx-background-radius: 5px; " +
                "-fx-padding: 8 15 8 15;");
        homeButton.setVisible(false);
        homeButton.setOnAction(e -> {
            // 隐藏所有服务界面
            hideAllServices();
            // 隐藏返回首页按钮
            homeButton.setVisible(false);
        });

        // 添加组件到顶部导航栏
        topBar.getChildren().addAll(titleLabel, homeButton);

        return topBar;
    }

    private HBox createButtonContainer() {
        HBox container = new HBox(30);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(20));

        // 创建三个功能卡片
        VBox cardCard = createServiceCard("进入一卡通", "校园卡管理与消费", "#4CAF50", "📱");
        VBox repairCard = createServiceCard("进入后勤报修", "设施故障报修服务", "#2196F3", "🔧");
        VBox paymentCard = createServiceCard("进入生活缴费", "水电网络费用缴纳", "#FF9800", "💳");

        container.getChildren().addAll(cardCard, repairCard, paymentCard);

        return container;
    }

    private VBox createServiceCard(String title, String description, String color, String emoji) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(25, 20, 25, 20));
        card.setPrefWidth(220);
        card.setPrefHeight(220);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        // 创建图标区域
        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(70, 70);
        iconContainer.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 35;");

        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font("System", 30));
        emojiLabel.setStyle("-fx-text-fill: white;");

        iconContainer.getChildren().add(emojiLabel);

        // 标题
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);

        // 描述
        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Microsoft YaHei", 14));
        descLabel.setTextFill(Color.web("#7f8c8d"));
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);

        // 进入按钮
        Button actionButton = new Button("进入");
        actionButton.setStyle("-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-font-family: 'Microsoft YaHei'; " +
                "-fx-background-radius: 20px; " +
                "-fx-padding: 8 20 8 20;");

        // 添加按钮点击事件
        actionButton.setOnAction(e -> {
            homeButton.setVisible(true);

            if (title.equals("进入一卡通")) {
                showCardService();
            } else if (title.equals("进入后勤报修")) {
                showRepairService();
            } else if (title.equals("进入生活缴费")) {
                showPaymentService();
            }
        });

        // 添加鼠标悬停效果
        actionButton.setOnMouseEntered(e -> {
            actionButton.setStyle("-fx-background-color: derive(" + color + ", -15%); " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 14px; " +
                    "-fx-font-family: 'Microsoft YaHei'; " +
                    "-fx-background-radius: 20px; " +
                    "-fx-padding: 8 20 8 20;");
        });

        actionButton.setOnMouseExited(e -> {
            actionButton.setStyle("-fx-background-color: " + color + "; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 14px; " +
                    "-fx-font-family: 'Microsoft YaHei'; " +
                    "-fx-background-radius: 20px; " +
                    "-fx-padding: 8 20 8 20;");
        });

        card.getChildren().addAll(iconContainer, titleLabel, descLabel, actionButton);

        // 添加卡片悬停效果
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20, 0, 0, 7);");
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");
        });

        return card;
    }

    private VBox createActivityContainer() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(30, 30, 30, 30));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 15px;");

        // 活动记录标题
        Label activityTitle = new Label("近期活动记录");
        activityTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 20));
        activityTitle.setTextFill(Color.web("#2c3e50"));

        // 分隔线
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 20, 0));

        // 创建活动记录内容区域
        VBox activityContent = new VBox(10);
        activityContent.setPadding(new Insets(10));

        // 获取并显示实际的活动记录
        List<ActivityItem> recentActivities = getRecentActivities();
        if (recentActivities.isEmpty()) {
            Label noActivitiesLabel = new Label("暂无近期活动");
            noActivitiesLabel.setFont(Font.font("Microsoft YaHei", 14));
            noActivitiesLabel.setTextFill(Color.web("#7f8c8d"));
            noActivitiesLabel.setAlignment(Pos.CENTER);
            noActivitiesLabel.setPadding(new Insets(20));
            activityContent.getChildren().add(noActivitiesLabel);
        } else {
            for (ActivityItem activity : recentActivities) {
                activityContent.getChildren().add(createActivityItem(
                        activity.getType(),
                        activity.getContent(),
                        activity.getTime()
                ));
            }
        }

        // 创建滚动面板
        ScrollPane scrollPane = new ScrollPane(activityContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        container.getChildren().addAll(activityTitle, separator, scrollPane);

        // 设置底部区域的外边距
        BorderPane.setMargin(container, new Insets(20, 30, 30, 30));

        return container;
    }

    /**
     * 获取最近的活动记录（从三个模块获取）
     */
    private List<ActivityItem> getRecentActivities() {
        List<ActivityItem> activities = new ArrayList<>();

        // 获取一卡通最近活动
        activities.addAll(getCardActivities());

        // 获取报修最近活动
        activities.addAll(getRepairActivities());

        // 获取缴费最近活动
        activities.addAll(getPaymentActivities());

        // 按时间排序（最新的在前）
        activities.sort((a1, a2) -> a2.getTime().compareTo(a1.getTime()));

        // 只保留最近5条记录
        return activities.subList(0, Math.min(activities.size(), 5));
    }

    /**
     * 获取一卡通相关活动
     */
    private List<ActivityItem> getCardActivities() {
        List<ActivityItem> activities = new ArrayList<>();

        try {
            // 获取一卡通信息
            CardInfo cardInfo = clientService.getCardInfo(currentUser.getUserId());
            if (cardInfo != null) {
                // 获取最近消费记录
                List<CardConsumption> consumptions = clientService.getConsumptionRecords(
                        cardInfo.getCardId(), 3);

                for (CardConsumption consumption : consumptions) {
                    activities.add(new ActivityItem(
                            "一卡通消费",
                            String.format("在%s消费¥%.2f", consumption.getLocation(), consumption.getAmount()),
                            formatDate(consumption.getTime())  // 使用 formatDate 方法转换
                    ));
                }


                // 获取最近充值记录
                List<CardRecharge> recharges = clientService.getRechargeRecords(
                        cardInfo.getCardId(), 2);

                for (CardRecharge recharge : recharges) {
                    activities.add(new ActivityItem(
                            "一卡通充值",
                            String.format("充值¥%.2f", recharge.getAmount()),
                            formatDate(recharge.getTime())  // 使用 formatDate 方法转换
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("获取一卡通活动记录失败: " + e.getMessage());
        }

        return activities;
    }

    /**
     * 获取报修相关活动
     */
    private List<ActivityItem> getRepairActivities() {
        List<ActivityItem> activities = new ArrayList<>();

        try {
            // 获取报修记录
            Message request = new Message(Message.Type.REPAIR_GET_LIST, "");
            Message response = clientService.sendRequest(request);

            if (response.getCode() == Message.Code.SUCCESS) {
                List<RepairRecord> records = CommonUtils.convertToGenericList(
                        response.getData(), RepairRecord.class);

                // 按时间排序，取最近3条
                records.sort((r1, r2) -> r2.getCreateTime().compareTo(r1.getCreateTime()));
                List<RepairRecord> recentRecords = records.subList(0, Math.min(records.size(), 3));

                for (RepairRecord record : recentRecords) {
                    String statusText = "";
                    switch (record.getStatus()) {
                        case "待处理": statusText = "已提交"; break;
                        case "处理中": statusText = "正在处理"; break;
                        case "已完成": statusText = "已完成"; break;
                        default: statusText = record.getStatus();
                    }

                    activities.add(new ActivityItem(
                            "报修" + statusText,
                            record.getTitle(),
                            formatDate(record.getCreateTime())
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("获取报修活动记录失败: " + e.getMessage());
        }

        return activities;
    }

    /**
     * 获取缴费相关活动
     */
    private List<ActivityItem> getPaymentActivities() {
        List<ActivityItem> activities = new ArrayList<>();

        try {
            // 获取缴费记录
            Message request = new Message(Message.Type.LIFE_PAYMENT_GET_RECORDS, 3);
            Message response = clientService.sendRequest(request);

            if (response.getCode() == Message.Code.SUCCESS) {
                List<LifePaymentRecord> records = CommonUtils.convertToGenericList(
                        response.getData(), LifePaymentRecord.class);

                for (LifePaymentRecord record : records) {
                    activities.add(new ActivityItem(
                            "缴费成功",
                            String.format("%s缴费¥%.2f", record.getBillType(), record.getPayAmount()),
                            formatDate(record.getPayTime())  // 使用 formatDate 方法转换
                    ));
                }
            }

            // 获取待缴费账单
            Message billRequest = new Message(Message.Type.LIFE_PAYMENT_GET_BILLS, "待支付");
            Message billResponse = clientService.sendRequest(billRequest);

            if (billResponse.getCode() == Message.Code.SUCCESS) {
                List<LifePaymentBill> bills = CommonUtils.convertToGenericList(
                        billResponse.getData(), LifePaymentBill.class);

                // 取最近2条待缴费账单
                bills.sort((b1, b2) -> b2.getDueDate().compareTo(b1.getDueDate()));
                List<LifePaymentBill> recentBills = bills.subList(0, Math.min(bills.size(), 2));

                for (LifePaymentBill bill : recentBills) {
                    activities.add(new ActivityItem(
                            "新账单",
                            String.format("%s账单¥%.2f待支付", bill.getBillType(), bill.getAmount()),
                            formatDate(bill.getCreateTime())  // 使用 formatDate 方法转换
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("获取缴费活动记录失败: " + e.getMessage());
        }

        return activities;
    }



    private HBox createActivityItem(String type, String content, String time) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12, 15, 12, 15));
        item.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10px;");

        // 类型标签
        Label typeLabel = new Label(type);
        typeLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 12));

        // 根据类型设置不同颜色
        switch(type) {
            case "新消费":
                typeLabel.setTextFill(Color.web("#e74c3c"));
                break;
            case "新报修":
                typeLabel.setTextFill(Color.web("#3498db"));
                break;
            case "缴费成功":
                typeLabel.setTextFill(Color.web("#2ecc71"));
                break;
            case "新通知":
                typeLabel.setTextFill(Color.web("#f39c12"));
                break;
            case "报修完成":
                typeLabel.setTextFill(Color.web("#9b59b6"));
                break;
            default:
                typeLabel.setTextFill(Color.web("#7f8c8d"));
        }

        typeLabel.setMinWidth(60);

        // 内容标签
        Label contentLabel = new Label(content);
        contentLabel.setFont(Font.font("Microsoft YaHei", 14));
        contentLabel.setTextFill(Color.web("#2c3e50"));
        contentLabel.setMaxWidth(400);
        contentLabel.setWrapText(true);

        // 时间标签
        Label timeLabel = new Label(time);
        timeLabel.setFont(Font.font("Microsoft YaHei", 12));
        timeLabel.setTextFill(Color.web("#95a5a6"));
        timeLabel.setMinWidth(70);

        HBox.setHgrow(contentLabel, Priority.ALWAYS);

        item.getChildren().addAll(typeLabel, contentLabel, timeLabel);

        return item;
    }

    private VBox createCardServiceContainer() {
        VBox container = new VBox(20);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);");

        // 标题
        Label titleLabel = new Label("一卡通服务");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        titleLabel.setPadding(new Insets(0, 0, 20, 0));
        container.getChildren().add(titleLabel);

        // 创建主容器，使用HBox实现左右布局
        HBox mainContainer = new HBox(30);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");

        // 左侧用户信息面板
        VBox userInfoPanel = createUserInfoPanel();
        userInfoPanel.setPrefWidth(300);
        userInfoPanel.setMaxWidth(300);

        // 右侧功能面板
        VBox functionPanel = new VBox(20);
        functionPanel.setAlignment(Pos.TOP_CENTER);
        functionPanel.setPadding(new Insets(0, 0, 0, 0));
        HBox.setHgrow(functionPanel, Priority.ALWAYS);

        // 功能选项 - 使用网格布局
        GridPane optionsGrid = new GridPane();
        optionsGrid.setAlignment(Pos.CENTER);
        optionsGrid.setHgap(20);
        optionsGrid.setVgap(15);
        optionsGrid.setPadding(new Insets(10));

        // 添加功能按钮 - 使用2x2网格布局
        optionsGrid.add(createFunctionOption("充值", "为一卡通账户充值", "#4CAF50", null), 0, 0);
        optionsGrid.add(createFunctionOption("账单", "查看消费记录", "#2196F3", null), 1, 0);
        optionsGrid.add(createFunctionOption("挂失", "挂失或解挂一卡通", "#FF9800", null), 0, 1);
        optionsGrid.add(createFunctionOption("余额查询", "查询当前余额", "#9C27B0", null), 1, 1);

        functionPanel.getChildren().add(optionsGrid);

        // 将左右面板添加到主容器
        mainContainer.getChildren().addAll(userInfoPanel, functionPanel);
        HBox.setHgrow(mainContainer, Priority.ALWAYS);

        // 将主容器添加到容器
        container.getChildren().add(mainContainer);
        VBox.setVgrow(mainContainer, Priority.ALWAYS);

        return container;
    }

    private VBox createUserInfoPanel() {
        VBox panel = new VBox(15);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(20, 15, 20, 15));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");

        // 从数据库获取一卡通信息
        CardInfo cardInfo = clientService.getCardInfo(currentUser.getUserId());

        // 用户头像
        StackPane avatarContainer = new StackPane();
        avatarContainer.setPrefSize(80, 80);
        avatarContainer.setStyle("-fx-background-color: #3498db; -fx-background-radius: 40;");

        Label avatarLabel = new Label("图片");
        avatarLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
        avatarLabel.setTextFill(Color.WHITE);

        avatarContainer.getChildren().add(avatarLabel);

        // 用户姓名 - 从当前用户获取
        Label nameLabel = new Label(currentUser.getDisplayName());
        nameLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        nameLabel.setTextFill(Color.web("#2c3e50"));

        // 学号 - 从当前用户获取
        Label idLabel = new Label("学号: " + currentUser.getUserId());
        idLabel.setFont(Font.font("Microsoft YaHei", 14));
        idLabel.setTextFill(Color.web("#7f8c8d"));

        // 分隔线
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        // 卡片余额 - 从数据库获取
        VBox balanceBox = new VBox(5);
        balanceBox.setAlignment(Pos.CENTER);

        Label balanceTitle = new Label("当前余额");
        balanceTitle.setFont(Font.font("Microsoft YaHei", 14));
        balanceTitle.setTextFill(Color.web("#7f8c8d"));

        // 保存余额标签为成员变量，以便后续更新
        balanceValue = new Label(cardInfo != null ? "¥ " + String.format("%.2f", cardInfo.getBalance()) : "¥ 0.00");
        balanceValue.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 22));
        balanceValue.setTextFill(Color.web("#e74c3c"));

        balanceBox.getChildren().addAll(balanceTitle, balanceValue);

        // 卡片状态 - 从数据库获取
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER);

        // 保存状态标签为成员变量，以便后续更新
        statusLabel = new Label("状态: " + (cardInfo != null ? getStatusText(cardInfo.getStatus()) : "未知"));
        statusLabel.setFont(Font.font("Microsoft YaHei", 14));

        // 根据状态设置不同颜色
        if (cardInfo != null && "NORMAL".equals(cardInfo.getStatus())) {
            statusLabel.setTextFill(Color.web("#2ecc71"));
        } else if (cardInfo != null && "LOST".equals(cardInfo.getStatus())) {
            statusLabel.setTextFill(Color.web("#e74c3c"));
        } else {
            statusLabel.setTextFill(Color.web("#7f8c8d"));
        }

        statusBox.getChildren().add(statusLabel);

        panel.getChildren().addAll(avatarContainer, nameLabel, idLabel, separator, balanceBox, statusBox);

        return panel;
    }

    // 创建后勤报修服务容器
    private VBox createRepairServiceContainer() {
        VBox container = new VBox(20);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);");

        // 标题
        Label titleLabel = new Label("后勤报修服务");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        titleLabel.setPadding(new Insets(0, 0, 20, 0));
        container.getChildren().add(titleLabel);

        // 创建上下分栏布局
        VBox mainContainer = new VBox(20);
        mainContainer.setFillWidth(true); // 允许宽度填充

        // 统计栏和申请按钮容器 - 修改为使用BorderPane实现左右布局
        BorderPane topContainer = new BorderPane();
        topContainer.setPadding(new Insets(0, 0, 10, 0));

        // 统计栏 - 修改为居中显示
        HBox statsContainer = createRepairStats();
        statsContainer.setAlignment(Pos.CENTER); // 确保统计栏内容居中
        BorderPane.setAlignment(statsContainer, Pos.CENTER);
        topContainer.setCenter(statsContainer);

        // 申请报修按钮 - 放在右侧
        Button applyButton = new Button("申请报修");
        applyButton.setStyle("-fx-background-color: #2196F3; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 16px; " +
                "-fx-font-family: 'Microsoft YaHei'; " +
                "-fx-background-radius: 10px; " +
                "-fx-padding: 12 30 12 30;");
        applyButton.setOnAction(e -> {
            showRepairApplicationDialog();
        });
        BorderPane.setAlignment(applyButton, Pos.CENTER_RIGHT);
        topContainer.setRight(applyButton);

        mainContainer.getChildren().add(topContainer);

        // 分隔线
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));
        mainContainer.getChildren().add(separator);

        // 报修记录标题
        Label recordsTitle = new Label("报修记录");
        recordsTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        recordsTitle.setTextFill(Color.web("#2c3e50"));
        mainContainer.getChildren().add(recordsTitle);

        // 报修记录选项卡
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // 待处理报修选项卡
        Tab pendingTab = new Tab("待处理");
        pendingTab.setContent(createRepairList(true));

        // 已处理报修选项卡
        Tab completedTab = new Tab("已处理");
        completedTab.setContent(createRepairList(false));

        tabPane.getTabs().addAll(pendingTab, completedTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS); // 允许选项卡区域扩展
        mainContainer.getChildren().add(tabPane);

        // 创建滚动面板
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true); // 允许高度适应
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        // 设置容器扩展属性
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        container.getChildren().add(scrollPane);

        return container;
    }

    // 创建生活缴费服务容器
    private VBox createPaymentServiceContainer() {
        VBox container = new VBox(20);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);");

        // 标题
        Label titleLabel = new Label("生活缴费服务");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        titleLabel.setPadding(new Insets(0, 0, 20, 0));
        container.getChildren().add(titleLabel);

        // 创建主要内容容器
        VBox mainContainer = new VBox(20);

        // 统计栏
        HBox statsContainer = createPaymentStats();
        mainContainer.getChildren().add(statsContainer);

        // 分隔线
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));
        mainContainer.getChildren().add(separator);

        // 待缴费项目标题
        Label pendingTitle = new Label("待缴费项目");
        pendingTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        pendingTitle.setTextFill(Color.web("#2c3e50"));
        mainContainer.getChildren().add(pendingTitle);

        // 待缴费项目列表
        VBox pendingPayments = createPendingPayments();
        mainContainer.getChildren().add(pendingPayments);

        // 分隔线
        Separator separator2 = new Separator();
        separator2.setPadding(new Insets(15, 0, 15, 0));
        mainContainer.getChildren().add(separator2);

        // 缴费记录标题
        Label historyTitle = new Label("缴费记录");
        historyTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        historyTitle.setTextFill(Color.web("#2c3e50"));
        mainContainer.getChildren().add(historyTitle);

        // 缴费记录选项卡
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // 本月记录选项卡
        Tab currentMonthTab = new Tab("本月");
        currentMonthTab.setContent(createPaymentHistory(true));

        // 历史记录选项卡
        Tab historyTab = new Tab("历史");
        historyTab.setContent(createPaymentHistory(false));

        tabPane.getTabs().addAll(currentMonthTab, historyTab);
        mainContainer.getChildren().add(tabPane);

        // 创建滚动面板
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        container.getChildren().add(scrollPane);

        return container;
    }

    // 创建缴费统计栏
    private HBox createPaymentStats() {
        HBox statsContainer = new HBox(20);
        statsContainer.setAlignment(Pos.CENTER);
        statsContainer.setPadding(new Insets(10, 0, 10, 0));

        // 计算统计数据
        double totalCurrentMonth = 0.0;
        double totalPending = 0.0;
        double totalPaid = 0.0;

        // 获取当前月份
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        // 计算本月消费和总已缴费
        for (LifePaymentRecord record : paymentRecords) {
            LocalDate paymentDate = record.getPayTime().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();

            // Cast getAmount() to a numeric type (e.g., Double) before use
            double amount = (Double) record.getPayAmount(); // Use appropriate type casting

            if (paymentDate.getMonthValue() == currentMonth &&
                    paymentDate.getYear() == currentYear) {
                totalCurrentMonth += amount;
            }
            totalPaid += amount;
        }

        // 计算待缴费金额
        for (LifePaymentBill bill : pendingBills) {
            if ("待支付".equals(bill.getStatus())) {
                totalPending += bill.getAmount();
            }
        }


        // 使用实际数据创建统计盒子
        VBox totalBox = createStatBox("本月消费", "¥" + String.format("%.2f", totalCurrentMonth), "#3498db", "💰");
        VBox pendingBox = createStatBox("待缴费", "¥" + String.format("%.2f", totalPending), "#e74c3c", "⏳");
        VBox paidBox = createStatBox("已缴费", "¥" + String.format("%.2f", totalPaid), "#2ecc71", "✅");

        statsContainer.getChildren().addAll(totalBox, pendingBox, paidBox);

        return statsContainer;
    }


    // 创建待缴费项目列表
    private VBox createPendingPayments() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        for (LifePaymentBill bill : pendingBills) {
            container.getChildren().add(createPaymentItem(bill));
        }

        return container;
    }


    // 创建缴费记录列表
    private ScrollPane createPaymentHistory(boolean isCurrentMonth) {
        VBox listContainer = new VBox(10);
        listContainer.setPadding(new Insets(10));

        // 获取当前月份
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        // 筛选记录
        List<LifePaymentRecord> filteredRecords = new ArrayList<>();
        for (LifePaymentRecord record : paymentRecords) {
            LocalDate paymentDate = record.getPayTime().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();

            if (isCurrentMonth) {
                if (paymentDate.getMonthValue() == currentMonth &&
                        paymentDate.getYear() == currentYear) {
                    filteredRecords.add(record);
                }
            } else {
                if (paymentDate.getMonthValue() != currentMonth ||
                        paymentDate.getYear() != currentYear) {
                    filteredRecords.add(record);
                }
            }
        }

        // 如果没有记录，显示提示信息
        if (filteredRecords.isEmpty()) {
            Label noRecordsLabel = new Label(isCurrentMonth ? "本月暂无缴费记录" : "暂无历史缴费记录");
            noRecordsLabel.setFont(Font.font("Microsoft YaHei", 14));
            noRecordsLabel.setTextFill(Color.web("#7f8c8d"));
            noRecordsLabel.setAlignment(Pos.CENTER);
            noRecordsLabel.setPadding(new Insets(20));
            listContainer.getChildren().add(noRecordsLabel);
        } else {
            // 按日期倒序排列
            filteredRecords.sort((r1, r2) -> r2.getPayTime().compareTo(r1.getPayTime()));

            // 添加缴费记录项
            for (LifePaymentRecord record : filteredRecords) {
                listContainer.getChildren().add(createPaymentRecordItem(record));
            }
        }

        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        return scrollPane;
    }

    private HBox createPaymentRecordItem(LifePaymentRecord record) {
        HBox container = new HBox(15);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(12, 15, 12, 15));
        container.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10px;");

        // 类型图标
        Label typeIcon = new Label();
        typeIcon.setFont(Font.font("System", 20));

        switch(record.getBillType()) {
            case "电费":
                typeIcon.setText("⚡");
                break;
            case "水费":
                typeIcon.setText("💧");
                break;
            case "网费":
                typeIcon.setText("🌐");
                break;
            default:
                typeIcon.setText("💰");
        }

        // 主要内容
        VBox contentBox = new VBox(5);

        Label titleLabel = new Label(record.getBillType());
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web("#2c3e50"));


        contentBox.getChildren().addAll(titleLabel);

        // 右侧信息
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_RIGHT);
        infoBox.setMinWidth(120);

        Label amountLabel = new Label("¥" + String.format("%.2f", record.getPayAmount()));
        amountLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        amountLabel.setTextFill(Color.web("#2c3e50"));

        // 格式化日期
        String formattedDate = formatDate(record.getPayTime());
        Label dateLabel = new Label(formattedDate);
        dateLabel.setFont(Font.font("Microsoft YaHei", 12));
        dateLabel.setTextFill(Color.web("#7f8c8d"));

        // 支付方式
        Label methodLabel = new Label(record.getPaymentMethod());
        methodLabel.setFont(Font.font("Microsoft YaHei", 11));
        methodLabel.setTextFill(Color.web("#7f8c8d"));

        infoBox.getChildren().addAll(amountLabel, dateLabel, methodLabel);

        HBox.setHgrow(contentBox, Priority.ALWAYS);

        container.getChildren().addAll(typeIcon, contentBox, infoBox);

        return container;
    }

    // 创建缴费项
    private HBox createPaymentItem(LifePaymentBill bill) {
        HBox container = new HBox(15);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(12, 15, 12, 15));
        container.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10px;");

        // 类型图标
        Label typeIcon = new Label();
        typeIcon.setFont(Font.font("System", 20));

        switch(bill.getBillType()) {
            case "电费":
                typeIcon.setText("⚡");
                break;
            case "水费":
                typeIcon.setText("💧");
                break;
            case "网费":
                typeIcon.setText("🌐");
                break;
            default:
                typeIcon.setText("💰");
        }

        // 主要内容
        VBox contentBox = new VBox(5);

        Label titleLabel = new Label(bill.getBillType());
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        Label dueDateLabel = new Label("截止日期: " + bill.getDueDate().toString());
        dueDateLabel.setFont(Font.font("Microsoft YaHei", 12));
        dueDateLabel.setTextFill(Color.web("#7f8c8d"));

        contentBox.getChildren().addAll(titleLabel, dueDateLabel);

        // 右侧信息
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_RIGHT);
        infoBox.setMinWidth(120);

        Label amountLabel = new Label("¥" + String.format("%.2f", bill.getAmount()));
        amountLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        amountLabel.setTextFill(Color.web("#2c3e50"));

        // 状态标签
        Label statusLabel = new Label(bill.getStatus());
        statusLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 12));
        statusLabel.setPadding(new Insets(3, 8, 3, 8));
        statusLabel.setStyle("-fx-background-radius: 10;");

        // 根据状态设置颜色
        if ("待支付".equals(bill.getStatus())) {
            statusLabel.setTextFill(Color.web("#e74c3c"));
            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #ffebee;");

            // 添加支付按钮
            Button payButton = new Button("立即支付");
            payButton.setStyle("-fx-background-color: #4CAF50; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 12px; " +
                    "-fx-font-family: 'Microsoft YaHei'; " +
                    "-fx-background-radius: 5px; " +
                    "-fx-padding: 5 10 5 10;");
            payButton.setOnAction(e -> {
                showPaymentDialog(bill);
            });

            infoBox.getChildren().addAll(amountLabel, statusLabel, payButton);
        } else {
            statusLabel.setTextFill(Color.web("#2ecc71"));
            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #e8f5e9;");
            infoBox.getChildren().addAll(amountLabel, statusLabel);
        }

        HBox.setHgrow(contentBox, Priority.ALWAYS);

        container.getChildren().addAll(typeIcon, contentBox, infoBox);

        return container;
    }

    // 显示支付对话框
    private void showPaymentDialog(PaymentItem item) {
        // 创建对话框
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("支付确认");
        dialog.setHeaderText("请确认支付信息");

        // 设置对话框按钮
        ButtonType payButtonType = new ButtonType("确认支付", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(payButtonType, ButtonType.CANCEL);

        // 创建表单布局
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        // 显示支付信息
        grid.add(new Label("缴费类型:"), 0, 0);
        grid.add(new Label(item.type), 1, 0);

        grid.add(new Label("缴费位置:"), 0, 1);
        grid.add(new Label(item.location), 1, 1);

        grid.add(new Label("缴费金额:"), 0, 2);
        grid.add(new Label(item.amount), 1, 2);

        grid.add(new Label("缴费日期:"), 0, 3);
        grid.add(new Label(item.date), 1, 3);

        // 支付方式选择
        ComboBox<String> paymentMethod = new ComboBox<>();
        paymentMethod.getItems().addAll("一卡通支付", "银行卡支付", "支付宝", "微信支付");
        paymentMethod.setValue("一卡通支付");

        grid.add(new Label("支付方式:"), 0, 4);
        grid.add(paymentMethod, 1, 4);

        // 设置对话框内容
        dialog.getDialogPane().setContent(grid);

        // 设置支付按钮的处理逻辑
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == payButtonType) {
                // 这里可以添加支付逻辑
                System.out.println("支付: " + item.type + ", " + item.amount + ", 方式: " + paymentMethod.getValue());
            }
            return null;
        });

        // 显示对话框
        dialog.showAndWait();
    }

    // 创建报修统计栏
    private HBox createRepairStats() {
        HBox statsContainer = new HBox(20);
        statsContainer.setAlignment(Pos.CENTER); // 确保内容居中
        statsContainer.setPadding(new Insets(10, 0, 10, 0));
        statsContainer.setMaxWidth(Double.MAX_VALUE);

        // 计算统计数据
        int total = repairRecords.size();
        int pending = 0;
        int completed = 0;

        for (RepairRecord record : repairRecords) {
            if ("待处理".equals(record.getStatus())) {
                pending++;
            } else if ("已完成".equals(record.getStatus())) {
                completed++;
            }
        }

        // 使用实际数据创建统计盒子
        VBox totalBox = createStatBox("总报修数", String.valueOf(total), "#3498db", "📋");
        VBox pendingBox = createStatBox("待处理", String.valueOf(pending), "#e74c3c", "⏳");
        VBox completedBox = createStatBox("已完成", String.valueOf(completed), "#2ecc71", "✅");

        // 设置每个统计项的扩展属性
        HBox.setHgrow(totalBox, Priority.ALWAYS);
        HBox.setHgrow(pendingBox, Priority.ALWAYS);
        HBox.setHgrow(completedBox, Priority.ALWAYS);

        // 设置每个统计项的最小和最大宽度
        totalBox.setMinWidth(120);
        totalBox.setMaxWidth(Double.MAX_VALUE); // 允许扩展到最大宽度
        pendingBox.setMinWidth(120);
        pendingBox.setMaxWidth(Double.MAX_VALUE);
        completedBox.setMinWidth(120);
        completedBox.setMaxWidth(Double.MAX_VALUE);

        statsContainer.getChildren().addAll(totalBox, pendingBox, completedBox);

        return statsContainer;
    }


    // 创建统计盒子
    private VBox createStatBox(String title, String value, String color, String emoji) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15, 20, 15, 20));
        box.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");
        box.setMinWidth(100);
        box.setMaxWidth(Double.MAX_VALUE); // 允许扩展到最大宽度

        // 表情符号
        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font("System", 20));

        // 数值
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        valueLabel.setTextFill(Color.web(color));
        valueLabel.setMaxWidth(Double.MAX_VALUE);
        valueLabel.setAlignment(Pos.CENTER);

        // 标题
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Microsoft YaHei", 12));
        titleLabel.setTextFill(Color.web("#7f8c8d"));
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);

        box.getChildren().addAll(emojiLabel, valueLabel, titleLabel);

        return box;
    }

    // 创建报修列表
    private ScrollPane createRepairList(boolean isPending) {
        VBox listContainer = new VBox(10);
        listContainer.setPadding(new Insets(10));

        // 筛选对应状态的报修记录
        List<RepairRecord> filteredRecords = new ArrayList<>();
        for (RepairRecord record : repairRecords) {
            if (isPending && "待处理".equals(record.getStatus())) {
                filteredRecords.add(record);
            } else if (!isPending && !"待处理".equals(record.getStatus())) {
                filteredRecords.add(record);
            }
        }

        System.out.println("Filtered " + filteredRecords.size() + " records for " +
                (isPending ? "pending" : "completed")); // 调试信息

        // 如果没有记录，显示提示信息
        if (filteredRecords.isEmpty()) {
            Label noRecordsLabel = new Label(isPending ? "暂无待处理报修" : "暂无已处理报修");
            noRecordsLabel.setFont(Font.font("Microsoft YaHei", 14));
            noRecordsLabel.setTextFill(Color.web("#7f8c8d"));
            noRecordsLabel.setAlignment(Pos.CENTER);
            noRecordsLabel.setPadding(new Insets(20));
            listContainer.getChildren().add(noRecordsLabel);
        } else {
            // 添加报修项
            for (RepairRecord record : filteredRecords) {
                listContainer.getChildren().add(createRepairItem(record));
            }
        }

        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(250);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        return scrollPane;
    }

    // 创建报修项
    private HBox createRepairItem(RepairRecord record) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12, 15, 12, 15));
        item.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10px;");

        // 优先级指示器
        Rectangle priorityIndicator = new Rectangle(8, 60);

        // 根据优先级设置颜色
        switch(record.getPriority()) {
            case "高":
                priorityIndicator.setFill(Color.web("#e74c3c"));
                break;
            case "中":
                priorityIndicator.setFill(Color.web("#f39c12"));
                break;
            case "低":
                priorityIndicator.setFill(Color.web("#3498db"));
                break;
            default:
                priorityIndicator.setFill(Color.web("#95a5a6"));
        }

        priorityIndicator.setArcWidth(10);
        priorityIndicator.setArcHeight(10);

        // 主要内容
        VBox contentBox = new VBox(5);

        Label titleLabel = new Label(record.getTitle());
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        Label descLabel = new Label(record.getDescription());
        descLabel.setFont(Font.font("Microsoft YaHei", 12));
        descLabel.setTextFill(Color.web("#7f8c8d"));
        descLabel.setWrapText(true);

        contentBox.getChildren().addAll(titleLabel, descLabel);

        // 右侧信息
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_RIGHT);
        infoBox.setMinWidth(120);

        Label locationLabel = new Label("位置: " + record.getLocation());
        locationLabel.setFont(Font.font("Microsoft YaHei", 11));
        locationLabel.setTextFill(Color.web("#7f8c8d"));

        // 格式化日期显示
        String formattedDate = formatDate(record.getCreateTime());
        Label dateLabel = new Label(formattedDate);
        dateLabel.setFont(Font.font("Microsoft YaHei", 11));
        dateLabel.setTextFill(Color.web("#7f8c8d"));

        // 状态标签
        Label statusLabel = new Label(record.getStatus());
        statusLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 12));
        statusLabel.setPadding(new Insets(3, 8, 3, 8));
        statusLabel.setStyle("-fx-background-radius: 10;");

        // 根据状态设置颜色
        if ("待处理".equals(record.getStatus())) {
            statusLabel.setTextFill(Color.web("#e74c3c"));
            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #ffebee;");
        } else if ("处理中".equals(record.getStatus())) {
            statusLabel.setTextFill(Color.web("#f39c12"));
            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #fff3e0;");
        } else if ("已完成".equals(record.getStatus())) {
            statusLabel.setTextFill(Color.web("#2ecc71"));
            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #e8f5e9;");
        } else {
            statusLabel.setTextFill(Color.web("#95a5a6"));
            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #f5f5f5;");
        }

        infoBox.getChildren().addAll(locationLabel, dateLabel, statusLabel);

        HBox.setHgrow(contentBox, Priority.ALWAYS);

        item.getChildren().addAll(priorityIndicator, contentBox, infoBox);

        return item;
    }

    // 辅助方法：格式化日期
    private String formatDate(Date date) {
        if (date == null) return "未知日期";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return date.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime()
                .format(formatter);
    }

    // 显示报修申请对话框
    private void showRepairApplicationDialog() {
        // 创建对话框
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("申请报修");
        dialog.setHeaderText("请填写报修信息");

        // 设置对话框按钮
        ButtonType submitButtonType = new ButtonType("提交", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        // 创建表单布局
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        // 表单字段
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("水电维修", "空调维修", "网络故障", "家具维修", "其他");
        typeCombo.setValue("水电维修");
        typeCombo.setPrefWidth(200);

        TextField locationField = new TextField();
        locationField.setPromptText("例如: D502宿舍");

        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("高", "中", "低");
        priorityCombo.setValue("中");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("请详细描述问题情况...");
        descriptionArea.setPrefRowCount(4);

        // 添加字段到网格
        grid.add(new Label("报修类型:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label("报修位置:"), 0, 1);
        grid.add(locationField, 1, 1);
        grid.add(new Label("紧急程度:"), 0, 2);
        grid.add(priorityCombo, 1, 2);
        grid.add(new Label("问题描述:"), 0, 3);
        grid.add(descriptionArea, 1, 3);

        // 设置对话框内容
        dialog.getDialogPane().setContent(grid);

        // 设置提交按钮的处理逻辑
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                // 提交报修申请
                Object[] params = {
                        typeCombo.getValue() + "报修", // 标题
                        descriptionArea.getText(),    // 描述
                        locationField.getText(),      // 位置
                        priorityCombo.getValue()      // 优先级
                };

                Message request = new Message(Message.Type.REPAIR_APPLY, params);
                Message response = clientService.sendRequest(request);

                if (response.getCode() == Message.Code.SUCCESS) {
                    showAlert(Alert.AlertType.INFORMATION, "提交成功", "报修申请已提交，我们会尽快处理");

                    // 重新加载数据并刷新UI
                    Platform.runLater(() -> {
                        loadRepairData();
                        refreshRepairUI();
                    });
                } else {
                    showAlert(Alert.AlertType.ERROR, "提交失败", "报修申请提交失败: " + response.getData());
                }
            }
            return null;
        });

        // 显示对话框
        dialog.showAndWait();
    }

    private HBox createFunctionOption(String title, String description, String color, String emoji) {
        HBox option = new HBox(15);
        option.setAlignment(Pos.CENTER_LEFT);
        option.setPadding(new Insets(15, 20, 15, 20));
        option.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");
        option.setPrefWidth(250);

        // 彩色标识
        Rectangle colorIndicator = new Rectangle(8, 40);
        colorIndicator.setFill(Color.web(color));
        colorIndicator.setArcWidth(10);
        colorIndicator.setArcHeight(10);

        // 文本内容
        VBox textContainer = new VBox(5);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Microsoft YaHei", 13));
        descLabel.setTextFill(Color.web("#7f8c8d"));

        textContainer.getChildren().addAll(titleLabel, descLabel);

        // 添加悬停效果
        option.setOnMouseEntered(e -> {
            option.setStyle("-fx-background-color: #e8f4fc; -fx-background-radius: 10; -fx-cursor: hand;");
        });

        option.setOnMouseExited(e -> {
            option.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");
        });

        // 根据标题添加不同的点击事件
        option.setOnMouseClicked(e -> {
            if (title.equals("充值")) {
                showRechargeDialog();
            } else if (title.equals("账单")) {
                showConsumptionRecords();
            } else if (title.equals("挂失")) {
                showReportLossDialog();
            } else if (title.equals("余额查询")) {
                refreshCardInfo();
            }
        });

        option.getChildren().addAll(colorIndicator, textContainer);

        return option;
    }

    private void showCardService() {
        // 显示一卡通服务容器
        cardServiceContainer.setVisible(true);
        CardInfo cardInfo = clientService.getCardInfo("当前用户ID");
        if (cardInfo != null) {
            // 更新UI显示
            updateCardInfoUI(cardInfo);

            // 获取消费记录
            List<CardConsumption> consumptions = clientService.getConsumptionRecords(
                    cardInfo.getCardId(), 10);
            updateConsumptionUI(consumptions);
        }

        // 将一卡通服务容器放置在中心区域（占用整个下方）
        this.setCenter(cardServiceContainer);

        // 添加淡入动画
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), cardServiceContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // 隐藏底部活动记录区域
        this.setBottom(null);

        // 隐藏右侧区域
        this.setRight(null);

        // 隐藏其他服务容器
        repairServiceContainer.setVisible(false);
        paymentServiceContainer.setVisible(false);
    }

    private void showRepairService() {
        loadRepairData();
        refreshRepairUI();

        // 显示后勤报修服务容器
        repairServiceContainer.setVisible(true);

        // 将后勤报修服务容器放置在中心区域（占用整个下方）
        this.setCenter(repairServiceContainer);

        // 添加淡入动画
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), repairServiceContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // 隐藏底部活动记录区域
        this.setBottom(null);

        // 隐藏右侧区域
        this.setRight(null);

        // 隐藏其他服务容器
        cardServiceContainer.setVisible(false);
        paymentServiceContainer.setVisible(false);
    }

    private void showPaymentService() {
        // 显示生活缴费服务容器
        paymentServiceContainer.setVisible(true);

        // 将生活缴费服务容器放置在中心区域（占用整个下方）
        this.setCenter(paymentServiceContainer);

        // 添加淡入动画
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), paymentServiceContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // 隐藏底部活动记录区域
        this.setBottom(null);

        // 隐藏右侧区域
        this.setRight(null);

        // 隐藏其他服务容器
        cardServiceContainer.setVisible(false);
        repairServiceContainer.setVisible(false);
    }

    // 添加充值对话框方法
    private void showRechargeDialog() {
        // 获取当前用户的一卡通信息
        CardInfo cardInfo = clientService.getCardInfo(currentUser.getUserId());
        if (cardInfo == null) {
            showAlert(Alert.AlertType.ERROR, "错误", "无法获取一卡通信息");
            return;
        }

        // 创建充值对话框
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("一卡通充值");

        // 设置对话框按钮
        ButtonType rechargeButtonType = new ButtonType("充值", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(rechargeButtonType, ButtonType.CANCEL);

        // 创建充值表单
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        TextField amountField = new TextField();
        amountField.setPromptText("请输入充值金额");

        ComboBox<String> methodCombo = new ComboBox<>();
        methodCombo.getItems().addAll("支付宝", "微信", "银行卡", "现金");
        methodCombo.setValue("支付宝");

        grid.add(new Label("充值金额:"), 0, 0);
        grid.add(amountField, 1, 0);
        grid.add(new Label("支付方式:"), 0, 1);
        grid.add(methodCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // 设置结果转换器
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == rechargeButtonType) {
                try {
                    return Double.parseDouble(amountField.getText());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
            return null;
        });

        // 显示对话框并处理结果
        Optional<Double> result = dialog.showAndWait();
        result.ifPresent(amount -> {
            if (amount > 0) {
                // 调用充值服务，使用正确的卡号
                boolean success = clientService.rechargeCard(
                        cardInfo.getCardId(), // 使用实际卡号
                        amount,
                        methodCombo.getValue()
                );

                if (success) {
                    // 显示成功消息
                    showAlert(Alert.AlertType.INFORMATION, "充值成功", "已成功充值 ¥" + amount);
                    // 刷新一卡通信息
                    refreshCardInfo();
                } else {
                    showAlert(Alert.AlertType.ERROR, "充值失败", "请稍后重试");
                }
            }
        });
    }

    // 添加更新UI的方法
    private void updateCardInfoUI(CardInfo cardInfo) {
        // 更新用户信息面板
        // 这里需要根据实际UI组件进行更新
    }

    private void updateConsumptionUI(List<CardConsumption> consumptions) {
        // 更新消费记录列表
        // 根据实际UI组件进行更新
    }

    // 显示消费记录
    private void showConsumptionRecords() {
        CardInfo cardInfo = clientService.getCardInfo(currentUser.getUserId());
        if (cardInfo != null) {
            List<CardConsumption> consumptions = clientService.getConsumptionRecords(
                    cardInfo.getCardId(), 20);

            // 创建新窗口显示消费记录
            Stage consumptionStage = new Stage();
            consumptionStage.setTitle("消费记录 - " + cardInfo.getCardId());

            VBox root = new VBox(10);
            root.setPadding(new Insets(15));

            Label titleLabel = new Label("最近20条消费记录");
            titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

            TableView<CardConsumption> table = new TableView<>();

            // 创建表格列
            TableColumn<CardConsumption, String> amountCol = new TableColumn<>("金额");
            amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

            TableColumn<CardConsumption, String> locationCol = new TableColumn<>("地点");
            locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));

            TableColumn<CardConsumption, String> typeCol = new TableColumn<>("类型");
            typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

            TableColumn<CardConsumption, Date> timeCol = new TableColumn<>("时间");
            timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));

            table.getColumns().addAll(amountCol, locationCol, typeCol, timeCol);
            table.getItems().addAll(consumptions);

            root.getChildren().addAll(titleLabel, table);

            Scene scene = new Scene(root, 600, 400);
            consumptionStage.setScene(scene);
            consumptionStage.show();
        } else {
            showAlert(Alert.AlertType.ERROR, "错误", "无法获取一卡通信息");
        }
    }

    // 显示挂失对话框
    private void showReportLossDialog() {
        CardInfo cardInfo = clientService.getCardInfo(currentUser.getUserId());
        if (cardInfo != null) {
            if ("LOST".equals(cardInfo.getStatus())) {
                showAlert(Alert.AlertType.INFORMATION, "提示", "一卡通已处于挂失状态");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("一卡通挂失");
            alert.setHeaderText("确认挂失一卡通？");
            alert.setContentText("挂失后一卡通将无法使用，如需解挂请联系管理员。");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = clientService.reportCardLoss(cardInfo.getCardId());
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "成功", "一卡通挂失成功");
                    refreshCardInfo(); // 刷新一卡通信息
                } else {
                    showAlert(Alert.AlertType.ERROR, "失败", "一卡通挂失失败，请稍后重试");
                }
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "错误", "无法获取一卡通信息");
        }
    }

    // 添加状态文本转换方法
    private String getStatusText(String status) {
        switch (status) {
            case "NORMAL": return "正常";
            case "LOST": return "已挂失";
            case "FROZEN": return "已冻结";
            default: return "未知";
        }
    }

    // 添加刷新一卡通信息的方法
    private void refreshCardInfo() {
        CardInfo cardInfo = clientService.getCardInfo(currentUser.getUserId());
        if (cardInfo != null) {
            // 更新余额显示
            balanceValue.setText("¥ " + String.format("%.2f", cardInfo.getBalance()));

            // 更新状态显示
            statusLabel.setText("状态: " + getStatusText(cardInfo.getStatus()));

            // 根据状态设置不同颜色
            if ("NORMAL".equals(cardInfo.getStatus())) {
                statusLabel.setTextFill(Color.web("#2ecc71"));
            } else if ("LOST".equals(cardInfo.getStatus())) {
                statusLabel.setTextFill(Color.web("#e74c3c"));
            } else {
                statusLabel.setTextFill(Color.web("#7f8c8d"));
            }

            showAlert(Alert.AlertType.INFORMATION, "刷新成功", "一卡通信息已更新");
        } else {
            showAlert(Alert.AlertType.ERROR, "错误", "无法获取一卡通信息");
        }
    }

    // 添加加载报修数据的方法
    private void loadRepairData() {
        Message request = new Message(Message.Type.REPAIR_GET_LIST, "");
        Message response = clientService.sendRequest(request);

        if (response.getCode() == Message.Code.SUCCESS) {
            repairRecords = CommonUtils.convertToGenericList(response.getData(), RepairRecord.class);
            System.out.println("Loaded " + repairRecords.size() + " repair records"); // 调试信息
        } else {
            System.err.println("获取报修列表失败: " + response.getData());
            repairRecords = new ArrayList<>(); // 确保不会为null
        }
    }

    /**
     * 加载缴费数据
     */
    private void loadPaymentData() {
        // 获取待缴费账单
        Message request = new Message(Message.Type.LIFE_PAYMENT_GET_BILLS, "待支付");
        Message response = clientService.sendRequest(request);

        if (response.getCode() == Message.Code.SUCCESS) {
            pendingBills = CommonUtils.convertToGenericList(response.getData(), LifePaymentBill.class);
        } else {
            System.err.println("获取待缴费账单失败: " + response.getData());
            pendingBills = new ArrayList<>();
        }

        // 获取缴费记录
        Message recordRequest = new Message(Message.Type.LIFE_PAYMENT_GET_RECORDS, 10);
        Message recordResponse = clientService.sendRequest(recordRequest);

        if (recordResponse.getCode() == Message.Code.SUCCESS) {
            paymentRecords = CommonUtils.convertToGenericList(recordResponse.getData(), LifePaymentRecord.class);
        } else {
            System.err.println("获取缴费记录失败: " + recordResponse.getData());
            paymentRecords = new ArrayList<>();
        }
    }

    /**
     * 刷新缴费UI
     */
    private void refreshPaymentUI() {
        // 重新加载数据
        loadPaymentData();

        // 更新统计栏
        HBox newStatsContainer = createPaymentStats();

        // 获取ScrollPane及其内容
        ScrollPane scrollPane = (ScrollPane) paymentServiceContainer.getChildren().get(1);
        VBox mainContainer = (VBox) scrollPane.getContent();

        // 找到并替换现有的统计栏
        HBox oldStatsContainer = (HBox) mainContainer.getChildren().get(0);
        mainContainer.getChildren().set(0, newStatsContainer);

        // 刷新待缴费列表
        VBox pendingPayments = createPendingPayments();
        mainContainer.getChildren().set(2, pendingPayments);

        // 刷新缴费记录
        TabPane tabPane = (TabPane) mainContainer.getChildren().get(6);
        Tab currentMonthTab = tabPane.getTabs().get(0);
        Tab historyTab = tabPane.getTabs().get(1);

        currentMonthTab.setContent(createPaymentHistory(true));
        historyTab.setContent(createPaymentHistory(false));
    }

    /**
     * 显示支付对话框
     */
    private void showPaymentDialog(LifePaymentBill bill) {
        // 创建对话框
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("支付确认");
        dialog.setHeaderText("请确认支付信息");

        // 设置对话框按钮
        ButtonType payButtonType = new ButtonType("确认支付", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(payButtonType, ButtonType.CANCEL);

        // 创建表单布局
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        // 显示支付信息
        grid.add(new Label("缴费类型:"), 0, 0);
        grid.add(new Label(bill.getBillType()), 1, 0);

        grid.add(new Label("缴费金额:"), 0, 1);
        grid.add(new Label("¥" + String.format("%.2f", bill.getAmount())), 1, 1);

        grid.add(new Label("截止日期:"), 0, 2);
        grid.add(new Label(bill.getDueDate().toString()), 1, 2);

        // 支付方式选择
        ComboBox<String> paymentMethod = new ComboBox<>();
        paymentMethod.getItems().addAll("一卡通支付", "银行卡支付", "支付宝", "微信支付");
        paymentMethod.setValue("一卡通支付");

        grid.add(new Label("支付方式:"), 0, 3);
        grid.add(paymentMethod, 1, 3);

        // 设置对话框内容
        dialog.getDialogPane().setContent(grid);

        // 设置支付按钮的处理逻辑
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == payButtonType) {
                // 发送支付请求
                Object[] params = {bill.getBillId(), paymentMethod.getValue()};
                Message request = new Message(Message.Type.LIFE_PAYMENT_PAY, params);
                Message response = clientService.sendRequest(request);

                if (response.getCode() == Message.Code.SUCCESS) {
                    showAlert(Alert.AlertType.INFORMATION, "支付成功", "账单支付成功");

                    // 刷新UI
                    Platform.runLater(() -> {
                        loadPaymentData();
                        refreshPaymentUI();
                    });
                } else {
                    showAlert(Alert.AlertType.ERROR, "支付失败", "支付失败: " + response.getData());
                }
            }
            return null;
        });

        // 显示对话框
        dialog.showAndWait();
    }

    // 添加显示提示框的方法
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


    private void hideAllServices() {
        // 隐藏一卡通服务容器
        if (cardServiceContainer.isVisible()) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), cardServiceContainer);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                cardServiceContainer.setVisible(false);
                this.setCenter(null);

                // 恢复原始内容
                this.setCenter(buttonContainer);
                this.setBottom(activityContainer);
            });
            fadeOut.play();
        }

        // 隐藏后勤报修服务容器
        if (repairServiceContainer.isVisible()) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), repairServiceContainer);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                repairServiceContainer.setVisible(false);
                this.setCenter(null);

                // 恢复原始内容
                this.setCenter(buttonContainer);
                this.setBottom(activityContainer);
            });
            fadeOut.play();
        }

        // 隐藏生活缴费服务容器
        if (paymentServiceContainer.isVisible()) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), paymentServiceContainer);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                paymentServiceContainer.setVisible(false);
                this.setCenter(null);

                // 恢复原始内容
                this.setCenter(buttonContainer);
                this.setBottom(activityContainer);
            });
            fadeOut.play();
        }
    }


    // 缴费项目数据类
    private static class PaymentItem {
        String type;
        String date;
        String amount;
        String location;
        String status;

        PaymentItem(String type, String date, String amount, String location, String status) {
            this.type = type;
            this.date = date;
            this.amount = amount;
            this.location = location;
            this.status = status;
        }
    }

    /**
     * 活动项内部类
     */
    private class ActivityItem {
        private String type;
        private String content;
        private String time;

        public ActivityItem(String type, String content, String time) {
            this.type = type;
            this.content = content;
            this.time = time;
        }

        public String getType() { return type; }
        public String getContent() { return content; }
        public String getTime() { return time; }
    }
}