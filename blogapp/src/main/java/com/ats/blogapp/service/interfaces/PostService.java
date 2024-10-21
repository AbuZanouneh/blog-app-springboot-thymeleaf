package com.ats.blogapp.service.interfaces;

import com.ats.blogapp.access.entity.Category;
import com.ats.blogapp.access.entity.Post;
import com.ats.blogapp.access.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.Optional;

public interface PostService {

    Page<Post> findAllPosts(Pageable pageable);

    Optional<Post> findPostById(Long id);

    Post savePost(Post post);

    void deletePostById(Long id);

    Page<Post> findPostsByCategory(Category category, Pageable pageable);

    Page<Post> findPostsByTag(String tagName, Pageable pageable);

    Page<Post> findPostsByAuthor(User author, Pageable pageable);

}
