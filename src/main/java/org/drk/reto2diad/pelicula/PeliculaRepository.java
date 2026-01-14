// java
package org.drk.reto2diad.pelicula;

import org.drk.reto2diad.utils.Repository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

/**
 * Repository de Pelicula: CRUD básico.
 */
public class PeliculaRepository implements Repository<Pelicula> {

    private final SessionFactory sessionFactory;

    public PeliculaRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Pelicula save(Pelicula entity) {
        try (Session s = sessionFactory.openSession()) {
            s.beginTransaction();
            Pelicula managed = s.merge(entity);
            s.getTransaction().commit();
            return managed;
        }
    }

    @Override
    public Optional<Pelicula> delete(Pelicula entity) {
        try (Session s = sessionFactory.openSession()) {
            s.beginTransaction();
            s.remove(entity);
            s.getTransaction().commit();
            return Optional.ofNullable(entity);
        }
    }

    @Override
    public Optional<Pelicula> deleteById(Long id) {
        try (Session s = sessionFactory.openSession()) {
            Pelicula p = s.find(Pelicula.class, id);
            if (p != null) {
                s.beginTransaction();
                s.remove(p);
                s.getTransaction().commit();
            }
            return Optional.ofNullable(p);
        }
    }

    @Override
    public Optional<Pelicula> findById(Long id) {
        try (Session s = sessionFactory.openSession()) {
            return Optional.ofNullable(s.find(Pelicula.class, id));
        }
    }

    @Override
    public List<Pelicula> findAll() {
        try (Session s = sessionFactory.openSession()) {
            return s.createQuery("from Pelicula", Pelicula.class).list();
        }
    }

    @Override
    public Long count() {
        try (Session s = sessionFactory.openSession()) {
            return s.createQuery("select count(p) from Pelicula p", Long.class).getSingleResult();
        }
    }
}
