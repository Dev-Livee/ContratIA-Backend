package pe.contrataia;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.TestInstance;

import static io.restassured.RestAssured.given;

/**
 * Clase base para tests de integración contra el servidor en http://localhost:8080.
 *
 * PREREQUISITOS ANTES DE EJECUTAR LOS TESTS:
 * ============================================
 * 1. El servidor debe estar corriendo: mvn spring-boot:run
 *
 * 2. Registrar manualmente los dos usuarios de prueba:
 *    a) ENTIDAD_PUBLICA (POST /auth/register/entidad):
 *       - ruc: 20123456789
 *       - razonSocial: Municipalidad de Prueba
 *       - email: test.entidad@contrataia.pe
 *       - dniRepresentante: 12345678
 *       - representanteLegal: Juan Test Perez
 *       - password: TestPass123!
 *       - tipo: MUNICIPALIDAD
 *       - distrito: San Isidro
 *       - provincia: Lima
 *       - region: Lima
 *       - telefono: 014445555
 *       - cargo: Alcalde
 *
 *    b) EMPRESA (POST /auth/register/empresa):
 *       - ruc: 20987654321
 *       - razonSocial: Constructora de Prueba SAC
 *       - email: test.empresa@contrataia.pe
 *       - dniRepresentante: 87654321
 *       - representanteLegal: Maria Test Garcia
 *       - password: TestPass123!
 *       - sector: CONSTRUCCION
 *       - telefono: 014446666
 *
 * 3. Verificar los OTPs recibidos en cada correo (POST /auth/verify-otp)
 *
 * 4. Ejecutar los tests: mvn test
 *
 * Si tienes credenciales diferentes, configura estas variables de entorno:
 *   TEST_ENTIDAD_EMAIL, TEST_ENTIDAD_PASSWORD, TEST_EMPRESA_EMAIL, TEST_EMPRESA_PASSWORD
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseIntegrationTest {

    protected static final String BASE_URL;

    protected static final String TEST_ENTIDAD_EMAIL = env("TEST_ENTIDAD_EMAIL", "test.entidad@contrataia.pe");
    protected static final String TEST_ENTIDAD_PASSWORD = env("TEST_ENTIDAD_PASSWORD", "TestPass123!");
    protected static final String TEST_EMPRESA_EMAIL = env("TEST_EMPRESA_EMAIL", "test.empresa@contrataia.pe");
    protected static final String TEST_EMPRESA_PASSWORD = env("TEST_EMPRESA_PASSWORD", "TestPass123!");

    protected String entidadToken;
    protected String empresaToken;
    protected String entidadRefreshToken;
    protected String empresaRefreshToken;

    static {
        BASE_URL = env("API_BASE_URL", "http://localhost:8080");
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    protected void loginAll() {
        Response entidadResp = doLogin(TEST_ENTIDAD_EMAIL, TEST_ENTIDAD_PASSWORD);
        if (entidadResp.statusCode() == 200) {
            entidadToken = entidadResp.jsonPath().getString("accessToken");
            entidadRefreshToken = entidadResp.jsonPath().getString("refreshToken");
            System.out.printf("[TEST] Login OK - ENTIDAD_PUBLICA (%s)%n", TEST_ENTIDAD_EMAIL);
        } else {
            System.err.printf("[TEST] ⚠️  Login FALLIDO para ENTIDAD_PUBLICA (%s) status=%d: %s%n",
                    TEST_ENTIDAD_EMAIL, entidadResp.statusCode(), entidadResp.asString());
        }

        Response empresaResp = doLogin(TEST_EMPRESA_EMAIL, TEST_EMPRESA_PASSWORD);
        if (empresaResp.statusCode() == 200) {
            empresaToken = empresaResp.jsonPath().getString("accessToken");
            empresaRefreshToken = empresaResp.jsonPath().getString("refreshToken");
            System.out.printf("[TEST] Login OK - EMPRESA (%s)%n", TEST_EMPRESA_EMAIL);
        } else {
            System.err.printf("[TEST] ⚠️  Login FALLIDO para EMPRESA (%s) status=%d: %s%n",
                    TEST_EMPRESA_EMAIL, empresaResp.statusCode(), empresaResp.asString());
        }
    }

    private Response doLogin(String email, String password) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(email, password))
                .when()
                .post("/auth/login");
    }

    protected RequestSpecification asEntidad() {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + entidadToken);
    }

    protected RequestSpecification asEmpresa() {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + empresaToken);
    }

    protected RequestSpecification asCiudadano() {
        return given().contentType(ContentType.JSON);
    }

    protected void assumeEntidadToken() {
        Assumptions.assumeTrue(entidadToken != null,
                "Token de ENTIDAD_PUBLICA no disponible. Registra y verifica " + TEST_ENTIDAD_EMAIL + " primero.");
    }

    protected void assumeEmpresaToken() {
        Assumptions.assumeTrue(empresaToken != null,
                "Token de EMPRESA no disponible. Registra y verifica " + TEST_EMPRESA_EMAIL + " primero.");
    }

    protected static String env(String key, String defaultVal) {
        String val = System.getenv(key);
        return (val != null && !val.isBlank()) ? val : defaultVal;
    }
}
