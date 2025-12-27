package com.SmartPlanner.SmartPlanner.repository;

import com.SmartPlanner.SmartPlanner.model.UserProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserProfileRepository extends MongoRepository<UserProfile, String> {

    /**
     * User ID se profile dhundna
     */
    Optional<UserProfile> findByUserId(String userId);

    /**
     * Check if profile exists for a user
     */
    boolean existsByUserId(String userId);

    /**
     * Delete profile by userId
     */
    void deleteByUserId(String userId);

    /**
     * Find profile by phone number (for uniqueness check if needed)
     */
    Optional<UserProfile> findByPhoneNumber(String phoneNumber);

}
