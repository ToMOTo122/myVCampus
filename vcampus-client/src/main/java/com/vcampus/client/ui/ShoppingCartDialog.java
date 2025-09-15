// 文件路径: src/main/java/com/vcampus/client/ui/ShoppingCartDialog.java
//yhr9.14 8：09添加该类
package com.vcampus.client.ui;

import com.vcampus.client.service.ClientService;
import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.Message.Code;
import com.vcampus.common.entity.Message.Type;
import com.vcampus.common.entity.ShoppingCartItem;
import com.vcampus.common.entity.User;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车弹窗
 */
public class ShoppingCartDialog extends Stage {

    private final User currentUser;
    private final ClientService clientService;
    private final ShopMainPanel parentPanel;
    private VBox cartItemsContainer;
    private Label totalLabel;

    public ShoppingCartDialog(User currentUser, ClientService clientService, ShopMainPanel parentPanel) {
        this.currentUser = currentUser;
        this.clientService = clientService;
        this.parentPanel = parentPanel;
        this.initModality(Modality.APPLICATION_MODAL);
        this.setTitle("我的购物车");
        this.setResizable(false);

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("shopping-cart-dialog");

        Label title = new Label("购物车");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        cartItemsContainer = new VBox(15);
        cartItemsContainer.setPadding(new Insets(10));
        scrollPane.setContent(cartItemsContainer);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        totalLabel = new Label("总计：¥ 0.00");
        totalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        totalLabel.getStyleClass().add("total-label");

        Button checkoutButton = new Button("立即购买");
        checkoutButton.setPrefWidth(200);
        checkoutButton.getStyleClass().add("checkout-button");
        checkoutButton.setOnAction(e -> handleCheckout());

        root.getChildren().addAll(title, scrollPane, totalLabel, checkoutButton);

        Scene scene = new Scene(root, 600, 500);
        // 加载CSS
        try {
            String cssPath = getClass().getResource("/styles/shop.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("加载CSS文件失败：" + e.getMessage());
        }

        this.setScene(scene);
        loadShoppingCartItems();
    }

    /**
     * 加载购物车商品列表
     */
    private void loadShoppingCartItems() {
        cartItemsContainer.getChildren().clear();
        new Thread(() -> {
            // 使用正确的Message构造函数
            Message request = new Message(Type.GET_CART_ITEMS, currentUser.getUserId());
            try {
                Message response = clientService.sendAndReceive(request);
                Platform.runLater(() -> {
                    // 使用正确的成功判断逻辑
                    if (response != null && response.getCode() == Code.SUCCESS) {
                        // 正确地进行类型转换
                        List<ShoppingCartItem> items = (List<ShoppingCartItem>) response.getData();
                        if (items != null && !items.isEmpty()) {
                            for (ShoppingCartItem item : items) {
                                cartItemsContainer.getChildren().add(createCartItemPane(item));
                            }
                        } else {
                            cartItemsContainer.getChildren().add(new Label("购物车是空的。"));
                        }
                    } else {
                        cartItemsContainer.getChildren().add(new Label("加载购物车失败：" + (response != null ? response.getData() : "服务器无响应")));
                    }
                    updateTotal();
                });
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                Platform.runLater(() -> cartItemsContainer.getChildren().add(new Label("网络通信失败，请稍后重试。")));
            }
        }).start();
    }

    /**
     * 创建购物车单个商品面板
     */
    private HBox createCartItemPane(ShoppingCartItem item) {
        HBox itemBox = new HBox(15);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setPadding(new Insets(10));
        itemBox.getStyleClass().add("cart-item-box");

        Label nameLabel = new Label(item.getProductName());
        nameLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        nameLabel.setPrefWidth(200);

        Label priceLabel = new Label("¥ " + String.format("%.2f", item.getProductPrice()));
        priceLabel.setPrefWidth(80);

        HBox quantityBox = new HBox(5);
        quantityBox.setAlignment(Pos.CENTER);
        Button minusButton = new Button("-");
        TextField quantityField = new TextField(String.valueOf(item.getProductNum()));
        quantityField.setPrefWidth(40);
        quantityField.setAlignment(Pos.CENTER);
        Button plusButton = new Button("+");
        quantityBox.getChildren().addAll(minusButton, quantityField, plusButton);

        Label subtotalLabel = new Label("¥ " + String.format("%.2f", item.getProductPrice().multiply(new BigDecimal(item.getProductNum()))));
        subtotalLabel.setPrefWidth(80);
        subtotalLabel.setAlignment(Pos.CENTER_RIGHT);

        Button deleteButton = new Button("删除");
        deleteButton.getStyleClass().add("delete-button");

        itemBox.getChildren().addAll(nameLabel, priceLabel, quantityBox, subtotalLabel, deleteButton);

        // 按钮事件处理
        minusButton.setOnAction(e -> updateQuantity(item, -1));
        plusButton.setOnAction(e -> updateQuantity(item, 1));
        deleteButton.setOnAction(e -> handleDeleteItem(item));

        return itemBox;
    }

    /**
     * 更新购物车商品数量
     */
    private void updateQuantity(ShoppingCartItem item, int delta) {
        int newNum = item.getProductNum() + delta;
        if (newNum <= 0) {
            handleDeleteItem(item);
            return;
        }

        // 发送更新请求到服务器
        new Thread(() -> {
            item.setProductNum(newNum); // 更新本地对象
            // 使用正确的Message构造函数
            Message request = new Message(Type.UPDATE_CART_ITEM, item);
            try {
                Message response = clientService.sendAndReceive(request);
                Platform.runLater(() -> {
                    // 使用正确的成功判断逻辑
                    if (response != null && response.getCode() == Code.SUCCESS) {
                        loadShoppingCartItems(); // 重新加载以更新界面
                    } else {
                        new Alert(Alert.AlertType.ERROR, "更新数量失败：" + (response != null ? response.getData() : "服务器无响应")).show();
                        item.setProductNum(newNum - delta); // 恢复本地对象
                    }
                });
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "网络通信失败，请稍后重试。").show());
            }
        }).start();
    }

    /**
     * 删除购物车商品
     */
    private void handleDeleteItem(ShoppingCartItem item) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "确定要从购物车中删除该商品吗？", ButtonType.YES, ButtonType.NO);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                new Thread(() -> {
                    // 使用正确的Message构造函数
                    Message request = new Message(Type.REMOVE_CART_ITEM, item);
                    try {
                        Message responseFromServer = clientService.sendAndReceive(request);
                        Platform.runLater(() -> {
                            // 使用正确的成功判断逻辑
                            if (responseFromServer != null && responseFromServer.getCode() == Code.SUCCESS) {
                                loadShoppingCartItems();
                                parentPanel.updateShoppingCartCount();
                            } else {
                                new Alert(Alert.AlertType.ERROR, "删除失败：" + (responseFromServer != null ? responseFromServer.getData() : "服务器无响应")).show();
                            }
                        });
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "网络通信失败，请稍后重试。").show());
                    }
                }).start();
            }
        });
    }

    /**
     * 更新总金额
     */
    private void updateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (javafx.scene.Node node : cartItemsContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox itemBox = (HBox) node;
                Label subtotalLabel = (Label) itemBox.getChildren().get(3);
                String subtotalText = subtotalLabel.getText().replace("¥ ", "");
                total = total.add(new BigDecimal(subtotalText));
            }
        }
        totalLabel.setText("总计：¥ " + String.format("%.2f", total));
    }

    /**
     * 处理购买
     */
    private void handleCheckout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "确定购买购物车中的所有商品吗？", ButtonType.YES, ButtonType.NO);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                new Thread(() -> {
                    try {
                        // 获取购物车商品列表
                        Message getItemsRequest = new Message(Type.GET_CART_ITEMS, currentUser.getUserId());
                        Message getItemsResponse = clientService.sendAndReceive(getItemsRequest);

                        // 使用正确的成功判断逻辑
                        if (getItemsResponse != null && getItemsResponse.getCode() == Code.SUCCESS) {
                            // 正确地进行类型转换
                            List<ShoppingCartItem> cartItems = (List<ShoppingCartItem>) getItemsResponse.getData();
                            if (cartItems == null || cartItems.isEmpty()) {
                                Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "购物车是空的！").show());
                                return;
                            }

                            // 发送购买请求
                            Message checkoutRequest = new Message(Type.CHECKOUT, currentUser.getUserId());
                            Message checkoutResponse = clientService.sendAndReceive(checkoutRequest);

                            Platform.runLater(() -> {
                                // 使用正确的成功判断逻辑
                                if (checkoutResponse != null && checkoutResponse.getCode() == Code.SUCCESS) {
                                    new Alert(Alert.AlertType.INFORMATION, "购买成功！订单已生成。").show();
                                    this.close(); // 关闭购物车弹窗
                                    parentPanel.updateShoppingCartCount(); // 更新父面板购物车数量
                                } else {
                                    new Alert(Alert.AlertType.ERROR, "购买失败：" + (checkoutResponse != null ? checkoutResponse.getData() : "服务器无响应")).show();
                                }
                            });
                        } else {
                            Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "购买失败，无法获取购物车信息：" + (getItemsResponse != null ? getItemsResponse.getData() : "服务器无响应")).show());
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "网络通信失败，请稍后重试。").show());
                    }
                }).start();
            }
        });
    }
}