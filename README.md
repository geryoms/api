Finance API - Documentación del Proyecto y .gitignore

Este documento contiene toda la información relevante sobre el proyecto, incluyendo la descripción, la guía de uso, la hoja de ruta y el contenido del fichero .gitignore.

📖 README

✨ Características Implementadas

Autenticación y Seguridad:

Registro de usuarios con contraseñas encriptadas (BCrypt).

Login mediante credenciales que devuelve un Token JWT para autenticación sin estado.

Endpoints protegidos que solo son accesibles con un token JWT válido.

Lógica de autorización que asegura que un usuario solo puede acceder y modificar sus propios datos.

Gestión de Transacciones:

CRUD completo (Crear, Leer, Actualizar, Borrar) para transacciones financieras.

Cada transacción se asocia automáticamente al usuario autenticado.

Validación de datos para asegurar la integridad de la información (ej. montos positivos, descripciones no vacías).

Gestión de Categorías:

CRUD completo para que los usuarios puedan gestionar sus propias categorías de gastos e ingresos.

Asociación de transacciones a categorías para una mejor clasificación.

Dashboard Inteligente:

Un endpoint de resumen que calcula en tiempo real el total de ingresos, gastos y el balance del mes actual.

🛠️ Stack Tecnológico

Lenguaje: Java 21

Framework: Spring Boot 3

Seguridad: Spring Security 6

Acceso a Datos: Spring Data JPA (con Hibernate)

Base de Datos: PostgreSQL

Autenticación: JSON Web Tokens (JWT)

Construcción: Apache Maven

Librerías Clave:

Lombok: Para reducir el código boilerplate.

jjwt: Para la gestión de tokens JWT.

jackson-datatype-hibernate5: Para la correcta serialización de entidades JPA.

🚀 Cómo Empezar

Sigue estos pasos para levantar el proyecto en tu entorno local.

Prerrequisitos

Java JDK 21 o superior.

Apache Maven 3.8 o superior.

Una instancia de PostgreSQL corriendo.

Configuración

Clona el repositorio:

git clone [https://github.com/tu-usuario/tu-repositorio.git](https://github.com/tu-usuario/tu-repositorio.git)
cd tu-repositorio


Configura la base de datos:

Abre el fichero src/main/resources/application.properties.

Modifica las siguientes líneas con los datos de tu base de datos PostgreSQL:

spring.datasource.url=jdbc:postgresql://<HOST>:<PORT>/<DATABASE_NAME>
spring.datasource.username=<TU_USUARIO>
spring.datasource.password=<TU_CONTRASEÑA>


Ejecución

Una vez configurado, puedes arrancar la aplicación usando el wrapper de Maven incluido:

# En Windows
./mvnw spring-boot:run

# En Mac/Linux
./mvnw spring-boot:run


El servidor se iniciará en http://localhost:8088.

🌐 API Endpoints

Todos los endpoints bajo /api requieren un token JWT en la cabecera Authorization: Bearer <token>.

Autenticación (/auth)

Método

URL

Descripción

POST

/auth/register

Registra un nuevo usuario.

POST

/auth/login

Autentica a un usuario y devuelve un token JWT.

Transacciones (/api/transactions)

Método

URL

Descripción

GET

/

Obtiene todas las transacciones del usuario.

GET

/{id}

Obtiene una transacción específica por su ID.

POST

/

Crea una nueva transacción.

PUT

/{id}

Actualiza una transacción existente.

DELETE

/{id}

Borra una transacción.

Categorías (/api/categories)

Método

URL

Descripción

GET

/

Obtiene todas las categorías del usuario.

POST

/

Crea una nueva categoría.

PUT

/{id}

Actualiza una categoría existente.

DELETE

/{id}

Borra una categoría.

Dashboard (/api/dashboard)

Método

URL

Descripción

GET

/summary

Devuelve un resumen del mes actual (ingresos, gastos, balance).

🗺️ Hoja de Ruta (Roadmap)

Funcionalidades futuras planeadas para el proyecto:

Opción 2: Gestión de Cuentas 🏦

[ ] Crear Entidad Account: Modelar cuentas de banco, efectivo, tarjetas, etc.

[ ] CRUD de Cuentas: Endpoints para gestionar las cuentas.

[ ] Asociar Transacciones a Cuentas: Cada transacción afectará el balance de una cuenta.

[ ] Implementar Transferencias: Lógica para mover dinero entre cuentas.

Opción 3: Planificación y Automatización 📅

[ ] Crear Entidad Budget: Permitir crear presupuestos mensuales por categoría.

[ ] Endpoint de Progreso de Presupuestos: Comparar gastos reales con los presupuestos.

[ ] Crear Entidad Subscription: Modelar gastos recurrentes.

[ ] Automatización con Cron Jobs: Generar automáticamente las transacciones de las suscripciones.
