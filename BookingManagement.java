import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.*;

public class BookingManagement {

    // ============================================================
    // FIELDS
    // ============================================================
    private FlightManagement flightManagement;
    private PassengerManagement passengerManagement;
    private Scanner scanner;

    private final int WINDOW_SEAT_PRICE = 500;
    private final int MIDDLE_SEAT_PRICE = 300;
    private final double CANCELLATION_REFUND_PERCENTAGE = 0.5;
    private final int CANCELLATION_HOURS_THRESHOLD = 6;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    public BookingManagement(FlightManagement fm, PassengerManagement pm) {
        this.flightManagement = fm;
        this.passengerManagement = pm;
        this.scanner = new Scanner(System.in);
    }

    // ============================================================
    // MAIN BOOKING FLOW
    // ============================================================
    public void startBooking() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    ✈️  BOOKING MANAGEMENT  ✈️                    ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        System.out.print("\n📍 Enter destination: ");
        String destination = scanner.nextLine();

        System.out.print("📅 Enter travel date (yyyy-MM-dd): ");
        String dateInput = scanner.nextLine();
        LocalDate date;
        try {
            date = LocalDate.parse(dateInput, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            System.out.println("❌ Invalid date format.");
            return;
        }

        List<Flight> availableFlights = flightManagement.getFlightsByDestinationAndDate(destination, date);
        if (availableFlights.isEmpty()) {
            System.out.println("\n❌ No flights available for " + destination + " on " + date);
            return;
        }

        displayAvailableFlights(availableFlights);

        System.out.print("\n✈️ Enter the number of the flight you want to book: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        Flight selectedFlight = getSelectedFlight(availableFlights, choice);
        if (selectedFlight == null) return;

        if (selectedFlight.getSeatsLeft() == 0) {
            System.out.println("❌ No seats left on this flight.");
            return;
        }

        displayFlightDetails(selectedFlight);

        SeatLayout seatLayout = new SeatLayout(selectedFlight);
        List<String> selectedSeats = new ArrayList<>();
        int totalPrice = 0;

        while (true) {
            System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
            System.out.println("║                      🪑 SEAT SELECTION 🪑                      ║");
            System.out.println("╚════════════════════════════════════════════════════════════════╝");
            seatLayout.displayLayout(selectedSeats);

            System.out.print("\n🪑 Enter seats to select (comma-separated, e.g., A1,B2) or leave empty to skip: ");
            String seatInput = scanner.nextLine().trim();
            if (!seatInput.isEmpty()) {
                String[] seatChoices = seatInput.split(",");
                for (String s : seatChoices) {
                    s = s.trim().toUpperCase();

                    if (!seatLayout.isValidSeat(s)) {
                        System.out.println("   ❌ Seat " + s + " does not exist.");
                        continue;
                    }

                    if (!selectedSeats.contains(s) && seatLayout.isAvailable(s)) {
                        selectedSeats.add(s);
                        char seatCol = s.charAt(0);
                        int seatPrice = (seatCol == 'A' || seatCol == 'F') ? WINDOW_SEAT_PRICE : MIDDLE_SEAT_PRICE;
                        totalPrice += seatPrice;
                        System.out.println("   ✅ Seat " + s + " added. Price: ৳" + seatPrice);
                    } else {
                        System.out.println("   ❌ Seat " + s + " is not available or already selected.");
                    }
                }
            }

            seatLayout.displayLayout(selectedSeats);
            System.out.println("\n💰 Total price so far: ৳" + totalPrice);

            System.out.println("\n📋 Options:");
            System.out.println("   1. Select more seats");
            System.out.println("   2. Cancel a selected seat");
            System.out.println("   3. Proceed to confirmation");
            System.out.print("\n👉 Enter choice: ");
            int action = scanner.nextInt();
            scanner.nextLine();

            if (action == 1) {
                continue;
            } else if (action == 2) {
                System.out.print("🗑️ Enter seat to cancel: ");
                String cancelSeat = scanner.nextLine().trim().toUpperCase();
                if (selectedSeats.remove(cancelSeat)) {
                    char seatCol = cancelSeat.charAt(0);
                    totalPrice -= (seatCol == 'A' || seatCol == 'F') ? WINDOW_SEAT_PRICE : MIDDLE_SEAT_PRICE;
                    System.out.println("   ✅ Seat " + cancelSeat + " cancelled.");
                } else {
                    System.out.println("   ❌ Seat not in selection.");
                }
            } else if (action == 3) {
                break;
            } else {
                System.out.println("   ❌ Invalid choice.");
            }
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                  📝 BOOKING CONFIRMATION 📝                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        System.out.print("\n👤 Enter passenger name: ");
        String passengerName = scanner.nextLine();

        displayBookingOverview(passengerName, selectedFlight, selectedSeats, totalPrice);

        System.out.print("\n✅ Confirm booking? (Y/N): ");
        String confirm = scanner.nextLine().trim().toUpperCase();
        if (!confirm.equals("Y")) {
            System.out.println("\n❌ Booking cancelled.");
            return;
        }

        String ticketId = generateTicketID();

        LocalDateTime departureTime = selectedFlight.getDepartDateTime();
        LocalDateTime checkInStartTime = departureTime.minusHours(2);
        LocalDateTime boardingStartTime = selectedFlight.getScheduledActionTime();
        LocalDateTime boardingCloseTime = departureTime.minusMinutes(15);

        Passenger passenger = new Passenger(
                ticketId, passengerName, selectedFlight.getFlightInstanceId(),
                selectedFlight.getOrigin(), selectedFlight.getDestination(),
                departureTime, checkInStartTime,
                false, false, "-",
                selectedSeats, totalPrice
        );

        passengerManagement.addPassenger(passenger);
        selectedFlight.bookSpecificSeats(selectedSeats);
        flightManagement.saveFlightsToFile();

        displayTicket(passenger, selectedFlight, boardingStartTime, boardingCloseTime);
    }

    // ============================================================
    // DISPLAY HELPER METHODS
    // ============================================================
    private void displayAvailableFlights(List<Flight> flights) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                              ✈️ AVAILABLE FLIGHTS ✈️                                 ║");
        System.out.println("╠════╦══════════════════════════╦══════════════╦══════════════╦══════════════════════════╣");
        System.out.println("║ No ║ Flight Instance ID       ║ Departure    ║ Departure    ║ Seats Left               ║");
        System.out.println("║    ║                          ║ Date         ║ Time         ║                          ║");
        System.out.println("╠════╬══════════════════════════╬══════════════╬══════════════╬══════════════════════════╣");

        int index = 1;
        for (Flight f : flights) {
            System.out.printf("║ %-2d ║ %-24s ║ %-12s ║ %-12s ║ %-22d ║\n",
                    index, f.getFlightInstanceId(),
                    f.getDepartDateTime().toLocalDate(),
                    f.getDepartDateTime().toLocalTime(),
                    f.getSeatsLeft());
            index++;
        }
        System.out.println("╚════╩══════════════════════════╩══════════════╩══════════════╩══════════════════════════╝");
    }

    private Flight getSelectedFlight(List<Flight> flights, int choice) {
        if (choice < 1 || choice > flights.size()) {
            System.out.println("❌ Invalid selection.");
            return null;
        }
        return flights.get(choice - 1);
    }

    private void displayFlightDetails(Flight flight) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                     ✈️ FLIGHT DETAILS ✈️                      ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ %-15s : %-44s ║\n", "Flight Number", flight.getFlightNumber());
        System.out.printf("║ %-15s : %-44s ║\n", "Flight Instance", flight.getFlightInstanceId());
        System.out.printf("║ %-15s : %-44s ║\n", "Origin", flight.getOrigin());
        System.out.printf("║ %-15s : %-44s ║\n", "Destination", flight.getDestination());
        System.out.printf("║ %-15s : %-44s ║\n", "Departure", flight.getDepartDateTime());
        System.out.printf("║ %-15s : %-44s ║\n", "Arrival", flight.getArrivalDateTime());
        System.out.printf("║ %-15s : %-44d ║\n", "Seats Available", flight.getSeatsLeft());
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }

