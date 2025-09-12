package com.vcampus.client.onlineclass;

import com.sun.javafx.charts.Legend;
import com.vcampus.common.entity.User;
import com.vcampus.common.entity.Course;
import com.vcampus.common.entity.Assignment;
import com.vcampus.common.entity.CourseMaterial;
import com.vcampus.common.entity.CoursePlayback;

import com.vcampus.server.service.OnlineClassService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 在线课堂模块 - 学生界面
 */
public class StudentOnlineClass extends BorderPane {

    private String currentView = "home";
    private Course currentCourse; // 当前选中的课程
    private User currentUser;
    private OnlineClassService onlineClassService;
    private Label detailLabel;
    private ListView<String> scheduleList;
    private LocalDate currentCalendarDate = LocalDate.now();
    private List<Integer> daysWithReminders = new ArrayList<>();

    private ObservableList<Course> courseList = FXCollections.observableArrayList();
    private ObservableList<Assignment> assignmentList = FXCollections.observableArrayList( );
    private ObservableList<CoursePlayback> playbackList = FXCollections.observableArrayList();
    private ObservableList<CourseMaterial> materialList = FXCollections.observableArrayList();
    private ObservableList<String> discussionList = FXCollections.observableArrayList();

    public StudentOnlineClass(User user) {
        this.currentUser = user;
        this.onlineClassService = new OnlineClassService(user);

        // 初始化数据
        loadCoursesFromDatabase();
        loadAssignmentsFromDatabase();

        initRootLayout();
        showHomePage();
    }

    /**
     * 从数据库加载课程数据
     */
    private void loadCoursesFromDatabase() {
        try {
            List<Course> courses = onlineClassService.getStudentCourses();
            courseList.setAll(courses);
        } catch (SQLException e) {
            e.printStackTrace();
            // 显示错误消息
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("加载失败");
            alert.setHeaderText(null);
            alert.setContentText("加载课程数据失败: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * 从数据库加载作业数据
     */
    private void loadAssignmentsFromDatabase() {
        try {
            List<Assignment> assignments = onlineClassService.getStudentAssignments();
            assignmentList.setAll(assignments);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("加载作业数据失败: " + e.getMessage());
        }
    }

    /**
     * 初始化根布局
     */
    private void initRootLayout() {
        this.setTop(createHeader());
    }

    /**
     * 创建顶部标题栏
     */
    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #2c3e50;");
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("在线课堂");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backButton = new Button("返回主菜单");
        backButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        backButton.setOnAction(e -> showHomePage());

        header.getChildren().addAll(title, spacer, backButton);
        return header;
    }

    /**
     * 显示首页
     */
    private void showHomePage() {
        currentView = "home";

        VBox homePage = new VBox(20);
        homePage.setPadding(new Insets(20));
        homePage.setStyle("-fx-background-color: #ecf0f1;");

        // 欢迎区域
        HBox welcomeBox = new HBox(10);
        welcomeBox.setAlignment(Pos.CENTER_LEFT);
        Label welcomeLabel = new Label("欢迎使用在线课堂！");
        welcomeLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 20));
        welcomeBox.getChildren().add(welcomeLabel);

        // 快速入口卡片
        HBox quickAccess = new HBox(15);
        quickAccess.setAlignment(Pos.CENTER);

        VBox scheduleCard = createQuickAccessCard("日程提醒", "查看课程安排", "📅", e -> showSchedulePage());
        VBox coursesCard = createQuickAccessCard("我的课程", "管理学习课程", "📚", e -> showCoursesPage());
        VBox assignmentsCard = createQuickAccessCard("我的作业", "完成学习任务", "📝", e -> showAssignmentsPage());

        quickAccess.getChildren().addAll(scheduleCard, coursesCard, assignmentsCard);

        // 最近活动
        VBox recentActivity = new VBox(10);
        Label activityLabel = new Label("最近活动");
        activityLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        ListView<String> activityList = new ListView<>();
        try {
            List<String> activities = onlineClassService.getRecentActivities();
            activityList.getItems().setAll(activities);
        } catch (SQLException e) {
            e.printStackTrace();
            activityList.getItems().add("加载活动失败: " + e.getMessage());
        }
        activityList.setPrefHeight(120);

        recentActivity.getChildren().addAll(activityLabel, activityList);

        homePage.getChildren().addAll(welcomeBox, quickAccess, recentActivity);
        this.setCenter(homePage);
        this.setLeft(null); // 移除左侧导航
    }

