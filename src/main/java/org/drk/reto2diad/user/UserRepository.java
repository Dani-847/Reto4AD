package org.drk.reto2diad.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.drk.reto2diad.utils.DataProvider;
import org.drk.reto2diad.utils.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad User usando ObjectDB.
 */
public class UserRepository implements Repository<User> {

    @Override
    public User save(User entity) {
        EntityManager em = DataProvider.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            User managed = em.merge(entity);
            tx.commit();
            return managed;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<User> delete(User entity) {
        EntityManager em = DataProvider.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            User managed = em.merge(entity);
            em.remove(managed);
            tx.commit();
            return Optional.of(entity);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<User> deleteById(Long id) {
        EntityManager em = DataProvider.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            User u = em.find(User.class, id.intValue());
            if (u != null) {
                tx.begin();
                em.remove(u);
                tx.commit();
            }
            return Optional.ofNullable(u);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        EntityManager em = DataProvider.createEntityManager();
        try {
            return Optional.ofNullable(em.find(User.class, id.intValue()));
        } finally {
            em.close();
        }
    }

    @Override
    public List<User> findAll() {
        EntityManager em = DataProvider.createEntityManager();
        try {
            return em.createQuery("SELECT u FROM User u", User.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Long count() {
        EntityManager em = DataProvider.createEntityManager();
        try {
            return em.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    public Optional<User> findByEmail(String email) {
        EntityManager em = DataProvider.createEntityManager();
        try {
            List<User> list = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getResultList();
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } finally {
            em.close();
        }
    }
}
