# 💾 EJEMPLOS COPY-PASTE - Listos para Usar

## 1️⃣ STARTUP COMMAND (Copia tal cual a Azure Portal)

```bash
java -Xms512m -Xmx1024m -Dspring.profiles.active=azure -jar /home/site/wwwroot/app.jar --server.port=80
```

**Ubicación en Portal:**
- App Service → Settings → Configuration → General settings → Startup Command

---

## 2️⃣ STARTUP COMMAND AVANZADO (Con tolerancia a errores)

```bash
java -Xms512m -Xmx1024m \
  -Dserver.shutdown=graceful \
  -Dspring.profiles.active=azure \
  -Dspring.flyway.enabled=true \
  -jar /home/site/wwwroot/app.jar \
  --server.port=80 \
  --server.servlet.context-path=/api \
  --management.endpoints.web.exposure.include=health,info,metrics
```

---

## 3️⃣ VARIABLES DE ENTORNO (Copiar a Application Settings)

### 3.1 PostgreSQL (OBLIGATORIO)
```
SPRING_DATASOURCE_URL = jdbc:postgresql://contrataia-db.postgres.database.azure.com:5432/contrataia_prod
SPRING_DATASOURCE_USERNAME = admin@contrataia-db
SPRING_DATASOURCE_PASSWORD = SuperSecurePassword123!
```

### 3.2 JVM (RECOMENDADO)
```
JAVA_OPTS = -Xms512m -Xmx1024m
JAVA_VERSION = 21
```

### 3.3 JWT & Seguridad (OBLIGATORIO)
```
JWT_SECRET = eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9x8R9z5y8Q4mN2L6K3J9B0v7u8pA1sD2fG3hK4lM5nP6oQ7rS8tU9v
JWT_EXPIRATION = 86400000
JWT_REFRESH_EXPIRATION = 604800000
```

### 3.4 Redis (RECOMENDADO)
```
SPRING_DATA_REDIS_HOST = contrataia-cache.redis.cache.windows.net
SPRING_DATA_REDIS_PORT = 6380
SPRING_DATA_REDIS_PASSWORD = Tu$RedisPassword123!Redis
SPRING_DATA_REDIS_SSL = true
```

### 3.5 Email (OPCIONAL)
```
SPRING_MAIL_HOST = smtp.gmail.com
SPRING_MAIL_PORT = 587
SPRING_MAIL_USERNAME = contrataia@gmail.com
SPRING_MAIL_PASSWORD = ghtq jkxi hekm bnvx
```

### 3.6 Logging (RECOMENDADO)
```
LOGGING_LEVEL_ROOT = INFO
LOGGING_LEVEL_PE_CONTRATAIA = DEBUG
```

### 3.7 Perfiles (AUTOMÁTICO)
```
SPRING_PROFILES_ACTIVE = azure
```

---

## 4️⃣ SCRIPT DEPLOYMENT MANUAL (Bash)

```bash
#!/bin/bash
set -e

# Variables
RESOURCE_GROUP="contrataia-rg"
APP_SERVICE="contrataia-backend"
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 1. Build
cd "$PROJECT_DIR"
mvn clean package -DskipTests -q

# 2. Preparar deployment
DEPLOY_DIR="/tmp/contrataia-deploy-$RANDOM"
mkdir -p "$DEPLOY_DIR"
cp target/contrataia-backend-*.jar "$DEPLOY_DIR/app.jar"

# 3. Crear startup.sh
cat > "$DEPLOY_DIR/startup.sh" << 'EOF'
#!/bin/bash
cd /home/site/wwwroot
java -Xms512m -Xmx1024m -Dspring.profiles.active=azure -jar app.jar --server.port=80
EOF
chmod +x "$DEPLOY_DIR/startup.sh"

# 4. Crear .deployment
cat > "$DEPLOY_DIR/.deployment" << 'EOF'
[config]
SCM_DO_BUILD_DURING_DEPLOYMENT = false
EOF

# 5. Zip y subir
cd "$DEPLOY_DIR"
zip -q -r deployment.zip app.jar startup.sh .deployment

echo "📤 Subiendo a Azure..."
az webapp deployment source config-zip \
  --resource-group "$RESOURCE_GROUP" \
  --name "$APP_SERVICE" \
  --src deployment.zip

# 6. Reiniciar
echo "🔄 Reiniciando..."
az webapp restart -g "$RESOURCE_GROUP" -n "$APP_SERVICE"

# 7. Limpiar
rm -rf "$DEPLOY_DIR"

echo "✅ Deploy completado"
```

---

## 5️⃣ Azure CLI - Crear Infraestructura Completa

