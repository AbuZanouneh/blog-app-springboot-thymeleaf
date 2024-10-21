package com.ats.blogapp.controller;

import com.ats.blogapp.access.entity.Category;
import com.ats.blogapp.access.entity.Comment;
import com.ats.blogapp.access.entity.Tag;
import com.ats.blogapp.service.interfaces.CommentService;
import com.ats.blogapp.service.interfaces.PostService;
import com.ats.blogapp.service.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

// Controller Layer
@Controller
@RequestMapping("/admin/comments")
@PreAuthorize("hasRole('ADMIN')") // Ensures all methods require ADMIN role -- only admin can manage categories.
public class CommentController {

    private final CommentService commentService;

    private final PostService postService;

    private final UserService userService;

    @Autowired
    public CommentController(CommentService commentService,
                             PostService postService, UserService userService) {
        this.commentService = commentService;
        this.postService = postService;
        this.userService = userService;
    }

    // @GetMapping({"", "/"})  // include them both ({"", "/"}) or don't include ("/").
    @GetMapping
    public String viewAllComments(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    Model model){
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending()); // Sort here to sort the comment list at descending order.
        Page<Comment> commentPage = commentService.findListOfComments(pageable);   /// Find without ordering
        List<Comment> comments =  commentPage.getContent();

        model.addAttribute("comments", comments);
        model.addAttribute("currentPage", commentPage.getNumber());
        model.addAttribute("totalPages", commentPage.getTotalPages());
        model.addAttribute("totalItems", commentPage.getTotalElements());
        model.addAttribute("size", size);

        return "admin/admin-comments";
    }

    /**
     * Handle deletion of a comment
     */
    @GetMapping("/delete/{id}")
    public String deleteComment(@PathVariable Long id, RedirectAttributes redirectAttributes){
        Optional<Comment> commentOpt = commentService.findCommentById(id);
        if(commentOpt.isEmpty()){
            redirectAttributes.addFlashAttribute("errorMessage", "Comment not found.");
            return "redirect:/admin/comments";
        }

        commentService.deleteCommentById(id);
        redirectAttributes.addFlashAttribute("message", "Comment deleted successfully.");
        return "redirect:/admin/comments";
    }

    @GetMapping("/edit/{id}")
    public String showEditCommentForm(@PathVariable Long id,
                                      Authentication authentication,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        Optional<Comment> commentOpt = commentService.findCommentById(id);
        if(commentOpt.isEmpty()){
            redirectAttributes.addFlashAttribute("errorMessage", "Comment not found.");
            return "redirect:/admin/comments";
        }

        Comment comment = commentOpt.get();
        String username = authentication.getName();
        if(!comment.getAuthor().getUsername().equals(username)){
            redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to edit this comment.");
            return "redirect:/admin/comments";
        }

        model.addAttribute("comment", comment);
        return "admin/comment-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateComment(@PathVariable Long id,
                                @Validated @ModelAttribute("comment") Comment comment,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes,
                                Model model,
                                BindingResult bindingResult){

        if(bindingResult.hasErrors()){
            return "admin/comment-edit";
        }

        Optional<Comment> currentCommentOpt = commentService.findCommentById(id);
        if(currentCommentOpt.isEmpty()){
            redirectAttributes.addFlashAttribute("errorMessage", "Comment not found.");
            return "redirect:/admin/comments";
        }

        Comment currentComment = currentCommentOpt.get();
        String username = authentication.getName();
        if(!currentComment.getAuthor().getUsername().equals(username)){
            redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to edit this comment.");
            return "redirect:/admin/comments";
        }

        currentComment.setContent(comment.getContent());
        commentService.saveComment(currentComment);

        redirectAttributes.addFlashAttribute("message", "Comment updated successfully.");
        return "redirect:/admin/comments";
    }


}
