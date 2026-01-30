package org.drk.reto2diad.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Proveedor único de EntityManagerFactory para ObjectDB.
 * Conecta directamente al archivo .odb sin necesidad de persistence.xml.
 * @author Dani-847
 */
public final class DataProvider {

    private static EntityManagerFactory emf = null;

    private DataProvider() {}

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            // Conecta directamente al archivo ObjectDB (se crea si no existe)
            emf = Persistence.createEntityManagerFactory("objectdb:peliculas.odb");
        }
        return emf;
    }

    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            emf = null;
        }
    }
}
