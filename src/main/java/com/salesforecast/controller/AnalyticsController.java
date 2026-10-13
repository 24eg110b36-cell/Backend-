package com.salesforecast.controller;

import com.salesforecast.service.DashboardService;
import com.salesforecast.service.ForecastService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AnalyticsController {
    private final DashboardService dashboard;
    private final ForecastService forecast;

    public AnalyticsController(DashboardService dashboard, ForecastService forecast) {
        this.dashboard = dashboard;
        this.forecast = forecast;
    }

    @GetMapping("/dashboard/summary")
    public Map<String, Object> summary() { return dashboard.summary(); }

    @GetMapping("/forecast")
    public Map<String, Object> forecast(@RequestParam(required = false) Long productId,
                                        @RequestParam(defaultValue = "6") int months) {
        return forecast.forecast(productId, months);
    }
}
