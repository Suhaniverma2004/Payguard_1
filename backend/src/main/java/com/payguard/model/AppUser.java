package com.payguard.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name="app_users", indexes=@Index(name="idx_user_username", columnList="username", unique=true))
public class AppUser {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(nullable=false, unique=true) private String username;
    @Column(nullable=false) private String passwordHash;
    @Column(nullable=false) private String role;
    public AppUser() {}
    public AppUser(String username, String passwordHash, String role){this.username=username;this.passwordHash=passwordHash;this.role=role;}
    public UUID getId(){return id;}
    public String getUsername(){return username;}
    public String getPasswordHash(){return passwordHash;}
    public String getRole(){return role;}
}
