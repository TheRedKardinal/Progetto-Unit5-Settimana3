package com.example.be.config;

import com.example.be.service.VinLookupService;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Cache in memoria (si svuota al riavvio), usata per non ripetere chiamate ad auto.dev. */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(VinLookupService.CACHE);
    }
}
