package com.example.be.repository;

import com.example.be.entity.Favorite;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    @EntityGraph(attributePaths = "car")
    List<Favorite> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    @EntityGraph(attributePaths = "car")
    Optional<Favorite> findByUserIdAndCarId(UUID userId, UUID carId);

    /** Preferiti da notificare: soglia impostata, prezzo sceso sotto la soglia, non ancora notificati. */
    @Query("""
            select f from Favorite f
            join fetch f.user
            join fetch f.car
            where f.car.id = :carId
              and f.sogliaPrezzo is not null
              and :prezzo < f.sogliaPrezzo
              and f.notificatoAt is null
            """)
    List<Favorite> findDaNotificare(@Param("carId") UUID carId, @Param("prezzo") BigDecimal prezzo);

    /** Il prezzo è tornato pari o sopra la soglia: la notifica potrà ripartire se riscende. */
    @Modifying
    @Query("""
            update Favorite f set f.notificatoAt = null
            where f.car.id = :carId
              and f.notificatoAt is not null
              and :prezzo >= f.sogliaPrezzo
            """)
    int resetNotifiche(@Param("carId") UUID carId, @Param("prezzo") BigDecimal prezzo);
}
