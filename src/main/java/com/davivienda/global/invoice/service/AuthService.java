package com.davivienda.global.invoice.service;

import com.davivienda.global.invoice.domain.AppUser;
import com.davivienda.global.invoice.dto.AuthResponse;
import com.davivienda.global.invoice.dto.LoginRequest;
import com.davivienda.global.invoice.repository.UserRepository;
import com.davivienda.global.invoice.security.AppUserDetails;
import com.davivienda.global.invoice.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        AppUserDetails principal = (AppUserDetails) authentication.getPrincipal();
        AppUser user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        String token = jwtService.generateToken(principal);
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }
}
