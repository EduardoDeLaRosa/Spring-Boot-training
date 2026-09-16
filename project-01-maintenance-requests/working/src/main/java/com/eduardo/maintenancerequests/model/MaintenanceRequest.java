package com.eduardo.maintenancerequests.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "maintenance")
public class MaintenanceRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank(message = "El título es obligatorio")
	@Size(min = 5, max = 120)
	private String title;
	
	@NotBlank(message = "La descripción es obligatoria")
	@Size(min = 10, max = 1000)
	private String description;
	
	@NotBlank(message = "El nombre es ogligatorio")
	@Size(min = 3, max = 80)
	private String requesterName;
	
	
	@NotBlank(message = "El email es obligatorio")
	@Email(message = "El email no tiene un formato valido")
	@Size(max = 150)
	private String requesterEmail;
	
	@NotBlank(message = "La localización es obligatoria")
	@Size(min = 2, max = 120)
	private String location;
	
	@Enumerated(EnumType.STRING)
	private MaintenanceCategory category;
	
	@Enumerated(EnumType.STRING)
	private Priority priority;
	
	@CreationTimestamp
	private LocalDateTime createdAt; 
	
	@UpdateTimestamp
	private LocalDateTime updatedAt;
	
	
	public MaintenanceRequest() {}

	public MaintenanceRequest(String title, String description, String requesterName, String requesterEmail,
			String location, MaintenanceCategory category, Priority priority) {
		this.title = title;
		this.description = description;
		this.requesterName = requesterName;
		this.requesterEmail = requesterEmail;
		this.location = location;
		this.category = category;
		this.priority = priority;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRequesterName() {
		return requesterName;
	}

	public void setRequesterName(String requestName) {
		this.requesterName = requestName;
	}

	public String getRequesterEmail() {
		return requesterEmail;
	}

	public void setRequesterEmail(String requesterEmail) {
		this.requesterEmail = requesterEmail;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public MaintenanceCategory getCategory() {
		return category;
	}

	public void setCategory(MaintenanceCategory category) {
		this.category = category;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
	
	
}
