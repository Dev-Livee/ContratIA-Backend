#!/bin/bash

# ===================================================================
# Monitoreo y Troubleshooting en tiempo real
# ===================================================================

RESOURCE_GROUP="${AZURE_RESOURCE_GROUP:-contrataia-rg}"
APP_SERVICE="${AZURE_APP_SERVICE:-contrataia-backend}"
APP_URL="https://$APP_SERVICE.azurewebsites.net"

echo "🔍 Dashboard de Monitoreo - ContratIA Backend"
echo "================================================"
echo ""

while true; do
    clear
    echo "🔄 Actualizando... $(date '+%H:%M:%S')"
    echo ""

    # 1. Estado del App Service
    echo "1️⃣  Estado del App Service:"
    STATE=$(az webapp show -g $RESOURCE_GROUP -n $APP_SERVICE -q state -o tsv 2>/dev/null || echo "Error")
    echo "   Estado: $STATE"
    echo ""

    # 2. Health Check
    echo "2️⃣  Health Check:"
    HEALTH=$(curl -s -w "\n%{http_code}" "$APP_URL/api/actuator/health" 2>/dev/null || echo "Error\n000")
    HTTP_CODE=$(echo "$HEALTH" | tail -1)

    if [ "$HTTP_CODE" = "200" ]; then
        STATUS=$(echo "$HEALTH" | head -1 | jq -r '.status' 2>/dev/null || echo "UNKNOWN")
        echo "   HTTP Code: $HTTP_CODE ✅"
        echo "   Status: $STATUS"
    else
        echo "   HTTP Code: $HTTP_CODE ❌"
    fi
    echo ""

    # 3. Métricas del servidor
    echo "3️⃣  CPU y Memoria (últimas 5 minutos):"
    CPU=$(az monitor metrics list \
      --resource-group $RESOURCE_GROUP \
      --resource-type microsoftWeb/sites \
      --resource-name $APP_SERVICE \
      --metric "AverageCpuTime" \
      --statistics Average \
      --interval PT5M \
      --query "value[0].timeseries[0].data[-1].average" \
      -o tsv 2>/dev/null || echo "N/A")

    MEM=$(az monitor metrics list \
      --resource-group $RESOURCE_GROUP \
      --resource-type microsoftWeb/sites \
      --resource-name $APP_SERVICE \
      --metric "MemoryPercentage" \
      --statistics Average \
      --interval PT5M \
      --query "value[0].timeseries[0].data[-1].average" \
      -o tsv 2>/dev/null || echo "N/A")

    echo "   CPU Time: ${CPU}ms"
    echo "   Memory: ${MEM}%"
    echo ""

    # 4. Request count
    echo "4️⃣  Requests (últimas 5 minutos):"
    REQUESTS=$(az monitor metrics list \
      --resource-group $RESOURCE_GROUP \
      --resource-type microsoftWeb/sites \
      --resource-name $APP_SERVICE \
      --metric "Requests" \
      --statistics Total \
      --interval PT5M \
      --query "value[0].timeseries[0].data[-1].total" \
      -o tsv 2>/dev/null || echo "N/A")

    echo "   Total: $REQUESTS"
    echo ""

    # 5. Errores HTTP
    echo "5️⃣  Errores HTTP (últimas 5 minutos):"
    ERRORS_5XX=$(az monitor metrics list \
      --resource-group $RESOURCE_GROUP \
      --resource-type microsoftWeb/sites \
      --resource-name $APP_SERVICE \
      --metric "Http5xx" \
      --statistics Total \
      --interval PT5M \
      --query "value[0].timeseries[0].data[-1].total" \
      -o tsv 2>/dev/null || echo "0")

    ERRORS_4XX=$(az monitor metrics list \
      --resource-group $RESOURCE_GROUP \
      --resource-type microsoftWeb/sites \
      --resource-name $APP_SERVICE \
      --metric "Http4xx" \
      --statistics Total \
      --interval PT5M \
      --query "value[0].timeseries[0].data[-1].total" \
      -o tsv 2>/dev/null || echo "0")

    echo "   5XX: $ERRORS_5XX ❌"
    echo "   4XX: $ERRORS_4XX ⚠️"
    echo ""

    # 6. Último error en log
    echo "6️⃣  Últimas líneas de log:"
    LOGS=$(az webapp log tail -g $RESOURCE_GROUP -n $APP_SERVICE --lines 10 2>/dev/null | tail -5)
    if [ -z "$LOGS" ]; then
        echo "   (Sin logs disponibles)"
    else
        echo "$LOGS" | sed 's/^/   /'
    fi

    echo ""
    echo "════════════════════════════════════════════════════════════════"
    echo "Presiona Ctrl+C para salir | Refresca cada 10 segundos"
    echo ""

    sleep 10
done

