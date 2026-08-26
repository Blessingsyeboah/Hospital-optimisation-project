package com.hospitaloptimizer.shared.model;

/** A job the hospital network must schedule/dispatch/route/optimize. */
public class ServiceRequest implements Comparable<ServiceRequest> {
    private String requestId;
    private String source;
    private String destination;
    private String category;
    private int urgency;          // 1 (low) .. 5 (critical)
    private String timeSubmitted; // "yyyy-MM-dd HH:mm"
    private String deadline;      // "yyyy-MM-dd HH:mm"
    private String status;        // PENDING, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED

    public ServiceRequest() {}

    public ServiceRequest(String requestId, String source, String destination, String category,
                           int urgency, String timeSubmitted, String deadline, String status) {
        this.requestId = requestId;
        this.source = source;
        this.destination = destination;
        this.category = category;
        this.urgency = urgency;
        this.timeSubmitted = timeSubmitted;
        this.deadline = deadline;
        this.status = status;
    }

    /** Natural ordering: higher urgency first (used by priority queue / heap). */
    @Override
    public int compareTo(ServiceRequest other) {
        return Integer.compare(other.urgency, this.urgency);
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getUrgency() { return urgency; }
    public void setUrgency(int urgency) { this.urgency = urgency; }
    public String getTimeSubmitted() { return timeSubmitted; }
    public void setTimeSubmitted(String timeSubmitted) { this.timeSubmitted = timeSubmitted; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return requestId + " [" + category + ", urgency=" + urgency + ", " + source + "->" + destination + ", " + status + "]";
    }
}
