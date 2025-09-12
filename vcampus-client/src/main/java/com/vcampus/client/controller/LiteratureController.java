// 文件路径: vcampus-client/src/main/java/com/vcampus/client/controller/LiteratureController.java

package com.vcampus.client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

public class LiteratureController implements javafx.fxml.Initializable {

    // FXML 变量与视图绑定
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private TableView<Literature> literatureTableView;
    @FXML private TableColumn<Literature, String> titleCol;
    @FXML private TableColumn<Literature, String> authorCol;
    @FXML private TableColumn<Literature, String> sourceCol;
    @FXML private TableColumn<Literature, String> publishDateCol;
    @FXML private TableColumn<Literature, Void> actionCol;

    // 数据模型
    private ObservableList<Literature> literatureList = FXCollections.observableArrayList();

    /**
     * FXML加载后自动调用，用于初始化界面
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化表格列
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        sourceCol.setCellValueFactory(new PropertyValueFactory<>("source"));
        publishDateCol.setCellValueFactory(new PropertyValueFactory<>("publishDate"));

        // 设置操作列，添加“查看”按钮
        actionCol.setCellFactory(param -> new TableCell<Literature, Void>() {
            private final Button openButton = new Button("查看");

            {
                // 为按钮添加样式类
                openButton.getStyleClass().add("action-button");
                // 绑定点击事件
                openButton.setOnAction(event -> {
                    Literature literature = getTableView().getItems().get(getIndex());
                    if (literature != null) {
                        handleOpenLink(literature.getLink());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // 将按钮放入HBox中以居中显示
                    HBox buttonBox = new HBox(openButton);
                    buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(buttonBox);
                }
            }
        });

        // 将数据绑定到表格
        literatureTableView.setItems(literatureList);

        // 绑定搜索按钮事件
        searchButton.setOnAction(event -> handleSearch());

        // 加载初始文献数据
        loadInitialData();
    }

    /**
     * 加载初始文献数据（模拟）
     */
    private void loadInitialData() {
        literatureList.setAll(
                new Literature("联合国框架下的人工智能科技治理：进展、影响与发展方向", "张庚炎", "国际展望", "2025-09-11", "https://kns.cnki.net/kns/brief/result.aspx?dbPrefix=CJFQ&clusterResult=false&keyword=%e4%ba%ba%e5%b7%a5%e6%99%ba%e8%83%bd"),
                new Literature("人工智能赋能主流意识形态的传播风险及其规制", "郑林，管荣君", "理论探索", "2025-09-11", "https://kns.cnki.net/kns/brief/result.aspx?dbPrefix=CJFQ&clusterResult=false&keyword=%e4%ba%ba%e5%b7%a5%e6%99%ba%e8%83%bd"),
                new Literature("超极智能增强与求真社会建构", "潘德宏", "理论探索", "2025-09-11", "https://kns.cnki.net/kns/brief/result.aspx?dbPrefix=CJFQ&clusterResult=false&keyword=%e4%ba%ba%e5%b7%a5%e6%99%ba%e8%83%bd"),
                new Literature("中国携手“全球南方”倡导践行真正的多边主义的理论与实践逻辑", "李猛", "南亚研究", "2025-09-11", "https://kns.cnki.net/kns/brief/result.aspx?dbPrefix=CJFQ&clusterResult=false&keyword=%e4%ba%ba%e5%b7%a5%e6%99%ba%e8%83%bd"),
                new Literature("基于Logistic回归分析探讨无创产前检查筛查失败的影响因素", "大连医科大学", "大连医科大学", "2021-02-01", "https://kns.cnki.net/kns/brief/result.aspx?dbPrefix=CJFQ&clusterResult=false&keyword=%e6%97%a0%e5%88%9b%e4%ba%a7%e5%89%8d%e6%a3%80%e6%9f%a5"),
                new Literature("无创产前筛查技术在染色体异常及微缺失检测中的应用价值", "延边大学医学学报", "延边大学医学学报", "2025-06-28", "https://kns.cnki.net/kns/brief/result.aspx?dbPrefix=CJFQ&clusterResult=false&keyword=%e6%97%a0%e5%88%9b%e4%ba%a7%e5%89%8d%e6%a3%80%e6%9f%a5"),
                new Literature("数字时代背景下新闻生产的模式变革与发展路径", "黄传文", "传媒", "2024-05-15", "https://kns.cnki.net/kns/brief/result.aspx?dbPrefix=CJFQ&clusterResult=false&keyword=%e6%95%b0%e5%ad%97%e6%97%b6%e4%bb%a3"),
                new Literature("《人工智能》期末论文", "张三", "CSDN", "2025-01-01", "https://blog.csdn.net/your_blog_link")
        );
    }

    /**
     * 处理搜索事件
     */
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        // 这里可以添加逻辑来根据关键词从数据库或API获取数据
        // 为简化示例，我们不做任何处理
        System.out.println("正在搜索文献：" + keyword);
    }

    /**
     * 处理打开链接事件，使用本地浏览器打开指定URL
     * @param url 要打开的链接
     */
    private void handleOpenLink(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                System.out.println("无法在您的操作系统上打开浏览器。");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 辅助内部类，用于存储文献信息
     */
    public static class Literature {
        private final String title;
        private final String author;
        private final String source;
        private final String publishDate;
        private final String link;

        public Literature(String title, String author, String source, String publishDate, String link) {
            this.title = title;
            this.author = author;
            this.source = source;
            this.publishDate = publishDate;
            this.link = link;
        }

        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public String getSource() { return source; }
        public String getPublishDate() { return publishDate; }
        public String getLink() { return link; }
    }
}