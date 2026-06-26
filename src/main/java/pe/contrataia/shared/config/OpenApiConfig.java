package pe.contrataia.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ContrataIA Perú API")
                        .description("""
                                API REST para la plataforma de gestión de contratos públicos ContrataIA Perú.

                                ## Autenticación
                                La mayoría de endpoints requieren un token JWT. Para obtenerlo:
                                1. Registra una cuenta con `/auth/register/entidad` o `/auth/register/empresa`
                                2. Verifica tu correo con `/auth/verify-otp`
                                3. Inicia sesión con `/auth/login` y copia el `accessToken`
                                4. Haz clic en **Authorize** e ingresa: `Bearer {accessToken}`

                                ## Roles
                                - **ENTIDAD_PUBLICA**: Acceso a proyectos, proveedores y seguimiento
                                - **EMPRESA**: Acceso a perfil y gestión de empresa
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ContrataIA Perú")
                                .email("soporte@contrataia.pe")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingresa el token JWT obtenido del endpoint /auth/login")));
    }
}
