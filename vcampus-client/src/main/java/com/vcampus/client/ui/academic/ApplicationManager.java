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
 * 申请管理功能实现
 */
public class ApplicationManager {

    private ClientService clientService;
    private User currentUser;
    private static final String PRIMARY_COLOR = "#4a7c59";
    private static final String ACCENT_COLOR = "#6b9279";
    private static final String SUCCESS_COLOR = "#28a745";
    private static final String WARNING_COLOR = "#ffc107";
    private static final String ERROR_COLOR = "#dc3545";

    public ApplicationManager(ClientService clientService, User currentUser) {
        this.clientService = clientService;
        this.currentUser = currentUser;
    }

    /**
     * 显示新建申请对话框
     */
    public void showNewApplicationDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("新建申请");
        dialog.setWidth(650);
        dialog.setHeight(600);
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // 标题
        Label titleLabel = new Label("提交新申请");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        // 表单区域
        VBox formArea = new VBox(15);
        formArea.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        // 申请类型
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(
                "成绩证明", "在读证明", "学籍证明", "毕业证明",
                "转专业申请", "休学申请", "复学申请", "退学申请",
                "选课申请", "补考申请", "重修申请", "其他申请"
        );
        typeBox.setValue("成绩证明");
        typeBox.setPrefHeight(40);
        VBox typeBoxContainer = createFormGroup("申请类型 *", typeBox);

        // 申请标题
        TextField titleField = new TextField();
        titleField.setPromptText("请输入申请标题");
        titleField.setPrefHeight(40);
        VBox titleBox = createFormGroup("申请标题 *", titleField);

        // 申请原因/内容
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("请详细说明申请原因和具体情况...");
        contentArea.setPrefRowCount(8);
        contentArea.setWrapText(true);
        VBox contentBox = createFormGroup("申请内容 *", contentArea);

        // 预填充标题
        typeBox.setOnAction(e -> {
            String selectedType = typeBox.getValue();
            if (titleField.getText().isEmpty() || titleField.getText().startsWith("申请")) {
                titleField.setText("申请" + selectedType);
            }
        });

        // 按钮区域
        HBox buttonArea = new HBox(15);
        buttonArea.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = createButton("取消", "#6c757d");
        cancelButton.setOnAction(e -> dialog.close());

        Button submitButton = createButton("提交申请", PRIMARY_COLOR);
        submitButton.setOnAction(e -> {
            String type = typeBox.getValue();
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();

            if (title.isEmpty() || content.isEmpty()) {
                showAlert("输入错误", "请填写申请标题和内容", Alert.AlertType.WARNING);
                return;
            }

            if (content.length() < 20) {
                showAlert("内容太短", "申请内容至少需要20个字符", Alert.AlertType.WARNING);
                return;
            }

            // 准备申请数据
            Map<String, Object> applicationData = new HashMap<>();
            applicationData.put("applicationType", type);
            applicationData.put("title", title);
            applicationData.put("content", content);

            // 提交申请
            submitApplication(applicationData, dialog);
        });

        buttonArea.getChildren().addAll(cancelButton, submitButton);

