package br.com.pedrobelmino.langchain4j.hello.service;

import br.com.pedrobelmino.langchain4j.hello.model.Booking;
import br.com.pedrobelmino.langchain4j.hello.model.Scooter;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
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
    private ScooterService scooterService;

    @Autowired
    private BookingService bookingService;

    // Mapa para armazenar códigos de confirmação em memória
    private final Map<String, String> confirmationCodes = new ConcurrentHashMap<>();

    // Record para armazenar os detalhes de preço de cada cidade
    private record CityPrice(double unlockFee, double pricePerMinute) {}

    // Tabela de preços em memória para as cidades do Brasil
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

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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

    @Tool("Lista os patinetes disponíveis para aluguel em uma determinada cidade.")
    public String listAvailableScooters(String city) {
        List<Scooter> available = scooterService.listAvailableScootersByCity(city);

        if (available.isEmpty()) {
            return String.format("Não há patinetes disponíveis na cidade de '%s' no momento. Tente novamente mais tarde.", capitalize(city));
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Patinetes disponíveis em %s:\n\n", capitalize(city)));

        for (Scooter scooter : available) {
            sb.append(String.format("• ID: %s | Modelo: %s | Local: %s | Bateria: %d%%\n",
                    scooter.getId(), scooter.getModel(), scooter.getLocation(), scooter.getBatteryLevel()));
        }

        sb.append(String.format("\nTotal: %d patinete(s) disponível(is).", available.size()));
        return sb.toString();
    }

    @Tool("Reserva um patinete para um usuário. Necessita do ID do patinete e do nome do usuário.")
    public String bookScooter(String scooterId, String userName) {
        Optional<Scooter> scooterOpt = scooterService.findById(scooterId);

        if (scooterOpt.isEmpty()) {
            return String.format("Patinete com ID '%s' não encontrado. Verifique o ID e tente novamente.", scooterId);
        }

        Scooter scooter = scooterOpt.get();
        if (scooter.getStatus() != Scooter.Status.AVAILABLE) {
            return String.format("O patinete %s não está disponível no momento. Status atual: %s.", scooterId, scooter.getStatus());
        }

        Booking booking = bookingService.createBooking(scooterId, userName);
        if (booking == null) {
            return "Não foi possível realizar a reserva. Por favor, tente novamente.";
        }

        return String.format(
                "Reserva realizada com sucesso!\n" +
                "• ID da Reserva: %s\n" +
                "• Patinete: %s (%s)\n" +
                "• Usuário: %s\n" +
                "• Local: %s\n" +
                "• Início: %s\n\n" +
                "Guarde o ID da reserva para devolver o patinete depois.",
                booking.getId(),
                scooter.getId(),
                scooter.getModel(),
                userName,
                scooter.getLocation(),
                booking.getStartTime().format(DATE_FORMATTER)
        );
    }

    @Tool("Devolve um patinete alugado e calcula o custo total. Necessita do ID da reserva.")
    public String returnScooter(String bookingId) {
        Optional<Booking> bookingOpt = bookingService.findById(bookingId);

        if (bookingOpt.isEmpty()) {
            return String.format("Reserva com ID '%s' não encontrada. Verifique o ID e tente novamente.", bookingId);
        }

        Booking booking = bookingOpt.get();
        if (booking.getStatus() != Booking.Status.ACTIVE) {
            return String.format("A reserva %s já foi %s.", bookingId,
                    booking.getStatus() == Booking.Status.COMPLETED ? "finalizada" : "cancelada");
        }

        Booking completed = bookingService.completeBooking(bookingId);
        if (completed == null) {
            return "Não foi possível finalizar a devolução. Por favor, tente novamente.";
        }

        long durationMinutes = java.time.temporal.ChronoUnit.MINUTES.between(
                completed.getStartTime(), completed.getEndTime());

        return String.format(
                "Patinete devolvido com sucesso!\n" +
                "• Reserva: %s\n" +
                "• Duração: %d minuto(s)\n" +
                "• Custo total: R$ %.2f\n\n" +
                "Obrigado por usar nosso serviço!",
                completed.getId(),
                durationMinutes,
                completed.getTotalCost()
        );
    }

    @Tool("Verifica o status de uma reserva pelo ID da reserva.")
    public String checkBookingStatus(String bookingId) {
        Optional<Booking> bookingOpt = bookingService.findById(bookingId);

        if (bookingOpt.isEmpty()) {
            return String.format("Reserva com ID '%s' não encontrada.", bookingId);
        }

        Booking booking = bookingOpt.get();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Status da Reserva %s:\n", booking.getId()));
        sb.append(String.format("• Patinete: %s\n", booking.getScooterId()));
        sb.append(String.format("• Usuário: %s\n", booking.getUserName()));
        sb.append(String.format("• Cidade: %s\n", capitalize(booking.getCity())));
        sb.append(String.format("• Início: %s\n", booking.getStartTime().format(DATE_FORMATTER)));
        sb.append(String.format("• Status: %s\n", translateStatus(booking.getStatus())));

        if (booking.getEndTime() != null) {
            sb.append(String.format("• Fim: %s\n", booking.getEndTime().format(DATE_FORMATTER)));
            sb.append(String.format("• Custo total: R$ %.2f\n", booking.getTotalCost()));
        }

        return sb.toString();
    }

    @Tool("Lista todas as reservas de um usuário pelo nome.")
    public String listUserBookings(String userName) {
        List<Booking> userBookings = bookingService.listUserBookings(userName);

        if (userBookings.isEmpty()) {
            return String.format("Nenhuma reserva encontrada para o usuário '%s'.", userName);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Reservas de %s:\n\n", userName));

        for (Booking booking : userBookings) {
            sb.append(String.format("• Reserva %s | Patinete: %s | Status: %s | Início: %s",
                    booking.getId(),
                    booking.getScooterId(),
                    translateStatus(booking.getStatus()),
                    booking.getStartTime().format(DATE_FORMATTER)));

            if (booking.getTotalCost() > 0) {
                sb.append(String.format(" | Custo: R$ %.2f", booking.getTotalCost()));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private String translateStatus(Booking.Status status) {
        return switch (status) {
            case ACTIVE -> "Ativo";
            case COMPLETED -> "Finalizado";
            case CANCELLED -> "Cancelado";
        };
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
