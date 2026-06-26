package pe.contrataia.empresa;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import pe.contrataia.BaseIntegrationTest;

import java.util.List;

import static org.hamcrest.Matchers.*;

/**
 * Tests del controlador /empresa
 *
 * ACCESO ESPERADO:
 *   EMPRESA         → CRUD completo en su propio perfil
 *   ENTIDAD_PUBLICA → 403 en todos los endpoints
 *   CIUDADANO       → 401 en todos los endpoints
 */
@DisplayName("Empresa Endpoints - /empresa")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmpresaEndpointTest extends BaseIntegrationTest {

    private static String experienciaId;
    private static String certificacionId;

    @BeforeAll
    void setup() {
        loginAll();
    }

    // =========================================================
    // CONTROL DE ACCESO: Entidad y Ciudadano → denegado
    // =========================================================

    @Test
    @Order(1)
    @DisplayName("[GET /empresa/perfil] CIUDADANO 401 - sin token")
    void getPerfil_sinToken_retorna401() {
        asCiudadano()
                .when().get("/empresa/perfil")
                .then().statusCode(401);
    }

    @Test
    @Order(2)
    @DisplayName("[GET /empresa/perfil] ENTIDAD_PUBLICA 403 - rol incorrecto")
    void getPerfil_comoEntidad_retorna403() {
        assumeEntidadToken();
        asEntidad()
                .when().get("/empresa/perfil")
                .then().statusCode(403);
    }

    @Test
    @Order(3)
    @DisplayName("[POST /empresa/experiencia] ENTIDAD_PUBLICA 403 - rol incorrecto")
    void agregarExperiencia_comoEntidad_retorna403() {
        assumeEntidadToken();
        asEntidad()
                .body("{\"descripcion\": \"Test\", \"monto\": 10000}")
                .when().post("/empresa/experiencia")
                .then().statusCode(403);
    }

    @Test
    @Order(4)
    @DisplayName("[GET /empresa/certificaciones] ENTIDAD_PUBLICA 403 - rol incorrecto")
    void listarCertificaciones_comoEntidad_retorna403() {
        assumeEntidadToken();
        asEntidad()
                .when().get("/empresa/certificaciones")
                .then().statusCode(403);
    }

    @Test
    @Order(5)
    @DisplayName("[GET /empresa/evaluaciones] CIUDADANO 401 - sin token")
    void listarEvaluaciones_sinToken_retorna401() {
        asCiudadano()
                .when().get("/empresa/evaluaciones")
                .then().statusCode(401);
    }

    // =========================================================
    // PERFIL
    // =========================================================

    @Test
    @Order(6)
    @DisplayName("[GET /empresa/perfil] EMPRESA 200 - obtener perfil propio")
    void getPerfil_comoEmpresa_retorna200() {
        assumeEmpresaToken();
        asEmpresa()
                .when().get("/empresa/perfil")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("ruc", notNullValue())
                .body("email", equalTo(TEST_EMPRESA_EMAIL))
                .body("razonSocial", notNullValue());
    }

    @Test
    @Order(7)
    @DisplayName("[PUT /empresa/perfil] EMPRESA 200 - actualizar perfil")
    void actualizarPerfil_datosValidos_retorna200() {
        assumeEmpresaToken();
        asEmpresa()
                .body("""
                        {
                            "descripcion": "Empresa de construcción civil con 10 años de experiencia",
                            "sector": "CONSTRUCCION",
                            "sitioWeb": "https://constructora-test.pe",
                            "telefono": "01-4445566"
                        }
                        """)
                .when().put("/empresa/perfil")
                .then()
                .statusCode(200)
                .body("descripcion", equalTo("Empresa de construcción civil con 10 años de experiencia"));
    }

    // =========================================================
    // EXPERIENCIA
    // =========================================================

    @Test
    @Order(8)
    @DisplayName("[POST /empresa/experiencia] EMPRESA 400 - sin campos obligatorios")
    void agregarExperiencia_sinDatos_retorna400() {
        assumeEmpresaToken();
        asEmpresa()
                .body("{}")
                .when().post("/empresa/experiencia")
                .then().statusCode(400);
    }

    @Test
    @Order(9)
    @DisplayName("[POST /empresa/experiencia] EMPRESA 400 - monto negativo")
    void agregarExperiencia_montoNegativo_retorna400() {
        assumeEmpresaToken();
        asEmpresa()
                .body("""
                        {
                            "descripcion": "Obra vial en Callao",
                            "entidadContratante": "Municipalidad del Callao",
                            "monto": -50000,
                            "rubro": "CONSTRUCCION_VIAL",
                            "region": "Callao",
                            "fechaInicio": "2022-01-01",
                            "fechaFin": "2022-09-30"
                        }
                        """)
                .when().post("/empresa/experiencia")
                .then().statusCode(400);
    }

    @Test
    @Order(10)
    @DisplayName("[POST /empresa/experiencia] EMPRESA 201 - agregar experiencia laboral")
    void agregarExperiencia_datosValidos_retorna201() {
        assumeEmpresaToken();
        Response resp = asEmpresa()
                .body("""
                        {
                            "descripcion": "Construcción de 3km de pista vehicular en Lima Norte",
                            "entidadContratante": "Municipalidad de Los Olivos",
                            "monto": 350000.00,
                            "rubro": "CONSTRUCCION_VIAL",
                            "region": "Lima",
                            "fechaInicio": "2023-01-15",
                            "fechaFin": "2023-08-30"
                        }
                        """)
                .when().post("/empresa/experiencia")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("entidadContratante", equalTo("Municipalidad de Los Olivos"))
                .body("monto", equalTo(350000.0f))
                .extract().response();

        experienciaId = resp.jsonPath().getString("id");
        System.out.println("[TEST] Experiencia creada: id=" + experienciaId);
    }

    @Test
    @Order(11)
    @DisplayName("[GET /empresa/experiencia] EMPRESA 200 - listar experiencias")
    void listarExperiencias_comoEmpresa_retorna200() {
        assumeEmpresaToken();
        asEmpresa()
                .when().get("/empresa/experiencia")
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class));
    }

    // =========================================================
    // CERTIFICACIONES
    // =========================================================

    @Test
    @Order(12)
    @DisplayName("[POST /empresa/certificaciones] EMPRESA 400 - sin nombre")
    void agregarCertificacion_sinNombre_retorna400() {
        assumeEmpresaToken();
        asEmpresa()
                .body("""
                        {"entidadEmisora": "Bureau Veritas"}
                        """)
                .when().post("/empresa/certificaciones")
                .then().statusCode(400);
    }

    @Test
    @Order(13)
    @DisplayName("[POST /empresa/certificaciones] EMPRESA 201 - agregar certificación ISO")
    void agregarCertificacion_datosValidos_retorna201() {
        assumeEmpresaToken();
        Response resp = asEmpresa()
                .body("""
                        {
                            "nombre": "ISO 9001:2015",
                            "entidadEmisora": "Bureau Veritas",
                            "fechaEmision": "2023-06-01",
                            "fechaVencimiento": "2026-06-01",
                            "documentoUrl": "https://docs.empresa-test.pe/iso9001.pdf"
                        }
                        """)
                .when().post("/empresa/certificaciones")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("nombre", equalTo("ISO 9001:2015"))
                .body("entidadEmisora", equalTo("Bureau Veritas"))
                .extract().response();

        certificacionId = resp.jsonPath().getString("id");
        System.out.println("[TEST] Certificación creada: id=" + certificacionId);
    }

    @Test
    @Order(14)
    @DisplayName("[GET /empresa/certificaciones] EMPRESA 200 - listar certificaciones")
    void listarCertificaciones_comoEmpresa_retorna200() {
        assumeEmpresaToken();
        asEmpresa()
                .when().get("/empresa/certificaciones")
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class));
    }

    // =========================================================
    // EVALUACIONES
    // =========================================================

    @Test
    @Order(15)
    @DisplayName("[GET /empresa/evaluaciones] EMPRESA 200 - historial de evaluaciones IA")
    void listarEvaluaciones_comoEmpresa_retorna200() {
        assumeEmpresaToken();
        asEmpresa()
                .when().get("/empresa/evaluaciones")
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class));
    }

    // =========================================================
    // DOCUMENTOS
    // =========================================================

    @Test
    @Order(16)
    @DisplayName("[POST /empresa/documentos] EMPRESA 201 - agregar documento por query params")
    void agregarDocumento_datosValidos_retorna201() {
        assumeEmpresaToken();
        asEmpresa()
                .queryParam("tipo", "RNP")
                .queryParam("nombre", "Registro Nacional de Proveedores 2024")
                .queryParam("url", "https://docs.empresa-test.pe/rnp-2024.pdf")
                .when().post("/empresa/documentos")
                .then()
                .statusCode(201)
                .body("message", notNullValue());
    }

    @Test
    @Order(17)
    @DisplayName("[POST /empresa/documentos] ENTIDAD_PUBLICA 403 - rol incorrecto")
    void agregarDocumento_comoEntidad_retorna403() {
        assumeEntidadToken();
        asEntidad()
                .queryParam("tipo", "RNP")
                .queryParam("nombre", "Doc Test")
                .queryParam("url", "https://docs.test.pe/doc.pdf")
                .when().post("/empresa/documentos")
                .then().statusCode(403);
    }

    // =========================================================
    // ELIMINACIÓN
    // =========================================================

    @Test
    @Order(18)
    @DisplayName("[DELETE /empresa/experiencia/{id}] EMPRESA 204 - eliminar experiencia propia")
    void eliminarExperiencia_idValido_retorna204() {
        assumeEmpresaToken();
        Assumptions.assumeTrue(experienciaId != null, "No hay experiencia creada");

        asEmpresa()
                .when().delete("/empresa/experiencia/" + experienciaId)
                .then().statusCode(204);
    }

    @Test
    @Order(19)
    @DisplayName("[DELETE /empresa/experiencia/{id}] EMPRESA 404 - ID inexistente")
    void eliminarExperiencia_idInexistente_retorna404() {
        assumeEmpresaToken();
        asEmpresa()
                .when().delete("/empresa/experiencia/00000000-0000-0000-0000-000000000000")
                .then().statusCode(anyOf(equalTo(404), equalTo(400)));
    }

    @Test
    @Order(20)
    @DisplayName("[DELETE /empresa/certificaciones/{id}] EMPRESA 204 - eliminar certificación propia")
    void eliminarCertificacion_idValido_retorna204() {
        assumeEmpresaToken();
        Assumptions.assumeTrue(certificacionId != null, "No hay certificación creada");

        asEmpresa()
                .when().delete("/empresa/certificaciones/" + certificacionId)
                .then().statusCode(204);
    }
}
