package com.vcampus.client.service;

import com.vcampus.common.entity.Book;
import com.vcampus.common.entity.BorrowRecord;
import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.Message.Type;

import java.util.List;
import java.util.Map;

/**
 * 图书馆管理员专用服务类
 * 提供图书管理、借阅管理、统计分析等功能
 */
public class LibraryAdminService {

    private ClientService clientService;

    public LibraryAdminService(ClientService clientService) {
        this.clientService = clientService;
    }

    // ================== 图书管理相关 ==================

    /**
     * 获取所有图书列表
     */
    public List<Book> getAllBooks() {
        Message request = new Message(Type.BOOK_LIST, null);
        Message response = clientService.sendRequest(request);

        if (response != null && response.getCode() == Message.Code.SUCCESS) {
            return (List<Book>) response.getData();
        }
        return null;
    }

    /**
     * 添加新图书
     */
    public boolean addBook(Book book) {
        Message request = new Message(Type.BOOK_ADD, book);
        Message response = clientService.sendRequest(request);

        return response != null && response.getCode() == Message.Code.SUCCESS;
    }

    /**
     * 更新图书信息
     */
    public boolean updateBook(Book book) {
        Message request = new Message(Type.BOOK_UPDATE, book);
        Message response = clientService.sendRequest(request);

        return response != null && response.getCode() == Message.Code.SUCCESS;
    }

    /**
     * 删除图书
     */
    public boolean deleteBook(String bookId) {
        Message request = new Message(Type.BOOK_DELETE, bookId);
        Message response = clientService.sendRequest(request);

        return response != null && response.getCode() == Message.Code.SUCCESS;
    }

    /**
     * 搜索图书
     */
    public List<Book> searchBooks(String keyword) {
        Message request = new Message(Type.BOOK_SEARCH, keyword);
        Message response = clientService.sendRequest(request);

        if (response != null && response.getCode() == Message.Code.SUCCESS) {
            return (List<Book>) response.getData();
        }
        return null;
    }

    // ================== 借阅管理相关 ==================

    /**
     * 获取所有借阅记录
     */
    public List<BorrowRecord> getAllBorrowRecords() {
        Message request = new Message(Type.BORROW_RECORD_GET_ALL, null);
        Message response = clientService.sendRequest(request);

        if (response != null && response.getCode() == Message.Code.SUCCESS) {
            return (List<BorrowRecord>) response.getData();
        }
        return null;
    }

    /**
     * 强制归还图书
     */
    public boolean forceReturnBook(int recordId) {
        Message request = new Message(Type.BORROW_RECORD_RETURN, recordId);
        Message response = clientService.sendRequest(request);

        return response != null && response.getCode() == Message.Code.SUCCESS;
    }

    /**
     * 管理员续借操作
     */
    public boolean adminRenewBook(int recordId) {
        Message request = new Message(Type.BOOK_RENEW, recordId);
        Message response = clientService.sendRequest(request);

        return response != null && response.getCode() == Message.Code.SUCCESS;
    }

    /**
     * 根据用户ID搜索借阅记录
     */
    public List<BorrowRecord> searchBorrowRecordsByUser(String userId) {
        // 这里可以扩展为专门的搜索API
        List<BorrowRecord> allRecords = getAllBorrowRecords();
        if (allRecords != null) {
            return allRecords.stream()
                    .filter(record -> record.getUserId().contains(userId))
                    .collect(java.util.stream.Collectors.toList());
        }
        return null;
    }

    // ================== 统计分析相关 ==================

    /**
     * 获取图书统计数据
     */
    public Map<String, Object> getBookStatistics() {
        List<Book> books = getAllBooks();
        if (books == null) return null;

        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalBooks", books.size());
        stats.put("availableBooks", books.stream().filter(b -> b.getStock() > 0).count());
        stats.put("borrowedBooks", books.stream().filter(b -> b.getStock() == 0).count());

        // 按状态分组统计
        Map<String, Long> statusCount = books.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Book::getStatus,
                        java.util.stream.Collectors.counting()
                ));
        stats.put("statusDistribution", statusCount);

        return stats;
    }

    /**
     * 获取借阅统计数据
     */
    public Map<String, Object> getBorrowStatistics() {
        List<BorrowRecord> records = getAllBorrowRecords();
        if (records == null) return null;

        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalBorrows", records.size());
        stats.put("activeBorrows", records.stream().filter(r -> "BORROWED".equals(r.getStatus())).count());
        stats.put("returnedBooks", records.stream().filter(r -> "RETURNED".equals(r.getStatus())).count());

        // 计算逾期数量 (这里需要根据实际业务逻辑实现)
        long overdueCount = records.stream()
                .filter(r -> "BORROWED".equals(r.getStatus()))
                .filter(r -> r.getReturnDate() != null && r.getReturnDate().before(new java.util.Date()))
                .count();
        stats.put("overdueBooks", overdueCount);

        return stats;
    }

    /**
     * 获取热门图书排行
     */
    public List<Map<String, Object>> getPopularBooks(int limit) {
        List<BorrowRecord> records = getAllBorrowRecords();
        List<Book> books = getAllBooks();

        if (records == null || books == null) return null;

        // 统计每本书的借阅次数
        Map<String, Long> borrowCount = records.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        BorrowRecord::getBookId,
                        java.util.stream.Collectors.counting()
                ));

        // 创建图书标题映射
        Map<String, String> bookTitles = books.stream()
                .collect(java.util.stream.Collectors.toMap(Book::getBookId, Book::getTitle));

        return borrowCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> item = new java.util.HashMap<>();
                    item.put("bookId", entry.getKey());
                    item.put("title", bookTitles.getOrDefault(entry.getKey(), "未知书名"));
                    item.put("borrowCount", entry.getValue());
                    return item;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取用户借阅活跃度
     */
    public List<Map<String, Object>> getUserBorrowActivity(int limit) {
        List<BorrowRecord> records = getAllBorrowRecords();
        if (records == null) return null;

        Map<String, Long> userBorrowCount = records.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        BorrowRecord::getUserId,
                        java.util.stream.Collectors.counting()
                ));

        return userBorrowCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> item = new java.util.HashMap<>();
                    item.put("userId", entry.getKey());
                    item.put("borrowCount", entry.getValue());
                    return item;
                })
                .collect(java.util.stream.Collectors.toList());
    }
}