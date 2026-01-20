package com.biolock.repository;

import com.biolock.model.RetinaData;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
import java.util.List;

/**
 * Repository interface for accessing RetinaData documents in MongoDB.
 */
public interface RetinaDataRepository extends MongoRepository<RetinaData, String> {

    // Find one record by email
    Optional<RetinaData> findByEmail(String email);

    // Find all records by name (could return multiple)
    List<RetinaData> findByName(String name);

    // Check if a record exists with a specific email
    boolean existsByEmail(String email);
}
