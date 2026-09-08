package com.eduardo.equipmentrental.exception;

public class RentalContractNotFoundException extends RuntimeException {

    public RentalContractNotFoundException(Long id) {
        super("Rental contract with id " + id + " was not found");
    }
}
