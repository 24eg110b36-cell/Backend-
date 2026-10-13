package com.salesforecast.controller;

import com.salesforecast.dto.SalesRequest;
import com.salesforecast.model.SalesRecord;
import com.salesforecast.service.SalesService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SalesController {
    private final SalesService service;

    public SalesController(SalesService service) { this.service = service; }

    @GetMapping
    public List<SalesRecord> all() { return service.findAll(); }

    @PostMapping
    public SalesRecord create(@Valid @RequestBody SalesRequest req) { return service.save(null, req); }

    @PutMapping("/{id}")
    public SalesRecord update(@PathVariable Long id, @Valid @RequestBody SalesRequest req) { return service.save(id, req); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }
}
