import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ArrivalFlightManager {

    private FlightManagement flightManagement;
    private String arrivedFlightFilePath = "arrivedFlights.txt";
    private List<Flight> arrivedFlights;

    public ArrivalFlightManager(FlightManagement fm) {
        this.flightManagement = fm;
        this.arrivedFlights = new ArrayList<>();
    }

    // ========================
    // Process arrival
    // ========================
    public void processArrival(String flightInstanceId, LocalDateTime currentTime) {
        Flight f = flightManagement.FindFlightByInstanceId(flightInstanceId);
        if (f == null) {
            System.out.println("Flight " + flightInstanceId + " not found.");
            return;
        }

        if (!"Dhaka".equalsIgnoreCase(f.getDestination())) {
            System.out.println("Flight " + flightInstanceId + " is not an arrival flight.");
            return;
        }

        if (f.getStatus().equalsIgnoreCase("ARRIVING") || f.getStatus().equalsIgnoreCase("ARRIVED")) {
            System.out.println("Flight already in " + f.getStatus() + " status.");
            return;
        }

        WeatherManager weatherManager = flightManagement.getWeatherManager();
        WeatherType weather = weatherManager.getWeather(currentTime);

        if (weather.isBadWeather()) {
            LocalDateTime stormEndTime = weatherManager.getWeatherEndTime(currentTime);
            flightManagement.handleBadWeather(currentTime, stormEndTime);
            return;
        }

        flightManagement.processGoodWeatherProcessArrival(flightInstanceId, currentTime);
    }

    // ========================
    // Check and complete arrivals
    // ========================
    public void checkAndCompleteArrivals(LocalDateTime currentTime) {
        for (Flight f : flightManagement.getFlights()) {
            if (f.getStatus().equalsIgnoreCase("ARRIVING")) {
                if (!currentTime.isBefore(f.getArrivalDateTime().plusMinutes(20))) {
                    flightManagement.updateFlightStatus(f, "ARRIVED");
                    arrivedFlights.add(f);
                    System.out.println("Flight " + f.getFlightInstanceId() + " has ARRIVED at gate " +
                            f.getGateId() + " using runway " + f.getRunwayId() + ".");

                    if (f.getGateId() != null && !f.getGateId().equals("-")) {
                        flightManagement.freeGate(f);
                    }
                    if (f.getRunwayId() != null && !f.getRunwayId().equals("-")) {
                        flightManagement.freeRunway(f);
                    }
                    System.out.println("-------------------------------------");
                }
            }
        }
        saveAllArrivedFlights(arrivedFlights);
    }

    // ========================
    // Save diverted flight
    // ========================
    public void saveDivertedFlight(Flight f) {
        f.setStatus("DIVERTED");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(arrivedFlightFilePath, true))) {
            bw.write(f.toFileString());
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error saving diverted flight: " + e.getMessage());
        }
    }

    // ========================
    // Save all arrived flights
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
        arrivedFlights.clear();
    }

    // ========================
    // Display arrived flights
    // ========================
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
    // Clear arrived flights file
    // ========================
    public void clearArrivedFlightsFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(arrivedFlightFilePath))) {
            bw.write("");
            System.out.println("Arrived flights file cleared.");
        } catch (IOException e) {
            System.out.println("Error clearing arrived flights file: " + e.getMessage());
        }
    }

    public List<Flight> getArrivedFlights() {
        return new ArrayList<>(arrivedFlights);
    }
}