package com.vcampus.server.service;

import com.vcampus.common.entity.Book;
import com.vcampus.common.entity.BorrowRecord;
import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.User;
import com.vcampus.common.util.DatabaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 修复后的图书馆服务类
 * 解决管理员无法获取图书数据的问题，并完善所有CRUD功能
 */
public class LibraryService {
    private Connection connection;

    public LibraryService() {
        // 默认构造函数
    }

    public LibraryService(Connection connection) {
        this.connection = connection;
    }

    /**
     * 处理图书馆相关请求的主入口方法
     */
    public Message handleRequest(Message message, User currentUser) {
        Message.Type type = message.getType();
        System.out.println("LibraryService 处理请求类型: " + type + ", 用户: " +
                (currentUser != null ? currentUser.getUserId() + "(" + currentUser.getUserType() + ")" : "null"));

        try {
            switch (type) {
                // 图书查询功能
                case BOOK_SEARCH:
                    return handleBookSearch(message);
                case BOOK_LIST:
                case BOOK_GET_ALL:  // 添加这个处理，确保管理员可以获取所有图书
                    return handleBookList();

                // 借阅相关功能
                case BOOK_BORROW:
                    return handleBookBorrow(message, currentUser);
                case BOOK_RETURN:
                    return handleBookReturn(message, currentUser);
                case BOOK_RENEW:
                    return handleRenewBook(message, currentUser);

                // 管理员图书管理功能
                case BOOK_ADD:
                    return handleAddBook(message, currentUser);
                case BOOK_DELETE:
                    return handleDeleteBook(message, currentUser);
                case BOOK_UPDATE:
                    return handleUpdateBook(message, currentUser);

                // 借阅记录功能
                case BORROW_RECORD_LIST:
                    return handleBorrowRecordList(currentUser);
                case BORROW_RECORD_GET_ALL:  // 修复：添加管理员获取所有借阅记录的功能
                    return handleGetAllBorrowRecords(currentUser);
                case BORROW_RECORD_RETURN:   // 修复：添加管理员强制归还功能
                    return handleForceReturn(message, currentUser);

                default:
                    System.err.println("LibraryService: 不支持的操作类型: " + type);
                    return Message.error("不支持的图书馆操作类型: " + type);
            }
        } catch (Exception e) {
            System.err.println("LibraryService 处理请求时发生异常: " + e.getMessage());
            e.printStackTrace();
            return Message.error("服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 处理图书搜索
     */
    private Message handleBookSearch(Message message) {
        String query = (String) message.getData();
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM tbl_book WHERE title LIKE ? OR author LIKE ? OR bookId LIKE ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String searchPattern = "%" + query + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(extractBookFromResultSet(rs));
                }
            }

            System.out.println("图书搜索完成，找到 " + books.size() + " 条记录");
            return Message.success(books);

        } catch (SQLException e) {
            System.err.println("图书搜索失败: " + e.getMessage());
            return Message.error("数据库查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有图书列表 - 修复管理员无法获取图书的问题
     */
    private Message handleBookList() {
        System.out.println("LibraryService: 开始处理获取图书列表请求...");
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM tbl_book ORDER BY bookId";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("LibraryService: 成功连接数据库并执行查询");
            while (rs.next()) {
                Book book = extractBookFromResultSet(rs);
                books.add(book);
            }

            System.out.println("LibraryService: 成功获取 " + books.size() + " 本图书数据");
            return Message.success(books);

        } catch (SQLException e) {
            System.err.println("LibraryService: 获取图书列表失败: " + e.getMessage());
            e.printStackTrace();
            return Message.error("数据库查询图书列表失败: " + e.getMessage());
        }
    }

    /**
     * 处理图书借阅
     */
    private Message handleBookBorrow(Message message, User currentUser) {
        if (currentUser == null) {
            return Message.error("用户未登录");
        }

        String bookId = (String) message.getData();
        String userId = currentUser.getUserId();

        // 检查用户是否已经借阅了这本书
        String checkBorrowedSql = "SELECT COUNT(*) FROM tbl_borrow_record WHERE bookId = ? AND userId = ? AND status = 'BORROWED'";
        String checkStockSql = "SELECT stock FROM tbl_book WHERE bookId = ?";
        String updateBookSql = "UPDATE tbl_book SET stock = stock - 1, status = CASE WHEN stock - 1 = 0 THEN '已借完' ELSE '可借阅' END WHERE bookId = ?";
        String insertRecordSql = "INSERT INTO tbl_borrow_record (bookId, userId, borrowDate, returnDate, status) VALUES (?, ?, ?, ?, 'BORROWED')";

        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false); // 开启事务

            // 检查是否已借阅
            try (PreparedStatement psCheck = conn.prepareStatement(checkBorrowedSql)) {
                psCheck.setString(1, bookId);
                psCheck.setString(2, userId);
                ResultSet rs = psCheck.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return Message.error("您已经借阅了这本书，请先归还后再借阅");
                }
            }

            // 检查库存
            try (PreparedStatement psStock = conn.prepareStatement(checkStockSql)) {
                psStock.setString(1, bookId);
                ResultSet rs = psStock.executeQuery();
                if (rs.next()) {
                    int currentStock = rs.getInt("stock");
                    if (currentStock <= 0) {
                        return Message.error("图书库存不足，无法借阅");
                    }
                } else {
                    return Message.error("未找到该图书");
                }
            }

            // 更新图书库存
            try (PreparedStatement psUpdate = conn.prepareStatement(updateBookSql)) {
                psUpdate.setString(1, bookId);
                psUpdate.executeUpdate();
            }

            // 插入借阅记录
            try (PreparedStatement psInsert = conn.prepareStatement(insertRecordSql)) {
                Date now = new Date();
                Date returnDate = new Date(now.getTime() + 30L * 24 * 60 * 60 * 1000); // 30天后归还

                psInsert.setString(1, bookId);
                psInsert.setString(2, userId);
                psInsert.setTimestamp(3, new Timestamp(now.getTime()));
                psInsert.setTimestamp(4, new Timestamp(returnDate.getTime()));
                psInsert.executeUpdate();
            }

            conn.commit(); // 提交事务
            System.out.println("用户 " + userId + " 成功借阅图书 " + bookId);
            return Message.success("图书借阅成功！预计归还日期：" + new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000));

        } catch (SQLException e) {
            System.err.println("图书借阅失败: " + e.getMessage());
            return Message.error("数据库操作失败: " + e.getMessage());
        }
    }

    /**
     * 处理图书归还
     */
    private Message handleBookReturn(Message message, User currentUser) {
        if (currentUser == null) {
            return Message.error("用户未登录");
        }

        Integer recordId = (Integer) message.getData();
        String userId = currentUser.getUserId();

        String findRecordSql = "SELECT bookId, status FROM tbl_borrow_record WHERE recordId = ? AND userId = ?";
        String updateBookSql = "UPDATE tbl_book SET stock = stock + 1, status = '可借阅' WHERE bookId = ?";
        String updateRecordSql = "UPDATE tbl_borrow_record SET returnDate = ?, status = 'RETURNED' WHERE recordId = ?";

        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false); // 开启事务

            String bookId = null;
            // 查找借阅记录
            try (PreparedStatement psFindRecord = conn.prepareStatement(findRecordSql)) {
                psFindRecord.setInt(1, recordId);
                psFindRecord.setString(2, userId);
                ResultSet rs = psFindRecord.executeQuery();

                if (rs.next()) {
                    bookId = rs.getString("bookId");
                    String status = rs.getString("status");

                    if (!"BORROWED".equals(status)) {
                        return Message.error("归还失败，该记录状态不正确");
                    }
                } else {
                    return Message.error("归还失败，未找到该借阅记录");
                }
            }

            // 更新图书库存
            try (PreparedStatement psUpdateBook = conn.prepareStatement(updateBookSql)) {
                psUpdateBook.setString(1, bookId);
                psUpdateBook.executeUpdate();
            }

            // 更新借阅记录
            try (PreparedStatement psUpdateRecord = conn.prepareStatement(updateRecordSql)) {
                psUpdateRecord.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                psUpdateRecord.setInt(2, recordId);
                psUpdateRecord.executeUpdate();
            }

            conn.commit(); // 提交事务
            System.out.println("用户 " + userId + " 成功归还图书 " + bookId);
            return Message.success("图书归还成功！");

        } catch (SQLException e) {
            System.err.println("图书归还失败: " + e.getMessage());
            return Message.error("数据库操作失败: " + e.getMessage());
        }
    }

    /**
     * 处理图书续借
     */
    private Message handleRenewBook(Message message, User currentUser) {
        if (currentUser == null) {
            return Message.error("用户未登录");
        }

        Integer recordId = (Integer) message.getData();
        String userId = currentUser.getUserId();

        String checkSql = "SELECT status, returnDate FROM tbl_borrow_record WHERE recordId = ? AND userId = ?";
        String renewSql = "UPDATE tbl_borrow_record SET returnDate = DATE_ADD(returnDate, INTERVAL 30 DAY) WHERE recordId = ? AND userId = ? AND status = 'BORROWED'";

        try (Connection conn = DatabaseHelper.getConnection()) {
            // 检查记录状态
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, recordId);
                psCheck.setString(2, userId);
                ResultSet rs = psCheck.executeQuery();

                if (rs.next()) {
                    String status = rs.getString("status");
                    if (!"BORROWED".equals(status)) {
                        return Message.error("续借失败，该图书已归还或状态异常");
                    }
                } else {
                    return Message.error("续借失败，未找到该借阅记录");
                }
            }

            // 执行续借
            try (PreparedStatement psRenew = conn.prepareStatement(renewSql)) {
                psRenew.setInt(1, recordId);
                psRenew.setString(2, userId);
                int rowsAffected = psRenew.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("用户 " + userId + " 成功续借记录 " + recordId);
                    return Message.success("图书续借成功，借阅期延长30天");
                } else {
                    return Message.error("续借失败，请检查借阅状态");
                }
            }

        } catch (SQLException e) {
            System.err.println("图书续借失败: " + e.getMessage());
            return Message.error("数据库操作失败: " + e.getMessage());
        }
    }

    /**
     * 管理员添加图书
     */
    private Message handleAddBook(Message message, User currentUser) {
        if (!checkAdminPermission(currentUser)) {
            return Message.error("权限不足，只有管理员可以添加图书");
        }

        Book newBook = (Book) message.getData();
        if (newBook == null) {
            return Message.error("图书数据为空");
        }

        // 检查图书ID是否已存在
        String checkSql = "SELECT COUNT(*) FROM tbl_book WHERE bookId = ?";
        String insertSql = "INSERT INTO tbl_book (bookId, title, author, publisher, status, stock) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseHelper.getConnection()) {
            // 检查重复
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setString(1, newBook.getBookId());
                ResultSet rs = psCheck.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return Message.error("图书ID已存在，请使用其他ID");
                }
            }

            // 插入新图书
            try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                psInsert.setString(1, newBook.getBookId());
                psInsert.setString(2, newBook.getTitle());
                psInsert.setString(3, newBook.getAuthor());
                psInsert.setString(4, newBook.getPublisher() != null ? newBook.getPublisher() : "");
                psInsert.setString(5, newBook.getStatus() != null ? newBook.getStatus() : "可借阅");
                psInsert.setInt(6, newBook.getStock());

                int rowsAffected = psInsert.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("管理员 " + currentUser.getUserId() + " 成功添加图书: " + newBook.getBookId());
                    return Message.success("图书添加成功");
                } else {
                    return Message.error("图书添加失败");
                }
            }

        } catch (SQLException e) {
            System.err.println("添加图书失败: " + e.getMessage());
            return Message.error("数据库操作失败: " + e.getMessage());
        }
    }

    /**
     * 管理员删除图书
     */
    private Message handleDeleteBook(Message message, User currentUser) {
        if (!checkAdminPermission(currentUser)) {
            return Message.error("权限不足，只有管理员可以删除图书");
        }

        String bookId = (String) message.getData();
        if (bookId == null || bookId.trim().isEmpty()) {
            return Message.error("图书ID不能为空");
        }

        // 检查是否有未归还的借阅记录
        String checkBorrowSql = "SELECT COUNT(*) FROM tbl_borrow_record WHERE bookId = ? AND status = 'BORROWED'";
        String deleteSql = "DELETE FROM tbl_book WHERE bookId = ?";

        try (Connection conn = DatabaseHelper.getConnection()) {
            // 检查借阅记录
            try (PreparedStatement psCheck = conn.prepareStatement(checkBorrowSql)) {
                psCheck.setString(1, bookId);
                ResultSet rs = psCheck.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return Message.error("该图书还有未归还的借阅记录，无法删除");
                }
            }

            // 删除图书
            try (PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {
                psDelete.setString(1, bookId);
                int rowsAffected = psDelete.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("管理员 " + currentUser.getUserId() + " 成功删除图书: " + bookId);
                    return Message.success("图书删除成功");
                } else {
                    return Message.error("未找到该图书");
                }
            }

        } catch (SQLException e) {
            System.err.println("删除图书失败: " + e.getMessage());
            return Message.error("数据库操作失败: " + e.getMessage());
        }
    }

    /**
     * 管理员更新图书信息
     */
    private Message handleUpdateBook(Message message, User currentUser) {
        if (!checkAdminPermission(currentUser)) {
            return Message.error("权限不足，只有管理员可以更新图书信息");
        }

        Book updatedBook = (Book) message.getData();
        if (updatedBook == null) {
            return Message.error("图书数据为空");
        }

        String updateSql = "UPDATE tbl_book SET title=?, author=?, publisher=?, status=?, stock=? WHERE bookId=?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {

            ps.setString(1, updatedBook.getTitle());
            ps.setString(2, updatedBook.getAuthor());
            ps.setString(3, updatedBook.getPublisher() != null ? updatedBook.getPublisher() : "");
            ps.setString(4, updatedBook.getStatus() != null ? updatedBook.getStatus() : "可借阅");
            ps.setInt(5, updatedBook.getStock());
            ps.setString(6, updatedBook.getBookId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("管理员 " + currentUser.getUserId() + " 成功更新图书: " + updatedBook.getBookId());
                return Message.success("图书信息更新成功");
            } else {
                return Message.error("未找到该图书");
            }

        } catch (SQLException e) {
            System.err.println("更新图书失败: " + e.getMessage());
            return Message.error("数据库操作失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的借阅记录
     */
    private Message handleBorrowRecordList(User currentUser) {
        if (currentUser == null) {
            return Message.error("用户未登录");
        }

        List<BorrowRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM tbl_borrow_record WHERE userId = ? ORDER BY borrowDate DESC";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, currentUser.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(extractBorrowRecordFromResultSet(rs));
                }
            }

            System.out.println("用户 " + currentUser.getUserId() + " 的借阅记录查询完成，共 " + records.size() + " 条记录");
            return Message.success(records);

        } catch (SQLException e) {
            System.err.println("查询借阅记录失败: " + e.getMessage());
            return Message.error("数据库查询失败: " + e.getMessage());
        }
    }

    /**
     * 管理员获取所有借阅记录
     */
    private Message handleGetAllBorrowRecords(User currentUser) {
        if (!checkAdminPermission(currentUser)) {
            return Message.error("权限不足，只有管理员可以查看所有借阅记录");
        }

        List<BorrowRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM tbl_borrow_record ORDER BY borrowDate DESC";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                records.add(extractBorrowRecordFromResultSet(rs));
            }

            System.out.println("管理员 " + currentUser.getUserId() + " 查询所有借阅记录完成，共 " + records.size() + " 条记录");
            return Message.success(records);

        } catch (SQLException e) {
            System.err.println("查询所有借阅记录失败: " + e.getMessage());
            return Message.error("数据库查询失败: " + e.getMessage());
        }
    }

    /**
     * 管理员强制归还图书
     */
    private Message handleForceReturn(Message message, User currentUser) {
        if (!checkAdminPermission(currentUser)) {
            return Message.error("权限不足，只有管理员可以强制归还图书");
        }

        Integer recordId = (Integer) message.getData();
        if (recordId == null) {
            return Message.error("记录ID不能为空");
        }

        String findRecordSql = "SELECT bookId, status, userId FROM tbl_borrow_record WHERE recordId = ?";
        String updateBookSql = "UPDATE tbl_book SET stock = stock + 1, status = '可借阅' WHERE bookId = ?";
        String updateRecordSql = "UPDATE tbl_borrow_record SET returnDate = ?, status = 'RETURNED' WHERE recordId = ?";

        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false); // 开启事务

            String bookId = null;
            String borrowUserId = null;

            // 查找借阅记录
            try (PreparedStatement psFindRecord = conn.prepareStatement(findRecordSql)) {
                psFindRecord.setInt(1, recordId);
                ResultSet rs = psFindRecord.executeQuery();

                if (rs.next()) {
                    bookId = rs.getString("bookId");
                    String status = rs.getString("status");
                    borrowUserId = rs.getString("userId");

                    if (!"BORROWED".equals(status)) {
                        return Message.error("该记录已经归还或状态异常");
                    }
                } else {
                    return Message.error("未找到该借阅记录");
                }
            }

            // 更新图书库存
            try (PreparedStatement psUpdateBook = conn.prepareStatement(updateBookSql)) {
                psUpdateBook.setString(1, bookId);
                psUpdateBook.executeUpdate();
            }

            // 更新借阅记录
            try (PreparedStatement psUpdateRecord = conn.prepareStatement(updateRecordSql)) {
                psUpdateRecord.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                psUpdateRecord.setInt(2, recordId);
                psUpdateRecord.executeUpdate();
            }

            conn.commit(); // 提交事务
            System.out.println("管理员 " + currentUser.getUserId() + " 强制归还了用户 " + borrowUserId + " 的图书 " + bookId);
            return Message.success("强制归还成功");

        } catch (SQLException e) {
            System.err.println("强制归还失败: " + e.getMessage());
            return Message.error("数据库操作失败: " + e.getMessage());
        }
    }

    /**
     * 从ResultSet提取图书对象
     */
    private Book extractBookFromResultSet(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setBookId(rs.getString("bookId"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setPublisher(rs.getString("publisher"));
        book.setStatus(rs.getString("status"));
        book.setStock(rs.getInt("stock"));
        return book;
    }

    /**
     * 从ResultSet提取借阅记录对象
     */
    private BorrowRecord extractBorrowRecordFromResultSet(ResultSet rs) throws SQLException {
        BorrowRecord record = new BorrowRecord();
        record.setRecordId(rs.getInt("recordId"));
        record.setBookId(rs.getString("bookId"));
        record.setUserId(rs.getString("userId"));
        record.setBorrowDate(rs.getTimestamp("borrowDate"));
        record.setReturnDate(rs.getTimestamp("returnDate"));
        record.setStatus(rs.getString("status"));
        return record;
    }

    /**
     * 检查管理员权限
     */
    private boolean checkAdminPermission(User currentUser) {
        if (currentUser == null) {
            System.err.println("权限检查失败: 用户未登录");
            return false;
        }

        boolean isAdmin = "admin".equals(currentUser.getUserType());
        System.out.println("权限检查: 用户 " + currentUser.getUserId() +
                " 角色: " + currentUser.getUserType() + " 是否管理员: " + isAdmin);
        return isAdmin;
    }
}