package pe.contrataia.seguimiento;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import pe.contrataia.BaseIntegrationTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.*;

/**
 * Tests del controlador /proyectos/{proyectoId}/* (Seguimiento)
 *
 * ACCESO ESPERADO:
 *   ENTIDAD_PUBLICA → CRUD completo en hitos, avances, evidencias, documentos, incidencias
 *   EMPRESA         → 403 en todos los sub-recursos
 *   CIUDADANO       → 401 en todos los sub-recursos
 *
 * El @BeforeAll crea un proyecto de prueba reutilizable por todos los tests de esta clase.
 */
@DisplayName("Seguimiento Endpoints - /proyectos/{id}/hitos|avances|evidencias|documentos|incidencias")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SeguimientoEndpointTest extends BaseIntegrationTest {

    private static String proyectoId;
    private static String hitoId;
    private static String incidenciaId;

    @BeforeAll
    void setup() {
        loginAll();
        crearProyectoParaTests();
    }

    private void crearProyectoParaTests() {
        if (entidadToken == null) {
            System.err.println("[TEST SETUP] No hay token de ENTIDAD. Los tests de seguimiento serán omitidos.");
            return;
        }
        String fechaInicio = LocalDate.now().plusWeeks(2).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String fechaFin    = LocalDate.now().plusMonths(4).format(DateTimeFormatter.ISO_LOCAL_DATE);

        Response resp = asEntidad()
                .body("""
                        {
                            "titulo": "Proyecto para Tests de Seguimiento",
                            "descripcion": "Proyecto auxiliar creado automáticamente por los tests",
                            "presupuesto": 200000.00,
                            "rubro": "INFRAESTRUCTURA",
                            "distrito": "Surco",
                            "provincia": "Lima",
                            "region": "Lima",
                            "direccion": "Calle Los Tests 123",
                            "fechaInicioPrevista": "%s",
                            "fechaFinPrevista": "%s",
                            "plazoMeses": 3,
                            "requisitos": "Experiencia en infraestructura urbana"
                        }
                        """.formatted(fechaInicio, fechaFin))
                .when().post("/proyectos")
                .then().extract().response();

        if (resp.statusCode() == 201) {
            proyectoId = resp.jsonPath().getString("id");
            System.out.println("[TEST SETUP] Proyecto auxiliar creado: id=" + proyectoId);
        } else {
            System.err.println("[TEST SETUP] No se pudo crear proyecto auxiliar: " + resp.asString());
        }
    }

    // =========================================================
    // CONTROL DE ACCESO
    // =========================================================

    @Test
    @Order(1)
    @DisplayName("[POST /proyectos/{id}/hitos] CIUDADANO 401 - sin token")
    void crearHito_sinToken_retorna401() {
        asCiudadano()
                .body("{\"nombre\": \"Hito 1\"}")
                .when().post("/proyectos/00000000-0000-0000-0000-000000000001/hitos")
                .then().statusCode(401);
    }

    @Test
    @Order(2)
    @DisplayName("[GET /proyectos/{id}/hitos] EMPRESA 403 - rol incorrecto")
    void listarHitos_comoEmpresa_retorna403() {
        assumeEmpresaToken();
        asEmpresa()
                .when().get("/proyectos/00000000-0000-0000-0000-000000000001/hitos")
                .then().statusCode(403);
    }

    @Test
    @Order(3)
    @DisplayName("[POST /proyectos/{id}/avances] EMPRESA 403 - rol incorrecto")
    void registrarAvance_comoEmpresa_retorna403() {
        assumeEmpresaToken();
        asEmpresa()
                .body("{\"avanceFisico\": 10}")
                .when().post("/proyectos/00000000-0000-0000-0000-000000000001/avances")
                .then().statusCode(403);
    }

    @Test
    @Order(4)
    @DisplayName("[GET /proyectos/{id}/incidencias] CIUDADANO 401 - sin token")
    void listarIncidencias_sinToken_retorna401() {
        asCiudadano()
                .when().get("/proyectos/00000000-0000-0000-0000-000000000001/incidencias")
                .then().statusCode(401);
    }

    // =========================================================
    // HITOS
    // =========================================================

    @Test
    @Order(5)
    @DisplayName("[POST /proyectos/{id}/hitos] ENTIDAD 400 - sin nombre")
    void crearHito_sinNombre_retorna400() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null, "No hay proyecto auxiliar");

        asEntidad()
                .body("{}")
                .when().post("/proyectos/" + proyectoId + "/hitos")
                .then().statusCode(400);
    }

    @Test
    @Order(6)
    @DisplayName("[POST /proyectos/{id}/hitos] ENTIDAD 201 - crear hito")
    void crearHito_datosValidos_retorna201() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null, "No hay proyecto auxiliar");

        Response resp = asEntidad()
                .body("""
                        {
                            "nombre": "Hito 1: Preparación del terreno",
                            "descripcion": "Limpieza, nivelación y delimitación del área de trabajo",
                            "fechaPrevista": "%s",
                            "orden": 1
                        }
                        """.formatted(LocalDate.now().plusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .when().post("/proyectos/" + proyectoId + "/hitos")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("nombre", equalTo("Hito 1: Preparación del terreno"))
                .body("estado", equalTo("PENDIENTE"))
                .extract().response();

        hitoId = resp.jsonPath().getString("id");
        System.out.println("[TEST] Hito creado: id=" + hitoId);
    }

    @Test
    @Order(7)
    @DisplayName("[GET /proyectos/{id}/hitos] ENTIDAD 200 - listar hitos del proyecto")
    void listarHitos_comoEntidad_retorna200() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null, "No hay proyecto auxiliar");

        asEntidad()
                .when().get("/proyectos/" + proyectoId + "/hitos")
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class));
    }

    @Test
    @Order(8)
    @DisplayName("[PUT /proyectos/{id}/hitos/{hitoId}] ENTIDAD 200 - actualizar hito")
    void actualizarHito_datosValidos_retorna200() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null && hitoId != null, "No hay hito creado");

        asEntidad()
                .body("""
                        {
                            "nombre": "Hito 1: Preparación del terreno - EN PROGRESO",
                            "fechaPrevista": "%s",
                            "orden": 1
                        }
                        """.formatted(LocalDate.now().plusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .when().put("/proyectos/" + proyectoId + "/hitos/" + hitoId)
                .then()
                .statusCode(200)
                .body("nombre", containsString("EN PROGRESO"));
    }

    // =========================================================
    // AVANCES
    // =========================================================

    @Test
    @Order(9)
    @DisplayName("[POST /proyectos/{id}/avances] ENTIDAD 400 - sin avanceFisico")
    void registrarAvance_sinAvanceFisico_retorna400() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null, "No hay proyecto auxiliar");

        asEntidad()
                .body("""
                        {"descripcion": "Avance sin porcentaje"}
                        """)
                .when().post("/proyectos/" + proyectoId + "/avances")
                .then().statusCode(400);
    }

    @Test
    @Order(10)
    @DisplayName("[POST /proyectos/{id}/avances] ENTIDAD 201 - registrar avance con porcentaje")
    void registrarAvance_datosValidos_retorna201() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null, "No hay proyecto auxiliar");

        asEntidad()
                .body("""
                        {
                            "avanceFisico": 25.5,
                            "avanceFinanciero": 20.0,
                            "descripcion": "Se completó la preparación del terreno y las excavaciones iniciales"
                        }
                        """)
                .when().post("/proyectos/" + proyectoId + "/avances")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("avanceFisico", equalTo(25.5f));
    }

    @Test
    @Order(11)
    @DisplayName("[GET /proyectos/{id}/avances] ENTIDAD 200 - listar historial de avances")
    void listarAvances_comoEntidad_retorna200() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null, "No hay proyecto auxiliar");

        asEntidad()
                .when().get("/proyectos/" + proyectoId + "/avances")
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class));
    }

    // =========================================================
    // EVIDENCIAS
    // =========================================================

    @Test
    @Order(12)
    @DisplayName("[POST /proyectos/{id}/evidencias] ENTIDAD 201 - agregar evidencia tipo FOTO")
    void agregarEvidencia_foto_retorna201() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null, "No hay proyecto auxiliar");

        asEntidad()
                .body("""
                        {
                            "tipo": "FOTO",
                            "nombre": "Avance semana 1 - vista frontal",
                            "url": "https://storage.contrataia.pe/evidencias/foto-001.jpg",
                            "descripcion": "Vista frontal del terreno nivelado al finalizar la semana 1",
                            "esPublico": true
                        }
                        """)
                .when().post("/proyectos/" + proyectoId + "/evidencias")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("tipo", equalTo("FOTO"))
                .body("esPublico", equalTo(true));
    }

    @Test
    @Order(13)
    @DisplayName("[POST /proyectos/{id}/evidencias] ENTIDAD 201 - agregar evidencia tipo DOCUMENTO")
    void agregarEvidencia_documento_retorna201() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null, "No hay proyecto auxiliar");

        asEntidad()
                .body("""
                        {
                            "tipo": "DOCUMENTO",
                            "nombre": "Informe técnico semana 1",
                            "url": "https://storage.contrataia.pe/evidencias/informe-001.pdf",
                            "descripcion": "Informe técnico del supervisor de obra",
                            "esPublico": false
                        }
                        """)
                .when().post("/proyectos/" + proyectoId + "/evidencias")
                .then()
                .statusCode(201)
                .body("tipo", equalTo("DOCUMENTO"))
                .body("esPublico", equalTo(false));
    }

    @Test
    @Order(14)
    @DisplayName("[GET /proyectos/{id}/evidencias] ENTIDAD 200 - listar evidencias")
    void listarEvidencias_comoEntidad_retorna200() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null, "No hay proyecto auxiliar");

        asEntidad()
                .when().get("/proyectos/" + proyectoId + "/evidencias")
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class));
    }

    // =========================================================
    // DOCUMENTOS DEL PROYECTO
    // =========================================================

    @Test
    @Order(15)
    @DisplayName("[POST /proyectos/{id}/documentos] ENTIDAD 201 - agregar documento contractual")
    void agregarDocumento_retorna201() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null, "No hay proyecto auxiliar");

        asEntidad()
                .body("""
                        {
                            "tipo": "CONTRATO",
                            "nombre": "Contrato Principal de Obra",
                            "url": "https://storage.contrataia.pe/docs/contrato-principal.pdf",
                            "descripcion": "Contrato firmado entre entidad y empresa adjudicada",
                            "esPublico": true
                        }
                        """)
                .when().post("/proyectos/" + proyectoId + "/documentos")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("tipo", equalTo("CONTRATO"));
    }

    @Test
    @Order(16)
    @DisplayName("[GET /proyectos/{id}/documentos] ENTIDAD 200 - listar documentos del proyecto")
    void listarDocumentos_comoEntidad_retorna200() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null, "No hay proyecto auxiliar");

        asEntidad()
                .when().get("/proyectos/" + proyectoId + "/documentos")
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class));
    }

    // =========================================================
    // INCIDENCIAS
    // =========================================================

    @Test
    @Order(17)
    @DisplayName("[POST /proyectos/{id}/incidencias] ENTIDAD 201 - reportar incidencia RETRASO")
    void reportarIncidencia_retraso_retorna201() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null, "No hay proyecto auxiliar");

        Response resp = asEntidad()
                .body("""
                        {
                            "titulo": "Retraso por lluvias intensas",
                            "descripcion": "Las lluvias de los últimos 3 días han paralizado la excavación",
                            "tipo": "RETRASO",
                            "severidad": "MEDIA"
                        }
                        """)
                .when().post("/proyectos/" + proyectoId + "/incidencias")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("estado", equalTo("ABIERTA"))
                .body("tipo", equalTo("RETRASO"))
                .body("severidad", equalTo("MEDIA"))
                .extract().response();

        incidenciaId = resp.jsonPath().getString("id");
        System.out.println("[TEST] Incidencia creada: id=" + incidenciaId);
    }

    @Test
    @Order(18)
    @DisplayName("[POST /proyectos/{id}/incidencias] ENTIDAD 201 - reportar incidencia CRITICA")
    void reportarIncidencia_critica_retorna201() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null, "No hay proyecto auxiliar");

        asEntidad()
                .body("""
                        {
                            "titulo": "Accidente laboral con herido grave",
                            "descripcion": "Trabajador sufrió caída desde andamio a 3m de altura",
                            "tipo": "ACCIDENTE",
                            "severidad": "CRITICA"
                        }
                        """)
                .when().post("/proyectos/" + proyectoId + "/incidencias")
                .then()
                .statusCode(201)
                .body("severidad", equalTo("CRITICA"))
                .body("estado", equalTo("ABIERTA"));
    }

    @Test
    @Order(19)
    @DisplayName("[GET /proyectos/{id}/incidencias] ENTIDAD 200 - listar incidencias")
    void listarIncidencias_comoEntidad_retorna200() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null, "No hay proyecto auxiliar");

        asEntidad()
                .when().get("/proyectos/" + proyectoId + "/incidencias")
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class));
    }

    @Test
    @Order(20)
    @DisplayName("[PATCH /proyectos/{id}/incidencias/{incidenciaId}/resolver] ENTIDAD 200 - resolver incidencia")
    void resolverIncidencia_datosValidos_retorna200() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null && incidenciaId != null, "No hay incidencia creada");

        asEntidad()
                .body("""
                        {
                            "resolucion": "Se instalaron estructuras de drenaje y se reanudaron trabajos con personal de seguridad adicional"
                        }
                        """)
                .when().patch("/proyectos/" + proyectoId + "/incidencias/" + incidenciaId + "/resolver")
                .then()
                .statusCode(200)
                .body("estado", anyOf(equalTo("RESUELTA"), equalTo("EN_PROCESO"), equalTo("CERRADA")))
                .body("resolucion", notNullValue());
    }

    @Test
    @Order(21)
    @DisplayName("[PATCH /proyectos/{id}/incidencias/{incidenciaId}/resolver] ENTIDAD 404 - incidencia inexistente")
    void resolverIncidencia_idInexistente_retorna404() {
        assumeEntidadToken();
        Assumptions.assumeTrue(proyectoId != null, "No hay proyecto auxiliar");

        asEntidad()
                .body("""
                        {"resolucion": "Test resolución"}
                        """)
                .when().patch("/proyectos/" + proyectoId + "/incidencias/00000000-0000-0000-0000-000000000000/resolver")
                .then()
                .statusCode(anyOf(equalTo(404), equalTo(400)));
    }
}
