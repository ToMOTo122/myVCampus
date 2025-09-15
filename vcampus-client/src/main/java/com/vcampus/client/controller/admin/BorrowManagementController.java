package com.vcampus.client.controller.admin;

import com.vcampus.client.service.ClientService;
import com.vcampus.client.service.ServiceManager;  // 添加这个导入
import com.vcampus.common.entity.Book;
import com.vcampus.common.entity.BorrowRecord;
import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.Message.Type;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 修复版借阅管理控制器 - 使用ServiceManager获取ClientService
 */
public class BorrowManagementController {

    private ClientService clientService;
    private ObservableList<BorrowRecord> allRecords = FXCollections.observableArrayList();
    private ObservableList<BorrowRecord> filteredRecords = FXCollections.observableArrayList();
    private Map<String, Book> bookMap;

    // 基础控件 - 与FXML文件中的fx:id匹配
    @FXML private Button refreshButton;
    @FXML private TextField userSearchField;
    @FXML private TextField bookSearchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button applyFilterButton;
    @FXML private Button resetFilterButton;

    // 统计标签
    @FXML private Label activeBorrowsLabel;
    @FXML private Label returnedBooksLabel;
    @FXML private Label overdueBooksLabel;
    @FXML private Label totalRecordsLabel;

    // 表格
    @FXML private TableView<BorrowRecord> borrowRecordsTable;
    @FXML private TableColumn<BorrowRecord, Integer> recordIdCol;
    @FXML private TableColumn<BorrowRecord, String> userIdCol;
    @FXML private TableColumn<BorrowRecord, String> bookIdCol;
    @FXML private TableColumn<BorrowRecord, String> bookTitleCol;
    @FXML private TableColumn<BorrowRecord, String> borrowDateCol;
    @FXML private TableColumn<BorrowRecord, String> dueDateCol;
    @FXML private TableColumn<BorrowRecord, String> returnDateCol;
    @FXML private TableColumn<BorrowRecord, String> statusCol;
    @FXML private TableColumn<BorrowRecord, String> fineCol;
    @FXML private TableColumn<BorrowRecord, Void> actionCol;

    // 分页控件
    @FXML private Button firstPageButton;
    @FXML private Button prevPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private Button nextPageButton;
    @FXML private Button lastPageButton;
    @FXML private ComboBox<Integer> pageSizeCombo;

    // 分页变量
    private int currentPage = 1;
    private int pageSize = 20;
    private int totalPages = 1;

    /**
     * 修复的setClientService方法
     */
    public void setClientService(ClientService clientService) {
        System.out.println("=== BorrowManagementController.setClientService 被调用 ===");
        this.clientService = clientService;

        if (clientService != null) {
            System.out.println("ClientService 设置成功，连接状态: " + clientService.isConnected());
            initializeData();
        } else {
            System.err.println("传入的 ClientService 为 null");
        }
    }

    /**
     * 修复的获取ClientService方法
     */
    private ClientService getClientService() {
        // 如果本地clientService为空，尝试从ServiceManager获取
        if (clientService == null) {
            System.out.println("本地 clientService 为 null，尝试从 ServiceManager 获取");
            clientService = ServiceManager.getInstance().getClientService();

            if (clientService != null) {
                System.out.println("从 ServiceManager 获取 ClientService 成功");
            } else {
                System.err.println("从 ServiceManager 获取 ClientService 失败");
            }
        }
        return clientService;
    }

    @FXML
    public void initialize() {
        System.out.println("=== BorrowManagementController 初始化开始 ===");

        setupTableView();
        setupFormControls();
        bindEvents();

        // 延迟初始化数据，给 ServiceManager 时间设置 ClientService
        javafx.application.Platform.runLater(() -> {
            ClientService service = getClientService();
            if (service != null) {
                System.out.println("延迟初始化数据开始");
                initializeData();
            } else {
                System.err.println("无法获取 ClientService，可能需要重新登录");
                showAlert("错误", "无法连接到服务器，请重新登录");
            }
        });
    }

