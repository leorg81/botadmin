#!/bin/bash
# Script para migrar código de org.vaadin.example -> gub.rionegro.syslr
# Manteniendo la estructura de subpaquetes

OLD_BASE="src/main/java/org/vaadin/example"
NEW_BASE="src/main/java/gub/rionegro/syslr"

echo "Creando nueva estructura de paquetes en: $NEW_BASE"
mkdir -p "$NEW_BASE"

echo "Moviendo archivos preservando estructura..."
rsync -av --remove-source-files "$OLD_BASE"/ "$NEW_BASE"/

echo "Eliminando directorios viejos..."
find src/main/java/org -type d -empty -delete

echo "Actualizando declaraciones de paquetes..."
# Reemplazar package y también imports que usen org.vaadin.example.*
find "$NEW_BASE" -type f -name "*.java" -exec sed -i \
    -e 's/package org\.vaadin\.example/package gub.rionegro.syslr/' \
    -e 's/import org\.vaadin\.example/import gub.rionegro.syslr/' {} \;

echo "Migración completada ✅"

