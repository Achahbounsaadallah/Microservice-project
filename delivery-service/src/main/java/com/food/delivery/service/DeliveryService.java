
package com.food.delivery.service;

import com.food.delivery.entity.Delivery;
import com.food.delivery.entity.Driver;

import com.food.delivery.exception.LivraisonIntrouvableException;
import com.food.delivery.exception.LivreurIndisponibleException;

import com.food.delivery.repository.DeliveryRepository;
import com.food.delivery.repository.DriverRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DriverRepository driverRepository;

    // Constructeur
    public DeliveryService(
            DeliveryRepository deliveryRepository,
            DriverRepository driverRepository) {

        this.deliveryRepository = deliveryRepository;
        this.driverRepository = driverRepository;
    }

    // 1. Ajouter une livraison
    @Transactional
    public Delivery ajouterDelivery(Delivery delivery) {

        // Vérifier si l'identifiant de commande est fourni
        if (delivery.getOrderId() == null) {
            throw new IllegalArgumentException(
                "L'identifiant de commande est obligatoire"
            );
        }

        // Vérifier si l'adresse est absente ou vide
        if (delivery.getDeliveryAddress() == null ||
            delivery.getDeliveryAddress().isBlank()) {

            throw new IllegalArgumentException(
                "L'adresse de livraison est obligatoire"
            );
        }

        // Rechercher un livreur disponible
        Driver driver = driverRepository
            .findFirstByAvailableTrueOrderByIdAsc()
            .orElseThrow(() ->
                new LivreurIndisponibleException(
                    "Aucun livreur disponible"
                )
            );

        // Associer le livreur à la livraison
        delivery.setDriverId(driver.getId());

        // Définir le statut initial
        delivery.setStatus("ASSIGNED");

        // Rendre le livreur indisponible
        driver.setAvailable(false);
        driverRepository.save(driver);

        // Enregistrer la livraison
        return deliveryRepository.save(delivery);
    }

    // 2. Modifier le statut d'une livraison
    @Transactional
    public Delivery modifierStatut(Long id, String nouveauStatut) {

        // Rechercher la livraison
        Delivery delivery = deliveryRepository.findById(id)
            .orElseThrow(() ->
                new LivraisonIntrouvableException(
                    "Livraison introuvable"
                )
            );

        // Récupérer le statut actuel
        String statutActuel = delivery.getStatus();

        // Vérifier si le changement est autorisé
        boolean changementValide =
            ("ASSIGNED".equals(statutActuel)
                && "PICKED_UP".equals(nouveauStatut))
            ||
            ("PICKED_UP".equals(statutActuel)
                && "DELIVERED".equals(nouveauStatut));

        if (!changementValide) {
            throw new IllegalArgumentException(
                "Changement de statut non autorisé"
            );
        }

        // Modifier le statut
        delivery.setStatus(nouveauStatut);

        // Si la livraison est terminée
        if ("DELIVERED".equals(nouveauStatut)) {

            Driver driver = driverRepository
                .findById(delivery.getDriverId())
                .orElseThrow(() ->
                    new IllegalStateException(
                        "Livreur introuvable"
                    )
                );

            // Rendre le livreur disponible
            driver.setAvailable(true);
            driverRepository.save(driver);
        }

        // Enregistrer la modification
        return deliveryRepository.save(delivery);
    }

    // 3. Récupérer toutes les livraisons
    public List<Delivery> obtenirToutesLesDeliveries() {
        return deliveryRepository.findAll();
    }

    // 4. Récupérer une livraison par ID
    public Delivery obtenirDeliveryParId(Long id) {

        return deliveryRepository.findById(id)
            .orElseThrow(() ->
                new LivraisonIntrouvableException(
                    "Livraison introuvable"
                )
            );
    }
}
