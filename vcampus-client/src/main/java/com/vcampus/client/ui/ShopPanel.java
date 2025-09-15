//yhr9.14 0：31添加该文件
package com.vcampus.client.ui;

import com.vcampus.client.controller.SecondHandMarketController;
import com.vcampus.client.service.ClientService;
import com.vcampus.common.entity.User;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * 校园商店面板
 * 包含“二手商城”、“商城”和“失物招领”三个按钮
 */
//yhr9.14 16：04注释全部
//public class ShopPanel extends VBox {
//
//    //yhr9.14 11：43添加下两行
//    private final User currentUser;
//    private final ClientService clientService;
//
//
//    // 修改构造函数，传入user和service
//    //yhr9.14 12:32添加该方法
//    public ShopPanel(User currentUser, ClientService clientService) {
//        this.currentUser = currentUser;
//        this.clientService = clientService;
//        initializeUI();
//    }
//
//    //yhr9.14 12:34添加该方法
//    private void initializeUI() {
//        //this.currentUser = currentUser;
//        //this.clientService = clientService;
//
//        this.setAlignment(Pos.CENTER);
//        this.setPadding(new Insets(50, 20, 20, 20));
//        this.getStyleClass().add("shop-panel");
//
//
//
//
//        Label titleLabel = new Label("校园商店");
//        titleLabel.getStyleClass().add("shop-title");
//
//        // 创建一个用于放置按钮的面板
//        FlowPane buttonPane = new FlowPane();
//        buttonPane.setAlignment(Pos.CENTER);
//        buttonPane.setHgap(50);
//        buttonPane.setVgap(50);
//        buttonPane.setPadding(new Insets(50, 0, 0, 0));
//
//        // 创建“二手商城”按钮
//        Button secondHandButton = createShopButton("二手商城", "♻️");
//        secondHandButton.setOnAction(e -> {
//            // TODO: 这里可以添加点击后跳转到相应界面的逻辑
//            System.out.println("进入二手商城");
//        });
//
//        Button officialShopButton = createShopButton("商城", "🛍️");
//        officialShopButton.setOnAction(e -> {
//            // TODO: 这里可以添加点击后跳转到相应界面的逻辑
//            System.out.println("进入商城");
//
//            // ============ yhr 新增：跳转到商城主页的逻辑 ============
//            // 1. 获取当前场景的根节点（通常是BorderPane）
//            //BorderPane root = (BorderPane) officialShopButton.getScene().getRoot();
//
//            // 2. 获取中央区域（centerArea），这个变量名需要和你MainApplication中的保持一致
//            //Pane centerArea = (Pane) root.getCenter();
//
//            // 3. 清空中央区域当前的内容（也就是清除三个商城按钮）
//            //centerArea.getChildren().clear();
//
//            // 4. 创建并加载商城主界面 (ShopMainPanel)
//            // 注意：你需要想办法获取到 currentUser 和 clientService 并传进来。
//            // 通常可以通过构造函数参数传递给ShopPanel，然后再在这里使用。
//            //ShopMainPanel shopMainPanel = new ShopMainPanel(currentUser, clientService);
//
//            // 5. 将商城主页添加到中央区域
//            //centerArea.getChildren().add(shopMainPanel);
//            // ============ 跳转逻辑结束 ============
//
//            // 上面的跳转代码放在这里，现在可以使用 this.currentUser 和 this.clientService 了
//            BorderPane root = (BorderPane) officialShopButton.getScene().getRoot();
//            Pane centerArea = (Pane) root.getCenter();
//            centerArea.getChildren().clear();
//            ShopMainPanel shopMainPanel = new ShopMainPanel(this.currentUser, this.clientService); // 使用传入的参数
//            centerArea.getChildren().add(shopMainPanel);
//        });
//        // ... 其他代码 ...
//        buttonPane.getChildren().addAll(secondHandButton, officialShopButton, lostAndFoundButton);
//
//        this.getChildren().addAll(titleLabel, buttonPane);
//    }
//
////    public ShopPanel() {
////        this.setAlignment(Pos.CENTER);
////        this.setPadding(new Insets(50, 20, 20, 20));
////        this.getStyleClass().add("shop-panel");
////
////
////        Label titleLabel = new Label("校园商店");
////        titleLabel.getStyleClass().add("shop-title");
////
////        // 创建一个用于放置按钮的面板
////        FlowPane buttonPane = new FlowPane();
////        buttonPane.setAlignment(Pos.CENTER);
////        buttonPane.setHgap(50);
////        buttonPane.setVgap(50);
////        buttonPane.setPadding(new Insets(50, 0, 0, 0));
////
////        // 创建“二手商城”按钮
////        Button secondHandButton = createShopButton("二手商城", "♻️");
////        secondHandButton.setOnAction(e -> {
////            // TODO: 这里可以添加点击后跳转到相应界面的逻辑
////            System.out.println("进入二手商城");
////        });
////
////        // 创建“商城”按钮
////        Button officialShopButton = createShopButton("商城", "🛍️");
////        officialShopButton.setOnAction(e -> {
////            // TODO: 这里可以添加点击后跳转到相应界面的逻辑
////            System.out.println("进入商城");
////        });
////
////        // 创建“失物招领”按钮
////        Button lostAndFoundButton = createShopButton("失物招领", "🔍");
////        lostAndFoundButton.setOnAction(e -> {
////            // TODO: 这里可以添加点击后跳转到相应界面的逻辑
////            System.out.println("进入失物招领");
////        });
////
////        buttonPane.getChildren().addAll(secondHandButton, officialShopButton, lostAndFoundButton);
////
////        this.getChildren().addAll(titleLabel, buttonPane);
////    }
//    //对上述方法修改如下:yhr 9.14 11:14
//    //yhr 9.14 12：36注释下列内容
////    public ShopPanel(User currentUser, ClientService clientService) {
////
////        this.currentUser = currentUser;
////        this.clientService = clientService;
////
////        this.setAlignment(Pos.CENTER);
////        this.setPadding(new Insets(50, 20, 20, 20));
////        this.getStyleClass().add("shop-panel");
////
////
////
////
////        Label titleLabel = new Label("校园商店");
////        titleLabel.getStyleClass().add("shop-title");
////
////        // 创建一个用于放置按钮的面板
////        FlowPane buttonPane = new FlowPane();
////        buttonPane.setAlignment(Pos.CENTER);
////        buttonPane.setHgap(50);
////        buttonPane.setVgap(50);
////        buttonPane.setPadding(new Insets(50, 0, 0, 0));
////
////        // 创建“二手商城”按钮
////        Button secondHandButton = createShopButton("二手商城", "♻️");
////        secondHandButton.setOnAction(e -> {
////            // TODO: 这里可以添加点击后跳转到相应界面的逻辑
////            System.out.println("进入二手商城");
////        });
////
////        // 创建“商城”按钮
////        Button officialShopButton = createShopButton("商城", "🛍️");
//////        officialShopButton.setOnAction(e -> {
//////            // TODO: 这里可以添加点击后跳转到相应界面的逻辑
//////            System.out.println("进入商城");
//////        });
////        officialShopButton.setOnAction(e -> {
////            // TODO: 这里可以添加点击后跳转到相应界面的逻辑
////            System.out.println("进入商城");
////
////            // ============ yhr 新增：跳转到商城主页的逻辑 ============
////            // 1. 获取当前场景的根节点（通常是BorderPane）
////            BorderPane root = (BorderPane) officialShopButton.getScene().getRoot();
////
////            // 2. 获取中央区域（centerArea），这个变量名需要和你MainApplication中的保持一致
////            Pane centerArea = (Pane) root.getCenter();
////
////            // 3. 清空中央区域当前的内容（也就是清除三个商城按钮）
////            centerArea.getChildren().clear();
////
////            // 4. 创建并加载商城主界面 (ShopMainPanel)
////            // 注意：你需要想办法获取到 currentUser 和 clientService 并传进来。
////            // 通常可以通过构造函数参数传递给ShopPanel，然后再在这里使用。
////            ShopMainPanel shopMainPanel = new ShopMainPanel(currentUser, clientService);
////
////            // 5. 将商城主页添加到中央区域
////            centerArea.getChildren().add(shopMainPanel);
////            // ============ 跳转逻辑结束 ============
////        });
////
////        buttonPane.getChildren().addAll(secondHandButton, officialShopButton, lostAndFoundButton);
////
////        this.getChildren().addAll(titleLabel, buttonPane);
////    }
//
//    /**
//     * 创建一个统一风格的商店按钮
//     */
//    private Button createShopButton(String text, String icon) {
//        Button button = new Button(icon + "\n" + text);
//        button.setPrefSize(180, 100);
//        button.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
//        button.getStyleClass().add("shop-button");
//        button.setContentDisplay(javafx.scene.control.ContentDisplay.TOP);
//        button.setPadding(new Insets(20, 10, 10, 10));
//        return button;
//    }
//}



