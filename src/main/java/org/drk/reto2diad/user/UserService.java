// java
package org.drk.reto2diad.user;

import org.drk.reto2diad.utils.DataProvider;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de User:
 * - Orquesta CRUD del repository.
 * - Operaciones específicas: cambio de contraseña, activar/desactivar admin.
 */
public class UserService {

    private final UserRepository repo;

    public UserService() {
        this.repo = new UserRepository(DataProvider.getSessionFactory());
    }

    public User create(User u) {
        return repo.save(u);
    }

    public User update(User u) {
        return repo.save(u);
    }

    public Optional<User> findById(Long id) {
        return repo.findById(id);
    }

    public List<User> findAll() {
        return repo.findAll();
    }

    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    public Optional<User> delete(User u) {
        return repo.delete(u);
    }

    public Optional<User> deleteById(Long id) {
        return repo.deleteById(id);
    }

    public Long count() {
        return repo.count();
    }

    public Optional<User> changePassword(Long userId, String newPass) {
        if (newPass == null || newPass.isBlank()) return Optional.empty();
        try (Session s = DataProvider.getSessionFactory().openSession()) {
            User managed = s.find(User.class, userId);
            if (managed == null) return Optional.empty();
            s.beginTransaction();
            managed.setPassword(newPass);
            s.merge(managed);
            s.getTransaction().commit();
            return Optional.of(managed);
        }
    }

    public Optional<User> toggleAdmin(Long userId, boolean admin) {
        try (Session s = DataProvider.getSessionFactory().openSession()) {
            User managed = s.find(User.class, userId);
            if (managed == null) return Optional.empty();
            s.beginTransaction();
            managed.setIs_admin(admin);
            s.merge(managed);
            s.getTransaction().commit();
            return Optional.of(managed);
        }
    }
}
