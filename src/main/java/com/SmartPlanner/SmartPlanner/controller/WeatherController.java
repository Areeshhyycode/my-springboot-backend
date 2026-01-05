package com.SmartPlanner.SmartPlanner.controller;

import com.SmartPlanner.SmartPlanner.dto.PackingSuggestionsResponse;
import com.SmartPlanner.SmartPlanner.dto.WeatherAlertsResponse;
import com.SmartPlanner.SmartPlanner.dto.WeatherResponse;
import com.SmartPlanner.SmartPlanner.service.WeatherAlertService;
import com.SmartPlanner.SmartPlanner.service.WeatherService;
import com.SmartPlanner.SmartPlanner.service.PackingSuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WeatherController {

    private final WeatherService weatherService;
    private final WeatherAlertService weatherAlertService;
    private final PackingSuggestionService packingSuggestionService;

    @GetMapping("/city/{cityId}")
    public ResponseEntity<WeatherResponse> getWeatherByCityId(@PathVariable String cityId) {
        WeatherResponse response = weatherService.getWeatherByCityId(cityId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/city/name/{cityName}")
    public ResponseEntity<WeatherResponse> getWeatherByCityName(@PathVariable String cityName) {
        WeatherResponse response = weatherService.getWeatherByCityName(cityName);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/coordinates")
    public ResponseEntity<WeatherResponse> getWeatherByCoordinates(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(required = false) String cityName) {
        WeatherResponse response = weatherService.getWeatherByCoordinates(lat, lon,
                cityName != null ? cityName : "Unknown");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/alerts/city/{cityId}")
    public ResponseEntity<WeatherAlertsResponse> getWeatherAlertsByCityId(@PathVariable String cityId) {
        WeatherAlertsResponse response = weatherAlertService.getAlertsByCityId(cityId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/alerts/coordinates")
    public ResponseEntity<WeatherAlertsResponse> getWeatherAlertsByCoordinates(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam String cityName) {
        WeatherAlertsResponse response = weatherAlertService.getAlertsByCoordinates(lat, lon, cityName);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/packing/city/{cityId}")
    public ResponseEntity<PackingSuggestionsResponse> getPackingSuggestionsByCityId(@PathVariable String cityId) {
        PackingSuggestionsResponse response = packingSuggestionService.getPackingSuggestionsByCityId(cityId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/packing/coordinates")
    public ResponseEntity<PackingSuggestionsResponse> getPackingSuggestionsByCoordinates(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam String cityName,
            @RequestParam(required = false) String country) {
        PackingSuggestionsResponse response = packingSuggestionService
                .getPackingSuggestionsByCoordinates(lat, lon, cityName,
                        country != null ? country : "");
        return ResponseEntity.ok(response);
    }
}
