#!/bin/bash

# Working directory: cliente-servidor-java/src

# Definir directorios de trabajo
SOURCE_DIR="services/java"
JAR_DIR="services/jar"
PACKAGE_NAME="services.java"

# Limpiar el directorio jar, si existe
echo "Limpiando directorio JAR..."
rm -rf "$JAR_DIR"/*.jar

# Crear el directorio jar si no existe
mkdir -p "$JAR_DIR"

# Compilar los archivos .java, excluyendo Service.java
echo "Compilando archivos Java..."

# Buscar todos los archivos .java en el directorio java, excepto Service.java
find "$SOURCE_DIR" -name "*.java" ! -name "Service.java" > sources.txt

# Compilar los archivos encontrados
javac -d "$JAR_DIR" @sources.txt

# Crear un JAR por cada servicio
for service in $(find "$SOURCE_DIR" -name "*.java" ! -name "Service.java"); do
    service_name=$(basename "$service" .java)
    echo "Creando JAR para $service_name..."

    # Crear el archivo JAR para cada servicio, incluyendo solo su .class
    mkdir -p "$JAR_DIR/$service_name"
    cp "$JAR_DIR/$SOURCE_DIR/$service_name.class" "$JAR_DIR/$service_name"  # Copiar el .class generado
    cp "$JAR_DIR/$SOURCE_DIR/Service.class" "$JAR_DIR/$service_name"  # Copiar el .class generado

    # Crear un archivo MANIFEST.MF con la clase completa del servicio
    mkdir -p "$JAR_DIR/$service_name/META-INF"
    echo "Manifest-Version: 1.0" > "$JAR_DIR/$service_name/META-INF/MANIFEST.MF"
    echo "Service-Class: $PACKAGE_NAME.$service_name" >> "$JAR_DIR/$service_name/META-INF/MANIFEST.MF"


    # Crear el JAR del servicio
    jar cfm "$JAR_DIR/$service_name.jar" "$JAR_DIR/$service_name/META-INF/MANIFEST.MF" -C "$JAR_DIR/$service_name" .

    # Limpiar los archivos de clase temporales para cada servicio
    rm -rf "$JAR_DIR/$service_name"
done

# Limpiar los directorios temporales generados en jar
echo "Limpiando directorios temporales..."
rm -rf "$JAR_DIR"/services

# Limpiar archivo temporal de fuentes
rm -f sources.txt

echo "Compilación y creación del JAR completada."
