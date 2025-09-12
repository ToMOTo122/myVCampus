package com.vcampus.server.service;

import com.vcampus.common.entity.Book;
import com.vcampus.common.entity.BorrowRecord;
import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.User;
import com.vcampus.common.util.DatabaseHelper;
import com.vcampus.common.entity.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LibraryService {

    public Message handleRequest(Message message, User currentUser) {
        Message.Type type = message.getType();
        try {
            switch (type) {
                case BOOK_SEARCH:
                    return handleBookSearch(message);
                case BOOK_BORROW:
                    return handleBookBorrow(message, currentUser);
                case BOOK_RETURN:
                    return handleBookReturn(message, currentUser);
                case BOOK_RENEW:
                    return handleRenewBook(message, currentUser);
                case BOOK_ADD:
                    return handleAddBook(message, currentUser);
                case BOOK_DELETE:
                    return handleDeleteBook(message, currentUser);
                case BOOK_UPDATE:
                    return handleUpdateBook(message, currentUser);
                case BOOK_LIST:
                    return handleBookList();
                case BORROW_RECORD_LIST:
                    return handleBorrowRecordList(currentUser);
                default:
                    return Message.error("不支持的图书馆操作");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Message.error("服务器内部错误: " + e.getMessage());
        }
    }

    private Message handleBookSearch(Message message) {
        String query = (String) message.getData();
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM tbl_book WHERE title LIKE ? OR author LIKE ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(extractBookFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            return Message.error("数据库查询失败: " + e.getMessage());
        }
        return Message.success(books);
    }

    private Message handleBookBorrow(Message message, User currentUser) {
        String bookId = (String) message.getData();
        String userId = currentUser.getUserId();

        String checkSql = "SELECT stock FROM tbl_book WHERE bookId = ?";
        String updateBookSql = "UPDATE tbl_book SET stock = stock - 1, status = ? WHERE bookId = ?";
        String insertRecordSql = "INSERT INTO tbl_borrow_record (bookId, userId, borrowDate, status) VALUES (?, ?, ?, 'BORROWED')";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement psCheck = conn.prepareStatement(checkSql);
             PreparedStatement psUpdateBook = conn.prepareStatement(updateBookSql);
             PreparedStatement psInsertRecord = conn.prepareStatement(insertRecordSql)) {

            psCheck.setString(1, bookId);
            ResultSet rs = psCheck.executeQuery();
            if (rs.next()) {
                int currentStock = rs.getInt("stock");
                if (currentStock > 0) {
                    String newStatus = (currentStock - 1 == 0) ? "已借完" : "可借阅";
                    psUpdateBook.setString(1, newStatus);
                    psUpdateBook.setString(2, bookId);
                    psUpdateBook.executeUpdate();

                    psInsertRecord.setString(1, bookId);
                    psInsertRecord.setString(2, userId);
                    psInsertRecord.setTimestamp(3, new Timestamp(new Date().getTime()));
                    psInsertRecord.executeUpdate();

                    return Message.success("图书借阅成功！");
                } else {
                    return Message.error("图书库存不足，无法借阅。");
                }
            } else {
                return Message.error("未找到该图书。");
            }
        } catch (SQLException e) {
            return Message.error("数据库操作失败: " + e.getMessage());
        }
    }

    private Message handleBookReturn(Message message, User currentUser) {
        Integer recordId = (Integer) message.getData();
        String userId = currentUser.getUserId();

        String findRecordSql = "SELECT bookId, status FROM tbl_borrow_record WHERE recordId = ? AND userId = ?";
        String updateBookSql = "UPDATE tbl_book SET stock = stock + 1, status = '可借阅' WHERE bookId = ?";
        String updateRecordSql = "UPDATE tbl_borrow_record SET returnDate = ?, status = 'RETURNED' WHERE recordId = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement psFindRecord = conn.prepareStatement(findRecordSql);
             PreparedStatement psUpdateBook = conn.prepareStatement(updateBookSql);
             PreparedStatement psUpdateRecord = conn.prepareStatement(updateRecordSql)) {

            psFindRecord.setInt(1, recordId);
            psFindRecord.setString(2, userId);
            ResultSet rs = psFindRecord.executeQuery();

            if (rs.next()) {
                String bookId = rs.getString("bookId");
                String status = rs.getString("status");

                if ("BORROWED".equals(status)) {
                    psUpdateBook.setString(1, bookId);
                    psUpdateBook.executeUpdate();

                    psUpdateRecord.setTimestamp(1, new Timestamp(new Date().getTime()));
                    psUpdateRecord.setInt(2, recordId);
                    psUpdateRecord.executeUpdate();

                    return Message.success("图书归还成功！");
                } else {
                    return Message.error("归还失败，该记录状态不正确。");
                }
            } else {
                return Message.error("归还失败，未找到该借阅记录。");
            }
        } catch (SQLException e) {
            return Message.error("数据库操作失败: " + e.getMessage());
        }
    }

    // vcampus-server/src/main/java/com/vcampus/server/service/LibraryService.java

    private Message handleBookList() {
        System.out.println("服务器正在处理 BOOK_LIST 请求...");
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM tbl_book";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("成功连接数据库并执行查询。");
            while (rs.next()) {
                Book book = new Book();
                book.setBookId(rs.getString("bookId"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setPublisher(rs.getString("publisher"));
                book.setStatus(rs.getString("status"));
                book.setStock(rs.getInt("stock"));
                books.add(book);
            }
            System.out.println("已获取 " + books.size() + " 本图书数据。");
        } catch (SQLException e) {
            System.err.println("数据库操作失败！错误信息：" + e.getMessage());
            e.printStackTrace();
            return Message.error("数据库查询图书列表失败：" + e.getMessage());
        }
        System.out.println("成功返回图书列表。");
        return Message.success(books);
    }

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

    // --- 管理员功能 ---
    private Message handleAddBook(Message message, User currentUser) {
        if (!"ADMIN".equals(currentUser.getRole())) {
            return Message.error("权限不足");
        }
        Book newBook = (Book) message.getData();
        String sql = "INSERT INTO tbl_book (bookId, title, author, publisher, status, stock) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newBook.getBookId());
            ps.setString(2, newBook.getTitle());
            ps.setString(3, newBook.getAuthor());
            ps.setString(4, newBook.getPublisher());
            ps.setString(5, newBook.getStatus());
            ps.setInt(6, newBook.getStock());
            int rowsAffected = ps.executeUpdate();
            return (rowsAffected > 0) ? Message.success("图书添加成功") : Message.error("图书添加失败");
        } catch (SQLException e) {
            return Message.error("数据库操作失败: " + e.getMessage());
        }
    }

    private Message handleDeleteBook(Message message, User currentUser) {
        if (!"ADMIN".equals(currentUser.getRole())) {
            return Message.error("权限不足");
        }
        String bookId = (String) message.getData();
        String sql = "DELETE FROM tbl_book WHERE bookId = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            int rowsAffected = ps.executeUpdate();
            return (rowsAffected > 0) ? Message.success("图书删除成功") : Message.error("未找到该图书");
        } catch (SQLException e) {
            return Message.error("数据库操作失败: " + e.getMessage());
        }
    }

    private Message handleUpdateBook(Message message, User currentUser) {
        if (!"ADMIN".equals(currentUser.getRole())) {
            return Message.error("权限不足");
        }
        Book updatedBook = (Book) message.getData();
        String sql = "UPDATE tbl_book SET title=?, author=?, publisher=?, status=?, stock=? WHERE bookId=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, updatedBook.getTitle());
            ps.setString(2, updatedBook.getAuthor());
            ps.setString(3, updatedBook.getPublisher());
            ps.setString(4, updatedBook.getStatus());
            ps.setInt(5, updatedBook.getStock());
            ps.setString(6, updatedBook.getBookId());
            int rowsAffected = ps.executeUpdate();
            return (rowsAffected > 0) ? Message.success("图书更新成功") : Message.error("未找到该图书");
        } catch (SQLException e) {
            return Message.error("数据库操作失败: " + e.getMessage());
        }
    }

    // --- 用户功能 ---
    private Message handleRenewBook(Message message, User currentUser) {
        Integer recordId = (Integer) message.getData();
        String userId = currentUser.getUserId();
        String sql = "UPDATE tbl_borrow_record SET returnDate = DATE_ADD(returnDate, INTERVAL 30 DAY) WHERE recordId = ? AND userId = ? AND status = 'BORROWED'";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recordId);
            ps.setString(2, userId);
            int rowsAffected = ps.executeUpdate();
            return (rowsAffected > 0) ? Message.success("图书续借成功") : Message.error("续借失败，请检查借阅状态或联系管理员");
        } catch (SQLException e) {
            return Message.error("数据库操作失败: " + e.getMessage());
        }
    }

    private Message handleBorrowRecordList(User currentUser) {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM tbl_borrow_record WHERE userId = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currentUser.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(extractBorrowRecordFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            return Message.error("数据库查询失败: " + e.getMessage());
        }
        return Message.success(records);
    }
}