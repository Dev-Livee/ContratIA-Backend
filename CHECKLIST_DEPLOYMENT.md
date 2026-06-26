# 📋 CHECKLIST VISUAL - Deployment a Azure App Service

## ✅ PASO 1: GENERAR BUILD

Historia: [ ] Build completado con Maven
```bash
cd /Users/maycolrojas/Documents/GitHub/ContratIA-MVP/ContratIA-Backend
mvn clean package -DskipTests -X
```
- [ ] Sin errores en compilación
- [ ] archivo generado: `target/contrataia-backend-1.0.0-SNAPSHOT.jar`
- [ ] Tamaño: ~60-100MB

---

## ✅ PASO 2: CREAR RECURSO EN AZURE

### Vía Azure Portal:
1. [ ] Crear o verificar **Resource Group**: `contrataia-rg`
2. [ ] Crear **App Service**:
   - [ ] Nombre: `contrataia-backend`
   - [ ] Región: `East US` (o tu región preferida)
   - [ ] Publicar: `Code`
   - [ ] Runtime: `Java 21` ✨
   - [ ] SO: `Linux` (recomendado) o Windows
   - [ ] Plan: `B1` (básico) o superior

### Vía CLI (alternativa automática):
```bash
az group create -n contrataia-rg -l eastus
az appservice plan create -n contrataia-plan -g contrataia-rg --sku B1 --is-linux
az webapp create -g contrataia-rg -p contrataia-plan -n contrataia-backend --runtime "java|21"
```

---

## ✅ PASO 3: CONFIGURAR RUNTIME EN AZURE PORTAL

**App Service → Settings → Configuration → General settings**

```
Stack settings:
  [ ] Java version: 21 ✅
  [ ] Java web server container: Tomcat 10.1
  [ ] Java web server container version: LATEST
  
Startup Command:
  [ ] java -Xms512m -Xmx1024m -Dspring.profiles.active=azure -jar /home/site/wwwroot/app.jar --server.port=80
```

---

## ✅ PASO 4: CONFIGURAR VARIABLES DE ENTORNO

**App Service → Settings → Configuration → Application settings**

### Base de Datos PostgreSQL:
```
[ ] SPRING_DATASOURCE_URL = jdbc:postgresql://tu-server.postgres.database.azure.com:5432/contrataia_db
[ ] SPRING_DATASOURCE_USERNAME = admin@tu-server
[ ] SPRING_DATASOURCE_PASSWORD = Tu$Password123!
```

### JVM:
```
[ ] JAVA_OPTS = -Xms512m -Xmx1024m
[ ] JAVA_VERSION = 21
```

### JWT y Seguridad:
```
[ ] JWT_SECRET = tu-clave-aleatoria-super-larga-minimo-32-caracteres-aqui-123456
[ ] JWT_EXPIRATION = 86400000
[ ] JWT_REFRESH_EXPIRATION = 604800000
```

### Redis (Opcional):
```
[ ] SPRING_DATA_REDIS_HOST = tu-redis.redis.cache.windows.net
[ ] SPRING_DATA_REDIS_PORT = 6380
[ ] SPRING_DATA_REDIS_PASSWORD = Tu$RedisPassword123!
[ ] SPRING_DATA_REDIS_SSL = true
```

### Email (Opcional):
```
[ ] SPRING_MAIL_HOST = smtp.gmail.com
[ ] SPRING_MAIL_PORT = 587
[ ] SPRING_MAIL_USERNAME = tu-email@gmail.com
[ ] SPRING_MAIL_PASSWORD = tu-app-password
```

### Logging:
```
[ ] LOGGING_LEVEL_ROOT = INFO
[ ] LOGGING_LEVEL_PE_CONTRATAIA = DEBUG
```

---

## ✅ PASO 5: SUBIR JAR A AZURE

**Opción A: Via CLI (Recomendado)**

```bash
# Preparar deployment
mkdir -p deployment
cp target/contrataia-backend-1.0.0-SNAPSHOT.jar deployment/app.jar

# Crear startup.sh
cat > deployment/startup.sh << 'EOF'
#!/bin/bash
cd /home/site/wwwroot
java -Xms512m -Xmx1024m -Dspring.profiles.active=azure -jar app.jar --server.port=80
EOF
chmod +x deployment/startup.sh

# Crear .deployment
cat > deployment/.deployment << 'EOF'
[config]
SCM_DO_BUILD_DURING_DEPLOYMENT = false
EOF

# Empaquetar y subir
cd deployment
zip -r deployment.zip app.jar startup.sh .deployment
az webapp deployment source config-zip \
  --resource-group contrataia-rg \
  --name contrataia-backend \
  --src deployment.zip

# Reiniciar
az webapp restart -g contrataia-rg -n contrataia-backend
```

**Opción B: Via Portal (Manual)**
1. [ ] Ve a **Deployment** → **Deployment Center**
2. [ ] Selecciona **Manual deployment**
3. [ ] Arrastra el ZIP
4. [ ] Espera a que complete

---

## ✅ PASO 6: VALIDAR DEPLOYMENT

