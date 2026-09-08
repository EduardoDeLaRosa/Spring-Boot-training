package com.eduardo.freelancebilling.service;

import com.eduardo.freelancebilling.exception.BillingRecordNotFoundException;
import com.eduardo.freelancebilling.exception.InvalidBillingPeriodException;
import com.eduardo.freelancebilling.model.BillingRecord;
import com.eduardo.freelancebilling.model.ServiceCategory;
import com.eduardo.freelancebilling.repository.BillingRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

@Service
@Transactional
public class BillingRecordServiceImpl implements BillingRecordService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private final BillingRecordRepository repository;

    public BillingRecordServiceImpl(BillingRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public BillingRecord create(BillingRecord record) {
        record.setId(null);
        normalize(record);
        calculateAmounts(record);
        return repository.save(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingRecord> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public BillingRecord findById(Long id) {
        return getExisting(id);
    }

    @Override
    public BillingRecord update(Long id, BillingRecord incoming) {
        BillingRecord existing = getExisting(id);

        existing.setClientName(incoming.getClientName());
        existing.setProjectCode(incoming.getProjectCode());
        existing.setServiceCategory(incoming.getServiceCategory());
        existing.setWorkDate(incoming.getWorkDate());
        existing.setHoursWorked(incoming.getHoursWorked());
        existing.setHourlyRate(incoming.getHourlyRate());
        existing.setDiscountPercent(incoming.getDiscountPercent());
        existing.setTaxPercent(incoming.getTaxPercent());

        normalize(existing);
        calculateAmounts(existing);

        return repository.save(existing);
    }

    @Override
    public void delete(Long id) {
        repository.delete(getExisting(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingRecord> findByClient(String clientName) {
        return repository.findByClientNameContainingIgnoreCaseOrderByWorkDateDesc(clientName.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingRecord> findByCategory(ServiceCategory category) {
        return repository.findByServiceCategoryOrderByWorkDateDesc(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingRecord> findByProjectCode(String projectCode) {
        return repository.findByProjectCodeIgnoreCaseOrderByWorkDateDesc(
                projectCode.trim().toUpperCase(Locale.ROOT)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingRecord> findBetween(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        return repository.findByWorkDateBetweenOrderByWorkDateAsc(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> calculateRevenueReport(LocalDate startDate, LocalDate endDate) {
        List<BillingRecord> records = findBetween(startDate, endDate);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("recordCount", records.size());
        report.put("totalHours", sum(records, BillingRecord::getHoursWorked));
        report.put("totalSubtotal", sum(records, BillingRecord::getSubtotal));
        report.put("totalDiscount", sum(records, BillingRecord::getDiscountAmount));
        report.put("totalTax", sum(records, BillingRecord::getTaxAmount));
        report.put("totalRevenue", sum(records, BillingRecord::getTotalAmount));

        return report;
    }

    private BillingRecord getExisting(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BillingRecordNotFoundException(id));
    }

    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new InvalidBillingPeriodException(startDate, endDate);
        }
    }

    private void normalize(BillingRecord record) {
        record.setClientName(record.getClientName().trim());
        record.setProjectCode(record.getProjectCode().trim().toUpperCase(Locale.ROOT));
    }

    private void calculateAmounts(BillingRecord record) {
        BigDecimal subtotal = money(
                record.getHoursWorked().multiply(record.getHourlyRate())
        );

        BigDecimal discountAmount = percentageOf(subtotal, record.getDiscountPercent());
        BigDecimal taxableAmount = money(subtotal.subtract(discountAmount));
        BigDecimal taxAmount = percentageOf(taxableAmount, record.getTaxPercent());
        BigDecimal totalAmount = money(taxableAmount.add(taxAmount));

        record.setSubtotal(subtotal);
        record.setDiscountAmount(discountAmount);
        record.setTaxableAmount(taxableAmount);
        record.setTaxAmount(taxAmount);
        record.setTotalAmount(totalAmount);
    }

    private BigDecimal percentageOf(BigDecimal amount, BigDecimal percent) {
        return money(
                amount.multiply(percent)
                        .divide(ONE_HUNDRED, MONEY_SCALE + 4, MONEY_ROUNDING)
        );
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, MONEY_ROUNDING);
    }

    private BigDecimal sum(
            List<BillingRecord> records,
            Function<BillingRecord, BigDecimal> extractor) {

        return records.stream()
                .map(extractor)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, MONEY_ROUNDING);
    }
}
