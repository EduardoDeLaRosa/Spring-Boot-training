package com.eduardo.vehicleservice.exception;

public class ServiceRecordNotFoundException extends RuntimeException {

    public ServiceRecordNotFoundException(String message) {
        super(message);
    }
}
