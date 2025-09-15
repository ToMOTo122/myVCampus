package com.vcampus.client.onlineclass;

import com.vcampus.client.service.ClientService;
import com.vcampus.common.entity.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 在线课堂模块 - 教师界面
 */
public class TeacherOnlineClass extends BorderPane {

    private String currentView = "home";
    private Course currentCourse;
    private ClientService clientService;
    private User currentUser;
    private Label detailLabel;
    private ListView<String> scheduleList;
    private LocalDate currentCalendarDate = LocalDate.now();
    private List<Integer> daysWithReminders = new ArrayList<>();
    private Assignment currentAssignment;


    private ObservableList<Course> courseList = FXCollections.observableArrayList();
    private ObservableList<Assignment> assignmentList = FXCollections.observableArrayList();
    private ObservableList<CoursePlayback> playbackList = FXCollections.observableArrayList();
    private ObservableList<CourseMaterial> materialList = FXCollections.observableArrayList();
    private ObservableList<Discussion> discussions = FXCollections.observableArrayList();
    private ObservableList<Assignment> allAssignments = FXCollections.observableArrayList();


    public TeacherOnlineClass(User user, ClientService clientService) {
        this.currentUser = user;
        this.clientService = clientService;

        // 初始化数据
        loadCoursesFromDatabase();
        loadAssignmentsFromDatabase();

        initRootLayout();
        showHomePage();
    }

