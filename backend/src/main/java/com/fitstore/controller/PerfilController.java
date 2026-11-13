package com.fitstore.controller;

import com.fitstore.dto.response.ApiResponse;
import com.fitstore.service.AuthService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Perfil del usuario autenticado. */
@RestController
@RequestMapping("/api/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final AuthService authService;

    @PatchMapping("/clave")
    public ApiResponse<Map<String, String>> cambiarClave(@RequestBody Map<String, String> body, Authentication auth) {

        authService.cambiarClave(auth.getName(), body.get("claveActual"), body.get("claveNueva"));
        return ApiResponse.of(Map.of("mensaje", "Contraseña actualizada"));
    }
}
