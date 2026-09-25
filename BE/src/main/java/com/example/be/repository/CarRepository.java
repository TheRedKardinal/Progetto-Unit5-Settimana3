package com.example.be.repository;

import com.example.be.entity.Car;
import com.example.be.entity.StatoAnnuncio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CarRepository extends JpaRepository<Car, UUID>, JpaSpecificationExecutor<Car> {

    Optional<Car> findByIdAndStatoAnnuncio(UUID id, StatoAnnuncio statoAnnuncio);

    boolean existsByVin(String vin);
}
