package com.eduardo.freelancebilling.exception;

import java.time.LocalDate;

public class InvalidBillingPeriodException extends RuntimeException {
    public InvalidBillingPeriodException(LocalDate startDate, LocalDate endDate) {
        super("Start date " + startDate + " cannot be after end date " + endDate);
    }
}
