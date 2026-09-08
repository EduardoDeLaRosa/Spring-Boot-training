package com.eduardo.shipmentworkflow.exception;

import com.eduardo.shipmentworkflow.model.ShipmentStatus;

public class InvalidShipmentTransitionException extends RuntimeException {
    public InvalidShipmentTransitionException(Long id, ShipmentStatus current, ShipmentStatus target) {
        super("El envío " + id + " no puede pasar de " + current + " a " + target);
    }
}
