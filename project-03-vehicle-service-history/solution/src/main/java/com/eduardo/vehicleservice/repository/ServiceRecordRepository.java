package com.eduardo.vehicleservice.repository;

import com.eduardo.vehicleservice.model.ServiceRecord;
import com.eduardo.vehicleservice.model.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {

    List<ServiceRecord> findAllByOrderByServiceDateDesc();

    List<ServiceRecord> findByVehiclePlateIgnoreCaseOrderByServiceDateDesc(String vehiclePlate);

    List<ServiceRecord> findByServiceTypeOrderByServiceDateDesc(ServiceType serviceType);

    List<ServiceRecord> findByServiceDateBetweenOrderByServiceDateDesc(LocalDate startDate, LocalDate endDate);

    List<ServiceRecord> findByNextServiceDateBetweenOrderByNextServiceDateAsc(LocalDate startDate, LocalDate endDate);

    Optional<ServiceRecord> findFirstByVehiclePlateIgnoreCaseOrderByServiceDateDescMileageDesc(String vehiclePlate);
}
