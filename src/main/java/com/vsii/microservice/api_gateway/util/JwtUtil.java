package com.vsii.microservice.api_gateway.util;

import com.nimbusds.jwt.JWTClaimsSet;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    @Value("${jwt.secretKey}")
    private String secretKey;
//
//    public boolean validateToken(String token) throws Exception {
//        String phoneNumber = extractUsername(token);
//        return  !isTokenExpired(token);
//    }
    public boolean isTokenExpired(String token){
        Date exprirationDate = this.extractClaims(token, Claims::getExpiration);
        return exprirationDate.before(new Date());
    }
    private Key getSecretInKey() {
        byte[] bytes = Decoders.BASE64.decode(secretKey);
        //Keys.hmacShaKeyFor(Decoders.BASE64.decode("PstJjnP30Ohm2YKW/bgvGvk80UFeylLdzcbHcH136z4="));
        return Keys.hmacShaKeyFor(bytes);
    }
    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSecretInKey())
                .build()
                .parseClaimsJws(token)// decode token, check valid ?
                .getBody();
    }
    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver){
        final Claims claims = this.extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    public String extractPhoneNumber(String token) throws Exception {
        return extractClaims(token, Claims::getSubject);

    }
    /**
     * Extracts and cleans the JWT token from the Authorization header.
     *
     * @param request The incoming server HTTP request.
     * @return The extracted JWT token.
     */
    public String extractToken(ServerHttpRequest request) {
        String token = request.getHeaders().getOrEmpty("Authorization").get(0);

        // Remove "Bearer " prefix if present
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        return token;
    }
}