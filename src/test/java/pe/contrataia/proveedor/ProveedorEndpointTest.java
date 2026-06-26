package pe.contrataia.proveedor;

import org.junit.jupiter.api.*;
import pe.contrataia.BaseIntegrationTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Tests del controlador /proveedores (integración con LatInfo API)
 *
 * ACCESO ESPERADO:
 *   ENTIDAD_PUBLICA → acceso completo a búsquedas y consultas
 *   EMPRESA         → 403 en todos los endpoints
 *   CIUDADANO       → 401 en todos los endpoints
 *
 * NOTA: Los endpoints que llaman a LatInfo API externa pueden retornar 503/502
 * si la API externa no está disponible. Los tests aceptan esos códigos como válidos.
 */
@DisplayName("Proveedor Endpoints - /proveedores")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProveedorEndpointTest extends BaseIntegrationTest {

    private static final String RUC_CONSULTA = "20100028698";
    private static final String QUERY_BUSQUEDA = "CONSTRUCTORA";

    @BeforeAll
    void setup() {
        loginAll();
    }

    // =========================================================
    // CONTROL DE ACCESO: Empresa y Ciudadano → denegado
    // =========================================================

    @Test
    @Order(1)
    @DisplayName("[GET /proveedores/buscar] CIUDADANO 401 - sin token")
    void buscarProveedores_sinToken_retorna401() {
        given().queryParam("q", QUERY_BUSQUEDA)
                .when().get("/proveedores/buscar")
                .then().statusCode(401);
    }

    @Test
    @Order(2)
    @DisplayName("[GET /proveedores/buscar/agregado] CIUDADANO 401 - sin token")
    void buscarAgregado_sinToken_retorna401() {
        given().queryParam("q", QUERY_BUSQUEDA)
                .when().get("/proveedores/buscar/agregado")
                .then().statusCode(401);
    }

    @Test
    @Order(3)
    @DisplayName("[GET /proveedores/{ruc}] CIUDADANO 401 - sin token")
    void getProveedorPorRuc_sinToken_retorna401() {
        given()
                .when().get("/proveedores/" + RUC_CONSULTA)
                .then().statusCode(401);
    }

    @Test
    @Order(4)
    @DisplayName("[GET /proveedores/buscar] EMPRESA 403 - rol incorrecto")
    void buscarProveedores_comoEmpresa_retorna403() {
        assumeEmpresaToken();
        asEmpresa()
                .queryParam("q", QUERY_BUSQUEDA)
                .when().get("/proveedores/buscar")
                .then().statusCode(403);
    }

    @Test
    @Order(5)
    @DisplayName("[GET /proveedores/{ruc}] EMPRESA 403 - rol incorrecto")
    void getProveedorPorRuc_comoEmpresa_retorna403() {
        assumeEmpresaToken();
        asEmpresa()
                .when().get("/proveedores/" + RUC_CONSULTA)
                .then().statusCode(403);
    }

    @Test
    @Order(6)
    @DisplayName("[GET /proveedores/licitaciones] EMPRESA 403 - rol incorrecto")
    void getLicitaciones_comoEmpresa_retorna403() {
        assumeEmpresaToken();
        asEmpresa()
                .when().get("/proveedores/licitaciones")
                .then().statusCode(403);
    }

    // =========================================================
    // VALIDACIÓN DE PARÁMETROS
    // =========================================================

    @Test
    @Order(7)
    @DisplayName("[GET /proveedores/buscar] ENTIDAD 400 - query con menos de 3 caracteres")
    void buscarProveedores_queryCorta_retorna400() {
        assumeEntidadToken();
        asEntidad()
                .queryParam("q", "AB")
                .when().get("/proveedores/buscar")
                .then().statusCode(400);
    }

    @Test
    @Order(8)
    @DisplayName("[GET /proveedores/buscar/agregado] ENTIDAD 400 - query con menos de 3 caracteres")
    void buscarAgregado_queryCorta_retorna400() {
        assumeEntidadToken();
        asEntidad()
                .queryParam("q", "X")
                .when().get("/proveedores/buscar/agregado")
                .then().statusCode(400);
    }

    // =========================================================
    // HAPPY PATH ENTIDAD_PUBLICA
    // =========================================================

    @Test
    @Order(9)
    @DisplayName("[GET /proveedores/buscar] ENTIDAD 200 - búsqueda por nombre en SUNAT")
    void buscarProveedores_queryValida_retorna200() {
        assumeEntidadToken();
        asEntidad()
                .queryParam("q", QUERY_BUSQUEDA)
                .when().get("/proveedores/buscar")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503), equalTo(502)));
    }

    @Test
    @Order(10)
    @DisplayName("[GET /proveedores/buscar/agregado] ENTIDAD 200 - búsqueda multi-fuente (SUNAT+OSCE+OEFA)")
    void buscarAgregado_queryValida_retorna200() {
        assumeEntidadToken();
        asEntidad()
                .queryParam("q", QUERY_BUSQUEDA)
                .when().get("/proveedores/buscar/agregado")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503), equalTo(502)));
    }

    @Test
    @Order(11)
    @DisplayName("[GET /proveedores/{ruc}] ENTIDAD 200 - perfil KYB completo por RUC")
    void getProveedorPorRuc_rucValido_retornaPerfilOError() {
        assumeEntidadToken();
        asEntidad()
                .when().get("/proveedores/" + RUC_CONSULTA)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(404), equalTo(503), equalTo(502)));
    }

    @Test
    @Order(12)
    @DisplayName("[GET /proveedores/{ruc}/rnp] ENTIDAD 200 - consulta RNP (Registro Nacional de Proveedores)")
    void getRnpProveedor_rucValido_retornaRnpOError() {
        assumeEntidadToken();
        asEntidad()
                .when().get("/proveedores/" + RUC_CONSULTA + "/rnp")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(404), equalTo(503), equalTo(502)));
    }

    @Test
    @Order(13)
    @DisplayName("[GET /proveedores/{ruc}/coactiva] ENTIDAD 200 - consulta deuda coactiva SUNAT")
    void getCoactivaProveedor_rucValido_retornaCoactivaOError() {
        assumeEntidadToken();
        asEntidad()
                .when().get("/proveedores/" + RUC_CONSULTA + "/coactiva")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(404), equalTo(503), equalTo(502)));
    }

    @Test
    @Order(14)
    @DisplayName("[GET /proveedores/licitaciones] ENTIDAD 200 - búsqueda sin filtros")
    void getLicitaciones_sinFiltros_retorna200() {
        assumeEntidadToken();
        asEntidad()
                .when().get("/proveedores/licitaciones")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503), equalTo(502)));
    }

    @Test
    @Order(15)
    @DisplayName("[GET /proveedores/licitaciones] ENTIDAD 200 - con filtros de búsqueda y límite")
    void getLicitaciones_conFiltros_retorna200() {
        assumeEntidadToken();
        asEntidad()
                .queryParam("q", "construccion")
                .queryParam("limit", 10)
                .when().get("/proveedores/licitaciones")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503), equalTo(502)));
    }

    @Test
    @Order(16)
    @DisplayName("[GET /proveedores/licitaciones] ENTIDAD 200 - filtrar por comprador (entidad)")
    void getLicitaciones_conFiltroBuyer_retorna200() {
        assumeEntidadToken();
        asEntidad()
                .queryParam("buyer", "MUNICIPALIDAD")
                .queryParam("limit", 5)
                .when().get("/proveedores/licitaciones")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503), equalTo(502)));
    }
}
