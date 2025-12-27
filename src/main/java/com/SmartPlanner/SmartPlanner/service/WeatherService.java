package com.SmartPlanner.SmartPlanner.service;

import com.SmartPlanner.SmartPlanner.dto.OpenMeteoResponse;
import com.SmartPlanner.SmartPlanner.dto.WeatherResponse;
import com.SmartPlanner.SmartPlanner.model.City;
import com.SmartPlanner.SmartPlanner.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * WEATHER SERVICE - Open-Meteo API se weather data fetch karta hai
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final CityRepository cityRepository;
    private final RestTemplate restTemplate;

    // Open-Meteo API base URL (with humidity for storing)
    private static final String WEATHER_API_URL =
            "https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}" +
                    "&current=temperature_2m,wind_speed_10m,relative_humidity_2m,weather_code" +
                    "&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m";

    /**
     * City ID se weather fetch karo
     */
    public WeatherResponse getWeatherByCityId(String cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new RuntimeException("City not found with id: " + cityId));

        return fetchWeatherForCity(city);
    }

    /**
     * City name se weather fetch karo
     */
    public WeatherResponse getWeatherByCityName(String cityName) {
        City city = cityRepository.findByNameIgnoreCase(cityName)
                .orElseThrow(() -> new RuntimeException("City not found: " + cityName));

        return fetchWeatherForCity(city);
    }

    /**
     * Latitude/Longitude se direct weather fetch karo
     */
    public WeatherResponse getWeatherByCoordinates(Double lat, Double lon, String cityName) {
        return fetchWeather(lat, lon, cityName, "");
    }

    /**
     * Open-Meteo API call karo aur response map karo
     */
    private WeatherResponse fetchWeatherForCity(City city) {
        return fetchWeather(city.getLatitude(), city.getLongitude(), city.getName(), city.getCountryName());
    }

    private WeatherResponse fetchWeather(Double lat, Double lon, String cityName, String country) {
        try {
            log.info("Fetching weather for {} ({}, {})", cityName, lat, lon);

            // API call
            OpenMeteoResponse apiResponse = restTemplate.getForObject(
                    WEATHER_API_URL,
                    OpenMeteoResponse.class,
                    lat, lon
            );

            if (apiResponse == null) {
                throw new RuntimeException("Failed to fetch weather data");
            }

            // Response build karo
            return buildWeatherResponse(apiResponse, cityName, country);

        } catch (Exception e) {
            log.error("Error fetching weather for {}: {}", cityName, e.getMessage());
            throw new RuntimeException("Failed to fetch weather: " + e.getMessage());
        }
    }

    /**
     * API response ko clean WeatherResponse mein convert karo
     */
    private WeatherResponse buildWeatherResponse(OpenMeteoResponse api, String cityName, String country) {
        // Current weather
        WeatherResponse.CurrentWeather current = WeatherResponse.CurrentWeather.builder()
                .time(api.getCurrent().getTime())
                .temperature(api.getCurrent().getTemperature())
                .windSpeed(api.getCurrent().getWindSpeed())
                .temperatureUnit("°C")
                .windSpeedUnit("km/h")
                .build();

        // Hourly weather (next 24 hours)
        List<WeatherResponse.HourlyWeather> hourlyList = new ArrayList<>();
        int hoursToShow = Math.min(24, api.getHourly().getTime().size());

        for (int i = 0; i < hoursToShow; i++) {
            WeatherResponse.HourlyWeather hourly = WeatherResponse.HourlyWeather.builder()
                    .time(api.getHourly().getTime().get(i))
                    .temperature(api.getHourly().getTemperature().get(i))
                    .humidity(api.getHourly().getHumidity().get(i))
                    .windSpeed(api.getHourly().getWindSpeed().get(i))
                    .build();
            hourlyList.add(hourly);
        }

        return WeatherResponse.builder()
                .cityName(cityName)
                .country(country)
                .current(current)
                .hourly(hourlyList)
                .build();
    }

    /**
     * Fetch CityWeather object for storing in City model
     * Called when admin adds a new city
     */
    public City.CityWeather fetchCityWeather(Double lat, Double lon) {
        try {
            log.info("Fetching weather for coordinates ({}, {})", lat, lon);

            OpenMeteoResponse apiResponse = restTemplate.getForObject(
                    WEATHER_API_URL,
                    OpenMeteoResponse.class,
                    lat, lon
            );

            if (apiResponse == null || apiResponse.getCurrent() == null) {
                throw new RuntimeException("Failed to fetch weather data");
            }

            // Get humidity from first hourly entry if current doesn't have it
            Integer humidity = null;
            if (apiResponse.getHourly() != null &&
                apiResponse.getHourly().getHumidity() != null &&
                !apiResponse.getHourly().getHumidity().isEmpty()) {
                humidity = apiResponse.getHourly().getHumidity().get(0);
            }

            // Map weather code to description
            String weatherDescription = getWeatherDescription(apiResponse.getCurrent().getWeatherCode());

            return new City.CityWeather(
                    apiResponse.getCurrent().getTemperature(),
                    apiResponse.getCurrent().getWindSpeed(),
                    humidity,
                    String.valueOf(apiResponse.getCurrent().getWeatherCode()),
                    weatherDescription
            );

        } catch (Exception e) {
            log.error("Error fetching weather: {}", e.getMessage());
            // Return null weather instead of failing
            return null;
        }
    }

    /**
     * Weather code ko human readable description mein convert karo
     * Based on WMO Weather interpretation codes
     */
    private String getWeatherDescription(Integer code) {
        if (code == null) return "Unknown";

        return switch (code) {
            case 0 -> "Clear sky";
            case 1, 2, 3 -> "Partly cloudy";
            case 45, 48 -> "Foggy";
            case 51, 53, 55 -> "Drizzle";
            case 61, 63, 65 -> "Rainy";
            case 71, 73, 75 -> "Snowy";
            case 77 -> "Snow grains";
            case 80, 81, 82 -> "Rain showers";
            case 85, 86 -> "Snow showers";
            case 95 -> "Thunderstorm";
            case 96, 99 -> "Thunderstorm with hail";
            default -> "Unknown";
        };
    }
}
