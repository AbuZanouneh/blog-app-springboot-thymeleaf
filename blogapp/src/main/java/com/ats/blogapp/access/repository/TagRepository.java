package com.ats.blogapp.access.repository;

import com.ats.blogapp.access.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Data Access Layer
public interface TagRepository extends JpaRepository<Tag, Long> {

    // Find Tag By Name.
    Optional<Tag> findByName(String name);

    // Fetch all Tags ordered by name - ascending mode.  // --without pageable (pagination).
    List<Tag> findAllByOrderByNameAsc();

    Page<Tag> findAllByOrderByNameAsc(Pageable pageable); // --with pageable (pagination).

}
