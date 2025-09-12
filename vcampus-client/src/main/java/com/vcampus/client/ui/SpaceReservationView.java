package com.vcampus.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import com.vcampus.common.entity.User;
import com.vcampus.client.service.ClientService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 改进的空间预约系统主界面
 */
public class SpaceReservationView {
    private User currentUser;
    private ClientService clientService;
    private BorderPane mainLayout;

    // 界面组件
    private VBox leftNavigation;
    private ScrollPane centerContent;
    private VBox rightDashboard;
    private String selectedCategory = "";
    private StackPane contentStack; // 用于页面切换

    // 模拟数据
    private Map<String, List<String>> categoryData;
    private List<Map<String, String>> announcements;
    private Map<String, Integer> reservationStats;

    public SpaceReservationView(User user, ClientService service) {
        this.currentUser = user;
        this.clientService = service;
        initializeData();
    }

    /**
     * 初始化模拟数据
     */
    private void initializeData() {
        // 分类数据
        categoryData = new HashMap<>();
        categoryData.put("体育馆", Arrays.asList("游泳馆", "羽毛球馆", "篮球馆", "健身房", "乒乓球馆"));
        categoryData.put("教学楼", Arrays.asList("多媒体教室", "计算机实验室", "语音室", "会议室", "报告厅"));
        categoryData.put("图书馆", Arrays.asList("研讨室", "自习室", "会议室", "报告厅", "展示厅"));
        categoryData.put("其他", Arrays.asList("学生活动中心", "艺术中心", "创客空间", "咖啡厅"));

        // 公告数据
        announcements = new ArrayList<>();
        Map<String, String> announcement1 = new HashMap<>();
        announcement1.put("title", "关于调整体育馆开放时间的通知");
        announcement1.put("date", "2023-09-28");
        announcements.add(announcement1);

        Map<String, String> announcement2 = new HashMap<>();
        announcement2.put("title", "图书馆研讨室预约规则更新");
        announcement2.put("date", "2023-09-25");
        announcements.add(announcement2);

        Map<String, String> announcement3 = new HashMap<>();
        announcement3.put("title", "自习室临时关闭通知");
        announcement3.put("date", "2023-09-20");
        announcements.add(announcement3);

        Map<String, String> announcement4 = new HashMap<>();
        announcement4.put("title", "智慧教室使用指南");
        announcement4.put("date", "2023-09-15");
        announcements.add(announcement4);

        // 预约统计数据
        reservationStats = new HashMap<>();
        reservationStats.put("待审批", 2);
        reservationStats.put("待赴约", 2);
        reservationStats.put("已完成", 1);
        reservationStats.put("违约", 1);
    }

    /**
     * 创建主界面布局
     */
    public BorderPane createLayout() {
        mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("space-reservation-root");
        // 添加半透明白色背景
        mainLayout.setStyle("-fx-background-color: rgba(255, 255, 255, 0.85);");

        // 顶部标题栏
        HBox headerBar = createHeaderBar();
        mainLayout.setTop(headerBar);

        // 创建主要内容区域
        HBox contentArea = new HBox();
        contentArea.setSpacing(0);

        // 左侧导航栏
        leftNavigation = createLeftNavigation();

        // 中间内容区域 - 使用StackPane支持页面切换
        contentStack = new StackPane();
        contentStack.setPrefWidth(500);
        centerContent = createCenterContent();
        contentStack.getChildren().add(centerContent);

        // 右侧仪表盘
        rightDashboard = createRightDashboard();

        contentArea.getChildren().addAll(leftNavigation, contentStack, rightDashboard);
        mainLayout.setCenter(contentArea);

        return mainLayout;
    }

    /**
     * 创建顶部标题栏
     */
    private HBox createHeaderBar() {
        HBox headerBar = new HBox();
        headerBar.getStyleClass().add("space-header-bar");
        headerBar.setPadding(new Insets(20, 30, 20, 30));
        headerBar.setAlignment(Pos.CENTER_LEFT);
        headerBar.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9);");

