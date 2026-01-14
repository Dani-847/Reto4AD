package org.drk.reto2diad.copia;

import org.drk.reto2diad.pelicula.Pelicula;
import org.drk.reto2diad.user.User;
import org.drk.reto2diad.utils.DataProvider;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

public class CopiaService {

    private final CopiaRepository repo;

    public CopiaService() {
        this.repo = new CopiaRepository(DataProvider.getSessionFactory());
    }

    public List<Copia> findByUser(User user) {
        return repo.findByUser(user);
    }

    public Copia create(User user, Pelicula pelicula, String estado, String soporte) {
        try (Session s = DataProvider.getSessionFactory().openSession()) {
            s.beginTransaction();
            User managedUser = s.find(User.class, user.getId());
            Pelicula managedPeli = s.find(Pelicula.class, pelicula.getId());
            Copia c = new Copia();
            c.setUser(managedUser);
            c.setMovie(managedPeli);
            c.setEstado(estado);
            c.setSoporte(soporte);
            Copia merged = (Copia) s.merge(c);
            s.getTransaction().commit();
            return merged;
        }
    }

    public Optional<Copia> delete(Integer copiaId, User active) {
        if (copiaId == null || active == null || active.getId() == null) return Optional.empty();

        try (Session s = DataProvider.getSessionFactory().openSession()) {
            s.beginTransaction();

            // 1) Cargar para poder devolver la copia borrada (opcional)
            Copia existing = s.find(Copia.class, copiaId);
            if (existing == null) {
                s.getTransaction().rollback();
                return Optional.empty();
            }

            // 2) Borrado condicionado por propietario (1 query -> se verá el DELETE en el log)
            int rows = s.createMutationQuery(
                            "delete from Copia c where c.id = :id and c.user.id = :userId"
                    )
                    .setParameter("id", copiaId)
                    .setParameter("userId", active.getId())
                    .executeUpdate();

            if (rows != 1) {
                s.getTransaction().rollback();
                return Optional.empty();
            }

            s.getTransaction().commit();
            return Optional.of(existing);
        } catch (Exception ex) {
            ex.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<Copia> update(Integer copiaId, User active, String estado, String soporte) {
        if (copiaId == null || active == null || active.getId() == null) return Optional.empty();

        try (Session s = DataProvider.getSessionFactory().openSession()) {
            s.beginTransaction();

            Copia c = s.find(Copia.class, copiaId);
            if (c == null) {
                s.getTransaction().rollback();
                return Optional.empty();
            }

            if (c.getUser() == null || c.getUser().getId() == null) {
                s.getTransaction().rollback();
                return Optional.empty();
            }

            long ownerId = ((Number) c.getUser().getId()).longValue();
            long activeId = ((Number) active.getId()).longValue();
            if (ownerId != activeId) {
                s.getTransaction().rollback();
                return Optional.empty();
            }

            c.setEstado(estado);
            c.setSoporte(soporte);

            s.flush();
            s.getTransaction().commit();
            return Optional.of(c);
        } catch (Exception ex) {
            ex.printStackTrace();
            return Optional.empty();
        }
    }

    public List<Copia> findByMovie(Pelicula pelicula) {
        try (Session s = DataProvider.getSessionFactory().openSession()) {
            return s.createQuery(
                            "select c from Copia c where c.movie.id = :movieId",
                            Copia.class
                    )
                    .setParameter("movieId", pelicula.getId())
                    .getResultList();
        }
    }
}
