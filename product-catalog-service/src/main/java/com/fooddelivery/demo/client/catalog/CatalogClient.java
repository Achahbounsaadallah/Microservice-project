package com.fooddelivery.demo.client.catalog;

import com.fooddelivery.demo.exception.CatalogUnavailableException;
import com.fooddelivery.demo.exception.MealUnavailableException;
import com.fooddelivery.demo.order.dto.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.UUID;

@Service
public class CatalogClient {

    private final RestClient restClient;

    public CatalogClient(
            RestClient.Builder builder,
            @Value("${services.catalog.url}") String catalogUrl
    ) {
        this.restClient = builder
                .baseUrl(catalogUrl)
                .build();
    }

    public CatalogVariantResponse getVariant(UUID variantId) {

        try {

            CatalogVariantResponse response = restClient
                    .get()
                    .uri("/api/v1/variants/{id}", variantId)
                    .retrieve()
                    .body(CatalogVariantResponse.class);

            if (response == null) {
                throw new CatalogUnavailableException(
                        "Empty response from Catalog Service"
                );
            }

            return response;

        } catch (RestClientResponseException ex) {

            if (ex.getStatusCode().value() == 404) {
                throw new MealUnavailableException(
                        "Meal variant not found: " + variantId
                );
            }

            throw new CatalogUnavailableException(
                    "Catalog Service returned an error"
            );

        } catch (RestClientException ex) {

            throw new CatalogUnavailableException(
                    "Unable to communicate with Catalog Service"
            );
        }
    }
}