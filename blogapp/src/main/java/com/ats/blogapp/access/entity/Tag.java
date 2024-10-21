package com.ats.blogapp.access.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    // Many-to-Many relationship with Post
    @ManyToMany(mappedBy = "tags")
    private Set<Post> posts = new HashSet<>();

}
