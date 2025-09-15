package com.vcampus.client;

import com.vcampus.client.LifeService.AdminLifeService;
import com.vcampus.client.LifeService.LifeService;
import com.vcampus.client.controller.BookListController;
import com.vcampus.client.controller.admin.LibraryAdminController;
import com.vcampus.client.controller.MenuController;
import com.vcampus.client.controller.course.AdminCourseController;
import com.vcampus.client.controller.course.StudentCourseController;
import com.vcampus.client.controller.course.TeacherCourseController;
import com.vcampus.client.controller.course.ScheduleViewController;
import com.vcampus.client.onlineclass.StudentOnlineClass;
import com.vcampus.client.onlineclass.TeacherOnlineClass;
import com.vcampus.client.ui.SpaceReservationController;
import com.vcampus.common.entity.User;
import com.vcampus.client.service.ClientService;
import com.vcampus.client.ui.AcademicSystemPanel;
import com.vcampus.client.ui.ShopPanel; // 导入新创建的 ShopPanel

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.net.URL;
/**
 * 主应用程序界面
 * 用户登录成功后的主界面
 */
public class MainApplication extends Application {

    private User currentUser;
    private ClientService clientService;
    private Stage primaryStage;
    private StackPane centerArea;
    private AcademicSystemPanel academicPanel;
    private BorderPane rootLayout;

    public MainApplication(User currentUser, ClientService clientService) {
        this.currentUser = currentUser;
        this.clientService = clientService;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        setupPrimaryStage();
        showMainScene();
    }

    /**
     * 配置主窗口
     */
    private void setupPrimaryStage() {
        primaryStage.setTitle("VCampus - " + currentUser.getDisplayName());
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();

        // 设置关闭事件
        primaryStage.setOnCloseRequest(event -> {
            if (clientService != null && clientService.isConnected()) {
                clientService.logout();
                clientService.disconnect();
            }
        });
    }

    /**
     * 显示主界面
     */
    private void showMainScene() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-root");

        // 顶部导航栏
        HBox topBar = createTopBar();
        root.setTop(topBar);

        // 左侧菜单
        VBox leftMenu = createLeftMenu();
        root.setLeft(leftMenu);

        // 中心内容区域
        centerArea = createCenterArea();
        root.setCenter(centerArea);

        // 初始化教务系统面板
        if (academicPanel == null) {
            academicPanel = new AcademicSystemPanel(clientService, currentUser, centerArea);
        }

        Scene scene = new Scene(root);

        // 加载CSS样式文件
        loadStylesheets(scene);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * 加载CSS样式文件
     */
    private void loadStylesheets(Scene scene) {
        try {
            // 加载主界面CSS
            URL mainCssResource = getClass().getResource("/styles/main.css");
            if (mainCssResource != null) {
                scene.getStylesheets().add(mainCssResource.toExternalForm());
                System.out.println("成功加载主界面CSS: " + mainCssResource);
            } else {
                System.err.println("未找到主界面CSS文件: /styles/main.css");
            }

            // 加载选课系统CSS
            URL courseCssResource = getClass().getResource("/styles/course.css");
            if (courseCssResource != null) {
                scene.getStylesheets().add(courseCssResource.toExternalForm());
                System.out.println("成功加载选课系统CSS: " + courseCssResource);
            } else {
                System.err.println("未找到选课系统CSS文件: /styles/course.css");
            }
        } catch (Exception e) {
            System.err.println("加载CSS文件失败: " + e.getMessage());
        }
    }

