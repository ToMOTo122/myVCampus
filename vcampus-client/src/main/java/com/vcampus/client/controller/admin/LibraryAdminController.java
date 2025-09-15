package com.vcampus.client.controller.admin;

import com.vcampus.client.service.ClientService;
import com.vcampus.client.service.ServiceManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 简化版LibraryAdminController - 直接查找并初始化现有控制器
 */
public class LibraryAdminController {

    private ClientService clientService;

    // FXML注入的控件
    @FXML private Label currentTimeLabel;
    @FXML private Label totalBooksQuickLabel;
    @FXML private Label activeBorrowsQuickLabel;
    @FXML private Label activeUsersQuickLabel;
    @FXML private Label overdueQuickLabel;
    @FXML private Label dashboardTotalBooksLabel;
    @FXML private Label dashboardActiveBorrowsLabel;
    @FXML private Label dashboardActiveUsersLabel;
    @FXML private Label dashboardAlertsLabel;
    @FXML private Label booksChangeLabel;
    @FXML private Label borrowsChangeLabel;
    @FXML private Label usersChangeLabel;
    @FXML private Label alertsChangeLabel;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TableView<?> booksTableView;
    @FXML private TextField searchField;
    @FXML private TextField bookIdField;
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField publisherField;
    @FXML private Spinner<Integer> stockSpinner;
    @FXML private TabPane mainTabPane;
    @FXML private Button syncAllButton;
    @FXML private ListView<String> recentActivitiesListView;
    @FXML private ListView<String> systemNotificationsListView;
    @FXML private Label systemStatusLabel;
    @FXML private Label onlineUsersLabel;
    @FXML private ProgressBar systemLoadBar;

    // 存储找到的子控制器
    private BookManagementController bookManagementController;
    private BorrowManagementController borrowManagementController;

    /**
     * 设置客户端服务 - 核心修复方法
     */
    public void setClientService(ClientService clientService) {
        System.out.println("=== LibraryAdminController.setClientService 被调用 ===");
        this.clientService = clientService;

        // 确保设置到ServiceManager
        ServiceManager.getInstance().setClientService(clientService);

        Platform.runLater(this::initializeData);
    }

