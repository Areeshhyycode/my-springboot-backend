package com.SmartPlanner.SmartPlanner.controller;

import com.SmartPlanner.SmartPlanner.dto.CityRequest;
import com.SmartPlanner.SmartPlanner.model.City;
import com.SmartPlanner.SmartPlanner.security.JwtUtil;
import com.SmartPlanner.SmartPlanner.service.CityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller Test for CityController
 */
@WebMvcTest(
        controllers = CityController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)
class CityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CityService cityService;

    private City testCity;
    private CityRequest cityRequest;

    @BeforeEach
    void setUp() {
        City.CityWeather weather = new City.CityWeather();
        weather.setTemperature(28.5);
        weather.setWindSpeed(15.0);
        weather.setHumidity(65);
        weather.setWeatherCode("1");
        weather.setDescription("Sunny");

        testCity = new City();
        testCity.setId("city123");
        testCity.setCountryId("country123");
        testCity.setCountryName("UAE");
        testCity.setName("Dubai");
        testCity.setLatitude(25.2048);
        testCity.setLongitude(55.2708);
        testCity.setImageUrl("/images/dubai.jpg");
        testCity.setDescription("City of Gold");
        testCity.setIsActive(true);
        testCity.setWeather(weather);
        testCity.setCreatedAt(LocalDateTime.now());
        testCity.setUpdatedAt(LocalDateTime.now());

        cityRequest = new CityRequest();
        cityRequest.setCountryId("country123");
        cityRequest.setName("Dubai");
        cityRequest.setImageUrl("/images/dubai.jpg");
        cityRequest.setDescription("City of Gold");
    }

    // ==================== PUBLIC APIs ====================

    @Test
    @DisplayName("GET /api/v1/cities - Get all cities")
    void testGetAllCities() throws Exception {
        List<City> cities = Arrays.asList(testCity);
        when(cityService.getAllCities()).thenReturn(cities);

        mockMvc.perform(get("/api/v1/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("city123"))
                .andExpect(jsonPath("$[0].name").value("Dubai"));

        verify(cityService).getAllCities();
    }

    @Test
    @DisplayName("GET /api/v1/cities/{id} - Get city by ID - Success")
    void testGetCityById_Success() throws Exception {
        when(cityService.getCityById("city123")).thenReturn(testCity);

        mockMvc.perform(get("/api/v1/cities/city123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("city123"))
                .andExpect(jsonPath("$.name").value("Dubai"))
                .andExpect(jsonPath("$.weather.temperature").value(28.5));

        verify(cityService).getCityById("city123");
    }

    @Test
    @DisplayName("GET /api/v1/cities/{id} - Get city by ID - Not Found")
    void testGetCityById_NotFound() throws Exception {
        when(cityService.getCityById("invalid")).thenThrow(new RuntimeException("City not found"));

        mockMvc.perform(get("/api/v1/cities/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("City not found"));
    }

    @Test
    @DisplayName("GET /api/v1/countries/{countryId}/cities - Get cities by country")
    void testGetCitiesByCountry_Success() throws Exception {
        List<City> cities = Arrays.asList(testCity);
        when(cityService.getCitiesByCountry("country123")).thenReturn(cities);

        mockMvc.perform(get("/api/v1/countries/country123/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("city123"))
                .andExpect(jsonPath("$[0].countryId").value("country123"));

        verify(cityService).getCitiesByCountry("country123");
    }

    @Test
    @DisplayName("GET /api/v1/countries/{countryId}/cities - Country not found")
    void testGetCitiesByCountry_CountryNotFound() throws Exception {
        when(cityService.getCitiesByCountry("invalid"))
                .thenThrow(new RuntimeException("Country not found"));

        mockMvc.perform(get("/api/v1/countries/invalid/cities"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Country not found"));
    }

    // ==================== ADMIN APIs ====================

    @Test
    @DisplayName("POST /api/v1/admin/cities - Add city - Success")
    void testAddCity_Success() throws Exception {
        when(cityService.addCity(any(CityRequest.class))).thenReturn(testCity);

        mockMvc.perform(post("/api/v1/admin/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cityRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("city123"))
                .andExpect(jsonPath("$.name").value("Dubai"));

        verify(cityService).addCity(any(CityRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/admin/cities - Add city - Country not found")
    void testAddCity_CountryNotFound() throws Exception {
        when(cityService.addCity(any(CityRequest.class)))
                .thenThrow(new RuntimeException("Country not found"));

        mockMvc.perform(post("/api/v1/admin/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cityRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Country not found"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/cities - Add city - Invalid request")
    void testAddCity_Invalid() throws Exception {
        CityRequest invalidRequest = new CityRequest();
        // Missing required fields

        mockMvc.perform(post("/api/v1/admin/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/admin/cities/{id} - Update city - Success")
    void testUpdateCity_Success() throws Exception {
        when(cityService.updateCity(eq("city123"), any(CityRequest.class))).thenReturn(testCity);

        mockMvc.perform(put("/api/v1/admin/cities/city123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cityRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("city123"));

        verify(cityService).updateCity(eq("city123"), any(CityRequest.class));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/cities/{id} - Update city - Not Found")
    void testUpdateCity_NotFound() throws Exception {
        when(cityService.updateCity(eq("invalid"), any(CityRequest.class)))
                .thenThrow(new RuntimeException("City not found"));

        mockMvc.perform(put("/api/v1/admin/cities/invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cityRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("City not found"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/cities/{id} - Delete city - Success")
    void testDeleteCity_Success() throws Exception {
        doNothing().when(cityService).deleteCity("city123");

        mockMvc.perform(delete("/api/v1/admin/cities/city123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("City deleted successfully"));

        verify(cityService).deleteCity("city123");
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/cities/{id} - Delete city - Not Found")
    void testDeleteCity_NotFound() throws Exception {
        doThrow(new RuntimeException("City not found")).when(cityService).deleteCity("invalid");

        mockMvc.perform(delete("/api/v1/admin/cities/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("City not found"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/cities/{id}/toggle - Toggle status - Success")
    void testToggleCityStatus_Success() throws Exception {
        testCity.setIsActive(false);
        when(cityService.toggleCityStatus("city123")).thenReturn(testCity);

        mockMvc.perform(patch("/api/v1/admin/cities/city123/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        verify(cityService).toggleCityStatus("city123");
    }

    @Test
    @DisplayName("POST /api/v1/admin/cities/{id}/refresh-weather - Refresh weather - Success")
    void testRefreshCityWeather_Success() throws Exception {
        when(cityService.refreshWeather("city123")).thenReturn(testCity);

        mockMvc.perform(post("/api/v1/admin/cities/city123/refresh-weather"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("city123"))
                .andExpect(jsonPath("$.weather").exists());

        verify(cityService).refreshWeather("city123");
    }

    @Test
    @DisplayName("POST /api/v1/admin/cities/{id}/refresh-weather - City not found")
    void testRefreshCityWeather_NotFound() throws Exception {
        when(cityService.refreshWeather("invalid"))
                .thenThrow(new RuntimeException("City not found"));

        mockMvc.perform(post("/api/v1/admin/cities/invalid/refresh-weather"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("City not found"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/cities/refresh-all-weather - Refresh all weather")
    void testRefreshAllWeather() throws Exception {
        List<City> cities = Arrays.asList(testCity);
        when(cityService.refreshAllWeather()).thenReturn(cities);

        mockMvc.perform(post("/api/v1/admin/cities/refresh-all-weather"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("city123"));

        verify(cityService).refreshAllWeather();
    }

    @Test
    @DisplayName("POST /api/v1/admin/countries/{countryId}/cities/seed - Seed cities - Success")
    void testSeedCities_Success() throws Exception {
        List<City> cities = Arrays.asList(testCity);
        when(cityService.addSampleCities("country123")).thenReturn(cities);

        mockMvc.perform(post("/api/v1/admin/countries/country123/cities/seed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("city123"));

        verify(cityService).addSampleCities("country123");
    }

    @Test
    @DisplayName("POST /api/v1/admin/countries/{countryId}/cities/seed - Country not found")
    void testSeedCities_CountryNotFound() throws Exception {
        when(cityService.addSampleCities("invalid"))
                .thenThrow(new RuntimeException("Country not found"));

        mockMvc.perform(post("/api/v1/admin/countries/invalid/cities/seed"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Country not found"));
    }
}
