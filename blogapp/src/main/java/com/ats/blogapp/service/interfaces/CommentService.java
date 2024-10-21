package com.ats.blogapp.service.interfaces;

import com.ats.blogapp.access.entity.Comment;
import com.ats.blogapp.access.entity.Post;
import com.ats.blogapp.access.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CommentService {

    List<Comment> findAllComments();

    List<Comment> findCommentsByPost(Post post);

    Optional<Comment> findCommentById(Long id);

    Comment saveComment(Comment comment);

    Page<Comment> findListOfComments(Pageable pageable);

    void deleteCommentById(Long id);

    Page<Comment> findCommentsByAuthor(User author, Pageable pageable);
}
