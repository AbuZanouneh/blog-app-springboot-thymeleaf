package com.ats.blogapp.controller;

import com.ats.blogapp.access.entity.Category;
import com.ats.blogapp.access.entity.Post;
import com.ats.blogapp.access.entity.Tag;
import com.ats.blogapp.service.interfaces.CategoryService;
import com.ats.blogapp.service.interfaces.PostService;
import com.ats.blogapp.service.interfaces.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

// Controller Layer
// You can add @GetMapping({"/"}) here.
@Controller
public class HomeController {

    private final PostService postService;

    private final CategoryService categoryService;

    private final TagService tagService;

    @Autowired
    public HomeController(PostService postService,
                          CategoryService categoryService,
                          TagService tagService) {
        this.postService = postService;
        this.categoryService = categoryService;
        this.tagService = tagService;
    }


    // Here, Fetch all posts ordered by creation date - descending
    // @Transactional  // To Ensures that lazy-loaded relationships are initialized .. it's not necessary to add.
    // here the page, and size is for the pagination.
    // In .. home page.
    @GetMapping({"/", "/home"})
    public String home(Model model, @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size){ // change the page numbers here.
        // Fetch posts with pagination
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postService.findAllPosts(pageable); // Fetch all posts in descending mode.
        List<Post> posts = postPage.getContent(); // Extract the list of posts for the current page.

        // Retrieve categories & tags.
        List<Category> categories = categoryService.findAllCategories();
        List<Tag> tags = tagService.findAllTags();

        model.addAttribute("posts", posts);
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);
        model.addAttribute("currentPage", postPage.getNumber());
        model.addAttribute("totalPages", postPage.getTotalPages());

        return "home";
    }

    // Category Filtering .. Fetch posts by categories.
    // here the page, and size is for the pagination.
    // In .. home page.
    @GetMapping("/categories/{id}")
    public String viewCategory(@PathVariable Long id,
                               Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size // change the page numbers here.
                               ){
        Optional<Category> categoryOpt = categoryService.findCategoryById(id);
        if(categoryOpt.isPresent()){
            // Fetch posts with pagination
            Category category = categoryOpt.get();
            Pageable pageable = PageRequest.of(page, size);
            Page<Post> postPage = postService.findPostsByCategory(category, pageable);
            List<Post> posts = postPage.getContent();
            List<Category> categories = categoryService.findAllCategories();
            List<Tag> tags = tagService.findAllTags();

            model.addAttribute("posts", posts);
            model.addAttribute("categories", categories);
            model.addAttribute("tags", tags);
            model.addAttribute("selectedCategory", category);
            model.addAttribute("currentPage", postPage.getNumber());
            model.addAttribute("totalPages", postPage.getTotalPages());

            return "home";

        }
        else{
            // Handle category not found
            return "redirect:/home?error=CategoryNotFound";
        }
    }

    // tag Filtering .. Fetch posts by tags (only one tag and by it's id) ... same as viewCategory().
    // here the page, and size is for the pagination.
    // In .. home page.
    @GetMapping("/tags/{id}")
    public String viewTag(@PathVariable Long id,
                          Model model,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size){ // change the page numbers here.

        Optional<Tag> tagOpt = tagService.findTagById(id);
        if(tagOpt.isPresent()){
            Tag tag = tagOpt.get();
            Pageable pageable = PageRequest.of(page, size);
            Page<Post> postPage = postService.findPostsByTag(tag.getName(), pageable);
            List<Post> posts = postPage.getContent();
            List<Category> categories = categoryService.findAllCategories();
            List<Tag> tags = tagService.findAllTags();

            model.addAttribute("posts", posts);
            model.addAttribute("categories", categories);
            model.addAttribute("tags", tags);
            model.addAttribute("selectedTag", tag);
            model.addAttribute("currentPage", postPage.getNumber());
            model.addAttribute("totalPages", postPage.getTotalPages());

            return "home";
        }
        else{
            // Handle tag not found.
            return "redirect:/home?error=TagNotFound";
        }

    }

    @GetMapping("/about")
    public String aboutPage() {
        return "about";
    }

    @GetMapping("/contact")
    public String contactPage() {
        return "contact";
    }

}
