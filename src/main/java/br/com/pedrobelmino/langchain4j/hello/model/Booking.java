package br.com.pedrobelmino.langchain4j.hello.model;

import java.time.LocalDateTime;

public class Booking {

    public enum Status {
        ACTIVE, COMPLETED, CANCELLED
    }

    private String id;
    private String scooterId;
    private String userName;
    private String city;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Status status;
    private double totalCost;

    public Booking() {
    }

    public Booking(String id, String scooterId, String userName, String city, LocalDateTime startTime) {
        this.id = id;
        this.scooterId = scooterId;
        this.userName = userName;
        this.city = city;
        this.startTime = startTime;
        this.status = Status.ACTIVE;
        this.totalCost = 0.0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getScooterId() {
        return scooterId;
    }

    public void setScooterId(String scooterId) {
        this.scooterId = scooterId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    @Override
    public String toString() {
        return String.format("Reserva [ID: %s, Patinete: %s, Usuário: %s, Cidade: %s, Início: %s, Status: %s, Custo: R$ %.2f]",
                id, scooterId, userName, city, startTime, status, totalCost);
    }
}
