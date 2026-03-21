package br.com.pedrobelmino.langchain4j.hello.service;

import br.com.pedrobelmino.langchain4j.hello.model.Scooter;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ScooterService {

    private final Map<String, Scooter> scooters = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // São Paulo
        addScooter(new Scooter("SP-001", "Xiaomi Pro 2", "são paulo", "Av. Paulista, 1000", 85, Scooter.Status.AVAILABLE));
        addScooter(new Scooter("SP-002", "Ninebot Max G30", "são paulo", "Rua Augusta, 500", 72, Scooter.Status.AVAILABLE));
        addScooter(new Scooter("SP-003", "Xiaomi Essential", "são paulo", "Parque Ibirapuera", 45, Scooter.Status.IN_USE));
        addScooter(new Scooter("SP-004", "Ninebot E25", "são paulo", "Vila Madalena", 90, Scooter.Status.AVAILABLE));

        // Rio de Janeiro
        addScooter(new Scooter("RJ-001", "Xiaomi Pro 2", "rio de janeiro", "Copacabana, Posto 6", 60, Scooter.Status.AVAILABLE));
        addScooter(new Scooter("RJ-002", "Ninebot Max G30", "rio de janeiro", "Ipanema, Praça General Osório", 80, Scooter.Status.AVAILABLE));
        addScooter(new Scooter("RJ-003", "Xiaomi Essential", "rio de janeiro", "Barra da Tijuca", 30, Scooter.Status.MAINTENANCE));

        // Salvador
        addScooter(new Scooter("SSA-001", "Xiaomi Pro 2", "salvador", "Barra, Farol", 55, Scooter.Status.AVAILABLE));
        addScooter(new Scooter("SSA-002", "Ninebot E25", "salvador", "Pelourinho", 70, Scooter.Status.AVAILABLE));

        // Brasília
        addScooter(new Scooter("BSB-001", "Ninebot Max G30", "brasília", "Asa Norte, W3", 88, Scooter.Status.AVAILABLE));
        addScooter(new Scooter("BSB-002", "Xiaomi Pro 2", "brasília", "Esplanada dos Ministérios", 65, Scooter.Status.IN_USE));

        // Fortaleza
        addScooter(new Scooter("FOR-001", "Xiaomi Essential", "fortaleza", "Praia de Iracema", 78, Scooter.Status.AVAILABLE));
        addScooter(new Scooter("FOR-002", "Ninebot E25", "fortaleza", "Beira Mar", 92, Scooter.Status.AVAILABLE));

        // Curitiba
        addScooter(new Scooter("CWB-001", "Xiaomi Pro 2", "curitiba", "Jardim Botânico", 82, Scooter.Status.AVAILABLE));
        addScooter(new Scooter("CWB-002", "Ninebot Max G30", "curitiba", "Rua XV de Novembro", 67, Scooter.Status.AVAILABLE));

        // Campina Grande
        addScooter(new Scooter("CPG-001", "Xiaomi Essential", "campina grande", "Açude Velho", 75, Scooter.Status.AVAILABLE));

        // Santa Cruz do Sul
        addScooter(new Scooter("SCS-001", "Ninebot E25", "santa cruz do sul", "Parque da Gruta", 80, Scooter.Status.AVAILABLE));
    }

    private void addScooter(Scooter scooter) {
        scooters.put(scooter.getId(), scooter);
    }

    public List<Scooter> listAvailableScootersByCity(String city) {
        String normalizedCity = city.toLowerCase().trim();
        return scooters.values().stream()
                .filter(s -> s.getCity().equalsIgnoreCase(normalizedCity))
                .filter(s -> s.getStatus() == Scooter.Status.AVAILABLE)
                .collect(Collectors.toList());
    }

    public Optional<Scooter> findById(String scooterId) {
        return Optional.ofNullable(scooters.get(scooterId));
    }

    public void updateStatus(String scooterId, Scooter.Status status) {
        Scooter scooter = scooters.get(scooterId);
        if (scooter != null) {
            scooter.setStatus(status);
        }
    }

    public List<Scooter> listAllScootersByCity(String city) {
        String normalizedCity = city.toLowerCase().trim();
        return scooters.values().stream()
                .filter(s -> s.getCity().equalsIgnoreCase(normalizedCity))
                .collect(Collectors.toList());
    }
}
