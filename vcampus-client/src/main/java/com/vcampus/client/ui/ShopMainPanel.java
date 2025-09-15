//yhr9.14 8：29修改该类
package com.vcampus.client.ui;

import com.vcampus.client.service.ClientService;
import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.Message.Code;
import com.vcampus.common.entity.Message.Type;
import com.vcampus.common.entity.Product;
import com.vcampus.common.entity.ShoppingCartItem;
import com.vcampus.common.entity.User;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.util.List;

/**
 * 商城主界面
 */
public class ShopMainPanel extends VBox {

    private final User currentUser;
    private final ClientService clientService;
    private FlowPane productGrid;
    private TextField searchField;
    private Label shoppingCartCountLabel;

    public ShopMainPanel(User currentUser, ClientService clientService) {
        this.currentUser = currentUser;
        this.clientService = clientService;

        this.setPadding(new Insets(20));
        this.setSpacing(20);
        this.getStyleClass().add("shop-main-panel");
        this.setAlignment(Pos.TOP_CENTER);

        HBox topBar = createTopBar();
        this.getChildren().add(topBar);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("product-scroll-pane");

        productGrid = new FlowPane();
        productGrid.setHgap(30);
        productGrid.setVgap(30);
        productGrid.setPadding(new Insets(20));
        productGrid.setAlignment(Pos.TOP_LEFT);

        scrollPane.setContent(productGrid);
        this.getChildren().add(scrollPane);

        loadProducts("");
        updateShoppingCartCount();
    }

    /**
     * 创建顶部搜索和购物车栏
     */
    private HBox createTopBar() {
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER);
        topBar.getStyleClass().add("shop-top-bar");

        Label titleLabel = new Label("校园商城");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
        titleLabel.getStyleClass().add("shop-title");

        HBox searchBox = new HBox(5);
        searchBox.setAlignment(Pos.CENTER);
        searchBox.setPrefWidth(500);

        searchField = new TextField();
        searchField.setPromptText("搜索商品名称...");
        searchField.setPrefHeight(40);
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button searchButton = new Button("搜索");
        searchButton.setPrefHeight(40);
        searchButton.getStyleClass().add("search-button");
        searchButton.setOnAction(e -> {
            loadProducts(searchField.getText().trim());
        });

        searchBox.getChildren().addAll(searchField, searchButton);
        HBox.setHgrow(searchBox, Priority.ALWAYS);

        // 购物车图标按钮
        StackPane cartIconPane = new StackPane();

        // 直接使用 Label 和 Unicode 字符来创建图标
        Label cartIconLabel = new Label("🛒");
        cartIconLabel.setStyle("-fx-font-size: 20px;"); // 可以通过内联样式或CSS类控制大小

        Button cartButton = new Button("", cartIconLabel);
        cartButton.getStyleClass().add("cart-button");
        cartButton.setPrefSize(40, 40);
        cartButton.setOnAction(e -> {
            new ShoppingCartDialog(currentUser, clientService, this).show();
        });

        shoppingCartCountLabel = new Label("0");
        shoppingCartCountLabel.getStyleClass().add("cart-count-label");

        cartIconPane.getChildren().addAll(cartButton, shoppingCartCountLabel);
        StackPane.setAlignment(shoppingCartCountLabel, Pos.TOP_RIGHT);
        StackPane.setMargin(shoppingCartCountLabel, new Insets(0, -8, 0, 0));

