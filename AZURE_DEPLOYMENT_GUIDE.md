# 📘 Guía Completa: Desplegar Spring Boot en Azure App Service

**Proyecto:** ContratIA Backend  
**Stack:** Spring Boot 3.3.5 | Maven | Java 21  
**Objetivo:** Deploy 100% funcional sin página default de Azure

---

## 🎯 Índice
1. [Paso 1: Generar el BUILD](#paso-1-generar-el-build)
2. [Paso 2: Configuración de Azure App Service](#paso-2-configuración-de-azure-app-service)
3. [Paso 3: Desplegar el JAR](#paso-3-desplegar-el-jar)
4. [Paso 4: Startup Command y Variables](#paso-4-startup-command-y-variables-de-entorno)
5. [Paso 5: Validar Deployment](#paso-5-validar-deployment)
6. [Paso 6: Troubleshooting](#paso-6-troubleshooting)
7. [Errores Comunes y Soluciones](#errores-comunes-y-soluciones)

---

## Paso 1: Generar el BUILD

### 1.1 Prerequisitos Locales
```bash
# Verificar Java 21
java -version

# Verificar Maven 3.8+
mvn -version
```

### 1.2 Limpiar y Compilar
```bash
# En la raíz del proyecto /ContratIA-Backend
cd /Users/maycolrojas/Documents/GitHub/ContratIA-MVP/ContratIA-Backend

# Limpiar builds anteriores
mvn clean

# Compilar y generar JAR
mvn package -DskipTests -X

# O con más detalles si hay problemas:
mvn clean package -DskipTests -e -X
```

### 1.3 Ubicación del JAR generado
```
✅ El JAR estará en:
target/contrataia-backend-1.0.0-SNAPSHOT.jar

✅ Tamaño esperado: ~60-100MB (con dependencias embebidas)
```

### 1.4 Optimizar el JAR para Azure (Opcional pero Recomendado)
Agrega al `pom.xml` en la sección `<build>` para generar un JAR más pequeño:

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <excludes>
            <exclude>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
            </exclude>
        </excludes>
        <!-- Opcional: usar layers para Docker -->
        <layers>
            <enabled>true</enabled>
        </layers>
    </configuration>
</plugin>
```

---

## Paso 2: Configuración de Azure App Service

### Opción A: Crear App Service en Azure Portal

**Paso 2.1: Crear el recurso**
1. Ir a **Azure Portal** → **Create a resource** → **App Service**

**Paso 2.2: Llenar formulario**
```
Suscripción: Tu suscripción
Grupo de recursos: contrataia-rg (crear si no existe)
Nombre: contrataia-backend (máx 60 caracteres, sin espacios)
Publicar: Code
Runtime stack: Java 21 ⭐ (MUY IMPORTANTE)
Sistema operativo: Linux (recomendado) o Windows
Plan: B1 (Basic) mínimo para producción, o B2
Región: East US | West Europe | Central India (según tus usuarios)
```

**Paso 2.3: Verificar configuración de Runtime**
Después de crear, ve a:
- **Settings** → **Configuration** → **General settings**

```
Stack settings:
- Java version: 21 ✅
- Java web server container: Tomcat 10.1 (por defecto)
- Java web server container version: LATEST
```

### Opción B: Crear con Azure CLI (Automatizado)
```bash
# Variables
RG_NAME="contrataia-rg"
APP_NAME="contrataia-backend"
LOCATION="eastus"
PLAN_NAME="contrataia-plan"

# Crear grupo de recursos
az group create \
  --name $RG_NAME \
  --location $LOCATION

# Crear plan App Service
az appservice plan create \
  --name $PLAN_NAME \
  --resource-group $RG_NAME \
  --sku B1 \
  --is-linux

# Crear App Service
az webapp create \
  --resource-group $RG_NAME \
  --plan $PLAN_NAME \
  --name $APP_NAME \
  --runtime "java|21"
```

---

## Paso 3: Desplegar el JAR

### Opción 3.1: Zip Deploy (RECOMENDADO para JAR)

**Paso 3.1.1: Preparar estructura**
```bash
# Crea carpeta D:\home\site\wwwroot en la máquina local
mkdir -p deployment
cd deployment

# Copiar JAR
cp ../target/contrataia-backend-1.0.0-SNAPSHOT.jar ./app.jar

# Crear archivo startup.sh (CRÍTICO)
# Ver Paso 4
cat > startup.sh << 'EOF'
#!/bin/bash
cd /home/site/wwwroot
java -Xms256m -Xmx512m -jar app.jar \
  --spring.profiles.active=azure \
  --server.port=80
EOF

chmod +x startup.sh

# Crear archivo .deployment para que Azure sepa qué hacer
cat > .deployment << 'EOF'
[config]
command = cd /home/site/wwwroot && bash startup.sh
SCM_DO_BUILD_DURING_DEPLOYMENT = false
EOF

# Crear ZIP
zip -r deployment.zip app.jar startup.sh .deployment
```

**Paso 3.1.2: Subir con Azure CLI**
```bash
# Variables
RESOURCE_GROUP="contrataia-rg"
APP_NAME="contrataia-backend"

# Deploy con Zip
az webapp deployment source config-zip \
  --resource-group $RESOURCE_GROUP \
  --name $APP_NAME \
  --src deployment.zip
```

**Paso 3.1.3: O subir vía Portal**
1. Ve a **Deployment** → **Deployment Center**
2. Selecciona **Manual deployment**
3. Carga el archivo `deployment.zip`

---

### Opción 3.2: Deploy directo JAR (Más simple)

**Método 1: A través de Deployment Center en Portal**
```
1. App Service → Deployment → Deployment Center
2. Selecciona "External Git" o "Manual (ZIP)"
3. Carga el JAR directamente
```

**Método 2: Con Azure CLI**
```bash
az webapp deploy \
  --resource-group contrataia-rg \
  --name contrataia-backend \
  --src-path target/contrataia-backend-1.0.0-SNAPSHOT.jar \
  --type jar
```

---

## Paso 4: Startup Command y Variables de Entorno

### Paso 4.1: En Azure Portal

**UBICACIÓN:** App Service → **Settings** → **Configuration** → **General settings**

---

### 4.1.1 Configurar Startup Command

**Opción A: Startup Command básico (RECOMENDADO)**
```
java -Xms256m -Xmx512m -jar /home/site/wwwroot/app.jar --server.port=80 --spring.profiles.active=azure
```

**Opción B: Con variables de entorno**
```
java -Xms512m -Xmx1024m \
  -Dspring.config.location=file:///home/site/wwwroot/application-azure.yml \
  -jar /home/site/wwwroot/app.jar \
  --server.port=80 \
  --spring.profiles.active=azure
```

**Opción C: Full personalizado para tu proyecto**
```bash
java -Xms512m -Xmx1024m \
  -Dserver.shutdown=graceful \
  -Dspring.profiles.active=azure \
  -jar /home/site/wwwroot/app.jar \
  --server.port=80 \
  --server.servlet.context-path=/api \
  --management.endpoints.web.exposure.include=health,info,metrics
```

---

### 4.1.2 Configurar Variables de Entorno

En **Settings** → **Configuration** → **Application settings**, agrega:

```
JAVA_OPTS = -Xms512m -Xmx1024m
JAVA_VERSION = 21

# Base de datos PostgreSQL (reemplaza con tus valores)
SPRING_DATASOURCE_URL = jdbc:postgresql://tu-server.postgres.database.azure.com:5432/contrataia_db
SPRING_DATASOURCE_USERNAME = admin@tu-server
SPRING_DATASOURCE_PASSWORD = Tu$Password123!

# Redis (si usas Azure Cache for Redis)
SPRING_DATA_REDIS_HOST = contrataia-redis.redis.cache.windows.net
SPRING_DATA_REDIS_PORT = 6380
SPRING_DATA_REDIS_PASSWORD = Tu$RedisPassword123!
SPRING_DATA_REDIS_SSL = true

# JWT y Seguridad
JWT_SECRET = tu-secret-key-super-largo-aleatorio-aqui-123456
JWT_EXPIRATION = 86400000

# Mail (si está configurado)
SPRING_MAIL_HOST = smtp.gmail.com
SPRING_MAIL_PORT = 587
SPRING_MAIL_USERNAME = tu-email@gmail.com
SPRING_MAIL_PASSWORD = tu-app-password

# Logging
LOGGING_LEVEL_ROOT = INFO
LOGGING_LEVEL_PE_CONTRATAIA = DEBUG
```

⚠️ **CRÍTICO:** "Click Save" después de cada cambio.

---

### 4.1.3 archivo `application-azure.yml`

Crea en `src/main/resources/`:

```yaml
# application-azure.yml
spring:
  application:
    name: contrataia-backend
  
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/contrataia}
    username: ${SPRING_DATASOURCE_USERNAME:postgres}
    password: ${SPRING_DATASOURCE_PASSWORD:postgres}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
  
  data:
    redis:
      host: ${SPRING_DATA_REDIS_HOST:localhost}
      port: ${SPRING_DATA_REDIS_PORT:6379}
      password: ${SPRING_DATA_REDIS_PASSWORD:}
      ssl: ${SPRING_DATA_REDIS_SSL:false}
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: false
        dialect: org.hibernate.dialect.PostgreSQLDialect
  
  mail:
    host: ${SPRING_MAIL_HOST}
    port: ${SPRING_MAIL_PORT}
    username: ${SPRING_MAIL_USERNAME}
    password: ${SPRING_MAIL_PASSWORD}
  
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

server:
  port: 80
  servlet:
    context-path: /api
  error:
    include-message: always
    include-binding-errors: always
  compression:
    enabled: true
    min-response-size: 1024

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

logging:
  level:
    root: INFO
    pe.contrataia: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

---

## Paso 5: Validar Deployment

### 5.1 Verificar Estado en Portal
```
App Service → Overview
- Status: Running ✅
- Availability: 100%
- URL: https://contrataia-backend.azurewebsites.net
```

### 5.2 Revisar Logs en Tiempo Real

**Opción A: Log Stream en Portal**
```
App Service → Deployment → Log stream
(Refresh cada 2 segundos)
```

**Opción B: Con Azure CLI**
```bash
az webapp log tail \
  --resource-group contrataia-rg \
  --name contrataia-backend
```

**Opción C: Descargar logs completos**
```bash
az webapp log download \
  --resource-group contrataia-rg \
  --name contrataia-backend \
  --log-file contrataia-logs.zip
```

### 5.3 Probar Endpoints

```bash
# Health check
curl https://contrataia-backend.azurewebsites.net/api/actuator/health

# Respuesta esperada:
{
  "status": "UP",
  "database": {
    "status": "UP"
  },
  "redis": {
    "status": "UP"
  }
}

# Swagger/OpenAPI
https://contrataia-backend.azurewebsites.net/api/swagger-ui.html

# Endpoint específico (login)
curl -X POST https://contrataia-backend.azurewebsites.net/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"pass123"}'
```

### 5.4 Monitorar CPU y Memoria

```
App Service → Metrics
- Average CPU Percentage
- Memory Percentage
- HTTP Server Errors (4xx, 5xx)
```

---

## Paso 6: Troubleshooting

### 6.1 Pantalla "Hey, Java developers!" (ERROR CRÍTICO)

**CAUSA:** El JAR no se está ejecutando. Tomcat está sirviendo la página por defecto.

**SOLUCIONES:**

**Solución 1: Verificar Startup Command**
```bash
# En Portal: Configuration → General settings → Startup Command
# Debe estar correctamente configurado:
java -Xms512m -Xmx1024m -jar /home/site/wwwroot/app.jar --server.port=80
```

**Solución 2: Verificar que el JAR está en la ruta correcta**
```bash
# En Kudu Console (Advanced Tools):
# https://contrataia-backend.scm.azurewebsites.net/

ls -la /home/site/wwwroot/
# Debe mostrar: app.jar ✅
```

**Solución 3: Reiniciar el App Service**
```bash
az webapp restart \
  --resource-group contrataia-rg \
  --name contrataia-backend
```

**Solución 4: Verificar permisos del JAR**
```bash
# En Kudu Console
chmod +x /home/site/wwwroot/app.jar
```

---

### 6.2 Error: "Java process is not running"

**CAUSA:** Spring Boot crasha en startup.

**SOLUCIONES:**

1. **Revisar logs:**
   ```bash
   az webapp log tail --resource-group contrataia-rg --name contrataia-backend
   ```

2. **Errores comunes:**
   - ❌ Database connection refused
     - Verificar `SPRING_DATASOURCE_URL` y credenciales
     - Verificar firewall de Azure Database for PostgreSQL
   
   - ❌ Redis connection timeout
     - Verificar SSL: `SPRING_DATA_REDIS_SSL=true`
     - Verificar credenciales de Azure Cache for Redis
   
   - ❌ OutOfMemory
     - Aumentar `-Xmx` en Startup Command
     - Cambiar App Service Plan a tier superior

3. **Forzar redeploy desde cero:**
   ```bash
   # Eliminar JAR anterior
   az webapp deployment source config-zip \
     --resource-group contrataia-rg \
     --name contrataia-backend \
     --src deployment-clean.zip
   ```

---

### 6.3 Error 502 Bad Gateway

**CAUSA:** Spring Boot tardó demasiado en iniciar.

**SOLUCIONES:**

1. **Aumentar timeout:**
   ```bash
   # En Portal: Configuration → General settings
   Add Application Setting:
   - Name: SCM_COMMAND_IDLE_TIMEOUT
   - Value: 300 (segundos)
   ```

2. **Optimizar tiempo de startup:**
   - Revisar queries de Flyway lentas
   - Verificar conexión a base de datos
   - Aumentar `-Xms` (minimum memory)

---

### 6.4 Error 503 Service Unavailable

**CAUSA:** App está reiniciando continuamente o hay falta de memoria.

**SOLUCIONES:**

```bash
# 1. Ver si hay memory leaks
az webapp log tail --resource-group contrataia-rg --name contrataia-backend | grep -i memory

# 2. Aumentar App Service Plan
az appservice plan update \
  --name contrataia-plan \
  --resource-group contrataia-rg \
  --sku B2  # De B1 a B2

# 3. Activar Always On
az webapp config set \
  --resource-group contrataia-rg \
  --name contrataia-backend \
  --number-of-workers 1 \
  --always-on true
```

---

### 6.5 Requests lentos o timeouts

**CAUSA:** Problemas de red, base de datos lenta, o app service sin suficientes recursos.

**SOLUCIONES:**

```bash
# 1. Verificar latencia de base de datos
curl -v https://contrataia-backend.azurewebsites.net/api/actuator/health

# 2. Revisar Application Insights
Portal → Insights → Performance

# 3. Aumentar pool de conexiones en application-azure.yml:
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # Aumentar de 10 a 20
      minimum-idle: 5        # Aumentar de 2 a 5

# 4. Cambiar a App Service Plan con más CPU
```

---

## Errores Comunes y Soluciones

| Error | Causa | Solución |
|-------|-------|----------|
| "Hey Java developers!" | JAR no se ejecuta | Verificar Startup Command y permisos |
| 502 Bad Gateway | Timeout al iniciar | Aumentar `SCM_COMMAND_IDLE_TIMEOUT` |
| 503 Service Unavailable | OutOfMemory o reinicio continuo | Aumentar `-Xmx` y tier del App Service |
| Database connection refused | Firewall o credenciales | Verificar firewall y conexión |
| HTTP 500 | Error en endpoint | Revisar logs con `az webapp log tail` |
| HTTPS SSL errors | Certificado no configurado | Azure redirige automáticamente a HTTPS |

---

## Checklist Pre-Producción

- [ ] Java version = 21 en App Service
- [ ] Startup Command configurado correctamente
- [ ] Variables de entorno (BD, Redis, JWT) configuradas
- [ ] application-azure.yml en src/main/resources/
- [ ] JAR generado sin errores: `mvn clean package -DskipTests`
- [ ] JAR subido a `/home/site/wwwroot/app.jar`
- [ ] Health check responde: `/api/actuator/health` = UP
- [ ] Swagger funciona: `/api/swagger-ui.html`
- [ ] Base de datos conecta exitosamente
- [ ] Logs limpios (sin errores WARNING o ERROR en startup)
- [ ] Plan App Service = B1 mínimo (B2 recomendado para producción)
- [ ] Always On = true
- [ ] Firewall Azure abierto solo a IPs necesarias

---

## Ejemplo Real: Startup Command Completo para tu Proyecto

```bash
java -Xms512m -Xmx1024m \
  -Dserver.shutdown=graceful \
  -Dspring.profiles.active=azure \
  -Dspring.flyway.enabled=true \
  -jar /home/site/wwwroot/app.jar \
  --server.port=80 \
  --server.servlet.context-path=/api \
  --spring.datasource.url=${SPRING_DATASOURCE_URL} \
  --spring.datasource.username=${SPRING_DATASOURCE_USERNAME} \
  --spring.datasource.password=${SPRING_DATASOURCE_PASSWORD} \
  --management.endpoints.web.exposure.include=health,info,metrics \
  --logging.level.pe.contrataia=DEBUG
```

---

## Dashboard de Monitoreo Recomendado

```bash
# Terminal 1: Logs streaming
az webapp log tail --resource-group contrataia-rg --name contrataia-backend

# Terminal 2: Health checks cada 10 segundos
while true; do
  curl -s https://contrataia-backend.azurewebsites.net/api/actuator/health | jq .
  sleep 10
done

# Terminal 3: Metrics
az monitor metrics list \
  --resource-group contrataia-rg \
  --resource-type microsoftWeb/sites \
  --resource-name contrataia-backend
```

---

## 🎯 Resumen Flujo Completo

```
1. mvn clean package -DskipTests
   ↓
2. Crear App Service (Java 21, Linux)
   ↓
3. Configurar variables de entorno en Settings → Configuration
   ↓
4. Configurar Startup Command en Settings → Configuration → General settings
   ↓
5. Subir JAR vía Zip Deploy o Deployment Center
   ↓
6. Reiniciar App Service
   ↓
7. Verificar: az webapp log tail
   ↓
8. Test: curl https://contrataia-backend.azurewebsites.net/api/actuator/health
   ↓
✅ BACKEND EN PRODUCCIÓN
```

---

**¿Necesitas ayuda con algún paso específico? Responde con la sección del error que estés experimentando.**

