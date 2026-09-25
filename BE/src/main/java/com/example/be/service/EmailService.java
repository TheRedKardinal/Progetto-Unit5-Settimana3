package com.example.be.service;

import com.example.be.config.AppProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Invio email HTML generate con i template Thymeleaf in templates/email/.
 * I metodi sono @Async e ricevono valori semplici (non entity): la richiesta HTTP non aspetta l'SMTP
 * e non ci sono problemi di lazy loading fuori dalla transazione.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Locale IT = Locale.ITALY;

    private final JavaMailSender mailSender;
    private final ITemplateEngine templateEngine;
    private final AppProperties props;

    @Async
    public void inviaVerificaEmail(String email, String nome, String token) {
        String link = props.frontendUrl() + "/verifica-email?token=" + token;
        invia(email, "Conferma il tuo indirizzo email", "email/verifica-email",
                Map.of("nome", nome, "link", link));
    }

    @Async
    public void inviaResetPassword(String email, String nome, String token, long minutiValidita) {
        String link = props.frontendUrl() + "/reset-password?token=" + token;
        invia(email, "Reimposta la tua password", "email/reset-password",
                Map.of("nome", nome, "link", link, "minuti", minutiValidita));
    }

    @Async
    public void inviaNotificaPrezzo(String email, String nome, UUID carId, String titoloAuto,
                                    BigDecimal nuovoPrezzo, BigDecimal soglia) {
        String link = props.frontendUrl() + "/auto/" + carId;
        invia(email, "Il prezzo di " + titoloAuto + " è sceso!", "email/price-alert",
                Map.of("nome", nome,
                        "titolo", titoloAuto,
                        "prezzo", euro(nuovoPrezzo),
                        "soglia", euro(soglia),
                        "link", link));
    }

    private void invia(String destinatario, String oggetto, String template, Map<String, Object> variabili) {
        try {
            Context context = new Context(IT, variabili);
            String html = templateEngine.process(template, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(props.mailFrom(), "Salone Auto");
            helper.setTo(destinatario);
            helper.setSubject(oggetto);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Email '{}' inviata a {}", template, destinatario);
        } catch (MessagingException | MailException | UnsupportedEncodingException e) {
            // Metodo asincrono: l'errore non può tornare al client, lo registriamo
            log.error("Invio email '{}' a {} fallito: {}", template, destinatario, e.getMessage());
        }
    }

    private static String euro(BigDecimal valore) {
        return NumberFormat.getCurrencyInstance(IT).format(valore);
    }
}
