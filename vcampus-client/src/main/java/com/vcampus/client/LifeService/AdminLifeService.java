package com.vcampus.client.LifeService;

import com.vcampus.client.service.ClientService;
import com.vcampus.common.entity.*;
import com.vcampus.common.util.CommonUtils;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AdminLifeService extends BorderPane {
    private VBox cardServiceContainer; // 一卡通服务容器
    private VBox repairServiceContainer; // 后勤报修服务容器
    private VBox paymentServiceContainer; // 生活缴费服务容器
    private HBox topBar; // 顶部导航栏
    private Button homeButton; // 返回首页按钮
    private ClientService clientService;
    private User currentUser;
    private List<RepairRecord> repairRecords = new ArrayList<>();
    private List<User> userSearchResults = new ArrayList<>();

    // 添加成员变量保存原始内容
    private HBox buttonContainer;

    public AdminLifeService(ClientService clientService, User currentUser) {
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

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // 标题
        Label titleLabel = new Label("管理员生活服务功能");
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
        VBox cardCard = createServiceCard("一卡通管理", "添加和管理用户一卡通", "#4CAF50", "💳");
        VBox repairCard = createServiceCard("报修管理", "查看和分配维修任务", "#2196F3", "🔧");
        VBox paymentCard = createServiceCard("缴费管理", "添加和管理缴费账单", "#FF9800", "💰");

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

            if (title.equals("一卡通管理")) {
                showCardService();
            } else if (title.equals("报修管理")) {
                showRepairService();
            } else if (title.equals("缴费管理")) {
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

    private VBox createCardServiceContainer() {
        VBox container = new VBox(20);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);");

        // 标题
        Label titleLabel = new Label("一卡通管理");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        titleLabel.setPadding(new Insets(0, 0, 20, 0));
        container.getChildren().add(titleLabel);

        // 创建添加一卡通表单
        GridPane formGrid = new GridPane();
        formGrid.setAlignment(Pos.CENTER);
        formGrid.setHgap(10);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(20));
        formGrid.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");

        // 用户ID输入框
        Label userIdLabel = new Label("用户ID:");
        userIdLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        TextField userIdField = new TextField();
        userIdField.setPromptText("请输入用户ID");
        userIdField.setPrefWidth(200);

        // 搜索用户按钮
        Button searchUserButton = new Button("搜索用户");
        searchUserButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        searchUserButton.setOnAction(e -> {
            String keyword = userIdField.getText();
            if (!keyword.isEmpty()) {
                searchUsers(keyword);
            }
        });

        // 一卡通号输入框
        Label cardIdLabel = new Label("一卡通号:");
        cardIdLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        TextField cardIdField = new TextField();
        cardIdField.setPromptText("请输入一卡通号");
        cardIdField.setPrefWidth(200);

        // 添加按钮
        Button addCardButton = new Button("添加一卡通");
        addCardButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        addCardButton.setOnAction(e -> {
            String userId = userIdField.getText();
            String cardId = cardIdField.getText();

            if (userId.isEmpty() || cardId.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "错误", "请填写完整信息");
                return;
            }

            boolean success = clientService.addCard(userId, cardId);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "成功", "一卡通添加成功");
                userIdField.clear();
                cardIdField.clear();
                loadCardData(); // 刷新一卡通列表
            } else {
                showAlert(Alert.AlertType.ERROR, "失败", "一卡通添加失败");
            }
        });

        // 刷新按钮
        Button refreshButton = new Button("刷新列表");
        refreshButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        refreshButton.setOnAction(e -> loadCardData());

        // 添加到网格
        formGrid.add(userIdLabel, 0, 0);
        formGrid.add(userIdField, 1, 0);
        formGrid.add(searchUserButton, 2, 0);
        formGrid.add(cardIdLabel, 0, 1);
        formGrid.add(cardIdField, 1, 1);
        formGrid.add(addCardButton, 1, 2);
        formGrid.add(refreshButton, 2, 2);

        // 添加表单到容器
        container.getChildren().add(formGrid);

        // 一卡通列表表格
        TableView<CardInfo> cardTable = new TableView<>();
        cardTable.setPrefHeight(300);

        // 创建表格列
        TableColumn<CardInfo, String> cardIdCol = new TableColumn<>("卡号");
        cardIdCol.setCellValueFactory(new PropertyValueFactory<>("cardId"));

        TableColumn<CardInfo, String> userIdCol = new TableColumn<>("用户ID");
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));

        TableColumn<CardInfo, Double> balanceCol = new TableColumn<>("余额");
        balanceCol.setCellValueFactory(new PropertyValueFactory<>("balance"));

        TableColumn<CardInfo, String> statusCol = new TableColumn<>("状态");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 添加操作列
        TableColumn<CardInfo, Void> actionCol = new TableColumn<>("操作");
        actionCol.setCellFactory(param -> new TableCell<CardInfo, Void>() {
            private final Button reportLossButton = new Button("挂失");
            private final Button unfreezeButton = new Button("解挂");
            private final HBox buttons = new HBox(5, reportLossButton, unfreezeButton);

            {
                reportLossButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px;");
                unfreezeButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px;");

                reportLossButton.setOnAction(event -> {
                    CardInfo card = getTableView().getItems().get(getIndex());
                    if ("NORMAL".equals(card.getStatus())) {
                        boolean success = clientService.reportCardLoss(card.getCardId());
                        if (success) {
                            showAlert(Alert.AlertType.INFORMATION, "成功", "一卡通挂失成功");
                            loadCardData(); // 刷新列表
                        } else {
                            showAlert(Alert.AlertType.ERROR, "失败", "一卡通挂失失败");
                        }
                    } else {
                        showAlert(Alert.AlertType.WARNING, "提示", "此卡已挂失");
                    }
                });

                unfreezeButton.setOnAction(event -> {
                    CardInfo card = getTableView().getItems().get(getIndex());
                    if ("LOST".equals(card.getStatus())) {
                        boolean success = clientService.unfreezeCard(card.getCardId());
                        if (success) {
                            showAlert(Alert.AlertType.INFORMATION, "成功", "一卡通解挂成功");
                            loadCardData(); // 刷新列表
                        } else {
                            showAlert(Alert.AlertType.ERROR, "失败", "一卡通解挂失败");
                        }
                    } else {
                        showAlert(Alert.AlertType.WARNING, "提示", "此卡未挂失");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    CardInfo card = getTableView().getItems().get(getIndex());
                    // 根据卡片状态显示/隐藏按钮
                    reportLossButton.setVisible("NORMAL".equals(card.getStatus()));
                    unfreezeButton.setVisible("LOST".equals(card.getStatus()));
                    setGraphic(buttons);
                }
            }
        });

        cardTable.getColumns().addAll(cardIdCol, userIdCol, balanceCol, statusCol, actionCol);
        container.getChildren().add(cardTable);

        return container;
    }


    private VBox createRepairServiceContainer() {
        VBox container = new VBox(20);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);");

        // 标题
        Label titleLabel = new Label("报修管理");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        titleLabel.setPadding(new Insets(0, 0, 20, 0));
        container.getChildren().add(titleLabel);

        // 创建统计栏
        HBox statsContainer = createRepairStats();
        container.getChildren().add(statsContainer);

        // 分隔线
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));
        container.getChildren().add(separator);

        // 筛选选项
        HBox filterContainer = new HBox(10);
        filterContainer.setAlignment(Pos.CENTER_LEFT);
        filterContainer.setPadding(new Insets(10));

        Label filterLabel = new Label("状态筛选:");
        filterLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("全部", "待处理", "处理中", "已完成", "已取消");
        statusFilter.setValue("全部");

        Button refreshButton = new Button("刷新");
        refreshButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        refreshButton.setOnAction(e -> {
            loadRepairData();
            refreshRepairUI();
        });

        filterContainer.getChildren().addAll(filterLabel, statusFilter, refreshButton);
        container.getChildren().add(filterContainer);

        // 报修记录表格
        TableView<RepairRecord> repairTable = new TableView<>();
        repairTable.setPrefHeight(400);

        // 创建表格列
        TableColumn<RepairRecord, Integer> idCol = new TableColumn<>("报修ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("repairId"));

        TableColumn<RepairRecord, String> userIdCol = new TableColumn<>("用户ID");
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));

        TableColumn<RepairRecord, String> titleCol = new TableColumn<>("标题");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<RepairRecord, String> locationCol = new TableColumn<>("位置");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));

        TableColumn<RepairRecord, String> priorityCol = new TableColumn<>("优先级");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));

        TableColumn<RepairRecord, String> statusCol = new TableColumn<>("状态");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<RepairRecord, String> handlerCol = new TableColumn<>("处理人");
        handlerCol.setCellValueFactory(new PropertyValueFactory<>("handler"));

        TableColumn<RepairRecord, Date> createTimeCol = new TableColumn<>("创建时间");
        createTimeCol.setCellValueFactory(new PropertyValueFactory<>("createTime"));

        // 添加操作列
        TableColumn<RepairRecord, Void> actionCol = new TableColumn<>("操作");
        actionCol.setCellFactory(param -> new TableCell<RepairRecord, Void>() {
            private final Button assignButton = new Button("分配");
            private final Button completeButton = new Button("完成");
            private final Button detailButton = new Button("详情");
            private final HBox buttons = new HBox(5, assignButton, completeButton, detailButton);

            {
                assignButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px;");
                completeButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 12px;");
                detailButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px;");

                assignButton.setOnAction(event -> {
                    RepairRecord record = getTableView().getItems().get(getIndex());
                    showAssignDialog(record);
                });

                completeButton.setOnAction(event -> {
                    RepairRecord record = getTableView().getItems().get(getIndex());
                    if ("处理中".equals(record.getStatus())) {
                        boolean success = clientService.completeRepair(record.getRepairId());
                        if (success) {
                            showAlert(Alert.AlertType.INFORMATION, "成功", "报修已完成");
                            loadRepairData();
                            refreshRepairUI();
                        } else {
                            showAlert(Alert.AlertType.ERROR, "失败", "操作失败");
                        }
                    } else {
                        showAlert(Alert.AlertType.WARNING, "提示", "只有处理中的报修可以标记为完成");
                    }
                });

                detailButton.setOnAction(event -> {
                    RepairRecord record = getTableView().getItems().get(getIndex());
                    showRepairDetail(record);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    RepairRecord record = getTableView().getItems().get(getIndex());
                    // 根据状态显示/隐藏按钮
                    assignButton.setVisible("待处理".equals(record.getStatus()));
                    completeButton.setVisible("处理中".equals(record.getStatus()));
                    setGraphic(buttons);
                }
            }
        });

        repairTable.getColumns().addAll(idCol, userIdCol, titleCol, locationCol, priorityCol, statusCol, handlerCol, createTimeCol, actionCol);

        // 添加表格到容器
        container.getChildren().add(repairTable);

        return container;
    }

    private VBox createPaymentServiceContainer() {
        VBox container = new VBox(20);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);");

        // 标题
        Label titleLabel = new Label("缴费管理");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        titleLabel.setPadding(new Insets(0, 0, 20, 0));
        container.getChildren().add(titleLabel);

        // 创建添加缴费账单表单
        GridPane formGrid = new GridPane();
        formGrid.setAlignment(Pos.CENTER);
        formGrid.setHgap(10);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(20));
        formGrid.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");

        // 用户ID输入框
        Label userIdLabel = new Label("用户ID:");
        userIdLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        TextField userIdField = new TextField();
        userIdField.setPromptText("请输入用户ID");
        userIdField.setPrefWidth(200);

        // 账单类型选择
        Label billTypeLabel = new Label("账单类型:");
        billTypeLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        ComboBox<String> billTypeCombo = new ComboBox<>();
        billTypeCombo.getItems().addAll("电费", "水费", "网费", "其他");
        billTypeCombo.setValue("电费");

        // 金额输入框
        Label amountLabel = new Label("金额:");
        amountLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        TextField amountField = new TextField();
        amountField.setPromptText("请输入金额");
        amountField.setPrefWidth(200);

        // 截止日期选择
        Label dueDateLabel = new Label("截止日期:");
        dueDateLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        DatePicker dueDatePicker = new DatePicker();
        dueDatePicker.setValue(LocalDate.now().plusDays(7));

        // 添加按钮
        Button addBillButton = new Button("添加账单");
        addBillButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        addBillButton.setOnAction(e -> {
            String userId = userIdField.getText();
            String billType = billTypeCombo.getValue();
            double amount;

            try {
                amount = Double.parseDouble(amountField.getText());
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "错误", "请输入有效的金额");
                return;
            }

            Date dueDate = java.sql.Date.valueOf(dueDatePicker.getValue());

            if (userId.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "错误", "请填写用户ID");
                return;
            }

            boolean success = clientService.addPaymentBill(userId, billType, amount, dueDate);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "成功", "缴费账单添加成功");
                userIdField.clear();
                amountField.clear();
                dueDatePicker.setValue(LocalDate.now().plusDays(7));
            } else {
                showAlert(Alert.AlertType.ERROR, "失败", "缴费账单添加失败");
            }
        });

        // 添加到网格
        formGrid.add(userIdLabel, 0, 0);
        formGrid.add(userIdField, 1, 0);
        formGrid.add(billTypeLabel, 0, 1);
        formGrid.add(billTypeCombo, 1, 1);
        formGrid.add(amountLabel, 0, 2);
        formGrid.add(amountField, 1, 2);
        formGrid.add(dueDateLabel, 0, 3);
        formGrid.add(dueDatePicker, 1, 3);
        formGrid.add(addBillButton, 1, 4);

        // 添加表单到容器
        container.getChildren().add(formGrid);

        return container;
    }

    private HBox createRepairStats() {
        HBox statsContainer = new HBox(20);
        statsContainer.setAlignment(Pos.CENTER);
        statsContainer.setPadding(new Insets(10, 0, 10, 0));

        // 计算统计数据
        int total = repairRecords.size();
        int pending = 0;
        int processing = 0;
        int completed = 0;
        int cancelled = 0;

        for (RepairRecord record : repairRecords) {
            switch (record.getStatus()) {
                case "待处理":
                    pending++;
                    break;
                case "处理中":
                    processing++;
                    break;
                case "已完成":
                    completed++;
                    break;
                case "已取消":
                    cancelled++;
                    break;
            }
        }

        // 使用实际数据创建统计盒子
        VBox totalBox = createStatBox("总报修数", String.valueOf(total), "#3498db", "📋");
        VBox pendingBox = createStatBox("待处理", String.valueOf(pending), "#e74c3c", "⏳");
        VBox processingBox = createStatBox("处理中", String.valueOf(processing), "#f39c12", "🔧");
        VBox completedBox = createStatBox("已完成", String.valueOf(completed), "#2ecc71", "✅");
        VBox cancelledBox = createStatBox("已取消", String.valueOf(cancelled), "#95a5a6", "❌");

        statsContainer.getChildren().addAll(totalBox, pendingBox, processingBox, completedBox, cancelledBox);

        return statsContainer;
    }

    private VBox createStatBox(String title, String value, String color, String emoji) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15, 20, 15, 20));
        box.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");
        box.setMinWidth(100);

        // 表情符号
        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font("System", 20));

        // 数值
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        valueLabel.setTextFill(Color.web(color));

        // 标题
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Microsoft YaHei", 12));
        titleLabel.setTextFill(Color.web("#7f8c8d"));

        box.getChildren().addAll(emojiLabel, valueLabel, titleLabel);

        return box;
    }

    private void showCardService() {
        // 加载一卡通数据
        loadCardData();

        // 显示一卡通服务容器
        cardServiceContainer.setVisible(true);

        // 将一卡通服务容器放置在中心区域
        this.setCenter(cardServiceContainer);

        // 添加淡入动画
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), cardServiceContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // 隐藏其他服务容器
        repairServiceContainer.setVisible(false);
        paymentServiceContainer.setVisible(false);
    }

    private void showRepairService() {
        loadRepairData();
        refreshRepairUI();

        // 显示后勤报修服务容器
        repairServiceContainer.setVisible(true);

        // 将后勤报修服务容器放置在中心区域
        this.setCenter(repairServiceContainer);

        // 添加淡入动画
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), repairServiceContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // 隐藏其他服务容器
        cardServiceContainer.setVisible(false);
        paymentServiceContainer.setVisible(false);
    }

    private void showPaymentService() {
        // 显示生活缴费服务容器
        paymentServiceContainer.setVisible(true);

        // 将生活缴费服务容器放置在中心区域
        this.setCenter(paymentServiceContainer);

        // 添加淡入动画
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), paymentServiceContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // 隐藏其他服务容器
        cardServiceContainer.setVisible(false);
        repairServiceContainer.setVisible(false);
    }

    private void refreshRepairUI() {
        // 重新加载数据
        loadRepairData();

        // 更新统计栏
        HBox newStatsContainer = createRepairStats();

        // 找到并替换现有的统计栏
        if (repairServiceContainer.getChildren().size() > 1) {
            repairServiceContainer.getChildren().set(1, newStatsContainer);
        }

        // 刷新报修表格
        if (repairServiceContainer.getChildren().size() > 4) {
            TableView<RepairRecord> table = (TableView<RepairRecord>) repairServiceContainer.getChildren().get(4);
            ObservableList<RepairRecord> items = FXCollections.observableArrayList(repairRecords);
            table.setItems(items);
        }
    }

    private void loadRepairData() {
        Message request = new Message(Message.Type.REPAIR_GET_ALL, "");
        Message response = clientService.sendRequest(request);

        if (response.getCode() == Message.Code.SUCCESS) {
            repairRecords = CommonUtils.convertToGenericList(response.getData(), RepairRecord.class);
            System.out.println("Loaded " + repairRecords.size() + " repair records");
        } else {
            System.err.println("获取报修列表失败: " + response.getData());
            repairRecords = new ArrayList<>();
        }
    }

    private void searchUsers(String keyword) {
        Message request = new Message(Message.Type.USER_SEARCH, keyword);
        Message response = clientService.sendRequest(request);

        if (response.getCode() == Message.Code.SUCCESS) {
            userSearchResults = CommonUtils.convertToGenericList(response.getData(), User.class);
            showUserSearchResults();
        } else {
            System.err.println("搜索用户失败: " + response.getData());
            userSearchResults = new ArrayList<>();
        }
    }

    private void showUserSearchResults() {
        // 实现用户搜索结果展示
        // 这里需要根据UI设计来展示搜索结果
    }

    // 加载一卡通数据的方法
    private void loadCardData() {
        // 这里需要实现获取所有一卡通信息的逻辑
        List<CardInfo> allCards = clientService.getAllCards();
        ObservableList<CardInfo> items = FXCollections.observableArrayList(allCards);

        // 找到表格并设置数据
        if (cardServiceContainer.getChildren().size() > 2) {
            TableView<CardInfo> table = (TableView<CardInfo>) cardServiceContainer.getChildren().get(2);
            table.setItems(items);
        }
    }

    private void showAssignDialog(RepairRecord record) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("分配维修人员");
        dialog.setHeaderText("为报修 #" + record.getRepairId() + " 分配维修人员");

        // 设置按钮
        ButtonType assignButtonType = new ButtonType("分配", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, ButtonType.CANCEL);

        // 创建表单
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        TextField handlerField = new TextField();
        handlerField.setPromptText("请输入维修人员姓名");

        grid.add(new Label("维修人员:"), 0, 0);
        grid.add(handlerField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        // 设置结果转换器
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == assignButtonType) {
                return handlerField.getText();
            }
            return null;
        });

        // 显示对话框并处理结果
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(handler -> {
            boolean success = clientService.assignRepairHandler(record.getRepairId(), handler);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "成功", "维修人员分配成功");
                loadRepairData();
                refreshRepairUI();
            } else {
                showAlert(Alert.AlertType.ERROR, "失败", "维修人员分配失败");
            }
        });
    }

    private void showRepairDetail(RepairRecord record) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("报修详情");
        dialog.setHeaderText("报修 #" + record.getRepairId() + " 的详细信息");

        // 设置按钮
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // 创建详情布局
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("报修ID:"), 0, 0);
        grid.add(new Label(String.valueOf(record.getRepairId())), 1, 0);

        grid.add(new Label("用户ID:"), 0, 1);
        grid.add(new Label(record.getUserId()), 1, 1);

        grid.add(new Label("标题:"), 0, 2);
        grid.add(new Label(record.getTitle()), 1, 2);

        grid.add(new Label("描述:"), 0, 3);
        TextArea descriptionArea = new TextArea(record.getDescription());
        descriptionArea.setEditable(false);
        descriptionArea.setPrefRowCount(3);
        grid.add(descriptionArea, 1, 3);

        grid.add(new Label("位置:"), 0, 4);
        grid.add(new Label(record.getLocation()), 1, 4);

        grid.add(new Label("优先级:"), 0, 5);
        grid.add(new Label(record.getPriority()), 1, 5);

        grid.add(new Label("状态:"), 0, 6);
        grid.add(new Label(record.getStatus()), 1, 6);

        grid.add(new Label("处理人:"), 0, 7);
        grid.add(new Label(record.getHandler() != null ? record.getHandler() : "未分配"), 1, 7);

        grid.add(new Label("创建时间:"), 0, 8);
        grid.add(new Label(formatDate(record.getCreateTime())), 1, 8);

        if (record.getHandleTime() != null) {
            grid.add(new Label("处理时间:"), 0, 9);
            grid.add(new Label(formatDate(record.getHandleTime())), 1, 9);
        }

        if (record.getRemark() != null && !record.getRemark().isEmpty()) {
            grid.add(new Label("备注:"), 0, 10);
            TextArea remarkArea = new TextArea(record.getRemark());
            remarkArea.setEditable(false);
            remarkArea.setPrefRowCount(2);
            grid.add(remarkArea, 1, 10);
        }

        content.getChildren().add(grid);
        dialog.getDialogPane().setContent(content);

        // 显示对话框
        dialog.showAndWait();
    }

    private String formatDate(Date date) {
        if (date == null) return "未知日期";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return date.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime()
                .format(formatter);
    }

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
            });
            fadeOut.play();
        }
    }
}
