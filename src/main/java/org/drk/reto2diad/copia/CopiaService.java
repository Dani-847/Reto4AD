package org.drk.reto2diad.copia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.drk.reto2diad.pelicula.Pelicula;
import org.drk.reto2diad.user.User;
import org.drk.reto2diad.utils.DataProvider;

import java.util.List;
import java.util.Optional;

public class CopiaService {

    private final CopiaRepository repo = new CopiaRepository();

    public List<Copia> findByUser(User user) {
        return repo.findByUser(user);
    }

    public Copia create(User user, Pelicula pelicula, String estado, String soporte) {
        EntityManager em = DataProvider.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            User managedUser = em.find(User.class, user.getId());
            Pelicula managedPeli = em.find(Pelicula.class, pelicula.getId());
            Copia c = new Copia();
            c.setUser(managedUser);
            c.setMovie(managedPeli);
            c.setEstado(estado);
            c.setSoporte(soporte);
            Copia merged = em.merge(c);
            tx.commit();
            return merged;
        } finally {
            em.close();
        }
    }

    public Optional<Copia> delete(Integer copiaId, User active) {
        if (copiaId == null || active == null) return Optional.empty();
        EntityManager em = DataProvider.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Copia c = em.find(Copia.class, copiaId);
            if (c == null || c.getUser() == null || !c.getUser().getId().equals(active.getId())) {
                tx.rollback();
                return Optional.empty();
            }
            em.remove(c);
            tx.commit();
            return Optional.of(c);
        } finally {
            em.close();
        }
    }

    public Optional<Copia> update(Integer copiaId, User active, String estado, String soporte) {
        if (copiaId == null || active == null) return Optional.empty();
        EntityManager em = DataProvider.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Copia c = em.find(Copia.class, copiaId);
            if (c == null || c.getUser() == null || !c.getUser().getId().equals(active.getId())) {
                tx.rollback();
                return Optional.empty();
            }
            c.setEstado(estado);
            c.setSoporte(soporte);
            tx.commit();
            return Optional.of(c);
        } finally {
            em.close();
        }
    }

    public List<Copia> findByMovie(Pelicula pelicula) {
        EntityManager em = DataProvider.createEntityManager();
        try {
            return em.createQuery("SELECT c FROM Copia c WHERE c.movie.id = :movieId", Copia.class)
                    .setParameter("movieId", pelicula.getId())
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
