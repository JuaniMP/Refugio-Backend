-- =============================================================
-- REFUGIO — Datos de demostración
-- Contraseña de todos los usuarios: Demo1234*
-- Las fotos de las mascotas están en Refugio-Frontend/public/images/pets/
-- =============================================================

-- Refugio ------------------------------------------------------
INSERT INTO Refugio (id_refugio, nombre, direccion, responsable) VALUES
(1, 'Bigotes Felices', 'Calle 134 # 9-51, Bogotá', 'Juanita Mejía');

INSERT INTO Telefono_Refugio (id_refugio, telefono, estado) VALUES
(1, '601 555 0101', 'ACTIVO'),
(1, '310 555 0199', 'ACTIVO');

-- Catálogos ----------------------------------------------------
INSERT INTO Especie (id_especie, nombre, estado) VALUES
(1, 'Perro', 'ACTIVO'),
(2, 'Gato', 'ACTIVO'),
(3, 'Roedor', 'ACTIVO');

INSERT INTO Raza (id_raza, id_especie, nombre, estado) VALUES
(1, 1, 'Labrador Retriever', 'ACTIVO'),
(2, 1, 'Golden Retriever', 'ACTIVO'),
(3, 1, 'Beagle', 'ACTIVO'),
(4, 1, 'Mestizo', 'ACTIVO'),
(5, 2, 'Siamés', 'ACTIVO'),
(6, 2, 'Himalayo', 'ACTIVO'),
(7, 2, 'Común europeo', 'ACTIVO'),
(8, 3, 'Ratón doméstico', 'ACTIVO');

INSERT INTO Vacuna_Catalogo (id_vacuna, nombre, estado) VALUES
(1, 'Rabia', 'ACTIVO'),
(2, 'Moquillo canino', 'ACTIVO'),
(3, 'Parvovirus', 'ACTIVO'),
(4, 'Triple felina', 'ACTIVO'),
(5, 'Leucemia felina', 'ACTIVO');

-- Usuarios -----------------------------------------------------
-- El hash replica HashPass.generarHash(usuario, clave): SHA-256(login + clave).
-- El backend lo migra a BCrypt automáticamente en el primer inicio de sesión.
INSERT INTO Usuario (id_usuario, login, password_hash, rol, estado, requires_password_change) VALUES
(1, 'admin@refugio.com',     SHA2(CONCAT('admin@refugio.com',     'Demo1234*'), 256), 'AD', 'ACTIVO', b'0'),
(2, 'vet@refugio.com',       SHA2(CONCAT('vet@refugio.com',       'Demo1234*'), 256), 'V',  'ACTIVO', b'0'),
(3, 'cuidador@refugio.com',  SHA2(CONCAT('cuidador@refugio.com',  'Demo1234*'), 256), 'C',  'ACTIVO', b'0'),
(4, 'laura@correo.com',      SHA2(CONCAT('laura@correo.com',      'Demo1234*'), 256), 'AP', 'ACTIVO', b'0'),
(5, 'andres@correo.com',     SHA2(CONCAT('andres@correo.com',     'Demo1234*'), 256), 'AP', 'ACTIVO', b'0');

INSERT INTO Empleado (id_empleado, id_refugio, id_usuario, cedula, nombre, telefono) VALUES
(1, 1, 2, '1020304050', 'Dra. Camila Rojas', '311 555 0111'),
(2, 1, 3, '1030405060', 'Mateo Gómez', '312 555 0122');

INSERT INTO Veterinario (id_empleado, especialidad, registro_profesional) VALUES
(1, 'Cirugía General', 'MV-2019-0456');

INSERT INTO Cuidador (id_empleado, turno, zona_asignada) VALUES
(2, 'MAÑANA', 'Patio Perros Grandes');

INSERT INTO Adoptante (id_adoptante, id_usuario, nombre, documento, direccion, telefono) VALUES
(1, 4, 'Laura Torres', '52123456', 'Carrera 7 # 72-41, Bogotá', '315 555 0144'),
(2, 5, 'Andrés Pardo', '80123456', 'Calle 80 # 20-10, Bogotá', '316 555 0155');

