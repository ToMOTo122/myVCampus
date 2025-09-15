// ============= 管理员课程管理控制器 - 修复teacher_id问题 =============
package com.vcampus.client.controller.course;

import com.vcampus.client.service.ClientService;
import com.vcampus.common.entity.Course;
import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class AdminCourseController implements Initializable {

    // 搜索和筛选控件
    @FXML private TextField searchField;
    @FXML private ComboBox<String> searchDepartmentFilter;
    @FXML private ComboBox<String> searchTypeFilter;
    @FXML private Button searchButton;

    // 课程表单控件
    @FXML private TextField courseIdField;
    @FXML private TextField courseNameField;
    @FXML private ComboBox<String> teacherComboBox;
    @FXML private TextField creditsField;
    @FXML private TextField capacityField;
    @FXML private ComboBox<String> departmentComboBox;
    @FXML private ComboBox<String> courseTypeComboBox;
    @FXML private ComboBox<String> classroomComboBox;
    @FXML private ComboBox<String> semesterComboBox;
    @FXML private TextArea descriptionArea;

    // 时间安排控件
    @FXML private ComboBox<String> weekDayComboBox;
    @FXML private ComboBox<String> startPeriodComboBox;
    @FXML private ComboBox<String> endPeriodComboBox;
    @FXML private ListView<String> selectedTimeSlotsListView;

    // 操作按钮
    @FXML private Button addCourseButton;
    @FXML private Button updateCourseButton;
    @FXML private Button deleteCourseButton;

    // 课程列表表格
    @FXML private TableView<Course> coursesTable;
    @FXML private TableColumn<Course, String> adminCourseIdCol;
    @FXML private TableColumn<Course, String> adminCourseNameCol;
    @FXML private TableColumn<Course, String> adminTeacherCol;
    @FXML private TableColumn<Course, Integer> adminCreditsCol;
    @FXML private TableColumn<Course, String> adminScheduleCol;
    @FXML private TableColumn<Course, Integer> adminEnrolledCol;
    @FXML private TableColumn<Course, String> adminStatusCol;

    private ClientService clientService;
    private User currentUser;
    private ObservableList<Course> allCourses = FXCollections.observableArrayList();
    private FilteredList<Course> filteredCourses;
    private ObservableList<String> selectedTimeSlots = FXCollections.observableArrayList();

    // 教师信息映射：教师姓名 -> 教师ID
    private Map<String, String> teacherNameToIdMap = new HashMap<>();
    private Map<String, String> teacherIdToNameMap = new HashMap<>();

    // 网络状态和重连相关
    private volatile boolean isConnected = true;
    private Course pendingCourse = null;
    private int maxRetryAttempts = 3;
    private int currentRetryCount = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("AdminCourseController 初始化开始");

        try {
            initializeFilteredList();
            initializeTableColumns();
            initializeComboBoxes();
            initializeTimeSlotsList();
            setupEventHandlers();

            System.out.println("AdminCourseController 初始化完成");
        } catch (Exception e) {
            System.err.println("初始化AdminCourseController时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeFilteredList() {
        filteredCourses = new FilteredList<>(allCourses);
    }

    public void setClientService(ClientService clientService) {
        this.clientService = clientService;
        System.out.println("AdminCourseController 设置ClientService: " + (clientService != null ? "成功" : "失败"));
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        System.out.println("设置当前管理员用户: " + (currentUser != null ? currentUser.getDisplayName() : "null"));

        if (currentUser != null && clientService != null) {
            loadAdminCourseData();
            loadTeachers(); // 重要：加载教师信息
        }
    }

    private void initializeTableColumns() {
        try {
            // 设置表格列
            if (adminCourseIdCol != null) adminCourseIdCol.setCellValueFactory(new PropertyValueFactory<>("courseId"));
            if (adminCourseNameCol != null) adminCourseNameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
            if (adminTeacherCol != null) adminTeacherCol.setCellValueFactory(new PropertyValueFactory<>("teacherName"));
            if (adminCreditsCol != null) adminCreditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
            if (adminScheduleCol != null) adminScheduleCol.setCellValueFactory(new PropertyValueFactory<>("schedule"));
            if (adminEnrolledCol != null) adminEnrolledCol.setCellValueFactory(new PropertyValueFactory<>("enrolled"));

            // 状态列自定义显示
            if (adminStatusCol != null) {
                adminStatusCol.setCellFactory(column -> new TableCell<Course, String>() {
                    @Override
                    protected void updateItem(String status, boolean empty) {
                        super.updateItem(status, empty);
                        if (empty || getTableView() == null || getIndex() >= getTableView().getItems().size()) {
                            setText(null);
                            setStyle("");
                        } else {
                            Course course = getTableView().getItems().get(getIndex());
                            if (course.getEnrolled() >= course.getCapacity()) {
                                setText("已满员");
                                setStyle("-fx-text-fill: #e53e3e; -fx-font-weight: bold;");
                            } else if (course.getEnrolled() > course.getCapacity() * 0.8) {
                                setText("接近满员");
                                setStyle("-fx-text-fill: #ff8c00; -fx-font-weight: bold;");
                            } else {
                                setText("正常");
                                setStyle("-fx-text-fill: #38a169; -fx-font-weight: bold;");
                            }
                        }
                    }
                });
            }

            // 设置表格数据
            if (coursesTable != null) {
                coursesTable.setItems(filteredCourses);
            }

            System.out.println("管理员表格列初始化完成");
        } catch (Exception e) {
            System.err.println("初始化管理员表格列时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeComboBoxes() {
        try {
            // 搜索筛选器初始化
            if (searchDepartmentFilter != null) {
                searchDepartmentFilter.getItems().addAll("全部", "计算机学院", "数学学院", "物理学院",
                        "化学学院", "外语学院", "经管学院", "文学院");
                searchDepartmentFilter.setValue("全部");
            }

            if (searchTypeFilter != null) {
                searchTypeFilter.getItems().addAll("全部", "必修", "选修", "实践", "通识");
                searchTypeFilter.setValue("全部");
            }

            // 课程表单下拉框初始化
            if (departmentComboBox != null) {
                departmentComboBox.getItems().addAll("计算机学院", "数学学院", "物理学院",
                        "化学学院", "外语学院", "经管学院", "文学院");
            }

            if (courseTypeComboBox != null) {
                courseTypeComboBox.getItems().addAll("必修", "选修", "实践", "通识");
            }

            if (classroomComboBox != null) {
                classroomComboBox.getItems().addAll("A101", "A102", "A201", "A202", "B101", "B102",
                        "C101", "C102", "实验室1", "实验室2");
            }

            if (semesterComboBox != null) {
                semesterComboBox.getItems().addAll("2024-1", "2024-2", "2025-1", "2025-2", "2026-1", "2026-2");
                semesterComboBox.setValue("2025-1");
            }

            // 时间安排下拉框
            if (weekDayComboBox != null) {
                weekDayComboBox.getItems().addAll("周一", "周二", "周三", "周四", "周五");
            }

            if (startPeriodComboBox != null) {
                startPeriodComboBox.getItems().addAll("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
            }

            if (endPeriodComboBox != null) {
                endPeriodComboBox.getItems().addAll("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
            }

            System.out.println("下拉框初始化完成");
        } catch (Exception e) {
            System.err.println("初始化下拉框时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeTimeSlotsList() {
        if (selectedTimeSlotsListView != null) {
            selectedTimeSlotsListView.setItems(selectedTimeSlots);
        }
    }

    private void setupEventHandlers() {
        try {
            // 搜索事件处理
            if (searchField != null) {
                searchField.textProperty().addListener((observable, oldValue, newValue) -> filterCourses());
            }
            if (searchDepartmentFilter != null) {
                searchDepartmentFilter.setOnAction(e -> filterCourses());
            }
            if (searchTypeFilter != null) {
                searchTypeFilter.setOnAction(e -> filterCourses());
            }

            // 表格选择事件
            if (coursesTable != null) {
                coursesTable.getSelectionModel().selectedItemProperty().addListener(
                        (observable, oldValue, newValue) -> {
                            if (newValue != null) {
                                loadCourseToForm(newValue);
                                enableEditMode(true);
                            }
                        });
            }

            System.out.println("事件处理器设置完成");
        } catch (Exception e) {
            System.err.println("设置管理员事件处理器时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 修复的关键方法：加载教师信息包括ID映射
    private void loadTeachers() {
        if (clientService == null) {
            System.out.println("无法加载教师信息: clientService为null");
            return;
        }

        try {
            System.out.println("开始加载教师信息...");

            // 从服务器获取教师列表
            Message request = new Message(Message.Type.USER_LIST, User.Role.TEACHER);
            Message response = clientService.sendRequest(request);

            if (response != null && response.getCode() == Message.Code.SUCCESS) {
                @SuppressWarnings("unchecked")
                List<User> teachers = (List<User>) response.getData();

                if (teachers != null && !teachers.isEmpty()) {
                    // 清空现有数据
                    teacherNameToIdMap.clear();
                    teacherIdToNameMap.clear();
                    if (teacherComboBox != null) {
                        teacherComboBox.getItems().clear();
                    }

                    // 构建教师信息映射
                    for (User teacher : teachers) {
                        if (teacher.getRole() == User.Role.TEACHER) {
                            String teacherId = teacher.getUserId();
                            String teacherName = teacher.getDisplayName();

                            // 建立双向映射
                            teacherNameToIdMap.put(teacherName, teacherId);
                            teacherIdToNameMap.put(teacherId, teacherName);

                            // 添加到下拉框
                            if (teacherComboBox != null) {
                                teacherComboBox.getItems().add(teacherName);
                            }
                        }
                    }

                    System.out.println("成功加载 " + teachers.size() + " 位教师信息");
                    System.out.println("教师映射表: " + teacherNameToIdMap);
                } else {
                    System.out.println("教师列表为空，使用默认数据");
                    loadDefaultTeachers();
                }
            } else {
                System.out.println("获取教师列表失败，使用默认数据");
                loadDefaultTeachers();
            }
        } catch (Exception e) {
            System.err.println("加载教师信息时发生错误: " + e.getMessage());
            e.printStackTrace();
            loadDefaultTeachers();
        }
    }

    // 备用方案：加载默认教师数据
    private void loadDefaultTeachers() {
        try {
            if (teacherComboBox != null) {
                teacherComboBox.getItems().clear();
                teacherNameToIdMap.clear();
                teacherIdToNameMap.clear();

                // 默认教师数据（实际应该从数据库获取）
                String[][] defaultTeachers = {
                        {"T001", "张教授"},
                        {"T002", "李教授"},
                        {"T003", "王教授"},
                        {"T004", "赵教授"},
                        {"T005", "刘教授"}
                };

                for (String[] teacher : defaultTeachers) {
                    String teacherId = teacher[0];
                    String teacherName = teacher[1];

                    teacherNameToIdMap.put(teacherName, teacherId);
                    teacherIdToNameMap.put(teacherId, teacherName);
                    teacherComboBox.getItems().add(teacherName);
                }

                System.out.println("加载默认教师数据完成: " + teacherNameToIdMap);
            }
        } catch (Exception e) {
            System.err.println("加载默认教师数据时发生错误: " + e.getMessage());
        }
    }

    // 根据教师姓名获取教师ID的方法
    private String getTeacherIdByName(String teacherName) {
        if (teacherName == null || teacherName.trim().isEmpty()) {
            return null;
        }

        String teacherId = teacherNameToIdMap.get(teacherName);
        if (teacherId == null) {
            System.out.println("警告: 未找到教师 '" + teacherName + "' 的ID映射");
            // 如果找不到映射，尝试使用教师名称作为ID（临时方案）
            return teacherName;
        }

        return teacherId;
    }

    // 根据教师ID获取教师姓名的方法
    private String getTeacherNameById(String teacherId) {
        if (teacherId == null || teacherId.trim().isEmpty()) {
            return null;
        }

        String teacherName = teacherIdToNameMap.get(teacherId);
        if (teacherName == null) {
            System.out.println("警告: 未找到教师ID '" + teacherId + "' 的姓名映射");
            return teacherId;
        }

        return teacherName;
    }

    private void filterCourses() {
        try {
            String searchText = searchField != null ? searchField.getText().toLowerCase().trim() : "";
            String selectedDepartment = searchDepartmentFilter != null ? searchDepartmentFilter.getValue() : "全部";
            String selectedType = searchTypeFilter != null ? searchTypeFilter.getValue() : "全部";

            System.out.println("管理员筛选条件 - 搜索: '" + searchText + "', 院系: " + selectedDepartment + ", 类型: " + selectedType);

            Predicate<Course> combinedFilter = course -> {
                // 文本搜索过滤
                boolean textMatch = searchText.isEmpty() ||
                        course.getCourseName().toLowerCase().contains(searchText) ||
                        course.getCourseId().toLowerCase().contains(searchText) ||
                        (course.getTeacherName() != null && course.getTeacherName().toLowerCase().contains(searchText));

                // 院系过滤
                boolean departmentMatch = "全部".equals(selectedDepartment) ||
                        (course.getDepartment() != null && course.getDepartment().equals(selectedDepartment));

                // 课程类型过滤
                boolean typeMatch = "全部".equals(selectedType) ||
                        (course.getCourseType() != null && course.getCourseType().equals(selectedType));

                return textMatch && departmentMatch && typeMatch;
            };

            filteredCourses.setPredicate(combinedFilter);

            System.out.println("管理员筛选结果: " + filteredCourses.size() + " 门课程");
        } catch (Exception e) {
            System.err.println("筛选管理员课程时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        System.out.println("管理员执行搜索...");
        filterCourses();
    }

    // 网络连接检测方法
    private boolean checkConnection() {
        try {
            if (clientService == null) {
                System.out.println("ClientService为null，连接检测失败");
                return false;
            }

            // 发送心跳检测 - 注意这里需要你的Message类支持PING类型
            // 如果没有PING类型，可以使用其他简单的请求
            Message pingRequest = new Message(Message.Type.COURSE_LIST, null);
            Message pingResponse = clientService.sendRequest(pingRequest);

            boolean connected = pingResponse != null && pingResponse.getCode() == Message.Code.SUCCESS;
            isConnected = connected;
            System.out.println("连接检测结果: " + (connected ? "已连接" : "未连接"));
            return connected;
        } catch (Exception e) {
            System.err.println("连接检测异常: " + e.getMessage());
            isConnected = false;
            return false;
        }
    }

    private void loadAdminCourseData() {
        if (clientService == null) {
            System.out.println("无法加载管理员数据: clientService为null");
            return;
        }

        try {
            System.out.println("开始加载管理员课程数据...");

            Message request = new Message(Message.Type.COURSE_LIST, null);
            Message response = clientService.sendRequest(request);

            if (response != null && response.getCode() == Message.Code.SUCCESS) {
                @SuppressWarnings("unchecked")
                List<Course> courses = (List<Course>) response.getData();
                if (courses != null) {
                    allCourses.setAll(courses);
                    isConnected = true;
                    System.out.println("成功加载 " + courses.size() + " 门课程到管理界面");
                } else {
                    System.out.println("管理员课程数据为空");
                }
            } else {
                System.out.println("加载管理员课程失败: " +
                        (response != null ? response.getData() : "无响应"));
                showAlert("加载失败", "无法获取课程数据，请检查网络连接");
                isConnected = false;
            }
        } catch (Exception e) {
            System.err.println("加载管理员课程数据时发生错误: " + e.getMessage());
            isConnected = false;
            showAlert("系统错误", "加载数据时发生错误: " + e.getMessage());
        }
    }

    private void loadCourseToForm(Course course) {
        if (course == null) return;

        try {
            if (courseIdField != null) courseIdField.setText(course.getCourseId());
            if (courseNameField != null) courseNameField.setText(course.getCourseName());
            if (teacherComboBox != null) {
                // 优先使用教师姓名，如果没有则尝试通过ID查找
                String teacherName = course.getTeacherName();
                if (teacherName == null && course.getTeacherId() != null) {
                    teacherName = getTeacherNameById(course.getTeacherId());
                }
                teacherComboBox.setValue(teacherName);
            }
            if (creditsField != null) creditsField.setText(String.valueOf(course.getCredits()));
            if (capacityField != null) capacityField.setText(String.valueOf(course.getCapacity()));
            if (departmentComboBox != null) departmentComboBox.setValue(course.getDepartment());
            if (courseTypeComboBox != null) courseTypeComboBox.setValue(course.getCourseType());
            if (classroomComboBox != null) classroomComboBox.setValue(course.getClassroom());
            if (semesterComboBox != null) semesterComboBox.setValue(course.getSemester());
            if (descriptionArea != null) descriptionArea.setText(course.getDescription());

            // 解析并显示时间安排
            parseAndDisplaySchedule(course.getSchedule());

            System.out.println("课程信息已加载到表单: " + course.getCourseName());
        } catch (Exception e) {
            System.err.println("加载课程到表单时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void parseAndDisplaySchedule(String schedule) {
        selectedTimeSlots.clear();
        if (schedule != null && !schedule.trim().isEmpty()) {
            String[] timeSegments = schedule.split("[,，]");
            for (String segment : timeSegments) {
                selectedTimeSlots.add(segment.trim());
            }
        }
    }

    private void enableEditMode(boolean enable) {
        if (updateCourseButton != null) updateCourseButton.setDisable(!enable);
        if (deleteCourseButton != null) deleteCourseButton.setDisable(!enable);
        if (addCourseButton != null) addCourseButton.setDisable(enable);
    }

    @FXML
    private void handleAddTimeSlot() {
        try {
            String weekDay = weekDayComboBox != null ? weekDayComboBox.getValue() : null;
            String startPeriod = startPeriodComboBox != null ? startPeriodComboBox.getValue() : null;
            String endPeriod = endPeriodComboBox != null ? endPeriodComboBox.getValue() : null;

            if (weekDay == null || startPeriod == null || endPeriod == null) {
                showAlert("输入错误", "请选择完整的时间信息");
                return;
            }

            String timeSlot = weekDay + startPeriod + "-" + endPeriod + "节";
            if (!selectedTimeSlots.contains(timeSlot)) {
                selectedTimeSlots.add(timeSlot);
                System.out.println("添加时间段: " + timeSlot);
            } else {
                showAlert("时间冲突", "该时间段已存在");
            }
        } catch (Exception e) {
            System.err.println("添加时间段时发生错误: " + e.getMessage());
            showAlert("系统错误", "添加时间段失败: " + e.getMessage());
        }
    }

    @FXML
    private void handleRemoveTimeSlot() {
        try {
            String selected = selectedTimeSlotsListView != null ?
                    selectedTimeSlotsListView.getSelectionModel().getSelectedItem() : null;
            if (selected != null) {
                selectedTimeSlots.remove(selected);
                System.out.println("移除时间段: " + selected);
            } else {
                showAlert("选择提示", "请先选择要移除的时间段");
            }
        } catch (Exception e) {
            System.err.println("移除时间段时发生错误: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearAllTimeSlots() {
        try {
            selectedTimeSlots.clear();
            System.out.println("清空所有时间段");
        } catch (Exception e) {
            System.err.println("清空时间段时发生错误: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddCourse() {
        System.out.println("管理员添加课程...");

        // 先检查连接状态
        if (!isConnected && !checkConnection()) {
            showAlert("连接错误", "无法连接到服务器，请检查网络连接");
            return;
        }

        try {
            Course course = createCourseFromForm();
            if (course != null) {
                submitCourse(course);
            }
        } catch (Exception e) {
            System.err.println("添加课程时发生错误: " + e.getMessage());
            showAlert("系统错误", "添加课程失败: " + e.getMessage());
        }
    }

    private void submitCourse(Course course) {
        System.out.println("提交课程数据: " + course.getCourseName());
        System.out.println("课程教师ID: " + course.getTeacherId());
        System.out.println("课程教师姓名: " + course.getTeacherName());

        try {
            Message request = new Message(Message.Type.ADD_COURSE, course);
            Message response = clientService.sendRequest(request);

            if (response != null && response.getCode() == Message.Code.SUCCESS) {
                pendingCourse = null;
                loadAdminCourseData();
                handleClearForm();
                showAlert("操作成功", "课程添加成功");
                System.out.println("课程添加成功: " + course.getCourseName());
            } else {
                String errorMsg = response != null ? response.getData().toString() : "提交失败";
                showAlert("操作失败", errorMsg);
                System.out.println("课程添加失败: " + errorMsg);
            }
        } catch (Exception e) {
            System.err.println("提交课程时发生异常: " + e.getMessage());
            showAlert("系统错误", "提交课程失败: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateCourse() {
        System.out.println("管理员更新课程...");

        if (!isConnected && !checkConnection()) {
            showAlert("连接错误", "无法连接到服务器，请检查网络连接");
            return;
        }

        try {
            Course course = createCourseFromForm();
            if (course != null) {
                Message request = new Message(Message.Type.UPDATE_COURSE, course);
                Message response = clientService.sendRequest(request);

                if (response != null && response.getCode() == Message.Code.SUCCESS) {
                    loadAdminCourseData();
                    handleClearForm();
                    showAlert("操作成功", "课程更新成功");
                } else {
                    showAlert("操作失败", response != null ? response.getData().toString() : "更新失败");
                }
            }
        } catch (Exception e) {
            System.err.println("更新课程时发生错误: " + e.getMessage());
            showAlert("系统错误", "更新课程失败: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteCourse() {
        Course selected = coursesTable != null ? coursesTable.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showAlert("选择提示", "请先选择要删除的课程");
            return;
        }

        if (!isConnected && !checkConnection()) {
            showAlert("连接错误", "无法连接到服务器，请检查网络连接");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认删除");
        confirmAlert.setHeaderText("您确定要删除这门课程吗？");
        confirmAlert.setContentText("课程: " + selected.getCourseName() + "\n删除后无法恢复");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Message request = new Message(Message.Type.DELETE_COURSE, selected.getCourseId());
                    Message serverResponse = clientService.sendRequest(request);

                    if (serverResponse != null && serverResponse.getCode() == Message.Code.SUCCESS) {
                        loadAdminCourseData();
                        handleClearForm();
                        showAlert("操作成功", "课程删除成功");
                    } else {
                        showAlert("操作失败", serverResponse != null ? serverResponse.getData().toString() : "删除失败");
                    }
                } catch (Exception e) {
                    System.err.println("删除课程时发生错误: " + e.getMessage());
                    showAlert("系统错误", "删除课程失败: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleClearForm() {
        try {
            if (courseIdField != null) courseIdField.clear();
            if (courseNameField != null) courseNameField.clear();
            if (teacherComboBox != null) teacherComboBox.setValue(null);
            if (creditsField != null) creditsField.clear();
            if (capacityField != null) capacityField.clear();
            if (departmentComboBox != null) departmentComboBox.setValue(null);
            if (courseTypeComboBox != null) courseTypeComboBox.setValue(null);
            if (classroomComboBox != null) classroomComboBox.setValue(null);
            if (semesterComboBox != null) semesterComboBox.setValue("2025-1");
            if (descriptionArea != null) descriptionArea.clear();

            selectedTimeSlots.clear();
            enableEditMode(false);
            pendingCourse = null;

            if (coursesTable != null) {
                coursesTable.getSelectionModel().clearSelection();
            }

            System.out.println("表单已清空");
        } catch (Exception e) {
            System.err.println("清空表单时发生错误: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        System.out.println("管理员刷新数据...");
        try {
            loadAdminCourseData();
            loadTeachers(); // 同时刷新教师信息
            if (isConnected) {
                showAlert("刷新成功", "数据已更新");
            }
        } catch (Exception e) {
            System.err.println("刷新管理员数据时发生错误: " + e.getMessage());
            showAlert("刷新失败", "刷新数据时发生错误: " + e.getMessage());
        }
    }

    // 修复的关键方法：确保创建的课程包含teacherId
    private Course createCourseFromForm() {
        try {
            String courseId = courseIdField != null ? courseIdField.getText() : "";
            String courseName = courseNameField != null ? courseNameField.getText() : "";
            String teacherName = teacherComboBox != null ? teacherComboBox.getValue() : "";
            String creditsText = creditsField != null ? creditsField.getText() : "0";
            String capacityText = capacityField != null ? capacityField.getText() : "0";
            String department = departmentComboBox != null ? departmentComboBox.getValue() : "";
            String courseType = courseTypeComboBox != null ? courseTypeComboBox.getValue() : "";
            String classroom = classroomComboBox != null ? classroomComboBox.getValue() : "";
            String semester = semesterComboBox != null ? semesterComboBox.getValue() : "2025-1";
            String description = descriptionArea != null ? descriptionArea.getText() : "";

            if (courseId.trim().isEmpty() || courseName.trim().isEmpty()) {
                showAlert("输入错误", "课程编号和名称不能为空");
                return null;
            }

            if (teacherName == null || teacherName.trim().isEmpty()) {
                showAlert("输入错误", "请选择授课教师");
                return null;
            }

            // 重要：获取教师ID
            String teacherId = getTeacherIdByName(teacherName);
            if (teacherId == null) {
                showAlert("数据错误", "无法获取教师ID，请重新选择教师");
                return null;
            }

            int credits = Integer.parseInt(creditsText);
            int capacity = Integer.parseInt(capacityText);

            // 构建时间安排字符串
            String schedule = String.join(",", selectedTimeSlots);

            Course course = new Course();
            course.setCourseId(courseId);
            course.setCourseName(courseName);
            course.setTeacherName(teacherName); // 设置教师姓名
            course.setTeacherId(teacherId);     // 重要：设置教师ID
            course.setCredits(credits);
            course.setCapacity(capacity);
            course.setDepartment(department);
            course.setCourseType(courseType);
            course.setClassroom(classroom);
            course.setSemester(semester);
            course.setDescription(description);
            course.setSchedule(schedule);

            System.out.println("创建课程对象 - ID: " + courseId + ", 教师: " + teacherName + ", 教师ID: " + teacherId);
            return course;

        } catch (NumberFormatException e) {
            showAlert("输入错误", "学分和容量必须是有效数字");
            return null;
        } catch (Exception e) {
            System.err.println("从表单创建课程对象时发生错误: " + e.getMessage());
            showAlert("系统错误", "创建课程信息失败: " + e.getMessage());
            return null;
        }
    }

    private void showAlert(String title, String message) {
        try {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            });
        } catch (Exception e) {
            System.err.println("显示管理员提示框时发生错误: " + e.getMessage());
        }
    }
}