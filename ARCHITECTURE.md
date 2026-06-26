# 🏗️ Arquitectura: Spring Boot en Azure App Service

## 📊 Diagrama de Deployment

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          INTERNET (USUARIOS)                                │
│                                  ↓                                           │
│                      ┌───────────────────────┐                              │
│                      │   Azure Traffic Mgr   │ (Balanceo global - opcional) │
│                      │   HTTPS/SSL           │                              │
│                      └───────────┬───────────┘                              │
│                                  ↓                                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                        AZURE APP SERVICE (Linux)                            │
│                                                                             │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                   ☁️ App Service Instance (B1/B2)                   │  │
│  │                                                                      │  │
│  │  ┌────────────────────────────────────────────────────────────────┐ │  │
│  │  │              Java Runtime Engine (Java 21)                     │ │  │
│  │  │                        ↓                                       │ │  │
│  │  │  ┌────────────────────────────────────────────────────────┐   │ │  │
│  │  │  │       Tomcat 10.1 (Embedded)                          │   │ │  │
│  │  │  │       Listening on: 0.0.0.0:80                        │   │ │  │
│  │  │  │       ↓                                               │   │ │  │
│  │  │  │   ┌──────────────────────────────────────────────┐   │   │ │  │
│  │  │  │   │   🍃 Spring Boot 3.3.5                       │   │   │ │  │
│  │  │  │   │   ContrataIA Backend                         │   │   │ │  │
│  │  │  │   │                                               │   │   │ │  │
│  │  │  │   │   📍 Controllers:                            │   │   │ │  │
│  │  │  │   │      /api/auth                               │   │   │ │  │
│  │  │  │   │      /api/empresa                             │   │   │ │  │
│  │  │  │   │      /api/proveedor                           │   │   │ │  │
│  │  │  │   │      /api/proyecto                            │   │   │ │  │
│  │  │  │   │      /api/seguimiento                         │   │   │ │  │
│  │  │  │   │      /api/actuator/health ⭐                │   │   │ │  │
│  │  │  │   │                                               │   │   │ │  │
│  │  │  └───┬───────────────────────────────────────────────┘   │   │ │  │
│  │  │      │                                                    │   │ │  │
│  │  │  ┌───┴──────────────────────────────────────────────────┐ │   │ │  │
│  │  │  │ Spring Data JPA + Hibernate                         │ │   │ │  │
│  │  │  │ Spring Data Redis                                   │ │   │ │  │
│  │  │  │ Spring Security + JWT                              │ │   │ │  │
│  │  │  └───┬──────────────────┬──────────────────┬──────────┘ │   │ │  │
│  │  │      │                  │                  │            │   │ │  │
│  │  └──────┼──────────────────┼──────────────────┼────────────┘   │ │  │
│  │         │                  │                  │                │ │  │
└──┼─────────┼──────────────────┼──────────────────┼────────────────┼─┘  │
   │         │                  │                  │                │     │
   │         ↓                  ↓                  ↓                ↓     │
   │  ┌────────────────┐ ┌──────────────┐ ┌────────────────┐ ┌─────────┐ │
   │  │ PostgreSQL DB  │ │ Azure Redis  │ │ Azure Mail     │ │ Storage │ │
   │  │ (Azure DB)     │ │ Cache        │ │ (SMTP)         │ │ Account │ │
   │  │                │ │              │ │                │ │ (Blobs) │ │
   │  │ - Connection   │ │ - Cache      │ │ - Emails       │ │ - Docs  │ │
   │  │   Pooling      │ │ - Sessions   │ │ - Notif        │ │ - Files │ │
   │  │ - SSL/TLS      │ │ - SSL: true  │ │                │ │         │ │
   │  │ - Flyway       │ │ - Port: 6380 │ │ - Gmail SMTP   │ │         │ │
   │  │   Migrations   │ │              │ │                │ │         │ │
   │  └────────────────┘ └──────────────┘ └────────────────┘ └─────────┘ │
   │                                                                       │
   │  CONFIGURACIÓN EN AZURE PORTAL:                                      │
   │  ┌─────────────────────────────────────────────────────────────────┐ │
   │  │ Settings → Configuration                                       │ │
   │  │   ↳ General settings:                                         │ │
   │  │     - Java version: 21                                        │ │
   │  │     - Web server container: Tomcat 10.1                       │ │
   │  │     - Startup Command: ⭐ (CRÍTICO)                        │ │
   │  │                                                             │ │
   │  │   ↳ Application settings (Environment Variables):           │ │
   │  │     - SPRING_DATASOURCE_URL                                │ │
   │  │     - SPRING_DATASOURCE_USERNAME                           │ │
   │  │     - SPRING_DATASOURCE_PASSWORD                           │ │
   │  │     - JWT_SECRET                                           │ │
   │  │     - JAVA_OPTS: -Xms512m -Xmx1024m                        │ │
   │  │     - etc...                                               │ │
   │  └─────────────────────────────────────────────────────────────────┘ │
   │                                                                       │
   └───────────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Flujo del Deployment

