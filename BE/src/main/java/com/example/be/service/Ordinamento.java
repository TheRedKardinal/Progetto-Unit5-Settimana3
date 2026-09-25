package com.example.be.service;

import com.example.be.exception.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

/** Validazione dei campi di ordinamento richiesti dal client (whitelist). */
public final class Ordinamento {

    private Ordinamento() {
    }

    /**
     * Rifiuta i campi non ammessi e aggiunge "id" come criterio finale,
     * così la paginazione è stabile anche con valori uguali (es. stesso prezzo).
     */
    public static Pageable valida(Pageable pageable, Set<String> campiAmmessi) {
        for (Sort.Order order : pageable.getSort()) {
            if (!campiAmmessi.contains(order.getProperty())) {
                throw new BadRequestException("Ordinamento non ammesso: '" + order.getProperty()
                        + "'. Valori consentiti: " + String.join(", ", campiAmmessi.stream().sorted().toList()));
            }
        }
        Sort sort = pageable.getSort().and(Sort.by("id"));
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
