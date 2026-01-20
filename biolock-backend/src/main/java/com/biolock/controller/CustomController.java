package com.biolock.controller;

import com.biolock.model.CustomRecord;
import com.biolock.repository.CustomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("api/custom")
@CrossOrigin(origins = "http://localhost:5500")
public class CustomController {

    @Autowired
    private CustomRepository customRepository;

    // ✅ Create new custom record (returns saved object with id)
    @PostMapping
    public CustomRecord createCustom(@RequestBody CustomRecord record) {
        return customRepository.save(record);
    }

    // ✅ Get custom records by UID
    @GetMapping
    public List<CustomRecord> getCustom(@RequestParam String uid) {
        return customRepository.findByUidOrderByCreatedAtDesc(uid);
    }
}
