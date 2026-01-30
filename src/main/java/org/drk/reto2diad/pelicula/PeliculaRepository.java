package org.drk.reto2diad.pelicula;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.drk.reto2diad.utils.DataProvider;
import org.drk.reto2diad.utils.Repository;

import java.util.List;
import java.util.Optional;

public class PeliculaRepository implements Repository<Pelicula> {

    @Override
    public Pelicula save(Pelicula entity) {
        EntityManager em = DataProvider.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();Pelicula managed = em.merge(entity);
            tx.commit();
            return managed;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Pelicula> delete(Pelicula entity) {
        EntityManager em = DataProvider.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Pelicula managed = em.merge(entity);
            em.remove(managed);
            tx.commit();
            return Optional.of(entity);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Pelicula> deleteById(Long id) {
        EntityManager em = DataProvider.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            Pelicula p = em.find(Pelicula.class, id.intValue());
            if (p != null) {
                tx.begin();
                em.remove(p);
                tx.commit();}
            return Optional.ofNullable(p);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Pelicula> findById(Long id) {
        EntityManager em = DataProvider.createEntityManager();
        try {
            return Optional.ofNullable(em.find(Pelicula.class, id.intValue()));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Pelicula> findAll() {
        EntityManager em = DataProvider.createEntityManager();
        try {
            return em.createQuery("SELECT p FROM Pelicula p", Pelicula.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Long count() {
        EntityManager em = DataProvider.createEntityManager();
        try {
            return em.createQuery("SELECT COUNT(p) FROM Pelicula p", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }
}
