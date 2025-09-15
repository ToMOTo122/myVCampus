package com.vcampus.client.ui;

import com.vcampus.client.service.ClientService;
import com.vcampus.common.entity.User;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * 空间预约系统控制器
 */
public class SpaceReservationController {
    private User currentUser;
    private ClientService clientService;
    private Stage primaryStage;
    private SpaceReservationView spaceView;

    public SpaceReservationController(User user, ClientService service) {
        this.currentUser = user;
        this.clientService = service;
    }

    /**
     * 显示空间预约界面
     */
    public void showSpaceReservationWindow() {
        primaryStage = new Stage();
        primaryStage.setTitle("VCampus - 空间预约系统");
        primaryStage.setMaximized(true);

        // 创建空间预约视图
        spaceView = new SpaceReservationView(currentUser, clientService);
        BorderPane root = spaceView.createLayout();

        Scene scene = new Scene(root, 1200, 800);

        // 加载CSS样式
        try {
            String cssPath = getClass().getResource("/styles/space-reservation.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            System.out.println("成功加载空间预约CSS: " + cssPath);
        } catch (Exception e) {
            System.err.println("加载空间预约CSS失败: " + e.getMessage());
            // 添加后备样式
            addFallbackStyles(scene);
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * 在现有窗口中显示空间预约界面
     */
    public BorderPane getSpaceReservationPane() {
        spaceView = new SpaceReservationView(currentUser, clientService);
        return spaceView.createLayout();
    }

    /**
     * 添加后备样式
     */
    private void addFallbackStyles(Scene scene) {
        String fallbackCss = """
            .root {
                -fx-background-color: #f8f9fa;
            }
            """;
        scene.getRoot().setStyle(fallbackCss);
    }

    /**
     * 关闭窗口
     */
    public void closeWindow() {
        if (primaryStage != null) {
            primaryStage.close();
        }
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