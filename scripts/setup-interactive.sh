#!/bin/bash

# ===================================================================
# Script de Configuración Rápida en Azure Portal
# Ejecuta esto para obtener instrucciones paso a paso
# ===================================================================

clear

cat << 'EOF'
╔══════════════════════════════════════════════════════════════════════════════════╗
║                 ☁️ DEPLOYMENT SPRING BOOT A AZURE APP SERVICE ☁️                 ║
║                          Guía Interactiva Paso a Paso                           ║
╚══════════════════════════════════════════════════════════════════════════════════╝

Este script te guiará a través de la configuración en Azure Portal.

REQUERIMIENTOS ANTES DE EMPEZAR:
  ✅ Cuenta de Azure con suscripción activa
  ✅ Azure CLI instalado (brew install azure-cli)
  ✅ Estar logueado en Azure CLI (az login)
  ✅ JAR compilado localmente

ESTIMADO: 15-20 minutos total

═══════════════════════════════════════════════════════════════════════════════════
EOF

read -p "¿Deseas continuar? (s/n): " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Ss]$ ]]; then
    echo "Abortado."
    exit 1
fi

# Ask for resource group and app service name
echo ""
echo "═══════════════════════════════════════════════════════════════════════════════════"
echo "1️⃣  INFORMACIÓN DE AZURE"
echo "═══════════════════════════════════════════════════════════════════════════════════"
echo ""

read -p "Nombre del Resource Group (default: contrataia-rg): " RG
RESOURCE_GROUP="${RG:=contrataia-rg}"

read -p "Nombre del App Service (default: contrataia-backend): " APP
APP_SERVICE="${APP:=contrataia-backend}"

read -p "Región Azure (default: eastus): " REGION
AZURE_REGION="${REGION:=eastus}"

echo ""
echo "📋 Resumen:"
echo "   Resource Group: $RESOURCE_GROUP"
echo "   App Service: $APP_SERVICE"
echo "   Región: $AZURE_REGION"
echo ""

# Check if resource group exists
echo "⏳ Verificando que el Resource Group existe..."
if az group exists -n "$RESOURCE_GROUP" | grep -q true; then
    echo "✅ Resource Group existe"
else
    echo "❌ Resource Group NO existe"
    read -p "¿Crear Resource Group? (s/n): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Ss]$ ]]; then
        az group create --name "$RESOURCE_GROUP" --location "$AZURE_REGION"
        echo "✅ Resource Group creado"
    else
        echo "Abortado. Crea primero el Resource Group en Azure Portal."
        exit 1
    fi
fi

# Check if app service exists
echo ""
echo "⏳ Verificando que el App Service existe..."
APP_EXISTS=$(az webapp show \
  --resource-group "$RESOURCE_GROUP" \
  --name "$APP_SERVICE" \
  2>/dev/null || echo "")

if [ -n "$APP_EXISTS" ]; then
    echo "✅ App Service existe"
else
    echo "❌ App Service NO existe"
    echo ""
    echo "Debes crear el App Service primero manualmente o con this comando:"
    echo ""
    echo "  PLAN_NAME=\"${APP_SERVICE}-plan\""
    echo "  az appservice plan create \\"
    echo "    --name \$PLAN_NAME \\"
    echo "    --resource-group $RESOURCE_GROUP \\"
    echo "    --sku B1 \\"
    echo "    --is-linux"
    echo ""
    echo "  az webapp create \\"
    echo "    --resource-group $RESOURCE_GROUP \\"
    echo "    --plan \$PLAN_NAME \\"
    echo "    --name $APP_SERVICE \\"
    echo "    --runtime \"java|21\""
    echo ""
    read -p "¿Crear App Service ahora? (s/n): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Ss]$ ]]; then
        PLAN_NAME="${APP_SERVICE}-plan"
        echo "🔧 Creando App Service Plan..."
        az appservice plan create \
          --name "$PLAN_NAME" \
          --resource-group "$RESOURCE_GROUP" \
          --sku B1 \
          --is-linux \
          --output none

        echo "🔧 Creando App Service..."
        az webapp create \
          --resource-group "$RESOURCE_GROUP" \
          --plan "$PLAN_NAME" \
          --name "$APP_SERVICE" \
          --runtime "java|21" \
          --output none

        echo "✅ App Service creado"
    else
        echo "Por favor crea el App Service primero."
        exit 1
    fi
