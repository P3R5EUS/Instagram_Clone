package org.example.models;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users") // Maps to 'users' table in DB
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment primary key
    private Long id;

    @Column(unique = true, nullable = false) // Username must be unique and not null
    private String username;

    @Column(nullable = false) // Ensure password is never null
    private String password;

    @Column(unique = true, nullable = false) // Email must be unique and not null
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id")) // Ensures proper table creation for roles
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    // Default constructor
    public User() {
        this.roles.add("USER"); // Default role
    }

    // All-args constructor
    public User(Long id, String username, String password, String email, Set<String> roles) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = (roles != null) ? roles : new HashSet<>();
        if (this.roles.isEmpty()) {
            this.roles.add("USER"); // Default role
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password; // Make sure to hash before saving
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            this.roles.add("USER"); // Assign default role if none is provided
        } else {
            this.roles = roles;
        }
    }
}
