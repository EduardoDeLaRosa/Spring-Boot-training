package com.eduardo.equipmentrental.service;

import com.eduardo.equipmentrental.model.EquipmentType;
import com.eduardo.equipmentrental.model.RentalContract;
import com.eduardo.equipmentrental.model.RentalStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface RentalContractService {

    RentalContract create(RentalContract contract);

    List<RentalContract> findAll();

    RentalContract findById(Long id);

    RentalContract update(Long id, RentalContract incoming);

    void delete(Long id);

    RentalContract activate(Long id);

    RentalContract cancel(Long id);

    RentalContract returnRental(Long id, LocalDate returnDate);

    List<RentalContract> findByStatus(RentalStatus status);

    List<RentalContract> findByEquipmentCode(String equipmentCode);

    List<RentalContract> findByEquipmentType(EquipmentType equipmentType);

    List<RentalContract> findByCustomer(String customerName);

    List<RentalContract> findOverdue();

    Map<String, Object> checkAvailability(
            String equipmentCode,
            LocalDate startDate,
            LocalDate endDate
    );

    Map<String, Object> summary();
}
