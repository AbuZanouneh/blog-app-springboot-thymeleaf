package com.ats.blogapp.security;

import com.ats.blogapp.access.entity.Comment;
import com.ats.blogapp.access.entity.Post;
import com.ats.blogapp.service.interfaces.CommentService;
import com.ats.blogapp.service.interfaces.PostService;
import org.springframework.stereotype.Component;

// 'PostSecurity' is Essential: To ensure that only the post's author can delete, edit, and change it.
@Component
public class PostSecurity {

    private final PostService postService;

    private final CommentService commentService;

    //Constructor
    public PostSecurity(PostService postService,
                        CommentService commentService) {
        this.postService = postService;
        this.commentService = commentService;
    }

    // This is method to ensure that only the author of a post can perform actions like's: editing or deleting the post.
    // Used In ... user/user-posts page.. In PostController .. DeletePost method.
    public boolean isPostAuthor(Long postId, String username){
        Post post = postService.findPostById(postId).orElse(null);;  //// Find without comments
        if(post == null){
            return false;  // check if the post found or not.
        }
        return post.getAuthor().getUsername().equals(username);
        // here check if author username in the Post is equal the username of the user,
        // that need to update or delete the post.
    }

    // Method to check if the user is the author of the comment
    public boolean isCommentAuthor(Long commentId, String username) {
        Comment comment = commentService.findCommentById(commentId).orElse(null);
        if (comment == null) {
            return false;
        }
        return comment.getAuthor().getUsername().equals(username);
    }
}
