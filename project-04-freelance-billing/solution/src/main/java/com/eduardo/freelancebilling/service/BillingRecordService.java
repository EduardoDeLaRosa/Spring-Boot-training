package com.eduardo.freelancebilling.service;

import com.eduardo.freelancebilling.model.BillingRecord;
import com.eduardo.freelancebilling.model.ServiceCategory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BillingRecordService {

    BillingRecord create(BillingRecord record);
    List<BillingRecord> findAll();
    BillingRecord findById(Long id);
    BillingRecord update(Long id, BillingRecord incoming);
    void delete(Long id);

    List<BillingRecord> findByClient(String clientName);
    List<BillingRecord> findByCategory(ServiceCategory category);
    List<BillingRecord> findByProjectCode(String projectCode);
    List<BillingRecord> findBetween(LocalDate startDate, LocalDate endDate);

    Map<String, Object> calculateRevenueReport(LocalDate startDate, LocalDate endDate);
}
