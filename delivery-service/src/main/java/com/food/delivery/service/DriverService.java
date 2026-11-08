
package com.food.delivery.service;

import com.food.delivery.entity.Driver;
import com.food.delivery.repository.DriverRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public Driver ajouterDriver(Driver driver) {
        return driverRepository.save(driver);
    }

    public List<Driver> obtenirTousLesDrivers() {
        return driverRepository.findAll();
    }

    public Driver obtenirDriverParId(Long id) {
        return driverRepository.findById(id).orElse(null);
    }

}
