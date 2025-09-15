// 文件路径: vcampus-client/src/main/java/com/vcampus/client/controller/BookListController.java

package com.vcampus.client.controller;

import com.vcampus.client.service.ClientService;
import com.vcampus.common.entity.Book;
import com.vcampus.common.entity.BorrowRecord;
import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.Message.Type;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.geometry.Pos;

import java.awt.Desktop;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 图书馆主控制器 - 处理所有图书馆相关功能
 */
public class BookListController {

    // 客户端服务实例
    private ClientService clientService;
    // 用于通过bookId快速查找图书对象
    private Map<String, Book> bookMap;

    // ========== 图书列表 Tab 控件 ==========
    @FXML private TabPane libraryTabPane;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button refreshButton;
    @FXML private GridPane statsGrid;
    @FXML private Label totalBooksLabel;
    @FXML private Label availableBooksLabel;
    @FXML private Label borrowedBooksLabel;
    @FXML private Label newBooksLabel;
    @FXML private TableView<Book> booksTableView;
    @FXML private TableColumn<Book, String> bookIdCol;
    @FXML private TableColumn<Book, String> titleCol;
    @FXML private TableColumn<Book, String> authorCol;
    @FXML private TableColumn<Book, String> publisherCol;
    @FXML private TableColumn<Book, String> statusCol;
    @FXML private TableColumn<Book, Integer> stockCol;
    @FXML private TableColumn<Book, Void> actionCol;

    // ========== 个人借阅 Tab 控件 ==========
    @FXML private TableView<BorrowRecord> borrowHistoryTable;
    @FXML private TableColumn<BorrowRecord, Integer> borrowIdCol;
    @FXML private TableColumn<BorrowRecord, String> borrowTitleCol;
    @FXML private TableColumn<BorrowRecord, String> borrowDateCol;
    @FXML private TableColumn<BorrowRecord, String> returnDateCol;
    @FXML private TableColumn<BorrowRecord, String> borrowStatusCol;
    @FXML private TableColumn<BorrowRecord, Void> borrowActionCol;
    @FXML private Label currentBorrowLabel;
    @FXML private Label historyBorrowLabel;
    @FXML private Label expiringBooksLabel;
    @FXML private Label overdueBooksLabel;

    // ========== 图书馆活动 Tab 控件 ==========
    @FXML private ListView<Activity> activityListView;
    @FXML private Label activityTitleLabel;
    @FXML private Label activityDateLabel;
    @FXML private Label activityLocationLabel;
    @FXML private Text activityContentText;
    @FXML private Label activityOrganizerLabel;
    @FXML private Label activitySpeakerLabel;
    @FXML private Label activityParticipantsLabel;
    @FXML private Label activityStatusLabel;
    @FXML private Button activityRegisterButton;

    // ========== 文献查阅 Tab 控件 ==========
    @FXML private TextField literatureSearchField;
    @FXML private Button literatureSearchButton;
    @FXML private TableView<Literature> literatureTableView;
    @FXML private TableColumn<Literature, String> literatureTitleCol;
    @FXML private TableColumn<Literature, String> literatureAuthorCol;
    @FXML private TableColumn<Literature, String> literatureSourceCol;
    @FXML private TableColumn<Literature, String> literaturePublishDateCol;
    @FXML private TableColumn<Literature, Void> literatureActionCol;

    // 数据存储
    private ObservableList<Activity> activityList = FXCollections.observableArrayList();
    private ObservableList<Literature> literatureList = FXCollections.observableArrayList();

    /**
     * 设置ClientService实例
     */
    public void setClientService(ClientService clientService) {
        this.clientService = clientService;
        if (clientService != null) {
            loadAllBooks();
            loadBorrowRecords();
            loadActivities();
            loadLiteratures();
        } else {
            showAlert("错误", "ClientService未正确初始化，请检查主程序。");
        }
    }

    @FXML
    public void initialize() {
        System.out.println("BookListController initialize 方法被调用。");
        setupBookListTab();
        setupBorrowHistoryTab();
        setupActivityTab();
        setupLiteratureTab();
    }

    // ========== 图书列表功能 ==========

    /**
     * 加载所有图书数据
     */
    private void loadAllBooks() {
        try {
            Message request = new Message(Type.BOOK_LIST, null);
            Message response = clientService.sendRequest(request);

            if (response != null && response.getCode() == Message.Code.SUCCESS) {
                Object data = response.getData();
                if (data instanceof List<?>) {
                    @SuppressWarnings("unchecked")
                    List<Book> books = (List<Book>) data;
                    bookMap = books.stream().collect(Collectors.toMap(Book::getBookId, Function.identity()));
                    System.out.println("成功加载 " + books.size() + " 本图书数据");
                    booksTableView.setItems(FXCollections.observableArrayList(bookMap.values()));
                    updateBookStats(books);
                } else {
                    System.err.println("服务器返回数据格式错误: " + data.getClass().getName());
                    showAlert("错误", "服务器返回数据格式错误");
                    bookMap = new HashMap<>();
                }
            } else {
                String errorMsg = response != null ? response.getData().toString() : "未知错误";
                System.err.println("加载图书数据失败: " + errorMsg);
                showAlert("错误", "无法加载图书数据: " + errorMsg);
                bookMap = new HashMap<>();
            }
        } catch (Exception e) {
            System.err.println("加载图书数据时发生异常: " + e.getMessage());
            e.printStackTrace();
            showAlert("错误", "加载图书数据失败: " + e.getMessage());
            bookMap = new HashMap<>();
        }
    }

