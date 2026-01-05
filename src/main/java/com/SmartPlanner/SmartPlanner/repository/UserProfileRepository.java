package com.SmartPlanner.SmartPlanner.repository;

import com.SmartPlanner.SmartPlanner.model.UserProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserProfileRepository extends MongoRepository<UserProfile, String> {

    Optional<UserProfile> findByUserId(String userId);

    boolean existsByUserId(String userId);

    void deleteByUserId(String userId);

    Optional<UserProfile> findByPhoneNumber(String phoneNumber);
}
