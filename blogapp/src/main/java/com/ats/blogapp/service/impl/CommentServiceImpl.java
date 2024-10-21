package com.ats.blogapp.service.impl;

import com.ats.blogapp.access.entity.Category;
import com.ats.blogapp.access.entity.Comment;
import com.ats.blogapp.access.entity.Post;
import com.ats.blogapp.access.entity.User;
import com.ats.blogapp.access.repository.CommentRepository;
import com.ats.blogapp.service.interfaces.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public List<Comment> findAllComments(){
        return commentRepository.findAll();
    }

    @Override
    public List<Comment> findCommentsByPost(Post post){
        return commentRepository.findByPost(post);
    }

    @Override
    public Optional<Comment> findCommentById(Long id){
        return commentRepository.findById(id);
    }

    @Override
    public Comment saveComment(Comment comment){
        // Business logic can be added here if needed
        return commentRepository.save(comment);
    }

    @Override
    public void deleteCommentById(Long id){
        commentRepository.deleteById(id);
    }

    @Override
    public Page<Comment> findListOfComments(Pageable pageable){
        return commentRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    public Page<Comment> findCommentsByAuthor(User author, Pageable pageable) {
        return commentRepository.findByAuthor(author, pageable);
    }

}
