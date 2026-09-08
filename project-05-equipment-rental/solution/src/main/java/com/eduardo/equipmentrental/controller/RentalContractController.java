package com.eduardo.equipmentrental.controller;

import com.eduardo.equipmentrental.model.EquipmentType;
import com.eduardo.equipmentrental.model.RentalContract;
import com.eduardo.equipmentrental.model.RentalStatus;
import com.eduardo.equipmentrental.service.RentalContractService;
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
@RequestMapping("/api/rentals")
public class RentalContractController {

    private final RentalContractService service;

    public RentalContractController(RentalContractService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RentalContract> create(
            @Valid @RequestBody RentalContract contract) {

        RentalContract created = service.create(contract);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<List<RentalContract>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalContract> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RentalContract> update(
            @PathVariable Long id,
            @Valid @RequestBody RentalContract contract) {

        return ResponseEntity.ok(service.update(id, contract));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<RentalContract> activate(@PathVariable Long id) {
        return ResponseEntity.ok(service.activate(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<RentalContract> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancel(id));
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<RentalContract> returnRental(
            @PathVariable Long id,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate returnDate) {

        return ResponseEntity.ok(service.returnRental(id, returnDate));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<RentalContract>> findByStatus(
            @PathVariable RentalStatus status) {

        return ResponseEntity.ok(service.findByStatus(status));
    }

    @GetMapping("/equipment/{equipmentCode}")
    public ResponseEntity<List<RentalContract>> findByEquipmentCode(
            @PathVariable String equipmentCode) {

        return ResponseEntity.ok(service.findByEquipmentCode(equipmentCode));
    }

    @GetMapping("/type/{equipmentType}")
    public ResponseEntity<List<RentalContract>> findByEquipmentType(
            @PathVariable EquipmentType equipmentType) {

        return ResponseEntity.ok(service.findByEquipmentType(equipmentType));
    }

    @GetMapping("/customer/{customerName}")
    public ResponseEntity<List<RentalContract>> findByCustomer(
            @PathVariable String customerName) {

        return ResponseEntity.ok(service.findByCustomer(customerName));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<RentalContract>> findOverdue() {
        return ResponseEntity.ok(service.findOverdue());
    }

    @GetMapping("/availability")
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @RequestParam String equipmentCode,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        return ResponseEntity.ok(
                service.checkAvailability(equipmentCode, startDate, endDate)
        );
    }

    @GetMapping("/reports/summary")
    public ResponseEntity<Map<String, Object>> summary() {
        return ResponseEntity.ok(service.summary());
    }
}
