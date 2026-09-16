package com.eduardo.maintenancerequests.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduardo.maintenancerequests.exception.MaintenanceRequestNotFoundException;
import com.eduardo.maintenancerequests.model.MaintenanceCategory;
import com.eduardo.maintenancerequests.model.MaintenanceRequest;
import com.eduardo.maintenancerequests.model.Priority;
import com.eduardo.maintenancerequests.repository.MaintenanceRepository;

@Service
public class MaintenanceService {

	private MaintenanceRepository maintenanceRepository;
	
	public MaintenanceService(MaintenanceRepository maintenanceRepository) {
		this.maintenanceRepository = maintenanceRepository;
	}
	
	@Transactional
	public MaintenanceRequest createMaintenanceRequest(MaintenanceRequest request){
		
		MaintenanceRequest requestMaintenance = new MaintenanceRequest(
				request.getTitle(),
				request.getDescription(),
				request.getRequesterName(),
				request.getRequesterEmail(),
				request.getLocation(),
				request.getCategory(),
				request.getPriority());
		
		return maintenanceRepository.save(requestMaintenance);
	}
	
	@Transactional(readOnly = true)
	public List<MaintenanceRequest> listAllRequest(){
		
		return maintenanceRepository.findAllByOrderByCreatedAtDesc();
	}
	
	@Transactional(readOnly = true)
	public MaintenanceRequest findById(Long id) {
		
		return maintenanceRepository.findById(id).orElseThrow(() -> new MaintenanceRequestNotFoundException(id));
	}
	
	@Transactional
	public MaintenanceRequest updateMaintenance(Long id, MaintenanceRequest request){
		
		MaintenanceRequest maintenance = maintenanceRepository.findById(id).orElseThrow(() -> new MaintenanceRequestNotFoundException(id));
		
		maintenance.setTitle(request.getTitle());
		maintenance.setDescription(request.getDescription());
		maintenance.setRequesterName(request.getRequesterName());
		maintenance.setRequesterEmail(request.getRequesterEmail());
		maintenance.setLocation(request.getLocation());
		maintenance.setCategory(request.getCategory());
		maintenance.setPriority(request.getPriority());
		
		return maintenanceRepository.save(maintenance);
	}
	
	@Transactional
	public void deleteMaintenance(Long id) {
		
		maintenanceRepository.findById(id).orElseThrow(() -> new MaintenanceRequestNotFoundException(id));
		
		maintenanceRepository.deleteById(id);
	}
	
	/* ---- OPCIONAL ---- */
	
	@Transactional(readOnly = true)
	public List<MaintenanceRequest> findByPriority(Priority priority) {
		
		return maintenanceRepository.findByPriority(priority);
	}
	
	@Transactional(readOnly = true)
	public List<MaintenanceRequest> findByCategory(MaintenanceCategory category) {
		
		return maintenanceRepository.findByCategory(category);
	}
	
	@Transactional(readOnly = true)
	public String countRequest() {
		
		return "El número total de solicitudes es de: " + maintenanceRepository.count();
	}
	
	// Indicamos lista, al ser titulo un campo unico sin posibilidad de repetirse
	@Transactional(readOnly = true)
	public List<MaintenanceRequest> findByTitle(String title){
		
		return maintenanceRepository.findByTitleContainingIgnoreCase(title);
		
	}
	
	
	
}