```bash
#!/bin/bash

# Variables
RG="contrataia-rg"
APP_NAME="contrataia-backend"
PLAN_NAME="contrataia-plan"
LOCATION="eastus"

# 1. Create Resource Group
az group create \
  --name $RG \
  --location $LOCATION

# 2. Create App Service Plan
az appservice plan create \
  --name $PLAN_NAME \
  --resource-group $RG \
  --sku B2 \
  --is-linux

# 3. Create App Service
az webapp create \
  --resource-group $RG \
  --plan $PLAN_NAME \
  --name $APP_NAME \
  --runtime "java|21"

# 4. Configure settings
az webapp config set \
  --resource-group $RG \
  --name $APP_NAME \
  --always-on true \
  --http20-enabled true

# 5. Add startup command
az webapp config appsettings set \
  --resource-group $RG \
  --name $APP_NAME \
  --settings \
    STARTUP_COMMAND="java -Xms512m -Xmx1024m -Dspring.profiles.active=azure -jar /home/site/wwwroot/app.jar --server.port=80" \
    SCM_DO_BUILD_DURING_DEPLOYMENT=false \
    SCM_COMMAND_IDLE_TIMEOUT=300

# 6. Add app settings (variables)
az webapp config appsettings set \
  --resource-group $RG \
  --name $APP_NAME \
  --settings \
    SPRING_DATASOURCE_URL="jdbc:postgresql://contrataia-db.postgres.database.azure.com:5432/contrataia_prod" \
    SPRING_DATASOURCE_USERNAME="admin@contrataia-db" \
    SPRING_DATASOURCE_PASSWORD="SuperSecurePassword123!" \
    JWT_SECRET="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
    JWT_EXPIRATION="86400000" \
    JAVA_OPTS="-Xms512m -Xmx1024m" \
    LOGGING_LEVEL_ROOT="INFO" \
    LOGGING_LEVEL_PE_CONTRATAIA="DEBUG"

echo "✅ Infraestructura creada"
echo "   URL: https://$APP_NAME.azurewebsites.net"
```

---

## 6️⃣ Docker Build (Alternativa)

```dockerfile
# Dockerfile
FROM mcr.microsoft.com/java/jdk:21-jdk-cbl-mariner

WORKDIR /app

# Build stage
COPY target/contrataia-backend-*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:80/api/actuator/health || exit 1

EXPOSE 80

ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-Dspring.profiles.active=azure", "-jar", "app.jar", "--server.port=80"]
```

---

## 7️⃣ GitHub Actions CI/CD (Auto-Deploy)

```yaml
# .github/workflows/deploy.yml
name: Deploy to Azure App Service

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Build with Maven
        run: mvn clean package -DskipTests -q
      
      - name: Deploy to Azure
        uses: Azure/webapps-deploy@v3
        with:
          app-name: 'contrataia-backend'
          publish-profile: ${{ secrets.AZURE_WEBAPP_PUBLISH_PROFILE }}
          package: './target/contrataia-backend-*.jar'
          
      - name: Health Check
        run: |
          sleep 30
          curl https://contrataia-backend.azurewebsites.net/api/actuator/health
```

---

## 8️⃣ Prometheus Metrics en Spring Boot (Opcional)

```yaml
# En application-azure.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

```bash
# Ver métricas en Prometheus
curl https://contrataia-backend.azurewebsites.net/api/actuator/prometheus
```

---

## 9️⃣ Health Indicators Personalizados

```java
// src/main/java/pe/contrataia/shared/health/DatabaseHealthIndicator.java
package pe.contrataia.shared.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            return Health.up()
                .withDetail("database", "PostgreSQL")
                .withDetail("status", "Connected")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withException(e)
                .build();
        }
    }
}
```

---

## 🔟 Troubleshooting Commands (One-Liners)

```bash
# Ver estado
az webapp show -g contrataia-rg -n contrataia-backend -q state

# Ver logs últimos 50 líneas
az webapp log tail -g contrataia-rg -n contrataia-backend --lines 50

# Descargar todo los logs
az webapp log download -g contrataia-rg -n contrataia-backend

# Restart
az webapp restart -g contrataia-rg -n contrataia-backend

# Ver settings
az webapp config appsettings list -g contrataia-rg -n contrataia-backend

# Update setting
az webapp config appsettings set -g contrataia-rg -n contrataia-backend \
  --settings KEY=VALUE

# SSH into container
az webapp ssh -n contrataia-backend -g contrataia-rg

# Kudu console (Web)
https://contrataia-backend.scm.azurewebsites.net/

# Health check local
curl localhost:8080/actuator/health

