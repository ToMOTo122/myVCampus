//yhr9.14 1:19添加该类
package com.vcampus.client.ui;

import com.vcampus.common.entity.Product;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 商品详情弹窗
 */
public class ProductDetailDialog extends Stage {

    public ProductDetailDialog(Product product) {
        this.initModality(Modality.APPLICATION_MODAL);
        this.setTitle("商品详情 - " + product.getProductName());

        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label("商品名称：" + product.getProductName());
        name.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 20));

        Label category = new Label("商品分类：" + product.getCategory());
        Label price = new Label("商品价格：¥ " + String.format("%.2f", product.getPrice()));
        Label stock = new Label("库存：" + product.getStock());
        Label description = new Label("商品描述：" + product.getDescription());
        description.setWrapText(true);

        content.getChildren().addAll(name, category, price, stock, description);

        Scene scene = new Scene(content, 400, 300);
        this.setScene(scene);
    }
}