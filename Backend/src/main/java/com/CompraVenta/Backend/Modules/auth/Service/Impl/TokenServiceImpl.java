package com.CompraVenta.Backend.Modules.auth.Service.Impl;

import com.CompraVenta.Backend.Exception.custom.BusinessException;
import com.CompraVenta.Backend.Exception.custom.ResourceNotFoundException;
import com.CompraVenta.Backend.Exception.custom.UnauthorizedException;
import com.CompraVenta.Backend.Modules.Employee.Entity.Employee;
import com.CompraVenta.Backend.Modules.Employee.Repository.EmployeeRepository;
import com.CompraVenta.Backend.Modules.auth.Dto.Request.RefreshRequest;
import com.CompraVenta.Backend.Modules.auth.Dto.Response.AuthResponse;
import com.CompraVenta.Backend.Modules.auth.Service.TokenService;
import com.CompraVenta.Backend.Security.model.CustomUserDetails;
import com.CompraVenta.Backend.Security.service.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private static final String BLACKLIST_PREFIX ="auth:blacklist:";
    private static final String REFRESH_PREFIX ="auth:refresh:";

    private final JwtService jwtService;
    private final EmployeeRepository employeeRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Override
    @Transactional
    public AuthResponse refresh(RefreshRequest request){
        String refreshToken = request.refreshToken();

        if(jwtService.isTokenExpired(refreshToken)){
            throw new UnauthorizedException("El refresh token ha expirado. inicie sesion nuevamente");
        }
        String email = jwtService.extractUsername(refreshToken);
        validateRefreshTokenOwnership(refreshToken,email);

        Employee employee = employeeRepository.findByEmailIgnoreCase(email).orElseThrow(
                ()-> new ResourceNotFoundException("empleado no encontrado")
        );
        if(!employee.canAuthenticate()){
            throw new BadCredentialsException("La cuenta esta desactivada");
        }
        rotateRefreshToken(email);
        CustomUserDetails userDetails = buildUserDetails(employee);
        String newAccessToken = jwtService.generateAccessToken(buildExtraClaims(employee), userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);
        storeRefreshToken(email,newRefreshToken);
        log.debug("Refresh token rotado para: {}", email);
        return buildAuthResponse(newAccessToken,newRefreshToken,employee);
    }

    @Override
    public void logout(String accessToken) {
        if(accessToken==null|| accessToken.isBlank()){
            return;
        }
        try {
            String email = jwtService.extractUsername(accessToken);
            long remainigMs =jwtService.getRemainingExpirationMs(accessToken);
            if(remainigMs> 0){
                addTokenToBlacklist(accessToken,remainigMs);
            }
            revokeAlltokensForUser(email);
            log.info("Logout token exitoso para: {}", email);
        }catch (Exception ex){
            log.debug("Logout con token no parseable (aceptable):{}",ex.getMessage());
        }

    }


    @Override
    public void storeRefreshToken(String email, String refreshToken) {
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + email,
                refreshToken,
                refreshExpirationMs,
                TimeUnit.MILLISECONDS);

    }

    @Override
    public void revokeAlltokensForUser(String email) {
        redisTemplate.delete(REFRESH_PREFIX + email);

    }
    private void validateRefreshTokenOwnership(String refreshToken,String email){
        String storedToken =(String)  redisTemplate.opsForValue().get(REFRESH_PREFIX + email);

        if (!refreshToken.equals(storedToken)){
            log.warn("\"Intento de reutilización de refresh token detectado para: {}", email);
            revokeAlltokensForUser(email);
            throw new UnauthorizedException("La refresh token esta invalido");
        }
    }
    private void rotateRefreshToken(String email){
        redisTemplate.delete(REFRESH_PREFIX + email);
    }

    private void addTokenToBlacklist(String accessToken, long remainingMs){
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + accessToken,
                "revoked",
                remainingMs,
                TimeUnit.MILLISECONDS
        );
    }
    private Map<String,Object> buildExtraClaims(Employee employee){
        return Map.of(
                "role", employee.getRol().name(),
                "employeeId", employee.getId().toString()
        );
    }
    private CustomUserDetails buildUserDetails(Employee employee){
        return new CustomUserDetails(
                employee.getEmail(),
                employee.getPasswordHash(),
                employee.getRol().name(),
                employee.isActive()
        );
    }
    private AuthResponse buildAuthResponse(String accessToken,String refreshToken,Employee employee) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                new AuthResponse.EmployeeInfo(
                        employee.getId(),
                        employee.getEmail(),
                        employee.getFullName(),
                        employee.getRol()
                ),
                "online"
        );
    }

}
