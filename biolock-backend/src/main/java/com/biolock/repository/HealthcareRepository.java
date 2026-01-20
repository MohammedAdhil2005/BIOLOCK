package com.biolock.repository;

import com.biolock.model.HealthcareRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface HealthcareRepository extends MongoRepository<HealthcareRecord, String> {
    List<HealthcareRecord> findByUidOrderByCreatedAtDesc(String uid);
}
