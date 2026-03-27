import java.time.LocalDateTime;

public class Passenger {
    private String ticketId;
    private String passengerName;

    private String flightInstanceId;
    private String origin;
    private String destination;
    private LocalDateTime journeyDateTime;    // Updated if flight delayed
    private LocalDateTime checkInStartTime;
    private boolean boardingPassIssued;
    private String gateId;                     // Assigned at boarding pass

    // Normal constructor
    public Passenger(String ticketId, String passengerName, String flightInstanceId,
                     String origin, String destination,
                     LocalDateTime journeyDateTime, LocalDateTime checkInStartTime,
                     boolean boardingPassIssued, String gateId) {
        this.ticketId = ticketId;
        this.passengerName = passengerName;
        this.flightInstanceId = flightInstanceId;
        this.origin = origin;
        this.destination = destination;
        this.journeyDateTime = journeyDateTime;
        this.checkInStartTime = checkInStartTime;
        this.boardingPassIssued = boardingPassIssued;
        this.gateId = gateId;
    }


    // Command-style method to issue boarding pass
    public void issueBoardingPass(String gateId) {
        this.boardingPassIssued = true;
        this.gateId = gateId;
    }

    // Setters for updates
    public void setJourneyDateTime(LocalDateTime newJourneyTime) {
        this.journeyDateTime = newJourneyTime;
    }

    // Getters
    public String getTicketId() {
        return ticketId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public String getFlightInstanceId() {
        return flightInstanceId;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDateTime getJourneyDateTime() {
        return journeyDateTime;
    }

    public LocalDateTime getCheckInStartTime() {
        return checkInStartTime;
    }

    public boolean isBoardingPassIssued() {
        return boardingPassIssued;
    }

    public String getGateId() {
        return gateId;
    }

    // Convert to file line for persistence
    public String toFileString() {
        return ticketId + "," +
                passengerName + "," +
                flightInstanceId + "," +
                origin + "," +
                destination + "," +
                journeyDateTime + "," +
                checkInStartTime + "," +
                boardingPassIssued + "," +
                gateId;
    }

//    @Override
//    public String toString() {
//        return "Passenger{" +
//                "ticketId='" + ticketId + '\'' +
//                ", name='" + passengerName + '\'' +
//                ", flightId='" + flightId + '\'' +
//                ", origin='" + origin + '\'' +
//                ", destination='" + destination + '\'' +
//                ", journey=" + journeyDateTime +
//                ", checkInStart=" + checkInStartTime +
//                ", boardingPassIssued=" + boardingPassIssued +
//                ", gateId='" + gateId + '\'' +
//                ", departure=" + departureTime +
//                '}';
//    }
}
