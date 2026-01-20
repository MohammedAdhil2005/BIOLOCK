package com.biolock.controller;

import com.biolock.model.EmployeeRecord;
import com.biolock.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/employees")
@CrossOrigin
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @PostMapping
    public EmployeeRecord createEmployee(@RequestBody EmployeeRecord employee) {
        return employeeRepository.save(employee);
    }

    @GetMapping
    public List<EmployeeRecord> getEmployees(@RequestParam String uid) {
        return employeeRepository.findByUidOrderByCreatedAtDesc(uid);
    }
}
