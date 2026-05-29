package com.fitstore.service;

import com.fitstore.config.JwtUtil;
import com.fitstore.entity.Cliente;
import com.fitstore.exception.FitStoreException;
import com.fitstore.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ClienteRepository clienteRepo;
    private final PasswordEncoder   passwordEncoder;
    private final JwtUtil           jwtUtil;

    public Map<String, String> login(String email, String clave) {
        Cliente cliente = clienteRepo.findByEmail(email)
            .orElseThrow(() -> new FitStoreException("Credenciales inválidas"));

        if (!passwordEncoder.matches(clave, cliente.getClave())) {
            throw new FitStoreException("Credenciales inválidas");
        }

        String token = jwtUtil.generarToken(email, cliente.getRol().name());

        return Map.of(
            "token",  token,
            "nombre", cliente.getNombre(),
            "email",  cliente.getEmail(),
            "rol",    cliente.getRol().name()
        );
    }

    public Map<String, String> registrar(String nombre, String email, String clave) {
        if (clienteRepo.existsByEmail(email)) {
            throw new FitStoreException("El email ya está registrado");
        }

        Cliente nuevo = new Cliente();
        nuevo.setNombre(nombre);
        nuevo.setEmail(email);
        nuevo.setClave(passwordEncoder.encode(clave));
        nuevo.setRol(Cliente.Rol.CLIENTE);
        clienteRepo.save(nuevo);

        String token = jwtUtil.generarToken(email, "CLIENTE");
        return Map.of(
            "token",  token,
            "nombre", nombre,
            "email",  email,
            "rol",    "CLIENTE"
        );
    }
}
