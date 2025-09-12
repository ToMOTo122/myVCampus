package com.vcampus.client.controller;

import com.vcampus.client.service.ClientService;
import com.vcampus.common.entity.Book;
import com.vcampus.common.entity.BorrowRecord;
import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.Message.Type;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 这个类是 BookList.fxml 的控制器。
 * 它负责处理图书馆界面的所有用户交互和数据展示。
 */
public class BookListController {

    private ClientService clientService;
    private Map<String, Book> bookMap; // 用于通过bookId快速查找图书对象

    // 绑定图书列表 Tab 的 FXML 控件
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

    // 绑定个人借阅 Tab 的 FXML 控件
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

    // 绑定图书馆活动 Tab 的 FXML 控件
    @FXML private ListView<String> activityListView;
    @FXML private Label activityTitleLabel;
    @FXML private Label activityDateLabel;
    @FXML private Label activityLocationLabel;
    @FXML private Text activityContentText;

    // 绑定文献查阅 Tab 的 FXML 控件
    @FXML private TextField literatureSearchField;
    @FXML private Button literatureSearchButton;
    @FXML private VBox literatureListVBox;

    // 添加一个方法来设置ClientService
    public void setClientService(ClientService clientService) {
        this.clientService = clientService;
    }

    @FXML
    public void initialize() {
        System.out.println("BookListController initialize 方法被调用。");

        // 如果clientService为空，创建一个新的实例（但这不推荐）
        if (clientService == null) {
            clientService = new ClientService();
        }

        // 首先加载所有图书数据，这是解决问题的关键步骤
        loadAllBooks();

        // 然后初始化各个Tab
        setupBookListTab();
        setupBorrowHistoryTab();
        setupActivityTab();
        setupLiteratureTab();
    }

