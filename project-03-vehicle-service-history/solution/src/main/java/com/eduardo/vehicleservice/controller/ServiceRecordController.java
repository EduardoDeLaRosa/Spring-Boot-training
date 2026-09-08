package com.eduardo.vehicleservice.controller;

import com.eduardo.vehicleservice.model.ServiceRecord;
import com.eduardo.vehicleservice.model.ServiceType;
import com.eduardo.vehicleservice.service.ServiceRecordService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/service-records")
public class ServiceRecordController {

    private final ServiceRecordService serviceRecordService;

    public ServiceRecordController(ServiceRecordService serviceRecordService) {
        this.serviceRecordService = serviceRecordService;
    }

    @PostMapping
    public ResponseEntity<ServiceRecord> create(@Valid @RequestBody ServiceRecord serviceRecord) {
        ServiceRecord created = serviceRecordService.create(serviceRecord);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ServiceRecord>> findAll() {
        return ResponseEntity.ok(serviceRecordService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceRecord> findById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceRecordService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceRecord> update(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRecord serviceRecord) {
        return ResponseEntity.ok(serviceRecordService.update(id, serviceRecord));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        serviceRecordService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vehicle/{vehiclePlate}")
    public ResponseEntity<List<ServiceRecord>> findByVehiclePlate(@PathVariable String vehiclePlate) {
        return ResponseEntity.ok(serviceRecordService.findByVehiclePlate(vehiclePlate));
    }

    @GetMapping("/type/{serviceType}")
    public ResponseEntity<List<ServiceRecord>> findByServiceType(@PathVariable ServiceType serviceType) {
        return ResponseEntity.ok(serviceRecordService.findByServiceType(serviceType));
    }

    @GetMapping("/between")
    public ResponseEntity<List<ServiceRecord>> findBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(serviceRecordService.findBetween(startDate, endDate));
    }

    @GetMapping("/due")
    public ResponseEntity<List<ServiceRecord>> findDueUntil(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate untilDate) {
        return ResponseEntity.ok(serviceRecordService.findDueUntil(untilDate));
    }

    @GetMapping("/vehicle/{vehiclePlate}/latest")
    public ResponseEntity<ServiceRecord> findLatestForVehicle(@PathVariable String vehiclePlate) {
        return ResponseEntity.ok(serviceRecordService.findLatestForVehicle(vehiclePlate));
    }
}
