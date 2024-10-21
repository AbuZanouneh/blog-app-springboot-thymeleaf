package com.ats.blogapp.controller;

import com.ats.blogapp.access.entity.Category;
import com.ats.blogapp.access.entity.Post;
import com.ats.blogapp.service.interfaces.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/admin/categories")
@PreAuthorize("hasRole('ADMIN')") // Ensures all methods require ADMIN role -- only admin can manage categories.
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // To retrieve all categories to manage them... same as in userController but add findAll().
    // No need to use the currently authenticated admin to retrieve categories ... We retrieve them all.
    // In .... admin/admin-categories page.

    // @GetMapping({"", "/"})  // include them both ({"", "/"}) or don't include ("/").
    @GetMapping
    public String viewAllCategories(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               Model model){
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").descending()); // Sort here to sort the Post list at descending order.
        Page<Category> categoryPage = categoryService.findListOfCategories(pageable);   /// Find without ordering
        List<Category> categories =  categoryPage.getContent();

        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", categoryPage.getNumber());
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        model.addAttribute("totalItems", categoryPage.getTotalElements());
        model.addAttribute("size", size);

        return "admin/admin-categories";
    }

    @GetMapping("create")
    public String showCreateCategoryForm(Model model){
        model.addAttribute("category",new Category());
        return "admin/category-create";
    }

    @PostMapping("/create")
    public String createCategory(@Validated @ModelAttribute("category") Category category,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model){

        if(bindingResult.hasErrors()){
            return "admin/category-create";
        }

        // Before saving we must check if the name of new added category is already exists in db.
        Optional<Category> existingCategory = categoryService.findCategoryByName(category.getName());
        if(existingCategory.isPresent())
        {  bindingResult.rejectValue("name", "error.category", "Category name already exists.");
            return "admin/category-create";}

        // save category
        categoryService.saveCategory(category);
        redirectAttributes.addFlashAttribute("message", "Category created successfully.");
        return "redirect:/admin/categories";

    }

    @GetMapping("/edit/{id}")
    public String showEditCategoryForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes){
        Optional<Category> categoryOpt = categoryService.findCategoryById(id);
        if(categoryOpt.isEmpty()){
            redirectAttributes.addFlashAttribute("errorMessage", "Category not found.");
            return "redirect:/admin/categories/";
        }
        model.addAttribute("category", categoryOpt.get());
        return "admin/category-edit"; // Reusing the create template for editing
    }

    @PostMapping("/edit/{id}")
    public String updateCategory(@PathVariable Long id,
                                 @Validated @ModelAttribute("category") Category category,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model){
        if(bindingResult.hasErrors()){
            return "admin/category-edit"; // Path to the edit template
        }

        Optional<Category> existingCategoryOpt = categoryService.findCategoryById(id);
        if(existingCategoryOpt.isEmpty()){
            redirectAttributes.addFlashAttribute("errorMessage", "Category not found.");
            return "redirect:/admin/categories";
        }

        Category existingCategory = existingCategoryOpt.get();

        // Check if the new name is already taken by another category
        if(!existingCategory.getName().equals(category.getName())){
            Optional<Category> categoryWithName = categoryService.findCategoryByName(category.getName());
            if(categoryWithName.isPresent()){
                bindingResult.rejectValue("name", "error.category", "Category name already exists.");
                return "admin/category-edit";
            }
        }

        // Update category details
        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());

        categoryService.saveCategory(existingCategory);
        redirectAttributes.addFlashAttribute("message", "Category updated successfully.");
        return "redirect:/admin/categories";
    }

}
