package com.fooddelivery.demo.client.catalog;

import com.fooddelivery.demo.exception.CatalogNotFoundException;
import com.fooddelivery.demo.exception.CatalogUnavailableException;
import com.fooddelivery.demo.exception.MealUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.UUID;

@Service
public class CatalogClient {

    private final RestClient restClient;
    //est une classe de Spring Framework qui permet d'envoyer des requêtes HTTP vers d'autres applications ou microservices et de récupérer leurs réponses.

    public CatalogClient(
            RestClient.Builder builder,
            @Value("${services.catalog.url}") String catalogUrl,
            @Value("${services.catalog.connect-timeout-ms:3000}") long connectTimeoutMs,
            @Value("${services.catalog.read-timeout-ms:5000}") long readTimeoutMs
    ) {
        this.restClient = builder
                .requestFactory(createRequestFactory(connectTimeoutMs, readTimeoutMs))
                .baseUrl(catalogUrl)
                .build();
    }

    private static SimpleClientHttpRequestFactory createRequestFactory(
            long connectTimeoutMs,
            long readTimeoutMs
    ) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) connectTimeoutMs);
        factory.setReadTimeout((int) readTimeoutMs);
        return factory;
    }

    public CatalogRestaurantResponse getRestaurant(UUID restaurantId) {

        try {

            CatalogRestaurantResponse response = restClient
                    .get()
                    .uri("/api/v1/restaurants/{id}", restaurantId)
                    .retrieve()
                    .body(CatalogRestaurantResponse.class);

            if (response == null) {
                throw new CatalogUnavailableException(
                        "Empty response from Catalog Service"
                );
            }

            return response;

        } catch (RestClientResponseException ex) {

            if (ex.getStatusCode().value() == 404) {
                throw new CatalogNotFoundException(
                        "Restaurant not found: " + restaurantId
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
