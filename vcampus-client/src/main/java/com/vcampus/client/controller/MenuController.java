// 文件路径: vcampus-client/src/main/java/com/vcampus/client/controller/MenuController.java

package com.vcampus.client.controller;

import com.vcampus.client.MainApplication;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.util.List;

public class MenuController {

    @FXML
    private VBox menuVBox;

    private MainApplication mainApp;

    // 接受主应用程序的引用
    public void setMainApp(MainApplication mainApp) {
        this.mainApp = mainApp;
    }


    public void initialize() {
        // 在这里为所有按钮添加事件监听器
        List<javafx.scene.Node> children = menuVBox.getChildren();
        for (javafx.scene.Node node : children) {
            if (node instanceof Button) {
                Button button = (Button) node;
                button.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    String menuText = (String) button.getUserData();
                    if (mainApp != null) {
                        mainApp.handleMenuClick(menuText); // 调用主应用程序的方法
                    }
                });
            }
        }
    }
}