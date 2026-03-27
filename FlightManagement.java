import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FlightManagement {

    private List<Flight> flights;
    private String flightFilePath = "flights.txt";
    private GateManagement gateManagement;
    private RunwayManagement runwayManagement;

    public FlightManagement(GateManagement gm, RunwayManagement rm) {
        this.flights = new ArrayList<>();
        this.gateManagement = gm;
        this.runwayManagement = rm;
        loadFlightsFromFile();
    }

    // ========================
    // Load flights from file
    // ========================
    private void loadFlightsFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(flightFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String flightNumber = parts[0];
                String flightInstanceId = parts[1];
                int seatCapacity = Integer.parseInt(parts[2]);
                int passengerCount = Integer.parseInt(parts[3]);
                String origin = parts[4];
                String destination = parts[5];
                LocalDateTime departDateTime = LocalDateTime.parse(parts[6]);
                LocalDateTime arrivalDateTime = LocalDateTime.parse(parts[7]);
                String status = parts[8];
                String gateId = parts[9];
                String runwayId = parts[10];

                Flight f = new Flight(flightNumber, flightInstanceId, seatCapacity, passengerCount,
                        origin, destination, departDateTime, arrivalDateTime, status, gateId, runwayId);
                flights.add(f);
            }
        } catch (IOException e) {
            System.out.println("Error loading flights: " + e.getMessage());
        }
    }

    // ========================
    // Save flights to file
    // ========================
    public void saveFlightsToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(flightFilePath))) {
            for (Flight f : flights) {
                bw.write(f.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving flights: " + e.getMessage());
        }
    }

    // ========================
    // Find flight by instance ID
    // ========================
    public Flight FindFlightByInstanceId(String flightInstanceId) {
        for (Flight f : flights) {
            if (f.getFlightInstanceId().equals(flightInstanceId)) return f;
        }
        return null;
    }

    // ========================
    // Assign gate/runway
    // ========================
    public boolean assignGate(Flight f) {
        if (f != null) {
            String freeGate = gateManagement.assignGate(f.getFlightInstanceId());
            if (freeGate != null) {
                f.setGateId(freeGate);
                saveFlightsToFile();
                return true;
            }
        }
        return false;
    }

    public boolean assignRunway(Flight f) {
        if (f != null) {
            String freeRunway = runwayManagement.assignRunway(f.getFlightInstanceId());
            if (freeRunway != null) {
                f.setRunwayId(freeRunway);
                saveFlightsToFile();
                return true;
            }
        }
        return false;
    }

    // ========================
// Free gate assigned to a flight
// ========================
    public void freeGate(Flight f) {
        if (f != null && f.getGateId() != null && !f.getGateId().equals("-")) {
            boolean gateFreed = gateManagement.freeGate(f.getFlightInstanceId());
            if (gateFreed) {
                f.setGateId("-"); // reset the flight's gate
                System.out.println("Gate freed for flight: " + f.getFlightInstanceId());
                saveFlightsToFile(); // save updated flight info
            }
        }
    }

    // ========================
// Free runway assigned to a flight
// ========================
    public void freeRunway(Flight f) {
        if (f != null && f.getRunwayId() != null && !f.getRunwayId().equals("-")) {
            boolean runwayFreed = runwayManagement.freeRunway(f.getFlightInstanceId());
            if (runwayFreed) {
                f.setRunwayId("-"); // reset the flight's runway
                System.out.println("Runway freed for flight: " + f.getFlightInstanceId());
                saveFlightsToFile(); // save updated flight info
            }
        }
    }

    // ========================
    // Update flight status
    // ========================
    public void updateFlightStatus(Flight f, String newStatus) {
        if (f != null) {
            f.setStatus(newStatus);
            saveFlightsToFile();
        }
    }

    // ========================
    // Add/Remove passengers (increment/decrement count)(later be used)
    // ========================
//    public void addPassengerToFlight(String flightInstanceId) {
//        Flight f = FindFlightByInstanceId(flightInstanceId);
//        if (f != null) {
//            f.incrementPassengerCount();
//            saveFlightsToFile();
//        }
//    }
//
//    public void removePassengerFromFlight(String flightInstanceId) {
//        Flight f = FindFlightByInstanceId(flightInstanceId);
//        if (f != null) {
//            f.decrementPassengerCount();
//            saveFlightsToFile();
//        }
//    }

    // ========================
    // Display flights
    // ========================
    public void displayFlights() {
        for (Flight f : flights) {
            displayFlightDetails(f);
        }
    }
    // Display a single flight in a human-readable format
    public void displayFlightDetails(Flight f) {
        if (f == null) {
            System.out.println("Flight not found.");
            return;
        }

        System.out.println("Flight Number: " + f.getFlightNumber());
        System.out.println("Flight Instance ID: " + f.getFlightInstanceId());
        System.out.println("Origin: " + f.getOrigin());
        System.out.println("Destination: " + f.getDestination());
        System.out.println("Departure: " + f.getDepartDateTime());
        System.out.println("Arrival: " + f.getArrivalDateTime());
        System.out.println("Status: " + f.getStatus());
        System.out.println("Gate: " + f.getGateId());
        System.out.println("Runway: " + f.getRunwayId());
        System.out.println("Passenger Count: " + f.getPassengerCount());
        System.out.println("----------------------------------------------------");
    }

    // Display all flights with a specific status
    public void displayFlightsByStatus(String status) {
        boolean found = false;
        for (Flight f : flights) {
            if (f.getStatus().equalsIgnoreCase(status)) {
                displayFlightDetails(f); // use the readable display method
                System.out.println("------------------------");
                found = true;
            }
        }
        if (!found) {
            System.out.println("No flights found with status: " + status);
        }
    }

    // ========================
    // Getter for all active flights
    // ========================
    public List<Flight> getFlights() {
        return flights;
    }


///////////////////////////Display Flight by ID///////////////////////////////////
    public void displayFlightByID(String flightInstanceID) {
        boolean found = false;
        for (Flight f : flights) {
            if (f.getFlightInstanceId().equalsIgnoreCase(flightInstanceID)) {
                displayFlightDetails(f);
                found = true;
                break; // stop after finding the flight
            }
        }
        if (!found) {
            System.out.println("Flight with ID " + flightInstanceID + " not found.");
        }
    }

}