fi

# Configuration
echo ""
echo "═══════════════════════════════════════════════════════════════════════════════════"
echo "2️⃣  CONFIGURACIÓN DEL RUNTIME"
echo "═══════════════════════════════════════════════════════════════════════════════════"
echo ""

echo "🔧 Verificando Java version..."
JAVA_VERS=$(az webapp config show \
  --resource-group "$RESOURCE_GROUP" \
  --name "$APP_SERVICE" \
  --query linuxFxVersion -o tsv 2>/dev/null)

echo "   Versión actual: $JAVA_VERS"

echo ""
echo "ℹ️  TO CONFIGURE IN AZURE PORTAL:"
echo "   1. Go to: App Service → Settings → Configuration"
echo "   2. Click: General settings"
echo "   3. Set:"
echo "      - Java version: 21"
echo "      - Java web server container: Tomcat 10.1"
echo ""
read -p "Presiona Enter cuando hayas completado esta configuración en Portal..."

# Startup Command
echo ""
echo "═══════════════════════════════════════════════════════════════════════════════════"
echo "3️⃣  STARTUP COMMAND"
echo "═══════════════════════════════════════════════════════════════════════════════════"
echo ""

STARTUP_CMD="java -Xms512m -Xmx1024m -Dspring.profiles.active=azure -jar /home/site/wwwroot/app.jar --server.port=80"

echo "ℹ️  CONFIGURAR EN AZURE PORTAL:"
echo "   1. Go to: App Service → Settings → Configuration"
echo "   2. Click: General settings"
echo "   3. Find: Startup Command"
echo "   4. Paste:"
echo ""
echo "      $STARTUP_CMD"
echo ""
echo "   5. Click: Save"
echo ""
read -p "Presiona Enter cuando hayas completado..."

# Application Settings
echo ""
echo "═══════════════════════════════════════════════════════════════════════════════════"
echo "4️⃣  VARIABLES DE ENTORNO"
echo "═══════════════════════════════════════════════════════════════════════════════════"
echo ""

echo "ℹ️  CONFIGURAR EN AZURE PORTAL:"
echo "   1. Go to: App Service → Settings → Configuration"
echo "   2. Click: Application settings"
echo ""

# Database config
echo "   DATABASE:"
echo "   ─────────────────────────────────────────────────────"
echo "   Click '+ New application setting' para cada una:"
echo ""
echo "   Name: SPRING_DATASOURCE_URL"
echo "   Value: jdbc:postgresql://TU-SERVER.postgres.database.azure.com:5432/contrataia_db"
echo ""
echo "   Name: SPRING_DATASOURCE_USERNAME"
echo "   Value: admin@TU-SERVER"
echo ""
echo "   Name: SPRING_DATASOURCE_PASSWORD"
echo "   Value: Tu$SuperSecurePassword123!"
echo ""

# JVM config
echo "   JVM:"
echo "   ─────────────────────────────────────────────────────"
echo "   Name: JAVA_OPTS"
echo "   Value: -Xms512m -Xmx1024m"
echo ""

# JWT config
echo "   JWT & SECURITY:"
echo "   ─────────────────────────────────────────────────────"
echo "   Name: JWT_SECRET"
echo "   Value: tu-super-larga-cadena-aleatoria-min-32-caracteres"
echo ""
echo "   Name: JWT_EXPIRATION"
echo "   Value: 86400000"
echo ""

# Logging
echo "   LOGGING:"
echo "   ─────────────────────────────────────────────────────"
echo "   Name: LOGGING_LEVEL_ROOT"
echo "   Value: INFO"
echo ""
echo "   Name: LOGGING_LEVEL_PE_CONTRATAIA"
echo "   Value: DEBUG"
echo ""

echo "   3. Click: Save"
echo ""
read -p "Presiona Enter cuando hayas completado..."

