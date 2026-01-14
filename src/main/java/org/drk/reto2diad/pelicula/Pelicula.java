package org.drk.reto2diad.pelicula;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "pelicula")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pelicula implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @Column(name = "año")
    private Integer anio;

    @Column(name = "genero", nullable = false, length = 100)
    private String genero;

    @Column(name = "director", length = 255)
    private String director;

    @Override
    public String toString() {
        return "Pelicula{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", anio=" + anio +
                ", genero='" + genero + '\'' +
                ", director='" + director + '\'' +
                '}';
    }
}
