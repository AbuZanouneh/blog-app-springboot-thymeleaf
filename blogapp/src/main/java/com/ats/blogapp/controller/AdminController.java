package com.ats.blogapp.controller;

import com.ats.blogapp.access.entity.*;
import com.ats.blogapp.service.interfaces.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

// Controller Layer
@PreAuthorize("hasRole('ADMIN')") // Ensures all methods require ADMIN role
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final PostService postService;

    private final UserService userService;

    private final CategoryService categoryService;

    private final TagService tagService;

    private final RoleService roleService;

    // BASE UPLOAD_DIR : Where the images located.
    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    // Need's to encrypt the password to store it in db.
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminController(PostService postService,
                           UserService userService,
                           CategoryService categoryService,
                           TagService tagService,
                           RoleService roleService,
                           PasswordEncoder passwordEncoder) {
        this.postService = postService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    // To view the .. admin/dashboard page.
    @GetMapping("/dashboard")
    public String adminDashboard(){
        return "admin/dashboard";
    }

    // Add admin profile here  .. to manage post's and user's.

    // retrieve all info for the Authenticated admin ... who's just logged in.
    // In .... admin/profile page.
    @GetMapping("/profile")
    public String adminProfile(Authentication authentication, Model model){
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);

        if(userOpt.isPresent()){
            User user = userOpt.get();
            model.addAttribute("user" , user);
            return  "admin/profile";

        } else {
            // Handle the case where the user is not found.
            return "redirect:/login?error";
        }
    }

    // Show Edit Profile Form ... In admin/profile-edit page.
    // Same method in userController.
    @GetMapping("/edit")
    public String showEditProfileForm(Authentication authentication,
                                      Model model){
        // Get the currently authenticated user
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User Not Found!"));

        model.addAttribute("user", user);
        return "admin/profile-edit";
    }

    @PostMapping("/edit")
    public String updateProfile(Authentication authentication,
                                @Validated @ModelAttribute("user") User user,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes){

        if(bindingResult.hasErrors()){
            return "redirect:/admin/profile-edit";
        }
        // Get the currently authenticated admin that need to modify his profile.
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username).
                orElseThrow(() -> new IllegalArgumentException("User not Found!"));

        // Check if the new username is already taken before update it.... You can add it in User page.
        if (!currentUser.getUsername().equals(user.getUsername())) {
            boolean usernameExists = userService.existsByUsername(user.getUsername());
            if (usernameExists) {
                bindingResult.rejectValue("username", "error.user", "Username is already taken.");
                return "admin/profile-edit";
            }
        }

        // Fields to update.
        currentUser.setUsername(user.getUsername());
        currentUser.setFirstName(user.getFirstName());
        currentUser.setLastName(user.getLastName());
        currentUser.setEmail(user.getEmail());

        userService.saveUser(currentUser);

        redirectAttributes.addFlashAttribute("message", "Profile updated successfully.");
        return "redirect:/admin/profile";
    }

    // To retrieve all post's to manage them... same as in userController but add findAll().
    // No need to use the currently authenticated admin to retrieve post's ... We retrieve them all.
    // In .... admin/admin-posts page.
    @GetMapping("/posts")
    public String viewAllPosts(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Model model){
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending()); // Sort here to sort the Post list at descending order.
        Page<Post> postPage = postService.findAllPosts(pageable);   /// Find without ordering
        List<Post> posts =  postPage.getContent();

        model.addAttribute("posts", posts);
        model.addAttribute("currentPage", postPage.getNumber());
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("totalItems", postPage.getTotalElements());
        model.addAttribute("size", size);


        return "admin/admin-posts";
    }


    // This method to delete a specific post by ID.
    // In delete methods use RedirectAttributes instead of Model .. To pass messages after a redirect.
    @GetMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        if (postService.findPostById(id).isPresent()) {   /// Find without comments
            postService.deletePostById(id);
            redirectAttributes.addFlashAttribute("message", "Post deleted successfully.");
            return "redirect:/admin/posts";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Post not found!");
            return "redirect:/admin/posts";
        }
    }

    // This method to initialize a new Post() & get category & tags list as a dropdown menu to select them
    // For .. admin/post-create page.
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

    // In create post, you can depend on the create method in postController .. but you must defined showCreatePostForm() in adminController.
    // This method to create a new Post .. with an uploaded image, category, author,  and tags.
    // For .. admin/post-create page.
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

            return "admin/post-create";
        }

        // Get the currently authenticated user.
        String username = authentication.getName(); // to get the username that created the post.
        User user = userService.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User Not Found!"));

        // Set the author of the post.
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
                    return "admin/post-create";
                }
            } else {
                model.addAttribute("imageError", "Only JPEG and PNG images are allowed.");
                return "admin/post-create";
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
        // Redirect to the post's page
        return "redirect:/admin/posts";

    }

    // This method to save the uploaded images into uploads folder (located them into uploads folder).
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

    // Show Edit Post Form
    // This method to retrieve the post detail's to modify & get the selected category & tags list as a dropdown menu to modify them.
    // For .. admin/post-edit page.
    @GetMapping("/edit/{id}")
    public String showEditPostForm(@PathVariable Long id, Model model) {
        // Fetch the post to edit
        Post post = postService.findPostById(id)  /// find without comments
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        model.addAttribute("post", post);

        // Fetch categories and tags for selection
        List<Category> categories = categoryService.findAllCategories();
        List<Tag> tags = tagService.findAllTags();

        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);

        return "admin/post-edit"; // Use admin-specific edit template
    }

    // Handle Edit Post Submission
    @PostMapping("/edit/{id}")
    public String updatePost(@PathVariable Long id,
                             @Validated @ModelAttribute("post") Post post,
                             BindingResult bindingResult,
                             @RequestParam(value = "tagIds", required = false) List<Long> tagIds,
                             Model model,
                             RedirectAttributes redirectAttributes){

        if(bindingResult.hasErrors()){
            // If any errors occur, re-fetch the categories & tags.
            List<Category> categories = categoryService.findAllCategories();
            List<Tag> tags = tagService.findAllTags();
            model.addAttribute("categories", categories);
            model.addAttribute("tags", tags);

            return "admin/post-edit"; // Use admin-specific template
        }

        // Retrieve the existing post
        Post existingPost = postService.findPostById(id)  //// find without comments
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // Update post fields except for the image
        existingPost.setTitle(post.getTitle());
        existingPost.setContent(post.getContent());

        // Handle category update
        if (post.getCategory() != null && post.getCategory().getId() != null) {
            Category category = categoryService.findCategoryById(post.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));
            existingPost.setCategory(category);
        } else {
            existingPost.setCategory(null);
        }

        // Handle tags update
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Tag> selectedTags = tagService.findTagsByIds(tagIds);
            existingPost.setTags(new HashSet<>(selectedTags));
        } else {
            existingPost.setTags(new HashSet<>());
        }

        // Note: Image is not updated here

        // Save the updated post
        postService.savePost(existingPost);

        redirectAttributes.addFlashAttribute("message", "Post updated successfully.");
        return "redirect:/admin/posts";
    }

    @GetMapping("/users")
    public String viewAllUsers(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               Model model){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending()); // Sort here to sort the Post list at descending order.
        Page<User> userPage = userService.findAllUsers(pageable);
        List<User> users = userPage.getContent();

        model.addAttribute("users", users);
        model.addAttribute("currentPage", userPage.getNumber());
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("size", size);

        return "admin/admin-users";

    }

    // Show registration form .. to add user's or Admin's.
    // In .. admin/register page.
    @GetMapping("/register")
    public String showRegistrationForm(Model model){
        model.addAttribute("user", new User());

        List<Role> roles = roleService.findAllRoles();
        model.addAttribute("roles", roles);

        return "admin/register";
    }

    // Handle registration form submission .. To create a new user with 'ROLE_USER' role or a new admin with 'ROLE_ADMIN'.
    // In .. admin/register page.
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") @Validated User user,
                               BindingResult result,
                               @RequestParam("role") String roleName,
                               Model model,
                               RedirectAttributes redirectAttributes){

        // Check for validation errors
        if(result.hasErrors()){
            // logger.warn("Validation errors occurred while registering user: {}", user.getUsername());
            List<Role> roles = roleService.findAllRoles();
            model.addAttribute("roles", roles);
            return "admin/register";
        }

        // Check if username already exists
        Optional<User> existingUser = userService.findByUsername(user.getUsername());
        if(existingUser.isPresent()){
            model.addAttribute("usernameError", "Username is already taken.");
            List<Role> roles = roleService.findAllRoles();
            model.addAttribute("roles", roles);
            return "admin/register";
        }

        // Check if email already exists
        existingUser = userService.findByEmail(user.getEmail());
        if(existingUser.isPresent()){
            model.addAttribute("emailError", "Email is already registered.");
            List<Role> roles = roleService.findAllRoles();
            model.addAttribute("roles", roles);
            return "admin/register";
        }

        // Encode the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // logger.info("Password for user '{}' encoded successfully.", user.getUsername());

        // Assign the selected role to the user
        try {
            Role selectedRole = roleService.findRoleByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Selected role not found."));
            user.setRole(selectedRole);
            // logger.info("Assigned role '{}' to user '{}'.", selectedRole.getName(), user.getUsername());
        } catch(Exception e){
            // logger.error("Error assigning role to user '{}': {}", user.getUsername(), e.getMessage());
            model.addAttribute("roleError", "Invalid role selected.");
            List<Role> roles = roleService.findAllRoles();
            model.addAttribute("roles", roles);
            return "admin/register";
        }

        // Save the user to the database
        userService.saveUser(user);
        // logger.info("User '{}' registered successfully with role '{}'.", user.getUsername(), user.getRole().getName());

        // Add a success message as a flash attribute
        redirectAttributes.addFlashAttribute("registerSuccess", "User/Admin created successfully.");

        // Redirect to the users list page
        return "redirect:/admin/users";
    }

    // Show Edit User/Admin Form
    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes){
        Optional<User> userOpt = userService.findUserById(id);
        if(userOpt.isEmpty()){
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/admin/users";
        }

        User user = userOpt.get();
        model.addAttribute("user", user);
        List<Role> roles = roleService.findAllRoles();
        model.addAttribute("roles", roles);
        return "admin/user-edit"; // Updated template name
    }

    // Handle Edit User/Admin Form Submission
    @PostMapping("/users/edit/{id}")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute("user") @Validated User user,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (result.hasErrors()) {
            List<Role> roles = roleService.findAllRoles(); // Remove if roles are not needed
            model.addAttribute("roles", roles); // Remove if roles are not needed
            return "admin/user-edit"; // Updated template name
        }

        // Retrieve the existing user
        Optional<User> existingUserOpt = userService.findUserById(id);
        if (existingUserOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/admin/users";
        }

        User existingUser = existingUserOpt.get();

        // Check if username is being updated and is unique
        if (!existingUser.getUsername().equals(user.getUsername())) {
            Optional<User> userWithUsername = userService.findByUsername(user.getUsername());
            if (userWithUsername.isPresent()) {
                model.addAttribute("usernameError", "Username is already taken.");
                List<Role> roles = roleService.findAllRoles(); // Remove if roles are not needed
                model.addAttribute("roles", roles); // Remove if roles are not needed
                return "admin/user-edit"; // Updated template name
            }
        }

        // Check if email is being updated and is unique
        if (!existingUser.getEmail().equals(user.getEmail())) {
            Optional<User> userWithEmail = userService.findByEmail(user.getEmail());
            if (userWithEmail.isPresent()) {
                model.addAttribute("emailError", "Email is already registered.");
                List<Role> roles = roleService.findAllRoles(); // Remove if roles are not needed
                model.addAttribute("roles", roles); // Remove if roles are not needed
                return "admin/user-edit"; // Updated template name
            }
        }

        // Update fields
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());

        // Handle password update (optional)
        if (StringUtils.hasText(user.getPassword())) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // **Role updating has been removed**
        // If you still need to display roles in the form without updating, keep the roles list

        // Save the updated user
        userService.saveUser(existingUser);

        // Add a success message as a flash attribute
        redirectAttributes.addFlashAttribute("registerSuccess", "User/Admin updated successfully.");

        // Redirect to the users list page
        return "redirect:/admin/users";
    }

    @PostMapping("/users/activate/{id}")
    public String activateUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.findUserById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!user.getEnabled()) {
                user.setEnabled(true);
                userService.saveUser(user);
                redirectAttributes.addFlashAttribute("message", "User activated successfully.");
            } else {
                redirectAttributes.addFlashAttribute("message", "User is already active.");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/deactivate/{id}")
    public String deactivateUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.findUserById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getEnabled()) {
                user.setEnabled(false);
                userService.saveUser(user);
                redirectAttributes.addFlashAttribute("message", "User deactivated successfully.");
            } else {
                redirectAttributes.addFlashAttribute("message", "User is already deactivated.");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
        }
        return "redirect:/admin/users";
    }
}
