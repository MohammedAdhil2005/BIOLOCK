package com.biolock.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String redirectToHome() {
        // This will redirect to: src/main/resources/static/biolock-frontend/index.html
        return "redirect:/biolock-frontend/index.html";
    }
}
