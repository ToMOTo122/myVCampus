package com.vcampus.client.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.vcampus.client.service.ClientService;
import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.User;

// 新增：学籍管理两个面板
import com.vcampus.client.ui.academic.EnrollmentPanel;
import com.vcampus.client.ui.academic.EnrollmentAdminPanel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 嵌入式教务系统管理面板
 */
public class AcademicSystemPanel {

    private final ClientService clientService;
    private final User currentUser;
    private final StackPane contentArea;

    // 色彩方案
    private static final String PRIMARY_COLOR = "#4a7c59";
    private static final String ACCENT_COLOR  = "#6b9279";
    private static final String SUCCESS_COLOR = "#28a745";
    private static final String WARNING_COLOR = "#ffc107";
    private static final String ERROR_COLOR   = "#dc3545";

    public AcademicSystemPanel(ClientService clientService, User currentUser, StackPane contentArea) {
        this.clientService = clientService;
        this.currentUser   = currentUser;
        this.contentArea   = contentArea;
    }

    /** 显示教务系统主界面 */
    public void showAcademicSystem() {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30));

        Label titleLabel = new Label("教务管理系统");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        GridPane functionsGrid = createFunctionsGrid();
        VBox announcementArea  = createAnnouncementArea();
        VBox applicationArea   = createApplicationArea();

        mainContent.getChildren().addAll(titleLabel, functionsGrid, announcementArea, applicationArea);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f8f9fa;");

        contentArea.getChildren().setAll(scrollPane);
    }

    /** 创建功能卡片网格 */
    private GridPane createFunctionsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);

        if (currentUser.isAdmin()) {
            grid.add(createFunctionCard("发布公告", "📢", "发布系统通知和公告", this::showPublishAnnouncementDialog), 0, 0);
            grid.add(createFunctionCard("审批申请", "✅", "审核学生提交的各类申请", this::showPendingApplications), 1, 0);
            grid.add(createFunctionCard("用户管理", "👥", "管理学生和教师账户", this::showUserManagement), 0, 1);
            grid.add(createFunctionCard("系统设置", "⚙️", "系统参数和配置管理", this::showSystemSettings), 1, 1);
            // 管理员入口：学籍审批
            grid.add(createFunctionCard("学籍审批", "✅", "审批学生学籍变更", this::showEnrollmentAdmin), 0, 2);

        } else if (currentUser.isTeacher()) {
            grid.add(createFunctionCard("新建申请", "📝", "提交各类教学申请", this::showNewApplicationDialog), 0, 0);
            grid.add(createFunctionCard("我的申请", "📋", "查看申请状态和历史", this::showMyApplications), 1, 0);
            grid.add(createFunctionCard("学生管理", "🎓", "查看所教学生信息", this::showStudentManagement), 0, 1);
            grid.add(createFunctionCard("教学资料", "📚", "下载教学相关资料", this::showTeachingMaterials), 1, 1);

        } else { // 学生
            grid.add(createFunctionCard("新建申请", "📝", "提交成绩证明、在读证明等申请", this::showNewApplicationDialog), 0, 0);
            grid.add(createFunctionCard("我的申请", "📋", "查看申请进度和结果", this::showMyApplications), 1, 0);
            grid.add(createFunctionCard("文件下载", "💾", "下载各类表格和文件", this::showDocumentDownload), 0, 1);
            grid.add(createFunctionCard("个人信息", "👤", "查看和修改个人信息", this::showPersonalInfo), 1, 1);
            // 学生入口：学籍管理
            grid.add(createFunctionCard("学籍管理", "🎓", "查看/提交学籍变更", this::showEnrollment), 0, 2);
        }
        return grid;
    }

    /** 创建功能卡片 */
    private VBox createFunctionCard(String title, String icon, String description, Runnable action) {
        VBox card = new VBox(10);
        card.setPrefSize(200, 120);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); -fx-cursor: hand;");

        Label iconLabel  = new Label(icon);
        iconLabel.setFont(Font.font(24));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        Label descLabel  = new Label(description);
        descLabel.setFont(Font.font("Arial", 11));
        descLabel.setTextFill(Color.web("#666666"));
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);

        card.getChildren().addAll(iconLabel, titleLabel, descLabel);

        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-background-radius: 10; "
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 3); -fx-cursor: hand;");
            titleLabel.setTextFill(Color.WHITE);
            descLabel.setTextFill(Color.web("#e8f5e8"));
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 10; "
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); -fx-cursor: hand;");
            titleLabel.setTextFill(Color.web(PRIMARY_COLOR));
            descLabel.setTextFill(Color.web("#666666"));
        });
        card.setOnMouseClicked(e -> action.run());
        return card;
    }

    /** 最新公告区域 */
    private VBox createAnnouncementArea() {
        VBox area = new VBox(15);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("最新公告");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        Button viewAllButton = new Button("查看全部");
        viewAllButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white; -fx-background-radius: 5;");
        viewAllButton.setOnAction(e -> showAllAnnouncements());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(titleLabel, spacer, viewAllButton);

        VBox announcementList = new VBox(10);
        announcementList.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        loadRecentAnnouncements(announcementList);

        area.getChildren().addAll(header, announcementList);
        return area;
    }

    /** 我的申请区域 */
    private VBox createApplicationArea() {
        VBox area = new VBox(15);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("我的申请");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        Button newAppButton = new Button("新建申请");
        newAppButton.setStyle("-fx-background-color: " + SUCCESS_COLOR + "; -fx-text-fill: white; -fx-background-radius: 5;");
        newAppButton.setOnAction(e -> showNewApplicationDialog());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(titleLabel, spacer, newAppButton);

        VBox applicationList = new VBox(10);
        applicationList.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        loadRecentApplications(applicationList);

        area.getChildren().addAll(header, applicationList);
        return area;
    }

    /** 发布公告对话框 */
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
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        Label titleFieldLabel = new Label("公告标题 *");
        TextField titleField = new TextField();
        titleField.setPromptText("请输入公告标题");
        titleField.setPrefHeight(35);

        Label categoryLabel = new Label("公告分类 *");
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("最新动态", "教务信息", "学籍管理通知", "教学研究公告", "实践教学安排", "国际交流信息", "文化素质教育");
        categoryBox.setValue("教务信息");
        categoryBox.setPrefHeight(35);

        Label priorityLabel = new Label("优先级");
        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("普通", "重要", "紧急");
        priorityBox.setValue("普通");
        priorityBox.setPrefHeight(35);

        Label audienceLabel = new Label("目标受众");
        ComboBox<String> audienceBox = new ComboBox<>();
        audienceBox.getItems().addAll("全体", "学生", "教师");
        audienceBox.setValue("全体");
        audienceBox.setPrefHeight(35);

        Label contentLabel = new Label("公告内容 *");
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("请输入公告内容...");
        contentArea.setPrefRowCount(8);
        contentArea.setWrapText(true);

        form.add(titleFieldLabel, 0, 0);
        form.add(titleField,      1, 0);
        form.add(categoryLabel,   0, 1);
        form.add(categoryBox,     1, 1);
        form.add(priorityLabel,   0, 2);
        form.add(priorityBox,     1, 2);
        form.add(audienceLabel,   0, 3);
        form.add(audienceBox,     1, 3);
        form.add(contentLabel,    0, 4);
        form.add(contentArea,     1, 4);

        HBox buttonArea = new HBox(15);
        buttonArea.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = new Button("取消");
        cancelButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-background-radius: 5;");
        cancelButton.setOnAction(e -> dialog.close());

        Button publishButton = new Button("发布公告");
        publishButton.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; -fx-background-radius: 5;");
        publishButton.setOnAction(e -> {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();
            if (title.isEmpty() || content.isEmpty()) {
                showAlert("输入错误", "请填写公告标题和内容", Alert.AlertType.WARNING);
                return;
            }
            publishAnnouncement(title, content, categoryBox.getValue(), priorityBox.getValue(), audienceBox.getValue(), dialog);
        });

        buttonArea.getChildren().addAll(cancelButton, publishButton);
        root.getChildren().addAll(titleLabel, form, buttonArea);

        dialog.setScene(new Scene(root));
        dialog.show();
    }

    /** 新建申请对话框 */
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
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        Label typeLabel = new Label("申请类型 *");
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("成绩证明", "在读证明", "学籍证明", "毕业证明", "转专业申请", "休学申请", "复学申请", "其他申请");
        typeBox.setValue("成绩证明");
        typeBox.setPrefHeight(35);

        Label titleFieldLabel = new Label("申请标题 *");
        TextField titleField = new TextField();
        titleField.setPromptText("请输入申请标题");
        titleField.setPrefHeight(35);

        Label contentLabel = new Label("申请内容 *");
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("请详细说明申请原因和具体情况...");
        contentArea.setPrefRowCount(10);
        contentArea.setWrapText(true);

        typeBox.setOnAction(e -> {
            String selectedType = typeBox.getValue();
            if (titleField.getText().isEmpty() || titleField.getText().startsWith("申请")) {
                titleField.setText("申请" + selectedType);
            }
        });

        form.add(typeLabel,      0, 0);
        form.add(typeBox,        1, 0);
        form.add(titleFieldLabel,0, 1);
        form.add(titleField,     1, 1);
        form.add(contentLabel,   0, 2);
        form.add(contentArea,    1, 2);

        HBox buttonArea = new HBox(15);
        buttonArea.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = new Button("取消");
        cancelButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-background-radius: 5;");
        cancelButton.setOnAction(e -> dialog.close());

        Button submitButton = new Button("提交申请");
        submitButton.setStyle("-fx-background-color: " + SUCCESS_COLOR + "; -fx-text-fill: white; -fx-background-radius: 5;");
        submitButton.setOnAction(e -> {
            String type    = typeBox.getValue();
            String title   = titleField.getText().trim();
            String content = contentArea.getText().trim();
            if (title.isEmpty() || content.isEmpty()) {
                showAlert("输入错误", "请填写申请标题和内容", Alert.AlertType.WARNING);
                return;
            }
            if (content.length() < 20) {
                showAlert("内容太短", "申请内容至少需要20个字符", Alert.AlertType.WARNING);
                return;
            }
            submitApplication(type, title, content, dialog);
        });

        buttonArea.getChildren().addAll(cancelButton, submitButton);
        root.getChildren().addAll(titleLabel, form, buttonArea);

        dialog.setScene(new Scene(root));
        dialog.show();
    }

    /** 发布公告（后端请求） */
    private void publishAnnouncement(String title, String content, String category, String priority, String audience, Stage dialog) {
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
                        showAcademicSystem();
                    } else {
                        showAlert("发布失败", "公告发布失败：" + response.getData(), Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("网络错误", "发布公告时发生网络错误：" + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    /** 提交申请（后端请求） */
    private void submitApplication(String type, String title, String content, Stage dialog) {
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
                        showAlert("提交成功", "申请已成功提交！\n申请编号：" + applicationNo + "\n请在申请列表中查看进度。", Alert.AlertType.INFORMATION);
                        dialog.close();
                        showAcademicSystem();
                    } else {
                        showAlert("提交失败", "申请提交失败：" + response.getData(), Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("网络错误", "提交申请时发生网络错误：" + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    /** 加载最新公告（后端请求） */
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
                        if (announcements == null || announcements.isEmpty()) {
                            Label emptyLabel = new Label("暂无公告");
                            emptyLabel.setStyle("-fx-text-fill: #999999;");
                            container.getChildren().add(emptyLabel);
                        } else {
                            for (Map<String, Object> a : announcements) {
                                container.getChildren().add(createAnnouncementItem(a));
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

    /** 加载最新申请（后端请求） */
    private void loadRecentApplications(VBox container) {
        new Thread(() -> {
            try {
                Map<String, Object> params = new HashMap<>();
                params.put("isMyApplications", true);
                params.put("page", 1);
                params.put("pageSize", 5);

                Message response = clientService.request(Message.Type.APPLICATION_LIST, params);
                Platform.runLater(() -> {
                    container.getChildren().clear();
                    if (response.getCode() == Message.Code.SUCCESS) {
                        Map<String, Object> result = (Map<String, Object>) response.getData();
                        List<Map<String, Object>> applications = (List<Map<String, Object>>) result.get("applications");
                        if (applications == null || applications.isEmpty()) {
                            Label emptyLabel = new Label("暂无申请记录");
                            emptyLabel.setStyle("-fx-text-fill: #999999;");
                            container.getChildren().add(emptyLabel);
                        } else {
                            for (Map<String, Object> a : applications) {
                                container.getChildren().add(createApplicationItem(a));
                            }
                        }
                    } else {
                        Label errorLabel = new Label("加载申请失败：" + response.getData());
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

    /** 单条公告项 */
    private HBox createAnnouncementItem(Map<String, Object> announcement) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(10));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-cursor: hand;");

        VBox content = new VBox(5);
        HBox.setHgrow(content, Priority.ALWAYS);

        Label titleLabel = new Label(String.valueOf(announcement.get("title")));
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        Label infoLabel = new Label(announcement.get("category") + " | " + announcement.get("publishDate"));
        infoLabel.setFont(Font.font("Arial", 12));
        infoLabel.setTextFill(Color.web("#666666"));

        content.getChildren().addAll(titleLabel, infoLabel);

        Label priorityLabel = new Label(String.valueOf(announcement.get("priority")));
        priorityLabel.setPadding(new Insets(2, 8, 2, 8));
        priorityLabel.setStyle("-fx-background-radius: 10; -fx-text-fill: white; -fx-font-size: 11;");

        String priority = String.valueOf(announcement.get("priority"));
        if ("紧急".equals(priority)) {
            priorityLabel.setStyle(priorityLabel.getStyle() + "-fx-background-color: " + ERROR_COLOR + ";");
        } else if ("重要".equals(priority)) {
            priorityLabel.setStyle(priorityLabel.getStyle() + "-fx-background-color: " + WARNING_COLOR + ";");
        } else {
            priorityLabel.setStyle(priorityLabel.getStyle() + "-fx-background-color: " + ACCENT_COLOR + ";");
        }

        item.getChildren().addAll(content, priorityLabel);
        item.setOnMouseClicked(e -> showAnnouncementDetail(announcement));
        return item;
    }

    /** 单条申请项 */
    private HBox createApplicationItem(Map<String, Object> application) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(10));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-cursor: hand;");

        VBox content = new VBox(5);
        HBox.setHgrow(content, Priority.ALWAYS);

        Label titleLabel = new Label(String.valueOf(application.get("title")));
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        Label infoLabel = new Label(application.get("applicationType") + " | " + application.get("submitTime"));
        infoLabel.setFont(Font.font("Arial", 12));
        infoLabel.setTextFill(Color.web("#666666"));

        content.getChildren().addAll(titleLabel, infoLabel);

        Label statusLabel = new Label(String.valueOf(application.get("status")));
        statusLabel.setPadding(new Insets(2, 8, 2, 8));
        statusLabel.setStyle("-fx-background-radius: 10; -fx-text-fill: white; -fx-font-size: 11;");

        String status = String.valueOf(application.get("status"));
        if ("已通过".equals(status)) {
            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: " + SUCCESS_COLOR + ";");
        } else if ("已拒绝".equals(status)) {
            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: " + ERROR_COLOR + ";");
        } else {
            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: " + WARNING_COLOR + ";");
        }

        item.getChildren().addAll(content, statusLabel);
        item.setOnMouseClicked(e -> showApplicationDetail(application));
        return item;
    }

    // ====== 下方是各个功能入口 ======

    private void showPendingApplications() { showFunctionPlaceholder("待审批申请"); }
    private void showMyApplications()      { showFunctionPlaceholder("我的申请"); }
    private void showAllAnnouncements()    { showFunctionPlaceholder("所有公告"); }
    private void showAnnouncementDetail(Map<String, Object> a) {
        showAlert("公告详情", "标题: " + a.get("title"), Alert.AlertType.INFORMATION);
    }
    private void showApplicationDetail(Map<String, Object> app) {
        showAlert("申请详情", "标题: " + app.get("title") + "\n状态: " + app.get("status"), Alert.AlertType.INFORMATION);
    }
    private void showUserManagement()      { showFunctionPlaceholder("用户管理"); }
    private void showSystemSettings()      { showFunctionPlaceholder("系统设置"); }
    private void showStudentManagement()   { showFunctionPlaceholder("学生管理"); }
    private void showTeachingMaterials()   { showFunctionPlaceholder("教学资料"); }
    private void showDocumentDownload()    { showFunctionPlaceholder("文件下载"); }
    private void showPersonalInfo()        { showFunctionPlaceholder("个人信息"); }

    // 学籍管理：学生端
    private void showEnrollment() {
        Pane pane = new EnrollmentPanel(clientService, currentUser, contentArea).build();
        contentArea.getChildren().setAll(pane);
    }

    // 学籍管理：管理员端
    private void showEnrollmentAdmin() {
        Pane pane = new EnrollmentAdminPanel(clientService, currentUser, contentArea).build();
        contentArea.getChildren().setAll(pane);
    }

    private void showFunctionPlaceholder(String name) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("功能提示");
        alert.setHeaderText(name);
        alert.setContentText(name + " 功能正在开发中...");
        alert.showAndWait();
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
