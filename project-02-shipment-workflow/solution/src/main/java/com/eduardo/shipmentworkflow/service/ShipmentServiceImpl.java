package com.eduardo.shipmentworkflow.service;

import com.eduardo.shipmentworkflow.exception.DuplicateTrackingCodeException;
import com.eduardo.shipmentworkflow.exception.InvalidShipmentTransitionException;
import com.eduardo.shipmentworkflow.exception.ShipmentDeletionNotAllowedException;
import com.eduardo.shipmentworkflow.exception.ShipmentNotFoundException;
import com.eduardo.shipmentworkflow.model.Shipment;
import com.eduardo.shipmentworkflow.model.ShipmentStatus;
import com.eduardo.shipmentworkflow.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;

    public ShipmentServiceImpl(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    public Shipment create(Shipment shipment) {
        if (shipmentRepository.existsByTrackingCode(shipment.getTrackingCode())) {
            throw new DuplicateTrackingCodeException(shipment.getTrackingCode());
        }

        // El estado inicial lo controla el backend, no el JSON recibido.
        shipment.setId(null);
        shipment.setStatus(ShipmentStatus.REGISTERED);
        shipment.setCreatedAt(null);
        shipment.setUpdatedAt(null);
        return shipmentRepository.save(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Shipment> findAll() {
        return shipmentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Shipment findById(Long id) {
        return getExisting(id);
    }

    @Override
    public Shipment update(Long id, Shipment incoming) {
        Shipment existing = getExisting(id);

        // En este módulo todavía no usamos DTOs. Por eso copiamos expresamente
        // solo los campos que el negocio permite modificar.
        existing.setRecipientName(incoming.getRecipientName());
        existing.setDestinationCity(incoming.getDestinationCity());
        existing.setParcelType(incoming.getParcelType());
        existing.setSpecialInstructions(incoming.getSpecialInstructions());

        return shipmentRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        Shipment shipment = getExisting(id);
        if (shipment.getStatus() != ShipmentStatus.CANCELLED) {
            throw new ShipmentDeletionNotAllowedException(id, shipment.getStatus());
        }
        shipmentRepository.delete(shipment);
    }

    @Override
    public Shipment dispatch(Long id) {
        Shipment shipment = getExisting(id);
        requireStatus(shipment, ShipmentStatus.REGISTERED, ShipmentStatus.IN_TRANSIT);
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        return shipmentRepository.save(shipment);
    }

    @Override
    public Shipment markOutForDelivery(Long id) {
        Shipment shipment = getExisting(id);
        requireStatus(shipment, ShipmentStatus.IN_TRANSIT, ShipmentStatus.OUT_FOR_DELIVERY);
        shipment.setStatus(ShipmentStatus.OUT_FOR_DELIVERY);
        return shipmentRepository.save(shipment);
    }

    @Override
    public Shipment deliver(Long id) {
        Shipment shipment = getExisting(id);
        requireStatus(shipment, ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.DELIVERED);
        shipment.setStatus(ShipmentStatus.DELIVERED);
        return shipmentRepository.save(shipment);
    }

    @Override
    public Shipment cancel(Long id) {
        Shipment shipment = getExisting(id);
        requireStatus(shipment, ShipmentStatus.REGISTERED, ShipmentStatus.CANCELLED);
        shipment.setStatus(ShipmentStatus.CANCELLED);
        return shipmentRepository.save(shipment);
    }

    private Shipment getExisting(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException(id));
    }

    private void requireStatus(Shipment shipment, ShipmentStatus required, ShipmentStatus target) {
        if (shipment.getStatus() != required) {
            throw new InvalidShipmentTransitionException(shipment.getId(), shipment.getStatus(), target);
        }
    }
}
