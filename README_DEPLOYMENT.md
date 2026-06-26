# 📚 ÍNDICE MAESTRO - Guías de Deployment

> **Eres DevOps senior ahora.** Este es tu handbook completo para desplegar Spring Boot en Azure.

---

## 📖 ¿POR DÓNDE EMPIEZO?

### 🟢 OPCIÓN 1: Quiero deployar YA (5 minutos)
```
1. Lee: QUICK_START_AZURE.md (este archivo)
2. Elige: OPCIÓN A, B, o C
3. Ejecuta: bash scripts/deploy-to-azure.sh
4. Valida: bash scripts/validate-deployment.sh
5. LISTO ✅
```

### 🟡 OPCIÓN 2: Quiero entender primero (20 minutos)
```
1. Lee: ARCHITECTURE.md (entender la arquitectura)
2. Lee: AZURE_DEPLOYMENT_GUIDE.md (detalles técnicos)
3. Sigue: CHECKLIST_DEPLOYMENT.md (paso a paso)
4. Ejecuta: bash scripts/deploy-to-azure.sh
5. LISTO ✅
```

### 🔴 OPCIÓN 3: Quiero hacerlo manual (30 minutos)
```
1. Lee: EXAMPLES_COPY_PASTE.md (ejemplos listos para copiar)
2. Lee: AZURE_DEPLOYMENT_GUIDE.md (referencia completa)
3. Sigue: CHECKLIST_DEPLOYMENT.md (checklist visual)
4. Ejecuta manualmente los comandos
5. LISTO ✅
```

---

## 📑 DOCUMENTOS DISPONIBLES

| # | Documento | Duración | Para Quién | Contenido |
|----|-----------|----------|-----------|-----------|
| 1️⃣ | **[QUICK_START_AZURE.md](./QUICK_START_AZURE.md)** | 5 min | 🚀 Impaciente | Deploy rápido, 3 opciones diferentes |
| 2️⃣ | **[ARCHITECTURE.md](./ARCHITECTURE.md)** | 15 min | 🏗️ Arquitecto | Diagramas, flujos, layers, seguridad |
| 3️⃣ | **[AZURE_DEPLOYMENT_GUIDE.md](./AZURE_DEPLOYMENT_GUIDE.md)** | 45 min | 📖 Estudiante | GUÍA COMPLETA (todo en detalle) |
| 4️⃣ | **[CHECKLIST_DEPLOYMENT.md](./CHECKLIST_DEPLOYMENT.md)** | 20 min | ✅ Perfeccionista | Checklist visual, fácil de seguir |
| 5️⃣ | **[EXAMPLES_COPY_PASTE.md](./EXAMPLES_COPY_PASTE.md)** | 10 min | 💾 Copypaster | 15 ejemplos listos para pegar |

---

## 🎯 FLUJO RECOMENDADO POR EXPERIENCIA

### 👶 Junior DevOps
```
AZURE_DEPLOYMENT_GUIDE.md
    ↓
CHECKLIST_DEPLOYMENT.md
    ↓
bash scripts/setup-interactive.sh
    ↓
✅ LISTO
```

### 🧑‍💻 Mid-level DevOps
```
QUICK_START_AZURE.md (Opción B)
    ↓
bash scripts/deploy-to-azure.sh
    ↓
bash scripts/validate-deployment.sh
    ↓
✅ LISTO
```

### 👨‍💼 Senior DevOps / SRE
```
ARCHITECTURE.md (10 min)
    ↓
EXAMPLES_COPY_PASTE.md (5 min)
    ↓
bash scripts/deploy-to-azure.sh
    ↓
bash scripts/monitor-deployment.sh
    ↓
✅ LISTO + Monitoreo activo
```

---

## 🛠️ SCRIPTS DISPONIBLES

| Script | Propósito | Tiempo | Uso |
|--------|-----------|--------|-----|
| `deploy-to-azure.sh` | Deploy automático completo | 3 min | `bash scripts/deploy-to-azure.sh` |
| `setup-interactive.sh` | Instalador guiado paso a paso | 15 min | `bash scripts/setup-interactive.sh` |
| `validate-deployment.sh` | Validar que todo funciona | 2 min | `bash scripts/validate-deployment.sh` |
| `configure-azure-env.sh` | Configurar variables de entorno | 5 min | `bash scripts/configure-azure-env.sh` |
| `monitor-deployment.sh` | Dashboard en vivo | ∞ | `bash scripts/monitor-deployment.sh` |

---

## 📋 DECISIÓN RÁPIDA: ¿Qué necesito hacer?

```
¿Quiero deployar lo antes posible (5 min)?
  → QUICK_START_AZURE.md (Opción A)

¿Necesito entender qué estoy deployando?
  → ARCHITECTURE.md + AZURE_DEPLOYMENT_GUIDE.md

¿Quiero un checklist visual para no olvidar nada?
  → CHECKLIST_DEPLOYMENT.md

¿Only quiero ejemplos copy-paste?
  → EXAMPLES_COPY_PASTE.md

¿Tengo problemas post-deploy?
  → AZURE_DEPLOYMENT_GUIDE.md → Sección "Paso 6: Troubleshooting"

¿Necesito monitorear en tiempo real?
  → bash scripts/monitor-deployment.sh

¿Necesito validar que todo está bien?
  → bash scripts/validate-deployment.sh
```

---

## ✨ RESUMEN: 5 PASOS PRINCIPALES

