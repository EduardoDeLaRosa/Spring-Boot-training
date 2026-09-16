package com.eduardo.maintenancerequests.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduardo.maintenancerequests.model.MaintenanceCategory;
import com.eduardo.maintenancerequests.model.MaintenanceRequest;
import com.eduardo.maintenancerequests.model.Priority;
import com.eduardo.maintenancerequests.service.MaintenanceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/maintenance-requests")
public class MaintenanceController {

private MaintenanceService maintenanceService;
	
	public MaintenanceController(MaintenanceService maintenanceService) {
		this.maintenanceService = maintenanceService;
	}
	
	@PostMapping()
	public ResponseEntity<MaintenanceRequest> createRequest(@Valid @RequestBody MaintenanceRequest request){
		
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(maintenanceService.createMaintenanceRequest(request));
	}
	
	@GetMapping()
	public ResponseEntity<List<MaintenanceRequest>> listRequests(){
		
		return ResponseEntity.ok(maintenanceService.listAllRequest());
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<MaintenanceRequest> findRequestById(@Valid @PathVariable Long id){
		
		return ResponseEntity.ok(maintenanceService.findById(id));
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<MaintenanceRequest> updateMaintenance(@Valid @PathVariable Long id, 
			@Valid @RequestBody MaintenanceRequest request){
		
		return ResponseEntity.ok(maintenanceService.updateMaintenance(id, request));
		
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<MaintenanceRequest> deleteRequestById(@Valid @PathVariable Long id){
		
		maintenanceService.deleteMaintenance(id);
		
		return ResponseEntity.noContent().build();
	}
	
	/* ---- OPCIONAL ---- */
	@GetMapping("/optional/priority/{priority}")
	public ResponseEntity<List<MaintenanceRequest>> findByPriority(
			@Valid @PathVariable Priority priority){
		
		return ResponseEntity.ok(maintenanceService.findByPriority(priority));
	}
	
	@GetMapping("/optional/category/{category}")
	public ResponseEntity<List<MaintenanceRequest>> findByCategory(
			@Valid @PathVariable MaintenanceCategory category){
		
		return ResponseEntity.ok(maintenanceService.findByCategory(category));
	}
	
	@GetMapping("/optional/count")
	public ResponseEntity<String> countRequest(){
		
		return ResponseEntity.ok(maintenanceService.countRequest());
	}
	
	@GetMapping("/optional/search/{title}")
	public ResponseEntity<List<MaintenanceRequest>> findByTitle(@Valid @PathVariable String title){
		
		return ResponseEntity.ok(maintenanceService.findByTitle(title));
	}
	
}
