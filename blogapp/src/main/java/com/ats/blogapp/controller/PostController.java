package com.ats.blogapp.controller;

import com.ats.blogapp.access.entity.*;
import com.ats.blogapp.service.interfaces.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// Controller Layer
@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    private final UserService userService;

    private final TagService tagService;

    private final CategoryService categoryService;

    private final CommentService commentService;

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    // BASE UPLOAD_DIR : Where the images located.
    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    // Constructor..
    @Autowired
    public PostController(PostService postService,
                          UserService userService,
                          TagService tagService,
                          CategoryService categoryService, CommentService commentService) {
        this.postService = postService;
        this.userService = userService;
        this.tagService = tagService;
        this.categoryService = categoryService;
        this.commentService = commentService;
    }

    // This method to retrieve a specific post by Id & it's comments (retrieve all post located in the HomeController)
    // In .. post-view page.
    @GetMapping("/{id}")
    public String viewPost(@PathVariable Long id, Model model){
        Optional<Post> postOpt = postService.findPostById(id); // This method retrieves the post with its comments.
        if(postOpt.isPresent()){
            Post post = postOpt.get();
            model.addAttribute("post", post);
            model.addAttribute("comment", new Comment()); // For Comments
            return "post-view";
        } else {
            // Handle post not found
            return "redirect:/home?error=PostNotFound";
        }
    }


    // This method to create or add a new comment for a specific post
    // In ..  post-view page.
    @PostMapping("/{id}/comments")
    public String addComment(@PathVariable Long id, @ModelAttribute("comment") Comment comment, Authentication authentication) {
        Optional<Post> postOpt = postService.findPostById(id);  /// finds without comments
        if (postOpt.isPresent()) {
            Post post = postOpt.get();

            // Get the currently authenticated user
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Ensure the comment is new
            comment.setId(null);

            // Set comment properties
            comment.setAuthor(user);
            comment.setPost(post);

            // Save the comment
            commentService.saveComment(comment);

            return "redirect:/posts/" + id;
        } else {
            // Handle post not found
            return "redirect:/home?error=PostNotFound";
        }
    }

    // This method to initialize a new Post() & get category & tags list as a dropdown menu to select them
    // When the user navigate to post-create page.
    // For .. user/post-create page.
    @GetMapping("/create")
    public String showCreatePostForm(Model model) {
        model.addAttribute("post", new Post());
        // Fetch categories and tags
        List<Category> categories = categoryService.findAllCategories();
        List<Tag> tags = tagService.findAllTags();
        // You need to fetch them again in
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);
        return "user/post-create";
    }

    // This method to create a new Post .. with an uploaded image, category, author,  and tags.
    // It's have a 4 section: authenticated user .. to determine who's user is post (it's must be authenticated user) and save it as author.
    // section: Handle image upload .. located the uploaded images in /uploads folder.
    // section: Handle Tags & category .. Select multiple tags and one category.
    // section: to save and create the new post .. If any errors occur you must re-fetch the categories & tags.
    // For .. user/post-create page.
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/create")
    public String createPost(@Validated @ModelAttribute("post") Post post,
                             BindingResult bindingResult,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             Authentication authentication,
                             @RequestParam(value = "tagIds", required = false) List<Long> tagIds,
                             Model model){

        if(bindingResult.hasErrors()){
            // If any errors occur you must re-fetch the categories & tags.
            List<Category> categories = categoryService.findAllCategories();
            List<Tag> tags = tagService.findAllTags();
            model.addAttribute("categories",categories);
            model.addAttribute("tags", tags);

            return "user/post-create";
        }

        // Get the currently authenticated user
        String username = authentication.getName(); // to get the username that created the post.
        User user = userService.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User Not Found!"));

        // Set the author of the post
        post.setAuthor(user);

        // Handle image upload
        if (!imageFile.isEmpty()) {
            String contentType = imageFile.getContentType();
            if (contentType != null && (contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
                try {
                    // Save the image and set the imagePath
                    String imagePath = saveImage(imageFile);
                    post.setImagePath(imagePath);
                } catch (IOException e) {
                    e.printStackTrace();
                    model.addAttribute("imageError", "Failed to upload image.");
                    return "user/post-create";
                }
            } else {
                model.addAttribute("imageError", "Only JPEG and PNG images are allowed.");
                return "user/post-create";
            }
        }

        // Set the selected category (Here you can select one category)
        if (post.getCategory() != null && post.getCategory().getId() != null) {
            Category category = categoryService.findCategoryById(post.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));
            post.setCategory(category);
        }

        // Set the selected tag (Here you can select multiple tags - its a many to many relation).
        if(tagIds != null && !tagIds.isEmpty()) {
            List<Tag> selectedTags = tagService.findTagsByIds(tagIds);
            post.setTags(new HashSet<>(selectedTags));
        } else {
            post.setTags(new HashSet<>());
        }

        // Save the post
        postService.savePost(post);

        // Redirect to the post's page if its an user.
        // Redirect to the admin/posts page if its an admin.
        // Determine user's role
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        if(isAdmin) {
            return "redirect:/admin/posts";
        } else {
            return "redirect:/posts/" + post.getId();
        }
    }

    // This method to save the uploaded images into (located them) /uploads folder.
    // Further work: change the uploaded image name by a method (Generate a name never used before)
    // to avoid images name conflict if the user upload image alrady exist as a name.
    public String saveImage(MultipartFile imageFile) throws IOException{
        // Normalize file name .. Why we need it.
        String fileName = StringUtils.cleanPath(imageFile.getOriginalFilename());

        // Create the uploads directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Resolve the file path
        Path filePath = uploadPath.resolve(fileName);

        // Copy the file to the target location (Replacing existing file with the same name)
        Files.copy(imageFile.getInputStream(), filePath);

        // Return the relative path to the image
        return "/uploads/" + fileName; // This must be handle by you not user .. provided a method to auto generate the file name.
    }

    // This must br in userController
    // This method to delete a specific post by ID.
    // '@PreAuthorize' This is important to ensure that the user is PreAuthorize() before access this method and delete the post.
    // It's ensures that only the author of the post can delete it. Give it the 'id' and the 'name' using 'authentication'.
    // In delete methods use RedirectAttributes instead of Model .. To pass messages after a redirect. or other methods that needs to pass messages after a redirect.
    @GetMapping("/delete/{id}")
    @PreAuthorize("@postSecurity.isPostAuthor(#id, authentication.name)")
    public String deletePost(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        if (postService.findPostById(id).isPresent()) {   //// find without comment
            postService.deletePostById(id);
            redirectAttributes.addFlashAttribute("message", "Post deleted successfully.");
            return "redirect:/user/posts";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Post not found!");
            return "redirect:/user/posts";
        }
    }

    // This must br in userController
    // Show Edit Post Form
    // This method to retrieve the post detail's to modify & get the selected category & tags list as a dropdown menu to modify them.
    // 'PreAuthorize' It's ensures that only the author of the post can update it.
    // For .. user/post-edit page.
    @GetMapping("/edit/{id}")
    @PreAuthorize("@postSecurity.isPostAuthor(#id, authentication.name)")
    public String showEditPostForm(@PathVariable Long id,Model model) {
        // Fetch post detail's (Ex: title, description ..)
        Post post = postService.findPostById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        model.addAttribute("post", post);

        // Fetch categories and tags for selection
        List<Category> categories = categoryService.findAllCategories();
        List<Tag> tags = tagService.findAllTags();

        model.addAttribute("categories",categories);
        model.addAttribute("tags", tags);

        return "user/post-edit";
    }


    // This must br in userController
    // This method to update Post.
    // It's ame of createPost but with little bit difference... Used a currentPost that want's to update and post(edit post).
    // 'PreAuthorize' It's ensures that only the author of the post can update it.
    @PostMapping("/edit/{id}")
    @PreAuthorize("@postSecurity.isPostAuthor(#id, authentication.name)")
    public String updatePost(@PathVariable Long id,
                             @Validated @ModelAttribute("post") Post post,
                             BindingResult bindingResult,
                             @RequestParam(value = "tagIds", required = false) List<Long> tagIds,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        if(bindingResult.hasErrors()){
            // If any errors occur you must re-fetch the categories & tags.
            List<Category> categories = categoryService.findAllCategories();
            List<Tag> tags = tagService.findAllTags();
            model.addAttribute("categories",categories);
            model.addAttribute("tags", tags);

            return "user/post-edit";
        }

        // Retrieve the current post that need to be edit.
        Post currentPost = postService.findPostById(id)   //// find without comments.
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // handle Update post fields
        currentPost.setTitle(post.getTitle());
        currentPost.setContent(post.getContent());

        // Handle image upload (To update the image if a new one is uploaded).
        // More Handle in the future(make the image in a class and handle it alone).

        // Handle category update.
        if (post.getCategory() != null && post.getCategory().getId() != null) {
            Category category = categoryService.findCategoryById(post.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));
            currentPost.setCategory(category);
        } else {
            currentPost.setCategory(null);
        }

        // Handle tags update.
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Tag> selectedTags = tagService.findTagsByIds(tagIds);
            currentPost.setTags(new HashSet<>(selectedTags));
        } else {
            currentPost.setTags(new HashSet<>());
        }

       // Save the updated post
        postService.savePost(currentPost);

        redirectAttributes.addFlashAttribute("message", "Post updated successfully.");
        return "redirect:/posts/" + currentPost.getId();
    }


}