```
PASO 1: COMPILAR
├─ mvn clean package -DskipTests
└─ Genera: target/contrataia-backend-1.0.0-SNAPSHOT.jar

PASO 2: CREAR INFRAESTRUCTURA EN AZURE
├─ Resource Group
├─ App Service Plan (B1-B2)
├─ App Service (Linux, Java 21)
└─ PostgreSQL + Redis

PASO 3: CONFIGURAR JAVA EN AZURE PORTAL
├─ Java version: 21
├─ Web server: Tomcat 10.1
├─ Startup Command: (EL CRÍTICO ⭐)
└─ Environment variables: (15-20 vars)

PASO 4: SUBIR JAR
├─ Zip Deploy
├─ Or manual upload via Portal
└─ Reiniciar App Service

PASO 5: VALIDAR
├─ curl /api/actuator/health
├─ curl /api/swagger-ui.html
└─ az webapp log tail (ver logs)
```

---

## 🔥 COMANDO DE UNA LÍNEA (Para apurados)

```bash
# Deploy automático total (construcción + subida + validación)
bash scripts/deploy-to-azure.sh && bash scripts/validate-deployment.sh
```

---

## ⚠️ LOS 3 ERRORES MÁS COMUNES

| Error | Solución |
|-------|----------|
| 🔴 "Hey, Java developers!" | Configura **Startup Command** en Portal |
| 🔴 502 Bad Gateway | Aumenta `SCM_COMMAND_IDLE_TIMEOUT` a 300 |
| 🔴 Connection refused (BD) | Verifica credenciales + firewall PostgreSQL |

---

## 🚨 STARTUP COMMAND (COPIAR DIRECTAMENTE)

```bash
java -Xms512m -Xmx1024m -Dspring.profiles.active=azure -jar /home/site/wwwroot/app.jar --server.port=80
```

**En Azure Portal:**
- App Service → Settings → Configuration → General settings → Startup Command
- Pega el comando anterior
- Click "Save"

---

## 📊 TIMELINE ESTIMADO

```
Tiempo Total: 30-60 minutos (todo incluido)

├─ Setup local (verificar herramientas)        5 min
├─ Compilar con Maven                          5 min
├─ Crear infraestructura (si es nueva)        10 min
├─ Configurar variables en Portal              10 min
├─ Deploy (subir JAR)                          5 min
├─ Esperar a que inicie Spring Boot           20 min ⏳
└─ Validación + Testing                        5 min

OPTIMIZADO (con scripts automáticos):
└─ Total: 15-20 minutos
```

---

## 🎁 BONUS: Lo que NO necesitas hacer

```
❌ NO instalar Tomcat (viene con Spring Boot)
❌ NO crear contenedores Docker (App Service Java 21 nativo es más simple)
❌ NO configurar nginx/reverse proxy (Azure maneja HTTPS automáticamente)
❌ NO crear base de datos manualmente (Flyway hace migraciones automáticas)
❌ NO hacer shell scripts para startup (App Service ejecuta JAR directamente)
❌ NO configurar logs manualmente (Azure transmite a log stream automáticamente)
```

---

## 📞 RECURSOS Y CONTACTOS

### Azure Oficial
- Portal: https://portal.azure.com
- Docs: https://docs.microsoft.com/azure/app-service/
- CLI: https://docs.microsoft.com/cli/azure/

### Spring Boot
- Docs: https://spring.io/
- Starters: https://start.spring.io/

### Community
- Stack Overflow: [tag:azure-app-service] [tag:spring-boot]
- GitHub Issues: https://github.com/Azure/azure-sdk-for-java

---

## 🏆 ÉXITO

```
Si ves esto en el browser:
  https://contrataia-backend.azurewebsites.net/api/actuator/health
  
  {
    "status": "UP",
    "database": {
      "status": "UP"
    },
    "redis": {
      "status": "UP"
    }
  }

¡FELICITACIONES! 🎉 Tu backend está 100% funcional en Azure.
```

---

## 🤔 PREGUNTAS FRECUENTES

**P: ¿Cuánto cuesta?**  
R: B1 ~$13/mes. Con PostgreSQL + Redis: ~$50-80/mes total.

**P: ¿Qué Java version necesito en local?**  
R: 21 (debe coincidir con pom.xml)

**P: ¿Puedo usar Windows en lugar de Linux?**  
R: Sí, pero Linux es más rápido y económico en Azure.

**P: ¿Cómo rollback si algo sale mal?**  
R: Azure mantiene versiones anteriores. Ve a Deployment Center → Redeploy previous version.

**P: ¿Es automático el auto-scale?**  
R: No con B1/B2. Necesitas Premium V2 o App Service Plan con auto-scale rules.

**P: ¿Cómo backup la base de datos?**  
R: Azure maneja backups automáticos diarios de PostgreSQL.

---

## 📌 TL;DR (Too Long; Didn't Read)

```
1. cd /path/to/project
2. bash scripts/deploy-to-azure.sh
3. Espera 30 segundos
4. curl https://contrataia-backend.azurewebsites.net/api/actuator/health
5. VES UP? ✅ ¡Hecho!
   NO VES UP? → Lee: AZURE_DEPLOYMENT_GUIDE.md → Troubleshooting
```

---

**¿Listo? Comienza aquí:** [QUICK_START_AZURE.md →](./QUICK_START_AZURE.md)

O si quieres automatizado:
```bash
bash scripts/deploy-to-azure.sh
```

---

*Última actualización: Junio 26, 2026*  
*Aplicable a: Spring Boot 3.3.5+, Java 21, Azure App Service*

