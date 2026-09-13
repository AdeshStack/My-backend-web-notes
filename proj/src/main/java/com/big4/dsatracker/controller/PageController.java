package com.big4.dsatracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Serves the static Thymeleaf pages. All data loading/saving happens client-side
// via /api/progress/{tracker} or /api/misc calls from each page's JS.
@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/dsa")
    public String dsa() {
        return "dsa";
    }

    @GetMapping("/dbms")
    public String dbms() {
        return "dbms";
    }

    @GetMapping("/azure")
    public String azure() {
        return "azure";
    }

    @GetMapping("/java")
    public String java() {
        return "java";
    }

    @GetMapping("/docker")
    public String docker() {
        return "docker";
    }

    @GetMapping("/misc")
    public String misc() {
        return "misc";
    }
}
