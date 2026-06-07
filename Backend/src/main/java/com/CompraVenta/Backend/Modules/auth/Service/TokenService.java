package com.CompraVenta.Backend.Modules.auth.Service;

import com.CompraVenta.Backend.Modules.auth.Dto.Request.RefreshRequest;
import com.CompraVenta.Backend.Modules.auth.Dto.Response.AuthResponse;

public interface TokenService {
    AuthResponse refresh(RefreshRequest request);
    void logout(String accessToken);
    void storeRefreshToken(String email, String refreshToken);
    void revokeAlltokensForUser(String email);
}
