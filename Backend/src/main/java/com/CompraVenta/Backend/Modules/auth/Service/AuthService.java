package com.CompraVenta.Backend.Modules.auth.Service;

import com.CompraVenta.Backend.Modules.auth.Dto.Request.LoginRequest;
import com.CompraVenta.Backend.Modules.auth.Dto.Request.RefreshRequest;
import com.CompraVenta.Backend.Modules.auth.Dto.Response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshRequest request);
    void logout(String accessToken);

}
