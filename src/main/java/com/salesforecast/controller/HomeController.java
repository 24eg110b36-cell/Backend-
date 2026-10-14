package com.salesforecast.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
            "status", "UP",
            "message", "Sales Forecast Backend is running!",
            "endpoints", List.of(
                "/api/dashboard/summary",
                "/api/products",
                "/api/sales",
                "/api/forecast?productId=1&months=6"
            )
        );
    }
}
