package br.com.pedrobelmino.langchain4j.hello.config;

import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Configuration
public class PricingConfig {

    public record CityPrice(BigDecimal unlockFee, BigDecimal pricePerMinute) {}

    private static final Map<String, CityPrice> PRICE_TABLE = Map.of(
            "são paulo", new CityPrice(new BigDecimal("3.00"), new BigDecimal("0.75")),
            "rio de janeiro", new CityPrice(new BigDecimal("3.50"), new BigDecimal("0.80")),
            "salvador", new CityPrice(new BigDecimal("2.50"), new BigDecimal("0.60")),
            "brasília", new CityPrice(new BigDecimal("2.80"), new BigDecimal("0.70")),
            "fortaleza", new CityPrice(new BigDecimal("2.20"), new BigDecimal("0.55")),
            "curitiba", new CityPrice(new BigDecimal("2.20"), new BigDecimal("0.55")),
            "campina grande", new CityPrice(new BigDecimal("2.20"), new BigDecimal("0.55")),
            "santa cruz do sul", new CityPrice(new BigDecimal("2.20"), new BigDecimal("0.55"))
    );

    public Optional<CityPrice> getPriceForCity(String city) {
        return Optional.ofNullable(PRICE_TABLE.get(city.toLowerCase().trim()));
    }

    public BigDecimal calculateCost(String city, long durationInMinutes) {
        return getPriceForCity(city)
                .map(price -> price.unlockFee()
                        .add(price.pricePerMinute().multiply(BigDecimal.valueOf(durationInMinutes))))
                .orElse(BigDecimal.ZERO);
    }

    public String getAvailableCities() {
        return PRICE_TABLE.keySet().stream()
                .sorted()
                .collect(Collectors.joining(", "));
    }

    public boolean isCitySupported(String city) {
        return PRICE_TABLE.containsKey(city.toLowerCase().trim());
    }
}
