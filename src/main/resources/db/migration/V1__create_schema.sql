-- =============================================================
-- ContrataIA Perú - Esquema Base
-- =============================================================

-- Tabla base de usuarios (herencia JOINED)
CREATE TABLE IF NOT EXISTS usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo_usuario VARCHAR(30) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true,
    email_verificado BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Entidades Públicas / Municipalidades
CREATE TABLE IF NOT EXISTS entidades_publicas (
    id UUID PRIMARY KEY REFERENCES usuarios(id) ON DELETE CASCADE,
    ruc VARCHAR(11) UNIQUE NOT NULL,
    razon_social VARCHAR(255) NOT NULL,
    tipo VARCHAR(100),
    distrito VARCHAR(100),
    provincia VARCHAR(100),
    region VARCHAR(100),
    direccion TEXT,
    telefono VARCHAR(20),
    representante_legal VARCHAR(255),
    dni_representante VARCHAR(8),
    cargo VARCHAR(100)
);

-- Empresas Privadas
CREATE TABLE IF NOT EXISTS empresas (
    id UUID PRIMARY KEY REFERENCES usuarios(id) ON DELETE CASCADE,
    ruc VARCHAR(11) UNIQUE NOT NULL,
    razon_social VARCHAR(255) NOT NULL,
    estado_sunat VARCHAR(50),
    condicion VARCHAR(50),
    sector VARCHAR(100),
    direccion TEXT,
    telefono VARCHAR(20),
    sitio_web VARCHAR(255),
    descripcion TEXT,
    representante_legal VARCHAR(255),
    dni_representante VARCHAR(8),
    fecha_inscripcion DATE
);

-- Experiencia de Empresas
CREATE TABLE IF NOT EXISTS experiencias_empresa (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    descripcion TEXT,
    entidad_contratante VARCHAR(255),
    monto DECIMAL(15, 2),
    rubro VARCHAR(100),
    region VARCHAR(100),
    fecha_inicio DATE,
    fecha_fin DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Certificaciones de Empresas
CREATE TABLE IF NOT EXISTS certificaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    nombre VARCHAR(255) NOT NULL,
    entidad_emisora VARCHAR(255),
    fecha_emision DATE,
    fecha_vencimiento DATE,
    documento_url TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Documentos Técnicos de Empresas
CREATE TABLE IF NOT EXISTS documentos_empresa (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    tipo VARCHAR(100) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    url TEXT NOT NULL,
    fecha_subida TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Proyectos de Contratación
CREATE TABLE IF NOT EXISTS proyectos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entidad_publica_id UUID NOT NULL REFERENCES entidades_publicas(id),
    empresa_adjudicada_id UUID REFERENCES empresas(id),
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    presupuesto DECIMAL(15, 2),
    rubro VARCHAR(100),
    distrito VARCHAR(100),
    provincia VARCHAR(100),
    region VARCHAR(100),
    direccion TEXT,
    fecha_inicio_prevista DATE,
    fecha_fin_prevista DATE,
    plazo_meses INTEGER,
    requisitos TEXT,
    estado VARCHAR(30) NOT NULL DEFAULT 'BORRADOR',
    codigo_unico VARCHAR(20) UNIQUE,
    avance_fisico DECIMAL(5, 2) DEFAULT 0,
    avance_financiero DECIMAL(5, 2) DEFAULT 0,
    fecha_adjudicacion TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Candidatos Proveedores por Proyecto
CREATE TABLE IF NOT EXISTS candidatos_proveedor (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    proyecto_id UUID NOT NULL REFERENCES proyectos(id) ON DELETE CASCADE,
    empresa_id UUID REFERENCES empresas(id),
    ruc VARCHAR(11) NOT NULL,
    razon_social VARCHAR(255),
    snapshot_latinfo JSONB,
    estado VARCHAR(30) NOT NULL DEFAULT 'CANDIDATO',
    fecha_agregado TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (proyecto_id, ruc)
);

-- Hitos del Proyecto
CREATE TABLE IF NOT EXISTS hitos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    proyecto_id UUID NOT NULL REFERENCES proyectos(id) ON DELETE CASCADE,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    fecha_prevista DATE,
    fecha_real DATE,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    orden INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Registro de Avances
CREATE TABLE IF NOT EXISTS avances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    proyecto_id UUID NOT NULL REFERENCES proyectos(id) ON DELETE CASCADE,
    avance_fisico DECIMAL(5, 2) NOT NULL,
    avance_financiero DECIMAL(5, 2),
    descripcion TEXT,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Evidencias (Fotos y Archivos)
CREATE TABLE IF NOT EXISTS evidencias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    proyecto_id UUID NOT NULL REFERENCES proyectos(id) ON DELETE CASCADE,
    hito_id UUID REFERENCES hitos(id),
    tipo VARCHAR(30) NOT NULL,
    nombre VARCHAR(255),
    url TEXT NOT NULL,
    descripcion TEXT,
    es_publico BOOLEAN NOT NULL DEFAULT true,
    fecha_subida TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Documentos del Proyecto
CREATE TABLE IF NOT EXISTS documentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    proyecto_id UUID NOT NULL REFERENCES proyectos(id) ON DELETE CASCADE,
    tipo VARCHAR(50) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    url TEXT NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    es_publico BOOLEAN NOT NULL DEFAULT false,
    fecha_subida TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Refresh Tokens JWT
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    token VARCHAR(512) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revocado BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- OTP Tokens para verificación
CREATE TABLE IF NOT EXISTS otp_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    codigo VARCHAR(6) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================
-- Índices
-- =============================================================
CREATE INDEX IF NOT EXISTS idx_proyectos_distrito ON proyectos(distrito);
CREATE INDEX IF NOT EXISTS idx_proyectos_estado ON proyectos(estado);
CREATE INDEX IF NOT EXISTS idx_proyectos_region ON proyectos(region);
CREATE INDEX IF NOT EXISTS idx_proyectos_entidad ON proyectos(entidad_publica_id);
CREATE INDEX IF NOT EXISTS idx_proyectos_codigo ON proyectos(codigo_unico);
CREATE INDEX IF NOT EXISTS idx_candidatos_proyecto ON candidatos_proveedor(proyecto_id);
CREATE INDEX IF NOT EXISTS idx_candidatos_ruc ON candidatos_proveedor(ruc);
CREATE INDEX IF NOT EXISTS idx_hitos_proyecto ON hitos(proyecto_id);
CREATE INDEX IF NOT EXISTS idx_avances_proyecto ON avances(proyecto_id);
CREATE INDEX IF NOT EXISTS idx_evidencias_proyecto ON evidencias(proyecto_id);
CREATE INDEX IF NOT EXISTS idx_documentos_proyecto ON documentos(proyecto_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_otp_email ON otp_tokens(email);
CREATE INDEX IF NOT EXISTS idx_exp_empresa ON experiencias_empresa(empresa_id);
