package com.eduardo.freelancebilling.controller;

import com.eduardo.freelancebilling.model.BillingRecord;
import com.eduardo.freelancebilling.model.ServiceCategory;
import com.eduardo.freelancebilling.service.BillingRecordService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing-records")
public class BillingRecordController {

    private final BillingRecordService service;

    public BillingRecordController(BillingRecordService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<BillingRecord> create(@Valid @RequestBody BillingRecord record) {
        BillingRecord created = service.create(record);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<List<BillingRecord>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillingRecord> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BillingRecord> update(
            @PathVariable Long id,
            @Valid @RequestBody BillingRecord record) {
        return ResponseEntity.ok(service.update(id, record));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/client/{clientName}")
    public ResponseEntity<List<BillingRecord>> findByClient(@PathVariable String clientName) {
        return ResponseEntity.ok(service.findByClient(clientName));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<BillingRecord>> findByCategory(@PathVariable ServiceCategory category) {
        return ResponseEntity.ok(service.findByCategory(category));
    }

    @GetMapping("/project/{projectCode}")
    public ResponseEntity<List<BillingRecord>> findByProject(@PathVariable String projectCode) {
        return ResponseEntity.ok(service.findByProjectCode(projectCode));
    }

    @GetMapping("/between")
    public ResponseEntity<List<BillingRecord>> findBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(service.findBetween(startDate, endDate));
    }

    @GetMapping("/reports/revenue")
    public ResponseEntity<Map<String, Object>> revenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(service.calculateRevenueReport(startDate, endDate));
    }
}