    private void setupTableView() {
        // 配置表格列
        if (recordIdCol != null) recordIdCol.setCellValueFactory(new PropertyValueFactory<>("recordId"));
        if (userIdCol != null) userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        if (bookIdCol != null) bookIdCol.setCellValueFactory(new PropertyValueFactory<>("bookId"));

        // 图书标题列
        if (bookTitleCol != null) {
            bookTitleCol.setCellValueFactory(cellData -> {
                String bookId = cellData.getValue().getBookId();
                String title = getBookTitle(bookId);
                return new SimpleStringProperty(title);
            });
        }

        // 日期格式化
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        if (borrowDateCol != null) {
            borrowDateCol.setCellValueFactory(cellData -> {
                Date date = cellData.getValue().getBorrowDate();
                return new SimpleStringProperty(date != null ? dateFormat.format(date) : "");
            });
        }

        if (dueDateCol != null) {
            dueDateCol.setCellValueFactory(cellData -> {
                Date date = cellData.getValue().getReturnDate();
                return new SimpleStringProperty(date != null ? dateFormat.format(date) : "");
            });
        }

        if (returnDateCol != null) {
            returnDateCol.setCellValueFactory(cellData -> {
                BorrowRecord record = cellData.getValue();
                if ("RETURNED".equals(record.getStatus()) && record.getReturnDate() != null) {
                    return new SimpleStringProperty(dateFormat.format(record.getReturnDate()));
                }
                return new SimpleStringProperty("未归还");
            });
        }

        if (statusCol != null) statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 罚金列
        if (fineCol != null) {
            fineCol.setCellValueFactory(cellData -> {
                BorrowRecord record = cellData.getValue();
                double fine = calculateFine(record);
                return new SimpleStringProperty(fine > 0 ? String.format("%.2f元", fine) : "无");
            });
        }

        // 操作列
        setupActionColumn();

        // 表格选择事件
        if (borrowRecordsTable != null) {
            borrowRecordsTable.setItems(filteredRecords);
        }
    }

