package org.drk.reto2diad.utils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Proveedor único de \`SessionFactory\` para Hibernate.
 * - Inicializa perezosamente con credenciales de entorno.
 * - Muestra alertas de error mediante \`JavaFXUtil\`.
 * - Lanza excepciones claras para fallos críticos.
 */
public final class DataProvider {

    private static volatile SessionFactory sessionFactory = null;

    private DataProvider() {}

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            synchronized (DataProvider.class) {
                if (sessionFactory == null) {
                    String dbUser = System.getenv("DB_USER");
                    String dbPassword = System.getenv("DB_PASSWORD");

                    if (dbUser == null || dbPassword == null) {
                        JavaFXUtil.showModal(
                                javafx.scene.control.Alert.AlertType.ERROR,
                                "Error conexión BD",
                                "Variables de entorno no definidas",
                                "Asegúrate de que Docker esté corriendo y que las variables `DB_USER` y `DB_PASSWORD` estén definidas."
                        );
                        throw new IllegalStateException("Variables de entorno DB_USER/DB_PASSWORD ausentes");
                    }

                    try {
                        Configuration configuration = new Configuration().configure()
                                .setProperty("hibernate.connection.username", dbUser)
                                .setProperty("hibernate.connection.password", dbPassword);

                        sessionFactory = configuration.buildSessionFactory();
                    } catch (Exception ex) {
                        JavaFXUtil.showModal(
                                javafx.scene.control.Alert.AlertType.ERROR,
                                "Error conexión BD",
                                "No se pudo conectar a la base de datos",
                                "¿Está Docker corriendo?"
                        );
                        throw new RuntimeException("No se pudo crear SessionFactory", ex);
                    }
                }
            }
        }
        return sessionFactory;
    }
}
