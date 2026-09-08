package com.eduardo.vehicleservice.service;

import com.eduardo.vehicleservice.model.ServiceRecord;
import com.eduardo.vehicleservice.model.ServiceType;

import java.time.LocalDate;
import java.util.List;

public interface ServiceRecordService {

    ServiceRecord create(ServiceRecord serviceRecord);

    List<ServiceRecord> findAll();

    ServiceRecord findById(Long id);

    ServiceRecord update(Long id, ServiceRecord serviceRecord);

    void delete(Long id);

    List<ServiceRecord> findByVehiclePlate(String vehiclePlate);

    List<ServiceRecord> findByServiceType(ServiceType serviceType);

    List<ServiceRecord> findBetween(LocalDate startDate, LocalDate endDate);

    List<ServiceRecord> findDueUntil(LocalDate untilDate);

    ServiceRecord findLatestForVehicle(String vehiclePlate);
}
