package br.com.pedrobelmino.langchain4j.hello.service;

import br.com.pedrobelmino.langchain4j.hello.model.Booking;
import br.com.pedrobelmino.langchain4j.hello.model.Scooter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final Map<String, Booking> bookings = new ConcurrentHashMap<>();
    private final ScooterService scooterService;

    // Tabela de preços por cidade (mesma do AssistantTools)
    private record CityPrice(double unlockFee, double pricePerMinute) {}

    private static final Map<String, CityPrice> PRICE_TABLE = Map.of(
            "são paulo", new CityPrice(3.00, 0.75),
            "rio de janeiro", new CityPrice(3.50, 0.80),
            "salvador", new CityPrice(2.50, 0.60),
            "brasília", new CityPrice(2.80, 0.70),
            "fortaleza", new CityPrice(2.20, 0.55),
            "curitiba", new CityPrice(2.20, 0.55),
            "campina grande", new CityPrice(2.20, 0.55),
            "santa cruz do sul", new CityPrice(2.20, 0.55)
    );

    public BookingService(ScooterService scooterService) {
        this.scooterService = scooterService;
    }

    public Booking createBooking(String scooterId, String userName) {
        Optional<Scooter> scooterOpt = scooterService.findById(scooterId);
        if (scooterOpt.isEmpty()) {
            return null;
        }

        Scooter scooter = scooterOpt.get();
        if (scooter.getStatus() != Scooter.Status.AVAILABLE) {
            return null;
        }

        String bookingId = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Booking booking = new Booking(bookingId, scooterId, userName, scooter.getCity(), LocalDateTime.now());

        scooterService.updateStatus(scooterId, Scooter.Status.IN_USE);
        bookings.put(bookingId, booking);

        return booking;
    }

    public Booking completeBooking(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null || booking.getStatus() != Booking.Status.ACTIVE) {
            return null;
        }

        booking.setEndTime(LocalDateTime.now());
        booking.setStatus(Booking.Status.COMPLETED);

        long durationMinutes = ChronoUnit.MINUTES.between(booking.getStartTime(), booking.getEndTime());
        if (durationMinutes < 1) {
            durationMinutes = 1; // mínimo de 1 minuto
        }

        String normalizedCity = booking.getCity().toLowerCase().trim();
        CityPrice price = PRICE_TABLE.get(normalizedCity);
        if (price != null) {
            double totalCost = price.unlockFee() + (durationMinutes * price.pricePerMinute());
            booking.setTotalCost(totalCost);
        }

        scooterService.updateStatus(booking.getScooterId(), Scooter.Status.AVAILABLE);

        return booking;
    }

    public Booking cancelBooking(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null || booking.getStatus() != Booking.Status.ACTIVE) {
            return null;
        }

        booking.setEndTime(LocalDateTime.now());
        booking.setStatus(Booking.Status.CANCELLED);
        booking.setTotalCost(0.0);

        scooterService.updateStatus(booking.getScooterId(), Scooter.Status.AVAILABLE);

        return booking;
    }

    public Optional<Booking> findById(String bookingId) {
        return Optional.ofNullable(bookings.get(bookingId));
    }

    public List<Booking> listUserBookings(String userName) {
        return bookings.values().stream()
                .filter(b -> b.getUserName().equalsIgnoreCase(userName))
                .collect(Collectors.toList());
    }
}
