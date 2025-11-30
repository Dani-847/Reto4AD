# Reto2DI-AD

Proyecto **Reto2DI-AD**  
Descripción breve del proyecto (puedes completarla según el objetivo de tu aplicación).

## Requisitos

- Java
- Maven
- MySQL (Docker)
- Acceso a una base de datos configurada (Tablas Usuario, Peliculas y Copia)

## Instalación / Configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/Dani-847/Reto2DI-AD.git
```

### 2. Configurar variables de entorno

El proyecto necesita estas variables para poder conectar con la base de datos.  
En el desarrollo las usaba integrada en intellij con la herramienta de ejecución:

```env
DB_USER=root
DB_PASSWORD=root
```

## Estructura del proyecto

```
.
├── src/               # Código fuente del proyecto
├── pom.xml            # Dependencias y configuración de Maven
├── README.md
└── .gitignore
```

## Variables de entorno

El proyecto requiere como mínimo:

| Variable       | Descripción                     | Ejemplo |
|----------------|----------------------------------|---------|
| DB_USER        | Usuario de la base de datos      | root    |
| DB_PASSWORD    | Contraseña del usuario           | root    |
