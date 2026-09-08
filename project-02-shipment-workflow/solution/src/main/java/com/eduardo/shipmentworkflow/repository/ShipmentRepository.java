package com.eduardo.shipmentworkflow.repository;

import com.eduardo.shipmentworkflow.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    boolean existsByTrackingCode(String trackingCode);
}