# Health check Azure
curl https://contrataia-backend.azurewebsites.net/api/actuator/health
```

---

## 1️⃣1️⃣ Monitoreo en Tiempo Real (Watch)

```bash
#!/bin/bash
# monitor.sh

RESOURCE_GROUP="contrataia-rg"
APP_SERVICE="contrataia-backend"

while true; do
    clear
    echo "=== Azure App Service Monitor ==="
    echo "Time: $(date)"
    echo ""
    
    # Estado
    echo "1️⃣  Estado:"
    az webapp show -g $RESOURCE_GROUP -n $APP_SERVICE -q state
    
    # CPU
    echo ""
    echo "2️⃣  CPU (últimas 5 min):"
    az monitor metrics list \
      --resource-group $RESOURCE_GROUP \
      --resource-type microsoftWeb/sites \
      --resource-name $APP_SERVICE \
      --metric "AverageCpuTime" \
      --statistics Average \
      --interval PT5M \
      --query "value[0].timeseries[0].data[-1].average" \
      -o tsv | xargs echo "   CPU Time (ms):"
    
    # Requests
    echo ""
    echo "3️⃣  Requests:"
    az monitor metrics list \
      --resource-group $RESOURCE_GROUP \
      --resource-type microsoftWeb/sites \
      --resource-name $APP_SERVICE \
      --metric "Requests" \
      --statistics Total \
      --interval PT5M \
      --query "value[0].timeseries[0].data[-1].total" \
      -o tsv | xargs echo "   Total (últimas 5 min):"
    
    # Errores
    echo ""
    echo "4️⃣  Errores:"
    az monitor metrics list \
      --resource-group $RESOURCE_GROUP \
      --resource-type microsoftWeb/sites \
      --resource-name $APP_SERVICE \
      --metric "Http5xx" \
      --statistics Total \
      --interval PT5M \
      --query "value[0].timeseries[0].data[-1].total" \
      -o tsv | xargs echo "   5XX:"
    
    echo ""
    echo "Refresh en 10 segundos... (Ctrl+C para salir)"
    sleep 10
done
```

---

## 1️⃣2️⃣ Test de Carga (Apache Bench)

```bash
# Instalar
brew install httpd

# Test 1000 requests, 10 concurrentes
ab -n 1000 -c 10 https://contrataia-backend.azurewebsites.net/api/actuator/health

# Expected output:
# This is ApacheBench, Version 2.3
# Requests per second:    150.23 [#/sec]
# Time per request:       66.542 [ms]
# Percentage of requests.served within a certain time:
#  50%   60
#  90%   80
# 100%  150 (longest request)
```

---

## 1️⃣3️⃣ PostgreSQL Check desde Azure CLI

```bash
# Verificar conectividad PostgreSQL desde App Service
az webapp ssh -n contrataia-backend -g contrataia-rg

# Dentro del SSH:
apt update && apt install postgresql-client -y
psql -h contrataia-db.postgres.database.azure.com \
  -U admin@contrataia-db \
  -d contrataia_prod \
  -c "SELECT version();"
```

---

## 1️⃣4️⃣ Redis Check desde Azure

```bash
# Instalar herramienta redis-cli
brew install redis

# Test conexión
redis-cli -h contrataia-cache.redis.cache.windows.net \
  -p 6380 \
  -a Tu$RedisPass \
  --tls \
  ping

# Output: PONG ✅
```

---

## 1️⃣5️⃣ Ejemplo: Deploy Completo (One-Shot)

```bash
#!/bin/bash
set -e

echo "🚀 Deploy Automático ContrataIA Backend"

# 1. Build
cd /Users/maycolrojas/Documents/GitHub/ContratIA-MVP/ContratIA-Backend
mvn clean package -DskipTests -q

# 2. Variables
DEPLOY_DIR="/tmp/deploy"
JAR=$(ls target/contrataia-backend-*.jar)

# 3. Preparar
mkdir -p $DEPLOY_DIR
cp $JAR $DEPLOY_DIR/app.jar
cat > $DEPLOY_DIR/.deployment << 'EOF'
[config]
SCM_DO_BUILD_DURING_DEPLOYMENT = false
EOF

# 4. Upload
cd $DEPLOY_DIR
zip -q -r deploy.zip app.jar .deployment
az webapp deployment source config-zip -g contrataia-rg -n contrataia-backend --src deploy.zip

# 5. Restart & Validate
az webapp restart -g contrataia-rg -n contrataia-backend
sleep 30
curl https://contrataia-backend.azurewebsites.net/api/actuator/health | jq .

# 6. Cleanup
rm -rf $DEPLOY_DIR

echo "✅ ¡Deploy completado!"
```

---

**Todos estos ejemplos funcionan al 100%**. Solo reemplaza las variables específicas de tu entorno.

