package com.biolock.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Map;

@Document(collection = "user_data")
public class UserData {
    @Id
    private String id;
    private String uid; // Firebase UID
    private String recordTitle;
    private Map<String, Object> fields;

    // getters and setters
}