        Label titleLabel = new Label("🏢 空间预约系统");
        titleLabel.getStyleClass().add("space-header-title");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.valueOf("#2c3e50"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label timeLabel = new Label("📅 " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        timeLabel.getStyleClass().add("space-time-label");
        timeLabel.setFont(Font.font("Microsoft YaHei", 12));
        timeLabel.setTextFill(Color.valueOf("#7f8c8d"));

        headerBar.getChildren().addAll(titleLabel, spacer, timeLabel);
        return headerBar;
    }

    /**
     * 创建左侧导航栏
     */
    private VBox createLeftNavigation() {
        VBox navigation = new VBox();
        navigation.getStyleClass().add("space-left-nav");
        navigation.setPrefWidth(200);
        navigation.setPadding(new Insets(20));
        navigation.setSpacing(5);
        navigation.setStyle("-fx-background-color: rgba(52, 73, 94, 0.9);");

        // 创建一级导航按钮和二级导航容器
        for (String category : categoryData.keySet()) {
            VBox categorySection = createCategorySection(category);
            navigation.getChildren().add(categorySection);
        }

        return navigation;
    }

    /**
     * 创建分类区域（包含一级按钮和二级导航）
     */
    private VBox createCategorySection(String category) {
        VBox section = new VBox();
        section.setSpacing(3);

        // 一级导航按钮
        Button mainButton = createNavButton(category);
        section.getChildren().add(mainButton);

        // 二级导航容器（初始隐藏）
        VBox subNavContainer = new VBox();
        subNavContainer.setSpacing(3);
        subNavContainer.setVisible(false);
        subNavContainer.setManaged(false);

        // 创建二级导航按钮
        if (categoryData.containsKey(category)) {
            List<String> subCategories = categoryData.get(category);
            for (String subCategory : subCategories) {
                Button subButton = createSubNavButton(subCategory);
                subNavContainer.getChildren().add(subButton);
            }
        }

        section.getChildren().add(subNavContainer);

        // 点击一级按钮展开/收起二级导航
        mainButton.setOnAction(e -> {
            // 隐藏其他分类的二级导航
            hideAllSubNavigation();

            selectedCategory = category;

            // 切换当前分类的二级导航显示状态
            boolean isVisible = subNavContainer.isVisible();
            subNavContainer.setVisible(!isVisible);
            subNavContainer.setManaged(!isVisible);

            // 更新主内容区域
            updateCenterContent(category, "");
            updateNavButtonStyles(mainButton);
        });

        return section;
    }

    /**
     * 隐藏所有二级导航
     */
    private void hideAllSubNavigation() {
        for (javafx.scene.Node node : leftNavigation.getChildren()) {
            if (node instanceof VBox) {
                VBox section = (VBox) node;
                if (section.getChildren().size() > 1) {
                    VBox subNav = (VBox) section.getChildren().get(1);
                    subNav.setVisible(false);
                    subNav.setManaged(false);
                }
            }
        }
    }

    /**
     * 创建导航按钮
     */
    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("space-nav-button");
        button.setPrefWidth(160);
        button.setPrefHeight(40);
        button.setFont(Font.font("Microsoft YaHei", FontWeight.MEDIUM, 14));
        button.setAlignment(Pos.CENTER_LEFT);

        // 美化按钮样式
        button.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: rgba(255, 255, 255, 0.9);" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 8 15;"
        );

        // 添加悬停效果
        button.setOnMouseEntered(e -> {
            if (!button.getStyleClass().contains("active")) {
                button.setStyle(
                        "-fx-background-color: rgba(255, 255, 255, 0.1);" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 8;" +
                                "-fx-border-radius: 8;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 8 15;"
                );
            }
        });

