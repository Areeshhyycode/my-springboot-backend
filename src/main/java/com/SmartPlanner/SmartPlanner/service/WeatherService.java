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

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final CityRepository cityRepository;
    private final RestTemplate restTemplate;

    private static final String WEATHER_API_URL =
            "https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}" +
                    "&current=temperature_2m,wind_speed_10m,relative_humidity_2m,weather_code" +
                    "&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m" +
                    "&timezone=auto";

    public WeatherResponse getWeatherByCityId(String cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new RuntimeException("City not found with id: " + cityId));

        return fetchWeatherForCity(city);
    }

    public WeatherResponse getWeatherByCityName(String cityName) {
        City city = cityRepository.findByNameIgnoreCase(cityName)
                .orElseThrow(() -> new RuntimeException("City not found: " + cityName));

        return fetchWeatherForCity(city);
    }

    public WeatherResponse getWeatherByCoordinates(Double lat, Double lon, String cityName) {
        return fetchWeather(lat, lon, cityName, "");
    }

    private WeatherResponse fetchWeatherForCity(City city) {
        return fetchWeather(city.getLatitude(), city.getLongitude(), city.getName(), city.getCountryName());
    }

    private WeatherResponse fetchWeather(Double lat, Double lon, String cityName, String country) {
        try {
            log.info("Fetching weather for {} ({}, {})", cityName, lat, lon);

            OpenMeteoResponse apiResponse = restTemplate.getForObject(
                    WEATHER_API_URL,
                    OpenMeteoResponse.class,
                    lat, lon
            );

            if (apiResponse == null || apiResponse.getCurrent() == null) {
                throw new RuntimeException("Failed to fetch weather data");
            }

            return buildWeatherResponse(apiResponse, cityName, country);

        } catch (Exception e) {
            log.error("Error fetching weather for {}: {}", cityName, e.getMessage());
            throw new RuntimeException("Failed to fetch weather: " + e.getMessage());
        }
    }

    private WeatherResponse buildWeatherResponse(OpenMeteoResponse api, String cityName, String country) {
        WeatherResponse.CurrentWeather current = WeatherResponse.CurrentWeather.builder()
                .time(api.getCurrent().getTime())
                .temperature(api.getCurrent().getTemperature2m())
                .windSpeed(api.getCurrent().getWindSpeed10m())
                .temperatureUnit(api.getCurrentUnits() != null ? api.getCurrentUnits().getTemperature2m() : "°C")
                .windSpeedUnit(api.getCurrentUnits() != null ? api.getCurrentUnits().getWindSpeed10m() : "km/h")
                .build();

        List<WeatherResponse.HourlyWeather> hourlyList = new ArrayList<>();

        if (api.getHourly() != null &&
                api.getHourly().getTime() != null &&
                api.getHourly().getTemperature2m() != null) {

            int hoursToShow = Math.min(24, api.getHourly().getTime().size());

            for (int i = 0; i < hoursToShow; i++) {
                WeatherResponse.HourlyWeather hourly = WeatherResponse.HourlyWeather.builder()
                        .time(api.getHourly().getTime().get(i))
                        .temperature(api.getHourly().getTemperature2m().get(i))
                        .humidity(api.getHourly().getRelativeHumidity2m() != null ?
                                api.getHourly().getRelativeHumidity2m().get(i) : null)
                        .windSpeed(api.getHourly().getWindSpeed10m().get(i))
                        .build();
                hourlyList.add(hourly);
            }
        }

        return WeatherResponse.builder()
                .cityName(cityName)
                .country(country)
                .current(current)
                .hourly(hourlyList)
                .build();
    }

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

            Integer humidity = apiResponse.getCurrent().getRelativeHumidity2m();
            if (humidity == null && apiResponse.getHourly() != null &&
                    apiResponse.getHourly().getRelativeHumidity2m() != null &&
                    !apiResponse.getHourly().getRelativeHumidity2m().isEmpty()) {
                humidity = apiResponse.getHourly().getRelativeHumidity2m().get(0);
            }

            String weatherDescription = getWeatherDescription(apiResponse.getCurrent().getWeatherCode());

            return new City.CityWeather(
                    apiResponse.getCurrent().getTemperature2m(),
                    apiResponse.getCurrent().getWindSpeed10m(),
                    humidity,
                    String.valueOf(apiResponse.getCurrent().getWeatherCode()),
                    weatherDescription
            );

        } catch (Exception e) {
            log.error("Error fetching weather: {}", e.getMessage());
            return null;
        }
    }

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
