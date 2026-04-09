package com.example.orderserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TotalPageController {

    @GetMapping("/total")
    public String totalPage() {
        return "forward:/total.html";
    }
}
