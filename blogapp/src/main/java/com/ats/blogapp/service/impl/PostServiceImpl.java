package com.ats.blogapp.service.impl;

import com.ats.blogapp.access.entity.Category;
import com.ats.blogapp.access.entity.Post;
import com.ats.blogapp.access.entity.User;
import com.ats.blogapp.access.repository.PostRepository;
import com.ats.blogapp.service.interfaces.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    @Autowired
    public PostServiceImpl(PostRepository postRepository){
        this.postRepository = postRepository;
    }

    @Override
    public Page<Post> findAllPosts(Pageable pageable){
        return postRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    public Optional<Post> findPostById(Long id){
        return postRepository.findWithCommentsById(id);
    }

    @Override
    public Post savePost(Post post){
        return postRepository.save(post);
    }

    @Override
    public void deletePostById(Long id){
        postRepository.deleteById(id);
    }

    @Override
    public Page<Post> findPostsByCategory(Category category, Pageable pageable){
        return postRepository.findByCategoryOrderByCreatedAtDesc(category, pageable);
    }

    @Override
    public Page<Post> findPostsByTag(String tagName, Pageable pageable){
        return postRepository.findByTags_NameOrderByCreatedAtDesc(tagName, pageable);
    }

    @Override
    public Page<Post> findPostsByAuthor(User author, Pageable pageable){
        return postRepository.findByAuthor(author, pageable);
    }
}
