package com.biolock.repository;

import com.biolock.model.EmployeeRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface EmployeeRepository extends MongoRepository<EmployeeRecord, String> {
    List<EmployeeRecord> findByUidOrderByCreatedAtDesc(String uid);
}
