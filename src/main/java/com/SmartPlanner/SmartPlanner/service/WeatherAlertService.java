package com.SmartPlanner.SmartPlanner.service;

import com.SmartPlanner.SmartPlanner.dto.OpenMeteoResponse;
import com.SmartPlanner.SmartPlanner.dto.WeatherAlertsResponse;
import com.SmartPlanner.SmartPlanner.model.City;
import com.SmartPlanner.SmartPlanner.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherAlertService {

    private final CityRepository cityRepository;
    private final RestTemplate restTemplate;

    public WeatherAlertsResponse getAlertsByCityId(String cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new RuntimeException("City not found"));
        return getAlertsByCoordinates(city.getLatitude(), city.getLongitude(), city.getName());
    }

    public WeatherAlertsResponse getAlertsByCoordinates(double lat, double lon, String cityName) {
        String url = String.format(
                "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code&daily=temperature_2m_max,temperature_2m_min,weather_code,wind_speed_10m_max&timezone=auto",
                lat, lon
        );

        OpenMeteoResponse weather = restTemplate.getForObject(url, OpenMeteoResponse.class);

        if (weather == null || weather.getCurrent() == null) {
            throw new RuntimeException("Failed to fetch weather data from OpenMeteo API");
        }

        return generateAlerts(weather, cityName);
    }

    private WeatherAlertsResponse generateAlerts(OpenMeteoResponse weather, String cityName) {
        List<WeatherAlertsResponse.WeatherAlert> alerts = new ArrayList<>();
        double temp = weather.getCurrent().getTemperature2m();
        double wind = weather.getCurrent().getWindSpeed10m();
        int humidity = weather.getCurrent().getRelativeHumidity2m();
        int weatherCode = weather.getCurrent().getWeatherCode();

        if (temp >= 40) {
            alerts.add(WeatherAlertsResponse.WeatherAlert.builder()
                    .type("temperature")
                    .severity("extreme")
                    .title("Extreme Heat Warning")
                    .message("Dangerous heat conditions. Stay indoors and hydrated.")
                    .icon("🔥")
                    .build());
        } else if (temp >= 35) {
            alerts.add(WeatherAlertsResponse.WeatherAlert.builder()
                    .type("temperature")
                    .severity("high")
                    .title("Heat Advisory")
                    .message("Very hot conditions expected. Limit outdoor activities.")
                    .icon("☀️")
                    .build());
        } else if (temp <= 0) {
            alerts.add(WeatherAlertsResponse.WeatherAlert.builder()
                    .type("temperature")
                    .severity("high")
                    .title("Freezing Conditions")
                    .message("Sub-zero temperatures. Dress warmly in layers.")
                    .icon("❄️")
                    .build());
        } else if (temp <= 5) {
            alerts.add(WeatherAlertsResponse.WeatherAlert.builder()
                    .type("temperature")
                    .severity("medium")
                    .title("Cold Weather Alert")
                    .message("Cold temperatures expected. Bring warm clothing.")
                    .icon("🥶")
                    .build());
        }

        if (wind >= 50) {
            alerts.add(WeatherAlertsResponse.WeatherAlert.builder()
                    .type("wind")
                    .severity("extreme")
                    .title("Severe Wind Warning")
                    .message("Dangerous winds. Avoid outdoor activities.")
                    .icon("🌪️")
                    .build());
        } else if (wind >= 35) {
            alerts.add(WeatherAlertsResponse.WeatherAlert.builder()
                    .type("wind")
                    .severity("high")
                    .title("Strong Wind Advisory")
                    .message("Strong winds expected. Secure loose items.")
                    .icon("💨")
                    .build());
        }

        if (weatherCode >= 95) {
            alerts.add(WeatherAlertsResponse.WeatherAlert.builder()
                    .type("storm")
                    .severity("extreme")
                    .title("Thunderstorm Warning")
                    .message("Severe thunderstorms. Seek shelter immediately.")
                    .icon("⛈️")
                    .build());
        } else if (weatherCode >= 61 && weatherCode <= 67) {
            alerts.add(WeatherAlertsResponse.WeatherAlert.builder()
                    .type("rain")
                    .severity("medium")
                    .title("Rain Expected")
                    .message("Bring an umbrella and waterproof clothing.")
                    .icon("🌧️")
                    .build());
        } else if (weatherCode >= 71 && weatherCode <= 77) {
            alerts.add(WeatherAlertsResponse.WeatherAlert.builder()
                    .type("snow")
                    .severity("high")
                    .title("Snow Alert")
                    .message("Snowfall expected. Plan for slippery conditions.")
                    .icon("🌨️")
                    .build());
        }

        if (humidity >= 85) {
            alerts.add(WeatherAlertsResponse.WeatherAlert.builder()
                    .type("humidity")
                    .severity("medium")
                    .title("High Humidity")
                    .message("Very humid conditions. Stay hydrated.")
                    .icon("💧")
                    .build());
        }

        WeatherAlertsResponse.WeatherStatus status = determineStatus(alerts);

        return WeatherAlertsResponse.builder()
                .cityName(cityName)
                .country("")
                .currentConditions(WeatherAlertsResponse.CurrentConditions.builder()
                        .temperature(temp)
                        .windSpeed(wind)
                        .humidity(humidity)
                        .weatherDescription(getWeatherDescription(weatherCode))
                        .time(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .build())
                .alerts(alerts)
                .status(status)
                .build();
    }

    private WeatherAlertsResponse.WeatherStatus determineStatus(List<WeatherAlertsResponse.WeatherAlert> alerts) {
        if (alerts.isEmpty()) {
            return WeatherAlertsResponse.WeatherStatus.builder()
                    .level("safe")
                    .message("Weather conditions are favorable for travel")
                    .color("green")
                    .build();
        }

        boolean hasExtreme = alerts.stream().anyMatch(a -> "extreme".equals(a.getSeverity()));
        boolean hasHigh = alerts.stream().anyMatch(a -> "high".equals(a.getSeverity()));

        if (hasExtreme) {
            return WeatherAlertsResponse.WeatherStatus.builder()
                    .level("danger")
                    .message("Dangerous weather conditions. Exercise extreme caution.")
                    .color("red")
                    .build();
        } else if (hasHigh) {
            return WeatherAlertsResponse.WeatherStatus.builder()
                    .level("warning")
                    .message("Adverse weather expected. Plan accordingly.")
                    .color("orange")
                    .build();
        } else {
            return WeatherAlertsResponse.WeatherStatus.builder()
                    .level("caution")
                    .message("Some weather conditions to be aware of.")
                    .color("yellow")
                    .build();
        }
    }

    private String getWeatherDescription(int code) {
        if (code == 0) return "Clear sky";
        if (code <= 3) return "Partly cloudy";
        if (code <= 49) return "Foggy";
        if (code <= 59) return "Drizzle";
        if (code <= 69) return "Rain";
        if (code <= 79) return "Snow";
        if (code <= 84) return "Rain showers";
        if (code <= 86) return "Snow showers";
        if (code >= 95) return "Thunderstorm";
        return "Unknown";
    }
}
