package com.example.be.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Catalogo iniziale da auto.dev (CatalogSeeder). Disattivato di default: ogni avvio con enabled=true
 * consuma chiamate della quota auto.dev, ma solo se il catalogo ha meno di {@code size} annunci.
 *
 * @param marche       filtro vehicle.make di auto.dev, separato da virgole
 * @param fasciaPrezzo filtro retailListing.price nel formato "min-max" (dollari)
 */
@ConfigurationProperties(prefix = "app.catalog-seed")
public record CatalogSeedProperties(boolean enabled, int size, String marche, String fasciaPrezzo) {
}