```
1. DESARROLLO (Local)
   ├─ Escribir código Java/Spring Boot
   ├─ Compilar: mvn clean package -DskipTests
   └─ Genera: target/contrataia-backend-1.0.0-SNAPSHOT.jar

2. PREPARACIÓN (Local)
   ├─ Copiar JAR a carpeta deploy/
   ├─ Crear startup.sh con comando de arranque
   ├─ Crear .deployment file
   └─ Empaquetar en zip: deployment.zip

3. INFRAESTRUCTURA (Azure Portal o CLI)
   ├─ Crear Resource Group
   ├─ Crear App Service (Linux, Java 21)
   ├─ Crear plan App Service (B1/B2)
   ├─ Crear bases de datos (PostgreSQL, Redis)
   └─ Configurar Network/Firewall

4. CONFIGURACIÓN (Azure Portal)
   ├─ Runtime stack: Java 21
   ├─ Web server: Tomcat 10.1
   ├─ Startup Command: (EL CRÍTICO)
   └─ Environment variables (15-20 variables)

5. DEPLOYMENT (Azure)
   ├─ Subir ZIP vía Zip Deploy
   ├─ Extraer archivos en /home/site/wwwroot/
   ├─ Ejecutar startup.sh
   ├─ Java detecta app.jar
   └─ Log: "Started ContrataIAApplication..."

6. ROUTING (Internet)
   ├─ Request HTTPS → Azure Front Door/Traffic Manager
   ├─ Redirige HTTP → HTTPS
   ├─ Llega a App Service puerto 80 (interno)
   ├─ Tomcat routing:
   │  └─ /api/* → Spring Controllers
   └─ Response JSON/HTML → Cliente

7. VALIDACIÓN (Post-Deploy)
   ├─ curl /api/actuator/health
   ├─ curl /api/swagger-ui.html
   ├─ curl /api/login (test endpoint)
   └─ az webapp log tail (ver logs)
```

---

## 📦 Capas de la Aplicación

```
CAPA 1: PRESENTACIÓN
├─ Swagger UI: /api/swagger-ui.html
├─ REST Endpoints: /api/*
└─ WebFlux para async (WebClient)

CAPA 2: CONTROLADORES
├─ @RestController
├─ @RequestMapping("/api/*")
└─ JWT Security

CAPA 3: SERVICIOS
├─ Lógica de negocio
├─ Transacciones
└─ Integración con BD/Cache

CAPA 4: PERSISTENCIA
├─ JPA Repositories
├─ Hibernate ORM
├─ Flyway Migrations
└─ Connection Pooling (Hikari)

CAPA 5: DATOS
├─ PostgreSQL (tablas)
├─ Redis (cache/sessions)
└─ Azure Storage (archivos)
```

---

## 🔐 Seguridad - Layers

