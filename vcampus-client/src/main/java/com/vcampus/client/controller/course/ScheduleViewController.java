// ============= 课表查看控制器 - 修复格子大小版本 =============
package com.vcampus.client.controller.course;

import com.vcampus.client.entity.TimeSlot;
import com.vcampus.client.service.ClientService;
import com.vcampus.common.entity.Course;
import com.vcampus.common.entity.CourseSelection;
import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.DayOfWeek;

public class ScheduleViewController implements Initializable {

    @FXML private ComboBox<String> semesterComboBox;
    @FXML private ComboBox<String> viewModeComboBox;
    @FXML private Label weekInfoLabel;
    @FXML private GridPane scheduleMainGrid;
    @FXML private Label weeklyHoursLabel;
    @FXML private Label totalCreditsLabel;
    @FXML private Label coursesCountLabel;

    private ClientService clientService;
    private User currentUser;
    private ObservableList<Course> scheduleCourses = FXCollections.observableArrayList();

    // 时间配置常量
    private static final String[] TIME_SLOTS = {
            "08:00-08:45", "08:50-09:35", "10:00-10:45", "10:50-11:35",
            "14:00-14:45", "14:50-15:35", "16:00-16:45", "16:50-17:35",
            "19:00-19:45", "19:50-20:35"
    };

