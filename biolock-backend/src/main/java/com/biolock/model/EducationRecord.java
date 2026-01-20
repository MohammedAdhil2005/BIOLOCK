package com.biolock.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "education")
public class EducationRecord {
    @Id
    private String id;
    private String uid;
    private String recordId;
    private String institution;
    private String course;
    private String marks;
    private int yearOfPassing;
    private Date createdAt = new Date();

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public String getMarks() { return marks; }
    public void setMarks(String marks) { this.marks = marks; }

    public int getYearOfPassing() { return yearOfPassing; }
    public void setYearOfPassing(int yearOfPassing) { this.yearOfPassing = yearOfPassing; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
