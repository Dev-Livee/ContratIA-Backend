#!/bin/bash

# ===================================================================
# Script de Deployment para Azure App Service
# Proyecto: ContratIA Backend
# Propósito: Automatizar build, preparación y deploy a Azure
# ===================================================================

set -e  # Exit on error

echo "🚀 Iniciando deployment de ContratIA Backend a Azure App Service"
echo ""

# ===================================================================
# CONFIGURACIÓN
# ===================================================================

PROJECT_DIR="/Users/maycolrojas/Documents/GitHub/ContratIA-MVP/ContratIA-Backend"
DEPLOYMENT_DIR="$PROJECT_DIR/azure-deployment"
JAR_NAME="contrataia-backend-1.0.0-SNAPSHOT.jar"
APP_JAR="app.jar"
RESOURCE_GROUP="${AZURE_RESOURCE_GROUP:-contrataia-rg}"
APP_SERVICE="${AZURE_APP_SERVICE:-contrataia-backend}"
ZIP_FILE="deployment.zip"

# ===================================================================
# PASO 1: Limpiar y compilar
# ===================================================================

echo "📦 PASO 1: Compilando con Maven..."
cd "$PROJECT_DIR"
rm -rf target/
mvn clean package -DskipTests -q

if [ ! -f "target/$JAR_NAME" ]; then
    echo "❌ Error: JAR no se generó en target/$JAR_NAME"
    exit 1
fi
echo "✅ JAR generado exitosamente"
echo ""

# ===================================================================
# PASO 2: Preparar estructura para Azure
# ===================================================================

echo "📁 PASO 2: Preparando estructura de deployment..."
rm -rf "$DEPLOYMENT_DIR"
mkdir -p "$DEPLOYMENT_DIR"

# Copiar JAR
cp "target/$JAR_NAME" "$DEPLOYMENT_DIR/$APP_JAR"
echo "  ✅ JAR copiado"

# Crear script de startup
cat > "$DEPLOYMENT_DIR/startup.sh" << 'EOF'
#!/bin/bash

# Archivo de startup para Azure App Service
# Se ejecuta automáticamente cuando arranca el contenedor

echo "🔥 Iniciando Spring Boot - ContratIA Backend"
echo "Timestamp: $(date)"
echo ""

# Ir al directorio de la app
cd /home/site/wwwroot

# Mostrar información del entorno
echo "📋 Información del entorno:"
echo "  Java version: $(java -version 2>&1 | head -1)"
echo "  PWD: $(pwd)"
echo "  Files en wwwroot:"
ls -lah

echo ""
echo "🚀 Ejecutando Spring Boot..."

# Comando de startup
# NOTA: Las variables de entorno se cargan automáticamente desde Application Settings en Azure Portal
java -Xms512m -Xmx1024m \
  -Dserver.shutdown=graceful \
  -Dspring.profiles.active=azure \
  -Dspring.config.location=file:///home/site/wwwroot/application-azure.yml \
  -jar /home/site/wwwroot/app.jar \
  --server.port=80 \
  --spring.datasource.url=${SPRING_DATASOURCE_URL} \
  --spring.datasource.username=${SPRING_DATASOURCE_USERNAME} \
  --spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

EOF

chmod +x "$DEPLOYMENT_DIR/startup.sh"
echo "  ✅ Script startup.sh creado"

# Crear archivo .deployment
cat > "$DEPLOYMENT_DIR/.deployment" << 'EOF'
[config]
SCM_DO_BUILD_DURING_DEPLOYMENT = false
command = bash /home/site/wwwroot/startup.sh
EOF

echo "  ✅ Archivo .deployment creado"

# Copiar application-azure.yml si existe
if [ -f "src/main/resources/application-azure.yml" ]; then
    cp "src/main/resources/application-azure.yml" "$DEPLOYMENT_DIR/application-azure.yml"
    echo "  ✅ application-azure.yml copiado"
