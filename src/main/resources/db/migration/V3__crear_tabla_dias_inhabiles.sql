CREATE TABLE dias_inhabiles (
    fecha DATE PRIMARY KEY,
    descripcion VARCHAR(255) NOT NULL
);

-- Festivos oficiales de México (LFT art. 74) — AÑO 2026
INSERT INTO dias_inhabiles (fecha, descripcion) VALUES
    ('2026-01-01', 'Año Nuevo'),
    ('2026-02-02', 'Primer lunes de febrero — Día de la Constitución'),
    ('2026-03-16', 'Tercer lunes de marzo — Natalicio de Benito Juárez'),
    ('2026-05-01', 'Día del Trabajo'),
    ('2026-09-16', 'Independencia de México'),
    ('2026-11-16', 'Tercer lunes de noviembre — Revolución Mexicana'),
    ('2026-12-25', 'Navidad');

-- AÑO 2027
INSERT INTO dias_inhabiles (fecha, descripcion) VALUES
    ('2027-01-01', 'Año Nuevo'),
    ('2027-02-01', 'Primer lunes de febrero — Día de la Constitución'),
    ('2027-03-15', 'Tercer lunes de marzo — Natalicio de Benito Juárez'),
    ('2027-05-01', 'Día del Trabajo'),
    ('2027-09-16', 'Independencia de México'),
    ('2027-11-15', 'Tercer lunes de noviembre — Revolución Mexicana'),
    ('2027-12-25', 'Navidad');