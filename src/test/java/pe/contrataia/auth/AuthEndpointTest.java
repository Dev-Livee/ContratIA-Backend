package pe.contrataia.auth;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import pe.contrataia.BaseIntegrationTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Tests del controlador /auth
 * Cubre: registro (validación), login, refresh y logout
 * Roles probados: Entidad Pública, Empresa, Ciudadano (anónimo)
 */
@DisplayName("Auth Endpoints - /auth")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthEndpointTest extends BaseIntegrationTest {

    @BeforeAll
    void setup() {
        loginAll();
    }

    // =========================================================
    // REGISTRO - Validaciones (no requieren OTP)
    // =========================================================

    @Test
    @Order(1)
    @DisplayName("[POST /auth/register/entidad] 400 - cuerpo vacío")
    void registrarEntidad_sinDatos_retorna400() {
        given().contentType(ContentType.JSON)
                .body("{}")
                .when().post("/auth/register/entidad")
                .then().statusCode(400);
    }

    @Test
    @Order(2)
    @DisplayName("[POST /auth/register/entidad] 400 - RUC con menos de 11 dígitos")
    void registrarEntidad_rucInvalido_retorna400() {
        given().contentType(ContentType.JSON)
                .body("""
                        {
                            "ruc": "2012345",
                            "razonSocial": "Municipalidad Test",
                            "email": "muni.invalida@test.com",
                            "dniRepresentante": "12345678",
                            "representanteLegal": "Juan Perez",
                            "password": "TestPass123!",
                            "tipo": "MUNICIPALIDAD",
                            "distrito": "Lima", "provincia": "Lima", "region": "Lima",
                            "telefono": "999888777", "cargo": "Alcalde"
                        }
                        """)
                .when().post("/auth/register/entidad")
                .then().statusCode(400);
    }

    @Test
    @Order(3)
    @DisplayName("[POST /auth/register/entidad] 400 - contraseña menor a 8 caracteres")
    void registrarEntidad_passwordCorta_retorna400() {
        given().contentType(ContentType.JSON)
                .body("""
                        {
                            "ruc": "20111222333",
                            "razonSocial": "Municipalidad Test",
                            "email": "muni.pass@test.com",
                            "dniRepresentante": "12345678",
                            "representanteLegal": "Juan Perez",
                            "password": "123",
                            "tipo": "MUNICIPALIDAD",
                            "distrito": "Lima", "provincia": "Lima", "region": "Lima",
                            "telefono": "999888777", "cargo": "Alcalde"
                        }
                        """)
                .when().post("/auth/register/entidad")
                .then().statusCode(400);
    }

    @Test
    @Order(4)
    @DisplayName("[POST /auth/register/entidad] 400 - email con formato inválido")
    void registrarEntidad_emailInvalido_retorna400() {
        given().contentType(ContentType.JSON)
                .body("""
                        {
                            "ruc": "20111222334",
                            "razonSocial": "Municipalidad Test",
                            "email": "no-es-un-email",
                            "dniRepresentante": "12345678",
                            "representanteLegal": "Juan Perez",
                            "password": "TestPass123!",
                            "tipo": "MUNICIPALIDAD",
                            "distrito": "Lima", "provincia": "Lima", "region": "Lima",
                            "telefono": "999888777", "cargo": "Alcalde"
                        }
                        """)
                .when().post("/auth/register/entidad")
                .then().statusCode(400);
    }

    @Test
    @Order(5)
    @DisplayName("[POST /auth/register/empresa] 400 - cuerpo vacío")
    void registrarEmpresa_sinDatos_retorna400() {
        given().contentType(ContentType.JSON)
                .body("{}")
                .when().post("/auth/register/empresa")
                .then().statusCode(400);
    }

    @Test
    @Order(6)
    @DisplayName("[POST /auth/register/empresa] 400 - DNI representante con menos de 8 dígitos")
    void registrarEmpresa_dniInvalido_retorna400() {
        given().contentType(ContentType.JSON)
                .body("""
                        {
                            "ruc": "20555444333",
                            "razonSocial": "Empresa Test SAC",
                            "email": "empresa.dni@test.com",
                            "dniRepresentante": "123",
                            "representanteLegal": "Maria Garcia",
                            "password": "TestPass123!",
                            "sector": "CONSTRUCCION",
                            "telefono": "999888777"
                        }
                        """)
                .when().post("/auth/register/empresa")
                .then().statusCode(400);
    }

    // =========================================================
    // VERIFICAR OTP - Validaciones
    // =========================================================

    @Test
    @Order(7)
    @DisplayName("[POST /auth/verify-otp] 400 - OTP con menos de 6 dígitos")
    void verificarOtp_codigoCortfo_retorna400() {
        given().contentType(ContentType.JSON)
                .body("""
                        {"email": "test@test.com", "codigo": "12"}
                        """)
                .when().post("/auth/verify-otp")
                .then().statusCode(400);
    }

    @Test
    @Order(8)
    @DisplayName("[POST /auth/verify-otp] 400 - OTP con letras (no numérico)")
    void verificarOtp_codigoConLetras_retorna400() {
        given().contentType(ContentType.JSON)
                .body("""
                        {"email": "test@test.com", "codigo": "ABCDEF"}
                        """)
                .when().post("/auth/verify-otp")
                .then().statusCode(400);
    }

    @Test
    @Order(9)
    @DisplayName("[POST /auth/verify-otp] 400 - OTP válido pero email inexistente")
    void verificarOtp_emailInexistente_retornaError() {
        given().contentType(ContentType.JSON)
                .body("""
                        {"email": "noexiste@contrataia.pe", "codigo": "123456"}
                        """)
                .when().post("/auth/verify-otp")
                .then().statusCode(anyOf(equalTo(400), equalTo(404)));
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @Test
    @Order(10)
    @DisplayName("[POST /auth/login] 400 - email con formato inválido")
    void login_emailInvalido_retorna400() {
        given().contentType(ContentType.JSON)
                .body("""
                        {"email": "noesuncorreo", "password": "TestPass123!"}
                        """)
                .when().post("/auth/login")
                .then().statusCode(400);
    }

    @Test
    @Order(11)
    @DisplayName("[POST /auth/login] 400 - password vacía")
    void login_passwordVacia_retorna400() {
        given().contentType(ContentType.JSON)
                .body("""
                        {"email": "alguien@test.com", "password": ""}
                        """)
                .when().post("/auth/login")
                .then().statusCode(400);
    }

    @Test
    @Order(12)
    @DisplayName("[POST /auth/login] 401 - usuario inexistente")
    void login_usuarioInexistente_retorna401() {
        given().contentType(ContentType.JSON)
                .body("""
                        {"email": "fantasma@contrataia.pe", "password": "Cualquier123!"}
                        """)
                .when().post("/auth/login")
                .then().statusCode(401);
    }

    @Test
    @Order(13)
    @DisplayName("[POST /auth/login] 401 - contraseña incorrecta")
    void login_passwordIncorrecta_retorna401() {
        assumeEntidadToken(); // solo si el usuario existe
        given().contentType(ContentType.JSON)
                .body("""
                        {"email": "%s", "password": "PassMalona999!"}
                        """.formatted(TEST_ENTIDAD_EMAIL))
                .when().post("/auth/login")
                .then().statusCode(401);
    }

    @Test
    @Order(14)
    @DisplayName("[POST /auth/login] 200 - login exitoso como ENTIDAD_PUBLICA")
    void login_entidadPublica_retorna200ConToken() {
        assumeEntidadToken();
        given().contentType(ContentType.JSON)
                .body("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(TEST_ENTIDAD_EMAIL, TEST_ENTIDAD_PASSWORD))
                .when().post("/auth/login")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("tokenType", equalTo("Bearer"))
                .body("role", equalTo("ENTIDAD_PUBLICA"))
                .body("email", equalTo(TEST_ENTIDAD_EMAIL));
    }

    @Test
    @Order(15)
    @DisplayName("[POST /auth/login] 200 - login exitoso como EMPRESA")
    void login_empresa_retorna200ConToken() {
        assumeEmpresaToken();
        given().contentType(ContentType.JSON)
                .body("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(TEST_EMPRESA_EMAIL, TEST_EMPRESA_PASSWORD))
                .when().post("/auth/login")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("role", equalTo("EMPRESA"))
                .body("email", equalTo(TEST_EMPRESA_EMAIL));
    }

    // =========================================================
    // REFRESH TOKEN
    // =========================================================

    @Test
    @Order(16)
    @DisplayName("[POST /auth/refresh] 401 - refresh token inválido/inventado")
    void refresh_tokenInvalido_retorna401() {
        given().contentType(ContentType.JSON)
                .body("""
                        {"refreshToken": "este-token-no-existe-ni-existirá"}
                        """)
                .when().post("/auth/refresh")
                .then().statusCode(401);
    }

    @Test
    @Order(17)
    @DisplayName("[POST /auth/refresh] 200 - nuevo accessToken con refresh token válido")
    void refresh_tokenValido_retornaNuevoToken() {
        assumeEntidadToken();
        given().contentType(ContentType.JSON)
                .body("""
                        {"refreshToken": "%s"}
                        """.formatted(entidadRefreshToken))
                .when().post("/auth/refresh")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("tokenType", equalTo("Bearer"));
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    @Test
    @Order(18)
    @DisplayName("[POST /auth/logout] 200 - logout exitoso con refresh token válido")
    void logout_tokenValido_retorna200() {
        assumeEmpresaToken();
        given().contentType(ContentType.JSON)
                .body("""
                        {"refreshToken": "%s"}
                        """.formatted(empresaRefreshToken))
                .when().post("/auth/logout")
                .then()
                .statusCode(200)
                .body("message", notNullValue());
    }
}
