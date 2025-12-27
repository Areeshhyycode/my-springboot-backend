package com.SmartPlanner.SmartPlanner.repository;

import com.SmartPlanner.SmartPlanner.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * USER REPOSITORY - Database operations ke liye interface
 *
 * MongoRepository<User, String> extends karne se ye methods FREE milte hain:
 * - save(user)         -> User save karo
 * - findById(id)       -> ID se user dhundho
 * - findAll()          -> Saare users lo
 * - deleteById(id)     -> User delete karo
 * - count()            -> Total users count
 *
 * Custom methods:
 * - Spring Data automatically query banata hai method name se!
 * - findByEmail -> SELECT * FROM users WHERE email = ?
 * - existsByEmail -> Check if email exists (true/false)
 */
@Repository  // Spring ko batao ye database repository hai
public interface UserRepository extends MongoRepository<User, String> {

    // Email se user dhundho (login ke liye)
    // Spring automatically query generate karega: {email: ?}
    Optional<User> findByEmail(String email);

    // Username se user dhundho
    Optional<User> findByUsername(String username);

    // Check karo email already exists hai ya nahi (registration ke liye)
    boolean existsByEmail(String email);

    // Check karo username already exists hai ya nahi
    boolean existsByUsername(String username);
}