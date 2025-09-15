package com.vcampus.client.service;

import com.vcampus.common.entity.Reservation;
import com.vcampus.common.entity.Space;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 模拟空间服务类
 * 用于前端开发测试，后端完成后替换为真实服务调用
 */
public class SpaceService {

    private static List<Space> mockSpaces;
    private static List<Reservation> mockReservations;

    static {
        initMockData();
    }

    /**
     * 初始化模拟数据
     */
    private static void initMockData() {
        mockSpaces = new ArrayList<>();
        mockReservations = new ArrayList<>();

        // 体育馆空间
        mockSpaces.add(new Space("S001", "游泳馆A", "体育馆", "游泳馆", "体育馆1楼", 50,
                "标准游泳池，配备救生设备", Arrays.asList("救生圈", "计时器", "更衣室"),
                "swimming_pool.jpg", true));

        mockSpaces.add(new Space("S002", "羽毛球馆1号场", "体育馆", "羽毛球馆", "体育馆2楼", 4,
                "标准羽毛球场地", Arrays.asList("羽毛球网", "计分牌", "照明"),
                "badminton_court.jpg", true));

        mockSpaces.add(new Space("S003", "篮球馆主场", "体育馆", "篮球馆", "体育馆3楼", 100,
                "标准篮球场地，可举办比赛", Arrays.asList("篮球架", "计分牌", "音响"),
                "basketball_court.jpg", true));

        // 教学楼空间
        mockSpaces.add(new Space("S004", "多媒体教室201", "教学楼", "多媒体教室", "A楼2层", 60,
                "配备投影仪和音响设备", Arrays.asList("投影仪", "音响", "黑板", "空调"),
                "multimedia_room.jpg", true));

        mockSpaces.add(new Space("S005", "计算机实验室301", "教学楼", "计算机实验室", "B楼3层", 40,
                "配备50台电脑", Arrays.asList("电脑", "投影仪", "网络", "空调"),
                "computer_lab.jpg", false));

        // 图书馆空间
        mockSpaces.add(new Space("S006", "研讨室A101", "图书馆", "研讨室", "图书馆1楼", 8,
                "小型研讨空间", Arrays.asList("白板", "桌椅", "网络"),
                "study_room.jpg", true));

        // 模拟预约数据
        mockReservations.add(new Reservation("R001", "S001", "2021001", "张三",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2),
                "游泳训练", "13800138001", Reservation.ReservationStatus.PENDING,
                LocalDateTime.now(), ""));

        mockReservations.add(new Reservation("R002", "S002", "2021001", "张三",
                LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(1),
                "羽毛球比赛", "13800138001", Reservation.ReservationStatus.APPROVED,
                LocalDateTime.now(), ""));
    }

    /**
     * 获取所有空间分类
     */
    public static Map<String, List<String>> getSpaceCategories() {
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("体育馆", Arrays.asList("游泳馆", "羽毛球馆", "篮球馆", "健身房", "乒乓球馆"));
        categories.put("教学楼", Arrays.asList("多媒体教室", "计算机实验室", "语音室", "会议室", "报告厅"));
        categories.put("图书馆", Arrays.asList("研讨室", "自习室", "会议室", "报告厅", "展示厅"));
        categories.put("其他", Arrays.asList("学生活动中心", "艺术中心", "创客空间", "咖啡厅"));
        return categories;
    }

    /**
     * 根据分类获取空间列表
     */
    public static List<Space> getSpacesByCategory(String category, String subCategory) {
        List<Space> result = new ArrayList<>();
        for (Space space : mockSpaces) {
            if (category.isEmpty() || space.getCategory().equals(category)) {
                if (subCategory.isEmpty() || space.getSubCategory().equals(subCategory)) {
                    result.add(space);
                }
            }
        }
        return result;
    }

    /**
     * 根据ID获取空间信息
     */
    public static Space getSpaceById(String spaceId) {
        return mockSpaces.stream()
                .filter(space -> space.getId().equals(spaceId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 搜索空间
     */
    public static List<Space> searchSpaces(String keyword) {
        List<Space> result = new ArrayList<>();
        for (Space space : mockSpaces) {
            if (space.getName().contains(keyword) ||
                    space.getLocation().contains(keyword) ||
                    space.getDescription().contains(keyword)) {
                result.add(space);
            }
        }
        return result;
    }

    /**
     * 创建预约
     */
    public static boolean createReservation(Reservation reservation) {
        try {
            // 生成新的预约ID
            String newId = "R" + String.format("%03d", mockReservations.size() + 1);
            reservation.setId(newId);
            reservation.setCreateTime(LocalDateTime.now());
            reservation.setStatus(Reservation.ReservationStatus.PENDING);

            mockReservations.add(reservation);
            System.out.println("创建预约成功: " + reservation);
            return true;
        } catch (Exception e) {
            System.err.println("创建预约失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取用户的预约列表
     */
    public static List<Reservation> getUserReservations(String userId) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation reservation : mockReservations) {
            if (reservation.getUserId().equals(userId)) {
                result.add(reservation);
            }
        }
        return result;
    }

    /**
     * 取消预约
     */
    public static boolean cancelReservation(String reservationId, String userId) {
        for (Reservation reservation : mockReservations) {
            if (reservation.getId().equals(reservationId) &&
                    reservation.getUserId().equals(userId)) {
                reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
                System.out.println("取消预约成功: " + reservationId);
                return true;
            }
        }
        return false;
    }

    /**
     * 获取空间的预约时间段
     */
    public static List<Reservation> getSpaceReservations(String spaceId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation reservation : mockReservations) {
            if (reservation.getSpaceId().equals(spaceId) &&
                    reservation.getStartTime().isAfter(startDate) &&
                    reservation.getEndTime().isBefore(endDate) &&
                    reservation.getStatus() != Reservation.ReservationStatus.CANCELLED) {
                result.add(reservation);
            }
        }
        return result;
    }

    /**
     * 检查时间段是否可预约
     */
    public static boolean isTimeSlotAvailable(String spaceId, LocalDateTime startTime, LocalDateTime endTime) {
        for (Reservation reservation : mockReservations) {
            if (reservation.getSpaceId().equals(spaceId) &&
                    reservation.getStatus() != Reservation.ReservationStatus.CANCELLED) {

                // 检查时间冲突
                if (!(endTime.isBefore(reservation.getStartTime()) ||
                        startTime.isAfter(reservation.getEndTime()))) {
                    return false; // 有冲突
                }
            }
        }
        return true; // 无冲突，可预约
    }

    /**
     * 获取预约统计信息
     */
    public static Map<String, Integer> getReservationStats(String userId) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("待审批", 0);
        stats.put("待赴约", 0);
        stats.put("已完成", 0);
        stats.put("违约", 0);

        for (Reservation reservation : mockReservations) {
            if (reservation.getUserId().equals(userId)) {
                switch (reservation.getStatus()) {
                    case PENDING:
                        stats.put("待审批", stats.get("待审批") + 1);
                        break;
                    case APPROVED:
                        if (reservation.getStartTime().isAfter(LocalDateTime.now())) {
                            stats.put("待赴约", stats.get("待赴约") + 1);
                        }
                        break;
                    case COMPLETED:
                        stats.put("已完成", stats.get("已完成") + 1);
                        break;
                    case CANCELLED:
                        // 这里可以根据具体业务逻辑判断是否算违约
                        break;
                }
            }
        }

        return stats;
    }
}