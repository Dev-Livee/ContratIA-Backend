# 🚀 QUICK START - Deploy a Azure en 5 Minutos

## 🎯 Objetivo
Deployar tu Spring Boot en Azure App Service SIN la página "Hey, Java developers!"

---

## 📋 Requisitos (2 min)

```bash
# 1. Verificar Azure CLI
az --version

# Si no está instalado:
brew install azure-cli

# 2. Loguearset en Azure
az login

# 3. Verificar Maven
mvn -version

# 4. Verificar Java 21
java -version
```

---

## 🏃 OPCIÓN A: Deployment Completamente Automático (3 minutos)

```bash
# 1. Ir a tu proyecto
cd /Users/maycolrojas/Documents/GitHub/ContratIA-MVP/ContratIA-Backend

# 2. Ejecutar script automático
bash scripts/deploy-to-azure.sh

# ✅ ¡LISTO! El script hace TODO automáticamente
```

**El script:**
- ✅ Compila con Maven
- ✅ Genera el JAR
- ✅ Sube a Azure
- ✅ Reinicia la app
- ✅ Valida que funciona

---

## 🏃 OPCIÓN B: Deployment Paso a Paso Guiado (5 minutos)

```bash
# Ejecutar instalador interactivo
bash scripts/setup-interactive.sh

# Sigue las instrucciones en pantalla
# El script te guía a través de:
# 1. Verificar/crear Resource Group
# 2. Verificar/crear App Service
# 3. Configurar Java 21 en Portal (te dice qué hacer)
# 4. Configurar Startup Command (te da el texto listo)
# 5. Configurar variables de entorno (te lista todas)
# 6. Subir JAR
# 7. Validar que funciona
```

---

## 🏃 OPCIÓN C: Manual Total (control completo)

### Paso 1: Build Local
```bash
cd /Users/maycolrojas/Documents/GitHub/ContratIA-MVP/ContratIA-Backend
mvn clean package -DskipTests
```
✅ Genera: `target/contrataia-backend-1.0.0-SNAPSHOT.jar`

### Paso 2: Crear App Service (si no existe)
```bash
# Variables
RG="contrataia-rg"
APP="contrataia-backend"
PLAN="contrataia-plan"

# Crear Resource Group
az group create -n $RG -l eastus

# Crear App Service Plan
az appservice plan create -n $PLAN -g $RG --sku B1 --is-linux

# Crear App Service
az webapp create -g $RG -p $PLAN -n $APP --runtime "java|21"
```

### Paso 3: Subir JAR
```bash
# Crear estructura
mkdir -p deploy
cp target/contrataia-backend-*.jar deploy/app.jar

# Crear startup script
cat > deploy/startup.sh << 'EOF'
#!/bin/bash
cd /home/site/wwwroot
java -Xms512m -Xmx1024m -Dspring.profiles.active=azure -jar app.jar --server.port=80
EOF
chmod +x deploy/startup.sh

# Crear deployment config
cat > deploy/.deployment << 'EOF'
[config]
SCM_DO_BUILD_DURING_DEPLOYMENT = false
EOF

# Empaquetar
cd deploy
zip -r deployment.zip app.jar startup.sh .deployment

# Subir
az webapp deployment source config-zip -g contrataia-rg -n contrataia-backend --src deployment.zip

# Reiniciar
az webapp restart -g contrataia-rg -n contrataia-backend
```

### Paso 4: Configurar en Azure Portal
**App Service → Settings → Configuration → General settings**
```
Startup Command:
java -Xms512m -Xmx1024m -Dspring.profiles.active=azure -jar /home/site/wwwroot/app.jar --server.port=80
```

**App Service → Settings → Configuration → Application settings**
```
SPRING_DATASOURCE_URL = jdbc:postgresql://TU-SERVER.postgres.database.azure.com:5432/contrataia_db
SPRING_DATASOURCE_USERNAME = admin@TU-SERVER
SPRING_DATASOURCE_PASSWORD = Tu$Password123!
JWT_SECRET = tu-clave-super-larga-minimo-32-caracteres
JAVA_OPTS = -Xms512m -Xmx1024m
```

### Paso 5: Validar
```bash
# Esperar 30 segundos
sleep 30

# Health check
curl https://contrataia-backend.azurewebsites.net/api/actuator/health

# Debe responder: {"status":"UP",...}
```

---

## 🔍 Verificación Rápida

```bash
# ✅ Backend está UP
curl https://contrataia-backend.azurewebsites.net/api/actuator/health

# ✅ Swagger accesible
curl -s https://contrataia-backend.azurewebsites.net/api/swagger-ui.html | head -20

# ✅ Ver logs
az webapp log tail -g contrataia-rg -n contrataia-backend

# ✅ Ver estado
az webapp show -g contrataia-rg -n contrataia-backend -q state
```

---

## ⚠️ Si ves "Hey, Java developers!"

**CAUSAS Y SOLUCIONES:**

### 1️⃣ Startup Command no configurado
```bash
# En Portal → Settings → Configuration → General settings
# Debe estar:
java -Xms512m -Xmx1024m -Dspring.profiles.active=azure -jar /home/site/wwwroot/app.jar --server.port=80
```

### 2️⃣ JAR no llegó a Azure
```bash
# Verificar en Kudu Console:
https://contrataia-backend.scm.azurewebsites.net/
# Navega a: /home/site/wwwroot/
# Debe haber un archivo: app.jar

# Si no está:
cd deploy
zip -r deployment.zip app.jar startup.sh .deployment
az webapp deployment source config-zip -g contrataia-rg -n contrataia-backend --src deployment.zip
```

