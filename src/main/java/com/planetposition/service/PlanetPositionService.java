package com.planetposition.service;

import com.planetposition.dto.PlanetPositionRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class PlanetPositionService {

    @Value("${prokerala.api.client-id}")
    private String clientId;

    @Value("${prokerala.api.client-secret}")
    private String clientSecret;

    @Value("${prokerala.api.token-url}")
    private String tokenUrl;

    @Value("${prokerala.api.base-url}")
    private String baseUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private String getAccessToken() throws Exception {
        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpRequest tokenRequest = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Basic " + encodedCredentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .build();

        HttpResponse<String> response = httpClient.send(tokenRequest, HttpResponse.BodyHandlers.ofString());

        JSONObject json = new JSONObject(response.body());
        return json.getString("access_token");
    }

    private String resolveCoordinatesFromPlace(String place, String token) throws Exception {
        String encodedPlace = URLEncoder.encode(place, StandardCharsets.UTF_8);
        String url = "https://nominatim.openstreetmap.org/search?q=" + encodedPlace + "&format=json&limit=1";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "planetposition-app") // Required by Nominatim
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        JSONArray jsonArray = new JSONArray(response.body());
        if (jsonArray.isEmpty()) {
            throw new Exception("No coordinates found for the place: " + place);
        }

        JSONObject location = jsonArray.getJSONObject(0);
        double lat = location.getDouble("lat");
        double lon = location.getDouble("lon");

        return String.format("%.4f,%.4f", lat, lon);
    }

    public Map<String, Object> fetchPlanetPositions(PlanetPositionRequest request) throws Exception {
        Map<String, Object> result = new HashMap<>();

        if (request == null ||
            request.getPlace() == null || request.getPlace().trim().isEmpty() ||
            request.getDateTime() == null || request.getDateTime().trim().isEmpty() ||
            request.getLanguage() == null || request.getLanguage().trim().isEmpty()) {

            result.put("error", "Missing required input fields: place, dateTime, or language.");
            return result;
        }

        String token = getAccessToken();

        String coordinates = resolveCoordinatesFromPlace(request.getPlace(), token);
        if (coordinates == null) {
            result.put("error", "Could not resolve coordinates.");
            return result;
        }

        String endpoint = baseUrl + "/planet-position";
        String encodedCoordinates = URLEncoder.encode(coordinates, StandardCharsets.UTF_8);

        String url = String.format("%s?datetime=%s&coordinates=%s&ayanamsa=%d&language=%s",
                endpoint,
                URLEncoder.encode(request.getDateTime(), StandardCharsets.UTF_8),
                encodedCoordinates,
                request.getAyanamsa(),
                URLEncoder.encode(request.getLanguage(), StandardCharsets.UTF_8));

        HttpRequest apiRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(apiRequest, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();

        JSONObject json = new JSONObject(responseBody);

        if (!json.has("data")) {
            result.put("error", "API response does not contain 'data'.");
            result.put("api_response", json.toMap());
            return result;
        }

        JSONObject data = json.getJSONObject("data");
        JSONArray planetPositions = data.getJSONArray("planet_position");
        result.put("planet_positions", planetPositions.toList());      
        
        
        endpoint = baseUrl + "/upagraha-position";
        encodedCoordinates = URLEncoder.encode(coordinates, StandardCharsets.UTF_8);

        url = String.format("%s?datetime=%s&coordinates=%s&ayanamsa=%d&language=%s",
                endpoint,
                URLEncoder.encode(request.getDateTime(), StandardCharsets.UTF_8),
                encodedCoordinates,
                request.getAyanamsa(),
                URLEncoder.encode(request.getLanguage(), StandardCharsets.UTF_8));

        apiRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        response = httpClient.send(apiRequest, HttpResponse.BodyHandlers.ofString());
        responseBody = response.body();

        json = new JSONObject(responseBody);

        if (!json.has("data")) {
            result.put("error", "API response does not contain 'data'.");
            result.put("api_response", json.toMap());
            return result;
        }

        data = json.getJSONObject("data");
        JSONArray upagrahas = data.optJSONArray("upagraha_position");
        result.put("upagraha_positions", upagrahas.toList());      
        
        if (upagrahas != null) {
            for (int i = 0; i < upagrahas.length(); i++) {
            	JSONObject upa = upagrahas.getJSONObject(i);
                if ("Mandi".equalsIgnoreCase(upa.optString("name"))) {
                    result.put("mandi_positions", upa.toMap());
                    break;
                }
            }
        }

        return result;
    }
}
