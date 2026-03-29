package br.com.pedrobelmino.langchain4j.hello.config;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PricingConfigTest {

    private final PricingConfig pricingConfig = new PricingConfig();

    @Test
    void shouldReturnPriceForKnownCity() {
        Optional<PricingConfig.CityPrice> price = pricingConfig.getPriceForCity("São Paulo");
        assertTrue(price.isPresent());
        assertEquals(new BigDecimal("3.00"), price.get().unlockFee());
        assertEquals(new BigDecimal("0.75"), price.get().pricePerMinute());
    }

    @Test
    void shouldReturnEmptyForUnknownCity() {
        Optional<PricingConfig.CityPrice> price = pricingConfig.getPriceForCity("Porto Alegre");
        assertTrue(price.isEmpty());
    }

    @Test
    void shouldCalculateCostCorrectly() {
        BigDecimal cost = pricingConfig.calculateCost("são paulo", 30);
        // 3.00 + (30 * 0.75) = 3.00 + 22.50 = 25.50
        assertEquals(new BigDecimal("25.50"), cost);
    }

    @Test
    void shouldReturnZeroForUnknownCityCost() {
        BigDecimal cost = pricingConfig.calculateCost("Porto Alegre", 30);
        assertEquals(BigDecimal.ZERO, cost);
    }

    @Test
    void shouldHandleCaseInsensitiveCityNames() {
        assertTrue(pricingConfig.isCitySupported("SÃO PAULO"));
        assertTrue(pricingConfig.isCitySupported("são paulo"));
        assertTrue(pricingConfig.isCitySupported("  São Paulo  "));
    }

    @Test
    void shouldSupportAllConfiguredCities() {
        assertTrue(pricingConfig.isCitySupported("são paulo"));
        assertTrue(pricingConfig.isCitySupported("rio de janeiro"));
        assertTrue(pricingConfig.isCitySupported("salvador"));
        assertTrue(pricingConfig.isCitySupported("brasília"));
        assertTrue(pricingConfig.isCitySupported("fortaleza"));
        assertTrue(pricingConfig.isCitySupported("curitiba"));
        assertTrue(pricingConfig.isCitySupported("campina grande"));
        assertTrue(pricingConfig.isCitySupported("santa cruz do sul"));
    }

    @Test
    void getAvailableCitiesShouldReturnAllCities() {
        String cities = pricingConfig.getAvailableCities();
        assertTrue(cities.contains("são paulo"));
        assertTrue(cities.contains("rio de janeiro"));
        assertTrue(cities.contains("curitiba"));
    }
}
