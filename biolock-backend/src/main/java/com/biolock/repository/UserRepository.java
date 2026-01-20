package com.biolock.repository;

import com.biolock.model.UserData;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface UserRepository extends MongoRepository<UserData, String> {
    List<UserData> findByUid(String uid);
}
