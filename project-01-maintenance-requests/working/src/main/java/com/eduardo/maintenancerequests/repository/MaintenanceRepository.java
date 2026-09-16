package com.eduardo.maintenancerequests.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eduardo.maintenancerequests.model.MaintenanceCategory;
import com.eduardo.maintenancerequests.model.MaintenanceRequest;
import com.eduardo.maintenancerequests.model.Priority;

@Repository
public interface MaintenanceRepository extends JpaRepository<MaintenanceRequest, Long>{
	
	public List<MaintenanceRequest> findAllByOrderByCreatedAtDesc();
	
	public List<MaintenanceRequest> findByCategory(MaintenanceCategory category);
	
	public List<MaintenanceRequest> findByPriority(Priority priority);
	
	public List<MaintenanceRequest> findByTitleContainingIgnoreCase(String phase);

}
