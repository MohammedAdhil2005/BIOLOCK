package com.biolock.controller;

import com.biolock.model.StudentRecord;
import com.biolock.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/students")
@CrossOrigin
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    @PostMapping
    public StudentRecord createStudent(@RequestBody StudentRecord student) {
        return studentRepository.save(student);
    }

    @GetMapping
    public List<StudentRecord> getStudents(@RequestParam String uid) {
        return studentRepository.findByUidOrderByCreatedAtDesc(uid);
    }
}
