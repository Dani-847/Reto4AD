// java
package org.drk.reto2diad.copia;

import org.drk.reto2diad.pelicula.Pelicula;
import org.drk.reto2diad.user.User;
import org.drk.reto2diad.utils.DataProvider;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de Copia:
 * - Crea copias asociadas a usuario y película.
 * - Actualiza solo si el usuario es propietario.
 * - Elimina validando propiedad.
 * - Lista copias por usuario.
 */
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

    public Optional<Copia> update(Long copiaId, User active, String estado, String soporte) {
        try (Session s = DataProvider.getSessionFactory().openSession()) {
            Copia c = s.find(Copia.class, copiaId);
            if (c == null) return Optional.empty();
            if (!c.getUser().getId().equals(active.getId())) return Optional.empty();
            s.beginTransaction();
            c.setEstado(estado);
            c.setSoporte(soporte);
            s.merge(c);
            s.getTransaction().commit();
            return Optional.of(c);
        }
    }

    public Optional<Copia> delete(Long copiaId, User active) {
        try (Session s = DataProvider.getSessionFactory().openSession()) {
            Copia c = s.find(Copia.class, copiaId);
            if (c == null) return Optional.empty();
            if (!c.getUser().getId().equals(active.getId())) return Optional.empty();
            s.beginTransaction();
            s.remove(c);
            s.getTransaction().commit();
            return Optional.of(c);
        }
    }
}
