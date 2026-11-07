package com.fitstore.controller;

import com.fitstore.dto.response.ApiResponse;
import com.fitstore.service.AuthService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Autenticación y registro de usuarios. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@RequestBody Map<String, String> body) {
        return ApiResponse.of(authService.login(body.get("email"), body.get("clave")));
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, String>> register(@RequestBody Map<String, String> body) {
        return ApiResponse.of(authService.registrar(body.get("nombre"), body.get("email"), body.get("clave")));
    }
}
