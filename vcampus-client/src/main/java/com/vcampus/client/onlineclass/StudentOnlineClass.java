package com.vcampus.client.onlineclass;

import com.vcampus.client.service.ClientService;
import com.vcampus.common.entity.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 在线课堂模块 - 学生界面
 */
public class StudentOnlineClass extends BorderPane {

    private String currentView = "home";
    private Course currentCourse; // 当前选中的课程
    private User currentUser;
    private ClientService clientService;
    private Label detailLabel;
    private ListView<String> scheduleList;
    private LocalDate currentCalendarDate = LocalDate.now();
    private List<Integer> daysWithReminders = new ArrayList<>();
    private TableView<Course> courseTable = new TableView<>();
    private TableView<Assignment> urgentTable = new TableView<>();
    private TableView<Assignment> allTable = new TableView<>();

    private boolean courseTableInitialized = false;
    private boolean assignmentTableInitialized = false;

    private ObservableList<Course> courseList = FXCollections.observableArrayList();
    private ObservableList<Assignment> assignmentList = FXCollections.observableArrayList();
    private ObservableList<CoursePlayback> playbackList = FXCollections.observableArrayList();
    private ObservableList<CourseMaterial> materialList = FXCollections.observableArrayList();
    private ObservableList<String> discussionList = FXCollections.observableArrayList();

    public StudentOnlineClass(User user, ClientService clientService) {
        this.currentUser = user;
        this.clientService = clientService;

        initCourseTableColumns();
        initAssignmentTableColumns();

        // 初始化数据
        loadCoursesFromDatabase();
        loadAssignmentsFromDatabase();

        initRootLayout();
        showHomePage();
    }

    /**
     * 初始化课程表格列
     */
    private void initCourseTableColumns() {
        if (courseTableInitialized) return;

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
        courseTableInitialized = true;
    }