    // 从数据库加载课程数据
    private void loadCoursesFromDatabase() {
        try {
            List<Course> courses = clientService.getTeacherCourses(currentUser);

            // 为每个课程设置学生人数
            for (Course course : courses) {
                try {
                    List<Student> courseStudents = clientService.getCourseStudents1(course.getCourseId());
                    course.setStudentCount(courseStudents.size());
                } catch (Exception e) {
                    course.setStudentCount(0);
                    System.err.println("获取课程 " + course.getCourseName() + " 的学生人数失败: " + e.getMessage());
                }
            }

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

    // 从数据库加载作业数据
    private void loadAssignmentsFromDatabase() {
        try {
            List<Assignment> assignments = clientService.getTeacherAssignments(currentUser);
            assignmentList.setAll(assignments);
            allAssignments.setAll(assignments); // 保存完整列表用于过滤
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

        Label title = new Label("教师在线课堂");
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
        Label welcomeLabel = new Label("教师工作台 - 欢迎使用在线课堂！");
        welcomeLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 20));
        welcomeBox.getChildren().add(welcomeLabel);

        // 快速入口卡片
        HBox quickAccess = new HBox(15);
        quickAccess.setAlignment(Pos.CENTER);

        VBox scheduleReminderCard = createQuickAccessCard("日程提醒", "查看教学日程", "📅", e -> showSchedulePage());
        VBox scheduleCard = createQuickAccessCard("课程管理", "管理授课课程", "📚", e -> showCoursesPage());
        VBox assignmentsCard = createQuickAccessCard("作业管理", "布置批改作业", "📝", e -> showAssignmentsPage());

        quickAccess.getChildren().addAll(scheduleReminderCard, scheduleCard, assignmentsCard);

        // 待办事项
        VBox todoSection = new VBox(10);
        Label todoLabel = new Label("待办事项");
        todoLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        ListView<String> todoList = new ListView<>();
        try {
            // 从数据库获取待办事项
            List<String> todos = clientService.getTeacherTodos(currentUser);
            todoList.getItems().addAll(todos);
        } catch (Exception e) {
            e.printStackTrace();
            todoList.getItems().add("加载待办事项失败: " + e.getMessage());
        }
        todoList.setPrefHeight(120);

        todoSection.getChildren().addAll(todoLabel, todoList);

        homePage.getChildren().addAll(welcomeBox, quickAccess, todoSection);
        this.setCenter(homePage);
        this.setLeft(null);
    }

    private VBox createQuickAccessCard(String title, String description, String emoji, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
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
        detailLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

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
     * 显示课程管理页面
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

        int totalCourses = courseList.size();
        int notStarted = 0;
        int inProgress = 0;
        int completed = 0;
        int totalStudents = 0;

// 使用当前日期
        Date currentDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (Course course : courseList) {
            try {
                Date startDate = sdf.parse(course.getStartDate());
                Date endDate = sdf.parse(course.getEndDate());

                if (currentDate.before(startDate)) {
                    notStarted++;
                } else if (currentDate.after(endDate)) {
                    completed++;
                } else {
                    inProgress++;
                }
            } catch (ParseException e) {
                e.printStackTrace();
                // 如果解析失败，默认设为进行中
                inProgress++;
            }
            totalStudents += course.getStudentCount();
        }

// 创建统计卡片
        VBox teaching = createStatCard("正在授课", inProgress + "门", "#3498db");
        VBox completedCard = createStatCard("已结课", completed + "门", "#27ae60");
        VBox totalCard = createStatCard("总计", totalCourses + "门", "#2c3e50");
        VBox students = createStatCard("学生总数", totalStudents + "人", "#f39c12");

// 更新statsBox
        statsBox.getChildren().setAll(teaching, completedCard, totalCard, students);

        // 右侧内容区域
        VBox contentArea = new VBox(20);
        contentArea.setPrefWidth(800);

        // 搜索和筛选区域
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("搜索课程名称或班级...");
        searchField.setPrefWidth(300);

        Button searchBtn = new Button("搜索");
        Button addCourseBtn = new Button("新建课程");
        addCourseBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        // 修改搜索按钮事件处理
        searchBtn.setOnAction(e -> {
            try {
                String keyword = searchField.getText();
                List<Course> filteredCourses = clientService.searchTeacherCourses(keyword, currentUser);

                // 为每个课程设置学生人数
                for (Course course : filteredCourses) {
                    try {
                        List<Student> courseStudents = clientService.getCourseStudents1(course.getCourseId());
                        course.setStudentCount(courseStudents.size());
                    } catch (Exception ex) {
                        course.setStudentCount(0);
                        System.err.println("获取课程 " + course.getCourseName() + " 的学生人数失败: " + ex.getMessage());
                    }
                }

                courseList.setAll(filteredCourses);

                // 显示搜索结果信息
                if (filteredCourses.isEmpty()) {
                    showAlert("提示", "没有找到匹配的课程");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("错误", "搜索失败: " + ex.getMessage());
            }
        });

        // 添加新建课程按钮事件
        addCourseBtn.setOnAction(e -> showAddCourseDialog());

        searchBox.getChildren().addAll(searchField, searchBtn, addCourseBtn);

        // 课程表格
        Label tableLabel = new Label("我的课程");
        tableLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        TableView<Course> courseTable = new TableView<>();
        courseTable.setItems(courseList);
        courseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Course, String> nameCol = new TableColumn<>("课程名称");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));

        TableColumn<Course, String> classCol = new TableColumn<>("班级");
        classCol.setCellValueFactory(new PropertyValueFactory<>("className"));

        TableColumn<Course, String> startCol = new TableColumn<>("开课时间");
        startCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));

        TableColumn<Course, String> endCol = new TableColumn<>("结课时间");
        endCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        TableColumn<Course, String> statusCol = new TableColumn<>("课程状态");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Course, Integer> studentsCol = new TableColumn<>("学生人数");
        studentsCol.setCellValueFactory(new PropertyValueFactory<>("studentCount"));

        TableColumn<Course, Void> actionCol = new TableColumn<>("操作");
        actionCol.setCellFactory(createCourseActionCellFactory());

        courseTable.getColumns().addAll(nameCol, classCol, startCol, endCol, statusCol, studentsCol, actionCol);
        courseTable.setPrefHeight(400);

        contentArea.getChildren().addAll(searchBox, tableLabel, courseTable);

        mainLayout.getChildren().addAll(statsBox, contentArea);
        this.setCenter(mainLayout);
        this.setLeft(null);
    }

    // 添加新建课程对话框
    private void showAddCourseDialog() {
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle("新建课程");
        dialog.setHeaderText("请输入课程信息");

        // 创建表单
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("课程名称");

        ComboBox<String> classComboBox = new ComboBox<>();
        try {
            classComboBox.getItems().addAll(clientService.getClassNames());
        } catch (Exception e) {
            e.printStackTrace();
        }
        classComboBox.setPromptText("选择班级");

        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setPromptText("开始日期");

        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPromptText("结束日期");

        TextField creditField = new TextField();
        creditField.setPromptText("学分");

        grid.add(new Label("课程名称:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("班级:"), 0, 1);
        grid.add(classComboBox, 1, 1);
        grid.add(new Label("开始日期:"), 0, 2);
        grid.add(startDatePicker, 1, 2);
        grid.add(new Label("结束日期:"), 0, 3);
        grid.add(endDatePicker, 1, 3);
        grid.add(new Label("学分:"), 0, 4);
        grid.add(creditField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // 添加按钮
        ButtonType addButtonType = new ButtonType("创建", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // 转换结果
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    int credits = Integer.parseInt(creditField.getText());
                    Course newCourse = new Course(
                            nameField.getText(),
                            classComboBox.getValue(),
                            currentUser.getRealName(), // 教师姓名
                            startDatePicker.getValue().toString(),
                            endDatePicker.getValue().toString(),
                            "未开始",
                            credits
                    );
                    newCourse.setCourseId("TEMP_ID"); // 服务器端会重新生成
                    return newCourse;
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("输入错误");
                    alert.setHeaderText(null);
                    alert.setContentText("学分必须是数字");
                    alert.showAndWait();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(course -> {
            try {
                boolean success = clientService.addCourse(course, currentUser);
                if (success) {
                    loadCoursesFromDatabase(); // 刷新课程列表
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("成功");
                    alert.setHeaderText(null);
                    alert.setContentText("课程创建成功");
                    alert.showAndWait();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("创建失败");
                alert.setHeaderText(null);
                alert.setContentText("课程创建失败: " + ex.getMessage());
                alert.showAndWait();
            }
        });
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

        Label courseTitle = new Label(course.getCourseName() + " - " + course.getClassName());
        courseTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(10, 0, 0, 0));

        infoGrid.add(new Label("授课教师:"), 0, 0);
        infoGrid.add(new Label(course.getTeacherName()), 1, 0);

        infoGrid.add(new Label("课程时间:"), 0, 1);
        infoGrid.add(new Label(course.getStartDate() + " 至 " + course.getEndDate()), 1, 1);

        infoGrid.add(new Label("课程状态:"), 0, 2);
        Label statusLabel = new Label(course.getStatus());
        statusLabel.setTextFill(course.getStatus().equals("进行中") ? Color.GREEN : Color.GRAY);
        infoGrid.add(statusLabel, 1, 2);

        infoGrid.add(new Label("学生人数:"), 0, 3);
        infoGrid.add(new Label(String.valueOf(course.getCapacity())), 1, 3);

        courseInfoBox.getChildren().addAll(courseTitle, infoGrid);

        // 功能选项卡
        TabPane tabPane = new TabPane();

        // 课程回放选项卡 - 教师可以添加回放
        Tab playbackTab = new Tab("课程回放管理", createPlaybackManagementContent());
        playbackTab.setClosable(false);

        // 课程资料选项卡 - 教师可以上传资料
        Tab materialsTab = new Tab("课程资料管理", createMaterialsManagementContent());
        materialsTab.setClosable(false);

        // 课程讨论选项卡 - 替换原来的学生管理
        Tab discussionTab = new Tab("课程讨论", createDiscussionContent());
        discussionTab.setClosable(false);

        tabPane.getTabs().addAll(playbackTab, materialsTab, discussionTab);

        detailPage.getChildren().addAll(backButtonBox, courseInfoBox, tabPane);
        this.setCenter(detailPage);
        this.setLeft(null);
    }


    /**
     * 创建课程回放管理内容
     */
    private VBox createPlaybackManagementContent() {
        VBox playbackContent = new VBox(15);
        playbackContent.setPadding(new Insets(15));

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("课程回放列表");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        Button addPlaybackBtn = new Button("添加回放");
        addPlaybackBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        addPlaybackBtn.setOnAction(e -> showAddPlaybackDialog());

        headerBox.getChildren().addAll(titleLabel, addPlaybackBtn);

        TableView<CoursePlayback> playbackTableView = new TableView<>();
        try {
            List<CoursePlayback> playbacks = clientService.getCoursePlaybacks(currentCourse.getCourseId());
            playbackList.setAll(playbacks);
        } catch (Exception e) {
            e.printStackTrace();
        }
        playbackTableView.setItems(playbackList);

        // 添加playback_id列（隐藏）
        TableColumn<CoursePlayback, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("playbackId"));
        idCol.setVisible(false);

        TableColumn<CoursePlayback, String> dateCol = new TableColumn<>("日期");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<CoursePlayback, String> titleCol = new TableColumn<>("标题");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<CoursePlayback, String> durationCol = new TableColumn<>("时长");
        durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));

        TableColumn<CoursePlayback, Void> actionCol = new TableColumn<>("操作");
        actionCol.setCellFactory(param -> new TableCell<CoursePlayback, Void>() {
            private final Button deleteBtn = new Button("删除");

            {
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                deleteBtn.setOnAction(event -> {
                    CoursePlayback playback = getTableView().getItems().get(getIndex());
                    try {
                        boolean success = clientService.deletePlayback(playback.getPlaybackId());
                        if (success) {
                            // 刷新回放列表
                            List<CoursePlayback> playbacks = clientService.getCoursePlaybacks(currentCourse.getCourseId());
                            playbackList.setAll(playbacks);
                            showAlert("成功", "回放删除成功");
                        } else {
                            showAlert("错误", "删除失败: 您没有权限删除此回放");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("错误", "删除失败: " + e.getMessage());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });

        playbackTableView.getColumns().addAll(idCol, dateCol, titleCol, durationCol, actionCol);
        playbackTableView.setPrefHeight(300);

        playbackContent.getChildren().addAll(headerBox, playbackTableView);
        return playbackContent;
    }

    /**
     * 创建课程资料管理内容
     */
    private VBox createMaterialsManagementContent() {
        VBox materialsContent = new VBox(15);
        materialsContent.setPadding(new Insets(15));

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("课程资料列表");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        Button addMaterialBtn = new Button("上传资料");
        addMaterialBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        addMaterialBtn.setOnAction(e -> showAddMaterialDialog());

        headerBox.getChildren().addAll(titleLabel, addMaterialBtn);

        TableView<CourseMaterial> materialsTable = new TableView<>();
        try {
            List<CourseMaterial> materials = clientService.getCourseMaterials(currentCourse.getCourseId());
            materialList.setAll(materials);
        } catch (Exception e) {
            e.printStackTrace();
        }
        materialsTable.setItems(materialList);

        // 添加material_id列（隐藏）
        TableColumn<CourseMaterial, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("materialId"));
        idCol.setVisible(false);

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
            private final Button deleteBtn = new Button("删除");

            {
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                deleteBtn.setOnAction(event -> {
                    CourseMaterial material = getTableView().getItems().get(getIndex());
                    try {
                        boolean success = clientService.deleteMaterial(material.getMaterialId());
                        if (success) {
                            // 刷新资料列表
                            List<CourseMaterial> materials = clientService.getCourseMaterials(currentCourse.getCourseId());
                            materialList.setAll(materials);
                            showAlert("成功", "资料删除成功");
                        } else {
                            showAlert("错误", "删除失败: 您没有权限删除此资料");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("错误", "删除失败: " + e.getMessage());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });

        materialsTable.getColumns().addAll(idCol, nameCol, typeCol, sizeCol, dateCol, actionCol);
        materialsTable.setPrefHeight(300);

        materialsContent.getChildren().addAll(headerBox, materialsTable);
        return materialsContent;
    }

    /**
     * 创建课程讨论内容
     */
    private VBox createDiscussionContent() {
        VBox discussionContent = new VBox(15);
        discussionContent.setPadding(new Insets(15));

        Label titleLabel = new Label("课程讨论区");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        // 讨论列表
        ListView<Discussion> discussionListView = new ListView<>();
        try {
            List<Discussion> discussionList = clientService.getCourseDiscussions(currentCourse.getCourseId());
            discussions.setAll(discussionList);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("错误", "加载讨论失败: " + e.getMessage());
        }

        discussionListView.setItems(discussions);
        discussionListView.setCellFactory(param -> new ListCell<Discussion>() {
            @Override
            protected void updateItem(Discussion item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox container = new VBox(5);
                    HBox header = new HBox(5);

                    Label nameLabel = new Label(item.getUserName() + " (" + item.getUserRole() + ")");
                    Label timeLabel = new Label(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(item.getPostTime()));
                    Label contentLabel = new Label(item.getContent());

                    contentLabel.setWrapText(true);

                    header.getChildren().addAll(nameLabel, timeLabel);
                    container.getChildren().addAll(header, contentLabel);

                    // 添加回复按钮
                    Button replyBtn = new Button("回复");
                    replyBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                    replyBtn.setOnAction(e -> showReplyDialog(item));
                    container.getChildren().add(replyBtn);

                    setGraphic(container);
                }
            }
        });

        discussionListView.setPrefHeight(300);

        // 发表新讨论
        TextArea newPostArea = new TextArea();
        newPostArea.setPromptText("发表新的讨论...");
        newPostArea.setPrefHeight(100);

        Button postButton = new Button("发表");
        postButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        postButton.setOnAction(e -> {
            String content = newPostArea.getText();
            if (content.isEmpty()) {
                showAlert("警告", "请输入讨论内容");
                return;
            }

            try {
                boolean success = clientService.postDiscussion(currentCourse.getCourseName(), content, currentUser);
                if (success) {
                    // 刷新讨论列表
                    List<Discussion> discussionList = clientService.getCourseDiscussions(currentCourse.getCourseId());
                    discussions.setAll(discussionList);
                    newPostArea.clear();
                    showAlert("成功", "讨论发表成功");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("错误", "发表失败: " + ex.getMessage());
            }
        });

        discussionContent.getChildren().addAll(titleLabel, discussionListView, newPostArea, postButton);
        return discussionContent;
    }

    /**
     * 显示回复对话框
     */
    private void showReplyDialog(Discussion parentDiscussion) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("回复讨论");
        dialog.setHeaderText("回复: " + parentDiscussion.getUserName());

        TextArea replyArea = new TextArea();
        replyArea.setPromptText("输入回复内容...");
        replyArea.setPrefRowCount(3);

        VBox content = new VBox(10);
        content.getChildren().addAll(new Label("回复内容:"), replyArea);
        dialog.getDialogPane().setContent(content);

        ButtonType replyButtonType = new ButtonType("回复", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(replyButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == replyButtonType) {
                return replyArea.getText();
            }
            return null;
        });

        // 添加回复逻辑
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(replyContent -> {
            try {
                boolean success = clientService.replyToDiscussion(
                        currentCourse.getCourseId(),
                        parentDiscussion.getDiscussionId(),
                        replyContent
                );
                if (success) {
                    // 刷新讨论列表
                    List<Discussion> discussionList = clientService.getCourseDiscussions(currentCourse.getCourseId());
                    discussions.setAll(discussionList);
                    showAlert("成功", "回复成功");
                } else {
                    showAlert("错误", "回复失败");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("错误", "回复失败: " + ex.getMessage());
            }
        });
    }

    /**
     * 显示添加回放对话框
     */
    private void showAddPlaybackDialog() {
        Dialog<CoursePlayback> dialog = new Dialog<>();
        dialog.setTitle("添加课程回放");
        dialog.setHeaderText("请输入回放信息");

        // 创建表单
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("选择日期");
        datePicker.setValue(LocalDate.now()); // 默认选择今天

        TextField titleField = new TextField();
        titleField.setPromptText("回放标题");

        TextField durationField = new TextField();
        durationField.setPromptText("时长（分钟）");

        // 添加视频文件选择
        Label fileLabel = new Label("未选择文件");
        Button selectFileBtn = new Button("选择视频文件");
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("视频文件", "*.mp4", "*.avi", "*.mov", "*.wmv"),
                new FileChooser.ExtensionFilter("所有文件", "*.*")
        );

        selectFileBtn.setOnAction(e -> {
            File file = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                fileLabel.setText(file.getName());
                fileLabel.setUserData(file); // 存储文件对象
            }
        });

        grid.add(new Label("日期:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("标题:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("时长:"), 0, 2);
        grid.add(durationField, 1, 2);
        grid.add(new Label("视频文件:"), 0, 3);
        grid.add(selectFileBtn, 1, 3);
        grid.add(fileLabel, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // 添加按钮
        ButtonType addButtonType = new ButtonType("添加", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // 转换结果
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                // 验证输入
                if (titleField.getText().isEmpty()) {
                    showAlert("错误", "请输入回放标题");
                    return null;
                }
                if (durationField.getText().isEmpty()) {
                    showAlert("错误", "请输入回放时长");
                    return null;
                }
                if (datePicker.getValue() == null) {
                    showAlert("错误", "请选择日期");
                    return null;
                }

                // 创建回放对象
                CoursePlayback playback = new CoursePlayback(
                        datePicker.getValue().toString(),
                        titleField.getText(),
                        durationField.getText() + "分钟"
                );

                // 设置文件路径（如果有）
                if (fileLabel.getUserData() != null) {
                    File selectedFile = (File) fileLabel.getUserData();
                    playback.setVideoPath(selectedFile.getAbsolutePath());
                }

                return playback;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(playback -> {
            try {
                boolean success = clientService.addPlayback(currentCourse.getCourseId(), playback, currentUser);
                if (success) {
                    // 刷新回放列表
                    List<CoursePlayback> playbacks = clientService.getCoursePlaybacks(currentCourse.getCourseId());
                    playbackList.setAll(playbacks);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("成功");
                    alert.setHeaderText(null);
                    alert.setContentText("回放添加成功");
                    alert.showAndWait();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("添加失败");
                alert.setHeaderText(null);
                alert.setContentText("回放添加失败: " + ex.getMessage());
                alert.showAndWait();
            }
        });
    }

    /**
     * 显示添加资料对话框
     */
    private void showAddMaterialDialog() {
        Dialog<CourseMaterial> dialog = new Dialog<>();
        dialog.setTitle("上传课程资料");
        dialog.setHeaderText("请输入资料信息");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("资料名称");

        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("PDF文档", "Word文档", "PPT课件", "视频", "其他");
        typeComboBox.setValue("PDF文档");

        // 添加文件选择
        Label fileLabel = new Label("未选择文件");
        Button selectFileBtn = new Button("选择文件");
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有文件", "*.*"),
                new FileChooser.ExtensionFilter("PDF文档", "*.pdf"),
                new FileChooser.ExtensionFilter("Word文档", "*.doc", "*.docx"),
                new FileChooser.ExtensionFilter("PPT课件", "*.ppt", "*.pptx"),
                new FileChooser.ExtensionFilter("视频文件", "*.mp4", "*.avi", "*.mov")
        );

        selectFileBtn.setOnAction(e -> {
            File file = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                fileLabel.setText(file.getName());
                fileLabel.setUserData(file); // 存储文件对象

                // 自动设置资料名称和类型
                if (nameField.getText().isEmpty()) {
                    nameField.setText(file.getName().replaceFirst("[.][^.]+$", ""));
                }

                // 根据文件扩展名自动设置类型
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".pdf")) {
                    typeComboBox.setValue("PDF文档");
                } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
                    typeComboBox.setValue("Word文档");
                } else if (fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) {
                    typeComboBox.setValue("PPT课件");
                } else if (fileName.endsWith(".mp4") || fileName.endsWith(".avi") ||
                        fileName.endsWith(".mov") || fileName.endsWith(".wmv")) {
                    typeComboBox.setValue("视频");
                }
            }
        });

        grid.add(new Label("名称:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("类型:"), 0, 1);
        grid.add(typeComboBox, 1, 1);
        grid.add(new Label("文件:"), 0, 2);
        grid.add(selectFileBtn, 1, 2);
        grid.add(fileLabel, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType addButtonType = new ButtonType("上传", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                // 验证输入
                if (nameField.getText().isEmpty()) {
                    showAlert("错误", "请输入资料名称");
                    return null;
                }
                if (fileLabel.getUserData() == null) {
                    showAlert("错误", "请选择文件");
                    return null;
                }

                File selectedFile = (File) fileLabel.getUserData();
                long fileSize = selectedFile.length();
                String sizeText;

                if (fileSize < 1024) {
                    sizeText = fileSize + "B";
                } else if (fileSize < 1024 * 1024) {
                    sizeText = String.format("%.1fKB", fileSize / 1024.0);
                } else {
                    sizeText = String.format("%.1fMB", fileSize / (1024.0 * 1024.0));
                }

                return new CourseMaterial(
                        nameField.getText(),
                        typeComboBox.getValue(),
                        sizeText,
                        LocalDate.now().toString(),
                        selectedFile.getAbsolutePath()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(material -> {
            try {
                boolean success = clientService.addMaterial(currentCourse.getCourseId(), material, currentUser);
                if (success) {
                    // 刷新资料列表
                    List<CourseMaterial> materials = clientService.getCourseMaterials(currentCourse.getCourseId());
                    materialList.setAll(materials);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("成功");
                    alert.setHeaderText(null);
                    alert.setContentText("资料上传成功");
                    alert.showAndWait();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("上传失败");
                alert.setHeaderText(null);
                alert.setContentText("资料上传失败: " + ex.getMessage());
                alert.showAndWait();
            }
        });
    }

    /**
     * 显示作业管理页面
     */
    private void showAssignmentsPage() {
        currentView = "assignments";

        VBox assignmentsPage = new VBox(20);
        assignmentsPage.setPadding(new Insets(20));
        assignmentsPage.setStyle("-fx-background-color: #ecf0f1;");

        // 标题和添加按钮
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("作业管理");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 20));

        Button addAssignmentBtn = new Button("布置新作业");
        addAssignmentBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        addAssignmentBtn.setOnAction(e -> showAddAssignmentDialog());

        // 添加统计信息卡片 - 修改为从assignmentList计算
        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER_LEFT);

        // 从assignmentList计算统计数据
        int totalAssignments = assignmentList.size();
        int notSubmitted = (int) assignmentList.stream()
                .filter(a -> "待批改".equals(a.getStatus()))
                .count();
        int submitted = totalAssignments - notSubmitted;

        VBox pendingCard = createAssignmentStatCard("待批改作业", String.valueOf(notSubmitted), "#e74c3c", "📝");
        VBox totalCard = createAssignmentStatCard("总作业数", String.valueOf(totalAssignments), "#3498db", "📚");
        VBox completedCard = createAssignmentStatCard("已批改", String.valueOf(submitted), "#27ae60", "✅");

        statsBox.getChildren().addAll(pendingCard, totalCard, completedCard);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(titleLabel, spacer, addAssignmentBtn);

        // 搜索和筛选区域
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(10, 0, 10, 0));

        TextField searchField = new TextField();
        searchField.setPromptText("搜索作业名称...");
        searchField.setPrefWidth(200);

        ComboBox<String> courseFilter = new ComboBox<>();
        courseFilter.getItems().add("所有课程");

        // 从assignmentList获取课程列表
        List<String> courseNames = assignmentList.stream()
                .map(Assignment::getCourse)
                .distinct()
                .collect(Collectors.toList());
        courseFilter.getItems().addAll(courseNames);
        courseFilter.setValue("所有课程");
        courseFilter.setPrefWidth(120);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("所有状态", "待批改", "已批改");
        statusFilter.setValue("所有状态");
        statusFilter.setPrefWidth(100);

        Button searchBtn = new Button("搜索");
        searchBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

        // 修改搜索按钮事件处理 - 使用本地过滤
        searchBtn.setOnAction(e -> {
            String keyword = searchField.getText().toLowerCase();
            String courseFilterValue = courseFilter.getValue();
            String statusFilterValue = statusFilter.getValue();

            // 本地过滤
            List<Assignment> filteredAssignments = allAssignments.stream()
                    .filter(assignment ->
                            (keyword.isEmpty() || assignment.getName().toLowerCase().contains(keyword)) &&
                                    ("所有课程".equals(courseFilterValue) || assignment.getCourse().equals(courseFilterValue)) &&
                                    ("所有状态".equals(statusFilterValue) || assignment.getStatus().equals(statusFilterValue))
                    )
                    .collect(Collectors.toList());

            assignmentList.setAll(filteredAssignments);

            // 更新统计信息
            int filteredTotal = filteredAssignments.size();
            int filteredNotSubmitted = (int) filteredAssignments.stream()
                    .filter(a -> "待批改".equals(a.getStatus()))
                    .count();
            int filteredSubmitted = filteredTotal - filteredNotSubmitted;

            // 更新统计卡片
            statsBox.getChildren().clear();
            VBox filteredPendingCard = createAssignmentStatCard("待批改作业", String.valueOf(filteredNotSubmitted), "#e74c3c", "📝");
            VBox filteredTotalCard = createAssignmentStatCard("总作业数", String.valueOf(filteredTotal), "#3498db", "📚");
            VBox filteredCompletedCard = createAssignmentStatCard("已批改", String.valueOf(filteredSubmitted), "#27ae60", "✅");
            statsBox.getChildren().addAll(filteredPendingCard, filteredTotalCard, filteredCompletedCard);
        });

        filterBox.getChildren().addAll(
                new Label("搜索:"), searchField,
                new Label("课程:"), courseFilter,
                new Label("状态:"), statusFilter,
                searchBtn
        );

        // 作业表格
        TableView<Assignment> assignmentTable = new TableView<>();
        assignmentTable.setItems(assignmentList);
        assignmentTable.setStyle("-fx-background-color: white; -fx-border-radius: 5;");

        TableColumn<Assignment, String> nameCol = new TableColumn<>("作业名称");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(column -> new TableCell<Assignment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    // 根据状态添加图标
                    Assignment assignment = getTableView().getItems().get(getIndex());
                    if ("待批改".equals(assignment.getStatus())) {
                        setGraphic(new Label("⏰"));
                    } else {
                        setGraphic(new Label("✅"));
                    }
                }
            }
        });

        TableColumn<Assignment, String> courseCol = new TableColumn<>("所属课程");
        courseCol.setCellValueFactory(new PropertyValueFactory<>("course"));

        TableColumn<Assignment, String> assignDateCol = new TableColumn<>("布置日期");
        assignDateCol.setCellValueFactory(new PropertyValueFactory<>("assignDate"));

        TableColumn<Assignment, String> dueDateCol = new TableColumn<>("截止日期");
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        dueDateCol.setCellFactory(column -> new TableCell<Assignment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // 截止日期临近的显示为红色
                    try {
                        LocalDate dueDate = LocalDate.parse(item);
                        if (dueDate.isBefore(LocalDate.now().plusDays(3))) {
                            setTextFill(Color.RED);
                            setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 12));
                        } else {
                            setTextFill(Color.BLACK);
                        }
                    } catch (Exception e) {
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });

        TableColumn<Assignment, String> statusCol = new TableColumn<>("批改状态");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(column -> new TableCell<Assignment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("待批改".equals(item)) {
                        setTextFill(Color.RED);
                        setStyle("-fx-font-weight: bold;");
                    } else {
                        setTextFill(Color.GREEN);
                    }
                }
            }
        });

        TableColumn<Assignment, String> submissionCol = new TableColumn<>("提交情况");
        submissionCol.setCellValueFactory(new PropertyValueFactory<>("submissionInfo"));
        submissionCol.setCellFactory(column -> new TableCell<Assignment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // 解析提交比例
                    if (item.contains("/")) {
                        String[] parts = item.split("/");
                        if (parts.length == 2) {
                            try {
                                int submitted = Integer.parseInt(parts[0].replaceAll("\\D", ""));
                                int total = Integer.parseInt(parts[1].replaceAll("\\D", ""));
                                double ratio = (double) submitted / total;
                                if (ratio == 0) {
                                    setTextFill(Color.RED);
                                } else if (ratio < 1.0) {
                                    setTextFill(Color.ORANGE);
                                } else {
                                    setTextFill(Color.GREEN);
                                }
                            } catch (NumberFormatException e) {
                                setTextFill(Color.BLACK);
                            }
                        }
                    }
                }
            }
        });

        TableColumn<Assignment, Void> actionCol = new TableColumn<>("操作");
        actionCol.setCellFactory(param -> new TableCell<Assignment, Void>() {
            private final Button gradeBtn = new Button("批改作业");
            private final Button viewBtn = new Button("查看详情");
            private final HBox buttonBox = new HBox(5, gradeBtn, viewBtn);

            {
                gradeBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px;");
                viewBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 12px;");

                gradeBtn.setOnAction(event -> {
                    currentAssignment = getTableView().getItems().get(getIndex());
                    showGradingPage(currentAssignment);
                });

                viewBtn.setOnAction(event -> {
                    Assignment assignment = getTableView().getItems().get(getIndex());
                    showAssignmentDetailDialog(assignment);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });

        assignmentTable.getColumns().addAll(nameCol, courseCol, assignDateCol, dueDateCol, statusCol, submissionCol, actionCol);
        assignmentTable.setPrefHeight(400);

        // 添加底部统计信息
        HBox summaryBox = new HBox(20);
        summaryBox.setAlignment(Pos.CENTER_LEFT);
        summaryBox.setPadding(new Insets(10));
        summaryBox.setStyle("-fx-background-color: #dfe6e9; -fx-background-radius: 5;");

        Label summaryLabel = new Label("作业统计: ");
        summaryLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));

        long overdueCount = assignmentList.stream()
                .filter(a -> {
                    try {
                        return LocalDate.parse(a.getDueDate()).isBefore(LocalDate.now()) &&
                                "待批改".equals(a.getStatus());
                    } catch (Exception ex) {
                        return false;
                    }
                })
                .count();

        Label overdueLabel = new Label("逾期未批: " + overdueCount + "个");
        overdueLabel.setTextFill(Color.RED);
        overdueLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 12));

        summaryBox.getChildren().addAll(summaryLabel, overdueLabel);

        assignmentsPage.getChildren().addAll(
                headerBox,
                statsBox,
                new Separator(),
                filterBox,
                assignmentTable,
                summaryBox
        );
        this.setCenter(assignmentsPage);
        this.setLeft(null);
    }

    /**
     * 创建作业统计卡片
     */
    private VBox createAssignmentStatCard(String title, String value, String color, String emoji) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10;");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(120);

        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font(20));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.WHITE);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Microsoft YaHei", 12));
        titleLabel.setTextFill(Color.WHITE);

        card.getChildren().addAll(emojiLabel, valueLabel, titleLabel);
        return card;
    }

    /**
     * 显示作业详情对话框
     */
    private void showAssignmentDetailDialog(Assignment assignment) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("作业详情");
        dialog.setHeaderText(assignment.getName());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("课程:"), 0, 0);
        grid.add(new Label(assignment.getCourse()), 1, 0);

        grid.add(new Label("布置日期:"), 0, 1);
        grid.add(new Label(assignment.getAssignDate()), 1, 1);

        grid.add(new Label("截止日期:"), 0, 2);
        grid.add(new Label(assignment.getDueDate()), 1, 2);

        grid.add(new Label("状态:"), 0, 3);
        Label statusLabel = new Label(assignment.getStatus());
        statusLabel.setTextFill("待批改".equals(assignment.getSubmissionInfo()) ? Color.RED : Color.GREEN);
        grid.add(statusLabel, 1, 3);

        grid.add(new Label("提交情况:"), 0, 4);
        grid.add(new Label(assignment.getStatus()), 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    /**
     * 显示添加作业对话框
     */
    private void showAddAssignmentDialog() {
        Dialog<Assignment> dialog = new Dialog<>();
        dialog.setTitle("布置新作业");
        dialog.setHeaderText("请输入作业信息");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("作业名称");

        // 使用课程下拉框替代文本输入
        ComboBox<Course> courseComboBox = new ComboBox<>();
        try {
            List<Course> courses = clientService.getTeacherCoursesForAssignment(currentUser);
            courseComboBox.setItems(FXCollections.observableArrayList(courses));
            courseComboBox.setCellFactory(param -> new ListCell<Course>() {
                @Override
                protected void updateItem(Course item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getCourseName());
                    }
                }
            });
            courseComboBox.setButtonCell(new ListCell<Course>() {
                @Override
                protected void updateItem(Course item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getCourseName());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        DatePicker assignDatePicker = new DatePicker(LocalDate.now());
        DatePicker dueDatePicker = new DatePicker(LocalDate.now().plusDays(7));

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("作业描述");
        descriptionArea.setPrefRowCount(3);

        grid.add(new Label("作业名称:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("所属课程:"), 0, 1);
        grid.add(courseComboBox, 1, 1);
        grid.add(new Label("布置日期:"), 0, 2);
        grid.add(assignDatePicker, 1, 2);
        grid.add(new Label("截止日期:"), 0, 3);
        grid.add(dueDatePicker, 1, 3);
        grid.add(new Label("作业描述:"), 0, 4);
        grid.add(descriptionArea, 1, 4);

        dialog.getDialogPane().setContent(grid);

        ButtonType addButtonType = new ButtonType("布置", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Course selectedCourse = courseComboBox.getValue();
                if (selectedCourse == null) {
                    showAlert("错误", "请选择课程");
                    return null;
                }

                return new Assignment(
                        selectedCourse.getCourseId(),
                        nameField.getText(),
                        assignDatePicker.getValue().toString(),
                        dueDatePicker.getValue().toString(),
                        descriptionArea.getText()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(assignment -> {
            try {
                boolean success = clientService.publishAssignment(assignment);
                if (success) {
                    showAlert("成功", "作业发布成功");
                    // 重新加载作业数据
                    loadAssignmentsFromDatabase();
                    // 刷新页面
                    showAssignmentsPage();
                } else {
                    showAlert("错误", "作业发布失败");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                // 记录详细的错误信息到日志
                String errorType;
                if (ex instanceof java.sql.SQLException) {
                    errorType = "数据库错误";
                    System.err.println("数据库连接或操作失败: " + ex.getMessage());
                    // 如果是SQLException，可以获取更多的SQL状态信息
                    java.sql.SQLException sqlEx = (java.sql.SQLException) ex;
                    System.err.println("SQL状态: " + sqlEx.getSQLState() + ", 错误代码: " + sqlEx.getErrorCode());
                } else if (ex instanceof java.io.IOException || ex instanceof java.net.SocketException) {
                    errorType = "网络连接错误";
                    System.err.println("网络通信失败: " + ex.getMessage());
                } else {
                    errorType = "未知错误";
                    System.err.println("未知错误类型: " + ex.getClass().getName() + ", 消息: " + ex.getMessage());
                }

                // 显示用户友好的错误信息
                showAlert("错误", "作业发布失败: " + errorType + " - " + ex.getMessage());
            }
        });
    }
    /**
     * 显示批改作业页面
     */
    private void showGradingPage(Assignment assignment) {
        VBox gradingPage = new VBox(20);
        gradingPage.setPadding(new Insets(20));
        gradingPage.setStyle("-fx-background-color: #ecf0f1;");

        // 返回按钮
        HBox backButtonBox = new HBox();
        backButtonBox.setAlignment(Pos.CENTER_LEFT);
        Button backButton = new Button("← 返回作业列表");
        backButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        backButton.setOnAction(e -> showAssignmentsPage());
        backButtonBox.getChildren().add(backButton);

        // 作业信息
        VBox assignmentInfoBox = new VBox(10);
        assignmentInfoBox.setPadding(new Insets(15));
        assignmentInfoBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label assignmentTitle = new Label(assignment.getName());
        assignmentTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 20));

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(10, 0, 0, 0));

        infoGrid.add(new Label("所属课程:"), 0, 0);
        infoGrid.add(new Label(assignment.getCourse()), 1, 0);

        infoGrid.add(new Label("布置日期:"), 0, 1);
        infoGrid.add(new Label(assignment.getAssignDate()), 1, 1);

        infoGrid.add(new Label("截止日期:"), 0, 2);
        infoGrid.add(new Label(assignment.getDueDate()), 1, 2);

        infoGrid.add(new Label("提交情况:"), 0, 3);
        infoGrid.add(new Label(assignment.getSubmissionInfo()), 1, 3);

        infoGrid.add(new Label("作业状态:"), 0, 4);
        Label statusLabel = new Label(assignment.getStatus());
        if ("已提交".equals(assignment.getStatus())) {
            statusLabel.setTextFill(Color.GREEN);
        } else if ("部分提交".equals(assignment.getStatus())) {
            statusLabel.setTextFill(Color.ORANGE);
        } else {
            statusLabel.setTextFill(Color.RED);
        }
        infoGrid.add(statusLabel, 1, 4);

        assignmentInfoBox.getChildren().addAll(assignmentTitle, infoGrid);

        // 学生作业列表
        VBox studentWorkBox = new VBox(10);
        String submissionInfo = assignment.getSubmissionInfo();
        String totalStudents = "0";
        if (submissionInfo != null && submissionInfo.contains("/")) {
            String[] parts = submissionInfo.split("/");
            if (parts.length >= 2) {
                totalStudents = parts[1];
            }
        }

        Label studentWorkLabel = new Label("学生作业列表 - 共" + totalStudents + "名学生");
        studentWorkLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        // 获取学生作业数据 - 修复：使用正确的assignmentId
        ObservableList<StudentAssignment> studentWorkList = FXCollections.observableArrayList();
        try {
            List<StudentAssignment> assignments = clientService.getStudentAssignmentsForTeacher(assignment.getAssignmentId());
            studentWorkList.setAll(assignments);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("错误", "加载学生作业失败: " + e.getMessage());
        }

        TableView<StudentAssignment> studentWorkTable = new TableView<>();
        studentWorkTable.setItems(studentWorkList);

        // 修复表格列定义
        TableColumn<StudentAssignment, String> idCol = new TableColumn<>("学号");
        idCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));

        TableColumn<StudentAssignment, String> nameCol = new TableColumn<>("姓名");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));

        TableColumn<StudentAssignment, String> classCol = new TableColumn<>("班级");
        classCol.setCellValueFactory(new PropertyValueFactory<>("className"));

        TableColumn<StudentAssignment, String> statusCol = new TableColumn<>("提交状态");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(column -> new TableCell<StudentAssignment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("已提交".equals(item)) {
                        setTextFill(Color.GREEN);
                    } else {
                        setTextFill(Color.RED);
                    }
                }
            }
        });

        TableColumn<StudentAssignment, Timestamp> submitDateCol = new TableColumn<>("提交时间");
        submitDateCol.setCellValueFactory(new PropertyValueFactory<>("submitTime"));
        submitDateCol.setCellFactory(column -> new TableCell<StudentAssignment, Timestamp>() {
            @Override
            protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
                    setText(sdf.format(item));
                }
            }
        });

        TableColumn<StudentAssignment, Integer> scoreCol = new TableColumn<>("成绩");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
        scoreCol.setCellFactory(column -> new TableCell<StudentAssignment, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    if (item >= 90) {
                        setTextFill(Color.GREEN);
                    } else if (item >= 60) {
                        setTextFill(Color.ORANGE);
                    } else {
                        setTextFill(Color.RED);
                    }
                }
            }
        });

        TableColumn<StudentAssignment, String> feedbackCol = new TableColumn<>("评语");
        feedbackCol.setCellValueFactory(new PropertyValueFactory<>("feedback"));

        TableColumn<StudentAssignment, Void> actionCol = new TableColumn<>("操作");
        actionCol.setCellFactory(param -> new TableCell<StudentAssignment, Void>() {
            private final Button gradeBtn = new Button("批改");

            {
                gradeBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                gradeBtn.setOnAction(event -> {
                    StudentAssignment work = getTableView().getItems().get(getIndex());
                    if ("已提交".equals(work.getStatus())) {
                        showGradeStudentWorkDialog(work);
                    } else {
                        showAlert("提示", "该学生尚未提交作业，无法批改。");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    StudentAssignment work = getTableView().getItems().get(getIndex());
                    if ("已提交".equals(work.getStatus())) {
                        setGraphic(gradeBtn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        studentWorkTable.getColumns().addAll(idCol, nameCol, classCol, statusCol, submitDateCol, scoreCol, feedbackCol, actionCol);
        studentWorkTable.setPrefHeight(400);

        studentWorkBox.getChildren().addAll(studentWorkLabel, studentWorkTable);

        gradingPage.getChildren().addAll(backButtonBox, assignmentInfoBox, studentWorkBox);
        this.setCenter(gradingPage);
        this.setLeft(null);
    }

    /**
     * 显示批改学生作业对话框
     */
    private void showGradeStudentWorkDialog(StudentAssignment work) {
        Dialog<StudentAssignment> dialog = new Dialog<>();
        dialog.setTitle("批改作业");
        dialog.setHeaderText("批改学生: " + work.getStudentName() + " (" + work.getStudentId() + ")");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField scoreField = new TextField();
        scoreField.setPromptText("请输入成绩（0-100）");

        TextArea feedbackArea = new TextArea();
        feedbackArea.setPromptText("请输入评语");
        feedbackArea.setPrefRowCount(3);

        grid.add(new Label("成绩:"), 0, 0);
        grid.add(scoreField, 1, 0);
        grid.add(new Label("评语:"), 0, 1);
        grid.add(feedbackArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        ButtonType gradeButtonType = new ButtonType("提交批改", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(gradeButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == gradeButtonType) {
                try {
                    int score = Integer.parseInt(scoreField.getText());
                    if (score < 0 || score > 100) {
                        showAlert("错误", "成绩必须在0-100之间");
                        return null;
                    }

                    boolean success = clientService.gradeAssignment(
                            work.getStudentId(),
                            work.getAssignmentId(),
                            score,
                            feedbackArea.getText()
                    );

                    if (success) {
                        showAlert("成功", "批改成功");
                        work.setScore(score);
                        work.setFeedback(feedbackArea.getText());
                        work.setStatus("已批改");
                        return work;
                    } else {
                        showAlert("错误", "批改失败");
                    }
                } catch (NumberFormatException e) {
                    showAlert("错误", "请输入有效的成绩数字");
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("错误", "批改失败: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            // 刷新表格
            showGradingPage(currentAssignment);
        });
    }

    /**
     * 创建统计卡片
     */
    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10;");
        card.setAlignment(Pos.CENTER);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 20));
        valueLabel.setTextFill(Color.WHITE);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Microsoft YaHei", 12));
        titleLabel.setTextFill(Color.WHITE);

        card.getChildren().addAll(valueLabel, titleLabel);
        return card;
    }

    /**
     * 创建课程操作列
     */
    private Callback<TableColumn<Course, Void>, TableCell<Course, Void>> createCourseActionCellFactory() {
        return new Callback<TableColumn<Course, Void>, TableCell<Course, Void>>() {
            @Override
            public TableCell<Course, Void> call(final TableColumn<Course, Void> param) {
                return new TableCell<Course, Void>() {
                    private final Button detailBtn = new Button("查看详情");

                    {
                        detailBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
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
                            setGraphic(detailBtn);
                        }
                    }
                };
            }
        };
    }

    /**
     * 显示警告对话框
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}