package com.eduardo.equipmentrental.repository;

import com.eduardo.equipmentrental.model.EquipmentType;
import com.eduardo.equipmentrental.model.RentalContract;
import com.eduardo.equipmentrental.model.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface RentalContractRepository extends JpaRepository<RentalContract, Long> {

    List<RentalContract> findByStatusOrderByStartDateAsc(RentalStatus status);

    List<RentalContract> findByEquipmentCodeIgnoreCaseOrderByStartDateDesc(String equipmentCode);

    List<RentalContract> findByEquipmentTypeOrderByStartDateDesc(EquipmentType equipmentType);

    List<RentalContract> findByCustomerNameContainingIgnoreCaseOrderByStartDateDesc(String customerName);

    List<RentalContract> findByStatusAndExpectedReturnDateBeforeOrderByExpectedReturnDateAsc(
            RentalStatus status,
            LocalDate date
    );

    boolean existsByEquipmentCodeIgnoreCaseAndStatusInAndStartDateLessThanAndExpectedReturnDateGreaterThan(
            String equipmentCode,
            Collection<RentalStatus> statuses,
            LocalDate requestedEndDate,
            LocalDate requestedStartDate
    );

    boolean existsByEquipmentCodeIgnoreCaseAndStatusInAndStartDateLessThanAndExpectedReturnDateGreaterThanAndIdNot(
            String equipmentCode,
            Collection<RentalStatus> statuses,
            LocalDate requestedEndDate,
            LocalDate requestedStartDate,
            Long excludedId
    );
}
