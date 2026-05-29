# 🧪 Testing BDD con Cucumber en FitStore

Este proyecto utiliza **Cucumber** con **Gherkin en español** para pruebas BDD (Behavior-Driven Development).

## 📁 Estructura de Tests

```
backend/
├── src/test/
│   ├── java/com/fitstore/
│   │   └── CompraStepDefinitions.java    ← Step definitions en Java
│   └── resources/features/
│       └── compra.feature                ← Escenarios Gherkin en español
```

## 🚀 Configuración

### 1. Dependencias en `pom.xml`

Asegúrate de tener Cucumber configurado:

```xml
<!-- Cucumber para BDD -->
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-java</artifactId>
    <version>7.14.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-junit-platform-engine</artifactId>
    <version>7.14.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-spring</artifactId>
    <version>7.14.0</version>
    <scope>test</scope>
</dependency>

<!-- JUnit 5 -->
<dependency>
    <groupId>org.junit.platform</groupId>
    <artifactId>junit-platform-suite</artifactId>
    <version>1.9.3</version>
    <scope>test</scope>
</dependency>
```

### 2. Clase ejecutora de Cucumber

Crear `backend/src/test/java/com/fitstore/CucumberIntegrationTest.java`:

```java
package com.fitstore;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = "cucumber.features", value = "classpath:features")
@ConfigurationParameter(key = "cucumber.glue", value = "classpath:com.fitstore")
public class CucumberIntegrationTest {
}
```

## 📝 Escenarios BDD Incluidos

El archivo `compra.feature` contiene los siguientes escenarios en **español**:

### ✅ Compra Exitosa
```gherkin
Escenario: Compra exitosa de múltiples productos
```
- Verifica que se puedan comprar múltiples productos
- Valida el cálculo del total
- Confirma que el stock se reduce
- Verifica el envío de email

### ❌ Stock Insuficiente
```gherkin
Escenario: Compra con stock insuficiente
```
- Intenta agregar más productos de los disponibles
- Verifica que se rechace la adición
- Valida el mensaje de error

### 🔐 Login Fallido
```gherkin
Escenario: Login fallido con credenciales inválidas
Escenario: Login fallido con usuario no registrado
```
- Prueba credenciales incorrectas
- Verifica que no se otorgue token JWT
- Valida manejo de excepciones

### 📦 Verificación de Stock
```gherkin
Escenario: Verificación de stock en tiempo real desde la API
Escenario: Verificación de disponibilidad en stock
Escenario: Stock actualizado en Redis después de compra
```
- Consulta stock desde API
- Verifica endpoint dedicado de disponibilidad
- Valida sincronización con Redis

### 📋 Estados de Pedido
```gherkin
Escenario: Estados de pedido transitan correctamente
Escenario: Cancelación de pedido por admin
```
- Verifica transiciones CONFIRMADO → EN_CAMINO → ENTREGADO
- Valida cancelación y restauración de stock

## 🏃 Ejecutar Tests

### Desde Maven

```bash
# Ejecutar todos los tests BDD
mvn test

# Ejecutar solo Cucumber
mvn test -Dtest=CucumberIntegrationTest

# Ejecutar un escenario específico
mvn test -Dtest=CucumberIntegrationTest -Dcucumber.filter.tags="@compra"
```

### Desde IDE (IntelliJ IDEA / VS Code)

1. Click derecho en `compra.feature`
2. Seleccionar "Run" o "Debug"
3. Los escenarios se ejecutarán y mostrarán resultados en tiempo real

## 📊 Reporte de Ejecución

Los reportes se generan en:
```
target/cucumber-reports/
```

## 🔧 Implementar Step Definitions

El archivo `CompraStepDefinitions.java` contiene **stubs** (esqueletos) de todos los pasos.

Para completar la implementación:

1. **Agregar lógica HTTP real:**
```java
@Dado("que soy usuario autenticado como {string} con token JWT")
public void autenticarUsuario(String email) {
    // TODO: Reemplazar con:
    // response = HTTP.post("/api/auth/login", email, clave)
    // token = response.getToken()
}
```

2. **Usar RestTemplate o WebTestClient:**
```java
@Autowired
private WebTestClient webTestClient;

// Luego en los pasos:
webTestClient.post()
    .uri("/api/productos")
    .exchange()
    .expectStatus().isOk();
```

3. **Validar respuestas:**
```java
@Entonces("el total del carrito es {string}")
public void verificarTotalCarrito(String totalEsperado) {
    // Obtener total real desde API o variable local
    assertEquals(totalEsperado, totalReal);
}
```

## 📌 Convención de Nomenclatura

- **Feature files:** `.feature` en `src/test/resources/features/`
- **Step definitions:** `*StepDefinitions.java` en `src/test/java/`
- **Lenguaje:** Español (Gherkin con `# language: es`)

## 🎯 Mejores Prácticas

1. ✅ **Un escenario = un comportamiento**
   ```gherkin
   Escenario: Compra exitosa    # ✓ Claro y conciso
   Escenario: El sistema debe funcionar  # ✗ Vago
   ```

2. ✅ **Usar Antecedentes para setup común**
   ```gherkin
   Antecedentes:
     Dado que el servidor está disponible
     Y existen productos en el catálogo
   ```

3. ✅ **DataTables para datos complejos**
   ```gherkin
   Dado existen los siguientes productos:
     | nombre | precio | stock |
     | ... | ... | ... |
   ```

4. ✅ **Pasos reutilizables y específicos**
   ```gherkin
   # Bueno - específico
   Cuando agrego 2 unidades de "Proteína"
   
   # Malo - muy genérico
   Cuando hago algo
   ```

5. ✅ **Validar estados, no detalles de implementación**
   ```gherkin
   # Bueno - comportamiento
   Entonces el pedido tiene estado "CONFIRMADO"
   
   # Malo - implementación
   Entonces la base de datos contiene INSERT
   ```

## 🐛 Debugging

Para ver logs detallados:

```bash
mvn test -X  # Debug mode
```

O agregar puntos de parada en `CompraStepDefinitions.java` y usar el debugger del IDE.

## 📚 Recursos

- [Cucumber Documentation](https://cucumber.io/)
- [Gherkin en Español](https://cucumber.io/docs/gherkin/languages/)
- [BDD Best Practices](https://cucumber.io/docs/bdd/)

## ✨ Tags para organizar tests

En el archivo feature:
```gherkin
@compra
Escenario: Compra exitosa

@admin
Escenario: Actualizar stock
```

Ejecutar por tag:
```bash
mvn test -Dcucumber.filter.tags="@compra"
```

---

**FitStore Team** 💪 | Tests BDD en Español
