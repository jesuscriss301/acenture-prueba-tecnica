# Franchise API

API REST **reactiva** para administrar franquicias, sus sucursales y los productos de cada sucursal. Está construida con **Spring Boot 4 + WebFlux**, sigue **arquitectura hexagonal (puertos y adaptadores)** organizada por funcionalidad y guarda los datos en **Amazon DynamoDB** con un diseño de tabla única.

> Prueba técnica — Desarrollador Backend.

---

## Tabla de contenido

1. [Tecnologías](#tecnologías)
2. [Arquitectura](#arquitectura)
3. [Estructura del proyecto](#estructura-del-proyecto)
4. [Modelo de datos en DynamoDB](#modelo-de-datos-en-dynamodb)
5. [Endpoints](#endpoints)
6. [Reglas de negocio y decisiones de diseño](#reglas-de-negocio-y-decisiones-de-diseño)
7. [Cómo ejecutar en local](#cómo-ejecutar-en-local)
8. [Pruebas](#pruebas)
9. [Documentación OpenAPI](#documentación-openapi)
10. [Flujo de trabajo con Git](#flujo-de-trabajo-con-git)
11. [Estado de los criterios de la prueba](#estado-de-los-criterios-de-la-prueba)

---

## Tecnologías

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje |
| Spring Boot 4.1 / Spring WebFlux | API reactiva sobre Netty (sin bloqueos), rutas funcionales (`RouterFunction`) |
| Project Reactor | `Mono` / `Flux` de punta a punta |
| AWS SDK v2 — DynamoDB Enhanced **Async** Client | Persistencia no bloqueante (`CompletableFuture` → `Mono`) |
| JUnit 5, Mockito, AssertJ, `StepVerifier`, `WebTestClient` | Pruebas unitarias |
| springdoc-openapi (Swagger UI) | Documentación de la API |
| Maven (wrapper incluido) | Construcción, empaquetado como **JAR** ejecutable |

---

## Arquitectura

Se aplica **arquitectura hexagonal** siguiendo los principios de **Clean Architecture**: el dominio está en el centro y no depende de Spring, de HTTP ni de DynamoDB. Las dependencias siempre apuntan hacia adentro.

```mermaid
flowchart LR
    Client(["Cliente HTTP"])

    subgraph INFRA_IN["Infraestructura · entrada"]
        Router["Router WebFlux"]
        Handler["Handler + DTOs"]
    end

    subgraph APP["Aplicación"]
        PortIn[["Puerto de entrada<br/>(UseCase)"]]
        Service["Servicio<br/>(caso de uso)"]
        PortOut[["Puerto de salida<br/>(RepositoryPort)"]]
    end

    subgraph DOMAIN["Dominio"]
        Model["Modelos y reglas<br/>Franchise · Branch · Product"]
    end

    subgraph INFRA_OUT["Infraestructura · salida"]
        Adapter["Adaptador DynamoDB"]
        Mapper["Mapper ↔ CatalogItem"]
    end

    DB[("Amazon DynamoDB<br/>tabla única")]

    Client --> Router --> Handler --> PortIn
    PortIn -. implementa .- Service
    Service --> Model
    Service --> PortOut
    PortOut -. implementa .- Adapter
    Adapter --> Mapper
    Adapter --> DB
```

**Reglas que se respetan:**

- **Dominio** (`domain`): records inmutables con sus validaciones (nombre obligatorio, máximo 100 caracteres, stock nunca negativo). Sin anotaciones de frameworks.
- **Aplicación** (`application`): casos de uso y puertos (interfaces). Los servicios **no tienen anotaciones de Spring**; se registran como beans en una clase `*BeanConfig` de infraestructura.
- **Infraestructura** (`infrastructure`): todo lo que habla con el exterior. `in/web` (rutas, handlers, DTOs) y `out/persistence` (adaptadores DynamoDB).
- Un módulo usa a otro **solo a través de sus puertos** (por ejemplo, `AddBranchService` consulta `FranchiseRepositoryPort` para validar que la franquicia exista).

### Organización por funcionalidad

En lugar de una carpeta global por capa, cada funcionalidad tiene sus propias capas. Así cada módulo se entiende y se cambia de forma aislada.

```mermaid
flowchart TB
    subgraph franchise["franchise"]
        f1["domain"] --- f2["application"] --- f3["infrastructure"]
    end
    subgraph branch["branch"]
        b1["domain"] --- b2["application"] --- b3["infrastructure"]
    end
    subgraph product["product"]
        p1["domain"] --- p2["application"] --- p3["infrastructure"]
    end
    subgraph shared["shared"]
        s1["excepciones y validaciones"] --- s2["config DynamoDB · manejo de errores · claves"]
    end

    branch -- "FranchiseRepositoryPort" --> franchise
    product -- "BranchRepositoryPort" --> branch
    product -- "FranchiseRepositoryPort" --> franchise
    franchise --> shared
    branch --> shared
    product --> shared
```

### Flujo de una petición (ejemplo: agregar producto)

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente
    participant R as ProductRouter
    participant H as ProductHandler
    participant S as AddProductService
    participant BP as BranchRepositoryPort
    participant PP as ProductRepositoryPort
    participant D as DynamoDB

    C->>R: POST /api/franchises/{fId}/branches/{bId}/products
    R->>H: add(request)
    H->>H: bodyToMono(AddProductRequest)
    H->>S: add(AddProductCommand)
    S->>S: Product.create(...) valida nombre y stock
    alt datos inválidos
        S-->>H: error DomainValidationException
        H-->>C: 400 BAD_REQUEST
    end
    S->>BP: existsById(fId, bId)
    BP->>D: GetItem(PK, SK=BRANCH#bId)
    D-->>BP: ítem / vacío
    alt la sucursal no existe
        S-->>H: error ResourceNotFoundException
        H-->>C: 404 NOT_FOUND
    end
    S->>PP: save(product)
    PP->>D: PutItem (condición: no existe)
    D-->>PP: OK
    PP-->>S: Mono<Product>
    S-->>H: Mono<Product>
    H-->>C: 201 Created + Location + JSON
```

Todo el recorrido es **no bloqueante**: cada paso devuelve `Mono`/`Flux` y el cliente de DynamoDB es asíncrono.

### Manejo de errores

Las excepciones se traducen en un único lugar (`ApiErrorResponses.withErrorHandling`) y todas las respuestas de error tienen el mismo formato:

| Excepción | HTTP | Ejemplo |
|---|---|---|
| `DomainValidationException` | 400 | Nombre vacío, stock negativo |
| `ServerWebInputException` | 400 | JSON mal formado |
| `ResourceNotFoundException` | 404 | Franquicia, sucursal o producto inexistente |

```json
{ "code": "NOT_FOUND", "message": "La franquicia 7d1c... no existe" }
```

---

## Estructura del proyecto

```text
src/main/java/com/acenture/prueba_tecnica
├── franchise
│   ├── domain/model/                 Franchise
│   ├── application
│   │   ├── port/in/                  CreateFranchiseUseCase, RenameFranchiseUseCase
│   │   ├── port/out/                 FranchiseRepositoryPort
│   │   └── service/                  CreateFranchiseService, RenameFranchiseService
│   └── infrastructure
│       ├── config/                   FranchiseBeanConfig
│       ├── in/web/                   FranchiseRouter, FranchiseHandler, dto/
│       └── out/persistence/          FranchiseDynamoDbAdapter, FranchiseItemMapper
├── branch                            (misma estructura) AddBranch, RenameBranch
├── product                           (misma estructura) AddProduct, DeleteProduct,
│                                     UpdateProductStock, RenameProduct, GetTopStockProducts
└── shared
    ├── domain/exception/             DomainValidationException, ResourceNotFoundException
    ├── domain/validation/            Validations
    └── infrastructure
        ├── config/                   DynamoDbConfig, DynamoDbProperties
        ├── persistence/              CatalogItem, DynamoDbKeys, DynamoDbQueries
        └── web/                      ApiErrorResponses, ErrorResponse, dto/RenameRequest

src/main/resources
├── application.properties
└── static/openapi.yaml               Contrato OpenAPI 3
```

---

## Modelo de datos en DynamoDB

### Modelo de dominio

```mermaid
erDiagram
    FRANCHISE ||--o{ BRANCH : "tiene"
    BRANCH ||--o{ PRODUCT : "ofrece"
    FRANCHISE {
        string id
        string name
    }
    BRANCH {
        string id
        string franchiseId
        string name
    }
    PRODUCT {
        string id
        string franchiseId
        string branchId
        string name
        int stock
    }
```

### Diseño de tabla única (single-table design)

Todo lo de una franquicia vive en **la misma partición** (`PK`). La clave de ordenamiento (`SK`) indica qué es cada ítem y refleja la jerarquía:

| Entidad | PK | SK | Atributos |
|---|---|---|---|
| Franquicia | `FRANCHISE#{franchiseId}` | `METADATA` | `entityType=FRANCHISE`, `name` |
| Sucursal | `FRANCHISE#{franchiseId}` | `BRANCH#{branchId}` | `entityType=BRANCH`, `name` |
| Producto | `FRANCHISE#{franchiseId}` | `BRANCH#{branchId}#PRODUCT#{productId}` | `entityType=PRODUCT`, `name`, `stock` |

```mermaid
flowchart LR
    P["Partición<br/>PK = FRANCHISE#f1"]
    P --> M["SK = METADATA<br/>Franquicia Norte"]
    P --> B1["SK = BRANCH#b1<br/>Sucursal Centro"]
    P --> B1P1["SK = BRANCH#b1#PRODUCT#p1<br/>Café · stock 10"]
    P --> B1P2["SK = BRANCH#b1#PRODUCT#p2<br/>Té · stock 30"]
    P --> B2["SK = BRANCH#b2<br/>Sucursal Norte"]
    P --> B2P1["SK = BRANCH#b2#PRODUCT#p3<br/>Pan · stock 7"]
```

**Patrones de acceso que resuelve:**

| Necesidad | Operación |
|---|---|
| Leer o validar una franquicia | `GetItem(PK, SK=METADATA)` |
| Leer o validar una sucursal | `GetItem(PK, SK=BRANCH#id)` |
| Leer, actualizar o eliminar un producto | `GetItem` / `PutItem` / `DeleteItem` con `SK=BRANCH#id#PRODUCT#id` |
| Todas las sucursales y productos de una franquicia | **Una** `Query(PK, begins_with(SK, "BRANCH#"))` |

**Por qué este diseño:** el endpoint de “mayor stock por sucursal” necesita todas las sucursales y productos de una franquicia; con una sola partición eso es una consulta, sin `Scan` y sin uniones. Además, las escrituras usan condiciones (`attribute_not_exists` al crear, `attribute_exists` al actualizar) para no sobrescribir datos por accidente.

---

## Endpoints

Base: `http://localhost:8080`

| # | Método | Ruta | Descripción | Respuesta |
|---|---|---|---|---|
| 1 | `POST` | `/api/franchises` | Crear franquicia | `201` |
| 2 | `POST` | `/api/franchises/{franchiseId}/branches` | Agregar sucursal | `201` |
| 3 | `POST` | `/api/franchises/{franchiseId}/branches/{branchId}/products` | Agregar producto | `201` |
| 4 | `DELETE` | `/api/franchises/{franchiseId}/branches/{branchId}/products/{productId}` | Eliminar producto | `204` |
| 5 | `PATCH` | `/api/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock` | Modificar stock | `200` |
| 6 | `GET` | `/api/franchises/{franchiseId}/top-stock-products` | Producto con más stock por sucursal | `200` |
| Extra | `PATCH` | `/api/franchises/{franchiseId}/name` | Cambiar nombre de la franquicia | `200` |
| Extra | `PATCH` | `/api/franchises/{franchiseId}/branches/{branchId}/name` | Cambiar nombre de la sucursal | `200` |
| Extra | `PATCH` | `/api/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name` | Cambiar nombre del producto | `200` |

### Ejemplos con `curl`

```bash
# 1. Crear franquicia
curl -X POST http://localhost:8080/api/franchises \
  -H "Content-Type: application/json" \
  -d '{"name":"Franquicia Norte"}'
# -> 201 {"id":"<franchiseId>","name":"Franquicia Norte"}

# 2. Agregar sucursal
curl -X POST http://localhost:8080/api/franchises/<franchiseId>/branches \
  -H "Content-Type: application/json" \
  -d '{"name":"Sucursal Centro"}'

# 3. Agregar producto
curl -X POST http://localhost:8080/api/franchises/<franchiseId>/branches/<branchId>/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Café","stock":25}'

# 4. Eliminar producto
curl -X DELETE http://localhost:8080/api/franchises/<franchiseId>/branches/<branchId>/products/<productId>

# 5. Modificar stock
curl -X PATCH http://localhost:8080/api/franchises/<franchiseId>/branches/<branchId>/products/<productId>/stock \
  -H "Content-Type: application/json" \
  -d '{"stock":40}'

# 6. Producto con más stock por sucursal
curl http://localhost:8080/api/franchises/<franchiseId>/top-stock-products
```

Respuesta del endpoint 6:

```json
[
  { "branchId": "b1", "branchName": "Sucursal Centro", "productId": "p2", "productName": "Té",  "stock": 30 },
  { "branchId": "b2", "branchName": "Sucursal Norte",  "productId": "p3", "productName": "Pan", "stock": 7 }
]
```

---

## Reglas de negocio y decisiones de diseño

**Validaciones del dominio**

- Los nombres son obligatorios, se recortan los espacios y tienen máximo 100 caracteres.
- El stock es obligatorio y nunca puede ser negativo (0 sí es válido).
- Se valida **antes** de consultar la base de datos: una petición inválida no genera lecturas en DynamoDB.
- Los ids se generan en el dominio (UUID).

**Producto con más stock por sucursal** (`GetTopStockProductsService`)

```mermaid
flowchart TD
    A["GET /top-stock-products"] --> B{"¿Existe la franquicia?"}
    B -- No --> E["404 NOT_FOUND"]
    B -- Sí --> C["Sucursales de la franquicia<br/>(mapa id → nombre)"]
    C --> D["Productos de la franquicia"]
    D --> F["Descarta productos de sucursales inexistentes"]
    F --> G["groupBy(branchId)"]
    G --> H["reduce: mayor stock<br/>empate → nombre alfabético → id"]
    H --> I["Ordena por nombre de sucursal"]
    I --> J["200 OK · lista"]
```

- Devuelve **un producto por sucursal**, indicando a qué sucursal pertenece.
- Las sucursales **sin productos no aparecen**.
- **Empate** en stock: gana el producto con nombre alfabéticamente menor (y luego el id), para que el resultado sea siempre el mismo.
- El resultado se ordena por nombre de sucursal.
- El cálculo se hace con operadores reactivos (`groupBy` + `reduce`) sin bloquear.

**Otras decisiones**

| Decisión | Motivo |
|---|---|
| Rutas funcionales (`RouterFunction`) en vez de `@RestController` | Estilo idiomático de WebFlux; separa ruteo y lógica del handler. |
| Empaquetado JAR (no WAR) | Netty embebido; pensado para contenedores. |
| Contrato OpenAPI escrito a mano (contract-first) | springdoc no detecta rutas funcionales sin llenar la capa web de anotaciones. |
| Crear devuelve `201` + `Location`; eliminar devuelve `204` | Semántica HTTP estándar. |
| `PATCH` para cambios parciales (stock, nombre) | Solo se modifica un campo del recurso. |
| Errores de lista: se junta la lista antes de responder | Así un 404 se devuelve bien en vez de cortar una respuesta ya iniciada. |

---

## Cómo ejecutar en local

### Requisitos

- **JDK 21** (por ejemplo, Eclipse Temurin) con `JAVA_HOME` configurado.
- Una tabla de DynamoDB: en AWS o con **DynamoDB Local** (requiere Docker).
- No hace falta instalar Maven: el proyecto incluye el wrapper (`mvnw` / `mvnw.cmd`).

### Variables de configuración

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `DYNAMODB_TABLE` | `franchises` | Nombre de la tabla |
| `AWS_REGION` | `us-east-2` | Región de AWS |
| `DYNAMODB_ENDPOINT` | *(vacío)* | URL de DynamoDB Local; vacío = AWS real |
| `AWS_PROFILE` | — | Perfil de credenciales de AWS (solo contra AWS real) |

Las credenciales se toman de la cadena estándar del SDK de AWS (variables de entorno, perfil, `aws login`, rol IAM). **Nunca se guardan en el repositorio.**

### Opción A — DynamoDB Local (sin cuenta de AWS)

```bash
# 1. Levantar DynamoDB Local
docker run -d --name dynamodb-local -p 8000:8000 amazon/dynamodb-local

# 2. Crear la tabla (las credenciales pueden ser falsas para DynamoDB Local)
AWS_ACCESS_KEY_ID=local AWS_SECRET_ACCESS_KEY=local \
aws dynamodb create-table \
  --endpoint-url http://localhost:8000 --region us-east-2 \
  --table-name franchises \
  --attribute-definitions AttributeName=PK,AttributeType=S AttributeName=SK,AttributeType=S \
  --key-schema AttributeName=PK,KeyType=HASH AttributeName=SK,KeyType=RANGE \
  --billing-mode PAY_PER_REQUEST

# 3. Arrancar la API apuntando a DynamoDB Local
DYNAMODB_ENDPOINT=http://localhost:8000 \
AWS_ACCESS_KEY_ID=local AWS_SECRET_ACCESS_KEY=local \
./mvnw spring-boot:run
```

En **Windows (PowerShell)**:

```powershell
$env:DYNAMODB_ENDPOINT = "http://localhost:8000"
$env:AWS_ACCESS_KEY_ID = "local"
$env:AWS_SECRET_ACCESS_KEY = "local"
.\mvnw.cmd spring-boot:run
```

### Opción B — DynamoDB en AWS

1. Inicia sesión: `aws login --region us-east-2 --profile <tu-perfil>`.
2. Crea la tabla `franchises` con `PK` (String, partition key) y `SK` (String, sort key), con el mismo comando de arriba sin `--endpoint-url`.
3. Arranca la API:

```bash
AWS_PROFILE=<tu-perfil> AWS_REGION=us-east-2 ./mvnw spring-boot:run
```

### Generar y ejecutar el JAR

```bash
./mvnw clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

La API queda disponible en `http://localhost:8080`.

---

## Pruebas

```bash
./mvnw test          # Linux / macOS
.\mvnw.cmd test      # Windows
```

Las pruebas son **unitarias** y no necesitan AWS ni DynamoDB (los puertos se reemplazan con mocks).

| Capa | Herramientas | Qué se prueba |
|---|---|---|
| Dominio | JUnit 5, AssertJ | Validaciones de `Franchise` y `Product` |
| Casos de uso | Mockito, `StepVerifier` | Lógica de cada servicio: éxito, 404, validaciones, errores del repositorio, que no se consulte la base si los datos son inválidos |
| Web | `WebTestClient` | Códigos HTTP, cuerpo JSON, encabezado `Location`, formato de errores, JSON inválido |

Cobertura por endpoint:

| Endpoint | Pruebas |
|---|---|
| Crear franquicia | 8 (+4 del modelo) |
| Agregar sucursal | 9 |
| Agregar producto | 9 (+3 del modelo) |
| Eliminar producto | 9 |
| Modificar stock | 10 |
| Producto con más stock | 11 |
| Renombrar franquicia | 8 |
| Renombrar sucursal | 8 |
| Renombrar producto | 8 |

---

## Documentación OpenAPI

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **Contrato:** `http://localhost:8080/openapi.yaml` (archivo `src/main/resources/static/openapi.yaml`)

El contrato documenta los 9 endpoints con sus parámetros, cuerpos, respuestas (201/200/204/400/404) y ejemplos.

---

## Flujo de trabajo con Git

- Rama principal: `main`.
- Commits pequeños, uno por funcionalidad, con mensajes en formato **Conventional Commits** (`feat:`, `refactor:`, `test:`, `docs:`).

```mermaid
gitGraph
    commit id: "first commit"
    commit id: "feat: crear franquicia"
    commit id: "refactor: tabla única"
    commit id: "feat: agregar sucursal"
    commit id: "feat: agregar producto"
    commit id: "feat: eliminar producto"
    commit id: "feat: modificar stock"
    commit id: "feat: mayor stock"
    commit id: "feat: renombrar x3"
    commit id: "test: más pruebas"
    commit id: "docs: OpenAPI y README"
```

---

## Estado de los criterios de la prueba

| Criterio | Estado |
|---|---|
| Proyecto en Spring Boot | ✅ |
| Endpoints 1 a 6 (crear franquicia, sucursal, producto, eliminar, stock, mayor stock) | ✅ |
| Persistencia en la nube (DynamoDB) | ✅ Código listo · la tabla en AWS se crea con Terraform (pendiente) |
| Programación reactiva | ✅ WebFlux + Reactor + cliente asíncrono de DynamoDB |
| Endpoints para cambiar nombres (franquicia, sucursal, producto) | ✅ |
| Pruebas unitarias | ✅ |
| Documentación (README + OpenAPI) | ✅ |
| Empaquetado con Docker | ⏳ Pendiente |
| Infraestructura como código (Terraform) | ⏳ Pendiente |
| Despliegue en la nube | ⏳ Pendiente |
