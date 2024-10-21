package com.ats.blogapp.service.impl;

import com.ats.blogapp.access.entity.Category;
import com.ats.blogapp.access.entity.Post;
import com.ats.blogapp.access.repository.CategoryRepository;
import com.ats.blogapp.service.interfaces.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> findAllCategories(){
        return categoryRepository.findAllByOrderByNameAsc();
    }

    @Override
    public Optional<Category> findCategoryById(Long id){
        return categoryRepository.findById(id);
    }

    @Override
    public Category saveCategory(Category category){
        return categoryRepository.save(category);
    }

    @Override
    public void deleteCategoryById(Long id){
        categoryRepository.deleteById(id);
    }

    @Override
    public Page<Category> findListOfCategories(Pageable pageable){
        return categoryRepository.findAllByOrderByNameAsc(pageable);
    }

    @Override
    public Optional<Category> findCategoryByName(String name){
        return categoryRepository.findByName(name);
    }

}
