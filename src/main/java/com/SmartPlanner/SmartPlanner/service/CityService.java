package com.SmartPlanner.SmartPlanner.service;

import com.SmartPlanner.SmartPlanner.dto.CityRequest;
import com.SmartPlanner.SmartPlanner.model.City;
import com.SmartPlanner.SmartPlanner.model.Country;
import com.SmartPlanner.SmartPlanner.repository.CityRepository;
import com.SmartPlanner.SmartPlanner.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CITY SERVICE - Cities under Countries
 *
 * Hierarchy:
 * Country (UAE)
 *   └── City (Dubai) ← Managed here
 *         └── Activity (Beach, Safari)
 *
 * Auto-fetch features:
 * - Latitude/Longitude from Geocoding API
 * - Weather from Open-Meteo API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final GeocodingService geocodingService;
    private final WeatherService weatherService;

    /**
     * Get all cities
     */
    public List<City> getAllCities() {
        return cityRepository.findAll();
    }

    /**
     * Get cities by country ID
     */
    public List<City> getCitiesByCountry(String countryId) {
        // Verify country exists
        if (!countryRepository.existsById(countryId)) {
            throw new RuntimeException("Country not found: " + countryId);
        }
        return cityRepository.findByCountryIdAndIsActiveTrue(countryId);
    }

    /**
     * Get city by ID
     */
    public City getCityById(String id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("City not found with id: " + id));
    }

    /**
     * Get city by name
     */
    public City getCityByName(String name) {
        return cityRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new RuntimeException("City not found: " + name));
    }

    /**
     * ADD NEW CITY under a Country
     *
     * Flow:
     * 1. Admin sends: { "countryId": "xxx", "name": "Dubai" }
     * 2. Verify country exists
     * 3. Fetch lat/lon from Geocoding API
     * 4. Fetch weather from Open-Meteo API
     * 5. Save city with country reference
     */
    public City addCity(CityRequest request) {
        // Step 1: Verify country exists
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new RuntimeException("Country not found: " + request.getCountryId()));

        // Step 2: Check if city already exists in this country
        if (cityRepository.existsByNameIgnoreCaseAndCountryId(request.getName(), request.getCountryId())) {
            throw new RuntimeException("City already exists in " + country.getName() + ": " + request.getName());
        }

        log.info("Adding city {} to country {}", request.getName(), country.getName());

        // Step 3: Fetch coordinates from Geocoding API
        GeocodingService.GeoLocation location = geocodingService.getCoordinates(request.getName());

        // Step 4: Fetch weather from Open-Meteo API
        City.CityWeather weather = weatherService.fetchCityWeather(
                location.getLatitude(),
                location.getLongitude()
        );

        // Step 5: Create and save city
        City city = new City();
        city.setCountryId(country.getId());
        city.setCountryName(country.getName());
        city.setName(location.getDisplayName());
        city.setLatitude(location.getLatitude());
        city.setLongitude(location.getLongitude());
        city.setImageUrl(request.getImageUrl());
        city.setDescription(request.getDescription());
        city.setIsActive(true);
        city.setWeather(weather);
        city.setCreatedAt(LocalDateTime.now());
        city.setUpdatedAt(LocalDateTime.now());
        city.setWeatherUpdatedAt(LocalDateTime.now());

        City savedCity = cityRepository.save(city);
        log.info("City added: {} in {} at ({}, {})",
                savedCity.getName(), country.getName(),
                savedCity.getLatitude(), savedCity.getLongitude());

        return savedCity;
    }

    /**
     * Update city
     */
    public City updateCity(String id, CityRequest request) {
        City city = getCityById(id);

        // Verify new country if changed
        if (!city.getCountryId().equals(request.getCountryId())) {
            Country newCountry = countryRepository.findById(request.getCountryId())
                    .orElseThrow(() -> new RuntimeException("Country not found: " + request.getCountryId()));
            city.setCountryId(newCountry.getId());
            city.setCountryName(newCountry.getName());
        }

        // Check if name changed - need to re-fetch coordinates
        boolean nameChanged = !city.getName().equalsIgnoreCase(request.getName());

        if (nameChanged) {
            GeocodingService.GeoLocation location = geocodingService.getCoordinates(request.getName());
            City.CityWeather weather = weatherService.fetchCityWeather(
                    location.getLatitude(),
                    location.getLongitude()
            );

            city.setName(location.getDisplayName());
            city.setLatitude(location.getLatitude());
            city.setLongitude(location.getLongitude());
            city.setWeather(weather);
            city.setWeatherUpdatedAt(LocalDateTime.now());
        }

        city.setImageUrl(request.getImageUrl());
        city.setDescription(request.getDescription());
        city.setUpdatedAt(LocalDateTime.now());

        return cityRepository.save(city);
    }

    /**
     * Refresh weather for a city
     */
    public City refreshWeather(String cityId) {
        City city = getCityById(cityId);

        City.CityWeather weather = weatherService.fetchCityWeather(
                city.getLatitude(),
                city.getLongitude()
        );

        city.setWeather(weather);
        city.setWeatherUpdatedAt(LocalDateTime.now());

        return cityRepository.save(city);
    }

    /**
     * Refresh weather for all cities
     */
    public List<City> refreshAllWeather() {
        List<City> cities = cityRepository.findAll();

        for (City city : cities) {
            try {
                City.CityWeather weather = weatherService.fetchCityWeather(
                        city.getLatitude(),
                        city.getLongitude()
                );
                city.setWeather(weather);
                city.setWeatherUpdatedAt(LocalDateTime.now());
                cityRepository.save(city);
            } catch (Exception e) {
                log.error("Failed to refresh weather for {}: {}", city.getName(), e.getMessage());
            }
        }

        return cityRepository.findAll();
    }

    /**
     * Toggle city active status
     */
    public City toggleCityStatus(String id) {
        City city = getCityById(id);
        city.setIsActive(!city.getIsActive());
        city.setUpdatedAt(LocalDateTime.now());
        return cityRepository.save(city);
    }

    /**
     * Delete city
     */
    public void deleteCity(String id) {
        if (!cityRepository.existsById(id)) {
            throw new RuntimeException("City not found with id: " + id);
        }
        cityRepository.deleteById(id);
    }

    /**
     * Seed sample cities for a country
     */
    public List<City> addSampleCities(String countryId) {
        Country country = countryRepository.findById(countryId)
                .orElseThrow(() -> new RuntimeException("Country not found: " + countryId));

        // Sample cities based on country
        String[] sampleCities;
        switch (country.getCode()) {
            case "UAE" -> sampleCities = new String[]{"Dubai", "Abu Dhabi", "Sharjah"};
            case "PK" -> sampleCities = new String[]{"Karachi", "Lahore", "Islamabad"};
            case "UK" -> sampleCities = new String[]{"London", "Manchester", "Birmingham"};
            case "USA" -> sampleCities = new String[]{"New York", "Los Angeles", "Chicago"};
            case "JP" -> sampleCities = new String[]{"Tokyo", "Osaka", "Kyoto"};
            default -> sampleCities = new String[]{};
        }

        for (String cityName : sampleCities) {
            try {
                if (!cityRepository.existsByNameIgnoreCaseAndCountryId(cityName, countryId)) {
                    CityRequest request = new CityRequest();
                    request.setCountryId(countryId);
                    request.setName(cityName);
                    addCity(request);
                }
            } catch (Exception e) {
                log.error("Failed to add city {}: {}", cityName, e.getMessage());
            }
        }

        return cityRepository.findByCountryId(countryId);
    }
}
