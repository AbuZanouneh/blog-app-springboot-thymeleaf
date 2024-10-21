package com.ats.blogapp.access.repository;

import com.ats.blogapp.access.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Data Access Layer
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find Category By Name.
    Optional<Category> findByName(String name);

    // Fetch all categories ordered by name - ascending mode.  --without pageable (pagination).
    List<Category> findAllByOrderByNameAsc();


    // Fetch all categories ordered by name - ascending mode.   --with pageable (pagination).
    Page<Category> findAllByOrderByNameAsc(Pageable pageable);

}
