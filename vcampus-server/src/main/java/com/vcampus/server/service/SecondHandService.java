//yhr9.14 22:11添加该类
package com.vcampus.server.service;

import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.SecondHandItem;
import com.vcampus.common.entity.User;
import com.vcampus.server.dao.SecondHandDao;

import java.util.List;

/**
 * 二手商品服务类
 * 负责处理二手商品业务逻辑
 */
public class SecondHandService {

    private final SecondHandDao secondHandDao = new SecondHandDao();

    /**
     * 处理二手商品相关请求
     */
    public Message handleRequest(Message message, User currentUser) {
        try {
            Message.Type type = message.getType();
            String studentId = currentUser.getUserId(); // 假设用户ID就是学生ID

            switch (type) {
                case SECOND_HAND_LIST:
                    List<SecondHandItem> allItems = secondHandDao.getAllItems();
                    return Message.success(allItems);

                case SECOND_HAND_SEARCH:
                    String keyword = (String) message.getData();
                    List<SecondHandItem> searchResults = secondHandDao.searchItems(keyword);
                    return Message.success(searchResults);

                case SECOND_HAND_POST:
                    SecondHandItem newItem = (SecondHandItem) message.getData();
                    newItem.setStudentId(studentId); // 确保发布者ID正确
                    boolean postSuccess = secondHandDao.postNewItem(newItem);
                    return postSuccess ? Message.success("商品发布成功") : Message.error("商品发布失败");

                case SECOND_HAND_MY_POSTS:
                    List<SecondHandItem> myPosts = secondHandDao.getMyPostedItems(studentId);
                    return Message.success(myPosts);

                case SECOND_HAND_WANT:
                    Integer itemId = (Integer) message.getData();
                    boolean wantSuccess = secondHandDao.addWantedItem(studentId, itemId);
                    return wantSuccess ? Message.success("成功添加到“想要”列表") : Message.error("添加“想要”失败");

                case SECOND_HAND_MY_WANTS:
                    List<SecondHandItem> myWants = secondHandDao.getMyWantedItems(studentId);
                    return Message.success(myWants);

                case SECOND_HAND_REMOVE_WANT:
                    Integer removeItemId = (Integer) message.getData();
                    boolean removeSuccess = secondHandDao.removeWantedItem(studentId, removeItemId);
                    return removeSuccess ? Message.success("成功移除“想要”") : Message.error("移除“想要”失败");

                default:
                    return Message.error("不支持的二手市场操作");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Message.error("服务器内部错误: " + e.getMessage());
        }
    }
}