package com.ats.blogapp.access.repository;

import com.ats.blogapp.access.entity.Comment;
import com.ats.blogapp.access.entity.Post;
import com.ats.blogapp.access.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Data Access Layer
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Find comments by post.
    List<Comment> findByPost(Post post);

    Page<Comment> findAllByOrderByCreatedAtDesc(Pageable pageable); // -- with pagination.

    // Add method to find comments by author
    Page<Comment> findByAuthor(User author, Pageable pageable);
}
