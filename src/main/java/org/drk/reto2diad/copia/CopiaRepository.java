package org.drk.reto2diad.copia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.drk.reto2diad.user.User;
import org.drk.reto2diad.utils.DataProvider;
import org.drk.reto2diad.utils.Repository;

import java.util.List;
import java.util.Optional;

public class CopiaRepository implements Repository<Copia> {

    @Override
    public Copia save(Copia entity) {
        EntityManager em = DataProvider.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Copia managed = em.merge(entity);
            tx.commit();
            return managed;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Copia> delete(Copia entity) {
        EntityManager em = DataProvider.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Copia managed = em.merge(entity);
            em.remove(managed);
            tx.commit();
            return Optional.of(entity);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Copia> deleteById(Long id) {
        EntityManager em = DataProvider.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            Copia c = em.find(Copia.class, id.intValue());
            if (c != null) {
                tx.begin();
                em.remove(c);
                tx.commit();
            }
            return Optional.ofNullable(c);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Copia> findById(Long id) {
        EntityManager em = DataProvider.createEntityManager();
        try {
            return Optional.ofNullable(em.find(Copia.class, id.intValue()));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Copia> findAll() {
        EntityManager em = DataProvider.createEntityManager();
        try {
            return em.createQuery("SELECT c FROM Copia c", Copia.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Long count() {
        EntityManager em = DataProvider.createEntityManager();
        try {
            return em.createQuery("SELECT COUNT(c) FROM Copia c", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    public List<Copia> findByUser(User user) {
        EntityManager em = DataProvider.createEntityManager();
        try {
            return em.createQuery("SELECT c FROM Copia c WHERE c.user = :user", Copia.class)
                    .setParameter("user", user)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
