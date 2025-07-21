package com.planetposition.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.planetposition.dto.PlanetPositionRequest;
import com.planetposition.service.PlanetPositionService;

@RestController
@RequestMapping("/api/astrology")
public class PlanetPositionController {

    @Autowired
    private PlanetPositionService planetPositionService;

    @PostMapping("/planet-position")
    public String getPlanetPosition(@RequestBody PlanetPositionRequest request) {
        try {
            return planetPositionService.getPlanetPositions(request);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}