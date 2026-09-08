package com.eduardo.equipmentrental.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "rental_contracts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_rental_contract_code",
                columnNames = "contract_code"
        )
)
@EntityListeners(AuditingEntityListener.class)
public class RentalContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_code", nullable = false, unique = true, updatable = false, length = 50)
    private String contractCode;

    @NotBlank(message = "Equipment code is required")
    @Size(min = 3, max = 30, message = "Equipment code must contain between 3 and 30 characters")
    @Pattern(
            regexp = "^[A-Za-z0-9-]+$",
            message = "Equipment code can only contain letters, numbers and hyphens"
    )
    @Column(nullable = false, length = 30)
    private String equipmentCode;

    @NotBlank(message = "Equipment name is required")
    @Size(min = 2, max = 120, message = "Equipment name must contain between 2 and 120 characters")
    @Column(nullable = false, length = 120)
    private String equipmentName;

    @NotNull(message = "Equipment type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EquipmentType equipmentType;

    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 120, message = "Customer name must contain between 2 and 120 characters")
    @Column(nullable = false, length = 120)
    private String customerName;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date cannot be in the past")
    @Column(nullable = false)
    private LocalDate startDate;

    @NotNull(message = "Expected return date is required")
    @Column(nullable = false)
    private LocalDate expectedReturnDate;

    private LocalDate actualReturnDate;

    @NotNull(message = "Daily rate is required")
    @DecimalMin(value = "0.01", message = "Daily rate must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Daily rate can have at most 2 decimal places")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RentalStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal estimatedCost;

    private Integer lateDays;

    @Column(precision = 12, scale = 2)
    private BigDecimal lateFee;

    @Column(precision = 12, scale = 2)
    private BigDecimal finalCost;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public RentalContract() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContractCode() { return contractCode; }
    public void setContractCode(String contractCode) { this.contractCode = contractCode; }

    public String getEquipmentCode() { return equipmentCode; }
    public void setEquipmentCode(String equipmentCode) { this.equipmentCode = equipmentCode; }

    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }

    public EquipmentType getEquipmentType() { return equipmentType; }
    public void setEquipmentType(EquipmentType equipmentType) { this.equipmentType = equipmentType; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getExpectedReturnDate() { return expectedReturnDate; }
    public void setExpectedReturnDate(LocalDate expectedReturnDate) { this.expectedReturnDate = expectedReturnDate; }

    public LocalDate getActualReturnDate() { return actualReturnDate; }
    public void setActualReturnDate(LocalDate actualReturnDate) { this.actualReturnDate = actualReturnDate; }

    public BigDecimal getDailyRate() { return dailyRate; }
    public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }

    public RentalStatus getStatus() { return status; }
    public void setStatus(RentalStatus status) { this.status = status; }

    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }

    public Integer getLateDays() { return lateDays; }
    public void setLateDays(Integer lateDays) { this.lateDays = lateDays; }

    public BigDecimal getLateFee() { return lateFee; }
    public void setLateFee(BigDecimal lateFee) { this.lateFee = lateFee; }

    public BigDecimal getFinalCost() { return finalCost; }
    public void setFinalCost(BigDecimal finalCost) { this.finalCost = finalCost; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
