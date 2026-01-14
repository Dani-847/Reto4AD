// src/main/java/org/drk/reto2diad/session/AuthService.java
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

    public enum LoginFailureReason {
        EMAIL_NOT_FOUND,
        PASSWORD_INCORRECT,
        INVALID_INPUT
    }

    public record LoginResult(Optional<User> user, LoginFailureReason reason) {
        public boolean success() {
            return user != null && user.isPresent();
        }
    }

    public LoginResult validateUserWithReason(String email, String password) {
        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            return new LoginResult(Optional.empty(), LoginFailureReason.INVALID_INPUT);
        }

        Optional<User> byEmail = userService.findByEmail(email);
        if (byEmail.isEmpty()) {
            return new LoginResult(Optional.empty(), LoginFailureReason.EMAIL_NOT_FOUND);
        }

        User u = byEmail.get();
        if (!Objects.equals(u.getPassword(), password)) {
            return new LoginResult(Optional.empty(), LoginFailureReason.PASSWORD_INCORRECT);
        }

        return new LoginResult(Optional.of(u), null);
    }
}
