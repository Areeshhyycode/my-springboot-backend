package com.SmartPlanner.SmartPlanner.controller;

import com.SmartPlanner.SmartPlanner.dto.SearchResponse;
import com.SmartPlanner.SmartPlanner.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * SEARCH CONTROLLER - Search countries, cities with weather
 *
 * PUBLIC APIs:
 *   - GET /api/v1/search?q=dubai     - Search countries & cities
 *   - GET /api/v1/search/cities?q=dubai - Search cities only
 *   - GET /api/v1/search/city/{name} - Get city with fresh weather
 */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SearchController {

    private final SearchService searchService;

    /**
     * SEARCH ALL - Countries & Cities
     *
     * URL: GET /api/v1/search?q=dubai
     *
     * Response:
     * {
     *   "query": "dubai",
     *   "totalResults": 2,
     *   "results": [
     *     {
     *       "type": "CITY",
     *       "name": "Dubai",
     *       "weather": { "temperature": 28.5, "description": "Clear sky" },
     *       "activities": [...]
     *     }
     *   ]
     * }
     */
    @GetMapping
    public ResponseEntity<?> search(@RequestParam("q") String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Search query is required"));
        }

        SearchResponse response = searchService.search(query.trim());
        return ResponseEntity.ok(response);
    }

    /**
     * SEARCH CITIES ONLY
     *
     * URL: GET /api/v1/search/cities?q=lahore
     */
    @GetMapping("/cities")
    public ResponseEntity<?> searchCities(@RequestParam("q") String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Search query is required"));
        }

        SearchResponse response = searchService.searchCities(query.trim());
        return ResponseEntity.ok(response);
    }

    /**
     * GET CITY WITH FRESH WEATHER
     *
     * URL: GET /api/v1/search/city/Dubai
     *
     * Fetches fresh weather from API
     */
    @GetMapping("/city/{name}")
    public ResponseEntity<?> getCityWithWeather(@PathVariable String name) {
        try {
            SearchResponse.SearchResult result = searchService.getCityWithWeather(name);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    record ErrorResponse(String message) {}
}
