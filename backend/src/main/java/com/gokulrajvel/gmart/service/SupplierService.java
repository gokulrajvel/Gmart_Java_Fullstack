package com.gokulrajvel.gmart.service;

import com.gokulrajvel.gmart.data.dto.Supplier;
import com.gokulrajvel.gmart.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public Supplier saveSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    public Optional<Supplier> updateSupplier(int id, Supplier updatedSupplier) {
        return supplierRepository.findById(id).map(existingSupplier -> {
            existingSupplier.setName(updatedSupplier.getName());
            existingSupplier.setContactInfo(updatedSupplier.getContactInfo());
            return supplierRepository.save(existingSupplier);
        });
    }
}