fi

echo ""

# ===================================================================
# PASO 3: Crear ZIP para deployment
# ===================================================================

echo "📦 PASO 3: Empaquetando en ZIP..."
cd "$DEPLOYMENT_DIR"
rm -f "$ZIP_FILE"
zip -q -r "$ZIP_FILE" .
ZIP_SIZE=$(du -h "$ZIP_FILE" | cut -f1)
echo "  ✅ ZIP creado: $ZIP_FILE ($ZIP_SIZE)"
echo ""

# ===================================================================
# PASO 4: Desplegar a Azure
# ===================================================================

echo "☁️  PASO 4: Desplegando a Azure App Service..."
echo "  Resource Group: $RESOURCE_GROUP"
echo "  App Service: $APP_SERVICE"
echo ""

# Verificar que Azure CLI está disponible
if ! command -v az &> /dev/null; then
    echo "❌ Error: Azure CLI no está instalado"
    echo "   Instálalo con: brew install azure-cli"
    exit 1
fi

# Desplegar ZIP
echo "  Subiendo ZIP a Azure (esto puede tardar 1-3 minutos)..."
az webapp deployment source config-zip \
  --resource-group "$RESOURCE_GROUP" \
  --name "$APP_SERVICE" \
  --src "$DEPLOYMENT_DIR/$ZIP_FILE"

if [ $? -eq 0 ]; then
    echo "  ✅ ZIP desplegado exitosamente"
else
    echo "  ❌ Error en el deployment"
    exit 1
fi

echo ""

# ===================================================================
# PASO 5: Reiniciar App Service
# ===================================================================

echo "🔄 PASO 5: Reiniciando App Service..."
az webapp restart \
  --resource-group "$RESOURCE_GROUP" \
  --name "$APP_SERVICE"
echo "  ✅ App Service reiniciado"
echo ""

# ===================================================================
# PASO 6: Verificar deployment
# ===================================================================

echo "✅ PASO 6: Verificando deployment..."
APP_URL="https://$APP_SERVICE.azurewebsites.net"
echo ""
echo "📊 URLs de verificación:"
echo "  Health Check:  $APP_URL/api/actuator/health"
echo "  Swagger UI:    $APP_URL/api/swagger-ui.html"
echo "  Portal:        https://portal.azure.com/"
echo ""
echo "📝 Para ver logs en tiempo real:"
echo "  az webapp log tail --resource-group $RESOURCE_GROUP --name $APP_SERVICE"
echo ""

# Esperar 10 segundos y hacer primer health check
echo "⏳ Esperando 10 segundos para que inicie Spring Boot..."
sleep 10

echo "🔍 Haciendo health check..."
HEALTH_RESPONSE=$(curl -s "$APP_URL/api/actuator/health" || echo "Error de conexión")

if echo "$HEALTH_RESPONSE" | grep -q "UP"; then
    echo "  ✅ Health check EXITOSO"
    echo "  Respuesta: $HEALTH_RESPONSE"
else
    echo "  ⚠️  Health check aún no responde (normal si está iniciando)"
    echo "  Respuesta: $HEALTH_RESPONSE"
    echo "  Espera 30-60 segundos más y vuelve a intentar"
fi

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "✨ ¡DEPLOYMENT COMPLETADO!"
echo "════════════════════════════════════════════════════════════════"
echo ""
echo "Próximos pasos:"
echo "  1. Espera 30-60 segundos para que Spring Boot inicie completamente"
echo "  2. Verifica: $APP_URL/api/actuator/health"
echo "  3. Revisa logs: az webapp log tail -g $RESOURCE_GROUP -n $APP_SERVICE"
echo "  4. Accede a Swagger: $APP_URL/api/swagger-ui.html"
echo ""

# Limpiar archivos temporales
echo "🧹 Limpiando temporales..."
rm -rf "$DEPLOYMENT_DIR"
echo "✅ Hecho"
echo ""

