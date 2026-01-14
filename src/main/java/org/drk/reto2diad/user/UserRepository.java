// java
package org.drk.reto2diad.user;

import org.drk.reto2diad.utils.Repository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository de User: CRUD básico y búsqueda por email.
 */
public class UserRepository implements Repository<User> {

    private final SessionFactory sessionFactory;

    public UserRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public User save(User entity) {
        try (Session s = sessionFactory.openSession()) {
            s.beginTransaction();
            User managed = s.merge(entity);
            s.getTransaction().commit();
            return managed;
        }
    }

    @Override
    public Optional<User> delete(User entity) {
        try (Session s = sessionFactory.openSession()) {
            s.beginTransaction();
            s.remove(entity);
            s.getTransaction().commit();
            return Optional.ofNullable(entity);
        }
    }

    @Override
    public Optional<User> deleteById(Long id) {
        try (Session s = sessionFactory.openSession()) {
            User u = s.find(User.class, id);
            if (u != null) {
                s.beginTransaction();
                s.remove(u);
                s.getTransaction().commit();
            }
            return Optional.ofNullable(u);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        try (Session s = sessionFactory.openSession()) {
            return Optional.ofNullable(s.find(User.class, id));
        }
    }

    @Override
    public List<User> findAll() {
        try (Session s = sessionFactory.openSession()) {
            return s.createQuery("from User", User.class).list();
        }
    }

    @Override
    public Long count() {
        try (Session s = sessionFactory.openSession()) {
            return s.createQuery("select count(u) from User u", Long.class).getSingleResult();
        }
    }

    public Optional<User> findByEmail(String email) {
        try (Session s = sessionFactory.openSession()) {
            Query<User> q = s.createQuery("from User where email = :email", User.class);
            q.setParameter("email", email);
            return Optional.ofNullable(q.uniqueResult());
        }
    }
}
