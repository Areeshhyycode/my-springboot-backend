package com.SmartPlanner.SmartPlanner.service;

import com.SmartPlanner.SmartPlanner.dto.Coordinates;
import com.SmartPlanner.SmartPlanner.dto.DirectionsResponse;
import com.SmartPlanner.SmartPlanner.dto.DistanceResponse;
import com.SmartPlanner.SmartPlanner.dto.GeocodingResponse;
import com.SmartPlanner.SmartPlanner.dto.NearbyPlacesResponse;
import com.SmartPlanner.SmartPlanner.model.Trip;
import com.SmartPlanner.SmartPlanner.repository.TripRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final TripRepository tripRepository;

    // Free APIs URLs
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org";
    private static final String OSRM_URL = "http://router.project-osrm.org";
    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";

    // Rate limiting for Nominatim (1 request per second)
    private long lastNominatimRequest = 0;

    // User-Agent required by Nominatim
    private HttpHeaders createNominatimHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "SmartPlanner/1.0");
        headers.set("Accept-Language", "en");
        return headers;
    }

    // Rate limiting method
    private synchronized void rateLimitNominatim() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastNominatimRequest;

        if (timeSinceLastRequest < 1000) { // 1 second
            try {
                Thread.sleep(1000 - timeSinceLastRequest);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastNominatimRequest = System.currentTimeMillis();
    }

    // Geocoding: Address to coordinates
    @Cacheable(value = "geocoding", key = "#address")
    public GeocodingResponse geocodeAddress(String address) {
        rateLimitNominatim();

        try {
            String url = NOMINATIM_URL + "/search?q=" + address + "&format=json&limit=1";
            HttpEntity<String> entity = new HttpEntity<>(createNominatimHeaders());

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.isArray() && root.size() > 0) {
                JsonNode firstResult = root.get(0);

                GeocodingResponse result = new GeocodingResponse();
                result.setLatitude(firstResult.get("lat").asDouble());
                result.setLongitude(firstResult.get("lon").asDouble());
                result.setDisplayName(firstResult.get("display_name").asText());
                result.setType(firstResult.get("type").asText());
                result.setOsmId(firstResult.get("osm_id").asText());

                return result;
            }
        } catch (Exception e) {
            log.error("Geocoding error for address: {}", address, e);
        }
        return null;
    }

    // Reverse Geocoding: Coordinates to address
    @Cacheable(value = "reverseGeocoding", key = "#lat + '-' + #lon")
    public GeocodingResponse reverseGeocode(double lat, double lon) {
        rateLimitNominatim();

        try {
            String url = String.format("%s/reverse?lat=%f&lon=%f&format=json&zoom=18",
                    NOMINATIM_URL, lat, lon);
            HttpEntity<String> entity = new HttpEntity<>(createNominatimHeaders());

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());

            GeocodingResponse result = new GeocodingResponse();
            result.setLatitude(lat);
            result.setLongitude(lon);
            result.setDisplayName(root.get("display_name").asText());

            if (root.has("address")) {
                result.setAddress(objectMapper.writeValueAsString(root.get("address")));
            }

            return result;
        } catch (Exception e) {
            log.error("Reverse geocoding error for coordinates: {}, {}", lat, lon, e);
        }
        return null;
    }

    // Get directions between two points using OSRM
    @Cacheable(value = "directions", key = "#originLat + '-' + #originLon + '-' + #destLat + '-' + #destLon + '-' + #mode")
    public DirectionsResponse getDirections(double originLat, double originLon,
                                            double destLat, double destLon, String mode) {
        try {
            String profile = mode.equals("walking") ? "foot" : "driving";
            String url = String.format("%s/route/v1/%s/%f,%f;%f,%f?overview=full&steps=true&geometries=geojson",
                    OSRM_URL, profile, originLon, originLat, destLon, destLat);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            if (root.get("code").asText().equals("Ok")) {
                JsonNode route = root.get("routes").get(0);
                JsonNode legs = route.get("legs");

                DirectionsResponse result = new DirectionsResponse();
                result.setOrigin(new Coordinates(originLat, originLon));
                result.setDestination(new Coordinates(destLat, destLon));
                result.setMode(mode);
                result.setDistance(String.format("%.1f km", route.get("distance").asDouble() / 1000));
                result.setDuration(formatDuration(route.get("duration").asInt()));

                // Extract polyline (using geometry)
                if (route.has("geometry") && route.get("geometry").has("coordinates")) {
                    result.setPolyline(route.get("geometry").toString());
                }

                // Extract steps
                List<DirectionsResponse.RouteStep> steps = new ArrayList<>();
                for (JsonNode leg : legs) {
                    for (JsonNode step : leg.get("steps")) {
                        DirectionsResponse.RouteStep routeStep = new DirectionsResponse.RouteStep();
                        routeStep.setInstruction(step.get("name").asText());
                        routeStep.setDistance(String.format("%.1f m", step.get("distance").asDouble()));
                        routeStep.setDuration(formatDuration(step.get("duration").asInt()));

                        JsonNode start = step.get("maneuver").get("location");
                        JsonNode end = step.get("geometry").get("coordinates").get(
                                step.get("geometry").get("coordinates").size() - 1);

                        routeStep.setStartLocation(new Coordinates(
                                start.get(1).asDouble(), start.get(0).asDouble()));
                        routeStep.setEndLocation(new Coordinates(
                                end.get(1).asDouble(), end.get(0).asDouble()));

                        steps.add(routeStep);
                    }
                }
                result.setSteps(steps);

                return result;
            }
        } catch (Exception e) {
            log.error("Directions error", e);
        }
        return null;
    }

    // Get multi-stop route
    public DirectionsResponse getMultiStopRoute(List<double[]> coordinates, String mode) {
        if (coordinates == null || coordinates.size() < 2) {
            throw new IllegalArgumentException("At least 2 coordinates are required");
        }

        try {
            StringBuilder coordinatesStr = new StringBuilder();
            for (double[] coord : coordinates) {
                coordinatesStr.append(String.format("%f,%f;", coord[1], coord[0])); // lon,lat
            }
            coordinatesStr.deleteCharAt(coordinatesStr.length() - 1); // Remove last semicolon

            String profile = mode.equals("walking") ? "foot" : "driving";
            String url = String.format("%s/route/v1/%s/%s?overview=full&steps=true&geometries=geojson",
                    OSRM_URL, profile, coordinatesStr);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            if (root.get("code").asText().equals("Ok")) {
                JsonNode route = root.get("routes").get(0);
                JsonNode legs = route.get("legs");

                DirectionsResponse result = new DirectionsResponse();
                double[] origin = coordinates.get(0);
                double[] destination = coordinates.get(coordinates.size() - 1);
                result.setOrigin(new Coordinates(origin[0], origin[1]));
                result.setDestination(new Coordinates(destination[0], destination[1]));
                result.setMode(mode);
                result.setDistance(String.format("%.1f km", route.get("distance").asDouble() / 1000));
                result.setDuration(formatDuration(route.get("duration").asInt()));

                // Extract polyline
                if (route.has("geometry") && route.get("geometry").has("coordinates")) {
                    result.setPolyline(route.get("geometry").toString());
                }

                // Extract steps
                List<DirectionsResponse.RouteStep> steps = new ArrayList<>();
                for (JsonNode leg : legs) {
                    for (JsonNode step : leg.get("steps")) {
                        DirectionsResponse.RouteStep routeStep = new DirectionsResponse.RouteStep();
                        routeStep.setInstruction(step.get("name").asText());
                        routeStep.setDistance(String.format("%.1f m", step.get("distance").asDouble()));
                        routeStep.setDuration(formatDuration(step.get("duration").asInt()));
                        steps.add(routeStep);
                    }
                }
                result.setSteps(steps);

                return result;
            }
        } catch (Exception e) {
            log.error("Multi-stop route error", e);
        }
        return null;
    }

    // Get nearby places using Overpass API
    @Cacheable(value = "nearbyPlaces", key = "#lat + '-' + #lon + '-' + #radius + '-' + #type")
    public NearbyPlacesResponse getNearbyPlaces(double lat, double lon, int radiusMeters, String type) {
        try {
            String overpassQuery = buildOverpassQuery(lat, lon, radiusMeters, type);
            String url = OVERPASS_URL + "?data=" + overpassQuery;

            ResponseEntity<String> response = restTemplate.getForEntity(
                    URI.create(url), String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            List<NearbyPlacesResponse.Place> places = new ArrayList<>();

            if (root.has("elements")) {
                for (JsonNode element : root.get("elements")) {
                    if (element.has("tags")) {
                        JsonNode tags = element.get("tags");
                        String name = tags.has("name") ? tags.get("name").asText() : "Unnamed";

                        NearbyPlacesResponse.Place place = new NearbyPlacesResponse.Place();
                        place.setId(String.valueOf(element.get("id").asLong()));
                        place.setName(name);
                        place.setType(type);
                        place.setLatitude(element.get("lat").asDouble());
                        place.setLongitude(element.get("lon").asDouble());

                        if (tags.has("addr:street") || tags.has("addr:city")) {
                            StringBuilder address = new StringBuilder();
                            if (tags.has("addr:street")) address.append(tags.get("addr:street").asText());
                            if (tags.has("addr:city")) address.append(", ").append(tags.get("addr:city").asText());
                            place.setAddress(address.toString());
                        }

                        // Calculate distance
                        DistanceResponse distanceResp = calculateDistance(lat, lon,
                                place.getLatitude(), place.getLongitude());
                        place.setDistance(String.format("%.1f km", distanceResp.getDistanceKm()));

                        places.add(place);
                    }
                }
            }

            NearbyPlacesResponse result = new NearbyPlacesResponse();
            result.setCenter(new Coordinates(lat, lon));
            result.setRadius(radiusMeters);
            result.setType(type);
            result.setPlaces(places);

            return result;
        } catch (Exception e) {
            log.error("Nearby places error", e);
        }
        return null;
    }

    // Build Overpass API query
    private String buildOverpassQuery(double lat, double lon, int radius, String type) {
        String query;

        switch (type.toLowerCase()) {
            case "restaurant":
                query = String.format("[out:json];node[\"amenity\"=\"restaurant\"](around:%d,%f,%f);out;",
                        radius, lat, lon);
                break;
            case "hotel":
                query = String.format("[out:json];node[\"tourism\"=\"hotel\"](around:%d,%f,%f);out;",
                        radius, lat, lon);
                break;
            case "atm":
                query = String.format("[out:json];node[\"amenity\"=\"atm\"](around:%d,%f,%f);out;",
                        radius, lat, lon);
                break;
            case "hospital":
                query = String.format("[out:json];node[\"amenity\"=\"hospital\"](around:%d,%f,%f);out;",
                        radius, lat, lon);
                break;
            case "tourism":
                query = String.format("[out:json];node[\"tourism\"](around:%d,%f,%f);out;",
                        radius, lat, lon);
                break;
            default:
                query = String.format("[out:json];node(around:%d,%f,%f);out;", radius, lat, lon);
        }

        return query;
    }

    // Calculate distance between two points using Haversine formula
    public DistanceResponse calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceKm = R * c;
        double distanceMiles = distanceKm * 0.621371;

        // Estimate driving duration (assuming average speed 40 km/h)
        int minutes = (int) ((distanceKm / 40) * 60);

        DistanceResponse response = new DistanceResponse();
        response.setPoint1(new Coordinates(lat1, lon1));
        response.setPoint2(new Coordinates(lat2, lon2));
        response.setDistanceKm(Math.round(distanceKm * 100.0) / 100.0);
        response.setDistanceMiles(Math.round(distanceMiles * 100.0) / 100.0);
        response.setEstimatedDuration(minutes + " mins");

        return response;
    }

    // Helper method to format duration
    private String formatDuration(int seconds) {
        if (seconds < 60) {
            return seconds + " sec";
        } else if (seconds < 3600) {
            return (seconds / 60) + " mins";
        } else {
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            return hours + " hrs " + minutes + " mins";
        }
    }

    // Get route for a trip based on its city location
    public DirectionsResponse getTripRoute(String tripId) {
        try {
            Trip trip = tripRepository.findById(tripId).orElse(null);
            if (trip == null) {
                log.warn("Trip not found: {}", tripId);
                return null;
            }

            // Geocode the city to get coordinates
            GeocodingResponse cityLocation = geocodeAddress(trip.getCityName() + ", " + trip.getCountry());
            if (cityLocation == null) {
                log.warn("Could not geocode city for trip: {}", tripId);
                return null;
            }

            DirectionsResponse response = new DirectionsResponse();
            response.setOrigin(new Coordinates(cityLocation.getLatitude(), cityLocation.getLongitude()));
            response.setDestination(new Coordinates(cityLocation.getLatitude(), cityLocation.getLongitude()));
            response.setMode("driving");
            response.setDistance("0 km");
            response.setDuration("0 mins");
            response.setSteps(new ArrayList<>());

            return response;
        } catch (Exception e) {
            log.error("Error getting trip route for tripId: {}", tripId, e);
            return null;
        }
    }
}