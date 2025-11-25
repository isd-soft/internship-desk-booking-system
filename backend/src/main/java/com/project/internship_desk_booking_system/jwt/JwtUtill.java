package com.project.internship_desk_booking_system.jwt;

import com.project.internship_desk_booking_system.enums.Role;
import com.project.internship_desk_booking_system.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtUtill {

    @Value("${auth.jwt.secret}")
    private String secret;

    @Value("${auth.jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${auth.jwt.expiration}")
    private long expiration;
    private final UserRepository userRepository;

    private Key signingKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email, Role role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(email)
                .addClaims(Map.of(
                        "email", email,
                        "role", role.name()
                ))
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public Role extractRole(String token) {
        Object r = parseClaims(token).get("role");
        return (r != null) ? Role.valueOf(r.toString()) : null;
    }


    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateRefreshToken(String email) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public Claims validateRefreshToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}