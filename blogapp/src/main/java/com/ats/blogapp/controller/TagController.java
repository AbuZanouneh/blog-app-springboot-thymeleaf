package com.ats.blogapp.controller;

import com.ats.blogapp.access.entity.Category;
import com.ats.blogapp.access.entity.Tag;
import com.ats.blogapp.service.interfaces.TagService;
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
@RequestMapping("/admin/tags")
@PreAuthorize("hasRole('ADMIN')") // Ensures all methods require ADMIN role -- only admin can manage categories.
public class TagController {

    private final TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }


    // @GetMapping({"", "/"})  // include them both ({"", "/"}) or don't include ("/").
    @GetMapping
    public String viewAllTags(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    Model model){
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").descending()); // Sort here to sort the Post list at descending order.
        Page<Tag> tagPage = tagService.findListOfTags(pageable);   /// Find without ordering
        List<Tag> tags =  tagPage.getContent();

        model.addAttribute("tags", tags);
        model.addAttribute("currentPage", tagPage.getNumber());
        model.addAttribute("totalPages", tagPage.getTotalPages());
        model.addAttribute("totalItems", tagPage.getTotalElements());
        model.addAttribute("size", size);

        return "admin/admin-tags";
    }

    @GetMapping("/create")
    public String showCreateTagForm(Model model){
        model.addAttribute("tag", new Tag());
        return "admin/tag-create"; // Thymeleaf template for creating tags
    }

    /**
     * Handle creation of a new tag
     */
    @PostMapping("/create")
    public String createTag(@Validated @ModelAttribute("tag") Tag tag,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model){

        if(bindingResult.hasErrors()){
            return "admin/tag-create";
        }

        // Check if tag name already exists
        Optional<Tag> existingTag = tagService.findTagByName(tag.getName());
        if(existingTag.isPresent()){
            bindingResult.rejectValue("name", "error.tag", "Tag name already exists.");
            return "admin/tag-create";
        }

        // Save tag
        tagService.saveTag(tag);
        redirectAttributes.addFlashAttribute("message", "Tag created successfully.");
        return "redirect:/admin/tags";
    }

    /**
     * Show form to edit an existing tag
     */
    @GetMapping("/edit/{id}")
    public String showEditTagForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes){
        Optional<Tag> tagOpt = tagService.findTagById(id);
        if(tagOpt.isEmpty()){
            redirectAttributes.addFlashAttribute("errorMessage", "Tag not found.");
            return "redirect:/admin/tags";
        }
        model.addAttribute("tag", tagOpt.get());
        return "admin/tag-edit"; // Thymeleaf template for editing tags
    }

    /**
     * Handle updating of an existing tag
     */
    @PostMapping("/edit/{id}")
    public String updateTag(@PathVariable Long id,
                            @Validated @ModelAttribute("tag") Tag tag,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model){
        if(bindingResult.hasErrors()){
            return "admin/tag-edit";
        }

        Optional<Tag> existingTagOpt = tagService.findTagById(id);
        if(existingTagOpt.isEmpty()){
            redirectAttributes.addFlashAttribute("errorMessage", "Tag not found.");
            return "redirect:/admin/tags";
        }

        Tag existingTag = existingTagOpt.get();

        // Check if the new name is already taken by another tag
        if(!existingTag.getName().equals(tag.getName())){
            Optional<Tag> tagWithName = tagService.findTagByName(tag.getName());
            if(tagWithName.isPresent()){
                bindingResult.rejectValue("name", "error.tag", "Tag name already exists.");
                return "admin/tag-edit";
            }
        }

        // Update tag name field.
        existingTag.setName(tag.getName());

        tagService.saveTag(existingTag);
        redirectAttributes.addFlashAttribute("message", "Tag updated successfully.");
        return "redirect:/admin/tags";
    }



}
