package com.eduardo.shipmentworkflow.exception;

public class DuplicateTrackingCodeException extends RuntimeException {
    public DuplicateTrackingCodeException(String trackingCode) {
        super("Ya existe un envío con el código de seguimiento " + trackingCode);
    }
}
