package com.biolock.controller;

import com.biolock.model.*;
import com.biolock.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5500") // adjust your frontend port
@RequestMapping("/api")
public class DataController {

    @Autowired
    private StudentRepository studentRepo;
    @Autowired
    private EmployeeRepository employeeRepo;
    @Autowired
    private BusinessRepository businessRepo;
    @Autowired
    private HealthcareRepository healthcareRepo;
    @Autowired
    private EducationRepository educationRepo;

    // =============================
    // ✅ Student Endpoints
    // =============================
    @GetMapping("/students")
    public List<StudentRecord> getStudents(@RequestParam String uid) {
        return studentRepo.findByUidOrderByCreatedAtDesc(uid);
    }

    @PostMapping("/students")
    public Map<String, Object> saveStudent(@RequestBody StudentRecord student) {
        Map<String, Object> resp = new HashMap<>();
        try {
            studentRepo.save(student);
            resp.put("success", true);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", e.getMessage());
        }
        return resp;
    }

    // =============================
    // ✅ Employee Endpoints
    // =============================
    @GetMapping("/employees")
    public List<EmployeeRecord> getEmployees(@RequestParam String uid) {
        return employeeRepo.findByUidOrderByCreatedAtDesc(uid);
    }

    @PostMapping("/employees")
    public Map<String, Object> saveEmployee(@RequestBody EmployeeRecord emp) {
        Map<String, Object> resp = new HashMap<>();
        try {
            employeeRepo.save(emp);
            resp.put("success", true);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", e.getMessage());
        }
        return resp;
    }

    // =============================
    // ✅ Business Endpoints
    // =============================
    @GetMapping("/business")
    public List<BusinessRecord> getBusiness(@RequestParam String uid) {
        return businessRepo.findByUidOrderByCreatedAtDesc(uid);
    }

    @PostMapping("/business")
    public Map<String, Object> saveBusiness(@RequestBody BusinessRecord business) {
        Map<String, Object> resp = new HashMap<>();
        try {
            businessRepo.save(business);
            resp.put("success", true);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", e.getMessage());
        }
        return resp;
    }

    // =============================
    // ✅ Healthcare Endpoints
    // =============================
    @GetMapping("/healthcare")
    public List<HealthcareRecord> getHealthcare(@RequestParam String uid) {
        return healthcareRepo.findByUidOrderByCreatedAtDesc(uid);
    }

    @PostMapping("/healthcare")
    public Map<String, Object> saveHealthcare(@RequestBody HealthcareRecord record) {
        Map<String, Object> resp = new HashMap<>();
        try {
            healthcareRepo.save(record);
            resp.put("success", true);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", e.getMessage());
        }
        return resp;
    }

    // =============================
    // ✅ Education Endpoints
    // =============================
    @GetMapping("/education")
    public List<EducationRecord> getEducation(@RequestParam String uid) {
        return educationRepo.findByUidOrderByCreatedAtDesc(uid);
    }

    @PostMapping("/education")
    public Map<String, Object> saveEducation(@RequestBody EducationRecord record) {
        Map<String, Object> resp = new HashMap<>();
        try {
            educationRepo.save(record);
            resp.put("success", true);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", e.getMessage());
        }
        return resp;
    }
}
