package com.vcampus.client.onlineclass;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;

import java.time.LocalDate;

/**
 * 在线课堂模块 - 教师界面
 */
public class TeacherOnlineClass extends BorderPane {

    private String currentView = "home";
    private Course currentCourse; // 当前选中的课程

    // 模拟数据
    private ObservableList<Course> courseList = FXCollections.observableArrayList(
            new Course("高等数学", "数学1班", "张教授", "2024-09-01", "2025-01-15", "进行中", 60),
            new Course("大学英语", "英语2班", "李教授", "2024-09-01", "2025-01-15", "进行中", 45),
            new Course("计算机基础", "计算机3班", "王教授", "2024-09-01", "2025-01-15", "进行中", 30)
    );

    private ObservableList<Assignment> assignmentList = FXCollections.observableArrayList(
            new Assignment("微积分作业1", "高等数学", "2024-09-05", "2024-09-12", "待批改", "已提交: 45/60"),
            new Assignment("英语作文", "大学英语", "2024-09-06", "2024-09-20", "待批改", "已提交: 30/45"),
            new Assignment("编程练习", "计算机基础", "2024-09-07", "2024-09-14", "已批改", "已提交: 25/30")
    );

    // 课程回放数据
    private ObservableList<CoursePlayback> playbackList = FXCollections.observableArrayList();

    // 课程资料数据
    private ObservableList<CourseMaterial> materialList = FXCollections.observableArrayList();

    public TeacherOnlineClass() {
        // 初始化回放数据
        initializePlaybackList();
        initRootLayout();
        showHomePage();
    }

