package pe.contrataia.proyecto;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import pe.contrataia.BaseIntegrationTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.*;

/**
 * Tests del controlador /proyectos
 *
 * ACCESO ESPERADO:
 *   ENTIDAD_PUBLICA → CRUD completo en proyectos propios
 *   EMPRESA         → 403 en todos los endpoints
 *   CIUDADANO       → 401 en todos los endpoints
 *
 * Los tests están ordenados para crear datos y reutilizarlos en pasos siguientes.
 */
@DisplayName("Proyecto Endpoints - /proyectos")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProyectoEndpointTest extends BaseIntegrationTest {

    private static String proyectoId;
    private static String candidatoId;

    @BeforeAll
    void setup() {
        loginAll();
    }

    // =========================================================
    // CONTROL DE ACCESO: Empresa y Ciudadano → denegado
    // =========================================================

    @Test
    @Order(1)
    @DisplayName("[GET /proyectos] CIUDADANO 401 - sin token")
    void listarProyectos_sinToken_retorna401() {
        asCiudadano()
                .when().get("/proyectos")
                .then().statusCode(401);
    }

    @Test
    @Order(2)
    @DisplayName("[POST /proyectos] CIUDADANO 401 - sin token")
    void crearProyecto_sinToken_retorna401() {
        asCiudadano()
                .body("{\"titulo\":\"Proyecto\"}")
                .when().post("/proyectos")
                .then().statusCode(401);
    }

    @Test
    @Order(3)
    @DisplayName("[GET /proyectos] EMPRESA 403 - rol incorrecto")
    void listarProyectos_comoEmpresa_retorna403() {
        assumeEmpresaToken();
        asEmpresa()
                .when().get("/proyectos")
                .then().statusCode(403);
    }

    @Test
    @Order(4)
    @DisplayName("[POST /proyectos] EMPRESA 403 - rol incorrecto")
    void crearProyecto_comoEmpresa_retorna403() {
        assumeEmpresaToken();
        asEmpresa()
                .body("""
                        {"titulo": "Intento de empresa", "descripcion": "No debería funcionar",
                         "presupuesto": 100000, "rubro": "CONSTRUCCION",
                         "distrito": "Lima", "provincia": "Lima", "region": "Lima"}
                        """)
                .when().post("/proyectos")
                .then().statusCode(403);
    }

    @Test
    @Order(5)
    @DisplayName("[POST /proyectos/{id}/candidatos] EMPRESA 403 - rol incorrecto")
    void agregarCandidato_comoEmpresa_retorna403() {
        assumeEmpresaToken();
        asEmpresa()
                .body("{\"ruc\": \"20100028698\"}")
                .when().post("/proyectos/00000000-0000-0000-0000-000000000001/candidatos")
                .then().statusCode(403);
    }

    // =========================================================
    // VALIDACIÓN DE DATOS
    // =========================================================

    @Test
    @Order(6)
    @DisplayName("[POST /proyectos] ENTIDAD 400 - cuerpo vacío")
    void crearProyecto_cuerpoVacio_retorna400() {
        assumeEntidadToken();
        asEntidad()
                .body("{}")
                .when().post("/proyectos")
                .then().statusCode(400);
    }

    @Test
    @Order(7)
    @DisplayName("[POST /proyectos] ENTIDAD 400 - presupuesto negativo")
    void crearProyecto_presupuestoNegativo_retorna400() {
        assumeEntidadToken();
        asEntidad()
                .body("""
                        {
                            "titulo": "Proyecto inválido",
                            "descripcion": "Test",
                            "presupuesto": -5000,
                            "rubro": "CONSTRUCCION",
                            "distrito": "Lima", "provincia": "Lima", "region": "Lima"
                        }
                        """)
                .when().post("/proyectos")
                .then().statusCode(400);
    }

    // =========================================================
    // HAPPY PATH ENTIDAD_PUBLICA
    // =========================================================

    @Test
    @Order(8)
    @DisplayName("[POST /proyectos] ENTIDAD 201 - crear proyecto exitosamente")
    void crearProyecto_datosCompletos_retorna201() {
        assumeEntidadToken();
        String fechaInicio = LocalDate.now().plusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String fechaFin    = LocalDate.now().plusMonths(7).format(DateTimeFormatter.ISO_LOCAL_DATE);

        Response resp = asEntidad()
                .body("""
                        {
                            "titulo": "Construcción de Pista Vehicular - Test Integración",
                            "descripcion": "Proyecto de construcción de pista vehicular en zona norte de la ciudad",
                            "presupuesto": 500000.00,
                            "rubro": "CONSTRUCCION_VIAL",
                            "distrito": "San Miguel",
                            "provincia": "Lima",
                            "region": "Lima",
                            "direccion": "Av. La Marina cdra 20",
                            "fechaInicioPrevista": "%s",
                            "fechaFinPrevista": "%s",
                            "plazoMeses": 6,
                            "requisitos": "ISO 9001, Experiencia mínima 5 años en obras viales"
                        }
                        """.formatted(fechaInicio, fechaFin))
                .when().post("/proyectos")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("titulo", equalTo("Construcción de Pista Vehicular - Test Integración"))
                .body("estado", equalTo("BORRADOR"))
                .body("codigoUnico", notNullValue())
                .body("presupuesto", equalTo(500000.0f))
                .extract().response();

        proyectoId = resp.jsonPath().getString("id");
        System.out.println("[TEST] Proyecto creado: id=" + proyectoId);
    }

    @Test
    @Order(9)
    @DisplayName("[GET /proyectos] ENTIDAD 200 - lista los proyectos propios")
    void listarProyectos_comoEntidad_retorna200() {
        assumeEntidadToken();
        asEntidad()
                .when().get("/proyectos")
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class));
    }

    @Test
    @Order(10)
    @DisplayName("[GET /proyectos/{id}] ENTIDAD 200 - obtiene el proyecto por ID")
    void getProyectoPorId_comoEntidad_retorna200() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null, "No hay proyecto creado");

        asEntidad()
                .when().get("/proyectos/" + proyectoId)
                .then()
                .statusCode(200)
                .body("id", equalTo(proyectoId))
                .body("titulo", notNullValue())
                .body("estado", notNullValue());
    }

    @Test
    @Order(11)
    @DisplayName("[GET /proyectos/{id}] ENTIDAD 404 - ID de proyecto inexistente")
    void getProyecto_idInexistente_retorna404() {
        assumeEntidadToken();
        asEntidad()
                .when().get("/proyectos/00000000-0000-0000-0000-000000000000")
                .then().statusCode(404);
    }

    @Test
    @Order(12)
    @DisplayName("[PUT /proyectos/{id}] ENTIDAD 200 - actualizar título y descripción")
    void actualizarProyecto_datosValidos_retorna200() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null, "No hay proyecto creado");

        asEntidad()
                .body("""
                        {
                            "titulo": "Construcción de Pista Vehicular - ACTUALIZADO",
                            "descripcion": "Descripción actualizada en test de integración",
                            "presupuesto": 520000.00,
                            "rubro": "CONSTRUCCION_VIAL",
                            "distrito": "San Miguel",
                            "provincia": "Lima",
                            "region": "Lima"
                        }
                        """)
                .when().put("/proyectos/" + proyectoId)
                .then()
                .statusCode(200)
                .body("titulo", equalTo("Construcción de Pista Vehicular - ACTUALIZADO"));
    }

    @Test
    @Order(13)
    @DisplayName("[POST /proyectos/{id}/candidatos] ENTIDAD 201 - agregar candidato por RUC")
    void agregarCandidato_rucValido_retorna201oError() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null, "No hay proyecto creado");

        Response resp = asEntidad()
                .body("""
                        {"ruc": "20100028698"}
                        """)
                .when().post("/proyectos/" + proyectoId + "/candidatos")
                .then()
                .statusCode(anyOf(equalTo(201), equalTo(400), equalTo(422), equalTo(503)))
                .extract().response();

        if (resp.statusCode() == 201) {
            candidatoId = resp.jsonPath().getString("id");
            System.out.println("[TEST] Candidato agregado: id=" + candidatoId);
        } else {
            System.out.println("[TEST] Agregar candidato retornó " + resp.statusCode()
                    + " (puede requerir LatInfo API activa): " + resp.asString());
        }
    }

    @Test
    @Order(14)
    @DisplayName("[GET /proyectos/{id}/candidatos] ENTIDAD 200 - lista candidatos del proyecto")
    void listarCandidatos_comoEntidad_retorna200() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null, "No hay proyecto creado");

        asEntidad()
                .when().get("/proyectos/" + proyectoId + "/candidatos")
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class));
    }

    @Test
    @Order(15)
    @DisplayName("[GET /proyectos/{id}/comparador] ENTIDAD 200 - comparador IA de candidatos")
    void compararCandidatos_comoEntidad_retorna200() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null, "No hay proyecto creado");

        asEntidad()
                .when().get("/proyectos/" + proyectoId + "/comparador")
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class));
    }

    @Test
    @Order(16)
    @DisplayName("[DELETE /proyectos/{id}/candidatos/{candidatoId}] ENTIDAD 204 - eliminar candidato")
    void eliminarCandidato_idValido_retorna204() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null && candidatoId != null, "No hay candidato creado");

        asEntidad()
                .when().delete("/proyectos/" + proyectoId + "/candidatos/" + candidatoId)
                .then().statusCode(204);
    }

    @Test
    @Order(17)
    @DisplayName("[PUT /proyectos/{id}/estado] ENTIDAD 200 - cambiar estado a EN_EVALUACION")
    void actualizarEstado_aEnEvaluacion_retorna200() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null, "No hay proyecto creado");

        asEntidad()
                .body("""
                        {"estado": "EN_EVALUACION"}
                        """)
                .when().put("/proyectos/" + proyectoId + "/estado")
                .then()
                .statusCode(200)
                .body("estado", equalTo("EN_EVALUACION"));
    }

    @Test
    @Order(18)
    @DisplayName("[POST /proyectos/{id}/adjudicar] ENTIDAD 400 - sin candidatos seleccionados")
    void adjudicar_sinCandidatoSeleccionado_retornaError() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null, "No hay proyecto creado");

        asEntidad()
                .body("""
                        {"candidatoId": "00000000-0000-0000-0000-000000000000"}
                        """)
                .when().post("/proyectos/" + proyectoId + "/adjudicar")
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(404), equalTo(422)));
    }
}
