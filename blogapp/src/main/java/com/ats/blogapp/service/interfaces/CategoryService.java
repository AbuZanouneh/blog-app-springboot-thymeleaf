package com.ats.blogapp.service.interfaces;

import com.ats.blogapp.access.entity.Category;
import com.ats.blogapp.access.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    List<Category> findAllCategories();   // - without pagination.

    Optional<Category> findCategoryById(Long id);

    Category saveCategory(Category category);

    void deleteCategoryById(Long id);

    Page<Category> findListOfCategories(Pageable pageable); // - with pagination.

    Optional<Category> findCategoryByName(String name); // To check if the name of new added category is already exists in db.


}
