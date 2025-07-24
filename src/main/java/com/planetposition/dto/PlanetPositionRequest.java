package com.planetposition.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class PlanetPositionRequest {

    @NotNull(message = "dateTime cannot be null.")
    @NotEmpty(message = "dateTime cannot be empty.")
    private String dateTime; // Format: yyyy-MM-ddTHH:mm:ss+05:30

    @NotNull(message = "Place cannot be null.")
    @NotEmpty(message = "Place cannot be empty.")
    private String place;

    private int ayanamsa = 1;

    @NotNull(message = "Language cannot be null.")
    @NotEmpty(message = "Language cannot be empty.")
    private String language = "en";

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        if (dateTime == null || dateTime.trim().isEmpty()) {
            throw new IllegalArgumentException("dateTime cannot be null or empty.");
        }

        // Append +05:30 if no timezone provided
        if (!dateTime.contains("Z") && !dateTime.matches(".*[+-]\\d{2}:\\d{2}")) {
            dateTime += "+05:30";
        }

        try {
            OffsetDateTime.parse(dateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            this.dateTime = dateTime;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid dateTime format. Must be ISO 8601 with time zone. Example: 2024-07-21T06:00:00+05:30");
        }
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        if (place == null || place.trim().isEmpty()) {
            throw new IllegalArgumentException("Place cannot be null or empty.");
        }
        this.place = place;
    }

    public int getAyanamsa() {
        return ayanamsa;
    }

    public void setAyanamsa(int ayanamsa) {
        this.ayanamsa = ayanamsa;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        if (language == null || language.trim().isEmpty()) {
            this.language = "en";
        } else {
            this.language = language;
        }
    }
}
