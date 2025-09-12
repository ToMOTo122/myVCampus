package com.vcampus.client.ui.academic;

import javafx.application.Platform;
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
 * 公告管理功能实现
 */
public class AnnouncementManager {

    private ClientService clientService;
    private User currentUser;
    private static final String PRIMARY_COLOR = "#4a7c59";
    private static final String ACCENT_COLOR = "#6b9279";
    private static final String SUCCESS_COLOR = "#28a745";
    private static final String ERROR_COLOR = "#dc3545";

    public AnnouncementManager(ClientService clientService, User currentUser) {
        this.clientService = clientService;
        this.currentUser = currentUser;
    }

    /**
     * 显示发布公告对话框
     */
    public void showPublishDialog() {
        if (!currentUser.isAdmin()) {
            showAlert("权限不足", "只有管理员可以发布公告", Alert.AlertType.WARNING);
            return;
        }

        Stage dialog = new Stage();
        dialog.setTitle("发布新公告");
        dialog.setWidth(600);
        dialog.setHeight(500);
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // 标题
        Label titleLabel = new Label("发布新公告");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        // 表单区域
        VBox formArea = new VBox(15);
        formArea.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        // 公告标题
        TextField titleField = new TextField();
        titleField.setPromptText("请输入公告标题");
        titleField.setPrefHeight(40);
        VBox titleBox = createFormGroup("公告标题 *", titleField);

        // 公告分类
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(
                "最新动态", "教务信息", "学籍管理通知",
                "教学研究公告", "实践教学安排", "国际交流信息", "文化素质教育"
        );
        categoryBox.setValue("教务信息");
        categoryBox.setPrefHeight(40);
        VBox categoryBoxContainer = createFormGroup("公告分类 *", categoryBox);

        // 优先级
        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("普通", "重要", "紧急");
        priorityBox.setValue("普通");
        priorityBox.setPrefHeight(40);
        VBox priorityBoxContainer = createFormGroup("优先级", priorityBox);

        // 目标受众
        ComboBox<String> audienceBox = new ComboBox<>();
        audienceBox.getItems().addAll("全体", "学生", "教师");
        audienceBox.setValue("全体");
        audienceBox.setPrefHeight(40);
        VBox audienceBoxContainer = createFormGroup("目标受众", audienceBox);

        // 公告内容
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("请输入公告内容...");
        contentArea.setPrefRowCount(8);
        contentArea.setWrapText(true);
        VBox contentBox = createFormGroup("公告内容 *", contentArea);

        // 按钮区域
        HBox buttonArea = new HBox(15);
        buttonArea.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = createButton("取消", "#6c757d");
        cancelButton.setOnAction(e -> dialog.close());

        Button publishButton = createButton("发布公告", PRIMARY_COLOR);
        publishButton.setOnAction(e -> {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();

            if (title.isEmpty() || content.isEmpty()) {
                showAlert("输入错误", "请填写公告标题和内容", Alert.AlertType.WARNING);
                return;
            }

            // 准备公告数据
            Map<String, Object> announcementData = new HashMap<>();
            announcementData.put("title", title);
            announcementData.put("content", content);
            announcementData.put("category", categoryBox.getValue());
            announcementData.put("priority", priorityBox.getValue());
            announcementData.put("targetAudience", audienceBox.getValue());
            announcementData.put("isPublished", true);

            // 发布公告
            publishAnnouncement(announcementData, dialog);
        });

        buttonArea.getChildren().addAll(cancelButton, publishButton);

        formArea.getChildren().addAll(
                titleBox, categoryBoxContainer, priorityBoxContainer,
                audienceBoxContainer, contentBox
        );

        root.getChildren().addAll(titleLabel, formArea, buttonArea);

        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.show();
    }

