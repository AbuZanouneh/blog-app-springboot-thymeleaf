package com.ats.blogapp.access.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="Roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name", length = 50, nullable = false, unique = true)
    private String name;

    // One-to-Many relationship with User
    @OneToMany(mappedBy = "role")
    private Set<User> users = new HashSet<>();

    // Custom constructor
    public Role(String name) {
        this.name = name;
    }

}