    private static final String[] WEEK_DAYS = {"周一", "周二", "周三", "周四", "周五"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("ScheduleViewController 初始化开始");

        try {
            initializeComponents();
            createScheduleGrid();
            System.out.println("ScheduleViewController 初始化完成");
        } catch (Exception e) {
            System.err.println("初始化ScheduleViewController时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setClientService(ClientService clientService) {
        this.clientService = clientService;
        System.out.println("ScheduleViewController 设置ClientService: " + (clientService != null ? "成功" : "失败"));
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        System.out.println("ScheduleViewController 设置当前用户: " + (currentUser != null ? currentUser.getDisplayName() : "null"));

        if (currentUser != null && clientService != null) {
            loadScheduleData();
        }
    }

    private void initializeComponents() {
        try {
            // 学期选择初始化
            if (semesterComboBox != null) {
                semesterComboBox.getItems().clear();
                semesterComboBox.getItems().addAll("2024-1", "2024-2", "2025-1", "2025-2");
                semesterComboBox.setValue("2025-1");
                semesterComboBox.setOnAction(e -> {
                    System.out.println("学期选择变更为: " + semesterComboBox.getValue());
                    loadScheduleData();
                });
            }

            // 查看模式初始化
            if (viewModeComboBox != null) {
                viewModeComboBox.getItems().clear();
                viewModeComboBox.getItems().addAll("周视图", "月视图", "学期视图");
                viewModeComboBox.setValue("周视图");
                viewModeComboBox.setOnAction(e -> {
                    System.out.println("视图模式变更为: " + viewModeComboBox.getValue());
                    updateViewMode();
                });
            }

            // 更新周信息
            updateWeekInfo();

            System.out.println("组件初始化完成");

        } catch (Exception e) {
            System.err.println("初始化组件时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createScheduleGrid() {
        if (scheduleMainGrid == null) {
            System.out.println("scheduleMainGrid为null，跳过网格创建");
            return;
        }

        try {
            // 清空现有内容
            scheduleMainGrid.getChildren().clear();
            scheduleMainGrid.setHgap(1);
            scheduleMainGrid.setVgap(1);

            // 创建左上角空白单元格
            Label cornerCell = new Label("");
            cornerCell.setPrefSize(100, 50); // 标准尺寸
            cornerCell.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; " +
                    "-fx-border-width: 1; -fx-alignment: center;");
            scheduleMainGrid.add(cornerCell, 0, 0);

            // 添加星期表头
            for (int j = 0; j < WEEK_DAYS.length; j++) {
                Label dayLabel = new Label(WEEK_DAYS[j]);
                dayLabel.setPrefSize(150, 50); // 标准尺寸
                dayLabel.setStyle("-fx-background-color: linear-gradient(135deg, #667eea, #764ba2); " +
                        "-fx-text-fill: black; -fx-border-color: #dee2e6; -fx-border-width: 1; " +
                        "-fx-alignment: center; -fx-font-weight: bold; -fx-font-size: 13px;");
                scheduleMainGrid.add(dayLabel, j + 1, 0);
            }

            // 添加时间段标签
            for (int i = 0; i < TIME_SLOTS.length; i++) {
                Label timeLabel = new Label(TIME_SLOTS[i]);
                timeLabel.setPrefSize(100, 60); // 标准尺寸
                timeLabel.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #495057; " +
                        "-fx-border-color: #dee2e6; -fx-border-width: 1; " +
                        "-fx-alignment: center; -fx-font-weight: 600; -fx-font-size: 11px;");
                scheduleMainGrid.add(timeLabel, 0, i + 1);
            }

            // 创建空白课程格子 - 关键修复：确保尺寸一致
            for (int day = 0; day < WEEK_DAYS.length; day++) {
                for (int time = 0; time < TIME_SLOTS.length; time++) {
                    Label emptyCell = new Label("");
                    emptyCell.setPrefSize(150, 60); // 与课程格子相同的标准尺寸
                    emptyCell.setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; " +
                            "-fx-border-width: 1; -fx-alignment: center;");
                    scheduleMainGrid.add(emptyCell, day + 1, time + 1);
                }
            }

            System.out.println("课程表网格创建完成: " + WEEK_DAYS.length + "x" + TIME_SLOTS.length);

        } catch (Exception e) {
            System.err.println("创建课程表网格时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadScheduleData() {
        if (clientService == null || currentUser == null) {
            System.out.println("无法加载数据: clientService=" + (clientService != null) +
                    ", currentUser=" + (currentUser != null));
            return;
        }

        try {
            System.out.println("开始加载课程表数据...");

            String selectedSemester = semesterComboBox != null ? semesterComboBox.getValue() : "2025-1";
            System.out.println("当前学期: " + selectedSemester);

            // 获取学生已选课程
            Message request = new Message(Message.Type.STUDENT_SELECTED_COURSES, currentUser.getUserId());
            Message response = clientService.sendRequest(request);

            if (response != null && response.getCode() == Message.Code.SUCCESS) {
                System.out.println("成功获取已选课程响应");

                @SuppressWarnings("unchecked")
                List<CourseSelection> selections = (List<CourseSelection>) response.getData();
                if (selections != null && !selections.isEmpty()) {
                    System.out.println("找到 " + selections.size() + " 个选课记录");

                    // 获取详细课程信息
                    List<Course> courses = getCourseDetails(selections, selectedSemester);

                    if (!courses.isEmpty()) {
                        scheduleCourses.setAll(courses);
                        updateScheduleGrid(courses);
                        updateStatistics(courses);
                        System.out.println("成功加载 " + courses.size() + " 门课程到课程表");
                    } else {
                        System.out.println("未找到匹配学期的课程");
                        clearScheduleDisplay();
                    }
                } else {
                    System.out.println("学生暂无选课记录");
                    clearScheduleDisplay();
                }
            } else {
                System.out.println("获取已选课程失败: " +
                        (response != null ? response.getCode() + " - " + response.getData() : "无响应"));
                clearScheduleDisplay();
                showAlert("数据加载失败", "无法获取课程数据，请检查网络连接或联系管理员");
            }

        } catch (Exception e) {
            System.err.println("加载课程数据时发生错误: " + e.getMessage());
            e.printStackTrace();
            clearScheduleDisplay();
            showAlert("系统错误", "加载数据时发生错误: " + e.getMessage());
        }
    }

    private List<Course> getCourseDetails(List<CourseSelection> selections, String semester) {
        List<Course> courses = new ArrayList<>();

        try {
            // 获取所有课程信息
            Message courseRequest = new Message(Message.Type.COURSE_LIST, null);
            Message courseResponse = clientService.sendRequest(courseRequest);

            if (courseResponse != null && courseResponse.getCode() == Message.Code.SUCCESS) {
                @SuppressWarnings("unchecked")
                List<Course> allCourses = (List<Course>) courseResponse.getData();
                System.out.println("获取到 " + (allCourses != null ? allCourses.size() : 0) + " 门总课程");

                if (allCourses != null) {
                    // 筛选已选择的课程并匹配学期
                    for (CourseSelection selection : selections) {
                        // 跳过已退选的课程
                        if (selection.getStatus() == CourseSelection.Status.DROPPED) {
                            continue;
                        }

                        for (Course course : allCourses) {
                            if (course.getCourseId().equals(selection.getCourseId()) &&
                                    course.getSemester().equals(semester)) {

                                // 设置状态信息
                                course.setStatus(getStatusDisplayName(selection.getStatus()));
                                courses.add(course);
                                System.out.println("添加课程: " + course.getCourseName() +
                                        " (" + course.getCourseId() + ")");
                                break;
                            }
                        }
                    }
                }
            } else {
                System.out.println("获取课程列表失败");
            }

        } catch (Exception e) {
            System.err.println("获取课程详情时发生错误: " + e.getMessage());
            e.printStackTrace();
        }

        return courses;
    }

    private String getStatusDisplayName(CourseSelection.Status status) {
        if (status == null) return "未知";

        switch (status) {
            case SELECTED:
                return "已选择";
            case COMPLETED:
                return "已完成";
            case DROPPED:
                return "已退选";
            default:
                return "未知";
        }
    }

    private void updateScheduleGrid(List<Course> courses) {
        if (scheduleMainGrid == null || courses == null) {
            return;
        }

        try {
            // 重新创建网格以清除旧的课程信息
            createScheduleGrid();

            // 添加每门课程到课程表
            for (Course course : courses) {
                addCourseToGrid(course);
            }

            System.out.println("课程表网格更新完成，显示 " + courses.size() + " 门课程");

        } catch (Exception e) {
            System.err.println("更新课程表网格时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addCourseToGrid(Course course) {
        if (course == null || scheduleMainGrid == null) {
            return;
        }

        try {
            String schedule = course.getSchedule();
            if (schedule == null || schedule.trim().isEmpty()) {
                System.out.println("课程 " + course.getCourseName() + " 没有时间安排");
                return;
            }

            System.out.println("处理课程: " + course.getCourseName() + ", 时间: " + schedule);

            // 解析时间
            List<TimeSlot> timeSlots = parseSchedule(schedule);

            for (TimeSlot timeSlot : timeSlots) {
                int dayIndex = timeSlot.getDayOfWeek().getValue(); // 1-7, 周一是1
                int startPeriod = timeSlot.getStartPeriod(); // 1-10
                int endPeriod = timeSlot.getEndPeriod();

                // 确保在有效范围内
                if (dayIndex >= 1 && dayIndex <= 5 && startPeriod >= 1 && startPeriod <= 10) {

                    // 关键修复：创建标准大小的课程格子
                    Label courseCell = createStandardCourseCell(course);

                    // 添加到网格（dayIndex对应列，startPeriod对应行）
                    scheduleMainGrid.add(courseCell, dayIndex, startPeriod);

                    System.out.println("成功添加课程 " + course.getCourseName() +
                            " 到位置 [" + dayIndex + ", " + startPeriod + "]");
                }
            }

        } catch (Exception e) {
            System.err.println("添加课程到网格时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 关键修复：创建标准大小的课程格子
    private Label createStandardCourseCell(Course course) {
        Label courseCell = new Label();

        // 构建显示文本 - 优化内容显示
        StringBuilder text = new StringBuilder();
        text.append(course.getCourseName()).append("\n");
        text.append(course.getTeacherName());
        if (course.getClassroom() != null && !course.getClassroom().isEmpty()) {
            text.append("\n").append(course.getClassroom());
        }

        courseCell.setText(text.toString());
        courseCell.setPrefSize(150, 60); // 与空白格子相同的标准尺寸
        courseCell.setMaxSize(150, 60);  // 限制最大尺寸
        courseCell.setMinSize(150, 60);  // 限制最小尺寸
        courseCell.setWrapText(true);

        // 根据课程类型设置样式
        String style = getCourseStyle(course.getCourseType());
        courseCell.setStyle(style + " -fx-border-color: #495057; -fx-border-width: 1.5; " +
                "-fx-padding: 3; -fx-alignment: center; -fx-font-size: 10px; " +
                "-fx-background-radius: 5; -fx-border-radius: 5;");

        return courseCell;
    }

    private String getCourseStyle(String courseType) {
        if (courseType == null) {
            return "-fx-background-color: linear-gradient(135deg, #f8f9fa, #e9ecef); -fx-text-fill: #495057;";
        }

        switch (courseType) {
            case "必修":
                return "-fx-background-color: linear-gradient(135deg, #e3f2fd, #bbdefb); -fx-text-fill: #1565c0;";
            case "选修":
                return "-fx-background-color: linear-gradient(135deg, #e8f5e8, #c8e6c9); -fx-text-fill: #2e7d32;";
            case "实践":
                return "-fx-background-color: linear-gradient(135deg, #fff3e0, #ffcc02); -fx-text-fill: #ef6c00;";
            case "通识":
                return "-fx-background-color: linear-gradient(135deg, #f3e5f5, #e1bee7); -fx-text-fill: #7b1fa2;";
            default:
                return "-fx-background-color: linear-gradient(135deg, #f8f9fa, #e9ecef); -fx-text-fill: #495057;";
        }
    }

    private List<TimeSlot> parseSchedule(String schedule) {
        List<TimeSlot> timeSlots = new ArrayList<>();

        if (schedule == null || schedule.trim().isEmpty()) {
            return timeSlots;
        }

        return parseScheduleSimple(schedule);
    }

    private List<TimeSlot> parseScheduleSimple(String schedule) {
        List<TimeSlot> timeSlots = new ArrayList<>();

        try {
            // 解析类似 "周一3-4节,周三5-6节" 的格式
            String[] segments = schedule.split("[,，]");

            for (String segment : segments) {
                segment = segment.trim();

                DayOfWeek dayOfWeek = null;
                int startPeriod = 1;
                int endPeriod = 1;

                // 解析星期
                if (segment.contains("周一") || segment.contains("星期一")) {
                    dayOfWeek = DayOfWeek.MONDAY;
                } else if (segment.contains("周二") || segment.contains("星期二")) {
                    dayOfWeek = DayOfWeek.TUESDAY;
                } else if (segment.contains("周三") || segment.contains("星期三")) {
                    dayOfWeek = DayOfWeek.WEDNESDAY;
                } else if (segment.contains("周四") || segment.contains("星期四")) {
                    dayOfWeek = DayOfWeek.THURSDAY;
                } else if (segment.contains("周五") || segment.contains("星期五")) {
                    dayOfWeek = DayOfWeek.FRIDAY;
                }

                // 解析节数
                if (segment.contains("1-2节") || segment.contains("1~2节")) {
                    startPeriod = 1; endPeriod = 2;
                } else if (segment.contains("3-4节") || segment.contains("3~4节")) {
                    startPeriod = 3; endPeriod = 4;
                } else if (segment.contains("5-6节") || segment.contains("5~6节")) {
                    startPeriod = 5; endPeriod = 6;
                } else if (segment.contains("7-8节") || segment.contains("7~8节")) {
                    startPeriod = 7; endPeriod = 8;
                } else if (segment.contains("9-10节") || segment.contains("9~10节")) {
                    startPeriod = 9; endPeriod = 10;
                }

                if (dayOfWeek != null) {
                    TimeSlot timeSlot = new TimeSlot(dayOfWeek, startPeriod, endPeriod);
                    timeSlots.add(timeSlot);
                    System.out.println("解析时间段: " + dayOfWeek + " " + startPeriod + "-" + endPeriod + "节");
                }
            }

        } catch (Exception e) {
            System.err.println("解析课程时间时发生错误: " + e.getMessage());
        }

        return timeSlots;
    }

    private void updateStatistics(List<Course> courses) {
        if (courses == null) {
            courses = new ArrayList<>();
        }

        try {
            // 计算统计数据
            int totalCredits = courses.stream().mapToInt(Course::getCredits).sum();
            int totalCourses = courses.size();

            // 计算周课时（基于实际课程安排）
            int weeklyHours = 0;
            for (Course course : courses) {
                List<TimeSlot> timeSlots = parseSchedule(course.getSchedule());
                for (TimeSlot slot : timeSlots) {
                    weeklyHours += (slot.getEndPeriod() - slot.getStartPeriod() + 1);
                }
            }

            // 更新显示
            if (totalCreditsLabel != null) {
                totalCreditsLabel.setText(String.valueOf(totalCredits));
            }
            if (coursesCountLabel != null) {
                coursesCountLabel.setText(String.valueOf(totalCourses));
            }
            if (weeklyHoursLabel != null) {
                weeklyHoursLabel.setText(String.valueOf(weeklyHours));
            }

            System.out.println("统计信息更新 - 学分:" + totalCredits +
                    ", 课程:" + totalCourses + ", 课时:" + weeklyHours);

        } catch (Exception e) {
            System.err.println("更新统计信息时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearScheduleDisplay() {
        try {
            scheduleCourses.clear();
            createScheduleGrid();
            updateStatistics(new ArrayList<>());
            System.out.println("课程表显示已清空");
        } catch (Exception e) {
            System.err.println("清空课程表显示时发生错误: " + e.getMessage());
        }
    }

    private void updateWeekInfo() {
        try {
            LocalDate now = LocalDate.now();
            int weekOfYear = now.getDayOfYear() / 7 + 1;
            String weekInfo = String.format("%d年第%d周", now.getYear(), weekOfYear);

            if (weekInfoLabel != null) {
                weekInfoLabel.setText(weekInfo);
            }

        } catch (Exception e) {
            System.err.println("更新周信息时发生错误: " + e.getMessage());
        }
    }

    private void updateViewMode() {
        if (viewModeComboBox == null) return;

        try {
            String mode = viewModeComboBox.getValue();
            System.out.println("切换视图模式: " + mode);

            switch (mode) {
                case "周视图":
                    createScheduleGrid();
                    if (!scheduleCourses.isEmpty()) {
                        updateScheduleGrid(scheduleCourses);
                    }
                    break;
                case "月视图":
                    showAlert("功能提示", "月视图功能开发中，敬请期待...");
                    break;
                case "学期视图":
                    showAlert("功能提示", "学期视图功能开发中，敬请期待...");
                    break;
            }
        } catch (Exception e) {
            System.err.println("更新视图模式时发生错误: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        System.out.println("用户请求刷新课程表数据");
        try {
            loadScheduleData();
            updateWeekInfo();
            showAlert("刷新成功", "课程表数据已更新");
        } catch (Exception e) {
            System.err.println("刷新数据时发生错误: " + e.getMessage());
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
            System.err.println("显示提示框时发生错误: " + e.getMessage());
        }
    }
}