package com.biolock.controller;

import com.biolock.model.BusinessRecord;
import com.biolock.repository.BusinessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/businesses")
@CrossOrigin
public class BusinessController {

    @Autowired
    private BusinessRepository businessRepository;

    @PostMapping
    public BusinessRecord createBusiness(@RequestBody BusinessRecord business) {
        return businessRepository.save(business);
    }

    @GetMapping
    public List<BusinessRecord> getBusinesses(@RequestParam String uid) {
        return businessRepository.findByUidOrderByCreatedAtDesc(uid);
    }
}
