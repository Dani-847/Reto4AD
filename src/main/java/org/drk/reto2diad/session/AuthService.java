package org.drk.reto2diad.session;

import org.drk.reto2diad.user.User;
import org.drk.reto2diad.user.UserService;

import java.util.Objects;
import java.util.Optional;

/**
 * Servicio de autenticación.
 * Valida credenciales delegando en UserService.
 */
public class AuthService {

    private final UserService userService;

    public AuthService(UserService userService) {
        this.userService = userService;
    }

    public Optional<User> validateUser(String email, String password) {
        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            return Optional.empty();
        }
        return userService.findByEmail(email)
                .filter(u -> Objects.equals(u.getPassword(), password));
    }
}