        topBar.getChildren().addAll(titleLabel, new Region(), searchBox, cartIconPane);
        return topBar;
    }

    /**
     * 加载商品列表
     */
    private void loadProducts(String keyword) {
        productGrid.getChildren().clear();

        Message msg;
        if (keyword == null || keyword.isEmpty()) {
            //msg = new Message(Message.Type.GET_ALL_PRODUCTS, null);
            //yhr9.14 8：36修改
            // 将GET_ALL_PRODUCTS 修改为 SHOP_LIST，以匹配服务器端的消息类型
            msg = new Message(Type.SHOP_LIST, null);
        } else {
            msg = new Message(Type.SEARCH_PRODUCT, keyword);
        }

        try {
            Message response = clientService.sendAndReceive(msg);

            if (response != null && response.getCode() == Code.SUCCESS) {
                List<Product> products = (List<Product>) response.getData();
                if (products != null && !products.isEmpty()) {
                    for (Product product : products) {
                        productGrid.getChildren().add(createProductCard(product));
                    }
                } else {
                    productGrid.getChildren().add(new Label("没有找到任何商品。"));
                }
            } else {
                productGrid.getChildren().add(new Label("加载商品失败：" + (response != null ? response.getData() : "服务器无响应")));
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            productGrid.getChildren().add(new Label("网络通信失败，请稍后重试。"));
        }
    }





    /**
     * 创建单个商品卡片
     */
    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("product-card");
        card.setPrefSize(200, 280);
        card.setAlignment(Pos.CENTER);

        // 商品图片
        ImageView imageView = new ImageView();
        Image productImage = null;

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            try {
                // 使用绝对路径加载图片
                productImage = new Image(product.getImageUrl());
            } catch (Exception e) {
                System.err.println("无法加载商品图片: " + product.getImageUrl());
                productImage = new Image(getClass().getResourceAsStream("/images/image_not_found.png"));
            }
        } else {
            productImage = new Image(getClass().getResourceAsStream("/images/default_product.png"));
        }

        imageView.setImage(productImage);
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        // 商品名称
        Label nameLabel = new Label(product.getProductName());
        nameLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        nameLabel.getStyleClass().add("product-name");
        nameLabel.setWrapText(true);

        // 商品价格
        Label priceLabel = new Label("¥ " + String.format("%.2f", product.getPrice()));
        priceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        priceLabel.getStyleClass().add("product-price");

        // 按钮容器
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getStyleClass().add("card-button-box");

        Button detailButton = new Button("详情");
        detailButton.getStyleClass().add("detail-button");
        detailButton.setPrefWidth(80);
        detailButton.setOnAction(e -> {
            new ProductDetailDialog(product).show();
        });

        Button addToCartButton = new Button("加入购物车");
        addToCartButton.getStyleClass().add("add-to-cart-button");
        addToCartButton.setPrefWidth(100);
        addToCartButton.setOnAction(e -> {
            handleAddToCart(product);
        });

        buttonBox.getChildren().addAll(detailButton, addToCartButton);

        card.getChildren().addAll(imageView, nameLabel, priceLabel, buttonBox);
        return card;
    }

    /**
     * 处理“加入购物车”操作
     */
    private void handleAddToCart(Product product) {
        if (product.getStock() <= 0) {
            new Alert(Alert.AlertType.WARNING, "抱歉，该商品库存不足！").show();
            return;
        }

        // 创建购物车项
        ShoppingCartItem item = new ShoppingCartItem(currentUser.getUserId(), product.getProductId(), 1);

        // 发送消息到服务器
        Message request = new Message(Type.ADD_TO_CART, item);
        try {
            Message response = clientService.sendAndReceive(request);

            if (response != null && response.getCode() == Code.SUCCESS) {
                new Alert(Alert.AlertType.INFORMATION, "成功加入购物车！").show();
                updateShoppingCartCount(); // 更新购物车数量显示
            } else {
                new Alert(Alert.AlertType.ERROR, "加入购物车失败：" + (response != null ? response.getData() : "服务器无响应")).show();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "网络通信失败，请稍后重试。").show();
        }
    }

    /**
     * 更新购物车数量显示
     */
    public void updateShoppingCartCount() {
        new Thread(() -> {
            Message request = new Message(Type.GET_CART_COUNT, currentUser.getUserId());
            try {
                Message response = clientService.sendAndReceive(request);
                Platform.runLater(() -> {
                    if (response != null && response.getCode() == Code.SUCCESS) {
                        int count = (int) response.getData();
                        shoppingCartCountLabel.setText(String.valueOf(count));
                    }
                });
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();
    }
}