package com.eduardo.maintenancerequests.service;

import com.eduardo.maintenancerequests.exception.MaintenanceRequestNotFoundException;
import com.eduardo.maintenancerequests.model.MaintenanceRequest;
import com.eduardo.maintenancerequests.repository.MaintenanceRequestRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MaintenanceRequestServiceImpl implements MaintenanceRequestService {

    private final MaintenanceRequestRepository repository;

    public MaintenanceRequestServiceImpl(MaintenanceRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    public MaintenanceRequest create(MaintenanceRequest request) {
        return repository.save(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceRequest> findAll() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceRequest findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new MaintenanceRequestNotFoundException(id));
    }

    @Override
    public MaintenanceRequest update(Long id, MaintenanceRequest request) {
        MaintenanceRequest existing = findById(id);

        // Actualizamos la entidad ya gestionada por JPA. No sustituimos id ni timestamps.
        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setRequesterName(request.getRequesterName());
        existing.setRequesterEmail(request.getRequesterEmail());
        existing.setLocation(request.getLocation());
        existing.setCategory(request.getCategory());
        existing.setPriority(request.getPriority());

        // save() hace explícita la intención. Al estar la entidad gestionada,
        // JPA también detectaría los cambios mediante dirty checking al cerrar la transacción.
        return repository.save(existing);
    }

    @Override
    public void delete(Long id) {
        MaintenanceRequest existing = findById(id);
        repository.delete(existing);
    }

}
