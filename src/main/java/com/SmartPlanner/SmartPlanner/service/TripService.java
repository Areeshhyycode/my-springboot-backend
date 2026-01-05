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

@Slf4j
@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final CityRepository cityRepository;
    private final CategoryRepository categoryRepository;

    public Trip createTrip(TripRequest request, String userId, String userEmail) {
        log.info("Creating trip for user {} to city {}", userEmail, request.getCityId());

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new RuntimeException("City not found: " + request.getCityId()));

        List<Trip.SelectedActivity> selectedActivities = new ArrayList<>();
        BigDecimal totalCost = BigDecimal.ZERO;

        for (TripRequest.ActivitySelection selection : request.getSelectedActivities()) {
            Category activity = categoryRepository.findById(selection.getActivityId())
                    .orElseThrow(() -> new RuntimeException("Activity not found: " + selection.getActivityId()));

            if (!activity.getCityId().equals(request.getCityId())) {
                throw new RuntimeException("Activity " + activity.getName() + " is not available in " + city.getName());
            }

            int quantity = selection.getQuantity() != null ? selection.getQuantity() : 1;
            int durationValue = selection.getDurationValue() != null ? selection.getDurationValue() : 1;

            BigDecimal unitPrice;
            if (selection.getDurationType() == TripRequest.DurationType.HOURS) {
                unitPrice = activity.getPricePerHour() != null ? activity.getPricePerHour() : BigDecimal.ZERO;
            } else {
                unitPrice = activity.getPricePerDay() != null ? activity.getPricePerDay() : BigDecimal.ZERO;
            }

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
                    subtotal,
                    activity.getLatitude(),
                    activity.getLongitude()
            );

            selectedActivities.add(selected);
            totalCost = totalCost.add(subtotal);
        }

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
        trip.setWeatherSnapshot(city.getWeather());
        trip.setStatus(Trip.TripStatus.PLANNED);
        trip.setCreatedAt(LocalDateTime.now());
        trip.setUpdatedAt(LocalDateTime.now());

        Trip savedTrip = tripRepository.save(trip);
        log.info("Trip created: {} to {} for ${}", savedTrip.getId(), city.getName(), totalCost);

        return savedTrip;
    }

    public Trip getTripById(String tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + tripId));
    }

    public List<Trip> getUserTrips(String userId) {
        return tripRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Trip> getTripsByEmail(String email) {
        return tripRepository.findByUserEmail(email);
    }

    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    public Trip updateTripStatus(String tripId, Trip.TripStatus status) {
        Trip trip = getTripById(tripId);
        trip.setStatus(status);
        trip.setUpdatedAt(LocalDateTime.now());
        return tripRepository.save(trip);
    }

    public Trip cancelTrip(String tripId, String userEmail) {
        Trip trip = getTripById(tripId);

        if (!trip.getUserEmail().equals(userEmail)) {
            throw new RuntimeException("You can only cancel your own trips");
        }

        trip.setStatus(Trip.TripStatus.CANCELLED);
        trip.setUpdatedAt(LocalDateTime.now());
        log.info("Trip {} cancelled by user {}", tripId, userEmail);
        return tripRepository.save(trip);
    }

    public Trip editTrip(String tripId, TripRequest request, String userEmail) {
        log.info("Editing trip {} for user {}", tripId, userEmail);

        Trip trip = getTripById(tripId);

        if (!trip.getUserEmail().equals(userEmail)) {
            throw new RuntimeException("You can only edit your own trips");
        }

        if (trip.getStatus() == Trip.TripStatus.CANCELLED) {
            throw new RuntimeException("Cannot edit a cancelled trip");
        }

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new RuntimeException("City not found: " + request.getCityId()));

        List<Trip.SelectedActivity> selectedActivities = new ArrayList<>();
        BigDecimal totalCost = BigDecimal.ZERO;

        for (TripRequest.ActivitySelection selection : request.getSelectedActivities()) {
            Category activity = categoryRepository.findById(selection.getActivityId())
                    .orElseThrow(() -> new RuntimeException("Activity not found: " + selection.getActivityId()));

            if (!activity.getCityId().equals(request.getCityId())) {
                throw new RuntimeException("Activity " + activity.getName() + " is not available in " + city.getName());
            }

            int quantity = selection.getQuantity() != null ? selection.getQuantity() : 1;
            int durationValue = selection.getDurationValue() != null ? selection.getDurationValue() : 1;

            BigDecimal unitPrice;
            if (selection.getDurationType() == TripRequest.DurationType.HOURS) {
                unitPrice = activity.getPricePerHour() != null ? activity.getPricePerHour() : BigDecimal.ZERO;
            } else {
                unitPrice = activity.getPricePerDay() != null ? activity.getPricePerDay() : BigDecimal.ZERO;
            }

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
                    subtotal,
                    activity.getLatitude(),
                    activity.getLongitude()
            );

            selectedActivities.add(selected);
            totalCost = totalCost.add(subtotal);
        }

        trip.setCityId(city.getId());
        trip.setCityName(city.getName());
        trip.setCountry(city.getCountryName());
        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getStartDate().plusDays(request.getDurationDays()));
        trip.setDurationDays(request.getDurationDays());
        trip.setSelectedActivities(selectedActivities);
        trip.setTotalCost(totalCost);
        trip.setWeatherSnapshot(city.getWeather());
        trip.setUpdatedAt(LocalDateTime.now());

        Trip savedTrip = tripRepository.save(trip);
        log.info("Trip updated: {} to {} for ${}", savedTrip.getId(), city.getName(), totalCost);

        return savedTrip;
    }

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
            String durationType,
            Integer durationValue,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal subtotal
    ) {}
}