    /**
     * 加载所有图书数据并缓存到 map 中，以便后续查找书名。
     */
    private void loadAllBooks() {
        try {
            Message request = new Message(Type.BOOK_LIST, null);
            Message response = clientService.sendRequest(request);

            if (response != null && response.getCode() == Message.Code.SUCCESS) {
                // 检查返回的数据类型
                Object data = response.getData();
                if (data instanceof List<?>) {
                    @SuppressWarnings("unchecked")
                    List<Book> books = (List<Book>) data;
                    // 将图书列表转换为Map，方便根据bookId快速查找
                    bookMap = books.stream().collect(Collectors.toMap(Book::getBookId, Function.identity()));
                    System.out.println("成功加载 " + books.size() + " 本图书数据");
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
        bookIdCol.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        publisherCol.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // 绑定按钮事件
        searchButton.setOnAction(event -> handleBookSearch());
        refreshButton.setOnAction(event -> handleBookRefresh());

        // 使用已加载的图书数据来填充表格
        if (bookMap != null && !bookMap.isEmpty()) {
            booksTableView.setItems(FXCollections.observableArrayList(bookMap.values()));
            updateBookStats(booksTableView.getItems());
        }

        // 为"操作"列添加按钮
        Callback<TableColumn<Book, Void>, TableCell<Book, Void>> cellFactory = new Callback<TableColumn<Book, Void>, TableCell<Book, Void>>() {
            @Override
            public TableCell<Book, Void> call(final TableColumn<Book, Void> param) {
                final TableCell<Book, Void> cell = new TableCell<Book, Void>() {
                    private final Button borrowButton = new Button("借阅");
                    {
                        borrowButton.setOnAction(event -> {
                            Book book = getTableView().getItems().get(getIndex());
                            handleBorrowBook(book);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
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

    private void handleBorrowBook(Book book) {
        Message request = new Message(Type.BOOK_BORROW, book.getBookId());
        Message response = clientService.sendRequest(request);

        if (response != null && response.getCode() == Message.Code.SUCCESS) {
            showAlert("借阅成功", response.getData().toString());
            loadAllBooks();
            setupBookListTab();
            loadBorrowRecords();
        } else {
            String errorMsg = response != null ? response.getData().toString() : "未知错误";
            showAlert("借阅失败", errorMsg);
        }
    }

    // 更新图书统计数据
    private void updateBookStats(List<Book> books) {
        if (books != null) {
            long totalBooks = books.size();
            long availableBooks = books.stream().filter(b -> b.getStock() > 0).count();
            totalBooksLabel.setText(String.valueOf(totalBooks));
            availableBooksLabel.setText(String.valueOf(availableBooks));
            borrowedBooksLabel.setText(String.valueOf(totalBooks - availableBooks));
            newBooksLabel.setText("0"); // 示例，需要实际逻辑
        } else {
            totalBooksLabel.setText("N/A");
            availableBooksLabel.setText("N/A");
            borrowedBooksLabel.setText("N/A");
            newBooksLabel.setText("N/A");
        }
    }

    private void handleBookSearch() {
        String query = searchField.getText();
        if (query == null || query.trim().isEmpty()) {
            showAlert("提示", "请输入搜索关键词");
            return;
        }

        Message request = new Message(Type.BOOK_SEARCH, query);
        Message response = clientService.sendRequest(request);

        if (response != null && response.getCode() == Message.Code.SUCCESS) {
            Object data = response.getData();
            if (data instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<Book> books = (List<Book>) data;
                booksTableView.setItems(FXCollections.observableArrayList(books));
            } else {
                showAlert("错误", "搜索结果格式错误");
            }
        } else {
            String errorMsg = response != null ? response.getData().toString() : "搜索失败";
            showAlert("搜索失败", errorMsg);
        }
    }

    private void handleBookRefresh() {
        searchField.clear();
        loadAllBooks();
        if (bookMap != null && !bookMap.isEmpty()) {
            booksTableView.setItems(FXCollections.observableArrayList(bookMap.values()));
            updateBookStats(booksTableView.getItems());
        }
    }

    private void setupBorrowHistoryTab() {
        borrowIdCol.setCellValueFactory(new PropertyValueFactory<>("recordId"));
        // 关键修改：使用自定义的CellFactory来显示书名
        borrowTitleCol.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        borrowTitleCol.setCellFactory(column -> new TableCell<BorrowRecord, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // item就是这里的bookId
                    if (bookMap != null && bookMap.containsKey(item)) {
                        setText(bookMap.get(item).getTitle());
                    } else {
                        setText("未知书名");
                    }
                }
            }
        });

        borrowDateCol.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        returnDateCol.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        borrowStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadBorrowRecords();

        // 为"操作"列添加按钮
        Callback<TableColumn<BorrowRecord, Void>, TableCell<BorrowRecord, Void>> cellFactory = new Callback<TableColumn<BorrowRecord, Void>, TableCell<BorrowRecord, Void>>() {
            @Override
            public TableCell<BorrowRecord, Void> call(final TableColumn<BorrowRecord, Void> param) {
                final TableCell<BorrowRecord, Void> cell = new TableCell<BorrowRecord, Void>() {
                    private final Button renewButton = new Button("续借");

                    {
                        renewButton.setOnAction(event -> {
                            BorrowRecord record = getTableView().getItems().get(getIndex());
                            handleRenewBook(record);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            BorrowRecord record = getTableView().getItems().get(getIndex());
                            renewButton.setDisable(!"BORROWED".equals(record.getStatus()));
                            setGraphic(renewButton);
                        }
                    }
                };
                return cell;
            }
        };
        borrowActionCol.setCellFactory(cellFactory);
    }

    private void loadBorrowRecords() {
        Message request = new Message(Type.BORROW_RECORD_LIST, null);
        Message response = clientService.sendRequest(request);

        if (response != null && response.getCode() == Message.Code.SUCCESS) {
            Object data = response.getData();
            if (data instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<BorrowRecord> records = (List<BorrowRecord>) data;
                borrowHistoryTable.setItems(FXCollections.observableArrayList(records));
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

    // 更新借阅统计数据
    private void updateBorrowStats(List<BorrowRecord> records) {
        if (records != null) {
            long current = records.stream().filter(r -> "BORROWED".equals(r.getStatus())).count();
            long history = records.stream().filter(r -> "RETURNED".equals(r.getStatus())).count();
            currentBorrowLabel.setText(String.valueOf(current));
            historyBorrowLabel.setText(String.valueOf(history));
            expiringBooksLabel.setText("0"); // 示例，需要实际日期逻辑
            overdueBooksLabel.setText("0"); // 示例，需要实际日期逻辑
        } else {
            currentBorrowLabel.setText("N/A");
            historyBorrowLabel.setText("N/A");
            expiringBooksLabel.setText("N/A");
            overdueBooksLabel.setText("N/A");
        }
    }

    // --- 图书馆活动和文献查阅的占位方法 ---

    private void setupActivityTab() {
        // TODO: 为图书馆活动 Tab 添加逻辑
        // 绑定活动列表点击事件
        // activityListView.getSelectionModel().selectedItemProperty().addListener(...)
        // 在此处调用后端服务获取活动列表，并填充 activityListView
    }

    private void setupLiteratureTab() {
        // TODO: 为文献查阅 Tab 添加逻辑
        // 绑定搜索按钮点击事件
        // literatureSearchButton.setOnAction(event -> handleLiteratureSearch());
        // 在此处实现文献搜索和展示逻辑
    }

    // --- 辅助方法 ---

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}