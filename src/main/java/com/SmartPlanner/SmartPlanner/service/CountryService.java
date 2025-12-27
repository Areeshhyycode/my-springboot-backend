package com.SmartPlanner.SmartPlanner.service;

import com.SmartPlanner.SmartPlanner.dto.CountryRequest;
import com.SmartPlanner.SmartPlanner.dto.FullCountryRequest;
import com.SmartPlanner.SmartPlanner.dto.FullCountryResponse;
import com.SmartPlanner.SmartPlanner.model.Category;
import com.SmartPlanner.SmartPlanner.model.City;
import com.SmartPlanner.SmartPlanner.model.Country;
import com.SmartPlanner.SmartPlanner.repository.CategoryRepository;
import com.SmartPlanner.SmartPlanner.repository.CityRepository;
import com.SmartPlanner.SmartPlanner.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * COUNTRY SERVICE - Country CRUD + Full nested operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final CategoryRepository categoryRepository;
    private final GeocodingService geocodingService;
    private final WeatherService weatherService;

    // ==================== GET APIs ====================

    /**
     * Get all countries (basic)
     */
    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }

    /**
     * Get active countries only
     */
    public List<Country> getActiveCountries() {
        return countryRepository.findByIsActiveTrue();
    }

    /**
     * GET ALL DATA - Countries with Cities and Activities (for Frontend)
     */
    public List<FullCountryResponse> getAllCountriesWithCitiesAndActivities() {
        List<Country> countries = countryRepository.findByIsActiveTrue();
        List<FullCountryResponse> result = new ArrayList<>();

        for (Country country : countries) {
            result.add(buildFullCountryResponse(country));
        }

        return result;
    }

    /**
     * GET SINGLE COUNTRY with all Cities and Activities
     */
    public FullCountryResponse getCountryWithCitiesAndActivities(String countryId) {
        Country country = getCountryById(countryId);
        return buildFullCountryResponse(country);
    }

    /**
     * Build full nested response for a country
     */
    private FullCountryResponse buildFullCountryResponse(Country country) {
        List<City> cities = cityRepository.findByCountryIdAndIsActiveTrue(country.getId());
        List<FullCountryResponse.CityWithActivities> cityList = new ArrayList<>();

        for (City city : cities) {
            List<Category> activities = categoryRepository.findByCityIdAndIsActiveTrue(city.getId());
            List<FullCountryResponse.ActivityInfo> activityList = new ArrayList<>();

            for (Category activity : activities) {
                activityList.add(FullCountryResponse.ActivityInfo.builder()
                        .id(activity.getId())
                        .name(activity.getName())
                        .description(activity.getDescription())
                        .pricePerHour(activity.getPricePerHour())
                        .pricePerDay(activity.getPricePerDay())
                        .imageUrl(activity.getImageUrl())
                        .build());
            }

            cityList.add(FullCountryResponse.CityWithActivities.builder()
                    .id(city.getId())
                    .name(city.getName())
                    .latitude(city.getLatitude())
                    .longitude(city.getLongitude())
                    .imageUrl(city.getImageUrl())
                    .description(city.getDescription())
                    .weather(city.getWeather())
                    .activities(activityList)
                    .build());
        }

        return FullCountryResponse.builder()
                .id(country.getId())
                .name(country.getName())
                .code(country.getCode())
                .imageUrl(country.getImageUrl())
                .description(country.getDescription())
                .cities(cityList)
                .build();
    }

    // ==================== CREATE APIs ====================

    /**
     * Get country by ID
     */
    public Country getCountryById(String id) {
        return countryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Country not found with id: " + id));
    }

    /**
     * Add new country (simple)
     */
    public Country addCountry(CountryRequest request) {
        if (countryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Country already exists: " + request.getName());
        }

        Country country = new Country();
        country.setName(request.getName());
        country.setCode(request.getCode() != null ? request.getCode().toUpperCase() : generateCode(request.getName()));
        country.setImageUrl(request.getImageUrl());
        country.setDescription(request.getDescription());
        country.setIsActive(true);
        country.setCreatedAt(LocalDateTime.now());
        country.setUpdatedAt(LocalDateTime.now());

        return countryRepository.save(country);
    }

    /**
     * ADD FULL COUNTRY - Country + Cities + Activities in one API
     */
    public FullCountryResponse addFullCountry(FullCountryRequest request) {
        log.info("Adding full country: {} with {} cities", request.getName(), request.getCities().size());

        // Step 1: Create Country
        if (countryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Country already exists: " + request.getName());
        }

        Country country = new Country();
        country.setName(request.getName());
        country.setCode(request.getCode() != null ? request.getCode().toUpperCase() : generateCode(request.getName()));
        country.setImageUrl(request.getImageUrl());
        country.setDescription(request.getDescription());
        country.setIsActive(true);
        country.setCreatedAt(LocalDateTime.now());
        country.setUpdatedAt(LocalDateTime.now());
        country = countryRepository.save(country);

        log.info("Country created: {} ({})", country.getName(), country.getId());

        // Step 2: Create Cities with Activities
        for (FullCountryRequest.CityData cityData : request.getCities()) {
            try {
                // Fetch coordinates
                GeocodingService.GeoLocation location = geocodingService.getCoordinates(cityData.getName());

                // Fetch weather
                City.CityWeather weather = weatherService.fetchCityWeather(
                        location.getLatitude(),
                        location.getLongitude()
                );

                // Create city
                City city = new City();
                city.setCountryId(country.getId());
                city.setCountryName(country.getName());
                city.setName(location.getDisplayName());
                city.setLatitude(location.getLatitude());
                city.setLongitude(location.getLongitude());
                city.setImageUrl(cityData.getImageUrl());
                city.setDescription(cityData.getDescription());
                city.setIsActive(true);
                city.setWeather(weather);
                city.setCreatedAt(LocalDateTime.now());
                city.setUpdatedAt(LocalDateTime.now());
                city.setWeatherUpdatedAt(LocalDateTime.now());
                city = cityRepository.save(city);

                log.info("City created: {} in {}", city.getName(), country.getName());

                // Step 3: Create Activities for this city
                if (cityData.getActivities() != null) {
                    for (FullCountryRequest.ActivityData actData : cityData.getActivities()) {
                        Category activity = new Category();
                        activity.setName(actData.getName());
                        activity.setDescription(actData.getDescription());
                        activity.setCityId(city.getId());
                        activity.setPricePerHour(actData.getPricePerHour());
                        activity.setPricePerDay(actData.getPricePerDay());
                        activity.setImageUrl(actData.getImageUrl());
                        activity.setIsActive(true);
                        categoryRepository.save(activity);

                        log.info("Activity created: {} in {}", actData.getName(), city.getName());
                    }
                }

            } catch (Exception e) {
                log.error("Failed to add city {}: {}", cityData.getName(), e.getMessage());
            }
        }

        // Return full response
        return buildFullCountryResponse(country);
    }

    // ==================== UPDATE APIs ====================

    /**
     * Update country
     */
    public Country updateCountry(String id, CountryRequest request) {
        Country country = getCountryById(id);

        country.setName(request.getName());
        if (request.getCode() != null) {
            country.setCode(request.getCode().toUpperCase());
        }
        country.setImageUrl(request.getImageUrl());
        country.setDescription(request.getDescription());
        country.setUpdatedAt(LocalDateTime.now());

        return countryRepository.save(country);
    }

    // ==================== DELETE APIs ====================

    /**
     * Delete country (and all its cities and activities)
     */
    public void deleteCountry(String id) {
        Country country = getCountryById(id);

        // Delete all activities in all cities of this country
        List<City> cities = cityRepository.findByCountryId(id);
        for (City city : cities) {
            List<Category> activities = categoryRepository.findByCityId(city.getId());
            categoryRepository.deleteAll(activities);
        }

        // Delete all cities
        cityRepository.deleteAll(cities);

        // Delete country
        countryRepository.deleteById(id);

        log.info("Deleted country {} with {} cities", country.getName(), cities.size());
    }

    /**
     * Toggle country active status
     */
    public Country toggleCountryStatus(String id) {
        Country country = getCountryById(id);
        country.setIsActive(!country.getIsActive());
        country.setUpdatedAt(LocalDateTime.now());
        return countryRepository.save(country);
    }

    /**
     * Generate country code from name
     */
    private String generateCode(String name) {
        if (name.length() <= 3) {
            return name.toUpperCase();
        }
        return name.substring(0, 3).toUpperCase();
    }
}
