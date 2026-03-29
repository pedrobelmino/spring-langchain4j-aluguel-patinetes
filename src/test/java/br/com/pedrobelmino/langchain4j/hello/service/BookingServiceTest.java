package br.com.pedrobelmino.langchain4j.hello.service;

import br.com.pedrobelmino.langchain4j.hello.config.PricingConfig;
import br.com.pedrobelmino.langchain4j.hello.model.Booking;
import br.com.pedrobelmino.langchain4j.hello.model.Scooter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {

    private BookingService bookingService;
    private ScooterService scooterService;

    @BeforeEach
    void setUp() {
        scooterService = new ScooterService();
        scooterService.init();
        PricingConfig pricingConfig = new PricingConfig();
        bookingService = new BookingService(scooterService, pricingConfig);
    }

    @Test
    void shouldCreateBookingSuccessfully() {
        Booking booking = bookingService.createBooking("SP-001", "João");

        assertNotNull(booking);
        assertTrue(booking.getId().startsWith("RES-"));
        assertEquals("SP-001", booking.getScooterId());
        assertEquals("João", booking.getUserName());
        assertEquals("são paulo", booking.getCity());
        assertEquals(Booking.Status.ACTIVE, booking.getStatus());
        assertEquals(BigDecimal.ZERO, booking.getTotalCost());

        Scooter scooter = scooterService.findById("SP-001").orElseThrow();
        assertEquals(Scooter.Status.IN_USE, scooter.getStatus());
    }

    @Test
    void shouldThrowWhenBookingNonexistentScooter() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking("INVALID", "João"));
        assertTrue(ex.getMessage().contains("não encontrado"));
    }

    @Test
    void shouldThrowWhenBookingUnavailableScooter() {
        bookingService.createBooking("SP-001", "João");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> bookingService.createBooking("SP-001", "Maria"));
        assertTrue(ex.getMessage().contains("não está disponível"));
    }

    @Test
    void shouldCompleteBookingAndCalculateCost() {
        Booking booking = bookingService.createBooking("SP-001", "João");
        Booking completed = bookingService.completeBooking(booking.getId());

        assertEquals(Booking.Status.COMPLETED, completed.getStatus());
        assertNotNull(completed.getEndTime());
        assertTrue(completed.getTotalCost().compareTo(BigDecimal.ZERO) > 0);

        Scooter scooter = scooterService.findById("SP-001").orElseThrow();
        assertEquals(Scooter.Status.AVAILABLE, scooter.getStatus());
    }

    @Test
    void shouldThrowWhenCompletingNonexistentBooking() {
        assertThrows(IllegalArgumentException.class,
                () -> bookingService.completeBooking("RES-INVALID"));
    }

    @Test
    void shouldThrowWhenCompletingAlreadyCompletedBooking() {
        Booking booking = bookingService.createBooking("SP-001", "João");
        bookingService.completeBooking(booking.getId());

        assertThrows(IllegalStateException.class,
                () -> bookingService.completeBooking(booking.getId()));
    }

    @Test
    void shouldCancelBooking() {
        Booking booking = bookingService.createBooking("SP-001", "João");
        Booking cancelled = bookingService.cancelBooking(booking.getId());

        assertEquals(Booking.Status.CANCELLED, cancelled.getStatus());
        assertEquals(BigDecimal.ZERO, cancelled.getTotalCost());

        Scooter scooter = scooterService.findById("SP-001").orElseThrow();
        assertEquals(Scooter.Status.AVAILABLE, scooter.getStatus());
    }

    @Test
    void shouldFindBookingByIdCaseInsensitive() {
        Booking booking = bookingService.createBooking("SP-001", "João");
        String lowerId = booking.getId().toLowerCase();

        assertTrue(bookingService.findById(lowerId).isPresent());
    }

    @Test
    void shouldListUserBookings() {
        bookingService.createBooking("SP-001", "João");
        bookingService.createBooking("SP-002", "João");
        bookingService.createBooking("RJ-001", "Maria");

        List<Booking> joaoBookings = bookingService.listUserBookings("João");
        assertEquals(2, joaoBookings.size());

        List<Booking> mariaBookings = bookingService.listUserBookings("Maria");
        assertEquals(1, mariaBookings.size());
    }

    @Test
    void shouldReturnEmptyListForUnknownUser() {
        List<Booking> bookings = bookingService.listUserBookings("Desconhecido");
        assertTrue(bookings.isEmpty());
    }

    @Test
    void costCalculationShouldUseMinimumOneMinute() {
        Booking booking = bookingService.createBooking("SP-001", "João");
        Booking completed = bookingService.completeBooking(booking.getId());

        // SP unlock=3.00, per_min=0.75, min 1 min => 3.00 + 0.75 = 3.75
        assertEquals(new BigDecimal("3.75"), completed.getTotalCost());
    }
}
