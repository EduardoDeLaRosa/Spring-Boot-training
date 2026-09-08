package com.eduardo.maintenancerequests.service;

import com.eduardo.maintenancerequests.model.MaintenanceRequest;

import java.util.List;

public interface MaintenanceRequestService {

    MaintenanceRequest create(MaintenanceRequest request);

    List<MaintenanceRequest> findAll();

    MaintenanceRequest findById(Long id);

    MaintenanceRequest update(Long id, MaintenanceRequest request);

    void delete(Long id);
}
