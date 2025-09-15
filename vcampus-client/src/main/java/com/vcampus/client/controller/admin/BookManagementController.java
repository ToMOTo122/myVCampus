package com.vcampus.client.controller.admin;

import com.vcampus.client.service.ClientService;
import com.vcampus.client.service.ServiceManager;
import com.vcampus.client.service.LibraryAdminService;
import com.vcampus.common.entity.Book;
import com.vcampus.common.entity.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 使用ServiceManager的图书管理控制器
 */
public class BookManagementController {

    private ClientService clientService;
    private LibraryAdminService adminService;

    private final ObservableList<Book> allBooks = FXCollections.observableArrayList();
    private final ObservableList<Book> filteredBooks = FXCollections.observableArrayList();

    // FXML控件
    @FXML private Button refreshButton;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button clearSearchButton;
    @FXML private TableView<Book> booksTableView;
    @FXML private TableColumn<Book, String> bookIdCol;
    @FXML private TableColumn<Book, String> titleCol;
    @FXML private TableColumn<Book, String> authorCol;
    @FXML private TableColumn<Book, String> publisherCol;
    @FXML private TableColumn<Book, String> statusCol;
    @FXML private TableColumn<Book, Integer> stockCol;
    @FXML private Label totalBooksLabel;
    @FXML private Label availableBooksLabel;
    @FXML private Label unavailableBooksLabel;
    @FXML private TextField bookIdField;
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField publisherField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Spinner<Integer> stockSpinner;
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;

    /**
     * FXML初始化方法 - 自动从ServiceManager获取服务
     */
    @FXML
    public void initialize() {
        System.out.println("=== BookManagementController 初始化开始 ===");

        try {
            // 检查FXML控件
            checkFXMLInjection();

            // 设置界面组件
            setupTableView();
            setupFormControls();
            bindEvents();

            // 从ServiceManager获取ClientService
            initializeService();

            System.out.println("BookManagementController 初始化完成");
        } catch (Exception e) {
            System.err.println("BookManagementController 初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 从ServiceManager初始化服务
     */
    private void initializeService() {
        System.out.println("=== 从ServiceManager获取服务 ===");

        // 立即尝试获取
        this.clientService = ServiceManager.getInstance().getClientService();

        if (clientService != null && clientService.isConnected()) {
            System.out.println("立即获取到ClientService，连接状态正常");
            setupClientService();
        } else {
            System.out.println("ServiceManager中暂无ClientService，启动定时检查...");
            startServiceMonitoring();
        }
    }

    /**
     * 设置ClientService并初始化相关服务
     */
    private void setupClientService() {
        try {
            if (clientService != null) {
                this.adminService = new LibraryAdminService(clientService);
                System.out.println("LibraryAdminService 创建成功");

                // 立即加载数据
                Platform.runLater(this::loadBooksData);
            }
        } catch (Exception e) {
            System.err.println("设置ClientService失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 启动服务监控 - 等待ServiceManager中出现ClientService
     */
    private void startServiceMonitoring() {
        Task<Void> monitorTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                System.out.println("开始监控ServiceManager...");

                for (int i = 0; i < 30; i++) { // 监控30秒
                    Thread.sleep(1000);

                    ClientService service = ServiceManager.getInstance().getClientService();
                    if (service != null && service.isConnected()) {
                        System.out.println("监控成功：ServiceManager中出现了有效的ClientService");

                        Platform.runLater(() -> {
                            clientService = service;
                            setupClientService();
                        });

                        break;
                    }

                    if (i % 5 == 0) { // 每5秒打印一次状态
                        System.out.println("等待ClientService... (" + i + "/30)");
                    }
                }

                return null;
            }

            @Override
            protected void failed() {
                System.err.println("服务监控失败: " + getException().getMessage());
            }
        };

        Thread monitorThread = new Thread(monitorTask);
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    /**
     * 检查FXML控件注入
     */
    private void checkFXMLInjection() {
        System.out.println("检查FXML控件注入:");
        System.out.println("- booksTableView: " + (booksTableView != null));
        System.out.println("- refreshButton: " + (refreshButton != null));
        System.out.println("- bookIdField: " + (bookIdField != null));
    }

    /**
     * 设置表格
     */
    private void setupTableView() {
        if (booksTableView == null) return;

        if (bookIdCol != null) bookIdCol.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        if (titleCol != null) titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (authorCol != null) authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        if (publisherCol != null) publisherCol.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        if (statusCol != null) statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (stockCol != null) stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));

        booksTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) populateForm(newValue);
                }
        );

        booksTableView.setItems(filteredBooks);
        System.out.println("表格设置完成");
    }

