package com.example.be.service;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Esegue un'azione solo dopo il commit della transazione corrente
 * (es. inviare una email solo se i dati sono stati davvero salvati).
 */
public final class AfterCommit {

    private AfterCommit() {
    }

    public static void run(Runnable azione) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            azione.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                azione.run();
            }
        });
    }
}