    /**
     * 初始化作业表格列
     */
    private void initAssignmentTableColumns() {
        if (assignmentTableInitialized) return;

        // 紧急作业表格列
        TableColumn<Assignment, String> urgentNameCol = new TableColumn<>("作业名称");
        urgentNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Assignment, String> urgentCourseCol = new TableColumn<>("课程");
        urgentCourseCol.setCellValueFactory(new PropertyValueFactory<>("course"));

        TableColumn<Assignment, String> urgentDeadlineCol = new TableColumn<>("截止时间");
        urgentDeadlineCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));

        TableColumn<Assignment, String> urgentTimeLeftCol = new TableColumn<>("剩余时间");
        urgentTimeLeftCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Assignment, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Assignment, String> param) {
                Assignment assignment = param.getValue();
                if (assignment != null && assignment.getDueDate() != null) {
                    try {
                        LocalDate dueDate = LocalDate.parse(assignment.getDueDate());
                        LocalDate today = LocalDate.now();
                        long daysBetween = ChronoUnit.DAYS.between(today, dueDate);

                        if (daysBetween < 0) {
                            return new SimpleStringProperty("已过期");
                        } else {
                            return new SimpleStringProperty(daysBetween + "天");
                        }
                    } catch (Exception e) {
                        return new SimpleStringProperty("日期格式错误");
                    }
                }
                return new SimpleStringProperty("无截止日期");
            }
        });

        TableColumn<Assignment, String> urgentStatusCol = new TableColumn<>("状态");
        urgentStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Assignment, Void> urgentActionCol = new TableColumn<>("操作");
        urgentActionCol.setCellFactory(createAssignmentActionCellFactory());

        urgentTable.getColumns().addAll(urgentNameCol, urgentCourseCol, urgentDeadlineCol, urgentTimeLeftCol, urgentStatusCol, urgentActionCol);

        // 全部作业表格列
        TableColumn<Assignment, String> allNameCol = new TableColumn<>("作业名称");
        allNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Assignment, String> allCourseCol = new TableColumn<>("课程");
        allCourseCol.setCellValueFactory(new PropertyValueFactory<>("course"));

        TableColumn<Assignment, String> allDeadlineCol = new TableColumn<>("截止时间");
        allDeadlineCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));

        TableColumn<Assignment, String> allStatusCol = new TableColumn<>("状态");
        allStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Assignment, String> priorityCol = new TableColumn<>("优先级");
        priorityCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Assignment, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Assignment, String> param) {
                Assignment assignment = param.getValue();
                if (assignment != null) {
                    return new SimpleStringProperty(assignment.getPriority() != null ? assignment.getPriority() : "普通");
                }
                return new SimpleStringProperty("普通");
            }
        });

        TableColumn<Assignment, Void> allActionCol = new TableColumn<>("操作");
        allActionCol.setCellFactory(createAssignmentActionCellFactory());

        allTable.getColumns().addAll(allNameCol, allCourseCol, allDeadlineCol, allStatusCol, priorityCol, allActionCol);
        assignmentTableInitialized = true;
    }

    /**
     * 从数据库加载课程数据
     */
    private void loadCoursesFromDatabase() {
        try {
            List<Course> courses = clientService.getStudentCourses(currentUser);
            courseList.setAll(courses);
        } catch (Exception e) {
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
            List<Assignment> assignments = clientService.getStudentAssignments(currentUser);

            // 检查每个作业的截止日期，如果临近则设置优先级为紧急
            LocalDate today = LocalDate.now();
            for (Assignment assignment : assignments) {
                if ("未提交".equals(assignment.getStatus()) && assignment.getDueDate() != null) {
                    try {
                        LocalDate dueDate = LocalDate.parse(assignment.getDueDate());
                        long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);

                        // 设置优先级
                        if (daysUntilDue < 3 && daysUntilDue >= 0) {
                            assignment.setPriority("紧急");
                        } else {
                            assignment.setPriority("普通");
                        }
                    } catch (Exception e) {
                        assignment.setPriority("普通");
                    }
                } else {
                    assignment.setPriority("普通");
                }
            }

            assignmentList.setAll(assignments);
            System.out.println("Loaded " + assignmentList.size() + " assignments");
            for (Assignment a : assignments) {
                System.out.println(a.getName() + " - Status: " + a.getStatus() + ", Priority: " + a.getPriority());
            }

            // 刷新表格
            Platform.runLater(() -> {
                urgentTable.refresh();
                allTable.refresh();
            });
        } catch (Exception e) {
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
        title.setFont(Font.font("微软雅黑", FontWeight.BOLD, 30));
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-font: 微软雅黑; -fx-font-weight: BOLD; -fx-font-size: 30px; -fx-text-alignment: center;");

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
        welcomeLabel.setStyle("-fx-font-family: '微软雅黑'; -fx-font-weight: BOLD; -fx-font-size: 28px; -fx-text-fill: #2c3e50;");
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
        activityLabel.setStyle("-fx-font-family: '微软雅黑'; -fx-font-weight: BOLD; -fx-font-size: 22px; -fx-text-fill: #2c3e50;");

        ListView<String> activityList = new ListView<>();
        try {
            List<String> activities = clientService.getRecentActivities(currentUser);
            activityList.getItems().setAll(activities);
        } catch (Exception e) {
            e.printStackTrace();
            activityList.getItems().add("加载活动失败: " + e.getMessage());
        }

        // 修改1: 设置活动列表为可调整大小
        activityList.setPrefHeight(Region.USE_COMPUTED_SIZE);
        activityList.setStyle("-fx-font-size: 18px;");
        VBox.setVgrow(activityList, Priority.ALWAYS);

        recentActivity.getChildren().addAll(activityLabel, activityList);
        VBox.setVgrow(recentActivity, Priority.ALWAYS);

        homePage.getChildren().addAll(welcomeBox, quickAccess, recentActivity);
        VBox.setVgrow(recentActivity, Priority.ALWAYS);
        this.setCenter(homePage);
        this.setLeft(null);
    }

    private VBox createQuickAccessCard(String title, String description, String emoji, EventHandler<ActionEvent> action) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefSize(200, 120);
        card.setAlignment(Pos.CENTER);

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 24px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-family: '微软雅黑'; -fx-font-weight: BOLD; -fx-font-size: 24px; -fx-text-fill: #2c3e50;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-family: '微软雅黑'; -fx-font-size: 18px; -fx-text-fill: #7f8c8d;");

        Button enterBtn = new Button("进入");
        enterBtn.setStyle("-fx-font-size: 18px; -fx-background-color: #3498db; -fx-text-fill: white;");
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
            daysWithReminders = clientService.getMonthReminders(
                    currentCalendarDate.getYear(),
                    currentCalendarDate.getMonthValue()
            );
        } catch (Exception e) {
            e.printStackTrace();
            daysWithReminders = new ArrayList<>();
        }

        VBox schedulePage = new VBox(20);
        schedulePage.setPadding(new Insets(20));
        schedulePage.setStyle("-fx-background-color: white;");

        // 日历和日程区域
        HBox mainContent = new HBox(20);
        mainContent.setAlignment(Pos.TOP_CENTER);

        // 日历部分 - 修改2: 增大日历区域
        VBox calendarSection = new VBox(10);
        calendarSection.setPrefWidth(600);
        calendarSection.setAlignment(Pos.TOP_CENTER);
        calendarSection.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");

        // 月份标签和导航
        Label monthLabel = new Label(currentCalendarDate.getYear() + "年 " + currentCalendarDate.getMonthValue() + "月");
        monthLabel.setStyle("-fx-font-family: '微软雅黑'; -fx-font-weight: BOLD; -fx-font-size: 28px; -fx-text-fill: #2c3e50;");

        HBox monthNav = new HBox(10);
        monthNav.setAlignment(Pos.CENTER);
        Button prevMonth = new Button("上月");
        Button todayBtn = new Button("今天");
        Button nextMonth = new Button("下月");
        prevMonth.setStyle("-fx-font-family: '微软雅黑'; -fx-font-size: 18px; -fx-background-color: #3498db; -fx-text-fill: white;");
        todayBtn.setStyle("-fx-font-family: '微软雅黑'; -fx-font-size: 18px; -fx-background-color: #2ecc71; -fx-text-fill: white;");
        nextMonth.setStyle("-fx-font-family: '微软雅黑'; -fx-font-size: 18px; -fx-background-color: #3498db; -fx-text-fill: white;");

        // 月份导航按钮事件
        prevMonth.setOnAction(e -> {
            currentCalendarDate = currentCalendarDate.minusMonths(1);
            showSchedulePage();
        });

        nextMonth.setOnAction(e -> {
            currentCalendarDate = currentCalendarDate.plusMonths(1);
            showSchedulePage();
        });

        todayBtn.setOnAction(e -> {
            currentCalendarDate = LocalDate.now();
            showSchedulePage();
        });

        monthNav.getChildren().addAll(prevMonth, todayBtn, nextMonth);

        // 日历网格
        GridPane calendarGrid = createCalendarGrid(currentCalendarDate);
        calendarSection.getChildren().addAll(monthLabel, monthNav, calendarGrid);

        // 日程详情部分
        VBox scheduleDetail = createScheduleDetail();
        scheduleDetail.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");

        mainContent.getChildren().addAll(calendarSection, scheduleDetail);
        schedulePage.getChildren().add(mainContent);

        this.setCenter(schedulePage);
        this.setLeft(null);
    }


    /**
     * 创建日历网格
     */
    private GridPane createCalendarGrid(LocalDate date) {
        GridPane calendarGrid = new GridPane();
        calendarGrid.setHgap(10);
        calendarGrid.setVgap(10);
        calendarGrid.setAlignment(Pos.CENTER);
        calendarGrid.setPadding(new Insets(10));

        // 添加星期标题
        String[] days = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setPrefSize(60, 40);
            dayLabel.setStyle("-fx-font-size: 18px; -fx-background-color: #3498db; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
            calendarGrid.add(dayLabel, i, 0);
        }

        // 获取月份的第一天和这个月的天数
        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        int daysInMonth = date.lengthOfMonth();

        // 计算第一天是星期几 (0=周日, 1=周一, ..., 6=周六)
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7;

        // 填充日历
        int day = 1;
        for (int week = 1; week <= 6; week++) {
            for (int d = 0; d < 7; d++) {
                if ((week == 1 && d < dayOfWeek) || day > daysInMonth) {
                    // 添加空单元格
                    StackPane emptyCell = new StackPane();
                    emptyCell.setPrefSize(60, 60);
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
        dayCell.setPrefSize(60, 60);
        dayCell.setStyle("-fx-border-color: #bdc3c7; -fx-border-radius: 5;");

        // 检查这一天是否有提醒
        boolean hasReminder = daysWithReminders.contains(day);

        // 如果是今天，高亮显示
        LocalDate today = LocalDate.now();
        boolean isToday = currentCalendarDate.getYear() == today.getYear() &&
                currentCalendarDate.getMonthValue() == today.getMonthValue() &&
                day == today.getDayOfMonth();

        if (isToday) {
            dayCell.setStyle("-fx-background-color: #3498db; -fx-background-radius: 5;");
        } else if (hasReminder) {
            dayCell.setStyle("-fx-background-color: #fff9c4; -fx-background-radius: 5;");
        }

        VBox content = new VBox(2);
        content.setAlignment(Pos.TOP_CENTER);

        Label dateLabel = new Label(String.valueOf(day));
        dateLabel.setAlignment(Pos.CENTER);
        dateLabel.setStyle("-fx-font-size: 18px;");

        if (isToday) {
            dateLabel.setTextFill(Color.WHITE);
            dateLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: BOLD;");
        } else if (hasReminder) {
            dateLabel.setTextFill(Color.DARKBLUE);
            dateLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: BOLD;");
        }

        // 如果有提醒，添加提醒标记
        if (hasReminder) {
            Circle reminderDot = new Circle(4);
            reminderDot.setFill(Color.RED);
            content.getChildren().addAll(dateLabel, reminderDot);
        } else {
            content.getChildren().add(dateLabel);
        }

        dayCell.getChildren().add(content);

        // 添加点击事件
        dayCell.setOnMouseClicked(e -> {
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
        detailLabel.setStyle("-fx-font: 微软雅黑; -fx-font-weight: BOLD; -fx-font-size: 28px; -fx-text-alignment: center;");

        // 获取选中日期的提醒
        try {
            List<Reminder> reminders = clientService.getDayReminders(
                    currentCalendarDate.getYear(),
                    currentCalendarDate.getMonthValue(),
                    day
            );
            List<String> reminderContents = new ArrayList<>();
            for (Reminder reminder : reminders) {
                reminderContents.add(reminder.getContent());
            }
            scheduleList.getItems().setAll(reminderContents);
        } catch (Exception e) {
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
        detailLabel.setStyle("-fx-font-family: '微软雅黑'; -fx-font-weight: BOLD; -fx-font-size: 24px; -fx-text-fill: #2c3e50;");

        scheduleList = new ListView<>();
        try {
            List<Reminder> reminders = clientService.getDayReminders(
                    currentCalendarDate.getYear(),
                    currentCalendarDate.getMonthValue(),
                    currentCalendarDate.getDayOfMonth()
            );
            List<String> reminderContents = new ArrayList<>();
            for (Reminder reminder : reminders) {
                reminderContents.add(reminder.getContent());
            }
            scheduleList.getItems().setAll(reminderContents);
        } catch (Exception e) {
            e.printStackTrace();
            scheduleList.getItems().add("加载提醒失败: " + e.getMessage());
        }
        scheduleList.setPrefHeight(200);
        scheduleList.setStyle("-fx-font-size: 16px;");

        // 添加提醒表单
        VBox addReminderForm = new VBox(10);
        Label addReminderLabel = new Label("添加新提醒");
        addReminderLabel.setStyle("-fx-font-family: '微软雅黑'; -fx-font-weight: BOLD; -fx-font-size: 20px; -fx-text-fill: #2c3e50;");

        TextField reminderContentField = new TextField();
        reminderContentField.setPromptText("提醒内容");
        reminderContentField.setStyle("-fx-font-size: 16px;");

        DatePicker reminderDatePicker = new DatePicker();
        reminderDatePicker.setValue(currentCalendarDate);
        reminderDatePicker.setPromptText("选择日期");
        reminderDatePicker.setStyle("-fx-font-size: 16px;");

        Button addReminderBtn = new Button("添加提醒");
        addReminderBtn.setStyle("-fx-font-size: 16px; -fx-background-color: #27ae60; -fx-text-fill: white;");
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
                boolean success = clientService.addReminder(content, java.sql.Date.valueOf(date), currentUser);
                if (success) {
                    // 刷新提醒列表
                    List<Reminder> reminders = clientService.getDayReminders(
                            date.getYear(), date.getMonthValue(), date.getDayOfMonth());
                    List<String> reminderContents = new ArrayList<>();
                    for (Object reminderObj : reminders) {
                        if (reminderObj instanceof Map) {
                            Map<?, ?> reminderMap = (Map<?, ?>) reminderObj;
                            Object content1 = reminderMap.get("content");
                            if (content != null) {
                                reminderContents.add(content.toString());
                            }
                        }
                    }
                    scheduleList.getItems().setAll(reminderContents);

                    // 刷新日历上的提醒标记
                    daysWithReminders = clientService.getMonthReminders(
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
            } catch (Exception ex) {
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

        // 左侧统计栏 - 修改3: 设置为可调整大小
        VBox statsBox = new VBox(15);
        statsBox.setMinWidth(200);
        statsBox.setStyle("-fx-background-color: #34495e; -fx-padding: 15; -fx-background-radius: 10;");
        VBox.setVgrow(statsBox, Priority.ALWAYS);

        // 初始化统计卡片（将从数据库获取数据）
        VBox notStarted = createStatCard("未开始课程", "0门", "#e74c3c");
        VBox inProgress = createStatCard("进行中课程", "0门", "#3498db");
        VBox completed = createStatCard("已结束课程", "0门", "#27ae60");
        VBox total = createStatCard("总计", "0门", "#2c3e50");

        // 从数据库获取统计数据
        try {
            Object statsObj = clientService.getCourseStats(currentUser);
            CourseStats stats;

            if (statsObj instanceof Map) {
                stats = new CourseStats((Map<String, Object>) statsObj);
            } else {
                stats = (CourseStats) statsObj;
            }

            notStarted = createStatCard("未开始课程", stats.notStarted + "门", "#e74c3c");
            inProgress = createStatCard("进行中课程", stats.inProgress + "门", "#3498db");
            completed = createStatCard("已结束课程", stats.completed + "门", "#27ae60");
            total = createStatCard("总计", stats.total + "门", "#2c3e50");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("加载课程统计失败: " + e.getMessage());
        }

        statsBox.getChildren().addAll(notStarted, inProgress, completed, total);

        // 右侧内容区域
        VBox contentArea = new VBox(20);
        contentArea.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-background-radius: 10;");
        HBox.setHgrow(contentArea, Priority.ALWAYS);
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        // 搜索和筛选区域
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("搜索课程名称或教师...");
        searchField.setStyle("-fx-font-size: 16px;");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button searchBtn = new Button("搜索");
        searchBtn.setStyle("-fx-font-size: 16px; -fx-background-color: #3498db; -fx-text-fill: white;");

        // 修改3: 筛选按钮放在筛选框右边
        ComboBox<String> filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("全部课程", "进行中", "未开始", "已结束");
        filterComboBox.setValue("全部课程");
        filterComboBox.setStyle("-fx-font-size: 16px;");
        filterComboBox.setPrefWidth(120);

        Button filterBtn = new Button("筛选");
        filterBtn.setStyle("-fx-font-size: 16px; -fx-background-color: #2ecc71; -fx-text-fill: white;");

        // 搜索按钮事件
        searchBtn.setOnAction(e -> {
            String keyword = searchField.getText();
            String filter = filterComboBox.getValue();

            try {
                // 确保传递正确的参数
                List<Course> courses = clientService.searchCourses(keyword, filter, currentUser);
                courseList.setAll(courses);
                courseTable.refresh(); // 刷新表格显示

                System.out.println("搜索到 " + courses.size() + " 门课程"); // 调试信息
            } catch (Exception ex) {
                ex.printStackTrace();
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
                List<Course> courses = clientService.searchCourses(keyword, filter, currentUser);
                courseList.setAll(courses);
                courseTable.refresh();

                System.out.println("筛选到 " + courses.size() + " 门课程"); // 调试信息
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("筛选失败");
                alert.setHeaderText(null);
                alert.setContentText("筛选课程失败: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        searchBox.getChildren().addAll(searchField, searchBtn, filterComboBox, filterBtn);

        // 课程表格
        Label tableLabel = new Label("课程列表");
        tableLabel.setStyle("-fx-font-family: '微软雅黑'; -fx-font-weight: BOLD; -fx-font-size: 20px; -fx-text-fill: #2c3e50;");

        courseTable.setItems(courseList);
        courseTable.setStyle("-fx-font-size: 16px;");
        VBox.setVgrow(courseTable, Priority.ALWAYS);

        contentArea.getChildren().addAll(searchBox, tableLabel, courseTable);
        VBox.setVgrow(courseTable, Priority.ALWAYS);

        mainLayout.getChildren().addAll(statsBox, contentArea);
        HBox.setHgrow(contentArea, Priority.ALWAYS);
        this.setCenter(mainLayout);
        this.setLeft(null);
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
        backButton.setStyle("-fx-font-size: 16px; -fx-background-color: #3498db; -fx-text-fill: white;");
        backButton.setOnAction(e -> showCoursesPage());
        backButtonBox.getChildren().add(backButton);

        // 课程基本信息 - 修改4: 增大字体
        VBox courseInfoBox = new VBox(10);
        courseInfoBox.setPadding(new Insets(15));
        courseInfoBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label courseTitle = new Label(course.getCourseName());
        courseTitle.setStyle("-fx-font-family: '微软雅黑'; -fx-font-weight: BOLD; -fx-font-size: 24px; -fx-text-fill: #2c3e50;");

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(10, 0, 0, 0));

        // 课程信息表格 - 修改4: 增大字体
        infoGrid.add(createStyledLabel("授课教师:"), 0, 0);
        infoGrid.add(createStyledLabel(course.getTeacherName()), 1, 0);

        infoGrid.add(createStyledLabel("课程时间:"), 0, 1);
        infoGrid.add(createStyledLabel(course.getStartDate() + " 至 " + course.getEndDate()), 1, 1);

        infoGrid.add(createStyledLabel("课程状态:"), 0, 2);
        Label statusLabel = createStyledLabel(course.getStatus());
        statusLabel.setTextFill(course.getStatus().equals("进行中") ? Color.GREEN : Color.GRAY);
        infoGrid.add(statusLabel, 1, 2);

        infoGrid.add(createStyledLabel("课程学分:"), 0, 3);
        infoGrid.add(createStyledLabel(String.valueOf(course.getCredits())), 1, 3);

        courseInfoBox.getChildren().addAll(courseTitle, infoGrid);

        // 功能选项卡
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-font-size: 16px;");

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
        this.setLeft(null);
    }

    // 创建样式化标签的辅助方法
    private Label createStyledLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 16px;");
        return label;
    }


    /**
     * 创建课程回放内容
     */
    private VBox createPlaybackContent() {
        VBox playbackContent = new VBox(15);
        playbackContent.setPadding(new Insets(15));

        Label titleLabel = new Label("课程回放列表");
        titleLabel.setStyle("-fx-font-family: '微软雅黑'; -fx-font-weight: BOLD; -fx-font-size: 20px; -fx-text-fill: #2c3e50;");

        ListView<CoursePlayback> playbackListView = new ListView<>();
        try {
            playbackList.setAll(clientService.getCoursePlaybacks(currentCourse.getCourseId()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 修改4: 增大列表字体
        playbackListView.setStyle("-fx-font-size: 16px;");
        playbackListView.setCellFactory(param -> new ListCell<CoursePlayback>() {
            @Override
            protected void updateItem(CoursePlayback item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDate() + " - " + item.getTitle() + " (" + item.getDuration() + ")");
                    setStyle("-fx-font-size: 16px;");
                }
            }
        });
        playbackListView.setPrefHeight(300);
        playbackListView.setItems(playbackList);

        Button playButton = new Button("播放选中的回放");
        playButton.setStyle("-fx-font-size: 16px; -fx-background-color: #3498db; -fx-text-fill: white;");

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
        titleLabel.setStyle("-fx-font-family: '微软雅黑'; -fx-font-weight: BOLD; -fx-font-size: 20px; -fx-text-fill: #2c3e50;");

        TableView<CourseMaterial> materialsTable = new TableView<>();
        try {
            materialList.setAll(clientService.getCourseMaterials(currentCourse.getCourseId()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 修改4: 增大表格字体
        materialsTable.setStyle("-fx-font-size: 16px;");

        TableColumn<CourseMaterial, String> nameCol = new TableColumn<>("资料名称");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setStyle("-fx-font-size: 16px;");

        TableColumn<CourseMaterial, String> typeCol = new TableColumn<>("类型");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setStyle("-fx-font-size: 16px;");

        TableColumn<CourseMaterial, String> sizeCol = new TableColumn<>("大小");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        sizeCol.setStyle("-fx-font-size: 16px;");

        TableColumn<CourseMaterial, String> dateCol = new TableColumn<>("上传日期");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("uploadDate"));
        dateCol.setStyle("-fx-font-size: 16px;");

        TableColumn<CourseMaterial, Void> actionCol = new TableColumn<>("操作");
        actionCol.setCellFactory(param -> new TableCell<CourseMaterial, Void>() {
            private final Button downloadBtn = new Button("下载");

            {
                downloadBtn.setStyle("-fx-font-size: 14px; -fx-background-color: #27ae60; -fx-text-fill: white;");
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
        materialsTable.setItems(materialList);

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
        titleLabel.setStyle("-fx-font-family: '微软雅黑'; -fx-font-weight: BOLD; -fx-font-size: 20px; -fx-text-fill: #2c3e50;");

        TextArea newPostArea = new TextArea();
        newPostArea.setPromptText("发表新的讨论...");
        newPostArea.setPrefHeight(100);
        newPostArea.setStyle("-fx-font-size: 16px;");

        Button postButton = new Button("发表");
        postButton.setStyle("-fx-font-size: 16px; -fx-background-color: #3498db; -fx-text-fill: white;");

        // 初始化讨论列表
        ListView<String> discussionListView = new ListView<>();
        discussionListView.setItems(discussionList);
        discussionListView.setStyle("-fx-font-size: 16px;");

        // 加载讨论列表的方法
        Runnable loadDiscussions = () -> {
            try {
                List<String> discussions = clientService.getDiscussions(currentCourse.getCourseName());
                Platform.runLater(() -> {
                    discussionList.setAll(discussions);
                    discussionListView.refresh();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    discussionList.add("加载讨论失败: " + e.getMessage());
                });
            }
        };

        // 初始加载讨论
        loadDiscussions.run();

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

            // 使用异步任务处理网络请求
            Task<Boolean> postTask = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return clientService.postDiscussion(currentCourse.getCourseName(), content, currentUser);
                }

                @Override
                protected void succeeded() {
                    boolean success = getValue();
                    if (success) {
                        // 发表成功，清空输入框并刷新讨论列表
                        newPostArea.clear();

                        // 重新加载讨论列表
                        Task<Void> refreshTask = new Task<Void>() {
                            @Override
                            protected Void call() throws Exception {
                                List<String> newDiscussions = clientService.getDiscussions(currentCourse.getCourseName());
                                Platform.runLater(() -> {
                                    discussionList.setAll(newDiscussions);
                                    discussionListView.refresh();
                                });
                                return null;
                            }
                        };
                        new Thread(refreshTask).start();

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("发表成功");
                        alert.setHeaderText(null);
                        alert.setContentText("讨论发表成功！");
                        alert.showAndWait();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("发表失败");
                        alert.setHeaderText(null);
                        alert.setContentText("发表讨论失败，请检查网络连接或联系管理员");
                        alert.showAndWait();
                    }
                }

                @Override
                protected void failed() {
                    // 显示详细的异常信息
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("发表失败");
                    alert.setHeaderText("发生错误");
                    alert.setContentText("发表讨论失败: " + getException().getMessage());

                    // 添加详细错误信息到对话框
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    getException().printStackTrace(pw);
                    String exceptionText = sw.toString();

                    Label label = new Label("详细错误信息:");
                    TextArea textArea = new TextArea(exceptionText);
                    textArea.setEditable(false);
                    textArea.setWrapText(true);
                    textArea.setMaxWidth(Double.MAX_VALUE);
                    textArea.setMaxHeight(Double.MAX_VALUE);

                    VBox contentBox = new VBox(10);
                    contentBox.getChildren().addAll(label, textArea);
                    alert.getDialogPane().setExpandableContent(contentBox);

                    alert.showAndWait();
                }
            };

            new Thread(postTask).start();
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
        urgentTable.refresh();
        allTable.refresh();

        // 创建主布局
        HBox mainLayout = new HBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #ecf0f1;");

        // 计算统计数据
        int submittedCount = 0;
        int notSubmittedCount = 0;
        int urgentCount = 0;
        int totalCount = assignmentList.size();

        for (Assignment assignment : assignmentList) {
            if ("已提交".equals(assignment.getStatus()) || "已批改".equals(assignment.getStatus())) {
                submittedCount++;
            } else if ("未提交".equals(assignment.getStatus())) {
                notSubmittedCount++;
                if ("紧急".equals(assignment.getPriority())) {
                    urgentCount++;
                }
            }
        }

        // 左侧统计栏 - 修改5: 设置为可调整大小
        VBox statsBox = new VBox(15);
        statsBox.setMinWidth(200);
        statsBox.setStyle("-fx-background-color: #34495e; -fx-padding: 15; -fx-background-radius: 10;");
        VBox.setVgrow(statsBox, Priority.ALWAYS);

        // 创建统计卡片
        VBox submitted = createStatCard("已提交", submittedCount + "个", "#27ae60");
        VBox notSubmitted = createStatCard("未提交", notSubmittedCount + "个", "#e74c3c");
        VBox urgentCard = createStatCard("紧急作业", urgentCount + "个", "#f39c12");
        VBox totalCard = createStatCard("总计", totalCount + "个", "#2c3e50");

        // 进度条
        double progress = totalCount > 0 ? (double) submittedCount / totalCount : 0;
        ProgressBar progressBar = new ProgressBar(progress);
        Label progressLabel = new Label(String.format("提交进度: %.1f%%", progress * 100));
        progressLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        VBox progressBox = new VBox(5);
        progressBox.setAlignment(Pos.CENTER);
        progressBox.setStyle("-fx-background-color: #2c3e50; -fx-padding: 10; -fx-background-radius: 8;");
        progressBar.setPrefWidth(180);
        progressBox.getChildren().addAll(progressLabel, progressBar);

        statsBox.getChildren().addAll(submitted, notSubmitted, urgentCard, totalCard, progressBox);

        // 右侧内容区域
        VBox contentArea = new VBox(20);
        contentArea.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-background-radius: 10;");
        HBox.setHgrow(contentArea, Priority.ALWAYS);
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        // 搜索和筛选区域
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("搜索作业名称或课程...");
        searchField.setStyle("-fx-font-size: 16px;");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button searchBtn = new Button("搜索");
        searchBtn.setStyle("-fx-font-size: 16px; -fx-background-color: #3498db; -fx-text-fill: white;");

        ComboBox<String> filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("全部作业", "已提交", "未提交", "紧急作业");
        filterComboBox.setValue("全部作业");
        filterComboBox.setStyle("-fx-font-size: 16px;");
        filterComboBox.setPrefWidth(120);

        Button filterBtn = new Button("筛选");
        filterBtn.setStyle("-fx-font-size: 16px; -fx-background-color: #2ecc71; -fx-text-fill: white;");


        // 搜索按钮事件
        searchBtn.setOnAction(e -> {
            String keyword = searchField.getText();
            String filter = filterComboBox.getValue();

            try {
                List<Assignment> assignments = clientService.searchAssignments(keyword, filter, currentUser);
                assignmentList.setAll(assignments);
                urgentTable.refresh();
                allTable.refresh();
            } catch (Exception ex) {
                // 错误处理
            }
        });

        // 筛选按钮事件
        filterBtn.setOnAction(e -> {
            String keyword = searchField.getText();
            String filter = filterComboBox.getValue();

            try {
                List<Assignment> assignments = clientService.searchAssignments(keyword, filter, currentUser);
                assignmentList.setAll(assignments);
                urgentTable.refresh();
                allTable.refresh();
            } catch (Exception ex) {
                // 错误处理
            }
        });

        searchBox.getChildren().addAll(searchField, searchBtn, filterComboBox, filterBtn);

        // 紧急作业区域
        Label urgentLabel = new Label("紧急作业（截止日期临近）");
        urgentLabel.setStyle("-fx-font-family: '微软雅黑'; -fx-font-weight: BOLD; -fx-font-size: 20px; -fx-text-fill: #2c3e50;");

        // 使用过滤后的列表
        ObservableList<Assignment> urgentAssignments = assignmentList.filtered(a ->
                "紧急".equals(a.getPriority()) && "未提交".equals(a.getStatus()));
        urgentTable.setItems(urgentAssignments);
        urgentTable.setPrefHeight(120);
        urgentTable.setStyle("-fx-font-size: 16px;");

        // 全部作业区域
        Label allLabel = new Label("全部作业");
        allLabel.setStyle("-fx-font-family: '微软雅黑'; -fx-font-weight: BOLD; -fx-font-size: 20px; -fx-text-fill: #2c3e50;");

        allTable.setItems(assignmentList);
        allTable.setPrefHeight(300);
        allTable.setStyle("-fx-font-size: 16px;");
        VBox.setVgrow(allTable, Priority.ALWAYS);

        contentArea.getChildren().addAll(searchBox, urgentLabel, urgentTable, allLabel, allTable);
        VBox.setVgrow(allTable, Priority.ALWAYS);

        mainLayout.getChildren().addAll(statsBox, contentArea);
        HBox.setHgrow(contentArea, Priority.ALWAYS);
        this.setCenter(mainLayout);
        this.setLeft(null);
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
        valueLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 26));
        valueLabel.setTextFill(Color.WHITE);
        valueLabel.setStyle("-fx-font-size: 26; -fx-font: Microsoft YaHei; -fx-font-weight: BOLD");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font(24));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-font-size: 24; -fx-font: Microsoft YaHei; -fx-font-weight: BOLD");

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
                    private final Button viewFeedbackBtn = new Button("查看批改");
                    private final HBox buttonBox = new HBox(5, submitBtn, viewFeedbackBtn);
                    private final FileChooser fileChooser = new FileChooser();

                    {
                        submitBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                        viewFeedbackBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                        fileChooser.setTitle("选择作业文件");

                        submitBtn.setOnAction(event -> {
                            Assignment assignment = getTableView().getItems().get(getIndex());
                            File file = fileChooser.showOpenDialog(null);
                            if (file != null) {
                                // 使用异步任务处理文件上传
                                Task<Boolean> submitTask = new Task<Boolean>() {
                                    @Override
                                    protected Boolean call() throws Exception {
                                        return clientService.submitAssignment(
                                                assignment.getName(), file.getAbsolutePath(), currentUser);
                                    }

                                    @Override
                                    protected void succeeded() {
                                        boolean success = getValue();
                                        if (success) {
                                            // 更新UI
                                            assignment.setStatus("已提交");
                                            getTableView().refresh();

                                            // 重新加载作业列表
                                            loadAssignmentsFromDatabase();

                                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                            alert.setTitle("提交成功");
                                            alert.setHeaderText(null);
                                            alert.setContentText("作业提交成功！");
                                            alert.showAndWait();
                                        } else {
                                            Alert alert = new Alert(Alert.AlertType.ERROR);
                                            alert.setTitle("提交失败");
                                            alert.setHeaderText(null);
                                            alert.setContentText("作业提交失败");
                                            alert.showAndWait();
                                        }
                                    }

                                    @Override
                                    protected void failed() {
                                        Alert alert = new Alert(Alert.AlertType.ERROR);
                                        alert.setTitle("提交失败");
                                        alert.setHeaderText(null);
                                        alert.setContentText("作业提交失败: " + getException().getMessage());
                                        alert.showAndWait();
                                    }
                                };

                                new Thread(submitTask).start();
                            }
                        });

                        viewFeedbackBtn.setOnAction(event -> {
                            Assignment assignment = getTableView().getItems().get(getIndex());
                            // 使用异步任务获取批改详情
                            Task<Map<String, String>> feedbackTask = new Task<Map<String, String>>() {
                                @Override
                                protected Map<String, String> call() throws Exception {
                                    return clientService.getAssignmentFeedback(
                                            assignment.getName(), currentUser);
                                }

                                @Override
                                protected void succeeded() {
                                    Map<String, String> feedback = getValue();
                                    if (feedback != null && !feedback.isEmpty()) {
                                        // 显示批改详情对话框
                                        showFeedbackDialog(assignment, feedback);
                                    } else {
                                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                        alert.setTitle("批改详情");
                                        alert.setHeaderText(null);
                                        alert.setContentText("暂无批改详情");
                                        alert.showAndWait();
                                    }
                                }

                                @Override
                                protected void failed() {
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle("获取失败");
                                    alert.setHeaderText(null);
                                    alert.setContentText("获取批改详情失败: " + getException().getMessage());
                                    alert.showAndWait();
                                }
                            };

                            new Thread(feedbackTask).start();
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Assignment assignment = getTableView().getItems().get(getIndex());
                            // 根据作业状态显示不同的按钮
                            if ("未提交".equals(assignment.getStatus())) {
                                setGraphic(submitBtn);
                                viewFeedbackBtn.setVisible(false);
                            } else if ("已批改".equals(assignment.getStatus())) {
                                setGraphic(viewFeedbackBtn);
                                submitBtn.setVisible(false);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        };
    }

    /**
     * 显示批改详情对话框
     */
    private void showFeedbackDialog(Assignment assignment, Map<String, String> feedback) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("作业批改详情 - " + assignment.getName());
        dialog.setHeaderText("课程: " + assignment.getCourse());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // 添加批改详情
        grid.add(new Label("作业名称:"), 0, 0);
        grid.add(new Label(assignment.getName()), 1, 0);

        grid.add(new Label("课程:"), 0, 1);
        grid.add(new Label(assignment.getCourse()), 1, 1);

        grid.add(new Label("成绩:"), 0, 2);
        Label scoreLabel = new Label(feedback.get("score"));
        scoreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        grid.add(scoreLabel, 1, 2);

        grid.add(new Label("评语:"), 0, 3);
        TextArea feedbackArea = new TextArea(feedback.get("feedback"));
        feedbackArea.setEditable(false);
        feedbackArea.setWrapText(true);
        feedbackArea.setPrefRowCount(3);
        grid.add(feedbackArea, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
}