//
//yhr 9.14 16：05新增
public class ShopPanel extends VBox {

    private final User currentUser;
    private final ClientService clientService;
    // 声明所有按钮为成员变量
    private Button lostAndFoundButton;
    private Button secondHandButton;
    private Button officialShopButton;

    public ShopPanel(User currentUser, ClientService clientService) {
        this.currentUser = currentUser;
        this.clientService = clientService;
        initializeUI();
    }

    private void initializeUI() {
        this.setSpacing(20);
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(50));

        // 创建按钮
        this.lostAndFoundButton = createShopButton("失物招领", "🔍");
        this.secondHandButton = createShopButton("二手市场", "♻️");
        this.officialShopButton = createShopButton("商城", "🛍️");

        // 为商城按钮设置跳转逻辑
        this.officialShopButton.setOnAction(e -> {
            BorderPane root = (BorderPane) officialShopButton.getScene().getRoot();
            Pane centerArea = (Pane) root.getCenter();
            centerArea.getChildren().clear();
            ShopMainPanel shopMainPanel = new ShopMainPanel(this.currentUser, this.clientService);
            centerArea.getChildren().add(shopMainPanel);
        });

        // 为失物招领按钮设置事件（暂时先打印日志）
        this.lostAndFoundButton.setOnAction(e -> {
            System.out.println("跳转到失物招领功能");
            // TODO: 后续添加实际的跳转逻辑
        });

