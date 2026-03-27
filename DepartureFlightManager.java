import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DepartureFlightManager {

    private FlightManagement flightManagement;
    private PassengerManagement passengerManagement;
    private String departedFlightFilePath = "departedFlights.txt";

    public DepartureFlightManager(FlightManagement fm, PassengerManagement pm) {
        this.flightManagement = fm;
        this.passengerManagement = pm;
    }

    // ========================
    // Prepare flight for boarding
    // ========================
    public void prepareBoarding(String flightInstanceId, LocalDateTime currentTime) {
        Flight f = flightManagement.FindFlightByInstanceId(flightInstanceId);
        if (f == null) {
            System.out.println("Flight " + flightInstanceId + " not found.");
            return;
        }

        if (!"Dhaka".equalsIgnoreCase(f.getOrigin())) {
            System.out.println("Flight " + flightInstanceId + " is not a departure flight.");
            return;
        }

        if (f.getStatus().equalsIgnoreCase("BOARDING")) {
            System.out.println("Flight already in BOARDING status.");
            return;
        }

        WeatherManager weatherManager = flightManagement.getWeatherManager();
        WeatherType weather = weatherManager.getWeather(currentTime);

        if (weather.isBadWeather()) {
            LocalDateTime stormEndTime = weatherManager.getWeatherEndTime(currentTime);
            flightManagement.handleBadWeather(currentTime, stormEndTime);
            return;
        }

        flightManagement.processGoodWeatherPrepareBoarding(flightInstanceId, currentTime);
    }

    // ========================
    // Check and depart flights based on current time
    // ========================
    public void checkAndDepartFlights(LocalDateTime currentTime) {
        List<Flight> departedFlights = new ArrayList<>();
        for (Flight f : flightManagement.getFlights()) {
            if (f.getStatus().equalsIgnoreCase("BOARDING")) {
                if (currentTime.isAfter(f.getDepartDateTime().plusMinutes(20))) {
                    f.setStatus("DEPARTED");
                    departedFlights.add(f);
                    System.out.println("Flight " + f.getFlightInstanceId() + " has DEPARTED.");

                    if (f.getGateId() != null && !f.getGateId().equals("-")) {
                        flightManagement.freeGate(f);
                    }
                    if (f.getRunwayId() != null && !f.getRunwayId().equals("-")) {
                        flightManagement.freeRunway(f);
                    }

                    if (passengerManagement != null) {
                        passengerManagement.freePassengersOfFlight(f.getFlightInstanceId());
                    }
                    System.out.println("-------------------------------------");
                }
            }
        }
        saveDepartedFlights(departedFlights);
    }

    // ========================
    // Save cancelled flight
    // ========================
    public void saveCancelledFlight(Flight f) {
        f.setStatus("CANCELLED");

        if (passengerManagement != null) {
            passengerManagement.freePassengersOfFlight(f.getFlightInstanceId());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(departedFlightFilePath, true))) {
            bw.write(f.toFileString());
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error saving cancelled flight: " + e.getMessage());
        }
    }

    // ========================
    // Save departed flights
    // ========================
    private void saveDepartedFlights(List<Flight> departedFlights) {
        if (departedFlights.isEmpty())
            return;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(departedFlightFilePath, true))) {
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
    // Display departed flights
    // ========================
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
            if (empty)
                System.out.println("No departed flights found.");
        } catch (IOException e) {
            System.out.println("Error reading departed flights: " + e.getMessage());
        }
    }

    // ========================
    // Clear departed flights file
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