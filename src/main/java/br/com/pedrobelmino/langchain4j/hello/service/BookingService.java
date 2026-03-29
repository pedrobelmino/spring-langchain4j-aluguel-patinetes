package br.com.pedrobelmino.langchain4j.hello.service;

import br.com.pedrobelmino.langchain4j.hello.config.PricingConfig;
import br.com.pedrobelmino.langchain4j.hello.model.Booking;
import br.com.pedrobelmino.langchain4j.hello.model.Scooter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BookingService {

    private final Map<String, Booking> bookings = new ConcurrentHashMap<>();
    private final ScooterService scooterService;
    private final PricingConfig pricingConfig;

    public BookingService(ScooterService scooterService, PricingConfig pricingConfig) {
        this.scooterService = scooterService;
        this.pricingConfig = pricingConfig;
    }

    public Booking createBooking(String scooterId, String userName) {
        Scooter scooter = scooterService.findById(scooterId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Patinete com ID '%s' não encontrado.", scooterId)));

        if (!scooterService.tryReserve(scooterId)) {
            throw new IllegalStateException(
                    String.format("O patinete %s não está disponível no momento. Status atual: %s.",
                            scooterId, scooter.getStatus()));
        }

        String bookingId = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Booking booking = new Booking(bookingId, scooterId, userName, scooter.getCity(), LocalDateTime.now());
        bookings.put(bookingId, booking);

        return booking;
    }

    public Booking completeBooking(String bookingId) {
        Booking booking = findActiveBooking(bookingId);

        booking.setEndTime(LocalDateTime.now());
        booking.setStatus(Booking.Status.COMPLETED);

        long durationMinutes = Math.max(1,
                ChronoUnit.MINUTES.between(booking.getStartTime(), booking.getEndTime()));

        BigDecimal totalCost = pricingConfig.calculateCost(booking.getCity(), durationMinutes);
        booking.setTotalCost(totalCost);

        scooterService.release(booking.getScooterId());

        return booking;
    }

    public Booking cancelBooking(String bookingId) {
        Booking booking = findActiveBooking(bookingId);

        booking.setEndTime(LocalDateTime.now());
        booking.setStatus(Booking.Status.CANCELLED);
        booking.setTotalCost(BigDecimal.ZERO);

        scooterService.release(booking.getScooterId());

        return booking;
    }

    public Optional<Booking> findById(String bookingId) {
        return Optional.ofNullable(bookings.get(bookingId.toUpperCase()));
    }

    public List<Booking> listUserBookings(String userName) {
        return bookings.values().stream()
                .filter(b -> b.getUserName().equalsIgnoreCase(userName))
                .toList();
    }

    public List<Booking> listActiveUserBookings(String userName) {
        return bookings.values().stream()
                .filter(b -> b.getUserName().equalsIgnoreCase(userName))
                .filter(b -> b.getStatus() == Booking.Status.ACTIVE)
                .toList();
    }

    private Booking findActiveBooking(String bookingId) {
        Booking booking = bookings.get(bookingId.toUpperCase());
        if (booking == null) {
            throw new IllegalArgumentException(
                    String.format("Reserva com ID '%s' não encontrada.", bookingId));
        }
        if (booking.getStatus() != Booking.Status.ACTIVE) {
            throw new IllegalStateException(
                    String.format("A reserva %s já foi %s.", bookingId,
                            booking.getStatus() == Booking.Status.COMPLETED ? "finalizada" : "cancelada"));
        }
        return booking;
    }
}