        formArea.getChildren().addAll(typeBoxContainer, titleBox, contentBox);
        root.getChildren().addAll(titleLabel, formArea, buttonArea);

        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.show();
    }

    /**
     * 提交申请
     */
    private void submitApplication(Map<String, Object> applicationData, Stage dialog) {
        // 显示加载状态
        ProgressIndicator progress = new ProgressIndicator();
        progress.setPrefSize(30, 30);

        VBox loadingBox = new VBox(10);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.getChildren().addAll(progress, new Label("正在提交申请..."));

        Scene loadingScene = new Scene(loadingBox, 200, 100);
        dialog.setScene(loadingScene);

        // 异步提交
        new Thread(() -> {
            try {
                Message response = clientService.request(Message.Type.APPLICATION_SUBMIT, applicationData);

                Platform.runLater(() -> {
                    if (response.getCode() == Message.Code.SUCCESS) {
                        Map<String, Object> result = (Map<String, Object>) response.getData();
                        String applicationNo = (String) result.get("applicationNo");

                        showAlert("提交成功",
                                "申请已成功提交！\n申请编号：" + applicationNo +
                                        "\n请记住申请编号，可在\"我的申请\"中查看进度。",
                                Alert.AlertType.INFORMATION);
                        dialog.close();
                    } else {
                        showAlert("提交失败", "申请提交失败：" + response.getData(), Alert.AlertType.ERROR);
                        dialog.close();
                        showNewApplicationDialog(); // 重新显示申请对话框
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("网络错误", "提交申请时发生网络错误：" + e.getMessage(), Alert.AlertType.ERROR);
                    dialog.close();
                });
            }
        }).start();
    }

    /**
     * 加载我的申请列表
     */
    public List<Map<String, Object>> loadMyApplications() {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("isMyApplications", true);
            params.put("page", 1);
            params.put("pageSize", 20);

            Message response = clientService.request(Message.Type.APPLICATION_LIST, params);

            if (response.getCode() == Message.Code.SUCCESS) {
                Map<String, Object> result = (Map<String, Object>) response.getData();
                return (List<Map<String, Object>>) result.get("applications");
            } else {
                showAlert("加载失败", "加载申请列表失败：" + response.getData(), Alert.AlertType.ERROR);
                return new ArrayList<>();
            }

        } catch (Exception e) {
            showAlert("网络错误", "加载申请列表时发生错误：" + e.getMessage(), Alert.AlertType.ERROR);
            return new ArrayList<>();
        }
    }

    /**
     * 加载待审批申请（管理员/教师）
     */
    public List<Map<String, Object>> loadPendingApplications() {
        if (!currentUser.isAdmin() && !currentUser.isTeacher()) {
            showAlert("权限不足", "只有管理员和教师可以查看待审批申请", Alert.AlertType.WARNING);
            return new ArrayList<>();
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("status", "已提交");
            params.put("page", 1);
            params.put("pageSize", 20);

            Message response = clientService.request(Message.Type.APPLICATION_LIST, params);

            if (response.getCode() == Message.Code.SUCCESS) {
                Map<String, Object> result = (Map<String, Object>) response.getData();
                return (List<Map<String, Object>>) result.get("applications");
            } else {
                showAlert("加载失败", "加载待审批申请失败：" + response.getData(), Alert.AlertType.ERROR);
                return new ArrayList<>();
            }

        } catch (Exception e) {
            showAlert("网络错误", "加载待审批申请时发生错误：" + e.getMessage(), Alert.AlertType.ERROR);
            return new ArrayList<>();
        }
    }

    /**
     * 显示申请详情
     */
    public void showApplicationDetail(Map<String, Object> application) {
        Stage detailStage = new Stage();
        detailStage.setTitle("申请详情");
        detailStage.setWidth(800);
        detailStage.setHeight(700);
        detailStage.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // 加载详细内容
        Integer applicationId = (Integer) application.get("id");
        loadApplicationDetail(applicationId, root, detailStage);

        Scene scene = new Scene(root);
        detailStage.setScene(scene);
        detailStage.show();
    }

    /**
     * 加载申请详细内容
     */
    private void loadApplicationDetail(Integer applicationId, VBox container, Stage stage) {
        // 显示加载状态
        ProgressIndicator progress = new ProgressIndicator();
        Label loadingLabel = new Label("正在加载申请详情...");
        container.getChildren().addAll(progress, loadingLabel);

        new Thread(() -> {
            try {
                Message response = clientService.request(Message.Type.APPLICATION_DETAIL, applicationId);

                Platform.runLater(() -> {
                    container.getChildren().clear();

                    if (response.getCode() == Message.Code.SUCCESS) {
                        Map<String, Object> detail = (Map<String, Object>) response.getData();
                        createApplicationDetailContent(detail, container, stage);
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
     * 创建申请详情内容
     */
    private void createApplicationDetailContent(Map<String, Object> detail, VBox container, Stage stage) {
        // 基本信息卡片
        VBox infoCard = new VBox(10);
        infoCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        Label titleLabel = new Label((String) detail.get("title"));
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(PRIMARY_COLOR));

        // 基本信息
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(10);

        addInfoRow(infoGrid, 0, "申请编号:", (String) detail.get("applicationNo"));
        addInfoRow(infoGrid, 1, "申请类型:", (String) detail.get("applicationType"));
        addInfoRow(infoGrid, 2, "申请人:", (String) detail.get("applicantName"));
        addInfoRow(infoGrid, 3, "提交时间:", detail.get("submitTime").toString());

        // 状态显示
        String status = (String) detail.get("status");
        Label statusLabel = new Label(status);
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        statusLabel.setPadding(new Insets(5, 10, 5, 10));
        statusLabel.setStyle("-fx-background-radius: 15;");

        switch (status) {
            case "已提交":
                statusLabel.setStyle("-fx-background-color: " + WARNING_COLOR + "; -fx-text-fill: white; -fx-background-radius: 15;");
                break;
            case "已通过":
                statusLabel.setStyle("-fx-background-color: " + SUCCESS_COLOR + "; -fx-text-fill: white; -fx-background-radius: 15;");
                break;
            case "已拒绝":
                statusLabel.setStyle("-fx-background-color: " + ERROR_COLOR + "; -fx-text-fill: white; -fx-background-radius: 15;");
                break;
            default:
                statusLabel.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-background-radius: 15;");
        }

        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.getChildren().addAll(new Label("当前状态:"), statusLabel);

        infoCard.getChildren().addAll(titleLabel, infoGrid, statusBox);

        // 申请内容
        VBox contentCard = new VBox(10);
        contentCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        Label contentTitle = new Label("申请内容");
        contentTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        contentTitle.setTextFill(Color.web(PRIMARY_COLOR));

        ScrollPane contentScroll = new ScrollPane();
        contentScroll.setFitToWidth(true);
        contentScroll.setPrefHeight(150);
        contentScroll.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5;");

        Label contentLabel = new Label((String) detail.get("content"));
        contentLabel.setFont(Font.font("Arial", 14));
        contentLabel.setWrapText(true);
        contentLabel.setPadding(new Insets(10));

        contentScroll.setContent(contentLabel);
        contentCard.getChildren().addAll(contentTitle, contentScroll);

        // 审批信息（如果有）
        if (detail.get("reviewerName") != null) {
            VBox reviewCard = new VBox(10);
            reviewCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

            Label reviewTitle = new Label("审批信息");
            reviewTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            reviewTitle.setTextFill(Color.web(PRIMARY_COLOR));

            GridPane reviewGrid = new GridPane();
            reviewGrid.setHgap(20);
            reviewGrid.setVgap(10);

            addInfoRow(reviewGrid, 0, "审批人:", (String) detail.get("reviewerName"));
            addInfoRow(reviewGrid, 1, "审批时间:", detail.get("reviewTime").toString());

            if (detail.get("reviewComment") != null && !((String) detail.get("reviewComment")).isEmpty()) {
                Label commentTitle = new Label("审批意见:");
                commentTitle.setFont(Font.font("Arial", FontWeight.MEDIUM, 14));

                Label commentLabel = new Label((String) detail.get("reviewComment"));
                commentLabel.setFont(Font.font("Arial", 14));
                commentLabel.setWrapText(true);
                commentLabel.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 5;");

                reviewCard.getChildren().addAll(reviewTitle, reviewGrid, commentTitle, commentLabel);
            } else {
                reviewCard.getChildren().addAll(reviewTitle, reviewGrid);
            }

            container.getChildren().addAll(infoCard, contentCard, reviewCard);
        } else {
            container.getChildren().addAll(infoCard, contentCard);
        }

        // 申请流程（如果有）
        List<Map<String, Object>> flowSteps = (List<Map<String, Object>>) detail.get("flowSteps");
        if (flowSteps != null && !flowSteps.isEmpty()) {
            VBox flowCard = createFlowCard(flowSteps);
            container.getChildren().add(flowCard);
        }

        // 按钮区域
        HBox buttonArea = new HBox(15);
        buttonArea.setAlignment(Pos.CENTER_RIGHT);

        // 如果是管理员/教师且申请状态为"已提交"，显示审批按钮
        if ((currentUser.isAdmin() || currentUser.isTeacher()) && "已提交".equals(detail.get("status"))) {
            Button approveButton = createButton("通过", SUCCESS_COLOR);
            approveButton.setOnAction(e -> showApprovalDialog(detail, true, stage));

            Button rejectButton = createButton("拒绝", ERROR_COLOR);
            rejectButton.setOnAction(e -> showApprovalDialog(detail, false, stage));

            buttonArea.getChildren().addAll(approveButton, rejectButton);
        }

        Button closeButton = createButton("关闭", "#6c757d");
        closeButton.setOnAction(e -> stage.close());
        buttonArea.getChildren().add(closeButton);

        container.getChildren().add(buttonArea);
    }

    /**
     * 创建流程卡片
     */
    private VBox createFlowCard(List<Map<String, Object>> flowSteps) {
        VBox flowCard = new VBox(10);
        flowCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        Label flowTitle = new Label("申请流程");
        flowTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        flowTitle.setTextFill(Color.web(PRIMARY_COLOR));

        VBox stepsContainer = new VBox(5);
        for (Map<String, Object> step : flowSteps) {
            HBox stepBox = new HBox(10);
            stepBox.setAlignment(Pos.CENTER_LEFT);

            // 步骤状态图标
            Label statusIcon = new Label();
            statusIcon.setPrefSize(20, 20);
            statusIcon.setAlignment(Pos.CENTER);
            statusIcon.setStyle("-fx-background-radius: 10; -fx-text-fill: white; -fx-font-weight: bold;");

            String stepStatus = (String) step.get("status");
            switch (stepStatus) {
                case "已完成":
                    statusIcon.setText("✓");
                    statusIcon.setStyle(statusIcon.getStyle() + "-fx-background-color: " + SUCCESS_COLOR + ";");
                    break;
                case "处理中":
                    statusIcon.setText("●");
                    statusIcon.setStyle(statusIcon.getStyle() + "-fx-background-color: " + WARNING_COLOR + ";");
                    break;
                default:
                    statusIcon.setText("○");
                    statusIcon.setStyle(statusIcon.getStyle() + "-fx-background-color: #6c757d;");
            }

            Label stepName = new Label((String) step.get("stepName"));
            stepName.setFont(Font.font("Arial", 14));

            stepBox.getChildren().addAll(statusIcon, stepName);
            stepsContainer.getChildren().add(stepBox);
        }

        flowCard.getChildren().addAll(flowTitle, stepsContainer);
        return flowCard;
    }

    /**
     * 显示审批对话框
     */
    private void showApprovalDialog(Map<String, Object> application, boolean isApprove, Stage parentStage) {
        Stage dialog = new Stage();
        dialog.setTitle(isApprove ? "通过申请" : "拒绝申请");
        dialog.setWidth(500);
        dialog.setHeight(350);
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f8f9fa;");

        Label titleLabel = new Label((isApprove ? "通过" : "拒绝") + "申请");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(isApprove ? SUCCESS_COLOR : ERROR_COLOR));

        Label infoLabel = new Label("申请标题: " + application.get("title"));
        infoLabel.setFont(Font.font("Arial", 14));

        TextArea commentArea = new TextArea();
        commentArea.setPromptText(isApprove ? "请输入审批意见（可选）..." : "请输入拒绝原因...");
        commentArea.setPrefRowCount(5);
        commentArea.setWrapText(true);

        if (!isApprove) {
            Label requireLabel = new Label("* 拒绝申请需要填写原因");
            requireLabel.setTextFill(Color.web(ERROR_COLOR));
            requireLabel.setFont(Font.font("Arial", 12));
            root.getChildren().add(requireLabel);
        }

        HBox buttonArea = new HBox(15);
        buttonArea.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = createButton("取消", "#6c757d");
        cancelButton.setOnAction(e -> dialog.close());

        Button confirmButton = createButton("确认", isApprove ? SUCCESS_COLOR : ERROR_COLOR);
        confirmButton.setOnAction(e -> {
            String comment = commentArea.getText().trim();

            if (!isApprove && comment.isEmpty()) {
                showAlert("输入错误", "拒绝申请必须填写原因", Alert.AlertType.WARNING);
                return;
            }

            // 执行审批
            processApproval(application, isApprove, comment, dialog, parentStage);
        });

        buttonArea.getChildren().addAll(cancelButton, confirmButton);

        root.getChildren().addAll(titleLabel, infoLabel,
                new Label("审批意见:"), commentArea, buttonArea);

        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.show();
    }

    /**
     * 处理审批
     */
    private void processApproval(Map<String, Object> application, boolean isApprove,
                                 String comment, Stage dialog, Stage parentStage) {
        // 准备审批数据
        Map<String, Object> approvalData = new HashMap<>();
        approvalData.put("applicationId", application.get("id"));
        approvalData.put("reviewComment", comment);

        Message.Type requestType = isApprove ? Message.Type.APPLICATION_APPROVE : Message.Type.APPLICATION_REJECT;

        // 异步处理
        new Thread(() -> {
            try {
                Message response = clientService.request(requestType, approvalData);

                Platform.runLater(() -> {
                    if (response.getCode() == Message.Code.SUCCESS) {
                        showAlert("审批成功",
                                "申请已" + (isApprove ? "通过" : "拒绝"),
                                Alert.AlertType.INFORMATION);
                        dialog.close();
                        parentStage.close(); // 关闭详情页面
                    } else {
                        showAlert("审批失败", "审批失败：" + response.getData(), Alert.AlertType.ERROR);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("网络错误", "审批时发生网络错误：" + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    /**
     * 添加信息行到GridPane
     */
    private void addInfoRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setFont(Font.font("Arial", FontWeight.MEDIUM, 14));
        labelNode.setTextFill(Color.web("#333333"));

        Label valueNode = new Label(value != null ? value : "无");
        valueNode.setFont(Font.font("Arial", 14));
        valueNode.setTextFill(Color.web("#666666"));

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
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