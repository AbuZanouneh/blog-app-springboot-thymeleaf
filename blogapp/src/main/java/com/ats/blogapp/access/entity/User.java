package com.ats.blogapp.access.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name="password", nullable = false, length = 100)
    private String password;

    @Column(name="email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name="bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "profile_picture_url", length = 255)
    private String profilePictureUrl;

    @Column(name = "website_url", length = 255)
    private String websiteUrl;

    @Column(name="enabled", nullable = false)
    private Boolean enabled = true;

    // Many-to-One relationship with Role
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // One-to-Many relationship with Post
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Post> posts = new HashSet<>();

    // One-to-Many relationship with Comment
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

}
