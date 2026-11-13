package com.fitstore;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fitstore.config.JwtUtil;
import com.fitstore.entity.Cliente;
import com.fitstore.exception.FitStoreException;
import com.fitstore.repository.ClienteRepository;
import com.fitstore.service.AuthService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ClienteRepository clienteRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setEmail("cliente@fitstore.com");
        cliente.setClave("hash-actual");
        when(clienteRepo.findByEmail("cliente@fitstore.com")).thenReturn(Optional.of(cliente));
    }

    @Test
    void cambiaLaClaveCuandoLaActualEsCorrecta() {
        when(passwordEncoder.matches("vieja123", "hash-actual")).thenReturn(true);
        when(passwordEncoder.encode("nueva123")).thenReturn("hash-nuevo");

        authService.cambiarClave("cliente@fitstore.com", "vieja123", "nueva123");

        assertEquals("hash-nuevo", cliente.getClave());
        verify(clienteRepo).save(cliente);
    }

    @Test
    void rechazaCuandoLaClaveActualEsIncorrecta() {
        when(passwordEncoder.matches("equivocada", "hash-actual")).thenReturn(false);

        assertThrows(
                FitStoreException.class,
                () -> authService.cambiarClave("cliente@fitstore.com", "equivocada", "nueva123"));
        verify(clienteRepo, never()).save(any());
    }

    @Test
    void rechazaClaveNuevaMuyCorta() {
        when(passwordEncoder.matches("vieja123", "hash-actual")).thenReturn(true);

        assertThrows(
                FitStoreException.class, () -> authService.cambiarClave("cliente@fitstore.com", "vieja123", "123"));
        verify(clienteRepo, never()).save(any());
    }
}