        // 为二手市场按钮设置事件（暂时先打印日志）
        this.secondHandButton.setOnAction(e -> {
            System.out.println("跳转到二手市场功能");
            // TODO: 后续添加实际的跳转逻辑
            //yhr9.15 0:14添加
            try {
                // 1. 获取当前场景的根节点
                BorderPane root = (BorderPane) secondHandButton.getScene().getRoot();
                Pane centerArea = (Pane) root.getCenter();

                // 2. 使用 FXMLLoader 加载 FXML 布局文件
                // 确保你的 SecondHandMarketView.fxml 文件在 src/main/resources/view/ 目录下
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SecondHandMarketView.fxml"));
                Parent secondHandView = loader.load();

                // 3. 获取控制器实例并注入 ClientService
                SecondHandMarketController controller = loader.getController();
                controller.setClientService(this.clientService);

                // 4. 清空中央区域并添加新的界面
                centerArea.getChildren().clear();
                centerArea.getChildren().add(secondHandView);

            } catch (IOException ex) {
                // 捕获并处理 IOException
                // 这通常意味着 FXML 文件路径不正确或文件不存在
                System.err.println("加载二手市场界面失败：");
                ex.printStackTrace();
                // 显示一个友好的弹窗提示用户
                showAlert(Alert.AlertType.ERROR, "加载失败", "无法加载二手市场界面，请检查文件路径。");
            }
        });

        // 将按钮添加到布局
        this.getChildren().addAll(lostAndFoundButton, secondHandButton, officialShopButton);
    }

    private Button createShopButton(String text, String emoji) {
        Button button = new Button(text + " " + emoji);
        button.setPrefSize(200, 100);
        button.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        return button;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}