package com.SmartPlanner.SmartPlanner.controller;

import com.SmartPlanner.SmartPlanner.dto.CountryRequest;
import com.SmartPlanner.SmartPlanner.dto.FullCountryRequest;
import com.SmartPlanner.SmartPlanner.dto.FullCountryResponse;
import com.SmartPlanner.SmartPlanner.model.Country;
import com.SmartPlanner.SmartPlanner.security.JwtUtil;
import com.SmartPlanner.SmartPlanner.service.CountryService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller Test for CountryController
 */
@WebMvcTest(
        controllers = CountryController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)
class CountryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CountryService countryService;

    private Country testCountry;
    private CountryRequest countryRequest;
    private FullCountryRequest fullCountryRequest;
    private FullCountryResponse fullCountryResponse;

    @BeforeEach
    void setUp() {
        testCountry = new Country();
        testCountry.setId("country123");
        testCountry.setName("United Arab Emirates");
        testCountry.setCode("UAE");
        testCountry.setImageUrl("/images/uae.jpg");
        testCountry.setDescription("Land of wonders");
        testCountry.setIsActive(true);
        testCountry.setCreatedAt(LocalDateTime.now());
        testCountry.setUpdatedAt(LocalDateTime.now());

        countryRequest = new CountryRequest();
        countryRequest.setName("United Arab Emirates");
        countryRequest.setCode("UAE");
        countryRequest.setImageUrl("/images/uae.jpg");
        countryRequest.setDescription("Land of wonders");

        // Setup FullCountryRequest
        FullCountryRequest.ActivityData activityData = new FullCountryRequest.ActivityData();
        activityData.setName("Beach");
        activityData.setDescription("Beach activities");
        activityData.setPricePerHour(new BigDecimal("50.00"));
        activityData.setPricePerDay(new BigDecimal("200.00"));

        FullCountryRequest.CityData cityData = new FullCountryRequest.CityData();
        cityData.setName("Dubai");
        cityData.setDescription("City of Gold");
        cityData.setActivities(Arrays.asList(activityData));

        fullCountryRequest = new FullCountryRequest();
        fullCountryRequest.setName("United Arab Emirates");
        fullCountryRequest.setCode("UAE");
        fullCountryRequest.setCities(Arrays.asList(cityData));

        // Setup FullCountryResponse
        FullCountryResponse.ActivityInfo activityInfo = FullCountryResponse.ActivityInfo.builder()
                .id("act123")
                .name("Beach")
                .description("Beach activities")
                .pricePerHour(new BigDecimal("50.00"))
                .pricePerDay(new BigDecimal("200.00"))
                .build();

        FullCountryResponse.CityWithActivities cityWithActivities = FullCountryResponse.CityWithActivities.builder()
                .id("city123")
                .name("Dubai")
                .latitude(25.2048)
                .longitude(55.2708)
                .activities(Arrays.asList(activityInfo))
                .build();

        fullCountryResponse = FullCountryResponse.builder()
                .id("country123")
                .name("United Arab Emirates")
                .code("UAE")
                .cities(Arrays.asList(cityWithActivities))
                .build();
    }

    // ==================== PUBLIC APIs ====================

    @Test
    @DisplayName("GET /api/v1/countries/full - Get all countries with cities and activities")
    void testGetAllCountriesWithCitiesAndActivities() throws Exception {
        List<FullCountryResponse> responses = Arrays.asList(fullCountryResponse);
        when(countryService.getAllCountriesWithCitiesAndActivities()).thenReturn(responses);

        mockMvc.perform(get("/api/v1/countries/full"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("country123"))
                .andExpect(jsonPath("$[0].name").value("United Arab Emirates"))
                .andExpect(jsonPath("$[0].cities[0].name").value("Dubai"));

        verify(countryService).getAllCountriesWithCitiesAndActivities();
    }

    @Test
    @DisplayName("GET /api/v1/countries/{id}/full - Get country with cities and activities")
    void testGetCountryWithCitiesAndActivities_Success() throws Exception {
        when(countryService.getCountryWithCitiesAndActivities("country123")).thenReturn(fullCountryResponse);

        mockMvc.perform(get("/api/v1/countries/country123/full"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("country123"))
                .andExpect(jsonPath("$.cities[0].name").value("Dubai"));

        verify(countryService).getCountryWithCitiesAndActivities("country123");
    }

    @Test
    @DisplayName("GET /api/v1/countries/{id}/full - Country not found")
    void testGetCountryWithCitiesAndActivities_NotFound() throws Exception {
        when(countryService.getCountryWithCitiesAndActivities("invalid"))
                .thenThrow(new RuntimeException("Country not found"));

        mockMvc.perform(get("/api/v1/countries/invalid/full"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Country not found"));
    }

    @Test
    @DisplayName("GET /api/v1/countries - Get all countries (basic)")
    void testGetAllCountries() throws Exception {
        List<Country> countries = Arrays.asList(testCountry);
        when(countryService.getActiveCountries()).thenReturn(countries);

        mockMvc.perform(get("/api/v1/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("country123"))
                .andExpect(jsonPath("$[0].name").value("United Arab Emirates"));

        verify(countryService).getActiveCountries();
    }

    @Test
    @DisplayName("GET /api/v1/countries/{id} - Get country by ID - Success")
    void testGetCountryById_Success() throws Exception {
        when(countryService.getCountryById("country123")).thenReturn(testCountry);

        mockMvc.perform(get("/api/v1/countries/country123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("country123"))
                .andExpect(jsonPath("$.code").value("UAE"));

        verify(countryService).getCountryById("country123");
    }

    @Test
    @DisplayName("GET /api/v1/countries/{id} - Get country by ID - Not Found")
    void testGetCountryById_NotFound() throws Exception {
        when(countryService.getCountryById("invalid"))
                .thenThrow(new RuntimeException("Country not found"));

        mockMvc.perform(get("/api/v1/countries/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Country not found"));
    }

    // ==================== ADMIN APIs ====================

    @Test
    @DisplayName("POST /api/v1/admin/countries/full - Add full country - Success")
    void testAddFullCountry_Success() throws Exception {
        when(countryService.addFullCountry(any(FullCountryRequest.class))).thenReturn(fullCountryResponse);

        mockMvc.perform(post("/api/v1/admin/countries/full")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fullCountryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("country123"))
                .andExpect(jsonPath("$.cities[0].name").value("Dubai"));

        verify(countryService).addFullCountry(any(FullCountryRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/admin/countries/full - Add full country - Duplicate")
    void testAddFullCountry_Duplicate() throws Exception {
        when(countryService.addFullCountry(any(FullCountryRequest.class)))
                .thenThrow(new RuntimeException("Country already exists"));

        mockMvc.perform(post("/api/v1/admin/countries/full")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fullCountryRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Country already exists"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/countries - Add country - Success")
    void testAddCountry_Success() throws Exception {
        when(countryService.addCountry(any(CountryRequest.class))).thenReturn(testCountry);

        mockMvc.perform(post("/api/v1/admin/countries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(countryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("country123"))
                .andExpect(jsonPath("$.name").value("United Arab Emirates"));

        verify(countryService).addCountry(any(CountryRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/admin/countries - Add country - Duplicate")
    void testAddCountry_Duplicate() throws Exception {
        when(countryService.addCountry(any(CountryRequest.class)))
                .thenThrow(new RuntimeException("Country already exists"));

        mockMvc.perform(post("/api/v1/admin/countries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(countryRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Country already exists"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/countries - Add country - Invalid request")
    void testAddCountry_Invalid() throws Exception {
        CountryRequest invalidRequest = new CountryRequest();
        // Missing required name field

        mockMvc.perform(post("/api/v1/admin/countries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/admin/countries/{id} - Update country - Success")
    void testUpdateCountry_Success() throws Exception {
        when(countryService.updateCountry(eq("country123"), any(CountryRequest.class))).thenReturn(testCountry);

        mockMvc.perform(put("/api/v1/admin/countries/country123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(countryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("country123"));

        verify(countryService).updateCountry(eq("country123"), any(CountryRequest.class));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/countries/{id} - Update country - Not Found")
    void testUpdateCountry_NotFound() throws Exception {
        when(countryService.updateCountry(eq("invalid"), any(CountryRequest.class)))
                .thenThrow(new RuntimeException("Country not found"));

        mockMvc.perform(put("/api/v1/admin/countries/invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(countryRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Country not found"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/countries/{id} - Delete country - Success")
    void testDeleteCountry_Success() throws Exception {
        doNothing().when(countryService).deleteCountry("country123");

        mockMvc.perform(delete("/api/v1/admin/countries/country123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Country and all its data deleted successfully"));

        verify(countryService).deleteCountry("country123");
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/countries/{id} - Delete country - Not Found")
    void testDeleteCountry_NotFound() throws Exception {
        doThrow(new RuntimeException("Country not found")).when(countryService).deleteCountry("invalid");

        mockMvc.perform(delete("/api/v1/admin/countries/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Country not found"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/countries/{id}/toggle - Toggle status - Success")
    void testToggleCountryStatus_Success() throws Exception {
        testCountry.setIsActive(false);
        when(countryService.toggleCountryStatus("country123")).thenReturn(testCountry);

        mockMvc.perform(patch("/api/v1/admin/countries/country123/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        verify(countryService).toggleCountryStatus("country123");
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/countries/{id}/toggle - Country not found")
    void testToggleCountryStatus_NotFound() throws Exception {
        when(countryService.toggleCountryStatus("invalid"))
                .thenThrow(new RuntimeException("Country not found"));

        mockMvc.perform(patch("/api/v1/admin/countries/invalid/toggle"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Country not found"));
    }
}
