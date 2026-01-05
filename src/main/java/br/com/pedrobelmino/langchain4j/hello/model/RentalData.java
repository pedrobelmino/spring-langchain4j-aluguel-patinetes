package br.com.pedrobelmino.langchain4j.hello.model;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;

@Document(collectionName = "rentals")
public class RentalData {

    @DocumentId
    private String id;
    private String name;
    private String email;
    private String city;
    private int durationInMinutes;

    public RentalData() {
    }

    public RentalData(String name, String email, String city, int durationInMinutes) {
        this.name = name;
        this.email = email;
        this.city = city;
        this.durationInMinutes = durationInMinutes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public void setDurationInMinutes(int durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }
}
