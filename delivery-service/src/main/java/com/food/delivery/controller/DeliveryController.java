
package com.food.delivery.controller;

import com.food.delivery.entity.Delivery;
import com.food.delivery.service.DeliveryService;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    // Constructeur
    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    // 1. Ajouter une livraison
    @PostMapping
    public Delivery ajouterDelivery(@RequestBody Delivery delivery) {
        return deliveryService.ajouterDelivery(delivery);
    }

    // 2. Récupérer toutes les livraisons
    @GetMapping
    public List<Delivery> obtenirToutesLesDeliveries() {
        return deliveryService.obtenirToutesLesDeliveries();
    }

    // 3. Récupérer une livraison par ID
    @GetMapping("/{id}")
    public Delivery obtenirDeliveryParId(@PathVariable Long id) {
        return deliveryService.obtenirDeliveryParId(id);
    }

    // 4. Modifier le statut d'une livraison
    @PatchMapping("/{id}/status")
    public Delivery modifierStatut(
            @PathVariable Long id,
            @RequestParam String status) {

        return deliveryService.modifierStatut(id, status);
    }
}
