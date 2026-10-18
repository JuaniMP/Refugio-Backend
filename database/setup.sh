#!/usr/bin/env bash
# Crea la base de datos del refugio desde cero.
# Uso: ./database/setup.sh            (root sin contraseña en localhost)
#      MYSQL_USER=root MYSQL_PWD=clave ./database/setup.sh
# Variables: MYSQL_HOST, MYSQL_PORT, MYSQL_USER, MYSQL_PWD, DB_NAME (BD_Veterinaria),
#            DEMO=0 para no cargar los datos de demostración.
set -euo pipefail
cd "$(dirname "$0")"
DB_NAME="${DB_NAME:-BD_Veterinaria}"
export MYSQL_PWD="${MYSQL_PWD:-}"
mysql_cmd() { mysql -h"${MYSQL_HOST:-127.0.0.1}" -P"${MYSQL_PORT:-3306}" -u"${MYSQL_USER:-root}" "$@"; }

echo "→ Recreando $DB_NAME"
mysql_cmd -e "DROP DATABASE IF EXISTS \`$DB_NAME\`; CREATE DATABASE \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
echo "  · 01_schema.sql"
mysql_cmd "$DB_NAME" < 01_schema.sql
if [ "${DEMO:-1}" = "1" ]; then
  echo "  · 02_datos_demo.sql"
  mysql_cmd "$DB_NAME" < 02_datos_demo.sql
fi
echo "✓ Base de datos lista"