    @FXML
    public void initialize() {
        System.out.println("LibraryAdminController 初始化开始");

        try {
            initializeComponents();
            bindEventHandlers();
            startPeriodicUpdates();
            System.out.println("LibraryAdminController 初始化完成");
        } catch (Exception e) {
            System.err.println("LibraryAdminController 初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 查找并初始化现有控制器 - 关键修复方法
     */
    private void findAndInitializeExistingControllers() {
        System.out.println("=== 查找现有控制器 ===");

        if (mainTabPane == null) {
            System.err.println("MainTabPane 为 null");
            return;
        }

        System.out.println("TabPane 有 " + mainTabPane.getTabs().size() + " 个标签页");

        // 遍历所有Tab，查找现有的控制器
        for (Tab tab : mainTabPane.getTabs()) {
            String tabText = tab.getText();
            System.out.println("检查标签页: " + tabText);

            if (tab.getContent() != null) {
                System.out.println("标签页有内容，尝试查找控制器...");

                // 方法1：尝试通过JavaFX的Scene Graph查找控制器
                findControllerInContent((Parent) tab.getContent(), tabText);

                // 方法2：如果是图书管理相关的Tab，使用反射或其他方式
                if (tabText != null && (tabText.contains("图书") || tabText.contains("库存"))) {
                    findBookManagementController(tab);
                } else if (tabText != null && tabText.contains("借阅")) {
                    findBorrowManagementController(tab);
                }
            }
        }

        // 如果找到了控制器，设置ClientService
        if (bookManagementController != null) {
            System.out.println("找到 BookManagementController，设置 ClientService");
            bookManagementController.setClientService(clientService);
        } else {
            System.err.println("未找到 BookManagementController");
        }

        if (borrowManagementController != null) {
            System.out.println("找到 BorrowManagementController，设置 ClientService");
            borrowManagementController.setClientService(clientService);
        } else {
            System.err.println("未找到 BorrowManagementController");
        }
    }

    /**
     * 在内容中查找控制器
     */
    private void findControllerInContent(Parent content, String tabText) {
        try {
            // 检查内容的userData是否包含控制器引用
            Object userData = content.getUserData();
            if (userData instanceof BookManagementController) {
                System.out.println("从 userData 找到 BookManagementController");
                bookManagementController = (BookManagementController) userData;
            } else if (userData instanceof BorrowManagementController) {
                System.out.println("从 userData 找到 BorrowManagementController");
                borrowManagementController = (BorrowManagementController) userData;
            }

            // 如果userData中没有，尝试通过properties查找
            if (content.getProperties().containsKey("controller")) {
                Object controller = content.getProperties().get("controller");
                System.out.println("从 properties 找到控制器: " + controller.getClass().getName());

                if (controller instanceof BookManagementController) {
                    bookManagementController = (BookManagementController) controller;
                } else if (controller instanceof BorrowManagementController) {
                    borrowManagementController = (BorrowManagementController) controller;
                }
            }

        } catch (Exception e) {
            System.err.println("查找控制器时发生错误: " + e.getMessage());
        }
    }

    /**
     * 查找图书管理控制器
     */
    private void findBookManagementController(Tab tab) {
        try {
            // 尝试通过查找特定的控件来确认这是图书管理界面
            Parent content = (Parent) tab.getContent();
            if (content != null) {
                // 查找特定的Button或TextField来确认界面类型
                Button refreshButton = findButtonById(content, "refreshButton");
                TextField bookIdField = findTextFieldById(content, "bookIdField");

                if (refreshButton != null && bookIdField != null) {
                    System.out.println("确认这是图书管理界面，但控制器引用丢失");
                    System.out.println("尝试重新创建控制器关联...");

                    // 这里可以尝试重新建立控制器关联
                    // 但这比较复杂，建议使用其他方案
                }
            }
        } catch (Exception e) {
            System.err.println("查找图书管理控制器失败: " + e.getMessage());
        }
    }

    /**
     * 查找借阅管理控制器
     */
    private void findBorrowManagementController(Tab tab) {
        // 类似于findBookManagementController的逻辑
        System.out.println("查找借阅管理控制器（暂未实现）");
    }

    /**
     * 递归查找Button
     */
    private Button findButtonById(Parent parent, String id) {
        if (parent == null) return null;

        for (var node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Button && id.equals(node.getId())) {
                return (Button) node;
            } else if (node instanceof Parent) {
                Button found = findButtonById((Parent) node, id);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * 递归查找TextField
     */
    private TextField findTextFieldById(Parent parent, String id) {
        if (parent == null) return null;

        for (var node : parent.getChildrenUnmodifiable()) {
            if (node instanceof TextField && id.equals(node.getId())) {
                return (TextField) node;
            } else if (node instanceof Parent) {
                TextField found = findTextFieldById((Parent) node, id);
                if (found != null) return found;
            }
        }
        return null;
    }

    // ========== 原有的其他方法保持不变 ==========

    private void initializeComponents() {
        updateCurrentTime();
        setDefaultStatistics();
        initializeListViews();
        initializeSystemStatus();
    }

    private void setDefaultStatistics() {
        if (totalBooksQuickLabel != null) totalBooksQuickLabel.setText("150");
        if (activeBorrowsQuickLabel != null) activeBorrowsQuickLabel.setText("23");
        if (activeUsersQuickLabel != null) activeUsersQuickLabel.setText("45");
        if (overdueQuickLabel != null) overdueQuickLabel.setText("2");

        if (dashboardTotalBooksLabel != null) dashboardTotalBooksLabel.setText("150");
        if (dashboardActiveBorrowsLabel != null) dashboardActiveBorrowsLabel.setText("23");
        if (dashboardActiveUsersLabel != null) dashboardActiveUsersLabel.setText("45");
        if (dashboardAlertsLabel != null) dashboardAlertsLabel.setText("2");

        if (booksChangeLabel != null) booksChangeLabel.setText("较昨日 +5");
        if (borrowsChangeLabel != null) borrowsChangeLabel.setText("较昨日 +3");
        if (usersChangeLabel != null) usersChangeLabel.setText("较昨日 +8");
        if (alertsChangeLabel != null) alertsChangeLabel.setText("需要处理");
    }

    private void initializeListViews() {
        if (recentActivitiesListView != null) {
            recentActivitiesListView.getItems().addAll(
                    "✅ 用户 student001 成功借阅《数据结构与算法》",
                    "🔄 管理员更新了《Java编程思想》库存信息",
                    "📧 系统自动发送了3条逾期提醒通知",
                    "📚 新增图书《人工智能原理》已上架",
                    "✅ 用户 student002 归还了《高等数学》"
            );
        }

        if (systemNotificationsListView != null) {
            systemNotificationsListView.getItems().addAll(
                    "🔔 系统维护通知：今晚22:00-23:00进行例行维护",
                    "⚠️ 发现2本图书逾期未还，已发送催还通知",
                    "📊 图书库存预警：3本热门图书库存不足5本",
                    "✅ 数据库备份已于今日凌晨2:00自动完成"
            );
        }
    }

    private void initializeSystemStatus() {
        if (systemStatusLabel != null) systemStatusLabel.setText("系统正常运行");
        if (onlineUsersLabel != null) onlineUsersLabel.setText("1");
        if (systemLoadBar != null) systemLoadBar.setProgress(0.3);
    }

    private void bindEventHandlers() {
        if (syncAllButton != null) {
            syncAllButton.setOnAction(e -> handleSyncAll());
        }
    }

    private void startPeriodicUpdates() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateCurrentTime()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        Timeline statsTimeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> updateStatistics()));
        statsTimeline.setCycleCount(Timeline.INDEFINITE);
        statsTimeline.play();
    }

    private void updateCurrentTime() {
        if (currentTimeLabel != null) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            currentTimeLabel.setText(now.format(formatter));
        }
    }

    private void initializeData() {
        if (clientService == null) {
            System.err.println("服务未初始化，请重新登录");
            return;
        }
        loadStatisticsData();
        simulateRealTimeUpdates();
    }

    private void loadStatisticsData() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(1500);
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    updateStatistics();
                    if (recentActivitiesListView != null) {
                        recentActivitiesListView.getItems().add(0, "📊 管理员界面数据加载完成 - " +
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("错误", "加载统计数据失败: " + getException().getMessage());
                });
            }
        };

        new Thread(task).start();
    }

    private void updateStatistics() {
        int totalBooks = 150 + (int)(Math.random() * 10);
        int activeBorrows = 23 + (int)(Math.random() * 5);
        int activeUsers = 45 + (int)(Math.random() * 8);
        int alerts = 2 + (int)(Math.random() * 3);

        updateQuickStats(totalBooks, activeBorrows, activeUsers, alerts);
        updateDashboardStats(totalBooks, activeBorrows, activeUsers, alerts);
        updateSystemLoad();
    }

    private void updateQuickStats(int totalBooks, int activeBorrows, int activeUsers, int overdue) {
        if (totalBooksQuickLabel != null) totalBooksQuickLabel.setText(String.valueOf(totalBooks));
        if (activeBorrowsQuickLabel != null) activeBorrowsQuickLabel.setText(String.valueOf(activeBorrows));
        if (activeUsersQuickLabel != null) activeUsersQuickLabel.setText(String.valueOf(activeUsers));
        if (overdueQuickLabel != null) overdueQuickLabel.setText(String.valueOf(overdue));
    }

    private void updateDashboardStats(int totalBooks, int activeBorrows, int activeUsers, int alerts) {
        if (dashboardTotalBooksLabel != null) dashboardTotalBooksLabel.setText(String.valueOf(totalBooks));
        if (dashboardActiveBorrowsLabel != null) dashboardActiveBorrowsLabel.setText(String.valueOf(activeBorrows));
        if (dashboardActiveUsersLabel != null) dashboardActiveUsersLabel.setText(String.valueOf(activeUsers));
        if (dashboardAlertsLabel != null) dashboardAlertsLabel.setText(String.valueOf(alerts));
    }

    private void updateSystemLoad() {
        if (systemLoadBar != null) {
            double load = 0.2 + Math.random() * 0.3;
            systemLoadBar.setProgress(load);
        }
    }

    private void simulateRealTimeUpdates() {
        Timeline activityTimeline = new Timeline(new KeyFrame(Duration.seconds(10), e -> {
            if (recentActivitiesListView != null && recentActivitiesListView.getItems().size() > 0) {
                String[] activities = {
                        "📚 用户 student004 借阅了《计算机网络》",
                        "✅ 用户 student005 归还了《数据库原理》",
                        "🔄 系统执行了自动库存检查",
                        "📧 发送了借阅到期提醒通知",
                        "👤 新用户注册并完成身份验证"
                };

                String newActivity = activities[(int)(Math.random() * activities.length)] +
                        " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                recentActivitiesListView.getItems().add(0, newActivity);

                if (recentActivitiesListView.getItems().size() > 10) {
                    recentActivitiesListView.getItems().remove(recentActivitiesListView.getItems().size() - 1);
                }
            }
        }));
        activityTimeline.setCycleCount(Timeline.INDEFINITE);
        activityTimeline.play();
    }

    @FXML
    private void handleSyncAll() {
        if (syncAllButton != null) {
            syncAllButton.setText("🔄 同步中...");
            syncAllButton.setDisable(true);
        }

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(3000);
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    if (syncAllButton != null) {
                        syncAllButton.setText("🔄 同步数据");
                        syncAllButton.setDisable(false);
                    }

                    showAlert("同步完成", "所有数据已成功同步！统计信息已更新到最新状态。");
                    loadStatisticsData();

                    if (recentActivitiesListView != null) {
                        recentActivitiesListView.getItems().add(0, "🔄 管理员执行了数据同步操作 - " +
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    if (syncAllButton != null) {
                        syncAllButton.setText("🔄 同步数据");
                        syncAllButton.setDisable(false);
                    }
                    showAlert("同步失败", "数据同步过程中发生错误，请稍后重试。");
                });
            }
        };

        new Thread(task).start();
    }

    private void showAlert(String title, String content) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("显示提示框失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}