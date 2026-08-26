package com.hospitaloptimizer.shared.model;

/** An asset that can be assigned to a ServiceRequest: ambulance, officer, porter, van... */
public class Resource {
    private String resourceId;
    private String type;
    private String homeLocation;
    private int capacity;
    private String availabilityStatus; // AVAILABLE, BUSY, OFFLINE

    public Resource() {}

    public Resource(String resourceId, String type, String homeLocation, int capacity, String availabilityStatus) {
        this.resourceId = resourceId;
        this.type = type;
        this.homeLocation = homeLocation;
        this.capacity = capacity;
        this.availabilityStatus = availabilityStatus;
    }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getHomeLocation() { return homeLocation; }
    public void setHomeLocation(String homeLocation) { this.homeLocation = homeLocation; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }

    @Override
    public String toString() {
        return resourceId + " [" + type + " @ " + homeLocation + ", " + availabilityStatus + "]";
    }
}
