package com.SmartPlanner.SmartPlanner.repository;

import com.SmartPlanner.SmartPlanner.model.Trip;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TRIP REPOSITORY - Database operations for trips
 */
@Repository
public interface TripRepository extends MongoRepository<Trip, String> {

    // User ke trips
    List<Trip> findByUserId(String userId);

    List<Trip> findByUserEmail(String userEmail);

    // City ke trips
    List<Trip> findByCityId(String cityId);

    // Status ke hisab se
    List<Trip> findByUserIdAndStatus(String userId, Trip.TripStatus status);

    // User ke recent trips
    List<Trip> findByUserIdOrderByCreatedAtDesc(String userId);
}
