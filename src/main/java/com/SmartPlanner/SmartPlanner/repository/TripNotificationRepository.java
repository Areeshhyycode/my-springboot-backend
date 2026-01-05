package com.SmartPlanner.SmartPlanner.repository;

import com.SmartPlanner.SmartPlanner.model.Trip;
import com.SmartPlanner.SmartPlanner.model.TripNotification;
import com.SmartPlanner.SmartPlanner.model.TripNotification.NotificationType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface TripNotificationRepository extends MongoRepository<TripNotification, String> {

    // Find all notifications for a user by email
    List<TripNotification> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    // Find unread notifications for a user by email
    List<TripNotification> findByUserEmailAndIsReadFalseOrderByCreatedAtDesc(String userEmail);

    // Find notifications for a specific trip
    List<TripNotification> findByTripId(String tripId);

    // Check if a specific notification type already exists for a trip
    Optional<TripNotification> findByTripIdAndType(String tripId, NotificationType type);

    // Check if notification already sent
    boolean existsByTripIdAndTypeAndEmailSentTrue(String tripId, NotificationType type);

    // Count unread notifications by email
    long countByUserEmailAndIsReadFalse(String userEmail);

    // Delete notifications for a trip
    void deleteByTripId(String tripId);

    // ✅ IMPORTANT: Yeh method HATA DO - TripNotification mein status field nahi hai
    // List<TripNotification> findByStatus(Trip.TripStatus status); // ❌ REMOVE THIS
}