    private void setupActionColumn() {
        if (actionCol == null) return;

        actionCol.setCellFactory(new Callback<TableColumn<BorrowRecord, Void>, TableCell<BorrowRecord, Void>>() {
            @Override
            public TableCell<BorrowRecord, Void> call(TableColumn<BorrowRecord, Void> param) {
                return new TableCell<BorrowRecord, Void>() {
                    private final Button returnBtn = new Button("归还");
                    private final Button renewBtn = new Button("续借");

                    {
                        returnBtn.getStyleClass().add("btn-warning");
                        renewBtn.getStyleClass().add("btn-success");

                        returnBtn.setOnAction(event -> {
                            BorrowRecord record = getTableView().getItems().get(getIndex());
                            handleForceReturn(record);
                        });

                        renewBtn.setOnAction(event -> {
                            BorrowRecord record = getTableView().getItems().get(getIndex());
                            handleRenew(record);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            BorrowRecord record = getTableView().getItems().get(getIndex());
                            if ("BORROWED".equals(record.getStatus())) {
                                HBox box = new HBox(5);
                                box.getChildren().addAll(returnBtn, renewBtn);
                                setGraphic(box);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        });
    }

    private void setupFormControls() {
        if (statusFilterCombo != null) {
            statusFilterCombo.getItems().addAll("全部", "BORROWED", "RETURNED", "OVERDUE");
            statusFilterCombo.setValue("全部");
        }

        if (pageSizeCombo != null) {
            pageSizeCombo.getItems().addAll(10, 20, 50, 100);
            pageSizeCombo.setValue(pageSize);
        }
    }

    private void bindEvents() {
        if (refreshButton != null) refreshButton.setOnAction(e -> handleRefresh());
        if (applyFilterButton != null) applyFilterButton.setOnAction(e -> applyFilters());
        if (resetFilterButton != null) resetFilterButton.setOnAction(e -> resetFilters());

        // 分页控制
        if (firstPageButton != null) firstPageButton.setOnAction(e -> goToPage(1));
        if (prevPageButton != null) prevPageButton.setOnAction(e -> goToPage(currentPage - 1));
        if (nextPageButton != null) nextPageButton.setOnAction(e -> goToPage(currentPage + 1));
        if (lastPageButton != null) lastPageButton.setOnAction(e -> goToPage(totalPages));

        if (pageSizeCombo != null) {
            pageSizeCombo.setOnAction(e -> {
                pageSize = pageSizeCombo.getValue();
                currentPage = 1;
                updatePagination();
            });
        }
    }

    /**
     * 修复的初始化数据方法
     */
    private void initializeData() {
        ClientService service = getClientService();
        if (service == null) {
            System.err.println("ClientService 未初始化，无法加载数据");
            showAlert("错误", "服务未初始化，请重新登录");
            return;
        }

        System.out.println("开始加载借阅记录和图书数据...");
        loadBorrowRecordsData();
        loadBooksMap();
    }

    /**
     * 修复的加载借阅记录方法
     */
    private void loadBorrowRecordsData() {
        Task<List<BorrowRecord>> task = new Task<List<BorrowRecord>>() {
            @Override
            protected List<BorrowRecord> call() throws Exception {
                ClientService service = getClientService();
                if (service == null) {
                    throw new Exception("ClientService 为 null");
                }

                System.out.println("发送借阅记录查询请求...");
                Message request = new Message(Type.BORROW_RECORD_GET_ALL, null);
                Message response = service.sendRequest(request);

                if (response != null && response.getCode() == Message.Code.SUCCESS) {
                    List<BorrowRecord> records = (List<BorrowRecord>) response.getData();
                    System.out.println("成功获取 " + (records != null ? records.size() : 0) + " 条借阅记录");
                    return records;
                } else {
                    String errorMsg = response != null ? response.getData().toString() : "无响应";
                    throw new Exception("服务器返回错误: " + errorMsg);
                }
            }

            @Override
            protected void succeeded() {
                List<BorrowRecord> records = getValue();
                if (records != null) {
                    allRecords.setAll(records);
                    filteredRecords.setAll(records);
                    updateStatistics();
                    updatePagination();
                    //showAlert("成功", "借阅记录加载成功，共 " + records.size() + " 条记录");
                    System.out.println("借阅记录数据加载完成");
                } else {
                    showAlert("提示", "未找到任何借阅记录");
                }
            }

            @Override
            protected void failed() {
                String errorMsg = getException().getMessage();
                System.err.println("加载借阅记录失败: " + errorMsg);
                showAlert("错误", "加载借阅记录失败: " + errorMsg);
            }
        };

        new Thread(task).start();
    }

    private void loadBooksMap() {
        Task<List<Book>> task = new Task<List<Book>>() {
            @Override
            protected List<Book> call() throws Exception {
                ClientService service = getClientService();
                if (service == null) {
                    throw new Exception("ClientService 为 null");
                }

                Message request = new Message(Type.BOOK_LIST, null);
                Message response = service.sendRequest(request);

                if (response != null && response.getCode() == Message.Code.SUCCESS) {
                    return (List<Book>) response.getData();
                }
                return null;
            }

            @Override
            protected void succeeded() {
                List<Book> books = getValue();
                if (books != null) {
                    bookMap = books.stream()
                            .collect(Collectors.toMap(Book::getBookId, book -> book));
                    System.out.println("图书数据映射创建完成，共 " + books.size() + " 本图书");
                }
            }
        };

        new Thread(task).start();
    }

    @FXML
    private void handleRefresh() {
        System.out.println("手动刷新借阅记录数据");
        resetFilters();
        loadBorrowRecordsData();
    }

    // 以下方法保持不变...
    private void applyFilters() {
        String userFilter = userSearchField != null ? userSearchField.getText() : "";
        String bookFilter = bookSearchField != null ? bookSearchField.getText() : "";
        String statusFilter = statusFilterCombo != null ? statusFilterCombo.getValue() : "全部";
        LocalDate startDate = startDatePicker != null ? startDatePicker.getValue() : null;
        LocalDate endDate = endDatePicker != null ? endDatePicker.getValue() : null;

        List<BorrowRecord> filtered = allRecords.stream()
                .filter(record -> {
                    if (userFilter != null && !userFilter.trim().isEmpty()) {
                        if (!record.getUserId().toLowerCase().contains(userFilter.toLowerCase().trim())) {
                            return false;
                        }
                    }

                    if (bookFilter != null && !bookFilter.trim().isEmpty()) {
                        String bookTitle = getBookTitle(record.getBookId());
                        if (!record.getBookId().toLowerCase().contains(bookFilter.toLowerCase().trim()) &&
                                !bookTitle.toLowerCase().contains(bookFilter.toLowerCase().trim())) {
                            return false;
                        }
                    }

                    if (statusFilter != null && !"全部".equals(statusFilter)) {
                        if (!statusFilter.equals(record.getStatus())) {
                            return false;
                        }
                    }

                    if (startDate != null && record.getBorrowDate() != null) {
                        LocalDate recordDate = record.getBorrowDate().toInstant()
                                .atZone(ZoneId.systemDefault()).toLocalDate();
                        if (recordDate.isBefore(startDate)) {
                            return false;
                        }
                    }

                    if (endDate != null && record.getBorrowDate() != null) {
                        LocalDate recordDate = record.getBorrowDate().toInstant()
                                .atZone(ZoneId.systemDefault()).toLocalDate();
                        if (recordDate.isAfter(endDate)) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

        filteredRecords.setAll(filtered);
        currentPage = 1;
        updateStatistics();
        updatePagination();
    }

    private void resetFilters() {
        if (userSearchField != null) userSearchField.clear();
        if (bookSearchField != null) bookSearchField.clear();
        if (statusFilterCombo != null) statusFilterCombo.setValue("全部");
        if (startDatePicker != null) startDatePicker.setValue(null);
        if (endDatePicker != null) endDatePicker.setValue(null);

        filteredRecords.setAll(allRecords);
        currentPage = 1;
        updateStatistics();
        updatePagination();
    }

    private void handleForceReturn(BorrowRecord record) {
        if (!"BORROWED".equals(record.getStatus())) {
            showAlert("提示", "该记录状态不允许归还操作");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认强制归还");
        confirmAlert.setContentText("确定要强制归还用户 " + record.getUserId() + " 借阅的图书吗？");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            Task<Boolean> task = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    ClientService service = getClientService();
                    if (service == null) {
                        throw new Exception("ClientService 为 null");
                    }

                    Message request = new Message(Type.BORROW_RECORD_RETURN, record.getRecordId());
                    Message response = service.sendRequest(request);
                    return response != null && response.getCode() == Message.Code.SUCCESS;
                }

                @Override
                protected void succeeded() {
                    if (getValue()) {
                        showAlert("成功", "强制归还成功");
                        loadBorrowRecordsData();
                    } else {
                        showAlert("错误", "强制归还失败");
                    }
                }

                @Override
                protected void failed() {
                    showAlert("错误", "操作失败: " + getException().getMessage());
                }
            };

            new Thread(task).start();
        }
    }

    private void handleRenew(BorrowRecord record) {
        if (!"BORROWED".equals(record.getStatus())) {
            showAlert("提示", "该记录状态不允许续借操作");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认续借");
        confirmAlert.setContentText("确定要为用户 " + record.getUserId() + " 续借图书吗？");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            Task<Boolean> task = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    ClientService service = getClientService();
                    if (service == null) {
                        throw new Exception("ClientService 为 null");
                    }

                    Message request = new Message(Type.BOOK_RENEW, record.getRecordId());
                    Message response = service.sendRequest(request);
                    return response != null && response.getCode() == Message.Code.SUCCESS;
                }

                @Override
                protected void succeeded() {
                    if (getValue()) {
                        showAlert("成功", "续借成功");
                        loadBorrowRecordsData();
                    } else {
                        showAlert("错误", "续借失败");
                    }
                }

                @Override
                protected void failed() {
                    showAlert("错误", "操作失败: " + getException().getMessage());
                }
            };

            new Thread(task).start();
        }
    }

    private void updateStatistics() {
        long active = filteredRecords.stream().filter(r -> "BORROWED".equals(r.getStatus())).count();
        long returned = filteredRecords.stream().filter(r -> "RETURNED".equals(r.getStatus())).count();
        long overdue = filteredRecords.stream().filter(r -> "BORROWED".equals(r.getStatus()) && isOverdue(r)).count();

        if (activeBorrowsLabel != null) activeBorrowsLabel.setText(String.valueOf(active));
        if (returnedBooksLabel != null) returnedBooksLabel.setText(String.valueOf(returned));
        if (overdueBooksLabel != null) overdueBooksLabel.setText(String.valueOf(overdue));
        if (totalRecordsLabel != null) totalRecordsLabel.setText(String.valueOf(filteredRecords.size()));
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) filteredRecords.size() / pageSize);
        if (totalPages == 0) totalPages = 1;

        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, filteredRecords.size());

        List<BorrowRecord> pageRecords = filteredRecords.subList(start, end);
        if (borrowRecordsTable != null) {
            borrowRecordsTable.setItems(FXCollections.observableArrayList(pageRecords));
        }

        if (firstPageButton != null) firstPageButton.setDisable(currentPage == 1);
        if (prevPageButton != null) prevPageButton.setDisable(currentPage == 1);
        if (nextPageButton != null) nextPageButton.setDisable(currentPage == totalPages);
        if (lastPageButton != null) lastPageButton.setDisable(currentPage == totalPages);

        if (pageInfoLabel != null) {
            pageInfoLabel.setText("第 " + currentPage + " 页，共 " + totalPages + " 页");
        }
    }

    private void goToPage(int page) {
        if (page >= 1 && page <= totalPages) {
            currentPage = page;
            updatePagination();
        }
    }

    private String getBookTitle(String bookId) {
        if (bookMap != null && bookMap.containsKey(bookId)) {
            return bookMap.get(bookId).getTitle();
        }
        return "未知书名";
    }

    private boolean isOverdue(BorrowRecord record) {
        return "BORROWED".equals(record.getStatus()) &&
                record.getReturnDate() != null &&
                new Date().after(record.getReturnDate());
    }

    private long getOverdueDays(BorrowRecord record) {
        if (!isOverdue(record)) return 0;
        long diffInMillies = new Date().getTime() - record.getReturnDate().getTime();
        return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    private double calculateFine(BorrowRecord record) {
        if (!isOverdue(record)) return 0.0;
        return getOverdueDays(record) * 0.5; // 每天罚金0.5元
    }

    private void showAlert(String title, String content) {
        javafx.application.Platform.runLater(() -> {
            try {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(content);
                alert.showAndWait();
            } catch (Exception e) {
                System.err.println("显示提示框失败: " + e.getMessage());
            }
        });
    }
}