# Sistema de Ventas — API REST (Prueba Técnica Backend Java / Spring Boot)

API REST de un sistema simple de ventas: administración de **clientes** y **artículos**, y registro de
**ventas** que asocian un cliente con una o más líneas de detalle (artículo + cantidad). Protegida con
**JWT**.

## Stack técnico

| Aspecto        | Elección                                              |
|----------------|--------------------------------------------------------|
| Lenguaje       | Java 8                                                  |
| Framework      | Spring Boot 2.7.18 (Web, Data JPA, Validation, Security)|
| Base de datos  | MySQL 8 (único motor soportado, según el enunciado)     |
| Build          | Maven                                                   |
| Seguridad      | Spring Security + JWT (jjwt 0.11.5)                     |
| Documentación  | springdoc-openapi (Swagger UI)                          |
| Testing        | JUnit 5, Mockito, MockMvc, Testcontainers (MySQL real)  |

> Se usó Spring Boot 2.7.x porque es la última línea de Spring Boot con soporte para Java 8 (Spring Boot 3
> requiere Java 17). Por eso todas las anotaciones de JPA/Validation/Servlet son `javax.*` y no `jakarta.*`.

## Estructura del proyecto

```
src/main/java/com/pruebatecnica/ventas/
├── VentasApiApplication.java     # Clase principal
├── config/                       # Seguridad (config), OpenAPI, seed de usuario inicial
├── security/                     # Filtro JWT, proveedor de tokens, UserDetailsService
├── domain/                       # Entidades JPA
├── repository/                   # Repositorios Spring Data JPA (+ Specifications de Venta)
├── dto/                          # DTOs de request/response (nunca se exponen entidades)
├── mapper/                       # Mapeo manual entidad <-> DTO
├── service/ (+ impl/)            # Lógica de negocio, transacciones
├── controller/                   # Controladores REST
└── exception/                    # Excepciones de negocio + manejador global
```

## Cómo levantar el proyecto

### Opción A: Docker Compose (recomendada, no requiere instalar MySQL ni Maven)

```bash
docker compose up --build
```

Esto levanta MySQL 8 y la API (puerto `8080`), con la base de datos `ventas_db` creada automáticamente.

### Opción B: Maven + MySQL local

1. Tener MySQL 8 corriendo y crear la base de datos (o dejar que `createDatabaseIfNotExist=true` lo haga,
   si el usuario de MySQL tiene permisos para crear esquemas):

   ```sql
   CREATE DATABASE ventas_db;
   CREATE USER 'ventas_user'@'%' IDENTIFIED BY 'ventas_pass';
   GRANT ALL PRIVILEGES ON ventas_db.* TO 'ventas_user'@'%';
   ```

2. Ejecutar la aplicación:

   ```bash
   mvn spring-boot:run
   ```

   O compilar y ejecutar el jar:

   ```bash
   mvn clean package -DskipTests
   java -jar target/ventas-api-1.0.0.jar
   ```

### Variables de entorno (todas tienen un valor por defecto pensado para desarrollo local)

| Variable            | Default                                          | Descripción                          |
|---------------------|---------------------------------------------------|---------------------------------------|
| `DB_HOST`           | `localhost`                                        | Host de MySQL                         |
| `DB_PORT`           | `3306`                                             | Puerto de MySQL                       |
| `DB_NAME`           | `ventas_db`                                        | Nombre de la base de datos            |
| `DB_USER`           | `ventas_user`                                      | Usuario de MySQL                      |
| `DB_PASSWORD`       | `ventas_pass`                                      | Contraseña de MySQL                   |
| `JWT_SECRET`        | *(valor de ejemplo en `application.yml`)*          | Clave para firmar los JWT (HS256)     |
| `JWT_EXPIRATION_MS` | `3600000` (1 hora)                                 | Tiempo de expiración del token        |

**Importante:** `JWT_SECRET` trae un valor de ejemplo solo para que el proyecto funcione "out of the box".
En cualquier entorno real debe sobrescribirse con una variable de entorno.

## Autenticación

