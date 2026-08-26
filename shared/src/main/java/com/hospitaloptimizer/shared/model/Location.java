package com.hospitaloptimizer.shared.model;

/**
 * Represents a node in the hospital service network: a hospital, clinic,
 * polyclinic or health post somewhere in Ghana.
 */
public class Location {
    private String locationId;
    private String name;
    private String area;
    private String type;
    private double latitude;
    private double longitude;

    public Location() {}

    public Location(String locationId, String name, String area, String type, double latitude, double longitude) {
        this.locationId = locationId;
        this.name = name;
        this.area = area;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    @Override
    public String toString() {
        return locationId + " [" + name + ", " + area + ", " + type + "]";
    }
}
