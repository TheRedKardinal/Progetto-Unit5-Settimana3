package com.example.be.repository;

import com.example.be.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    @EntityGraph(attributePaths = "user")
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /** Invalida i token precedenti quando ne viene richiesto uno nuovo. */
    @Modifying
    @Query("delete from PasswordResetToken t where t.user.id = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);
}