No se pidió un CRUD de usuarios, así que se optó por lo mínimo indispensable: una tabla `usuarios` y un
`CommandLineRunner` (`DataSeeder`) que crea un usuario administrador la primera vez que arranca la
aplicación (si la tabla está vacía):

```
usuario:    admin
contraseña: admin123
```

Login:

```http
POST /api/auth/login
Content-Type: application/json

{ "username": "admin", "password": "admin123" }
```

Respuesta:

```json
{ "token": "eyJhbGciOiJI...", "type": "Bearer", "expiresInMs": 3600000 }
```

El resto de los endpoints (`/api/clientes/**`, `/api/articulos/**`, `/api/categorias/**`, `/api/ventas/**`)
requieren el header `Authorization: Bearer <token>`.

## Documentación interactiva (Swagger)

Con la aplicación corriendo: **http://localhost:8080/swagger-ui.html**

## Colección de ejemplos

Ver [`requests.http`](./requests.http) — pensado para la extensión **REST Client** de VS Code o el cliente
HTTP nativo de IntelliJ/WebStorm. Incluye ejemplos de todos los endpoints, casos de error (409, 401, 400) y
el flujo completo de login → crear cliente/artículo → registrar venta → cancelar venta.

## Cómo correr los tests

```bash
mvn test
```

- **Pruebas unitarias** (`src/test/.../service/*Test.java`): JUnit 5 + Mockito, sin contexto de Spring ni
  base de datos. Cubren el cálculo del total de una venta, la validación/descuento de stock, la reposición
  de stock al cancelar, y las validaciones de duplicados (email de cliente, código de artículo).
- **Pruebas de integración** (`src/test/.../controller/*IT.java`): `@SpringBootTest` + `MockMvc`, contra un
  **MySQL real levantado con Testcontainers** (no H2 ni una base en memoria). **Requieren Docker
  disponible** en la máquina donde se ejecutan los tests, porque Testcontainers levanta el contenedor de
  MySQL automáticamente.

> Si `mvn test` se ejecuta en un entorno sin Docker, las pruebas de integración fallarán al no poder
> levantar el contenedor. Las pruebas unitarias no se ven afectadas por esto.

## Decisiones de diseño relevantes

- **DTOs siempre, nunca entidades en la API.** Los mappers son manuales (sin MapStruct/ModelMapper) para
  que la transformación quede explícita y sea fácil de explicar/depurar, en línea con el pedido de evitar
  dependencias que oculten la lógica.

- **Baja lógica por defecto, baja física opcional y protegida.** `DELETE /api/clientes/{id}` y
  `DELETE /api/articulos/{id}` hacen baja lógica (`activo = false`) por defecto. Con
  `?permanente=true` intentan un borrado físico, que se rechaza (400) si el registro tiene ventas
  asociadas, para no romper el historial. Me pareció el balance más razonable entre lo que pide el
  enunciado ("baja lógica o física") y la integridad de los datos históricos.

- **Folio de venta = id autogenerado, formateado.** El folio (`V-000123`) se genera a partir del id
  `IDENTITY` de la venta una vez persistida (se guarda, se obtiene el id, se actualiza el folio y se vuelve
  a guardar). Es simple y aprovecha que MySQL ya garantiza que el id es único e incremental, sin necesitar
  una tabla contador aparte. La contra: el folio depende del id interno. Si en el futuro se necesitara un
  folio independiente del id (por ejemplo, reiniciable por año o por sucursal), lo cambiaría por una tabla
  contador dedicada con su propio bloqueo.

- **Precio "congelado" en el detalle de venta.** `VentaDetalle.precioUnitario` guarda el precio del
  artículo *al momento de la venta*. Si el precio del artículo cambia después, las ventas históricas no se
  ven afectadas. El total de la venta siempre se recalcula en el servidor a partir de estos valores; nunca
  se confía en un total enviado por el cliente.

