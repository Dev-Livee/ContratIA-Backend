# 🚀 DESPLIEGA TU SPRING BOOT EN AZURE APP SERVICE EN 5 MINUTOS

## Tu Backend 100% Funcional sin "Hey Java Developers!"

---

## 📌 EMPEZAR AQUÍ (3 opciones)

### ✅ OPCIÓN 1: Deployment Totalmente Automático (3-5 minutos)

```bash
bash scripts/deploy-to-azure.sh
```

**Qué hace:**
- ✅ Compila con Maven
- ✅ Genera el JAR
- ✅ Sube a Azure
- ✅ Reinicia la app
- ✅ Valida que funciona

**Dificultad:** 🟢 Muy Fácil

---

### ✅ OPCIÓN 2: Instalador Guiado Paso a Paso (15-20 minutos)

```bash
bash scripts/setup-interactive.sh
```

**Qué hace:**
- ✅ Te pregunta todos los datos
- ✅ Te guía por Azure Portal
- ✅ Valida configuración
- ✅ Hace deployment
- ✅ Verifica resultados

**Dificultad:** 🟡 Fácil

---

### ✅ OPCIÓN 3: Manual con Máximo Control (25-30 minutos)

1. Lee: `EXAMPLES_COPY_PASTE.md` (15 ejemplos listos)
2. Sigue: `CHECKLIST_DEPLOYMENT.md` (paso a paso visual)
3. Ejecuta comandos manualmente

**Dificultad:** 🔴 Intermedia

---

## 📚 DOCUMENTOS PRINCIPALES

| Documento | Lectura | Propósito |
|-----------|---------|----------|
| **START_HERE.md** | 2 min | 👈 Estás aquí |
| **README_DEPLOYMENT.md** | 5 min | Índice maestro y navegación |
| **QUICK_START_AZURE.md** | 5 min | Inicio rápido (para apurados) |
| **AZURE_DEPLOYMENT_GUIDE.md** | 45 min | Guía COMPLETA y detallada |
| **ARCHITECTURE.md** | 15 min | Diagramas y flujos |
| **CHECKLIST_DEPLOYMENT.md** | 20 min | Checklist visual paso a paso |
| **EXAMPLES_COPY_PASTE.md** | 10 min | 15 ejemplos listos para copiar |

---

## 🔴 STARTUP COMMAND (Copia a Azure Portal)

```bash
java -Xms512m -Xmx1024m -Dspring.profiles.active=azure -jar /home/site/wwwroot/app.jar --server.port=80
```

**Ubicación en Portal:**
- App Service → Settings → Configuration → General settings → Startup Command

---

## ⏱️ TIMELINE

- **Setup local:** 5 minutos
- **Compilar:** 5 minutos
- **Deploy:** 3-5 minutos
- **Spring Boot inicia:** 20-30 minutos ⏳
- **Validar:** 2 minutos

**TOTAL: 15-30 minutos**

---

## 🎯 RECOMENDACIÓN POR PERFIL

**👶 Junior DevOps:**
1. Lee: `ARCHITECTURE.md`
2. Lee: `AZURE_DEPLOYMENT_GUIDE.md`
3. Usa: `CHECKLIST_DEPLOYMENT.md`
4. Ejecuta: `bash scripts/setup-interactive.sh`

**🧑‍💻 Mid-level DevOps:**
1. Lee: `QUICK_START_AZURE.md`
2. Ejecuta: `bash scripts/deploy-to-azure.sh`

**👨‍💼 Senior DevOps:**
1. Lee: `ARCHITECTURE.md` (10 min)
2. Ejecuta: `bash scripts/deploy-to-azure.sh`
3. Monitorea: `bash scripts/monitor-deployment.sh`

---

## ⚠️ TOP 3 ERRORES COMUNES

| Error | Solución |
|-------|----------|
| 🔴 "Hey, Java developers!" | Configura **Startup Command** en Portal |
| 🔴 502 Bad Gateway | Aumenta `SCM_COMMAND_IDLE_TIMEOUT = 300` |
| 🔴 Connection refused (BD) | Verifica credenciales y firewall PostgreSQL |

---

## 🚀 LISTOS? EJECUTEN:

```bash
cd /Users/maycolrojas/Documents/GitHub/ContratIA-MVP/ContratIA-Backend
bash scripts/deploy-to-azure.sh
```

---

## ✅ ÉXITO = VER ESTO:

```
URL: https://contrataia-backend.azurewebsites.net/api/actuator/health

Respuesta:
{
  "status": "UP",
  "database": {"status": "UP"},
  "redis": {"status": "UP"}
}

¡¡¡ FELICITACIONES !!! 🎉
Tu backend está 100% funcional en Azure.
```

---

**Siguiente paso:** Lee uno de los documentos principales arriba según tu perfil.

**¿Apurado?** Ejecuta `bash scripts/deploy-to-azure.sh` y vuelve en 5 minutos.

