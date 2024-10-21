package com.ats.blogapp.service.interfaces;

import com.ats.blogapp.access.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TagService {

    List<Tag> findAllTags(); // - without pagination.

    List<Tag> findTagsByIds(List<Long> ids);

    Optional<Tag> findTagByName(String name);

    Optional<Tag> findTagById(Long id);

    Tag saveTag(Tag tag);

    void deleteTagById(Long id);

    Page<Tag> findListOfTags(Pageable pageable); // - with pagination.

}