```
CAPA 1: CLOUD
├─ Azure Front Door (DDoS Protection)
├─ HTTPS/TLS (automático)
├─ Network Security Group (firewall)
└─ Private Endpoints (opcional)

CAPA 2: APP SERVICE
├─ Runtime aislado por container
├─ Sandbox execution model
├─ No acceso directo a filesystem
└─ Logs auditados

CAPA 3: APLICACIÓN
├─ Spring Security
├─ JWT tokens
├─ CORS configurado
├─ CSRF protection
└─ SQL Injection prevention (Hibernate)

CAPA 4: BASE DE DATOS
├─ SSL/TLS connection required
├─ Usuario con permisos mínimos
├─ IP whitelist firewall
└─ Backups automáticos
```

---

## 💾 Resources Necesarios

| Componente | SKU | Pricing Estimado |
|-----------|-----|-----------------|
| App Service Plan | B1 | $10-15/mes |
| PostgreSQL | Standard_B1s | $15-30/mes |
| Redis Cache | Basic C0 | $15-30/mes |
| Storage Account | Standard LRS | $1-5/mes |
| **TOTAL** | | **$41-80/mes** |

**Recomendación para Producción:**
- Cambiar B1 → B2 (mejor performance)
- Agregar Application Insights ($10/mes)
- Backups automáticos de BD
- **TOTAL: ~$100-120/mes**

---

## 🔍 Monitoreo & Logging

```
┌─────────────────────────────────────────────────────────────┐
│              Application Insights (opcional)               │
│  ├─ Exceptions tracking                                    │
│  ├─ Performance metrics                                    │
│  ├─ Dependency tracking (BD, Redis)                        │
│  ├─ Log Analytics                                          │
│  └─ Alertas automáticas                                    │
└─────────────────────────────────────────────────────────────┘
                          ↑
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot Actuator                          │
│  ├─ /actuator/health                                       │
│  ├─ /actuator/metrics                                      │
│  ├─ /actuator/prometheus                                   │
│  ├─ /actuator/logs                                         │
│  └─ Custom health indicators (DB, Redis)                   │
└─────────────────────────────────────────────────────────────┘
                          ↑
┌─────────────────────────────────────────────────────────────┐
│              Logs en Azure                                 │
│  ├─ Log stream en tiempo real                              │
│  ├─ Archivos en /home/LogFiles/                            │
│  ├─ Storage account backup                                 │
│  └─ Application Insights ingestion                         │
└─────────────────────────────────────────────────────────────┘
                          ↑
┌─────────────────────────────────────────────────────────────┐
│              aplicación (Spring Boot)                      │
│  ├─ Logs a stdout/stderr (Tomcat)                        │
│  ├─ app/default_docker.log                                │
│  └─ Structured logging (JSON - opcional)                  │
└─────────────────────────────────────────────────────────────┘
```

---

## 🚀 Startup Sequence

```
TIME  EVENT                                   STATUS
────  ───────────────────────────────────────────────────────
 0s   Azure ejecuta: bash startup.sh          ⏳
 1s   Carga JVM con -Xms512m -Xmx1024m       ⏳
 3s   Spring Boot inicializa contexto        ⏳
 5s   Hibernate conecta a PostgreSQL          ⏳ (Flyway migrations)
 8s   Redis se inicializa (cache)            ⏳
10s   Spring Security se configura            ⏳
12s   Componentes async se registran         ⏳
15s   Swagger/OpenAPI se genera              ⏳
18s   "Started ContrataIAApplication..."     ✅ READY
20s   Health check: /api/actuator/health     ✅ UP
```

**Total startup: 18-25 segundos**

⚠️ Si tarda más de 60 seg → aumentar `SCM_COMMAND_IDLE_TIMEOUT`

---

## 📈 Escalabilidad

