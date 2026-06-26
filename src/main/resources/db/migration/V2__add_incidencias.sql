-- Incidencias del Proyecto (seguimiento de problemas durante ejecución)
CREATE TABLE IF NOT EXISTS incidencias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    proyecto_id UUID NOT NULL REFERENCES proyectos(id) ON DELETE CASCADE,
    hito_id UUID REFERENCES hitos(id),
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT NOT NULL,
    tipo VARCHAR(30) NOT NULL DEFAULT 'GENERAL',
    severidad VARCHAR(20) NOT NULL DEFAULT 'MEDIA',
    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTA',
    fecha_reporte TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_resolucion TIMESTAMP,
    resolucion TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_incidencias_proyecto ON incidencias(proyecto_id);
CREATE INDEX IF NOT EXISTS idx_incidencias_estado ON incidencias(estado);
