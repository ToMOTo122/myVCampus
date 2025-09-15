package com.vcampus.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import com.vcampus.client.service.ClientService;
import com.vcampus.common.entity.User;

import java.net.URL;

/**
 * VCampus客户端主类
 * JavaFX应用程序入口
 */
public class VCampusClient extends Application {

    private Stage primaryStage;
    private ClientService clientService;
    private User currentUser;

    // 界面元素
    private TextField usernameField;
    private PasswordField passwordField;
    private ComboBox<String> roleComboBox;
    private Button loginButton;
    private Button registerButton;
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.clientService = new ClientService();

        // 检查资源文件
        checkImageResources();

        setupPrimaryStage();
        showLoginScene();
    }

    /**
     * 配置主窗口
     */
    private void setupPrimaryStage() {
        primaryStage.setTitle("VCampus - 虚拟校园系统");
        primaryStage.setResizable(false);
        primaryStage.initStyle(StageStyle.DECORATED);

        // 设置关闭事件
        primaryStage.setOnCloseRequest(event -> {
            if (clientService.isConnected()) {
                clientService.disconnect();
            }
            Platform.exit();
        });
    }

    /**
     * 显示登录界面
     */
    private void showLoginScene() {
        VBox root = createLoginLayout();
        Scene scene = new Scene(root, 500, 650);

        // 使用外部CSS文件
        try {
            String cssPath = getClass().getResource("/styles/login.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            System.out.println("成功加载CSS: " + cssPath);
        } catch (Exception e) {
            System.err.println("加载CSS文件失败: " + e.getMessage());
            // 如果CSS加载失败，使用内联样式作为后备
            addFallbackStyles(scene);
        }

        primaryStage.setScene(scene);
        primaryStage.show();

        // 添加进入动画
        addLoginAnimations(root);

        // 设置默认焦点
        Platform.runLater(() -> usernameField.requestFocus());
    }

    /**
     * 创建登录界面布局
     */
    private VBox createLoginLayout() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));

        // 添加CSS类
        root.getStyleClass().add("login-root");

        // 创建登录卡片
        VBox loginCard = createLoginCard();

        root.getChildren().add(loginCard);
        return root;
    }

    /**
     * 创建登录卡片
     */
    private VBox createLoginCard() {
        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40, 30, 40, 30));
        card.setMaxWidth(350);

        // 使用CSS类
        card.getStyleClass().add("login-card");

        // 标题
        Label titleLabel = new Label("VCampus 统一认证");
        titleLabel.getStyleClass().add("title-label");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 28));
        // titleLabel.setTextFill(Color.valueOf("#333333")); // 已注释，由CSS控制

        Label subtitleLabel = new Label("虚拟校园管理系统");
        subtitleLabel.getStyleClass().add("subtitle-label");
        subtitleLabel.setFont(Font.font("Microsoft YaHei", 14));
        // subtitleLabel.setTextFill(Color.valueOf("#666666")); // 已注释，由CSS控制

        // 用户名输入
        VBox usernameBox = createInputBox("用户名", "请输入学号/工号");
        usernameField = (TextField) ((VBox) usernameBox.getChildren().get(1)).getChildren().get(0);

        // 密码输入
        VBox passwordBox = createPasswordBox("密码", "请输入密码");
        passwordField = (PasswordField) ((VBox) passwordBox.getChildren().get(1)).getChildren().get(0);

        // 角色选择
        VBox roleBox = createRoleBox();

        // 登录按钮
        loginButton = createStyledButton("登录", "login-button");
        loginButton.setOnAction(e -> handleLogin());

        // 注册按钮
        registerButton = createStyledButton("注册新用户", "register-button");
        registerButton.setOnAction(e -> showRegisterDialog());

        // 状态标签
        statusLabel = new Label();
        statusLabel.setFont(Font.font("Microsoft YaHei", 12));
        statusLabel.setWrapText(true);
        statusLabel.setAlignment(Pos.CENTER);

        // 版本信息
        Label versionLabel = new Label("Version 1.0 | ©2024 VCampus");
        versionLabel.setFont(Font.font("Microsoft YaHei", 10));
        // versionLabel.setTextFill(Color.valueOf("#999999")); // 已注释，由CSS控制
        versionLabel.getStyleClass().add("version-label"); // 新增CSS类

        card.getChildren().addAll(
                titleLabel, subtitleLabel,
                usernameBox, passwordBox, roleBox,
                loginButton, registerButton,
                statusLabel, versionLabel
        );

        return card;
    }

    /**
     * 创建输入框组件
     */
    private VBox createInputBox(String labelText, String promptText) {
        VBox box = new VBox(8);

        Label label = new Label(labelText);
        label.getStyleClass().add("input-label");
        label.setFont(Font.font("Microsoft YaHei", FontWeight.MEDIUM, 14));
        // label.setTextFill(Color.valueOf("#333333")); // 已注释，由CSS控制

        VBox fieldContainer = new VBox();
        fieldContainer.getStyleClass().add("glass-effect");
        fieldContainer.setPadding(new Insets(5));

        TextField field = new TextField();
        field.getStyleClass().add("input-field");
        field.setPromptText(promptText);
        field.setPrefHeight(45);

        fieldContainer.getChildren().add(field);
        box.getChildren().addAll(label, fieldContainer);

        return box;
    }

    /**
     * 创建密码输入框
     */
    private VBox createPasswordBox(String labelText, String promptText) {
        VBox box = new VBox(8);

        Label label = new Label(labelText);
        label.getStyleClass().add("input-label");
        label.setFont(Font.font("Microsoft YaHei", FontWeight.MEDIUM, 14));
        // label.setTextFill(Color.valueOf("#333333")); // 已注释，由CSS控制

        VBox fieldContainer = new VBox();
        fieldContainer.getStyleClass().add("glass-effect");
        fieldContainer.setPadding(new Insets(5));

        PasswordField field = new PasswordField();
        field.getStyleClass().add("input-field");
        field.setPromptText(promptText);
        field.setPrefHeight(45);

        fieldContainer.getChildren().add(field);
        box.getChildren().addAll(label, fieldContainer);

        return box;
    }

    /**
     * 创建角色选择框
     */
    private VBox createRoleBox() {
        VBox box = new VBox(8);

        Label label = new Label("登录身份");
        label.getStyleClass().add("input-label");
        label.setFont(Font.font("Microsoft YaHei", FontWeight.MEDIUM, 14));
        // label.setTextFill(Color.valueOf("#333333")); // 已注释，由CSS控制

        VBox comboContainer = new VBox();
        comboContainer.getStyleClass().add("glass-effect");
        comboContainer.setPadding(new Insets(5));

        roleComboBox = new ComboBox<>();
        roleComboBox.getStyleClass().add("choice-box");
        roleComboBox.getItems().addAll("学生", "教师", "管理员");
        roleComboBox.setValue("学生");
        roleComboBox.setPrefHeight(45);
        roleComboBox.setMaxWidth(Double.MAX_VALUE);

        comboContainer.getChildren().add(roleComboBox);
        box.getChildren().addAll(label, comboContainer);

        return box;
    }

    /**
     * 创建样式化按钮
     */
    private Button createStyledButton(String text, String styleClass) {
        Button button = new Button(text);
        button.setPrefHeight(45);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setFont(Font.font("Microsoft YaHei", FontWeight.MEDIUM, 14));
        // button.setTextFill(Color.WHITE); // 已注释，由CSS控制

        // 添加CSS类
        button.getStyleClass().addAll("modern-button", styleClass);

        return button;
    }

    /**
     * 添加登录动画
     */
    private void addLoginAnimations(VBox root) {
        // 淡入动画
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.8), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // 卡片上滑动画
        VBox card = (VBox) root.getChildren().get(0);
        TranslateTransition slideUp = new TranslateTransition(Duration.seconds(0.6), card);
        slideUp.setFromY(50);
        slideUp.setToY(0);
        slideUp.play();
    }

    /**
     * 处理登录
     */
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String roleText = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            showError("请输入用户名和密码");
            return;
        }

        // 转换角色
        User.Role role = convertRole(roleText);

        // 禁用登录按钮，显示加载状态
        loginButton.setDisable(true);
        loginButton.setText("登录中...");
        showInfo("正在连接服务器...");

        // 异步处理登录
        new Thread(() -> {
            try {
                // 连接服务器
                if (!clientService.connect("localhost", 8888)) {
                    Platform.runLater(() -> {
                        showError("无法连接到服务器，请检查网络连接");
                        resetLoginButton();
                    });
                    return;
                }

                // 创建用户对象
                User loginUser = new User(username, password, "", role);

                // 执行登录
                User authenticatedUser = clientService.login(loginUser);

                Platform.runLater(() -> {
                    if (authenticatedUser != null) {
                        this.currentUser = authenticatedUser;
                        showSuccess("登录成功！欢迎，" + authenticatedUser.getDisplayName());

                        // 延迟跳转到主界面
                        new Thread(() -> {
                            try {
                                Thread.sleep(1000);
                                Platform.runLater(this::showMainScene);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();

                    } else {
                        showError("登录失败：用户名或密码错误");
                        resetLoginButton();
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("登录失败：" + e.getMessage());
                    resetLoginButton();
                });
            }
        }).start();
    }

    /**
     * 重置登录按钮
     */
    private void resetLoginButton() {
        loginButton.setDisable(false);
        loginButton.setText("登录");
    }

    /**
     * 转换角色
     */
    private User.Role convertRole(String roleText) {
        switch (roleText) {
            case "教师": return User.Role.TEACHER;
            case "管理员": return User.Role.ADMIN;
            default: return User.Role.STUDENT;
        }
    }

    /**
     * 显示注册对话框
     */
    private void showRegisterDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("注册功能");
        alert.setHeaderText("用户注册");
        alert.setContentText("注册功能正在开发中，请联系管理员创建账户。\n\n" +
                "测试账户：\n" +
                "学生：2021001 / 123456\n" +
                "教师：T001 / 123456\n" +
                "管理员：admin / 123456");
        alert.showAndWait();
    }

    /**
     * 显示主界面
     */
    private void showMainScene() {
        try {
            MainApplication mainApp = new MainApplication(currentUser, clientService);
            Stage mainStage = new Stage();
            mainApp.start(mainStage);

            // 关闭登录窗口
            primaryStage.close();

        } catch (Exception e) {
            showError("打开主界面失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 显示错误信息
     */
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.valueOf("#dc3545"));
    }

    /**
     * 显示成功信息
     */
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.valueOf("#28a745"));
    }

    /**
     * 显示普通信息
     */
    private void showInfo(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.valueOf("#007bff"));
    }

    /**
     * 添加CSS加载失败的后备样式
     */
    private void addFallbackStyles(Scene scene) {
        String fallbackCss = """
        .root {
            -fx-background-color: #667eea;
        }
        """;
        scene.getRoot().setStyle(fallbackCss);
    }

    /**
     * 检查图片资源
     */
    private void checkImageResources() {
        try {
            // 检查登录背景图片
            URL loginBg = getClass().getResource("/images/campus_bg.jpg");
            if (loginBg != null) {
                System.out.println("找到登录背景图片: " + loginBg);
            } else {
                System.err.println("未找到登录背景图片: /images/campus_bg.jpg");
            }

            // 检查主界面背景图片
            URL mainBg = getClass().getResource("/images/campus-main-bg.jpg");
            if (mainBg != null) {
                System.out.println("找到主界面背景图片: " + mainBg);
            } else {
                System.err.println("未找到主界面背景图片: /images/campus-main-bg.jpg");
            }
        } catch (Exception e) {
            System.err.println("检查图片资源时出错: " + e.getMessage());
        }
    }

    /**
     * 应用程序主入口
     */
    public static void main(String[] args) {
        launch(args);
    }
}