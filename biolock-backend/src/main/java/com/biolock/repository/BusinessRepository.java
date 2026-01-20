package com.biolock.repository;

import com.biolock.model.BusinessRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface BusinessRepository extends MongoRepository<BusinessRecord, String> {
    List<BusinessRecord> findByUidOrderByCreatedAtDesc(String uid);
}
