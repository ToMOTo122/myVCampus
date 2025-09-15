// ============= 学生选课控制器 - 改进错误处理版本 =============
package com.vcampus.client.controller.course;

import com.vcampus.client.service.ClientService;
import com.vcampus.common.entity.Course;
import com.vcampus.common.entity.CourseSelection;
import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class StudentCourseController implements Initializable {

    @FXML private Label selectedCreditsLabel;
    @FXML private Label totalCreditsLabel;
    @FXML private Label selectedCoursesCountLabel;
    @FXML private Label weeklyHoursDisplayLabel;
    @FXML private ProgressBar creditsProgressBar;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> departmentFilter;
    @FXML private ComboBox<String> typeFilter;
    @FXML private Button searchButton;
    @FXML private TableView<Course> availableCoursesTable;

    // Table columns for available courses
    @FXML private TableColumn<Course, String> courseIdCol;
    @FXML private TableColumn<Course, String> courseNameCol;
    @FXML private TableColumn<Course, String> teacherCol;
    @FXML private TableColumn<Course, Integer> creditsCol;
    @FXML private TableColumn<Course, String> scheduleCol;
    @FXML private TableColumn<Course, String> courseTypeCol;
    @FXML private TableColumn<Course, String> statusCol;
    @FXML private TableColumn<Course, Void> actionCol;

    private ClientService clientService;
    private User currentUser;
    private ObservableList<Course> allCourses = FXCollections.observableArrayList();
    private FilteredList<Course> filteredCourses;
    private Set<String> selectedCourseIds = new HashSet<>(); // 已选课程ID集合

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("StudentCourseController 初始化开始");

        try {
            initializeFilteredList();
            initializeTableColumns();
            initializeFilters();
            setupEventHandlers();
            System.out.println("StudentCourseController 初始化完成");
        } catch (Exception e) {
            System.err.println("初始化StudentCourseController时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeFilteredList() {
        filteredCourses = new FilteredList<>(allCourses);
    }

    public void setClientService(ClientService clientService) {
        this.clientService = clientService;
        System.out.println("StudentCourseController 设置ClientService: " + (clientService != null ? "成功" : "失败"));
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        System.out.println("设置当前学生用户: " + (currentUser != null ? currentUser.getDisplayName() : "null"));

        if (currentUser != null && clientService != null) {
            loadStudentCourseData();
        }
    }

    private void initializeTableColumns() {
        try {
            // 可选课程表格列设置
            if (courseIdCol != null) courseIdCol.setCellValueFactory(new PropertyValueFactory<>("courseId"));
            if (courseNameCol != null) courseNameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
            if (teacherCol != null) teacherCol.setCellValueFactory(new PropertyValueFactory<>("teacherName"));
            if (creditsCol != null) creditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
            if (scheduleCol != null) scheduleCol.setCellValueFactory(new PropertyValueFactory<>("schedule"));

            // 课程类型列
            if (courseTypeCol != null) {
                courseTypeCol.setCellValueFactory(new PropertyValueFactory<>("courseType"));
                courseTypeCol.setCellFactory(column -> new TableCell<Course, String>() {
                    @Override
                    protected void updateItem(String courseType, boolean empty) {
                        super.updateItem(courseType, empty);
                        if (empty || courseType == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(courseType);
                            switch (courseType) {
                                case "必修":
                                    setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-font-weight: bold;");
                                    break;
                                case "选修":
                                    setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                                    break;
                                case "实践":
                                    setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #ef6c00; -fx-font-weight: bold;");
                                    break;
                                default:
                                    setStyle("-fx-text-fill: #000000;");
                                    break;
                            }
                        }
                    }
                });
            }

            // 状态列自定义渲染
            if (statusCol != null) {
                statusCol.setCellFactory(column -> new TableCell<Course, String>() {
                    @Override
                    protected void updateItem(String status, boolean empty) {
                        super.updateItem(status, empty);
                        if (empty || getTableView() == null || getIndex() >= getTableView().getItems().size()) {
                            setText(null);
                            setStyle("");
                        } else {
                            Course course = getTableView().getItems().get(getIndex());

                            if (selectedCourseIds.contains(course.getCourseId())) {
                                setText("已选择");
                                setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                            } else if (course.getEnrolled() >= course.getCapacity()) {
                                setText("已满员");
                                setStyle("-fx-text-fill: #ff0000; -fx-font-weight: bold;");
                            } else if (hasTimeConflict(course)) {
                                setText("时间冲突");
                                setStyle("-fx-text-fill: #ff6600; -fx-font-weight: bold;");
                            } else {
                                setText("可选");
                                setStyle("-fx-text-fill: #0066cc; -fx-font-weight: bold;");
                            }
                        }
                    }
                });
            }

            // 操作列 - 关键修改：根据选课状态显示不同按钮
            if (actionCol != null) {
                actionCol.setCellFactory(column -> new TableCell<Course, Void>() {
                    private final HBox buttonContainer = new HBox(5);
                    private final Button selectButton = new Button("选择");
                    private final Button dropButton = new Button("退选");

                    {
                        selectButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                                "-fx-border-radius: 5; -fx-background-radius: 5; -fx-font-weight: bold;");
                        dropButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; " +
                                "-fx-border-radius: 5; -fx-background-radius: 5; -fx-font-weight: bold;");

                        selectButton.setPrefWidth(60);
                        dropButton.setPrefWidth(60);

                        selectButton.setOnAction(event -> {
                            if (getTableView() != null && getIndex() < getTableView().getItems().size()) {
                                Course course = getTableView().getItems().get(getIndex());
                                selectCourse(course);
                            }
                        });

                        dropButton.setOnAction(event -> {
                            if (getTableView() != null && getIndex() < getTableView().getItems().size()) {
                                Course course = getTableView().getItems().get(getIndex());
                                dropCourse(course.getCourseId());
                            }
                        });

                        buttonContainer.getChildren().addAll(selectButton, dropButton);
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || getTableView() == null || getIndex() >= getTableView().getItems().size()) {
                            setGraphic(null);
                        } else {
                            Course course = getTableView().getItems().get(getIndex());
                            boolean isSelected = selectedCourseIds.contains(course.getCourseId());

                            if (isSelected) {
                                // 已选课程：只显示退选按钮
                                setGraphic(dropButton);
                            } else {
                                // 未选课程：显示选择按钮（如果可选）
                                boolean canSelect = course.getEnrolled() < course.getCapacity() && !hasTimeConflict(course);
                                selectButton.setDisable(!canSelect);
                                if (!canSelect) {
                                    selectButton.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666666; " +
                                            "-fx-border-radius: 5; -fx-background-radius: 5;");
                                } else {
                                    selectButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                                            "-fx-border-radius: 5; -fx-background-radius: 5; -fx-font-weight: bold;");
                                }
                                setGraphic(selectButton);
                            }
                        }
                    }
                });
            }

            // 设置表格数据
            if (availableCoursesTable != null) {
                availableCoursesTable.setItems(filteredCourses);
            }

            System.out.println("学生表格列初始化完成");
        } catch (Exception e) {
            System.err.println("初始化学生表格列时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeFilters() {
        try {
            // 院系筛选
            if (departmentFilter != null) {
                departmentFilter.getItems().clear();
                departmentFilter.getItems().addAll("全部", "计算机学院", "数学学院", "物理学院",
                        "化学学院", "外语学院", "经管学院", "文学院");
                departmentFilter.setValue("全部");
            }

            // 课程类型筛选
            if (typeFilter != null) {
                typeFilter.getItems().clear();
                typeFilter.getItems().addAll("全部", "必修", "选修", "实践", "通识");
                typeFilter.setValue("全部");
            }

            System.out.println("筛选器初始化完成");
        } catch (Exception e) {
            System.err.println("初始化筛选器时发生错误: " + e.getMessage());
        }
    }

    private void setupEventHandlers() {
        try {
            // 搜索事件处理
            if (searchField != null) {
                searchField.textProperty().addListener((observable, oldValue, newValue) -> filterCourses());
            }

            // 筛选器事件
            if (departmentFilter != null) {
                departmentFilter.setOnAction(e -> filterCourses());
            }
            if (typeFilter != null) {
                typeFilter.setOnAction(e -> filterCourses());
            }

            // 搜索按钮事件
            if (searchButton != null) {
                searchButton.setOnAction(e -> {
                    System.out.println("点击搜索按钮");
                    filterCourses();
                });
            }

            System.out.println("事件处理器设置完成");
        } catch (Exception e) {
            System.err.println("设置学生事件处理器时发生错误: " + e.getMessage());
        }
    }

    private void loadStudentCourseData() {
        if (clientService == null || currentUser == null) {
            System.out.println("无法加载学生数据: clientService=" + (clientService != null) +
                    ", currentUser=" + (currentUser != null));
            return;
        }

        try {
            System.out.println("开始加载学生选课数据...");

            // 先加载已选课程
            loadSelectedCourses();

            // 然后加载可选课程
            Message request = new Message(Message.Type.COURSE_LIST, null);
            Message response = clientService.sendRequest(request);

            if (response != null && response.getCode() == Message.Code.SUCCESS) {
                @SuppressWarnings("unchecked")
                List<Course> courses = (List<Course>) response.getData();
                if (courses != null) {
                    // 为每门课程设置实际选课人数
                    for (Course course : courses) {
                        int actualEnrolled = getActualEnrolledCount(course.getCourseId());
                        course.setEnrolled(actualEnrolled);
                    }

                    allCourses.setAll(courses);
                    System.out.println("成功加载 " + courses.size() + " 门课程");
                } else {
                    System.out.println("课程数据为空");
                }
            } else {
                System.out.println("加载课程失败: " + (response != null ? response.getData() : "无响应"));
            }

            updateCreditsDisplay();

        } catch (Exception e) {
            System.err.println("加载学生课程数据时发生错误: " + e.getMessage());
            e.printStackTrace();
            showAlert("系统错误", "无法加载课程数据: " + e.getMessage());
        }
    }

    // 关键改进：重新加载已选课程状态，确保状态同步
    private void loadSelectedCourses() {
        try {
            System.out.println("重新加载已选课程状态...");
            Message request = new Message(Message.Type.STUDENT_SELECTED_COURSES, currentUser.getUserId());
            Message response = clientService.sendRequest(request);

            selectedCourseIds.clear();

            if (response != null && response.getCode() == Message.Code.SUCCESS) {
                @SuppressWarnings("unchecked")
                List<CourseSelection> selections = (List<CourseSelection>) response.getData();
                if (selections != null) {
                    for (CourseSelection selection : selections) {
                        if (selection.getStatus() != CourseSelection.Status.DROPPED) {
                            selectedCourseIds.add(selection.getCourseId());
                        }
                    }
                    System.out.println("已选课程ID: " + selectedCourseIds);
                }
            } else {
                System.out.println("获取已选课程失败: " + (response != null ? response.getData() : "无响应"));
            }
        } catch (Exception e) {
            System.err.println("加载已选课程时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int getActualEnrolledCount(String courseId) {
        try {
            Message request = new Message(Message.Type.COURSE_SELECTIONS, courseId);
            Message response = clientService.sendRequest(request);

            if (response != null && response.getCode() == Message.Code.SUCCESS) {
                @SuppressWarnings("unchecked")
                List<CourseSelection> selections = (List<CourseSelection>) response.getData();

                if (selections != null) {
                    return (int) selections.stream()
                            .filter(s -> s.getStatus() != CourseSelection.Status.DROPPED)
                            .count();
                }
            } else {
                System.out.println("获取课程选课人数失败: " + (response != null ? response.getData() : "无响应"));
            }
        } catch (Exception e) {
            System.err.println("获取课程选课人数时发生错误: " + e.getMessage());
        }
        return 0;
    }

    private void filterCourses() {
        try {
            String searchText = searchField != null ? searchField.getText().toLowerCase().trim() : "";
            String selectedDepartment = departmentFilter != null ? departmentFilter.getValue() : "全部";
            String selectedType = typeFilter != null ? typeFilter.getValue() : "全部";

            System.out.println("筛选条件 - 搜索: '" + searchText + "', 院系: " + selectedDepartment + ", 类型: " + selectedType);

            Predicate<Course> combinedFilter = course -> {
                boolean textMatch = searchText.isEmpty() ||
                        course.getCourseName().toLowerCase().contains(searchText) ||
                        course.getCourseId().toLowerCase().contains(searchText) ||
                        (course.getTeacherName() != null && course.getTeacherName().toLowerCase().contains(searchText));

                boolean departmentMatch = "全部".equals(selectedDepartment) ||
                        (course.getDepartment() != null && course.getDepartment().equals(selectedDepartment));

                boolean typeMatch = "全部".equals(selectedType) ||
                        (course.getCourseType() != null && course.getCourseType().equals(selectedType));

                return textMatch && departmentMatch && typeMatch;
            };

            filteredCourses.setPredicate(combinedFilter);

            System.out.println("筛选结果: " + filteredCourses.size() + " 门课程");
        } catch (Exception e) {
            System.err.println("筛选课程时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        System.out.println("执行搜索...");
        filterCourses();
    }

    private void selectCourse(Course course) {
        if (course == null) {
            System.out.println("选课失败: 课程为空");
            return;
        }

        System.out.println("尝试选择课程: " + course.getCourseName());

        // 检查是否已选择（双重保护）
        if (selectedCourseIds.contains(course.getCourseId())) {
            showAlert("选课失败", "您已经选择了这门课程");
            return;
        }

        if (course.getEnrolled() >= course.getCapacity()) {
            showAlert("选课失败", "该课程已满员");
            return;
        }

        if (hasTimeConflict(course)) {
            showAlert("选课失败", "该课程与您已选课程时间冲突");
            return;
        }

        try {
            // 发送选课请求
            CourseSelection selection = new CourseSelection(currentUser.getUserId(), course.getCourseId());
            Message request = new Message(Message.Type.SELECT_COURSE, selection);
            Message response = clientService.sendRequest(request);

            if (response != null && response.getCode() == Message.Code.SUCCESS) {
                System.out.println("选课请求成功，开始同步状态");

                // 关键改进：重新加载状态而非直接修改
                refreshCourseData(course);
                showAlert("选课成功", "成功选择课程：" + course.getCourseName());
                System.out.println("选课成功: " + course.getCourseName());
            } else {
                String errorMsg = response != null ? response.getData().toString() : "未知错误";

                // 处理重复选课错误
                if (errorMsg.contains("Duplicate entry") || errorMsg.contains("已选择") || errorMsg.contains("已经选择")) {
                    // 同步本地状态
                    refreshCourseData(course);
                    showAlert("提示", "该课程已在您的选课列表中");
                } else {
                    showAlert("选课失败", errorMsg);
                }
                System.out.println("选课响应: " + errorMsg);
            }
        } catch (Exception e) {
            System.err.println("选课时发生错误: " + e.getMessage());
            e.printStackTrace();
            showAlert("系统错误", "选课失败: " + e.getMessage());
        }
    }

    private void dropCourse(String courseId) {
        if (courseId == null || courseId.trim().isEmpty()) {
            System.out.println("退选失败: 课程ID为空");
            return;
        }

        try {
            Course course = getCourseById(courseId);
            String courseName = course != null ? course.getCourseName() : "未知课程";

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("确认退选");
            confirmAlert.setHeaderText("您确定要退选这门课程吗？");
            confirmAlert.setContentText("课程: " + courseName + "\n退选后可以重新选择");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        System.out.println("尝试退选课程: " + courseName);

                        // 创建选课记录用于退选
                        CourseSelection selection = new CourseSelection(currentUser.getUserId(), courseId);
                        Message request = new Message(Message.Type.DROP_COURSE, selection);
                        Message serverResponse = clientService.sendRequest(request);

                        if (serverResponse != null && serverResponse.getCode() == Message.Code.SUCCESS) {
                            System.out.println("退选请求成功，开始同步状态");

                            // 关键改进：重新加载状态而非直接修改
                            refreshCourseData(course);
                            showAlert("退选成功", "成功退选课程：" + courseName);
                            System.out.println("退选成功: " + courseName);
                        } else {
                            String errorMsg = serverResponse != null ? serverResponse.getData().toString() : "未知错误";
                            showAlert("退选失败", errorMsg);
                            System.out.println("退选失败: " + errorMsg);
                        }
                    } catch (Exception e) {
                        System.err.println("退选时发生错误: " + e.getMessage());
                        e.printStackTrace();
                        showAlert("系统错误", "退选失败: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("退选课程时发生错误: " + e.getMessage());
            showAlert("系统错误", "退选失败: " + e.getMessage());
        }
    }

    // 关键改进：刷新课程数据和状态
    private void refreshCourseData(Course targetCourse) {
        try {
            System.out.println("刷新课程数据和状态...");

            // 重新加载已选课程状态
            loadSelectedCourses();

            // 重新计算选课人数
            if (targetCourse != null) {
                int newEnrolled = getActualEnrolledCount(targetCourse.getCourseId());
                targetCourse.setEnrolled(newEnrolled);
                System.out.println("课程 " + targetCourse.getCourseId() + " 更新选课人数为: " + newEnrolled);
            }

            // 刷新表格显示
            if (availableCoursesTable != null) {
                availableCoursesTable.refresh();
            }

            // 更新学分显示
            updateCreditsDisplay();

            System.out.println("课程数据刷新完成");
        } catch (Exception e) {
            System.err.println("刷新课程数据时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean hasTimeConflict(Course newCourse) {
        if (newCourse == null || selectedCourseIds.isEmpty()) return false;

        for (String selectedCourseId : selectedCourseIds) {
            Course selectedCourse = getCourseById(selectedCourseId);
            if (selectedCourse != null && hasScheduleConflict(newCourse, selectedCourse)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasScheduleConflict(Course course1, Course course2) {
        if (course1 == null || course2 == null) return false;
        if (course1.getSchedule() == null || course2.getSchedule() == null) return false;

        return course1.getSchedule().equals(course2.getSchedule());
    }

    private Course getCourseById(String courseId) {
        if (courseId == null || allCourses == null) return null;

        return allCourses.stream()
                .filter(course -> courseId.equals(course.getCourseId()))
                .findFirst()
                .orElse(null);
    }

    private void updateCreditsDisplay() {
        try {
            int selectedCredits = 0;
            int selectedCount = 0;
            int weeklyHours = 0;

            for (String courseId : selectedCourseIds) {
                Course course = getCourseById(courseId);
                if (course != null) {
                    selectedCredits += course.getCredits();
                    selectedCount++;

                    // 简化的课时计算
                    if (course.getSchedule() != null && !course.getSchedule().isEmpty()) {
                        weeklyHours += 2; // 假设每门课程每周2小时
                    }
                }
            }

            // 更新显示
            if (selectedCreditsLabel != null) {
                selectedCreditsLabel.setText(String.valueOf(selectedCredits));
            }
            if (totalCreditsLabel != null) {
                totalCreditsLabel.setText("/ 25");
            }
            if (selectedCoursesCountLabel != null) {
                selectedCoursesCountLabel.setText(String.valueOf(selectedCount));
            }
            if (weeklyHoursDisplayLabel != null) {
                weeklyHoursDisplayLabel.setText(String.valueOf(weeklyHours));
            }
            if (creditsProgressBar != null) {
                double progress = Math.min(1.0, selectedCredits / 25.0);
                creditsProgressBar.setProgress(progress);
            }

            System.out.println("学分显示更新: " + selectedCredits + "/25, 课程数: " + selectedCount);
        } catch (Exception e) {
            System.err.println("更新学分显示时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        System.out.println("学生手动刷新数据...");
        try {
            loadStudentCourseData();
            showAlert("刷新成功", "数据已更新");
        } catch (Exception e) {
            System.err.println("刷新学生数据时发生错误: " + e.getMessage());
            showAlert("刷新失败", "刷新数据时发生错误: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("显示学生提示框时发生错误: " + e.getMessage());
        }
    }
}