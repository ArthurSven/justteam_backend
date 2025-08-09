package com.devapps.just_team_backend.security;

import jakarta.annotation.PostConstruct;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class JwtService {

    private Key signInKey;

    @PostConstruct
    public void init() {
        String secretKey = "H7AAPJLcUMnJeJcznRsUtgFoZT_8Fc2Cmy8AX04T52Y="; // Get from environment variable

        try {
            byte[] keyBytes = Decoders.BASE64URL.decode(secretKey); // Decode the key
            signInKey = Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) { // Handle potential decoding exceptions
            throw new RuntimeException("Error decoding JWT secret key (invalid Base64URL): " + e.getMessage(), e); // Wrap and rethrow
        }
    }

    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(signInKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String createToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(signInKey, SignatureAlgorithm.HS256)
                .compact();
    }



    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            // Log the exception (e.g., malformed token, invalid signature)
            return false;
        }
    }

}

