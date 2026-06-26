package pe.contrataia.publico;

import org.junit.jupiter.api.*;
import pe.contrataia.BaseIntegrationTest;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Tests del controlador /publico (portal ciudadano)
 *
 * ACCESO ESPERADO:
 *   CIUDADANO  → 200 en todos los GET /publico/**
 *   ENTIDAD    → 200 en todos los GET /publico/**
 *   EMPRESA    → 200 en todos los GET /publico/**
 *   CUALQUIERA → 401 en endpoints protegidos (/proyectos, /empresa, /proveedores)
 */
@DisplayName("Público Endpoints - /publico (Ciudadano / Sin autenticación)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PublicoEndpointTest extends BaseIntegrationTest {

    @BeforeAll
    void setup() {
        loginAll();
    }

    // =========================================================
    // CIUDADANO: Acceso a endpoints públicos
    // =========================================================

    @Test
    @Order(1)
    @DisplayName("[GET /publico/obras] CIUDADANO 200 - lista paginada de obras")
    void listarObras_sinAuth_retorna200() {
        asCiudadano()
                .when().get("/publico/obras")
                .then()
                .statusCode(200)
                .body("content", notNullValue())
                .body("totalElements", greaterThanOrEqualTo(0))
                .body("size", notNullValue());
    }

    @Test
    @Order(2)
    @DisplayName("[GET /publico/obras] CIUDADANO 200 - filtrar por distrito")
    void listarObras_filtroPorDistrito_retorna200() {
        asCiudadano()
                .queryParam("distrito", "Miraflores")
                .when().get("/publico/obras")
                .then()
                .statusCode(200)
                .body("content", notNullValue());
    }

    @Test
    @Order(3)
    @DisplayName("[GET /publico/obras] CIUDADANO 200 - filtrar por estado BORRADOR")
    void listarObras_filtroPorEstado_retorna200() {
        asCiudadano()
                .queryParam("estado", "BORRADOR")
                .when().get("/publico/obras")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(4)
    @DisplayName("[GET /publico/obras] CIUDADANO 200 - paginación page=0 size=5")
    void listarObras_conPaginacion_retorna200() {
        asCiudadano()
                .queryParam("page", 0)
                .queryParam("size", 5)
                .when().get("/publico/obras")
                .then()
                .statusCode(200)
                .body("size", equalTo(5));
    }

    @Test
    @Order(5)
    @DisplayName("[GET /publico/obras] CIUDADANO 200 - filtrar por rango de presupuesto")
    void listarObras_filtroPorPresupuesto_retorna200() {
        asCiudadano()
                .queryParam("presupuestoMin", 100000)
                .queryParam("presupuestoMax", 1000000)
                .when().get("/publico/obras")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(6)
    @DisplayName("[GET /publico/obras/{codigoUnico}] CIUDADANO 404 - código inexistente")
    void getObraPorCodigo_codigoInexistente_retorna404() {
        asCiudadano()
                .when().get("/publico/obras/CODIGO-00000000000000000")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(7)
    @DisplayName("[GET /publico/obras/{codigoUnico}/timeline] CIUDADANO 404 - código inexistente")
    void getTimeline_codigoInexistente_retorna404() {
        asCiudadano()
                .when().get("/publico/obras/CODIGO-00000000000000000/timeline")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(8)
    @DisplayName("[GET /publico/distritos] CIUDADANO 200 - lista de distritos")
    void listarDistritos_sinAuth_retorna200() {
        asCiudadano()
                .when().get("/publico/distritos")
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class));
    }

    // =========================================================
    // ENTIDAD PÚBLICA: También puede ver obras públicas
    // =========================================================

    @Test
    @Order(9)
    @DisplayName("[GET /publico/obras] ENTIDAD_PUBLICA 200 - también puede listar obras")
    void listarObras_comoEntidad_retorna200() {
        assumeEntidadToken();
        asEntidad()
                .when().get("/publico/obras")
                .then()
                .statusCode(200)
                .body("content", notNullValue());
    }

    @Test
    @Order(10)
    @DisplayName("[GET /publico/distritos] ENTIDAD_PUBLICA 200 - también puede ver distritos")
    void listarDistritos_comoEntidad_retorna200() {
        assumeEntidadToken();
        asEntidad()
                .when().get("/publico/distritos")
                .then()
                .statusCode(200);
    }

    // =========================================================
    // EMPRESA: También puede ver obras públicas
    // =========================================================

    @Test
    @Order(11)
    @DisplayName("[GET /publico/obras] EMPRESA 200 - también puede listar obras")
    void listarObras_comoEmpresa_retorna200() {
        assumeEmpresaToken();
        asEmpresa()
                .when().get("/publico/obras")
                .then()
                .statusCode(200)
                .body("content", notNullValue());
    }

    @Test
    @Order(12)
    @DisplayName("[GET /publico/distritos] EMPRESA 200 - también puede ver distritos")
    void listarDistritos_comoEmpresa_retorna200() {
        assumeEmpresaToken();
        asEmpresa()
                .when().get("/publico/distritos")
                .then()
                .statusCode(200);
    }

    // =========================================================
    // CIUDADANO: Intentos de acceder a endpoints protegidos → 401
    // =========================================================

    @Test
    @Order(13)
    @DisplayName("[GET /proyectos] CIUDADANO 401 - no puede acceder a proyectos sin token")
    void ciudadano_accedeProyectos_retorna401() {
        asCiudadano()
                .when().get("/proyectos")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(14)
    @DisplayName("[POST /proyectos] CIUDADANO 401 - no puede crear proyectos sin token")
    void ciudadano_creaProyecto_retorna401() {
        asCiudadano()
                .body("{\"titulo\":\"Intento\"}")
                .when().post("/proyectos")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(15)
    @DisplayName("[GET /empresa/perfil] CIUDADANO 401 - no puede acceder al perfil de empresa")
    void ciudadano_accedePerfilEmpresa_retorna401() {
        asCiudadano()
                .when().get("/empresa/perfil")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(16)
    @DisplayName("[GET /proveedores/buscar] CIUDADANO 401 - no puede buscar proveedores")
    void ciudadano_buscaProveedores_retorna401() {
        given().queryParam("q", "constructora")
                .when().get("/proveedores/buscar")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(17)
    @DisplayName("[POST /empresa/experiencia] CIUDADANO 401 - no puede agregar experiencia")
    void ciudadano_agregaExperiencia_retorna401() {
        asCiudadano()
                .body("{\"descripcion\": \"Test\"}")
                .when().post("/empresa/experiencia")
                .then()
                .statusCode(401);
    }
}
