package br.com.pedrobelmino.langchain4j.hello.service;

import br.com.pedrobelmino.langchain4j.hello.config.PricingConfig;
import br.com.pedrobelmino.langchain4j.hello.model.Booking;
import br.com.pedrobelmino.langchain4j.hello.model.Scooter;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AssistantTools {

    private final EmailService emailService;
    private final ScooterService scooterService;
    private final BookingService bookingService;
    private final PricingConfig pricingConfig;

    private final Map<String, String> confirmationCodes = new ConcurrentHashMap<>();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public AssistantTools(EmailService emailService, ScooterService scooterService,
                          BookingService bookingService, PricingConfig pricingConfig) {
        this.emailService = emailService;
        this.scooterService = scooterService;
        this.bookingService = bookingService;
        this.pricingConfig = pricingConfig;
    }

    @Tool("Envia um e-mail de confirmação para o endereço de e-mail fornecido.")
    public String sendConfirmationEmail(String email) {
        String confirmationCode = String.format("%06d", new Random().nextInt(999999));
        String subject = "Seu código de confirmação";
        String body = "Olá! Seu código de confirmação é: " + confirmationCode;

        try {
            emailService.sendEmail(email, subject, body);
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
            confirmationCodes.remove(email);
            return String.format("E-mail %s confirmado com sucesso! Bem-vindo!", email);
        } else {
            return "Código de confirmação inválido ou expirado. Por favor, tente novamente.";
        }
    }

    @Tool("Calcula o custo do aluguel de um patinete elétrico com base na cidade e na duração em minutos.")
    public String calculateScooterRental(String city, int durationInMinutes) {
        String normalizedCity = city.toLowerCase().trim();
        return pricingConfig.getPriceForCity(normalizedCity)
                .map(price -> {
                    BigDecimal totalCost = price.unlockFee()
                            .add(price.pricePerMinute().multiply(BigDecimal.valueOf(durationInMinutes)));

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
                .orElseGet(() -> String.format(
                        "Desculpe, não temos operação na cidade de '%s'. Atualmente operamos em: %s.",
                        capitalize(city),
                        pricingConfig.getAvailableCities()
                ));
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
        try {
            Booking booking = bookingService.createBooking(scooterId, userName);
            Optional<Scooter> scooterOpt = scooterService.findById(scooterId);
            String model = scooterOpt.map(Scooter::getModel).orElse("N/A");
            String location = scooterOpt.map(Scooter::getLocation).orElse("N/A");

            return String.format(
                    "Reserva realizada com sucesso!\n" +
                    "• ID da Reserva: %s\n" +
                    "• Patinete: %s (%s)\n" +
                    "• Usuário: %s\n" +
                    "• Local: %s\n" +
                    "• Início: %s\n\n" +
                    "Guarde o ID da reserva para devolver o patinete depois.",
                    booking.getId(), scooterId, model, userName, location,
                    booking.getStartTime().format(DATE_FORMATTER));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return e.getMessage();
        }
    }

    @Tool("Devolve um patinete alugado e calcula o custo total. Necessita do ID da reserva.")
    public String returnScooter(String bookingId) {
        try {
            Booking completed = bookingService.completeBooking(bookingId);
            long durationMinutes = ChronoUnit.MINUTES.between(
                    completed.getStartTime(), completed.getEndTime());

            return String.format(
                    "Patinete devolvido com sucesso!\n" +
                    "• Reserva: %s\n" +
                    "• Duração: %d minuto(s)\n" +
                    "• Custo total: R$ %.2f\n\n" +
                    "Obrigado por usar nosso serviço!",
                    completed.getId(), durationMinutes, completed.getTotalCost());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return e.getMessage();
        }
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

    @Tool("Lista os aluguéis ativos de um usuário com tempo decorrido e custo acumulado em tempo real.")
    public String listActiveRentals(String userName) {
        List<Booking> activeBookings = bookingService.listActiveUserBookings(userName);

        if (activeBookings.isEmpty()) {
            return String.format("Nenhum aluguel ativo encontrado para o usuário '%s'.", userName);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Seus aluguéis ativos, %s:\n\n", userName));

        for (Booking booking : activeBookings) {
            long elapsedMinutes = Math.max(1,
                    ChronoUnit.MINUTES.between(booking.getStartTime(), java.time.LocalDateTime.now()));
            long hours = elapsedMinutes / 60;
            long minutes = elapsedMinutes % 60;

            BigDecimal accumulatedCost = pricingConfig.calculateCost(booking.getCity(), elapsedMinutes);

            Optional<Scooter> scooterOpt = scooterService.findById(booking.getScooterId());
            String model = scooterOpt.map(Scooter::getModel).orElse("N/A");

            sb.append(String.format("• Reserva %s\n", booking.getId()));
            sb.append(String.format("  Patinete: %s (%s)\n", booking.getScooterId(), model));
            sb.append(String.format("  Cidade: %s\n", capitalize(booking.getCity())));
            sb.append(String.format("  Início: %s\n", booking.getStartTime().format(DATE_FORMATTER)));

            if (hours > 0) {
                sb.append(String.format("  Tempo decorrido: %dh %dmin\n", hours, minutes));
            } else {
                sb.append(String.format("  Tempo decorrido: %dmin\n", minutes));
            }

            sb.append(String.format("  Custo acumulado: R$ %.2f\n\n", accumulatedCost));
        }

        sb.append(String.format("Total: %d aluguel(is) ativo(s).", activeBookings.size()));
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

            if (booking.getTotalCost().compareTo(BigDecimal.ZERO) > 0) {
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
