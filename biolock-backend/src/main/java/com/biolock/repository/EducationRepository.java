package com.biolock.repository;

import com.biolock.model.EducationRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface EducationRepository extends MongoRepository<EducationRecord, String> {
    List<EducationRecord> findByUidOrderByCreatedAtDesc(String uid);
}
