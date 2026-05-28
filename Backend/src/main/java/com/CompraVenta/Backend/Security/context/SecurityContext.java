package com.CompraVenta.Backend.Security.context;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

@UtilityClass
public class SecurityContext {
    public String getCurrentUsername(){
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .map(principal ->{
                    if(principal instanceof UserDetails ud) return  ud.getUsername();
                    return principal.toString();
                })
                .orElse("Anonymous");
    }
    public boolean isAuthenticated(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());
    }

}
