package com.eduardo.shipmentworkflow.exception;

import com.eduardo.shipmentworkflow.model.ShipmentStatus;

public class ShipmentDeletionNotAllowedException extends RuntimeException {
    public ShipmentDeletionNotAllowedException(Long id, ShipmentStatus status) {
        super("No se puede eliminar el envío " + id + " mientras esté en estado " + status + ". Debe estar CANCELLED");
    }
}
