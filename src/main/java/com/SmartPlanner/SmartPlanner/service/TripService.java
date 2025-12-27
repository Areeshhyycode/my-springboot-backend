package com.SmartPlanner.SmartPlanner.service;

import com.SmartPlanner.SmartPlanner.dto.TripRequest;
import com.SmartPlanner.SmartPlanner.model.Category;
import com.SmartPlanner.SmartPlanner.model.City;
import com.SmartPlanner.SmartPlanner.model.Trip;
import com.SmartPlanner.SmartPlanner.repository.CategoryRepository;
import com.SmartPlanner.SmartPlanner.repository.CityRepository;
import com.SmartPlanner.SmartPlanner.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TRIP SERVICE - Trip planning aur cost calculation
 *
 * Features:
 * - Create trip from user selections
 * - Calculate total cost based on activities
 * - Store weather snapshot at booking time
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final CityRepository cityRepository;
    private final CategoryRepository categoryRepository;

    /**
     * CREATE TRIP - User trip plan kare
     *
     * Flow:
     * 1. Validate city exists
     * 2. Validate all selected activities exist
     * 3. Calculate cost for each activity
     * 4. Calculate total cost
     * 5. Save trip with weather snapshot
     */
    public Trip createTrip(TripRequest request, String userId, String userEmail) {
        log.info("Creating trip for user {} to city {}", userEmail, request.getCityId());

        // Step 1: Validate city
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new RuntimeException("City not found: " + request.getCityId()));

        // Step 2 & 3: Process selected activities and calculate costs
        List<Trip.SelectedActivity> selectedActivities = new ArrayList<>();
        BigDecimal totalCost = BigDecimal.ZERO;

        for (TripRequest.ActivitySelection selection : request.getSelectedActivities()) {
            Category activity = categoryRepository.findById(selection.getActivityId())
                    .orElseThrow(() -> new RuntimeException("Activity not found: " + selection.getActivityId()));

            // Verify activity belongs to selected city
            if (!activity.getCityId().equals(request.getCityId())) {
                throw new RuntimeException("Activity " + activity.getName() + " is not available in " + city.getName());
            }

            int quantity = selection.getQuantity() != null ? selection.getQuantity() : 1;
            int durationValue = selection.getDurationValue() != null ? selection.getDurationValue() : 1;

            // Get price based on duration type
            BigDecimal unitPrice;
            if (selection.getDurationType() == TripRequest.DurationType.HOURS) {
                unitPrice = activity.getPricePerHour() != null ? activity.getPricePerHour() : BigDecimal.ZERO;
            } else {
                unitPrice = activity.getPricePerDay() != null ? activity.getPricePerDay() : BigDecimal.ZERO;
            }

            // Calculate: unitPrice * durationValue * quantity
            BigDecimal subtotal = unitPrice
                    .multiply(BigDecimal.valueOf(durationValue))
                    .multiply(BigDecimal.valueOf(quantity));

            Trip.SelectedActivity selected = new Trip.SelectedActivity(
                    activity.getId(),
                    activity.getName(),
                    selection.getDurationType().name(),
                    durationValue,
                    unitPrice,
                    quantity,
                    subtotal
            );

            selectedActivities.add(selected);
            totalCost = totalCost.add(subtotal);
        }

        // Step 4: Create trip
        Trip trip = new Trip();
        trip.setUserId(userId);
        trip.setUserEmail(userEmail);
        trip.setCityId(city.getId());
        trip.setCityName(city.getName());
        trip.setCountry(city.getCountryName());
        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getStartDate().plusDays(request.getDurationDays()));
        trip.setDurationDays(request.getDurationDays());
        trip.setSelectedActivities(selectedActivities);
        trip.setTotalCost(totalCost);
        trip.setCurrency("USD");
        trip.setWeatherSnapshot(city.getWeather());  // Store current weather
        trip.setStatus(Trip.TripStatus.PLANNED);
        trip.setCreatedAt(LocalDateTime.now());
        trip.setUpdatedAt(LocalDateTime.now());

        Trip savedTrip = tripRepository.save(trip);
        log.info("Trip created: {} to {} for ${}", savedTrip.getId(), city.getName(), totalCost);

        return savedTrip;
    }

    /**
     * Get trip by ID
     */
    public Trip getTripById(String tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + tripId));
    }

    /**
     * Get user's trips
     */
    public List<Trip> getUserTrips(String userId) {
        return tripRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get user's trips by email
     */
    public List<Trip> getTripsByEmail(String email) {
        return tripRepository.findByUserEmail(email);
    }

    /**
     * Update trip status
     */
    public Trip updateTripStatus(String tripId, Trip.TripStatus status) {
        Trip trip = getTripById(tripId);
        trip.setStatus(status);
        trip.setUpdatedAt(LocalDateTime.now());
        return tripRepository.save(trip);
    }

    /**
     * Cancel trip
     */
    public Trip cancelTrip(String tripId, String userId) {
        Trip trip = getTripById(tripId);

        // Verify trip belongs to user
        if (!trip.getUserId().equals(userId)) {
            throw new RuntimeException("You can only cancel your own trips");
        }

        trip.setStatus(Trip.TripStatus.CANCELLED);
        trip.setUpdatedAt(LocalDateTime.now());
        return tripRepository.save(trip);
    }

    /**
     * Calculate cost preview (without saving)
     */
    public CostPreview calculateCostPreview(TripRequest request) {
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new RuntimeException("City not found: " + request.getCityId()));

        List<ActivityCost> activityCosts = new ArrayList<>();
        BigDecimal totalCost = BigDecimal.ZERO;

        for (TripRequest.ActivitySelection selection : request.getSelectedActivities()) {
            Category activity = categoryRepository.findById(selection.getActivityId())
                    .orElseThrow(() -> new RuntimeException("Activity not found: " + selection.getActivityId()));

            int quantity = selection.getQuantity() != null ? selection.getQuantity() : 1;
            int durationValue = selection.getDurationValue() != null ? selection.getDurationValue() : 1;

            // Get price based on duration type
            BigDecimal unitPrice;
            if (selection.getDurationType() == TripRequest.DurationType.HOURS) {
                unitPrice = activity.getPricePerHour() != null ? activity.getPricePerHour() : BigDecimal.ZERO;
            } else {
                unitPrice = activity.getPricePerDay() != null ? activity.getPricePerDay() : BigDecimal.ZERO;
            }

            BigDecimal subtotal = unitPrice
                    .multiply(BigDecimal.valueOf(durationValue))
                    .multiply(BigDecimal.valueOf(quantity));

            activityCosts.add(new ActivityCost(
                    activity.getName(),
                    selection.getDurationType().name(),
                    durationValue,
                    unitPrice,
                    quantity,
                    subtotal
            ));

            totalCost = totalCost.add(subtotal);
        }

        return new CostPreview(
                city.getName(),
                city.getCountryName(),
                request.getDurationDays(),
                activityCosts,
                totalCost,
                "USD",
                city.getWeather()
        );
    }

    /**
     * Cost Preview DTO
     */
    public record CostPreview(
            String cityName,
            String country,
            Integer durationDays,
            List<ActivityCost> activities,
            BigDecimal totalCost,
            String currency,
            City.CityWeather weather
    ) {}

    public record ActivityCost(
            String name,
            String durationType,    // HOURS or DAYS
            Integer durationValue,  // e.g., 3 hours
            BigDecimal unitPrice,   // price per hour/day
            Integer quantity,
            BigDecimal subtotal     // unitPrice * durationValue * quantity
    ) {}
}