    /**
     * 设置表单控件
     */
    private void setupFormControls() {
        if (statusComboBox != null) {
            statusComboBox.getItems().addAll("可借阅", "已借完", "维护中", "已下架");
            statusComboBox.setValue("可借阅");
        }
        if (stockSpinner != null) {
            stockSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9999, 1));
        }
    }

    /**
     * 绑定事件
     */
    private void bindEvents() {
        if (refreshButton != null) refreshButton.setOnAction(e -> handleRefresh());
        if (searchButton != null) searchButton.setOnAction(e -> handleSearch());
        if (clearSearchButton != null) clearSearchButton.setOnAction(e -> handleClearSearch());
        if (addButton != null) addButton.setOnAction(e -> handleAddBook());
        if (updateButton != null) updateButton.setOnAction(e -> handleUpdateBook());
        if (deleteButton != null) deleteButton.setOnAction(e -> handleDeleteBook());
        if (clearButton != null) clearButton.setOnAction(e -> handleClearForm());

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldText, newText) -> {
                if (newText == null || newText.trim().isEmpty()) {
                    filteredBooks.setAll(allBooks);
                } else {
                    performSearch(newText);
                }
                updateStatistics();
            });
        }
    }

    /**
     * 加载图书数据
     */
    private void loadBooksData() {
        System.out.println("=== 开始加载图书数据 ===");

        if (clientService == null || !clientService.isConnected()) {
            System.err.println("ClientService 不可用");
            showAlert("错误", "服务连接不可用，请重新登录");
            return;
        }

        Task<List<Book>> task = new Task<List<Book>>() {
            @Override
            protected List<Book> call() throws Exception {
                System.out.println("后台任务：发送图书列表请求");

                Message request = new Message(Message.Type.BOOK_LIST, null);
                Message response = clientService.sendRequest(request);

                System.out.println("收到响应: " + (response != null ? response.getCode() : "null"));

                if (response != null && response.getCode() == Message.Code.SUCCESS) {
                    Object data = response.getData();
                    if (data instanceof List<?>) {
                        @SuppressWarnings("unchecked")
                        List<Book> books = (List<Book>) data;
                        System.out.println("成功获取 " + books.size() + " 本图书");
                        return books;
                    }
                }

                throw new Exception("服务器返回错误: " +
                        (response != null ? response.getData() : "无响应"));
            }

            @Override
            protected void succeeded() {
                System.out.println("=== 图书数据加载成功 ===");
                List<Book> books = getValue();

                if (books != null && !books.isEmpty()) {
                    Platform.runLater(() -> {
                        allBooks.setAll(books);
                        filteredBooks.setAll(books);
                        updateStatistics();
                        System.out.println("界面更新完成，显示 " + books.size() + " 条记录");
                        //showAlert("成功", "成功加载 " + books.size() + " 本图书");
                    });
                } else {
                    Platform.runLater(() -> showAlert("提示", "没有找到图书数据"));
                }
            }

            @Override
            protected void failed() {
                System.err.println("=== 图书数据加载失败 ===");
                Throwable e = getException();
                System.err.println("失败原因: " + e.getMessage());
                Platform.runLater(() -> showAlert("错误", "加载失败: " + e.getMessage()));
            }
        };

        new Thread(task).start();
    }

    /**
     * 刷新数据
     */
    @FXML
    private void handleRefresh() {
        System.out.println("=== 手动刷新 ===");

        // 重新从ServiceManager获取ClientService（防止连接更新）
        ClientService currentService = ServiceManager.getInstance().getClientService();
        if (currentService != null && currentService.isConnected()) {
            this.clientService = currentService;
            if (adminService == null) {
                try {
                    adminService = new LibraryAdminService(clientService);
                } catch (Exception e) {
                    System.err.println("重新创建AdminService失败: " + e.getMessage());
                }
            }
        }

        clearSearchField();
        loadBooksData();
    }

    // ========== 其他CRUD操作方法 ==========

    @FXML private void handleSearch() {
        String keyword = searchField != null ? searchField.getText() : "";
        if (keyword.trim().isEmpty()) {
            showAlert("提示", "请输入搜索关键词");
            return;
        }
        performSearch(keyword);
    }

    private void performSearch(String keyword) {
        if (allBooks.isEmpty()) return;

        String lower = keyword.toLowerCase();
        List<Book> results = allBooks.stream()
                .filter(book ->
                        book.getBookId().toLowerCase().contains(lower) ||
                                book.getTitle().toLowerCase().contains(lower) ||
                                book.getAuthor().toLowerCase().contains(lower) ||
                                (book.getPublisher() != null && book.getPublisher().toLowerCase().contains(lower))
                ).collect(Collectors.toList());

        filteredBooks.setAll(results);
    }

    @FXML private void handleClearSearch() {
        clearSearchField();
        filteredBooks.setAll(allBooks);
        updateStatistics();
    }

    private void clearSearchField() {
        if (searchField != null) searchField.clear();
    }

    @FXML private void handleAddBook() {
        if (!validateForm()) return;
        performBookOperation(Message.Type.BOOK_ADD, createBookFromForm(), "添加");
    }

    @FXML private void handleUpdateBook() {
        Book selected = booksTableView != null ? booksTableView.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showAlert("提示", "请先选择要更新的图书");
            return;
        }
        if (!validateForm()) return;
        performBookOperation(Message.Type.BOOK_UPDATE, createBookFromForm(), "更新");
    }

    @FXML private void handleDeleteBook() {
        Book selected = booksTableView != null ? booksTableView.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showAlert("提示", "请先选择要删除的图书");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("确认删除");
        confirm.setContentText("确定删除图书《" + selected.getTitle() + "》？");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            performBookOperation(Message.Type.BOOK_DELETE, selected.getBookId(), "删除");
        }
    }

    @FXML private void handleClearForm() {
        clearForm();
    }

    /**
     * 执行图书操作
     */
    private void performBookOperation(Message.Type type, Object data, String operation) {
        if (clientService == null) {
            showAlert("错误", "服务不可用");
            return;
        }

        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                Message request = new Message(type, data);
                Message response = clientService.sendRequest(request);
                return response != null && response.getCode() == Message.Code.SUCCESS;
            }

            @Override
            protected void succeeded() {
                if (getValue()) {
                    Platform.runLater(() -> {
                        showAlert("成功", "图书" + operation + "成功");
                        if (!"删除".equals(operation)) clearForm();
                        loadBooksData();
                    });
                } else {
                    Platform.runLater(() -> showAlert("错误", "图书" + operation + "失败"));
                }
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> showAlert("错误", operation + "失败: " + getException().getMessage()));
            }
        };

        new Thread(task).start();
    }

    // ========== 辅助方法 ==========

    private boolean validateForm() {
        if (isEmpty(bookIdField)) return showError("图书ID不能为空", bookIdField);
        if (isEmpty(titleField)) return showError("书名不能为空", titleField);
        if (isEmpty(authorField)) return showError("作者不能为空", authorField);
        if (statusComboBox == null || statusComboBox.getValue() == null) {
            return showError("请选择图书状态", statusComboBox);
        }
        return true;
    }

    private boolean isEmpty(TextField field) {
        return field == null || field.getText() == null || field.getText().trim().isEmpty();
    }

    private boolean showError(String msg, Control field) {
        showAlert("验证错误", msg);
        if (field != null) field.requestFocus();
        return false;
    }

    private Book createBookFromForm() {
        return new Book(
                bookIdField.getText().trim(),
                titleField.getText().trim(),
                authorField.getText().trim(),
                publisherField != null ? publisherField.getText().trim() : "",
                statusComboBox.getValue(),
                stockSpinner != null ? stockSpinner.getValue() : 1
        );
    }

    private void populateForm(Book book) {
        if (bookIdField != null) bookIdField.setText(book.getBookId());
        if (titleField != null) titleField.setText(book.getTitle());
        if (authorField != null) authorField.setText(book.getAuthor());
        if (publisherField != null) publisherField.setText(book.getPublisher());
        if (statusComboBox != null) statusComboBox.setValue(book.getStatus());
        if (stockSpinner != null) stockSpinner.getValueFactory().setValue(book.getStock());
    }

    private void clearForm() {
        if (bookIdField != null) bookIdField.clear();
        if (titleField != null) titleField.clear();
        if (authorField != null) authorField.clear();
        if (publisherField != null) publisherField.clear();
        if (statusComboBox != null) statusComboBox.setValue("可借阅");
        if (stockSpinner != null) stockSpinner.getValueFactory().setValue(1);
        if (booksTableView != null) booksTableView.getSelectionModel().clearSelection();
    }

    private void updateStatistics() {
        int total = filteredBooks.size();
        long available = filteredBooks.stream().filter(b -> b.getStock() > 0).count();
        long unavailable = total - available;

        if (totalBooksLabel != null) totalBooksLabel.setText("总计: " + total + " 本");
        if (availableBooksLabel != null) availableBooksLabel.setText("可借: " + available + " 本");
        if (unavailableBooksLabel != null) unavailableBooksLabel.setText("不可借: " + unavailable + " 本");
    }

    private void showAlert(String title, String content) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("显示提示框失败: " + e.getMessage());
        }
    }

    /**
     * 保持兼容性的方法（如果还有其他地方调用）
     */
    public void setClientService(ClientService clientService) {
        System.out.println("BookManagementController.setClientService 被直接调用");
        this.clientService = clientService;
        if (clientService != null) {
            setupClientService();
        }
    }
}