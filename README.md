# Finance API - Backend de Finanzas Personales

Este proyecto es el backend de una aplicación de finanzas personales, desarrollado con Java y Spring Boot. Proporciona una API RESTful segura para gestionar usuarios, transacciones, categorías y obtener resúmenes financieros.

## ✨ Características Implementadas

* **Autenticación y Seguridad:**
    * Registro de usuarios con contraseñas encriptadas (BCrypt).
    * Login mediante credenciales que devuelve un **Token JWT** para autenticación sin estado.
    * Endpoints protegidos que solo son accesibles con un token JWT válido.
    * Lógica de autorización que asegura que un usuario solo puede acceder y modificar sus propios datos.
* **Gestión de Transacciones:**
    * CRUD completo (Crear, Leer, Actualizar, Borrar) para transacciones financieras.
    * Cada transacción se asocia automáticamente al usuario autenticado.
    * Validación de datos para asegurar la integridad de la información (ej. montos positivos, descripciones no vacías).
* **Gestión de Categorías:**
    * CRUD completo para que los usuarios puedan gestionar sus propias categorías de gastos e ingresos.
    * Asociación de transacciones a categorías para una mejor clasificación.
* **Dashboard Inteligente:**
    * Un endpoint de resumen que calcula en tiempo real el total de ingresos, gastos y el balance del mes actual.

## 🛠️ Stack Tecnológico

* **Lenguaje:** Java 21
* **Framework:** Spring Boot 3
* **Seguridad:** Spring Security 6
* **Acceso a Datos:** Spring Data JPA (con Hibernate)
* **Base de Datos:** PostgreSQL
* **Autenticación:** JSON Web Tokens (JWT)
* **Construcción:** Apache Maven
* **Librerías Clave:**
    * `Lombok`: Para reducir el código boilerplate.
    * `jjwt`: Para la gestión de tokens JWT.
    * `jackson-datatype-hibernate5`: Para la correcta serialización de entidades JPA.

## 🚀 Cómo Empezar

Sigue estos pasos para levantar el proyecto en tu entorno local.

### Prerrequisitos

* **Java JDK 21** o superior.
* **Apache Maven** 3.8 o superior.
* Una instancia de **PostgreSQL** corriendo (localmente, con Docker o en un servicio como Railway).

### Configuración

1.  **Clona el repositorio:**
    ```bash
    git clone [https://github.com/tu-usuario/tu-repositorio.git](https://github.com/tu-usuario/tu-repositorio.git)
    cd tu-repositorio
    ```

2.  **Configura la base de datos:**
    * Abre el fichero `src/main/resources/application.properties`.
    * Modifica las siguientes líneas con los datos de tu base de datos PostgreSQL:
        ```properties
        spring.datasource.url=jdbc:postgresql://<HOST>:<PORT>/<DATABASE_NAME>
        spring.datasource.username=<TU_USUARIO>
        spring.datasource.password=<TU_CONTRASEÑA>
        ```

### Ejecución

Una vez configurado, puedes arrancar la aplicación usando el wrapper de Maven incluido:

```bash
# En Windows
./mvnw spring-boot:run

# En Mac/Linux
./mvnw spring-boot:run