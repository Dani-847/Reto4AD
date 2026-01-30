package org.drk.reto2diad.user;

import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserRepository repo = new UserRepository();

    public List<User> findAll() {
        return repo.findAll();
    }

    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return repo.findById(id);
    }

    public User save(User user) {
        return repo.save(user);
    }

    public User create(User user) {
        return repo.save(user);
    }

    public User update(User user) {
        return repo.save(user);
    }

    public Optional<User> delete(User user) {
        return repo.delete(user);
    }

    public Optional<User> deleteById(Long id) {
        return repo.deleteById(id);
    }
}
