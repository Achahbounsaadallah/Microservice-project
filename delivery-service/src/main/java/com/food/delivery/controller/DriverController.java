
package com.food.delivery.controller;

import com.food.delivery.entity.Driver;
import com.food.delivery.service.DriverService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping
    public Driver ajouterDriver(@RequestBody Driver driver) {
        return driverService.ajouterDriver(driver);
    }

    @GetMapping
    public List<Driver> obtenirTousLesDrivers() {
        return driverService.obtenirTousLesDrivers();
    }

    @GetMapping("/{id}")
    public Driver obtenirDriverParId(@PathVariable Long id) {
        return driverService.obtenirDriverParId(id);
    }
}
