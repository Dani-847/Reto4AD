// java
package org.drk.reto2diad.pelicula;

import org.drk.reto2diad.utils.DataProvider;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de Pelicula:
 * - Encapsula CRUD.
 * - Ejemplo de actualización parcial (titulo/género/director/año).
 */
public class PeliculaService {

    private final PeliculaRepository repo;

    public PeliculaService() {
        this.repo = new PeliculaRepository(DataProvider.getSessionFactory());
    }

    public Pelicula create(Pelicula p) {
        return repo.save(p);
    }

    public Pelicula update(Pelicula p) {
        return repo.save(p);
    }

    public Optional<Pelicula> findById(Long id) {
        return repo.findById(id);
    }

    public List<Pelicula> findAll() {
        return repo.findAll();
    }

    public Optional<Pelicula> delete(Pelicula p) {
        return repo.delete(p);
    }

    public Optional<Pelicula> deleteById(Long id) {
        return repo.deleteById(id);
    }

    public Long count() {
        return repo.count();
    }

    public Optional<Pelicula> updateFields(Long id, String titulo, Integer anio, String genero, String director) {
        try (Session s = DataProvider.getSessionFactory().openSession()) {
            Pelicula managed = s.find(Pelicula.class, id);
            if (managed == null) return Optional.empty();
            s.beginTransaction();
            if (titulo != null) managed.setTitulo(titulo);
            if (anio != null) managed.setAnio(anio);
            if (genero != null) managed.setGenero(genero);
            if (director != null) managed.setDirector(director);
            s.merge(managed);
            s.getTransaction().commit();
            return Optional.of(managed);
        }
    }
}