### En Portal:
1. [ ] App Service estado: **Running** ✅
2. [ ] Sin errores en vista general

### Health Check:
```bash
curl https://contrataia-backend.azurewebsites.net/api/actuator/health

# Respuesta esperada:
# {"status":"UP","database":{"status":"UP"},...}
```

[ ] Responde con HTTP 200
[ ] Status es "UP"
[ ] Database conecta exitosamente

### Logs:
```bash
az webapp log tail -g contrataia-rg -n contrataia-backend

# Debe mostrar:
# Spring Boot startup OK
# Tomcat started on port 80
# No errores de conexión a BD
```

[ ] Sin errores críticos (ERROR)
[ ] Mensaje "Started ContrataIAApplication"

### Endpoints:
```bash
# Swagger
https://contrataia-backend.azurewebsites.net/api/swagger-ui.html
[ ] Accesible, muestra endpoints

# Metrics
https://contrataia-backend.azurewebsites.net/api/actuator/metrics
[ ] Responde correctamente
```

---

## ✅ PASO 7: TROUBLESHOOTING

### ⚠️ Problema: "Hey, Java developers!" (página default)
**Causa:** JAR no se ejecuta  
**Checklist:**
- [ ] Startup Command configurado correctamente
- [ ] JAR está en `/home/site/wwwroot/app.jar` (verificar en Kudu)
- [ ] JAR tiene permisos de ejecución
- [ ] Variables de entorno configuradas

**Solución:**
```bash
# 1. Verificar en Kudu Console
https://contrataia-backend.scm.azurewebsites.net/
# Navega a: /home/site/wwwroot/
# Debe ver: app.jar ✅

# 2. Dar permisos
chmod +x /home/site/wwwroot/app.jar

# 3. Reiniciar
az webapp restart -g contrataia-rg -n contrataia-backend

# 4. Ver logs
az webapp log tail -g contrataia-rg -n contrataia-backend
```

### ⚠️ Problema: 502 Bad Gateway
**Causa:** Spring Boot tardó demasiado en iniciar  
**Solución:**
```bash
# Aumentar timeout
az webapp config appsettings set \
  -g contrataia-rg -n contrataia-backend \
  --settings SCM_COMMAND_IDLE_TIMEOUT=300

# Esperar 60 segundos e intentar de nuevo
```

### ⚠️ Problema: 503 Service Unavailable
**Causa:** OutOfMemory o reinicio continuo  
**Solución:**
```bash
# Aumentar memoria
# En Startup Command: -Xms1024m -Xmx2048m

# O cambiar App Service Plan a B2
az appservice plan update -n contrataia-plan -g contrataia-rg --sku B2
```

### ⚠️ Problema: Database connection refused
**Causa:** Firewall o credenciales inválidas  
**Checklist:**
- [ ] SPRING_DATASOURCE_URL es correcta
- [ ] SPRING_DATASOURCE_USERNAME y PASSWORD son válidas
- [ ] Firewall de Azure Database for PostgreSQL permite `0.0.0.0/0` o rango de Azure
- [ ] Base de datos existe

---

## ✅ PASO 8: MONITOREO CONTINUO

```bash
# Terminal 1: Logs streaming
az webapp log tail -g contrataia-rg -n contrataia-backend --follow

# Terminal 2: Health checks cada 10 segundos
while true; do
  curl -s https://contrataia-backend.azurewebsites.net/api/actuator/health | jq .
  sleep 10
done

# Terminal 3: Dashboard de métricas
bash scripts/monitor-deployment.sh
```

---

## ✅ PRODUCCIÓN - PRE-LAUNCH CHECKLIST

- [ ] Cambiar `LOGGING_LEVEL_PE_CONTRATAIA` a `INFO` (no DEBUG)
- [ ] Verificar que `JWT_SECRET` es único y seguro (mínimo 64 caracteres)
- [ ] Base de datos PostgreSQL configurada con backups automáticos
- [ ] Redis está en modo `azure` con SSL enabled
- [ ] Alertas de monitoreo configuradas
- [ ] Plan App Service es B2 o superior
- [ ] "Always On" enabled (Portal → Settings → General settings)
- [ ] HTTPS automático (Azure redirige HTTP → HTTPS)
- [ ] Firewall y Network Security Group configurados correctamente
- [ ] Application Insights habilitado (optional pero recomendado)

---

## 📌 COMANDOS RÁPIDOS DE REFERENCIA

```bash
# Deploy automático
bash scripts/deploy-to-azure.sh

# Validar
bash scripts/validate-deployment.sh

# Configurar variables
bash scripts/configure-azure-env.sh

# Monitorear
bash scripts/monitor-deployment.sh

# Ver logs
az webapp log tail -g contrataia-rg -n contrataia-backend

# Reiniciar
az webapp restart -g contrataia-rg -n contrataia-backend

# Ver estado
az webapp show -g contrataia-rg -n contrataia-backend -q state

# Descargar logs completos
az webapp log download -g contrataia-rg -n contrataia-backend -log-file logs.zip
```

---

**¿Completaste TODO?** ✨ Tu backend está 100% operativo en Azure App Service.

