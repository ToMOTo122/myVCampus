package com.vcampus.client.ui.academic;

import com.vcampus.client.service.ClientService;
import com.vcampus.client.ui.AcademicSystemPanel;
import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.User;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.*;

/**
 * 学籍审批（教师/管理员）
 * - 左侧：待审列表
 * - 右侧：详情 + 通过/驳回
 * - 返回：回到 AcademicSystemPanel（修复返回空白页）
 */
@SuppressWarnings("all")
public class EnrollmentAdminPanel {
    private final ClientService client;
    private final User currentUser;
    private final StackPane contentArea;

    private ListView<Map<String, Object>> lvPending;
    private TextArea taDetail;
    private Button btnApprove, btnReject, btnRefresh, btnBack;

    private VBox root;

    public EnrollmentAdminPanel(ClientService clientService, User currentUser, StackPane contentArea) {
        this.client = clientService;
        this.currentUser = currentUser;
        this.contentArea = contentArea;
    }

    public Pane build() {
        root = new VBox(18);
        root.setPadding(new Insets(24, 28, 28, 28));

        HBox header = new HBox(12);
        btnBack = new Button("← 返回");
        btnBack.setOnAction(e -> new AcademicSystemPanel(client, currentUser, contentArea).showAcademicSystem());
        Label title = new Label("学籍审批（教师/管理员）");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#4a7c59"));
        header.getChildren().addAll(btnBack, title);
        header.setAlignment(Pos.CENTER_LEFT);

        HBox main = new HBox(16);

        // 待审列表卡片
        VBox cardLeft = new VBox(10);
        cardLeft.setPadding(new Insets(16));
        cardLeft.setStyle("-fx-background-color: white; -fx-background-radius: 10; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,.06), 14, 0, 0, 2);");
        Label l1 = new Label("待审核（待审）");
        lvPending = new ListView<>();
        // 这里改成使用命名参数，而不是 "_"：
        lvPending.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String sid = s(item, "studentId");
                    String type = s(item, "changeType");
                    String status = s(item, "status");
                    setText(String.format("学号 %s | %s | %s", sid, typeName(type), status));
                }
            }
        });
        lvPending.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> showDetail(newV));
        btnRefresh = new Button("刷新列表");
        btnRefresh.setOnAction(e -> loadPending());
        cardLeft.getChildren().addAll(l1, lvPending, btnRefresh);

        // 详情卡片
        VBox cardRight = new VBox(10);
        cardRight.setPadding(new Insets(16));
        cardRight.setStyle("-fx-background-color: white; -fx-background-radius: 10; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,.06), 14, 0, 0, 2);");
        cardRight.getChildren().add(new Label("申请详情"));
        taDetail = new TextArea();
        taDetail.setPromptText("点击左侧项目查看详情…");
        taDetail.setPrefRowCount(18);
        taDetail.setEditable(false);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        btnApprove = new Button("通过");
        btnReject  = new Button("驳回");
        actions.getChildren().addAll(btnApprove, btnReject);

        cardRight.getChildren().addAll(taDetail, actions);

        main.getChildren().addAll(cardLeft, cardRight);

        root.getChildren().addAll(header, main);

        // 事件
        btnApprove.setOnAction(e -> approveSelected(true));
        btnReject.setOnAction(e -> approveSelected(false));

        // 初次载入
        loadPending();
        return root;
    }

    private void loadPending() {
        disableAll(true);
        new Thread(() -> {
            try {
                Map<String, Object> params = new HashMap<>();
                params.put("status", "PENDING");
                Message resp = client.request(Message.Type.ENROLLMENT_REQUEST_LIST, params);
                Platform.runLater(() -> {
                    disableAll(false);
                    if (resp.getCode() == Message.Code.SUCCESS && resp.getData() instanceof Map) {
                        Map data = (Map) resp.getData();
                        java.util.List<Map<String, Object>> list =
                                (java.util.List<Map<String, Object>>) data.getOrDefault("requests", Collections.emptyList());
                        lvPending.getItems().setAll(list);
                        taDetail.setText("点击左侧项目查看详情…");
                    } else {
                        alert("加载列表失败：" + resp.getData(), Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    disableAll(false);
                    alert("网络错误：" + ex.getMessage(), Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    private void showDetail(Map<String, Object> item) {
        if (item == null) {
            taDetail.setText("点击左侧项目查看详情…");
            return;
        }
        disableAll(true);
        new Thread(() -> {
            try {
                Map<String, Object> p = new HashMap<>();
                p.put("id", item.get("id"));
                Message resp = client.request(Message.Type.ENROLLMENT_REQUEST_DETAIL, p);
                Platform.runLater(() -> {
                    disableAll(false);
                    if (resp.getCode() == Message.Code.SUCCESS && resp.getData() instanceof Map) {
                        Map d = (Map) resp.getData();
                        StringBuilder sb = new StringBuilder();
                        sb.append("学号: ").append(s(d, "studentId")).append("\n");
                        sb.append("类型: ").append(typeName(s(d, "changeType"))).append("\n");
                        sb.append("当前状态: ").append(s(d, "status")).append("\n");
                        sb.append("提交时间: ").append(s(d, "submitTime")).append("\n");
                        sb.append("变更内容: ").append(s(d, "changes")).append("\n");
                        sb.append("申请原因: ").append(s(d, "reason")).append("\n");
                        taDetail.setText(sb.toString());
                    } else {
                        alert("加载详情失败：" + resp.getData(), Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    disableAll(false);
                    alert("网络错误：" + ex.getMessage(), Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    private void approveSelected(boolean pass) {
        Map<String, Object> sel = lvPending.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alert("请先选择一条待审核记录。", Alert.AlertType.WARNING);
            return;
        }
        String msg = pass ? "确认通过该申请吗？\n通过后将立即生效。" : "确认驳回该申请吗？";
        if (!confirm(msg)) return;

        disableAll(true);
        new Thread(() -> {
            try {
                Map<String, Object> p = new HashMap<>();
                p.put("id", sel.get("id"));
                p.put("comment", pass ? "同意" : "驳回");
                Message.Type op = pass ? Message.Type.ENROLLMENT_REQUEST_APPROVE
                        : Message.Type.ENROLLMENT_REQUEST_REJECT;
                Message resp = client.request(op, p);
                Platform.runLater(() -> {
                    disableAll(false);
                    if (resp.getCode() == Message.Code.SUCCESS) {
                        alert(pass ? "已通过并生效" : "已驳回", Alert.AlertType.INFORMATION);
                        loadPending();
                    } else {
                        alert("操作失败：" + resp.getData(), Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    disableAll(false);
                    alert("网络错误：" + ex.getMessage(), Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    // ===== 小工具 =====
    private void disableAll(boolean b) {
        for (Node n : root.lookupAll("*")) {
            if (n instanceof Control) ((Control) n).setDisable(b);
        }
    }

    private String s(Map<String, Object> m, String k) {
        Object v = m == null ? null : m.get(k);
        return v == null ? "" : String.valueOf(v);
    }

    private void alert(String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle("提示");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private boolean confirm(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL);
        a.setTitle("确认");
        a.setHeaderText(null);
        a.showAndWait();
        return a.getResult() == ButtonType.OK;
    }

    private String typeName(String t) {
        if (t == null) return "";
        switch (t) {
            case "INFO_UPDATE": return "资料变更";
            case "SUSPEND":     return "休学";
            case "DROP_OUT":    return "退学";
            case "RESUME":      return "复学";
            default:            return t;
        }
    }
}
