package com.planetposition.dto;

public class PlanetPositionRequest {
    private String datetime;
    private String latitude;
    private String longitude;
    private String timezone;
    private int ayanamsa;
    private String language;

    public String getDatetime() { return datetime; }
    public void setDatetime(String datetime) { this.datetime = datetime; }

    public String getLatitude() { return latitude; }
    public void setLatitude(String latitude) { this.latitude = latitude; }

    public String getLongitude() { return longitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public int getAyanamsa() { return ayanamsa; }
    public void setAyanamsa(int ayanamsa) { this.ayanamsa = ayanamsa; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}