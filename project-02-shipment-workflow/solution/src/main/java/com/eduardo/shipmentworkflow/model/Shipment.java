package com.eduardo.shipmentworkflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_shipment_tracking_code", columnNames = "tracking_code")
})
@EntityListeners(AuditingEntityListener.class)
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código de seguimiento es obligatorio")
    @Size(min = 8, max = 20, message = "El código de seguimiento debe tener entre 8 y 20 caracteres")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "El código de seguimiento solo puede contener mayúsculas y números")
    @Column(name = "tracking_code", nullable = false, unique = true, length = 20)
    private String trackingCode;

    @NotBlank(message = "El nombre del destinatario es obligatorio")
    @Size(min = 3, max = 120, message = "El nombre del destinatario debe tener entre 3 y 120 caracteres")
    @Column(name = "recipient_name", nullable = false, length = 120)
    private String recipientName;

    @NotBlank(message = "La ciudad de destino es obligatoria")
    @Size(min = 2, max = 100, message = "La ciudad de destino debe tener entre 2 y 100 caracteres")
    @Column(name = "destination_city", nullable = false, length = 100)
    private String destinationCity;

    @NotNull(message = "El tipo de paquete es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "parcel_type", nullable = false, length = 30)
    private ParcelType parcelType;

    @Size(max = 500, message = "Las instrucciones especiales no pueden superar 500 caracteres")
    @Column(name = "special_instructions", length = 500)
    private String specialInstructions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShipmentStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Shipment() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTrackingCode() { return trackingCode; }
    public void setTrackingCode(String trackingCode) { this.trackingCode = trackingCode; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public String getDestinationCity() { return destinationCity; }
    public void setDestinationCity(String destinationCity) { this.destinationCity = destinationCity; }
    public ParcelType getParcelType() { return parcelType; }
    public void setParcelType(ParcelType parcelType) { this.parcelType = parcelType; }
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    public ShipmentStatus getStatus() { return status; }
    public void setStatus(ShipmentStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
