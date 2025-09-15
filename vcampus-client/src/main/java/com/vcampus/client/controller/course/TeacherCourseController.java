// ============= 修复后的教师课程管理控制器 =============
package com.vcampus.client.controller.course;

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
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TeacherCourseController implements Initializable {

    // 教师信息显示
    @FXML private Label teacherNameLabel;
    @FXML private Label totalCoursesLabel;
    @FXML private Label totalStudentsLabel;

    // 课程详情面板
    @FXML private VBox courseDetailsPanel;
    @FXML private Label selectedCourseLabel;
    @FXML private Label courseTypeLabel;
    @FXML private Label departmentLabel;
    @FXML private Label classroomLabel;
    @FXML private Label semesterLabel;
    @FXML private TextArea courseDescriptionArea;

    // 学生列表相关
    @FXML private Label studentCountLabel;
    @FXML private TableView<User> studentsTable;

    // 教师课程表格
    @FXML private TableView<Course> teacherCoursesTable;

    // 教师课程表格列
    @FXML private TableColumn<Course, String> courseIdCol;
    @FXML private TableColumn<Course, String> courseNameCol;
    @FXML private TableColumn<Course, Integer> creditsCol;
    @FXML private TableColumn<Course, String> scheduleCol;
    @FXML private TableColumn<Course, Integer> enrolledCol;
    @FXML private TableColumn<Course, Integer> capacityCol;
    @FXML private TableColumn<Course, String> statusCol;

    // 学生表格列
    @FXML private TableColumn<User, String> studentIdCol;
    @FXML private TableColumn<User, String> studentNameCol;
    @FXML private TableColumn<User, String> majorCol;
    @FXML private TableColumn<User, String> gradeCol;

    private ClientService clientService;
    private User currentUser;
    private ObservableList<Course> teacherCourses = FXCollections.observableArrayList();
    private ObservableList<User> courseStudents = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("TeacherCourseController 初始化开始");
        try {
            initializeTableColumns();
            setupEventHandlers();
            System.out.println("TeacherCourseController 初始化完成");
        } catch (Exception e) {
            System.err.println("初始化TeacherCourseController时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setClientService(ClientService clientService) {
        this.clientService = clientService;
        System.out.println("TeacherCourseController 设置ClientService: " + (clientService != null ? "成功" : "失败"));
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        System.out.println("设置当前教师用户: " + (currentUser != null ? currentUser.getDisplayName() : "null"));

        if (teacherNameLabel != null) {
            teacherNameLabel.setText(currentUser != null ? currentUser.getDisplayName() : "未知教师");
        }

        if (currentUser != null && clientService != null) {
            loadTeacherCourseData();
        }
    }

    private void initializeTableColumns() {
        try {
            // 教师课程表格设置
            if (courseIdCol != null) courseIdCol.setCellValueFactory(new PropertyValueFactory<>("courseId"));
            if (courseNameCol != null) courseNameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
            if (creditsCol != null) creditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
            if (scheduleCol != null) scheduleCol.setCellValueFactory(new PropertyValueFactory<>("schedule"));
            if (enrolledCol != null) enrolledCol.setCellValueFactory(new PropertyValueFactory<>("enrolled"));
            if (capacityCol != null) capacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));

            // 状态列自定义显示
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

            // 学生表格设置 - 修复：使用正确的属性名
            if (studentIdCol != null) studentIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
            if (studentNameCol != null) studentNameCol.setCellValueFactory(new PropertyValueFactory<>("realName")); // 修改为realName
            if (majorCol != null) majorCol.setCellValueFactory(new PropertyValueFactory<>("major"));
            if (gradeCol != null) gradeCol.setCellValueFactory(new PropertyValueFactory<>("grade"));

            // 设置表格数据
            if (teacherCoursesTable != null) {
                teacherCoursesTable.setItems(teacherCourses);
            }
            if (studentsTable != null) {
                studentsTable.setItems(courseStudents);
            }

            System.out.println("教师表格列初始化完成");
        } catch (Exception e) {
            System.err.println("初始化教师表格列时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupEventHandlers() {
        try {
            // 课程选择事件
            if (teacherCoursesTable != null) {
                teacherCoursesTable.getSelectionModel().selectedItemProperty().addListener(
                        (observable, oldValue, newValue) -> {
                            if (newValue != null) {
                                System.out.println("选择课程: " + newValue.getCourseName() + " (ID: " + newValue.getCourseId() + ")");
                                loadCourseStudents(newValue);
                                showCourseDetails(newValue);
                            }
                        });
            }

            System.out.println("教师事件处理器设置完成");
        } catch (Exception e) {
            System.err.println("设置教师事件处理器时发生错误: " + e.getMessage());
        }
    }

    private void loadTeacherCourseData() {
        if (clientService == null || currentUser == null) {
            System.out.println("无法加载教师数据: clientService=" + (clientService != null) +
                    ", currentUser=" + (currentUser != null));
            return;
        }

        try {
            System.out.println("开始加载教师课程数据，教师ID: " + currentUser.getUserId());

            // 获取教师授课课程
            Message request = new Message(Message.Type.TEACHER_COURSES, currentUser.getUserId());
            Message response = clientService.sendRequest(request);

            if (response != null && response.getCode() == Message.Code.SUCCESS) {
                @SuppressWarnings("unchecked")
                List<Course> courses = (List<Course>) response.getData();

                if (courses != null && !courses.isEmpty()) {
                    System.out.println("从服务器获取到 " + courses.size() + " 门课程");

                    // 为每门课程设置实际选课人数
                    for (Course course : courses) {
                        int actualEnrolled = getActualEnrolledCount(course.getCourseId());
                        course.setEnrolled(actualEnrolled);
                        System.out.println("课程 " + course.getCourseId() + " 的选课人数: " + actualEnrolled);
                    }

                    teacherCourses.setAll(courses);
                    System.out.println("成功加载 " + courses.size() + " 门教师课程");

                    // 更新统计信息
                    updateStatistics(courses);
                } else {
                    System.out.println("教师课程数据为空或null");
                    clearTeacherData();
                    showAlert("提示", "当前教师暂无授课课程");
                }
            } else {
                System.out.println("获取教师课程失败: " +
                        (response != null ? response.getCode() + " - " + response.getData() : "无响应"));
                clearTeacherData();
                showAlert("加载失败", "无法获取课程数据: " +
                        (response != null ? response.getData() : "服务器无响应"));
            }
        } catch (Exception e) {
            System.err.println("加载教师课程数据时发生错误: " + e.getMessage());
            e.printStackTrace();
            clearTeacherData();
            showAlert("系统错误", "加载数据时发生错误: " + e.getMessage());
        }
    }

    private int getActualEnrolledCount(String courseId) {
        try {
            System.out.println("获取课程 " + courseId + " 的选课人数");

            // 获取课程实际选课人数
            Message request = new Message(Message.Type.COURSE_SELECTIONS, courseId);
            Message response = clientService.sendRequest(request);

            if (response != null && response.getCode() == Message.Code.SUCCESS) {
                @SuppressWarnings("unchecked")
                List<CourseSelection> selections = (List<CourseSelection>) response.getData();

                if (selections != null) {
                    System.out.println("课程 " + courseId + " 总选课记录数: " + selections.size());

                    // 只统计未退选的学生
                    long validCount = selections.stream()
                            .filter(s -> s.getStatus() != CourseSelection.Status.DROPPED)
                            .count();

                    System.out.println("课程 " + courseId + " 有效选课人数: " + validCount);
                    return (int) validCount;
                } else {
                    System.out.println("课程 " + courseId + " 选课记录为null");
                }
            } else {
                System.out.println("获取课程选课记录失败: " +
                        (response != null ? response.getCode() + " - " + response.getData() : "无响应"));
            }
        } catch (Exception e) {
            System.err.println("获取课程选课人数时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    private void updateStatistics(List<Course> courses) {
        try {
            if (totalCoursesLabel != null) {
                totalCoursesLabel.setText(String.valueOf(courses.size()));
            }

            int totalStudents = courses.stream().mapToInt(Course::getEnrolled).sum();
            if (totalStudentsLabel != null) {
                totalStudentsLabel.setText(String.valueOf(totalStudents));
            }

            System.out.println("教师统计 - 课程数: " + courses.size() + ", 学生总数: " + totalStudents);
        } catch (Exception e) {
            System.err.println("更新教师统计信息时发生错误: " + e.getMessage());
        }
    }

    private void loadCourseStudents(Course course) {
        if (course == null || clientService == null) {
            System.out.println("无法加载学生数据: course=" + (course != null) +
                    ", clientService=" + (clientService != null));
            return;
        }

        try {
            System.out.println("开始加载课程 " + course.getCourseId() + " 的学生列表");

            // 首先获取选课记录
            Message request = new Message(Message.Type.COURSE_SELECTIONS, course.getCourseId());
            Message response = clientService.sendRequest(request);

            if (response != null && response.getCode() == Message.Code.SUCCESS) {
                @SuppressWarnings("unchecked")
                List<CourseSelection> selections = (List<CourseSelection>) response.getData();

                System.out.println("获取到选课记录数: " + (selections != null ? selections.size() : 0));

                if (selections != null && !selections.isEmpty()) {
                    // 获取所有未退选的学生ID
                    List<String> studentIds = new ArrayList<>();
                    for (CourseSelection selection : selections) {
                        System.out.println("选课记录 - 学生ID: " + selection.getStudentId() +
                                ", 状态: " + selection.getStatus());

                        if (selection.getStatus() != CourseSelection.Status.DROPPED) {
                            studentIds.add(selection.getStudentId());
                        }
                    }

                    System.out.println("有效学生ID数量: " + studentIds.size());

                    if (!studentIds.isEmpty()) {
                        // 获取学生详细信息
                        List<User> students = getStudentDetails(studentIds);
                        courseStudents.setAll(students);

                        // 更新学生计数显示
                        if (studentCountLabel != null) {
                            studentCountLabel.setText("共 " + students.size() + " 名学生");
                        }

                        System.out.println("成功加载 " + students.size() + " 名选课学生");

                        // 调试：打印学生信息
                        for (User student : students) {
                            System.out.println("学生信息 - ID: " + student.getUserId() +
                                    ", 姓名: " + student.getRealName() +
                                    ", 专业: " + student.getMajor());
                        }
                    } else {
                        courseStudents.clear();
                        if (studentCountLabel != null) {
                            studentCountLabel.setText("共 0 名学生");
                        }
                        System.out.println("该课程暂无有效选课学生");
                    }
                } else {
                    courseStudents.clear();
                    if (studentCountLabel != null) {
                        studentCountLabel.setText("共 0 名学生");
                    }
                    System.out.println("该课程无选课记录");
                }
            } else {
                System.out.println("获取选课记录失败: " +
                        (response != null ? response.getCode() + " - " + response.getData() : "无响应"));
                courseStudents.clear();
                if (studentCountLabel != null) {
                    studentCountLabel.setText("共 0 名学生");
                }
            }
        } catch (Exception e) {
            System.err.println("加载课程学生时发生错误: " + e.getMessage());
            e.printStackTrace();
            courseStudents.clear();
            if (studentCountLabel != null) {
                studentCountLabel.setText("共 0 名学生");
            }
        }
    }

    private List<User> getStudentDetails(List<String> studentIds) {
        List<User> students = new ArrayList<>();

        try {
            System.out.println("开始获取学生详细信息，学生ID列表: " + studentIds);

            // 获取所有用户信息
            Message request = new Message(Message.Type.USER_LIST, null);
            Message response = clientService.sendRequest(request);

            if (response != null && response.getCode() == Message.Code.SUCCESS) {
                @SuppressWarnings("unchecked")
                List<User> allUsers = (List<User>) response.getData();

                System.out.println("从服务器获取到用户总数: " + (allUsers != null ? allUsers.size() : 0));

                if (allUsers != null) {
                    // 筛选出选课学生
                    for (String studentId : studentIds) {
                        for (User user : allUsers) {
                            if (studentId.equals(user.getUserId())) {
                                System.out.println("找到匹配用户 - ID: " + user.getUserId() +
                                        ", 角色: " + user.getRole() +
                                        ", 姓名: " + user.getRealName());

                                // 确保是学生角色
                                if (User.Role.STUDENT.equals(user.getRole())) {
                                    students.add(user);
                                    System.out.println("添加学生: " + user.getRealName());
                                } else {
                                    System.out.println("用户 " + user.getUserId() + " 不是学生角色: " + user.getRole());
                                }
                                break;
                            }
                        }
                    }
                } else {
                    System.out.println("所有用户列表为null");
                }
            } else {
                System.out.println("获取用户列表失败: " +
                        (response != null ? response.getCode() + " - " + response.getData() : "无响应"));
            }
        } catch (Exception e) {
            System.err.println("获取学生详细信息时发生错误: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("最终获取到的学生数: " + students.size());
        return students;
    }

    private void showCourseDetails(Course course) {
        try {
            if (selectedCourseLabel != null) {
                selectedCourseLabel.setText(course.getCourseName() + " (" + course.getCourseId() + ")");
            }
            if (courseTypeLabel != null) {
                courseTypeLabel.setText(course.getCourseType() != null ? course.getCourseType() : "未设置");
            }
            if (departmentLabel != null) {
                departmentLabel.setText(course.getDepartment() != null ? course.getDepartment() : "未设置");
            }
            if (classroomLabel != null) {
                classroomLabel.setText(course.getClassroom() != null ? course.getClassroom() : "未设置");
            }
            if (semesterLabel != null) {
                semesterLabel.setText(course.getSemester() != null ? course.getSemester() : "未设置");
            }
            if (courseDescriptionArea != null) {
                courseDescriptionArea.setText(course.getDescription() != null ?
                        course.getDescription() : "暂无课程描述");
            }
            if (courseDetailsPanel != null) {
                courseDetailsPanel.setVisible(true);
            }

            System.out.println("显示课程详情: " + course.getCourseName());
        } catch (Exception e) {
            System.err.println("显示课程详情时发生错误: " + e.getMessage());
        }
    }

    private void clearTeacherData() {
        try {
            teacherCourses.clear();
            courseStudents.clear();
            if (totalCoursesLabel != null) totalCoursesLabel.setText("0");
            if (totalStudentsLabel != null) totalStudentsLabel.setText("0");
            if (studentCountLabel != null) studentCountLabel.setText("共 0 名学生");
            if (courseDetailsPanel != null) courseDetailsPanel.setVisible(false);
        } catch (Exception e) {
            System.err.println("清空教师数据时发生错误: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        System.out.println("教师手动刷新数据...");
        try {
            loadTeacherCourseData();
            showAlert("刷新成功", "数据已更新");
        } catch (Exception e) {
            System.err.println("刷新教师数据时发生错误: " + e.getMessage());
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
            System.err.println("显示教师提示框时发生错误: " + e.getMessage());
        }
    }
}