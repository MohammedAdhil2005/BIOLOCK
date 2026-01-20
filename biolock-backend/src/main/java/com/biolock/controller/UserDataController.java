package com.biolock.controller;

import com.biolock.model.UserData;
import com.biolock.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class UserDataController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/save-user-data")
    public String saveUserData(@RequestBody UserData data) {
        userRepository.save(data);
        return "Data saved";
    }

    @GetMapping("/get-user-data")
    public List<UserData> getUserData(@RequestParam String uid) {
        return userRepository.findByUid(uid);
    }
}
