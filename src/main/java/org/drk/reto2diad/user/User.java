package org.drk.reto2diad.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.drk.reto2diad.copia.Copia;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name="user")
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String email;
    private String password;

    @Column(name="is_admin")
    private Boolean is_admin;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Copia> copias = new ArrayList<>();

    public void addCopia(Copia copia) {
        copia.setUser(this);
        this.copias.add(copia);
    }
}
