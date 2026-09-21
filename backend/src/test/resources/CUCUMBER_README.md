# 🧪 Testing BDD con Cucumber en FitStore

Suite BDD (Behavior-Driven Development) con **Cucumber** y **Gherkin en español**, integrada con Spring Boot y JUnit 5.

Estado: **15 escenarios verdes** (`mvn test` → 18 tests, 0 fallos).

## 📁 Estructura de Tests

```
backend/src/test/
├── java/com/fitstore/
│   ├── CucumberRunner.java          ← Ejecutor JUnit Platform (glue: com.fitstore.steps)
│   ├── TestDataRestorer.java        ← Restaura la BD (12 productos) al finalizar
│   └── steps/CompraSteps.java       ← Step definitions de todos los escenarios
└── resources/features/
    └── compra.feature               ← 15 escenarios Gherkin en español
```

## 🚀 Configuración (`pom.xml`)

- Cucumber: `cucumber-java`, `cucumber-spring`, `cucumber-junit-platform-engine` **7.14.0**
- JUnit Platform: `junit-platform-suite` y `junit-platform-engine` **1.10.2**
- Surefire: incluye `CucumberRunner` y `**/*Test.java`
- Runner: `CucumberRunner.java` con `@Suite`, `@IncludeEngines("cucumber")` y glue `classpath:com.fitstore.steps`

> Los escenarios usan **la BD, Redis y RabbitMQ reales** con `@SpringBootTest(webEnvironment = MOCK)` + `@AutoConfigureMockMvc` (no levanta otro puerto). Al terminar, `TestDataRestorer` vuelve a insertar el catálogo de 12 productos, borra pedidos/ítems y vacía la caché de Redis.

## 🏃 Ejecutar Tests

```bash
# Toda la suite (unitarios + BDD)
mvn test

# Solo Cucumber
mvn test -Dtest=CucumberRunner

# Escenarios con un tag
mvn test -Dtest=CucumberRunner -Dcucumber.filter.tags="@admin"
```

## 📝 Escenarios Cubiertos (15)

Compra exitosa y cálculo del total, stock insuficiente, login fallido (clave incorrecta / usuario no registrado), verificación de stock por API y en Redis, stock actualizado en Redis tras la compra, filtros por categoría, búsqueda por nombre, carrito y localStorage, estados de pedido, cancelación con restauración de stock, notificaciones asíncronas por RabbitMQ y panel admin.

## 📌 Convenciones

- Feature files: `src/test/resources/features/*.feature`
- Step definitions: paquete `com.fitstore.steps` → `CompraSteps.java`
- Lenguaje: Gherkin en español (`# language: es`)

---

**FitStore Team** 💪 | Tests BDD en Español