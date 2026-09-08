package com.eduardo.equipmentrental.service;

import com.eduardo.equipmentrental.exception.EquipmentUnavailableException;
import com.eduardo.equipmentrental.exception.InvalidRentalOperationException;
import com.eduardo.equipmentrental.exception.InvalidRentalPeriodException;
import com.eduardo.equipmentrental.exception.RentalContractNotFoundException;
import com.eduardo.equipmentrental.model.EquipmentType;
import com.eduardo.equipmentrental.model.RentalContract;
import com.eduardo.equipmentrental.model.RentalStatus;
import com.eduardo.equipmentrental.repository.RentalContractRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class RentalContractServiceImpl implements RentalContractService {

    private static final EnumSet<RentalStatus> BLOCKING_STATUSES =
            EnumSet.of(RentalStatus.RESERVED, RentalStatus.ACTIVE);

    private static final BigDecimal LATE_SURCHARGE_RATE = new BigDecimal("0.50");
    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private final RentalContractRepository repository;

    public RentalContractServiceImpl(RentalContractRepository repository) {
        this.repository = repository;
    }

    @Override
    public RentalContract create(RentalContract contract) {
        contract.setId(null);
        contract.setContractCode(generateContractCode());
        contract.setStatus(RentalStatus.RESERVED);
        contract.setActualReturnDate(null);
        contract.setLateDays(null);
        contract.setLateFee(null);
        contract.setFinalCost(null);

        normalize(contract);
        validateReservationPeriod(contract.getStartDate(), contract.getExpectedReturnDate());
        ensureEquipmentAvailable(
                contract.getEquipmentCode(),
                contract.getStartDate(),
                contract.getExpectedReturnDate(),
                null
        );

        contract.setEstimatedCost(calculateEstimatedCost(contract));

        return repository.save(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalContract> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public RentalContract findById(Long id) {
        return getExisting(id);
    }

    @Override
    public RentalContract update(Long id, RentalContract incoming) {
        RentalContract existing = getExisting(id);

        if (existing.getStatus() != RentalStatus.RESERVED) {
            throw new InvalidRentalOperationException(
                    "Only RESERVED contracts can be updated"
            );
        }

        existing.setEquipmentCode(incoming.getEquipmentCode());
        existing.setEquipmentName(incoming.getEquipmentName());
        existing.setEquipmentType(incoming.getEquipmentType());
        existing.setCustomerName(incoming.getCustomerName());
        existing.setStartDate(incoming.getStartDate());
        existing.setExpectedReturnDate(incoming.getExpectedReturnDate());
        existing.setDailyRate(incoming.getDailyRate());

        normalize(existing);
        validateReservationPeriod(existing.getStartDate(), existing.getExpectedReturnDate());

        ensureEquipmentAvailable(
                existing.getEquipmentCode(),
                existing.getStartDate(),
                existing.getExpectedReturnDate(),
                existing.getId()
        );

        existing.setEstimatedCost(calculateEstimatedCost(existing));

        return repository.save(existing);
    }

    @Override
    public void delete(Long id) {
        RentalContract existing = getExisting(id);

        if (existing.getStatus() != RentalStatus.CANCELLED) {
            throw new InvalidRentalOperationException(
                    "Only CANCELLED contracts can be deleted"
            );
        }

        repository.delete(existing);
    }

    @Override
    public RentalContract activate(Long id) {
        RentalContract existing = getExisting(id);

        if (existing.getStatus() != RentalStatus.RESERVED) {
            throw new InvalidRentalOperationException(
                    "Only RESERVED contracts can be activated"
            );
        }

        LocalDate today = LocalDate.now();

        if (today.isBefore(existing.getStartDate())) {
            throw new InvalidRentalOperationException(
                    "The rental cannot be activated before its start date"
            );
        }

        if (!today.isBefore(existing.getExpectedReturnDate())) {
            throw new InvalidRentalOperationException(
                    "The reservation period has already expired"
            );
        }

        existing.setStatus(RentalStatus.ACTIVE);

        return repository.save(existing);
    }

    @Override
    public RentalContract cancel(Long id) {
        RentalContract existing = getExisting(id);

        if (existing.getStatus() != RentalStatus.RESERVED) {
            throw new InvalidRentalOperationException(
                    "Only RESERVED contracts can be cancelled"
            );
        }

        if (!LocalDate.now().isBefore(existing.getStartDate())) {
            throw new InvalidRentalOperationException(
                    "A reservation can only be cancelled before its start date"
            );
        }

        existing.setStatus(RentalStatus.CANCELLED);

        return repository.save(existing);
    }

    @Override
    public RentalContract returnRental(Long id, LocalDate returnDate) {
        RentalContract existing = getExisting(id);

        if (existing.getStatus() != RentalStatus.ACTIVE) {
            throw new InvalidRentalOperationException(
                    "Only ACTIVE contracts can be returned"
            );
        }

        validateReturnDate(existing, returnDate);

        long actualDays = Math.max(
                1,
                ChronoUnit.DAYS.between(existing.getStartDate(), returnDate)
        );

        long lateDays = Math.max(
                0,
                ChronoUnit.DAYS.between(existing.getExpectedReturnDate(), returnDate)
        );

        BigDecimal baseCost = money(
                existing.getDailyRate()
                        .multiply(BigDecimal.valueOf(actualDays))
        );

        BigDecimal lateFee = money(
                existing.getDailyRate()
                        .multiply(BigDecimal.valueOf(lateDays))
                        .multiply(LATE_SURCHARGE_RATE)
        );

        BigDecimal finalCost = money(baseCost.add(lateFee));

        existing.setActualReturnDate(returnDate);
        existing.setLateDays(Math.toIntExact(lateDays));
        existing.setLateFee(lateFee);
        existing.setFinalCost(finalCost);
        existing.setStatus(RentalStatus.RETURNED);

        return repository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalContract> findByStatus(RentalStatus status) {
        return repository.findByStatusOrderByStartDateAsc(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalContract> findByEquipmentCode(String equipmentCode) {
        return repository.findByEquipmentCodeIgnoreCaseOrderByStartDateDesc(
                normalizeEquipmentCode(equipmentCode)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalContract> findByEquipmentType(EquipmentType equipmentType) {
        return repository.findByEquipmentTypeOrderByStartDateDesc(equipmentType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalContract> findByCustomer(String customerName) {
        return repository.findByCustomerNameContainingIgnoreCaseOrderByStartDateDesc(
                customerName.trim()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalContract> findOverdue() {
        return repository
                .findByStatusAndExpectedReturnDateBeforeOrderByExpectedReturnDateAsc(
                        RentalStatus.ACTIVE,
                        LocalDate.now()
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> checkAvailability(
            String equipmentCode,
            LocalDate startDate,
            LocalDate endDate) {

        validateGenericPeriod(startDate, endDate);

        String normalizedCode = normalizeEquipmentCode(equipmentCode);

        boolean unavailable =
                repository.existsByEquipmentCodeIgnoreCaseAndStatusInAndStartDateLessThanAndExpectedReturnDateGreaterThan(
                        normalizedCode,
                        BLOCKING_STATUSES,
                        endDate,
                        startDate
                );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("equipmentCode", normalizedCode);
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        response.put("available", !unavailable);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> summary() {
        List<RentalContract> contracts = repository.findAll();

        long reserved = countByStatus(contracts, RentalStatus.RESERVED);
        long active = countByStatus(contracts, RentalStatus.ACTIVE);
        long returned = countByStatus(contracts, RentalStatus.RETURNED);
        long cancelled = countByStatus(contracts, RentalStatus.CANCELLED);

        long overdue = contracts.stream()
                .filter(contract -> contract.getStatus() == RentalStatus.ACTIVE)
                .filter(contract -> contract.getExpectedReturnDate().isBefore(LocalDate.now()))
                .count();

        BigDecimal estimatedOpenValue = contracts.stream()
                .filter(contract ->
                        contract.getStatus() == RentalStatus.RESERVED
                        || contract.getStatus() == RentalStatus.ACTIVE
                )
                .map(RentalContract::getEstimatedCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal returnedRevenue = contracts.stream()
                .filter(contract -> contract.getStatus() == RentalStatus.RETURNED)
                .map(RentalContract::getFinalCost)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalContracts", contracts.size());
        report.put("reserved", reserved);
        report.put("active", active);
        report.put("returned", returned);
        report.put("cancelled", cancelled);
        report.put("overdue", overdue);
        report.put("estimatedOpenValue", money(estimatedOpenValue));
        report.put("returnedRevenue", money(returnedRevenue));

        return report;
    }

    private RentalContract getExisting(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RentalContractNotFoundException(id));
    }

    private void normalize(RentalContract contract) {
        contract.setEquipmentCode(normalizeEquipmentCode(contract.getEquipmentCode()));
        contract.setEquipmentName(contract.getEquipmentName().trim());
        contract.setCustomerName(contract.getCustomerName().trim());
    }

    private String normalizeEquipmentCode(String equipmentCode) {
        return equipmentCode.trim().toUpperCase(Locale.ROOT);
    }

    private String generateContractCode() {
        return "RNT-" + UUID.randomUUID().toString().toUpperCase(Locale.ROOT);
    }

    private void validateReservationPeriod(LocalDate startDate, LocalDate expectedReturnDate) {
        if (startDate.isBefore(LocalDate.now())) {
            throw new InvalidRentalPeriodException(
                    "Start date cannot be in the past"
            );
        }

        validateGenericPeriod(startDate, expectedReturnDate);
    }

    private void validateGenericPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new InvalidRentalPeriodException(
                    "Start date and end date are required"
            );
        }

        if (!endDate.isAfter(startDate)) {
            throw new InvalidRentalPeriodException(
                    "End date must be after start date"
            );
        }
    }

    private void validateReturnDate(RentalContract contract, LocalDate returnDate) {
        if (returnDate == null) {
            throw new InvalidRentalPeriodException("Return date is required");
        }

        if (returnDate.isBefore(contract.getStartDate())) {
            throw new InvalidRentalPeriodException(
                    "Return date cannot be before the rental start date"
            );
        }

        if (returnDate.isAfter(LocalDate.now())) {
            throw new InvalidRentalPeriodException(
                    "Return date cannot be in the future"
            );
        }
    }

    private void ensureEquipmentAvailable(
            String equipmentCode,
            LocalDate startDate,
            LocalDate endDate,
            Long excludedId) {

        boolean unavailable;

        if (excludedId == null) {
            unavailable =
                    repository.existsByEquipmentCodeIgnoreCaseAndStatusInAndStartDateLessThanAndExpectedReturnDateGreaterThan(
                            equipmentCode,
                            BLOCKING_STATUSES,
                            endDate,
                            startDate
                    );
        } else {
            unavailable =
                    repository.existsByEquipmentCodeIgnoreCaseAndStatusInAndStartDateLessThanAndExpectedReturnDateGreaterThanAndIdNot(
                            equipmentCode,
                            BLOCKING_STATUSES,
                            endDate,
                            startDate,
                            excludedId
                    );
        }

        if (unavailable) {
            throw new EquipmentUnavailableException(equipmentCode);
        }
    }

    private BigDecimal calculateEstimatedCost(RentalContract contract) {
        long days = ChronoUnit.DAYS.between(
                contract.getStartDate(),
                contract.getExpectedReturnDate()
        );

        return money(
                contract.getDailyRate()
                        .multiply(BigDecimal.valueOf(days))
        );
    }

    private long countByStatus(
            List<RentalContract> contracts,
            RentalStatus status) {

        return contracts.stream()
                .filter(contract -> contract.getStatus() == status)
                .count();
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, MONEY_ROUNDING);
    }
}
