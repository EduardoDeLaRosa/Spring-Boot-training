package com.eduardo.freelancebilling.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "billing_records")
@EntityListeners(AuditingEntityListener.class)
public class BillingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Client name is required")
    @Size(min = 2, max = 120, message = "Client name must contain between 2 and 120 characters")
    @Column(nullable = false, length = 120)
    private String clientName;

    @NotBlank(message = "Project code is required")
    @Size(min = 3, max = 30, message = "Project code must contain between 3 and 30 characters")
    @Column(nullable = false, length = 30)
    private String projectCode;

    @NotNull(message = "Service category is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ServiceCategory serviceCategory;

    @NotNull(message = "Work date is required")
    @PastOrPresent(message = "Work date cannot be in the future")
    @Column(nullable = false)
    private LocalDate workDate;

    @NotNull(message = "Hours worked is required")
    @DecimalMin(value = "0.01", message = "Hours worked must be greater than 0")
    @DecimalMax(value = "24.00", message = "Hours worked cannot exceed 24")
    @Digits(integer = 2, fraction = 2, message = "Hours worked can have at most 2 decimals")
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal hoursWorked;

    @NotNull(message = "Hourly rate is required")
    @DecimalMin(value = "0.01", message = "Hourly rate must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Hourly rate can have at most 2 decimals")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @NotNull(message = "Discount percent is required")
    @DecimalMin(value = "0.00", message = "Discount percent cannot be negative")
    @DecimalMax(value = "50.00", message = "Discount percent cannot exceed 50")
    @Digits(integer = 2, fraction = 2, message = "Discount percent can have at most 2 decimals")
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal discountPercent;

    @NotNull(message = "Tax percent is required")
    @DecimalMin(value = "0.00", message = "Tax percent cannot be negative")
    @DecimalMax(value = "30.00", message = "Tax percent cannot exceed 30")
    @Digits(integer = 2, fraction = 2, message = "Tax percent can have at most 2 decimals")
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal taxPercent;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal taxableAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public BillingRecord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
    public ServiceCategory getServiceCategory() { return serviceCategory; }
    public void setServiceCategory(ServiceCategory serviceCategory) { this.serviceCategory = serviceCategory; }
    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }
    public BigDecimal getHoursWorked() { return hoursWorked; }
    public void setHoursWorked(BigDecimal hoursWorked) { this.hoursWorked = hoursWorked; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }
    public BigDecimal getTaxPercent() { return taxPercent; }
    public void setTaxPercent(BigDecimal taxPercent) { this.taxPercent = taxPercent; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getTaxableAmount() { return taxableAmount; }
    public void setTaxableAmount(BigDecimal taxableAmount) { this.taxableAmount = taxableAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
