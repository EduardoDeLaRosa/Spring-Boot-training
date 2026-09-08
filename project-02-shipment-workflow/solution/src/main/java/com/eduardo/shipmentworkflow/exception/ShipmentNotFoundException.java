package com.eduardo.shipmentworkflow.exception;

public class ShipmentNotFoundException extends RuntimeException {
    public ShipmentNotFoundException(Long id) {
        super("No existe ningún envío con id " + id);
    }
}
