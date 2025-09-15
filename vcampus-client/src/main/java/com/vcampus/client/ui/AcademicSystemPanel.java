package com.vcampus.client.ui;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.vcampus.client.service.ClientService;
import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.User;

import java.util.*;

/**
 * 嵌入式教务系统管理面板
 */
public class AcademicSystemPanel {

    private ClientService clientService;
    private User currentUser;
    private StackPane contentArea;

    // 当前显示状态
    private String currentView = "main"; // main, announcement_categories, announcement_list, announcement_detail, files, file_list, upload
    private String currentCategory = ""; // 用于公告和文件分类，记录上一个分类以便返回

    // 色彩方案
    private static final String PRIMARY_COLOR = "#4a7c59";
    private static final String ACCENT_COLOR = "#6b9279";
    private static final String SUCCESS_COLOR = "#28a745";
    private static final String WARNING_COLOR = "#ffc107";
    private static final String ERROR_COLOR = "#dc3545";

    public AcademicSystemPanel(ClientService clientService, User currentUser, StackPane contentArea) {
        this.clientService = clientService;
        this.currentUser = currentUser;
        this.contentArea = contentArea;
        // 调试输出，用于检查 contentArea 是否为 null
        System.out.println("DEBUG: AcademicSystemPanel constructor called. contentArea is " + (contentArea == null ? "NULL" : "NOT NULL"));
    }

