package com.eduardo.vehicleservice.service;

import com.eduardo.vehicleservice.exception.InvalidDateRangeException;
import com.eduardo.vehicleservice.exception.InvalidNextServiceDateException;
import com.eduardo.vehicleservice.exception.ServiceRecordNotFoundException;
import com.eduardo.vehicleservice.model.ServiceRecord;
import com.eduardo.vehicleservice.model.ServiceType;
import com.eduardo.vehicleservice.repository.ServiceRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class ServiceRecordServiceImpl implements ServiceRecordService {

    private final ServiceRecordRepository serviceRecordRepository;

    public ServiceRecordServiceImpl(ServiceRecordRepository serviceRecordRepository) {
        this.serviceRecordRepository = serviceRecordRepository;
    }

    @Override
    public ServiceRecord create(ServiceRecord serviceRecord) {
        normalize(serviceRecord);
        validateNextServiceDate(serviceRecord.getServiceDate(), serviceRecord.getNextServiceDate());

        // El servidor controla estos campos, aunque el cliente intente enviarlos.
        serviceRecord.setId(null);
        serviceRecord.setCreatedAt(null);
        serviceRecord.setUpdatedAt(null);

        return serviceRecordRepository.save(serviceRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRecord> findAll() {
        return serviceRecordRepository.findAllByOrderByServiceDateDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRecord findById(Long id) {
        return getExistingRecord(id);
    }

    @Override
    public ServiceRecord update(Long id, ServiceRecord incoming) {
        ServiceRecord existing = getExistingRecord(id);

        validateNextServiceDate(incoming.getServiceDate(), incoming.getNextServiceDate());

        existing.setVehiclePlate(normalizePlate(incoming.getVehiclePlate()));
        existing.setServiceType(incoming.getServiceType());
        existing.setServiceDate(incoming.getServiceDate());
        existing.setMileage(incoming.getMileage());
        existing.setWorkshopName(normalizeText(incoming.getWorkshopName()));
        existing.setNotes(normalizeNullableText(incoming.getNotes()));
        existing.setNextServiceDate(incoming.getNextServiceDate());

        return serviceRecordRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        ServiceRecord existing = getExistingRecord(id);
        serviceRecordRepository.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRecord> findByVehiclePlate(String vehiclePlate) {
        return serviceRecordRepository.findByVehiclePlateIgnoreCaseOrderByServiceDateDesc(
                normalizePlate(vehiclePlate)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRecord> findByServiceType(ServiceType serviceType) {
        return serviceRecordRepository.findByServiceTypeOrderByServiceDateDesc(serviceType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRecord> findBetween(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        return serviceRecordRepository.findByServiceDateBetweenOrderByServiceDateDesc(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRecord> findDueUntil(LocalDate untilDate) {
        LocalDate today = LocalDate.now();
        validateDateRange(today, untilDate);
        return serviceRecordRepository.findByNextServiceDateBetweenOrderByNextServiceDateAsc(today, untilDate);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRecord findLatestForVehicle(String vehiclePlate) {
        String normalizedPlate = normalizePlate(vehiclePlate);

        return serviceRecordRepository
                .findFirstByVehiclePlateIgnoreCaseOrderByServiceDateDescMileageDesc(normalizedPlate)
                .orElseThrow(() -> new ServiceRecordNotFoundException(
                        "No existen mantenimientos registrados para el vehículo " + normalizedPlate
                ));
    }

    private ServiceRecord getExistingRecord(Long id) {
        return serviceRecordRepository.findById(id)
                .orElseThrow(() -> new ServiceRecordNotFoundException(
                        "No existe un registro de mantenimiento con id " + id
                ));
    }

    private void validateNextServiceDate(LocalDate serviceDate, LocalDate nextServiceDate) {
        if (serviceDate != null && nextServiceDate != null && !nextServiceDate.isAfter(serviceDate)) {
            throw new InvalidNextServiceDateException(
                    "La próxima revisión debe ser posterior a la fecha del mantenimiento"
            );
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new InvalidDateRangeException("Las dos fechas del rango son obligatorias");
        }

        if (startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException(
                    "La fecha inicial no puede ser posterior a la fecha final"
            );
        }
    }

    private void normalize(ServiceRecord record) {
        record.setVehiclePlate(normalizePlate(record.getVehiclePlate()));
        record.setWorkshopName(normalizeText(record.getWorkshopName()));
        record.setNotes(normalizeNullableText(record.getNotes()));
    }

    private String normalizePlate(String vehiclePlate) {
        if (vehiclePlate == null) {
            return null;
        }
        return vehiclePlate.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
