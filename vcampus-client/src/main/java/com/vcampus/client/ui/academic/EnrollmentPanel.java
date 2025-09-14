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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 学籍管理（学生端）
 * 1) 顶部显示学籍档案（只读）
 * 2) “信息变更...” 弹窗填写修改项与原因 -> 进入审批流
 * 3) “特殊申请” 菜单：休学/退学/复学 -> 弹窗填写原因 -> 进入审批流
 * 4) 底部显示“最近一次申请状态”与刷新
 * 5) “返回”回到 AcademicSystemPanel（修复返回空白页）
 */
@SuppressWarnings("all")
public class EnrollmentPanel {
    private final ClientService client;
    private final User currentUser;
    private final StackPane contentArea;

    // 只读展示控件
    private TextField tfStudentId;
    private Spinner<Integer> spEnrollYear;
    private Spinner<Integer> spGraduateYear;
    private TextField tfAdvisorId;
    private Label lbStatus;
    private Label lbGpa;
    private Label lbTotalScore;

    // 交互控件
    private Button btnRefresh;
    private Button btnChange;      // 信息变更…
    private MenuButton btnSpecial; // 特殊申请
    private Button btnBack;

    // 申请提示
    private Label lbLatest;

    private VBox root;

    public EnrollmentPanel(ClientService clientService, User currentUser, StackPane contentArea) {
        this.client = clientService;
        this.currentUser = currentUser;
        this.contentArea = contentArea;
    }

