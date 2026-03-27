import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PassengerManagement {

    private List<Passenger> passengers;
    private FlightManagement flightManagement;
    private String passengerFilePath = "passengers.txt";
    private String passengerRemovedFilePath = "passengerRemoved.txt";

    public PassengerManagement(FlightManagement fm) {
        this.passengers = new ArrayList<>();
        this.flightManagement = fm;
        loadPassengersFromFile();
    }

    // ====================
    // Load passengers from file
    // ====================
    private void loadPassengersFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(passengerFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // ticketId,name,flightInstanceId,origin,destination,journeyDateTime,checkInStartTime,boardingPassIssued,gateId
                String[] parts = line.split(",");
                String ticketId = parts[0];
                String name = parts[1];
                String flightInstanceId = parts[2];
                String origin = parts[3];
                String destination = parts[4];
                LocalDateTime journeyDateTime = LocalDateTime.parse(parts[5]);
                LocalDateTime checkInStartTime = LocalDateTime.parse(parts[6]);
                boolean boardingPassIssued = Boolean.parseBoolean(parts[7]);
                String gateId = parts[8];

                Passenger p = new Passenger(ticketId, name, flightInstanceId, origin, destination,
                        journeyDateTime, checkInStartTime, boardingPassIssued, gateId);
                passengers.add(p);
            }
        } catch (IOException e) {
            System.out.println("Error loading passengers: " + e.getMessage());
        }
    }

    // ====================
    // Save passengers to file
    // ====================
    private void savePassengersToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(passengerFilePath))) {
            for (Passenger p : passengers) {
                bw.write(p.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving passengers: " + e.getMessage());
        }
    }

    // ====================
    // Save removed passenger to removed file
    // ====================
    private void saveRemovedPassengers(List<Passenger> removedPassengers) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("passengerRemoved.txt", true))) { // append mode
            for (Passenger p : removedPassengers) {
                bw.write(p.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving removed passengers: " + e.getMessage());
        }
    }

    // ====================
// Clear passengerRemoved.txt completely
// ====================
    public void clearRemovedPassengersFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("passengerRemoved.txt"))) {
            bw.write(""); // overwrite with empty content
            System.out.println("Removed passengers list cleared.");
        } catch (IOException e) {
            System.out.println("Error clearing removed passengers file: " + e.getMessage());
        }
    }

    // ====================
    // Find passenger by ticket
    // ====================
    private Passenger findPassengerByTicket(String ticketId) {
        for (Passenger p : passengers) {
            if (p.getTicketId().equals(ticketId)) return p;
        }
        return null;
    }

    // ====================
    // Check-in with nested logic
    // ====================
    public void checkIn(String ticketId, LocalDateTime currentTime) {
        Passenger p = findPassengerByTicket(ticketId);

        if (p == null) {
            System.out.println("Invalid ticket.");
            return;
        }

        if (p.isBoardingPassIssued()) {
            System.out.println("Boarding pass already issued.");
            return;
        }

        Flight flight = flightManagement.FindFlightByInstanceId(p.getFlightInstanceId());
        if (flight == null) {
            System.out.println("Flight not found.");
            return;
        }

        LocalDateTime flightTime = flight.getDepartDateTime();

        // ====== NESTED LOGIC ======
        if (p.getJourneyDateTime().isBefore(flightTime)) {
            long delayMinutes = java.time.Duration.between(p.getJourneyDateTime(), flightTime).toMinutes();
            p.setJourneyDateTime(flightTime); // update passenger time
            System.out.println("Sorry! Flight delayed by " + delayMinutes + " minutes. Updated time: " + flightTime);
        }

        if (currentTime.plusMinutes(15).isAfter(p.getJourneyDateTime())) {
            System.out.println("Sorry! Boarding is over.");
        } else if (currentTime.isBefore(p.getCheckInStartTime())) {
            System.out.println("Check-in not started yet. Please wait.");
        } else {
            if (flight.getStatus().equalsIgnoreCase("BOARDING")) {
                p.issueBoardingPass(flight.getGateId());
                System.out.println("Boarding pass issued for passenger " + p.getPassengerName() +
                        " | Gate: " + p.getGateId() +
                        " | Departure: " + p.getJourneyDateTime());
                savePassengersToFile();
            } else {
                System.out.println("Flight not boarding yet. Please wait.");
            }
        }
    }

    // ====================
// Free all passengers of a flight
// ====================
    public void freePassengersOfFlight(String flightInstanceId) {
        List<Passenger> toRemove = new ArrayList<>();
        for (Passenger p : passengers) {
            if (p.getFlightInstanceId().equals(flightInstanceId)) {
                toRemove.add(p);
            }
        }

        if (!toRemove.isEmpty()) {
            saveRemovedPassengers(toRemove);   // write all at once
            passengers.removeAll(toRemove);    // remove from active list
            savePassengersToFile();            // update main passenger file
            System.out.println("Freed " + toRemove.size() + " passengers for flight " + flightInstanceId + ".");
        }
    }

    // ====================
    // Display all passengers
    // ====================
    public void displayPassengers() {
        System.out.println("ticketId" + "|" +
                "passengerName" + "|" +
                "flightInstanceId" + "|" +
                "origin" + "|" +
                "destination" + "|" +
                "journeyDateTime" + "|" +
                "checkInStartTime" + "|" +
                "boardingPassIssued" + "|" +
                "gateId");
        for (Passenger p : passengers) {
            System.out.println(p.toFileString());
        }
    }
}
