package com.fitstore;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Ejecutor de pruebas BDD con Cucumber
 * 
 * Ejecutar con:
 *   mvn test -Dtest=CucumberRunner
 * 
 * O ejecutar escenarios específicos por tag:
 *   mvn test -Dtest=CucumberRunner -Dcucumber.filter.tags="@compra"
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = "cucumber.features", value = "classpath:features")
@ConfigurationParameter(key = "cucumber.glue", value = "classpath:com.fitstore.steps")
@ConfigurationParameter(key = "cucumber.publish.quiet", value = "true")
public class CucumberRunner {
    // Esta clase actúa como ejecutor de Cucumber con JUnit 5
    // Los pasos se cargan automáticamente desde el paquete "com.fitstore.steps"
}
