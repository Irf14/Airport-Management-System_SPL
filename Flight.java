import java.time.LocalDateTime;

public class Flight {
    private String flightNumber;       // e.g., B101(represents route e.g B101 means dhk to cgp)
    private String flightInstanceId;   // e.g., B101-2026-01-01-16:00(created from flightNumber + its Original(departure/Arrival) time)
    private int seatCapacity;
    private int passengerCount;
    private String origin;
    private String destination;
    private LocalDateTime departDateTime;
    private LocalDateTime arrivalDateTime;
    private String status;             // SCHEDULED, BOARDING, DELAYED, DEPARTED, ARRIVED
    private String gateId;
    private String runwayId;

    // Constructor for existing flight instance
    public Flight(String flightNumber, String flightInstanceId, int seatCapacity, int passengerCount,
                  String origin, String destination, LocalDateTime departDateTime,
                  LocalDateTime arrivalDateTime, String status, String gateId, String runwayId) {
        this.flightNumber = flightNumber;
        this.flightInstanceId = flightInstanceId;
        this.seatCapacity = seatCapacity;
        this.passengerCount = passengerCount;
        this.origin = origin;
        this.destination = destination;
        this.departDateTime = departDateTime;
        this.arrivalDateTime = arrivalDateTime;
        this.status = status;
        this.gateId = gateId;
        this.runwayId = runwayId;
        this.generateInstanceId(this.flightNumber,this.departDateTime);
    }

    // =======================
    // Generate instance ID for new flight
    // =======================
    public void generateInstanceId(String flightNumber, LocalDateTime departDateTime) {
        if (this.flightInstanceId != "-") {
            return;
        }
        this.flightInstanceId = flightNumber + "-" + departDateTime.toString().replace("T", "-");
    }

    // =======================
    // Getters
    // =======================
    public String getFlightNumber() {
        return flightNumber;
    }

    public String getFlightInstanceId() {
        return flightInstanceId;
    }

    public int getSeatCapacity() {
        return seatCapacity;
    }

    public int getPassengerCount() {
        return passengerCount;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDateTime getDepartDateTime() {
        return departDateTime;
    }

    public LocalDateTime getArrivalDateTime() {
        return arrivalDateTime;
    }

    public String getStatus() {
        return status;
    }

    public String getGateId() {
        return gateId;
    }

    public String getRunwayId() {
        return runwayId;
    }

    // =======================
    // Setters for mutable fields
    // =======================
    public void setStatus(String status) {
        this.status = status;
    }

    public void setGateId(String gateId) {
        this.gateId = gateId;
    }

    public void setRunwayId(String runwayId) {
        this.runwayId = runwayId;
    }

    public void incrementPassengerCount() {
        if (this.passengerCount < this.seatCapacity) passengerCount++;
    }

    public void decrementPassengerCount() {
        if (this.passengerCount > 0) passengerCount--;
    }

    // =======================
    // Convert to file line for persistence
    // =======================
    public String toFileString() {
        return flightNumber + "," + flightInstanceId + "," + seatCapacity + "," + passengerCount + "," +
                origin + "," + destination + "," + departDateTime + "," + arrivalDateTime + "," +
                status + "," + gateId + "," + runwayId;
    }
}
