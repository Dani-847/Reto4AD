// java
package org.drk.reto2diad.copia;

import org.drk.reto2diad.user.User;
import org.drk.reto2diad.utils.Repository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

/**
 * Repository de Copia: CRUD y búsqueda por usuario.
 */
public class CopiaRepository implements Repository<Copia> {

    private final SessionFactory sessionFactory;

    public CopiaRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Copia save(Copia entity) {
        try (Session s = sessionFactory.openSession()) {
            s.beginTransaction();
            Copia managed = s.merge(entity);
            s.getTransaction().commit();
            return managed;
        }
    }

    @Override
    public Optional<Copia> delete(Copia entity) {
        try (Session s = sessionFactory.openSession()) {
            s.beginTransaction();
            s.remove(entity);
            s.getTransaction().commit();
            return Optional.ofNullable(entity);
        }
    }

    @Override
    public Optional<Copia> deleteById(Long id) {
        try (Session s = sessionFactory.openSession()) {
            Copia c = s.find(Copia.class, id);
            if (c != null) {
                s.beginTransaction();
                s.remove(c);
                s.getTransaction().commit();
            }
            return Optional.ofNullable(c);
        }
    }

    @Override
    public Optional<Copia> findById(Long id) {
        try (Session s = sessionFactory.openSession()) {
            return Optional.ofNullable(s.find(Copia.class, id));
        }
    }

    @Override
    public List<Copia> findAll() {
        try (Session s = sessionFactory.openSession()) {
            return s.createQuery("from Copia", Copia.class).list();
        }
    }

    @Override
    public Long count() {
        try (Session s = sessionFactory.openSession()) {
            return s.createQuery("select count(c) from Copia c", Long.class).getSingleResult();
        }
    }

    public List<Copia> findByUser(User user) {
        try (Session s = sessionFactory.openSession()) {
            return s.createQuery("from Copia c where c.user = :user", Copia.class)
                    .setParameter("user", user)
                    .list();
        }
    }
}
