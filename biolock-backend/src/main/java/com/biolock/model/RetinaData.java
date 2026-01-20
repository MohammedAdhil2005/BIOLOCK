package com.biolock.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "retina_images")
public class RetinaData {

    @Id
    private String id;

    private String name;
    private String email;
    private String retinaImageBase64; // base64 image string

    // Default constructor
    public RetinaData() {}

    // Getters and Setters
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

    public String getRetinaImageBase64() {
        return retinaImageBase64;
    }

    public void setRetinaImageBase64(String retinaImageBase64) {
        this.retinaImageBase64 = retinaImageBase64;
    }
}