-- Mascotas -----------------------------------------------------
INSERT INTO Mascota (id_mascota, id_refugio, id_raza, nombre, sexo, edad_meses, img, estado, zona_asignada) VALUES
(1,  1, 1, 'Bella',       'F', 36, '/images/pets/bella.png',       'EN_REFUGIO',          'Patio Perros Grandes'),
(2,  1, 7, 'Bigotes',     'M', 48, '/images/pets/bigotes.png',     'EN_REFUGIO',          'Área de Gatos (Adultos)'),
(3,  1, 3, 'Bruno',       'M', 8,  '/images/pets/bruno.png',       'EN_REFUGIO',          'Patio Perros Pequeños'),
(4,  1, 8, 'Carlitos',    'M', 6,  '/images/pets/carlitos.png',    'EN_REFUGIO',          'Recepción'),
(5,  1, 4, 'Cleo',        'F', 30, '/images/pets/cleo.png',        'EN_PROCESO_ADOPCION', 'Patio Perros Grandes'),
(6,  1, 7, 'Coco',        'M', 24, '/images/pets/coco.png',        'EN_REFUGIO',          'Área de Gatos (Adultos)'),
(7,  1, 1, 'Duque',       'M', 60, '/images/pets/duque.png',       'EN_REFUGIO',          'Patio Perros Grandes'),
(8,  1, 7, 'Freya',       'F', 18, '/images/pets/freya.png',       'EN_REFUGIO',          'Área de Gatos (Adultos)'),
(9,  1, 6, 'Kira',        'F', 14, '/images/pets/kira.png',        'EN_REFUGIO',          'Área de Gatos (Adultos)'),
(10, 1, 8, 'Kiwi',        'F', 5,  '/images/pets/kiwi.png',        'EN_REFUGIO',          'Recepción'),
(11, 1, 5, 'Leo',         'M', 40, '/images/pets/leo.png',         'EN_REFUGIO',          'Área de Gatos (Adultos)'),
(12, 1, 4, 'Limón',       'M', 20, '/images/pets/limon.png',       'EN_REFUGIO',          'Patio Perros Pequeños'),
(13, 1, 2, 'Max',         'M', 48, '/images/pets/max.png',         'ADOPTADA',            NULL),
(14, 1, 2, 'Maximiliano', 'M', 72, '/images/pets/maximiliano.png', 'EN_REFUGIO',          'Patio Perros Grandes'),
(15, 1, 4, 'Mister',      'M', 54, '/images/pets/mister.png',      'EN_REFUGIO',          'Patio Perros Grandes'),
(16, 1, 4, 'Pipo',        'M', 28, '/images/pets/pipo.png',        'EN_REFUGIO',          'Patio Perros Pequeños'),
(17, 1, 5, 'Princesa',    'F', 26, '/images/pets/princesa.png',    'EN_PROCESO_ADOPCION', 'Área de Gatos (Adultos)'),
(18, 1, 4, 'Rami',        'M', 34, '/images/pets/rami.png',        'EN_REFUGIO',          'Patio Perros Grandes'),
(19, 1, 2, 'Rocky',       'M', 44, '/images/pets/rocky.png',       'EN_REFUGIO',          'Patio Perros Grandes'),
(20, 1, 7, 'Sombra',      'F', 22, '/images/pets/sombra.png',      'EN_REFUGIO',          'Área de Gatos (Adultos)'),
(21, 1, 5, 'Tigre',       'M', 16, '/images/pets/tigre.png',       'EN_REFUGIO',          'Área de Gatos (Adultos)'),
(22, 1, 8, 'Zoe',         'F', 4,  '/images/pets/zoe.png',         'EN_REFUGIO',          'Recepción');

-- Salud --------------------------------------------------------
INSERT INTO Historial_Medico (id_historial, id_mascota, notas) VALUES
(1, 1, 'Ingresó en buen estado. Esterilizada.'),
(2, 3, 'Cachorro activo; pendiente refuerzo de vacunas.'),
(3, 5, 'Recuperada de dermatitis leve.'),
(4, 11, 'Control dental anual.');

INSERT INTO Aplicacion_Vacuna (id_historial, id_vacuna, id_empleado, fecha, observaciones) VALUES
(1, 1, 1, '2026-08-12', 'Sin reacciones'),
(1, 2, 1, '2026-08-12', NULL),
(2, 3, 1, '2026-09-30', 'Primera dosis'),
(4, 4, 1, '2026-07-05', NULL);

INSERT INTO Diagnostico (id_historial, id_empleado, fecha, estado_salud, diagnostico, tratamiento, observaciones) VALUES
(3, 1, '2026-09-15 10:30:00', 'ESTABLE', 'Dermatitis alérgica leve', 'Baños medicados durante 2 semanas', 'Revisar en 15 días'),
(2, 1, '2026-10-01 09:00:00', 'SANO', 'Chequeo general de ingreso', NULL, 'Peso adecuado para su edad');

INSERT INTO Asignacion_Cuidador (id_empleado, id_mascota, fecha_inicio, fecha_fin, comentarios) VALUES
(2, 1, '2026-09-01', NULL, 'Paseo dos veces al día'),
(2, 3, '2026-09-01', NULL, 'Socialización con otros perros'),
(2, 7, '2026-09-15', NULL, NULL);

-- Adopciones ---------------------------------------------------
INSERT INTO Solicitud_Adopcion (id_adoptante, id_mascota, estado, fecha_solicitud, observaciones) VALUES
(1, 5,  'PENDIENTE', '2026-10-10 15:20:00', 'Tengo casa con patio y experiencia con perros.'),
(2, 17, 'PENDIENTE', '2026-10-12 11:05:00', 'Vivo en apartamento, trabajo desde casa.'),
(1, 13, 'APROBADA',  '2026-08-02 09:40:00', 'Primera adopción en el refugio.');

INSERT INTO Adopcion (id_adoptante, id_mascota, fecha_adopcion) VALUES
(1, 13, '2026-08-20');
