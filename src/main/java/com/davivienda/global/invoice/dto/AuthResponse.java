package com.davivienda.global.invoice.dto;

import com.davivienda.global.invoice.domain.Role;

public record AuthResponse(String token, String username, Role role) {
}
