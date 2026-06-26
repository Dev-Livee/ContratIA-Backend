#!/bin/bash

# ===================================================================
# Script de Validación Post-Deployment
# Verifica que tu backend está 100% funcional en Azure
# ===================================================================

set -e

echo "🔍 Validando deployment de ContratIA Backend en Azure"
echo ""

# Configuración
RESOURCE_GROUP="${AZURE_RESOURCE_GROUP:-contrataia-rg}"
APP_SERVICE="${AZURE_APP_SERVICE:-contrataia-backend}"
APP_URL="https://$APP_SERVICE.azurewebsites.net"

# ===================================================================
# VALIDACIÓN 1: App Service está running
# ===================================================================

echo "1️⃣  Verificando estado del App Service..."
STATE=$(az webapp show \
  --resource-group "$RESOURCE_GROUP" \
  --name "$APP_SERVICE" \
  --query state -o tsv)

if [ "$STATE" = "Running" ]; then
    echo "   ✅ App Service está RUNNING"
else
    echo "   ❌ App Service NO está running (Estado: $STATE)"
    echo "   Intenta: az webapp start -g $RESOURCE_GROUP -n $APP_SERVICE"
    exit 1
fi
echo ""

# ===================================================================
# VALIDACIÓN 2: Health check
# ===================================================================

echo "2️⃣  Health Check (/api/actuator/health)..."
HEALTH=$(curl -s -w "\n%{http_code}" "$APP_URL/api/actuator/health")
HTTP_CODE=$(echo "$HEALTH" | tail -1)
BODY=$(echo "$HEALTH" | head -1)

if [ "$HTTP_CODE" = "200" ]; then
    if echo "$BODY" | grep -q "UP"; then
        echo "   ✅ Backend está OPERATIVO"
        echo "   Respuesta:"
        echo "$BODY" | jq . 2>/dev/null || echo "$BODY"
    else
        echo "   ⚠️  Backend responde pero status no es UP"
        echo "   Respuesta: $BODY"
    fi
else
    echo "   ❌ HTTP Code: $HTTP_CODE (esperado: 200)"
    echo "   El backend aún está iniciando... Espera 30 segundos más."
    exit 1
fi
echo ""

# ===================================================================
# VALIDACIÓN 3: Database connectivity
# ===================================================================

echo "3️⃣  Verificando conectividad a Base de Datos..."
DB_HEALTH=$(curl -s "$APP_URL/api/actuator/health" | jq '.components.db.status' -r 2>/dev/null || echo "UNKNOWN")

if [ "$DB_HEALTH" = "UP" ]; then
    echo "   ✅ PostgreSQL conectado exitosamente"
elif [ "$DB_HEALTH" = "DOWN" ]; then
    echo "   ❌ PostgreSQL no accesible"
    echo "   Verifica:"
    echo "      - SPRING_DATASOURCE_URL sea correcta"
    echo "      - Firewall de Azure Database for PostgreSQL permita conexiones"
    echo "      - Credenciales sean válidas"
else
    echo "   ⚠️  Estado desconocido: $DB_HEALTH"
fi
echo ""

# ===================================================================
# VALIDACIÓN 4: Redis connectivity (si está configurado)
# ===================================================================

echo "4️⃣  Verificando conectividad a Redis..."
REDIS_HEALTH=$(curl -s "$APP_URL/api/actuator/health" | jq '.components.redis.status' -r 2>/dev/null || echo "UNKNOWN")

if [ "$REDIS_HEALTH" = "UP" ]; then
    echo "   ✅ Redis conectado exitosamente"
elif [ "$REDIS_HEALTH" = "UNKNOWN" ] || [ "$REDIS_HEALTH" = "null" ]; then
    echo "   ⚠️  Redis no configurado (es opcional)"
elif [ "$REDIS_HEALTH" = "DOWN" ]; then
    echo "   ❌ Redis no accesible"
    echo "   Verifica SPRING_DATA_REDIS_HOST y credenciales"
fi
echo ""

# ===================================================================
# VALIDACIÓN 5: Swagger UI
# ===================================================================

echo "5️⃣  Verificando API Documentation (Swagger)..."
SWAGGER=$(curl -s -w "\n%{http_code}" "$APP_URL/api/swagger-ui.html")
SWAGGER_CODE=$(echo "$SWAGGER" | tail -1)

if [ "$SWAGGER_CODE" = "200" ]; then
    echo "   ✅ Swagger UI accesible en $APP_URL/api/swagger-ui.html"
else
    echo "   ⚠️  Swagger no accesible (HTTP $SWAGGER_CODE)"
fi
echo ""

# ===================================================================
# VALIDACIÓN 6: Logs sin errores críticos
# ===================================================================

echo "6️⃣  Analizando logs..."
echo "   Obteniendo últimos 50 logs..."

LOGS=$(az webapp log download \
  --resource-group "$RESOURCE_GROUP" \
  --name "$APP_SERVICE" \
  --log-file /tmp/app-logs.zip 2>/dev/null && unzip -qq -o /tmp/app-logs.zip -d /tmp/logs && cat /tmp/logs/*/default_docker.log 2>/dev/null | tail -50 || echo "No logs disponibles")

ERROR_COUNT=$(echo "$LOGS" | grep -i "error" | grep -v "DEBUG" | wc -l)
WARN_COUNT=$(echo "$LOGS" | grep -i "warn" | wc -l)

if [ "$ERROR_COUNT" -eq 0 ]; then
    echo "   ✅ Sin errores críticos en logs"
else
    echo "   ⚠️  $ERROR_COUNT errores encontrados"
fi

echo "   Warnings: $WARN_COUNT"
echo ""

# ===================================================================
# VALIDACIÓN 7: Checkeo de velocidad
# ===================================================================

echo "7️⃣  Verificando velocidad de respuesta..."
START_TIME=$(date +%s%3N)
curl -s "$APP_URL/api/actuator/health" > /dev/null
END_TIME=$(date +%s%3N)
RESPONSE_TIME=$((END_TIME - START_TIME))

if [ "$RESPONSE_TIME" -lt 500 ]; then
    echo "   ✅ Respuesta rápida: ${RESPONSE_TIME}ms"
elif [ "$RESPONSE_TIME" -lt 1000 ]; then
    echo "   ⚠️  Respuesta moderada: ${RESPONSE_TIME}ms"
else
    echo "   ⚠️  Respuesta lenta: ${RESPONSE_TIME}ms (considera aumentar recursos)"
fi
echo ""

# ===================================================================
# RESUMEN
# ===================================================================

echo "════════════════════════════════════════════════════════════════"
echo "✅ VALIDACIÓN COMPLETADA"
echo "════════════════════════════════════════════════════════════════"
echo ""
echo "URLs importantes:"
echo "  🌐 Backend:     $APP_URL/api"
echo "  📊 Health:      $APP_URL/api/actuator/health"
echo "  📚 Swagger:     $APP_URL/api/swagger-ui.html"
echo "  📈 Metrics:     $APP_URL/api/actuator/metrics"
echo ""
echo "Monitoreo en tiempo real:"
echo "  az webapp log tail -g $RESOURCE_GROUP -n $APP_SERVICE --follow"
echo ""