    /** AcademicSystemPanel 会调用 build() 把 Pane 放到 contentArea */
    public Pane build() {
        root = new VBox(18);
        root.setPadding(new Insets(24, 28, 28, 28));
        root.setStyle("-fx-background-color: transparent;");

        // 顶部：返回 + 标题
        HBox header = new HBox(12);
        btnBack = new Button("← 返回");
        // 修复：返回到教务首页，而不是清空造成空白页
        btnBack.setOnAction(e -> new AcademicSystemPanel(client, currentUser, contentArea).showAcademicSystem());
        Label title = new Label("学籍管理（学生）");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#4a7c59"));
        header.getChildren().addAll(btnBack, title);
        header.setAlignment(Pos.CENTER_LEFT);

        // 卡片
        VBox card = new VBox(14);
        card.setPadding(new Insets(22));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,.06), 14, 0, 0, 2);");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);

        int row = 0;
        form.add(new Label("学号:"), 0, row);
        tfStudentId = new TextField();
        tfStudentId.setEditable(false);
        form.add(tfStudentId, 1, row++);

        form.add(new Label("入学年:"), 0, row);
        spEnrollYear = new Spinner<>(2000, 2100, 2023);
        spEnrollYear.setEditable(false); // 只读展示
        spEnrollYear.setDisable(true);
        form.add(spEnrollYear, 1, row++);

        form.add(new Label("毕业年:"), 0, row);
        spGraduateYear = new Spinner<>(2000, 2100, 2027);
        spGraduateYear.setEditable(false);
        spGraduateYear.setDisable(true);
        form.add(spGraduateYear, 1, row++);

        form.add(new Label("导师ID:"), 0, row);
        tfAdvisorId = new TextField();
        tfAdvisorId.setEditable(false);
        form.add(tfAdvisorId, 1, row++);

        form.add(new Label("学籍状态:"), 0, row);
        lbStatus = new Label("-");
        form.add(lbStatus, 1, row++);

        form.add(new Label("GPA（只读）:"), 0, row);
        lbGpa = new Label("-");
        form.add(lbGpa, 1, row++);

        form.add(new Label("总学分（只读）:"), 0, row);
        lbTotalScore = new Label("-");
        form.add(lbTotalScore, 1, row++);

        // 最近一次申请状态
        lbLatest = new Label("最近申请：加载中…");
        lbLatest.setStyle("-fx-text-fill: #666;");
        form.add(lbLatest, 1, row++);

        // 按钮区
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        btnRefresh = new Button("刷新");
        btnChange  = new Button("信息变更…");  // 弹窗
        btnSpecial = new MenuButton("特殊申请");
        MenuItem miSuspend = new MenuItem("申请休学");
        MenuItem miDropout = new MenuItem("申请退学");
        MenuItem miResume  = new MenuItem("申请复学");
        btnSpecial.getItems().addAll(miSuspend, miDropout, miResume);

        actions.getChildren().addAll(btnRefresh, btnChange, btnSpecial);

        card.getChildren().addAll(form, actions);

        root.getChildren().addAll(header, card);

        // === 绑定事件 ===
        btnRefresh.setOnAction(e -> {
            loadProfile();
            loadLatestApplication();
        });
        btnChange.setOnAction(e -> showInfoChangeDialog());
        miSuspend.setOnAction(e -> showSpecialDialog("SUSPEND"));
        miDropout.setOnAction(e -> showSpecialDialog("DROP_OUT"));
        miResume.setOnAction(e -> showSpecialDialog("RESUME"));

        // 初次进入自动加载
        loadProfile();
        loadLatestApplication();
        return root;
    }

    /** 拉取当前学籍信息并刷新展示 */
    private void loadProfile() {
        disableAll(true);
        new Thread(() -> {
            try {
                Message resp = client.request(Message.Type.ENROLLMENT_PROFILE_GET, new HashMap<>());
                Platform.runLater(() -> {
                    disableAll(false);
                    if (resp.getCode() == Message.Code.SUCCESS && resp.getData() instanceof Map) {
                        Map data = (Map) resp.getData();
                        fillForm(data);
                    } else {
                        alert("加载学籍资料失败：" + resp.getData(), Alert.AlertType.ERROR);
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

    /** 最近一次申请状态 */
    private void loadLatestApplication() {
        new Thread(() -> {
            try {
                Map<String, Object> p = new HashMap<>();
                // 学生端不需要传 studentId，服务端会用当前用户
                Message resp = client.request(Message.Type.ENROLLMENT_REQUEST_LIST, p);
                Platform.runLater(() -> {
                    if (resp.getCode() == Message.Code.SUCCESS && resp.getData() instanceof Map) {
                        Map data = (Map) resp.getData();
                        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("requests");
                        if (list == null || list.isEmpty()) {
                            lbLatest.setText("最近申请：暂无");
                        } else {
                            Map<String, Object> first = list.get(0);
                            String type = s(first, "changeType");
                            String status = s(first, "status");
                            String submit = String.valueOf(first.get("submitTime"));
                            lbLatest.setText("最近申请：" + typeName(type) + "，状态：" + status + "，时间：" + submit);
                        }
                    } else {
                        lbLatest.setText("最近申请：加载失败");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> lbLatest.setText("最近申请：网络错误"));
            }
        }).start();
    }

    /** 用当前资料填充展示 */
    private void fillForm(Map data) {
        tfStudentId.setText(str(data, "studentId", currentUser.getUserId()));
        spEnrollYear.getValueFactory().setValue(intVal(data, "enrollYear", 2023));
        spGraduateYear.getValueFactory().setValue(intVal(data, "graduateYear", 2027));
        tfAdvisorId.setText(str(data, "advisorId", ""));
        lbStatus.setText(str(data, "status", "在读"));
        lbGpa.setText(str(data, "gpa", "-"));
        lbTotalScore.setText(str(data, "totalScore", "-"));
    }

    /** 打开“信息变更”弹窗（不含学籍状态；状态走特殊申请） */
    private void showInfoChangeDialog() {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("信息变更");
        dialog.setHeaderText("填写要修改的字段（留空则不修改）");

        ButtonType submitType = new ButtonType("提交申请", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, submitType);

        GridPane gp = new GridPane();
        gp.setHgap(10);
        gp.setVgap(12);
        gp.setPadding(new Insets(10));

        Spinner<Integer> eYear = new Spinner<>(2000, 2100, spEnrollYear.getValue());
        Spinner<Integer> gYear = new Spinner<>(2000, 2100, spGraduateYear.getValue());
        TextField advisor = new TextField(tfAdvisorId.getText());
        TextArea reason = new TextArea();
        reason.setPromptText("请填写申请原因（必填）");
        reason.setPrefRowCount(4);

        int r = 0;
        gp.add(new Label("入学年:"), 0, r); gp.add(eYear, 1, r++);
        gp.add(new Label("毕业年:"), 0, r); gp.add(gYear, 1, r++);
        gp.add(new Label("导师ID:"), 0, r); gp.add(advisor, 1, r++);
        gp.add(new Label("申请原因:"), 0, r); gp.add(reason, 1, r++);

        dialog.getDialogPane().setContent(gp);

        dialog.setResultConverter(bt -> {
            if (bt == submitType) {
                Map<String, Object> changes = new LinkedHashMap<>();
                changes.put("enrollYear", eYear.getValue());
                changes.put("graduateYear", gYear.getValue());
                changes.put("advisorId", advisor.getText().trim());

                Map<String, Object> data = new HashMap<>();
                data.put("changeType", "INFO_UPDATE");
                data.put("changes", changes);
                data.put("reason", reason.getText() == null ? "" : reason.getText().trim());
                return data;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(this::doSubmit);
    }

    /** 打开“特殊申请”（休学/退学/复学）弹窗 */
    private void showSpecialDialog(String type) {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("特殊申请 - " + typeName(type));
        dialog.setHeaderText("请填写申请原因");

        ButtonType submitType = new ButtonType("提交申请", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, submitType);

        GridPane gp = new GridPane();
        gp.setHgap(10);
        gp.setVgap(12);
        gp.setPadding(new Insets(10));

        TextArea reason = new TextArea();
        reason.setPromptText("请填写申请原因（必填）");
        reason.setPrefRowCount(5);

        gp.add(new Label("申请原因:"), 0, 0);
        gp.add(reason, 1, 0);

        dialog.getDialogPane().setContent(gp);

        dialog.setResultConverter(bt -> {
            if (bt == submitType) {
                Map<String, Object> data = new HashMap<>();
                data.put("changeType", type); // SUSPEND/DROP_OUT/RESUME
                data.put("changes", new HashMap<String, Object>());
                data.put("reason", reason.getText() == null ? "" : reason.getText().trim());
                return data;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(this::doSubmit);
    }

    /** 调用服务器提交申请，并给出友好提示；成功后刷新资料与“最近申请” */
    private void doSubmit(Map<String, Object> data) {
        String type = String.valueOf(data.get("changeType"));
        String okText = switch (type) {
            case "SUSPEND" -> "休学申请已提交，待审核。";
            case "DROP_OUT" -> "退学申请已提交，待审核。";
            case "RESUME" -> "复学申请已提交，待审核。";
            default -> "学籍变更申请已提交，待教务审核后生效。";
        };

        if (!confirm("确认提交该申请吗？\n提交后将进入教务审批流程。")) return;

        disableAll(true);
        new Thread(() -> {
            try {
                Message resp = client.request(Message.Type.ENROLLMENT_REQUEST_SUBMIT, data);
                Platform.runLater(() -> {
                    disableAll(false);
                    if (resp.getCode() == Message.Code.SUCCESS) {
                        alert(okText, Alert.AlertType.INFORMATION);
                        loadProfile();
                        loadLatestApplication();
                    } else {
                        alert("提交失败：" + resp.getData(), Alert.AlertType.ERROR);
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

    // 小工具
    private void disableAll(boolean b) {
        for (Node n : root.lookupAll("*")) {
            if (n instanceof Control) ((Control) n).setDisable(b);
        }
    }

    private static String str(Map m, String k, String def) {
        Object v = m == null ? null : m.get(k);
        return v == null ? def : String.valueOf(v);
    }

    private static int intVal(Map m, String k, int def) {
        Object v = m == null ? null : m.get(k);
        if (v == null) return def;
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return def; }
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

    private String s(Map<String, Object> m, String k) {
        Object v = m == null ? null : m.get(k);
        return v == null ? "" : String.valueOf(v);
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
