package com.CompraVenta.Backend.Security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(Map.of(), userDetails, expirationMs);
    }
    public String  generateAccessToken(Map<String,Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims,userDetails,expirationMs);
    }
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(Map.of("type","refresh"), userDetails, refreshExpirationMs);

    }
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try{
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername())&& !isTokenExpired(token);

        }catch (JwtException | IllegalArgumentException e){
            log.warn("Token JWT invalido: {}",e.getMessage());
            return false;
        }
    }
    public  boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }
    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        return  claimsResolver.apply(extraxctAllClaims(token));
    }
    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }
    private Claims extraxctAllClaims(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                secret.getBytes(
                        StandardCharsets.UTF_8
                )

        );
    }
}
