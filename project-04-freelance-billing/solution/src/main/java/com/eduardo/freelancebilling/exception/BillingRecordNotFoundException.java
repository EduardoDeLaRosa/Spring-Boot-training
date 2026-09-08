package com.eduardo.freelancebilling.exception;

public class BillingRecordNotFoundException extends RuntimeException {
    public BillingRecordNotFoundException(Long id) {
        super("Billing record with id " + id + " was not found");
    }
}
