import java.io.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


public class ArrivalFlightManager {

    private FlightManagement flightManagement;
    private String arrivedFlightFilePath = "arrivedFlights.txt";

    private DepartureFlightManager departureManager; // new
    private List<Flight> arrivedFlights; // store flights that have completed arrival

    public ArrivalFlightManager(FlightManagement fm) {
        this.flightManagement = fm;
        this.arrivedFlights = new ArrayList<>();
    }

    public void setDepartureManager(DepartureFlightManager dm) {
        this.departureManager = dm;
    }


    // ========================
    // Start arrival process (ARRIVE-SCHEDULED → ARRIVING)
    // ========================
    public void processArrival(String flightInstanceId, LocalDateTime currentTime) {
        Flight f = flightManagement.FindFlightByInstanceId(flightInstanceId);
        if (f == null) {
            System.out.println("Flight " + flightInstanceId + " not found.");
            return;
        }

        if (f.getStatus().equalsIgnoreCase("ARRIVING") || f.getStatus().equalsIgnoreCase("ARRIVED")) {
            System.out.println("Flight already in " + f.getStatus() + " status.");
            return;
        }

        if (f.getStatus().equalsIgnoreCase("ARRIVE-SCHEDULED")) {
            // Assign gate
            if (!flightManagement.assignGate(f)) {
                checkAndCompleteArrivals(currentTime);
                if (!flightManagement.assignGate(f)) {
                    departureManager.checkAndDepartFlights(currentTime);
                    if (!flightManagement.assignGate(f)) {
                        System.out.println("Failed to assign gate. Flight delayed.");
                        flightManagement.updateFlightStatus(f, "DELAYED");
                        return;
                    }
                }
            }

            // Assign runway
            if (!flightManagement.assignRunway(f)) {
                System.out.println("Failed to assign runway for flight " + flightInstanceId);
                return;
            }

            // Set status to ARRIVING
            flightManagement.updateFlightStatus(f, "ARRIVING");
            System.out.println("Flight " + flightInstanceId + " is now ARRIVING at gate " +
                    f.getGateId() + " using runway " + f.getRunwayId() + ".");
        } else {
            System.out.println("Flight " + flightInstanceId + " is not an arrival flight, skipping...");
        }
    }

    // ========================
    // Scan ARRIVING flights and complete arrival based on currentTime
    // ========================
    public void checkAndCompleteArrivals(LocalDateTime currentTime) {
        for (Flight f : flightManagement.getFlights()) {
            if (f.getStatus().equalsIgnoreCase("ARRIVING")) {
                if (!currentTime.isBefore(f.getArrivalDateTime().plusMinutes(20))) {
                    flightManagement.updateFlightStatus(f, "ARRIVED");
                    arrivedFlights.add(f); // add directly to the arrived list
                    System.out.println("Flight " + f.getFlightInstanceId() + " has ARRIVED at gate " +
                            f.getGateId() + " using runway " + f.getRunwayId() + ".");


                    // Free gate and runway
                    if (f.getGateId() != null && !f.getGateId().equals("-")) {
                        flightManagement.freeGate(f); // Actually we need freeGate
                    }
                    if (f.getRunwayId() != null && !f.getRunwayId().equals("-")) {
                        flightManagement.freeRunway(f); // Actually we need freeRunway
                    }
                    System.out.println("-------------------------------------");
                }
            }

        }
        saveAllArrivedFlights(arrivedFlights);
        // Remove all arrived flights from active flight list
//        if (!arrivedFlights.isEmpty()) {
//            flightManagement.getFlights().removeAll(arrivedFlights);
//            flightManagement.saveFlightsToFile();
//        }
    }

    // Display all departed flights from departedFlights.txt
    public void displayArrivedFlights() {
        try (BufferedReader br = new BufferedReader(new FileReader(arrivedFlightFilePath))) {
            String line;
            boolean empty = true;
            while ((line = br.readLine()) != null) {
                empty = false;
                String[] parts = line.split(",");
                System.out.println("\nFlight Number: " + parts[0]);
                System.out.println("Flight Instance ID: " + parts[1]);
                System.out.println("Origin: " + parts[4]);
                System.out.println("Destination: " + parts[5]);
                System.out.println("Departure: " + parts[6]);
                System.out.println("Arrival: " + parts[7]);
                System.out.println("Status: " + parts[8]);
                System.out.println("Gate: " + parts[9]);
                System.out.println("Runway: " + parts[10]);
                System.out.println("--------------------------------------------");
            }
            if (empty) System.out.println("No arrived flights found.");
        } catch (IOException e) {
            System.out.println("Error reading arrived flights: " + e.getMessage());
        }
    }

    // ========================
    // Save all arrived flights to arrivedFlights.txt
    // ========================
    public void saveAllArrivedFlights(List<Flight> arrivedFlights) {
        if (arrivedFlights.isEmpty()) return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(arrivedFlightFilePath, true))) {
            for (Flight f : arrivedFlights) {
                bw.write(f.toFileString());
                bw.newLine();
            }
            System.out.println("All arrived flights saved to " + arrivedFlightFilePath);
        } catch (IOException e) {
            System.out.println("Error saving arrived flights: " + e.getMessage());
        }

        // Clear arrivedFlights list
        arrivedFlights.clear();
    }

    // ========================
    // Clear arrivedFlights.txt
    // ========================
    public void clearArrivedFlightsFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(arrivedFlightFilePath))) {
            bw.write("");
            System.out.println("Arrived flights file cleared.");
        } catch (IOException e) {
            System.out.println("Error clearing arrived flights file: " + e.getMessage());
        }
    }


    // ========================
    // Getter for arrivedFlights list (optional)
    // ========================
    public List<Flight> getArrivedFlights() {
        return new ArrayList<>(arrivedFlights);
    }
}