    private void displayBookingOverview(String passengerName, Flight flight, List<String> seats, int totalPrice) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                     📋 BOOKING OVERVIEW 📋                     ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ %-15s : %-44s ║\n", "Passenger Name", passengerName);
        System.out.printf("║ %-15s : %-44s ║\n", "Destination", flight.getDestination());
        System.out.printf("║ %-15s : %-44s ║\n", "Flight", flight.getFlightInstanceId());
        System.out.printf("║ %-15s : %-44s ║\n", "Origin", flight.getOrigin());
        System.out.printf("║ %-15s : %-44s ║\n", "Departure Date", flight.getDepartDateTime().toLocalDate());
        System.out.printf("║ %-15s : %-44s ║\n", "Departure Time", flight.getDepartDateTime().toLocalTime());
        System.out.printf("║ %-15s : %-44s ║\n", "Arrival", flight.getArrivalDateTime());
        System.out.printf("║ %-15s : %-44s ║\n", "Seats Selected", String.join(", ", seats));
        System.out.printf("║ %-15s : ৳%-43d ║\n", "Total Price", totalPrice);
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }

    private void displayTicket(Passenger passenger, Flight flight, LocalDateTime boardingStart, LocalDateTime boardingClose) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                                    🎫 BOARDING PASS 🎫                                   ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ %-20s : %-64s ║\n", "Ticket ID", passenger.getTicketId());
        System.out.printf("║ %-20s : %-64s ║\n", "Passenger Name", passenger.getPassengerName());
        System.out.println("╠────────────────────────────────────────────────────────────────────────────────────────╣");
        System.out.printf("║ %-20s : %-64s ║\n", "Flight", passenger.getFlightInstanceId());
        System.out.printf("║ %-20s : %-64s ║\n", "Route", passenger.getOrigin() + " → " + passenger.getDestination());
        System.out.println("╠────────────────────────────────────────────────────────────────────────────────────────╣");
        System.out.printf("║ %-20s : %-64s ║\n", "Departure Date", flight.getDepartDateTime().toLocalDate());
        System.out.printf("║ %-20s : %-64s ║\n", "Departure Time", flight.getDepartDateTime().toLocalTime());
        System.out.printf("║ %-20s : %-64s ║\n", "Arrival Time", flight.getArrivalDateTime().toLocalTime());
        System.out.println("╠────────────────────────────────────────────────────────────────────────────────────────╣");
        System.out.printf("║ %-20s : %-64s ║\n", "Check-in Opens", passenger.getCheckInStartTime());
        System.out.printf("║ %-20s : %-64s ║\n", "Boarding Starts", boardingStart);
        System.out.printf("║ %-20s : %-64s ║\n", "Boarding Closes", boardingClose);
        System.out.println("╠────────────────────────────────────────────────────────────────────────────────────────╣");
        System.out.printf("║ %-20s : %-64s ║\n", "Seat(s)", String.join(", ", passenger.getSeats()));
        System.out.printf("║ %-20s : ৳%-63d ║\n", "Total Paid", passenger.getTotalPrice());
        System.out.println("╠════════════════════════════════════════════════════════════════════════════════════════╣");
        System.out.println("║                                    IMPORTANT NOTES                                    ║");
        System.out.println("║ • Please arrive at the airport at least 2 hours before departure                    ║");
        System.out.println("║ • Check-in closes 45 minutes before departure                                      ║");
        System.out.println("║ • Boarding closes 15 minutes before departure                                      ║");
        System.out.println("║ • Cancellation allowed up to 6 hours before departure (50% refund)                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════════════╝");
        System.out.println("\n🎉 Booking confirmed! Thank you for choosing our airline! 🎉\n");
    }

    // ============================================================
    // CANCELLATION SYSTEM
    // ============================================================
    public void cancelBooking(LocalDateTime currentTime) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                  🗑️ TICKET CANCELLATION 🗑️                   ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        System.out.print("\n🎫 Enter Ticket ID: ");
        String ticketId = scanner.nextLine().trim();

        Passenger passenger = findPassengerByTicketId(ticketId);
        if (passenger == null) {
            System.out.println("\n❌ Ticket not found.");
            return;
        }

        if (passenger.isBoardingPassIssued()) {
            System.out.println("\n❌ Cannot cancel. Boarding pass already issued.");
            return;
        }

        Flight flight = flightManagement.FindFlightByInstanceId(passenger.getFlightInstanceId());
        if (flight == null) {
            System.out.println("\n❌ Flight not found.");
            return;
        }

        LocalDateTime departureTime = flight.getDepartDateTime();
        long hoursUntilDeparture = Duration.between(currentTime, departureTime).toHours();

        displayCurrentBooking(passenger, flight, hoursUntilDeparture);

        if (hoursUntilDeparture < CANCELLATION_HOURS_THRESHOLD) {
            System.out.println("\n❌ Cancellation not allowed.");
            System.out.println("   Cancellations are only allowed up to " + CANCELLATION_HOURS_THRESHOLD + " hours before departure.");
            System.out.println("   Current simulation time is only " + hoursUntilDeparture + " hours before departure.");
            return;
        }

        showCancellationOptions(passenger, flight);
    }

    private void displayCurrentBooking(Passenger passenger, Flight flight, long hoursUntilDeparture) {
        System.out.println("\n📋 Current Booking Details:");
        System.out.println("   Ticket ID: " + passenger.getTicketId());
        System.out.println("   Passenger: " + passenger.getPassengerName());
        System.out.println("   Flight: " + passenger.getFlightInstanceId());
        System.out.println("   Route: " + passenger.getOrigin() + " → " + passenger.getDestination());
        System.out.println("   Departure: " + flight.getDepartDateTime());
        System.out.println("   Hours until departure: " + hoursUntilDeparture);
        System.out.println("   Seats booked: " + String.join(", ", passenger.getSeats()));
        System.out.println("   Total paid: ৳" + passenger.getTotalPrice());
    }

    private void showCancellationOptions(Passenger passenger, Flight flight) {
        List<String> currentSeats = new ArrayList<>(passenger.getSeats());
        int currentPrice = passenger.getTotalPrice();

        System.out.println("\n📋 Cancellation Options:");
        System.out.println("   1. Cancel entire booking (all seats)");
        System.out.println("   2. Cancel specific seat(s)");
        System.out.println("   3. Back to main menu");
        System.out.print("\n👉 Enter choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                cancelEntireBooking(passenger, flight, currentPrice);
                break;
            case 2:
                cancelSpecificSeats(passenger, flight, currentSeats, currentPrice);
                break;
            case 3:
                System.out.println("\n❌ Cancellation cancelled.");
                break;
            default:
                System.out.println("\n❌ Invalid choice.");
        }
    }

    private void cancelEntireBooking(Passenger passenger, Flight flight, int originalPrice) {
        System.out.println("\n⚠️ WARNING: You are about to cancel ALL seats for this booking.");
        System.out.println("   Seats to cancel: " + String.join(", ", passenger.getSeats()));
        System.out.println("   Total refund: ৳" + (int) (originalPrice * CANCELLATION_REFUND_PERCENTAGE));

        System.out.print("\n👉 Confirm cancellation? (Y/N): ");
        String confirm = scanner.nextLine().trim().toUpperCase();

        if (confirm.equals("Y")) {
            for (String seat : passenger.getSeats()) {
                flight.removeOccupiedSeat(seat);
            }
            flightManagement.saveFlightsToFile();

            // ✅ NEW: Save to cancelledBookings.txt
            passengerManagement.cancelBooking(passenger);

            System.out.println("\n✅ Entire booking cancelled successfully!");
            System.out.println("   Refund amount: ৳" + (int) (originalPrice * CANCELLATION_REFUND_PERCENTAGE));
            System.out.println("   Refund will be processed within 5-7 business days.");
        } else {
            System.out.println("\n❌ Cancellation aborted.");
        }
    }


    private void cancelSpecificSeats(Passenger passenger, Flight flight, List<String> currentSeats, int currentPrice) {
        System.out.println("\n🪑 Current seats: " + String.join(", ", currentSeats));
        System.out.print("\n🪑 Enter seat(s) to cancel (comma-separated, e.g., A1,B2): ");
        String seatInput = scanner.nextLine().trim().toUpperCase();

        String[] seatsToCancel = seatInput.split(",");
        List<String> cancelledSeats = new ArrayList<>();
        List<String> remainingSeats = new ArrayList<>(currentSeats);
        int refundAmount = 0;

        for (String seat : seatsToCancel) {
            seat = seat.trim();
            if (currentSeats.contains(seat)) {
                cancelledSeats.add(seat);
                remainingSeats.remove(seat);
                char seatCol = seat.charAt(0);
                int seatPrice = (seatCol == 'A' || seatCol == 'F') ? WINDOW_SEAT_PRICE : MIDDLE_SEAT_PRICE;
                refundAmount += seatPrice;
            } else {
                System.out.println("   ❌ Seat " + seat + " not found in your booking.");
            }
        }

        if (cancelledSeats.isEmpty()) {
            System.out.println("\n❌ No valid seats selected for cancellation.");
            return;
        }

        int actualRefund = (int) (refundAmount * CANCELLATION_REFUND_PERCENTAGE);
        int newTotalPrice = currentPrice - refundAmount;

        System.out.println("\n📋 Cancellation Summary:");
        System.out.println("   Seats to cancel: " + String.join(", ", cancelledSeats));
        System.out.println("   Remaining seats: " + (remainingSeats.isEmpty() ? "None" : String.join(", ", remainingSeats)));
        System.out.println("   Original price: ৳" + currentPrice);
        System.out.println("   Seat price refund (before policy): ৳" + refundAmount);
        System.out.println("   Actual refund (50%): ৳" + actualRefund);
        System.out.println("   New total price: ৳" + newTotalPrice);

        System.out.print("\n👉 Confirm cancellation of these seats? (Y/N): ");
        String confirm = scanner.nextLine().trim().toUpperCase();

        if (confirm.equals("Y")) {
            for (String seat : cancelledSeats) {
                flight.removeOccupiedSeat(seat);
            }
            flightManagement.saveFlightsToFile();

            passenger.setSeats(remainingSeats);
            passenger.setTotalPrice(newTotalPrice);

            if (remainingSeats.isEmpty()) {
                // ✅ NEW: All seats cancelled - move to cancelledBookings.txt
                passengerManagement.cancelBooking(passenger);
                System.out.println("\n✅ All seats cancelled. Booking fully cancelled.");
            } else {
                passengerManagement.savePassengersToFile();
                System.out.println("\n✅ Seat cancellation successful!");
                System.out.println("   Remaining seats: " + String.join(", ", remainingSeats));
                System.out.println("   New total: ৳" + newTotalPrice);
            }
            System.out.println("   Refund amount: ৳" + actualRefund);
            System.out.println("   Refund will be processed within 5-7 business days.");
        } else {
            System.out.println("\n❌ Cancellation aborted.");
        }
    }

    private Passenger findPassengerByTicketId(String ticketId) {
        for (Passenger p : passengerManagement.getAllPassengers()) {
            if (p.getTicketId().equals(ticketId)) {
                return p;
            }
        }
        return null;
    }

    // ============================================================
    // TICKET ID GENERATOR
    // ============================================================
    private String generateTicketID() {
        List<Passenger> allPassengers = passengerManagement.getAllPassengers();
        int maxId = 0;

        for (Passenger p : allPassengers) {
            String tid = p.getTicketId().replaceAll("\\D", "");
            try {
                int num = Integer.parseInt(tid);
                if (num > maxId) maxId = num;
            } catch (NumberFormatException ignored) {
            }
        }

        int nextId = maxId + 1;
        return String.format("T%03d", nextId);
    }
}