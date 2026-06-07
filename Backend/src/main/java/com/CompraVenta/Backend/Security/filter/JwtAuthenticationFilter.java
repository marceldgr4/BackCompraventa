package com.CompraVenta.Backend.Security.filter;

import com.CompraVenta.Backend.Security.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private  static final String BLACKLIST_PREFIX = "auth:blacklist:";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final RedisTemplate<String,Object> redisTemplate;


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
            ) throws ServletException, IOException{
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader == null ||  !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(BEARER_PREFIX.length());
        try{
            if(isTokenRevoked(token)){
                log.debug("Token en blacklist rechado para {}",request.getRequestURI());
                filterChain.doFilter(request,response);
                return;
            }
            String username = jwtService.extractUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if(jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }catch (Exception e){
            log.warn("No se pudo authentica con el token JWT : {}",e.getMessage());
        }
        filterChain.doFilter(request, response);
    }
    private boolean isTokenRevoked(String token){
        try{
            return Boolean.TRUE.equals(redisTemplate.hasKey(BEARER_PREFIX+token));
        }catch (Exception e){
            log.warn("redis no disponible para verificar blackList:{}",e.getMessage());
            return false;
        }
    }
}
