package com.biolock.controller;

import com.biolock.model.HealthcareRecord;
import com.biolock.repository.HealthcareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/healthcare")
@CrossOrigin
public class HealthcareController {

    @Autowired
    private HealthcareRepository healthcareRepository;

    @PostMapping
    public HealthcareRecord createHealthcare(@RequestBody HealthcareRecord record) {
        return healthcareRepository.save(record);
    }

    @GetMapping
    public List<HealthcareRecord> getHealthcare(@RequestParam String uid) {
        return healthcareRepository.findByUidOrderByCreatedAtDesc(uid);
    }
}