    private void setupBookListTab() {
        if (bookIdCol != null) bookIdCol.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        if (titleCol != null) titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (authorCol != null) authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        if (publisherCol != null) publisherCol.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        if (statusCol != null) statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (stockCol != null) stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));

        if (searchButton != null) searchButton.setOnAction(event -> handleBookSearch());
        if (refreshButton != null) refreshButton.setOnAction(event -> handleBookRefresh());

        // 借阅按钮
        if (actionCol != null) {
            Callback<TableColumn<Book, Void>, TableCell<Book, Void>> cellFactory = new Callback<TableColumn<Book, Void>, TableCell<Book, Void>>() {
                @Override
                public TableCell<Book, Void> call(final TableColumn<Book, Void> param) {
                    final TableCell<Book, Void> cell = new TableCell<Book, Void>() {
                        private final Button borrowButton = new Button("借阅");
                        {
                            borrowButton.getStyleClass().add("btn-primary");
                            borrowButton.setOnAction(event -> {
                                Book book = getTableView().getItems().get(getIndex());
                                handleBorrowBook(book);
                            });
                        }

                        @Override
                        public void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || getIndex() >= getTableView().getItems().size()) {
                                setGraphic(null);
                            } else {
                                Book book = getTableView().getItems().get(getIndex());
                                borrowButton.setDisable(book.getStock() <= 0);
                                setGraphic(borrowButton);
                            }
                        }
                    };
                    return cell;
                }
            };
            actionCol.setCellFactory(cellFactory);
        }
    }

    private void handleBorrowBook(Book book) {
        if (clientService == null) {
            showAlert("错误", "服务未初始化");
            return;
        }

        Message request = new Message(Type.BOOK_BORROW, book.getBookId());
        Message response = clientService.sendRequest(request);

        if (response != null && response.getCode() == Message.Code.SUCCESS) {
            showAlert("借阅成功", response.getData().toString());
            loadAllBooks();
            loadBorrowRecords();
        } else {
            String errorMsg = response != null ? response.getData().toString() : "未知错误";
            showAlert("借阅失败", errorMsg);
        }
    }

    private void updateBookStats(List<Book> books) {
        if (books != null) {
            long totalBooks = books.size();
            long availableBooks = books.stream().filter(b -> b.getStock() > 0).count();
            if (totalBooksLabel != null) totalBooksLabel.setText(String.valueOf(totalBooks));
            if (availableBooksLabel != null) availableBooksLabel.setText(String.valueOf(availableBooks));
            if (borrowedBooksLabel != null) borrowedBooksLabel.setText(String.valueOf(totalBooks - availableBooks));
            if (newBooksLabel != null) newBooksLabel.setText("0");
        } else {
            if (totalBooksLabel != null) totalBooksLabel.setText("N/A");
            if (availableBooksLabel != null) availableBooksLabel.setText("N/A");
            if (borrowedBooksLabel != null) borrowedBooksLabel.setText("N/A");
            if (newBooksLabel != null) newBooksLabel.setText("N/A");
        }
    }

    private void handleBookSearch() {
        String query = "";
        if (searchField != null) {
            query = searchField.getText();
        }

        if (query == null || query.trim().isEmpty()) {
            showAlert("提示", "请输入搜索关键词");
            return;
        }

        if (clientService == null) {
            showAlert("错误", "服务未初始化");
            return;
        }

        Message request = new Message(Type.BOOK_SEARCH, query);
        Message response = clientService.sendRequest(request);

        if (response != null && response.getCode() == Message.Code.SUCCESS) {
            Object data = response.getData();
            if (data instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<Book> books = (List<Book>) data;
                if (booksTableView != null) {
                    booksTableView.setItems(FXCollections.observableArrayList(books));
                }
            } else {
                showAlert("错误", "搜索结果格式错误");
            }
        } else {
            String errorMsg = response != null ? response.getData().toString() : "搜索失败";
            showAlert("搜索失败", errorMsg);
        }
    }

    private void handleBookRefresh() {
        if (searchField != null) searchField.clear();
        loadAllBooks();
    }

    // ========== 个人借阅功能 ==========

    private void setupBorrowHistoryTab() {
        if (borrowIdCol != null) borrowIdCol.setCellValueFactory(new PropertyValueFactory<>("recordId"));

        if (borrowTitleCol != null) {
            borrowTitleCol.setCellValueFactory(new PropertyValueFactory<>("bookId"));
            borrowTitleCol.setCellFactory(column -> new TableCell<BorrowRecord, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        if (bookMap != null && bookMap.containsKey(item)) {
                            setText(bookMap.get(item).getTitle());
                        } else {
                            setText("未知书名");
                        }
                    }
                }
            });
        }

        if (borrowDateCol != null) borrowDateCol.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        if (returnDateCol != null) returnDateCol.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        if (borrowStatusCol != null) borrowStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 操作按钮（还书和续借）
        if (borrowActionCol != null) {
            Callback<TableColumn<BorrowRecord, Void>, TableCell<BorrowRecord, Void>> cellFactory = new Callback<TableColumn<BorrowRecord, Void>, TableCell<BorrowRecord, Void>>() {
                @Override
                public TableCell<BorrowRecord, Void> call(final TableColumn<BorrowRecord, Void> param) {
                    final TableCell<BorrowRecord, Void> cell = new TableCell<BorrowRecord, Void>() {
                        private final Button renewButton = new Button("续借");
                        private final Button returnButton = new Button("还书");
                        private final HBox buttonBox = new HBox(5);

                        {
                            renewButton.getStyleClass().add("btn-secondary");
                            returnButton.getStyleClass().add("btn-primary");
                            buttonBox.setAlignment(Pos.CENTER);
                            buttonBox.getChildren().addAll(renewButton, returnButton);

                            renewButton.setOnAction(event -> {
                                BorrowRecord record = getTableView().getItems().get(getIndex());
                                handleRenewBook(record);
                            });

                            returnButton.setOnAction(event -> {
                                BorrowRecord record = getTableView().getItems().get(getIndex());
                                handleReturnBook(record);
                            });
                        }

                        @Override
                        public void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || getIndex() >= getTableView().getItems().size()) {
                                setGraphic(null);
                            } else {
                                BorrowRecord record = getTableView().getItems().get(getIndex());
                                boolean isBorrowed = "BORROWED".equals(record.getStatus());
                                renewButton.setDisable(!isBorrowed);
                                returnButton.setDisable(!isBorrowed);
                                setGraphic(buttonBox);
                            }
                        }
                    };
                    return cell;
                }
            };
            borrowActionCol.setCellFactory(cellFactory);
        }
    }

    private void loadBorrowRecords() {
        if (clientService == null) {
            return;
        }

        Message request = new Message(Type.BORROW_RECORD_LIST, null);
        Message response = clientService.sendRequest(request);

        if (response != null && response.getCode() == Message.Code.SUCCESS) {
            Object data = response.getData();
            if (data instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<BorrowRecord> records = (List<BorrowRecord>) data;
                if (borrowHistoryTable != null) {
                    borrowHistoryTable.setItems(FXCollections.observableArrayList(records));
                }
                updateBorrowStats(records);
            } else {
                showAlert("错误", "借阅记录数据格式错误");
            }
        } else {
            String errorMsg = response != null ? response.getData().toString() : "未知错误";
            showAlert("错误", "无法加载借阅记录: " + errorMsg);
        }
    }

    private void handleRenewBook(BorrowRecord record) {
        if (clientService == null) {
            showAlert("错误", "服务未初始化");
            return;
        }

        Message request = new Message(Type.BOOK_RENEW, record.getRecordId());
        Message response = clientService.sendRequest(request);

        if (response != null && response.getCode() == Message.Code.SUCCESS) {
            showAlert("续借成功", response.getData().toString());
            loadBorrowRecords();
        } else {
            String errorMsg = response != null ? response.getData().toString() : "续借失败";
            showAlert("续借失败", errorMsg);
        }
    }

    private void handleReturnBook(BorrowRecord record) {
        if (clientService == null) {
            showAlert("错误", "服务未初始化");
            return;
        }

        Message request = new Message(Type.BOOK_RETURN, record.getRecordId());
        Message response = clientService.sendRequest(request);

        if (response != null && response.getCode() == Message.Code.SUCCESS) {
            showAlert("还书成功", response.getData().toString());
            loadBorrowRecords();
            loadAllBooks();
        } else {
            String errorMsg = response != null ? response.getData().toString() : "还书失败";
            showAlert("还书失败", errorMsg);
        }
    }

    private void updateBorrowStats(List<BorrowRecord> records) {
        if (records != null) {
            long current = records.stream().filter(r -> "BORROWED".equals(r.getStatus())).count();
            long history = records.stream().filter(r -> "RETURNED".equals(r.getStatus())).count();
            if (currentBorrowLabel != null) currentBorrowLabel.setText(String.valueOf(current));
            if (historyBorrowLabel != null) historyBorrowLabel.setText(String.valueOf(history));
            if (expiringBooksLabel != null) expiringBooksLabel.setText("0");
            if (overdueBooksLabel != null) overdueBooksLabel.setText("0");
        } else {
            if (currentBorrowLabel != null) currentBorrowLabel.setText("N/A");
            if (historyBorrowLabel != null) historyBorrowLabel.setText("N/A");
            if (expiringBooksLabel != null) expiringBooksLabel.setText("N/A");
            if (overdueBooksLabel != null) overdueBooksLabel.setText("N/A");
        }
    }

    // ========== 图书馆活动功能 ==========

    private void setupActivityTab() {
        if (activityListView != null) {
            activityListView.setItems(activityList);
            activityListView.setCellFactory(listView -> new ListCell<Activity>() {
                @Override
                protected void updateItem(Activity activity, boolean empty) {
                    super.updateItem(activity, empty);
                    if (empty || activity == null) {
                        setText(null);
                    } else {
                        setText(activity.getTitle());
                    }
                }
            });

            activityListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    showActivityDetails(newSelection);
                }
            });
        }

        if (activityRegisterButton != null) {
            activityRegisterButton.setOnAction(event -> handleActivityRegistration());
        }
    }

    private void loadActivities() {
        activityList.clear();

        Activity activity1 = new Activity();
        activity1.setActivityId("ACT001");
        activity1.setTitle("数字时代的学术写作技巧");
        activity1.setDescription("探讨在数字化环境下如何提升学术写作能力");
        activity1.setCategory("学术讲座");
        activity1.setLocation("图书馆多功能厅");
        activity1.setStartTime(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L));
        activity1.setEndTime(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L + 2 * 60 * 60 * 1000L));
        activity1.setOrganizer("图书馆学术服务部");
        activity1.setSpeaker("张教授");
        activity1.setMaxParticipants(50);
        activity1.setCurrentParticipants(23);
        activity1.setStatus("报名中");
        activity1.setContent("在数字化时代，学术写作面临新的挑战和机遇。本次讲座将从以下几个方面展开：\n\n1. 数字化工具在学术写作中的应用\n2. 如何利用在线资源提升写作质量\n3. 学术诚信在数字时代的重要性\n4. 协作写作的最佳实践\n\n讲座适合所有对学术写作有兴趣的师生参加。");

        Activity activity2 = new Activity();
        activity2.setActivityId("ACT002");
        activity2.setTitle("经典名著读书分享会");
        activity2.setDescription("分享阅读经典文学作品的心得体会");
        activity2.setCategory("读书活动");
        activity2.setLocation("图书馆阅览室");
        activity2.setStartTime(new Date(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L));
        activity2.setEndTime(new Date(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L + 90 * 60 * 1000L));
        activity2.setOrganizer("图书馆读者服务部");
        activity2.setSpeaker("读者代表");
        activity2.setMaxParticipants(30);
        activity2.setCurrentParticipants(18);
        activity2.setStatus("报名中");
        activity2.setContent("本次读书分享会将围绕近期阅读的经典名著展开讨论，包括：\n\n• 《百年孤独》- 魔幻现实主义的魅力\n• 《1984》- 反乌托邦文学的现实意义\n• 《红楼梦》- 中国古典小说的巅峰之作\n\n欢迎所有热爱阅读的朋友参加，分享您的阅读感悟。");

        Activity activity3 = new Activity();
        activity3.setActivityId("ACT003");
        activity3.setTitle("信息检索技能培训");
        activity3.setDescription("系统性提升文献检索和信息获取能力，掌握现代学术研究的基础技能");
        activity3.setCategory("技能培训");
        activity3.setLocation("图书馆电子阅览室");
        activity3.setStartTime(new Date(System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000L));
        activity3.setEndTime(new Date(System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000L + 3 * 60 * 60 * 1000L));
        activity3.setOrganizer("图书馆信息服务部");
        activity3.setSpeaker("李老师 - 信息管理专家");
        activity3.setMaxParticipants(25);
        activity3.setCurrentParticipants(12);
        activity3.setStatus("报名中");
        activity3.setContent("在信息爆炸的时代，如何快速准确地获取所需信息成为学术研究和学习的关键技能。本次培训将系统性地传授信息检索的专业技能。\n\n" +
                "🎯 培训目标：\n" +
                "• 掌握高效的信息检索策略\n" +
                "• 熟练使用各类学术数据库\n" +
                "• 提升信息筛选与评估能力\n" +
                "• 建立个人学术资源库\n\n" +
                "📚 培训内容详解：\n\n" +
                "第一部分：数据库使用精通\n" +
                "1. 中国知网(CNKI)深度应用\n" +
                "   • 高级检索语法与技巧\n" +
                "   • 引文分析与知识发现\n" +
                "   • 个性化推送设置\n\n" +
                "2. Web of Science核心功能\n" +
                "   • SCI检索策略优化\n" +
                "   • 影响因子分析方法\n" +
                "   • 研究前沿识别技术\n\n" +
                "3. 专业数据库资源介绍\n" +
                "   • IEEE Xplore工程技术文献\n" +
                "   • PubMed生物医学资源\n" +
                "   • JSTOR人文社科典藏\n\n" +
                "第二部分：检索策略优化\n" +
                "• 关键词选择的科学方法\n" +
                "• 布尔逻辑运算符巧用\n" +
                "• 截词符与通配符应用\n" +
                "• 多数据库联合检索\n\n" +
                "第三部分：文献管理工具\n" +
                "• EndNote文献管理系统\n" +
                "• Zotero开源管理平台\n" +
                "• Mendeley协作研究工具\n\n" +
                "🛠️ 实践环节：\n" +
                "• 模拟检索任务演练\n" +
                "• 个人检索策略制定\n" +
                "• 疑难问题现场解答\n\n" +
                "🎓 适合群体：研究生、博士生、青年教师、科研人员");

        Activity activity4 = new Activity();
        activity4.setActivityId("ACT004");
        activity4.setTitle("人工智能前沿技术讲座");
        activity4.setDescription("深入探索AI技术的最新发展趋势，了解人工智能在各领域的创新应用");
        activity4.setCategory("学术讲座");
        activity4.setLocation("图书馆学术报告厅");
        activity4.setStartTime(new Date(System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000L));
        activity4.setEndTime(new Date(System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000L + 2 * 60 * 60 * 1000L));
        activity4.setOrganizer("图书馆科技服务部");
        activity4.setSpeaker("王博士 - 清华大学计算机系");
        activity4.setMaxParticipants(80);
        activity4.setCurrentParticipants(45);
        activity4.setStatus("报名中");
        activity4.setContent("人工智能正在重塑我们的世界，从日常生活到科学研究，AI技术的影响无处不在。本次讲座将带您走进AI的前沿世界。\n\n" +
                "🤖 讲座核心议题：\n\n" +
                "1. 深度学习技术突破\n" +
                "   • Transformer架构革命\n" +
                "   • 卷积神经网络新进展\n" +
                "   • 强化学习最新成果\n" +
                "   • 自监督学习范式\n\n" +
                "2. 大语言模型时代\n" +
                "   • GPT系列模型演进\n" +
                "   • 多模态AI能力展示\n" +
                "   • 模型训练技术创新\n" +
                "   • AI安全与对齐问题\n\n" +
                "3. 行业应用案例分析\n" +
                "   • 医疗诊断AI辅助系统\n" +
                "   • 自动驾驶技术现状\n" +
                "   • 智能制造解决方案\n" +
                "   • 教育AI个性化学习\n\n" +
                "4. AI伦理与社会影响\n" +
                "   • 算法偏见识别与消除\n" +
                "   • 隐私保护技术发展\n" +
                "   • AI治理政策框架\n" +
                "   • 人机协作新模式\n\n" +
                "5. 未来发展趋势预测\n" +
                "   • 通用人工智能(AGI)路径\n" +
                "   • 量子计算与AI结合\n" +
                "   • 边缘AI技术发展\n" +
                "   • AI科研新范式\n\n" +
                "💡 特色环节：\n" +
                "• 最新AI产品现场演示\n" +
                "• 与专家面对面交流\n" +
                "• 研究方向答疑解惑\n" +
                "• 学术合作机会介绍\n\n" +
                "🎯 目标听众：计算机、AI、数据科学相关专业师生，科技爱好者，创业者");

        Activity activity5 = new Activity();
        activity5.setActivityId("ACT005");
        activity5.setTitle("古籍保护与数字化展览");
        activity5.setDescription("展示珍贵古籍文献及其数字化保护成果，传承中华优秀传统文化");
        activity5.setCategory("展览活动");
        activity5.setLocation("图书馆展览厅");
        activity5.setStartTime(new Date(System.currentTimeMillis() + 1 * 24 * 60 * 60 * 1000L));
        activity5.setEndTime(new Date(System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L));
        activity5.setOrganizer("图书馆特藏部");
        activity5.setSpeaker("古籍保护专家团队");
        activity5.setMaxParticipants(200);
        activity5.setCurrentParticipants(67);
        activity5.setStatus("进行中");
        activity5.setContent("古籍是中华文明的珍贵载体，承载着千年的智慧与文化。本次展览将现代科技与传统文化完美结合，展现古籍保护的丰硕成果。\n\n" +
                "📜 展览核心内容：\n\n" +
                "1. 珍贵古籍实物展示\n" +
                "   • 宋版《资治通鉴》\n" +
                "   • 明刻《永乐大典》残卷\n" +
                "   • 清代科举试卷真迹\n" +
                "   • 民国时期名人手稿\n\n" +
                "2. 数字化技术应用\n" +
                "   • 高精度扫描技术展示\n" +
                "   • 3D古籍建模技术\n" +
                "   • VR虚拟翻阅体验\n" +
                "   • AI文字识别系统\n\n" +
                "3. 保护修复工艺\n" +
                "   • 传统装帧工艺演示\n" +
                "   • 纸张脱酸处理技术\n" +
                "   • 虫蛀修补方法展示\n" +
                "   • 环境监测系统介绍\n\n" +
                "4. 互动体验区域\n" +
                "   • 古籍装帧体验工作坊\n" +
                "   • 传统印刷术体验\n" +
                "   • 古代书写工具试用\n" +
                "   • 数字古籍检索体验\n\n" +
                "🎯 展览特色：\n" +
                "• 珍稀古籍首次公开展出\n" +
                "• 传统与现代技术融合\n" +
                "• 沉浸式文化体验\n" +
                "• 专业导览服务\n\n" +
                "📚 文化价值：\n" +
                "• 传承中华优秀传统文化\n" +
                "• 提升文化自信与认同\n" +
                "• 普及古籍保护知识\n" +
                "• 激发文化研究兴趣\n\n" +
                "🎪 特别活动：\n" +
                "• 周末专家讲座\n" +
                "• 学生志愿导览\n" +
                "• 主题摄影比赛\n" +
                "• 古籍知识竞答");

        Activity activity6 = new Activity();
        activity6.setActivityId("ACT006");
        activity6.setTitle("大学生学术论文写作指导");
        activity6.setDescription("针对本科生和研究生的学术论文写作专项培训，提升学术写作能力");
        activity6.setCategory("技能培训");
        activity6.setLocation("图书馆培训室");
        activity6.setStartTime(new Date(System.currentTimeMillis() + 12 * 24 * 60 * 60 * 1000L));
        activity6.setEndTime(new Date(System.currentTimeMillis() + 12 * 24 * 60 * 60 * 1000L + 4 * 60 * 60 * 1000L));
        activity6.setOrganizer("图书馆学术支持中心");
        activity6.setSpeaker("陈教授 - 文学院学术写作专家");
        activity6.setMaxParticipants(40);
        activity6.setCurrentParticipants(28);
        activity6.setStatus("报名中");
        activity6.setContent("学术论文写作是大学生必备的核心技能，也是学术研究的重要基础。本次培训将系统性地指导学术写作的全过程。\n\n" +
                "📝 培训核心模块：\n\n" +
                "第一部分：论文结构设计\n" +
                "1. 标题制定技巧\n" +
                "   • 精准概括研究内容\n" +
                "   • 关键词优化策略\n" +
                "   • 吸引读者注意方法\n\n" +
                "2. 摘要写作要领\n" +
                "   • 四要素结构法\n" +
                "   • 中英文摘要对比\n" +
                "   • 关键信息提取\n\n" +
                "3. 引言部分设计\n" +
                "   • 研究背景阐述\n" +
                "   • 问题提出逻辑\n" +
                "   • 研究意义论证\n\n" +
                "第二部分：文献综述技巧\n" +
                "• 文献搜集策略制定\n" +
                "• 文献分类整理方法\n" +
                "• 批判性阅读技能\n" +
                "• 综述写作逻辑构建\n\n" +
                "第三部分：数据分析呈现\n" +
                "• 统计方法选择原则\n" +
                "• 图表制作规范\n" +
                "• 数据解读技巧\n" +
                "• 结果讨论要点\n\n" +
                "第四部分：学术规范要求\n" +
                "• 引用格式标准化\n" +
                "• 参考文献整理\n" +
                "• 学术诚信要求\n" +
                "• 版权意识培养\n\n" +
                "第五部分：投稿发表指导\n" +
                "• 期刊选择策略\n" +
                "• 投稿流程详解\n" +
                "• 审稿意见回应\n" +
                "• 修改完善技巧\n\n" +
                "🛠️ 实践训练：\n" +
                "• 论文结构分析练习\n" +
                "• 摘要写作实战\n" +
                "• 文献综述模拟\n" +
                "• 一对一写作指导\n\n" +
                "📋 培训成果：\n" +
                "• 完整的写作指导手册\n" +
                "• 论文模板工具包\n" +
                "• 学术写作认证证书\n" +
                "• 后续辅导咨询机会\n\n" +
                "🎯 适合对象：本科三年级以上学生、研究生、青年教师");

        activityList.addAll(activity1, activity2, activity3, activity4, activity5, activity6);
    }

    private void showActivityDetails(Activity activity) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        if (activityTitleLabel != null) activityTitleLabel.setText(activity.getTitle());
        if (activityDateLabel != null) {
            String timeRange = "";
            if (activity.getStartTime() != null && activity.getEndTime() != null) {
                timeRange = sdf.format(activity.getStartTime()) + " - " + sdf.format(activity.getEndTime());
            }
            activityDateLabel.setText(timeRange);
        }
        if (activityLocationLabel != null) activityLocationLabel.setText(activity.getLocation());
        if (activityContentText != null) activityContentText.setText(activity.getContent());
        if (activityOrganizerLabel != null) activityOrganizerLabel.setText("主办：" + activity.getOrganizer());
        if (activitySpeakerLabel != null) activitySpeakerLabel.setText("主讲：" + activity.getSpeaker());
        if (activityParticipantsLabel != null) {
            activityParticipantsLabel.setText(String.format("参与人数：%d/%d",
                    activity.getCurrentParticipants(), activity.getMaxParticipants()));
        }
        if (activityStatusLabel != null) activityStatusLabel.setText("状态：" + activity.getStatus());
        if (activityRegisterButton != null) {
            boolean canRegister = "报名中".equals(activity.getStatus()) &&
                    activity.getCurrentParticipants() < activity.getMaxParticipants();
            activityRegisterButton.setDisable(!canRegister);
        }
    }

    private void handleActivityRegistration() {
        if (activityListView != null) {
            Activity selectedActivity = activityListView.getSelectionModel().getSelectedItem();
            if (selectedActivity != null) {
                boolean canRegister = "报名中".equals(selectedActivity.getStatus()) &&
                        selectedActivity.getCurrentParticipants() < selectedActivity.getMaxParticipants();
                if (canRegister) {
                    showAlert("报名成功", "您已成功报名参加活动：" + selectedActivity.getTitle());
                    selectedActivity.setCurrentParticipants(selectedActivity.getCurrentParticipants() + 1);
                    showActivityDetails(selectedActivity);
                } else {
                    showAlert("报名失败", "该活动已满员或不在报名期间");
                }
            }
        }
    }

    // ========== 文献查阅功能 ==========

    private void setupLiteratureTab() {
        if (literatureTitleCol != null) literatureTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (literatureAuthorCol != null) literatureAuthorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        if (literatureSourceCol != null) literatureSourceCol.setCellValueFactory(new PropertyValueFactory<>("source"));
        if (literaturePublishDateCol != null) literaturePublishDateCol.setCellValueFactory(new PropertyValueFactory<>("publishDate"));

        if (literatureActionCol != null) {
            literatureActionCol.setCellFactory(param -> new TableCell<Literature, Void>() {
                private final Button readButton = new Button("阅读");
                private final Button downloadButton = new Button("下载");
                private final HBox buttonBox = new HBox(5);

                {
                    readButton.getStyleClass().add("btn-primary");
                    downloadButton.getStyleClass().add("btn-secondary");
                    buttonBox.setAlignment(Pos.CENTER);
                    buttonBox.getChildren().addAll(readButton, downloadButton);

                    readButton.setOnAction(event -> {
                        Literature literature = getTableView().getItems().get(getIndex());
                        handleReadLiterature(literature);
                    });

                    downloadButton.setOnAction(event -> {
                        Literature literature = getTableView().getItems().get(getIndex());
                        handleDownloadLiterature(literature);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getIndex() >= getTableView().getItems().size()) {
                        setGraphic(null);
                    } else {
                        setGraphic(buttonBox);
                    }
                }
            });
        }

        if (literatureTableView != null) {
            literatureTableView.setItems(literatureList);
        }

        if (literatureSearchButton != null) {
            literatureSearchButton.setOnAction(event -> handleLiteratureSearch());
        }
    }

    private void loadLiteratures() {
        literatureList.clear();

        Literature lit1 = new Literature("人工智能在教育领域的应用研究", "张三, 李四",
                "教育技术学报", "2024-03-15",
                "https://kns.cnki.net/kns/brief/result.aspx?dbPrefix=CJFQ");

        Literature lit2 = new Literature("大数据时代的信息安全挑战与对策", "王五, 赵六",
                "计算机学报", "2024-02-20",
                "https://cjc.ict.ac.cn/online/");

        Literature lit3 = new Literature("物联网技术在智慧城市建设中的应用", "陈七, 刘八",
                "通信学报", "2024-01-10",
                "https://www.infocomm-journal.com/");

        Literature lit4 = new Literature("机器学习算法在医疗诊断中的最新进展", "杨九, 周十",
                "中国生物医学工程学报", "2023-12-05",
                "https://www.cjbmeonline.com/");

        Literature lit5 = new Literature("区块链技术在金融科技中的创新应用", "吴十一, 郑十二",
                "金融研究", "2023-11-18",
                "https://www.jryj.org.cn/");

        Literature lit6 = new Literature("5G网络架构优化与性能分析", "黄十三, 何十四",
                "电子学报", "2023-10-25",
                "https://www.ejournal.org.cn/");

        Literature lit7 = new Literature("深度学习在自然语言处理中的应用综述", "李明, 张伟",
                "软件学报", "2024-04-12",
                "https://www.jos.org.cn/");

        Literature lit8 = new Literature("量子计算技术发展现状与前景分析", "刘强, 王丽",
                "科学通报", "2024-03-28",
                "https://www.scichina.com/");

        Literature lit9 = new Literature("云计算环境下的数据隐私保护技术", "陈晓, 赵敏",
                "计算机研究与发展", "2024-02-14",
                "https://crad.ict.ac.cn/");

        Literature lit10 = new Literature("智能推荐系统中的协同过滤算法优化", "孙华, 李娜",
                "中国科学：信息科学", "2024-01-20",
                "https://www.scichina.com/");

        Literature lit11 = new Literature("边缘计算在工业互联网中的关键技术研究", "周杰, 王芳",
                "自动化学报", "2023-12-18",
                "http://www.aas.net.cn/");

        Literature lit12 = new Literature("数字孪生技术在制造业中的应用与发展", "马强, 林雪",
                "机械工程学报", "2023-11-25",
                "http://www.cjmenet.com.cn/");

        literatureList.addAll(lit1, lit2, lit3, lit4, lit5, lit6, lit7, lit8, lit9, lit10, lit11, lit12);
    }

    private void handleReadLiterature(Literature literature) {
        try {
            if (literature.getUrl() != null && !literature.getUrl().isEmpty()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        desktop.browse(new URI(literature.getUrl()));
                        showAlert("提示", "正在打开文献链接，请在浏览器中查看：\n" + literature.getTitle());
                    } else {
                        showAlert("错误", "系统不支持打开网页链接");
                    }
                } else {
                    showAlert("错误", "系统不支持Desktop操作");
                }
            } else {
                showAlert("提示", "该文献暂无在线阅读链接");
            }
        } catch (Exception e) {
            System.err.println("打开文献链接时发生错误: " + e.getMessage());
            e.printStackTrace();
            showAlert("错误", "无法打开文献链接: " + e.getMessage());
        }
    }

    private void handleDownloadLiterature(Literature literature) {
        try {
            showAlert("下载提示", "正在准备下载文献：\n" + literature.getTitle() +
                    "\n\n请注意：\n1. 部分文献需要相应权限才能下载\n2. 请遵守版权法律法规\n3. 下载的文献仅供学术研究使用");
        } catch (Exception e) {
            System.err.println("下载文献时发生错误: " + e.getMessage());
            e.printStackTrace();
            showAlert("错误", "下载文献失败: " + e.getMessage());
        }
    }

    private void handleLiteratureSearch() {
        String query = "";
        if (literatureSearchField != null) {
            query = literatureSearchField.getText();
        }

        if (query == null || query.trim().isEmpty()) {
            showAlert("提示", "请输入搜索关键词");
            return;
        }

        String searchTerm = query.toLowerCase();
        List<Literature> searchResults = literatureList.stream()
                .filter(lit -> lit.getTitle().toLowerCase().contains(searchTerm) ||
                        lit.getAuthor().toLowerCase().contains(searchTerm) ||
                        lit.getSource().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());

        if (literatureTableView != null) {
            literatureTableView.setItems(FXCollections.observableArrayList(searchResults));
        }

        if (searchResults.isEmpty()) {
            showAlert("搜索结果", "未找到匹配的文献，请尝试其他关键词");
        } else {
            showAlert("搜索结果", "找到 " + searchResults.size() + " 条相关文献");
        }
    }

    // ========== 工具方法 ==========

    private void showAlert(String title, String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("显示对话框时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== 内部类定义 ==========

    public static class Activity {
        private String activityId;
        private String title;
        private String description;
        private String category;
        private String location;
        private Date startTime;
        private Date endTime;
        private String organizer;
        private String speaker;
        private int maxParticipants;
        private int currentParticipants;
        private String status;
        private String content;

        // 构造函数
        public Activity() {}

        // Getter和Setter方法
        public String getActivityId() { return activityId; }
        public void setActivityId(String activityId) { this.activityId = activityId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public Date getStartTime() { return startTime; }
        public void setStartTime(Date startTime) { this.startTime = startTime; }

        public Date getEndTime() { return endTime; }
        public void setEndTime(Date endTime) { this.endTime = endTime; }

        public String getOrganizer() { return organizer; }
        public void setOrganizer(String organizer) { this.organizer = organizer; }

        public String getSpeaker() { return speaker; }
        public void setSpeaker(String speaker) { this.speaker = speaker; }

        public int getMaxParticipants() { return maxParticipants; }
        public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }

        public int getCurrentParticipants() { return currentParticipants; }
        public void setCurrentParticipants(int currentParticipants) { this.currentParticipants = currentParticipants; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class Literature {
        private String title;
        private String author;
        private String source;
        private String publishDate;
        private String url;

        public Literature() {}

        public Literature(String title, String author, String source, String publishDate, String url) {
            this.title = title;
            this.author = author;
            this.source = source;
            this.publishDate = publishDate;
            this.url = url;
        }

        // Getter和Setter方法
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        public String getPublishDate() { return publishDate; }
        public void setPublishDate(String publishDate) { this.publishDate = publishDate; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
}