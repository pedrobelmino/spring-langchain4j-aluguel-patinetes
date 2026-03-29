package br.com.pedrobelmino.langchain4j.hello.service;

import br.com.pedrobelmino.langchain4j.hello.model.Scooter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ScooterServiceTest {

    private ScooterService scooterService;

    @BeforeEach
    void setUp() {
        scooterService = new ScooterService();
        scooterService.init();
    }

    @Test
    void shouldListAvailableScootersByCityIgnoringCase() {
        List<Scooter> scooters = scooterService.listAvailableScootersByCity("São Paulo");
        assertEquals(3, scooters.size());
        assertTrue(scooters.stream().allMatch(s -> s.getStatus() == Scooter.Status.AVAILABLE));
    }

    @Test
    void shouldReturnEmptyForUnknownCity() {
        List<Scooter> scooters = scooterService.listAvailableScootersByCity("Porto Alegre");
        assertTrue(scooters.isEmpty());
    }

    @Test
    void shouldExcludeInUseAndMaintenanceScooters() {
        List<Scooter> spScooters = scooterService.listAvailableScootersByCity("são paulo");
        assertFalse(spScooters.stream().anyMatch(s -> s.getId().equals("SP-003")));

        List<Scooter> rjScooters = scooterService.listAvailableScootersByCity("rio de janeiro");
        assertFalse(rjScooters.stream().anyMatch(s -> s.getId().equals("RJ-003")));
    }

    @Test
    void shouldFindScooterById() {
        Optional<Scooter> scooter = scooterService.findById("SP-001");
        assertTrue(scooter.isPresent());
        assertEquals("Xiaomi Pro 2", scooter.get().getModel());
    }

    @Test
    void shouldReturnEmptyForNonexistentId() {
        Optional<Scooter> scooter = scooterService.findById("INVALID-ID");
        assertTrue(scooter.isEmpty());
    }

    @Test
    void tryReserveShouldSucceedForAvailableScooter() {
        boolean reserved = scooterService.tryReserve("SP-001");
        assertTrue(reserved);

        Scooter scooter = scooterService.findById("SP-001").orElseThrow();
        assertEquals(Scooter.Status.IN_USE, scooter.getStatus());
    }

    @Test
    void tryReserveShouldFailForInUseScooter() {
        boolean reserved = scooterService.tryReserve("SP-003");
        assertFalse(reserved);
    }

    @Test
    void tryReserveShouldFailForNonexistentScooter() {
        boolean reserved = scooterService.tryReserve("INVALID-ID");
        assertFalse(reserved);
    }

    @Test
    void doubleReserveShouldFail() {
        assertTrue(scooterService.tryReserve("SP-001"));
        assertFalse(scooterService.tryReserve("SP-001"));
    }

    @Test
    void releaseShouldMakeScooterAvailableAgain() {
        scooterService.tryReserve("SP-001");
        scooterService.release("SP-001");

        Scooter scooter = scooterService.findById("SP-001").orElseThrow();
        assertEquals(Scooter.Status.AVAILABLE, scooter.getStatus());

        assertTrue(scooterService.tryReserve("SP-001"));
    }
}
