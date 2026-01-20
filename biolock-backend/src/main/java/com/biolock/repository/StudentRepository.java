package com.biolock.repository;

import com.biolock.model.StudentRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface StudentRepository extends MongoRepository<StudentRecord, String> {
    List<StudentRecord> findByUidOrderByCreatedAtDesc(String uid);
    List<StudentRecord> findByUidOrderByCreatedAtAsc(String uid);  // ✅ Add this
}
