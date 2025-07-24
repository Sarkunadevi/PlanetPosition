package com.planetposition.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.planetposition.dto.PlanetPositionRequest;
import com.planetposition.service.PlanetPositionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/planet")
public class PlanetPositionController {

    @Autowired
    private PlanetPositionService planetPositionService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/position")
    public ResponseEntity<ObjectNode> getPlanetPositions(@Valid @RequestBody PlanetPositionRequest request) {
        ObjectNode response = objectMapper.createObjectNode();
        response.set("headers", objectMapper.createObjectNode());

        try {
            if (request == null ||
                request.getPlace() == null || request.getPlace().trim().isEmpty() ||
                request.getDateTime() == null || request.getDateTime().trim().isEmpty() ||
                request.getLanguage() == null || request.getLanguage().trim().isEmpty()) {

                response.put("statusCode", HttpStatus.BAD_REQUEST.name());
                response.put("statusCodeValue", HttpStatus.BAD_REQUEST.value());

                ObjectNode errorBody = objectMapper.createObjectNode();
                errorBody.put("error", "Missing required fields: place, dateTime, or language.");
                response.set("body", errorBody);

                return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> planetData = planetPositionService.fetchPlanetPositions(request);

            response.put("statusCode", HttpStatus.OK.name());
            response.put("statusCodeValue", HttpStatus.OK.value());
            response.putPOJO("body", planetData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.name());
            response.put("statusCodeValue", HttpStatus.INTERNAL_SERVER_ERROR.value());

            ObjectNode errorBody = objectMapper.createObjectNode();
            errorBody.put("error", "Unable to retrieve planet positions: " + e.getMessage());
            response.set("body", errorBody);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
