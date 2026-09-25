package com.example.be.security;

import com.example.be.config.AppProperties;
import com.example.be.entity.Role;
import com.example.be.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtService {

    /** Claim con la lista dei ruoli, mappato in ROLE_* da {@link SecurityConfig}. */
    public static final String ROLE_CLAIM = "role";

    private final JwtEncoder jwtEncoder;
    private final AppProperties props;

    public TokenJwt generaToken(User user) {
        Instant now = Instant.now();
        Instant scadenza = now.plus(props.jwt().expiration());

        List<String> ruoli = user.getRoles().stream().map(Role::getRole).sorted().toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtConfig.ISSUER)
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(scadenza)
                .claim("username", user.getUsername())
                .claim(ROLE_CLAIM, ruoli)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenJwt(token, scadenza);
    }

    public record TokenJwt(String token, Instant expiresAt) {
    }
}
