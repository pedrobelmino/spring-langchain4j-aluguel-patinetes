package br.com.pedrobelmino.langchain4j.hello.service;

import br.com.pedrobelmino.langchain4j.hello.model.RentalData;
import br.com.pedrobelmino.langchain4j.hello.repository.RentalRepository;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class AssistantTools {

    @Autowired
    private EmailService emailService;

    @Autowired
    private RentalRepository rentalRepository;

    // Mapa para armazenar códigos de confirmação em memória
    private final Map<String, String> confirmationCodes = new ConcurrentHashMap<>();

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

    @Tool("Envia um e-mail de confirmação para o endereço de e-mail fornecido.")
    public String sendConfirmationEmail(String email) {
        // Gera um código de 6 dígitos aleatório
        String confirmationCode = String.format("%06d", new Random().nextInt(999999));
        String subject = "Seu código de confirmação";
        String body = "Olá! Seu código de confirmação é: " + confirmationCode;

        try {
            emailService.sendEmail(email, subject, body);
            // Armazena o código associado ao e-mail
            confirmationCodes.put(email, confirmationCode);
            return String.format("Um código de confirmação foi enviado para %s. Por favor, verifique sua caixa de entrada e SPAM.", email);
        } catch (Exception e) {
            // Log do erro seria ideal aqui
            return String.format("Desculpe, não foi possível enviar o e-mail de confirmação para %s. Por favor, tente novamente mais tarde. Devido a %s", email, e.getMessage());
        }
    }

    @Tool("Confirma o cadastro do usuário por e-mail com um código de confirmação.")
    public String confirmRegistration(String email, String confirmationCode) {
        String storedCode = confirmationCodes.get(email);

        if (storedCode != null && storedCode.equals(confirmationCode)) {
            // Remove o código após o uso para segurança
            confirmationCodes.remove(email);
            return String.format("E-mail %s confirmado com sucesso! Bem-vindo!", email);
        } else {
            return "Código de confirmação inválido ou expirado. Por favor, tente novamente.";
        }
    }

    @Tool("Calcula o custo do aluguel de um patinete elétrico com base na cidade e na duração em minutos.")
    public String calculateScooterRental(String city, int durationInMinutes, String name, String email) {
        String normalizedCity = city.toLowerCase().trim();
        return Optional.ofNullable(PRICE_TABLE.get(normalizedCity))
                .map(price -> {
                    double totalCost = price.unlockFee() + (durationInMinutes * price.pricePerMinute());
                    RentalData rentalData = new RentalData(name, email, city, durationInMinutes);
                    rentalRepository.save(rentalData).block();
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
