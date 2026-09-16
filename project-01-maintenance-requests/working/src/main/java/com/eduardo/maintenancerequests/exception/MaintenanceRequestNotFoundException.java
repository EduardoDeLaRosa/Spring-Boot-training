package com.eduardo.maintenancerequests.exception;

public class MaintenanceRequestNotFoundException extends RuntimeException{
	
	public MaintenanceRequestNotFoundException(Long id){
		super("No existe un producto con el id: " + id);
	}

}