### 3️⃣ Spring Boot tardó en iniciar
```bash
# Aumentar timeout en Azure Portal:
# Settings → Configuration → Application settings
# + New setting:
# Name: SCM_COMMAND_IDLE_TIMEOUT
# Value: 300

# O en CLI:
az webapp config appsettings set -g contrataia-rg -n contrataia-backend \
  --settings SCM_COMMAND_IDLE_TIMEOUT=300
```

### 4️⃣ Reiniciar y esperar
```bash
az webapp restart -g contrataia-rg -n contrataia-backend

# Esperar 60 segundos
sleep 60

# Intentar de nuevo
curl https://contrataia-backend.azurewebsites.net/api/actuator/health
```

---

## 📊 Monitoreo en Tiempo Real

```bash
# Terminal 1: Logs streaming (muy útil)
az webapp log tail -g contrataia-rg -n contrataia-backend

# Terminal 2: Health checks cada 10 segundos
while true; do
  curl -s https://contrataia-backend.azurewebsites.net/api/actuator/health | jq .
  sleep 10
done

# Terminal 3: Dashboard interactivo
bash scripts/monitor-deployment.sh
```

---

## 📝 Variables de Entorno - Valores Reales

Necesitas configurar estas ANTES de hacer deploy:

```yaml
# Database PostgreSQL (OBLIGATORIO)
SPRING_DATASOURCE_URL: jdbc:postgresql://contrataia-db.postgres.database.azure.com:5432/contrataia_prod
SPRING_DATASOURCE_USERNAME: admin@contrataia-db
SPRING_DATASOURCE_PASSWORD: TuPasswordSeguro123!

# JWT (OBLIGATORIO)
JWT_SECRET: xyz123abc456def789ghi012jkl345mno678pqr901stu234vwx567yz
JWT_EXPIRATION: 86400000

# Redis (OPCIONAL pero recomendado)
SPRING_DATA_REDIS_HOST: contrataia.redis.cache.windows.net
SPRING_DATA_REDIS_PORT: 6380
SPRING_DATA_REDIS_PASSWORD: TuRedisPassword123!
SPRING_DATA_REDIS_SSL: true

# Mail (OPCIONAL)
SPRING_MAIL_HOST: smtp.gmail.com
SPRING_MAIL_PORT: 587
SPRING_MAIL_USERNAME: tu-email@gmail.com
SPRING_MAIL_PASSWORD: tu-app-password

# JVM
JAVA_OPTS: -Xms512m -Xmx1024m

# Logging
LOGGING_LEVEL_ROOT: INFO
LOGGING_LEVEL_PE_CONTRATAIA: DEBUG
```

---

## 📚 Otros Documentos Incluidos

| Documento | Propósito |
|-----------|-----------|
| `AZURE_DEPLOYMENT_GUIDE.md` | Guía completa y detallada |
| `CHECKLIST_DEPLOYMENT.md` | Checklist visual paso a paso |
| `scripts/deploy-to-azure.sh` | Deployment totalmente automático |
| `scripts/setup-interactive.sh` | Instalador guiado interactivo |
| `scripts/validate-deployment.sh` | Validación y diagnóstico |
| `scripts/configure-azure-env.sh` | Configurar variables de entorno |
| `scripts/monitor-deployment.sh` | Dashboard de monitoreo en tiempo real |
| `src/main/resources/application-azure.yml` | Configuración Spring para Azure |

---

## ✅ Checklist Final Pre-Producción

```bash
# Antes de ir a PRODUCCIÓN:
- [ ] Java version = 21 en App Service
- [ ] Startup Command configurado
- [ ] Todas las variables de entorno configuradas
- [ ] Database PostgreSQL conecta exitosamente
- [ ] Health check responde: /api/actuator/health = UP
- [ ] Swagger funciona: /api/swagger-ui.html
- [ ] Logs limpios (sin ERROR críticos)
- [ ] Plan App Service = B2 o superior
- [ ] Always On = enabled
- [ ] HTTPS funciona (Azure maneja automáticamente)
- [ ] Firewall Azure abierto correctamente
```

---

## 🚨 SOS - Troubleshooting Rápido

| Síntoma | Solución |
|---------|----------|
| "Hey Java developers!" | Configura Startup Command + reinicia |
| 502 Bad Gateway | Aumenta `SCM_COMMAND_IDLE_TIMEOUT` a 300 |
| 503 Bad Service | Aumenta memoria o cambia a B2 |
| Database connection refused | Verifica credenciales y firewall PostgreSQL |
| Requests lentos | Aumenta App Service Plan a B2 |
| OutOfMemory errors | Aumenta `-Xmx` en JAVA_OPTS |

---

## 🎯 Próximos Pasos

1. **Hoy:** Deploy con `bash scripts/deploy-to-azure.sh`
2. **Mañana:** Configurar Application Insights y alertas
3. **Próxima semana:** Setup CI/CD automático con GitHub Actions

---

## 📞 Stack de Datos Reales

```
Tu Backend: https://contrataia-backend.azurewebsites.net/api
Health:     https://contrataia-backend.azurewebsites.net/api/actuator/health
Swagger:    https://contrataia-backend.azurewebsites.net/api/swagger-ui.html
Portal:     https://portal.azure.com/ → App Services → contrataia-backend
```

---

**¿Listo?**

```bash
bash scripts/deploy-to-azure.sh
```

✨ Tu backend estará funcionando en **3 minutos**.

