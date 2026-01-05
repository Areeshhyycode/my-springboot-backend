package com.SmartPlanner.SmartPlanner.controller;

import com.SmartPlanner.SmartPlanner.dto.SearchResponse;
import com.SmartPlanner.SmartPlanner.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<?> search(@RequestParam("q") String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Search query is required"));
        }

        SearchResponse response = searchService.search(query.trim());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cities")
    public ResponseEntity<?> searchCities(@RequestParam("q") String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Search query is required"));
        }

        SearchResponse response = searchService.searchCities(query.trim());
        return ResponseEntity.ok(response);
    }

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
