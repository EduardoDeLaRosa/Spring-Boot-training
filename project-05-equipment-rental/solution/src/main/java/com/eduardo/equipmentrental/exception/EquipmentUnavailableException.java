package com.eduardo.equipmentrental.exception;

public class EquipmentUnavailableException extends RuntimeException {

    public EquipmentUnavailableException(String equipmentCode) {
        super("Equipment " + equipmentCode + " is not available for the requested period");
    }
}
