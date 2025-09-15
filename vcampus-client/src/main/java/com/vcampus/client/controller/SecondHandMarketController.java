//yhr9.14 23:13添加该类
package com.vcampus.client.controller;

import com.vcampus.client.service.ClientService;
import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.SecondHandItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

/**
 * 二手市场面板控制器
 */
public class SecondHandMarketController {

    // 注入 FXML 中定义的 UI 元素
    @FXML private TableView<SecondHandItem> itemTableView;
    @FXML private TableColumn<SecondHandItem, String> nameColumn;
    @FXML private TableColumn<SecondHandItem, BigDecimal> priceColumn;
    @FXML private TableColumn<SecondHandItem, String> descriptionColumn;
    @FXML private TableColumn<SecondHandItem, Integer> stockColumn;
    @FXML private TableColumn<SecondHandItem, String> posterColumn;
    @FXML private TableColumn<SecondHandItem, String> postTimeColumn;
    @FXML private TextField searchField;
    @FXML private Button wantButton;
    @FXML private Button postButton;
    @FXML private Button myPostsButton;
    @FXML private Button myWantsButton;

    // 核心服务实例，通过 VCampusClient 注入
    private ClientService clientService;
    private ObservableList<SecondHandItem> itemList;

    /**
     * 依赖注入方法，由主应用调用
     */
    public void setClientService(ClientService clientService) {
        this.clientService = clientService;
        // 在服务设置后，加载初始数据
        loadAllItems();
    }

    /**
     * FXML 初始化方法
     */
    @FXML
    private void initialize() {
        // 配置 TableView 的列与 SecondHandItem 类的属性绑定
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        posterColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        postTimeColumn.setCellValueFactory(new PropertyValueFactory<>("postTime"));

        itemList = FXCollections.observableArrayList();
        itemTableView.setItems(itemList);
    }

    /**
     * 加载所有二手商品列表
     */
    private void loadAllItems() {
        if (clientService == null) {
            showAlert(Alert.AlertType.ERROR, "错误", "客户端服务未初始化。");
            return;
        }
        Message response = clientService.getAllItems();
        if (response != null && response.isSuccess()) {
            List<SecondHandItem> items = (List<SecondHandItem>) response.getData();
            itemList.setAll(items);
        } else {
            showAlert(Alert.AlertType.ERROR, "加载失败", "加载二手商品列表失败：" + response.getData());
        }
    }

    /**
     * 搜索按钮事件处理
     */
    @FXML
    private void handleSearch() {
        if (clientService == null) {
            showAlert(Alert.AlertType.ERROR, "错误", "客户端服务未初始化。");
            return;
        }
        String keyword = searchField.getText();
        Message response = clientService.searchItems(keyword);
        if (response != null && response.isSuccess()) {
            List<SecondHandItem> items = (List<SecondHandItem>) response.getData();
            itemList.setAll(items);
        } else {
            showAlert(Alert.AlertType.ERROR, "搜索失败", "搜索失败：" + response.getData());
        }
    }

    /**
     * 发布闲置按钮事件处理
     */
    @FXML
    private void handlePost() {
        if (clientService == null) {
            showAlert(Alert.AlertType.ERROR, "错误", "客户端服务未初始化。");
            return;
        }
        Dialog<SecondHandItem> dialog = createPostItemDialog();
        dialog.showAndWait().ifPresent(newItem -> {
            Message response = clientService.postNewItem(newItem);
            if (response != null && response.isSuccess()) {
                showAlert(Alert.AlertType.INFORMATION, "发布成功", "商品已成功发布！");
                loadAllItems(); // 重新加载列表
            } else {
                showAlert(Alert.AlertType.ERROR, "发布失败", "发布失败：" + response.getData());
            }
        });
    }

    /**
     * 我的发布按钮事件处理
     */
    @FXML
    private void handleMyPosts() {
        if (clientService == null) {
            showAlert(Alert.AlertType.ERROR, "错误", "客户端服务未初始化。");
            return;
        }
        Message response = clientService.getMyPostedItems();
        if (response != null && response.isSuccess()) {
            List<SecondHandItem> items = (List<SecondHandItem>) response.getData();
            itemList.setAll(items);
        } else {
            showAlert(Alert.AlertType.ERROR, "加载失败", "加载我的发布失败：" + response.getData());
        }
    }

    /**
     * 我的想要按钮事件处理
     */
    @FXML
    private void handleMyWants() {
        if (clientService == null) {
            showAlert(Alert.AlertType.ERROR, "错误", "客户端服务未初始化。");
            return;
        }
        Message response = clientService.getMyWantedItems();
        if (response != null && response.isSuccess()) {
            List<SecondHandItem> items = (List<SecondHandItem>) response.getData();
            itemList.setAll(items);
        } else {
            showAlert(Alert.AlertType.ERROR, "加载失败", "加载我的想要失败：" + response.getData());
        }
    }

    /**
     * 我想要按钮事件处理
     */
    @FXML
    private void handleWant() {
        if (clientService == null) {
            showAlert(Alert.AlertType.ERROR, "错误", "客户端服务未初始化。");
            return;
        }
        SecondHandItem selectedItem = itemTableView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "未选择", "请先选择一个商品。");
            return;
        }

        Message response = clientService.addWantedItem(selectedItem.getItemId());
        if (response != null && response.isSuccess()) {
            showAlert(Alert.AlertType.INFORMATION, "成功", "已成功添加到“我的想要”列表。");
        } else {
            showAlert(Alert.AlertType.ERROR, "失败", "操作失败：" + response.getData());
        }
    }

    // 辅助方法：创建并显示发布商品对话框
    private Dialog<SecondHandItem> createPostItemDialog() {
        Dialog<SecondHandItem> dialog = new Dialog<>();
        dialog.setTitle("发布闲置商品");
        dialog.setHeaderText(null);

        // UI 元素
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField();
        TextArea descriptionArea = new TextArea();
        TextField priceField = new TextField();
        TextField stockField = new TextField();
        Label imagePathLabel = new Label("未选择图片");
        Button selectImageButton = new Button("选择图片");

        grid.add(new Label("商品名称:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("描述:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("价格:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("数量:"), 0, 3);
        grid.add(stockField, 1, 3);
        grid.add(new Label("图片:"), 0, 4);
        grid.add(selectImageButton, 1, 4);
        grid.add(imagePathLabel, 1, 5);

        // 按钮
        ButtonType postButtonType = new ButtonType("发布", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(postButtonType, ButtonType.CANCEL);

        // 事件处理
        selectImageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File file = fileChooser.showOpenDialog(new Stage());
            if (file != null) {
                imagePathLabel.setText(file.getAbsolutePath());
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == postButtonType) {
                try {
                    SecondHandItem newItem = new SecondHandItem();
                    newItem.setItemName(nameField.getText());
                    newItem.setDescription(descriptionArea.getText());
                    newItem.setPrice(new BigDecimal(priceField.getText()));
                    newItem.setStock(Integer.parseInt(stockField.getText()));
                    newItem.setImageUrl(imagePathLabel.getText());
                    return newItem;
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "输入错误", "价格或数量格式不正确。");
                    return null;
                }
            }
            return null;
        });

        dialog.getDialogPane().setContent(grid);
        return dialog;
    }

    // 辅助方法：显示弹窗
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}