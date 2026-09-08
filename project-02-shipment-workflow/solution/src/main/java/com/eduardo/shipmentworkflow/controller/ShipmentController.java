package com.eduardo.shipmentworkflow.controller;

import com.eduardo.shipmentworkflow.model.Shipment;
import com.eduardo.shipmentworkflow.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    public ResponseEntity<Shipment> create(@Valid @RequestBody Shipment shipment) {
        Shipment created = shipmentService.create(shipment);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Shipment>> findAll() {
        return ResponseEntity.ok(shipmentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Shipment> findById(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Shipment> update(@PathVariable Long id,
                                           @Valid @RequestBody Shipment shipment) {
        return ResponseEntity.ok(shipmentService.update(id, shipment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        shipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/dispatch")
    public ResponseEntity<Shipment> dispatch(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentService.dispatch(id));
    }

    @PatchMapping("/{id}/out-for-delivery")
    public ResponseEntity<Shipment> markOutForDelivery(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentService.markOutForDelivery(id));
    }

    @PatchMapping("/{id}/deliver")
    public ResponseEntity<Shipment> deliver(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentService.deliver(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Shipment> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentService.cancel(id));
    }
}
