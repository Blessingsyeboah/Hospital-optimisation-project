package com.hospitaloptimizer.shared.model;

/** A weighted edge between two Locations (a real road/dispatch route). */
public class Road {
    private String roadId;
    private String fromLocationId;
    private String toLocationId;
    private double distance;          // km
    private double travelTime;        // minutes, base
    private double roadConditionWeight; // multiplier: 1.0 good ... 3.0 bad/traffic

    public Road() {}

    public Road(String roadId, String fromLocationId, String toLocationId,
                double distance, double travelTime, double roadConditionWeight) {
        this.roadId = roadId;
        this.fromLocationId = fromLocationId;
        this.toLocationId = toLocationId;
        this.distance = distance;
        this.travelTime = travelTime;
        this.roadConditionWeight = roadConditionWeight;
    }

    /** Effective weight used by shortest-path algorithms. */
    public double effectiveWeight() {
        return travelTime * roadConditionWeight;
    }

    public String getRoadId() { return roadId; }
    public void setRoadId(String roadId) { this.roadId = roadId; }
    public String getFromLocationId() { return fromLocationId; }
    public void setFromLocationId(String fromLocationId) { this.fromLocationId = fromLocationId; }
    public String getToLocationId() { return toLocationId; }
    public void setToLocationId(String toLocationId) { this.toLocationId = toLocationId; }
    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }
    public double getTravelTime() { return travelTime; }
    public void setTravelTime(double travelTime) { this.travelTime = travelTime; }
    public double getRoadConditionWeight() { return roadConditionWeight; }
    public void setRoadConditionWeight(double w) { this.roadConditionWeight = w; }

    @Override
    public String toString() {
        return fromLocationId + " -> " + toLocationId + " (" + distance + "km, w=" + effectiveWeight() + ")";
    }
}
