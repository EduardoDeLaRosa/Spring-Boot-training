package com.eduardo.maintenancerequests.controller;

import com.eduardo.maintenancerequests.model.MaintenanceRequest;
import com.eduardo.maintenancerequests.service.MaintenanceRequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/maintenance-requests")
public class MaintenanceRequestController {

    private final MaintenanceRequestService service;

    public MaintenanceRequestController(MaintenanceRequestService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<MaintenanceRequest> create(
            @Valid @RequestBody MaintenanceRequest request) {

        MaintenanceRequest created = service.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<List<MaintenanceRequest>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceRequest> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaintenanceRequest> update(
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceRequest request) {

        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
