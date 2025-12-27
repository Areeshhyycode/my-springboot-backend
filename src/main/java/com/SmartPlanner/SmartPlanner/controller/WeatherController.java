package com.SmartPlanner.SmartPlanner.controller;

import com.SmartPlanner.SmartPlanner.dto.WeatherResponse;
import com.SmartPlanner.SmartPlanner.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * WEATHER CONTROLLER - Weather APIs
 *
 * Base URL: /api/v1/weather
 */
@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WeatherController {

    private final WeatherService weatherService;

    /**
     * GET WEATHER BY CITY ID
     *
     * URL: GET /api/v1/weather/city/{cityId}
     *
     * Example: GET /api/v1/weather/city/507f1f77bcf86cd799439011
     *
     * Response: WeatherResponse with current + hourly weather
     */
    @GetMapping("/city/{cityId}")
    public ResponseEntity<?> getWeatherByCityId(@PathVariable String cityId) {
        try {
            WeatherResponse weather = weatherService.getWeatherByCityId(cityId);
            return ResponseEntity.ok(weather);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET WEATHER BY CITY NAME
     *
     * URL: GET /api/v1/weather/city?name=Karachi
     *
     * Example: GET /api/v1/weather/city?name=Karachi
     *
     * Response: WeatherResponse with current + hourly weather
     */
    @GetMapping("/city")
    public ResponseEntity<?> getWeatherByCityName(@RequestParam String name) {
        try {
            WeatherResponse weather = weatherService.getWeatherByCityName(name);
            return ResponseEntity.ok(weather);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET WEATHER BY COORDINATES (Direct - without saving city)
     *
     * URL: GET /api/v1/weather/coordinates?lat=24.86&lon=67.01&name=Karachi
     *
     * Example: GET /api/v1/weather/coordinates?lat=24.86&lon=67.01&name=MyLocation
     */
    @GetMapping("/coordinates")
    public ResponseEntity<?> getWeatherByCoordinates(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(defaultValue = "Unknown") String name) {
        try {
            WeatherResponse weather = weatherService.getWeatherByCoordinates(lat, lon, name);
            return ResponseEntity.ok(weather);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * TEST ENDPOINT
     *
     * URL: GET /api/v1/weather/test
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Weather API is working!");
    }

    // Error response record
    record ErrorResponse(String message) {}
}
