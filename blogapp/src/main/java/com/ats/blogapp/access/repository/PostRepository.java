package com.ats.blogapp.access.repository;

import com.ats.blogapp.access.entity.Category;
import com.ats.blogapp.access.entity.Post;
import com.ats.blogapp.access.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

// Data Access Layer
public interface PostRepository extends JpaRepository<Post, Long> {

    // Find Posts by Category.
    List<Post> findByCategory(Category category);

    // Find Posts by Tag Name.
    Page<Post> findByTags_NameOrderByCreatedAtDesc(String tagName, Pageable pageable);

    // Fetch all posts ordered by creation date descending - without comments.
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Fetch posts by category ordered by creation date descending with pagination
    Page<Post> findByCategoryOrderByCreatedAtDesc(Category category, Pageable pageable);

    // This method retrieves the post with its comments.
    @EntityGraph(attributePaths = {"comments", "comments.author"})
    Optional<Post> findWithCommentsById(Long id);

    // Find posts by author // Used this.
    // Find post that created for a specific user.
    Page<Post> findByAuthor(User author, Pageable pageable);

    // Optional.
    // Find posts by author's ID
    // Here you need to get the user id from Authentication, get the username for the authenticated user then get his Id.
    // Find post that created for a specific user.
    Page<Post> findByAuthor_Id(Long authorId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"author", "category"})
    Page<Post> findAll(Pageable pageable);
}
