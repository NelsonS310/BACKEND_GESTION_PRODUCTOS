# Demo técnica: gestión de productos

Microservicio REST con Java 21, Spring Boot 3, WebFlux y Spring Data R2DBC. El acceso a H2 usa el driver R2DBC, sin JDBC ni JPA.

## Ejecutar con Docker

Con Docker y Docker Compose instalados, desde la raíz del proyecto:

```bash
docker compose up --build
```

La API escucha en `http://localhost:8080`. Para detenerla: `docker compose down`.

La base H2 está en memoria. Al iniciar el proceso, `schema.sql` crea la tabla y `data.sql` inserta tres productos de ejemplo. Los cambios se pierden al detener el proceso. No se usa `ddl-auto: create-drop`: es una opción de Hibernate y no aplica a R2DBC.

## Compilar y probar

Se necesita un JDK 21. El Maven Wrapper descarga la versión de Maven del proyecto si no está disponible.

| Tarea | Linux / macOS | Windows (PowerShell) |
| --- | --- | --- |
| Ejecutar localmente | `./mvnw spring-boot:run` | `.\mvnw.cmd spring-boot:run` |
| Compilar, ejecutar pruebas y validar cobertura | `./mvnw clean verify` | `.\mvnw.cmd clean verify` |

El informe de JaCoCo queda en `target/site/jacoco/index.html`. `verify` falla si la cobertura de líneas de la implementación de servicios baja del 70 %. Las pruebas cubren el servicio con Mockito y `StepVerifier`, el contrato HTTP con `WebTestClient` y un recorrido CRUD completo contra H2 vía R2DBC.

## Consumir la API

### Postman

Importa la [colección Product Management](postman/Product%20Management.postman_collection.json) y ejecútala en orden con el **Collection Runner**. No necesitas un entorno: `baseUrl` apunta a `http://localhost:8080` y la petición de creación guarda automáticamente `productId`. El recorrido borra el producto que acaba de crear y comprueba que consultarlo después devuelve `404`.

### cURL

Ejemplos para una terminal Bash. En Windows puedes ejecutarlos en Git Bash o WSL.

```bash
# Listar los productos iniciales
curl -i http://localhost:8080/api/products

# Crear: devuelve 201 y la ruta del nuevo producto en Location
curl -i -X POST http://localhost:8080/api/products \
  -H 'Content-Type: application/json' \
  -d '{"name":"Monitor","price":179.90,"description":"24-inch monitor"}'

# Sustituye 4 por el ID devuelto si ya has creado otros productos
curl -i http://localhost:8080/api/products/4

curl -i -X PUT http://localhost:8080/api/products/4 \
  -H 'Content-Type: application/json' \
  -d '{"name":"Monitor","price":159.90}'

curl -i -X DELETE http://localhost:8080/api/products/4

# Después del borrado: 404 Not Found
curl -i http://localhost:8080/api/products/4
```

`name` es obligatorio (máximo 120 caracteres), `price` debe ser positivo y admite hasta dos decimales, y `description` es opcional (máximo 500 caracteres). `PUT` reemplaza los campos editables: si se omite `description`, se borra su valor anterior. Los errores se devuelven como `application/problem+json`; los errores de validación incluyen una lista de campos en `errors`.

## Organización del código

Las capas viven directamente dentro de `com.example.productmanagement`: `presentation` contiene el controlador y los `Request`/`Response`, `service` separa interfaces e implementaciones, `persistence` separa entidades y repositorios, `mapper` convierte entre DTO y entidad, y `exception` centraliza el manejo de errores. El listado retorna `Flux`; las operaciones sobre un producto retornan `Mono`.
