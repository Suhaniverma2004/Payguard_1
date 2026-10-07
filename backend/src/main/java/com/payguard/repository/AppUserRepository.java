package com.payguard.repository;

import com.payguard.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);
}
