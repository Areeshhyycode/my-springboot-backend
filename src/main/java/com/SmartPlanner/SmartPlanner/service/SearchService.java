package com.SmartPlanner.SmartPlanner.service;

import com.SmartPlanner.SmartPlanner.dto.SearchResponse;
import com.SmartPlanner.SmartPlanner.model.Category;
import com.SmartPlanner.SmartPlanner.model.City;
import com.SmartPlanner.SmartPlanner.model.Country;
import com.SmartPlanner.SmartPlanner.repository.CategoryRepository;
import com.SmartPlanner.SmartPlanner.repository.CityRepository;
import com.SmartPlanner.SmartPlanner.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final CategoryRepository categoryRepository;
    private final WeatherService weatherService;

    public SearchResponse search(String query) {
        log.info("Searching for: {}", query);

        List<SearchResponse.SearchResult> results = new ArrayList<>();

        List<Country> countries = countryRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(query);
        for (Country country : countries) {
            results.add(SearchResponse.SearchResult.builder()
                    .type("COUNTRY")
                    .id(country.getId())
                    .name(country.getName())
                    .description(country.getDescription())
                    .imageUrl(country.getImageUrl())
                    .build());
        }

        List<City> cities = cityRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(query);
        for (City city : cities) {
            List<Category> activities = categoryRepository.findByCityIdAndIsActiveTrue(city.getId());
            List<SearchResponse.ActivityInfo> activityList = new ArrayList<>();

            for (Category activity : activities) {
                activityList.add(SearchResponse.ActivityInfo.builder()
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

            results.add(SearchResponse.SearchResult.builder()
                    .type("CITY")
                    .id(city.getId())
                    .name(city.getName())
                    .description(city.getDescription())
                    .imageUrl(city.getImageUrl())
                    .countryId(city.getCountryId())
                    .countryName(city.getCountryName())
                    .latitude(city.getLatitude())
                    .longitude(city.getLongitude())
                    .weather(city.getWeather())
                    .activities(activityList)
                    .build());
        }

        return SearchResponse.builder()
                .query(query)
                .totalResults(results.size())
                .results(results)
                .build();
    }

    public SearchResponse searchCities(String query) {
        log.info("Searching cities for: {}", query);

        List<SearchResponse.SearchResult> results = new ArrayList<>();
        List<City> cities = cityRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(query);

        for (City city : cities) {
            List<Category> activities = categoryRepository.findByCityIdAndIsActiveTrue(city.getId());
            List<SearchResponse.ActivityInfo> activityList = new ArrayList<>();

            for (Category activity : activities) {
                activityList.add(SearchResponse.ActivityInfo.builder()
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

            results.add(SearchResponse.SearchResult.builder()
                    .type("CITY")
                    .id(city.getId())
                    .name(city.getName())
                    .description(city.getDescription())
                    .imageUrl(city.getImageUrl())
                    .countryId(city.getCountryId())
                    .countryName(city.getCountryName())
                    .latitude(city.getLatitude())
                    .longitude(city.getLongitude())
                    .weather(city.getWeather())
                    .activities(activityList)
                    .build());
        }

        return SearchResponse.builder()
                .query(query)
                .totalResults(results.size())
                .results(results)
                .build();
    }

    public SearchResponse.SearchResult getCityWithWeather(String cityName) {
        City city = cityRepository.findByNameIgnoreCase(cityName)
                .orElseThrow(() -> new RuntimeException("City not found: " + cityName));

        City.CityWeather freshWeather = weatherService.fetchCityWeather(
                city.getLatitude(),
                city.getLongitude()
        );

        List<Category> activities = categoryRepository.findByCityIdAndIsActiveTrue(city.getId());
        List<SearchResponse.ActivityInfo> activityList = new ArrayList<>();

        for (Category activity : activities) {
            activityList.add(SearchResponse.ActivityInfo.builder()
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

        return SearchResponse.SearchResult.builder()
                .type("CITY")
                .id(city.getId())
                .name(city.getName())
                .description(city.getDescription())
                .imageUrl(city.getImageUrl())
                .countryId(city.getCountryId())
                .countryName(city.getCountryName())
                .latitude(city.getLatitude())
                .longitude(city.getLongitude())
                .weather(freshWeather != null ? freshWeather : city.getWeather())
                .activities(activityList)
                .build();
    }
}
