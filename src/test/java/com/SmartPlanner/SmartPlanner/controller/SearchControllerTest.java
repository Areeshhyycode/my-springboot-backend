package com.SmartPlanner.SmartPlanner.controller;

import com.SmartPlanner.SmartPlanner.dto.SearchResponse;
import com.SmartPlanner.SmartPlanner.model.City;
import com.SmartPlanner.SmartPlanner.security.JwtUtil;
import com.SmartPlanner.SmartPlanner.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller Test for SearchController
 */
@WebMvcTest(
        controllers = SearchController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private SearchService searchService;

    private SearchResponse searchResponse;
    private SearchResponse.SearchResult cityResult;
    private SearchResponse.SearchResult countryResult;

    @BeforeEach
    void setUp() {
        City.CityWeather weather = new City.CityWeather();
        weather.setTemperature(28.5);
        weather.setWindSpeed(15.0);
        weather.setHumidity(65);
        weather.setDescription("Sunny");

        SearchResponse.ActivityInfo activityInfo = SearchResponse.ActivityInfo.builder()
                .id("act123")
                .name("Beach")
                .description("Beach activities")
                .pricePerHour(new BigDecimal("50.00"))
                .pricePerDay(new BigDecimal("200.00"))
                .build();

        cityResult = SearchResponse.SearchResult.builder()
                .type("CITY")
                .id("city123")
                .name("Dubai")
                .description("City of Gold")
                .imageUrl("/images/dubai.jpg")
                .countryId("country123")
                .countryName("UAE")
                .latitude(25.2048)
                .longitude(55.2708)
                .weather(weather)
                .activities(Arrays.asList(activityInfo))
                .build();

        countryResult = SearchResponse.SearchResult.builder()
                .type("COUNTRY")
                .id("country123")
                .name("United Arab Emirates")
                .description("Land of wonders")
                .imageUrl("/images/uae.jpg")
                .build();

        searchResponse = SearchResponse.builder()
                .query("dubai")
                .totalResults(2)
                .results(Arrays.asList(countryResult, cityResult))
                .build();
    }

    // ==================== SEARCH ALL ====================

    @Test
    @DisplayName("GET /api/v1/search?q=dubai - Search all - Success")
    void testSearch_Success() throws Exception {
        when(searchService.search("dubai")).thenReturn(searchResponse);

        mockMvc.perform(get("/api/v1/search")
                        .param("q", "dubai"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("dubai"))
                .andExpect(jsonPath("$.totalResults").value(2))
                .andExpect(jsonPath("$.results[0].type").value("COUNTRY"))
                .andExpect(jsonPath("$.results[1].type").value("CITY"));

        verify(searchService).search("dubai");
    }

    @Test
    @DisplayName("GET /api/v1/search?q= - Search with empty query")
    void testSearch_EmptyQuery() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                        .param("q", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Search query is required"));

        verify(searchService, never()).search(any());
    }

    @Test
    @DisplayName("GET /api/v1/search?q=   - Search with blank query")
    void testSearch_BlankQuery() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                        .param("q", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Search query is required"));

        verify(searchService, never()).search(any());
    }

    @Test
    @DisplayName("GET /api/v1/search?q=xyz - Search with no results")
    void testSearch_NoResults() throws Exception {
        SearchResponse emptyResponse = SearchResponse.builder()
                .query("xyz")
                .totalResults(0)
                .results(Collections.emptyList())
                .build();

        when(searchService.search("xyz")).thenReturn(emptyResponse);

        mockMvc.perform(get("/api/v1/search")
                        .param("q", "xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("xyz"))
                .andExpect(jsonPath("$.totalResults").value(0))
                .andExpect(jsonPath("$.results").isEmpty());

        verify(searchService).search("xyz");
    }

    // ==================== SEARCH CITIES ONLY ====================

    @Test
    @DisplayName("GET /api/v1/search/cities?q=dubai - Search cities - Success")
    void testSearchCities_Success() throws Exception {
        SearchResponse cityOnlyResponse = SearchResponse.builder()
                .query("dubai")
                .totalResults(1)
                .results(Arrays.asList(cityResult))
                .build();

        when(searchService.searchCities("dubai")).thenReturn(cityOnlyResponse);

        mockMvc.perform(get("/api/v1/search/cities")
                        .param("q", "dubai"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("dubai"))
                .andExpect(jsonPath("$.totalResults").value(1))
                .andExpect(jsonPath("$.results[0].type").value("CITY"))
                .andExpect(jsonPath("$.results[0].name").value("Dubai"));

        verify(searchService).searchCities("dubai");
    }

    @Test
    @DisplayName("GET /api/v1/search/cities?q= - Search cities with empty query")
    void testSearchCities_EmptyQuery() throws Exception {
        mockMvc.perform(get("/api/v1/search/cities")
                        .param("q", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Search query is required"));

        verify(searchService, never()).searchCities(any());
    }

    @Test
    @DisplayName("GET /api/v1/search/cities?q=xyz - Search cities with no results")
    void testSearchCities_NoResults() throws Exception {
        SearchResponse emptyResponse = SearchResponse.builder()
                .query("xyz")
                .totalResults(0)
                .results(Collections.emptyList())
                .build();

        when(searchService.searchCities("xyz")).thenReturn(emptyResponse);

        mockMvc.perform(get("/api/v1/search/cities")
                        .param("q", "xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResults").value(0));

        verify(searchService).searchCities("xyz");
    }

    // ==================== GET CITY WITH WEATHER ====================

    @Test
    @DisplayName("GET /api/v1/search/city/Dubai - Get city with weather - Success")
    void testGetCityWithWeather_Success() throws Exception {
        when(searchService.getCityWithWeather("Dubai")).thenReturn(cityResult);

        mockMvc.perform(get("/api/v1/search/city/Dubai"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("CITY"))
                .andExpect(jsonPath("$.name").value("Dubai"))
                .andExpect(jsonPath("$.weather.temperature").value(28.5))
                .andExpect(jsonPath("$.activities[0].name").value("Beach"));

        verify(searchService).getCityWithWeather("Dubai");
    }

    @Test
    @DisplayName("GET /api/v1/search/city/Unknown - Get city with weather - Not Found")
    void testGetCityWithWeather_NotFound() throws Exception {
        when(searchService.getCityWithWeather("Unknown"))
                .thenThrow(new RuntimeException("City not found: Unknown"));

        mockMvc.perform(get("/api/v1/search/city/Unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("City not found: Unknown"));

        verify(searchService).getCityWithWeather("Unknown");
    }

    @Test
    @DisplayName("GET /api/v1/search/city/Lahore - Get city with weather and activities")
    void testGetCityWithWeather_WithActivities() throws Exception {
        SearchResponse.ActivityInfo activity1 = SearchResponse.ActivityInfo.builder()
                .id("act1")
                .name("Historical Tour")
                .pricePerHour(new BigDecimal("30.00"))
                .build();

        SearchResponse.ActivityInfo activity2 = SearchResponse.ActivityInfo.builder()
                .id("act2")
                .name("Food Tour")
                .pricePerHour(new BigDecimal("25.00"))
                .build();

        SearchResponse.SearchResult lahoreResult = SearchResponse.SearchResult.builder()
                .type("CITY")
                .id("city456")
                .name("Lahore")
                .countryName("Pakistan")
                .latitude(31.5204)
                .longitude(74.3587)
                .activities(Arrays.asList(activity1, activity2))
                .build();

        when(searchService.getCityWithWeather("Lahore")).thenReturn(lahoreResult);

        mockMvc.perform(get("/api/v1/search/city/Lahore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lahore"))
                .andExpect(jsonPath("$.activities").isArray())
                .andExpect(jsonPath("$.activities.length()").value(2));

        verify(searchService).getCityWithWeather("Lahore");
    }
}
