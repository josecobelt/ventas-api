# API REST de un sistema de ventas (Backend Java / Spring Boot)

## Stack

| Aspecto        | Elección                                              |
|----------------|--------------------------------------------------------|
| Lenguaje       | Java 8                                                  |
| Framework      | Spring Boot 2.7.18 (Web, Data JPA, Validation, Security)|
| Base de datos  | MySQL 8                                                 |
| Build          | Maven                                                   |
| Seguridad      | Spring Security + JWT (jjwt 0.11.5)                     |
| Documentación  | springdoc-openapi (Swagger UI)                          |
| Testing        | JUnit 5, Mockito, MockMvc, Testcontainers               |

## Estructura

src/main/java/com/pruebatecnica/ventas/
├── VentasApiApplication.java     # Principal
├── config/                       # Seguridad (config), OpenAPI, seed de usuario inicial
├── security/                     # Filtro JWT, proveedor de tokens, UserDetailsService
├── domain/                       # Entidades JPA
├── repository/                   # Repositorios Spring Data JPA (+ Specifications de Venta)
├── dto/                          # DTOs de request/response (nunca se exponen entidades)
├── mapper/                       # Mapeo manual entidad <-> DTO
├── service/ (+ impl/)            # Lógica de negocio, transacciones
├── controller/                   # Controladores REST
└── exception/                    # Excepciones de negocio + manejador global

## Instrucciones

### Opción A: Docker Compose (recomendada, no requiere nd)

```bash
docker compose up --build
```
**Aguas**: puede haber problemas con el puerto 3306 que es mysql que aveces se ejecuta ahi

Esto levanta MySQL 8 y la API (puerto `8080`), con la base de datos `ventas_db` creada automáticamente.

### Opción B: Maven + MySQL local

1. dejar que `createDatabaseIfNotExist=true` haga la bd si el usuario de MySQL tiene permisos para crear esquemas y/o:

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

### Variables de entorno (todas tienen un valor por defecto si)

| Variable            | Default                                          | Descripción                          |
|---------------------|---------------------------------------------------|---------------------------------------|
| `DB_HOST`           | `localhost`                                        | Host de MySQL                         |
| `DB_PORT`           | `3306`                                             | Puerto de MySQL                       |
| `DB_NAME`           | `ventas_db`                                        | Nombre de la base de datos            |
| `DB_USER`           | `ventas_user`                                      | Usuario de MySQL                      |
| `DB_PASSWORD`       | `ventas_pass`                                      | Contraseña de MySQL                   |
| `JWT_SECRET`        | *(valor de ejemplo en `application.yml`)*          | Clave para firmar los JWT (HS256)     |
| `JWT_EXPIRATION_MS` | `3600000` (1 hora)                                 | Tiempo de expiración del token        |
 `JWT_SECRET` trae un valor de ejemplo aguas

## Autenticación

Una tabla `usuarios` y un
`CommandLineRunner` (`DataSeeder`) que crea un usuario administrador la primera vez que arranca la
aplicación (si la tabla está vacía):

ej 

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

Con la aplicación corriendo: http://localhost:8080/swagger-ui.html desde el navegador.

## Colección de ejemplos

El enunciado permite Postman o `.http`; se incluyen ambos para no depender de una sola herramienta:

- [`requests.http`](./requests.http) — para la extensión REST Client de VS Code o el cliente HTTP nativo
  de IntelliJ/WebStorm.
- [`postman_collection.json`](./postman_collection.json) — colección de Postman (v2.1), organizada en
  carpetas (Autenticación, Clientes, Categorías, Artículos, Ventas, Seguridad). El request "Login" tiene un
  script de test que guarda automáticamente el token JWT en la variable de colección `{{token}}`, que ya
  queda disponible para el resto de los requests sin copiar/pegar nada a mano. Para usarla: Import en
  Postman → seleccionar el archivo → correr primero "Login" → ejecutar el resto.
Ambos cubren todos los endpoints, incluyendo casos de error (409 por duplicados, 401 sin token o con
credenciales inválidas, 400 por validación, 409 por stock insuficiente) y el flujo completo:
login → crear cliente/artículo → registrar venta → cancelar venta.

## Cómo correr los tests

```bash
mvn test
```

- Pruebas unitarias (`src/test/.../service/*Test.java`): JUnit 5 + Mockito, cubren el cálculo del total de una venta, la validación/descuento de stock, la reposición de stock al cancelar, y las validaciones de duplicados (email de cliente, código de artículo).

- Pruebas de integración (`src/test/.../controller/*IT.java`): `@SpringBootTest` + `MockMvc` cubren los 5 controllers
  (`AuthController`, `ClienteController`, `ArticuloController`, `CategoriaController`, `VentaController`).
  Requieren Docker disponible en la máquina donde se ejecutan los tests, porque testcontainers levanta
  el contenedor de MySQL automáticamente.

> Si `mvn test` se ejecuta en un entorno sin Docker, las pruebas de integración fallarán al no poder
> levantar el contenedor. Las pruebas unitarias no se ven afectadas por esto.

## Decisiones

- DTOs siempre. Los mappers son manuales (sin MapStruct/ModelMapper) para que todo sea transapernte y pues explicito.,

- Baja lógica por defecto, baja física opcional y protegida. `DELETE /api/clientes/{id}` y
  `DELETE /api/articulos/{id}` hacen baja lógica (`activo = false`) por defecto. Con
  `?permanente=true` intentan un borrado físico, que se rechaza (400) si el registro tiene ventas
  asociadas, para no romper el historial. Me pareció el balance más razonable entre lo que pide el
  enunciado ("baja lógica o física") y la integridad de los datos históricos.

- Folio de venta = id autogenerado, formateado. El folio (`V-000123`) se genera a partir del id
  `IDENTITY` de la venta una vez persistida (se guarda, se obtiene el id, se actualiza el folio y se vuelve
  a guardar). Es simple y aprovecha que MySQL ya garantiza que el id es único e incremental, sin necesitar
  una tabla contador aparte pero la contra: el folio depende del id interno.

- Precio "congelado" en el detalle de venta. `VentaDetalle.precioUnitario` guarda el precio del
  artículo al momento de la venta.

- Bloqueo pesimista al tocar stock. `ArticuloRepository.buscarPorIdParaActualizarStock` usa
  `@Lock(PESSIMISTIC_WRITE)` y se usa específicamente al descontar stock (crear venta) y al reponerlo
  (cancelar venta), para que dos ventas concurrentes sobre el mismo artículo no puedan generar stock
  negativo (problema clásico de "lost update"). El resto de las operaciones de artículo (CRUD normal) usan
  `findById` sin bloqueo, porque no comprometen el stock.

- Filtros de venta con `Specification`. El listado de ventas admite filtros combinables por cliente,
  estado y rango de fechas (`VentaSpecifications`), usando `JpaSpecificationExecutor` en vez de escribir
  una consulta por cada combinación posible.

- `Categoria` como entidad adicional simple (extra). Relación `Articulo -> Categoria` (opcional,
  `categoriaId` nullable? creo que asi se dice).

- `open-in-view: false` para evitar el antipatrón de dejar la sesión de Hibernate abierta durante el
  renderizado de la respuesta. Por eso todo el mapeo a DTO ocurre dentro de los métodos `@Transactional`
  de los servicios, mientras la sesión sigue activa.