```
ACTUAL (B1):
├─ CPU: 1 core
├─ RAM: 1.75 GB
├─ Concurrencia: ~100 requests/min
└─ Costo: $13/mes

ESCALADO (B2):
├─ CPU: 2 cores
├─ RAM: 3.5 GB
├─ Concurrencia: ~300 requests/min
└─ Costo: $30/mes

ESCALADO (B3):
├─ CPU: 4 cores
├─ RAM: 7 GB
├─ Concurrencia: ~600 requests/min
└─ Costo: $60/mes

AUTO-SCALING (Premium V2):
├─ Escala automáticamente
├─ Multi-instance (2-10)
├─ Load balancer integrado
└─ Costo: Variable ($200+/mes)
```

---

## ✅ Checklist Técnico Pre-Producción

```bash
Infraestructura:
  ☐ App Service Plan ≥ B2
  ☐ PostgreSQL ≥ Standard_B1s
  ☐ Redis ≥ Basic (C0)
  ☐ Storage account para backups
  ☐ Firewall configurado

Configuración:
  ☐ Java 21 verificado
  ☐ Startup Command correcto
  ☐ Environment vars completas
  ☐ application-azure.yml presente
  ☐ SSL/HTTPS habilitado (automático)

Aplicación:
  ☐ Build sin warnings
  ☐ JAR tamaño < 200MB
  ☐ Health check: UP
  ☐ Database: UP
  ☐ Redis: UP
  ☐ Logs: INFO level

Seguridad:
  ☐ JWT_SECRET único y largo (64+ chars)
  ☐ Credenciales BD seguras (contraseña fuerte)
  ☐ CORS configurado
  ☐ SQL Injection prevention
  ☐ HTTPS enforcement
  ☐ No secrets en logs
  ☐ No debug endpoints en prod

Monitoring:
  ☐ Application Insights activado
  ☐ Alertas configuradas
  ☐ Backups automáticos activos
  ☐ Log retention policy definida
```

---

## 📞 SLAs y Performance Targets

```
DISPONIBILIDAD:
├─ Target: 99.5% (máximo 3.6 horas downtime/mes)
├─ Monitoreado por: Azure App Service Health Probe
└─ Alertas: Si está DOWN > 5 minutos

PERFORMANCE:
├─ Response Time: < 500ms (p95)
├─ Throughput: 100+ requests/sec
├─ CPU Usage: < 80%
├─ Memory: < 85%
└─ Error Rate: < 0.1%

BACKUP:
├─ Automático: Diario
├─ Retención: 7 días
├─ RPO (Recovery Point): 24 horas
└─ RTO (Recovery Time): < 1 hora
```

---

## 🎯 Próximos Pasos Avanzados

```
1. CI/CD Setup
   └─ GitHub Actions → Auto-deploy on push

2. Infrastructure as Code
   └─ Azure Resource Manager Templates (ARM)

3. Containerización
   └─ Docker → Azure Container Registry

4. API Gateway
   └─ Azure API Management (rate limiting, policies)

5. Monitoring Avanzado
   └─ Application Insights + Log Analytics

6. Security
   └─ Key Vault, Managed Identity, VNet
```

---

## 📎 Archivos Creados

```
ContratIA-Backend/
├─ AZURE_DEPLOYMENT_GUIDE.md        ← Guía completa (REFERENCIA)
├─ QUICK_START_AZURE.md             ← Inicio rápido (EMPEZAR AQUÍ)
├─ CHECKLIST_DEPLOYMENT.md          ← Checklist visual (VERIFICAR)
├─ ARCHITECTURE.md                  ← Este archivo
├─ scripts/
│  ├─ deploy-to-azure.sh           ← Deployment automático ⭐
│  ├─ setup-interactive.sh          ← Instalador guiado
│  ├─ validate-deployment.sh        ← Validación post-deploy
│  ├─ configure-azure-env.sh        ← Config de variables
│  └─ monitor-deployment.sh         ← Dashboard en tiempo real
└─ src/main/resources/
   └─ application-azure.yml          ← Config Spring para Azure
```

---

**¿Listo para deployear? Comienza aquí:** [QUICK_START_AZURE.md](./QUICK_START_AZURE.md)

