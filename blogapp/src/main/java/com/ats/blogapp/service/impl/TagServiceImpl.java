package com.ats.blogapp.service.impl;

import com.ats.blogapp.access.entity.Category;
import com.ats.blogapp.access.entity.Tag;
import com.ats.blogapp.access.repository.TagRepository;
import com.ats.blogapp.service.interfaces.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Autowired
    public TagServiceImpl(TagRepository tagRepository){
        this.tagRepository = tagRepository;
    }

    @Override
    public List<Tag> findAllTags(){
        return tagRepository.findAllByOrderByNameAsc();
    }

    @Override
    public List<Tag> findTagsByIds(List<Long> ids){
        return tagRepository.findAllById(ids);
    }

    @Override
    public Optional<Tag> findTagByName(String name){
        return tagRepository.findByName(name);
    }

    @Override
    public Optional<Tag> findTagById(Long id){
        return tagRepository.findById(id);
    }

    @Override
    public Tag saveTag(Tag tag){
        return tagRepository.save(tag);
    }

    @Override
    public void deleteTagById(Long id){
        tagRepository.deleteById(id);
    }

    @Override
    public Page<Tag> findListOfTags(Pageable pageable){
        return tagRepository.findAllByOrderByNameAsc(pageable);
    }

}
