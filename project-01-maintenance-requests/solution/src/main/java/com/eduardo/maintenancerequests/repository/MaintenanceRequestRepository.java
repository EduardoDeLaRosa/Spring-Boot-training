package com.eduardo.maintenancerequests.repository;

import com.eduardo.maintenancerequests.model.MaintenanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {
}