        button.setOnMouseExited(e -> {
            if (!button.getStyleClass().contains("active")) {
                button.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: rgba(255, 255, 255, 0.9);" +
                                "-fx-background-radius: 8;" +
                                "-fx-border-radius: 8;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 8 15;"
                );
            }
        });

        return button;
    }

    /**
     * 创建二级导航按钮
     */
    private Button createSubNavButton(String text) {
        Button button = new Button("  • " + text);
        button.getStyleClass().add("space-sub-nav-button");
        button.setPrefWidth(160);
        button.setPrefHeight(35);
        button.setFont(Font.font("Microsoft YaHei", 12));
        button.setAlignment(Pos.CENTER_LEFT);

        // 美化二级按钮样式
        button.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: rgba(255, 255, 255, 0.7);" +
                        "-fx-background-radius: 6;" +
                        "-fx-border-radius: 6;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 6 15;"
        );

        // 悬停效果
        button.setOnMouseEntered(e -> {
            button.setStyle(
                    "-fx-background-color: rgba(255, 255, 255, 0.08);" +
                            "-fx-text-fill: rgba(255, 255, 255, 0.95);" +
                            "-fx-background-radius: 6;" +
                            "-fx-border-radius: 6;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 6 15;"
            );
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: rgba(255, 255, 255, 0.7);" +
                            "-fx-background-radius: 6;" +
                            "-fx-border-radius: 6;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 6 15;"
            );
        });

        button.setOnAction(e -> {
            // 切换到子类别页面
            showSubCategoryPage(selectedCategory, text);
        });

        return button;
    }

    /**
     * 显示子类别页面
     */
    private void showSubCategoryPage(String category, String subCategory) {
        VBox subPage = new VBox(20);
        subPage.setPadding(new Insets(30));
        subPage.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95);");

        // 返回按钮
        Button backButton = new Button("← 返回空间预约主页");
        backButton.setFont(Font.font("Microsoft YaHei", FontWeight.MEDIUM, 14));
        backButton.setStyle(
                "-fx-background-color: #3498db;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 6;" +
                        "-fx-border-radius: 6;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 8 16;"
        );
        backButton.setOnAction(e -> showMainPage());

        // 页面标题
        Label titleLabel = new Label(category + " - " + subCategory);
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.valueOf("#2c3e50"));

        // 页面内容
        Label contentLabel = new Label("这里将显示 " + subCategory + " 的具体空间列表和预约功能");
        contentLabel.setFont(Font.font("Microsoft YaHei", 14));
        contentLabel.setTextFill(Color.valueOf("#7f8c8d"));

        // 模拟一些空间卡片
        GridPane spaceGrid = createSpaceGrid(category, subCategory);

        subPage.getChildren().addAll(backButton, titleLabel, contentLabel, spaceGrid);

        // 切换到子页面
        contentStack.getChildren().clear();
        contentStack.getChildren().add(subPage);
    }

    /**
     * 创建空间网格
     */
    private GridPane createSpaceGrid(String category, String subCategory) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 0, 0, 0));

        // 模拟一些空间数据
        String[] spaceNames = {
                subCategory + "A区", subCategory + "B区",
                subCategory + "1号", subCategory + "2号"
        };

        int col = 0, row = 0;
        for (String spaceName : spaceNames) {
            VBox spaceCard = createSpaceCard(spaceName, category);
            grid.add(spaceCard, col, row);

            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }

        return grid;
    }

    /**
     * 创建空间卡片
     */
    private VBox createSpaceCard(String spaceName, String category) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefSize(180, 120);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);" +
                        "-fx-cursor: hand;"
        );

        Label nameLabel = new Label(spaceName);
        nameLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        nameLabel.setTextFill(Color.valueOf("#2c3e50"));

        Label statusLabel = new Label("可预约");
        statusLabel.setFont(Font.font("Microsoft YaHei", 12));
        statusLabel.setTextFill(Color.valueOf("#27ae60"));

        Button reserveButton = new Button("立即预约");
        reserveButton.setStyle(
                "-fx-background-color: #3498db;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 5;" +
                        "-fx-border-radius: 5;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 12;"
        );

        card.getChildren().addAll(nameLabel, statusLabel, reserveButton);

        reserveButton.setOnAction(e -> {
            // 创建时间轴组件
            ScheduleComponent scheduleComponent = new ScheduleComponent(
                    spaceName,
                    this::showMainPage  // 返回回调
            );

            // 创建时间轴界面
            HBox scheduleInterface = scheduleComponent.createScheduleInterface();

            // 替换中间和右侧内容
            contentStack.getChildren().clear();
            contentStack.getChildren().add(scheduleInterface);
        });

        // 悬停效果
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: #f8f9fa;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-radius: 10;" +
                            "-fx-border-color: #3498db;" +
                            "-fx-border-width: 2;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 4);" +
                            "-fx-cursor: hand;"
            );
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-radius: 10;" +
                            "-fx-border-color: #e0e0e0;" +
                            "-fx-border-width: 1;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);" +
                            "-fx-cursor: hand;"
            );
        });

        return card;
    }

    /**
     * 显示主页面
     */
    private void showMainPage() {
        contentStack.getChildren().clear();
        centerContent = createCenterContent();
        contentStack.getChildren().add(centerContent);
    }

    /**
     * 更新导航按钮样式
     */
    private void updateNavButtonStyles(Button activeButton) {
        // 重置所有按钮样式
        resetAllNavButtonStyles();

        // 设置选中按钮样式
        activeButton.getStyleClass().add("active");
        activeButton.setStyle(
                "-fx-background-color: rgba(103, 126, 234, 0.4);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 8 15;"
        );
    }

    /**
     * 重置所有导航按钮样式
     */
    private void resetAllNavButtonStyles() {
        resetNavButtonsInContainer(leftNavigation);
    }

    private void resetNavButtonsInContainer(VBox container) {
        for (javafx.scene.Node node : container.getChildren()) {
            if (node instanceof VBox) {
                VBox section = (VBox) node;
                for (javafx.scene.Node sectionNode : section.getChildren()) {
                    if (sectionNode instanceof Button) {
                        Button button = (Button) sectionNode;
                        button.getStyleClass().removeAll("active");
                        button.setStyle(
                                "-fx-background-color: transparent;" +
                                        "-fx-text-fill: rgba(255, 255, 255, 0.9);" +
                                        "-fx-background-radius: 8;" +
                                        "-fx-border-radius: 8;" +
                                        "-fx-cursor: hand;" +
                                        "-fx-padding: 8 15;"
                        );
                    }
                }
            }
        }
    }

    /**
     * 创建中间内容区域
     */
    private ScrollPane createCenterContent() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("space-center-content");
        scrollPane.setPrefWidth(500);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9);");

        VBox content = createAnnouncementContent();
        scrollPane.setContent(content);

        return scrollPane;
    }

    /**
     * 创建公告内容
     */
    private VBox createAnnouncementContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        Label titleLabel = new Label("校内通知与预约规则");
        titleLabel.getStyleClass().add("space-content-title");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.valueOf("#2c3e50"));

        content.getChildren().add(titleLabel);

        // 添加公告列表
        for (Map<String, String> announcement : announcements) {
            VBox announcementCard = createAnnouncementCard(
                    announcement.get("title"),
                    announcement.get("date")
            );
            content.getChildren().add(announcementCard);
        }

        return content;
    }

    /**
     * 创建公告卡片
     */
    private VBox createAnnouncementCard(String title, String date) {
        VBox card = new VBox(10);
        card.getStyleClass().add("space-announcement-card");
        card.setPadding(new Insets(15, 20, 15, 20));
        card.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.95);" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-color: rgba(52, 73, 94, 0.1);" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);" +
                        "-fx-cursor: hand;"
        );

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setSpacing(10);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.MEDIUM, 14));
        titleLabel.setTextFill(Color.valueOf("#2c3e50"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dateLabel = new Label(date);
        dateLabel.setFont(Font.font("Microsoft YaHei", 12));
        dateLabel.setTextFill(Color.valueOf("#95a5a6"));

        headerBox.getChildren().addAll(titleLabel, spacer, dateLabel);
        card.getChildren().add(headerBox);

        // 悬停效果
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: rgba(103, 126, 234, 0.02);" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-radius: 8;" +
                            "-fx-border-color: rgba(52, 73, 94, 0.1);" +
                            "-fx-border-width: 1;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);" +
                            "-fx-cursor: hand;"
            );
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: rgba(255, 255, 255, 0.95);" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-radius: 8;" +
                            "-fx-border-color: rgba(52, 73, 94, 0.1);" +
                            "-fx-border-width: 1;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);" +
                            "-fx-cursor: hand;"
            );
        });

        return card;
    }

    /**
     * 更新中间内容
     */
    private void updateCenterContent(String category, String subCategory) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        String contentTitle = category;
        if (!subCategory.isEmpty()) {
            contentTitle += " - " + subCategory;
        }

        Label titleLabel = new Label(contentTitle + " 空间列表");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.valueOf("#2c3e50"));

        content.getChildren().add(titleLabel);

        // 这里可以添加具体的空间列表内容
        Label placeholderLabel = new Label("空间列表内容将在这里显示...\n（后端接口完成后将显示实际数据）");
        placeholderLabel.setFont(Font.font("Microsoft YaHei", 14));
        placeholderLabel.setTextFill(Color.valueOf("#7f8c8d"));
        content.getChildren().add(placeholderLabel);

        ScrollPane newScrollPane = new ScrollPane(content);
        newScrollPane.setFitToWidth(true);
        newScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        newScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        newScrollPane.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9);");

        contentStack.getChildren().clear();
        contentStack.getChildren().add(newScrollPane);
    }

    /**
     * 创建右侧仪表盘
     */
    private VBox createRightDashboard() {
        VBox dashboard = new VBox(20);
        dashboard.getStyleClass().add("space-right-dashboard");
        dashboard.setPrefWidth(250);
        dashboard.setPadding(new Insets(30, 20, 30, 20));
        dashboard.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9);");

        Label titleLabel = new Label("我的预约");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.valueOf("#2c3e50"));

        dashboard.getChildren().add(titleLabel);

        // 添加统计卡片
        for (Map.Entry<String, Integer> entry : reservationStats.entrySet()) {
            VBox statCard = createStatCard(entry.getKey(), entry.getValue());
            dashboard.getChildren().add(statCard);
        }

        // 添加快速操作按钮
        VBox quickActions = createQuickActions();
        dashboard.getChildren().add(quickActions);

        return dashboard;
    }

    /**
     * 创建统计卡片
     */
    private VBox createStatCard(String title, int count) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.95);" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: rgba(52, 73, 94, 0.1);" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);" +
                        "-fx-cursor: hand;"
        );

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setSpacing(10);

        Label titleLabel = new Label(title + ":");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.MEDIUM, 14));
        titleLabel.setTextFill(Color.valueOf("#7f8c8d"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label countLabel = new Label(String.valueOf(count));
        countLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 20));

        // 根据状态设置不同颜色
        switch (title) {
            case "待审批":
                countLabel.setTextFill(Color.valueOf("#f39c12"));
                break;
            case "待赴约":
                countLabel.setTextFill(Color.valueOf("#3498db"));
                break;
            case "已完成":
                countLabel.setTextFill(Color.valueOf("#27ae60"));
                break;
            case "违约":
                countLabel.setTextFill(Color.valueOf("#e74c3c"));
                break;
            default:
                countLabel.setTextFill(Color.valueOf("#2c3e50"));
        }

        Label arrowLabel = new Label(">");
        arrowLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        arrowLabel.setTextFill(Color.valueOf("#bdc3c7"));

        headerBox.getChildren().addAll(titleLabel, spacer, countLabel, arrowLabel);
        card.getChildren().add(headerBox);

        // 添加点击事件
        card.setOnMouseClicked(e -> {
            System.out.println("点击查看: " + title + " 的详细信息");
            // TODO: 实现具体的跳转逻辑
        });

        // 悬停效果
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: rgba(103, 126, 234, 0.02);" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-radius: 10;" +
                            "-fx-border-color: rgba(52, 73, 94, 0.1);" +
                            "-fx-border-width: 1;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3);" +
                            "-fx-cursor: hand;"
            );
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: rgba(255, 255, 255, 0.95);" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-radius: 10;" +
                            "-fx-border-color: rgba(52, 73, 94, 0.1);" +
                            "-fx-border-width: 1;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);" +
                            "-fx-cursor: hand;"
            );
        });

        return card;
    }

    /**
     * 创建快速操作区域
     */
    private VBox createQuickActions() {
        VBox actions = new VBox(10);
        actions.setPadding(new Insets(20, 0, 0, 0));

        Label titleLabel = new Label("快速操作");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.valueOf("#2c3e50"));

        Button newReservationButton = new Button("📅 新建预约");
        newReservationButton.setPrefWidth(200);
        newReservationButton.setPrefHeight(40);
        newReservationButton.setFont(Font.font("Microsoft YaHei", FontWeight.MEDIUM, 13));
        newReservationButton.setStyle(
                "-fx-background-color: #3498db;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );
        newReservationButton.setOnAction(e -> {
            System.out.println("打开新建预约界面");
            // TODO: 实现新建预约功能
        });

        // 悬停效果
        newReservationButton.setOnMouseEntered(e -> {
            newReservationButton.setStyle(
                    "-fx-background-color: #2980b9;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-radius: 8;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 3);"
            );
        });

        newReservationButton.setOnMouseExited(e -> {
            newReservationButton.setStyle(
                    "-fx-background-color: #3498db;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-radius: 8;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
            );
        });

        Button myReservationsButton = new Button("📋 我的预约");
        myReservationsButton.setPrefWidth(200);
        myReservationsButton.setPrefHeight(40);
        myReservationsButton.setFont(Font.font("Microsoft YaHei", FontWeight.MEDIUM, 13));
        myReservationsButton.setStyle(
                "-fx-background-color: #27ae60;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );
        myReservationsButton.setOnAction(e -> {
            System.out.println("打开我的预约界面");
            // TODO: 实现我的预约功能
        });

        // 悬停效果
        myReservationsButton.setOnMouseEntered(e -> {
            myReservationsButton.setStyle(
                    "-fx-background-color: #229954;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-radius: 8;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 3);"
            );
        });

        myReservationsButton.setOnMouseExited(e -> {
            myReservationsButton.setStyle(
                    "-fx-background-color: #27ae60;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-radius: 8;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
            );
        });

        actions.getChildren().addAll(titleLabel, newReservationButton, myReservationsButton);
        return actions;
    }

    /**
     * 获取当前用户
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * 获取客户端服务
     */
    public ClientService getClientService() {
        return clientService;
    }
}