package com.project.internship_desk_booking_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaFallbackController {
    @GetMapping(value = "/{path:^(?!api|assets|static|favicon\\.ico)[^\\.]*}/**")
    public String forwardSpaRequests() {
        return "forward:/";
    }
}