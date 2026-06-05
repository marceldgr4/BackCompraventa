package com.CompraVenta.Backend.Modules.auth.Service;

import com.CompraVenta.Backend.Modules.auth.Dto.Request.LoginRequest;
import com.CompraVenta.Backend.Modules.auth.Dto.Response.AuthResponse;

public interface AuthenticationService {
    AuthResponse login(LoginRequest request);
}
