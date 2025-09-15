package com.vcampus.client;

import com.vcampus.client.service.ClientService;
import com.vcampus.common.entity.User;
import com.vcampus.common.entity.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Scanner;

/**
 * 简单的控制台测试客户端
 * 用于测试服务器连接和登录功能
 */
public class SimpleTestClient {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ClientService clientService = new ClientService();

        System.out.println("=== VCampus简单测试客户端 ===");

        // 连接服务器
        System.out.print("正在连接服务器...");
        if (clientService.connect("localhost", 8888)) {
            System.out.println("连接成功！");
        } else {
            System.out.println("连接失败！请检查服务器是否启动。");
            return;
        }

        // 登录测试
        while (true) {
            System.out.println("\n请输入登录信息：");
            System.out.print("用户名: ");
            String username = scanner.nextLine();

            if ("exit".equals(username)) {
                break;
            }

            System.out.print("密码: ");
            String password = scanner.nextLine();

            System.out.print("角色 (1-学生, 2-教师, 3-管理员): ");
            String roleChoice = scanner.nextLine();

            User.Role role = User.Role.STUDENT;
            switch (roleChoice) {
                case "2": role = User.Role.TEACHER; break;
                case "3": role = User.Role.ADMIN; break;
                default: role = User.Role.STUDENT; break;
            }

            // 创建登录用户
            User loginUser = new User(username, password, "", role);

            // 执行登录
            System.out.print("正在登录...");
            User authenticatedUser = clientService.login(loginUser);

            if (authenticatedUser != null) {
                System.out.println("登录成功！");
                System.out.println("欢迎，" + authenticatedUser.getDisplayName());
                System.out.println("用户ID: " + authenticatedUser.getUserId());
                System.out.println("角色: " + authenticatedUser.getRole().getDisplayName());

                // 简单功能测试
                testBasicFunctions(clientService);

                // 登出
                if (clientService.logout()) {
                    System.out.println("登出成功");
                }

            } else {
                System.out.println("登录失败！请检查用户名和密码。");
            }



            System.out.println("\n输入 'exit' 退出，或继续测试其他账户");
        }

        // 断开连接
        clientService.disconnect();
        scanner.close();
        System.out.println("测试客户端已退出");
    }

    /**
     * 测试基本功能
     */
    private static void testBasicFunctions(ClientService clientService) {
        System.out.println("\n=== 功能测试 ===");

        // 测试获取学生列表
        System.out.print("测试获取学生列表...");
        try {
            var students = clientService.getStudentList();
            System.out.println("成功，获取到 " + students.size() + " 条记录");
        } catch (Exception e) {
            System.out.println("失败: " + e.getMessage());
        }

        // 测试获取课程列表
        System.out.print("测试获取课程列表...");
        try {
            var courses = clientService.getCourseList();
            System.out.println("成功，获取到 " + courses.size() + " 条记录");
        } catch (Exception e) {
            System.out.println("失败: " + e.getMessage());
        }

        // 测试图书搜索
        System.out.print("测试图书搜索...");
        try {
            var books = clientService.searchBooks("Java");
            System.out.println("成功，搜索到 " + books.size() + " 条记录");
        } catch (Exception e) {
            System.out.println("失败: " + e.getMessage());
        }

        // 在登录成功后添加
        testAnnouncementFunctions(clientService);
        testApplicationFunctions(clientService);
    }

    // 测试公告功能
    private static void testAnnouncementFunctions(ClientService clientService) {
        System.out.println("\n=== 公告功能测试 ===");

        try {
            // 测试获取公告列表
            System.out.print("测试获取公告列表...");
            Message announcementRequest = new Message(Message.Type.ANNOUNCEMENT_LIST, null);
            Message response = clientService.sendAndReceive(announcementRequest);

            if (response.getCode() == Message.Code.SUCCESS) {
                Map<String, Object> result = (Map<String, Object>) response.getData();
                List<Map<String, Object>> announcements = (List<Map<String, Object>>) result.get("announcements");
                System.out.println("成功，获取到 " + announcements.size() + " 条公告");

                // 显示前几条公告标题
                for (int i = 0; i < Math.min(3, announcements.size()); i++) {
                    Map<String, Object> announcement = announcements.get(i);
                    System.out.println("  - " + announcement.get("title"));
                }
            } else {
                System.out.println("失败: " + response.getData());
            }

        } catch (Exception e) {
            System.out.println("测试失败: " + e.getMessage());
        }
    }

    // 测试申请功能
    private static void testApplicationFunctions(ClientService clientService) {
        System.out.println("\n=== 申请功能测试 ===");

        try {
            // 测试获取申请列表
            System.out.print("测试获取我的申请列表...");
            Map<String, Object> params = new HashMap<>();
            params.put("isMyApplications", true);
            params.put("page", 1);
            params.put("pageSize", 10);

            Message applicationRequest = new Message(Message.Type.APPLICATION_LIST, params);
            Message response = clientService.sendAndReceive(applicationRequest);

            if (response.getCode() == Message.Code.SUCCESS) {
                Map<String, Object> result = (Map<String, Object>) response.getData();
                List<Map<String, Object>> applications = (List<Map<String, Object>>) result.get("applications");
                System.out.println("成功，获取到 " + applications.size() + " 条申请记录");
            } else {
                System.out.println("失败: " + response.getData());
            }

        } catch (Exception e) {
            System.out.println("测试失败: " + e.getMessage());
        }
    }

}