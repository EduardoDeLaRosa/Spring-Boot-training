package com.eduardo.shipmentworkflow.service;

import com.eduardo.shipmentworkflow.model.Shipment;

import java.util.List;

public interface ShipmentService {
    Shipment create(Shipment shipment);
    List<Shipment> findAll();
    Shipment findById(Long id);
    Shipment update(Long id, Shipment shipment);
    void delete(Long id);
    Shipment dispatch(Long id);
    Shipment markOutForDelivery(Long id);
    Shipment deliver(Long id);
    Shipment cancel(Long id);
}
