package com.planetposition.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.planetposition.dto.PlanetPositionRequest;
import org.json.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PlanetPositionService {

    @Value("${planetPosition.client-id}")
    private String clientId;

    @Value("${planetPosition.client-secret}")
    private String clientSecret;

    @Value("${planetPosition.auth-url}")
    private String authUrl;

    @Value("${planetPosition.planet-position-url}")
    private String planetPositionApiUrl;

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public String getAccessToken() throws Exception {
        String form = "grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        return json.getString("access_token");
    }

    public String getPlanetPositions(PlanetPositionRequest input) throws Exception {
    String accessToken = getAccessToken();

    String coordinates = input.getLatitude() + "," + input.getLongitude();

    String query = String.format(
        "%s?datetime=%s&coordinates=%s&timezone=%s&ayanamsa=%d&language=%s",
        planetPositionApiUrl,
        URLEncoder.encode(input.getDatetime(), StandardCharsets.UTF_8),
        URLEncoder.encode(coordinates, StandardCharsets.UTF_8),
        URLEncoder.encode(input.getTimezone(), StandardCharsets.UTF_8),
        input.getAyanamsa(),
        URLEncoder.encode(input.getLanguage(), StandardCharsets.UTF_8)
    );

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(query))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() == 200) {
        return response.body();
    } else {
        throw new RuntimeException("API call failed: " + response.body());
    }
}
}