    /**
     * 发布公告
     */
    private void publishAnnouncement(Map<String, Object> announcementData, Stage dialog) {
        // 禁用发布按钮
        Platform.runLater(() -> {
            // 显示加载状态
            ProgressIndicator progress = new ProgressIndicator();
            progress.setPrefSize(30, 30);

            VBox loadingBox = new VBox(10);
            loadingBox.setAlignment(Pos.CENTER);
            loadingBox.getChildren().addAll(progress, new Label("正在发布公告..."));

            Scene loadingScene = new Scene(loadingBox, 200, 100);
            dialog.setScene(loadingScene);
        });

        // 异步发布
        new Thread(() -> {
            try {
                Message response = clientService.request(Message.Type.ANNOUNCEMENT_ADD, announcementData);

                Platform.runLater(() -> {
                    if (response.getCode() == Message.Code.SUCCESS) {
                        showAlert("发布成功", "公告已成功发布！", Alert.AlertType.INFORMATION);
                        dialog.close();
                    } else {
                        showAlert("发布失败", "公告发布失败：" + response.getData(), Alert.AlertType.ERROR);
                        dialog.close();
                        // 重新显示发布对话框
                        showPublishDialog();
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("网络错误", "发布公告时发生网络错误：" + e.getMessage(), Alert.AlertType.ERROR);
                    dialog.close();
                });
            }
        }).start();
    }

    /**
     * 加载公告列表
     */
    public List<Map<String, Object>> loadAnnouncementsByCategory(String category) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (!"全部".equals(category)) {
                params.put("category", category);
            }
            params.put("targetAudience", currentUser.getRole().name());
            params.put("page", 1);
            params.put("pageSize", 20);

            Message response = clientService.request(Message.Type.ANNOUNCEMENT_LIST, params);

            if (response.getCode() == Message.Code.SUCCESS) {
                Map<String, Object> result = (Map<String, Object>) response.getData();
                return (List<Map<String, Object>>) result.get("announcements");
            } else {
                showAlert("加载失败", "加载公告失败：" + response.getData(), Alert.AlertType.ERROR);
                return new ArrayList<>();
            }

        } catch (Exception e) {
            showAlert("网络错误", "加载公告时发生错误：" + e.getMessage(), Alert.AlertType.ERROR);
            return new ArrayList<>();
        }
    }

    /**
     * 显示公告详情
     */
    public void showAnnouncementDetail(Map<String, Object> announcement) {
        Stage detailStage = new Stage();
        detailStage.setTitle("公告详情");
        detailStage.setWidth(700);
        detailStage.setHeight(600);
        detailStage.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // 加载详细内容
        Integer announcementId = (Integer) announcement.get("id");
        loadAnnouncementDetail(announcementId, root, detailStage);

        Scene scene = new Scene(root);
        detailStage.setScene(scene);
        detailStage.show();
    }

    /**
     * 加载公告详细内容
     */
    private void loadAnnouncementDetail(Integer announcementId, VBox container, Stage stage) {
        // 显示加载状态
        ProgressIndicator progress = new ProgressIndicator();
        Label loadingLabel = new Label("正在加载公告详情...");
        container.getChildren().addAll(progress, loadingLabel);

        new Thread(() -> {
            try {
                Message response = clientService.request(Message.Type.ANNOUNCEMENT_DETAIL, announcementId);

                Platform.runLater(() -> {
                    container.getChildren().clear();

                    if (response.getCode() == Message.Code.SUCCESS) {
                        Map<String, Object> detail = (Map<String, Object>) response.getData();
                        createDetailContent(detail, container);
                    } else {
                        Label errorLabel = new Label("加载失败：" + response.getData());
                        errorLabel.setTextFill(Color.web(ERROR_COLOR));
                        container.getChildren().add(errorLabel);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    container.getChildren().clear();
                    Label errorLabel = new Label("网络错误：" + e.getMessage());
                    errorLabel.setTextFill(Color.web(ERROR_COLOR));
                    container.getChildren().add(errorLabel);
                });
            }
        }).start();
    }

    /**
     * 创建详情内容
     */
    private void createDetailContent(Map<String, Object> detail, VBox container) {
        // 标题
        Label titleLabel = new Label((String) detail.get("title"));
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));
        titleLabel.setWrapText(true);

        // 元信息
        HBox metaBox = new HBox(20);
        metaBox.setAlignment(Pos.CENTER_LEFT);

        Label categoryLabel = createMetaLabel("分类: " + detail.get("category"));
        Label authorLabel = createMetaLabel("发布者: " + detail.get("authorName"));
        Label dateLabel = createMetaLabel("发布时间: " + detail.get("publishDate"));
        Label viewLabel = createMetaLabel("浏览次数: " + detail.get("viewCount"));

        metaBox.getChildren().addAll(categoryLabel, authorLabel, dateLabel, viewLabel);

        // 内容区域
        ScrollPane contentScroll = new ScrollPane();
        contentScroll.setFitToWidth(true);
        contentScroll.setPrefHeight(350);
        contentScroll.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        VBox contentContainer = new VBox(10);
        contentContainer.setPadding(new Insets(20));

        Label contentLabel = new Label((String) detail.get("content"));
        contentLabel.setFont(Font.font("Arial", 14));
        contentLabel.setWrapText(true);
        contentLabel.setTextFill(Color.web("#333333"));

        contentContainer.getChildren().add(contentLabel);
        contentScroll.setContent(contentContainer);

        // 关闭按钮
        Button closeButton = createButton("关闭", "#6c757d");
        closeButton.setOnAction(e -> ((Stage) closeButton.getScene().getWindow()).close());

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(closeButton);

        container.getChildren().addAll(titleLabel, metaBox, contentScroll, buttonBox);
    }

    /**
     * 创建元信息标签
     */
    private Label createMetaLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", 12));
        label.setTextFill(Color.web("#666666"));
        return label;
    }

    /**
     * 创建表单组
     */
    private VBox createFormGroup(String labelText, Control control) {
        VBox group = new VBox(8);

        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", FontWeight.MEDIUM, 14));
        label.setTextFill(Color.web("#333333"));

        group.getChildren().addAll(label, control);
        return group;
    }

    /**
     * 创建按钮
     */
    private Button createButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefHeight(35);
        button.setPadding(new Insets(5, 20, 5, 20));
        button.setFont(Font.font("Arial", FontWeight.MEDIUM, 13));
        button.setTextFill(Color.WHITE);
        button.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 5;");

        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-background-color: derive(" + color + ", -10%); -fx-background-radius: 5;");
        });

        button.setOnMouseExited(e -> {
            button.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 5;");
        });

        return button;
    }

    /**
     * 显示提示对话框
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}