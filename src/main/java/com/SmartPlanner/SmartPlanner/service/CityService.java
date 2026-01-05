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

@Slf4j
@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final GeocodingService geocodingService;
    private final WeatherService weatherService;

    public List<City> getAllCities() {
        return cityRepository.findAll();
    }

    public List<City> getCitiesByCountry(String countryId) {
        if (!countryRepository.existsById(countryId)) {
            throw new RuntimeException("Country not found: " + countryId);
        }
        return cityRepository.findByCountryIdAndIsActiveTrue(countryId);
    }

    public City getCityById(String id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("City not found with id: " + id));
    }

    public City getCityByName(String name) {
        return cityRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new RuntimeException("City not found: " + name));
    }

    public City addCity(CityRequest request) {
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new RuntimeException("Country not found: " + request.getCountryId()));

        if (cityRepository.existsByNameIgnoreCaseAndCountryId(request.getName(), request.getCountryId())) {
            throw new RuntimeException("City already exists in " + country.getName() + ": " + request.getName());
        }

        log.info("Adding city {} to country {}", request.getName(), country.getName());

        GeocodingService.GeoLocation location = geocodingService.getCoordinates(request.getName());

        City.CityWeather weather = weatherService.fetchCityWeather(
                location.getLatitude(),
                location.getLongitude()
        );

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

    public City updateCity(String id, CityRequest request) {
        City city = getCityById(id);

        if (!city.getCountryId().equals(request.getCountryId())) {
            Country newCountry = countryRepository.findById(request.getCountryId())
                    .orElseThrow(() -> new RuntimeException("Country not found: " + request.getCountryId()));
            city.setCountryId(newCountry.getId());
            city.setCountryName(newCountry.getName());
        }

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

    public City toggleCityStatus(String id) {
        City city = getCityById(id);
        city.setIsActive(!city.getIsActive());
        city.setUpdatedAt(LocalDateTime.now());
        return cityRepository.save(city);
    }

    public void deleteCity(String id) {
        if (!cityRepository.existsById(id)) {
            throw new RuntimeException("City not found with id: " + id);
        }
        cityRepository.deleteById(id);
    }

    public List<City> addSampleCities(String countryId) {
        Country country = countryRepository.findById(countryId)
                .orElseThrow(() -> new RuntimeException("Country not found: " + countryId));

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