    private VBox createQuickAccessCard(String title, String description, String emoji, EventHandler<ActionEvent> action) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefSize(200, 120);
        card.setAlignment(Pos.CENTER);

        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font(24));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font(12));
        descLabel.setTextFill(Color.GRAY);

        Button enterBtn = new Button("进入");
        enterBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        enterBtn.setOnAction(action);

        card.getChildren().addAll(emojiLabel, titleLabel, descLabel, enterBtn);
        return card;
    }

    /**
     * 显示日程提醒页面
     */
    private void showSchedulePage() {
        currentView = "schedule";

        // 获取当前月份的提醒日期
        try {
            daysWithReminders = onlineClassService.getMonthReminders(
                    currentCalendarDate.getYear(),
                    currentCalendarDate.getMonthValue()
            );
        } catch (SQLException e) {
            e.printStackTrace();
            daysWithReminders = new ArrayList<>();
        }

        VBox schedulePage = new VBox(20);
        schedulePage.setPadding(new Insets(20));
        schedulePage.setStyle("-fx-background-color: #ecf0f1;");

        // 日历和日程区域
        HBox mainContent = new HBox(20);
        mainContent.setAlignment(Pos.TOP_CENTER);

        // 日历部分
        VBox calendarSection = new VBox(10);
        calendarSection.setPrefWidth(400);
        calendarSection.setAlignment(Pos.TOP_CENTER);

        // 月份标签和导航
        Label monthLabel = new Label(currentCalendarDate.getYear() + "年 " + currentCalendarDate.getMonthValue() + "月");
        monthLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));

        HBox monthNav = new HBox(10);
        monthNav.setAlignment(Pos.CENTER);
        Button prevMonth = new Button("上月");
        Button todayBtn = new Button("今天");
        Button nextMonth = new Button("下月");

        // 月份导航按钮事件
        prevMonth.setOnAction(e -> {
            currentCalendarDate = currentCalendarDate.minusMonths(1);
            showSchedulePage(); // 重新加载页面
        });

        nextMonth.setOnAction(e -> {
            currentCalendarDate = currentCalendarDate.plusMonths(1);
            showSchedulePage(); // 重新加载页面
        });

        todayBtn.setOnAction(e -> {
            currentCalendarDate = LocalDate.now();
            showSchedulePage(); // 重新加载页面
        });

        monthNav.getChildren().addAll(prevMonth, todayBtn, nextMonth);

        // 日历网格
        GridPane calendarGrid = createCalendarGrid(currentCalendarDate);
        calendarSection.getChildren().addAll(monthLabel, monthNav, calendarGrid);

        // 日程详情部分
        VBox scheduleDetail = createScheduleDetail();
        mainContent.getChildren().addAll(calendarSection, scheduleDetail);
        schedulePage.getChildren().add(mainContent);


        this.setCenter(schedulePage);
        this.setLeft(null); // 移除左侧导航
    }

    /**
     * 创建日历网格
     */
    private GridPane createCalendarGrid(LocalDate date) {
        GridPane calendarGrid = new GridPane();
        calendarGrid.setHgap(5);
        calendarGrid.setVgap(5);
        calendarGrid.setAlignment(Pos.CENTER);

        // 添加星期标题
        String[] days = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setPrefSize(40, 30);
            dayLabel.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6;");
            calendarGrid.add(dayLabel, i, 0);
        }

        // 获取月份的第一天和这个月的天数
        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        int daysInMonth = date.lengthOfMonth();

        // 计算第一天是星期几 (0=周日, 1=周一, ..., 6=周六)
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7; // Java的DayOfWeek是1=周一,7=周日

        // 填充日历
        int day = 1;
        for (int week = 1; week <= 6; week++) {
            for (int d = 0; d < 7; d++) {
                if ((week == 1 && d < dayOfWeek) || day > daysInMonth) {
                    // 添加空单元格
                    StackPane emptyCell = new StackPane();
                    emptyCell.setPrefSize(40, 40);
                    calendarGrid.add(emptyCell, d, week);
                } else {
                    // 添加日期单元格
                    StackPane dayCell = createDayCell(day);
                    calendarGrid.add(dayCell, d, week);
                    day++;
                }
            }
        }

        return calendarGrid;
    }

    /**
     * 创建日期单元格
     */
    private StackPane createDayCell(int day) {
        StackPane dayCell = new StackPane();
        dayCell.setPrefSize(40, 40);
        dayCell.setStyle("-fx-border-color: #bdc3c7; -fx-border-radius: 3;");

        // 检查这一天是否有提醒
        boolean hasReminder = daysWithReminders.contains(day);

        // 如果是今天，高亮显示
        LocalDate today = LocalDate.now();
        boolean isToday = currentCalendarDate.getYear() == today.getYear() &&
                currentCalendarDate.getMonthValue() == today.getMonthValue() &&
                day == today.getDayOfMonth();

        if (isToday) {
            dayCell.setStyle("-fx-background-color: #3498db; -fx-border-radius: 3;");
        }

        VBox content = new VBox(2);
        content.setAlignment(Pos.TOP_CENTER);

        Label dateLabel = new Label(String.valueOf(day));
        dateLabel.setAlignment(Pos.CENTER);

        if (isToday) {
            dateLabel.setTextFill(Color.WHITE);
        }

        // 如果有提醒，添加提醒标记
        if (hasReminder) {
            Circle reminderDot = new Circle(3);
            reminderDot.setFill(Color.RED);
            content.getChildren().addAll(dateLabel, reminderDot);
        } else {
            content.getChildren().add(dateLabel);
        }

        dayCell.getChildren().add(content);

        // 添加点击事件
        dayCell.setOnMouseClicked(e -> {
            // 更新日程详情为选中日期的内容
            updateScheduleDetail(day);
        });

        return dayCell;
    }

    /**
     * 更新日程详情
     */
    private void updateScheduleDetail(int day) {
        // 更新详情标签
        detailLabel.setText(currentCalendarDate.getYear() + "年" +
                currentCalendarDate.getMonthValue() + "月" + day + "日的日程");

        // 获取选中日期的提醒
        try {
            List<OnlineClassService.Reminder> reminders = onlineClassService.getDayReminders(
                    currentCalendarDate.getYear(),
                    currentCalendarDate.getMonthValue(),
                    day
            );
            List<String> reminderContents = new ArrayList<>();
            for (OnlineClassService.Reminder reminder : reminders) {
                reminderContents.add(reminder.getContent());
            }
            scheduleList.getItems().setAll(reminderContents);
        } catch (SQLException e) {
            e.printStackTrace();
            scheduleList.getItems().add("加载提醒失败: " + e.getMessage());
        }
    }

    /**
     * 创建日程详情部分
     */
    private VBox createScheduleDetail() {
        VBox scheduleDetail = new VBox(10);
        scheduleDetail.setPrefWidth(400);

        // 使用实例变量存储这些组件，以便在其他方法中更新
        detailLabel = new Label(currentCalendarDate.getYear() + "年" +
                currentCalendarDate.getMonthValue() + "月" +
                currentCalendarDate.getDayOfMonth() + "日的日程");
        detailLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        scheduleList = new ListView<>();
        try {
            List<OnlineClassService.Reminder> reminders = onlineClassService.getDayReminders(
                    currentCalendarDate.getYear(),
                    currentCalendarDate.getMonthValue(),
                    currentCalendarDate.getDayOfMonth()
            );
            List<String> reminderContents = new ArrayList<>();
            for (OnlineClassService.Reminder reminder : reminders) {
                reminderContents.add(reminder.getContent());
            }
            scheduleList.getItems().setAll(reminderContents);
        } catch (SQLException e) {
            e.printStackTrace();
            scheduleList.getItems().add("加载提醒失败: " + e.getMessage());
        }
        scheduleList.setPrefHeight(150);

        // 添加提醒表单
        VBox addReminderForm = new VBox(10);
        Label addReminderLabel = new Label("添加新提醒");
        TextField reminderContentField = new TextField();
        reminderContentField.setPromptText("提醒内容");
        DatePicker reminderDatePicker = new DatePicker();
        reminderDatePicker.setValue(currentCalendarDate); // 默认选择当前日历日期
        reminderDatePicker.setPromptText("选择日期");

        Button addReminderBtn = new Button("添加提醒");
        addReminderBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        addReminderBtn.setOnAction(e -> {
            String content = reminderContentField.getText();
            LocalDate date = reminderDatePicker.getValue();

            if (content.isEmpty() || date == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("输入不完整");
                alert.setHeaderText(null);
                alert.setContentText("请填写提醒内容和选择日期");
                alert.showAndWait();
                return;
            }

            try {
                boolean success = onlineClassService.addReminder(content, java.sql.Date.valueOf(date));
                if (success) {
                    // 刷新提醒列表
                    List<OnlineClassService.Reminder> reminders = onlineClassService.getDayReminders(
                            date.getYear(), date.getMonthValue(), date.getDayOfMonth());
                    List<String> reminderContents = new ArrayList<>();
                    for (OnlineClassService.Reminder reminder : reminders) {
                        reminderContents.add(reminder.getContent());
                    }
                    scheduleList.getItems().setAll(reminderContents);

                    // 刷新日历上的提醒标记
                    daysWithReminders = onlineClassService.getMonthReminders(
                            currentCalendarDate.getYear(), currentCalendarDate.getMonthValue());

                    // 重新加载页面以更新日历
                    showSchedulePage();

                    // 清空表单
                    reminderContentField.clear();
                    reminderDatePicker.setValue(currentCalendarDate);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("添加成功");
                    alert.setHeaderText(null);
                    alert.setContentText("提醒添加成功");
                    alert.showAndWait();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("添加失败");
                alert.setHeaderText(null);
                alert.setContentText("添加提醒失败: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        addReminderForm.getChildren().addAll(addReminderLabel, reminderContentField, reminderDatePicker, addReminderBtn);
        scheduleDetail.getChildren().addAll(detailLabel, scheduleList, addReminderForm);

        return scheduleDetail;
    }

    /**
     * 显示我的课程页面
     */
    private void showCoursesPage() {
        currentView = "courses";

        loadCoursesFromDatabase();

        // 创建主布局
        HBox mainLayout = new HBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #ecf0f1;");

        // 左侧统计栏
        VBox statsBox = new VBox(15);
        statsBox.setPrefWidth(200);
        statsBox.setStyle("-fx-background-color: #34495e; -fx-padding: 15;");

        // 初始化统计卡片（将从数据库获取数据）
        VBox notStarted = createStatCard("未开始课程", "0门", "#e74c3c");
        VBox inProgress = createStatCard("进行中课程", "0门", "#3498db");
        VBox completed = createStatCard("已结束课程", "0门", "#27ae60");
        VBox total = createStatCard("总计", "0门", "#2c3e50");

        // 从数据库获取统计数据
        try {
            OnlineClassService.CourseStats stats = onlineClassService.getCourseStats();
            notStarted = createStatCard("未开始课程", stats.notStarted + "门", "#e74c3c");
            inProgress = createStatCard("进行中课程", stats.inProgress + "门", "#3498db");
            completed = createStatCard("已结束课程", stats.completed + "门", "#27ae60");
            total = createStatCard("总计", stats.total + "门", "#2c3e50");
        } catch (SQLException e) {
            e.printStackTrace();
            // 显示错误消息
            System.err.println("加载课程统计失败: " + e.getMessage());
        }

        statsBox.getChildren().addAll(notStarted, inProgress, completed, total);

        // 右侧内容区域
        VBox contentArea = new VBox(20);
        contentArea.setPrefWidth(800);

        // 搜索和筛选区域
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("搜索课程名称或教师...");
        searchField.setPrefWidth(300);

        Button searchBtn = new Button("搜索");
        Button filterBtn = new Button("筛选");

        // 将"全部课程"按钮改为筛选框
        ComboBox<String> filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("全部课程", "进行中", "未开始", "已结束");
        filterComboBox.setValue("全部课程");
        filterComboBox.setPrefWidth(120);

        // 搜索按钮事件
        searchBtn.setOnAction(e -> {
            String keyword = searchField.getText();
            String filter = filterComboBox.getValue();

            try {
                List<Course> courses = onlineClassService.searchCourses(keyword, filter);
                courseList.setAll(courses);
            } catch (SQLException ex) {
                ex.printStackTrace();
                // 显示错误消息
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("搜索失败");
                alert.setHeaderText(null);
                alert.setContentText("搜索课程失败: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        // 筛选按钮事件
        filterBtn.setOnAction(e -> {
            String keyword = searchField.getText();
            String filter = filterComboBox.getValue();

            try {
                List<Course> courses = onlineClassService.searchCourses(keyword, filter);
                courseList.setAll(courses);
            } catch (SQLException ex) {
                ex.printStackTrace();
                // 显示错误消息
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("筛选失败");
                alert.setHeaderText(null);
                alert.setContentText("筛选课程失败: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        searchBox.getChildren().addAll(searchField, searchBtn, filterBtn, filterComboBox);

        // 课程表格
        Label tableLabel = new Label("课程列表");
        tableLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        TableView<Course> courseTable = new TableView<>();
        courseTable.setItems(courseList); // 使用从数据库加载的数据

        // 设置列宽策略，避免空白列
        courseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Course, String> nameCol = new TableColumn<>("课程名称");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));

        TableColumn<Course, String> classCol = new TableColumn<>("班级");
        classCol.setCellValueFactory(new PropertyValueFactory<>("className"));

        TableColumn<Course, String> teacherCol = new TableColumn<>("授课教师");
        teacherCol.setCellValueFactory(new PropertyValueFactory<>("teacherName"));

        TableColumn<Course, String> startCol = new TableColumn<>("开课时间");
        startCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));

        TableColumn<Course, String> endCol = new TableColumn<>("结课时间");
        endCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        TableColumn<Course, String> statusCol = new TableColumn<>("课程状态");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Course, Integer> creditCol = new TableColumn<>("学分");
        creditCol.setCellValueFactory(new PropertyValueFactory<>("credits"));

        TableColumn<Course, Void> actionCol = new TableColumn<>("操作");
        actionCol.setCellFactory(createActionCellFactory());

        courseTable.getColumns().addAll(nameCol, classCol, teacherCol, startCol, endCol, statusCol, creditCol, actionCol);
        courseTable.setPrefHeight(400);

        contentArea.getChildren().addAll(searchBox, tableLabel, courseTable);

        mainLayout.getChildren().addAll(statsBox, contentArea);
        this.setCenter(mainLayout);
        this.setLeft(null); // 移除左侧导航
    }

    /**
     * 显示课程详情页面
     */
    private void showCourseDetailPage(Course course) {
        currentView = "courseDetail";
        currentCourse = course;

        VBox detailPage = new VBox(20);
        detailPage.setPadding(new Insets(20));
        detailPage.setStyle("-fx-background-color: #ecf0f1;");

        // 返回按钮区域
        HBox backButtonBox = new HBox();
        backButtonBox.setAlignment(Pos.CENTER_LEFT);
        Button backButton = new Button("← 返回课程列表");
        backButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        backButton.setOnAction(e -> showCoursesPage());
        backButtonBox.getChildren().add(backButton);

        // 课程基本信息
        VBox courseInfoBox = new VBox(10);
        courseInfoBox.setPadding(new Insets(15));
        courseInfoBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label courseTitle = new Label(course.getCourseName());
        courseTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(10, 0, 0, 0));

        // 课程信息表格
        infoGrid.add(new Label("授课教师:"), 0, 0);
        infoGrid.add(new Label(course.getTeacherName()), 1, 0);

        infoGrid.add(new Label("课程时间:"), 0, 1);
        infoGrid.add(new Label(course.getStartDate() + " 至 " + course.getEndDate()), 1, 1);

        infoGrid.add(new Label("课程状态:"), 0, 2);
        Label statusLabel = new Label(course.getStatus());
        statusLabel.setTextFill(course.getStatus().equals("进行中") ? Color.GREEN : Color.GRAY);
        infoGrid.add(statusLabel, 1, 2);

        infoGrid.add(new Label("课程学分:"), 0, 3);
        infoGrid.add(new Label(String.valueOf(course.getCredits())), 1, 3);

        courseInfoBox.getChildren().addAll(courseTitle, infoGrid);

        // 功能选项卡
        TabPane tabPane = new TabPane();

        // 课程回放选项卡
        Tab playbackTab = new Tab("课程回放", createPlaybackContent());
        playbackTab.setClosable(false);

        // 课程资料选项卡
        Tab materialsTab = new Tab("课程资料", createMaterialsContent());
        materialsTab.setClosable(false);

        // 讨论区选项卡
        Tab discussionTab = new Tab("讨论区", createDiscussionContent());
        discussionTab.setClosable(false);

        tabPane.getTabs().addAll(playbackTab, materialsTab, discussionTab);

        detailPage.getChildren().addAll(backButtonBox, courseInfoBox, tabPane);
        this.setCenter(detailPage);
        this.setLeft(null); // 移除左侧导航
    }

    /**
     * 创建课程回放内容
     */
    private VBox createPlaybackContent() {
        VBox playbackContent = new VBox(15);
        playbackContent.setPadding(new Insets(15));

        Label titleLabel = new Label("课程回放列表");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        ListView<CoursePlayback> playbackListView = new ListView<>();
        try {
            Course course = new Course();
            playbackList.setAll(onlineClassService.getCoursePlaybacks(course.getCourseName()));
        } catch (SQLException e) {
            e.printStackTrace();
            // 处理错误
        }
        playbackListView.setCellFactory(param -> new ListCell<CoursePlayback>() {
            @Override
            protected void updateItem(CoursePlayback item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDate() + " - " + item.getTitle() + " (" + item.getDuration() + ")");
                }
            }
        });
        playbackListView.setPrefHeight(300);

        Button playButton = new Button("播放选中的回放");
        playButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

        playbackContent.getChildren().addAll(titleLabel, playbackListView, playButton);
        return playbackContent;
    }

    /**
     * 创建课程资料内容
     */
    private VBox createMaterialsContent() {
        VBox materialsContent = new VBox(15);
        materialsContent.setPadding(new Insets(15));

        Label titleLabel = new Label("课程资料列表");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        TableView<CourseMaterial> materialsTable = new TableView<>();
        try {
            Course course = new Course();
            materialList.setAll(onlineClassService.getCourseMaterials(course.getCourseName()));
        } catch (SQLException e) {
            e.printStackTrace();
            // 处理错误
        }

        TableColumn<CourseMaterial, String> nameCol = new TableColumn<>("资料名称");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<CourseMaterial, String> typeCol = new TableColumn<>("类型");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<CourseMaterial, String> sizeCol = new TableColumn<>("大小");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));

        TableColumn<CourseMaterial, String> dateCol = new TableColumn<>("上传日期");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("uploadDate"));

        TableColumn<CourseMaterial, Void> actionCol = new TableColumn<>("操作");
        actionCol.setCellFactory(param -> new TableCell<CourseMaterial, Void>() {
            private final Button downloadBtn = new Button("下载");

            {
                downloadBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                downloadBtn.setOnAction(event -> {
                    CourseMaterial material = getTableView().getItems().get(getIndex());
                    // 下载逻辑
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(downloadBtn);
                }
            }
        });

        materialsTable.getColumns().addAll(nameCol, typeCol, sizeCol, dateCol, actionCol);
        materialsTable.setPrefHeight(300);

        materialsContent.getChildren().addAll(titleLabel, materialsTable);
        return materialsContent;
    }

    /**
     * 创建讨论区内容
     */
    private VBox createDiscussionContent() {
        VBox discussionContent = new VBox(15);
        discussionContent.setPadding(new Insets(15));

        Label titleLabel = new Label("课程讨论区");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        TextArea newPostArea = new TextArea();
        newPostArea.setPromptText("发表新的讨论...");
        newPostArea.setPrefHeight(100);

        Button postButton = new Button("发表");
        postButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

        // 初始化讨论列表
        ListView<String> discussionListView = new ListView<>();
        discussionListView.setItems(discussionList);

        try {
            List<String> discussions = onlineClassService.getDiscussions(currentCourse.getCourseName());
            discussionList.setAll(discussions);
        } catch (SQLException e) {
            e.printStackTrace();
            discussionList.add("加载讨论失败: " + e.getMessage());
        }
        discussionListView.setPrefHeight(200);

        postButton.setOnAction(e -> {
            String content = newPostArea.getText();
            if (content.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("内容为空");
                alert.setHeaderText(null);
                alert.setContentText("请输入讨论内容");
                alert.showAndWait();
                return;
            }

            try {
                boolean success = onlineClassService.postDiscussion(currentCourse.getCourseName(), content);
                if (success) {
                    // 刷新讨论列表
                    List<String> discussions = onlineClassService.getDiscussions(currentCourse.getCourseName());
                    discussionList.setAll(discussions);
                    newPostArea.clear();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("发表成功");
                    alert.setHeaderText(null);
                    alert.setContentText("讨论发表成功");
                    alert.showAndWait();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("发表失败");
                alert.setHeaderText(null);
                alert.setContentText("发表讨论失败: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        discussionContent.getChildren().addAll(titleLabel, newPostArea, postButton, discussionListView);
        return discussionContent;
    }


    /**
     * 显示我的作业页面
     */
    private void showAssignmentsPage() {
        currentView = "assignments";

        loadAssignmentsFromDatabase();

        // 创建主布局
        HBox mainLayout = new HBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #ecf0f1;");

        // 左侧统计栏
        VBox statsBox = new VBox(15);
        statsBox.setPrefWidth(200);
        statsBox.setStyle("-fx-background-color: #34495e; -fx-padding: 15;");

        // 初始化统计卡片（将从数据库获取数据）
        VBox submitted = createStatCard("已提交", "0个", "#27ae60");
        VBox notSubmitted = createStatCard("未提交", "0个", "#e74c3c");
        VBox urgent = createStatCard("紧急作业", "0个", "#f39c12");
        VBox total = createStatCard("总计", "0个", "#2c3e50");

        // 从数据库获取统计数据
        try {
            OnlineClassService.AssignmentStats stats = onlineClassService.getAssignmentStats();
            submitted = createStatCard("已提交", stats.submitted + "个", "#27ae60");
            notSubmitted = createStatCard("未提交", stats.notSubmitted + "个", "#e74c3c");
            urgent = createStatCard("紧急作业", stats.urgent + "个", "#f39c12");
            total = createStatCard("总计", stats.total + "个", "#2c3e50");
        } catch (SQLException e) {
            e.printStackTrace();
            // 显示错误消息
            System.err.println("加载作业统计失败: " + e.getMessage());
        }

        VBox progressBox = new VBox(5);
        progressBox.setAlignment(Pos.CENTER);
        progressBox.setStyle("-fx-background-color: #2c3e50; -fx-padding: 10; -fx-background-radius: 8;");
        Label progressLabel = new Label("提交进度: 0%");
        progressLabel.setTextFill(Color.WHITE);
        progressLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(180);

        // 更新进度条
        try {
            OnlineClassService.AssignmentStats stats = onlineClassService.getAssignmentStats();
            double progress = stats.total > 0 ? (double) stats.submitted / stats.total : 0;
            progressBar.setProgress(progress);
            progressLabel.setText("提交进度: " + String.format("%.1f%%", progress * 100));
        } catch (SQLException e) {
            e.printStackTrace();
            // 显示错误消息
            System.err.println("加载作业统计失败: " + e.getMessage());
        }

        progressBox.getChildren().addAll(progressLabel, progressBar);

        statsBox.getChildren().addAll(submitted, notSubmitted, urgent, total, progressBox);

        // 右侧内容区域
        VBox contentArea = new VBox(20);
        contentArea.setPrefWidth(800);

        // 搜索和筛选区域
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("搜索作业名称或课程...");
        searchField.setPrefWidth(300);

        Button searchBtn = new Button("搜索");
        Button filterBtn = new Button("筛选");

        // 将"全部"按钮改为筛选框
        ComboBox<String> filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("全部作业", "已提交", "未提交", "紧急作业");
        filterComboBox.setValue("全部作业");
        filterComboBox.setPrefWidth(120);

        // 搜索按钮事件
        searchBtn.setOnAction(e -> {
            String keyword = searchField.getText();
            String filter = filterComboBox.getValue();

            try {
                List<Assignment> assignments = onlineClassService.searchAssignments(keyword, filter);
                assignmentList.setAll(assignments);
            } catch (SQLException ex) {
                ex.printStackTrace();
                // 显示错误消息
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("搜索失败");
                alert.setHeaderText(null);
                alert.setContentText("搜索作业失败: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        // 筛选按钮事件
        filterBtn.setOnAction(e -> {
            String keyword = searchField.getText();
            String filter = filterComboBox.getValue();

            try {
                List<Assignment> assignments = onlineClassService.searchAssignments(keyword, filter);
                assignmentList.setAll(assignments);
            } catch (SQLException ex) {
                ex.printStackTrace();
                // 显示错误消息
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("筛选失败");
                alert.setHeaderText(null);
                alert.setContentText("筛选作业失败: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        searchBox.getChildren().addAll(searchField, searchBtn, filterBtn, filterComboBox);

        // 紧急作业区域
        Label urgentLabel = new Label("紧急作业（截止日期临近）");
        urgentLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        TableView<Assignment> urgentTable = new TableView<>();
        // 使用从数据库加载的数据，筛选出紧急作业
        urgentTable.setItems(assignmentList.filtered(a -> "紧急".equals(a.getPriority())));

        // 设置列宽策略，避免空白列
        urgentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Assignment, String> nameCol = new TableColumn<>("作业名称");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Assignment, String> courseCol = new TableColumn<>("课程");
        courseCol.setCellValueFactory(new PropertyValueFactory<>("course"));

        TableColumn<Assignment, String> deadlineCol = new TableColumn<>("截止时间");
        deadlineCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));

        TableColumn<Assignment, String> timeLeftCol = new TableColumn<>("剩余时间");
        timeLeftCol.setCellValueFactory(new PropertyValueFactory<>("timeLeft"));

        TableColumn<Assignment, String> statusCol = new TableColumn<>("状态");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Assignment, Void> actionCol = new TableColumn<>("操作");
        actionCol.setCellFactory(createAssignmentActionCellFactory());

        urgentTable.getColumns().addAll(nameCol, courseCol, deadlineCol, timeLeftCol, statusCol, actionCol);
        urgentTable.setPrefHeight(120);

        // 全部作业区域
        Label allLabel = new Label("全部作业");
        allLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        TableView<Assignment> allTable = new TableView<>();
        allTable.setItems(assignmentList); // 使用从数据库加载的数据

        // 设置列宽策略，避免空白列
        allTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Assignment, String> allNameCol = new TableColumn<>("作业名称");
        allNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Assignment, String> allCourseCol = new TableColumn<>("课程");
        allCourseCol.setCellValueFactory(new PropertyValueFactory<>("course"));

        TableColumn<Assignment, String> assignDateCol = new TableColumn<>("布置时间");
        assignDateCol.setCellValueFactory(new PropertyValueFactory<>("assignDate"));

        TableColumn<Assignment, String> allDeadlineCol = new TableColumn<>("截止时间");
        allDeadlineCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));

        TableColumn<Assignment, String> allStatusCol = new TableColumn<>("状态");
        allStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Assignment, String> priorityCol = new TableColumn<>("优先级");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));

        TableColumn<Assignment, Void> allActionCol = new TableColumn<>("操作");
        allActionCol.setCellFactory(createAssignmentActionCellFactory());

        allTable.getColumns().addAll(allNameCol, allCourseCol, allDeadlineCol, allStatusCol, priorityCol, allActionCol);
        allTable.setPrefHeight(300);

        contentArea.getChildren().addAll(searchBox, urgentLabel, urgentTable, allLabel, allTable);

        mainLayout.getChildren().addAll(statsBox, contentArea);
        this.setCenter(mainLayout);
        this.setLeft(null); // 移除左侧导航
    }

    /**
     * 创建统计卡片
     */
    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 8;");
        card.setAlignment(Pos.CENTER);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        valueLabel.setTextFill(Color.WHITE);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font(12));
        titleLabel.setTextFill(Color.WHITE);

        card.getChildren().addAll(valueLabel, titleLabel);
        return card;
    }


    /**
     * 创建操作列的单元格工厂
     */
    private Callback<TableColumn<Course, Void>, TableCell<Course, Void>> createActionCellFactory() {
        return new Callback<TableColumn<Course, Void>, TableCell<Course, Void>>() {
            @Override
            public TableCell<Course, Void> call(final TableColumn<Course, Void> param) {
                return new TableCell<Course, Void>() {
                    private final Button detailBtn = new Button("查看详情");

                    {
                        detailBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12;");

                        detailBtn.setOnAction(event -> {
                            Course course = getTableView().getItems().get(getIndex());
                            showCourseDetailPage(course);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox buttons = new HBox(5);
                            buttons.getChildren().addAll(detailBtn);
                            setGraphic(buttons);
                        }
                    }
                };
            }
        };
    }

    /**
     * 创建作业表格操作列
     */
    private Callback<TableColumn<Assignment, Void>, TableCell<Assignment, Void>> createAssignmentActionCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Assignment, Void> call(final TableColumn<Assignment, Void> param) {
                return new TableCell<>() {
                    private final Button submitBtn = new Button("提交作业");
                    private final FileChooser fileChooser = new FileChooser();

                    {
                        submitBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                        submitBtn.setOnAction(event -> {
                            Assignment assignment = getTableView().getItems().get(getIndex());
                            // 添加文件选择器
                            File file = fileChooser.showOpenDialog(null);
                            if (file != null) {
                                try {
                                    boolean success = onlineClassService.submitAssignment(
                                            assignment.getName(), file.getAbsolutePath());
                                    if (success) {
                                        // 更新UI
                                        assignment.setStatus("已提交");
                                        getTableView().refresh();
                                        // 显示成功消息
                                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                        alert.setTitle("提交成功");
                                        alert.setHeaderText(null);
                                        alert.setContentText("作业提交成功！");
                                        alert.showAndWait();
                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                    // 显示错误消息
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle("提交失败");
                                    alert.setHeaderText(null);
                                    alert.setContentText("作业提交失败: " + e.getMessage());
                                    alert.showAndWait();
                                }
                            }
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Assignment assignment = getTableView().getItems().get(getIndex());
                            // 只有未提交的作业显示提交按钮
                            if ("未提交".equals(assignment.getStatus())) {
                                setGraphic(submitBtn);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        };
    }
}