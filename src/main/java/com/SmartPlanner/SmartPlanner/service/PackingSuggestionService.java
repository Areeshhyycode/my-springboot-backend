package com.SmartPlanner.SmartPlanner.service;

import com.SmartPlanner.SmartPlanner.dto.OpenMeteoResponse;
import com.SmartPlanner.SmartPlanner.dto.PackingSuggestionsResponse;
import com.SmartPlanner.SmartPlanner.model.City;
import com.SmartPlanner.SmartPlanner.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PackingSuggestionService {

    private final CityRepository cityRepository;
    private final RestTemplate restTemplate;

    public PackingSuggestionsResponse getPackingSuggestionsByCityId(String cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new RuntimeException("City not found"));
        return getPackingSuggestionsByCoordinates(city.getLatitude(), city.getLongitude(),
                city.getName(), city.getCountryName());
    }

    public PackingSuggestionsResponse getPackingSuggestionsByCoordinates(double lat, double lon,
                                                                         String cityName, String country) {
        String url = String.format(
                "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f" +
                        "&daily=temperature_2m_max,temperature_2m_min,weather_code,precipitation_sum,wind_speed_10m_max,relative_humidity_2m_max" +
                        "&timezone=auto",
                lat, lon
        );

        OpenMeteoResponse weather = restTemplate.getForObject(url, OpenMeteoResponse.class);

        if (weather == null || weather.getDaily() == null) {
            throw new RuntimeException("Failed to fetch weather forecast");
        }

        return generatePackingSuggestions(weather, cityName, country);
    }

    private PackingSuggestionsResponse generatePackingSuggestions(OpenMeteoResponse weather,
                                                                  String cityName, String country) {
        List<PackingSuggestionsResponse.PackingCategory> categories = new ArrayList<>();
        List<String> generalTips = new ArrayList<>();

        List<Double> maxTemps = weather.getDaily().getTemperature2mMax();
        List<Double> minTemps = weather.getDaily().getTemperature2mMin();
        List<Integer> weatherCodes = weather.getDaily().getWeatherCode();
        List<Double> precipitations = weather.getDaily().getPrecipitationSum();
        List<Double> windSpeeds = weather.getDaily().getWindSpeed10mMax();
        List<Integer> humidities = weather.getDaily().getRelativeHumidity2mMax();

        double avgTemp = calculateAverage(maxTemps);
        double maxTemp = maxTemps.stream().max(Double::compare).orElse(0.0);
        double minTemp = minTemps.stream().min(Double::compare).orElse(0.0);
        double maxWind = windSpeeds.stream().max(Double::compare).orElse(0.0);
        int avgHumidity = (int) humidities.stream().mapToInt(Integer::intValue).average().orElse(65);

        String dominantWeather = determineDominantWeather(weatherCodes);

        categories.add(generateClothingCategory(avgTemp, maxTemp, minTemp));
        categories.add(generateFootwearCategory(weatherCodes, precipitations));
        categories.add(generateAccessoriesCategory(weatherCodes, maxTemp, minTemp, maxWind));
        categories.add(generateToiletriesCategory(avgTemp, avgHumidity, maxWind));
        categories.add(generateElectronicsCategory());
        categories.add(generateDocumentsCategory());

        generalTips = generateGeneralTips(avgTemp, dominantWeather, maxWind, avgHumidity);

        PackingSuggestionsResponse.WeatherSummary summary = PackingSuggestionsResponse.WeatherSummary.builder()
                .avgTemperature(Math.round(avgTemp * 10) / 10.0)
                .maxTemperature(Math.round(maxTemp * 10) / 10.0)
                .minTemperature(Math.round(minTemp * 10) / 10.0)
                .avgHumidity(avgHumidity)
                .maxWindSpeed(Math.round(maxWind * 10) / 10.0)
                .dominantWeather(dominantWeather)
                .period("Next 7 days")
                .build();

        return PackingSuggestionsResponse.builder()
                .cityName(cityName)
                .country(country)
                .weatherSummary(summary)
                .categories(categories)
                .generalTips(generalTips)
                .build();
    }

    private PackingSuggestionsResponse.PackingCategory generateClothingCategory(double avgTemp,
                                                                                double maxTemp, double minTemp) {
        List<PackingSuggestionsResponse.PackingItem> items = new ArrayList<>();

        if (avgTemp >= 30) {
            items.add(PackingSuggestionsResponse.PackingItem.builder()
                    .name("Light cotton T-shirts")
                    .reason("Breathable for hot weather")
                    .priority("essential")
                    .icon("👕")
                    .build());
            items.add(PackingSuggestionsResponse.PackingItem.builder()
                    .name("Shorts/Skirts")
                    .reason("Comfortable in high temperatures")
                    .priority("essential")
                    .icon("🩳")
                    .build());
        } else if (avgTemp >= 20) {
            items.add(PackingSuggestionsResponse.PackingItem.builder()
                    .name("T-shirts/Polo shirts")
                    .reason("Versatile for daytime")
                    .priority("essential")
                    .icon("👚")
                    .build());
            items.add(PackingSuggestionsResponse.PackingItem.builder()
                    .name("Jeans/Chinos")
                    .reason("Comfortable for day and night")
                    .priority("essential")
                    .icon("👖")
                    .build());
        } else if (avgTemp >= 10) {
            items.add(PackingSuggestionsResponse.PackingItem.builder()
                    .name("Thermal base layers")
                    .reason("Warmth without bulk")
                    .priority("essential")
                    .icon("🩲")
                    .build());
            items.add(PackingSuggestionsResponse.PackingItem.builder()
                    .name("Wool sweaters/Fleece")
                    .reason("Insulation for cold days")
                    .priority("essential")
                    .icon("🧶")
                    .build());
        } else {
            items.add(PackingSuggestionsResponse.PackingItem.builder()
                    .name("Heavy thermal underwear")
                    .reason("Essential base layer")
                    .priority("essential")
                    .icon("🩲")
                    .build());
            items.add(PackingSuggestionsResponse.PackingItem.builder()
                    .name("Down jacket/Parka")
                    .reason("Maximum insulation")
                    .priority("essential")
                    .icon("🧥")
                    .build());
        }

        items.add(PackingSuggestionsResponse.PackingItem.builder()
                .name("Underwear (5-7 pairs)")
                .reason("Daily change")
                .priority("essential")
                .icon("🩲")
                .build());
        items.add(PackingSuggestionsResponse.PackingItem.builder()
                .name("Socks (5-7 pairs)")
                .reason("Daily change and comfort")
                .priority("essential")
                .icon("🧦")
                .build());

        return PackingSuggestionsResponse.PackingCategory.builder()
                .category("👕 Clothing")
                .icon("👕")
                .items(items)
                .build();
    }

    private PackingSuggestionsResponse.PackingCategory generateFootwearCategory(List<Integer> weatherCodes,
                                                                                List<Double> precipitations) {
        List<PackingSuggestionsResponse.PackingItem> items = new ArrayList<>();

        boolean hasRain = weatherCodes.stream().anyMatch(code -> code >= 51 && code <= 82);
        boolean hasSnow = weatherCodes.stream().anyMatch(code -> code >= 71 && code <= 86);

        items.add(PackingSuggestionsResponse.PackingItem.builder()
                .name("Comfortable walking shoes")
                .reason("Essential for sightseeing")
                .priority("essential")
                .icon("👟")
                .build());

        if (hasRain) {
            items.add(PackingSuggestionsResponse.PackingItem.builder()
                    .name("Waterproof shoes/Boots")
                    .reason("Keep feet dry in wet weather")
                    .priority("recommended")
                    .icon("🥾")
                    .build());
        }

        if (hasSnow) {
            items.add(PackingSuggestionsResponse.PackingItem.builder()
                    .name("Insulated winter boots")
                    .reason("Warmth and traction in snow")
                    .priority("essential")
                    .icon("👢")
                    .build());
        }

        return PackingSuggestionsResponse.PackingCategory.builder()
                .category("👟 Footwear")
                .icon("👟")
                .items(items)
                .build();
    }

    private PackingSuggestionsResponse.PackingCategory generateAccessoriesCategory(List<Integer> weatherCodes,
                                                                                   double maxTemp, double minTemp,
                                                                                   double maxWind) {
        List<PackingSuggestionsResponse.PackingItem> items = new ArrayList<>();

        if (maxTemp > 25) {
            items.add(PackingSuggestionsResponse.PackingItem.builder()
                    .name("Sunglasses with UV protection")
                    .reason("Eye protection from sun glare")
                    .priority("recommended")
                    .icon("🕶️")
                    .build());
        }

        boolean hasRain = weatherCodes.stream().anyMatch(code -> code >= 51 && code <= 82);
        if (hasRain) {
            items.add(PackingSuggestionsResponse.PackingItem.builder()
                    .name("Compact umbrella")
                    .reason("Quick rain protection")
                    .priority("essential")
                    .icon("☂️")
                    .build());
        }

        if (minTemp < 10) {
            items.add(PackingSuggestionsResponse.PackingItem.builder()
                    .name("Winter gloves")
                    .reason("Essential hand warmth")
                    .priority("recommended")
                    .icon("🧤")
                    .build());
        }

        return PackingSuggestionsResponse.PackingCategory.builder()
                .category("🧤 Accessories")
                .icon("🧤")
                .items(items)
                .build();
    }

    private PackingSuggestionsResponse.PackingCategory generateToiletriesCategory(double avgTemp,
                                                                                  int avgHumidity, double maxWind) {
        List<PackingSuggestionsResponse.PackingItem> items = new ArrayList<>();

        items.add(PackingSuggestionsResponse.PackingItem.builder()
                .name("Toothbrush & Toothpaste")
                .reason("Daily oral hygiene")
                .priority("essential")
                .icon("🪥")
                .build());

        if (avgTemp > 25) {
            items.add(PackingSuggestionsResponse.PackingItem.builder()
                    .name("Sunscreen SPF 30+")
                    .reason("Protect skin from UV rays")
                    .priority("essential")
                    .icon("🧴")
                    .build());
        }

        return PackingSuggestionsResponse.PackingCategory.builder()
                .category("🧴 Toiletries & Health")
                .icon("🧴")
                .items(items)
                .build();
    }

    private PackingSuggestionsResponse.PackingCategory generateElectronicsCategory() {
        List<PackingSuggestionsResponse.PackingItem> items = new ArrayList<>();

        items.add(PackingSuggestionsResponse.PackingItem.builder()
                .name("Smartphone & Charger")
                .reason("Navigation, communication, photos")
                .priority("essential")
                .icon("📱")
                .build());

        return PackingSuggestionsResponse.PackingCategory.builder()
                .category("📱 Electronics")
                .icon("📱")
                .items(items)
                .build();
    }

    private PackingSuggestionsResponse.PackingCategory generateDocumentsCategory() {
        List<PackingSuggestionsResponse.PackingItem> items = new ArrayList<>();

        items.add(PackingSuggestionsResponse.PackingItem.builder()
                .name("Passport/Government ID")
                .reason("Identification and travel")
                .priority("essential")
                .icon("📄")
                .build());

        return PackingSuggestionsResponse.PackingCategory.builder()
                .category("📄 Documents & Money")
                .icon("📄")
                .items(items)
                .build();
    }

    private List<String> generateGeneralTips(double avgTemp, String dominantWeather,
                                             double maxWind, int avgHumidity) {
        List<String> tips = new ArrayList<>();

        tips.add("Roll clothes instead of folding to save space");
        tips.add("Keep important documents in carry-on luggage");

        if (avgTemp > 30) {
            tips.add("Stay hydrated - drink at least 2-3 liters of water daily");
        }

        if (avgTemp < 15) {
            tips.add("Layer clothing for better warmth");
        }

        return tips;
    }

    private double calculateAverage(List<Double> values) {
        return values.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private String determineDominantWeather(List<Integer> weatherCodes) {
        int clear = 0, cloudy = 0, rain = 0, snow = 0, storm = 0;

        for (Integer code : weatherCodes) {
            if (code == 0) clear++;
            else if (code <= 3) clear++;
            else if (code <= 48) cloudy++;
            else if (code <= 67) rain++;
            else if (code <= 77) snow++;
            else if (code <= 82) rain++;
            else if (code <= 86) snow++;
            else if (code >= 95) storm++;
        }

        int total = weatherCodes.size();
        if (storm > 0) return "Thunderstorms";
        if (snow > total/2) return "Snowy";
        if (rain > total/2) return "Rainy";
        if (cloudy > total/2) return "Cloudy";
        if (clear > total/2) return "Sunny";
        return "Mixed conditions";
    }
}
