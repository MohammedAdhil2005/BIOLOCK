package com.biolock.repository;

import com.biolock.model.CustomRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomRepository extends MongoRepository<CustomRecord, String> {
    List<CustomRecord> findByUidOrderByCreatedAtDesc(String uid);
}