# Redis (optional)
echo ""
read -p "¿Deseas configurar Redis? (s/n): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Ss]$ ]]; then
    echo "   REDIS:"
    echo "   ─────────────────────────────────────────────────────"
    echo "   Name: SPRING_DATA_REDIS_HOST"
    echo "   Value: tu-redis.redis.cache.windows.net"
    echo ""
    echo "   Name: SPRING_DATA_REDIS_PORT"
    echo "   Value: 6380"
    echo ""
    echo "   Name: SPRING_DATA_REDIS_PASSWORD"
    echo "   Value: Tu$RedisPassoword123!"
    echo ""
    echo "   Name: SPRING_DATA_REDIS_SSL"
    echo "   Value: true"
    echo ""
    read -p "Presiona Enter cuando hayas completado..."
fi

# Deployment
echo ""
echo "═══════════════════════════════════════════════════════════════════════════════════"
echo "5️⃣  DEPLOYMENT DEL JAR"
echo "═══════════════════════════════════════════════════════════════════════════════════"
echo ""

read -p "¿Deseas hacer el deployment ahora? (s/n): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Ss]$ ]]; then
    echo "🔧 Compilando Maven..."
    mvn clean package -DskipTests

    echo ""
    echo "📦 Preparando deployment..."

    DEPLOY_DIR="/tmp/contrataia-azure-deploy"
    rm -rf "$DEPLOY_DIR"
    mkdir -p "$DEPLOY_DIR"

    cp target/contrataia-backend-*.jar "$DEPLOY_DIR/app.jar"

    # Create startup.sh
    cat > "$DEPLOY_DIR/startup.sh" << 'STARTUP_SCRIPT'
#!/bin/bash
cd /home/site/wwwroot
java -Xms512m -Xmx1024m \
  -Dspring.profiles.active=azure \
  -jar /home/site/wwwroot/app.jar \
  --server.port=80
STARTUP_SCRIPT
    chmod +x "$DEPLOY_DIR/startup.sh"

    # Create .deployment file
    cat > "$DEPLOY_DIR/.deployment" << 'DEPLOYMENT_FILE'
[config]
SCM_DO_BUILD_DURING_DEPLOYMENT = false
DEPLOYMENT_FILE

    # Create ZIP
    cd "$DEPLOY_DIR"
    zip -q -r deployment.zip app.jar startup.sh .deployment
    ZIP_SIZE=$(du -h deployment.zip | cut -f1)

    echo "📤 Subiendo a Azure ($ZIP_SIZE)..."
    az webapp deployment source config-zip \
      --resource-group "$RESOURCE_GROUP" \
      --name "$APP_SERVICE" \
      --src "$DEPLOY_DIR/deployment.zip"

    echo "✅ Deployment subido"

    echo ""
    echo "🔄 Reiniciando App Service..."
    az webapp restart \
      --resource-group "$RESOURCE_GROUP" \
      --name "$APP_SERVICE"

    echo "✅ App Service reiniciado"
fi

# Validation
echo ""
echo "═══════════════════════════════════════════════════════════════════════════════════"
echo "6️⃣  VALIDACIÓN"
echo "═══════════════════════════════════════════════════════════════════════════════════"
echo ""

APP_URL="https://$APP_SERVICE.azurewebsites.net"

echo "⏳ Esperando 30 segundos para que inicie Spring Boot..."
sleep 30

echo ""
echo "🔍 Health Check..."
HEALTH=$(curl -s -w "\n%{http_code}" "$APP_URL/api/actuator/health")
HTTP_CODE=$(echo "$HEALTH" | tail -1)

if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ Backend está OPERATIVO!"
    echo ""
    echo "URLs de acceso:"
    echo "   🌐 API:         $APP_URL/api"
    echo "   📊 Health:      $APP_URL/api/actuator/health"
    echo "   📚 Swagger:     $APP_URL/api/swagger-ui.html"
    echo "   📈 Metrics:     $APP_URL/api/actuator/metrics"
else
    echo "⚠️  Backend aún está iniciando (HTTP $HTTP_CODE)"
    echo "    Espera 30 segundos más e intenta:"
    echo "    curl $APP_URL/api/actuator/health"
fi

echo ""
echo "═══════════════════════════════════════════════════════════════════════════════════"
echo "✨ ¡CONFIGURACIÓN COMPLETADA!"
echo "═══════════════════════════════════════════════════════════════════════════════════"
echo ""
echo "Monitoreo en tiempo real:"
echo "  (bash scripts/monitor-deployment.sh)"
echo ""
echo "Ver logs:"
echo "  az webapp log tail -g $RESOURCE_GROUP -n $APP_SERVICE"
echo ""

