package com.biolock.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import java.util.List;

@Document(collection = "custom")
public class CustomRecord {

    @Id
    private String id;

    private String uid;
    private String recordTitle;
    private List<Field> fields; // List of Field objects
    private Date createdAt = new Date();

    // Constructors
    public CustomRecord() {}

    public CustomRecord(String uid, String recordTitle, List<Field> fields) {
        this.uid = uid;
        this.recordTitle = recordTitle;
        this.fields = fields;
        this.createdAt = new Date();
    }

    // Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRecordTitle() {
        return recordTitle;
    }

    public void setRecordTitle(String recordTitle) {
        this.recordTitle = recordTitle;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
