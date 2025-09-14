package com.vcampus.server.service; // <--- 放在您已有的service包

import com.vcampus.common.entity.Post;
import com.vcampus.server.dao.PostDAO;

import java.util.List;

public class PostService {

    private final PostDAO postDAO = new PostDAO();

    public List<Post> getAllPosts() {
        System.out.println("服务层：正在尝试获取所有帖子...");
        return postDAO.getAllPosts();
    }

    public boolean addPost(Post post) {
        System.out.println("服务层：正在尝试添加新帖子，标题：" + post.getTitle());
        return postDAO.addPost(post);
    }
}