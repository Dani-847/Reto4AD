package org.drk.reto2diad.pelicula;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.drk.reto2diad.utils.DataProvider;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para operaciones CRUD de Pelicula usando JPA/ObjectDB.
 */
public class PeliculaService {

    public List<Pelicula> findAll() {
        EntityManager em = DataProvider.createEntityManager();
        try {
            return em.createQuery("SELECT p FROM Pelicula p", Pelicula.class).getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Pelicula> findById(Long id) {
        EntityManager em = DataProvider.createEntityManager();
        try {
            return Optional.ofNullable(em.find(Pelicula.class, id.intValue()));
        } finally {
            em.close();
        }
    }

    public Pelicula create(Pelicula pelicula) {
        EntityManager em = DataProvider.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(pelicula);
            tx.commit();
            return pelicula;
        } finally {
            em.close();}
    }

    public Pelicula update(Pelicula pelicula) {
        EntityManager em = DataProvider.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();Pelicula merged = em.merge(pelicula);
            tx.commit();
            return merged;
        } finally {
            em.close();
        }
    }

    public void delete(Pelicula pelicula) {
        EntityManager em = DataProvider.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Pelicula managed = em.find(Pelicula.class, pelicula.getId());
            if (managed != null) {
                em.remove(managed);
            }
            tx.commit();
        } finally {
            em.close();
        }
    }
}
