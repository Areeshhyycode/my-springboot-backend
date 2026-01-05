package com.SmartPlanner.SmartPlanner.controller;

import com.SmartPlanner.SmartPlanner.dto.PackingSuggestionsResponse;
import com.SmartPlanner.SmartPlanner.dto.WeatherAlertsResponse;
import com.SmartPlanner.SmartPlanner.dto.WeatherResponse;
import com.SmartPlanner.SmartPlanner.security.JwtUtil;
import com.SmartPlanner.SmartPlanner.service.PackingSuggestionService;
import com.SmartPlanner.SmartPlanner.service.WeatherAlertService;
import com.SmartPlanner.SmartPlanner.service.WeatherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TEST CLASS FOR WeatherController
 * Tests all weather-related endpoints
 */
@WebMvcTest(
        controllers = WeatherController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Use MockitoBean instead of MockBean for Spring Boot 3.4+
    @MockitoBean
    private WeatherService weatherService;

    @MockitoBean
    private WeatherAlertService weatherAlertService;

    @MockitoBean
    private PackingSuggestionService packingSuggestionService;

    // Add MockitoBean for JwtUtil like in your UserProfileControllerTest
    @MockitoBean
    private JwtUtil jwtUtil;

    private WeatherResponse weatherResponse;
    private WeatherAlertsResponse alertsResponse;
    private PackingSuggestionsResponse packingResponse;

    @BeforeEach
    void setUp() {
        // Setup weather response with nested CurrentWeather
        WeatherResponse.CurrentWeather currentWeather = WeatherResponse.CurrentWeather.builder()
                .temperature(25.5)
                .windSpeed(10.0)
                .temperatureUnit("°C")
                .windSpeedUnit("km/h")
                .time("2024-01-01T12:00")
                .build();

        weatherResponse = WeatherResponse.builder()
                .cityName("Karachi")
                .country("Pakistan")
                .current(currentWeather)
                .hourly(Collections.emptyList())
                .build();

        // Setup alerts response with nested objects
        WeatherAlertsResponse.CurrentConditions conditions = WeatherAlertsResponse.CurrentConditions.builder()
                .temperature(25.5)
                .windSpeed(10.0)
                .humidity(60)
                .weatherDescription("Clear sky")
                .time("2024-01-01T12:00")
                .build();

        WeatherAlertsResponse.WeatherStatus status = WeatherAlertsResponse.WeatherStatus.builder()
                .level("safe")
                .message("Weather conditions are safe")
                .color("green")
                .build();

        alertsResponse = WeatherAlertsResponse.builder()
                .cityName("Karachi")
                .country("Pakistan")
                .currentConditions(conditions)
                .alerts(Collections.emptyList())
                .status(status)
                .build();

        // Setup packing response with nested categories
        PackingSuggestionsResponse.WeatherSummary weatherSummary = PackingSuggestionsResponse.WeatherSummary.builder()
                .avgTemperature(25.0)
                .maxTemperature(30.0)
                .minTemperature(20.0)
                .avgHumidity(60)
                .maxWindSpeed(15.0)
                .dominantWeather("Clear")
                .period("Next 7 days")
                .build();

        PackingSuggestionsResponse.PackingItem item1 = PackingSuggestionsResponse.PackingItem.builder()
                .name("Sunglasses")
                .reason("Bright sunshine expected")
                .priority("essential")
                .icon("🕶️")
                .build();

        PackingSuggestionsResponse.PackingCategory category = PackingSuggestionsResponse.PackingCategory.builder()
                .category("Accessories")
                .icon("🎒")
                .items(List.of(item1))
                .build();

        packingResponse = PackingSuggestionsResponse.builder()
                .cityName("Karachi")
                .country("Pakistan")
                .weatherSummary(weatherSummary)
                .categories(List.of(category))
                .generalTips(List.of("Stay hydrated", "Wear sunscreen"))
                .build();
    }

    @Test
    @DisplayName("Get weather by city ID - Success")
    void testGetWeatherByCityId_Success() throws Exception {
        when(weatherService.getWeatherByCityId(anyString()))
                .thenReturn(weatherResponse);

        mockMvc.perform(get("/api/weather/city/1174872")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cityName").value("Karachi"))
                .andExpect(jsonPath("$.current.temperature").value(25.5))
                .andExpect(jsonPath("$.country").value("Pakistan"));
    }

    @Test
    @DisplayName("Get weather by city name - Success")
    void testGetWeatherByCityName_Success() throws Exception {
        when(weatherService.getWeatherByCityName(anyString()))
                .thenReturn(weatherResponse);

        mockMvc.perform(get("/api/weather/city/name/Karachi")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cityName").value("Karachi"))
                .andExpect(jsonPath("$.current.temperature").exists());
    }

    @Test
    @DisplayName("Get weather by coordinates - Success")
    void testGetWeatherByCoordinates_Success() throws Exception {
        when(weatherService.getWeatherByCoordinates(anyDouble(), anyDouble(), anyString()))
                .thenReturn(weatherResponse);

        mockMvc.perform(get("/api/weather/coordinates")
                        .param("lat", "24.8607")
                        .param("lon", "67.0011")
                        .param("cityName", "Karachi")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cityName").value("Karachi"))
                .andExpect(jsonPath("$.current.temperature").exists());
    }

    @Test
    @DisplayName("Get weather by coordinates - Without city name")
    void testGetWeatherByCoordinates_WithoutCityName() throws Exception {
        WeatherResponse.CurrentWeather current = WeatherResponse.CurrentWeather.builder()
                .temperature(20.0)
                .windSpeed(5.0)
                .temperatureUnit("°C")
                .windSpeedUnit("km/h")
                .build();

        WeatherResponse unknownCityResponse = WeatherResponse.builder()
                .cityName("Unknown")
                .current(current)
                .hourly(Collections.emptyList())
                .build();

        when(weatherService.getWeatherByCoordinates(anyDouble(), anyDouble(), eq("Unknown")))
                .thenReturn(unknownCityResponse);

        mockMvc.perform(get("/api/weather/coordinates")
                        .param("lat", "24.8607")
                        .param("lon", "67.0011")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cityName").exists());
    }

    @Test
    @DisplayName("Get weather alerts by city ID - Success")
    void testGetWeatherAlertsByCityId_Success() throws Exception {
        when(weatherAlertService.getAlertsByCityId(anyString()))
                .thenReturn(alertsResponse);

        mockMvc.perform(get("/api/weather/alerts/city/1174872")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cityName").value("Karachi"))
                .andExpect(jsonPath("$.alerts").isArray());
    }

    @Test
    @DisplayName("Get weather alerts by coordinates - Success")
    void testGetWeatherAlertsByCoordinates_Success() throws Exception {
        when(weatherAlertService.getAlertsByCoordinates(anyDouble(), anyDouble(), anyString()))
                .thenReturn(alertsResponse);

        mockMvc.perform(get("/api/weather/alerts/coordinates")
                        .param("lat", "24.8607")
                        .param("lon", "67.0011")
                        .param("cityName", "Karachi")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cityName").value("Karachi"))
                .andExpect(jsonPath("$.alerts").isArray());
    }

    @Test
    @DisplayName("Get packing suggestions by city ID - Success")
    void testGetPackingSuggestionsByCityId_Success() throws Exception {
        when(packingSuggestionService.getPackingSuggestionsByCityId(anyString()))
                .thenReturn(packingResponse);

        mockMvc.perform(get("/api/weather/packing/city/1174872")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cityName").value("Karachi"))
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.generalTips").isArray())
                .andExpect(jsonPath("$.weatherSummary").exists());
    }

    @Test
    @DisplayName("Get packing suggestions by coordinates - Success")
    void testGetPackingSuggestionsByCoordinates_Success() throws Exception {
        when(packingSuggestionService.getPackingSuggestionsByCoordinates(
                anyDouble(), anyDouble(), anyString(), anyString()))
                .thenReturn(packingResponse);

        mockMvc.perform(get("/api/weather/packing/coordinates")
                        .param("lat", "24.8607")
                        .param("lon", "67.0011")
                        .param("cityName", "Karachi")
                        .param("country", "Pakistan")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cityName").value("Karachi"))
                .andExpect(jsonPath("$.categories").isArray());
    }

    @Test
    @DisplayName("Get packing suggestions by coordinates - Without country")
    void testGetPackingSuggestionsByCoordinates_WithoutCountry() throws Exception {
        when(packingSuggestionService.getPackingSuggestionsByCoordinates(
                anyDouble(), anyDouble(), anyString(), eq("")))
                .thenReturn(packingResponse);

        mockMvc.perform(get("/api/weather/packing/coordinates")
                        .param("lat", "24.8607")
                        .param("lon", "67.0011")
                        .param("cityName", "Karachi")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cityName").exists());
    }

    @Test
    @DisplayName("Get weather by invalid coordinates - Missing parameters")
    void testGetWeatherByCoordinates_MissingParameters() throws Exception {
        mockMvc.perform(get("/api/weather/coordinates")
                        .param("lat", "24.8607")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get weather alerts - Missing required cityName parameter")
    void testGetWeatherAlertsByCoordinates_MissingCityName() throws Exception {
        mockMvc.perform(get("/api/weather/alerts/coordinates")
                        .param("lat", "24.8607")
                        .param("lon", "67.0011")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}