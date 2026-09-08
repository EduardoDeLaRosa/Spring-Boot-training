package com.eduardo.maintenancerequests.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_requests")
@EntityListeners(AuditingEntityListener.class)
public class MaintenanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 5, max = 120, message = "El título debe tener entre 5 y 120 caracteres")
    @Column(nullable = false, length = 120)
    private String title;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 1000, message = "La descripción debe tener entre 10 y 1000 caracteres")
    @Column(nullable = false, length = 1000)
    private String description;

    @NotBlank(message = "El nombre del solicitante es obligatorio")
    @Size(min = 3, max = 80, message = "El nombre del solicitante debe tener entre 3 y 80 caracteres")
    @Column(nullable = false, length = 80)
    private String requesterName;

    @NotBlank(message = "El email del solicitante es obligatorio")
    @Email(message = "El email del solicitante debe tener un formato válido")
    @Size(max = 150, message = "El email del solicitante no puede superar los 150 caracteres")
    @Column(nullable = false, length = 150)
    private String requesterEmail;

    @NotBlank(message = "La ubicación es obligatoria")
    @Size(min = 2, max = 120, message = "La ubicación debe tener entre 2 y 120 caracteres")
    @Column(nullable = false, length = 120)
    private String location;

    @NotNull(message = "La categoría es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MaintenanceCategory category;

    @NotNull(message = "La prioridad es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updatedAt;

    protected MaintenanceRequest() {
        // Constructor requerido por JPA.
    }

    public MaintenanceRequest(String title,
                              String description,
                              String requesterName,
                              String requesterEmail,
                              String location,
                              MaintenanceCategory category,
                              Priority priority) {
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

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
