package com.eduardo.maintenancerequests.exception;

public class MaintenanceRequestNotFoundException extends RuntimeException {

    public MaintenanceRequestNotFoundException(Long id) {
        super("No existe una solicitud de mantenimiento con id " + id);
    }
}
