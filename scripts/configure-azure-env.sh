#!/bin/bash

# ===================================================================
# Script para Configurar Variables de Entorno en Azure App Service
# ===================================================================

set -e

RESOURCE_GROUP="${AZURE_RESOURCE_GROUP:-contrataia-rg}"
APP_SERVICE="${AZURE_APP_SERVICE:-contrataia-backend}"

echo "🔧 Configurando variables de entorno en Azure App Service"
echo "   Resource Group: $RESOURCE_GROUP"
echo "   App Service: $APP_SERVICE"
echo ""

# ⚠️ IMPORTANTE: Reemplaza estos valores con los reales
cat << 'EOF'

════════════════════════════════════════════════════════════════
EDITA LOS VALORES ANTES DE CONTINUAR
════════════════════════════════════════════════════════════════

SUSTITUYE ESTOS VALORES:

1. SPRING_DATASOURCE_URL
   Formato: jdbc:postgresql://SERVIDOR:5432/BASEDATOS
   Ejemplo: jdbc:postgresql://contrataia-db.postgres.database.azure.com:5432/contrataia_prod

2. SPRING_DATASOURCE_USERNAME
   Ejemplo: admin@contrataia-db

3. SPRING_DATASOURCE_PASSWORD
   Tu contraseña segura de PostgreSQL

4. JWT_SECRET
   Una cadena aleatoria muy larga (mínimo 32 caracteres)

5. Otros servicios (Redis, Mail, etc.)

════════════════════════════════════════════════════════════════

EOF

# Función para establecer configuración
set_config() {
    local KEY=$1
    local VALUE=$2

    echo "   Configurando $KEY..."
    az webapp config appsettings set \
      --resource-group "$RESOURCE_GROUP" \
      --name "$APP_SERVICE" \
      --settings "$KEY=$VALUE" \
      --output none
}

# ===================================================================
# VARIABLES DE JAVA
# ===================================================================

echo ""
echo "📦 Configurando JVM..."
set_config "JAVA_OPTS" "-Xms512m -Xmx1024m"
set_config "JAVA_VERSION" "21"

# ===================================================================
# DATABASE POSTGRESQL
# ===================================================================

echo ""
echo "🗄️  Configurando PostgreSQL..."
echo "   Introduce la URL PostgreSQL (SPRING_DATASOURCE_URL):"
read DATASOURCE_URL
set_config "SPRING_DATASOURCE_URL" "$DATASOURCE_URL"

echo "   Introduce el usuario PostgreSQL (SPRING_DATASOURCE_USERNAME):"
read DATASOURCE_USER
set_config "SPRING_DATASOURCE_USERNAME" "$DATASOURCE_USER"

echo "   Introduce la contraseña PostgreSQL (SPRING_DATASOURCE_PASSWORD):"
read -s DATASOURCE_PASS
set_config "SPRING_DATASOURCE_PASSWORD" "$DATASOURCE_PASS"
echo "" # Nueva línea después de read -s

# ===================================================================
# JWT / SEGURIDAD
# ===================================================================

echo ""
echo "🔐 Configurando JWT..."
echo "   Introduce JWT_SECRET (cadena aleatoria larga, mínimo 32 caracteres):"
read JWT_SECRET
set_config "JWT_SECRET" "$JWT_SECRET"

set_config "JWT_EXPIRATION" "86400000"  # 24 horas
set_config "JWT_REFRESH_EXPIRATION" "604800000"  # 7 días

# ===================================================================
# REDIS (Opcional)
# ===================================================================

read -p "¿Configurar Redis? (s/n): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Ss]$ ]]; then
    echo ""
    echo "💾 Configurando Redis..."
    echo "   Introduce SPRING_DATA_REDIS_HOST:"
    read REDIS_HOST
    set_config "SPRING_DATA_REDIS_HOST" "$REDIS_HOST"

    echo "   Introduce SPRING_DATA_REDIS_PORT (default 6380 para Azure):"
    read REDIS_PORT
    set_config "SPRING_DATA_REDIS_PORT" "${REDIS_PORT:=6380}"

    echo "   Introduce SPRING_DATA_REDIS_PASSWORD:"
    read -s REDIS_PASS
    set_config "SPRING_DATA_REDIS_PASSWORD" "$REDIS_PASS"
    echo ""

    set_config "SPRING_DATA_REDIS_SSL" "true"
fi

# ===================================================================
# MAIL (Opcional)
# ===================================================================

read -p "¿Configurar EMAIL SMTP? (s/n): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Ss]$ ]]; then
    echo ""
    echo "📧 Configurando Mail..."
    echo "   Introduce SPRING_MAIL_HOST (default: smtp.gmail.com):"
    read MAIL_HOST
    set_config "SPRING_MAIL_HOST" "${MAIL_HOST:=smtp.gmail.com}"

    echo "   Introduce SPRING_MAIL_PORT (default: 587):"
    read MAIL_PORT
    set_config "SPRING_MAIL_PORT" "${MAIL_PORT:=587}"

    echo "   Introduce SPRING_MAIL_USERNAME:"
    read MAIL_USER
    set_config "SPRING_MAIL_USERNAME" "$MAIL_USER"

    echo "   Introduce SPRING_MAIL_PASSWORD (App Password si es Gmail):"
    read -s MAIL_PASS
    set_config "SPRING_MAIL_PASSWORD" "$MAIL_PASS"
    echo ""
fi

# ===================================================================
# LOGGING
# ===================================================================

echo ""
echo "📝 Configurando Logging..."
set_config "LOGGING_LEVEL_ROOT" "INFO"
set_config "LOGGING_LEVEL_PE_CONTRATAIA" "DEBUG"

# ===================================================================
# PERFILES DE SPRING
# ===================================================================

echo ""
echo "🎯 Configurando Perfiles..."
set_config "SPRING_PROFILES_ACTIVE" "azure"

# ===================================================================
# MOSTRAR RESUMEN
# ===================================================================

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "✅ VARIABLES CONFIGURADAS"
echo "════════════════════════════════════════════════════════════════"
echo ""
echo "Verificar en Azure Portal:"
echo "  Settings → Configuration → Application settings"
echo ""
echo "O verificar vía CLI:"
echo "  az webapp config appsettings list -g $RESOURCE_GROUP -n $APP_SERVICE"
echo ""
echo "⚠️  Importante: Algún cambio de variable requiere reiniciar el App Service"
echo ""
echo "Reiniciar ahora?"
read -p "¿Deseas reiniciar el App Service? (s/n): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Ss]$ ]]; then
    echo "🔄 Reiniciando..."
    az webapp restart -g $RESOURCE_GROUP -n $APP_SERVICE
    echo "✅ Reinicio completado"
fi
echo ""