    /**
     * 创建顶部导航栏
     */
    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10));
        topBar.setSpacing(10);
        topBar.getStyleClass().add("header-bar");

        Label titleLabel = new Label("VCampus 虚拟校园系统");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLabel = new Label("欢迎，" + currentUser.getDisplayName());
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        Button logoutButton = new Button("退出");
        logoutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5;");
        logoutButton.setOnAction(e -> handleLogout());

        topBar.getChildren().addAll(titleLabel, spacer, userLabel, logoutButton);
        return topBar;
    }

    /**
     * 创建左侧菜单
     */
    private VBox createLeftMenu() {
        VBox menu = new VBox();
        menu.setPadding(new Insets(10));
        menu.setSpacing(10);
        menu.setPrefWidth(200);
        menu.getStyleClass().add("sidebar");

        // 根据用户角色显示不同菜单
        if (currentUser.isStudent()) {
            menu.getChildren().addAll(
                    createMenuButton("教务系统", "🎓"),
                    createMenuButton("个人信息", "👤"),
                    createMenuButton("选课系统", "📚"),
                    createMenuButton("我的课表", "🏣"),
                    createMenuButton("空间预约", "🏢"),
                    createMenuButton("成绩查询", "📊"),
                    createMenuButton("图书馆", "📖"),
                    createMenuButton("在线课堂", "👆"),
                    createMenuButton("校园商店", "🛒"),
                    createMenuButton("生活服务", "\uD83D\uDC81")
            );
        } else if (currentUser.isTeacher()) {
            menu.getChildren().addAll(
                    createMenuButton("教务系统", "🎓"),
                    createMenuButton("个人信息", "👤"),
                    createMenuButton("课程管理", "📚"),
                    createMenuButton("空间预约", "🏢"),
                    createMenuButton("成绩管理", "📊"),
                    createMenuButton("在线课堂", "👆"),
                    createMenuButton("学生管理", "👥"),
                    createMenuButton("校园商店", "🛒"),
                    createMenuButton("图书馆", "📖"),
                    createMenuButton("生活服务", "\uD83D\uDC81")
            );
        } else if (currentUser.isAdmin()) {
            menu.getChildren().addAll(
                    createMenuButton("教务系统", "🎓"),
                    createMenuButton("用户管理", "👥"),
                    createMenuButton("学生管理", "🎓"),
                    createMenuButton("教师管理", "👨‍🏫"),
                    createMenuButton("课程管理", "📚"),
                    createMenuButton("空间预约", "🏢"),
                    createMenuButton("生活服务", "\uD83D\uDC81"),
                    createMenuButton("图书管理", "📖"),
                    createMenuButton("商店管理", "🛒"),
                    createMenuButton("系统设置", "⚙️")
            );
        }

        return menu;
    }

    /**
     * 创建菜单按钮
     */
    private Button createMenuButton(String text, String icon) {
        Button button = new Button(icon + " " + text);
        button.setPrefWidth(180);
        button.setPrefHeight(40);
        button.getStyleClass().add("nav-button");
        button.setOnAction(e -> handleMenuClick(text));
        return button;
    }

    /**
     * 创建中心内容区域
     */
    private StackPane createCenterArea() {
        StackPane centerArea = new StackPane();
        centerArea.setPadding(new Insets(20));
        centerArea.getStyleClass().add("content-area");

        // 默认欢迎界面
        VBox welcomeBox = createWelcomeContent();
        centerArea.getChildren().add(welcomeBox);

        return centerArea;
    }

    /**
     * 创建美化的欢迎内容
     */
    private VBox createWelcomeContent() {
        VBox welcomeBox = new VBox(25);
        welcomeBox.setAlignment(javafx.geometry.Pos.CENTER);
        welcomeBox.setPadding(new Insets(40));
        welcomeBox.getStyleClass().add("welcome-card");

        // 主标题
        Label welcomeLabel = new Label("🎓 欢迎使用VCampus虚拟校园系统！");
        welcomeLabel.setStyle(
                "-fx-font-size: 28px;" +
                        "-fx-text-fill: linear-gradient(#2c3e50, #3498db);" +
                        "-fx-font-weight: bold;" +
                        "-fx-effect: dropshadow(one-pass-box, rgba(0,0,0,0.2), 2, 0, 1, 1);"
        );

        // 副标题
        Label subtitleLabel = new Label("智能化校园管理平台 · 高效便捷的数字化校园体验");
        subtitleLabel.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-text-fill: #6c757d;" +
                        "-fx-font-style: italic;"
        );

        // 分隔线
        Separator separator = new Separator();
        separator.setMaxWidth(400);
        separator.setStyle("-fx-background-color: #dee2e6;");

        // 用户信息卡片容器
        HBox infoCardsBox = new HBox(20);
        infoCardsBox.setAlignment(javafx.geometry.Pos.CENTER);

        // 用户信息卡片
        VBox userCard = createInfoCard("👤", "当前用户", currentUser.getDisplayName());
        VBox roleCard = createInfoCard("🔑", "用户角色", currentUser.getRole().getDisplayName());
        VBox timeCard = createInfoCard("🕐", "登录时间", getCurrentTime());

        infoCardsBox.getChildren().addAll(userCard, roleCard, timeCard);

        // 快速导航按钮区域
        Label quickNavLabel = new Label("🚀 快速导航");
        quickNavLabel.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-text-fill: #495057;" +
                        "-fx-font-weight: bold;"
        );

        GridPane quickNavGrid = createQuickNavigation();

        // 系统状态信息
        HBox statusBox = createSystemStatus();

        // 欢迎消息
        Text welcomeMessage = new Text("请从左侧菜单选择功能模块，开始您的校园数字化之旅");
        welcomeMessage.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-fill: #6c757d;" +
                        "-fx-text-alignment: center;"
        );
        welcomeMessage.setWrappingWidth(500);

        welcomeBox.getChildren().addAll(
                welcomeLabel,
                subtitleLabel,
                separator,
                infoCardsBox,
                quickNavLabel,
                quickNavGrid,
                statusBox,
                welcomeMessage
        );

        return welcomeBox;
    }

    /**
     * 创建信息卡片
     */
    private VBox createInfoCard(String icon, String title, String content) {
        VBox card = new VBox(8);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(160);
        card.getStyleClass().add("function-card");

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: #6c757d;" +
                        "-fx-font-weight: bold;"
        );

        Label contentLabel = new Label(content);
        contentLabel.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: #495057;" +
                        "-fx-text-alignment: center;"
        );
        contentLabel.setWrapText(true);

        card.getChildren().addAll(iconLabel, titleLabel, contentLabel);
        return card;
    }

    /**
     * 创建快速导航网格
     */
    private GridPane createQuickNavigation() {
        GridPane grid = new GridPane();
        grid.setAlignment(javafx.geometry.Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);

        // 根据用户角色创建不同的快速导航按钮
        String[][] quickNavItems;
        if (currentUser.isStudent()) {
            quickNavItems = new String[][]{
                    {"📚", "选课系统"},
                    {"📊", "成绩查询"},
                    {"🗓", "我的课表"},
                    {"📖", "图书馆"}
            };
        } else if (currentUser.isTeacher()) {
            quickNavItems = new String[][]{
                    {"📚", "课程管理"},
                    {"📊", "成绩管理"},
                    {"👥", "学生管理"},
                    {"📖", "图书馆"}
            };
        } else {
            quickNavItems = new String[][]{
                    {"👥", "用户管理"},
                    {"🎓", "学生管理"},
                    {"👨‍🏫", "教师管理"},
                    {"⚙️", "系统设置"}
            };
        }

        int col = 0, row = 0;
        for (String[] item : quickNavItems) {
            Button quickBtn = createQuickNavButton(item[0], item[1]);
            grid.add(quickBtn, col, row);

            col++;
            if (col >= 4) {
                col = 0;
                row++;
            }
        }

        return grid;
    }

    /**
     * 创建快速导航按钮
     */
    private Button createQuickNavButton(String icon, String text) {
        Button button = new Button(icon + "\n" + text);
        button.setPrefSize(100, 80);
        button.setStyle(
                "-fx-background-color: linear-gradient(#3498db, #2980b9);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);"
        );

        button.setOnMouseEntered(e -> {
            button.setStyle(
                    "-fx-background-color: linear-gradient(#2980b9, #3498db);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 12px;" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-radius: 8;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 8, 0, 0, 4);" +
                            "-fx-cursor: hand;" +
                            "-fx-scale-x: 1.05;" +
                            "-fx-scale-y: 1.05;"
            );
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: linear-gradient(#3498db, #2980b9);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 12px;" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-radius: 8;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);" +
                            "-fx-scale-x: 1.0;" +
                            "-fx-scale-y: 1.0;"
            );
        });

        button.setOnAction(e -> handleMenuClick(text));
        return button;
    }

    /**
     * 创建系统状态信息
     */
    private HBox createSystemStatus() {
        HBox statusBox = new HBox(30);
        statusBox.setAlignment(javafx.geometry.Pos.CENTER);
        statusBox.setPadding(new Insets(15));
        statusBox.getStyleClass().add("stat-card");

        Label systemStatus = new Label("🟢 系统运行正常");
        systemStatus.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60;");

        Label onlineUsers = new Label("👥 在线用户: 125");
        onlineUsers.setStyle("-fx-font-size: 14px; -fx-text-fill: #2980b9;");

        Label serverTime = new Label("⏰ 服务器时间: " + getCurrentTime());
        serverTime.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        statusBox.getChildren().addAll(systemStatus, onlineUsers, serverTime);
        return statusBox;
    }

    /**
     * 获取当前时间
     */
    private String getCurrentTime() {
        return java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 处理菜单点击
     */
    private void handleMenuClick(String menuText) {
        try {
            switch (menuText) {
                case "教务系统":
                    // 显示教务系统面板
                    academicPanel.showAcademicSystem();
                    break;

                case "选课系统":
                    if (currentUser.isStudent()) {
                        loadStudentCourseInterface();
                    } else {
                        showAlert("提示", "只有学生可以使用选课系统");
                    }
                    break;

                case "课程管理":
                    if (currentUser.isTeacher()) {
                        loadTeacherCourseInterface();
                    } else if (currentUser.isAdmin()) {
                        loadAdminCourseInterface();
                    } else {
                        showAlert("提示", "您没有权限访问课程管理");
                    }
                    break;

                case "我的课表":
                    loadScheduleInterface();
                    break;

                case "图书馆":
                    loadLibraryInterface();
                    break;

                case "图书管理":
                    if (currentUser.isAdmin()) {
                        loadLibraryAdminInterface();
                    } else {
                        showAlert("提示", "只有管理员可以使用图书管理");
                    }
                    break;

                case "个人信息":
                    showPersonalInfo();
                    break;
                case "空间预约":
                    System.out.println("正在加载空间预约系统...");

                    SpaceReservationController controller = new SpaceReservationController(currentUser, clientService);
                    BorderPane spacePane = controller.getSpaceReservationPane();

                    // 清空当前中心区域
                    centerArea.getChildren().clear();

                    // 添加新的空间预约界面到centerArea而不是mainLayout
                    centerArea.getChildren().add(spacePane);

                    System.out.println("空间预约界面添加到centerArea完成");

                    break;
                case "成绩查询":
                case "成绩管理":
                    showGradeManagement();
                    break;

                case "校园商店":
                case "商店管理":
                    // ======================== yhr9.14 0：35新增部分 ========================
                    System.out.println("正在加载校园商店...");
                    //System.out.println("你好...");
                    centerArea.getChildren().clear();
                    //centerArea.getChildren().add(new ShopMainPanel(currentUser, clientService));
                    //yhr 9.14 11：47修改上述语句如下：
                    showShopManagement(currentUser, clientService);
                    System.out.println("校园商店界面加载完成");
                    break;
                // =========================================================
                //下面两行是原版
                //showShopManagement();
                //break;

                case "用户管理":
                    if (currentUser.isAdmin()) {
                        showUserManagement();
                    } else {
                        showAlert("提示", "只有管理员可以使用用户管理");
                    }
                    break;

                case "学生管理":
                    if (currentUser.isAdmin() || currentUser.isTeacher()) {
                        showStudentManagement();
                    } else {
                        showAlert("提示", "您没有权限访问学生管理");
                    }
                    break;

                case "教师管理":
                    if (currentUser.isAdmin()) {
                        showTeacherManagement();
                    } else {
                        showAlert("提示", "只有管理员可以使用教师管理");
                    }
                    break;

                case "系统设置":
                    if (currentUser.isAdmin()) {
                        showSystemSettings();
                    } else {
                        showAlert("提示", "只有管理员可以使用系统设置");
                    }
                    break;
                case "在线课堂":
                {
                    if (currentUser.isStudent()) {
                        // 显示学生在线课堂界面
                        showOnlineClass(currentUser);
                        break;
                    } else if (currentUser.isTeacher()) {
                        // 显示教师在线课堂界面
                        showOnlineClass(currentUser);
                        break;
                    }
                }
                case "生活服务":
                    if (currentUser.isAdmin()) {
                        AdminLifeService adminLifeService = new AdminLifeService(clientService, currentUser);
                        centerArea.getChildren().add(adminLifeService);
                    } else {
                        LifeService lifeService = new LifeService(clientService, currentUser);
                        centerArea.getChildren().add(lifeService);
                    }
                    break;
                default:
                    showPlaceholder(menuText);
                    break;
            }
        } catch (Exception e) {
            System.err.println("处理菜单点击时发生错误: " + e.getMessage());
            e.printStackTrace();
            showAlert("错误", "加载功能模块时发生错误：" + e.getMessage());
        }
    }

    /**
     * 加载学生选课界面
     */
    private void loadStudentCourseInterface() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/course/StudentCourse.fxml"));
            Parent view = fxmlLoader.load();

            StudentCourseController controller = fxmlLoader.getController();
            if (controller != null) {
                controller.setClientService(this.clientService);
                controller.setCurrentUser(this.currentUser);
            }

            centerArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("加载学生选课界面失败: " + e.getMessage());
            e.printStackTrace();
            showAlert("错误", "无法加载选课界面：" + e.getMessage());
        }
    }

    /**
     * 加载教师课程界面
     */
    private void loadTeacherCourseInterface() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/course/TeacherCourse.fxml"));
            Parent view = fxmlLoader.load();

            TeacherCourseController controller = fxmlLoader.getController();
            if (controller != null) {
                controller.setClientService(this.clientService);
                controller.setCurrentUser(this.currentUser);
            }

            centerArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("加载教师课程界面失败: " + e.getMessage());
            e.printStackTrace();
            showAlert("错误", "无法加载教师课程界面：" + e.getMessage());
        }
    }

    /**
     * 加载管理员课程界面
     */
    private void loadAdminCourseInterface() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/course/AdminCourse.fxml"));
            Parent view = fxmlLoader.load();

            AdminCourseController controller = fxmlLoader.getController();
            if (controller != null) {
                controller.setClientService(this.clientService);
                controller.setCurrentUser(this.currentUser);
            }

            centerArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("加载管理员课程界面失败: " + e.getMessage());
            e.printStackTrace();
            showAlert("错误", "无法加载管理员课程界面：" + e.getMessage());
        }
    }

    /**
     * 加载课表界面
     */
    private void loadScheduleInterface() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/course/ScheduleView.fxml"));
            Parent view = fxmlLoader.load();

            ScheduleViewController controller = fxmlLoader.getController();
            if (controller != null) {
                controller.setClientService(this.clientService);
                controller.setCurrentUser(this.currentUser);
            }

            centerArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("加载课表界面失败: " + e.getMessage());
            e.printStackTrace();
            showAlert("错误", "无法加载课表界面：" + e.getMessage());
        }
    }

    /**
     * 加载图书馆界面
     */
    private void loadLibraryInterface() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/BookList.fxml"));
            Parent libraryView = fxmlLoader.load();

            BookListController controller = fxmlLoader.getController();
            if (controller != null) {
                controller.setClientService(this.clientService);
            }

            centerArea.getChildren().setAll(libraryView);
        } catch (IOException e) {
            System.err.println("加载图书馆界面失败: " + e.getMessage());
            e.printStackTrace();
            showAlert("错误", "无法加载图书馆界面：" + e.getMessage());
        }
    }

    /**
     * 加载图书馆管理界面
     */
    private void loadLibraryAdminInterface() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/admin/LibraryAdmin.fxml"));
            Parent adminView = fxmlLoader.load();

            LibraryAdminController controller = fxmlLoader.getController();
            if (controller != null) {
                controller.setClientService(this.clientService);
            }

            centerArea.getChildren().setAll(adminView);
        } catch (IOException e) {
            System.err.println("加载图书馆管理界面失败: " + e.getMessage());
            e.printStackTrace();
            showAlert("错误", "无法加载图书馆管理界面：" + e.getMessage());
        }
    }

    /**
     * 显示占位符内容
     */
    private void showPlaceholder(String functionName) {
        centerArea.getChildren().clear();

        VBox placeholder = new VBox(20);
        placeholder.setAlignment(javafx.geometry.Pos.CENTER);

        Label titleLabel = new Label(functionName);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        Label messageLabel = new Label("该功能正在开发中...");
        messageLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

        Button backButton = new Button("返回首页");
        backButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px 20px;");
        backButton.setOnAction(e -> showWelcome());

        placeholder.getChildren().addAll(titleLabel, messageLabel, backButton);
        centerArea.getChildren().add(placeholder);
    }

    /**
     * 返回欢迎界面
     */
    private void showWelcome() {
        centerArea.getChildren().clear();
        centerArea.getChildren().add(createWelcomeContent());
    }

    /**
     * 显示警告对话框
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 各功能模块的占位符方法
    private void showPersonalInfo() { showPlaceholder("个人信息"); }
    private void showGradeManagement() { showPlaceholder("成绩管理"); }
    private void showShopManagement() { showPlaceholder("商店管理"); }
    private void showUserManagement() { showPlaceholder("用户管理"); }
    private void showStudentManagement() { showPlaceholder("学生管理"); }
    private void showTeacherManagement() { showPlaceholder("教师管理"); }
    private void showSystemSettings() { showPlaceholder("系统设置"); }
    private void showOnlineClass(User currentUser) {
        try {
            centerArea.getChildren().clear();

            if (currentUser.isStudent()) {
                StudentOnlineClass studentOnlineClass = new StudentOnlineClass(currentUser, clientService);;
                centerArea.getChildren().add(studentOnlineClass);
            } else if(currentUser.isTeacher()) {
                TeacherOnlineClass teacherOnlineClass = new TeacherOnlineClass(currentUser, clientService);
                centerArea.getChildren().add(teacherOnlineClass);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showShopManagement(User currentUser, ClientService clientService) {
        //yhr 9.14 12：37添加
        ShopPanel shopPanel = new ShopPanel(currentUser, clientService); // 传入参数
        centerArea.getChildren().clear();
        centerArea.getChildren().add(shopPanel);
    }

    /**
     * 处理登出
     */
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认退出");
        alert.setHeaderText("您确定要退出系统吗？");
        alert.setContentText("登出后将返回登录界面");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (clientService != null && clientService.isConnected()) {
                    clientService.logout();
                    clientService.disconnect();
                }
                primaryStage.close();

                // 重新显示登录界面
                try {
                    VCampusClient loginApp = new VCampusClient();
                    Stage loginStage = new Stage();
                    loginApp.start(loginStage);
                } catch (Exception e) {
                    System.err.println("重新启动登录界面失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    // 公共方法供其他类调用
    public void showModule(String moduleName) {
        handleMenuClick(moduleName);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public ClientService getClientService() {
        return clientService;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}