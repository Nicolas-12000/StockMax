# StockMax

Aplicación de ejemplo para gestión de inventario (compras, ventas, productos, proveedores, clientes).

## Resumen

Proyecto Spring Boot con JPA/Hibernate y H2 (runtime) para demo de inventario. Incluye entidades, DTOs, servicios y controladores REST para gestionar productos, compras, ventas y reportes simples.

## Requisitos

- Java 21
- Maven (opcional: se incluye el wrapper `mvnw`)
- IDE (IntelliJ, VS Code) con soporte Lombok (se recomienda instalar el plugin Lombok)

## Build y ejecución (Windows PowerShell)

Desde la raíz del proyecto:

```
# Compilar (usar wrapper incluido)
.\mvnw.cmd -DskipTests package

# Ejecutar con Spring Boot (modo desarrollo)
.\mvnw.cmd spring-boot:run

# O ejecutar el JAR generado
java -jar target/StockMax-0.0.1-SNAPSHOT.jar
```

Si prefieres usar Maven instalado globalmente:

```powershell
mvn -DskipTests package
mvn spring-boot:run
```

## Base de datos

Se utiliza H2 en runtime (driver incluido). Puedes habilitar la consola H2 añadiendo en `application.properties`:

```properties
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

Luego acceder en `http://localhost:8080/h2-console` (si la aplicació narranca en el puerto 8080). URL JDBC típica: `jdbc:h2:mem:testdb`.


## Uso de `.env` (la aplicación lo carga automáticamente)

La aplicación ahora carga variables desde un archivo `.env` en la raíz del proyecto durante el arranque. Esto se realiza mediante la dependencia `java-dotenv` y un inicializador (no hace falta ningún script externo).

Pasos:

```powershell
# Copiar ejemplo a .env (local, no versionado)
copy .env.example .env

# Editar .env con credenciales reales (no lo comitees)

# Ejecutar la app normalmente
.\mvnw.cmd spring-boot:run
```

Qué se carga automáticamente:
- Todas las variables definidas en `.env` quedan disponibles como propiedades en Spring Environment.
- Además, si estableces `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME` y `DB_PASSWORD`, se construirá automáticamente `spring.datasource.url` apuntando a MySQL y se configurarán `spring.datasource.username` y `spring.datasource.password`.

Si preferís no usar `.env`, también funcionan las otras opciones (variables de entorno del sistema, `-D` JVM, `application-<profile>.properties`).


## Endpoints principales

- Productos
  - `POST /api/productos` — Crear producto
  - `GET  /api/productos` — Listar productos
  - `GET  /api/productos/stock-bajo` — Productos con stock bajo (<5)

- Compras
  - `POST /api/compras` — Registrar compra (aumenta stock)
  - `GET  /api/compras` — Listar compras
  - `GET  /api/compras/{id}` — Obtener compra por id

- Ventas
  - `POST /api/ventas` — Crear venta (creada en estado CREADA)
  - `PUT  /api/ventas/{id}/confirmar` — Confirmar venta (decrementa stock)
  - `PUT  /api/ventas/{id}/cancelar` — Cancelar venta (revierte stock si estaba confirmada)
  - `GET  /api/ventas` — Listar ventas
  - `GET  /api/ventas/{id}` — Obtener venta por id

- Reportes
  - `GET /api/reportes/productos-mas-vendidos`
  - `GET /api/reportes/stock-por-proveedor-categoria`
  - `GET /api/reportes/ventas-mensuales-clientes`

## Ejemplos rápidos (curl)

Crear producto:

```bash
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{"codigo":"P001","nombre":"Monitor 24\"","descripcion":"Monitor FullHD","precioVenta":150.00,"categoriaId":1}'
```

Registrar compra (aumenta stock):

```bash
curl -X POST http://localhost:8080/api/compras \
  -H "Content-Type: application/json" \
  -d '{"proveedorId":1, "detalles":[{"productoId":1,"cantidad":10,"precioCompra":100.00}]}'
```

Crear venta (reserva en estado CREADA):

```bash
curl -X POST http://localhost:8080/api/ventas \
  -H "Content-Type: application/json" \
  -d '{"clienteId":1, "detalles":[{"productoId":1,"cantidad":2,"precioVenta":150.00}]}'
```

Confirmar venta:

```bash
curl -X PUT http://localhost:8080/api/ventas/1/confirmar
```

## Consideraciones importantes

- Lombok: algunas clases usan Lombok (`@Builder`, `@Data`, `@Getter/@Setter`). Si tu IDE muestra errores, instala el plugin Lombok y habilita el procesador de anotaciones.
- He corregido warnings relacionados con `@Builder` (añadí `@Builder.Default` para campos con valores por defecto) para evitar que Lombok ignore inicializadores.
- Validaciones: los DTOs usan `jakarta.validation` — la API validará payloads entrantes y el `GlobalExceptionHandler` devuelve respuestas legibles cuando fallan las validaciones.

## Tests

No hay tests automáticos incluidos por defecto en este cambio, pero puedes ejecutar:

```powershell
.\mvnw.cmd test
```