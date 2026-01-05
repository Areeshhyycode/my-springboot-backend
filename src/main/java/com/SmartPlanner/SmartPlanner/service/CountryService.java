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

@Slf4j
@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final CategoryRepository categoryRepository;
    private final GeocodingService geocodingService;
    private final WeatherService weatherService;

    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }

    public List<Country> getActiveCountries() {
        return countryRepository.findByIsActiveTrue();
    }

    public List<FullCountryResponse> getAllCountriesWithCitiesAndActivities() {
        List<Country> countries = countryRepository.findByIsActiveTrue();
        List<FullCountryResponse> result = new ArrayList<>();

        for (Country country : countries) {
            result.add(buildFullCountryResponse(country));
        }

        return result;
    }

    public FullCountryResponse getCountryWithCitiesAndActivities(String countryId) {
        Country country = getCountryById(countryId);
        return buildFullCountryResponse(country);
    }

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
                        .latitude(activity.getLatitude())
                        .longitude(activity.getLongitude())
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

    public Country getCountryById(String id) {
        return countryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Country not found with id: " + id));
    }

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

    public FullCountryResponse addFullCountry(FullCountryRequest request) {
        log.info("Adding full country: {} with {} cities", request.getName(), request.getCities().size());

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

        for (FullCountryRequest.CityData cityData : request.getCities()) {
            try {
                GeocodingService.GeoLocation location = geocodingService.getCoordinates(cityData.getName());

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
                city.setImageUrl(cityData.getImageUrl());
                city.setDescription(cityData.getDescription());
                city.setIsActive(true);
                city.setWeather(weather);
                city.setCreatedAt(LocalDateTime.now());
                city.setUpdatedAt(LocalDateTime.now());
                city.setWeatherUpdatedAt(LocalDateTime.now());
                city = cityRepository.save(city);

                log.info("City created: {} in {}", city.getName(), country.getName());

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

        return buildFullCountryResponse(country);
    }

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

    public void deleteCountry(String id) {
        Country country = getCountryById(id);

        List<City> cities = cityRepository.findByCountryId(id);
        for (City city : cities) {
            List<Category> activities = categoryRepository.findByCityId(city.getId());
            categoryRepository.deleteAll(activities);
        }

        cityRepository.deleteAll(cities);

        countryRepository.deleteById(id);

        log.info("Deleted country {} with {} cities", country.getName(), cities.size());
    }

    public Country toggleCountryStatus(String id) {
        Country country = getCountryById(id);
        country.setIsActive(!country.getIsActive());
        country.setUpdatedAt(LocalDateTime.now());
        return countryRepository.save(country);
    }

    private String generateCode(String name) {
        if (name.length() <= 3) {
            return name.toUpperCase();
        }
        return name.substring(0, 3).toUpperCase();
    }
}
