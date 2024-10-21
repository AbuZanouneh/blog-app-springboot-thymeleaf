package com.ats.blogapp.controller;

import com.ats.blogapp.access.entity.Comment;
import com.ats.blogapp.access.entity.Post;
import com.ats.blogapp.access.entity.User;
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

import java.util.List;
import java.util.Optional;

// Controller Layer
@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    private final PostService postService;

    private final CommentService commentService;

    @Autowired
    public UserController(UserService userService,
                          PostService postService,
                          CommentService commentService) {
        this.userService = userService;
        this.postService = postService;
        this.commentService = commentService;
    }


    // To view the .. user/dashboard page.
    @GetMapping("/dashboard")
    public String userDashboard(){
        return "user/dashboard";
    }

    // retrieve all info for the Authenticated user ... who's just logged in.
    // In .... user/profile page.
    @GetMapping("/profile")
    public String userProfile(Authentication authentication, Model model){
        // From authentication you can get the user name & password.
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);

        if(userOpt.isPresent()){
            User user = userOpt.get();
            model.addAttribute("user", user); // user info model.
            return "user/profile";
        } else {
            // Handle the case where the user is not found.
            return "redirect:/login?error";
        }
    }

    // To retrieve all post's that created for a specific user to manage them... same as in homeController but add findByAuthor().
    // Use the currently authenticated user to retrieve post's ... use his ID or the user Obj.
    // In .... user/user-posts page.
    @GetMapping("/posts")
    public String viewUserPosts(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Authentication authentication,
                                Model model){

        // Get the currently authenticated user
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found!"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending()); // Sort here to sort the Post list at descending order.
        Page<Post> postPage = postService.findPostsByAuthor(user, pageable); // Fetch all posts in descending mode.
        List<Post> posts = postPage.getContent(); // Extract the list of posts for the current page.

        model.addAttribute("posts", posts);
        model.addAttribute("currentPage", postPage.getNumber());
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("totalItems", postPage.getTotalElements());
        model.addAttribute("size", size);


        return "user/user-posts";

    }

    // Show Edit Profile Form ... In profile-edit page.
    // No need to use '@PreAuthorize'.
    @GetMapping("/edit")
    public String showEditProfileForm(Authentication authentication,Model model){

        // Get the currently authenticated user
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found!"));

        model.addAttribute("user", user);

        return "user/profile-edit";
    }


    // Handle Edit Profile Submission
    // To update the current user profile who's logged into the system.
    // Same as deletePost, createPost ... and etc.
    // In profile-edit page.
   @PostMapping("/edit")
   public String updateProfile(Authentication authentication,
                               @Validated @ModelAttribute("user") User user,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {

       if (bindingResult.hasErrors()) {
           // If any errors occur you must re-fresh the page - redirect.
           return "user/profile-edit";
       }

       // Get the currently authenticated user that need to modify his profile.
       String username = authentication.getName();
       User currentUser = userService.findByUsername(username)
               .orElseThrow(() -> new IllegalArgumentException("User not found!"));


       currentUser.setEmail(user.getEmail());
       currentUser.setFirstName(user.getFirstName());
       currentUser.setLastName(user.getLastName());

       userService.saveUser(currentUser);

       redirectAttributes.addFlashAttribute("message", "Profile updated successfully.");
       return "redirect:/user/profile";
    }

    // To retrieve the list of comments that related to the authenticated user to manage them.
    // In .. user-comments page.

    @GetMapping("/comments")
    public String viewAllComments(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  Authentication authentication,
                                  Model model){
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found!"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending()); // Sort here to sort the comment list at descending order.
        Page<Comment> commentPage = commentService.findCommentsByAuthor(user, pageable);   /// Find without ordering
        List<Comment> comments =  commentPage.getContent();

        model.addAttribute("comments", comments);
        model.addAttribute("currentPage", commentPage.getNumber());
        model.addAttribute("totalPages", commentPage.getTotalPages());
        model.addAttribute("totalItems", commentPage.getTotalElements());
        model.addAttribute("size", size);

        return "user/user-comments";
    }

    // Show Edit Comment Form
    @GetMapping("/comments/edit/{id}")
    public String showEditCommentForm(@PathVariable Long id,
                                      Authentication authentication,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {

        Optional<Comment> commentOpt = commentService.findCommentById(id);
        if (commentOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Comment not found.");
            return "redirect:/user/comments";
        }
        Comment comment = commentOpt.get();
        // Ensure the authenticated user is the author
        String username = authentication.getName();
        if (!comment.getAuthor().getUsername().equals(username)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to edit this comment.");
            return "redirect:/user/comments";
        }

        model.addAttribute("comment", comment);
        return "user/comment-edit";
    }

    // Handle Edit Comment Submission
    @PostMapping("/comments/edit/{id}")
    public String updateComment(@PathVariable Long id,
                                @Validated @ModelAttribute("comment") Comment comment,
                                BindingResult bindingResult,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "user/comment-edit";
        }

        Optional<Comment> existingCommentOpt = commentService.findCommentById(id);
        if (existingCommentOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Comment not found.");
            return "redirect:/user/comments";
        }

        Comment currentComment = existingCommentOpt.get();

        // Ensure the authenticated user is the author
        String username = authentication.getName();
        if (!currentComment.getAuthor().getUsername().equals(username)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to edit this comment.");
            return "redirect:/user/comments";
        }

        currentComment.setContent(comment.getContent());
        commentService.saveComment(currentComment);

        redirectAttributes.addFlashAttribute("message", "Comment updated successfully.");
        return "redirect:/user/comments";
    }

    // Delete Comment
    @GetMapping("/comments/delete/{id}")
    @PreAuthorize("@postSecurity.isCommentAuthor(#id, authentication.name)")
    public String deleteComment(@PathVariable Long id,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {

        Optional<Comment> commentOpt = commentService.findCommentById(id);
        if (commentOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Comment not found.");
            return "redirect:/user/comments";
        }

        Comment comment = commentOpt.get();

        // Ensure the authenticated user is the author
        String username = authentication.getName();
        if (!comment.getAuthor().getUsername().equals(username)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to delete this comment.");
            return "redirect:/user/comments";
        }

        commentService.deleteCommentById(id);

        redirectAttributes.addFlashAttribute("message", "Comment deleted successfully.");
        return "redirect:/user/comments";
    }


}
