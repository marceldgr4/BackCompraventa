package com.CompraVenta.Backend.Modules.auth.Service.Impl;

import com.CompraVenta.Backend.Modules.auth.Dto.Request.LoginRequest;
import com.CompraVenta.Backend.Modules.auth.Dto.Request.RefreshRequest;
import com.CompraVenta.Backend.Modules.auth.Dto.Response.AuthResponse;
import com.CompraVenta.Backend.Modules.auth.Service.AuthService;

import com.CompraVenta.Backend.Modules.auth.Service.AuthenticationService;
import com.CompraVenta.Backend.Modules.auth.Service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceFacade implements AuthService {

    private final AuthenticationService authenticationService;
    private final TokenService tokenService;

    @Override
    public AuthResponse login(LoginRequest request) {
        return authenticationService.login(request);
    }

    @Override
    public AuthResponse refresh(RefreshRequest request) {
        return tokenService.refresh(request);
    }

    @Override
    public void logout(String accessToken) {
        tokenService.logout(accessToken);
    }
}
