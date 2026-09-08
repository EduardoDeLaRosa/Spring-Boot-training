package com.eduardo.freelancebilling.repository;

import com.eduardo.freelancebilling.model.BillingRecord;
import com.eduardo.freelancebilling.model.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BillingRecordRepository extends JpaRepository<BillingRecord, Long> {

    List<BillingRecord> findByClientNameContainingIgnoreCaseOrderByWorkDateDesc(String clientName);

    List<BillingRecord> findByServiceCategoryOrderByWorkDateDesc(ServiceCategory serviceCategory);

    List<BillingRecord> findByProjectCodeIgnoreCaseOrderByWorkDateDesc(String projectCode);

    List<BillingRecord> findByWorkDateBetweenOrderByWorkDateAsc(LocalDate startDate, LocalDate endDate);
}
