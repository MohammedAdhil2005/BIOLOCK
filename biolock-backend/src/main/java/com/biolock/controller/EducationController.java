package com.biolock.controller;

import com.biolock.model.EducationRecord;
import com.biolock.repository.EducationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/education")
@CrossOrigin
public class EducationController {

    @Autowired
    private EducationRepository educationRepository;

    @PostMapping
    public EducationRecord createEducation(@RequestBody EducationRecord record) {
        return educationRepository.save(record);
    }

    @GetMapping
    public List<EducationRecord> getEducation(@RequestParam String uid) {
        return educationRepository.findByUidOrderByCreatedAtDesc(uid);
    }
}
