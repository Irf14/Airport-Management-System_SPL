import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DepartureFlightManager {

    private FlightManagement flightManagement;
    private PassengerManagement passengerManagement;
    private String departedFlightFilePath = "departedFlights.txt";
    private ArrivalFlightManager arrivalManager; // new

    public DepartureFlightManager(FlightManagement fm, PassengerManagement pm) {
        this.flightManagement = fm;
        this.passengerManagement = pm;
    }

    public void setArrivalManager(ArrivalFlightManager am) {
        this.arrivalManager = am;
    }


    // ========================
    // Prepare flight for boarding
    // ========================
    public void prepareBoarding(String flightInstanceId, LocalDateTime currentTime) {
        Flight f = flightManagement.FindFlightByInstanceId(flightInstanceId);
        if (f == null) {
            System.out.println("Flight " + flightInstanceId + " not found.");
            return;
        } else if (f.getStatus().equalsIgnoreCase("BOARDING")) {
            System.out.println("Flight already in BOARDING status.");
            return;
        } else if (f.getStatus().equalsIgnoreCase("DEPART-SCHEDULED")) {

            if (!flightManagement.assignGate(f)) {
                checkAndDepartFlights(currentTime);
                if (!flightManagement.assignGate(f)) {
                    arrivalManager.checkAndCompleteArrivals(currentTime);
                    if (!flightManagement.assignGate(f)) {
                        System.out.println("Failed to assign gate. Flight delayed.");
                        flightManagement.updateFlightStatus(f, "DELAYED");
                        return;
                    }
                }
            }
            if (!flightManagement.assignRunway(f)) {
                System.out.println("Failed to assign runway. Flight delayed.");
                flightManagement.updateFlightStatus(f, "DELAYED");
                return;
            }

            String weather = "SUNNY";
            if (weather.equals("SUNNY")) {
                flightManagement.updateFlightStatus(f, "BOARDING");
                System.out.println("Flight " + f.getFlightInstanceId() + " is now BOARDING at gate "
                        + f.getGateId() + " using runway " + f.getRunwayId() + ".");
            } else {
                System.out.println("Cannot board flight due to bad weather.");
                flightManagement.updateFlightStatus(f, "DELAYED");
            }
        } else {
            System.out.println("Flight " + f.getFlightInstanceId() +
                    " is not a departure flight, skipping...");
        }
    }

    // ========================
    // Check and depart flights based on current time
    // ========================
    public void checkAndDepartFlights(LocalDateTime currentTime) {
        List<Flight> departedFlights = new ArrayList<>();
        for (Flight f : flightManagement.getFlights()) {
            if (f.getStatus().equalsIgnoreCase("BOARDING")) {
                if (currentTime.isAfter(f.getDepartDateTime().plusMinutes(20))) {
                    // Flight has departed
                    f.setStatus("DEPARTED");
                    departedFlights.add(f);
                    System.out.println("Flight " + f.getFlightInstanceId() + " has DEPARTED.");

                    // Free gate and runway
                    if (f.getGateId() != null && !f.getGateId().equals("-")) {
                        flightManagement.freeGate(f); // Actually we need freeGate
                    }
                    if (f.getRunwayId() != null && !f.getRunwayId().equals("-")) {
                        flightManagement.freeRunway(f); // Actually we need freeRunway
                    }

                    // Free passengers
                    if (passengerManagement != null) {
                        passengerManagement.freePassengersOfFlight(f.getFlightInstanceId());
                    }
                    System.out.println("-------------------------------------");
                }
            }
        }

        // Save departed flights to file
        saveDepartedFlights(departedFlights);

        // Remove departed flights from active list
//        flightManagement.flights.removeAll(departedFlights);
//        flightManagement.saveFlightsToFile();
    }

    // Display all departed flights from departedFlights.txt
    public void displayDepartedFlights() {
        try (BufferedReader br = new BufferedReader(new FileReader(departedFlightFilePath))) {
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
                System.out.println("-------------------------------------");
            }
            if (empty) System.out.println("No departed flights found.");
        } catch (IOException e) {
            System.out.println("Error reading departed flights: " + e.getMessage());
        }
    }


    // ========================
    // Save all departed flights at once
    // ========================
    private void saveDepartedFlights(List<Flight> departedFlights) {
        if (departedFlights.isEmpty()) return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(departedFlightFilePath, true))) { // append
            for (Flight f : departedFlights) {
                bw.write(f.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving departed flights: " + e.getMessage());
        }
        departedFlights.clear();
    }

    // ========================
    // Clear departedFlights.txt completely
    // ========================
    public void clearDepartedFlightsFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(departedFlightFilePath))) {
            bw.write("");
            System.out.println("Departed flights file cleared.");
        } catch (IOException e) {
            System.out.println("Error clearing departed flights file: " + e.getMessage());
        }
    }
}
