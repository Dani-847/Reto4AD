module org.drk.reto2diad {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires static lombok;
    requires java.naming;


    opens org.drk.reto2diad;
    exports org.drk.reto2diad;
    opens org.drk.reto2diad.utils;
    exports org.drk.reto2diad.utils;
    opens org.drk.reto2diad.controllers;
    exports org.drk.reto2diad.controllers;
    opens org.drk.reto2diad.user;
    exports org.drk.reto2diad.user;
    opens org.drk.reto2diad.pelicula;
    exports org.drk.reto2diad.pelicula;
    opens org.drk.reto2diad.copia;
    exports org.drk.reto2diad.copia;
}