- **Bloqueo pesimista al tocar stock.** `ArticuloRepository.buscarPorIdParaActualizarStock` usa
  `@Lock(PESSIMISTIC_WRITE)` y se usa específicamente al descontar stock (crear venta) y al reponerlo
  (cancelar venta), para que dos ventas concurrentes sobre el mismo artículo no puedan generar stock
  negativo (problema clásico de "lost update"). El resto de las operaciones de artículo (CRUD normal) usan
  `findById` sin bloqueo, porque no comprometen el stock.

- **Filtros de venta con `Specification`.** El listado de ventas admite filtros combinables por cliente,
  estado y rango de fechas (`VentaSpecifications`), usando `JpaSpecificationExecutor` en vez de escribir
  una consulta por cada combinación posible.

- **Testcontainers en vez de H2 para las pruebas de integración.** El enunciado es explícito en que no se
  acepta otro motor de base de datos, "ni H2 [...] en producción". Para no dejar dudas, incluso en tests se
  usa un MySQL real (vía Testcontainers) en lugar de una base en memoria, a costa de que los tests de
  integración requieran Docker.

- **`Categoria` como entidad adicional simple (extra).** Relación `Articulo -> Categoria` (opcional,
  `categoriaId` nullable). Se implementó CRUD mínimo (crear, listar paginado, obtener por id) para no
  desviar el foco del alcance principal; ver la sección siguiente.

- **`open-in-view: false`** para evitar el antipatrón de dejar la sesión de Hibernate abierta durante el
  renderizado de la respuesta. Por eso todo el mapeo a DTO ocurre **dentro** de los métodos `@Transactional`
  de los servicios, mientras la sesión sigue activa.

## Qué haría distinto con más tiempo

- **Optimizar el N+1 en el listado de ventas.** `Venta -> Cliente` y `VentaDetalle -> Articulo` son
  `LAZY`; al mapear una página de ventas a DTO se dispara una consulta adicional por cada asociación. Para
  el alcance de la prueba no lo consideré crítico, pero en un listado con muchos resultados lo resolvería
  con `@EntityGraph` o `JOIN FETCH` en la consulta de listado (o devolviendo un DTO "resumen" sin detalle en
  el listado, y dejando el detalle completo solo para `GET /api/ventas/{id}`).

- **Migraciones versionadas.** Hoy se usa `ddl-auto: update` por simplicidad de evaluación. En un proyecto
  real usaría Flyway o Liquibase desde el principio.

- **Roles y autorización más granular.** Hoy cualquier usuario autenticado puede hacer cualquier operación.
  Con más tiempo agregaría roles (`ROLE_ADMIN`, `ROLE_VENDEDOR`) y `@PreAuthorize` para, por ejemplo,
  restringir la baja física de clientes/artículos solo a administradores.

- **CRUD completo de `Categoria`** (actualizar, eliminar) y un CRUD real de usuarios, si el negocio lo
  llegara a requerir — hoy están deliberadamente acotados al mínimo pedido por el enunciado.

- **Refresh tokens / invalidación de tokens (logout real).** Hoy el JWT es válido hasta que expira; no hay
  forma de invalidarlo antes. Agregaría una lista de revocación o refresh tokens de corta duración.

- **Más filtros de búsqueda** (nombre/código parcial con `LIKE`, rango de precios) usando el mismo
  mecanismo de `Specification` ya implementado para ventas.

- **Pipeline de CI** (GitHub Actions) que corra `mvn test` en cada push, con un servicio de MySQL o
  Testcontainers, y publique el reporte de cobertura.

- **Cobertura de tests adicional:** casos de concurrencia real (dos hilos comprando el último artículo en
  stock), y tests de seguridad con distintos roles una vez que existan.

## Notas sobre el entorno de compilación

El proyecto se desarrolló apuntando a Java 8 (`<java.version>1.8</java.version>` en el `pom.xml`). Si se
compila con un JDK más nuevo (11, 17, 21), Maven debería seguir generando bytecode compatible con Java 8 sin
problemas hasta JDK 17 aproximadamente; con JDKs muy recientes algunos entornos requieren pasar
explícitamente `--release 8` o usar un JDK 8/11 real para evitar advertencias de "source/target obsoleto".