    /**
     * 显示教务系统主界面
     */
    public void showAcademicSystem() {
        // --- 修复 NPE 问题：添加防御性检查 ---
        if (this.contentArea == null) {
            System.err.println("ERROR: AcademicSystemPanel's contentArea is NULL. Cannot display content.");
            // 修正此处 showAlert 调用，补充 Alert.AlertType.ERROR
            showAlert("系统错误", "无法加载界面：主内容区域未初始化。请联系管理员。", Alert.AlertType.ERROR);
            return; // 阻止 NPE
        }
        // ------------------------------------

        currentView = "main"; // 设置当前视图状态
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30));

        // 标题
        Label titleLabel = new Label("教务管理系统");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 28)); // 字体改为System, 大小增加
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        // 功能卡片网格
        GridPane functionsGrid = createFunctionsGrid();

        // 最新公告区域（保持原样式）
        VBox announcementArea = createAnnouncementArea();

        mainContent.getChildren().addAll(titleLabel, functionsGrid, announcementArea);

        // 包装在ScrollPane中
        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f8f9fa;");

        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }

    /**
     * 创建最新公告区域（原来的样式）
     */
    private VBox createAnnouncementArea() {
        VBox area = new VBox(15);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("最新公告");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20)); // 字体改为System, 大小增加
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        Button viewAllButton = new Button("查看全部");
        viewAllButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 13px;"); // 字体大小微调
        viewAllButton.setOnAction(e -> showAnnouncementCategories());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(titleLabel, spacer, viewAllButton);

        // 公告列表
        VBox announcementList = new VBox(10);
        announcementList.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        // 加载公告数据
        loadRecentAnnouncements(announcementList);

        area.getChildren().addAll(header, announcementList);
        return area;
    }

    /**
     * 加载最新公告（原来的样式）
     */
    private void loadRecentAnnouncements(VBox container) {
        new Thread(() -> {
            try {
                Map<String, Object> params = new HashMap<>();
                params.put("targetAudience", currentUser.getRole().name());
                params.put("page", 1);
                params.put("pageSize", 5);

                Message response = clientService.request(Message.Type.ANNOUNCEMENT_LIST, params);

                Platform.runLater(() -> {
                    container.getChildren().clear();

                    if (response.getCode() == Message.Code.SUCCESS) {
                        Map<String, Object> result = (Map<String, Object>) response.getData();
                        List<Map<String, Object>> announcements = (List<Map<String, Object>>) result.get("announcements");

                        if (announcements.isEmpty()) {
                            Label emptyLabel = new Label("暂无公告");
                            emptyLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 13px;"); // 字体大小微调
                            container.getChildren().add(emptyLabel);
                        } else {
                            for (Map<String, Object> announcement : announcements) {
                                HBox item = createOriginalAnnouncementItem(announcement);
                                container.getChildren().add(item);
                            }
                        }
                    } else {
                        Label errorLabel = new Label("加载公告失败：" + response.getData());
                        errorLabel.setTextFill(Color.web(ERROR_COLOR));
                        container.getChildren().add(errorLabel);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label errorLabel = new Label("网络错误：" + e.getMessage());
                    errorLabel.setTextFill(Color.web(ERROR_COLOR));
                    container.getChildren().add(errorLabel);
                });
            }
        }).start();
    }

    /**
     * 创建原始样式的公告项目
     */
    private HBox createOriginalAnnouncementItem(Map<String, Object> announcement) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(10));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-cursor: hand;");

        VBox content = new VBox(5);
        HBox.setHgrow(content, Priority.ALWAYS);

        Label titleLabel = new Label((String) announcement.get("title"));
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 15)); // 字体改为System, 大小微调
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        Label infoLabel = new Label(announcement.get("category") + " | " + announcement.get("publishDate"));
        infoLabel.setFont(Font.font("System", 12)); // 字体改为System
        infoLabel.setTextFill(Color.web("#666666"));

        content.getChildren().addAll(titleLabel, infoLabel);

        Label priorityLabel = new Label((String) announcement.get("priority"));
        priorityLabel.setPadding(new Insets(2, 8, 2, 8));
        priorityLabel.setStyle("-fx-background-radius: 10; -fx-text-fill: white; -fx-font-size: 12;"); // 字体大小微调

        String priority = (String) announcement.get("priority");
        switch (priority) {
            case "紧急":
                priorityLabel.setStyle(priorityLabel.getStyle() + "-fx-background-color: " + ERROR_COLOR + ";");
                break;
            case "重要":
                priorityLabel.setStyle(priorityLabel.getStyle() + "-fx-background-color: " + WARNING_COLOR + ";");
                break;
            default:
                priorityLabel.setStyle(priorityLabel.getStyle() + "-fx-background-color: " + ACCENT_COLOR + ";");
        }

        item.getChildren().addAll(content, priorityLabel);

        item.setOnMouseClicked(e -> loadAnnouncementDetail((Integer) announcement.get("id")));

        return item;
    }

    /**
     * 创建功能卡片网格
     */
    private GridPane createFunctionsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);

        if (currentUser.isAdmin()) {
            // 管理员界面：发布公告、审批申请、文件上传、学籍管理
            grid.add(createFunctionCard("发布公告", "📢", "发布系统通知和公告", () -> showPublishAnnouncementDialog()), 0, 0);
            grid.add(createFunctionCard("审批申请", "✅", "审核学生提交的各类申请", () -> showApplicationManagement()), 1, 0);
            grid.add(createFunctionCard("文件上传", "📤", "上传各类文件和表格", () -> showFileUpload()), 0, 1);
            grid.add(createFunctionCard("学籍管理", "🎓", "管理学生学籍信息", () -> showRecordsManagement()), 1, 1);
        } else {
            // 学生和教师界面：教务公告、提交申请、文件下载、学籍管理
            grid.add(createFunctionCard("教务公告", "📢", "查看最新教务公告", () -> showAnnouncementCategories()), 0, 0);
            grid.add(createFunctionCard("提交申请", "📝", "提交各类教务申请", () -> showNewApplicationDialog()), 1, 0);
            grid.add(createFunctionCard("文件下载", "💾", "下载各类表格和文件", () -> showFileDownload()), 0, 1);
            grid.add(createFunctionCard("学籍管理", "👤", "查看和修改个人信息", () -> showRecordsManagement()), 1, 1);
        }

        return grid;
    }

    /**
     * 创建功能卡片
     */
    private VBox createFunctionCard(String title, String icon, String description, Runnable action) {
        VBox card = new VBox(10);
        card.setPrefSize(200, 120);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
                "-fx-cursor: hand;");

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(30)); // 图标字体大小增加

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 15)); // 字体改为System, 大小微调
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("System", 12)); // 字体改为System, 大小微调
        descLabel.setTextFill(Color.web("#666666"));
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);

        card.getChildren().addAll(iconLabel, titleLabel, descLabel);

        // 悬停效果
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; " +
                    "-fx-background-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 3); " +
                    "-fx-cursor: hand;");
            titleLabel.setTextFill(Color.WHITE);
            descLabel.setTextFill(Color.web("#e8f5e8"));
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; " +
                    "-fx-background-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
                    "-fx-cursor: hand;");
            titleLabel.setTextFill(Color.web(PRIMARY_COLOR));
            descLabel.setTextFill(Color.web("#666666"));
        });

        card.setOnMouseClicked(e -> action.run());

        return card;
    }

    // ============ 公告管理功能 ============

    /**
     * 显示公告分类页面（改为分区域布局）
     */
    private void showAnnouncementCategories() {
        currentView = "announcement_categories"; // 设置当前视图状态

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        // 标题和返回按钮
        HBox header = createHeaderWithBackButton("教务公告");

        // 创建六个分区域的网格布局
        GridPane categoriesGrid = new GridPane();
        categoriesGrid.setHgap(30);
        categoriesGrid.setVgap(30);
        categoriesGrid.setAlignment(Pos.TOP_CENTER);

        // 六个公告分区域，每行三个
        categoriesGrid.add(createAnnouncementSection("教务信息", "教务信息"), 0, 0);
        categoriesGrid.add(createAnnouncementSection("学籍管理通知", "学籍管理"), 1, 0);
        categoriesGrid.add(createAnnouncementSection("教学研究公告", "教学研究"), 2, 0);
        categoriesGrid.add(createAnnouncementSection("实践教学安排", "实践教学"), 0, 1);
        categoriesGrid.add(createAnnouncementSection("国际交流信息", "国际交流"), 1, 1);
        categoriesGrid.add(createAnnouncementSection("文化素质教育", "文化素质教育"), 2, 1);

        content.getChildren().addAll(header, categoriesGrid);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f8f9fa;");

        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }

    /**
     * 创建公告分区域（第二张图的样式）
     */
    private VBox createAnnouncementSection(String title, String category) {
        VBox section = new VBox(15);
        section.setPrefWidth(350);
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        // 区域标题和"查看更多"按钮
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 17)); // 字体改为System, 大小微调
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button moreButton = new Button("查看更多");
        moreButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white; " +
                "-fx-background-radius: 5; -fx-font-size: 13px; -fx-cursor: hand;"); // 字体大小微调
        moreButton.setOnAction(e -> showAnnouncementList(category));

        headerBox.getChildren().addAll(titleLabel, spacer, moreButton);

        // 分隔线
        Separator separator = new Separator();

        // 公告列表区域
        VBox announcementList = new VBox(10);
        announcementList.setMinHeight(200);

        // 加载该分类的公告
        loadAnnouncementsByCategory(announcementList, category, 5);

        section.getChildren().addAll(headerBox, separator, announcementList);

        return section;
    }

    /**
     * 加载指定分类的公告（用于分区域显示）
     */
    private void loadAnnouncementsByCategory(VBox container, String category, int limit) {
        new Thread(() -> {
            try {
                Map<String, Object> params = new HashMap<>();
                params.put("category", category);
                params.put("targetAudience", currentUser.getRole().name());
                params.put("page", 1);
                params.put("pageSize", limit);

                Message response = clientService.request(Message.Type.ANNOUNCEMENT_LIST, params);

                Platform.runLater(() -> {
                    container.getChildren().clear();

                    if (response.getCode() == Message.Code.SUCCESS) {
                        Map<String, Object> result = (Map<String, Object>) response.getData();
                        List<Map<String, Object>> announcements = (List<Map<String, Object>>) result.get("announcements");

                        if (announcements.isEmpty()) {
                            Label emptyLabel = new Label("暂无该分类公告");
                            emptyLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 13px;"); // 字体大小微调
                            container.getChildren().add(emptyLabel);
                        } else {
                            for (Map<String, Object> announcement : announcements) {
                                HBox item = createSectionAnnouncementItem(announcement);
                                container.getChildren().add(item);
                            }
                        }
                    } else {
                        Label errorLabel = new Label("加载失败");
                        errorLabel.setStyle("-fx-text-fill: " + ERROR_COLOR + "; -fx-font-size: 13px;"); // 字体大小微调
                        container.getChildren().add(errorLabel);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label errorLabel = new Label("网络错误");
                    errorLabel.setStyle("-fx-text-fill: " + ERROR_COLOR + "; -fx-font-size: 13px;"); // 字体大小微调
                    container.getChildren().add(errorLabel);
                });
            }
        }).start();
    }

    /**
     * 创建分区域内的公告项目（简化版）
     */
    private HBox createSectionAnnouncementItem(Map<String, Object> announcement) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(8, 0, 8, 0));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-cursor: hand;");

        // 公告标题
        Label titleLabel = new Label((String) announcement.get("title"));
        titleLabel.setFont(Font.font("System", 14)); // 字体改为System, 大小微调
        titleLabel.setTextFill(Color.web("#333333"));
        titleLabel.setMaxWidth(200);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 发布日期
        Label dateLabel = new Label(announcement.get("publishDate").toString().substring(5, 10)); // 只显示月-日
        dateLabel.setFont(Font.font("System", 13)); // 字体改为System, 大小微调
        dateLabel.setTextFill(Color.web("#666666"));

        item.getChildren().addAll(titleLabel, spacer, dateLabel);

        // 悬停效果
        item.setOnMouseEntered(e -> {
            item.setStyle("-fx-cursor: hand; -fx-background-color: #f0f0f0; -fx-background-radius: 3;");
        });

        item.setOnMouseExited(e -> {
            item.setStyle("-fx-cursor: hand; -fx-background-color: transparent;");
        });

        item.setOnMouseClicked(e -> loadAnnouncementDetail((Integer) announcement.get("id")));

        return item;
    }

    /**
     * 创建公告分类卡片 (这个方法在 showAnnouncementCategories() 中没有被使用，但在之前的版本中可能存在，为保持完整性保留)
     */
    private VBox createAnnouncementCategoryCard(String title, String icon, String description, String category) {
        VBox card = new VBox(10);
        card.setPrefSize(180, 150);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
                "-fx-cursor: hand;");

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(32)); // 图标字体大小增加

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 15)); // 字体改为System, 大小微调
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("System", 11)); // 字体改为System, 大小微调
        descLabel.setTextFill(Color.web("#666666"));
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);

        card.getChildren().addAll(iconLabel, titleLabel, descLabel);

        // 悬停效果
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: " + ACCENT_COLOR + "; " +
                    "-fx-background-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 3); " +
                    "-fx-cursor: hand;");
            titleLabel.setTextFill(Color.WHITE);
            descLabel.setTextFill(Color.web("#e8f5e8"));
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; " +
                    "-fx-background-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
                    "-fx-cursor: hand;");
            titleLabel.setTextFill(Color.web(PRIMARY_COLOR));
            descLabel.setTextFill(Color.web("#666666"));
        });

        card.setOnMouseClicked(e -> showAnnouncementList(category));

        return card;
    }

    /**
     * 显示指定分类的公告列表
     */
    private void showAnnouncementList(String category) {
        currentView = "announcement_list"; // 设置当前视图状态
        currentCategory = category; // 记录当前分类，以便返回详情后能回到正确的列表

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        // 标题和返回按钮
        HBox header = createHeaderWithBackButton(category + " 公告");

        // 公告列表容器
        VBox announcementList = new VBox(15);
        announcementList.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        // 加载公告数据
        loadAnnouncementsByCategory(announcementList, category);

        content.getChildren().addAll(header, announcementList);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f8f9fa;");

        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }

    /**
     * 显示公告详情
     */
    private void showAnnouncementDetail(Map<String, Object> announcement) {
        currentView = "announcement_detail"; // 设置当前视图状态

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        // 标题和返回按钮
        HBox header = createHeaderWithBackButton("公告详情");

        // 公告详情卡片
        VBox detailCard = new VBox(15);
        detailCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 30;");

        // 公告标题
        Label titleLabel = new Label((String) announcement.get("title"));
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 22)); // 字体改为System, 大小增加
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));
        titleLabel.setWrapText(true);

        // 公告信息
        HBox infoBox = new HBox(20);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label categoryLabel = new Label("分类：" + announcement.get("category"));
        categoryLabel.setFont(Font.font("System", 13)); // 字体改为System, 大小微调
        categoryLabel.setTextFill(Color.web("#666666"));

        Label dateLabel = new Label("发布时间：" + announcement.get("publishDate"));
        dateLabel.setFont(Font.font("System", 13)); // 字体改为System, 大小微调
        dateLabel.setTextFill(Color.web("#666666"));

        Label priorityLabel = new Label((String) announcement.get("priority"));
        priorityLabel.setPadding(new Insets(2, 8, 2, 8));
        priorityLabel.setStyle("-fx-background-radius: 10; -fx-text-fill: white; -fx-font-size: 12;"); // 字体大小微调

        String priority = (String) announcement.get("priority");
        switch (priority) {
            case "紧急":
                priorityLabel.setStyle(priorityLabel.getStyle() + "-fx-background-color: " + ERROR_COLOR + ";");
                break;
            case "重要":
                priorityLabel.setStyle(priorityLabel.getStyle() + "-fx-background-color: " + WARNING_COLOR + ";");
                break;
            default:
                priorityLabel.setStyle(priorityLabel.getStyle() + "-fx-background-color: " + ACCENT_COLOR + ";");
        }

        infoBox.getChildren().addAll(categoryLabel, dateLabel, priorityLabel);

        // 公告内容
        TextArea contentAreaTA = new TextArea((String) announcement.get("content"));
        contentAreaTA.setEditable(false);
        contentAreaTA.setWrapText(true);
        contentAreaTA.setPrefRowCount(10);
        contentAreaTA.setFont(Font.font("System", 13)); // 字体改为System, 大小微调
        contentAreaTA.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;");

        detailCard.getChildren().addAll(titleLabel, infoBox, new Separator(), contentAreaTA);
        content.getChildren().addAll(header, detailCard);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f8f9fa;");

        this.contentArea.getChildren().clear();
        this.contentArea.getChildren().add(scrollPane);
    }

    // ============ 文件管理功能 ============

    /**
     * 显示文件下载分类页面（改为分区域布局）
     */
    private void showFileDownload() {
        currentView = "files"; // 设置当前视图状态

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        // 标题和返回按钮
        HBox header = createHeaderWithBackButton("文件下载");

        // 创建四个分区域的网格布局
        GridPane categoriesGrid = new GridPane();
        categoriesGrid.setHgap(30);
        categoriesGrid.setVgap(30);
        categoriesGrid.setAlignment(Pos.TOP_CENTER);

        // 四个文件分区域，每行两个
        categoriesGrid.add(createFileSection("校历", "校历"), 0, 0);
        categoriesGrid.add(createFileSection("教务专区", "教务专区"), 1, 0);
        categoriesGrid.add(createFileSection("学籍专区", "学籍专区"), 0, 1);
        categoriesGrid.add(createFileSection("教室管理", "教室管理"), 1, 1);

        content.getChildren().addAll(header, categoriesGrid);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f8f9fa;");

        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }

    /**
     * 创建文件分区域（类似公告分区域的样式）
     */
    private VBox createFileSection(String title, String category) {
        VBox section = new VBox(15);
        section.setPrefWidth(400);
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        // 区域标题和"查看更多"按钮
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 17)); // 字体改为System, 大小微调
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button moreButton = new Button("查看更多");
        moreButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white; " +
                "-fx-background-radius: 5; -fx-font-size: 13px; -fx-cursor: hand;"); // 字体大小微调
        moreButton.setOnAction(e -> showFileList(category));

        headerBox.getChildren().addAll(titleLabel, spacer, moreButton);

        // 分隔线
        Separator separator = new Separator();

        // 文件列表区域
        VBox fileList = new VBox(10);
        fileList.setMinHeight(200);

        // 加载该分类的文件
        loadFilesByCategoryForSection(fileList, category, 5);

        section.getChildren().addAll(headerBox, separator, fileList);

        return section;
    }

    /**
     * 加载指定分类的文件（用于分区域显示）
     */
    private void loadFilesByCategoryForSection(VBox container, String category, int limit) {
        new Thread(() -> {
            try {
                Map<String, Object> params = new HashMap<>();
                params.put("category", category);

                Message response = clientService.request(Message.Type.FILE_LIST, params);

                Platform.runLater(() -> {
                    container.getChildren().clear();

                    if (response.getCode() == Message.Code.SUCCESS) {
                        Map<String, Object> result = (Map<String, Object>) response.getData();
                        List<Map<String, Object>> files = (List<Map<String, Object>>) result.get("files");

                        if (files.isEmpty()) {
                            Label emptyLabel = new Label("暂无该分类文件");
                            emptyLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 13px;"); // 字体大小微调
                            container.getChildren().add(emptyLabel);
                        } else {
                            // 限制显示数量
                            int displayCount = Math.min(files.size(), limit);
                            for (int i = 0; i < displayCount; i++) {
                                HBox item = createSectionFileItem(files.get(i));
                                container.getChildren().add(item);
                            }
                        }
                    } else {
                        Label errorLabel = new Label("加载失败");
                        errorLabel.setStyle("-fx-text-fill: " + ERROR_COLOR + "; -fx-font-size: 13px;"); // 字体大小微调
                        container.getChildren().add(errorLabel);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label errorLabel = new Label("网络错误");
                    errorLabel.setStyle("-fx-text-fill: " + ERROR_COLOR + "; -fx-font-size: 13px;"); // 字体大小微调
                    container.getChildren().add(errorLabel);
                });
            }
        }).start();
    }

    /**
     * 创建分区域内的文件项目（简化版）
     */
    private HBox createSectionFileItem(Map<String, Object> file) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(8, 0, 8, 0));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-cursor: hand;");

        // 文件图标
        Label iconLabel = new Label("📄");
        iconLabel.setFont(Font.font(16)); // 图标字体大小增加

        // 文件名
        Label nameLabel = new Label((String) file.get("originalName"));
        nameLabel.setFont(Font.font("System", 14)); // 字体改为System, 大小微调
        nameLabel.setTextFill(Color.web("#333333"));
        nameLabel.setMaxWidth(250);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 下载按钮
        Button downloadBtn = new Button("下载");
        downloadBtn.setStyle("-fx-background-color: " + SUCCESS_COLOR + "; -fx-text-fill: white; " +
                "-fx-background-radius: 3; -fx-font-size: 12px; -fx-cursor: hand; -fx-pref-width: 50;"); // 字体大小微调
        downloadBtn.setOnAction(e -> {
            e.consume(); // 阻止事件冒泡
            downloadFile((Integer) file.get("id"));
        });

        item.getChildren().addAll(iconLabel, nameLabel, spacer, downloadBtn);

        // 悬停效果
        item.setOnMouseEntered(e -> {
            if (e.getTarget() != downloadBtn) {
                item.setStyle("-fx-cursor: hand; -fx-background-color: #f0f0f0; -fx-background-radius: 3;");
            }
        });

        item.setOnMouseExited(e -> {
            item.setStyle("-fx-cursor: hand; -fx-background-color: transparent;");
        });

        return item;
    }

    /**
     * 创建文件分类卡片 (这个方法在 showFileDownload() 中没有被使用，但在之前的版本中可能存在，为保持完整性保留)
     */
    private VBox createFileCategoryCard(String title, String icon, String description, String category) {
        VBox card = new VBox(10);
        card.setPrefSize(200, 150);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
                "-fx-cursor: hand;");

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(32)); // 图标字体大小增加

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 15)); // 字体改为System, 大小微调
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("System", 11)); // 字体改为System, 大小微调
        descLabel.setTextFill(Color.web("#666666"));
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);

        card.getChildren().addAll(iconLabel, titleLabel, descLabel);

        // 悬停效果
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: " + ACCENT_COLOR + "; " +
                    "-fx-background-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 3); " +
                    "-fx-cursor: hand;");
            titleLabel.setTextFill(Color.WHITE);
            descLabel.setTextFill(Color.web("#e8f5e8"));
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; " +
                    "-fx-background-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
                    "-fx-cursor: hand;");
            titleLabel.setTextFill(Color.web(PRIMARY_COLOR));
            descLabel.setTextFill(Color.web("#666666"));
        });

        card.setOnMouseClicked(e -> showFileList(category));

        return card;
    }

    /**
     * 显示指定分类的文件列表
     */
    private void showFileList(String category) {
        currentView = "file_list"; // 设置当前视图状态
        currentCategory = category; // 记录当前分类，以便返回（虽然目前文件列表返回是回到文件分类，但保留以防将来需求变更）

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        // 标题和返回按钮
        HBox header = createHeaderWithBackButton(category + " - 文件下载");

        // 文件列表容器
        VBox fileList = new VBox(15);
        fileList.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        // 加载文件数据
        loadFilesByCategory(fileList, category);

        content.getChildren().addAll(header, fileList);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f8f9fa;");

        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }

    /**
     * 显示文件上传页面（管理员端）
     */
    private void showFileUpload() {
        currentView = "upload"; // 设置当前视图状态

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        // 标题和返回按钮
        HBox header = createHeaderWithBackButton("文件上传管理");

        // 上传区域
        VBox uploadArea = new VBox(20);
        uploadArea.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 30;");

        Label titleLabel = new Label("上传新文件");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20)); // 字体改为System, 大小增加
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        // 上传表单
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        // --- 修复文字截断问题：调整列约束 ---
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setHgrow(Priority.NEVER); // 标签列不自动增长
        labelCol.setHalignment(HPos.RIGHT); // 标签右对齐
        labelCol.setMinWidth(120); // 增加最小宽度，确保中文标签显示完整

        ColumnConstraints inputCol = new ColumnConstraints();
        inputCol.setHgrow(Priority.ALWAYS); // 输入框列自动增长
        inputCol.setMinWidth(250); // 输入框最小宽度

        form.getColumnConstraints().addAll(labelCol, inputCol);
        // ------------------------------------

        // 文件选择区域
        Label fileLabel = new Label("选择文件 *");
        fileLabel.setFont(Font.font("System", 14)); // 字体改为System
        HBox fileBox = new HBox(10);
        TextField filePathField = new TextField();
        filePathField.setPromptText("请选择要上传的文件");
        filePathField.setEditable(false);
        filePathField.setPrefHeight(35);
        filePathField.setFont(Font.font("System", 13)); // 字体改为System
        filePathField.setMaxWidth(Double.MAX_VALUE); // 确保文本框自动增长
        HBox.setHgrow(filePathField, Priority.ALWAYS); // 确保文本框在HBox中也自动增长

        Button browseButton = new Button("浏览文件");
        browseButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 13px;"); // 字体大小微调
        browseButton.setOnAction(e -> {
            // 模拟文件选择
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("选择要上传的文件");
            fileChooser.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("所有文件", "*.*"),
                    new javafx.stage.FileChooser.ExtensionFilter("PDF文件", "*.pdf"),
                    new javafx.stage.FileChooser.ExtensionFilter("Word文档", "*.doc", "*.docx"),
                    new javafx.stage.FileChooser.ExtensionFilter("Excel表格", "*.xls", "*.xlsx"),
                    new javafx.stage.FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.gif")
            );

            // 获取当前Stage
            Stage currentStage = (Stage) browseButton.getScene().getWindow();
            java.io.File selectedFile = fileChooser.showOpenDialog(currentStage);
            if (selectedFile != null) {
                filePathField.setText(selectedFile.getAbsolutePath());
            }
        });

        fileBox.getChildren().addAll(filePathField, browseButton);
        GridPane.setHgrow(fileBox, Priority.ALWAYS); // 确保HBox在GridPane中也自动增长

        // 文件分类
        Label categoryFileLabel = new Label("文件分类 *"); // 区分变量名
        categoryFileLabel.setFont(Font.font("System", 14)); // 字体改为System
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("校历", "教务专区", "学籍专区", "教室管理");
        categoryBox.setValue("校历");
        categoryBox.setPrefHeight(35);
        categoryBox.setMaxWidth(Double.MAX_VALUE); // 确保ComboBox自动增长
        categoryBox.setStyle("-fx-font-size: 13px;"); // 字体大小微调
        GridPane.setHgrow(categoryBox, Priority.ALWAYS); // 确保ComboBox在GridPane中也自动增长

        // 文件描述
        Label descLabel = new Label("文件描述");
        descLabel.setFont(Font.font("System", 14)); // 字体改为System
        TextArea descArea = new TextArea();
        descArea.setPromptText("请输入文件描述...");
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);
        descArea.setFont(Font.font("System", 13)); // 字体改为System
        descArea.setMaxWidth(Double.MAX_VALUE); // 确保TextArea自动增长
        GridPane.setHgrow(descArea, Priority.ALWAYS); // 确保TextArea在GridPane中也自动增长


        form.add(fileLabel, 0, 0);
        form.add(fileBox, 1, 0);
        form.add(categoryFileLabel, 0, 1); // 使用新的Label变量
        form.add(categoryBox, 1, 1);
        form.add(descLabel, 0, 2);
        form.add(descArea, 1, 2);

        // 按钮区域
        HBox buttonArea = new HBox(15);
        buttonArea.setAlignment(Pos.CENTER_RIGHT);

        Button uploadButton = new Button("上传文件");
        uploadButton.setStyle("-fx-background-color: " + SUCCESS_COLOR + "; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 13px;"); // 字体大小微调
        uploadButton.setOnAction(e -> {
            if (filePathField.getText().trim().isEmpty()) {
                showAlert("输入错误", "请选择要上传的文件", Alert.AlertType.WARNING);
                return;
            }
            // 模拟文件上传
            uploadFile(filePathField.getText(), categoryBox.getValue(), descArea.getText());
        });

        buttonArea.getChildren().add(uploadButton);

        uploadArea.getChildren().addAll(titleLabel, form, buttonArea);

        // 文件管理区域
        VBox managementArea = new VBox(15);
        managementArea.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        Label managementTitle = new Label("文件管理");
        managementTitle.setFont(Font.font("System", FontWeight.BOLD, 17)); // 字体改为System, 大小微调
        managementTitle.setTextFill(Color.web(PRIMARY_COLOR));

        // 文件列表
        VBox fileList = new VBox(10);
        loadAllFiles(fileList);

        managementArea.getChildren().addAll(managementTitle, new Separator(), fileList);

        content.getChildren().addAll(header, uploadArea, managementArea);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f8f9fa;");

        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }

    // ============ 通用功能方法 ============

    /**
     * 创建带返回按钮的标题
     */
    private HBox createHeaderWithBackButton(String title) {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Button backButton = new Button("← 返回");
        backButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white; " +
                "-fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 13px;"); // 字体大小微调
        backButton.setOnAction(e -> {
            // --- 修复公告返回无反应问题：根据 currentView 进行多层级返回 ---
            switch (currentView) {
                case "announcement_detail":
                    showAnnouncementList(currentCategory); // 从公告详情返回到特定公告列表
                    break;
                case "announcement_list":
                    showAnnouncementCategories(); // 从公告列表返回到公告分类页
                    break;
                case "announcement_categories":
                case "file_list": // 从文件列表返回到文件分类页 (如果需要返回到文件分类页面，需要有一个 showFileCategories() 方法)
                case "files": // 文件分类页
                case "upload": // 文件上传管理页
                    showAcademicSystem(); // 从分类页或文件管理页返回主界面
                    break;
                default:
                    showAcademicSystem(); // 默认返回主界面，以防万一
                    break;
            }
            // --------------------------------------------------------
        });

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20)); // 字体改为System, 大小增加
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        header.getChildren().addAll(backButton, titleLabel);
        return header;
    }

    /**
     * 显示发布公告对话框
     */
    private void showPublishAnnouncementDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("发布新公告");
        dialog.setWidth(600);
        dialog.setHeight(500);
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f8f9fa;");

        Label titleLabel = new Label("发布新公告");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20)); // 字体改为System, 大小增加
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        // 表单
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        // --- 修复文字截断问题：调整列约束 ---
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setHgrow(Priority.NEVER); // 标签列不自动增长
        labelCol.setHalignment(HPos.RIGHT); // 标签右对齐
        labelCol.setMinWidth(120); // 增加最小宽度，确保中文标签显示完整

        ColumnConstraints inputCol = new ColumnConstraints();
        inputCol.setHgrow(Priority.ALWAYS); // 输入框列自动增长
        inputCol.setMinWidth(250); // 输入框最小宽度

        form.getColumnConstraints().addAll(labelCol, inputCol);
        // ------------------------------------

        // 公告标题
        Label titleFieldLabel = new Label("公告标题 *");
        titleFieldLabel.setFont(Font.font("System", 14)); // 字体改为System
        TextField titleField = new TextField();
        titleField.setPromptText("请输入公告标题");
        titleField.setPrefHeight(35);
        titleField.setFont(Font.font("System", 13)); // 字体改为System
        titleField.setMaxWidth(Double.MAX_VALUE); // 确保文本框自动增长
        GridPane.setHgrow(titleField, Priority.ALWAYS); // 确保文本框在GridPane中自动增长

        // 公告分类
        Label categoryLabel = new Label("公告分类 *");
        categoryLabel.setFont(Font.font("System", 14)); // 字体改为System
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("教务信息", "学籍管理", "教学研究", "实践教学", "国际交流", "文化素质教育");
        categoryBox.setValue("教务信息");
        categoryBox.setPrefHeight(35);
        categoryBox.setMaxWidth(Double.MAX_VALUE); // 确保ComboBox自动增长
        categoryBox.setStyle("-fx-font-size: 13px;"); // 字体大小微调
        GridPane.setHgrow(categoryBox, Priority.ALWAYS); // 确保ComboBox在GridPane中自动增长

        // 优先级
        Label priorityLabel = new Label("优先级");
        priorityLabel.setFont(Font.font("System", 14)); // 字体改为System
        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("普通", "重要", "紧急");
        priorityBox.setValue("普通");
        priorityBox.setPrefHeight(35);
        priorityBox.setMaxWidth(Double.MAX_VALUE); // 确保ComboBox自动增长
        priorityBox.setStyle("-fx-font-size: 13px;"); // 字体大小微调
        GridPane.setHgrow(priorityBox, Priority.ALWAYS); // 确保ComboBox在GridPane中自动增长

        // 目标受众
        Label audienceLabel = new Label("目标受众");
        audienceLabel.setFont(Font.font("System", 14)); // 字体改为System
        ComboBox<String> audienceBox = new ComboBox<>();
        audienceBox.getItems().addAll("全体", "学生", "教师");
        audienceBox.setValue("全体");
        audienceBox.setPrefHeight(35);
        audienceBox.setMaxWidth(Double.MAX_VALUE); // 确保ComboBox自动增长
        audienceBox.setStyle("-fx-font-size: 13px;"); // 字体大小微调
        GridPane.setHgrow(audienceBox, Priority.ALWAYS); // 确保ComboBox在GridPane中自动增长

        // 公告内容
        Label contentLabel = new Label("公告内容 *");
        contentLabel.setFont(Font.font("System", 14)); // 字体改为System
        TextArea contentAreaDialog = new TextArea(); // 区分变量名
        contentAreaDialog.setPromptText("请输入公告内容...");
        contentAreaDialog.setPrefRowCount(8);
        contentAreaDialog.setWrapText(true);
        contentAreaDialog.setFont(Font.font("System", 13)); // 字体改为System
        contentAreaDialog.setMaxWidth(Double.MAX_VALUE); // 确保TextArea自动增长
        GridPane.setHgrow(contentAreaDialog, Priority.ALWAYS); // 确保TextArea在GridPane中自动增长

        form.add(titleFieldLabel, 0, 0);
        form.add(titleField, 1, 0);
        form.add(categoryLabel, 0, 1);
        form.add(categoryBox, 1, 1);
        form.add(priorityLabel, 0, 2);
        form.add(priorityBox, 1, 2);
        form.add(audienceLabel, 0, 3);
        form.add(audienceBox, 1, 3);
        form.add(contentLabel, 0, 4);
        form.add(contentAreaDialog, 1, 4);

        // 按钮
        HBox buttonArea = new HBox(15);
        buttonArea.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = new Button("取消");
        cancelButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 13px;"); // 字体大小微调
        cancelButton.setOnAction(e -> dialog.close());

        Button publishButton = new Button("发布公告");
        publishButton.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 13px;"); // 字体大小微调
        publishButton.setOnAction(e -> {
            String title = titleField.getText().trim();
            String content = contentAreaDialog.getText().trim(); // 使用新的变量名

            if (title.isEmpty() || content.isEmpty()) {
                showAlert("输入错误", "请填写公告标题和内容", Alert.AlertType.WARNING);
                return;
            }

            // 发布公告
            publishAnnouncement(title, content, categoryBox.getValue(), priorityBox.getValue(), audienceBox.getValue(), dialog);
        });

        buttonArea.getChildren().addAll(cancelButton, publishButton);

        root.getChildren().addAll(titleLabel, form, buttonArea);

        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.show();
    }

    /**
     * 显示新建申请对话框
     */
    private void showNewApplicationDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("新建申请");
        dialog.setWidth(650);
        dialog.setHeight(600);
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f8f9fa;");

        Label titleLabel = new Label("提交新申请");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20)); // 字体改为System, 大小增加
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        // 表单
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        // --- 修复文字截断问题：调整列约束 ---
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setHgrow(Priority.NEVER); // 标签列不自动增长
        labelCol.setHalignment(HPos.RIGHT); // 标签右对齐
        labelCol.setMinWidth(120); // 增加最小宽度，确保中文标签显示完整

        ColumnConstraints inputCol = new ColumnConstraints();
        inputCol.setHgrow(Priority.ALWAYS); // 输入框列自动增长
        inputCol.setMinWidth(250); // 输入框最小宽度

        form.getColumnConstraints().addAll(labelCol, inputCol);
        // ------------------------------------

        // 申请类型
        Label typeLabel = new Label("申请类型 *");
        typeLabel.setFont(Font.font("System", 14)); // 字体改为System
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("成绩证明", "在读证明", "学籍证明", "毕业证明", "转专业申请", "休学申请", "复学申请", "其他申请");
        typeBox.setValue("成绩证明");
        typeBox.setPrefHeight(35);
        typeBox.setMaxWidth(Double.MAX_VALUE); // 确保ComboBox自动增长
        typeBox.setStyle("-fx-font-size: 13px;"); // 字体大小微调
        GridPane.setHgrow(typeBox, Priority.ALWAYS); // 确保ComboBox在GridPane中也自动增长

        // 申请标题
        Label titleFieldLabel = new Label("申请标题 *");
        titleFieldLabel.setFont(Font.font("System", 14)); // 字体改为System
        TextField titleField = new TextField();
        titleField.setPromptText("请输入申请标题");
        titleField.setPrefHeight(35);
        titleField.setFont(Font.font("System", 13)); // 字体改为System
        titleField.setMaxWidth(Double.MAX_VALUE); // 确保文本框自动增长
        GridPane.setHgrow(titleField, Priority.ALWAYS); // 确保文本框在GridPane中也自动增长

        // 申请内容
        Label contentLabel = new Label("申请内容 *");
        contentLabel.setFont(Font.font("System", 14)); // 字体改为System
        TextArea contentAreaDialog = new TextArea(); // 区分变量名
        contentAreaDialog.setPromptText("请详细说明申请原因和具体情况...");
        contentAreaDialog.setPrefRowCount(10);
        contentAreaDialog.setWrapText(true);
        contentAreaDialog.setFont(Font.font("System", 13)); // 字体改为System
        contentAreaDialog.setMaxWidth(Double.MAX_VALUE); // 确保TextArea自动增长
        GridPane.setHgrow(contentAreaDialog, Priority.ALWAYS); // 确保TextArea在GridPane中也自动增长

        // 预填充标题
        typeBox.setOnAction(e -> {
            String selectedType = typeBox.getValue();
            if (titleField.getText().isEmpty() || titleField.getText().startsWith("申请")) {
                titleField.setText("申请" + selectedType);
            }
        });

        form.add(typeLabel, 0, 0);
        form.add(typeBox, 1, 0);
        form.add(titleFieldLabel, 0, 1);
        form.add(titleField, 1, 1);
        form.add(contentLabel, 0, 2);
        form.add(contentAreaDialog, 1, 2); // 使用新的变量名

        // 按钮
        HBox buttonArea = new HBox(15);
        buttonArea.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = new Button("取消");
        cancelButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 13px;"); // 字体大小微调
        cancelButton.setOnAction(e -> dialog.close());

        Button submitButton = new Button("提交申请");
        submitButton.setStyle("-fx-background-color: " + SUCCESS_COLOR + "; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 13px;"); // 字体大小微调
        submitButton.setOnAction(e -> {
            String type = typeBox.getValue();
            String title = titleField.getText().trim();
            String content = contentAreaDialog.getText().trim(); // 使用新的变量名

            if (title.isEmpty() || content.isEmpty()) {
                showAlert("输入错误", "请填写申请标题和内容", Alert.AlertType.WARNING);
                return;
            }

            if (content.length() < 20) {
                showAlert("内容太短", "申请内容至少需要20个字符", Alert.AlertType.WARNING);
                return;
            }

            // 提交申请
            submitApplication(type, title, content, dialog);
        });

        buttonArea.getChildren().addAll(cancelButton, submitButton);

        root.getChildren().addAll(titleLabel, form, buttonArea);

        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.show();
    }

    // ============ 数据加载方法 ============

    /**
     * 加载指定分类的公告
     */
    private void loadAnnouncementsByCategory(VBox container, String category) {
        new Thread(() -> {
            try {
                Map<String, Object> params = new HashMap<>();
                params.put("category", category);
                params.put("targetAudience", currentUser.getRole().name());
                params.put("page", 1);
                params.put("pageSize", 20);

                Message response = clientService.request(Message.Type.ANNOUNCEMENT_LIST, params);

                Platform.runLater(() -> {
                    container.getChildren().clear();

                    if (response.getCode() == Message.Code.SUCCESS) {
                        Map<String, Object> result = (Map<String, Object>) response.getData();
                        List<Map<String, Object>> announcements = (List<Map<String, Object>>) result.get("announcements");

                        if (announcements.isEmpty()) {
                            Label emptyLabel = new Label("暂无" + category + "相关公告");
                            emptyLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 15px;"); // 字体大小微调
                            emptyLabel.setPadding(new Insets(20));
                            container.getChildren().add(emptyLabel);
                        } else {
                            for (Map<String, Object> announcement : announcements) {
                                VBox item = createAnnouncementListItem(announcement);
                                container.getChildren().add(item);
                            }
                        }
                    } else {
                        Label errorLabel = new Label("加载公告失败：" + response.getData());
                        errorLabel.setTextFill(Color.web(ERROR_COLOR));
                        container.getChildren().add(errorLabel);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label errorLabel = new Label("网络错误：" + e.getMessage());
                    errorLabel.setTextFill(Color.web(ERROR_COLOR));
                    container.getChildren().add(errorLabel);
                });
            }
        }).start();
    }

    /**
     * 创建公告列表项
     */
    private VBox createAnnouncementListItem(Map<String, Object> announcement) {
        VBox item = new VBox(8);
        item.setPadding(new Insets(15));
        item.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-cursor: hand; " +
                "-fx-border-color: #dee2e6; -fx-border-radius: 8; -fx-border-width: 1;");

        // 标题行
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label((String) announcement.get("title"));
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 15)); // 字体改为System, 大小微调
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        Label priorityLabel = new Label((String) announcement.get("priority"));
        priorityLabel.setPadding(new Insets(2, 6, 2, 6));
        priorityLabel.setStyle("-fx-background-radius: 8; -fx-text-fill: white; -fx-font-size: 11;"); // 字体大小微调

        String priority = (String) announcement.get("priority");
        switch (priority) {
            case "紧急":
                priorityLabel.setStyle(priorityLabel.getStyle() + "-fx-background-color: " + ERROR_COLOR + ";");
                break;
            case "重要":
                priorityLabel.setStyle(priorityLabel.getStyle() + "-fx-background-color: " + WARNING_COLOR + ";");
                break;
            default:
                priorityLabel.setStyle(priorityLabel.getStyle() + "-fx-background-color: " + ACCENT_COLOR + ";");
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        titleRow.getChildren().addAll(titleLabel, spacer, priorityLabel);

        // 信息行
        Label infoLabel = new Label("发布时间：" + announcement.get("publishDate") +
                " | 浏览：" + announcement.get("viewCount") + "次");
        infoLabel.setFont(Font.font("System", 12)); // 字体改为System, 大小微调
        infoLabel.setTextFill(Color.web("#666666"));

        // 摘要
        String summary = (String) announcement.get("summary");
        if (summary != null && !summary.isEmpty()) {
            Label summaryLabel = new Label(summary + "...");
            summaryLabel.setFont(Font.font("System", 13)); // 字体改为System, 大小微调
            summaryLabel.setTextFill(Color.web("#333333"));
            summaryLabel.setWrapText(true);
            item.getChildren().addAll(titleRow, infoLabel, summaryLabel);
        } else {
            item.getChildren().addAll(titleRow, infoLabel);
        }

        // 悬停效果
        item.setOnMouseEntered(e -> {
            item.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-cursor: hand; " +
                    "-fx-border-color: " + PRIMARY_COLOR + "; -fx-border-radius: 8; -fx-border-width: 2; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");
        });

        item.setOnMouseExited(e -> {
            item.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-cursor: hand; " +
                    "-fx-border-color: #dee2e6; -fx-border-radius: 8; -fx-border-width: 1;");
        });

        item.setOnMouseClicked(e -> {
            // 获取完整公告详情
            loadAnnouncementDetail((Integer) announcement.get("id"));
        });

        return item;
    }

    /**
     * 加载公告详情
     */
    private void loadAnnouncementDetail(Integer announcementId) {
        new Thread(() -> {
            try {
                Message response = clientService.request(Message.Type.ANNOUNCEMENT_DETAIL, announcementId);

                Platform.runLater(() -> {
                    if (response.getCode() == Message.Code.SUCCESS) {
                        Map<String, Object> announcement = (Map<String, Object>) response.getData();
                        showAnnouncementDetail(announcement);
                    } else {
                        showAlert("错误", "获取公告详情失败：" + response.getData(), Alert.AlertType.ERROR);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("网络错误", "获取公告详情时发生网络错误：" + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    /**
     * 加载指定分类的文件
     */
    private void loadFilesByCategory(VBox container, String category) {
        new Thread(() -> {
            try {
                Map<String, Object> params = new HashMap<>();
                params.put("category", category);

                Message response = clientService.request(Message.Type.FILE_LIST, params);

                Platform.runLater(() -> {
                    container.getChildren().clear();

                    if (response.getCode() == Message.Code.SUCCESS) {
                        Map<String, Object> result = (Map<String, Object>) response.getData();
                        List<Map<String, Object>> files = (List<Map<String, Object>>) result.get("files");

                        if (files.isEmpty()) {
                            Label emptyLabel = new Label("暂无" + category + "相关文件");
                            emptyLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 15px;"); // 字体大小微调
                            emptyLabel.setPadding(new Insets(20));
                            container.getChildren().add(emptyLabel);
                        } else {
                            for (Map<String, Object> file : files) {
                                HBox item = createFileListItem(file);
                                container.getChildren().add(item);
                            }
                        }
                    } else {
                        showAlert("加载文件失败", "加载文件失败：" + response.getData(), Alert.AlertType.ERROR); // 修正showAlert
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("网络错误", "加载文件时发生网络错误：" + e.getMessage(), Alert.AlertType.ERROR); // 修正showAlert
                });
            }
        }).start();
    }

    /**
     * 创建文件列表项
     */
    private HBox createFileListItem(Map<String, Object> file) {
        HBox item = new HBox(15);
        item.setPadding(new Insets(15));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; " +
                "-fx-border-color: #dee2e6; -fx-border-radius: 8; -fx-border-width: 1;");

        // 文件图标
        Label iconLabel = new Label("📄");
        iconLabel.setFont(Font.font(26)); // 图标字体大小增加

        // 文件信息
        VBox fileInfo = new VBox(5);
        HBox.setHgrow(fileInfo, Priority.ALWAYS);

        Label nameLabel = new Label((String) file.get("originalName"));
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 15)); // 字体改为System, 大小微调
        nameLabel.setTextFill(Color.web(PRIMARY_COLOR));

        String description = (String) file.get("description");
        if (description != null && !description.isEmpty()) {
            Label descLabel = new Label(description);
            descLabel.setFont(Font.font("System", 13)); // 字体改为System, 大小微调
            descLabel.setTextFill(Color.web("#666666"));
            fileInfo.getChildren().addAll(nameLabel, descLabel);
        } else {
            fileInfo.getChildren().add(nameLabel);
        }

        // 文件大小和时间信息
        Label infoLabel = new Label("大小：" + formatFileSize((Long) file.get("fileSize")) +
                " | 上传时间：" + file.get("uploadTime"));
        infoLabel.setFont(Font.font("System", 12)); // 字体改为System, 大小微调
        infoLabel.setTextFill(Color.web("#999999"));
        fileInfo.getChildren().add(infoLabel);

        // 下载按钮
        Button downloadButton = new Button("下载");
        downloadButton.setStyle("-fx-background-color: " + SUCCESS_COLOR + "; -fx-text-fill: white; " +
                "-fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 13px;"); // 字体大小微调
        downloadButton.setOnAction(e -> downloadFile((Integer) file.get("id")));

        item.getChildren().addAll(iconLabel, fileInfo, downloadButton);

        // 悬停效果
        item.setOnMouseEntered(e -> {
            item.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                    "-fx-border-color: " + PRIMARY_COLOR + "; -fx-border-radius: 8; -fx-border-width: 2; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");
        });

        item.setOnMouseExited(e -> {
            item.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; " +
                    "-fx-border-color: #dee2e6; -fx-border-radius: 8; -fx-border-width: 1;");
        });

        return item;
    }

    /**
     * 下载文件
     */
    private void downloadFile(Integer fileId) {
        new Thread(() -> {
            try {
                Message response = clientService.request(Message.Type.FILE_DOWNLOAD, fileId);

                Platform.runLater(() -> {
                    if (response.getCode() == Message.Code.SUCCESS) {
                        Map<String, Object> fileInfo = (Map<String, Object>) response.getData();
                        // 这里应该触发实际的文件下载，由于是演示，只显示提示
                        showAlert("下载开始",
                                "文件 \"" + fileInfo.get("originalName") + "\" 开始下载！",
                                Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("下载失败", "文件下载失败：" + response.getData(), Alert.AlertType.ERROR);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("网络错误", "下载文件时发生网络错误：" + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    /**
     * 上传文件（模拟）
     */
    private void uploadFile(String filePath, String category, String description) {
        // 这里应该实现实际的文件上传逻辑
        // 为了演示，我们模拟上传过程
        new Thread(() -> {
            try {
                // 模拟上传延迟
                Thread.sleep(1000);

                Platform.runLater(() -> {
                    showAlert("上传成功", "文件已成功上传到 " + category + " 分类！", Alert.AlertType.INFORMATION);
                    // 刷新文件上传页面
                    showFileUpload();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("上传失败", "文件上传失败：" + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    /**
     * 加载所有文件（管理端）
     */
    private void loadAllFiles(VBox container) {
        new Thread(() -> {
            try {
                Message response = clientService.request(Message.Type.FILE_LIST, new HashMap<>());

                Platform.runLater(() -> {
                    container.getChildren().clear();

                    if (response.getCode() == Message.Code.SUCCESS) {
                        Map<String, Object> result = (Map<String, Object>) response.getData();
                        List<Map<String, Object>> files = (List<Map<String, Object>>) result.get("files");

                        if (files.isEmpty()) {
                            Label emptyLabel = new Label("暂无文件");
                            emptyLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 15px;"); // 字体大小微调
                            emptyLabel.setPadding(new Insets(20));
                            container.getChildren().add(emptyLabel);
                        } else {
                            for (Map<String, Object> file : files) {
                                HBox item = createFileManagementItem(file);
                                container.getChildren().add(item);
                            }
                        }
                    } else {
                        showAlert("加载文件失败", "加载文件失败：" + response.getData(), Alert.AlertType.ERROR); // 修正showAlert
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("网络错误", "加载文件时发生网络错误：" + e.getMessage(), Alert.AlertType.ERROR); // 修正showAlert
                });
            }
        }).start();
    }

    /**
     * 创建文件管理项（管理端）
     */
    private HBox createFileManagementItem(Map<String, Object> file) {
        HBox item = new HBox(15);
        item.setPadding(new Insets(10));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5; " +
                "-fx-border-color: #dee2e6; -fx-border-radius: 5; -fx-border-width: 1;");

        // 文件信息
        VBox fileInfo = new VBox(3);
        HBox.setHgrow(fileInfo, Priority.ALWAYS);

        Label nameLabel = new Label((String) file.get("originalName"));
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 13)); // 字体改为System, 大小微调

        Label infoLabel = new Label("分类：" + file.get("category") + " | 大小：" +
                formatFileSize((Long) file.get("fileSize")) + " | 上传时间：" + file.get("uploadTime"));
        infoLabel.setFont(Font.font("System", 11)); // 字体改为System, 大小微调
        infoLabel.setTextFill(Color.web("#666666"));

        fileInfo.getChildren().addAll(nameLabel, infoLabel);

        // 操作按钮
        HBox buttonBox = new HBox(5);

        Button deleteButton = new Button("删除");
        deleteButton.setStyle("-fx-background-color: " + ERROR_COLOR + "; -fx-text-fill: white; -fx-background-radius: 3; -fx-font-size: 12px;"); // 字体大小微调
        deleteButton.setOnAction(e -> deleteFile((Integer) file.get("id")));

        buttonBox.getChildren().add(deleteButton);

        item.getChildren().addAll(fileInfo, buttonBox);
        return item;
    }

    /**
     * 删除文件
     */
    private void deleteFile(Integer fileId) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认删除");
        confirmAlert.setHeaderText("删除文件");
        confirmAlert.setContentText("确定要删除这个文件吗？此操作无法撤销。");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        Message deleteResponse = clientService.request(Message.Type.FILE_DELETE, fileId);

                        Platform.runLater(() -> {
                            if (deleteResponse.getCode() == Message.Code.SUCCESS) {
                                showAlert("删除成功", "文件已成功删除", Alert.AlertType.INFORMATION);
                                // 刷新文件列表
                                showFileUpload();
                            } else {
                                showAlert("删除失败", "文件删除失败：" + deleteResponse.getData(), Alert.AlertType.ERROR);
                            }
                        });

                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showAlert("网络错误", "删除文件时发生网络错误：" + e.getMessage(), Alert.AlertType.ERROR);
                        });
                    }
                }).start();
            }
        });
    }

    // ============ 业务逻辑方法 ============

    /**
     * 发布公告
     */
    private void publishAnnouncement(String title, String content, String category, String priority, String audience, Stage dialog) {
        // 准备数据
        Map<String, Object> announcementData = new HashMap<>();
        announcementData.put("title", title);
        announcementData.put("content", content);
        announcementData.put("category", category);
        announcementData.put("priority", priority);
        announcementData.put("targetAudience", audience);
        announcementData.put("isPublished", true);

        new Thread(() -> {
            try {
                Message response = clientService.request(Message.Type.ANNOUNCEMENT_ADD, announcementData);

                Platform.runLater(() -> {
                    if (response.getCode() == Message.Code.SUCCESS) {
                        showAlert("发布成功", "公告已成功发布！", Alert.AlertType.INFORMATION);
                        dialog.close();
                        // 刷新公告列表（返回主界面后，最新公告区域会自动刷新）
                        showAcademicSystem();
                    } else {
                        showAlert("发布失败", "公告发布失败：" + response.getData(), Alert.AlertType.ERROR);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("网络错误", "发布公告时发生网络错误：" + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    /**
     * 提交申请
     */
    private void submitApplication(String type, String title, String content, Stage dialog) {
        // 准备数据
        Map<String, Object> applicationData = new HashMap<>();
        applicationData.put("applicationType", type);
        applicationData.put("title", title);
        applicationData.put("content", content);

        new Thread(() -> {
            try {
                Message response = clientService.request(Message.Type.APPLICATION_SUBMIT, applicationData);

                Platform.runLater(() -> {
                    if (response.getCode() == Message.Code.SUCCESS) {
                        Map<String, Object> result = (Map<String, Object>) response.getData();
                        String applicationNo = (String) result.get("applicationNo");

                        showAlert("提交成功",
                                "申请已成功提交！\n申请编号：" + applicationNo +
                                        "\n请记住申请编号，可在申请列表中查看进度。",
                                Alert.AlertType.INFORMATION);
                        dialog.close();
                        // 刷新申请列表（返回主界面后，如果有显示申请列表的区域，它会刷新）
                        showAcademicSystem();
                    } else {
                        showAlert("提交失败", "申请提交失败：" + response.getData(), Alert.AlertType.ERROR);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("网络错误", "提交申请时发生网络错误：" + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    // ============ 工具方法 ============

    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long sizeInBytes) {
        if (sizeInBytes == null || sizeInBytes == 0) return "0 B";

        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = sizeInBytes.doubleValue();

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.1f %s", size, units[unitIndex]);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ============ 占位符方法 ============

    private void showApplicationManagement() {
        showFunctionPlaceholder("审批申请");
    }

    // 将方法名更正为 showRecordsManagement
    private void showRecordsManagement() {
        showFunctionPlaceholder("学籍管理");
    }

    private void showFunctionPlaceholder(String functionName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("功能提示");
        alert.setHeaderText(functionName);
        alert.setContentText(functionName + " 功能正在开发中...");
        alert.showAndWait();
    }
}