-- =============================================================
-- REFUGIO / BD_Veterinaria — Esquema MySQL
-- Generado a partir de las entidades JPA (src/main/java/.../entity)
-- y revisado. Ejecutar sobre una base vacía (ver database/setup.sh).
-- =============================================================

SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE `Adopcion` (
  `fecha_adopcion` date NOT NULL,
  `id_adopcion` int NOT NULL AUTO_INCREMENT,
  `id_adoptante` int NOT NULL,
  `id_mascota` int NOT NULL,
  PRIMARY KEY (`id_adopcion`),
  UNIQUE KEY `UKc220v0e74h6qyvb7e6i08253g` (`id_mascota`),
  KEY `FKj6g8tmfnoo1s94e2u2ctklr5u` (`id_adoptante`),
  CONSTRAINT `FKj6g8tmfnoo1s94e2u2ctklr5u` FOREIGN KEY (`id_adoptante`) REFERENCES `Adoptante` (`id_adoptante`),
  CONSTRAINT `FKmq9fpxxg9wsa0krskpb482jyi` FOREIGN KEY (`id_mascota`) REFERENCES `Mascota` (`id_mascota`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Adoptante` (
  `id_adoptante` int NOT NULL AUTO_INCREMENT,
  `id_usuario` int DEFAULT NULL,
  `documento` varchar(30) DEFAULT NULL,
  `telefono` varchar(30) DEFAULT NULL,
  `nombre` varchar(120) NOT NULL,
  `direccion` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id_adoptante`),
  UNIQUE KEY `UKt3oq39pv0fmfo3t9bt4rq7v6w` (`id_usuario`),
  UNIQUE KEY `UKna5768teprcfmdkbalit28fkl` (`documento`),
  CONSTRAINT `FKaplvq7a9210verlqc7lrs5s1` FOREIGN KEY (`id_usuario`) REFERENCES `Usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Aplicacion_Vacuna` (
  `fecha` date NOT NULL,
  `id_aplicacion` int NOT NULL AUTO_INCREMENT,
  `id_empleado` int DEFAULT NULL,
  `id_historial` int NOT NULL,
  `id_vacuna` int NOT NULL,
  `observaciones` text,
  PRIMARY KEY (`id_aplicacion`),
  KEY `FKl3packhys7vidc5i6gx5n2qgv` (`id_empleado`),
  KEY `FK31e35tj39k3nthlhpcti1toe2` (`id_historial`),
  KEY `FK9lyvluegoagwswv3cqujrx0jv` (`id_vacuna`),
  CONSTRAINT `FK31e35tj39k3nthlhpcti1toe2` FOREIGN KEY (`id_historial`) REFERENCES `Historial_Medico` (`id_historial`),
  CONSTRAINT `FK9lyvluegoagwswv3cqujrx0jv` FOREIGN KEY (`id_vacuna`) REFERENCES `Vacuna_Catalogo` (`id_vacuna`),
  CONSTRAINT `FKl3packhys7vidc5i6gx5n2qgv` FOREIGN KEY (`id_empleado`) REFERENCES `Empleado` (`id_empleado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Asignacion_Cuidador` (
  `fecha_fin` date DEFAULT NULL,
  `fecha_inicio` date NOT NULL,
  `id_empleado` int NOT NULL,
  `id_mascota` int NOT NULL,
  `comentarios` text,
  PRIMARY KEY (`fecha_inicio`,`id_empleado`,`id_mascota`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Auditoria` (
  `id_auditoria` int NOT NULL AUTO_INCREMENT,
  `id_usuario` int DEFAULT NULL,
  `creado_en` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `id_registro` varchar(64) DEFAULT NULL,
  `tabla_afectada` varchar(64) NOT NULL,
  `comentario_auditoria` varchar(255) DEFAULT NULL,
  `accion` enum('DELETE','INSERT','LOGIN','LOGOUT','UPDATE') NOT NULL,
  PRIMARY KEY (`id_auditoria`),
  KEY `FK6tawa2acwbr8tr0pjvl6rqwve` (`id_usuario`),
  CONSTRAINT `FK6tawa2acwbr8tr0pjvl6rqwve` FOREIGN KEY (`id_usuario`) REFERENCES `Usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Cuidador` (
  `id_empleado` int NOT NULL,
  `zona_asignada` varchar(100) DEFAULT NULL,
  `turno` enum('MAÑANA','NOCHE','TARDE') DEFAULT NULL,
  PRIMARY KEY (`id_empleado`),
  CONSTRAINT `FKg2ilsf50pvcxt9l5t3xccdhve` FOREIGN KEY (`id_empleado`) REFERENCES `Empleado` (`id_empleado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Diagnostico` (
  `id_diagnostico` int NOT NULL AUTO_INCREMENT,
  `id_empleado` int NOT NULL,
  `id_historial` int NOT NULL,
  `fecha` datetime(6) DEFAULT NULL,
  `estado_salud` varchar(50) DEFAULT NULL,
  `diagnostico` text NOT NULL,
  `observaciones` text,
  `tratamiento` text,
  PRIMARY KEY (`id_diagnostico`),
  KEY `FKs097jst0rs0dxgv21233863ik` (`id_empleado`),
  KEY `FKq88r3fq9tyfwmxvp06vr8aeam` (`id_historial`),
  CONSTRAINT `FKq88r3fq9tyfwmxvp06vr8aeam` FOREIGN KEY (`id_historial`) REFERENCES `Historial_Medico` (`id_historial`),
  CONSTRAINT `FKs097jst0rs0dxgv21233863ik` FOREIGN KEY (`id_empleado`) REFERENCES `Empleado` (`id_empleado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Empleado` (
  `id_empleado` int NOT NULL AUTO_INCREMENT,
  `id_refugio` int NOT NULL,
  `id_usuario` int DEFAULT NULL,
  `cedula` varchar(30) DEFAULT NULL,
  `telefono` varchar(30) DEFAULT NULL,
  `nombre` varchar(120) DEFAULT NULL,
  PRIMARY KEY (`id_empleado`),
  UNIQUE KEY `UKd15k2amedcw6l5h5r43u9qmvg` (`id_usuario`),
  UNIQUE KEY `UK5oc42sxuu4ct8n3dut6awmllu` (`cedula`),
  KEY `FKmw6cunwne0okbox2lb23yf9ow` (`id_refugio`),
  CONSTRAINT `FKlib4y63jwvbcl4v2n88q2r586` FOREIGN KEY (`id_usuario`) REFERENCES `Usuario` (`id_usuario`),
  CONSTRAINT `FKmw6cunwne0okbox2lb23yf9ow` FOREIGN KEY (`id_refugio`) REFERENCES `Refugio` (`id_refugio`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Especie` (
  `id_especie` int NOT NULL AUTO_INCREMENT,
  `estado` varchar(10) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  PRIMARY KEY (`id_especie`),
  UNIQUE KEY `UKgf5fb3cbh9glcf7uxlugas41h` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Historial_Medico` (
  `id_historial` int NOT NULL AUTO_INCREMENT,
  `id_mascota` int NOT NULL,
  `notas` text,
  PRIMARY KEY (`id_historial`),
  UNIQUE KEY `UK6c23kaiqc4o4ky4ydssab1xt1` (`id_mascota`),
  CONSTRAINT `FKcc76sosr8iyjbt0oandj2yg5n` FOREIGN KEY (`id_mascota`) REFERENCES `Mascota` (`id_mascota`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Mascota` (
  `edad_meses` int DEFAULT NULL,
  `id_mascota` int NOT NULL AUTO_INCREMENT,
  `id_raza` int NOT NULL,
  `id_refugio` int NOT NULL,
  `nombre` varchar(80) NOT NULL,
  `img` varchar(100) DEFAULT NULL,
  `zona_asignada` varchar(100) DEFAULT NULL,
  `estado` enum('ADOPTADA','EN_PROCESO_ADOPCION','EN_REFUGIO','OTRO') DEFAULT NULL,
  `sexo` enum('DESCONOCIDO','F','M') DEFAULT NULL,
  PRIMARY KEY (`id_mascota`),
  KEY `FKeou0e18sote3sua57uovfyt5r` (`id_raza`),
  KEY `FKcqe9qeguypnqpj1w8jwipb36f` (`id_refugio`),
  CONSTRAINT `FKcqe9qeguypnqpj1w8jwipb36f` FOREIGN KEY (`id_refugio`) REFERENCES `Refugio` (`id_refugio`),
  CONSTRAINT `FKeou0e18sote3sua57uovfyt5r` FOREIGN KEY (`id_raza`) REFERENCES `Raza` (`id_raza`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Raza` (
  `id_especie` int NOT NULL,
  `id_raza` int NOT NULL AUTO_INCREMENT,
  `estado` varchar(10) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  PRIMARY KEY (`id_raza`),
  KEY `FK6thtxeuck5owa7s2yc8skgpgw` (`id_especie`),
  CONSTRAINT `FK6thtxeuck5owa7s2yc8skgpgw` FOREIGN KEY (`id_especie`) REFERENCES `Especie` (`id_especie`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Refugio` (
  `id_refugio` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) DEFAULT NULL,
  `responsable` varchar(100) DEFAULT NULL,
  `direccion` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id_refugio`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Solicitud_Adopcion` (
  `id_adoptante` int NOT NULL,
  `id_mascota` int NOT NULL,
  `id_solicitud` int NOT NULL AUTO_INCREMENT,
  `fecha_solicitud` datetime(6) DEFAULT NULL,
  `observaciones` text,
  `estado` enum('APROBADA','CANCELADA','PENDIENTE','RECHAZADA') DEFAULT NULL,
  PRIMARY KEY (`id_solicitud`),
  KEY `FK96gwsp21d0gf7eoiw1t24r2cq` (`id_adoptante`),
  KEY `FKnswvv52bv50v8a291t38ya2ey` (`id_mascota`),
  CONSTRAINT `FK96gwsp21d0gf7eoiw1t24r2cq` FOREIGN KEY (`id_adoptante`) REFERENCES `Adoptante` (`id_adoptante`),
  CONSTRAINT `FKnswvv52bv50v8a291t38ya2ey` FOREIGN KEY (`id_mascota`) REFERENCES `Mascota` (`id_mascota`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Telefono_Refugio` (
  `id_refugio` int NOT NULL,
  `estado` varchar(10) NOT NULL,
  `telefono` varchar(30) NOT NULL,
  PRIMARY KEY (`id_refugio`,`telefono`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Usuario` (
  `id_usuario` int NOT NULL AUTO_INCREMENT,
  `requires_password_change` bit(1) NOT NULL,
  `creado_en` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `verification_code_expires` datetime(6) DEFAULT NULL,
  `login` varchar(60) NOT NULL,
  `verification_code` varchar(64) DEFAULT NULL,
  `password_hash` varchar(255) NOT NULL,
  `estado` enum('ACTIVO','INACTIVO') NOT NULL,
  `rol` enum('AD','AP','C','V') NOT NULL,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `UKg1orfqvgih1w8s3vyg15fq2b8` (`login`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Vacuna_Catalogo` (
  `id_vacuna` int NOT NULL AUTO_INCREMENT,
  `estado` varchar(10) NOT NULL,
  `nombre` varchar(80) NOT NULL,
  PRIMARY KEY (`id_vacuna`),
  UNIQUE KEY `UKlmvarbllw4o5o2b48rgnt9ltb` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Veterinario` (
  `id_empleado` int NOT NULL,
  `registro_profesional` varchar(50) DEFAULT NULL,
  `especialidad` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id_empleado`),
  CONSTRAINT `FK9wvo9l1ns2i2k33hohx34egb9` FOREIGN KEY (`id_empleado`) REFERENCES `Empleado` (`id_empleado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
