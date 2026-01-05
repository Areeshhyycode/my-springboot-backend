package com.SmartPlanner.SmartPlanner.repository;

import com.SmartPlanner.SmartPlanner.model.Trip;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends MongoRepository<Trip, String> {

    List<Trip> findByUserId(String userId);

    List<Trip> findByUserEmail(String userEmail);

    List<Trip> findByCityId(String cityId);

    List<Trip> findByUserIdAndStatus(String userId, Trip.TripStatus status);

    List<Trip> findByUserIdOrderByCreatedAtDesc(String userId);

    // For scheduler - find all trips by status
    List<Trip> findByStatus(Trip.TripStatus status);
}