    /**
     * 初始化回放数据
     */
    private void initializePlaybackList() {
        playbackList.addAll(
                new CoursePlayback("2024-09-02", "微积分简介与极限概念", "90分钟"),
                new CoursePlayback("2024-09-04", "导数的定义与计算", "90分钟"),
                new CoursePlayback("2024-09-06", "导数的应用", "90分钟")
        );
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
        todoList.getItems().addAll(
                "待批改作业: 微积分作业1 (45份)",
                "待批改作业: 英语作文 (30份)",
                "课程回放待上传: 高等数学第4讲",
                "新学生提问: 关于导数的应用"
        );
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

        Label monthLabel = new Label("2025年 九月");
        monthLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));

        HBox monthNav = new HBox(10);
        monthNav.setAlignment(Pos.CENTER);
        Button prevMonth = new Button("上月");
        Button todayBtn = new Button("今天");
        Button nextMonth = new Button("下月");
        monthNav.getChildren().addAll(prevMonth, todayBtn, nextMonth);

        // 日历网格
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

        // 添加日期（简化版本）
        int day = 1;
        for (int week = 1; week <= 5; week++) { // 减少到5周，避免过多空白
            for (int d = 0; d < 7; d++) {
                if (day <= 30) {
                    StackPane dayCell = new StackPane();
                    dayCell.setPrefSize(40, 40);
                    dayCell.setStyle("-fx-border-color: #bdc3c7; -fx-border-radius: 3;");

                    Label dateLabel = new Label(String.valueOf(day));
                    dateLabel.setAlignment(Pos.CENTER);

                    if (day == 12) {
                        dayCell.setStyle("-fx-background-color: #3498db; -fx-border-radius: 3;");
                        dateLabel.setTextFill(Color.WHITE);
                    }

                    dayCell.getChildren().add(dateLabel);
                    calendarGrid.add(dayCell, d, week);
                    day++;
                } else {
                    // 添加空单元格填充剩余位置
                    StackPane emptyCell = new StackPane();
                    emptyCell.setPrefSize(40, 40);
                    calendarGrid.add(emptyCell, d, week);
                }
            }
        }

        calendarSection.getChildren().addAll(monthLabel, monthNav, calendarGrid);

        // 日程详情部分
        VBox scheduleDetail = new VBox(10);
        scheduleDetail.setPrefWidth(400);

        Label detailLabel = new Label("2025年09月12日的日程");
        detailLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        ListView<String> scheduleList = new ListView<>();
        scheduleList.getItems().addAll(
                "08:00 - 09:00 高等数学授课 (数学1班)",
                "10:00 - 11:00 大学英语授课 (英语2班)",
                "14:00 - 15:00 教研会议",
                "16:00 - 17:00 批改作业"
        );
        scheduleList.setPrefHeight(150);

        Button addReminderBtn = new Button("添加新提醒");
        addReminderBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        scheduleDetail.getChildren().addAll(detailLabel, scheduleList, addReminderBtn);

        mainContent.getChildren().addAll(calendarSection, scheduleDetail);
        schedulePage.getChildren().add(mainContent);

        this.setCenter(schedulePage);
        this.setLeft(null);
    }

    /**
     * 显示课程管理页面
     */
    private void showCoursesPage() {
        currentView = "courses";

        // 创建主布局
        HBox mainLayout = new HBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #ecf0f1;");

        // 左侧统计栏
        VBox statsBox = new VBox(15);
        statsBox.setPrefWidth(200);
        statsBox.setStyle("-fx-background-color: #34495e; -fx-padding: 15;");

        VBox teaching = createStatCard("正在授课", "2门", "#3498db");
        VBox completed = createStatCard("已结课", "1门", "#27ae60");
        VBox total = createStatCard("总计", "3门", "#2c3e50");
        VBox students = createStatCard("学生总数", "135人", "#f39c12");

        statsBox.getChildren().addAll(teaching, completed, total, students);

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

        ComboBox<String> filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("全部课程", "进行中", "已结课");
        filterComboBox.setValue("全部课程");
        filterComboBox.setPrefWidth(120);

        searchBox.getChildren().addAll(searchField, searchBtn, addCourseBtn, filterComboBox);

        // 课程表格
        Label tableLabel = new Label("我的课程");
        tableLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        TableView<Course> courseTable = new TableView<>();
        courseTable.setItems(courseList);
        courseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Course, String> nameCol = new TableColumn<>("课程名称");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

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

        Label courseTitle = new Label(course.getName() + " - " + course.getClassName());
        courseTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(10, 0, 0, 0));

        infoGrid.add(new Label("授课教师:"), 0, 0);
        infoGrid.add(new Label(course.getTeacher()), 1, 0);

        infoGrid.add(new Label("课程时间:"), 0, 1);
        infoGrid.add(new Label(course.getStartDate() + " 至 " + course.getEndDate()), 1, 1);

        infoGrid.add(new Label("课程状态:"), 0, 2);
        Label statusLabel = new Label(course.getStatus());
        statusLabel.setTextFill(course.getStatus().equals("进行中") ? Color.GREEN : Color.GRAY);
        infoGrid.add(statusLabel, 1, 2);

        infoGrid.add(new Label("学生人数:"), 0, 3);
        infoGrid.add(new Label(String.valueOf(course.getStudentCount())), 1, 3);

        courseInfoBox.getChildren().addAll(courseTitle, infoGrid);

        // 功能选项卡
        TabPane tabPane = new TabPane();

        // 课程回放选项卡 - 教师可以添加回放
        Tab playbackTab = new Tab("课程回放管理", createPlaybackManagementContent());
        playbackTab.setClosable(false);

        // 课程资料选项卡 - 教师可以上传资料
        Tab materialsTab = new Tab("课程资料管理", createMaterialsManagementContent());
        materialsTab.setClosable(false);

        // 学生管理选项卡
        Tab studentsTab = new Tab("学生管理", createStudentsManagementContent());
        studentsTab.setClosable(false);

        tabPane.getTabs().addAll(playbackTab, materialsTab, studentsTab);

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

        ListView<CoursePlayback> playbackListView = new ListView<>();
        playbackListView.setItems(playbackList);
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

        playbackContent.getChildren().addAll(headerBox, playbackListView);
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
        materialsTable.setItems(materialList);

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
                    materialList.remove(material);
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

        materialsTable.getColumns().addAll(nameCol, typeCol, sizeCol, dateCol, actionCol);
        materialsTable.setPrefHeight(300);

        materialsContent.getChildren().addAll(headerBox, materialsTable);
        return materialsContent;
    }

    /**
     * 创建学生管理内容
     */
    private VBox createStudentsManagementContent() {
        VBox studentsContent = new VBox(15);
        studentsContent.setPadding(new Insets(15));

        Label titleLabel = new Label("学生名单 (" + currentCourse.getStudentCount() + "人)");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        // 模拟学生数据
        ObservableList<Student> studentList = FXCollections.observableArrayList();
        for (int i = 1; i <= currentCourse.getStudentCount(); i++) {
            studentList.add(new Student("2023000" + i, "学生" + i, "计算机科学与技术", "90%"));
        }

        TableView<Student> studentTable = new TableView<>();
        studentTable.setItems(studentList);

        TableColumn<Student, String> idCol = new TableColumn<>("学号");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Student, String> nameCol = new TableColumn<>("姓名");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Student, String> majorCol = new TableColumn<>("专业");
        majorCol.setCellValueFactory(new PropertyValueFactory<>("major"));

        TableColumn<Student, String> attendanceCol = new TableColumn<>("出勤率");
        attendanceCol.setCellValueFactory(new PropertyValueFactory<>("attendance"));

        studentTable.getColumns().addAll(idCol, nameCol, majorCol, attendanceCol);
        studentTable.setPrefHeight(300);

        studentsContent.getChildren().addAll(titleLabel, studentTable);
        return studentsContent;
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

        TextField dateField = new TextField();
        dateField.setPromptText("YYYY-MM-DD");
        TextField titleField = new TextField();
        titleField.setPromptText("回放标题");
        TextField durationField = new TextField();
        durationField.setPromptText("时长（分钟）");

        grid.add(new Label("日期:"), 0, 0);
        grid.add(dateField, 1, 0);
        grid.add(new Label("标题:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("时长:"), 0, 2);
        grid.add(durationField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // 添加按钮
        ButtonType addButtonType = new ButtonType("添加", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // 转换结果
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new CoursePlayback(
                        dateField.getText(),
                        titleField.getText(),
                        durationField.getText() + "分钟"
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(playback -> {
            playbackList.add(playback);
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
        TextField sizeField = new TextField();
        sizeField.setPromptText("文件大小（MB）");

        grid.add(new Label("名称:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("类型:"), 0, 1);
        grid.add(typeComboBox, 1, 1);
        grid.add(new Label("大小:"), 0, 2);
        grid.add(sizeField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        ButtonType addButtonType = new ButtonType("上传", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new CourseMaterial(
                        nameField.getText(),
                        typeComboBox.getValue(),
                        sizeField.getText() + "MB",
                        "2024-09-10"
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(material -> {
            materialList.add(material);
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

        // 添加统计信息卡片
        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER_LEFT);

        // 待批改作业统计
        long pendingCount = assignmentList.stream()
                .filter(a -> "待批改".equals(a.getStatus()))
                .count();

        VBox pendingCard = createAssignmentStatCard("待批改作业", String.valueOf(pendingCount), "#e74c3c", "📝");
        VBox totalCard = createAssignmentStatCard("总作业数", String.valueOf(assignmentList.size()), "#3498db", "📚");
        VBox completedCard = createAssignmentStatCard("已批改",
                String.valueOf(assignmentList.size() - pendingCount), "#27ae60", "✅");

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
        courseFilter.getItems().addAll("所有课程", "高等数学", "大学英语", "计算机基础");
        courseFilter.setValue("所有课程");
        courseFilter.setPrefWidth(120);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("所有状态", "待批改", "已批改");
        statusFilter.setValue("所有状态");
        statusFilter.setPrefWidth(100);

        Button searchBtn = new Button("搜索");
        searchBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

        Button exportBtn = new Button("导出数据");
        exportBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
        exportBtn.setOnAction(e -> showAlert("提示", "数据导出功能即将上线！"));

        filterBox.getChildren().addAll(
                new Label("搜索:"), searchField,
                new Label("课程:"), courseFilter,
                new Label("状态:"), statusFilter,
                searchBtn, exportBtn
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
                                if (ratio < 0.5) {
                                    setTextFill(Color.ORANGE);
                                } else if (ratio >= 0.8) {
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
                    Assignment assignment = getTableView().getItems().get(getIndex());
                    showGradingPage(assignment);
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
                    } catch (Exception e) {
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
        statusLabel.setTextFill("待批改".equals(assignment.getStatus()) ? Color.RED : Color.GREEN);
        grid.add(statusLabel, 1, 3);

        grid.add(new Label("提交情况:"), 0, 4);
        grid.add(new Label(assignment.getSubmissionInfo()), 1, 4);

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
        ComboBox<String> courseComboBox = new ComboBox<>();
        courseComboBox.getItems().addAll("高等数学", "大学英语", "计算机基础");
        TextField assignDateField = new TextField();
        assignDateField.setPromptText("YYYY-MM-DD");
        TextField dueDateField = new TextField();
        dueDateField.setPromptText("YYYY-MM-DD");

        grid.add(new Label("作业名称:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("所属课程:"), 0, 1);
        grid.add(courseComboBox, 1, 1);
        grid.add(new Label("布置日期:"), 0, 2);
        grid.add(assignDateField, 1, 2);
        grid.add(new Label("截止日期:"), 0, 3);
        grid.add(dueDateField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType addButtonType = new ButtonType("布置", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new Assignment(
                        nameField.getText(),
                        courseComboBox.getValue(),
                        assignDateField.getText(),
                        dueDateField.getText(),
                        "待批改",
                        "已提交: 0/0"
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(assignment -> {
            assignmentList.add(assignment);
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

        assignmentInfoBox.getChildren().addAll(assignmentTitle, infoGrid);

        // 学生作业列表
        VBox studentWorkBox = new VBox(10);
        Label studentWorkLabel = new Label("学生作业列表");
        studentWorkLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        // 模拟学生作业数据
        ObservableList<StudentWork> studentWorkList = FXCollections.observableArrayList();
        String[] students = {"张三", "李四", "王五", "赵六", "钱七"};
        for (int i = 0; i < 5; i++) {
            studentWorkList.add(new StudentWork(
                    "2023000" + (i + 1),
                    students[i],
                    "2024-09-0" + (8 + i),
                    i % 2 == 0 ? "已提交" : "未提交",
                    i % 2 == 0 ? "待批改" : "-",
                    i % 2 == 0 ? "-" : "-"
            ));
        }

        TableView<StudentWork> studentWorkTable = new TableView<>();
        studentWorkTable.setItems(studentWorkList);

        TableColumn<StudentWork, String> idCol = new TableColumn<>("学号");
        idCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));

        TableColumn<StudentWork, String> nameCol = new TableColumn<>("姓名");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));

        TableColumn<StudentWork, String> submitDateCol = new TableColumn<>("提交时间");
        submitDateCol.setCellValueFactory(new PropertyValueFactory<>("submitDate"));

        TableColumn<StudentWork, String> statusCol = new TableColumn<>("提交状态");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("submitStatus"));

        TableColumn<StudentWork, String> gradeCol = new TableColumn<>("成绩");
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("grade"));

        TableColumn<StudentWork, String> feedbackCol = new TableColumn<>("评语");
        feedbackCol.setCellValueFactory(new PropertyValueFactory<>("feedback"));

        TableColumn<StudentWork, Void> actionCol = new TableColumn<>("操作");
        actionCol.setCellFactory(param -> new TableCell<StudentWork, Void>() {
            private final Button gradeBtn = new Button("批改");

            {
                gradeBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                gradeBtn.setOnAction(event -> {
                    StudentWork work = getTableView().getItems().get(getIndex());
                    if ("已提交".equals(work.getSubmitStatus())) {
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
                    setGraphic(gradeBtn);
                }
            }
        });

        studentWorkTable.getColumns().addAll(idCol, nameCol, submitDateCol, statusCol, gradeCol, feedbackCol, actionCol);
        studentWorkTable.setPrefHeight(300);

        studentWorkBox.getChildren().addAll(studentWorkLabel, studentWorkTable);

        gradingPage.getChildren().addAll(backButtonBox, assignmentInfoBox, studentWorkBox);
        this.setCenter(gradingPage);
        this.setLeft(null);
    }

    /**
     * 显示批改学生作业对话框
     */
    private void showGradeStudentWorkDialog(StudentWork work) {
        Dialog<StudentWork> dialog = new Dialog<>();
        dialog.setTitle("批改作业");
        dialog.setHeaderText("批改学生: " + work.getStudentName() + " (" + work.getStudentId() + ")");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField gradeField = new TextField();
        gradeField.setPromptText("请输入成绩（0-100）");
        TextArea feedbackArea = new TextArea();
        feedbackArea.setPromptText("请输入评语");
        feedbackArea.setPrefRowCount(3);

        grid.add(new Label("成绩:"), 0, 0);
        grid.add(gradeField, 1, 0);
        grid.add(new Label("评语:"), 0, 1);
        grid.add(feedbackArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        ButtonType gradeButtonType = new ButtonType("提交批改", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(gradeButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == gradeButtonType) {
                work.setGrade(gradeField.getText());
                work.setFeedback(feedbackArea.getText());
                return work;
            }
            return null;
        });

        dialog.showAndWait();
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

    // 内部类 - 课程实体
    public static class Course {
        private String name;
        private String className;
        private String teacher;
        private String startDate;
        private String endDate;
        private String status;
        private int studentCount;

        public Course(String name, String className, String teacher, String startDate, String endDate, String status, int studentCount) {
            this.name = name;
            this.className = className;
            this.teacher = teacher;
            this.startDate = startDate;
            this.endDate = endDate;
            this.status = status;
            this.studentCount = studentCount;
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        public String getTeacher() { return teacher; }
        public void setTeacher(String teacher) { this.teacher = teacher; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getStudentCount() { return studentCount; }
        public void setStudentCount(int studentCount) { this.studentCount = studentCount; }
    }

    // 内部类 - 作业实体
    public static class Assignment {
        private String name;
        private String course;
        private String assignDate;
        private String dueDate;
        private String status;
        private String submissionInfo;

        public Assignment(String name, String course, String assignDate, String dueDate, String status, String submissionInfo) {
            this.name = name;
            this.course = course;
            this.assignDate = assignDate;
            this.dueDate = dueDate;
            this.status = status;
            this.submissionInfo = submissionInfo;
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCourse() { return course; }
        public void setCourse(String course) { this.course = course; }
        public String getAssignDate() { return assignDate; }
        public void setAssignDate(String assignDate) { this.assignDate = assignDate; }
        public String getDueDate() { return dueDate; }
        public void setDueDate(String dueDate) { this.dueDate = dueDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getSubmissionInfo() { return submissionInfo; }
        public void setSubmissionInfo(String submissionInfo) { this.submissionInfo = submissionInfo; }
    }

    // 内部类 - 课程回放实体
    public static class CoursePlayback {
        private String date;
        private String title;
        private String duration;

        public CoursePlayback(String date, String title, String duration) {
            this.date = date;
            this.title = title;
            this.duration = duration;
        }

        // Getters and Setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
    }

    // 内部类 - 课程资料实体
    public static class CourseMaterial {
        private String name;
        private String type;
        private String size;
        private String uploadDate;

        public CourseMaterial(String name, String type, String size, String uploadDate) {
            this.name = name;
            this.type = type;
            this.size = size;
            this.uploadDate = uploadDate;
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getSize() { return size; }
        public void setSize(String size) { this.size = size; }
        public String getUploadDate() { return uploadDate; }
        public void setUploadDate(String uploadDate) { this.uploadDate = uploadDate; }
    }

    // 内部类 - 学生实体
    public static class Student {
        private String id;
        private String name;
        private String major;
        private String attendance;

        public Student(String id, String name, String major, String attendance) {
            this.id = id;
            this.name = name;
            this.major = major;
            this.attendance = attendance;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getMajor() { return major; }
        public void setMajor(String major) { this.major = major; }
        public String getAttendance() { return attendance; }
        public void setAttendance(String attendance) { this.attendance = attendance; }
    }

    // 内部类 - 学生作业实体
    public static class StudentWork {
        private String studentId;
        private String studentName;
        private String submitDate;
        private String submitStatus;
        private String grade;
        private String feedback;

        public StudentWork(String studentId, String studentName, String submitDate, String submitStatus, String grade, String feedback) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.submitDate = submitDate;
            this.submitStatus = submitStatus;
            this.grade = grade;
            this.feedback = feedback;
        }

        // Getters and Setters
        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public String getSubmitDate() { return submitDate; }
        public void setSubmitDate(String submitDate) { this.submitDate = submitDate; }
        public String getSubmitStatus() { return submitStatus; }
        public void setSubmitStatus(String submitStatus) { this.submitStatus = submitStatus; }
        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }
        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }
    }
}