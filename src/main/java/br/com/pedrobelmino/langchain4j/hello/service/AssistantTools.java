package br.com.pedrobelmino.langchain4j.hello.service;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AssistantTools {

    // Record para armazenar os detalhes de preço de cada cidade
    private record CityPrice(double unlockFee, double pricePerMinute) {}

    // Tabela de preços em memória para as 5 principais cidades do Brasil
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

    @Tool("Calcula o custo do aluguel de um patinete elétrico com base na cidade e na duração em minutos.")
    public String calculateScooterRental(String city, int durationInMinutes) {
        String normalizedCity = city.toLowerCase().trim();
        return Optional.ofNullable(PRICE_TABLE.get(normalizedCity))
                .map(price -> {
                    double totalCost = price.unlockFee() + (durationInMinutes * price.pricePerMinute());
                    return String.format(
                            "Simulação para %s: Alugar um patinete por %d minutos custa R$ %.2f. (Taxa de desbloqueio de R$ %.2f + %d min a R$ %.2f/min).",
                            capitalize(city),
                            durationInMinutes,
                            totalCost,
                            price.unlockFee(),
                            durationInMinutes,
                            price.pricePerMinute()
                    );
                })
                .orElseGet(() -> {
                    String availableCities = PRICE_TABLE.keySet().stream()
                            .map(this::capitalize)
                            .collect(Collectors.joining(", "));
                    return String.format(
                            "Desculpe, não temos operação na cidade de '%s'. Atualmente operamos em: %s.",
                            capitalize(city),
                            availableCities
                    );
                });
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        String[] words = str.split(" ");
        StringBuilder capitalized = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                capitalized.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase()).append(" ");
            }
        }
        return capitalized.toString().trim();
    }
}
