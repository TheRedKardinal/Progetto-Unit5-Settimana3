package com.example.be.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cars")
@Getter
@Setter
@NoArgsConstructor
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 17)
    private String vin;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String modello;

    @Column(nullable = false)
    private Year anno;

    @Column(nullable = false)
    private String titolo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descrizione;

    @Column(nullable = false, precision = 10, scale = 1)
    private BigDecimal chilometraggio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Carburante carburante;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prezzo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Condizione condizione;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatoAnnuncio statoAnnuncio = StatoAnnuncio.BOZZA;

    /** URL delle immagini, nell'ordine del carousel. */
    @ElementCollection
    @CollectionTable(name = "car_immagini", joinColumns = @JoinColumn(name = "car_id"))
    @OrderColumn(name = "posizione")
    @Column(name = "url", nullable = false, length = 1000)
    @BatchSize(size = 50)
    private List<String> immagini = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private Instant publishedAt;

    public boolean isPubblicato() {
        return statoAnnuncio == StatoAnnuncio.PUBBLICATO;
    }
}
