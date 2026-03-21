package br.com.pedrobelmino.langchain4j.hello.model;

public class Scooter {

    public enum Status {
        AVAILABLE, IN_USE, MAINTENANCE
    }

    private String id;
    private String model;
    private String city;
    private String location;
    private int batteryLevel;
    private Status status;

    public Scooter() {
    }

    public Scooter(String id, String model, String city, String location, int batteryLevel, Status status) {
        this.id = id;
        this.model = model;
        this.city = city;
        this.location = location;
        this.batteryLevel = batteryLevel;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("Patinete [ID: %s, Modelo: %s, Cidade: %s, Local: %s, Bateria: %d%%, Status: %s]",
                id, model, city, location, batteryLevel, status);
    }
}
